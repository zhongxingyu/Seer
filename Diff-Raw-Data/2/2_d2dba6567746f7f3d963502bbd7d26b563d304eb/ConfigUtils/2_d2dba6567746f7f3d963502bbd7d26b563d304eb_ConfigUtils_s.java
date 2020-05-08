 package com.oboturov.ht;
 
 import org.apache.hadoop.mapred.JobConf;
 
 /**
  * @author aoboturov
  */
 public final class ConfigUtils {
 
     public static JobConf makeMapOutputCompressedWithBZip2(final JobConf conf) {
         conf.setBoolean("mapreduce.output.fileoutputformat.compress", true);
        conf.set("mapreduce.output.fileoutputformat.compression.codec", "org.apache.hadoop.io.compress.BZip2Codec");
         return conf;
     }
 }
