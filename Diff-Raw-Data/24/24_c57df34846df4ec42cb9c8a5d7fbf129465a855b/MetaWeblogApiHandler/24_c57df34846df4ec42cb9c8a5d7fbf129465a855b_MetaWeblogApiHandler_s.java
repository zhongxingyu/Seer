 package org.otherobjects.cms.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.dao.DynaNodeDao;
 import org.otherobjects.cms.model.DynaNode;
 
 /**
  * See API documentation is available at <a href="http://www.xmlrpc.com/metaWeblogApi">http://www.xmlrpc.com/metaWeblogApi</a> 
  * @author rich
  */
 @SuppressWarnings("unchecked")
 public class MetaWeblogApiHandler
 {
     private DaoService daoService;
 
     public void setDaoService(DaoService daoService)
     {
         this.daoService = daoService;
     }
 
     public List<Map<String, Object>> getRecentPosts(String blogid, String username, String password, int numberOfPosts)
     {
         DynaNodeDao dao = (DynaNodeDao) this.daoService.getDao("DynaNode");
         List<DynaNode> news = dao.getAllByPath("/site/news/");
         List<Map<String, Object>> posts = new ArrayList<Map<String, Object>>();
 
         for (DynaNode newsStory : news)
         {
             posts.add(convertNodeToPost(newsStory));
         }
 
         return posts;
     }
 
     private Map<String, Object> convertNodeToPost(DynaNode node)
     {
         Map<String, Object> post = new HashMap<String, Object>();
        post.put("postid", node.getId());
         post.put("title", node.getLabel());
        post.put("description", node.get("content"));
        post.put("link", node.getLinkPath());
         return post;
     }
 
     private DynaNode convertPostToNode(Map<String, Object> post)
     {
         DynaNodeDao dao = (DynaNodeDao) this.daoService.getDao("DynaNode");
         DynaNode node = null;
        if (post.containsKey("postid"))
            node = dao.get((String) post.get("postid"));
         else
         {
             node = dao.create("com.maureenmichaelson.site.model.NewsStory");
             node.setPath("/site/news/");
         }
         node.setLabel((String) post.get("title"));
         node.set("content", post.get("description"));
         return node;
     }
 
     public String newPost(String blogid, String username, String password, Map post, boolean publish)
     {
         DynaNodeDao dao = (DynaNodeDao) this.daoService.getDao("DynaNode");
         DynaNode node = convertPostToNode(post);
         dao.save(node);
         if (publish)
             dao.publish(node);
        return node.getId();
     }
 
     public boolean editPost(String postId, String username, String password, Map post, boolean publish)
     {
         DynaNodeDao dao = (DynaNodeDao) this.daoService.getDao("DynaNode");
         DynaNode node = convertPostToNode(post);
         dao.save(node);
         if (publish)
             dao.publish(node);
         return true;
     }
 
     public Object getPost(String postId, String username, String password)
     {
        return null;
     }
 
     public void newMediaObject(String blogid, String username, String password, Object struct)
     {
     }
 
     public Object[] getCategories(String blogid, String username, String password)
     {
         return null;
     }
 }
