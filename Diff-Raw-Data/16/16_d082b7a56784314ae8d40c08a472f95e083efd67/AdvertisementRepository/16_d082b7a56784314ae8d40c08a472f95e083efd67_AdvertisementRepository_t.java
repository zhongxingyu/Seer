 package nl.zuyd.marktplaats_revised.repositories;
 
 import java.util.List;
 
 import javax.ejb.Local;
 import javax.ejb.Singleton;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import nl.zuyd.marktplaats_revised.entities.Advertisement;
 
 @Singleton
 @Local(IAdvertisementRepository.class)
 public class AdvertisementRepository implements IAdvertisementRepository 
 {
 	@PersistenceContext
 	private EntityManager em;
 	
 	//@Resource 
 	//private UserTransaction utx; 
 	
 	@Override
 	public Advertisement getById(int id)
 	{
 		return this.em.find(Advertisement.class, id);
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
 	public void addAdvertisement(Advertisement add) {
 		//try {
 		//	utx.begin();
 			em.persist(add);
 			this.em.flush();
 			//utx.commit();
 		//} catch (SecurityException | IllegalStateException
 		//		| NotSupportedException | SystemException | RollbackException
 		//		| HeuristicMixedException | HeuristicRollbackException e) {
 		//	e.printStackTrace();
 		//}
 	}
 
 	@Override
 	public void deleteAdvertisement(Advertisement advert)
 	{
		this.em.merge(advert);
 		this.em.remove(advert);
 		this.em.flush();
 	}
 
 	@Override
 	public void saveAdvertisement(Advertisement advert)
 	{
 		this.em.flush();		
 	}
 }
