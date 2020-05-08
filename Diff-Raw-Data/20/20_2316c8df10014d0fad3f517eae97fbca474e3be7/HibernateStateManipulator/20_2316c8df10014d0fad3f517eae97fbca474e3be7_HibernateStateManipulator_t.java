 package nz.ac.victoria.ecs.kpsmart.state.manipulation;
 
 import java.util.Collection;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import nz.ac.victoria.ecs.kpsmart.InjectOnContruct;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Location;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.MailDelivery;
 
 import com.google.inject.Inject;
 
 @InjectOnContruct
 public final class HibernateStateManipulator implements StateManipulator {
 	@Inject @PersistenceContext
 	private EntityManager entityManager;
 	
 	@Override
 	public MailDelivery getMailDelivery(final long id) {		
 		return entityManager.find(MailDelivery.class, id);
 	}
 
 	@Override
 	public void saveMailDelivery(final MailDelivery delivery) {
		this.entityManager.getTransaction().begin();
 		this.entityManager.merge(delivery);
		this.entityManager.flush();
		this.entityManager.getTransaction().commit();
 	}
 
 	@Override
 	public Collection<Location> getAllLocations() {
		try {
			this.entityManager.getTransaction().begin();
			CriteriaQuery<Location> cb = this.entityManager.getCriteriaBuilder().createQuery(Location.class);
			Root<Location> root = cb.from(Location.class);
			cb.select(root);
			
			return this.entityManager.createQuery(cb).getResultList();
		} finally {
			this.entityManager.getTransaction().commit();
		}
 	}
 
 }
