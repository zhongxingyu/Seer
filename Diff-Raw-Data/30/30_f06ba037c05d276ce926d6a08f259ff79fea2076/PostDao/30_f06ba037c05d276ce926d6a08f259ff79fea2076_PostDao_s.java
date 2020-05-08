 package com.example.xpSearchLiang.dao;
 
 import com.example.xpSearchLiang.entity.Post;
 import com.google.inject.Singleton;
 
 import java.util.List;
 import java.util.Map;
 
 @Singleton
 public class PostDao extends BaseHibernateDao<Post> {
 
     public List<Map> fullTextSearch(String q) {
        return null;
     }
 
 }
