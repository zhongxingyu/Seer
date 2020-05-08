 package nl.zuyd.marktplaats_revised.repositories;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.ejb.Local;
 import javax.ejb.Singleton;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.transaction.UserTransaction;
 
 import nl.zuyd.marktplaats_revised.entities.Advertisement;
 
 @Singleton
 @Local(IAdvertisementRepository.class)
 public class AdvertisementRepository implements IAdvertisementRepository 
 {
 	@PersistenceContext
 	private EntityManager em;
 	
 	@Resource 
 	private UserTransaction utx; 
 	
 	@Override
 	public Advertisement getById(int id)
 	{
 		return this.em.find(Advertisement.class, id);
 	}	
 
 	@Override
 	public void deleteAdvertisement(Advertisement a)
 	{
		Object o = em.find(a.getClass(), a.getId()); // triple wtf
		em.remove(o);
 		em.flush();
 	}	
 	
 	@Override
 	public List<Advertisement> getAll()
 	{
 		TypedQuery<Advertisement> q = this.em.createQuery("SELECT c FROM Advertisement c", Advertisement.class);
 		return q.getResultList();
 	}
 	
 	/**
 	 * Find all advertisements where a certain title is matched
 	 * @param title String
 	 * @return
 	 */
 	@Override
 	public List<Advertisement> getByTitle(String title) 
 	{
 		TypedQuery<Advertisement> q = this.em.createNamedQuery("SELECT c FROM Advertisement c WHERE c.title = " + title, Advertisement.class);
 		return q.getResultList();
 	}
 
 	@Override
 	public void AddAdvertisement(Advertisement add) {
 		//try {
 		//	utx.begin();
 			em.persist(add);
 		//	utx.commit();
 		//} catch (SecurityException | IllegalStateException
 		//		| NotSupportedException | SystemException | RollbackException
 		//		| HeuristicMixedException | HeuristicRollbackException e) {
 			// TODO Auto-generated catch block
 		//	e.printStackTrace();
 		//}
 	}
 }
