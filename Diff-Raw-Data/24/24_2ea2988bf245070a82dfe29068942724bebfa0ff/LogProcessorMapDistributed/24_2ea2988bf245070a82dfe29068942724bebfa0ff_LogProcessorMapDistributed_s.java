 package com.hadooptraining.lab12;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.hadooptraining.lab8.LogWritable;
 import com.maxmind.geoip.Country;
 import com.maxmind.geoip.LookupService;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 public class LogProcessorMapDistributed extends Mapper<Object, LogWritable, Text, IntWritable> {
 
     Path[] localCachePath;
     private LookupService lookupService;
 
     public void setup(Context context) throws IOException{
         Configuration conf = context.getConfiguration();
         localCachePath = DistributedCache.getLocalCacheFiles(conf);
 
         File lookupDbDir = new File(localCachePath[0].toString());
         lookupService = new LookupService(lookupDbDir, LookupService.GEOIP_MEMORY_CACHE);
     }
 
     public void cleanup(Context context) throws IOException{
         lookupService.close();
     }
 
     public void map(Object key, LogWritable value, Context context)
             throws IOException, InterruptedException {
 
         Country country = lookupService.getCountry(value.getUserIP().toString());
System.out.println(country.getName() + "  = >" + value.getResponseSize());
        context.write(new Text(country.getName()),value.getResponseSize());
     }
 }
