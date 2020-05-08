 package toctep.skynet.backend.bll;
 
 import toctep.skynet.backend.dal.dao.DaoFacade;
 import toctep.skynet.backend.dal.dao.TweetDao;
 import toctep.skynet.backend.dal.dao.impl.mysql.DaoFacadeImpl;
 import toctep.skynet.backend.dal.domain.BoundingBox;
 import toctep.skynet.backend.dal.domain.BoundingBoxType;
 import toctep.skynet.backend.dal.domain.Country;
 import toctep.skynet.backend.dal.domain.Geo;
 import toctep.skynet.backend.dal.domain.GeoType;
 import toctep.skynet.backend.dal.domain.Hashtag;
 import toctep.skynet.backend.dal.domain.Language;
 import toctep.skynet.backend.dal.domain.Place;
 import toctep.skynet.backend.dal.domain.PlaceType;
 import toctep.skynet.backend.dal.domain.SourceType;
 import toctep.skynet.backend.dal.domain.TimeZone;
 import toctep.skynet.backend.dal.domain.Tweet;
 import toctep.skynet.backend.dal.domain.TweetContributor;
 import toctep.skynet.backend.dal.domain.TweetHashtag;
 import toctep.skynet.backend.dal.domain.TweetMention;
 import toctep.skynet.backend.dal.domain.TweetURL;
 import toctep.skynet.backend.dal.domain.URL;
 import toctep.skynet.backend.dal.domain.User;
 import twitter4j.GeoLocation;
 import twitter4j.HashtagEntity;
 import twitter4j.Status;
 import twitter4j.URLEntity;
 import twitter4j.UserMentionEntity;
 
 public class TweetParser {
 	
 	private BoundingBoxType boundingBoxType;
 	private BoundingBox boundingBox;
 	private Country country;
 	private GeoType geoType;
 	private Geo geo;
 	private Language language;
 	private PlaceType placeType;
 	private SourceType sourceType;
 	private TimeZone timeZone;
 	private Place place;
 	private User user;
 	private Tweet tweet;
 		
 	private static TweetParser instance;
 	
 	private TweetParser() {
 	}
 	
 	public static TweetParser getInstance() {
 		if (instance == null) {
 			instance = new TweetParser();
 		}
 		return instance;
 	}
 	
 	public boolean parse(Status status) {
 		parseBoundingBoxType(status.getPlace());
 		parseBoundingBox(status.getPlace());
 		parseCountry(status.getPlace());
 		parseGeoType(status.getPlace());
 		parseGeo(status.getPlace());
 		parseLanguage(status.getUser());
 		parsePlaceType(status.getPlace());
 		parseSourceType(status);
 		parseTimeZone(status.getUser());
 		parsePlace(status.getPlace());
 		parseUser(status.getUser());
 		parseTweet(status);
 		parseUrl(status);
 		parseHashtag(status);
 		parseContributor(status);
 		parseMention(status);		
 		return true;
 	}
 	
 	private void parseBoundingBoxType(twitter4j.Place place) {
         boundingBoxType = new BoundingBoxType();
         boundingBoxType.setText(place.getBoundingBoxType());
 	}
 	
 	private void parseBoundingBox(twitter4j.Place place) {
         boundingBox = new BoundingBox();
         String coordinates = "";
         GeoLocation[][] array = place.getBoundingBoxCoordinates();
         for(GeoLocation[] x : array) {
         	for(GeoLocation y : x) {
         		coordinates += y.getLatitude() + ", " + y.getLongitude() + "; ";
         	}
         }        
         boundingBox.setCoordinates(coordinates);
         boundingBox.setType(boundingBoxType);
 	}
 
     private void parseCountry(twitter4j.Place place) {
         country = new Country();
         country.setCode(place.getCountryCode());
         country.setText(place.getCountry());
     }
 
     private void parseGeoType(twitter4j.Place place) {        
         geoType = new GeoType();
         geoType.setText(place.getGeometryType());
     }
 
     private void parseGeo(twitter4j.Place place) {
     	geo = new Geo();
         String geoCoordinates = "";
         GeoLocation[][] geoArray = place.getBoundingBoxCoordinates();
         for(GeoLocation[] x : geoArray) {
         	for(GeoLocation y : x) {
         		geoCoordinates += y.getLatitude() + ", " + y.getLongitude() + "; ";
         	}
         }        
         geo.setCoordinates(geoCoordinates);
         geo.setType(geoType);    	
     }
 
     private void parseLanguage(twitter4j.User user) {
         language = new Language();
         language.setText(user.getLang()); 
     }
     
     private void parsePlaceType(twitter4j.Place place) {
         placeType = new PlaceType();
         placeType.setText(place.getPlaceType());
     }
     
     private void parseSourceType(Status status) {
         sourceType = new SourceType();
         sourceType.setSourceType(status.getSource());
     }
     
     private void parseTimeZone(twitter4j.User user) {
         timeZone = new TimeZone();
         timeZone.setTimeZone(user.getTimeZone());
         timeZone.setUtcOffset(user.getUtcOffset()); 
     }
     
     private void parsePlace(twitter4j.Place placeStatus) {
         this.place = new Place();
         place.setTwitterId(placeStatus.getId());
         place.setName(placeStatus.getName());
         place.setFullName(placeStatus.getFullName());
         place.setStreetAddress(placeStatus.getStreetAddress());
         place.setLocality(""); //Twitter4j has no support for this?
         place.setRegion(""); //Twitter4j has no support for this?
         place.setIso3(""); //Twitter4j has no support for this?
         place.setPostalCode(""); //Twitter4j has no support for this?
         place.setPhone(""); //Twitter4j has no support for this?
         place.setTwitter(""); //Twitter4j has no support for this?
         place.setUrl(placeStatus.getURL());
         place.setAppId(""); //Twitter4j has no support for this?
         place.setType(placeType);
         place.setBoundingBox(boundingBox);
         place.setCountry(country);
     }
     
     private void parseUser(twitter4j.User userStatus) {
         this.user = new User();
         user.setTwitterId(userStatus.getId());
         user.setDefaultProfile(false); //Twitter4j has no support for this?
         user.setStatusesCount(userStatus.getStatusesCount());    
         user.setProfileBackgroundTile(0); //Twitter4j has no support for this?
         user.setProfileLinkColor(userStatus.getProfileLinkColor());
         user.setFollowing(userStatus.getFollowersCount());
         user.setFavouritesCount(userStatus.getFavouritesCount());
         user.setProtected(userStatus.isProtected());
         user.setProfileTextColor(userStatus.getProfileTextColor());
         user.setVerified(userStatus.isVerified());
         user.setContributorsEnabled(userStatus.isContributorsEnabled());
         user.setDescription(user.getDescription());
         user.setName(userStatus.getName());
         user.setProfileSidebarBorderColor(userStatus.getProfileSidebarBorderColor());
         user.setProfileBackgroundColor(userStatus.getProfileBackgroundColor());
         user.setCreatedAt(userStatus.getCreatedAt());
         user.setDefaultProfileImage(false); //Twitter4j has no support for this?
         user.setFollowersCount(userStatus.getFollowersCount()); //Same as setFollowing?
         user.setGeoEnabled(userStatus.isGeoEnabled());
         user.setProfileBackgroundImageUrl(userStatus.getProfileBackgroundImageUrl());
         user.setProfileBackgroundImageUrlHttps(userStatus.getProfileBackgroundImageUrlHttps());
         user.setFollowRequestSent(userStatus.isFollowRequestSent());
         user.setNotifications(0); //Twitter4j has no support for this?
         user.setProfileUseBackgroundImage(userStatus.isProfileUseBackgroundImage());
         user.setFriendsCount(userStatus.getFriendsCount());
         user.setProfileSideBarFillColor(userStatus.getProfileSidebarFillColor());
         user.setScreenName(userStatus.getScreenName());
         user.setProfileImageUrl(userStatus.getProfileImageURL().toExternalForm());
         user.setShowAllInlineMedia(userStatus.isShowAllInlineMedia());
         user.setTranslator(userStatus.isTranslator());
         user.setListedCount(userStatus.getListedCount());   
         user.setPlace(place);
         user.setLanguage(language);
        URL userUrl = new URL();
        userUrl.setText(userStatus.getURL().toExternalForm());
         user.setTimeZone(timeZone);
     }
     
     private void parseTweet(Status status) {
         tweet = new Tweet();
         tweet.setText(status.getText());
         tweet.setTruncated(status.isTruncated());
         tweet.setTwitterId(status.getId());
         tweet.setFavorited(status.isFavorited());
         tweet.setInReplyToTweetTwitterId(status.getInReplyToStatusId());
         tweet.setInReplyToUserTwitterId(status.getInReplyToUserId());
         tweet.setRetweetCount(status.getRetweetCount());
         tweet.setCreatedAt(status.getCreatedAt());
         tweet.setGeo(geo);
         tweet.setSourceType(sourceType);
         tweet.setPlace(place);
         tweet.setUser(user);
     } 
 
     private void parseUrl(Status status) {
         for(URLEntity urlEntity : status.getURLEntities()) {
             URL url = new URL();
             url.setText(urlEntity.getDisplayURL());
             TweetURL tweetURL = new TweetURL();
             tweetURL.setTweet(tweet);
             tweetURL.setUrl(url);
         }
     }
     
     private void parseHashtag(Status status) {
         for(HashtagEntity hashtagEntity : status.getHashtagEntities()) {
             Hashtag hashtag = new Hashtag();
             hashtag.setText(hashtagEntity.getText());
             TweetHashtag tweetHashtag = new TweetHashtag();
             tweetHashtag.setHashtag(hashtag);
             tweetHashtag.setTweet(tweet);
         } 
     }
 
     private void parseContributor(Status status) {
         for(long contributor : status.getContributors()) {
             TweetContributor tweetContributor = new TweetContributor();
             tweetContributor.setTweet(tweet);
             tweetContributor.setUser_twitter_id(contributor);
         }
     }
     
     private void parseMention(Status status) {
         for(UserMentionEntity mentionEntity : status.getUserMentionEntities()) {
             TweetMention tweetMention = new TweetMention();
             tweetMention.setTweet(tweet);
             tweetMention.setUser(mentionEntity.getId());
         }
     }	
 }
