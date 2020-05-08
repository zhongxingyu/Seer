 package ch.unibe.scg.cc.mappers;
 
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 import java.util.logging.Logger;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.mapreduce.MRJobConfig;
 
 import ch.unibe.scg.cc.WrappedRuntimeException;
 import ch.unibe.scg.cc.activerecord.Column;
 import ch.unibe.scg.cc.activerecord.PutFactory;
 
 import com.google.common.base.Optional;
 import com.google.common.io.BaseEncoding;
 
 public class MakeSnippet2Function implements Runnable {
 	static Logger logger = Logger.getLogger(MakeSnippet2Function.class.getName());
 	final HTable snippet2Function;
 	final MRWrapper mrWrapper;
 
 	@Inject
	MakeSnippet2Function(@Named("snippet2function") HTable snippet2Function, MRWrapper mrWrapper) {
 		this.snippet2Function = snippet2Function;
 		this.mrWrapper = mrWrapper;
 	}
 
	static class MakeSnippet2FunctionMapper<KEYOUT, VALUEOUT> extends GuiceTableMapper<KEYOUT, VALUEOUT> {
 		/** receives rows from htable function2snippet */
		@SuppressWarnings("unchecked")
 		// super class uses unchecked types
 		@Override
		public void map(ImmutableBytesWritable functionHashKey, Result value,
				@SuppressWarnings("rawtypes") org.apache.hadoop.mapreduce.Mapper.Context context) throws IOException,
 				InterruptedException {
 			byte[] functionHash = functionHashKey.get();
 			assert functionHash.length == 20;
 
 			logger.finer("map " + BaseEncoding.base16().encode(functionHashKey.get()).substring(0, 4));
 
 			NavigableMap<byte[], byte[]> familyMap = value.getFamilyMap(Column.FAMILY_NAME);
 
 			for (Entry<byte[], byte[]> column : familyMap.entrySet()) {
 				byte[] snippet = column.getKey();
 				byte[] functionHashPlusLocation = Bytes.add(functionHash, column.getValue());
 
 				logger.finer("snippet " + BaseEncoding.base16().encode(snippet).substring(0, 6) + " found");
 
 				context.write(new ImmutableBytesWritable(snippet), new ImmutableBytesWritable(functionHashPlusLocation));
 			}
 		}
 	}
 
 	static class MakeSnippet2FunctionReducer extends
 			GuiceTableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
 		final PutFactory putFactory;
 
 		@Inject
 		public MakeSnippet2FunctionReducer(@Named("snippet2function") HTableWriteBuffer snippet2Function,
 				PutFactory putFactory) {
 			super(snippet2Function);
 			this.putFactory = putFactory;
 		}
 
 		@Override
 		public void reduce(ImmutableBytesWritable snippetKey,
 				Iterable<ImmutableBytesWritable> functionHashesPlusLocations, Context context) throws IOException,
 				InterruptedException {
 			byte[] snippet = snippetKey.get();
 
 			logger.finer("reduce " + BaseEncoding.base16().encode(snippet).substring(0, 6));
 
 			Put put = putFactory.create(snippet);
 			int functionCount = 0;
 			for (ImmutableBytesWritable functionHashPlusLocation : functionHashesPlusLocations) {
 				functionCount++;
 
 				byte[] functionHashPlusLocationKey = functionHashPlusLocation.get();
 				byte[] functionHash = Bytes.head(functionHashPlusLocationKey, 20);
 				byte[] factRelativeLocation = Bytes.tail(functionHashPlusLocationKey, 8);
 
 				put.add(Constants.FAMILY, functionHash, 0l, factRelativeLocation);
 			}
 
 			// prevent saving non-recurring hashes
 			if (functionCount > 1) {
 				logger.finer("save " + BaseEncoding.base16().encode(snippet).substring(0, 6));
 				context.write(snippetKey, put);
 			}
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			mrWrapper.truncate(snippet2Function);
 
			Scan scan = new Scan();
			scan.setCaching(500); // TODO play with this. (100 is default value)

 			Configuration config = new Configuration();
 			config.set(MRJobConfig.MAP_LOG_LEVEL, "DEBUG");
 			config.set(MRJobConfig.NUM_REDUCES, "30");
 			// TODO test that
 			config.set(MRJobConfig.REDUCE_MERGE_INMEM_THRESHOLD, "0");
 			config.set(MRJobConfig.REDUCE_MEMTOMEM_ENABLED, "true");
 			config.set(MRJobConfig.IO_SORT_MB, "256");
 			config.set(MRJobConfig.IO_SORT_FACTOR, "100");
 			config.set(MRJobConfig.JOB_UBERTASK_ENABLE, "true");
 			// set to 1 if unsure TODO: check max mem allocation if only 1 jvm
 			config.set(MRJobConfig.JVM_NUMTASKS_TORUN, "-1");
 			config.set(MRJobConfig.TASK_TIMEOUT, "86400000");
 			config.set(MRJobConfig.MAP_MEMORY_MB, "1536");
 			config.set(MRJobConfig.MAP_JAVA_OPTS, "-Xmx1024M");
 			config.set(MRJobConfig.REDUCE_MEMORY_MB, "3072");
 			config.set(MRJobConfig.REDUCE_JAVA_OPTS, "-Xmx2560M");
 
 			mrWrapper.launchMapReduceJob(MakeSnippet2Function.class.getName() + "Job", config,
					Optional.of("function2snippet"), Optional.of("snippet2function"), Optional.of(scan),
 					MakeSnippet2FunctionMapper.class.getName(),
 					Optional.of(MakeSnippet2FunctionReducer.class.getName()), ImmutableBytesWritable.class,
 					ImmutableBytesWritable.class);
 		} catch (IOException | ClassNotFoundException e) {
 			throw new WrappedRuntimeException(e);
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			return; // Exit.
 		}
 	}
 }
