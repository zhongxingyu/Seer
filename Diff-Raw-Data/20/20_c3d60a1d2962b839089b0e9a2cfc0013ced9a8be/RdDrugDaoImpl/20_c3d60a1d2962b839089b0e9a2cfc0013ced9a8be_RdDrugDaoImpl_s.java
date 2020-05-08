 package rdproject.daoImpl;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.springframework.stereotype.Repository;
 
 import rdproject.dao.RdDrugDao;
 import rdproject.model.Drug;
 
 @Repository
 public class RdDrugDaoImpl implements RdDrugDao
 {
 	private EntityManager entityManager;
 	
 	/**
 	 * Injects the entity manager into each Dao
 	 * @param entityManager
 	 */
 	@PersistenceContext
     public void setEntityManager(EntityManager entityManager)
     {
     	this.entityManager = entityManager;
     }
     
 	/**
 	 * if the Drugs id is not preset
 	 *   add the new drug
 	 *   else update the drug
 	 */
 	@Override
 	public void saveDrug(Drug aDrug) 
 	{
 		if (aDrug.getId() !=  null)
 		{
 			entityManager.persist(aDrug);
 		}
 		else
 		{
 			entityManager.merge(aDrug);
 		}
 	}
 
 	/**
 	 * removes the aDrug from database
 	 */
 	@Override
 	public void deleteDrug(Drug aDrug) 
 	{
 		entityManager.remove(aDrug);
 	}
 
 	/**
 	 * Search Database for drug with aDrug's id
 	 *   Return Drug object.
 	 */
 	@Override
 	public Drug searchDrug(Drug aDrug) 
 	{
 		String hql = "Select d from Drug d where d.id = : aDrugId";
 		Query query = entityManager.createQuery(hql);
 		query.setParameter("aDrugId", aDrug.getId());
 		return (Drug)query.getSingleResult();	
 	}
 
 	@Override
 	public Double calcDrugDose(Drug aDrug) 
 	{
 		// Still unsure of the calculations
 		return null;
 	}
 
 }
