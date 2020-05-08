 package com.where.hadoop;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.umd.cloud9.io.JSONObjectWritable;
 
 /**
  * 
  * @author fliuzzi
  *
  */
 public class ZipCodeMapReduce extends Configured implements Tool  {
 	
 	public static class ZipMapper extends Mapper<LongWritable, Text, Text, JSONObjectWritable>{
 		
 		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
 	        try {
 	        	JSONObjectWritable json = new JSONObjectWritable(value.toString());
 				
 				String zipcode = json.optString("zip");
 				
 				// creates:  K: TEXT zip code  V: TEXT rest of json
 				if(zipcode.length() > 0)
 				{
 					if(zipcode.length() > 5)
 						zipcode = zipcode.substring(0,5);
 					
 					context.write(new Text(zipcode), json);
 				}
 			} catch (JSONException e) {
 				System.err.println(e.getMessage());
 			}
 		}
 	}
 	
 	public static class ZipReducer extends Reducer<Text,JSONObjectWritable,Text,JSONObjectWritable> {
 		public void reduce(Text key, Iterable<JSONObjectWritable> values, Context context) throws IOException, InterruptedException{
 			
 			JSONArray jarray = new JSONArray();
 			
			System.out.println("Key: "+key);
 			for(JSONObjectWritable val : values)
 			{
				jarray.put(val);
			}
			try{
				//write
				JSONObjectWritable json = new JSONObjectWritable();
				json.put("pois", jarray);
				
				context.write(key, json);
			}
			catch(JSONException e )
			{
				System.err.println(e.getMessage());
 			}
 		}
 	}
 	
 	
 
 	public static void main(String[] args) throws Exception {
 		int ret = ToolRunner.run(new ZipCodeMapReduce(), args);
 		System.exit(ret);
 	}
 
 
 
 	@Override
 	public int run(String[] args) throws Exception {
 		Job job = new Job(getConf());
 		
 		job.setJarByClass(ZipCodeMapReduce.class);
 		job.setJobName("ZipCodeMapReduce");
 		
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(JSONObjectWritable.class);
 		
 		job.setMapperClass(ZipMapper.class);
 		job.setCombinerClass(ZipReducer.class);
 		job.setReducerClass(ZipReducer.class);
 		
 		
 		
 		job.setInputFormatClass(TextInputFormat.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 		
 		
 		FileInputFormat.setInputPaths(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 		
 		boolean success = job.waitForCompletion(true);
 		return success ? 0 : 1;
 	}
 
 }
