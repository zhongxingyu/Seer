 package IonCannon.mapreduce.sampling;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapreduce.Mapper;
 import redis.clients.jedis.Jedis;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 public class SamplingMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
 
     private static final String SEPARATOR = "[,\t]";
     private static Pattern pattern = Pattern.compile(SEPARATOR);
     private int strengthToLinkFactor = 10;
     private int numberOfLinksPerCategory = 1500;
 
     @Override
     public void setup(Context context) {
         Configuration conf = context.getConfiguration();
         String strength = conf.get("strengthToLinkFactor");
 
         if (strength.length() > 0) {
             int tmp = Integer.parseInt(strength);
 
             if (tmp != 0)
                 strengthToLinkFactor = tmp;
         }
 
         String numOfLinks = conf.get("numberOfLinksPerCategory");
 
         if (numOfLinks.length() > 0) {
             int tmp = Integer.parseInt(numOfLinks);
 
             if (tmp != 0)
                 numberOfLinksPerCategory = tmp;
         }
     }
 
     @Override
     public void map(LongWritable line, Text input, Context context) throws IOException, InterruptedException {
         System.out.println( input.toString());
         String[] parsedConfigs = pattern.split(input.toString());
 
         Float[] config = new Float[parsedConfigs.length - 1];
         long userID = Long.parseLong(parsedConfigs[0]);
 
         for (int i = 1; i < parsedConfigs.length; i++) {
             config[i - 1] = Float.parseFloat(parsedConfigs[i]);
         }
 
         String redisHost = context.getConfiguration().get("redisHost");
         Jedis con = new Jedis(redisHost);
 
         for (int i = 0; i < config.length; i++) {
             float numberOfLinksForCategory = config[i] * strengthToLinkFactor;
             HashSet<Integer> linksInCategory = new HashSet<Integer>();
 
             while (true) {
                 //got enough links
                 if (linksInCategory.size() >= numberOfLinksForCategory) {
                     break;
                 }
 
                 Random random = new Random();
 
                 //next random link index in this category
                 int nextLinkIndex = random.nextInt(numberOfLinksPerCategory) + (i * numberOfLinksPerCategory );
 
                 if (linksInCategory.contains(nextLinkIndex)) {
                     continue;
                 } else {
                     linksInCategory.add(nextLinkIndex);
                     String categoryIndex = new Integer(i).toString();
                     String linkIndexInCategory = new Integer(nextLinkIndex).toString();
                     String output = categoryIndex + "," + linkIndexInCategory;
 
                     long currentTime = System.currentTimeMillis() / 1000L;
                     long randomOffset = (long) (Math.random() * ((864000)));
                     long timestamp = currentTime - randomOffset;
                     output += "," + timestamp;
 
                     con.set("urls." + linkIndexInCategory + ".timestamp", new Long(timestamp).toString());
 
                    con.rpush("user."+ userID +".links",linkIndexInCategory);
 
 
                     context.write(new LongWritable(userID), new Text(output));
                 }
             }
         }
 
         con.disconnect();
     }
 }
