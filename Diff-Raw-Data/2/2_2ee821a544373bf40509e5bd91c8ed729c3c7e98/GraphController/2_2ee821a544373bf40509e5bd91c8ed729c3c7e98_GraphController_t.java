 package org.torproject.ernie.web;
 
 import java.io.*;
 import java.util.*;
 
 import org.rosuda.REngine.Rserve.*;
 import org.rosuda.REngine.*;
 
 import org.torproject.ernie.util.ErnieProperties;
 
 public class GraphController {
 
   /* Singleton instance and getInstance method of this class. */
   private static GraphController instance = new GraphController();
   public static GraphController getInstance() {
     return instance;
   }
 
   /* Host and port where Rserve is listening. */
   private String rserveHost;
   private int rservePort;
 
   /* Some parameters for our cache of graph images. */
   private String cachedGraphsDirectory;
   private int maxCacheSize;
   private int minCacheSize;
   private long maxCacheAge;
   private int currentCacheSize;
   private long oldestGraph;
 
   protected GraphController ()  {
 
     /* Read properties from property file. */
     ErnieProperties props = new ErnieProperties();
     this.cachedGraphsDirectory = props.getProperty("cached.graphs.dir");
     this.maxCacheSize = props.getInt("max.cache.size");
     this.minCacheSize = props.getInt("min.cache.size");
     this.maxCacheAge = (long) props.getInt("max.cache.age");
     this.rserveHost = props.getProperty("rserve.host");
     this.rservePort = props.getInt("rserve.port");
 
     /* Clean up cache on startup. */
     this.cleanUpCache();
   }
 
   /* Generate a graph using the given R query that has a placeholder for
    * the absolute path to the image to be created. */
   public byte[] generateGraph(String rQuery, String imageFilename) {
 
     /* Check if we need to clean up the cache first, or we might give
      * someone an old grpah. */
     if (this.currentCacheSize > this.maxCacheSize ||
         (this.currentCacheSize > 0 && System.currentTimeMillis()
         - this.oldestGraph > this.maxCacheAge * 1000L)) {
       this.cleanUpCache();
     }
 
     /* See if we need to generate this graph. */
     File imageFile = new File(this.cachedGraphsDirectory + "/"
         + imageFilename);
     if (!imageFile.exists()) {
 
       /* We do. Update the R query to contain the absolute path to the file
        * to be generated, create a connection to Rserve, run the R query,
        * and close the connection. The generated graph will be on disk. */
       rQuery = String.format(rQuery, imageFile.getAbsolutePath());
       try {
         RConnection rc = new RConnection(rserveHost, rservePort);
         rc.eval(rQuery);
         rc.close();
       } catch (RserveException e) {
         return null;
       }
 
       /* Check that we really just generated the file */
       if (!imageFile.exists()) {
         return null;
       }
 
       /* Update our graph counter. */
       this.currentCacheSize++;
     }
 
     /* Read the image from disk and write it to a byte array. */
     byte[] result = null;
     try {
       BufferedInputStream bis = new BufferedInputStream(
           new FileInputStream(imageFile), 1024);
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       byte[] buffer = new byte[1024];
       int length;
       while ((length = bis.read(buffer)) > 0) {
         baos.write(buffer, 0, length);
       }
       result = baos.toByteArray();
     } catch (IOException e) {
       return null;
     }
 
     /* Return the graph bytes. */
     return result;
   }
 
   /* Clean up graph cache by removing all graphs older than maxCacheAge
    * and then the oldest graphs until we have minCacheSize graphs left.
    * Also update currentCacheSize and oldestGraph. */
   public void cleanUpCache() {
 
     /* Check if the cache is empty first. */
     File[] filesInCache = new File(this.cachedGraphsDirectory).
         listFiles();
     if (filesInCache.length == 0) {
       this.currentCacheSize = 0;
       this.oldestGraph = System.currentTimeMillis();
       return;
     }
 
     /* Sort graphs in cache by the time they were last modified. */
     List<File> graphsByLastModified = new LinkedList<File>(
         Arrays.asList(filesInCache));
     Collections.sort(graphsByLastModified, new Comparator<File>() {
       public int compare(File a, File b) {
         return a.lastModified() < b.lastModified() ? -1 :
             a.lastModified() > b.lastModified() ? 1 : 0;
       }
     });
 
     /* Delete the graphs that are either older than maxCacheAge and then
      * as many graphs as necessary to shrink to minCacheSize graphs. */
     long cutOffTime = System.currentTimeMillis()
         - this.maxCacheAge * 1000L;
     while (!graphsByLastModified.isEmpty()) {
       File oldestGraphInList = graphsByLastModified.remove(0);
      if (oldestGraphInList.lastModified() >= cutOffTime &&
           graphsByLastModified.size() < this.minCacheSize) {
         break;
       }
       oldestGraphInList.delete();
     }
 
     /* Update currentCacheSize and oldestGraph that we need to decide when
      * we should next clean up the graph cache. */
     this.currentCacheSize = graphsByLastModified.size();
     if (!graphsByLastModified.isEmpty()) {
       this.oldestGraph = graphsByLastModified.get(0).lastModified();
     } else {
       this.oldestGraph = System.currentTimeMillis();
     }
   }
 }
 
