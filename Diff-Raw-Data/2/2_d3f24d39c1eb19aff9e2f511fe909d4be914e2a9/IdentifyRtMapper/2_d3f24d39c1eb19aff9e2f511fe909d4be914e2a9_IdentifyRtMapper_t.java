 package fr.xebia.devoxx.hadoop.mostRt;
 
 import fr.xebia.devoxx.hadoop.mostRt.model.TwitterStream;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 public class IdentifyRtMapper extends Mapper<LongWritable, Text, TwitterStream, Text> {
     private final static Logger LOG = LoggerFactory.getLogger(IdentifyRtMapper.class);
 
     @Override
     protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
         if (value.toString().indexOf("-") > 0 && value.toString().contains("RT")) {
             TwitterStream twitterStream = new TwitterStream();
 
            Text retwittUser = new Text(value.toString().substring(0, value.toString().indexOf("-") - 1));
 
             // Process message
             String rtToProcess = value.toString().substring(value.toString().indexOf("RT") + 3);
 
             twitterStream.setUser(new Text(rtToProcess.substring(rtToProcess.indexOf("@"), rtToProcess.indexOf(":") - 1)));
             twitterStream.setMessage(new Text(rtToProcess.substring(rtToProcess.indexOf(":") + 1)));
 
             context.write(twitterStream, value);
         } else {
             System.out.println("Rejected : " + value.toString());
         }
     }
 }
