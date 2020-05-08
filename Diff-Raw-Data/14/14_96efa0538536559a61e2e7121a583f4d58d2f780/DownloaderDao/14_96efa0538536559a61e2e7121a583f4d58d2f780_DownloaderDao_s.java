 package org.jas.downloader.dao;
 
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 @Named
 public class DownloaderDao {
 	
 	@PersistenceContext
 	private EntityManager entityManager;
 
	public void save(String remoteAddress) {
		entityManager.persist(remoteAddress);
 	}
 
 }
