 package cmu.ds.mr.io;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import cmu.ds.mr.util.Log;
 
 public class MapOutputCollector implements OutputCollector<String, Integer> {
 
   public static final Log LOG =
           new Log("MapOutputCollector.class");
   
  private List<Map<String, Integer>> outlist;
 
   private int numRed;
 
   private String basePath;
 
   public MapOutputCollector(String basePath, int nred) throws IOException {
     
     LOG.setInfo(false);
     
     this.basePath = basePath;
     File file = new File(basePath);
     if(!file.exists())
       file.mkdirs();
     
     numRed = nred;
    outlist = new ArrayList<Map<String, Integer>>();
     for (int i = 0; i < numRed; i++) {
      outlist.add(new TreeMap<String, Integer>());
     }
   }
 
   @Override
   public void collect(String key, Integer value) throws IOException {
     // Assume key is String
     int k = key.toString().hashCode() % numRed;
     if(k < 0)
       k += numRed;
    outlist.get(k).put(key, value);
     
     LOG.info(String.format("MapOutput: key %s\tval %s", key, value));
   }
 
   public void writeToDisk() throws IOException {
     for (int i = 0; i < numRed; i++) {
       BufferedWriter bw = new BufferedWriter(
               new FileWriter(basePath + File.separator + i));
       try {
        Map<String, Integer> map = outlist.get(i);
        for (Entry<String, Integer> en : map.entrySet()) {
          bw.write(String.format("%s\t%d\n", en.getKey(), en.getValue()));
         }
       } finally {
         bw.close();
       }
     }
     
     LOG.info("write to disk finished");
   }
 
 }
