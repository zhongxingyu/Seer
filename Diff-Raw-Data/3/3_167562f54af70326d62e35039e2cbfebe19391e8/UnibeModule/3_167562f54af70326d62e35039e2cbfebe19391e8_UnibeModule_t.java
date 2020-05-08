 package ch.unibe.scg.cells.hadoop;
 
 import javax.inject.Provider;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.io.compress.SnappyCodec;
 import org.apache.hadoop.mapreduce.MRJobConfig;
 
 import com.google.inject.AbstractModule;
 
 /** Internal testing. Don't use. */
 public final class UnibeModule extends AbstractModule {
 	static class UnibeConfigurationProvider implements Provider<Configuration> {
 		@Override
 		public Configuration get() {
 			Configuration ret = HBaseConfiguration.create();
 			ret.set("hbase.master", "leela.unibe.ch:60000");
 			ret.set("hbase.zookeeper.quorum", "leela.unibe.ch");
 			ret.setInt("hbase.zookeeper.property.clientPort", 2181);
 			ret.setBoolean("fs.automatic.close", false);
 
 			// Performance settings.
 			ret.setLong(MRJobConfig.MAP_MEMORY_MB, 4000L);
 			ret.set(MRJobConfig.MAP_JAVA_OPTS, "-Xmx3500m");
 			ret.setLong(MRJobConfig.REDUCE_MEMORY_MB, 4000L);
 			ret.set(MRJobConfig.REDUCE_JAVA_OPTS, "-Xmx3500m");
 			// adjust this when total memory of cluster changes
 			ret.setInt(MRJobConfig.NUM_REDUCES, 27);
 			ret.setInt(MRJobConfig.MAP_FAILURES_MAX_PERCENT, 99);
 			ret.setBoolean(MRJobConfig.MAP_SPECULATIVE, false);
 			ret.setInt(MRJobConfig.JVM_NUMTASKS_TORUN, -1);
 			ret.setBoolean(MRJobConfig.MAP_OUTPUT_COMPRESS, true);
			ret.set(MRJobConfig.MAP_OUTPUT_COMPRESS_CODEC, SnappyCodec.class.getName());
 			ret.setInt(MRJobConfig.IO_SORT_MB, 500);
 			ret.setInt(MRJobConfig.IO_SORT_FACTOR, 50);
 			ret.setFloat(MRJobConfig.MAP_SORT_SPILL_PERCENT, 0.9f);
 			// as suggested on p. 27: http://www.slideshare.net/cloudera/mr-perf
 			ret.setInt(MRJobConfig.REDUCE_MERGE_INMEM_THRESHOLD, 0);
 			ret.setBoolean(MRJobConfig.REDUCE_MEMTOMEM_ENABLED, false);
 			// don't try a failed pack file a second time
 			ret.setInt(MRJobConfig.MAP_MAX_ATTEMPTS, 1);
 			// wait until all map tasks are completed (default: 0.05)
 			ret.setFloat(MRJobConfig.COMPLETED_MAPS_FOR_REDUCE_SLOWSTART, 0.05f);
 			return ret;
 		}
 	}
 
 	@Override
 	protected void configure() {
 		bind(Configuration.class).toProvider(UnibeConfigurationProvider.class);
 	}
 }
