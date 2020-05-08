 /**
  * 
  */
 package com.opensajux.service;
 
 import java.io.Serializable;
 import java.util.List;
 
import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Text;
 import com.opensajux.common.PaginationParameters;
 import com.opensajux.entity.SiteLink;
 
 /**
  * @author Sheikh Mohammad Sajid
  * 
  */
@ApplicationScoped
 public class SiteLinkService implements Serializable {
 	private static final long serialVersionUID = 6004592779983773367L;
 
 	@Inject
 	private transient PersistenceManagerFactory pmf;
 
 	@SuppressWarnings("unchecked")
 	public void saveSiteLink(String title, String url) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		try {
 			// check if link by the same url exists
 			Query query = pm.newQuery(SiteLink.class);
 			query.setFilter("url == s1");
 			query.declareParameters("String s1");
 			List<SiteLink> list = (List<SiteLink>) query.execute(url);
 			if (list != null && list.size() > 0)
 				throw new RuntimeException("Duplicate url");
 
 			SiteLink siteLink = new SiteLink();
 			siteLink.setTitle(new Text(title));
 			siteLink.setUrl(new Text(url));
 			pm.makePersistent(siteLink);
 		} finally {
 			pm.close();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void removeSiteLink(String url) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Query query = pm.newQuery(SiteLink.class);
 		query.setFilter("url == url1");
 		query.declareParameters("String url1");
 		List<SiteLink> list = (List<SiteLink>) query.execute(url);
 		pm.deletePersistent(list.get(0));
 		pm.close();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<SiteLink> getLinks(PaginationParameters params) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Query query = pm.newQuery(SiteLink.class);
 		if (params != null)
 			query.setRange(params.getFirst(), params.getFirst() + params.getPageSize());
 
 		List<SiteLink> siteLinks = (List<SiteLink>) query.execute();
 		pm.close();
 		return siteLinks;
 	}
 
 	public Long getCount() {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Long count = (Long) pm.newQuery("select count(key) from " + SiteLink.class.getName()).execute();
 		pm.close();
 		return count;
 	}
 
 	public SiteLink getById(String id) {
 		PersistenceManager pm = pmf.getPersistenceManager();
 		Key k = KeyFactory.createKey(SiteLink.class.getSimpleName(), Long.valueOf(id));
 		SiteLink siteLink = pm.getObjectById(SiteLink.class, k);
 		pm.close();
 		return siteLink;
 	}
 }
