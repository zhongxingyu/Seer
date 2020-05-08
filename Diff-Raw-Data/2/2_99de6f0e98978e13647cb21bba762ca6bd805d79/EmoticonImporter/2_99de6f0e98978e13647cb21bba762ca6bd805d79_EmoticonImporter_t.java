 package com.edge.twitter_research.relevance_filter;
 
 
 import com.edge.twitter_research.core.KijiConnection;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.kiji.mapreduce.KijiTableContext;
 import org.kiji.mapreduce.MapReduceJob;
 import org.kiji.mapreduce.bulkimport.KijiBulkImportJobBuilder;
 import org.kiji.mapreduce.bulkimport.KijiBulkImporter;
 import org.kiji.mapreduce.input.TextMapReduceJobInput;
 import org.kiji.mapreduce.output.DirectKijiTableMapReduceJobOutput;
 import org.kiji.schema.EntityId;
 import org.kiji.schema.KijiURI;
 
 import java.io.IOException;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public class EmoticonImporter extends Configured {
 
     public MapReduceJob mapReduceJob = null;
     public KijiConnection kijiConnection;
 
     public static Logger logger =
             Logger.getLogger(EmoticonImporter.class);
 
     public static class EmoticonBulkImporter
             extends KijiBulkImporter<LongWritable, Text>{
 
         @Override
         public void produce(LongWritable byteOffset,
                             Text line,
                             KijiTableContext kijiTableContext){
             final String[] fields = line.toString().split("\\t");
 
             if (5 != fields.length) {
                 return; // No inserts for this malformed line.
             }
 
             try{
                 final String emoticon = fields[1];
                 final long tweetId = Long.parseLong(fields[2]);
                 final long timeStamp = Long.parseLong(fields[4]);
 
                 final EntityId  entityId
                     = kijiTableContext.getEntityId(emoticon);
 
                 kijiTableContext.put(entityId,
                                 Constants.EMOTICON_OCCURRENCE_COLUMN_FAMILY_NAME,
                                 Constants.EMOTICON_TWEET_ID_COLUMN_NAME,
                                 timeStamp,
                                 tweetId);
 
             }catch (IOException ioException){
                 logger.error("Exception while putting an emoticon",
                         ioException);
             }catch (Exception unknownException){
                 logger.error("Unknown Exception while putting an emoticon",
                         unknownException);
             }
         }
     }
 
 
 
     public EmoticonImporter (String tableLayoutFilePath,
                                 String inputFilePath,
                                 String log4jPropertiesFilePath,
                                 String outputTableName){
 
         PropertyConfigurator.configure(log4jPropertiesFilePath);
 
         try{
 
         setConf(HBaseConfiguration.addHbaseResources(new Configuration(true)));
 
         kijiConnection =  new KijiConnection(tableLayoutFilePath,
                                            outputTableName);
 
         KijiURI tableUri =
                 KijiURI.newBuilder(String.format("kiji://.env/default/%s", outputTableName)).build();
 
             this.mapReduceJob = KijiBulkImportJobBuilder.create()
                     .withConf(getConf())
                     .withInput(new TextMapReduceJobInput(new Path(inputFilePath)))
                     .withOutput(new DirectKijiTableMapReduceJobOutput(tableUri))
                     .withBulkImporter(EmoticonBulkImporter.class)
                     .build();
         }catch (IOException ioException){
             logger.error("IO Exception while configuring MapReduce Job", ioException);
             System.exit(1);
         } catch (Exception unknownException){
             logger.error("Unknown Exception while configuring MapReduce Job");
             System.exit(1);
         }
     }
 
 
     public static void main(String[] args){
 
         if (args.length < 3){
             System.out.println("Usage: EmoticonImporter " +
                                     "<relevance_filter_root> " +
                                     "<HDFS_input_file_path> " +
                                     "<output_table_name>");
             return;
         }
 
         EmoticonImporter emoticonImporter =
                 new EmoticonImporter(args[0] + "/" + Constants.EMOTICON_STORE_TABLE_LAYOUT_FILE_NAME,
                                     args[1],
                                     args[0] + "/" + Constants.LOG4J_PROPERTIES_FILE_PATH,
                                     args[2]);
 
         boolean isSuccessful = false;
         if (emoticonImporter.mapReduceJob != null &&
                 emoticonImporter.kijiConnection.kijiTable != null){
             try{
                 isSuccessful = emoticonImporter.mapReduceJob.run();
             }catch (Exception unknownException){
                 logger.error("Unknown Exception while running MapReduce Job", unknownException);
                 System.exit(1);
             }
         }
 
         String result = isSuccessful ? "Successful" : "Failure";
         logger.info(result);
 
         emoticonImporter.kijiConnection.destroy();
 
     }
 
 
 }
