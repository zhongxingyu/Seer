 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.UserProfileItem;
 import com.directi.train.tweetapp.services.Auxillary.ShardStore;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 @Service
 public class SearchStore {
     @Autowired private ShardStore shardStore;
    @Autowired private UserStore userStore;
 
     public List<UserProfileItem> getResults(String query, Long userId) {
         List<UserProfileItem> usersList = shardStore.getShardDB().query("select userid as id, username, email from shards where username like ? or email like ?",
             UserProfileItem.rowMapper, '%' + query + '%', "%" + query + "%@%.%");
 
        return userStore.applyFollowing(userId, usersList);
     }
 }
