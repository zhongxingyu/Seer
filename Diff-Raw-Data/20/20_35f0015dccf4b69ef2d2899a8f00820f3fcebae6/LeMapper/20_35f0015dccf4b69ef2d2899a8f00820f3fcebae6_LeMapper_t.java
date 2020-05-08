 package project2;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapreduce.*;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.*;
 
 public class LeMapper extends Mapper<LongWritable, Text, Text, Text> {
 	private Float residualSum = new Float(0.0);
 	private static int totalNodes = 685230;
 
 	protected void map(LongWritable key, Text value, Context context)
 			throws IOException, InterruptedException {
 		
 		//value is in the format['node','PageRank Estimate','degrees(node)','outgoing edgelist'] delimiter = " "
 		
 		String line = value.toString();
 		line = line.trim();
		String[] temp = line.split("\\s+");
 		
 		Text node = new Text(temp[0]);
 		Float pageRank = new Float(temp[1]);
 		Integer degree = new Integer(temp[2]);
 		String edgeList = new String(temp[3]);
 		
 		// the pageRankFactor is used by the reducer to calculate the new pageRank for the outgoing edges
 		Float pageRankFactor = new Float(pageRank/degree);
 		
 		// to pass along previous pageRank and outgoing edgelist
 		// map key:node value:pageRank <outgoing edgelist>
 		Text mapperKey = new Text(node);
 		Text mapperValue = new Text(String.valueOf(pageRank) + " " + edgeList);
 		context.write(mapperKey, mapperValue);
 
 		// now map key:nodeOut value:pageRankFactor for each outgoing edge
 		mapperValue = new Text(String.valueOf(pageRankFactor));
 		String[] edgeListArray = edgeList.split(",");
 
 		for (int i = 0; i < edgeListArray.length; i++) {
 			mapperKey = new Text(edgeListArray[i]);
 			context.write(mapperKey, mapperValue);
 		}
 		cleanup(context);
 	}
 }
