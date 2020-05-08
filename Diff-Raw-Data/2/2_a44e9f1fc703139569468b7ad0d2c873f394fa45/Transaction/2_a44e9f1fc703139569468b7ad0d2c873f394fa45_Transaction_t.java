 package lets.code.better.todo.util;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 
 public class Transaction {
 
 	private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory("default");
 	private static final ThreadLocal<EntityManager> ENTITY_MANAGER = new ThreadLocal<EntityManager>();
 
 	public static void begin(){
 		ENTITY_MANAGER.set(factory.createEntityManager());
 		ENTITY_MANAGER.get().getTransaction().begin();
 	}
 	
 	public static void commit() {
 		ENTITY_MANAGER.get().getTransaction().commit();
 		ENTITY_MANAGER.get().close();
 	}
 
 	public static void rollbackIfActive() {
 		EntityTransaction transaction = ENTITY_MANAGER.get().getTransaction();
		if (transaction != null && transaction.isActive()) {
 			transaction.rollback();
 			ENTITY_MANAGER.get().close();
 		}
 	}
 	
 	public static EntityManager entityManager(){
 		return ENTITY_MANAGER.get();
 	}
 }
