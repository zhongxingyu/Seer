 package com.directi.train.tweetapp.services.Auxillary;
 
 import net.spy.memcached.MemcachedClient;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 
 @Service
 public class ShardStore {
     @Autowired
     private MemcachedClient memcachedClient;
     @Autowired
     @Qualifier("simpleJdbcTemplate2")
     private SimpleJdbcTemplate db2;
 
     @Autowired
     @Qualifier("simpleJdbcTemplate1")
     private SimpleJdbcTemplate db1;
 
     @Autowired
     @Qualifier("shardTemplate")
     private SimpleJdbcTemplate shardDB;
 
     public SimpleJdbcTemplate getShardDB() {
         return shardDB;
     }
 
     public SimpleJdbcTemplate getShardByUserId(Long userId) {
 
         return shardByIndex(cacheLayer("select shard from shards where userid = ?", userId));
     }
 
     public SimpleJdbcTemplate getShardByUserName(String userName) {
         return shardByIndex(cacheLayer("select shard from shards where username = ?", userName));
     }
 
     public SimpleJdbcTemplate getShardByUserEmail(String eMail) {
         return shardByIndex(cacheLayer("select shard from shards where email = ?", eMail));
     }
 
     public SimpleJdbcTemplate getAuthShard() {
         return shardDB;
     }
 
     public void insertNew(String eMail, String userName, String password) {
         SimpleJdbcTemplate db;
 
         int random = Math.random() <= 0.5 ? 1 : 2;
         db = shardByIndex(random);
 
         shardDB.update("insert into shards (username, email, shard) values(?, ?, ?)", userName, eMail, random);
         int userId = cacheLayer("select userid from shards where username = ?", userName);
 
         db.update("insert into users (id, email, username, password) values(?, ?, ?, ?)", userId, eMail, userName, password);
     }
 
     private int cacheLayer(String query, Object arg) {
         String argStr = arg.toString();
        Integer result = (Integer)memcachedClient.get(query + argStr);
         if (result!= null) {
            System.out.println("Entering MemCached");
             return result;
         }
        result = shardDB.queryForInt(query,argStr);
        memcachedClient.set(query + argStr,3600,result);
         return result;
     }
 
     private SimpleJdbcTemplate shardByIndex(int random) {
         SimpleJdbcTemplate db;
         if (random == 1)
             db = db1;
         else
             db = db2;
         return db;
     }
 }
