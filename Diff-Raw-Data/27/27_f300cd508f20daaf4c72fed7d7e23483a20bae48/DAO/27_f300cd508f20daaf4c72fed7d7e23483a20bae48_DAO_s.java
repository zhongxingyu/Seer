 package com.jin.tpdb.persistence;
 
 import java.util.List;
 import javax.persistence.*;
 import javax.persistence.criteria.*;
 
 public class DAO {
 	protected static EntityManagerFactory factory;
 	protected EntityManager em;
 	
 	public DAO() {
 		if(DAO.getManagerFactory() == null) {
 		factory = Persistence.createEntityManagerFactory("jin");		
 		DAO.setManagerFactory(factory);
 		} else {
 			factory = DAO.getManagerFactory();
 		}
 	}
 	
 	protected static void setManagerFactory(EntityManagerFactory f){
         factory = f;
     }
 	
 	protected static EntityManagerFactory getManagerFactory() {
 		return factory;
 	}
 	
 	public void open() {
 		try {
 			em = factory.createEntityManager();
 			em.getTransaction().begin();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void close() {
 		try {
 			em.getTransaction().commit();
 			em.close();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void save(Object o) {
 		em.persist(o);
 	}
 	
 	public void rollback() {
 		em.getTransaction().rollback();
 	}
 	
 	public static <T> T load(Class c, int i) {
 		DAO dao = new DAO();
 		dao.open();
 		T result = dao.get(c, i);
 		dao.close();
 		return result;		
 	}
 	
 	public static <T> List<T> getList(Class c) {		
 		DAO dao = new DAO();
 		dao.open();
 		List<T> results = dao.list(c);
 		dao.close();
 		return results;
 	}	
 	
 	public <T> T get(Class c, int i) {
 		return (T)em.find(c, i);
 	}
 	
 	protected <T> List<T> list(Class entity) {		
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<T> query = cb.createQuery(entity); 
 
 		TypedQuery<T> typedQuery = em.createQuery(
 			query.select(
 				query.from(entity)
 			)
 		);
 				
 		return typedQuery.getResultList();
 	}
 }
 
