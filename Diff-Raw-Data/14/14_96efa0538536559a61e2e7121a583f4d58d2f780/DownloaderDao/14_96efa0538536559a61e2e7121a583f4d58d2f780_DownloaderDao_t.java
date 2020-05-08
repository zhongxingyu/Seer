 package org.jas.downloader.dao;
 
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
import org.jas.downloader.model.Counter;

 @Named
 public class DownloaderDao {
 	
 	@PersistenceContext
 	private EntityManager entityManager;
 
	public void save(Counter counter) {
		entityManager.persist(counter);
 	}
 
 }
