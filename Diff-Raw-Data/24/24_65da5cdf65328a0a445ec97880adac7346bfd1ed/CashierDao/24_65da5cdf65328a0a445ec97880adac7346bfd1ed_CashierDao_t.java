 package system.dao;
 
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import system.Cashier;
 
 public class CashierDao
 {
 	public void delete(Cashier c) 
 	{
 		Session session = null;
         Transaction tx = null;
         
         try 
         {
             session = SessionFactorySingleton.getSessionFactory().openSession();
             tx = session.beginTransaction();
             
             session.delete(c);
 
             tx.commit(); 
         }
         catch(HibernateException ex) 
         {
             ex.printStackTrace();
             tx.rollback();
         } 
         finally 
         {
             session.close();
         }
 	}
 
 	public void delete(int id) 
 	{
 		Session session = null;
         Transaction tx = null;
         
         try 
         {
             session = SessionFactorySingleton.getSessionFactory().openSession();
             tx = session.beginTransaction();
             
             Cashier c = (Cashier) session.get(Cashier.class, id);
             session.delete(c);
 
             tx.commit(); 
         }
         catch(HibernateException ex) 
         {
             ex.printStackTrace();
             tx.rollback();
         } 
         finally 
         {
             session.close();
         }
 	}
 
 	public void save(Cashier c) 
 	{
 		Session session = null;
         Transaction tx = null;
         
         try 
         {
             session = SessionFactorySingleton.getSessionFactory().openSession();
             tx = session.beginTransaction();
             
             session.saveOrUpdate(c);
 
             tx.commit(); 
         }
         catch(HibernateException ex) 
         {
             ex.printStackTrace();
             tx.rollback();
         } 
         finally 
         {
             session.close();
         }
 	}
 	
 	public Cashier get(int index)
 	{
 		Session session = null;
         try 
         {
             session = SessionFactorySingleton.getSessionFactory().openSession();
             return (Cashier) session.get(Cashier.class, index);
 
         }
         finally 
         {
             session.close();
         }
 	}
 	/*
 	public Cashier get(int index, Store store){
 		Session session = null;
         try 
         {
             session = SessionFactorySingleton.getSessionFactory().openSession();
             Query q = session.createQuery("from Cashier cas where index = :ind And cas.store = :sto");
             q.setParameter("ind", index);
             q.setParameter("sto", store);
             return (Cashier) q.uniqueResult();
 
         }
         finally 
         {
             session.close();
         }
 	}
 	*/
 	@SuppressWarnings("unchecked")
 	public List<Cashier> getCashiers()
     {
         Session session = null;
         
         try 
         {
             session = SessionFactorySingleton.getSessionFactory().openSession();
             Query q = session.createQuery("from Cashier");
             List<Cashier> results = q.list();            
             return results;
         }
         finally 
         {
             session.close();
         }
     }
 }
