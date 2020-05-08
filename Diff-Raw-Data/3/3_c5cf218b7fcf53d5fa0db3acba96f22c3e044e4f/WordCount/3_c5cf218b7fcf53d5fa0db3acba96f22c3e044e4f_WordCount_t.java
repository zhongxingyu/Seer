 package com.zhoujie.test;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 
 public class WordCount {
     public static void main(String[] args) throws Exception {
         String hdfsHost = "localhost:9000";
         String jobTrackerHost = "localhost:9001";
 
         String dirInput = "/wordcount/input";
         String dirOutput = "/wordcount/output";
 
         if (args.length == 1
                 && (args[0].equals("--help") || args[0].equals("-h") || args[0]
                         .equals("/?"))) {
             System.out.println("Usage: WordCount <options>");
             System.out.println();
             System.out.println("Options:");
             System.out.println();
             System.out
                     .println("--input=DIR                   The directory containing the input files for the");
             System.out
                     .println("                              WordCount Hadoop job");
             System.out
                     .println("--output=DIR                  The directory where the results of the WordCount");
             System.out
                     .println("                              Hadoop job will be stored");
             System.out
                     .println("--hdfsHost=HOST               The host<:port> of the HDFS service");
             System.out
                     .println("                              e.g.- localhost:9000");
             System.out
                     .println("--jobTrackerHost=HOST         The host<:port> of the job tracker service");
             System.out
                     .println("                              e.g.- localhost:9001");
             System.out.println();
             System.out.println();
             System.out
                     .println("If an option is not provided through the command prompt the following defaults");
             System.out.println("will be used:");
             System.out.println("--input='/wordcount/input'");
             System.out.println("--output='/wordcount/output'");
             System.out.println("--hdfsHost=localhost:9000");
             System.out.println("--jobTrackerHost=localhost:9001");
 
         } else {
             if (args.length > 0) {
                 for (String arg : args) {
                     if (arg.startsWith("--input=")) {
                         dirInput = WordCount.getArgValue(arg);
                     } else if (arg.startsWith("--output=")) {
                         dirOutput = WordCount.getArgValue(arg);
                     } else if (arg.startsWith("--hdfsHost=")) {
                         hdfsHost = WordCount.getArgValue(arg);
                     } else if (arg.startsWith("--jobTrackerHost=")) {
                         jobTrackerHost = WordCount.getArgValue(arg);
                     }
                 }
             }
 
             JobConf conf = new JobConf(WordCount.class);
             conf.setJobName("WordCount");
 
             String hdfsBaseUrl = "hdfs://" + hdfsHost;
             conf.set("fs.default.name", hdfsBaseUrl);
             conf.set("mapred.job.tracker", jobTrackerHost);
             
 //            String [] inputArr = dirInput.split(",");
 //            Map<String, ArrayList<String>> dirFileMap = new HashMap<String, ArrayList<String>>();
 //            for(String inputStr : inputArr) {
 //                String inputDir = inputStr.substring(0, inputStr.lastIndexOf("/"));
 //                String inputfilename = inputStr.substring(inputStr.lastIndexOf("/") + 1);
 //                FileInputFormat.addInputPath(conf, new Path(inputDir));
 //                ArrayList<String> fileList = dirFileMap.get(inputDir);
 //                if (null == fileList) {
 //                    fileList = new ArrayList<String>();
 //                    dirFileMap.put(inputDir, fileList);
 //                }
 //                fileList.add(inputfilename);
 //            }
 //            for(String inputDir : dirFileMap.keySet()){
 //                ArrayList<String> fileList = dirFileMap.get(inputDir);
 //                String fileStrs = StringUtils.join(fileList.toArray());
 //                conf.set(inputDir, fileStrs);
 //            }
 
 //            FileInputFormat.setInputPathFilter(conf,);
             String [] inputArr = dirInput.split(",");
             Path [] inputPathArr = new Path[inputArr.length];
             for (int i = 0 ; i < inputArr.length ; i ++) {
                 inputPathArr[i] = new Path(inputArr[i]);
             }
//            FileInputFormat.setInputPaths(conf, inputPathArr);
            FileInputFormat.setInputPaths(conf, dirInput);
 
             FileOutputFormat.setOutputPath(conf, new Path(dirOutput));
 
             conf.setMapperClass(WordCountMapper.class);
             conf.setReducerClass(WordCountReducer.class);
 
             conf.setMapOutputKeyClass(Text.class);
             conf.setMapOutputValueClass(IntWritable.class);
 
             conf.setOutputKeyClass(Text.class);
             conf.setOutputValueClass(IntWritable.class);
 
             JobClient.runJob(conf);
         }
     }
 
     private static String getArgValue(String arg) {
         String result = null;
 
         String[] tokens = arg.split("=");
         if (tokens.length > 1) {
             result = tokens[1].replace("'", "").replace("\"", "");
         }
 
         return result;
     }
 }
