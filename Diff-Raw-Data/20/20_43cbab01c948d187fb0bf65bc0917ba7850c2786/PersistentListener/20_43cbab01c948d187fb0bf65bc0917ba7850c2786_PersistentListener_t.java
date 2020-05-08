 package eu.leads.crawler;
 
 import com.googlecode.flaxcrawler.model.Page;
 import com.likethecolor.alchemy.api.Client;
 import com.likethecolor.alchemy.api.call.SentimentCall;
 import com.likethecolor.alchemy.api.call.type.CallTypeUrl;
 import com.likethecolor.alchemy.api.entity.Response;
 import com.likethecolor.alchemy.api.entity.SentimentAlchemyEntity;
 import eu.leads.crawler.utils.Infinispan;
 import eu.leads.crawler.utils.Web;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.infinispan.notifications.Listener;
 import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
 import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ConcurrentMap;
 
 import static eu.leads.crawler.utils.Infinispan.addListenerForMap;
 import static java.lang.System.getProperties;
 
 /**
  *
  * @author P. Sutra
  *
  */
 @Listener
 public class PersistentListener {
 
     private static ConcurrentMap preprocessingMap = Infinispan.getOrCreatePersistentMap("preprocessingMap");
    private static ConcurrentMap postprocessingMap = Infinispan.getOrCreatePersistentMap("postprocessingMap");
     private static Log log = LogFactory.getLog(PersistentListener.class.getName());
 
     private Client client;
     private List<String> words;
 
 
     public PersistentListener(List<String> l) throws IOException {
         if(getProperties().containsKey("sentimentAnalysisKeyFile")){
             String sentimentAnalysisKeyFile = getProperties().getProperty("sentimentAnalysisKeyFile");
             client = new Client(sentimentAnalysisKeyFile);
         }else{
             throw new IOException("Incorrect properties file.");
         }
         words = l;
         addListenerForMap(this,preprocessingMap);
     }
 
     public boolean isMatching(Page page){
 
         // check for key words.
         String content = page.getContentString().toLowerCase();
         for(String w : words){
             if(!content.contains(w.toLowerCase()))
                 return false;
         }
 
         // check for the appropriate date.
         try{
             String header = page.getHeaders().get("last-modified");
             if(header == null)
                 return false;
             Date now = new Date();
             Date publication = org.apache.http.impl.cookie.DateUtils.parseDate(header);
             Calendar cal = Calendar.getInstance();
             cal.setTime(publication);
             cal.add(Calendar.DATE, 2);
             publication = cal.getTime();
             log.debug("Valid format for " + page.getHeaders());
             if(publication.after(now)){
                 return true;
             }
         } catch (Exception e) {
             log.debug("Invalid format for " + page.getHeaders());
         }
 
         return false;
 
     }
 
     @CacheEntryModified
     public void postProcessPage(CacheEntryModifiedEvent<Object,Object> event){
 
         if(event.isPre())
             return;
 
         try {
 
             URL url = new URL((String) event.getKey());
             Page page = (Page) event.getValue();
 
             if(!isMatching(page))
                 return;
 
            // Page rank
             Double pagerank = (double) Web.pagerank("http://" + url.toURI().getHost());

            // Sentiment analysis
             SentimentCall call = new SentimentCall(new CallTypeUrl(url.toString()));
             Response response = client.call(call);
             SentimentAlchemyEntity entity = (SentimentAlchemyEntity) response.iterator().next();
             Double sentiment = Double.valueOf(entity.getScore().toString());
 
             CrawlResult result = new CrawlResult(pagerank,sentiment);
             log.info("Processed " + url + " " + result);

            postprocessingMap.putIfAbsent(url.toString(), result);
 
         } catch (Exception e) {
             log.debug("An error while parsing a page.");
         }
 
     }
 
 }
