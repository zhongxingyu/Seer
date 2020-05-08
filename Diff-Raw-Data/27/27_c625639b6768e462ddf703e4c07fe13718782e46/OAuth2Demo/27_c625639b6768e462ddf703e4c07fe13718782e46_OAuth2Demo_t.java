 package t4j.example;
 
 import t4j.OAuthVersion;
 import t4j.TBlog;
 import t4j.TBlogException;
 import t4j.data.*;
 import t4j.org.json.JSONException;
 import t4j.org.json.JSONObject;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.List;
 
 
 /**
  * User: chenxiaojian
  * Date: 13-1-8
  */
 
 public class OAuth2Demo {
 
     public static void main(String[] args) throws TBlogException, IOException, JSONException {
         TBlog tblog = new TBlog(OAuthVersion.V2);
        tblog.setOAuth2AccessToken("c45495c5d344d23ab9723f9bf32a37e");
        showUser(tblog);
     }
 
     /**
      * 获取当前登录用户的微博列表
      *
      * @return
      */
     public static void getHomeTimeline(TBlog tblog) throws TBlogException, IOException {
         List<Status> statusList = tblog.getHomeTimeline();
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
     /**
      * 获取最新的公共微博列表(随便看看)
      *
      * @return
      */
     public static void getPublicTimeline(TBlog tblog) throws TBlogException, IOException {
         List<Status> statusList = tblog.getPublicTimeline();
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
     /**
      * 获取指定uid用户的微博列表
      *
      * @return
      */
     public static void getMyTweets(TBlog tblog) throws TBlogException, IOException {
         List<Status> statusList = tblog.getMyTweets();
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
     /**
      * 获取指定uid用户的微博列表
      * <p/>
      *
      * @return
      */
     public static void getUserTimelineByUid(TBlog tblog) throws TBlogException, IOException {
         long userId = 100945678L;
         List<Status> statusList = tblog.getUserTimeline(userId);
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
 
     }
 
     /**
      * 获取指定username用户的微博列表
      *
      * @return
      */
     public static void getUserTimeline(TBlog tblog) throws TBlogException, IOException {
         String userName = URLEncoder.encode("卫见见", "utf-8");
         List<Status> statusList = tblog.getUserTimeline(userName);
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
     /**
      * 获取@评论当前登录用户的微博列表
      *
      * @return
      * @throws TBlogException
      */
     public static void getMentions(TBlog tblog) throws TBlogException, IOException {
         List<Status> statusList = tblog.getMentions();
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
     /**
      * 查看指定微博的所有评论
      *
      * @return
      * @throws TBlogException
      */
     public static void getComments(TBlog tblog) throws TBlogException, IOException {
         List<Status> statusList = tblog.getComments(6596104880L);
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
     /**
      * 查看别人对我的评论
      *
      * @return
      * @throws TBlogException
      */
     public static void getCommentsToMe(TBlog tblog) throws TBlogException, IOException {
         List<Status> statusList = tblog.getCommentsToMe();
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
 
     /**
      * 显示一条微博的详细信息
      *
      * @return
      */
     public static void show(TBlog tblog) throws TBlogException, IOException {
         Status status = tblog.show(6582393002L);
         System.out.println(status.getText());
     }
 
     /**
      * 发布一条不带图微博
      *
      * @return
      */
     public static void updateStatus(TBlog tblog) throws TBlogException, IOException {
         String text = URLEncoder.encode("发一条微博", "utf-8");
         Status status = tblog.updateStatus(text);
         System.out.println(status.getText());
     }
 
     /**
      * 发布一条不带图微博
      *
      * @return
      */
     public static void uploadStatus(TBlog tblog) throws TBlogException, IOException {
         String text = URLEncoder.encode("发一条带图微博", "utf-8");
         File file = new File("/Users/chenxiaojian/Desktop/1.jpg");
         String ret = tblog.uploadStatus(text, file);
         System.out.println(ret);
     }
 
     /**
      * 删除指定的微博，也可以撤销已转发的微博
      *
      * @return
      */
     public static void destroyStatus(TBlog tblog) throws TBlogException, IOException {
         long statusId = 6580515039L;
         Status status = tblog.destroy(statusId);
         System.out.println(status.getText());
     }
 
     /**
      * 转发一条微博
      *
      * @return
      */
     public static void retweet(TBlog tblog) throws TBlogException, IOException {
         long id = 6577894435L;
         String text = URLEncoder.encode("转发一条微博", "utf-8");
         Status status = tblog.retweet(id, text);
         System.out.println(status.getText());
     }
 
     /**
      * 评论一条微博
      *
      * @return
      */
     public static void reply(TBlog tblog) throws TBlogException, IOException, JSONException {
         long id = 6472887860L;
         String text = URLEncoder.encode("好jb难搞，wo擦妹的，屌丝们没用的", "utf-8");
         Status status = tblog.reply(id, text);
         System.out.println(status.getText());
 
     }
 
     /**
      * 删除一条评论
      *
      * @return
      */
     public static void destroyReply(TBlog tblog) throws TBlogException, IOException, JSONException {
         long rpId = 5158123517L;
         Status status = tblog.destroyReply(rpId);
         System.out.println(status.getText());
     }
 
     /**
      * 根据微博id，获得某条微博的评论数和转发数
      *
      * @return
      */
     public static void getMsgCounts(TBlog tblog) throws TBlogException, IOException {
         long msgId = 6596439986L;
         Counts counts = tblog.getMsgCounts(msgId);
         System.out.println("comment_count: " + counts.getComments_count() + "  transmit_count: " + counts.getTransmit_count());
 
     }
 
     /**
      * 根据微博id，获得某条微博的评论数和转发数
      * 批量接口
      *
      * @return
      */
     public static void getBatchMsgCounts(TBlog tblog) throws TBlogException, IOException {
         List<Counts> countsList = tblog.getBatchMsgCounts("6596439986", "6596439986", "6596439986", "6596439986");
         for (Counts counts : countsList)
             System.out.println("comment_count: " + counts.getComments_count() + "  transmit_count: " + counts.getTransmit_count());
 
     }
 
 
     /**
      * 获取用户的最新提醒消息数包括“新粉丝数”、“新@提醒数”、“新评论数”、“新私信数”、“新微博数”
      *
      * @return
      */
     public static void getNewCounts(TBlog tblog) throws TBlogException, IOException {
         NewCounts newCounts = tblog.getNewCounts();
         System.out.println("new_msg_count: " + newCounts.getNew_msg_count() + "\r\n"
                 + "new_at_count: " + newCounts.getNew_at_count() + "\r\n"
                 + "new_comment_count: " + newCounts.getNew_comment_count() + "\r\n"
                 + "new_followers_count: " + newCounts.getNew_followers_count() + "\r\n"
                 + "new_twitter_count: " + newCounts.getNew_twitter_count() + "\r\n");
     }
 
 
     /**
      * 上传视频
      *
      * @return
      */
     public static void uploadVideo(TBlog tblog) throws TBlogException, IOException {
         String videoUrl = "http://tv.sohu.com/20130104/n362394497.shtml";
         Status status = tblog.uploadVideo(videoUrl);
         System.out.println(status.getText());
     }
 
     /**
      * 显示用户详情
      *
      * @return
      */
     public static void showUser(TBlog tblog) throws TBlogException, IOException {
        User showUser = tblog.showUser();
         System.out.println(showUser.getDescription());
        User showUser1 = tblog.showUser(67427379L);
        System.out.println(showUser1.getDescription());
         User showUser2 = tblog.showUser(URLEncoder.encode("向上de少年", "utf-8"));
         System.out.println(showUser2.getDescription());
     }
 
     /**
      * 查看用户a是否关注用户b
      *
      * @return
      */
     public static void isFollowing(TBlog tblog) throws TBlogException, JSONException, IOException {
         long uidA = 67427379L;
         long uidB = 246913395L;
         JSONObject isFollowing = tblog.isFollowing(uidA, uidB);
         System.out.println(isFollowing.getString("friends"));
 
         String userA = URLEncoder.encode("向上de少年", "utf-8");
         String userB = URLEncoder.encode("test14002", "utf-8");
         JSONObject isFollowing2 = tblog.isFollowing(userA, userB);
         System.out.println(isFollowing2.getString("friends"));
 
     }
 
 
     /**
      * 查看用户之间的关系
      *
      * @return
      */
     public static void showRelationShip(TBlog tblog) throws TBlogException, JSONException, IOException {
         long uidA = 67427379L;
         long uidB = 246913395L;
         JSONObject relationShip1 = tblog.showRelationShip(uidA, uidB);
         System.out.println(relationShip1.toString());
 
         String userA = URLEncoder.encode("向上de少年", "utf-8");
         String userB = URLEncoder.encode("test14002", "utf-8");
         JSONObject relationShip2 = tblog.showRelationShip(userA, userB);
         System.out.println(relationShip2.toString());
     }
 
     /**
      * 添加关注
      *
      * @return
      */
     public static void addFollow(TBlog tblog) throws TBlogException, JSONException, IOException {
         long uid = 370197083L;
         User user1 = tblog.addFollow(uid);
         System.out.println(user1.toString());
 
         String screenName = URLEncoder.encode("流年夏日", "utf-8");
         User user2 = tblog.addFollow(screenName);
         System.out.println(user2.toString());
     }
 
     /**
      * 取消关注
      *
      * @return
      */
     public static void cancelFollow(TBlog tblog) throws TBlogException, IOException {
         long uid = 370197083L;
         User user1 = tblog.cancelFollow(uid);
         System.out.println(user1.toString());
 
         String screenName = URLEncoder.encode("流年夏日", "utf-8");
         User user2 = tblog.cancelFollow(screenName);
         System.out.println(user2.toString());
     }
 
     /**
      * 好友列表
      *
      * @return
      */
     public static void getFriendsList(TBlog tblog) throws TBlogException, IOException {
         long uid = 67427379L;
         List<User> userList = tblog.getFriendsList(uid);
         for (User user : userList) {
             System.out.println(user.getScreen_name());
         }
     }
 
     /**
      * 粉丝列表
      *
      * @return
      */
     public static void getFollowsList(TBlog tblog) throws TBlogException, IOException {
         long uid = 67427379L;
         List<User> userList = tblog.getFollowsList(uid);
         for (User user : userList) {
             System.out.println(user.getScreen_name());
         }
     }
 
     /**
      * 我收到的私信列表
      *
      * @return
      */
     public static void getRecieveDirectMessages(TBlog tblog) throws TBlogException, IOException {
         List<Message> messages = tblog.getRecieveDirectMessages();
         for (Message message : messages) {
             System.out.println(message.getText());
         }
     }
 
     /**
      * 我收到的私信列表
      *
      * @return
      */
     public static void getSendDirectMessages(TBlog tblog) throws TBlogException, IOException {
         List<Message> messages = tblog.getSendDirectMessages();
         for (Message message : messages) {
             System.out.println(message.getText());
         }
     }
 
     /**
      * 发送私信
      *
      * @return
      */
     public static void sendDirectMessage(TBlog tblog) throws TBlogException, IOException {
         long userId = 67427379L;
         String text = URLEncoder.encode("发一封私信", "utf-8");
         tblog.sendDirectMessage(String.valueOf(userId), text);
     }
 
     /**
      * 发送私信
      *
      * @return
      */
     public static void destroyDirectMessage(TBlog tblog) throws TBlogException, IOException {
         long mailId = 201383546L;
         String type = "out";
         JSONObject ret = tblog.destroyDirectMessage(mailId, type);
         System.out.println(ret.toString());
     }
 
 
     /**
      * 获取收藏的微博列表
      *
      * @return
      */
     public static void getFavorites(TBlog tblog) throws TBlogException, IOException {
         List<Status> statusList = tblog.getFavorites();
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
     /**
      * 添加收藏
      *
      * @return
      */
     public static void createFavorite(TBlog tblog) throws TBlogException, IOException {
         long msgId = 6501539249L;
         Status status = tblog.createFavorite(msgId);
         System.out.println(status.getText());
     }
 
     /**
      * 删除收藏
      *
      * @return
      */
     public static void cancelFavorite(TBlog tblog) throws TBlogException, IOException {
         long msgId = 6501539249L;
         Status status = tblog.destroyFavorite(msgId);
         System.out.println(status.getText());
     }
 
     /**
      * 判断当前用户是否验证成功并返回该用户信息,登录时使用
      *
      * @return
      */
     public static void verifyCredentials(TBlog tblog) throws TBlogException, IOException {
         User user = tblog.verifyCredentials();
         System.out.println(user.getScreen_name());
     }
 
     /**
      * 修改头像
      *
      * @return
      */
     public static void updateProfileImage(TBlog tblog) throws TBlogException, IOException {
         File image = new File("/Users/chenxiaojian/Desktop/1.jpg");
         User user = tblog.updateProfileImage(image);
         System.out.println(user.getScreen_name());
     }
 
     /**
      * 修改用户信息
      * 更新用户的昵称、邮箱、性别和简介
      *
      * @return
      */
     public static void updateProfile(TBlog tblog) throws TBlogException, IOException {
         String description = "换个名字而已!!!";
         tblog.updateProfile("", "", "", description);
     }
 
     /**
      * 更新用户的个性域名
      *
      * @return
      */
     public static void updateProfileDomain(TBlog tblog) throws TBlogException, IOException {
         String smallDomain = "openapitest.t.sohu.com";
         tblog.updateProfileDomain(smallDomain);
     }
 
     /**
      * 查看剩余访问次数
      *
      * @return
      */
     public static void getRateLimitStatus(TBlog tblog) throws TBlogException, IOException {
         RateLimitStatus rateLimitStatus = tblog.getRateLimitStatus();
         System.out.println(rateLimitStatus.getRemaining_hits());
     }
 
     /**
      * 搜索用户
      *
      * @return
      */
     public static void searchUsers(TBlog tblog) throws TBlogException, IOException {
         String query = URLEncoder.encode("向上de少年", "utf-8");
         List<User> userList = tblog.searchUsers(query);
         for (User user : userList) {
             System.out.println(user.getScreen_name());
         }
     }
 
 
     /**
      * 搜索微博  返回Search数据集
      *
      * @return
      */
     public static void searchTweets(TBlog tblog) throws TBlogException, JSONException, IOException {
         String query = URLEncoder.encode("向上de少年", "utf-8");
         List<Search> searchList = tblog.searchTweets(query);
         for (Search searchUser : searchList) {
             System.out.println(searchUser.getText());
         }
     }
 
     /**
      * 搜索微博  返回Status数据集
      *
      * @return
      */
     public static void searchTweetsStatus(TBlog tblog) throws TBlogException, JSONException, IOException {
         String query = URLEncoder.encode("向上de少年", "utf-8");
         List<Status> statusList = tblog.searchTweetsStatus(query);
         for (Status status : statusList) {
             System.out.println(status.getText());
         }
     }
 
 
 }
