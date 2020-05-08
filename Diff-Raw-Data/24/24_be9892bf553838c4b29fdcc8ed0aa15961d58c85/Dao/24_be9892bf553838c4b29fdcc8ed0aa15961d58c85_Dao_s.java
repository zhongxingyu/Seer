 package dao;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 
 import util.JpaUtil;
 import model.Unit;
 
 public class Dao {
     private static void close(EntityManager em) {
         if (em != null) em.close();
     }
     public static Unit findById(Long id) {
 		EntityManager em = null;
         try {
             em = JpaUtil.getFactory().createEntityManager();
             return em.find(Unit.class, id);
         } finally {
             close(em);
         }
     }
     public static Unit findByCode(String code) {
 		EntityManager em = null;
         try {
             em = JpaUtil.getFactory().createEntityManager();
            Unit unit = em.createQuery("select p from Unit p where p.code=:code",
                     Unit.class).getSingleResult();
             return unit;
         } finally {
             close(em);
         }
     }
     public static List<Unit> findByChildId(Long id) {
     	EntityManager em = null;
 		try {
 			em = JpaUtil.getFactory().createEntityManager();
 			List<Unit> query = em.createQuery("select p from Unit p where p.super_unit_id = "+id, Unit.class).getResultList();
 			return query;
 	    } finally {
 	    	close(em);
 	    }
     }
 
     public static List<Unit> findAllUnits() {
         EntityManager em = null;
         try {
             em = JpaUtil.getFactory().createEntityManager();
 
             TypedQuery<Unit> query = em.createQuery(
                     "select p from Unit p", Unit.class);
 
             return query.getResultList();
 
         } finally {
         	close(em);
         }
     }
     public static List<Unit> search(String s) {
     	EntityManager em = null;
 		try {
 			em = JpaUtil.getFactory().createEntityManager();
 			TypedQuery<Unit> query = em.createQuery("select p from Unit p"
 					+ " WHERE lower(name) LIKE '%" + s.toLowerCase() + "%'", Unit.class);
 			return query.getResultList();
 	    } finally {
 	    	close(em);
 	    }
     }
     public static void deleteAll() {
         EntityManager em = null;
         try {
         	em = JpaUtil.getFactory().createEntityManager();
         	em.getTransaction().begin();
         	Query query = em.createQuery("delete from Unit where 1=1");
             query.executeUpdate();
 
             em.getTransaction().commit();
         } finally {
         	close(em);
         }
 	}
 	
 	public static void deleteUnit(Long id) {
         EntityManager em = null;
         try {
         	em = JpaUtil.getFactory().createEntityManager();
         	em.getTransaction().begin();
         	Unit unit = em.find(Unit.class, id);
             if (unit != null) em.remove(unit);
 
             em.getTransaction().commit();
         } finally {
         	close(em);
         }
 	}
 
 
     public static void addUnit(Unit unit) {
     	EntityManager em = null;
     	try {
             em = JpaUtil.getFactory().createEntityManager();
             em.getTransaction().begin();
             if (unit.getId() == null) {
                 em.persist(unit);
             } else {
                 em.merge(unit);
             }
 
             em.getTransaction().commit();
         } finally {
         	close(em);
         }
     }
 
 	public static void viewUnit(Unit unit) {
 
 	}
 }
