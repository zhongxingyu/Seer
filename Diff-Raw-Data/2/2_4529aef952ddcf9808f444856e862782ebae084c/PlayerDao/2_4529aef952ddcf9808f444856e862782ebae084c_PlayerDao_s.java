 package com.yanchuanli.games.pokr.dao;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.yanchuanli.games.pokr.model.Player;
 import com.yanchuanli.games.pokr.util.*;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
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
 
     static {
         players = new HashMap<>();
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
         if (json != null && json.contains("user")) {
             if (json.contains("{\"user\":null}") || json.contains("\"stat\": 0")) {
                 return null;
             } else {
 				json = json.replace(
 						"{\"message\":\"登录成功\",\"stat\":1,\"user\":{", "{")
 						.replace("}}", "}");
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
         ObjectMapper mapper = new ObjectMapper();
         try {
             return mapper.readValue(json, Player.class);
         } catch (JsonParseException e) {
             e.printStackTrace();
         } catch (JsonMappingException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
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
             player.setRoomid(Integer.MIN_VALUE);
         }
 
         return player;
     }
 
     public static boolean buyIn(Player player, int buyIn) {
         boolean result = false;
         if (player.getMoney() > buyIn) {
             player.setMoneyInGame(buyIn);
             log.debug(player.getName() + " has buyed in " + player.getMoneyInGame());
             result = true;
         } else {
             log.debug(player.getName() + " hasnot enough money:" + player.getMoney() + " to buyin:" + buyIn);
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
      * @param player
      */
     public static void updateRoomId(Player player) {
     	DBCollection coll = MongoDBFactory.getCollection(MongoDB.DBNAME, MongoDB.COLL_USER);
         DBObject searchQuery = new BasicDBObject(new BasicDBObject("udid", player.getUdid()));
         DBObject setQuery = new BasicDBObject("$set", new BasicDBObject("roomId", player.getRoomid()));
        coll.update(searchQuery, setQuery);
     }
 
 }
