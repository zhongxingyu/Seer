 package ch.unibe.scg.cells.hadoop;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.io.BaseEncoding.base64;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
 import org.apache.hadoop.hbase.mapreduce.TableMapper;
 import org.apache.hadoop.hbase.mapreduce.TableReducer;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableComparable;
 import org.apache.hadoop.io.WritableComparator;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.partition.BinaryPartitioner;
 
 import ch.unibe.scg.cells.Cell;
 import ch.unibe.scg.cells.CellSink;
 import ch.unibe.scg.cells.Codec;
 import ch.unibe.scg.cells.Codecs;
 import ch.unibe.scg.cells.Mapper;
 import ch.unibe.scg.cells.OfflineMapper;
 import ch.unibe.scg.cells.OneShotIterable;
 import ch.unibe.scg.cells.Pipeline;
 import ch.unibe.scg.cells.Sink;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.primitives.Bytes;
 import com.google.common.primitives.Ints;
 import com.google.protobuf.ByteString;
 
 /** The Hadoop version of a {@link Pipeline}. */
 public class HadoopPipeline<IN, EFF> implements Pipeline<IN, EFF> {
 	final private static byte[] fam = ByteString.copyFromUtf8("f").toByteArray();
 
 	final private Configuration baseConfiguration;
 	final private MapConfigurer<IN> firstMapConfigurer;
 	final private Table<EFF> efflux;
 	final private TableAdmin admin;
 
 	HadoopPipeline(Configuration baseConfiguration, MapConfigurer<IN> firstMapConfigurer, Table<EFF> efflux,
 			TableAdmin admin) {
 		this.baseConfiguration = baseConfiguration;
 		this.firstMapConfigurer = firstMapConfigurer;
 		this.efflux = efflux;
 		this.admin = admin;
 	}
 
 	/** @return a Pipeline that will run map/reduce jobs in the cluster. */
 	public static <IN, EFF> HadoopPipeline<IN, EFF> fromTableToTable(Configuration configuration, ByteString family,
 			Table<IN> influx, Table<EFF> efflux) {
 		return new HadoopPipeline<>(configuration,
 				new TableInputConfigurer<>(influx, family),
 				efflux,
 				new TableAdmin(configuration, family, HadoopModule.INDEX_FAMILY, new HTableFactory(configuration)));
 	}
 
 	@Override
 	public MappablePipeline<IN, EFF> influx(Codec<IN> c) {
 		return new HadoopMappablePipeline<>(firstMapConfigurer, c);
 	}
 
 	/** Configure the map part of the job to run from a table, or from HDFS, as the case may be. */
 	private interface MapConfigurer<MAP_IN> extends Closeable {
 		<MAP_OUT> void configure(Job job, Codec<MAP_IN> mapSrcCodec, Mapper<MAP_IN, MAP_OUT> map,
 				Codec<MAP_OUT> outCodec) throws IOException;
 	}
 
 	/** MapConfigurer for the case of table input. */
 	static class TableInputConfigurer<MAP_IN> implements MapConfigurer<MAP_IN> {
 		final private Table<MAP_IN> src;
 		final private ByteString family;
 
 		TableInputConfigurer(Table<MAP_IN> src, ByteString family) {
 			this.src = src;
 			this.family = family;
 		}
 
 		@Override
 		public <MAP_OUT> void configure(Job job, Codec<MAP_IN> mapSrcCodec,
 				Mapper<MAP_IN, MAP_OUT> map, Codec<MAP_OUT> outCodec) throws IOException {
 			HadoopTableMapper<MAP_IN, MAP_OUT> hMapper = new HadoopTableMapper<>(map, mapSrcCodec, outCodec, family);
 			writeObjectToConf(job.getConfiguration(), hMapper);
 
 			Scan scan = HBaseCellSource.makeScan();
 			scan.addFamily(family.toByteArray());
 
 			TableMapReduceUtil.initTableMapperJob(src.getTableName(), // input table
 					scan, // Scan instance to control CF and attribute selection
 					DecoratorHadoopTableMapper.class, // mapper class
 					ImmutableBytesWritable.class, // mapper output key
 					ImmutableBytesWritable.class, // mapper output value
 					job);
 		}
 
 		@Override
 		public void close() throws IOException {
 			src.close();
 		}
 	}
 
 	/** MapConfigurer for the case of HDFS input. */
 	class HadoopInputConfigurer<MAP_IN> implements MapConfigurer<MAP_IN> {
 		final private Class<? extends WritableComparable<?>> outputKey;
 		final private Class<? extends Writable> outputValue;
 		final private Class<? extends FileInputFormat<ImmutableBytesWritable, ImmutableBytesWritable>> inputFormat;
 		final private Path inputPath;
 
 		HadoopInputConfigurer(Class<? extends WritableComparable<?>> outputKey,
 				Class<? extends Writable> outputValue,
 				Class<? extends FileInputFormat<ImmutableBytesWritable, ImmutableBytesWritable>> inputFormat,
 				Path inputPath) {
 			this.outputKey = outputKey;
 			this.outputValue = outputValue;
 			this.inputFormat = inputFormat;
 			this.inputPath = inputPath;
 		}
 
 		@Override
 		public <MAP_OUT> void configure(Job job, Codec<MAP_IN> mapSrcCodec, Mapper<MAP_IN, MAP_OUT> map,
 				Codec<MAP_OUT> outCodec) throws IOException {
 			job.setInputFormatClass(inputFormat);
 			job.setMapperClass(HadoopMapper.class);
 
 			job.setMapOutputKeyClass(outputKey);
 			job.setMapOutputValueClass(outputValue);
 			FileInputFormat.addInputPath(job, inputPath);
 		}
 
 		@Override
 		public void close() throws IOException {
 			// Nothing to do.
 		}
 	}
 
 	/** Hadoop mapper for jobs that don't read from a table. For table reading jobs, see {@link HadoopTableMapper} */
 	static class HadoopMapper<KEYIN extends Writable, VALUEIN extends Writable, I, E>
 			extends org.apache.hadoop.mapreduce.Mapper<KEYIN, VALUEIN, ImmutableBytesWritable, ImmutableBytesWritable> {
 		final private Mapper<I, E> underlying;
 		final private Codec<I> inputCodec;
 		final private Codec<E> outputCodec;
 
 		HadoopMapper(Mapper<I, E> underlying, Codec<I> inputCodec, Codec<E> outputCodec) {
 			this.underlying = underlying;
 			this.inputCodec = inputCodec;
 			this.outputCodec = outputCodec;
 		}
 
 		@Override
 		protected void map(KEYIN key, VALUEIN value, Context context) throws IOException, InterruptedException {
 			ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
 			key.write(new DataOutputStream(keyStream));
 			ByteString cellKey = ByteString.copyFrom(keyStream.toByteArray());
 
 			ByteArrayOutputStream valueStream = new ByteArrayOutputStream();
 			key.write(new DataOutputStream(valueStream));
 			ByteString cellValue = ByteString.copyFrom(valueStream.toByteArray());
 
 			I decoded = inputCodec.decode(Cell.<I> make(cellKey, cellKey, cellValue));
 			try (Sink<E> sink = Codecs.encode(HadoopPipeline.<E, KEYIN, VALUEIN> makeMapperSink(context),
 					outputCodec)) {
 				underlying.map(decoded, ImmutableList.of(decoded), sink);
 			}
 		}
 
 		/** Overwritten to escalate visibility */
 		@Override
 		protected void setup(Context context) throws IOException, InterruptedException {
 			// Do nothing.
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			underlying.close();
 		}
 	}
 
 	private class HadoopMappablePipeline<I> implements MappablePipeline<I, EFF> {
 		final private Codec<I> srcCodec;
 		final private MapConfigurer<I> mapConfigurer;
 
 		HadoopMappablePipeline(MapConfigurer<I> mapConfigurer, Codec<I> srcCodec) {
 			this.mapConfigurer = mapConfigurer;
 			this.srcCodec = srcCodec;
 		}
 
 		@Override
 		public <E> ShuffleablePipeline<E, EFF> mapper(Mapper<I, E> m) {
 			return new HadoopShuffleablePipelineAfterMap<>(mapConfigurer, srcCodec, m);
 		}
 
 		@Override
 		public void efflux(Mapper<I, EFF> m, Codec<EFF> codec) throws IOException {
 			throw new UnsupportedOperationException("I'll think about this case later :)");
 		}
 
 		@Override
 		public void effluxWithOfflineMapper(OfflineMapper<I, EFF> offlineMapper, Codec<EFF> codec)
 				throws IOException {
 			throw new UnsupportedOperationException("I'll think about this case later :)");
 		}
 	}
 
 	private class HadoopReducablePipeline<MAP_IN, MAP_OUT> implements MappablePipeline<MAP_OUT, EFF> {
 		final private MapConfigurer<MAP_IN> mapConfigurer;
 		final private Codec<MAP_IN> mapSrcCodec;
 		final private Mapper<MAP_IN, MAP_OUT> map;
 		final private Codec<MAP_OUT> reduceSrcCodec;
 
 		HadoopReducablePipeline(MapConfigurer<MAP_IN> mapConfigurer, Codec<MAP_IN> mapSrcCodec,
 				Mapper<MAP_IN, MAP_OUT> map, Codec<MAP_OUT> reduceSrcCodec) {
 			this.mapConfigurer = mapConfigurer;
 			this.mapSrcCodec = mapSrcCodec;
 			this.map = map;
 			this.reduceSrcCodec = reduceSrcCodec;
 		}
 
 		@Override
 		public <E> ShuffleablePipeline<E, EFF> mapper(Mapper<MAP_OUT, E> m) {
 			return new HadoopShuffleablePipelineAfterReduce<>(mapConfigurer, mapSrcCodec, map, reduceSrcCodec, m);
 		}
 
 		@Override
 		public void efflux(Mapper<MAP_OUT, EFF> m, Codec<EFF> codec) throws IOException, InterruptedException {
 			run(mapConfigurer, mapSrcCodec, map, reduceSrcCodec, m, codec, efflux);
 			mapConfigurer.close();
 		}
 
 		@Override
 		public void effluxWithOfflineMapper(OfflineMapper<MAP_OUT, EFF> offlineMapper, Codec<EFF> codec)
 				throws IOException {
 			throw new UnsupportedOperationException("I'll think about this case later :)");
 		}
 	}
 
 	private class HadoopShuffleablePipelineAfterMap<I, E> implements ShuffleablePipeline<E, EFF> {
 		final private MapConfigurer<I> mapConfigurer;
 		final private Codec<I> srcCodec;
 		final private Mapper<I, E> mapper;
 
 		HadoopShuffleablePipelineAfterMap(MapConfigurer<I> mapConfigurer, Codec<I> srcCodec, Mapper<I, E> mapper) {
 			this.mapConfigurer = mapConfigurer;
 			this.srcCodec = srcCodec;
 			this.mapper = mapper;
 		}
 
 		@Override
 		public MappablePipeline<E, EFF> shuffle(Codec<E> codec) throws IOException {
 			return new HadoopReducablePipeline<>(mapConfigurer, srcCodec, mapper, codec);
 		}
 	}
 
 	private class HadoopShuffleablePipelineAfterReduce<MAP_IN, MAP_OUT, E> implements ShuffleablePipeline<E, EFF> {
 		final private MapConfigurer<MAP_IN> mapConfigurer;
 		final private Codec<MAP_IN> mapSrcCodec;
 		final private Mapper<MAP_IN, MAP_OUT> map;
 
 		final private Codec<MAP_OUT> reduceSrcCodec;
 		final private Mapper<MAP_OUT, E> reduce;
 
 		HadoopShuffleablePipelineAfterReduce(MapConfigurer<MAP_IN> mapConfigurer, Codec<MAP_IN> mapSrcCodec,
 				Mapper<MAP_IN, MAP_OUT> map, Codec<MAP_OUT> reduceSrcCodec,
 				Mapper<MAP_OUT, E> reduce) {
 			this.mapConfigurer = mapConfigurer;
 			this.mapSrcCodec = mapSrcCodec;
 			this.map = map;
 			this.reduceSrcCodec = reduceSrcCodec;
 			this.reduce = reduce;
 		}
 
 		@Override
 		public MappablePipeline<E, EFF> shuffle(Codec<E> codec) throws IOException, InterruptedException {
 			Table<E> target = admin.createTemporaryTable();
 
 			run(mapConfigurer, mapSrcCodec, map, reduceSrcCodec, reduce, codec, target);
 
 			// This will delete temporary tables if needed.
 			mapConfigurer.close();
 
 			return new HadoopMappablePipeline<>(new TableInputConfigurer<>(target, HadoopModule.INDEX_FAMILY), codec);
 		}
 	}
 
 	private <E, MAP_IN, MAP_OUT> void run(MapConfigurer<MAP_IN> mapConfigurer, Codec<MAP_IN> mapSrcCodec,
 			Mapper<MAP_IN, MAP_OUT> map, Codec<MAP_OUT> reduceSrcCodec,
 			Mapper<MAP_OUT, E> reduce, Codec<E> codec, Table<E> target)
 			throws IOException,	InterruptedException {
 		// TODO: The map configuration is split into a separate object, but the reduce part isn't.
 		// That's a strange symmetry break that should be fixed.
 		Job job = Job.getInstance(baseConfiguration);
 		mapConfigurer.configure(job, mapSrcCodec, map, reduceSrcCodec);
 
 		HadoopReducer<MAP_OUT, E> hReducer = new HadoopReducer<>(reduce, reduceSrcCodec, codec);
 		writeObjectToConf(job.getConfiguration(), hReducer);
 		TableMapReduceUtil.initTableReducerJob(
				target.getTableName(), // output table
 				DecoratorHadoopReducer.class, // reducer class
 				job);
 
 		TableMapReduceUtil.addDependencyJars(job); // TODO: Will this suffice?
 		job.setGroupingComparatorClass(KeyGroupingComparator.class);
 		job.setSortComparatorClass(KeySortingComparator.class);
 		job.setPartitionerClass(KeyGroupingPartitioner.class);
 
 	     // Submit the job, then poll for progress until the job is complete
 	     try {
 			job.waitForCompletion(true);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException("Loading of your job failed. ", e);
 		}
 	}
 
 	/** Loads the configured HadoopReducer and runs it.  */
 	private static class DecoratorHadoopReducer<I, E> extends TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
 		private HadoopReducer<I, E> decorated;
 
 		@Override
 		protected void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context)
 				throws IOException, InterruptedException {
 			decorated.reduce(key, values, context);
 		}
 
 		@SuppressWarnings("unchecked") // Unavoidable, since class literals cannot be generically typed.
 		@Override
 		protected void setup(Context context) throws IOException, InterruptedException {
 			decorated = readObjectFromConf(context.getConfiguration(), HadoopReducer.class);
 			decorated.setup(context);
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			decorated.cleanup(context);
 		}
 	}
 
 	private static class HadoopReducer<I, E> extends TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable>
 			implements Serializable {
 		static final private long serialVersionUID = 1L;
 
 		final private Mapper<I, E> mapper;
 		final private Codec<I> inCodec;
 		final private Codec<E> outCodec;
 
 		HadoopReducer(Mapper<I, E> mapper, Codec<I> inCodec, Codec<E> outCodec) {
 			this.mapper = mapper;
 			this.inCodec = inCodec;
 			this.outCodec = outCodec;
 		}
 
 		/** Format of input: key = len(rowKey) | rowKey | first colKey. value = len(colKey) | colKey | contents. */
 		@Override
 		protected void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context)
 				throws IOException, InterruptedException {
 			// Extract rowKey from key.
 			ByteBuffer rawKey = ByteBuffer.wrap(key.get());
 			byte[] lenBytes = new byte[Ints.BYTES];
 			rawKey.get(lenBytes);
 			int keyLen = Ints.fromByteArray(lenBytes);
 			final ByteString rowKey = ByteString.copyFrom(rawKey, keyLen);
 
 			Iterable<Cell<I>> row = Iterables.transform(values, new Function<ImmutableBytesWritable, Cell<I>>() {
 				@Override public Cell<I> apply(ImmutableBytesWritable value) {
 					ByteBuffer rawContent = ByteBuffer.wrap(value.get());
 					byte[] colKeyLenBytes = new byte[Ints.BYTES];
 					rawContent.get(colKeyLenBytes);
 					int colKeyLen = Ints.fromByteArray(colKeyLenBytes);
 					ByteString colKey = ByteString.copyFrom(rawContent, colKeyLen);
 					ByteString cellContent = ByteString.copyFrom(rawContent);
 					return Cell.<I> make(rowKey, colKey, cellContent);
 				}
 			});
 
 			runRow(Codecs.encode(makeSink(context), outCodec), Codecs.decodeRow(row, inCodec), mapper);
 		}
 
 		/** Format has to match {@link #readCell} below. */
 		private CellSink<E> makeSink(final Context context) {
 			return new CellSink<E>() {
 				final private static long serialVersionUID = 1L;
 
 				@Override
 				public void close() throws IOException {
 					// Nothing to close.
 				}
 
 				@Override
 				public void write(Cell<E> cell) throws IOException, InterruptedException {
 					byte[] rowKey = cell.getRowKey().toByteArray();
 					Put put = new Put(rowKey);
 					put.add(fam, cell.getColumnKey().toByteArray(), cell.getCellContents().toByteArray());
 					context.write(new ImmutableBytesWritable(rowKey), put);
 				}
 			};
 		}
 
 		/** Redefined to escalate visibility. */
 		@Override
 		protected void setup(Context context) throws IOException, InterruptedException {
 			// Do nothing.
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			super.cleanup(context);
 			mapper.close();
 		}
 	}
 
 	/** Loads the configured HadoopTableMapper and runs it. */
 	private static class DecoratorHadoopTableMapper<I, E> extends TableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {
 		private HadoopTableMapper<I, E> decorated;
 
 		@Override
 		protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
 			decorated.map(key, value, context);
 		}
 
 		@SuppressWarnings("unchecked") // Unavoidable, since class literals cannot be generically typed.
 		@Override
 		protected void setup(Context context) throws IOException, InterruptedException {
 			decorated = readObjectFromConf(context.getConfiguration(), HadoopTableMapper.class);
 			decorated.setup(context);
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			decorated.cleanup(context);
 		}
 	}
 
 	/** Read obj from conf from key obj.getClassName(), in base64 encoding of its java serialization. */
 	private static <T> T readObjectFromConf(Configuration conf, Class<T> clazz) throws IOException {
 		try {
 			return (T) new ObjectInputStream(new ByteArrayInputStream(base64().decode(
 					conf.getRaw(clazz.getName())))).readObject();
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException("Couldn't load " + clazz
 					+ ". You probably didn't ship the JAR properly to the server. See the README", e);
 		}
 	}
 
 	/** Write obj into conf under key obj.getClassName(), in base64 encoding of its java serialization. */
 	private static <T> void writeObjectToConf(Configuration conf, T obj) throws IOException {
 		// Serialize into byte array.
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		new ObjectOutputStream(bos).writeObject(obj);
 		byte[] serialized = bos.toByteArray();
 
 		conf.set(obj.getClass().getName(), base64().encode(serialized));
 	}
 
 	private static class HadoopTableMapper<I, E> extends TableMapper<ImmutableBytesWritable, ImmutableBytesWritable>
 			implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		private final Mapper<I, E> mapper;
 		private final Codec<I> inCodec;
 		private final Codec<E> outCodec;
 		/** Do not modify. */
 		private final byte[] family;
 		// TODO: hang on to sink.
 
 		HadoopTableMapper(Mapper<I, E> mapper, Codec<I> inCodec, Codec<E> outCodec, ByteString family) {
 			this.mapper = mapper;
 			this.inCodec = inCodec;
 			this.outCodec = outCodec;
 			this.family = family.toByteArray();
 		}
 
 		@Override
 		protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
 			try (CellSink<E> cellSink = makeMapperSink(context)) {
 				List<Cell<I>> cellRow = new ArrayList<>();
 				for (Entry<byte[], byte[]> kv : value.getFamilyMap(family).entrySet()) {
 					cellRow.add(Cell.<I> make(
 							ByteString.copyFrom(key.get()),
 							ByteString.copyFrom(kv.getKey()),
 							ByteString.copyFrom(kv.getValue())));
 				}
 				runRow(Codecs.encode(cellSink, outCodec), Codecs.decodeRow(cellRow, inCodec), mapper);
 			}
 		}
 
 		/** Re-implemented to escalate visibility. */
 		@Override
 		protected void setup(Context context) {
 			// Nothing to do.
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			mapper.close();
 		}
 	}
 
 	/** Format has to match {@link #readCell} below. */
 	private static <E, KEYIN, KEYOUT> CellSink<E> makeMapperSink(
 			final org.apache.hadoop.mapreduce.Mapper<KEYIN, KEYOUT, ImmutableBytesWritable, ImmutableBytesWritable>.Context context) {
 		return new CellSink<E>() {
 			final private static long serialVersionUID = 1L;
 
 			@Override
 			public void close() {
 				// Nothing to close.
 			}
 
 			@Override
 			public void write(Cell<E> cell) throws IOException, InterruptedException {
 				// Format: key = rowKeyLen | rowKey | colKey.
 				//         value = colKeyLen | colKey | contents
 				byte[] key = Bytes.concat(Ints.toByteArray(cell.getRowKey().size()),
 						(cell.getRowKey().concat(cell.getColumnKey()).toByteArray()));
 				byte[] value = Bytes.concat(Ints.toByteArray(cell.getColumnKey().size()),
 						(cell.getColumnKey().concat(cell.getCellContents()).toByteArray()));
 
 				context.write(new ImmutableBytesWritable(key), new ImmutableBytesWritable(value));
 			}
 		};
 	}
 
 	private static class KeyGroupingPartitioner extends Partitioner<ImmutableBytesWritable, ImmutableBytesWritable>  {
 		final private BinaryPartitioner<ImmutableBytesWritable> defaultPartitioner = new BinaryPartitioner<>();
 
 		@Override
 		public int getPartition(ImmutableBytesWritable key, ImmutableBytesWritable value, int parts) {
 			return defaultPartitioner
 					.getPartition(new BytesWritable(readEmptyCell(key).getRowKey().toByteArray()), value, parts);
 		}
 	}
 
 	private static class KeyGroupingComparator extends WritableComparator {
 		KeyGroupingComparator() {
 			super(ImmutableBytesWritable.class);
 		}
 
 		@Override
 		public int compare(WritableComparable a, WritableComparable b) {
 			Cell<Void> l = readEmptyCell((ImmutableBytesWritable) a);
 			Cell<Void> r = readEmptyCell((ImmutableBytesWritable) b);
 			return l.getRowKey().asReadOnlyByteBuffer().compareTo(r.getRowKey().asReadOnlyByteBuffer());
 		}
 	}
 
 	private static class KeySortingComparator extends WritableComparator {
 		KeySortingComparator() {
 			super(ImmutableBytesWritable.class);
 		}
 
 		@Override
 		public int compare(WritableComparable a, WritableComparable b) {
 			Cell<Void> l = readEmptyCell((ImmutableBytesWritable) a);
 			Cell<Void> r = readEmptyCell((ImmutableBytesWritable) b);
 			return l.compareTo(r);
 		}
 	}
 
 	/** Read cell as written from mapper output. Leave cell contents empty. Used for sorting. */
 	private static Cell<Void> readEmptyCell(ImmutableBytesWritable rawWritable) {
 		ByteBuffer raw = ByteBuffer.wrap(rawWritable.get());
 		// Read len
 		byte[] rawLen = new byte[Ints.BYTES];
 		raw.get(rawLen);
 		int len = Ints.fromByteArray(rawLen);
 
 		ByteString rowKey = ByteString.copyFrom(raw, len);
 
 		ByteString colKey = ByteString.copyFrom(raw);
 
 		return Cell.make(rowKey, colKey, ByteString.EMPTY);
 	}
 
 	/** Don't call for empty rows. */
 	private static <I, E> void runRow(Sink<E> sink, Iterable<I> row, Mapper<I, E> mapper)
 			throws IOException, InterruptedException {
 		checkArgument(!Iterables.isEmpty(row));
 
 		Iterator<I> iter = row.iterator();
 		I first = iter.next();
 		Iterable<I> gluedRow = Iterables.concat(Arrays.asList(first), new OneShotIterable<>(iter));
 		mapper.map(first, gluedRow, sink);
 	}
 }
