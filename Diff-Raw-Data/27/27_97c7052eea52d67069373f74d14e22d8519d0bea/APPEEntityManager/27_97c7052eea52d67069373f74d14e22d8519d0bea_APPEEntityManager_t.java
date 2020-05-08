 package ch.hslu.appe.fs1301.data;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import ch.hslu.appe.fs1301.data.shared.iAPPEEntityManager;
 

 /**
  * @author Thomas Bomatter
  * APPEEntityManager handles access to database
  */
 public class APPEEntityManager implements iAPPEEntityManager  {
 	
 	private EntityManagerFactory fEntityManagerFactory;
 	private EntityManager fEntityManager;
 	
 	public APPEEntityManager() {
 		fEntityManagerFactory = Persistence.createEntityManagerFactory("fbsdata");
 		fEntityManager = fEntityManagerFactory.createEntityManager();
 	}
 	
 	public<T> T getEntityObject(Class<T> entityType, int id) {
 		return fEntityManager.find(entityType, id);
 	}
 	
 	public<T> T createEntityObject(Class<T> entityType) throws InstantiationException, IllegalAccessException {
 		return entityType.newInstance();
 	}
 	
 	public void saveEntityObject(Object object) {
 		startTransaction();
 		fEntityManager.persist(object);
 		commitTransaction();
 	}
 	
 	public void deleteEntityObject(Object obj) {
 		startTransaction();
 		fEntityManager.remove(obj);
 		commitTransaction();
 	}
 	
 	public void startTransaction() {
 		fEntityManager.getTransaction().begin();
 	}
 	
 	public void commitTransaction() {
 		fEntityManager.getTransaction().commit();
 	}
 	
 	public void rollbackTransaction() {
 		fEntityManager.getTransaction().rollback();
 	}
 }
