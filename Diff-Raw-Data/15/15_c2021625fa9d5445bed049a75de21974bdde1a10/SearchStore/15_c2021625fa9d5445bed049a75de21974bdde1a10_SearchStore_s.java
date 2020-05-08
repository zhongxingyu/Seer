 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.UserProfileItem;
 import com.directi.train.tweetapp.services.Auxillary.ShardStore;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 @Service
 public class SearchStore {
     @Autowired private ShardStore shardStore;
 
     public List<UserProfileItem> getResults(String query, Long userId) {
         List<UserProfileItem> usersList = shardStore.getShardDB().query("select userid as id, username, email from shards where username like ? or email like ?",
             UserProfileItem.rowMapper, '%' + query + '%', "%" + query + "%@%.%");
        for (UserProfileItem user : usersList) {
            user.setFollowing(shardStore.getShardByUserId((long) user.getId()).queryForInt("select count(*) from following where user_id = ? and following_id = ?", userId, user.getId()) > 0);
        }
 
        return usersList;
     }
 }
