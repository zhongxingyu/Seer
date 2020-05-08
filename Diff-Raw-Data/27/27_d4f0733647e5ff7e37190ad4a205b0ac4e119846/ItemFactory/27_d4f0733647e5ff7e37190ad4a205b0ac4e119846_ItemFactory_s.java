 package eu.socialsensor.framework.common.factories;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 import eu.socialsensor.framework.common.domain.DyscoRequest;
 import eu.socialsensor.framework.common.domain.Feed;
 import eu.socialsensor.framework.common.domain.Item;
 import eu.socialsensor.framework.common.domain.Keyword;
 import eu.socialsensor.framework.common.domain.MediaItem;
 import eu.socialsensor.framework.common.domain.PlatformUser;
 import eu.socialsensor.framework.common.domain.Source;
 import eu.socialsensor.framework.common.domain.StreamUser;
 import eu.socialsensor.framework.common.domain.Timeslot;
 import eu.socialsensor.framework.common.domain.Vote;
 import eu.socialsensor.framework.common.influencers.Influencer;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  * @author etzoannos - e.tzoannos@atc.gr
  */
 public class ItemFactory {
 
     static Gson gson = new GsonBuilder()
             .excludeFieldsWithoutExposeAnnotation()
             .create();
 
     public static synchronized Item create(String json) {
         synchronized (gson) {
             Item item = gson.fromJson(json, Item.class);
             return item;
         }
     }
 
     public static MediaItem createMediaItem(String json) {
         synchronized (gson) {
             MediaItem item = gson.fromJson(json, MediaItem.class);
             return item;
         }
     }
 
     public static synchronized StreamUser createUser(String json) {
         synchronized (gson) {
             StreamUser item = gson.fromJson(json, StreamUser.class);
             return item;
         }
     }
 
     public static synchronized Feed createFeed(String json) {
         synchronized (gson) {
             Feed feed = gson.fromJson(json, Feed.class);
             return feed;
         }
     }
 
     public static synchronized DyscoRequest createDyscoRequest(String json) {
         synchronized (gson) {
             DyscoRequest request = gson.fromJson(json, DyscoRequest.class);
             return request;
         }
     }
 
     public static synchronized Influencer createInfluencer(String json) {
         synchronized (gson) {
             Influencer influencer = gson.fromJson(json, Influencer.class);
             return influencer;
         }
     }
 
     public static synchronized Keyword createKeyword(String json) {
         synchronized (gson) {
             Keyword keyword = gson.fromJson(json, Keyword.class);
             return keyword;
         }
     }
 
     public static synchronized Source createSource(String json) {
         synchronized (gson) {
             Source source = gson.fromJson(json, Source.class);
             return source;
         }
     }
 
     public static synchronized PlatformUser createPlatformUser(String json) {
         synchronized (gson) {
             PlatformUser user = gson.fromJson(json, PlatformUser.class);
             return user;
         }
     }
 
     public static synchronized Timeslot createTimeslot(String json) {
         synchronized (gson) {
             Timeslot timeslot = gson.fromJson(json, Timeslot.class);
             return timeslot;
         }
     }
 
     public static synchronized List<Vote> createVoteList(String json) {
         synchronized (gson) {
 
 
             Type listType = new TypeToken<ArrayList<Vote>>() {
             }.getType();
 
             List<Vote> voteList = gson.fromJson(json, listType);
 
             if (voteList == null) {
                 voteList = new ArrayList<Vote>();
             }
             return voteList;
         }
     }
 
     public static void main(String... args) {
 
         List<Vote> voteList = new ArrayList<Vote>();
         Vote vote1 = new Vote();
         vote1.setComment("hello1");
         vote1.setItemId("id1");
         vote1.setUsername("stratos");
         vote1.setDate(new Date());
         vote1.setPolarity(1);
 
         Vote vote2 = new Vote();
         vote2.setComment("hello2");
         vote2.setItemId("id2");
         vote2.setUsername("stratos2");
         vote2.setDate(new Date());
         vote2.setPolarity(0);
 
         voteList.add(vote1);
         voteList.add(vote2);
 
         String json = new Gson().toJson(voteList);
 
         System.out.println(json);
 
         List<Vote> result = ItemFactory.createVoteList(json);
         List<Vote> result2 = ItemFactory.createVoteList("");
         List<Vote> result3 = ItemFactory.createVoteList(null);
 
 
         System.out.println(result);
         System.out.println(result2);
         System.out.println(result3);
 
 
 
     }
 }
