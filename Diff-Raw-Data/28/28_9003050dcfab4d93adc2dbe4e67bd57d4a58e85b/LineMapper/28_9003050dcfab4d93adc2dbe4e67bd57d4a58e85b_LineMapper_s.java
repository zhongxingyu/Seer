 import java.io.IOException;
 import java.util.*;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.util.*;	
 
 
 
 public class LineMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{
 
 		private LineParser parser = new LineParser(); 
 		
		static enum DistinctKey {Num_Distinct}
 		
 		static ArrayList<String> keyList = new ArrayList<String>();
 		
 		public void map (LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException
 		{
 			
 			parser.parse(value);
 	        String first = parser.getFirst();
 			
 	        if(!keyList.contains(first))
 	        {
	        	reporter.incrCounter(DistinctKey.Num_Distinct, 1);
 	        	keyList.add(first);
 			}
 	        
	        if(DistinctKey.Num_Distinct.ordinal() > MainRunner.MAX_DISTINCT)
 	        	throw new IOException("Max distinct keys VIOLATED"); 
 	        
 			output.collect(new Text(first), value);
 		}
 }
