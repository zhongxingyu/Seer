 package us.yuxin.hump;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.compress.CompressionCodec;
 import org.apache.hadoop.mapreduce.MRJobConfig;
 
 public abstract class StoreBase implements Store {
 
   protected FileSystem fs;
   protected Configuration conf;
   protected CompressionCodec codec;
   protected StoreCounter counter;
   protected boolean useTemporary;
   protected Path lastRealPath;
   protected Path lastTempPath;
 
   public StoreBase(FileSystem fs, Configuration conf, CompressionCodec codec) {
     this.counter = new StoreCounter();
     this.fs = fs;
     this.conf = conf;
     this.codec = codec;
     this.useTemporary = true;
   }
 
   @Override
   public Path getLastRealPath() {
     return lastRealPath;
   }
 
   @Override
   public Path getLastTempPath() {
     return lastTempPath;
   }
 
   @Override
   public void store(Path file, JdbcSource source, Properties prop) throws IOException {
     store(file, source, prop, null);
   }
 
   @Override
   public void setUseTemporary(boolean useTemporary) {
     this.useTemporary = useTemporary;
   }
 
   protected Path genTempPath() {
     lastTempPath = new Path("/tmp/hump-" +
       conf.get(MRJobConfig.USER_NAME, "hadoop") + "/" +
      conf.get(MRJobConfig.APPLICATION_ATTEMPT_ID) +
       getLastRealPath().toString().replaceAll("/", "__"));
 
     return lastTempPath;
   }
 
 
   public void setLastRealPath(Path lastRealPath) {
     this.lastRealPath = lastRealPath;
   }
 }
