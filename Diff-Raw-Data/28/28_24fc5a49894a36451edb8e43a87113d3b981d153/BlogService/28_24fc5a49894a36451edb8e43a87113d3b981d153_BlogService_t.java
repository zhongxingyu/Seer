 /**
  * 
  */
 package com.opensajux.service;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import com.google.api.services.blogger.model.Blog;
 import com.google.appengine.api.datastore.Text;
 import com.opensajux.common.SocialSource;
 import com.opensajux.entity.MyBlog;
 import com.opensajux.integration.BloggerClient;
 
 /**
  * @author Sheikh Mohammad Sajid
  * 
  */
@ApplicationScoped
 public class BlogService implements Serializable {
 	private static final long serialVersionUID = 6004592779983773367L;
 
 	@Inject
 	private transient PersistenceManagerFactory pmf;
 	@Inject
 	private BloggerClient client;
 
 	public void saveBlog(String url) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		try {
 			// check if blog by the same url exists
 			Query query = pm.newQuery(MyBlog.class);
 			query.setFilter("name == s1");
 			query.declareParameters("String s1");
 			List<MyBlog> list = (List<MyBlog>) query.execute(url);
 			if (list != null && list.size() > 0)
 				throw new RuntimeException("Duplicate url");
 
 			Blog blog = client.getBlog(url);
 			MyBlog b = new MyBlog();
 			b.setId(blog.getId().toString());
 			b.setName(new Text(blog.getName()));
 			b.setPublishDate(new Date(blog.getPublished().getValue()));
 			b.setSource(SocialSource.BLOGGER);
 			b.setUpdatedDate(new Date(blog.getUpdated().getValue()));
 			b.setUrl(new Text(blog.getUrl()));
 			pm.makePersistent(b);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			pm.close();
 		}
 	}
 
 	public void removeBlog(String id) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Query query = pm.newQuery(MyBlog.class);
 		query.setFilter("id == id1");
 		query.declareParameters("String id1");
 		List<MyBlog> list = (List<MyBlog>) query.execute(id);
 		pm.deletePersistent(list.get(0));
 		pm.close();
 	}
 }
