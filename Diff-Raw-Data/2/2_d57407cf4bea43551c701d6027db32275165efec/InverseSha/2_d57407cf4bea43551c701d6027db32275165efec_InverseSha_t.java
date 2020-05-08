package org.stratus;
  
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.*;
 import java.util.Date;
 // import java.security.MessageDigest; 
 import java.security.*;
 
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.util.*;
 
 //  Test harness for doing scale-out timings of Inverse SHA 256 for bitcoin generations.  We hardcode the difficulty?!
 // 
 
 public class InverseSha {
 
     //                 KEY              VALUE
     //
     // INPUT           Text             IntWritable       source-text, nonce-start  - hex encoded
     // INTERMEDIATE    IntWritable      IntWritable       nonce-start, elapsed-time 
     // OUTPUT          IntWritable      IntWritable       nonce-start, elapsed-time
 
     
     public static class Map extends MapReduceBase implements Mapper<Text, IntWritable, IntWritable, IntWritable> {
 
 	// take a hex string, such as a representation of a SHA 256, and return as an array of bytes
 
 	private static byte[] hexStringToBytes(String str) {
 	    int len = str.length(); 
 	    byte[] data = new byte[len / 2];
 	    for (int i = 0; i < len; i += 2) {
 	    	data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i+1), 16));
 	    }
 	    return data;
 	}
 		
 	// take a hex string, such as a representation of a SHA 256, and return as a Hadoop BytesWritable
 
 	private static BytesWritable hexStringToBytesWritable(String hexstr) {
 	    return new BytesWritable(hexStringToBytes(hexstr));
 	}
 
 	private static MessageDigest initializeSha256(String match_data) {
 	    byte[] data = hexStringToBytes(match_data);
 	    MessageDigest digest = null;
 	    try {
 		digest = MessageDigest.getInstance("SHA-256");
 		digest.reset();
 		digest.update(data);
 	    } catch (NoSuchAlgorithmException e) { }
 	    return digest;
 	}
 	
 	private static final int NUMBER_OF_RUNS = 1000;
 
 	private static final BytesWritable DIFFICULTY = hexStringToBytesWritable("0fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
 
 	private static byte[] longToBytes(long number) {
 		int len;
 
 		if (number == 0) {
 			len = 1;
 		} else {
 			// get number of places base 16
 			len = (int) Math.ceil(Math.log10(number) / Math.log10(256) + 0.000000001);
 		}
 
 		byte[] data = new byte[len];
 
 		for (int i = len - 1; i >= 0; i--) {
 			data[i] = (byte) (number % 256);
 			number = number/256;
 		}
 
 		return data;
 	}
 
 
 
 	public void map(Text source_text, IntWritable start_nonce, OutputCollector<IntWritable, IntWritable> output, Reporter reporter) throws IOException {
 
 	    int hits = 0;
 	    Date start = new Date();
 
 	    MessageDigest prototype_digest = initializeSha256(source_text.toString());
 	    MessageDigest working_digest = null;
 	    for (long nonce = start_nonce.get(); nonce < (start_nonce.get() + NUMBER_OF_RUNS); nonce++) {
 
 		try {
 		    working_digest = (MessageDigest) prototype_digest.clone(); 
 		} catch (CloneNotSupportedException e) { }
 
 		working_digest.update(longToBytes(nonce));
 		BytesWritable candidate = new BytesWritable(working_digest.digest());
 		
 		if (candidate.compareTo(DIFFICULTY) < 0) {    // we aren't using hits ourselves; 
 		    hits++;
 		}
 	    }
 
 	    Date finished = new Date();
 
 	    output.collect(start_nonce, new IntWritable((int) (finished.getTime() - start.getTime())));
 	}
     }
  
     // Identity - if we have multiple values, it's an error.  Every map task gets its own nonce.
 
     // Key: nonce, Value: elapsed-
 
     public static class Reduce extends MapReduceBase implements Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
 	public void reduce(IntWritable key, Iterator<IntWritable> values, OutputCollector<IntWritable, IntWritable> output, Reporter reporter) throws IOException {
 	    IntWritable time;
 
 	    if (values.hasNext()) {
 		time = values.next();
 	    } else {
 		time = new IntWritable(0);
 	    }
 	    output.collect(key, time);
 	}
     }
  
     public static void main(String[] args) throws Exception {
 	JobConf conf = new JobConf(InverseSha.class);
 
 	conf.setJobName("inverse sha256");
 	conf.setBoolean("mapred.output.compress", false);
 
 	conf.setNumMapTasks(1);
 	conf.setNumReduceTasks(1);
  
 	conf.setOutputKeyClass(IntWritable.class);
 	conf.setOutputValueClass(IntWritable.class);
  
 	conf.setMapperClass(Map.class);
 	conf.setCombinerClass(Reduce.class);
 	conf.setReducerClass(Reduce.class);
  
 	conf.setInputFormat(TextInputFormat.class);
 	conf.setOutputFormat(TextOutputFormat.class);
  
 	FileInputFormat.setInputPaths(conf, new Path(args[0]));
 	FileOutputFormat.setOutputPath(conf, new Path(args[1]));
  
 	JobClient.runJob(conf);
     }
 }
