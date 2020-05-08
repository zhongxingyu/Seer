 package br.ufmg.dcc.vod.spiderpig.jobs.twitter;
 
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.conf.ConfigurationBuilder;
 import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
 import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
 import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultFactory;
 import br.ufmg.dcc.vod.spiderpig.jobs.PayloadsFactory;
 import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
 import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;
 
 import com.google.common.collect.Sets;
 
 public class TwitterSearchRequester implements ConfigurableRequester {
 
     private static final int QUOTA_ERROR = 429;
     
     private static final String CONKEY = "worker.job.twitter.conkey";
     private static final String CONSECRET = "worker.job.twitter.consecret";
     private static final String TOKEN = "worker.job.twitter.token";
     private static final String TOKENSECRET = "worker.job.twitter.tokensecret";
     
     private Twitter twitter;
     
     @Override
     public CrawlResult performRequest(CrawlID crawlID) throws QuotaException {
         
         CrawlResultFactory crawlResult = new CrawlResultFactory(crawlID);
         String id = crawlID.getId();
         
         Query query = new Query(id);
         query.setCount(100);
         StringBuilder returnValue = new StringBuilder();
         int i = 0;
             
         try {
             QueryResult result;
             do {
                 result = twitter.search(query);
                 List<Status> tweets = result.getTweets();
                 for (Status tweet : tweets) {
                     returnValue.append(i++);
                     returnValue.append(" - ");
                     returnValue.append(tweet);
                     returnValue.append(System.lineSeparator());
                 }
             } while ((query = result.nextQuery()) != null);
         } catch (TwitterException e) {
             int errorCode = e.getErrorCode();
             if (errorCode == QUOTA_ERROR) {
                 throw new QuotaException(e);
             } else {
                 UnableToCrawlException cause = new UnableToCrawlException(e);
                 return crawlResult.buildNonQuotaError(cause);
             }
         }
         
         PayloadsFactory payloadBuilder = new PayloadsFactory();
         payloadBuilder.addPayload(crawlID, returnValue.toString().getBytes());
         return crawlResult.buildOK(payloadBuilder.build(), null);
     }
 
     @Override
     public Set<String> getRequiredParameters() {
         return Sets.newHashSet(CONKEY, CONSECRET, TOKEN, TOKENSECRET);
     }
 
     @Override
     public void configurate(Configuration configuration, 
             ConfigurableBuilder configurableBuilder) {
         ConfigurationBuilder cb = new ConfigurationBuilder();
         
         cb.setOAuthConsumerKey(configuration.getString(CONKEY));
         cb.setOAuthConsumerSecret(configuration.getString(CONSECRET));
         cb.setOAuthAccessToken(configuration.getString(TOKEN));
         cb.setOAuthAccessTokenSecret(configuration.getString(TOKENSECRET));
         
         this.twitter = new TwitterFactory(cb.build()).getInstance();
     }
 }
