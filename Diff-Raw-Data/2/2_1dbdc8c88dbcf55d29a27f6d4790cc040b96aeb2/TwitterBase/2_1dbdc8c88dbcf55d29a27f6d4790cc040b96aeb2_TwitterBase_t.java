 package com.teamboid.twitterapi.client;
 
 import com.teamboid.twitterapi.config.TwitterAPIConfig;
 import com.teamboid.twitterapi.config.TwitterAPIConfigJSON;
 import com.teamboid.twitterapi.dm.DirectMessage;
 import com.teamboid.twitterapi.dm.DirectMessageJSON;
 import com.teamboid.twitterapi.experimentalapis.RelatedResults;
 import com.teamboid.twitterapi.experimentalapis.RelatedResultsJSON;
 import com.teamboid.twitterapi.list.UserList;
 import com.teamboid.twitterapi.list.UserListJSON;
 import com.teamboid.twitterapi.list.UserListMode;
 import com.teamboid.twitterapi.relationship.IDs;
 import com.teamboid.twitterapi.relationship.IDsJSON;
 import com.teamboid.twitterapi.relationship.Relationship;
 import com.teamboid.twitterapi.relationship.RelationshipJSON;
 import com.teamboid.twitterapi.savedsearch.SavedSearch;
 import com.teamboid.twitterapi.savedsearch.SavedSearchJSON;
 import com.teamboid.twitterapi.search.SearchQuery;
 import com.teamboid.twitterapi.search.SearchResult;
 import com.teamboid.twitterapi.search.SearchResultJSON;
 import com.teamboid.twitterapi.status.*;
 import com.teamboid.twitterapi.trend.*;
 import com.teamboid.twitterapi.user.FollowingType;
 import com.teamboid.twitterapi.user.User;
 import com.teamboid.twitterapi.user.UserJSON;
 import com.teamboid.twitterapi.utilities.Utils;
 import org.scribe.model.Response;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The base {@link Twitter} client class, contains the actual code content for
  * the functions called when you use the Twitter interface.
  *
  * @author Aidan Follestad
  */
 class TwitterBase extends RequestHandler implements Twitter {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Twitter setSslEnabled(boolean enabled) {
         _ssl = enabled;
         return this;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean getSslEnabled() { return _ssl; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User verifyCredentials() throws Exception {
         return new UserJSON(getObject(Urls.VERIFY_CREDENTIALS));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public TwitterAPIConfig getAPIConfiguration() throws Exception {
         return new TwitterAPIConfigJSON(getObject(Urls.API_CONFIG));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User updateProfileImage(File file) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("image", Utils.getBase64FromFile(file)));
         return new UserJSON(postObject(Urls.UPDATE_PROFILE_IMAGE, pairs));
     }
 
     /**
      * {@inheritDoc}
      * 
      */
     @Override
     public String getAccessToken() throws Exception {
     	if(super._oauthToken == null) {
     		throw new Exception("No access token has been set, this is most likely an unauthorized Twitter object.");
     	}
         return super._oauthToken.getToken();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getAccessSecret() throws Exception {
     	if(super._oauthToken == null) {
     		throw new Exception("No access token has been set, this is most likely an unauthorized Twitter object.");
     	}
         return super._oauthToken.getSecret();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getHomeTimeline(Paging paging) throws Exception {
         String url = Urls.HOME_TIMELINE;
         if(paging != null) url += paging.getUrlString('&', true);
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getMentions(Paging paging) throws Exception {
         String url = Urls.MENTIONS;
         if(paging != null) url += paging.getUrlString('&', true);
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getUserTimeline(Paging paging, boolean includeRetweets) throws Exception {
         String url = Urls.USER_TIMELINE + ("&include_rts=" + Boolean.toString(includeRetweets));
         if(paging != null) url += paging.getUrlString('&', true);
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getUserTimeline(Long userId, Paging paging, boolean includeRetweets) throws Exception {
         String url = Urls.USER_TIMELINE + "&user_id=" + userId +
                 ("&include_rts=" + Boolean.toString(includeRetweets));
         if(paging != null) url += paging.getUrlString('&', true);
         if(includeRetweets) url += "&include_rts=true";
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getUserTimeline(String screenName, Paging paging, boolean includeRetweets) throws Exception {
         String url = Urls.USER_TIMELINE + "&screen_name=" + screenName +
                 ("&include_rts=" + Boolean.toString(includeRetweets));
         if(paging != null) url += paging.getUrlString('&', true);
         if(includeRetweets) url += "&include_rts=true";
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getRetweetsOfMe(Paging paging) throws Exception {
         String url = Urls.RETWEETS_OF_ME;
         if(paging != null) url += paging.getUrlString('&', true);
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status showStatus(Long statusId) throws Exception {
         return new StatusJSON(getObject(Urls.SHOW_STATUS.replace("{id}", Long.toString(statusId))));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status retweetStatus(Long statusId) throws Exception {
         return new StatusJSON(postObject(Urls.RETWEET_STATUS.replace("{id}", Long.toString(statusId)), null));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status destroyStatus(Long statusId) throws Exception {
         return new StatusJSON(postObject(Urls.DESTROY_STATUS.replace("{id}", Long.toString(statusId)), null));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User showUser(Long userId) throws Exception {
         return new UserJSON(getObject(Urls.SHOW_USER + "&user_id=" + Long.toString(userId)));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User showUser(String screenName) throws Exception {
         return new UserJSON(getObject(Urls.SHOW_USER + "&screen_name=" + screenName));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User[] lookupUsers(String[] screenNames) throws Exception {
         String param = "";
         for(String name : screenNames) {
             param += name + ",";
         }
         return UserJSON.createUserList(getArray(Urls.LOOKUP_USERS + "?screen_name=" + param));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User[] lookupUsers(Long[] userIds) throws Exception {
         String param = "";
         for(Long id : userIds) {
             param += Long.toString(id) + ",";
         }
         return UserJSON.createUserList(getArray(Urls.LOOKUP_USERS + "?user_id=" + param));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getUserProfileImage(String screenName, ProfileImageSize size) throws Exception {
         String url = Urls.GET_PROFILE_IMAGE + "?screen_name=" + screenName;
         if(size != ProfileImageSize.NORMAL) {
             url += "&size=" + size.name().toLowerCase();
         }
         Response response = get(url, true);
         return response.getHeader("Location");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IDs getFriends(String screenName, Long cursor) throws Exception {
         String url = Urls.GET_FRIENDS + "?screen_name=" + screenName;
         if(cursor >= -1) url += "&cursor=" + Long.toString(cursor);
         return new IDsJSON(getObject(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IDs getFriends(Long userId, Long cursor) throws Exception {
         String url = Urls.GET_FRIENDS + "?user_id=" + Long.toString(userId);
         if(cursor >= -1) url += "&cursor=" + Long.toString(cursor);
         return new IDsJSON(getObject(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IDs getFollowers(String screenName, Long cursor) throws Exception {
         String url = Urls.GET_FOLLOWERS + "?screen_name=" + screenName;
         if(cursor >= -1) url += "&cursor=" + Long.toString(cursor);
         return new IDsJSON(getObject(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IDs getFollowers(Long userId, Long cursor) throws Exception {
         String url = Urls.GET_FOLLOWERS + "?user_id=" + Long.toString(userId);
         if(cursor >= -1) url += "&cursor=" + Long.toString(cursor);
         return new IDsJSON(getObject(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean existsFriendship(String fromScreenName, String toScreenName) throws Exception {
         Response response = get(Urls.FRIENDSHIP_EXISTS + "?screen_name_a=" +
                 fromScreenName + "&screen_name_b=" + toScreenName, true);
         return Boolean.parseBoolean(response.getBody());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IDs getFriendshipsIncoming(Long cursor) throws Exception {
         String url = Urls.INCOMING_FRIENDSHIPS;
         if(cursor >= -1) url += "?cursor=" + Long.toString(cursor);
         return new IDsJSON(getObject(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IDs getFriendshipsOutgoing(Long cursor) throws Exception {
         String url = Urls.OUTGOING_FRIENDSHIPS;
         if(cursor >= -1) url += "?cursor=" + Long.toString(cursor);
         return new IDsJSON(getObject(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Relationship getRelationship(Long fromUserId, Long toUserId) throws Exception {
         String url = Urls.SHOW_FRIENDSHIP +
                 "?source_id=" + Long.toString(fromUserId) + "&target_id=" + Long.toString(toUserId);
         return new RelationshipJSON(getObject(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Relationship getRelationship(String fromScreenName, String toScreenName) throws Exception {
         String url = Urls.SHOW_FRIENDSHIP +
                 "?source_screen_name=" + fromScreenName + "&target_screen_name=" + toScreenName;
         return new RelationshipJSON(getObject(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User createFriendship(Long userId) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("user_id", Long.toString(userId)));
         User toReturn = new UserJSON(postObject(Urls.CREATE_FRIENDSHIP, pairs));
         /*
          * The User JSON returned from the above HTTP POST doesn't seem to actually change the isFollowing value, so we do that manually.
          */
         toReturn.setFollowingType(FollowingType.FOLLOWING);
         return toReturn;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User createFriendship(String screenName) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("screen_name", screenName));
         User toReturn = new UserJSON(postObject(Urls.CREATE_FRIENDSHIP, pairs));
         /*
          * The User JSON returned from the above HTTP POST doesn't seem to actually change the isFollowing value, so we do that manually.
          */
         toReturn.setFollowingType(FollowingType.FOLLOWING);
         return toReturn;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User destroyFriendship(Long userId) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("user_id", Long.toString(userId)));
         User toReturn = new UserJSON(postObject(Urls.DESTROY_FRIENDSHIP, pairs));
         /*
          * The User JSON returned from the above HTTP POST doesn't seem to actually change the isFollowing value, so we do that manually.
          */
         toReturn.setFollowingType(FollowingType.NOT_FOLLOWING);
         return toReturn;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User destroyFriendship(String screenName) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("screen_name", screenName));
         User toReturn = new UserJSON(postObject(Urls.DESTROY_FRIENDSHIP, pairs));
         /*
          * The User JSON returned from the above HTTP POST doesn't seem to actually change the isFollowing value, so we do that manually.
          */
         toReturn.setFollowingType(FollowingType.NOT_FOLLOWING);
         return toReturn;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User reportSpam(String screenName) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("screen_name", screenName));
         return new UserJSON(postObject(Urls.REPORT_SPAM, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User reportSpam(Long userId) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("user_id", Long.toString(userId)));
         return new UserJSON(postObject(Urls.REPORT_SPAM, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User[] getBlocking(Long cursor) throws Exception {
         return UserJSON.createUserList(getObject(Urls.BLOCKING +
                 "?cursor=" + Long.toString(cursor)).getJSONArray("users"));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IDs getBlockingIds(Long cursor) throws Exception {
         return new IDsJSON(getObject(Urls.BLOCKING_IDS + "?cursor=" + Long.toString(cursor)));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean existsBlock(String screenName) throws Exception {
         return get(Urls.EXISTS_BLOCK + "?screen_name=" + screenName, true).getCode() != 404;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean existsBlock(Long userId) throws Exception {
         return get(Urls.EXISTS_BLOCK + "?user_id=" + Long.toString(userId), true).getCode() != 404;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User createBlock(String screenName) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("screen_name", screenName));
         return new UserJSON(postObject(Urls.CREATE_BLOCK, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User createBlock(Long userId) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("user_id", Long.toString(userId)));
         return new UserJSON(postObject(Urls.CREATE_BLOCK,pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User destroyBlock(String screenName) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("screen_name", screenName));
         return new UserJSON(postObject(Urls.DESTROY_BLOCK,pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User destroyBlock(Long userId) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("user_id", Long.toString(userId)));
         return new UserJSON(postObject(Urls.DESTROY_BLOCK,pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SavedSearch[] getSavedSearches() throws Exception {
         return SavedSearchJSON.createSavedSearchList(getArray(Urls.GET_SAVED_SEARCHES));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SavedSearch getSavedSearch(long id) throws Exception {
         return new SavedSearchJSON(getObject(Urls.SHOW_SAVED_SEARCH
                 .replace("{id}", Long.toString(id))));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SavedSearch createSavedSearch(String query) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("query", query));
         return new SavedSearchJSON(postObject(Urls.CREATE_SAVED_SEARCH, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SavedSearch destroySavedSearch(long id) throws Exception {
         return new SavedSearchJSON(postObject(Urls.DESTROY_SAVED_SEARCH
                 .replace("{id}", Long.toString(id)), null));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList createList(String name, UserListMode mode, String description) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("name", name));
         pairs.add(new HttpParam("mode", mode.name().toLowerCase()));
         if(description != null) {
             pairs.add(new HttpParam("description", description));
         }
         return new UserListJSON(postObject(Urls.CREATE_LIST, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void createListMembers(long listId, long[] userIds) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("list_id", Long.toString(listId)));
         String names = "";
         for(long ui : userIds) names += Long.toString(ui) + ",";
         pairs.add(new HttpParam("user_id", names));
         postObject(Urls.CREATE_LIST_MEMBERS, pairs);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void createListMembers(long listId, String[] screenNames) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("list_id", Long.toString(listId)));
         String names = "";
         for(String sn : screenNames) names += sn + ",";
         pairs.add(new HttpParam("screen_name", names));
         postObject(Urls.CREATE_LIST_MEMBERS, pairs);
     }
 
     @Override
     public void destroyListMembers(long listId, long[] userIds) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("list_id", Long.toString(listId)));
         String names = "";
         for(long ui : userIds) names += Long.toString(ui) + ",";
         pairs.add(new HttpParam("user_id", names));
         postObject(Urls.DESTROY_LIST_MEMBERS, pairs);
     }
 
     @Override
     public void destroyListMembers(long listId, String[] screenNames) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("list_id", Long.toString(listId)));
         String names = "";
         for(String sn : screenNames) names += sn + ",";
         pairs.add(new HttpParam("screen_name", names));
         postObject(Urls.DESTROY_LIST_MEMBERS, pairs);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList updateList(long listId, String name, UserListMode mode, String description) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("list_id", Long.toString(listId)));
         if(name != null) {
             pairs.add(new HttpParam("name", name));
         }
         pairs.add(new HttpParam("mode", mode.name().toLowerCase()));
         if(description != null) {
             pairs.add(new HttpParam("description", description));
         }
         return new UserListJSON(postObject(Urls.UPDATE_LIST, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList destroyList(long listId) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("list_id", Long.toString(listId)));
         return new UserListJSON(postObject(Urls.DESTROY_LIST, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList getList(long listId) throws Exception {
         return new UserListJSON(getObject(Urls.GET_LIST + "?list_id=" + Long.toString(listId)));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList[] getLists() throws Exception {
         return UserListJSON.createStatusList(getArray(Urls.GET_ALL_LISTS));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList[] getLists(long userId) throws Exception {
         return UserListJSON.createStatusList(getArray(Urls.GET_ALL_LISTS +
             "?user_id=" + Long.toString(userId)));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList[] getLists(String screenName) throws Exception {
         return UserListJSON.createStatusList(getArray(Urls.GET_ALL_LISTS +
                 "?screen_name=" + screenName));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getListTimeline(long listId, Paging paging) throws Exception {
         String url = Urls.GET_LIST_STATUSES + "&list_id=" + Long.toString(listId);
         if(paging != null) {
             url += paging.getUrlString('&', true).replace("count=", "per_page=");
         }
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User[] getListMembers(long listId, long cursor) throws Exception {
         String url = Urls.GET_LIST_MEMBERS + "?list_id=" + Long.toString(listId);
         if(cursor >= -1) url += "&cursor=" + Long.toString(cursor);
         return UserJSON.createUserList(getObject(url).getJSONArray("users"));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User[] getListSubscribers(long listId, long cursor) throws Exception {
         String url = Urls.GET_LIST_SUBSCRIBERS + "?list_id=" + Long.toString(listId);
         if(cursor >= -1) url += "&cursor=" + Long.toString(cursor);
         return UserJSON.createUserList(getObject(url).getJSONArray("users"));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList createListSubscription(long listId) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("list_id",Long.toString(listId)));
         return new UserListJSON(postObject(Urls.CREATE_LIST_SUBSCRIPTION, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserList destroyListSubscription(long listId) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("list_id",Long.toString(listId)));
         return new UserListJSON(postObject(Urls.DESTROY_LIST_SUBSCRIPTION, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Trends[] getTrendsDaily() throws Exception {
         return TrendsJSON.createTrendsList(getObject(Urls.DAILY_TRENDS));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Trends[] getTrendsWeekly() throws Exception {
         return TrendsJSON.createTrendsList(getObject(Urls.WEEKLY_TRENDS));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public TrendLocation[] getTrendsAvailable(GeoLocation location) throws Exception {
         return TrendLocationJSON.createLocationList(
         		getArray(Urls.AVAILABLE_TRENDS + location.getQueryString('?')));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Trend[] getLocationTrends(int woeid) throws Exception {
         return TrendJSON.createTrendList(getArray(Urls.LOCATION_TRENDS
                 .replace("{woeid}", Integer.toString(woeid)))
                 .getJSONObject(0).getJSONArray("trends"));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Trend[] getTrendsGlobal() throws Exception {
         return TrendJSON.createTrendList(getArray(Urls.GLOBAL_TRENDS)
                 .getJSONObject(0).getJSONArray("trends"));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Place getPlaceDetails(String placeId) throws Exception {
         return new PlaceJSON(getObject(Urls.PLACE_DETAILS.replace("{id}", placeId)));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getRetweets(Long statusId, int count) throws Exception {
         String url = Urls.RETWEETS.replace("{id}", Long.toString(statusId));
         if(count > 0) url += "&count=" + count;
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User[] getRetweetedBy(Long statusId, Paging paging) throws Exception {
         String url = Urls.RETWEETED_BY.replace("{id}", Long.toString(statusId));
         if(paging != null) url += paging.getUrlString('?', false);
         return UserJSON.createUserList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status updateStatus(StatusUpdate update) throws Exception {
         if(update.getMedia() != null) {
             return new StatusJSON(postObject(Urls.UPDATE_STATUS_MEDIA, 
             		update.getBodyParams(), update.getMedia()));
         } else if(update.getMediaStream() != null) {
         	return new StatusJSON(postObject(Urls.UPDATE_STATUS_MEDIA, update.getBodyParams(), 
         			update.getMediaStream(), update.getMediaStreamName()));
         } else {
         	return new StatusJSON(postObject(Urls.UPDATE_STATUS, update.getBodyParams()));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SearchResult search(SearchQuery query) throws Exception {
         return new SearchResultJSON(getObject(Urls.SEARCH_QUERY + query.getUrl(), false));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User[] searchUsers(String query, int page, int perPage) throws Exception {
         String url = Urls.SEARCH_USERS + "?q=" + query;
         url += "&include_entities=true";
         if(page > 0) url += "&page=" + page;
         if(perPage > 0) url += "&per_page=" + perPage;
         return UserJSON.createUserList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DirectMessage[] getDirectMessages(Paging paging) throws Exception {
         String url = Urls.DIRECT_MESSAGES;
         if(paging != null) url += paging.getUrlString('&', true);
         return DirectMessageJSON.createMessageList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DirectMessage[] getSentDirectMessages(Paging paging) throws Exception {
         String url = Urls.DIRECT_MESSAGES_SENT;
         if(paging != null) url += paging.getUrlString('&', true);
         return DirectMessageJSON.createMessageList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DirectMessage createDirectMessage(String screenName, String text) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("text", text));
         pairs.add(new HttpParam("screen_name", screenName));
         return new DirectMessageJSON(postObject(Urls.CREATE_DIRECT_MESSAGE, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DirectMessage createDirectMessage(Long userId, String text) throws Exception {
         List<HttpParam> pairs = new ArrayList<HttpParam>();
         pairs.add(new HttpParam("text", text));
         pairs.add(new HttpParam("user_id", Long.toString(userId)));
         return new DirectMessageJSON(postObject(Urls.CREATE_DIRECT_MESSAGE, pairs));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DirectMessage showDirectMessage(Long msgId) throws Exception {
         return new DirectMessageJSON(getObject(Urls.SHOW_DIRECT_MESSAGE.replace("{id}", Long.toString(msgId))));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DirectMessage destroyDirectMessage(Long msgId) throws Exception {
         return new DirectMessageJSON(deleteObject(Urls.DESTROY_DIRECT_MESSAGE.replace("{id}", Long.toString(msgId))));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getFavorites(Paging paging) throws Exception {
         String url = Urls.GET_FAVORITES;
         if(paging != null) url += paging.getUrlString('&', true);
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getFavorites(Paging paging, String screenName) throws Exception {
         String url = Urls.GET_FAVORITES + "&screen_name=" + screenName;
         if(paging != null) url += paging.getUrlString('&', true);
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status[] getFavorites(Paging paging, Long userId) throws Exception {
         String url = Urls.GET_FAVORITES + "&user_id=" + Long.toString(userId);
         if(paging != null) url += paging.getUrlString('&', true);
         return StatusJSON.createStatusList(getArray(url));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status createFavorite(Long statusId) throws Exception {
         Status toReturn = new StatusJSON(postObject(Urls.CREATE_FAVORITE.replace("{id}", Long.toString(statusId)), null));
         /*
          * The Status JSON returned from the above HTTP POST doesn't seem to actually change the isFavorited value, so we do that manually.
          */
         toReturn.setFavorited(true);
         return toReturn;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Status destroyFavorite(Long statusId) throws Exception {
         Status toReturn = new StatusJSON(postObject(Urls.DESTROY_FAVORITE.replace("{id}", Long.toString(statusId)), null));
         /*
          * The Status JSON returned from the above HTTP POST doesn't seem to actually change the isFavorited value, so we do that manually.
          */
         toReturn.setFavorited(false);
         return toReturn;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public RelatedResults getRelatedResults(Long statusId) throws Exception {
         return new RelatedResultsJSON(getArray(Urls.RELATED_RESULTS
         		.replace("{id}", Long.toString(statusId))));
     }
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	public Place[] getReverseGeocode(GeoLocation coordinates, String accuracy,
 			Granularity gran, int maxResults) throws Exception {
 		String url = Urls.REVERSE_GEOCODE + coordinates.getQueryString('?');
 		if(accuracy != null && accuracy.length() > 0) {
 			url += "&accuracy=" + accuracy;
 		}
 		url += ("&granularity=" + gran.name().toLowerCase());
 		if(maxResults > 0) {
 			url += ("&max_results=" + maxResults);
 		}
		return PlaceJSON.createPlaceList(getObject(url).getJSONObject("result").getJSONArray("places"));
 	}
 }
