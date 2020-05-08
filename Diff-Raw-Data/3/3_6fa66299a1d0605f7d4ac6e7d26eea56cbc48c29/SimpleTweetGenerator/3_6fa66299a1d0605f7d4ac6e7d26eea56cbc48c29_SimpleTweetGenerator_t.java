 package com.edge.twitter_research.core;
 
 import java.util.ArrayList;
 
 import twitter4j.*;
 
 import org.apache.log4j.Logger;
 
 public class SimpleTweetGenerator {
 
     private static Logger logger
             = Logger.getLogger(SimpleTweetGenerator.class);
     private static CrisisMailer crisisMailer
             = CrisisMailer.getCrisisMailer();
 
 
     private static SimpleTweet initializeSimpleTweet(){
         SimpleTweet simpleTweet = new SimpleTweet();
         simpleTweet.setUser(new SimpleUser());
 
         SimplePlace simplePlace = new SimplePlace();
         simplePlace.setBoundingBox(new ArrayList<SimpleGeoLocation>());
         simpleTweet.setPlace(simplePlace);
 
         return simpleTweet;
     }
 
 
     public static SimpleTweet generateSimpleTweet(Status status){
         SimpleTweet simpleTweet = initializeSimpleTweet();
 
         try{
         /*Setting contributors in SimpleTweet*/
         ArrayList<Long> contributors = new ArrayList<Long>();
         for (long contributor : status.getContributors())
             contributors.add(contributor);
         simpleTweet.setContributors(contributors);
 
         /*Setting basic elements in SimpleTweet*/
         simpleTweet.setCreatedAt(status.getCreatedAt().toString());
         simpleTweet.setCurrentUserRetweetId(status.getCurrentUserRetweetId());
         simpleTweet.setId(status.getId());
         simpleTweet.setInReplyToScreenName(status.getInReplyToScreenName());
         simpleTweet.setInReplyToStatusId(status.getInReplyToStatusId());
         simpleTweet.setInReplyToUserId(status.getInReplyToUserId());
         simpleTweet.setRetweetCount(status.getRetweetCount());
         simpleTweet.setSource(status.getSource());
         simpleTweet.setText(status.getText());
         simpleTweet.setIsFavorited(status.isFavorited());
         simpleTweet.setIsPossiblySensitive(status.isPossiblySensitive());
         simpleTweet.setIsRetweet(status.isRetweet());
         simpleTweet.setIsRetweetedByMe(status.isRetweetedByMe());
         simpleTweet.setIsTruncated(status.isTruncated());
 
         /*Setting basic GeoLocation elements*/
         if (status.getGeoLocation() != null){
             simpleTweet.setCoordinatesLatitude(status.getGeoLocation().getLatitude());
             simpleTweet.setCoordinatesLongitude(status.getGeoLocation().getLongitude());
         }
 
         /*Setting SimpleHashtagEntities*/
         ArrayList<SimpleHashtagEntity> hashtagEntities =
                 new ArrayList<SimpleHashtagEntity>();
         for (HashtagEntity hashtagEntity : status.getHashtagEntities()){
             SimpleHashtagEntity simpleHashtagEntity = new SimpleHashtagEntity();
             simpleHashtagEntity.setEnd(hashtagEntity.getEnd());
             simpleHashtagEntity.setStart(hashtagEntity.getStart());
             simpleHashtagEntity.setText(hashtagEntity.getText());
             hashtagEntities.add(simpleHashtagEntity);
         }
         simpleTweet.setHashTagEntities(hashtagEntities);
 
         /*Setting SimpleURLEntities*/
         ArrayList<SimpleURLEntity> urlEntities =
                 new ArrayList<SimpleURLEntity>();
         for (URLEntity urlEntity : status.getURLEntities()){
             SimpleURLEntity simpleURLEntity = new SimpleURLEntity();
             simpleURLEntity.setEnd(urlEntity.getEnd());
             simpleURLEntity.setStart(urlEntity.getStart());
             simpleURLEntity.setURL(urlEntity.getURL());
             simpleURLEntity.setDisplayURL(urlEntity.getDisplayURL());
             simpleURLEntity.setExpandedURL(urlEntity.getExpandedURL());
             urlEntities.add(simpleURLEntity);
         }
         simpleTweet.setUrlEntities(urlEntities);
 
         /*Setting SimpleUserMentionEntities*/
         ArrayList<SimpleUserMentionEntity> userMentionEntities =
                 new ArrayList<SimpleUserMentionEntity>();
         for (UserMentionEntity userMentionEntity : status.getUserMentionEntities()){
             SimpleUserMentionEntity simpleUserMentionEntity = new SimpleUserMentionEntity();
             simpleUserMentionEntity.setEnd(userMentionEntity.getEnd());
             simpleUserMentionEntity.setStart(userMentionEntity.getStart());
             simpleUserMentionEntity.setId(userMentionEntity.getId());
             simpleUserMentionEntity.setName(userMentionEntity.getName());
             simpleUserMentionEntity.setScreenName(userMentionEntity.getScreenName());
             userMentionEntities.add(simpleUserMentionEntity);
         }
         simpleTweet.setUserMentionEntities(userMentionEntities);
 
         /*Setting SimpleMediaEntities*/
         ArrayList<SimpleMediaEntity> mediaEntities =
                 new ArrayList<SimpleMediaEntity>();
         for (MediaEntity mediaEntity : status.getMediaEntities()){
             SimpleMediaEntity simpleMediaEntity = new SimpleMediaEntity();
             simpleMediaEntity.setId(mediaEntity.getId());
             simpleMediaEntity.setType(mediaEntity.getType());
             simpleMediaEntity.setMediaURL(mediaEntity.getMediaURL());
             simpleMediaEntity.setMediaURLHttps(mediaEntity.getMediaURLHttps());
             mediaEntities.add(simpleMediaEntity);
         }
         simpleTweet.setMediaEntities(mediaEntities);
 
         /*Setting SimpleUser*/
         SimpleUser simpleUser = new SimpleUser();
         simpleUser.setId(status.getUser().getId());
         simpleUser.setScreenName(status.getUser().getScreenName());
         simpleUser.setCreatedAt(status.getUser().getCreatedAt().toString());
         simpleUser.setFollowersCount(status.getUser().getFollowersCount());
         simpleUser.setFriendsCount(status.getUser().getFriendsCount());
         simpleUser.setListedCount(status.getUser().getListedCount());
         simpleUser.setProfileImageURL(status.getUser().getProfileImageURL());
         simpleUser.setStatusesCount(status.getUser().getStatusesCount());
         simpleUser.setVerified(status.getUser().isVerified());
         simpleTweet.setUser(simpleUser);
 
         /*Setting SimplePlace*/
         if (status.getPlace() != null){
             SimplePlace simplePlace = new SimplePlace();
             simplePlace.setURL(status.getPlace().getURL());
             simplePlace.setStreetAddress(status.getPlace().getStreetAddress());
             simplePlace.setPlaceType(status.getPlace().getPlaceType());
             simplePlace.setName(status.getPlace().getName());
             simplePlace.setFullName(status.getPlace().getFullName());
             simplePlace.setId(status.getPlace().getId());
             simplePlace.setCountry(status.getPlace().getCountry());
             simplePlace.setCountryCode(status.getPlace().getCountryCode());
 
             ArrayList<SimpleGeoLocation> geoLocationArrayList =
                     new ArrayList<SimpleGeoLocation>();
             GeoLocation[][] geoLocations = status.getPlace().getBoundingBoxCoordinates();
             if (geoLocations != null){
                 for (int i = 0; i < geoLocations.length; i++){
                     for (int j = 0; j < geoLocations[i].length; j++){
                         SimpleGeoLocation simpleGeoLocation
                             = new SimpleGeoLocation(geoLocations[i][j].getLatitude(),
                                                     geoLocations[i][j].getLongitude());
                         geoLocationArrayList.add(simpleGeoLocation);
                     }
                 }
             }
            simplePlace.setBoundingBox(geoLocationArrayList);
             simpleTweet.setPlace(simplePlace);
         }
         }catch (Exception exception){
             logger.error("Exception while converting a Status object to a SimpleTweet object",
                     exception);
             crisisMailer.sendEmailAlert(exception);
         }
 
         return simpleTweet;
     }
 }
