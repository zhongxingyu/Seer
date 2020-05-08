 package IonCannon.mapreduce.mapping;
 
 import IonCannon.io.RedisOutputFormat;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import java.awt.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 public class SampleToUrlReducer extends Reducer<LongWritable, LongWritable, String, String[]> {
 
    private static final String SEPARATOR = "[,]";
     private static Pattern pattern = Pattern.compile(SEPARATOR);
     private static int numberOfLinksPerCategory = 1500;
 
     HashMap<Integer, ArrayList<String>> urlMappings = new HashMap<Integer, ArrayList<String>>();
     HashMap<Integer, ArrayList<String>> titleMappings = new HashMap<Integer, ArrayList<String>>();
 
 
     @Override
     public void setup(Context context) throws IOException{
 
         String numOfLinks = context.getConfiguration().get("numberOfLinksPerCategory");
 
         if (numOfLinks.length() > 0) {
             int tmp = Integer.parseInt(numOfLinks);
 
             if (tmp != 0)
                 numberOfLinksPerCategory = tmp;
         }
 
        Path mappingPath = new Path("Laser/sampling/mapping");
         FileSystem fs = FileSystem.get(context.getConfiguration());
 
         FileStatus[] stats = fs.listStatus(mappingPath);
 
         for(int catIndex = 0; catIndex < stats.length; catIndex++) {
             Path mappingFilePath = stats[catIndex].getPath();
             FSDataInputStream input = fs.open(mappingFilePath);
 
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "UTF8"));
 
             String inputLine;
 
             while ((inputLine = bufferedReader.readLine()) != null) {
                 String[] map = pattern.split(inputLine);
 
                 if(map.length != 2) {
                     System.err.println("Failed mapping: " + mappingFilePath.toString() + " " + inputLine);
                     continue;
                 }
 
                 ArrayList<String> urls = urlMappings.get(catIndex) != null ? urlMappings.get(catIndex) : new ArrayList<String>();
                 ArrayList<String> titles = titleMappings.get(catIndex) != null ? titleMappings.get(catIndex) : new ArrayList<String>();
 
                 urls.add(map[0]);
                 titles.add(map[1]);
 
                 urlMappings.put(catIndex,urls);
                 titleMappings.put(catIndex, titles);
             }
         }
     }
 
     @Override
     public void reduce(LongWritable categoryId, Iterable<LongWritable> linkIds, Context context) throws IOException, InterruptedException{
         ArrayList<String> urls = urlMappings.get((int)categoryId.get());
         ArrayList<String> titles = titleMappings.get((int)categoryId.get());
 
         for(LongWritable linkId : linkIds) {
             long actualLinkId = (categoryId.get() * numberOfLinksPerCategory) + linkId.get();
 
             String[] output = new String[]{new String(urls.get((int)linkId.get())), new String(titles.get((int)linkId.get()))};
             System.out.println(output[0] + " " + output[1]);
 
             context.write(new String(new Long(actualLinkId).toString()), output);
         }
     }
 }
