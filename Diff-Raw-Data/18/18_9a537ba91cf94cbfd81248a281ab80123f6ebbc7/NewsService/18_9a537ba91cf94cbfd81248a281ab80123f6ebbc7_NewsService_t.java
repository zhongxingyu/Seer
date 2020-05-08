 /**
  * 
  */
 package com.opensajux.service;
 
 import java.io.Serializable;
 import java.util.Collection;
import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.opensajux.common.PaginationParameters;
 import com.opensajux.entity.News;
 
 /**
  * @author Sheikh Mohammad Sajid
  * 
  */
 @ApplicationScoped
 public class NewsService implements Serializable {
 	private static final long serialVersionUID = -7611808114349663126L;
 	private static Logger LOGGER = Logger.getLogger(NewsService.class.getName());
 
 	@Inject
 	private transient PersistenceManagerFactory pmf;
 
 	public void saveNews(News news) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		try {
			news.setUpdatedDate(new Date());
 			pm.makePersistent(news);
 			if (LOGGER.isLoggable(Level.INFO))
 				LOGGER.info("Saving news item: " + news.getTitle());
 		} finally {
 			pm.close();
 		}
 	}
 
 	public void removeNews(String id) {
 		News news = getById(id);
 		PersistenceManager pm = pmf.getPersistenceManager();
 		pm.deletePersistent(news);
 		pm.close();
 	}
 
 	public void removeNews(Collection<News> coll) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		pm.deletePersistentAll(coll);
 		pm.close();
 	}
 
 	public void removeNews(News[] selectedNews) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		pm.makePersistentAll(selectedNews);
 		pm.deletePersistentAll(selectedNews);
 		pm.close();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<News> getNews(PaginationParameters params) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Query query = pm.newQuery(News.class);
 		if (params.getSortField() != null)
 			query.setOrdering(params.getSortField() + " " + params.getSortOrder());
 		if (params != null)
 			query.setRange(params.getFirst(), params.getFirst() + params.getPageSize());
 
 		Object object = query.execute();
 		List<News> news = (List<News>) object;
 		news = news.subList(0, news.size());
 		pm.close();
 		return news;
 	}
 
 	public Long getCount() {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Long count = (Long) pm.newQuery("select count(key) from " + News.class.getName()).execute();
 		pm.close();
 		return count;
 	}
 
 	public News getById(String id) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Key k = KeyFactory.createKey(News.class.getSimpleName(), Long.valueOf(id));
 		News news = pm.getObjectById(News.class, k);
 		pm.close();
 		return news;
 	}
 }
