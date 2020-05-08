 package com.digitalpebble.behemoth.commoncrawl;
 
 // Java classes
 import java.io.IOException;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.PathFilter;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.*;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.log4j.Logger;
 import org.commoncrawl.hadoop.io.ARCFileInputFormat;
 import org.commoncrawl.util.shared.ByteArrayUtils;
 
 import com.digitalpebble.behemoth.BehemothDocument;
 import com.digitalpebble.behemoth.DocumentFilter;
 
 public class CommonCrawlConverterJob2012 extends Configured implements Tool {
 
     private static final Logger LOG = Logger
             .getLogger(CommonCrawlConverterJob2012.class);
 
     public static class ConversionMapper extends
             Mapper<Text, BytesWritable, Text, BehemothDocument> {
 
         DocumentFilter filter;
 
        // used to ensure that we don't get more than 120 Counters
        Set<String> mimetypeset = new HashSet<String>();

         public void map(Text key, BytesWritable rawinput, Context context)
                 throws IOException, InterruptedException {
             BehemothDocument newDoc = new BehemothDocument();
             newDoc.setUrl(key.toString());
 
             int indexofHeaderTerminator = ByteArrayUtils.indexOf(
                     rawinput.getBytes(), 0, rawinput.getLength(),
                     "\r\n\r\n".getBytes());
 
             String headersText = new String(rawinput.getBytes(), 0,
                     indexofHeaderTerminator, Charset.forName("UTF-8"));
             byte[] content = Arrays.copyOfRange(rawinput.getBytes(),
                     indexofHeaderTerminator + 4, rawinput.getLength());
             newDoc.setContent(content);
 
             // get the content type from the HTTP HEADERS
             // store the other metadata as well

            // TODO newDoc.setContentType(rawinput.getContentType());

            // TODO set IP address, HTTP headers etc...
             if (filter.keep(newDoc)) {
                 context.write(key, newDoc);
                 context.getCounter("COMMON CRAWL", "KEPT").increment(1l);
             } else
                 context.getCounter("COMMON CRAWL", "FILTERED").increment(1l);
 
         }
 
         @Override
         protected void setup(Context context) throws IOException,
                 InterruptedException {
             super.setup(context);
             filter = DocumentFilter.getFilters(context.getConfiguration());
         }
 
     }
 
     public static class TextConversionMapper extends
             Mapper<Text, Text, Text, BehemothDocument> {
 
         DocumentFilter filter;
 
         public void map(Text key, Text doc, Context context)
                 throws IOException, InterruptedException {
             BehemothDocument newDoc = new BehemothDocument();
             newDoc.setUrl(key.toString());
             newDoc.setText(doc.toString());
             // add the text as content
             newDoc.setContent(doc.getBytes());
             if (filter.keep(newDoc)) {
                 context.write(key, newDoc);
             } else
                 context.getCounter("COMMON CRAWL", "FILTERED").increment(1l);
         }
 
         @Override
         protected void setup(Context context) throws IOException,
                 InterruptedException {
             super.setup(context);
             filter = DocumentFilter.getFilters(context.getConfiguration());
         }
 
     }
 
     public static class ARCFilter implements PathFilter {
 
         public boolean accept(Path path) {
 
             if (!path.getName().endsWith(".arc.gz"))
                 return false;
             return true;
         }
     }
 
     public static class TextFilter implements PathFilter {
 
         public boolean accept(Path path) {
 
             if (!path.getName().startsWith("textData-"))
                 return false;
             return true;
         }
     }
 
     /**
      * Implmentation of Tool.run() method, which builds and runs the Hadoop job.
      * 
      * @param args
      *            command line parameters, less common Hadoop job parameters
      *            stripped out and interpreted by the Tool class.
      * @return 0 if the Hadoop job completes successfully, 1 if not.
      */
     public int run(String[] args) throws Exception {
 
         String outputPath = null;
         String inputPath = null;
 
         // Read the command line arguments.
         if (args.length < 1)
             throw new IllegalArgumentException(
                     "JAR must be passed an input + output path.");
 
         // String inputPath =
         // "s3n://aws-publicdatasets/common-crawl/parse-output/segment/1341690164240/1341817173109_4.arc.gz";
         // "s3n://aws-publicdatasets/common-crawl/parse-output/segment/*/*.arc.gz";
         inputPath = args[0];
         outputPath = args[1];
         boolean binary = true;
         if (args.length > 2) {
             if ("-text".equalsIgnoreCase(args[2]))
                 binary = false;
         }
 
         // Creates a new job configuration for this Hadoop job.
         Job job = new Job(this.getConf());
 
         job.setJarByClass(this.getClass());
 
         // Scan the provided input path for ARC files.
         LOG.info("setting input path to '" + inputPath + "'");
         FileInputFormat.addInputPath(job, new Path(inputPath));
 
         if (binary) {
             FileInputFormat.setInputPathFilter(job, ARCFilter.class);
             job.setInputFormatClass(ARCFileInputFormat.class);
             job.setMapperClass(ConversionMapper.class);
 
         } else {
             FileInputFormat.setInputPathFilter(job, TextFilter.class);
             job.setInputFormatClass(SequenceFileInputFormat.class);
             job.setMapperClass(TextConversionMapper.class);
         }
         // Delete the output path directory if it already exists.
         LOG.info("clearing the output path at '" + outputPath + "'");
 
         FileSystem fs = FileSystem.get(new URI(outputPath), this.getConf());
 
         if (fs.exists(new Path(outputPath)))
             fs.delete(new Path(outputPath), true);
 
         // Set the path where final output 'part' files will be saved.
         LOG.info("setting output path to '" + outputPath + "'");
         FileOutputFormat.setOutputPath(job, new Path(outputPath));
 
         // Set which OutputFormat class to use.
         job.setOutputFormatClass(SequenceFileOutputFormat.class);
 
         // Set the output data types.
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(BehemothDocument.class);
 
         job.setNumReduceTasks(0); // map-only
         return job.waitForCompletion(true) ? 0 : 1;
 
     }
 
     /**
      * Main entry point that uses the {@link ToolRunner} class to run the
      * example Hadoop job.
      */
     public static void main(String[] args) throws Exception {
         int res = ToolRunner.run(new Configuration(),
                 new CommonCrawlConverterJob2012(), args);
         System.exit(res);
     }
 }
