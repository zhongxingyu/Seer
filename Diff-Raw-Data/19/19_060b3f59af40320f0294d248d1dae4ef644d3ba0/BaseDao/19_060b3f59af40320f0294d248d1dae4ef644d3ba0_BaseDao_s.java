 package com.myboard.dao;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.exception.ConstraintViolationException;
 
 import com.myboard.exception.EntityAlreadyExistsException;
 import com.myboard.exception.EntityNotFoundException;
 
 public class BaseDao {
 
 	private static final Log log = LogFactory.getLog(BaseDao.class);
 	private final SessionFactory sessionFactory = initSessionFactory();
 	
 	@SuppressWarnings("deprecation")
 	private SessionFactory initSessionFactory() {
 		try {
 			return new Configuration().configure().buildSessionFactory();
 		} catch (Exception e) {
 			log.error("Could not locate SessionFactory in JNDI", e);
 			e.printStackTrace();
 			throw new IllegalStateException(
 					"Could not locate SessionFactory in JNDI ");
 		}
 	}
 	
 	public void create(Object transientInstance) throws EntityAlreadyExistsException{
 		Session session = null;
 		Transaction transaction = null;
 		
 		try {
 			session = this.sessionFactory.getCurrentSession();
 			transaction = session.beginTransaction();
 			session.persist(transientInstance);
 			transaction.commit();
 		} catch(ConstraintViolationException ex){
 			if(transaction != null){transaction.rollback();}
 			throw new EntityAlreadyExistsException("Entity already exists in database for this key.");
 		} catch (RuntimeException re) {
 			if(transaction != null){transaction.rollback();}
 			throw re;
 		}finally{
 			if(session != null && session.isOpen()){session.close();}
 		}
 	}
 	
 	public Object read(String entityClassFullName, String id) throws EntityNotFoundException{		
 		Session session = null;
 		Transaction transaction = null;
 		
 		try {
 			session = this.sessionFactory.getCurrentSession();
 			transaction = session.beginTransaction();
 			Object instance = session.get(entityClassFullName, /*id);*/Integer.parseInt(id));
 			transaction.commit();
 			
 			if(instance == null){
 				throw new EntityNotFoundException(entityClassFullName + " not found for id = " + id);
 			}
 			
 			return instance;
 		} catch (RuntimeException re) {
 			if(transaction != null){transaction.rollback();}
 			throw re;
 		}finally{
 			if(session != null && session.isOpen()){session.close();}
 		}
 	}
 	
 	public Object read(String entityClassFullName, int id) throws EntityNotFoundException{		
 		Session session = null;
 		Transaction transaction = null;
 		
 		try {
 			session = this.sessionFactory.getCurrentSession();
 			transaction = session.beginTransaction();
 			Object instance = session.get(entityClassFullName, id);
 			transaction.commit();
 			
 			if(instance == null){
				throw new EntityNotFoundException(entityClassFullName + " not foe + " not found for id = " + id);
 			}
 			
 			return instance;
 		} catch (RuntimeException re) {
 			if(transaction != null){transaction.rollback();}
 			throw re;
 		}finally{
 			if(session != null && session.isOpen()){session.close();}
 		}
 	}
 
 	public Object read(String entityClassFullName, Integer id) throws EntityNotFoundException{		
 		Session session = null;
 		Transaction transaction = null;
 		
 		try {
 			session = this.sessionFactory.getCurrentSession();
 			transaction = session.beginTransaction();
 			Object instance = session.get(entityClassFullName, id);
 			transaction.commit();
 			
 			if(instance == null){
 				throw new EntityNotFoundException(entityClassFullName + " not found for id = " + id);
 			}
 			
 			return instance;
 		} catch (RuntimeException re) {
 			if(transaction != null){transaction.rollback();}
 			throw re;
 		}finally{
 			if(session != null && session.isOpen()){session.close();}
 		}
 	}
 	public List<?> readAll(String entityClassFullName, Object o) throws EntityNotFoundException{
 		Session session = null;
 		Transaction transaction = null;
 		
 		try {
 			session = this.sessionFactory.getCurrentSession();
 			transaction = session.beginTransaction();
 			List<?> instance = session.createCriteria(entityClassFullName)
 							.add(org.hibernate.criterion.Example.create(o)).list();
 			transaction.commit();
 			
 			if(instance == null){
 				throw new EntityNotFoundException(entityClassFullName + " not found");
 			}
 			
 			return instance;
 		} catch (RuntimeException re) {
 			if(transaction != null){transaction.rollback();}
 			throw re;
 		}finally{
 			if(session != null && session.isOpen()){session.close();}
 		}
 	}
 	
 	public void update(Object instance) {
 		Session session = null;
 		Transaction transaction = null;
 		
 		try {
 			session = this.sessionFactory.getCurrentSession();
 			transaction = session.beginTransaction();
 			session.saveOrUpdate(instance);
 			transaction.commit();
 		} catch (RuntimeException re) {
 			if(transaction != null){transaction.rollback();}
 			throw re;
 		}finally {
 			if(session != null && session.isOpen()){session.close();}
 		}
 	}
 	
 	public void delete(Object persistentInstance) {
 		Session session = null;
 		Transaction transaction = null;
 		
 		try {
 			session = this.sessionFactory.getCurrentSession();
 			transaction = session.beginTransaction();
 			session.delete(persistentInstance);
 			transaction.commit();
 		} catch (RuntimeException re) {
 			if(transaction != null){transaction.rollback();}
 			throw re;
 		}finally{
 			if(session != null && session.isOpen()){session.close();}
 		}
 	}
 	
 }
