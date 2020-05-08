 package edu.nyu.cloud;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 
 public class GraphBuilder
 {
     private static final Pattern TITLE_PATTERN  = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);
     private static final Pattern LINK_PATTERN   = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.DOTALL);
     private static final String  INIT_PAGE_RANK = "1"; 
     
     public static class GraphMapper extends Mapper<Object, Text, Text, Text>
     {
         private static final Log LOG = LogFactory.getLog(GraphMapper.class);
         
         // Output record format: link \t pagerank \t outlink1 \t outlink2 \t ...
         public void map(Object key, Text value, Context context) throws IOException, InterruptedException
         {
             String pageBody = value.toString();
             
             String pageTitle;
             Matcher matcher = TITLE_PATTERN.matcher(pageBody);
             if (matcher.find())
             {
                pageTitle = matcher.group().replaceAll("</?title>", "").trim();
             } 
             else
             {
                 LOG.warn("No title in document " + value.toString());
                 return;
             }
 
            StringBuilder outboundLinks = new StringBuilder(INIT_PAGE_RANK);
             matcher = LINK_PATTERN.matcher(pageBody);
             while (matcher.find())
             {
                 String link = matcher.group().replaceAll("[\\[\\]]", "");
                 int pipe = link.indexOf("|");
                 if (pipe > -1)
                 {
                     link = link.substring(0, pipe);
                 }
                outboundLinks.append(PageRank.DELIMITER).append(link);
             }
 
            context.write(new Text(pageTitle), new Text(outboundLinks.toString()));
         }
     }
 
     public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
     {
         PageRank.printArray(args);
         Configuration conf = new Configuration();
         String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
         if (otherArgs.length != 3) {
           System.err.println("Usage: graph <in> <out>");
           System.exit(2);
         }
         Job job = new Job(conf, "graph builder");
         job.setJarByClass(GraphBuilder.class);
         job.setMapperClass(GraphMapper.class);
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
         FileInputFormat.addInputPath(job, new Path(otherArgs[1]));
         FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));
         System.exit(job.waitForCompletion(true) ? 0 : 1);
     }
     
 }
