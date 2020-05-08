 package org.conan.search.weibo.action.impl;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.conan.base.exception.WeiboExceptionParser;
 import org.conan.base.service.PageInObject;
 import org.conan.base.service.PageOutObject;
 import org.conan.base.service.SpringService;
 import org.conan.base.util.MyDate;
 import org.conan.search.weibo.action.LoadService;
 import org.conan.search.weibo.action.WeiboActionService;
 import org.conan.search.weibo.model.LoadFrequenceDTO;
 import org.conan.search.weibo.model.TweetDTO;
 import org.conan.search.weibo.model.TweetSourceDTO;
 import org.conan.search.weibo.model.UserDTO;
 import org.conan.search.weibo.model.UserRelateDTO;
 import org.conan.search.weibo.service.LoadFrequenceService;
 import org.conan.search.weibo.service.TweetService;
 import org.conan.search.weibo.service.TweetSourceService;
 import org.conan.search.weibo.service.UserRelateService;
 import org.conan.search.weibo.service.UserService;
 import org.conan.search.weibo.util.WeiboTransfer;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import weibo4j.Friendships;
 import weibo4j.Timeline;
 import weibo4j.examples.oauth2.Log;
 import weibo4j.model.Paging;
 import weibo4j.model.Status;
 import weibo4j.model.StatusWapper;
 import weibo4j.model.User;
 import weibo4j.model.UserWapper;
 import weibo4j.model.WeiboException;
 
 @Service
 public class LoadServiceImpl implements LoadService {
 
     final private static Logger log = Logger.getLogger(Log.class.getName());
 
     @Autowired
     UserService userService;
     @Autowired
     UserRelateService userRelateService;
     @Autowired
     TweetService tweetService;
     @Autowired
     TweetSourceService tweetSourceService;
     @Autowired
     LoadFrequenceService loadFrequenceService;
     @Autowired
     WeiboActionService action;
 
     @Override
     public void fans(long uid, String token) throws WeiboException {
         fans(uid, SpringService.WEIBO_LOAD_COUNT_200, token);
     }
 
     @Override
     public void fans(long uid, int n, String token) throws WeiboException {
         if (!loadLimit(uid, SpringService.LIMIT_WEIBO_LOAD_FANS, 30)) {
             log.info(uid + " Load fans limit! Try later.");
             return;
         }
 
         Friendships fm = new Friendships();
         fm.client.setToken(token);
         int count = n < SpringService.WEIBO_LOAD_COUNT_200 ? n : SpringService.WEIBO_LOAD_COUNT_200;
         int i = 0;
         do {
             UserWapper users = fm.getFollowersById(String.valueOf(uid), (int) count, (int) i);
             for (User u : users.getUsers()) {
                 try {
                     userService.insertUser(WeiboTransfer.user(u));
                 } catch (Exception e) {
                     log.debug("{user:" + uid + "}" + e.getMessage());
                 }
 
                 try {
                     userRelateService.insertUserRelate(new UserRelateDTO(uid, Long.parseLong(u.getId())));
                 } catch (Exception e) {
                     log.debug("{user:" + uid + ",fans:" + u.getId() + "}" + e.getMessage());
                 }
             }
             i += count;
             count = (int) users.getTotalNumber() - i > SpringService.WEIBO_LOAD_COUNT_200 ? SpringService.WEIBO_LOAD_COUNT_200 : (int) users.getTotalNumber() - i;
             n = (int) users.getTotalNumber() > n ? n : (int) users.getTotalNumber();
             log.info(uid + " LOAD fans count: " + i + "/" + n);
         } while (i < n && i <= SpringService.WEIBO_LOAD_COUNT_5000);
 
         loadFrequenceService.insertLoadFrequence(new LoadFrequenceDTO(uid, SpringService.LIMIT_WEIBO_LOAD_FANS, null));
     }
 
     @Override
     public void follow(long uid, String token) throws WeiboException {
         follow(uid, SpringService.WEIBO_LOAD_COUNT_200, token);
     }
 
     @Override
     public void follow(long uid, int n, String token) throws WeiboException {
         if (!loadLimit(uid, SpringService.LIMIT_WEIBO_LOAD_FOLLOW, 30)) {
             log.info(uid + " Load follow limit! Try later.");
             return;
         }
 
         Friendships fm = new Friendships();
         fm.client.setToken(token);
         int count = n < SpringService.WEIBO_LOAD_COUNT_200 ? n : SpringService.WEIBO_LOAD_COUNT_200;
         int i = 0;
 
         do {
             UserWapper users = fm.getFriendsById(String.valueOf(uid), (int) count, (int) i);
             for (User u : users.getUsers()) {
                 try {
                     userService.insertUser(WeiboTransfer.user(u));
                 } catch (Exception e) {
                     log.debug("{user:" + uid + "}" + e.getMessage());
                 }
                 try {
                     userRelateService.insertUserRelate(new UserRelateDTO(Long.parseLong(u.getId()), uid));
                 } catch (Exception e) {
                     log.debug("{user:" + u.getId() + ",fans:" + uid + "}" + e.getMessage());
                 }
             }
             i += count;
             count = (int) users.getTotalNumber() - i > SpringService.WEIBO_LOAD_COUNT_200 ? SpringService.WEIBO_LOAD_COUNT_200 : (int) users.getTotalNumber() - i;
             n = (int) users.getTotalNumber() > n ? n : (int) users.getTotalNumber();
             log.info(uid + " LOAD follow count: " + i + "/" + n);
         } while (i < n && i <= SpringService.WEIBO_LOAD_COUNT_2000);
         loadFrequenceService.insertLoadFrequence(new LoadFrequenceDTO(uid, SpringService.LIMIT_WEIBO_LOAD_FOLLOW, null));
     }
 
     @Override
     public void tweet(long uid, String token) throws WeiboException {
         tweet(uid, SpringService.WEIBO_LOAD_COUNT_200, token);
     }
 
     @Override
     public void tweet(long uid, int total, String token) throws WeiboException {
         int diffDays = loadDiffDays(uid, SpringService.LIMIT_WEIBO_LOAD_TWEET);
         if (diffDays >= 0 && diffDays <= 3) {// 3天之前不重复取
             log.info(uid + " Load tweet limit! Try later.");
             return;
 
         } else if (diffDays > 1) {// 以前取过调用since_id
             Map<String, Object> map = new HashMap<String, Object>();
             map.put("uid", uid);
             PageOutObject<TweetDTO> pageOut = tweetService.getTweetsPaging(map, new PageInObject(0, 1, "id", "desc"));// 最后一条tweet
             if (pageOut.getCount() > 0)
                 tweet(uid, pageOut.getList().get(0).getTid(), token);
 
         } else {// 新的没取过的
             Timeline tm = new Timeline();
             tm.client.setToken(token);
 
             int count = total < SpringService.WEIBO_LOAD_COUNT_100 ? total : SpringService.WEIBO_LOAD_COUNT_100;
             int page = 1;
             int num = count * page;
 
             do {
                 try {
                     num = count * page++;// 504 Gateway time-out
                     StatusWapper status = tm.getUserTimelineByUid(String.valueOf(uid), new Paging((page - 1), count));
                     insertTweets(status);
                     total = (int) status.getTotalNumber() > total ? total : (int) status.getTotalNumber();
                     log.info(uid + " LOAD tweet count: " + num + "/" + total);
                 } catch (WeiboException we) {
                     WeiboExceptionParser.parserCode(we, WeiboExceptionParser.SITE.CLIENT);
                 }
             } while (num < total);
         }
         loadFrequenceService.insertLoadFrequence(new LoadFrequenceDTO(uid, SpringService.LIMIT_WEIBO_LOAD_TWEET, null));
     }
 
     @Override
     public void tweet(long uid, long tid, String token) throws WeiboException {
         Timeline tm = new Timeline();
         tm.client.setToken(token);
 
         int num = 0;
         int page = 1;
         while (true) {
             try {
                 StatusWapper status = tm.getUserTimelineByUid(String.valueOf(uid), new Paging(page++, SpringService.WEIBO_LOAD_COUNT_100, tid));
                 if (status == null || status.getStatuses() == null || status.getStatuses().size() == 0) {
                     break;
                 }
                 num += status.getStatuses().size();
                 insertTweets(status);
                 log.info(uid + " LOAD tweet since " + tid + " Count " + num);
             } catch (WeiboException we) {
                 WeiboExceptionParser.parserCode(we, WeiboExceptionParser.SITE.CLIENT);
             }
         }
     }
 
     /**
      * @return true允许,false不允许
      */
     @Override
     public boolean loadLimit(long uid, String type, int date) {
        return loadDiffDays(uid, type) <= SpringService.TIME_DAY * date ? false : true;
     }
 
     /**
      * 限制时间
      */
     @Override
     public int loadDiffDays(long uid, String type) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("uid", uid);
         map.put("type", type);
         List<LoadFrequenceDTO> list = loadFrequenceService.getLoadFrequencesPaging(map, new PageInObject(0, 1, "id", "desc")).getList();
         int days = -1;
         if (list.size() > 0) {
             long diff = MyDate.diffSecs(new Date(), list.get(0).getCreate_date());
             days = (int) Math.floor(diff / SpringService.TIME_DAY);
         }
         return days;
     }
 
     private void insertTweets(StatusWapper status) {
         for (Status s : status.getStatuses()) {
             Map<String, Object> map = WeiboTransfer.tweet(s);
 
             if (map.size() > 0) {
                 if (map.containsKey(WeiboTransfer.KEY_RETWEET)) {
                     TweetDTO retweet = (TweetDTO) map.get(WeiboTransfer.KEY_RETWEET);
                     TweetSourceDTO rets = (TweetSourceDTO) map.get(WeiboTransfer.KEY_RETWEET_SOURCE);
                     try {
                         tweetSourceService.insertTweetSource(rets);
                     } catch (Exception e) {
                         log.debug("{tweet source:" + rets.getName() + "}" + e.getMessage());
                     }
                     try {
                         tweetService.insertTweet(retweet);
                     } catch (Exception e) {
                         log.debug("{tweet:" + retweet.getTid() + "}" + e.getMessage());
                     }
                 }
 
                 TweetDTO tweet = (TweetDTO) map.get(WeiboTransfer.KEY_TWEET);
                 TweetSourceDTO ts = (TweetSourceDTO) map.get(WeiboTransfer.KEY_TWEET_SOURCE);
 
                 try {
                     tweetSourceService.insertTweetSource(ts);
                 } catch (Exception e) {
                     log.debug("{tweet source:" + ts.getName() + "}" + e.getMessage());
                 }
                 try {
                     tweetService.insertTweet(tweet);
                 } catch (Exception e) {
                     log.debug("{tweet:" + tweet.getTid() + "}" + e.getMessage());
                 }
             }
         }
     }
 
     @Override
     public Long getUidByScreen(String screen, String token) throws WeiboException, IOException {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("screen_name", screen);
 
         UserDTO dto = userService.getUserOne(map);
         Long uid = null;
         if (dto != null && dto.getUid() > 0) {
             uid = dto.getUid();
         } else {
             User u = action.user(screen, token);
             uid = Long.parseLong(u.getId());
             try {
                 userService.insertUser(WeiboTransfer.user(u));
             } catch (Exception e) {
                 log.warn("{user:" + uid + "}" + e.getMessage());
             }
         }
         log.info(uid + "," + screen);
         return uid;
     }
 
     @Override
     public String getScreenByUid(long uid, String token) throws WeiboException, IOException {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("uid", uid);
 
         UserDTO dto = userService.getUserOne(map);
         String screen = null;
         if (dto != null && dto.getUid() > 0) {
             screen = dto.getScreen_name();
         } else {
             User u = action.user(uid, token);
             screen = u.getScreenName();
             try {
                 userService.insertUser(WeiboTransfer.user(u));
             } catch (Exception e) {
                 log.warn("{user:" + uid + "}" + e.getMessage());
             }
         }
         log.info(uid + "," + screen);
         return screen;
     }
 
 }
