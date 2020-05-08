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
   
  private List<Map<String, List<Integer>>> outlist;
 
   private int numRed;
 
   private String basePath;
 
   public MapOutputCollector(String basePath, int nred) throws IOException {
     
     LOG.setInfo(false);
     
     this.basePath = basePath;
     File file = new File(basePath);
     if(!file.exists())
       file.mkdirs();
     
     numRed = nred;
    outlist = new ArrayList<Map<String, List<Integer>>>();
     for (int i = 0; i < numRed; i++) {
      outlist.add(new TreeMap<String, List<Integer>>());
     }
   }
 
   @Override
   public void collect(String key, Integer value) throws IOException {
     // Assume key is String
     int k = key.toString().hashCode() % numRed;
     if(k < 0)
       k += numRed;
    if(outlist.get(k).containsKey(key))
      outlist.get(k).get(key).add(value);
    else {
      List<Integer> list = new ArrayList<Integer>();
      list.add(value);
      outlist.get(k).put(key, list);
    }
     
     LOG.info(String.format("MapOutput: key %s\tval %s", key, value));
   }
 
   public void writeToDisk() throws IOException {
     for (int i = 0; i < numRed; i++) {
       BufferedWriter bw = new BufferedWriter(
               new FileWriter(basePath + File.separator + i));
       try {
        Map<String, List<Integer>> map = outlist.get(i);
        for (Entry<String, List<Integer>> en : map.entrySet()) {
          for(int num : en.getValue())
            bw.write(String.format("%s\t%d\n", en.getKey(), num));
         }
       } finally {
         bw.close();
       }
     }
     
     LOG.info("write to disk finished");
   }
 
 }
