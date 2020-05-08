 package ch.hslu.appe.fs1301.data.shared;
 
import java.util.List;
 
 /**
  * @author Thomas Bomatter
  * iAPPEEntityManager defines interface for database access
  */
 public interface iAPPEEntityManager {
 	
 	public<T> T getEntityObject(Class<T> entityType, int id);
 	
 	public<T> T createEntityObject(Class<T> entityType) throws InstantiationException, IllegalAccessException;
 	
 	public void saveEntityObject(Object object);
 	
 	public void deleteEntityObject(Object obj);
 	
 	public void startTransaction();
 	
 	public void commitTransaction();
 	
 	public void rollbackTransaction();
	
	public<T> List<T> executeQuery(Class<T> entityType, String query);
 }
