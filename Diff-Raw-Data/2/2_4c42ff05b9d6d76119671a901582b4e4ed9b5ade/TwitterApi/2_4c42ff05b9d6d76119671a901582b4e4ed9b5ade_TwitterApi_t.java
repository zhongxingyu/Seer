 package org.zoneproject.extractor.twitterreader;
 
 /*
  * #%L
  * ZONE-TwitterReader
  * %%
  * Copyright (C) 2012 - 2013 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.twitter.Extractor;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.zoneproject.extractor.utils.Config;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 import org.zoneproject.extractor.utils.ZoneOntology;
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class TwitterApi {
     private static Extractor extractor = new Extractor();
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(TwitterApi.class);
     private static TwitterFactory factory = new TwitterFactory();
     
     /**
      * Get all the items from twitter for an user timeline list
      * @param sources the list of sources URIs
      * @return the list of items
      */
     public static ArrayList<Item> getFlux(String[] sources) {
         ArrayList<Item> result = new ArrayList<Item>();
         
         for(String sourceUri : sources){
             if(sourceUri.startsWith(ZoneOntology.SOURCES_TYPE_TWITTER_AUTHOR)){
                 String user = sourceUri.substring(ZoneOntology.SOURCES_TYPE_TWITTER_AUTHOR.length()+1);
                 result.addAll(getFluxFromUser(user,sourceUri));
                 
             }else if(sourceUri.startsWith(ZoneOntology.SOURCES_TYPE_TWITTER_HASHTAG)){
                 String tag = sourceUri.substring(ZoneOntology.SOURCES_TYPE_TWITTER_HASHTAG.length()+1);
                 result.addAll(getFluxFromSearch("#"+tag,sourceUri));
                 
             }else if(sourceUri.startsWith(ZoneOntology.SOURCES_TYPE_TWITTER_TIMELINE)){
                 result.addAll(getFluxFromTimeline(sourceUri));
                 
             }
 
         }
         return result;
     }
     public static ArrayList<Item> getFluxFromSearch(String search, String sourceUri) {
         ArrayList<Item> result = new ArrayList<Item>();
         try {
             
             Twitter twitter = new TwitterFactory().getInstance();
             twitter.setOAuthConsumer(Config.getVar("Twitter-OAuth-customer"),Config.getVar("Twitter-OAuth-customerKey"));
             twitter.setOAuthAccessToken(new AccessToken(Config.getVar("Twitter-OAuth-access"),Config.getVar("Twitter-OAuth-accessKey")));
 
             Query query = new Query(search);
             QueryResult items = twitter.search(query);
             for(Status r :items.getTweets()){
                 result.add(TwitterApi.getItemFromStatus(r,sourceUri));
             }
             
             return result;
         } catch (TwitterException ex) {
             Logger.getLogger(TwitterApi.class.getName()).log(Level.WARNING, null, ex);
             return result;
         }
     }
     
     public static ArrayList<Item> getFluxFromUser(String user, String sourceUri) {
         ArrayList<Item> result = new ArrayList<Item>();
         try {
             
             Twitter twitter = new TwitterFactory().getInstance();
             twitter.setOAuthConsumer(Config.getVar("Twitter-OAuth-customer"),Config.getVar("Twitter-OAuth-customerKey"));
             twitter.setOAuthAccessToken(new AccessToken(Config.getVar("Twitter-OAuth-access"),Config.getVar("Twitter-OAuth-accessKey")));
 
             List<Status> statusess = twitter.getUserTimeline(user);
             for(Status r :statusess){
                 result.add(TwitterApi.getItemFromStatus(r,sourceUri));
             }
             
             return result;
         } catch (TwitterException ex) {
            logger.warn(ex.getErrorMessage());
             return result;
         }
     }
         
     public static ArrayList<Item> getFluxFromTimeline(String sourceUri) {
         ArrayList<Item> result = new ArrayList<Item>();
         AccessToken userToken = TwitterApi.getAccessToken(sourceUri);
         Twitter twitter = factory.getInstance();
         twitter.setOAuthConsumer(Config.getVar("Twitter-OAuth-customer"),Config.getVar("Twitter-OAuth-customerKey"));
         twitter.setOAuthAccessToken(userToken);
         ResponseList<Status> status;
         try {
             status = twitter.getHomeTimeline();
             for(Status r :status){
                 result.add(TwitterApi.getItemFromStatus(r,sourceUri));
             }
         } catch (Exception ex) {
             logger.info(ex);
         }
         return result;
     }
     
     /**
      * create an item by his twitter Status description, will add hashtags and others "metas"
      * @param s the twitter Status
      * @param source the Uri of the source
      * @return the item created
      */
     private static Item getItemFromStatus(Status s, String source){
         String text = s.getText();
         if(s.isRetweet()){
             text = s.getRetweetedStatus().getText();
         }
         Item res = new Item(source, "https://twitter.com/"+s.getUser().getScreenName()+"/status/"+Long.toString(s.getId()), text, text, s.getCreatedAt());
         String[] hashtags = getHashTags(res.getDescription());
         for(String hashtag: hashtags){
             res.addProp(new Prop(ZoneOntology.PLUGIN_TWITTER_HASHTAG,hashtag,true,true));
         }
         for(String mentioned: TwitterApi.extractor.extractMentionedScreennames(s.getText())){
             res.addProp(new Prop(ZoneOntology.PLUGIN_TWITTER_MENTIONED,"@"+mentioned,true,true));
         }
         if(s.getGeoLocation() != null){
             res.addProp(new Prop(ZoneOntology.PLUGIN_TWITTER_POSITION_LONGITUDE,Double.toString(s.getGeoLocation().getLongitude()),true,true));
             res.addProp(new Prop(ZoneOntology.PLUGIN_TWITTER_POSITION_LATITUDE,Double.toString(s.getGeoLocation().getLatitude()),true,true));
         }
         res.addProp(new Prop(ZoneOntology.PLUGIN_TWITTER_AUTHOR,"@"+s.getUser().getScreenName(),true,true));
         return res;
     }
     
     /**
      * Get the access token for a specified URI
      * @param the twitter URI
      * @return the AccessToken for specified URI
      */
     private static AccessToken getAccessToken(String uri){
         ResultSet results = Database.getRelationsForURI(uri, ZoneOntology.GRAPH_SOURCES);
         String token=null, secret=null;
         while (results.hasNext()) {
             QuerySolution result = results.nextSolution();
             if (result.get("?relation").toString().equals(ZoneOntology.SOURCES_TWITTER_TOKEN)){
                 token = result.get("?object").toString();
             }else if (result.get("?relation").toString().equals(ZoneOntology.SOURCES_TWITTER_TOKEN_SECRET)){
                 secret = result.get("?object").toString();
             }
         }
         if(token == null || secret == null) {
             return null;
         }
         return new AccessToken(token, secret);
     }
     
     /**
      * Get all twitter sources in the database
      * @return List of sources URI
      */
     public static String [] getSources(){
         String query = "SELECT *  WHERE {"
                 + "{?uri rdf:type <"+ZoneOntology.SOURCES_TYPE_TWITTER+">}"
                 + "UNION {?uri rdf:type <"+ZoneOntology.SOURCES_TYPE_TWITTER_TIMELINE+">}"
                 + "UNION {?uri rdf:type <"+ZoneOntology.SOURCES_TYPE_TWITTER_AUTHOR+">}"
                 + "UNION {?uri rdf:type <"+ZoneOntology.SOURCES_TYPE_TWITTER_HASHTAG+">}"
                 + "}";
         logger.info(query);
         ResultSet res = Database.runSPARQLRequest(query, ZoneOntology.GRAPH_SOURCES);
         ArrayList<String> sources = new ArrayList<String>();
         while (res.hasNext()) {
             QuerySolution r = res.nextSolution();
             sources.add(r.get("?uri").toString());
         }
         return sources.toArray(new String[sources.size()]);
     }
 
     /**
      * Extract hashtags from a text
      * @param the tweet to analyse
      * @return list oh hashtags (# included)
      */
     public static String[] getHashTags(String description) {
         List<String> tags = extractor.extractHashtags(description);
         String[]result=new String[tags.size()];
         for(int i=0; i < tags.size();i++){
             result[i] = "#"+tags.get(i);
         }
         return result;
     }
     
     public static void main(String[] args){
         /*String[] res = getLastsSources(5);
         for(String r: res)
             System.out.println(r);*/
         String fileURI = "http://feeds.bbci.co.uk/news/world/rss.xml";
         fileURI = "http://camarade.over-blog.org/rss-articles.xml";
         String source = "http://zone-project.org/model/sources#twitter/timeline/PierreTran";
         ArrayList<Item> result =TwitterApi.getFluxFromUser("HerveKabla", source);//;.getFluxFromTimeline("http://zone-project.org/model/sources#twitter/timeline/uju_");// RSSGetter.getFlux(fileURI);
         
         for(Item i: result){
             logger.info(i);
         }
     }
 }
