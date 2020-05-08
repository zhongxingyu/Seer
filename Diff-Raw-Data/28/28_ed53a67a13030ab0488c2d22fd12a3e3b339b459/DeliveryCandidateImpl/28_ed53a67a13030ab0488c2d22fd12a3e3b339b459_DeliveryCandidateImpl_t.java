 package org.devilry.core.session.dao;
 
 import org.devilry.core.entity.Delivery;
 import org.devilry.core.entity.FileMeta;
 import org.devilry.core.entity.DeliveryCandidate;
 
 import java.util.Date;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 @Stateless
 public class DeliveryCandidateImpl implements DeliveryCandidateRemote {
 
 	@PersistenceContext(unitName = "DevilryCore")
 	protected EntityManager em;
 		
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public long create(long deliveryId) {
 		
 		Delivery delivery = em.find(Delivery.class, deliveryId);
 		
 		DeliveryCandidate candidate = new DeliveryCandidate();
 		// Set parent
 		candidate.setDelivery(delivery);
 		
 		// Set time of delivery
 		setTimeOfDelivery(candidate.getId(), new Date());
 		
 		em.persist(candidate);
 		em.flush();
 		
 		return candidate.getId();
 	}
 	
 	protected DeliveryCandidate getDeliveryCandidate(long deliveryCandidateId) {
 		return em.find(DeliveryCandidate.class, deliveryCandidateId);
 	}
 	
 
 	public long getDelivery(long deliveryCandidateId) {
 		return getDeliveryCandidate(deliveryCandidateId).getDelivery().getId();
 	}
 	
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public long addFile(long deliveryCandidateId, String filePath) {
 		
 		DeliveryCandidate deliveryCandidate = getDeliveryCandidate(deliveryCandidateId);
 		
 		FileMeta fileMeta = new FileMeta();
 		fileMeta.setDeliveryCandidate(deliveryCandidate);
 		fileMeta.setFilePath(filePath);		
 		
 		em.persist(fileMeta);
 		em.flush();
 		return fileMeta.getId();
 	}
 
 	public List<Long> getFiles(long deliveryCandidateId) {
 		Query q = em.createQuery("SELECT f.id FROM FileMeta f "
 				+ "WHERE f.deliveryCandidate.id = :id ORDER BY f.filePath");
 		q.setParameter("id", deliveryCandidateId);
 		return q.getResultList();
 	}
 
 
 	public int getStatus(long deliveryCandidateId) {
 		return getDeliveryCandidate(deliveryCandidateId).getStatus();
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public void setStatus(long deliveryCandidateId, int status) {
 		getDeliveryCandidate(deliveryCandidateId).setStatus(status);
 	}
 
 	public Date getTimeOfDelivery(long deliveryCandidateId) {
 		return getDeliveryCandidate(deliveryCandidateId).getTimeOfDelivery();
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public void setTimeOfDelivery(long deliveryCandidateId, Date timeOfDelivery) {
		System.err.println("getDeliveryCandidate("+deliveryCandidateId+"):" + getDeliveryCandidate(deliveryCandidateId));
		
 		getDeliveryCandidate(deliveryCandidateId).setTimeOfDelivery(timeOfDelivery);
 	}
 }
