 package cmu.ds.mr.test;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.io.OutputCollector;
 import cmu.ds.mr.mapred.Mapper;
 import cmu.ds.mr.mapred.Reducer;
 import cmu.ds.mr.mapred.JobClient;
 
 public class MRtest {
 
   public static class Map implements Mapper<String, String, String, String> {
 
     @Override
     public void map(String key, String value, OutputCollector<String, String> output)
             throws IOException {
       // TODO Auto-generated method stub
       
     }
 
   }
 
   public static class Reduce implements Reducer<String, String, String, String> {
 
     @Override
     public void reduce(String key, Iterator<String> values, OutputCollector<String, String> output)
             throws IOException {
       // TODO Auto-generated method stub
       
     }
   }
   
   public static void main(String[] args) throws Exception {
     JobClient.runJob(config());
   }
   
 
   protected static JobConf config() throws Exception {
     JobConf conf = new JobConf();
     conf.setJobName("test");
     conf.setMapperClass(Map.class);
     conf.setReducerClass(Reduce.class);
     conf.setInpath("test.txt");
    conf.setJobTrackerAddr("localhost");
     return conf;
   }
 }
