 package com.yanchuanli.games.pokr.dao;
 
 import com.mongodb.*;
 import com.yanchuanli.games.pokr.model.Dummy;
 import com.yanchuanli.games.pokr.model.Player;
 import com.yanchuanli.games.pokr.util.*;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.NullNode;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Copyright Candou.com
  * Author: Yanchuan Li
  * Email: mail@yanchuanli.com
  * Date: 12-6-22
  */
 public class PlayerDao {
 
     private static Map<String, Player> players;
     private static int globalid = 0;
     private static Logger log = Logger.getLogger(PlayerDao.class);
     private static ObjectMapper mapper;
 
     static {
         players = new HashMap<>();
         mapper = new ObjectMapper();
     }
 
     /**
      * 从WebServer获取用户信息
      *
      * @param udid
      * @param source 来源[0|1|...]
      * @return
      */
     public static Player getPlayer(String udid, String password,
                                    int source) {
         String json = URLFetchUtil.fetch(ServerConfig.webServerBase
                 + "login?udid=" + udid + "&password=" + password + "&source="
                 + source);
         log.debug(ServerConfig.webServerBase
                 + "login?udid=" + udid + "&password=" + password + "&source="
                 + source);
         if (json != null && json.contains("user")) {
             if (json.contains("{\"user\":null}") || json.contains("\"stat\": 0")) {
                 return null;
             } else {
                 Player player = parsePlayer(json);
                 if (player == null) {
                     return null;
                 }
                 players.put(udid, player);
             }
         }
         return players.get(udid);
     }
 
     public static Player parsePlayer(String json) {
     	Player player;
     	JsonNode root;
 		try {
 			root = mapper.readTree(json);
 			JsonNode content = root.get("user");
 			
 			player = new Player();
 			player.setAddress(content.get("address").getTextValue());
 			player.setAvatar(content.get("avatar").getTextValue());
 			player.setCustomAvatar(content.get("customAvatar").getIntValue());
 			player.setExp(content.get("exp").getIntValue());
 			player.setHistoricalBestHand(content.get("historicalBestHand").getTextValue());
 			player.setHistoricalBestHandRank(content.get("historicalBestHandRank").getIntValue());
 			player.setLevel(content.get("level").getIntValue());
 			player.setLoseCount(content.get("loseCount").getIntValue());
 			player.setMaxWin(content.get("maxWin").getIntValue());
 			player.setMoney(content.get("money").getIntValue());
 			player.setName(content.get("name").getTextValue());
 			player.setOnline(content.get("online").getBooleanValue());
 			JsonNode node = content.get("room");
 			if (!(node instanceof NullNode)) {
 				player.setRoomId(node.get("id").getIntValue());
 				player.setRoomName(node.get("name").getTextValue());
 			}
 			player.setSex(content.get("sex").getIntValue());
 			player.setTimeLevelToday(content.get("timeLevelToday").getIntValue());
 			player.setUdid(content.get("udid").getTextValue());
 			player.setWinCount(content.get("winCount").getIntValue());
 			
 			return player;
 		} catch (IOException e) {
 			e.printStackTrace();
 			player = null;
 		}
 		
         return player;
     }
 
     /**
      * 更新赢的次数
      *
      * @param player
      */
     public static void updateWinCount(Player player) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject("udid", player.getUdid());
         DBObject incQuery = new BasicDBObject("$inc", new BasicDBObject("win", 1));
         coll.update(searchQuery, incQuery);
         player.setWinCount(player.getWinCount() + 1);
     }
 
     /**
      * 更新输的次数
      *
      * @param player
      */
     public static void updateLoseCount(Player player) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject("udid", player.getUdid());
         DBObject incQuery = new BasicDBObject("$inc", new BasicDBObject("lose", 1));
         coll.update(searchQuery, incQuery);
         player.setLoseCount(player.getLoseCount() + 1);
     }
 
     /**
      * 更新资产
      *
      * @param udid
      * @param money
      */
     private static void updateMoney(String udid, int money) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME,
                 MongoDB.COLL_USER);
 
         DBObject query = new BasicDBObject();
         query.put("udid", udid);
 
         DBObject doc = new BasicDBObject().append("$set",
                 new BasicDBObject().append("money", money));
         coll.update(query, doc);
     }
 
     /**
      * 更新bestHand以及bestHandRank
      *
      * @param targetPlayer
      */
     public static void updateBestHandOfPlayer(Player targetPlayer) {
         Player player = queryByUdid(targetPlayer.getUdid());
         if (player != null) {
             if (player.getHistoricalBestHandRank() < targetPlayer.getBestHandRank()) {
                 DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME,
                         MongoDB.COLL_USER);
 
                 DBObject query = new BasicDBObject();
                 query.put("udid", targetPlayer.getUdid());
 
                 DBObject doc = new BasicDBObject().append("$set", new BasicDBObject()
                         .append("best", targetPlayer.getBestHand().getGIndexes()).append("br", targetPlayer.getBestHandRank()));
                 coll.update(query, doc);
             }
         }
     }
 
     /**
      * 更新最大赢取MaxWin
      *
      * @param udid
      * @param maxWin
      */
     public static void updateMaxWin(String udid, int maxWin) {
         Player player = queryByUdid(udid);
         if (player != null && (player.getMaxWin() < maxWin)) {
             DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME,
                     MongoDB.COLL_USER);
 
             DBObject query = new BasicDBObject();
             query.put("udid", udid);
 
             DBObject doc = new BasicDBObject().append("$set",
                     new BasicDBObject().append("max", maxWin));
             coll.update(query, doc);
         }
     }
 
     public static Player queryByUdid(String udid) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME,
                 MongoDB.COLL_USER);
 
         BasicDBObject query = new BasicDBObject();
         query.put("udid", udid);
         DBCursor cur = coll.find(query);
 
         Player player = null;
         while (cur.hasNext()) {
             DBObject obj = cur.next();
             player = new Player((String) obj.get("udid"), (String) obj.get("name"));
             player.setMoney((Integer) obj.get("money"));
             player.setExp((Integer) obj.get("exp"));
             player.setWinCount((Integer) obj.get("win"));
             player.setLoseCount((Integer) obj.get("lose"));
             player.setHistoricalBestHandRank((Integer) obj.get("br"));
             player.setHistoricalBestHand((String) obj.get("best"));
             player.setMaxWin((Integer) obj.get("max"));
             player.setCustomAvatar((Integer) obj.get("customAvatar"));
             player.setAvatar((String) obj.get("face"));
             player.setSex((Integer) obj.get("sex"));
             player.setAddress((String) obj.get("address"));
             player.setRoomId(Integer.MIN_VALUE);
         }
 
         return player;
     }
 
     public static boolean buyIn(Player player, int buyIn) {
         boolean result = false;
         if (player.getMoney() >= buyIn) {
             player.setMoneyInGame(buyIn);
             log.debug(player.getName() + " has buyed in " + player.getMoneyInGame());
             log.info(String.format("%s 已经买入了 %d", player.getName(), player.getMoneyInGame()));
             result = true;
         } else {
             log.debug(player.getName() + " hasnot enough money:" + player.getMoney() + " to buyin:" + buyIn);
             log.info(String.format("%s 的资产数为 %d, 不够买入 %d", player.getName(), player.getMoney(), player.getMoneyInGame()));
         }
         return result;
     }
 
     public static void cashBack(Player player, int holding) {
         log.debug("cashback:" + player.getUdid() + ":" + holding);
         Player persistence = queryByUdid(player.getUdid());
         // plus money
         updateMoney(player.getUdid(), persistence.getMoney() + holding);
 
     }
 
 
     /**
      * 增加经验值
      *
      * @param player
      * @param exp
      * @return 增加exp后的player
      */
     public static void updateExpAndLastTime(Player player, int exp) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject("udid", player.getUdid());
         DBObject incQuery = new BasicDBObject("$inc", new BasicDBObject("exp", exp));
         DBObject updateOnlineTime = new BasicDBObject("$set", new BasicDBObject("update", TimeUtil.unixtime()));
         coll.update(searchQuery, incQuery);
         coll.update(searchQuery, updateOnlineTime);
         player.setExp(player.getExp() + exp);
     }
 
     // 增加经验和提升等级
     public static void addExp(Player player, int exp) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject("udid", player.getUdid());
         DBObject incQuery = new BasicDBObject("$inc", new BasicDBObject("exp", exp));
         coll.update(searchQuery, incQuery);
         player.setExp(player.getExp() + exp);
         int level = Level.getLevel(player.getExp());
         DBObject updateLevel = new BasicDBObject("$set", new BasicDBObject("level", level));
         coll.update(searchQuery, updateLevel);
         player.setLevel(level);
     }
 
     public static void updateLastLoginTime(Player player) {
         int now = TimeUtil.unixtime();
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject("udid", player.getUdid());
         DBObject updateOnlineTime = new BasicDBObject("$set", new BasicDBObject("update", now));
         coll.update(searchQuery, updateOnlineTime);
         player.setLastOnlineTime(now);
     }
 
     public static void updateOnlineStatus(Player player) {
         int now = TimeUtil.unixtime();
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject("udid", player.getUdid());
         DBObject updateOnlineTime;
         DBObject updateOnlineStatus;
         if (player.isOnline()) {
             updateOnlineStatus = new BasicDBObject("$set", new BasicDBObject("online", Config.STATUS_ONLINE));
             updateOnlineTime = new BasicDBObject("$set", new BasicDBObject("update", now));
         } else {
             updateOnlineStatus = new BasicDBObject("$set", new BasicDBObject("offline", Config.STATUS_OFFLINE));
             updateOnlineTime = new BasicDBObject("$set", new BasicDBObject("update", 0));
         }
         coll.update(searchQuery, updateOnlineTime);
         coll.update(searchQuery, updateOnlineStatus);
         player.setLastOnlineTime(now);
 
     }
 
     /**
      * 累加用户在线时间
      *
      * @param player
      * @param seconds
      */
     public static void addElapsedTime(Player player, int seconds) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject("udid", player.getUdid());
         DBObject incQuery = new BasicDBObject("$inc", new BasicDBObject("elapsedTimeToday", seconds));
         coll.update(searchQuery, incQuery);
         player.setElapsedTimeToday(player.getElapsedTimeToday() + seconds);
     }
 
     /**
      * 清0所有用户的当天在线时间
      */
     public static void resetElapsedTime() {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject();
         DBObject setQuery = new BasicDBObject("$set", new BasicDBObject("elapsedTimeToday", 0));
        coll.update(searchQuery, setQuery, true, true);
     }
 
     /**
      * 标记用户当天已经得到过的经验值等级
      *
      * @param player
      */
     public static void updateTimeLevelToday(Player player) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject("udid", player.getUdid());
         DBObject incQuery = new BasicDBObject("$set", new BasicDBObject("timeLevelToday", player.getTimeLevelToday()));
         coll.update(searchQuery, incQuery);
     }
 
     /**
      * 清0所有用户的当天已经得到过的经验值等级
      */
     public static void resetTimeLevelToday() {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject();
         DBObject setQuery = new BasicDBObject("$set", new BasicDBObject("timeLevelToday", 0));
        coll.update(searchQuery, setQuery, true, true);
     }
 
     /**
      * 重置在线状态和房间状态
      */
     public static void resetOnlineStatusAndRoomId() {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject();
 
         DBObject setValue = new BasicDBObject();
         setValue.put("online", 0);
         setValue.put("roomId", 0);
         DBObject setQuery = new BasicDBObject();
         setQuery.put("$set", setValue);
        coll.update(searchQuery, setQuery, true, true);
     }
 
     /**
      * 更新当前玩家的房间信息
      *
      * @param player
      */
     public static void updateRoomId(Player player) {
         DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject(new BasicDBObject("udid", player.getUdid()));
         DBObject setQuery = new BasicDBObject("$set", new BasicDBObject("roomId", player.getRoomId()));
        coll.update(searchQuery, setQuery, true, true);
     }
     
     public static boolean insert(Dummy dummy) {
 		boolean result = true;
 		try {
 			DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME,
 					MongoDB.COLL_USER);
 
 			DBObject doc = new BasicDBObject();
 			doc.put("udid", dummy.getUdid());
 			doc.put("pwd", dummy.getPassword());
 			doc.put("source", dummy.getSource());
 			doc.put("name", dummy.getName());
 			doc.put("money", dummy.getMoney());
 			doc.put("exp", dummy.getExp());
 			doc.put("win", dummy.getWinCount());
 			doc.put("lose", dummy.getLoseCount());
 			doc.put("br", dummy.getHistoricalBestHandRank());
 			doc.put("best", dummy.getHistoricalBestHand());
 			doc.put("max", dummy.getMaxWin());
 			doc.put("face", dummy.getAvatar());
 			doc.put("create", TimeUtil.unixtime());
 			doc.put("update", TimeUtil.unixtime());
 			doc.put("customAvatar", dummy.getCustomAvatar());
 			doc.put("sex", dummy.getSex());
 			doc.put("address", dummy.getAddress());
 			doc.put("level", dummy.getLevel());
 			doc.put("online", dummy.getOnline());
 			doc.put("elapsedTimeToday", dummy.getElapsedTimeToday());
 			doc.put("timeLevelToday", dummy.getTimeLevelToday());
 			doc.put("roomId", dummy.getRoomId());
 
 			coll.insert(doc);
 		} catch (MongoException me) {
 			me.printStackTrace();
 			result = false;
 		}
 		return result;
 	}
 
 }
