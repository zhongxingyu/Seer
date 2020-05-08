 package lab;
 
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tamir
  * Date: 1/6/13
  * Time: 1:13 AM
  * To change this template use File | Settings | File Templates.
  */
 public class CrawlRequest {
     public static final int PROGRESS_NEW = 0;
     public static final int PROGRESS_WAITING_TO_START = 1;
     public static final int PROGRESS_WORKING = 2;
     public static final int PROGRESS_FINISHED = 3;
 
     public URL urlToCrawl;
     public boolean ignoreRobots;
     public int progress = PROGRESS_NEW;
     public long totalImages = 0;
     public long totalVideos = 0;
     public long totalDocuments = 0;
     public long totalImagesBytes = 0;
     public long totalVideosBytes = 0;
     public long totalDocumentsBytes = 0;
     public long totalPages = 0;
     public long totalPagesBytes = 0;
     public long totalLinks = 0;
     public float averageRTT = 0;
     public Set<String> domainsConnected;
 
     public CrawlRequest(URL urlToCrawl, boolean ignoreRobots) {
         this.urlToCrawl = urlToCrawl;
         this.ignoreRobots = ignoreRobots;
         this.domainsConnected = new HashSet<String>();
     }
 
     public synchronized void addStat(Resource resource) {
         // add the domain to the set of domains connected (will only add once for each domain)
         domainsConnected.add(resource.url.getHost());
 
         // calculate the new average response time and add a new link
         totalLinks += 1;
         averageRTT = (averageRTT * totalLinks + resource.rtt) / (totalLinks + 1);
 
         switch(resource.type) {
             case Resource.TYPE_DOCUMENT:
                 totalDocuments += 1;
                 totalDocumentsBytes += resource.length;
                break;
             case Resource.TYPE_IMAGE:
                 totalImages += 1;
                 totalImagesBytes += resource.length;
                break;
             case Resource.TYPE_VIDEO:
                 totalVideos += 1;
                 totalVideosBytes += resource.length;
                break;
             case Resource.TYPE_PAGE:
                 totalPages += 1;
                 totalPagesBytes += resource.length;
                break;
         }
 
     }
 }
