 package ch.unibe.scg.cc.mappers;
 
 import static ch.unibe.scg.cc.mappers.MakeFunction2RoughClones.ColumnKeyConverter.decode;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Logger;
 
 import javax.inject.Named;
 import javax.inject.Provider;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.Counter;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.MRJobConfig;
 import org.apache.hadoop.mapreduce.OutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 
 import ch.unibe.scg.cc.CloneExpander;
 import ch.unibe.scg.cc.Protos.Clone;
 import ch.unibe.scg.cc.Protos.CloneGroup;
 import ch.unibe.scg.cc.Protos.CloneGroup.Builder;
 import ch.unibe.scg.cc.Protos.Occurrence;
 import ch.unibe.scg.cc.Protos.SnippetLocation;
 import ch.unibe.scg.cc.Protos.SnippetMatch;
 import ch.unibe.scg.cc.SpamDetector;
 import ch.unibe.scg.cc.WrappedRuntimeException;
 import ch.unibe.scg.cc.lines.StringOfLinesFactory;
 import ch.unibe.scg.cc.mappers.CloneLoaderProvider.CloneLoader;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.io.BaseEncoding;
 import com.google.inject.Inject;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 public class MakeFunction2FineClones implements Runnable {
 	static final String OUT_DIR = "/tmp/fineclones/";
 	static Logger logger = Logger.getLogger(MakeFunction2FineClones.class.getName());
 	final HTable function2fineclones;
 	final HTable popularSnippets;
 	final MRWrapper mrWrapper;
 	final Provider<Scan> scanProvider;
 
 	@Inject
 	MakeFunction2FineClones(MRWrapper mrWrapper, @Named("popularSnippets") HTable popularSnippets,
 			@Named("function2fineclones") HTable function2fineclones, Provider<Scan> scanProvider) {
 		this.mrWrapper = mrWrapper;
 		this.function2fineclones = function2fineclones;
 		this.popularSnippets = popularSnippets;
 		this.scanProvider = scanProvider;
 	}
 
 	static class Function2FineClones {
 		final CloneExpander cloneExpander;
 		final LoadingCache<byte[], String> cloneLoader;
 		final SpamDetector spamDetector;
 		final StringOfLinesFactory stringOfLinesFactory;
 
 		// Optional because in MRMain, we have an injector that does not set
 		// this property, and can't, because it doesn't have the counter
 		// available.
 		@Inject(optional = true)
 		@Named(Constants.COUNTER_CLONES_REJECTED)
 		Counter clonesRejected;
 
 		@Inject(optional = true)
 		@Named(Constants.COUNTER_CLONES_PASSED)
 		Counter clonesPassed;
 
 		@Inject
 		Function2FineClones(CloneExpander cloneExpander, @CloneLoader LoadingCache<byte[], String> cloneLoader,
 				SpamDetector spamDetector, StringOfLinesFactory stringOfLinesFactory) {
 			this.cloneExpander = cloneExpander;
 			this.cloneLoader = cloneLoader;
 			this.spamDetector = spamDetector;
 			this.stringOfLinesFactory = stringOfLinesFactory;
 		}
 
 		Iterable<Clone> transform(Iterable<SnippetMatch> matches) throws IOException {
 			return filter(cloneExpander.expandClones(matches));
 		}
 
 		/**
 		 * Filter clones down to the clones that aren't spam. In case an
 		 * IOException occurs, abort. Otherwise, try on and log error.
 		 */
 		private Collection<Clone> filter(Collection<Clone> clones) throws IOException {
 			Collection<Clone> ret = Lists.newArrayList();
 			for (Clone clone : clones) {
 				try {
 					if (!spamDetector.isSpamByParameters(spamDetector.extractFeatureVector(
 							stringOfLinesFactory.make(cloneLoader.get(clone.getThisFunction().toByteArray())).getLines(
 									clone.getThisFromPosition(), clone.getThisLength()),
 							stringOfLinesFactory.make(cloneLoader.get(clone.getThatFunction().toByteArray())).getLines(
 									clone.getThatFromPosition(), clone.getThatLength())))) {
 						ret.add(clone);
 					}
 				} catch (ExecutionException e) {
 					Throwables.propagateIfPossible(e.getCause(), IOException.class);
 					logger.severe("Failure while trying to load sources for " + clone + e.getCause());
 				}
 			}
 			if (clonesRejected != null) {
 				clonesRejected.increment(clones.size() - ret.size());
 			}
 			if (clonesPassed != null) {
 				clonesPassed.increment(ret.size());
 			}
 			return ret;
 		}
 	}
 
 	static class MakeFunction2FineClonesMapper extends GuiceTableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {
 		final Function2FineClones function2FineClones;
 
 		@Inject
 		MakeFunction2FineClonesMapper(Function2FineClones function2FineClones) {
 			this.function2FineClones = function2FineClones;
 		}
 
 		/** receives rows from htable function2roughclones */
 		@Override
 		public void map(ImmutableBytesWritable uselessKey, Result value, Context context) throws IOException,
 				InterruptedException {
 			final byte[] function = value.getRow();
 			assert function.length == 20;
 
 			context.getCounter(Counters.FUNCTIONS).increment(1);
 
 			Set<Entry<byte[], byte[]>> columns = value.getFamilyMap(Constants.FAMILY).entrySet();
 			Iterable<SnippetMatch> matches = Iterables.transform(columns,
 					new Function<Entry<byte[], byte[]>, SnippetMatch>() {
 						@Override public SnippetMatch apply(Entry<byte[], byte[]> cell) {
 							// extract information from cellKey
 							// and reconstruct full SnippetLocations
 							MakeFunction2RoughClones.ColumnKey ck = decode(cell.getKey());
 							try {
 								final SnippetMatch partialSnippetMatch = SnippetMatch.parseFrom(cell.getValue());
 								return SnippetMatch
 										.newBuilder(partialSnippetMatch)
 										.setThisSnippetLocation(
 												SnippetLocation
 														.newBuilder(partialSnippetMatch.getThisSnippetLocation())
 														.setFunction(ByteString.copyFrom(function))
 														.setPosition(ck.thisPosition).setLength(ck.thisLength))
 										.setThatSnippetLocation(
 												SnippetLocation
 														.newBuilder(partialSnippetMatch.getThatSnippetLocation())
 														.setFunction(ByteString.copyFrom(ck.thatFunction))).build();
 							} catch (InvalidProtocolBufferException e) {
 								throw new WrappedRuntimeException(e);
 							}
 						}
 					});
 
 			// matching is symmetrical - so we do only half of it here
 			// after matching procedure we expand to full clones
 			for (Clone clone : function2FineClones.transform(matches)) {
 				context.write(new ImmutableBytesWritable(clone.getThisFunction().toByteArray()),
 						new ImmutableBytesWritable(clone.toByteArray()));
 			}
 		}
 	}
 
 	static class MakeFunction2FineClonesReducer extends
 			GuiceReducer<ImmutableBytesWritable, ImmutableBytesWritable, BytesWritable, NullWritable> {
 		final LoadingCache<byte[], String> functionStringCache;
 
 		final HTable strings;
 		final StringOfLinesFactory stringOfLinesFactory;
 		// Optional because in MRMain, we have an injector that does not set
 		// this property, and can't, because it doesn't have the counter
 		// available.
 		@Inject(optional = true)
 		@Named(Constants.COUNTER_MAKE_FUNCTION_2_FINE_CLONES_ARRAY_EXCEPTIONS)
 		Counter arrayExceptions;
 
 		final LoadingCache<byte[], Iterable<Occurrence>> fileLoader;
 		final LoadingCache<byte[], Iterable<Occurrence>> versionLoader;
 		final LoadingCache<byte[], Iterable<Occurrence>> projectLoader;
 
 		@Inject
 		MakeFunction2FineClonesReducer(@Named("strings") HTable strings, StringOfLinesFactory stringOfLinesFactory,
 				@CloneLoader LoadingCache<byte[], String> functionStringCache,
 				@Named("file2function") LoadingCache<byte[], Iterable<Occurrence>> fileLoader,
 				@Named("version2file") LoadingCache<byte[], Iterable<Occurrence>> versionLoader,
 				@Named("project2version") LoadingCache<byte[], Iterable<Occurrence>> projectLoader) {
 			this.strings = strings;
 			this.stringOfLinesFactory = stringOfLinesFactory;
 			this.functionStringCache = functionStringCache;
 			this.fileLoader = fileLoader;
 			this.versionLoader = versionLoader;
 			this.projectLoader = projectLoader;
 		}
 
 		@Override
 		public void reduce(final ImmutableBytesWritable functionHashKey,
 				Iterable<ImmutableBytesWritable> cloneProtobufs, Context context) throws IOException,
 				InterruptedException {
 			// there's always the same iterator instance returned when calling
 			// cloneProtobufs.iterator()
 
 			int from = Integer.MAX_VALUE;
 			int to = Integer.MIN_VALUE;
 
 			int commonness = 0;
 			Builder cloneGroupBuilder = CloneGroup.newBuilder();
 
 			findOccurrences(cloneGroupBuilder, functionHashKey.get());
 
 			for (ImmutableBytesWritable cloneProtobuf : cloneProtobufs) {
 				final Clone clone = Clone.parseFrom(cloneProtobuf.get());
 				checkState(Arrays.equals(clone.getThisFunction().toByteArray(), functionHashKey.get()),
 						"The function hash key did not match one of the clones. Clone says: "
 								+ BaseEncoding.base16().encode(clone.getThisFunction().toByteArray())
 								+ " reduce key: " + BaseEncoding.base16().encode(functionHashKey.get()));
 
 				if (!clone.getThisFunction().equals(ByteString.copyFrom(functionHashKey.get()))) {
 					throw new AssertionError(
 							"There is a clone in cloneProtobufs that doesn't match the input function "
 									+ BaseEncoding.base16().encode(functionHashKey.get()));
 				}
 
 				findOccurrences(cloneGroupBuilder, clone.getThatFunction().toByteArray());
 
 				from = Math.min(from, clone.getThisFromPosition());
 				to = Math.max(to, clone.getThisFromPosition() + clone.getThisLength());
 				commonness++;
 			}
 
 			if (commonness <= 0) {
 				throw new AssertionError("commonness must be non-negative, but was " + commonness);
 			}
 
 			String functionString;
 			try {
 				functionString = functionStringCache.get(functionHashKey.get());
 			} catch (ExecutionException e) {
 				Throwables.propagateIfPossible(e.getCause(), IOException.class);
 				throw new WrappedRuntimeException("The CacheLoader threw an exception while reading function "
 						+ BaseEncoding.base16().encode(functionHashKey.get()) + ".", e.getCause());
 			}
 
 			cloneGroupBuilder.setText(
 					stringOfLinesFactory.make(functionString, '\n').getLines(from, to - from).toString());
 
 			byte[] key = ColumnKeyCodec.encode(commonness, cloneGroupBuilder.build());
 			context.write(new BytesWritable(key), NullWritable.get());
 		}
 
 		/** Add all occurrences of function {@code functionKey} to cloneGroupBuilder */
 		private void findOccurrences(Builder cloneGroupBuilder, byte[] functionKey) throws IOException {
 			try {
 				for (Occurrence file : fileLoader.get(functionKey)) {
 					for (Occurrence version : versionLoader.get(file.getFileNameHash().toByteArray())) {
 						for (Occurrence project : projectLoader.get(version.getVersionHash().toByteArray())) {
 							cloneGroupBuilder.addOccurrences(Occurrence.newBuilder().mergeFrom(file)
 									.mergeFrom(version).mergeFrom(project));
 						}
 					}
 				}
 			} catch (ExecutionException e) {
 				Throwables.propagateIfPossible(e.getCause(), IOException.class);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	static class ColumnKeyCodec {
 		static final int COMMONNESS_LENGTH = 4;
 
 		static byte[] encode(int commonness, CloneGroup cloneGroup) {
 			checkArgument(commonness >= 0, "Negative commonness will ruin sorting. You supplied commonness "
 					+ commonness);
 			return Bytes.add(Bytes.toBytes(commonness), cloneGroup.toByteArray());
 		}
 
 		static ColumnKey decode(byte[] encoded) throws InvalidProtocolBufferException {
 			return new ColumnKey(Bytes.toInt(Bytes.head(encoded, COMMONNESS_LENGTH)), CloneGroup.parseFrom(Bytes.tail(
 					encoded, encoded.length - COMMONNESS_LENGTH)));
 		}
 	}
 
 	static class ColumnKey {
 		final int commonness;
 		final CloneGroup cloneGroup;
 
 		ColumnKey(int commonness, CloneGroup cloneGroup) {
 			this.commonness = commonness;
 			this.cloneGroup = cloneGroup;
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			FileSystem.get(new Configuration()).delete(new Path(OUT_DIR), true);
 			mrWrapper.truncate(function2fineclones);
 
 			Configuration config = new Configuration();
 			config.set(MRJobConfig.MAP_LOG_LEVEL, "DEBUG");
 			config.set(MRJobConfig.NUM_REDUCES, "30");
 			// TODO test that
 			config.set(MRJobConfig.REDUCE_MERGE_INMEM_THRESHOLD, "0");
 			config.set(MRJobConfig.REDUCE_MEMTOMEM_ENABLED, "true");
 			config.set(MRJobConfig.IO_SORT_MB, "512");
 			config.set(MRJobConfig.IO_SORT_FACTOR, "100");
 			config.set(MRJobConfig.JOB_UBERTASK_ENABLE, "true");
 			config.set(MRJobConfig.TASK_TIMEOUT, "86400000");
			config.set(MRJobConfig.MAP_MEMORY_MB, "8192");
			config.set(MRJobConfig.MAP_JAVA_OPTS, "-Xmx8000M");
			config.set(MRJobConfig.REDUCE_MEMORY_MB, "3072");
			config.set(MRJobConfig.REDUCE_JAVA_OPTS, "-Xmx2560M");
 			config.set(FileOutputFormat.OUTDIR, OUT_DIR);
 			config.setClass(Job.OUTPUT_FORMAT_CLASS_ATTR, SequenceFileOutputFormat.class, OutputFormat.class);
 			config.setClass(Job.OUTPUT_KEY_CLASS, BytesWritable.class, Object.class);
 			config.setClass(Job.OUTPUT_VALUE_CLASS, NullWritable.class, Object.class);
 
 			Scan scan = scanProvider.get();
 			scan.addFamily(Constants.FAMILY);
 
 			mrWrapper.launchMapReduceJob(MakeFunction2FineClones.class.getName() + "Job", config,
 					Optional.of("function2roughclones"), Optional.<String> absent(), Optional.of(scan),
 					MakeFunction2FineClonesMapper.class.getName(),
 					Optional.of(MakeFunction2FineClonesReducer.class.getName()), ImmutableBytesWritable.class,
 					ImmutableBytesWritable.class);
 		} catch (IOException | ClassNotFoundException e) {
 			throw new WrappedRuntimeException(e.getCause());
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			return; // Exit.
 		}
 	}
 }
