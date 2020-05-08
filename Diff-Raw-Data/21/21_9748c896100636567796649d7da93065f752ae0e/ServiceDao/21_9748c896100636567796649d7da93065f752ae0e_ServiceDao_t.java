 package database.dao.impl;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 
 import database.dao.IServiceDao;
 import database.entity.WebService;
 
 public class ServiceDao implements IServiceDao {
 
 	private Logger log = Logger.getLogger(this.getClass());
 	
 	
 	private EntityManager em;
 	
 	public ServiceDao(EntityManager em) {
 		this.em = em;
 	}
 	
 	@Override
 	public void addService(WebService service) {
 		this.log.debug("Try to store service " + service.getUrl() + " to database.");
 		em.persist(service);
 		this.log.debug("Stored service " + service.getUrl() + " and ID=" + service.getId() + " to database.");
 	}
 
 	@Override
 	public void updateService(WebService service) {
 		this.log.debug("Try to update " + service.getUrl() + ".");
 		em.merge(service);
 		this.log.debug("Updated service " + service.getUrl() + ".");
 	}
 
 	@Override
 	public void deleteService(WebService service) {
 		this.log.debug("Try to delete service " + service.getUrl() + ".");
 		em.remove(service);
 		this.log.debug("Deleted service " + service.getUrl() + ".");
 	}
 
 	@Override
 	public WebService getService(Long id) {
 		this.log.debug("Try to get service with ID=" + id + "from database.");
 		WebService ws = em.find(WebService.class, id);
 		if (ws != null) {
 			this.log.debug("Return service " + ws.getUrl() + ".");
 		}
 		return ws;
 	}
 
 	@Override
 	public WebService getByURL(String url) {
 		this.log.debug("Try to get service with URL=" + url + "from database.");
 		Query query = em.createQuery("SELECT w FROM WebService w WHERE url = :url ", WebService.class);
 		query.setParameter("url", url);
 		List<?> result = query.getResultList();
 		if (result == null || result.isEmpty()) {
 			return null;
 		}
 		this.log.debug("Found service with url " + url);
 		return (WebService) result.get(0);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<WebService> getAll() {
 		List<WebService> result = em.createQuery("SELECT ws FROM WebService ws").getResultList();
 		return result;
 	}
 
 }
