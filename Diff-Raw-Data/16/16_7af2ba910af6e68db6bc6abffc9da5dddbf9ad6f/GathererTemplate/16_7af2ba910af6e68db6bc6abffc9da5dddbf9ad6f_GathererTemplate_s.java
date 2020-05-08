 package com.edge.twitter_research.event_detection;
 
 
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.kiji.mapreduce.gather.GathererContext;
 import org.kiji.mapreduce.gather.KijiGatherer;
 import org.kiji.schema.KijiDataRequest;
 import org.kiji.schema.KijiDataRequestBuilder;
 import org.kiji.schema.KijiRowData;
 
 import java.io.IOException;
 
 public class GathererTemplate
         extends KijiGatherer<LongWritable, Text> {
 
 
     @Override
     public void setup(GathererContext<LongWritable, Text> context) throws IOException {
         super.setup(context); // Any time you override setup, call super.setup(context);
 
     }
 
 
     @Override
     public void gather(KijiRowData input, GathererContext<LongWritable, Text> context)
             throws IOException {
 
 
     }
 
 
     @Override
     public KijiDataRequest getDataRequest() {
         final KijiDataRequestBuilder builder = KijiDataRequest.builder();
         builder.newColumnsDef()
                 .withMaxVersions(1)
                 .addFamily(Constants.TWEET_COLUMN_FAMILY_NAME);
         return builder.build();
     }
 
 
     @Override
     public Class<?> getOutputValueClass() {
        return CharSequence.class;
     }
 
     @Override
     public Class<?> getOutputKeyClass() {
         return LongWritable.class;
     }
 
 
 }
 
