 package com.edge.twitter_research.relevance_filter;
 
 
 import com.edge.twitter_research.core.KijiConnection;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 
 import org.apache.hadoop.hbase.HConstants;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import org.kiji.mapreduce.MapReduceJob;
 
 import org.kiji.mapreduce.gather.KijiGatherJobBuilder;
 
 import org.kiji.mapreduce.output.TextMapReduceJobOutput;
 
 import org.kiji.schema.*;
 
 import java.io.IOException;
 
 
 public class TopEmoticonsCalculator extends Configured{
 
 
     public MapReduceJob mapReduceJob = null;
 
     public static Logger logger =
             Logger.getLogger(TopEmoticonsCalculator.class);
 
     public static int pageSize;
     public static int maxVersions;
 
 
     public TopEmoticonsCalculator (String outputFilePath,
                                     String log4jPropertiesFilePath,
                                     String inputTableName){
 
         PropertyConfigurator.configure(log4jPropertiesFilePath);
 
         try{
 
             setConf(HBaseConfiguration.addHbaseResources(new Configuration(true)));
 
             KijiURI tableUri =
                     KijiURI.newBuilder(String.format("kiji://.env/default/%s", inputTableName)).build();
 
             this.mapReduceJob = KijiGatherJobBuilder.create()
                     .withConf(getConf())
                     .withGatherer(TopEmoticonsGatherer.class)
                     .withReducer(TopEmoticonsReducer.class)
                     .withInputTable(tableUri)
                     .withOutput(new TextMapReduceJobOutput(new Path(outputFilePath), 1))
                     .build();
         }catch (IOException ioException){
             logger.error("IO Exception while configuring MapReduce Job", ioException);
             System.exit(1);
         } catch (Exception unknownException){
             logger.error("Unknown Exception while configuring MapReduce Job", unknownException);
             System.exit(1);
         }
     }
 
 
     public static void main(String[] args){
 
         if (args.length < 4){
             System.out.println("Usage: TopEmoticonsCalculator " +
                     "<relevance_filter_root> " +
                    "<input_table_name>" +
                     "<HDFS_output_file_path> " +
                     "<page_size> " +
                     "<max_num_versions>");
             return;
         }
 
        pageSize = Integer.parseInt(args[2]);
         maxVersions = HConstants.ALL_VERSIONS;
        if (args.length == 4)
            maxVersions = Integer.parseInt(args[3]);
 
         TopEmoticonsCalculator topEmoticonsCalculator =
                 new TopEmoticonsCalculator(args[2],
                         args[0] + "/" + Constants.LOG4J_PROPERTIES_FILE_PATH,
                         args[1]);
 
         boolean isSuccessful = false;
         if (topEmoticonsCalculator.mapReduceJob != null){
             try{
                 isSuccessful = topEmoticonsCalculator.mapReduceJob.run();
             }catch (Exception unknownException){
                 logger.error("Unknown Exception while running MapReduce Job", unknownException);
                 System.exit(1);
             }
         }
 
         String result = isSuccessful ? "Successful" : "Failure";
         logger.info(result);
     }
 }
