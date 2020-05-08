 package cmu.ds.mr.test;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.io.OutputCollector;
 import cmu.ds.mr.mapred.JobClient;
 import cmu.ds.mr.mapred.MapReduceBase;
 import cmu.ds.mr.mapred.Mapper;
 import cmu.ds.mr.mapred.Reducer;
 
 /**
  * Example word count program.
  * Adopted from http://www.cloudera.com/content/cloudera-content/cloudera-docs/HadoopTutorial/CDH4/Hadoop-Tutorial/ht_topic_5_1.html
  * */
 public class WordCountError {
 
   public static class Map extends MapReduceBase implements Mapper<Long, String, String, String> {
     private final static String one = "1";
     private String word = "";
 
     public void map(Long key, String value, OutputCollector<String, String> output) throws IOException {
       String line = value.toString();
       StringTokenizer tokenizer = new StringTokenizer(line);
       while (tokenizer.hasMoreTokens()) {
         word = tokenizer.nextToken();
         output.collect(word, one);
       }
     }
   }
 
   public static class Reduce extends MapReduceBase implements Reducer<String, String, String, String> {
     public void reduce(String key, Iterator<String> values, OutputCollector<String, String> output) throws IOException {
       int sum = 0;
       while (values.hasNext()) {
         int num = Integer.parseInt(values.next());
         sum += num;
       }
       output.collect(key, sum + "");
     }
   }
 
   public static void main(String[] args) throws Exception {
     if(args.length != 3) {
       System.err.println("Usage: WordCount <inPath> <outPath> <numReducer>");
       return;
     }
 //    args[0] = "data/abstract.txt";
 //    args[1] = "data/out/";
     
     JobConf conf = new JobConf();
     conf.setJobName("wordcount");
 
     conf.setInpath(args[0]);
     conf.setOutpath(args[1]);
     
     conf.setNumReduceTasks(Integer.parseInt(args[2]));
     
     conf.setMapperClass(Map.class);
     conf.setReducerClass(Reduce.class);
 
 
     JobClient.runJob(conf);
   }
 }
