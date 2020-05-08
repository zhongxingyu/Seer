 package com.example.xpSearchLiang.dao;
 
 import com.example.xpSearchLiang.entity.Post;
 import com.google.inject.Singleton;
import org.hibernate.transform.Transformers;
 
 import java.util.List;
 import java.util.Map;
 
 @Singleton
 public class PostDao extends BaseHibernateDao<Post> {
 
     public List<Map> fullTextSearch(String q) {
        StringBuffer sql = new StringBuffer();
        sql.append("select title,ts_headline(body, plainto_tsquery('")
                .append(q).append("'))  as body ")
                .append(",ts_rank(to_tsvector(body), plainto_tsquery('")
                .append(q)
                .append("')) as rank ")
                .append(" from post order by rank desc");
        return daoHelper.getSession().createSQLQuery(sql.toString())
                .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
     }
 
 }
