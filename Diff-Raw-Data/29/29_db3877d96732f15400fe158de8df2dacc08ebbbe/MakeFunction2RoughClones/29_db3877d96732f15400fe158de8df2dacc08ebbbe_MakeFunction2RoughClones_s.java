 package ch.unibe.scg.cc.mappers;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Provider;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.mapreduce.MRJobConfig;
 
 import ch.unibe.scg.cc.Protos.SnippetLocation;
 import ch.unibe.scg.cc.Protos.SnippetMatch;
 import ch.unibe.scg.cc.WrappedRuntimeException;
 import ch.unibe.scg.cc.activerecord.PutFactory;
 
 import com.google.common.base.Optional;
 import com.google.protobuf.ByteString;
 
 /**
  * INPUT:<br>
  *
  * <pre>
  * FAC1 --> { [FUN1|2] , [FUN2|3] , [FUN3|8] }
  * FAC2 --> { [FUN1|3] , [FUN3|9] }
  * </pre>
  *
  * OUTPUT:<br>
  *
  * <pre>
  * FUN1 --> { [FUN2,2|FAC1,3], [FUN3,2|FAC1,8], [FUN3,3|FAC2,9] }
  * FUN2 --> { [FUN1,3|FAC1,2], [FUN3,3|FAC1,8] }
  * FUN3 --> { [FUN1,8|FAC1,2], [FUN1,9|FAC2,9], [FUN2,8|FAC1,3] }
  * </pre>
  */
 public class MakeFunction2RoughClones implements Runnable {
 	static Logger logger = Logger.getLogger(MakeFunction2RoughClones.class.getName());
 	final HTable function2roughclones;
 	final HTable popularSnippets;
 	final MRWrapper mrWrapper;
 	final Provider<Scan> scanProvider;
 
 	@Inject
 	MakeFunction2RoughClones(@Named("function2roughclones") HTable function2roughclones,
 			@Named("popularSnippets") HTable popularSnippets, MRWrapper mrWrapper,
 			Provider<Scan> scanProvider) {
 		this.function2roughclones = function2roughclones;
 		this.popularSnippets = popularSnippets;
 		this.mrWrapper = mrWrapper;
 		this.scanProvider = scanProvider;
 	}
 
 	static class MakeFunction2RoughClonesMapper extends
 			GuiceTableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {
         // HBase dies when set to 1000!
 		private static final int POPULAR_SNIPPET_THRESHOLD = 500;
 		final PutFactory putFactory;
 
 		@Inject
 		MakeFunction2RoughClonesMapper(@Named("popularSnippets") HTableWriteBuffer popularSnippets,
 				PutFactory putFactory) {
 			super(popularSnippets);
 			this.putFactory = putFactory;
 		}
 
		/** receives rows from htable snippet2function */
 		@Override
 		public void map(ImmutableBytesWritable uselessKey, Result value, Context context) throws IOException,
 				InterruptedException {
 			byte[] snippet = value.getRow();
 			assert snippet.length == 21;
 
			Set<Entry<byte[], byte[]>> columns = value.getFamilyMap(Constants.FAMILY).entrySet();
 
 			// special handling of popular snippets
			if (columns.size() > POPULAR_SNIPPET_THRESHOLD) {
				logger.warning("FAMILY MAP SIZE " + columns.size());
 				// fill popularSnippets table
				for (Entry<byte[], byte[]> column : columns) {
 					byte[] location = column.getValue();
 					Put put = putFactory.create(MakeSnippet2Function.ColumnKeyConverter.decode(column.getKey()));
 					put.add(Constants.FAMILY, snippet, 0l, location);
 					write(put);
 				}
 				// we're done, don't go any further!
 				return;
 			}
 
 			// cross product of the columns of the snippetHash
			for (Entry<byte[], byte[]> columnFixed : columns) {
				byte[] thisFunction = MakeSnippet2Function.ColumnKeyConverter.decode(columnFixed.getKey());
				byte[] thisLocation = columnFixed.getValue();
				for (Entry<byte[], byte[]> columnVar : columns) {
					byte[] thatFunction = MakeSnippet2Function.ColumnKeyConverter.decode(columnVar.getKey());
					byte[] thatLocation = columnVar.getValue();
 
 					// save only half of the functions as row-key
 					// full table gets reconstructed in MakeSnippet2FineClones
 					// This *must* be the same as in CloneExpander.
 					if (Bytes.compareTo(thisFunction, thatFunction) >= 0) {
 						continue;
 					}
 
 					/*
 					 * REMARK 1: we don't set thisFunction because it gets
 					 * already passed to the reducer as key. REMARK 2: we don't
 					 * set thatSnippet because it gets already stored in
 					 * thisSnippet
 					 */
 					SnippetMatch snippetMatch = SnippetMatch
 							.newBuilder()
 							.setThisSnippetLocation(
 									SnippetLocation.newBuilder().setPosition(Bytes.toInt(Bytes.head(thisLocation, 4)))
 											.setLength(Bytes.toInt(Bytes.tail(thisLocation, 4)))
 											.setSnippet(ByteString.copyFrom(snippet)))
 							.setThatSnippetLocation(
 									SnippetLocation.newBuilder().setFunction(ByteString.copyFrom(thatFunction))
 											.setPosition(Bytes.toInt(Bytes.head(thatLocation, 4)))
 											.setLength(Bytes.toInt(Bytes.tail(thatLocation, 4)))).build();
 
 					context.write(new ImmutableBytesWritable(thisFunction),
 							new ImmutableBytesWritable(snippetMatch.toByteArray()));
 				}
 			}
 		}
 	}
 
 	static class MakeFunction2RoughClonesReducer extends
 			GuiceTableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
 		final PutFactory putFactory;
 
 		@Inject
 		public MakeFunction2RoughClonesReducer(@Named("function2roughclones") HTableWriteBuffer function2roughclones,
 				PutFactory putFactory) {
 			super(function2roughclones);
 			this.putFactory = putFactory;
 		}
 
 		@Override
 		public void reduce(ImmutableBytesWritable functionHashKey, Iterable<ImmutableBytesWritable> snippetMatchValues,
 				Context context) throws IOException, InterruptedException {
 			byte[] functionHash = functionHashKey.get();
 			Put put = putFactory.create(functionHash);
 
 			for (ImmutableBytesWritable snippetMatchValue : snippetMatchValues) {
 				SnippetMatch snippetMatch = SnippetMatch.parseFrom(snippetMatchValue.get());
 				SnippetLocation thisSnippet = snippetMatch.getThisSnippetLocation();
 				SnippetLocation thatSnippet = snippetMatch.getThatSnippetLocation();
 
 				// create partial SnippetLocations and clear fields already set
 				// in the cellName to save space in HBase
 				SnippetLocation thisPartialSnippet = SnippetLocation.newBuilder(thisSnippet).clearPosition()
 						.clearLength().build();
 				SnippetLocation thatPartialSnippet = SnippetLocation.newBuilder(thatSnippet).clearFunction().build();
 				SnippetMatch partialSnippetMatch = SnippetMatch.newBuilder().setThisSnippetLocation(thisPartialSnippet)
 						.setThatSnippetLocation(thatPartialSnippet).build();
 
 				byte[] columnKey = ColumnKeyConverter.encode(thatSnippet.getFunction().toByteArray(),
 						thisSnippet.getPosition(), thisSnippet.getLength());
 				put.add(Constants.FAMILY, columnKey, 0l, partialSnippetMatch.toByteArray());
 			}
 			context.write(functionHashKey, put);
 		}
 	}
 
 	static class ColumnKeyConverter {
 		static final int THAT_FUNCTION_LENGTH = 20;
 		static final int THIS_POSITION_LENGTH = 4;
 		static final int THIS_LENGTH = 4;
 
 		static byte[] encode(byte[] thatFunction, int thisPosition, int thisLength) {
 			checkArgument(thatFunction.length == THAT_FUNCTION_LENGTH, "function length illegally was "
 					+ thatFunction.length);
 
 			return Bytes.add(thatFunction, Bytes.toBytes(thisPosition), Bytes.toBytes(thisLength));
 		}
 
 		static ColumnKey decode(byte[] encoded) {
 			return new ColumnKey(Bytes.head(encoded, THAT_FUNCTION_LENGTH), Bytes.toInt(Bytes.head(
 					Bytes.tail(encoded, THIS_POSITION_LENGTH + THIS_LENGTH), THIS_POSITION_LENGTH)), Bytes.toInt(Bytes
 					.tail(encoded, THIS_LENGTH)));
 		}
 	}
 
 	static class ColumnKey {
 		final byte[] thatFunction;
 		final int thisPosition;
 		final int thisLength;
 
 		ColumnKey(byte[] thatFunction, int thisPosition, int thisLength) {
 			this.thatFunction = thatFunction;
 			this.thisPosition = thisPosition;
 			this.thisLength = thisLength;
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			mrWrapper.truncate(function2roughclones);
 			mrWrapper.truncate(popularSnippets);
 
 			Scan scan = scanProvider.get();
 			scan.addFamily(Constants.FAMILY);
 
 			Configuration config = new Configuration();
 			config.set(MRJobConfig.MAP_LOG_LEVEL, "DEBUG");
 			config.setInt(MRJobConfig.NUM_REDUCES, 30);
 			// TODO test that
 			config.setInt(MRJobConfig.REDUCE_MERGE_INMEM_THRESHOLD, 0);
 			config.setBoolean(MRJobConfig.REDUCE_MEMTOMEM_ENABLED, true);
 			config.setInt(MRJobConfig.IO_SORT_MB, 512);
 			config.setInt(MRJobConfig.IO_SORT_FACTOR, 100);
 			config.set(MRJobConfig.JOB_UBERTASK_ENABLE, "true");
 			config.set(MRJobConfig.TASK_TIMEOUT, "86400000");
 			config.setInt(MRJobConfig.MAP_MEMORY_MB, 1536);
 			config.set(MRJobConfig.MAP_JAVA_OPTS, "-Xmx1024M");
 			config.setInt(MRJobConfig.REDUCE_MEMORY_MB, 4400);
 			config.set(MRJobConfig.REDUCE_JAVA_OPTS, "-Xmx3800M");
 			config.set(Constants.GUICE_CUSTOM_MODULES_ANNOTATION_STRING, HBaseModule.class.getName());
 
 			mrWrapper.launchMapReduceJob(MakeFunction2RoughClones.class.getName() + "Job", config,
 					Optional.of("snippet2function"), Optional.of("function2roughclones"), Optional.of(scan),
 					MakeFunction2RoughClonesMapper.class.getName(),
 					Optional.of(MakeFunction2RoughClonesReducer.class.getName()), ImmutableBytesWritable.class,
 					ImmutableBytesWritable.class);
 
 			function2roughclones.flushCommits();
 		} catch (IOException | ClassNotFoundException e) {
 			throw new WrappedRuntimeException(e.getCause());
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			return; // Exit
 		}
 	}
 }
