 package com.ziroby.helloCassandraAndHadoop;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.TextInputFormat;
 import org.apache.hadoop.mapred.TextOutputFormat;
 
 /**
  * Hello world!
  * 
  */
 public class App {
 
 	private static EntityManagerFactory factory;
 	private static EntityManager entityManager;
 
 	public static class Map extends MapReduceBase implements
 	Mapper<LongWritable, Text, Text, Text> {
 
 		public void map(LongWritable key, Text value,
 				OutputCollector<Text, Text> output, Reporter reporter)
 						throws IOException {
 			Greeting greeting = new Greeting();
 			
 			String outText = "Hello " + value;
 			greeting.setGreetingWord(outText);
 			greeting.setAddressee(value.toString());
 
 			System.out.println("Outtext = " + outText);
 			output.collect(value, new Text(outText));
 		}
 	}
 
 	public static class Reduce extends MapReduceBase implements
 	Reducer<Text, Text, Text, Text> {
 
 		public void reduce(Text key, Iterator<Text> values,
 				OutputCollector<Text, Text> output, Reporter reported)
 						throws IOException {
 			while (values.hasNext()) {
 				Text value = values.next();
 				output.collect(key, value);
 			}
 		}
 
 	}
 
 	public static void main(String[] args) {
 		try {
 			JobConf conf = new JobConf(App.class);
 			conf.setJobName("HelloHadoopAndCassandra");
 
 			conf.setOutputKeyClass(Text.class);
 			conf.setOutputValueClass(Text.class);
 
 			conf.setMapperClass(Map.class);
 			// conf.setCombinerClass(Reducer.class);
 			// conf.setReducerClass(Reducer.class);
 
 			conf.setInputFormat(TextInputFormat.class);
 			conf.setOutputFormat(TextOutputFormat.class);
 
 			FileInputFormat.setInputPaths(conf, new Path(args[0]));
 			FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 			
 			factory = Persistence.createEntityManagerFactory("example3");
 			entityManager = factory.createEntityManager();
 
 			JobClient.runJob(conf);
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		} finally {
 			if (factory != null)
 				factory.close();
 			if (entityManager != null)
 				entityManager.close();
 		} 
 	}
 }
