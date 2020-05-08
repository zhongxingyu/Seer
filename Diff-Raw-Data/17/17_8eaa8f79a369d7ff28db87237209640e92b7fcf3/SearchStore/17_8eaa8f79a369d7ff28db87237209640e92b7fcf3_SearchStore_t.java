 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.UserProfileItem;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 @Service
 public class SearchStore {
     public SimpleJdbcTemplate db;
 
     @Autowired
     public SearchStore(SimpleJdbcTemplate template) {
         db = template;
     }
     public List<UserProfileItem> getResults(String query) {
         return db.query(String.format("select * from users where username like '%%%s%%' or email like '%%%s%%@%%.%%'",query,query), UserProfileItem.rowMapper);
     }
}
