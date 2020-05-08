 package com.edge.twitter_research.relevance_filter;
 
 
 import com.edge.twitter_research.core.SimpleTweet;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.kiji.mapreduce.gather.GathererContext;
 import org.kiji.mapreduce.gather.KijiGatherer;
 import org.kiji.schema.KijiDataRequest;
 import org.kiji.schema.KijiDataRequestBuilder;
 import org.kiji.schema.KijiRowData;
 
 import java.io.IOException;
 
 public class TweetToCSVGatherer
         extends KijiGatherer<LongWritable, Text> {
 
     private double threshold;
 
     @Override
     public void setup(GathererContext<LongWritable, Text> context) throws IOException {
         super.setup(context); // Any time you override setup, call super.setup(context);
 
         Configuration conf = getConf();
         threshold = conf.getFloat("sampling.rate", 100)/100.0;
     }
 
 
     @Override
     public void gather(KijiRowData input, GathererContext<LongWritable, Text> context)
             throws IOException {
 
 
         if (Math.random() < threshold){
 
             SimpleTweet tweet = input.getMostRecentValue(Constants.TWEET_COLUMN_FAMILY_NAME,
                                                             Constants.TWEET_OBJECT_COLUMN_NAME);
 
             String tweetWithoutCommas = tweet.getText().toString()
                                         .replace(",", " ")
                                         .replace("\n", " ")
                                         .replace("\r", " ");
 
             boolean valid = true;
             for (int i = 0; i < tweetWithoutCommas.length(); i++){
                 int codePoint = tweetWithoutCommas.codePointAt(i);
 
                 if (!(codePoint >= 0x0020 && codePoint <= 0x007E)){
                     valid = false;
                     break;
                 }
             }
 
             if (valid){
                 context.write(new LongWritable(tweet.getId()),
                                 new Text(tweetWithoutCommas));
             }
         }
     }
 
 
     @Override
     public KijiDataRequest getDataRequest() {
         // This method is how we specify which columns in each row the gatherer operates on.
         // In this case, we need all versions of the tweet_object:tweet column.
         final KijiDataRequestBuilder builder = KijiDataRequest.builder();
         builder.newColumnsDef()
                 .withMaxVersions(1)
                 .add(Constants.TWEET_COLUMN_FAMILY_NAME,
                         Constants.TWEET_OBJECT_COLUMN_NAME);
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
 
