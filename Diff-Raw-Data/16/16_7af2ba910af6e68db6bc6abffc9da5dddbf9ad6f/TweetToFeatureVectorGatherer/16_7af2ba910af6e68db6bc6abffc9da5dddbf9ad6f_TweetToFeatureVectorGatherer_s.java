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
 
 public class TweetToFeatureVectorGatherer
         extends KijiGatherer<LongWritable, Text> {
 
     private double threshold;
     private TweetFeatureVectorGenerator tweetFeatureVectorGenerator;
     private boolean generatingTrainingSet;
 
     @Override
     public void setup(GathererContext<LongWritable, Text> context) throws IOException {
         super.setup(context); // Any time you override setup, call super.setup(context);
 
         Configuration conf = getConf();
         threshold = conf.getFloat("sampling.rate", 100)/100.0;
         generatingTrainingSet = conf.getBoolean("generating.training.set", false);
         tweetFeatureVectorGenerator = new TweetFeatureVectorGenerator();
     }
 
 
     @Override
     public void gather(KijiRowData input, GathererContext<LongWritable, Text> context)
             throws IOException {
 
         String relevanceLabel =
                 input.getMostRecentValue(Constants.TWEET_COLUMN_FAMILY_NAME,
                         Constants.TWEET_RELEVANCE_LABEL_COLUMN_NAME);
 
         if (generatingTrainingSet){
 
             if (relevanceLabel != null &&
                     (relevanceLabel.equals(Constants.RELEVANT_RELEVANCE_LABEL) ||
                             relevanceLabel.equals(Constants.NOT_RELEVANT_RELEVANCE_LABEL))){
                 SimpleTweet tweet = input.getMostRecentValue(Constants.TWEET_COLUMN_FAMILY_NAME,
                                                                 Constants.TWEET_OBJECT_COLUMN_NAME);
                 String tweetFeatureVector = tweetFeatureVectorGenerator.getFeatureVector(tweet);
 
                 context.write(new LongWritable(tweet.getId()),
                                 new Text(tweetFeatureVector));
             }
         }else{
 
             if (relevanceLabel == null){
                 if (Math.random() < threshold){
 
                     SimpleTweet tweet =
                             input.getMostRecentValue(Constants.TWEET_COLUMN_FAMILY_NAME,
                                                     Constants.TWEET_OBJECT_COLUMN_NAME);
 
                     String tweetFeatureVector = tweetFeatureVectorGenerator
                             .getFeatureVector(tweet);
 
                     context.write(new LongWritable(tweet.getId()),
                             new Text(tweetFeatureVector));
                 }
             }
         }
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
