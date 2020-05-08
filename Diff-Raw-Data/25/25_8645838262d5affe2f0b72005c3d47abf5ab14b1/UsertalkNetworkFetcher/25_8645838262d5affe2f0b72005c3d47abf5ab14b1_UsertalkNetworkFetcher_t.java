 package edu.mit.cci.visualize.wiki.fetcher;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import edu.mit.cci.visualize.wiki.collector.UsertalkEdge;
 import edu.mit.cci.visualize.wiki.util.Const;
 import edu.mit.cci.visualize.wiki.util.Pair;
 import edu.mit.cci.visualize.wiki.util.WikiAPIClient;
 import edu.mit.cci.visualize.wiki.xml.Api;
 import edu.mit.cci.visualize.wiki.xml.XMLTransformer;
 
 public class UsertalkNetworkFetcher {
 
     private static final int NUM_THREADS = 16;
     private static final Logger LOG = LoggerFactory.getLogger(UsertalkNetworkFetcher.class.getName());
 
     private final String lang;
     private final List<String> sanitizedUserIDs;
     private final int numberOfUsersInNetwork;
     private final Map<Pair<String>, Integer> talkMatrix = Maps.newConcurrentMap();
     private final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(NUM_THREADS);
     private final CacheManager cacheManager = CacheManager.getInstance();
     private final Cache userTalkCache = cacheManager.getCache("usertalks");
 
     public UsertalkNetworkFetcher(final String lang, final List<String> userIDs) {
         this.lang = lang;
         sanitizedUserIDs = sanitizeUserIDs(userIDs);
         numberOfUsersInNetwork = sanitizedUserIDs.size();
     }
 
     public List<UsertalkEdge> getNetwork() {
         try{
             for (String userID1 : sanitizedUserIDs) {
                 for (String userID2 : sanitizedUserIDs) {
                     if (StringUtils.equals(userID1, userID2)) {
                         continue;
                     }
                     newFixedThreadPool.execute(new GetUserTalk(userID1, userID2));
                 }
             }
         } finally {
             shutdownThreadPool();
             cacheManager.shutdown();
         }
         return collectUserTalkEdges();
     }
 
     private void shutdownThreadPool() {
         newFixedThreadPool.shutdown();
         try {
             newFixedThreadPool.awaitTermination(1, TimeUnit.MINUTES);
         } catch (InterruptedException e) {
             LOG.error("Error while shutting down Threadpool", e);
         }
         while (!newFixedThreadPool.isTerminated()) {
             // wait for all tasks or timeout
         }
     }
 
     private List<UsertalkEdge> collectUserTalkEdges() {
         List<UsertalkEdge> userTalkEdges = Lists.newArrayList();
         for (int i = 0; i < numberOfUsersInNetwork; i++) {
             for (int j = i + 1; j < numberOfUsersInNetwork; j++) {
                 final String from = sanitizedUserIDs.get(i);
                 final String to = sanitizedUserIDs.get(j);
                 final Integer direction1 = talkMatrix.get(new Pair<String>(from, to));
                 final Integer direction2 = talkMatrix.get(new Pair<String>(to, from));
                 int totalConversations = direction1 + direction2; //sum of talk in both directions
                 if (totalConversations > 0) {
                     userTalkEdges.add(new UsertalkEdge(from, to, totalConversations));
                 }
             }
         }
         return userTalkEdges;
     }
 
     private List<String> sanitizeUserIDs(final List<String> userIDs) {
         List<String> sanitizedUserIDs = Lists.newArrayList();
         for (String id : userIDs) {
            sanitizedUserIDs.add(id.replaceAll("_", "_")); // XXX SANI FAIL?
         }
         return sanitizedUserIDs;
     }
 
     private final class GetUserTalk implements Runnable {
 
         private final Pair<String> userCommunicationPair;
 
         public GetUserTalk(final String from, final String to) {
             userCommunicationPair = new Pair<String>(from, to);
         }
 
         @Override
         public void run() {
             final Element pairFromCache = userTalkCache.get(userCommunicationPair);
             if(pairFromCache != null) {
                 talkMatrix.put(userCommunicationPair, (Integer) pairFromCache.getValue());
             } else {
                 int numberOfRevisions = downloadPairCommunication();
                 talkMatrix.put(userCommunicationPair, numberOfRevisions);
             }
         }
 
         private int downloadPairCommunication() {
             final WikiAPIClient wikiAPIClient = new WikiAPIClient(new DefaultHttpClient(), false);
             final String xml = getUserTalkContribs(userCommunicationPair, wikiAPIClient);
             int numberOfRevisions = 0;
             if (xml.indexOf("<revisions>") > 0) {
                 Api revisionFromXML;
                 try {
                     revisionFromXML = XMLTransformer.getRevisionFromXML(xml);
                     numberOfRevisions = revisionFromXML.getAllRevisionsForRequest().size();
                 } catch (Exception e) {
                     LOG.error("Exception while executing Fetch Thread", e);
                 }
             }
             userTalkCache.put(new Element(userCommunicationPair, numberOfRevisions));
             userTalkCache.flush();
             return numberOfRevisions;
         }
 
         private String getUserTalkContribs(final Pair<String> userCommunicationPair,
                                            final WikiAPIClient wikiAPIClient) {
             try {
                 final String from = URLEncoder.encode(userCommunicationPair.getFirst(), Const.ENCODING);
                 final String to = URLEncoder.encode(userCommunicationPair.getSecond(), Const.ENCODING);
                 String urlStr = "http://" + lang
                         + ".wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&titles=User%20talk:"
                         + to + "&rvlimit=500&rvprop=flags%7Ctimestamp%7Cuser%7Csize&rvuser=" + from;
                 return wikiAPIClient.executeHTTPRequest(urlStr);
             } catch (UnsupportedEncodingException e) {
                 LOG.error("UnsupportedEncodingException", e);
             }
             return "";
         }
     }
 }
