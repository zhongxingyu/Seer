 package br.ufmg.dcc.vod.spiderpig.jobs.twitter.maxidbased;
 
 import java.util.ArrayList;
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
 
 public class Requester implements ConfigurableRequester {
 
     private static final int QUOTA_ERROR = 429;
     
     private static final String CONKEY = "worker.job.twitter.conkey";
     private static final String CONSECRET = "worker.job.twitter.consecret";
     private static final String TOKEN = "worker.job.twitter.token";
     private static final String TOKENSECRET = "worker.job.twitter.tokensecret";
     
     private Twitter twitter;
     
     @Override
     public CrawlResult performRequest(CrawlID crawlID) throws QuotaException {
         
         CrawlResultFactory crawlResult = new CrawlResultFactory(crawlID);
         String queryContents = crawlID.getId();
         
         String[] split = queryContents.split("\t");
 
         long maxId;
         String hashtag;
         if (split.length == 2) {
 	        String max = split[0];
 	        hashtag = split[1];
 	        
 	        try {
 	        	maxId = Integer.parseInt(max);
 	        } catch (NumberFormatException nfme) {
 	        	maxId = Long.MAX_VALUE;
 	        }
         } else {
         	hashtag = split[0];
         	maxId = Long.MAX_VALUE;
         }
         
         Query query = new Query(hashtag);
         query.setResultType(Query.RECENT);
         query.setMaxId(maxId);
         query.setCount(100);
         
         StringBuilder returnValue = new StringBuilder();
         List<CrawlID> toQueue = new ArrayList<>();
         int i = 0;
         
         try {
             QueryResult result;
             long maxResultId = Long.MIN_VALUE;
             
             do {
                 result = twitter.search(query);
                 maxResultId = Math.max(maxResultId, result.getMaxId());
                 
                 List<Status> tweets = result.getTweets();
                 for (Status tweet : tweets) {
                     returnValue.append(i++);
                     returnValue.append("\t");
                     returnValue.append(tweet);
                     returnValue.append(System.lineSeparator());
                 }
             } while ((query = result.nextQuery()) != null);
             toQueue.add(CrawlID.newBuilder().setId(maxResultId+"").build());
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
         return crawlResult.buildOK(payloadBuilder.build(), toQueue);
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
