 package org.devilry.core.session;
 
 import org.devilry.core.entity.FileMetaEntity;
 import org.devilry.core.entity.DeliveryCandidateEntity;
 import java.util.List;
 import javax.ejb.Stateful;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 @Stateful
 public class DeliveryCandidateImpl implements DeliveryCandidateRemote {
 
 	@PersistenceContext(unitName = "DevilryCore")
 	protected EntityManager em;
 	protected DeliveryCandidateEntity deliveryCandidate;
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public void init(long id) {
 		// TODO: should load from db, but create new for now.
		try {
			em.persist(new DeliveryCandidateEntity());
		} catch (Exception e) {
		}
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public long addFile(String filePath) {
 		FileMetaEntity f = new FileMetaEntity(deliveryCandidate, filePath);
 		em.persist(f);
 		return f.getId();
 	}
 
 	public List<Long> getFileIds() {
 		Query q = em.createQuery("SELECT f.id FROM FileMetaEntity f");
 		return q.getResultList();
 	}
 }
