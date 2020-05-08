 package aic12.project3.service.statistics;
 
 import java.net.URI;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import aic12.project3.common.beans.SentimentProcessingRequest;
 import aic12.project3.common.beans.SentimentRequest;
 import aic12.project3.common.beans.SentimentRequestList;
 import aic12.project3.common.beans.StatisticsBean;
 import aic12.project3.common.config.ServersConfig;
 import aic12.project3.service.util.ManagementConfig;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 
 public class StatisticsImpl implements Statistics
 {
     private WebResource resource;
     @Autowired private ServersConfig serversConfig;
     @Autowired private ManagementConfig config;
 
     private StatisticsBean bean;
 
    /**
     * do constructor work in separate method since autowired beans
     * can't be used in a constructor (not yet wired)
     */
    protected void init() {
         ClientConfig config = new DefaultClientConfig();
         config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
         Client client = Client.create(config);
        
         resource = client.resource(serversConfig.getProperty("databaseServer") +
         		"/" + serversConfig.getProperty("databaseDeployment") + 
         		"/" + serversConfig.getProperty("databaseRequestRestPath") + 
         		"/" + "getall");
     }
 
     @Override
     public StatisticsBean getStatistics()
     {
         return bean == null ? calculateStatistics() : bean;
     }
 
     @Override
     public StatisticsBean calculateStatistics()
     {
         bean = new StatisticsBean();
 
         long totalTime = 0;
         long totalProcessingTime = 0;
         long totalTotalTime = 0;
         int count = 0;
         
         SentimentRequestList list = resource.accept(MediaType.APPLICATION_JSON).get(SentimentRequestList.class);
         
         if (list != null && list.getList().size() > 0)
         {
             for (SentimentRequest request : list.getList())
             {
                 long requestDuration = request.getTimestampRequestFinished() - request.getTimestampRequestSending();
                 totalTime += requestDuration;
                 
                 if (requestDuration > bean.getMaximumDurationOfRequest())
                 {
                     bean.setMaximumDurationOfRequest(requestDuration);
                 }
                 if (requestDuration < bean.getMinimumDurationOfRequest() || bean.getMinimumDurationOfRequest() == -1)
                 {
                     bean.setMinimumDurationOfRequest(requestDuration);
                 }
                 
                 if (request.getSubRequestsProcessed() != null) // TODO chech if this is correct
                 {
                     for (SentimentProcessingRequest procrequest : request.getSubRequestsProcessed())
                     {
                         totalProcessingTime += procrequest.getTimestampAnalyzed() - procrequest.getTimestampDataFetched();
                         totalTotalTime += procrequest.getTimestampAnalyzed() - procrequest.getTimestampStartOfAnalysis();
                         count += procrequest.getNumberOfTweets();
                     }
                 }
                 
                 totalTotalTime += requestDuration - request.getTimestampProcessingDone() + request.getTimestampProcessingStart();
             }
             
             bean.setAverageDurationPerRequest(totalTime / list.getList().size());
             bean.setAverageProcessingDurationPerTweet(totalProcessingTime / count);
             bean.setAverageTotalDurationPerTweet(totalTotalTime / count);
         }
         else
         {
             bean.setAverageDurationPerRequest(Long.parseLong(config.getProperty("defaultAverageDurationPerRequest")));
             bean.setAverageProcessingDurationPerTweet(Long.parseLong(config.getProperty("defaultAverageProcessingDurationPerTweet")));
             bean.setAverageTotalDurationPerTweet(Long.parseLong(config.getProperty("defaultAverageTotalDurationPerTweet")));
         }
 
         return bean;
     }
     
     public long getNumberOfTweetsForRequest(SentimentRequest req){
 		URI uri = UriBuilder.fromUri(serversConfig.getProperty("databaseServer"))
				.path(serversConfig.getProperty("databaseDeployment"))
 				.path(serversConfig.getProperty("databaseTweetRestPath"))
 				.path("getnumberoftweetforrequest")
 				.build();
 
 		// Jersey Client Config
 		ClientConfig config = new DefaultClientConfig();
 		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
 		Client client = Client.create(config);
 
 		WebResource resource = client.resource(uri);
 		Long resp = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(Long.class, req);
 		return resp;
     }
 }
