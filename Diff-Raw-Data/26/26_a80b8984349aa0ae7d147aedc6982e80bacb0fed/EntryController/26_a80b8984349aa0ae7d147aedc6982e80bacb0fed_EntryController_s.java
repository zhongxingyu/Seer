 package com.mavenlab.jetset.controller;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 
 import com.mavenlab.jetset.model.Entry;
 
 @Stateless
 public class EntryController {
 
 	@Inject
 	private EntityManager em;
 	
 	public Entry getEntryFetchAll(int id) {
 		try {
 			return (Entry) em.createNamedQuery("jetset.query.Entry.findIdFetchAll").setParameter("id", id).getSingleResult();
 		} catch(NoResultException e) {
 		}
 
 		return null;
 	}
 	
 	public boolean isDuplicate(Entry entry, boolean web) {
 		
 		long count = (Long) em.createNamedQuery("jetset.query.Entry.duplicateCheck").
 								setParameter("receipt", entry.getReceipt()).
 								setParameter("stationId", entry.getStation().getId()).
 								getSingleResult();
 
		return (count > 1 && !web) || (count > 0);
 	}
 	
 }
