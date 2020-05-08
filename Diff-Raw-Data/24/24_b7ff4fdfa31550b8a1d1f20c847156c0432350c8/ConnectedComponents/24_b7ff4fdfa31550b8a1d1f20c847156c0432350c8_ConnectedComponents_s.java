 package org.myorg;
 	
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapreduce.*;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.*;
 
 import org.apache.commons.logging.*;
 
 public class ConnectedComponents {
 
   // Logging
   //static Log mapLog = LogFactory.getLog(Map.class);
   //static Log reduceLog = LogFactory.getLog(Reduce.class);  
 
   // g^2 = m is optimal
   static long computeG(long val) {
 
       long estG = (long)(Math.pow((double)val, 1.5));
 
       List<Integer> factors  = new ArrayList<Integer>();
       for(int i=1; i <= val/2; i++)
       {
           if(val % i == 0)
           {
               factors.add(i);
           }
       }
 
       return factors.get(factors.size() - 1);
   }
   
   public static class Node{
     public long groupNum;
     public long nodeNum;
     public long nodeLabel;
     public boolean visited;
     
     public Node(long gn, long nn, long nl){
       groupNum = gn;
       nodeNum = nn;
       nodeLabel = nl;
       visited = false;
     }
   }
 
  private static final long m = 4;
  private static final long g = computeG(m);
   
   // compute filter parameters for netid jsh263
   private static final double fromNetID = 0.362;
   private static final double desiredDensity = 0.59;
   private static final double wMin = 0.4 * fromNetID;
   private static final double wLimit = wMin + desiredDensity;
 
   public static class FirstPassMap extends Mapper<LongWritable, Text, LongWritable, LongWritable> {
       
       private Text word = new Text();
       
       public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
         
         double val = Double.parseDouble(value.toString());
         
         long lineNum = key.get() / 12 + 1;
         
         // Make sure m is actually going to be a point in our graph
         if(lineNum > m*m || val < wMin || val >= wLimit){
           return;
         }
         
         long x, y;
         
         long sqrt = (long) Math.ceil(Math.sqrt((double) lineNum));
         
         // Calculate x/y coordinates of this point
         if(sqrt*sqrt == lineNum){
           x = sqrt - 1;
           y = sqrt - 1;
         } else {
           if((sqrt % 2) == (lineNum % 2)){
             y = sqrt - 1;
             x = sqrt - (1 + (sqrt * sqrt - lineNum) / 2);
           } else {
             x = sqrt - 1;
             y = sqrt - (1 + (sqrt * sqrt - lineNum + 1) / 2);
           }
         }
 
         context.write(new LongWritable(x/g), new LongWritable(x*m + y));
         // If this is a boundary point
         if(x != (m - 1) && (x % g) == (g - 1)){
           context.write(new LongWritable(x/g + 1), new LongWritable(x*m + y));
         }
         
         return;
       }
   }
 	
     public static class FirstPassReduce extends Reducer<LongWritable,LongWritable,LongWritable,Text> {
     
     // Depth first labeling for the nodes
     private static void dfs(long i, long l, long[] elements, long[] label){
       label[(int)i] = l;
       if(i % m != (m - 1) && i != elements.length){
         if(elements[(int)(i + 1)] != 0 && label[(int)(i + 1)] == -1){
           dfs(i+1, l, elements, label);
         }
       }
       
       if(i % m != 0 && i != 0){
         if(elements[(int)(i - 1)] != 0  && label[(int)(i - 1)] == -1){
           dfs(i-1, l, elements, label);
         }
       }
       
       if(i >= m){
         if(elements[(int)(i - m)] != 0  && label[(int)(i - m)] == -1){
           dfs(i-m, l, elements, label);
         }
       }
       
       if(i < elements.length - m){
         if(elements[(int)(i + m)] != 0  && label[(int)(i + m)] == -1){
           dfs(i+m, l, elements, label);
         }
       }
       
       return;
     }
     
     public void reduce(LongWritable key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
       
       long groupNum = key.get();
       // Size of first group (g = 0) is g*m, otherwise (1 + g) * m
       long maxElements = (g + 1)*m;
       long offset = groupNum * m * g - m;
       if(groupNum == 0){ maxElements -= m; offset += m;}
       
       long[] elements = new long[(int)maxElements];
       long[] label = new long[(int)maxElements];
       Arrays.fill(label, (long)(-1));
       
       for(LongWritable val : values) {
         int index = (int)(val.get() - offset);
         elements[index] = 1;
       }
       
       for(long i = 0; i < maxElements; i++){
         if(elements[(int)i] == 1 && label[(int)i] == -1){
           // Perform a DFS 
           dfs(i, i, elements, label);
         }
         if(elements[(int)i] == 1){
           // Emit if node exists 
           context.write(new LongWritable(groupNum),
               new Text(Long.toString(i+offset) + " " + Long.toString(label[(int)i] + offset))); 
         }
       }
     }
   }
   	
   public static class SecondPassMap extends Mapper<LongWritable, Text, LongWritable, Text> { 
       
       private static long countEmissions = 0;
       
       public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
         
         LongWritable one = new LongWritable(1);
         
         String nodeStr = value.toString();
         
         String[] node = nodeStr.split("\\s+");
         
         long groupNum = Long.parseLong(node[0]);
         long nodeNum = Long.parseLong(node[1]);
         long nodeLabel = Long.parseLong(node[2]);
         
         long gm = g*m;
         long modulus = nodeNum % gm;
         
         // First find out if the node is a boundary node
         if( (nodeNum < m*(m - 1)) && (modulus >= (g-1)*m) ){
           context.write(one, new Text(nodeStr));
         }
         
         return;
       }
   }
   
   public static class SecondPassReduce extends Reducer<LongWritable,Text,LongWritable,Text> {
     
     // Depth first labeling of nodes
     private static void dfs(long currentLabel, long newLabel,
       HashMap<Long, ArrayList<Node>> positionsMap, HashMap<Long, ArrayList<Node>> labelsMap){
         Long key = new Long(currentLabel);
         ArrayList<Node> nodes = labelsMap.get(key);
         
         // Iterate through each position in the labels list, running dfs
         // on each of those labels
         for(int i = 0; i < nodes.size(); i++){
             nodes.get(i).nodeLabel = newLabel;
             nodes.get(i).visited = true;
         }
         
          for(int i = 0; i < nodes.size(); i++){
             Long nodeNum = new Long(nodes.get(i).nodeNum);
             ArrayList<Node> nodesWithNodeNum = positionsMap.get(nodeNum);
             for(int j = 0; j < nodesWithNodeNum.size(); j++){
                 if(!(nodesWithNodeNum.get(j).visited)){
                     dfs(nodesWithNodeNum.get(j).nodeLabel, newLabel, positionsMap, labelsMap);
                 }
             }
         }
     }
     
     public void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
       
       // Hashmaps with an appropriate initial capacity
       HashMap<Long, ArrayList<Node>> positionsMap = new HashMap<Long, ArrayList<Node>>((int)(m*m/g));
       HashMap<Long, ArrayList<Node>> labelsMap = new HashMap<Long, ArrayList<Node>>((int)(m*m/g));
       
       String nodeStr;
       String[] nodeInfo;
       Node node;
       
       for(Text val : values) {
         nodeStr = val.toString();
         
         nodeInfo = nodeStr.split("\\s+");
         
         node = new Node(Long.parseLong(nodeInfo[0]),
                              Long.parseLong(nodeInfo[1]),
                              Long.parseLong(nodeInfo[2]));
         Long nn = new Long(node.nodeNum);
         Long nl = new Long(node.nodeLabel);
         
         if( positionsMap.get(nn) == null ){
           positionsMap.put(nn, new ArrayList<Node>());
         }
         positionsMap.get(nn).add(node);
         
         if( labelsMap.get(nl) == null ){
           labelsMap.put(nl, new ArrayList<Node>());
         }
         labelsMap.get(nl).add(node);
       }
       
       List<Long> positions=new ArrayList<Long>(positionsMap.keySet());
       Collections.sort(positions);
       List<Long> labels=new ArrayList<Long>(labelsMap.keySet());
       Collections.sort(labels);
       
       // Loop through labels, performing DFS where necessary
       for(long i = 0; (int)i < labels.size(); i++){
           Long label = labels.get((int)i);
           if( !(labelsMap.get(label).get(0).visited) ){
               dfs(label.longValue(), label.longValue(), positionsMap, labelsMap);
           }
       }
       
       // Loop through positions, outputting what we've found
       for(long k = 0; (int)k < positions.size(); k++){
           Long position = positions.get((int)k);
           ArrayList<Node> nodes = positionsMap.get(position);
           for(int l = 0; l < nodes.size(); l++){
               node = nodes.get(l);
               context.write(new LongWritable(node.groupNum), 
                 new Text(Long.toString(node.nodeNum) + " " + Long.toString(node.nodeLabel))); 
           }
       }
     }
   }
   
   public static class ThirdPassMap extends Mapper<LongWritable, Text, LongWritable, Text> { 
       
       private static long countEmissions = 0;
       
       public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
         
         LongWritable one = new LongWritable(1);
         
         String nodeStr = value.toString();
         
         String[] node = nodeStr.split("\\s+");
         
         long groupNum = Long.parseLong(node[0]);
         long nodeNum = Long.parseLong(node[1]);
         long nodeLabel = Long.parseLong(node[2]);
         
         long gm = g*m;
         long modulus = nodeNum % gm;
         
         context.write(new LongWritable(groupNum), new Text(node[1] + " " + node[2]));
         
         return;
       }
   }
   
   public static class ThirdPassReduce extends Reducer<LongWritable,Text,LongWritable,LongWritable> {
     
     private static void dfs(long currentLabel, long newLabel,
       HashMap<Long, ArrayList<Node>> positionsMap, HashMap<Long, ArrayList<Node>> labelsMap){
         Long key = new Long(currentLabel);
         ArrayList<Node> nodes = labelsMap.get(key);
         
         // Iterate through each position in the labels list, running dfs
         // on each of those labels
         for(int i = 0; i < nodes.size(); i++){
             nodes.get(i).nodeLabel = newLabel;
             nodes.get(i).visited = true;
         }
         
          for(int i = 0; i < nodes.size(); i++){
             Long nodeNum = new Long(nodes.get(i).nodeNum);
             ArrayList<Node> nodesWithNodeNum = positionsMap.get(nodeNum);
             for(int j = 0; j < nodesWithNodeNum.size(); j++){
                 if(!(nodesWithNodeNum.get(j).visited)){
                     dfs(nodesWithNodeNum.get(j).nodeLabel, newLabel, positionsMap, labelsMap);
                 }
             }
         }
     }
     
     public void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
       
       // Hashmaps with an appropriate initial capacity
       HashMap<Long, ArrayList<Node>> positionsMap = new HashMap<Long, ArrayList<Node>>((int)(m*m/g));
       HashMap<Long, ArrayList<Node>> labelsMap = new HashMap<Long, ArrayList<Node>>((int)(m*m/g));
       
       String nodeStr;
       String[] nodeInfo;
       Node node;
       
       long groupNum = key.get();
       
       for(Text val : values) {
         nodeStr = val.toString();
         
         nodeInfo = nodeStr.split("\\s+");
         
         node = new Node(groupNum,
                              Long.parseLong(nodeInfo[0]),
                              Long.parseLong(nodeInfo[1]));
         Long nn = new Long(node.nodeNum);
         Long nl = new Long(node.nodeLabel);
         
         if( positionsMap.get(nn) == null ){
           positionsMap.put(nn, new ArrayList<Node>());
         }
         positionsMap.get(nn).add(node);
         
         if( labelsMap.get(nl) == null ){
           labelsMap.put(nl, new ArrayList<Node>());
         }
         labelsMap.get(nl).add(node);
       }
       
       List<Long> positions=new ArrayList<Long>(positionsMap.keySet());
       Collections.sort(positions);
       List<Long> labels=new ArrayList<Long>(labelsMap.keySet());
       Collections.sort(labels);
       
       // Loop through labels, performing DFS where necessary
       for(long i = 0; (int)i < labels.size(); i++){
           Long label = labels.get((int)i);
           if( !(labelsMap.get(label).get(0).visited) ){
               dfs(label.longValue(), label.longValue(), positionsMap, labelsMap);
           }
       }
       
       // Loop through positions, outputting what we've found
       for(long k = 0; (int)k < positions.size(); k++){
           Long position = positions.get((int)k);
           ArrayList<Node> nodes = positionsMap.get(position);
           //for(int l = 0; l < nodes.size(); l++){
               //node = nodes.get(l);
               node = nodes.get(0);
               context.write(new LongWritable(node.nodeNum), 
                 new LongWritable(node.nodeLabel)); 
           //}
       }
     }
   }
   
   public static void main(String[] args) throws Exception {
 
     Configuration conf = new Configuration();
         
     Job job = new Job(conf, "firstpass");
     job.setMapOutputKeyClass(LongWritable.class);
     job.setMapOutputValueClass(LongWritable.class);
     job.setOutputKeyClass(LongWritable.class);
     job.setOutputValueClass(Text.class);        
     job.setMapperClass(FirstPassMap.class);
     job.setReducerClass(FirstPassReduce.class);        
     job.setInputFormatClass(TextInputFormat.class);
     job.setOutputFormatClass(TextOutputFormat.class);       
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
     job.waitForCompletion(true);
     
     job = new Job(conf, "secondpass");
     job.setMapOutputKeyClass(LongWritable.class);
     job.setMapOutputValueClass(Text.class);
     job.setOutputKeyClass(LongWritable.class);
     job.setOutputValueClass(Text.class);
     job.setMapperClass(SecondPassMap.class);
     job.setReducerClass(SecondPassReduce.class);
     job.setInputFormatClass(TextInputFormat.class);
     job.setOutputFormatClass(TextOutputFormat.class);
    FileInputFormat.addInputPath(job, new Path(args[1]));
    FileOutputFormat.setOutputPath(job, new Path(args[2]));
     job.waitForCompletion(true);
     
     job = new Job(conf, "thirdpass");
     job.setMapOutputKeyClass(LongWritable.class);
     job.setMapOutputValueClass(Text.class);
     job.setOutputKeyClass(LongWritable.class);
     job.setOutputValueClass(LongWritable.class);
     job.setMapperClass(ThirdPassMap.class);
     job.setReducerClass(ThirdPassReduce.class);
     job.setInputFormatClass(TextInputFormat.class);
     job.setOutputFormatClass(TextOutputFormat.class);
    FileInputFormat.addInputPath(job, new Path(args[1]));
     FileInputFormat.addInputPath(job, new Path(args[2]));
    FileOutputFormat.setOutputPath(job, new Path(args[3]));
     job.waitForCompletion(true); 
   }
 }
