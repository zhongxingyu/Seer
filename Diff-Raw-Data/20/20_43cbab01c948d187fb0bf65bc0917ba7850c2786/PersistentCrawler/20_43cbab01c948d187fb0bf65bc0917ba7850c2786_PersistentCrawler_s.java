 package eu.leads.crawler;
 
 import com.googlecode.flaxcrawler.DefaultCrawler;
 import com.googlecode.flaxcrawler.model.CrawlerTask;
 import com.googlecode.flaxcrawler.model.Page;
 import eu.leads.crawler.utils.Infinispan;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.net.HttpURLConnection;
 import java.util.concurrent.ConcurrentMap;
 
 
 /**
 * @author P.Sutra
  *
 */
 public class PersistentCrawler extends DefaultCrawler {
 
     private static ConcurrentMap preprocessingMap = Infinispan.getOrCreatePersistentMap("preprocessingMap");
 
     private static Log log = LogFactory.getLog(PersistentCrawler.class.getName());
 
     public PersistentCrawler(){
     }
 
     private boolean isMatching(Page page){
         if(page.getContent() == null)
             return false;
         return true;
     }
 
     @Override
     protected void afterCrawl(CrawlerTask crawlerTask, Page page) {
         super.afterCrawl(crawlerTask, page);
 
         if ( page == null
              || page.getResponseCode() != HttpURLConnection.HTTP_OK
             || ! isMatching(page)){  // this pages violated the crawler constriants (size, etc..).
             return;
         }
 
         log.info("Crawled: " + page.getUrl().toString());
 
         preprocessingMap.putIfAbsent(page.getUrl().toString(), page);
 
     }
 
     @Override
     public boolean shouldCrawl(CrawlerTask task, CrawlerTask parent){
         if(preprocessingMap.containsKey(task.getUrl().toString())){
             log.debug("Page already crawled: " + task.getUrl().toString() + " ; thrashing.");
             return false;
         }
         // return super.shouldCrawl(task,parent);
         return task.getDomain() != null && parent.getDomain() != null;
     }
 
 }
