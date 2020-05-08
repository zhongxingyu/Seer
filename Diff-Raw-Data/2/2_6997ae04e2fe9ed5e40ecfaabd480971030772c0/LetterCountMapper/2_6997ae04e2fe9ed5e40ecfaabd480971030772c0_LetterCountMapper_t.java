 package letter;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileSplit;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.JobConf;
 
 public class LetterCountMapper extends MapReduceBase
     implements Mapper<LongWritable, Text, Text, Text> {
 
   public LetterCountMapper() { }
 
   public void map(LongWritable key, Text value, OutputCollector<Text, Text> output,
       Reporter reporter) throws IOException {
 	    
 	    //Split string into array of chars. Remove all characters that are not in [a-z^A-Z]  
	    char[] letterArray = value.toString().toLowerCase().replaceAll("[^a-z]","").toCharArray();
 	   
 	    //For each letter send it to the reducer
 	    for(int i=0; i<letterArray.length; i++){
 	    	output.collect( new Text(String.valueOf(letterArray[i])), new Text("1"));
 	    }
 	  }
 }
 
