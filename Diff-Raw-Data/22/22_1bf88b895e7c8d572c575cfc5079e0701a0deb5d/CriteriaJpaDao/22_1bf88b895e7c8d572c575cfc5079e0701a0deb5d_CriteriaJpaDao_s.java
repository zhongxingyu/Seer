 package jacs.database.exceed.jpa;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import jacs.database.exceed.dao.CriteriaDAO;
 import jacs.database.exceed.model.Criteria;
 
 public class CriteriaJpaDao implements CriteriaDAO {
 
 private static final String PERSISTENCE_UNIT = "exceedvote";
 	
 	private static EntityManagerFactory emf;
 	private static EntityManager em;
 	static {
 		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
 		em = emf.createEntityManager();
 	}
 	
 	@Override
 	public Criteria findCriteriaByName(String name) {
 		// TODO Auto-generated method stub
 		Query q = em.createQuery("SELECT c FROM Criteria c WHERE c.name LIKE :name");
 		q.setParameter("name","%"+name+"%");
 		List<Criteria> result = q.getResultList();
		System.out.printf("found %d matches\n",result.size());
 		
 		if(!(result.size()==0)) return result.get(0);
 		else return null;
 	}
 	@Override
 	public boolean checkMatchCriteria(Criteria criteria) {
 		// TODO Auto-generated method stub
 		if(criteria == null)	return false;
 		return true;
 	}
 	@Override
 	public String saveCriteria(String name) {
 		// TODO Auto-generated method stub
 		EntityTransaction tx = em.getTransaction();
 		try {
 			if(!checkMatchCriteria(findCriteriaByName(name)))	{
 				tx.begin();
 				Criteria c = new Criteria(name);
 				em.persist(c);
 				tx.commit();
 				return "ADD_CRITERIA_SUCCESSED : "+c.getName();
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 			System.err.println("Exception occurred!");
 			if(tx.isActive()) tx.rollback();
 		}
 		return "ADD_CRITERIA_FAILED";
 	}
 	@Override
 	public String deleteCriteria(String name) {
 		// TODO Auto-generated method stub
 		EntityTransaction tx = em.getTransaction();
 		try	{
 			if(checkMatchCriteria(findCriteriaByName(name)))	{
 				Criteria c = findCriteriaByName(name);
 				tx.begin();
 				em.remove(c);
 				tx.commit();
 				System.out.printf("Remove. id = %d : Criteria = %s\n", c.getId(),c.getName());
 				return "REMOVE_SUCCESS";
 			}
 		}catch(Exception ex)	{
 			System.err.println("Exception occurred!");
 			if(tx.isActive()) tx.rollback();
 		}
 		return "REMOVE_FAILED";
 	}
 	@Override
 	public List<Criteria> findAllCriteria() {
 		
 		Query q = em.createQuery("SELECT c FROM Criteria c");
 		List<Criteria> result = q.getResultList();
 		System.out.printf("found %d matches\n",result.size());
 		return result;
 	}
 	@Override
 	public Criteria findCriteriaByID(int id) {
 		// TODO Auto-generated method stub
 		Query q = em.createQuery("SELECT c FROM Criteria c WHERE c.id =:id");
 		q.setParameter("id",id);
 		List<Criteria> result = q.getResultList();
		System.out.printf("found %d matches\n",result.size());	
 		if(!(result.size()==0)) return result.get(0);
 		else return null;
 	}
 	
 
 }
