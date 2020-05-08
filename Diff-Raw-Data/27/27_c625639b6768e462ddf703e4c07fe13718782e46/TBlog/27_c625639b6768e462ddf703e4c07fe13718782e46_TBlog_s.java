 /*
 Copyright (c) 2007-2009, Yusuke Yamamoto
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the Yusuke Yamamoto nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package t4j;
 
 import t4j.data.*;
 import t4j.http.*;
 import t4j.org.json.JSONArray;
 import t4j.org.json.JSONException;
 import t4j.org.json.JSONObject;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 /**
  * @author chenxj
  */
 public class TBlog extends TBlogSupport {
 
     private String baseURL = "http://api.t.sohu.com/";
 
     private String redirectURL;
 
     public String getRedirectURL() {
         return redirectURL;
     }
 
     public void setRedirectURL(String redirectURL) {
         this.redirectURL = redirectURL;
     }
 
     public TBlog() {
         this(OAuthVersion.V1);
     }
 
     public TBlog(OAuthVersion version) {
 
         super();
         format.setTimeZone(TimeZone.getTimeZone("GMT"));
 
         if (version == OAuthVersion.V2) {
             this.baseURL = "https://api.t.sohu.com/";
             this.http.setHttps(true);
         }
     }
 
     public void setOAuth2AccessToken(String token) {
         http.setOAuth2AccessToken(token);
     }
 
     public void setToken(String token, String tokenSecret) {
         http.setToken(token, tokenSecret);
     }
 
     public void setBaseURL(String baseURL) {
         this.baseURL = baseURL;
     }
 
     public String getBaseURL() {
         return this.baseURL;
     }
 
     public synchronized void setOAuthConsumer(String consumerKey, String consumerSecret) {
         this.http.setOAuthConsumer(consumerKey, consumerSecret);
     }
 
     public RequestToken getOAuthRequestToken() throws TBlogException {
         return http.getOAuthRequestToken();
     }
 
     public synchronized AccessToken getOAuthAccessToken(RequestToken requestToken) throws TBlogException {
         return http.getOAuthAccessToken(requestToken);
     }
 
     public synchronized AccessToken getOAuthAccessToken(String token, String tokenSecret) throws TBlogException {
         AccessToken accessToken = http.getOAuthAccessToken(token, tokenSecret);
         setUserId(accessToken.getScreenName());
         return accessToken;
     }
 
     public AccessToken getXAuthAccessToken(String passport, String password) throws TBlogException {
         return this.http.getXAuthAccessToken(passport, password, false);
     }
 
     public AccessToken getXAuthAccessToken(String passport, String password, boolean isMD5) throws TBlogException {
         return this.http.getXAuthAccessToken(passport, password, isMD5);
     }
 
     public String getOAuth2AuthorizeURL() throws TBlogException {
         return this.http.getOAuth2AuthorizeURL(redirectURL);
     }
 
     public String getOAuth2AuthorizeTokenURL() throws TBlogException {
         return this.http.getOAuth2AuthorizeTokenURL(redirectURL);
     }
 
     public OAuth2AccessToken getOAuth2AccessTokenByCode(String code) throws TBlogException {
         return this.http.getOAuth2AccessTokenByCode(code, redirectURL);
     }
 
     public OAuth2AccessToken getOAuth2AccessTokenByRefreshToken(String refreshToken) throws TBlogException {
         return this.http.getOAuth2AccessTokenByRefreshToken(refreshToken);
     }
 
     public OAuth2AccessToken getOAuth2AccessTokenByPassword(String username, String password) throws TBlogException {
         return this.http.getOAuth2AccessTokenByPassword(username, password);
     }
 
     //SDK request method wrapper begin
 
     /**
      * 获取当前登录用户的微博列表
      *
      * @return
      * @throws TBlogException
      */
     public List<Status> getHomeTimeline() throws TBlogException {
         return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json", true));
     }
 
 
     /**
      * 获取最新的公共微博列表(随便看看)
      *
      * @return
      * @throws TBlogException
      */
     public List<Status> getPublicTimeline() throws TBlogException {
         return Status.constructStatuses(get(getBaseURL() + "statuses/public_timeline.json", false));
     }
 
 
     /**
      * 获取我的微博列表
      *
      * @return
      * @throws TBlogException
      */
     public List<Status> getMyTweets() throws TBlogException {
         return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json", true));
     }
 
 
     /**
      * 获取指定用户的微博列表
      *
      * @param userId 用户id
      * @return
      * @throws TBlogException
      */
     public List<Status> getUserTimeline(long userId) throws TBlogException {
         return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + String.valueOf(userId) + ".json", true));
     }
 
     /**
      * 获取指定用户的微博列表
      *
      * @param screenName 用户昵称
      * @return
      * @throws TBlogException
      */
     public List<Status> getUserTimeline(String screenName) throws TBlogException {
         return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + screenName + ".json", true));
     }
 
     /**
      * 获取@评论当前登录用户的微博列表
      *
      * @return
      * @throws TBlogException
      */
     public List<Status> getMentions() throws TBlogException {
         return Status.constructStatuses(get(getBaseURL() + "statuses/mentions_timeline.json", true));
     }
 
 
     /**
      * 查看指定微博的所有评论
      *
      * @param id 微博id
      * @return
      * @throws TBlogException
      */
     public List<Status> getComments(long id) throws TBlogException {
         return Status.constructStatuses(get(getBaseURL() + "statuses/comments/" + String.valueOf(id) + ".json", true));
     }
 
 
     /**
      * 查看别人对我的评论
      *
      * @return
      * @throws TBlogException
      */
     public List<Status> getCommentsToMe() throws TBlogException {
         JSONObject json = get(getBaseURL() + "statuses/comments_timeline.json", true).asJSONObject();
         JSONArray commentsJsonArray = null;
         try {
             commentsJsonArray = json.getJSONArray("comments");
         } catch (JSONException je) {
 
         }
         return Status.constructStatuses(commentsJsonArray);
     }
 
     /**
      * 显示一条微博的详细信息
      *
      * @return
      * @throws TBlogException
      */
     public Status show(long statusId) throws TBlogException {
         return new Status(http.get(getBaseURL() + "statuses/show/" + String.valueOf(statusId) + ".json", true));
     }
 
 
     /**
      * 发布一条微博
      *
      * @param status 微博内容，不得超过163个字符
      * @return
      * @throws TBlogException
      */
     public Status updateStatus(String status) throws TBlogException {
         return new Status(http.post(getBaseURL() + "statuses/update.json", new PostParameter[]{new PostParameter("status", status)}, true));
     }
 
 
     /**
      * 发布一条带图微博
      *
      * @param status 微博内容，不得超过163个字符
      * @return
      * @throws TBlogException
      */
     public String uploadStatus(String status, File file) throws TBlogException {
         JSONObject json = http.multPartURL("pic", getBaseURL() + "statuses/upload.json", new PostParameter[]{new PostParameter("status", status)},
                 file, true).asJSONObject();
         return json.toString();
     }
 
     /**
      * 删除指定的微博，也可以撤销已转发的微博
      *
      * @param statusId 删除或撤销转发微博的ID
      * @return
      * @throws TBlogException
      */
     public Status destroy(long statusId) throws TBlogException {
         return new Status(http.post(getBaseURL() + "statuses/destroy/" + String.valueOf(statusId) + ".json", true));
     }
 
     /**
      * 转发一条微博
      *
      * @param id     微博ID
      * @param status 转发理由
      * @return
      * @throws TBlogException
      */
     public Status retweet(long id, String status) throws TBlogException {
         JSONObject json = http.post(getBaseURL() + "statuses/transmit/" + String.valueOf(id) + ".json",
                 new PostParameter[]{new PostParameter("status", status)}, true).asJSONObject();
         try {
             if (!json.isNull("retweeted_status")) {
                 return new Status(json.getJSONObject("retweeted_status"));
             } else {
                 return null;
             }
         } catch (JSONException e) {
             throw new TBlogException(e);
         }
     }
 
 
     /**
      * 评论一条微博
      *
      * @param id      被评论的微博id
      * @param comment 评论内容
      * @return
      * @throws TBlogException
      */
     public Status reply(long id, String comment) throws TBlogException, JSONException {
         PostParameter[] params = new PostParameter[]{
                 new PostParameter("id", String.valueOf(id)),
                 new PostParameter("comment", comment),
         };
         return new Status(http.post(getBaseURL() + "statuses/comment.json", params, true));
     }
 
 
     /**
      * 删除一条评论
      *
      * @param id 被删除评论id
      * @return
      * @throws TBlogException
      */
     public Status destroyReply(long id) throws TBlogException, JSONException {
 
         return new Status(http.post(getBaseURL() + "statuses/comment_destroy/" + String.valueOf(id) + ".json", true));
     }
 
 
     /**
      * 根据微博id，获得某条微博的评论数和转发数
      *
      * @return
      * @throw TBlogException
      */
     public Counts getMsgCounts(long msgId) throws TBlogException {
         return new Counts(http.get(getBaseURL() + "statuses/counts/" + msgId + ".json", true));
     }
 
     /**
      * 批量接口
      * 根据微博id，获得某条微博的评论数和转发数
      *
      * @return
      * @throw TBlogException
      */
     public List<Counts> getBatchMsgCounts(String... msgIds) throws TBlogException {
         StringBuffer sb = new StringBuffer();
         for (String id : msgIds) {
             sb.append(id).append(",");
         }
         String msgs = "";
         if (sb != null && ",".equals(sb.substring(sb.length() - 1))) {
             msgs = sb.substring(0, sb.length() - 1);
         }
 
         return Counts.constructStatuses(http.get(getBaseURL() + "statuses/counts.json?ids=" + msgs, true));
     }
 
     /**
      * 获取用户的最新提醒消息数包括“新粉丝数”、“新@提醒数”、“新评论数”、“新私信数”、“新微博数”
      *
      * @return
      * @throws TBlogException
      */
     public NewCounts getNewCounts() throws TBlogException {
         return new NewCounts(http.get(getBaseURL() + "statuses/check.json", true));
     }
 
     /**
      * 上传视频
      *
      * @return
      * @throws TBlogException
      */
     public Status uploadVideo(String videoUrl) throws TBlogException {
         PostParameter[] params = new PostParameter[]{
                 new PostParameter("videoUrl", videoUrl),
         };
         return new Status(http.post(getBaseURL() + "statuses/video.json", params, true));
     }
 
     /**
      * 显示用户详情
      *
      * @param userId
      * @return
      * @throws TBlogException
      */
     public User showUser(long userId) throws TBlogException {
         return new User(http.get(getBaseURL() + "users/show/" + String.valueOf(userId) + ".json", true));
 
     }
 
     /**
      * 显示用户详情
      *
      * @param screenName
      * @return
      * @throws TBlogException
      */
     public User showUser(String screenName) throws TBlogException {
         return new User(http.get(getBaseURL() + "users/show/" + screenName + ".json", true));
     }
 
 
     /**
      * 查看用户a是否关注用户b
      *
      * @param uidA
      * @param uidB
      * @return
      */
     public JSONObject isFollowing(long uidA, long uidB) throws TBlogException {
         return http.get(getBaseURL() + "friendships/exists.json?user_a=" +
                 String.valueOf(uidA) + "&user_b=" + String.valueOf(uidB), true).asJSONObject();
     }
 
     /**
      * 查看用户a是否关注用户b
      *
      * @param ScreenNameA
      * @param ScreenNameB
      * @return
      */
     public JSONObject isFollowing(String ScreenNameA, String ScreenNameB) throws TBlogException {
         return http.get(getBaseURL() + "friendships/exists.json?screen_name_a=" +
                 ScreenNameA + "&screen_name_b=" + ScreenNameB, true).asJSONObject();
     }
 
     /**
      * 查看用户之间的关系
      *
      * @param sourceUid
      * @param targetUid
      * @return
      */
     public JSONObject showRelationShip(long sourceUid, long targetUid) throws TBlogException {
         return http.get(getBaseURL() + "friendships/show.json?source_id=" + sourceUid +
                 "&target_id=" + targetUid, true).asJSONObject();
     }
 
     /**
      * 查看用户之间的关系
      *
      * @param sourceScreenName
      * @param targetScreenName
      * @return
      */
     public JSONObject showRelationShip(String sourceScreenName, String targetScreenName) throws TBlogException {
         return http.get(getBaseURL() + "friendships/show.json?source_screen_name=" + sourceScreenName +
                 "&target_screen_name=" + targetScreenName, true).asJSONObject();
     }
 
     /**
      * 添加关注
      *
      * @param uid
      * @return
      */
     public User addFollow(long uid) throws TBlogException {
         return new User(http.post(getBaseURL() + "friendships/create/" + String.valueOf(uid) + ".json", true));
     }
 
     /**
      * 添加关注
      *
      * @param screenName
      * @return
      */
     public User addFollow(String screenName) throws TBlogException {
         return new User(http.post(getBaseURL() + "friendships/create/" + screenName + ".json", true));
     }
 
     /**
      * 取消关注
      *
      * @param uid
      * @return
      */
     public User cancelFollow(long uid) throws TBlogException {
         return new User(http.post(getBaseURL() + "friendships/destroy/" + String.valueOf(uid) + ".json", true));
     }
 
     /**
      * 添加关注
      *
      * @param screenName
      * @return
      */
     public User cancelFollow(String screenName) throws TBlogException {
         return new User(http.post(getBaseURL() + "friendships/destroy/" + screenName + ".json", true));
     }
 
     /**
      * 获得好友列表
      *
      * @param
      * @return
      */
     public List<User> getFriendsList(long userId) throws TBlogException {
         return User.constructUsers(http.get(getBaseURL() + "statuses/friends/" + String.valueOf(userId) + ".json", true).asJSONArray());
     }
 
     /**
      * 获得粉丝列表
      *
      * @param userId
      * @return
      */
     public List<User> getFollowsList(long userId) throws TBlogException {
         return User.constructUsers(http.get(getBaseURL() + "statuses/followers/" + String.valueOf(userId) + ".json", true).asJSONArray());
     }
 
     /**
      * 我收到的私信列表
      *
      * @return
      */
     public List<Message> getRecieveDirectMessages() throws TBlogException {
         return Message.constructMessages(http.get(getBaseURL() + "direct_messages.json", true));
     }
 
     /**
      * 我发出的私信列表
      *
      * @return
      */
     public List<Message> getSendDirectMessages() throws TBlogException {
         return Message.constructMessages(http.get(getBaseURL() + "direct_messages/sent.json", true));
     }
 
     /**
      * 发私信
      *
      * @return
      */
     public Message sendDirectMessage(String userId, String text) throws TBlogException {
         PostParameter[] params = new PostParameter[]{
                 new PostParameter("user", userId),
                 new PostParameter("text", text)
         };
         return new Message(http.post(getBaseURL() + "direct_messages/new.json", params, true));
     }
 
 
     /**
      * 删除私信
      *
      * @param mailId
      * @param type   in:收件箱 out:发件箱
      * @return
      */
     public JSONObject destroyDirectMessage(long mailId, String type) throws TBlogException {
         return (http.post(getBaseURL() + "direct_messages/destroy/" + mailId + ".json?type=" + type, true)).asJSONObject();
 
     }
 
 
     /**
      * 获取收藏的微博列表
      *
      * @return
      * @throws TBlogException
      */
     public List<Status> getFavorites() throws TBlogException {
         return Status.constructStatuses(get(getBaseURL() + "favourites.json", true));
     }
 
     /**
      * 添加收藏
      *
      * @param msgId 要收藏的微博ID
      * @return
      * @throws TBlogException
      */
     public Status createFavorite(long msgId) throws TBlogException {
         return new Status(http.post(getBaseURL() + "favourites/create/" + String.valueOf(msgId) + ".json", true));
     }
 
     /**
      * 删除当前用户的收藏
      *
      * @param id 要删除收藏的微博ID
      * @return
      * @throws TBlogException
      */
     public Status destroyFavorite(long id) throws TBlogException {
         return new Status(http.post(getBaseURL() + "favourites/destroy/" + String.valueOf(id) + ".json", true));
     }
 
     /**
      * 判断当前用户是否验证成功并返回该用户信息
      *
      * @return
      * @throws TBlogException
      */
     public User verifyCredentials() throws TBlogException {
         return new User(get(getBaseURL() + "account/verify_credentials.json", true));
     }
 
     /**
      * 修改头像
      *
      * @param file 头像文件 jpg, gif, png
      * @return
      * @throws TBlogException
      */
     public User updateProfileImage(File file) throws TBlogException {
         Response res = http.multPartURL("image", getBaseURL() + "account/update_profile_image.json", new PostParameter[0], file, true);
         return new User(res);
     }
 
 
     /**
      * 修改用户信息
      * 更新用户的昵称、邮箱、性别和简介
      *
      * @param nickName    昵称
      * @param email       邮箱
      * @param gender      性别 0：女；1：男
      * @param description 个人信息
      * @throws
      */
     public User updateProfile(String nickName, String email, String gender, String description) throws TBlogException {
         PostParameter[] params = new PostParameter[]{
                 new PostParameter("nick_name", nickName),
                 new PostParameter("email", email),
                 new PostParameter("gender", gender),
                 new PostParameter("description", description),
         };
 
         return new User(http.post(getBaseURL() + "account/update_profile.json", params, true));
     }
 
 
     /**
      * 更新用户的个性域名
      *
      * @param shortDomain 短域名
      * @return
      * @throws
      */
     public User updateProfileDomain(String shortDomain) throws TBlogException {
         PostParameter[] params = new PostParameter[]{
                 new PostParameter("short_domain", shortDomain)
         };
         return new User(http.post(getBaseURL() + "account/update_profile_domain.json", params, true));
 
     }
 
     /**
      * 查看剩余访问次数
      *
      * @return
      * @throws
      */
     public RateLimitStatus getRateLimitStatus() throws TBlogException {
         return new RateLimitStatus(get(getBaseURL() + "account/rate_limit_status.json", true));
     }
 
     /**
      * 搜索用户
      *
      * @return
      * @throws
      */
     public List<User> searchUsers(String query) throws TBlogException {
         return User.constructUsers(get(getBaseURL() + "users/search.json?q=" + query, true).asJSONArray());
     }
 
 
     /**
      * 搜索微博 返回Search数据集
      *
      * @return
      * @throws
      */
     public List<Search> searchTweets(String query) throws TBlogException, JSONException {
         JSONObject result = get(getBaseURL() + "search.json?q=" + query, true).asJSONObject();
         JSONArray searches = result.getJSONArray("results");
         return Search.constructSearches(searches);
     }
 
 
     /**
      * 搜索微博 返回Status数据集
      *
      * @return
      * @throws
      */
     public List<Status> searchTweetsStatus(String query) throws TBlogException, JSONException {
         JSONObject result = get(getBaseURL() + "statuses/search.json?q=" + query, true).asJSONObject();
         JSONArray statuses = result.getJSONArray("statuses");
         return Status.constructStatuses(statuses);
     }
 
 
     // request wrapper end
 
     private SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
 
     private Response get(String url, boolean authenticated) throws TBlogException {
         return http.get(url, authenticated);
     }
 
 
 }
