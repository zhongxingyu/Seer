 package org.softwaresynthesis.mytalk.server.dao;
 
 import java.util.List;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 
 /**
  * Crea un punto di accesso verso il database per
  * la gestione degli utenti
  * 
  * @author 	Andrea Meneghinello
  * @version	%I%, %G%
  */
 public class UserDataDAO 
 {
 	/**
 	 * Elimina uno {@link IUserData} dal database del
 	 * sistema mytalk
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @param 	user	{@link IUserData} che deve essere
 	 * 					eliminato
 	 * @return	true se l'operazione di cancellazione è andata
 	 * 			a buon fine, false altrimenti
 	 */
 	public boolean delete(IUserData user)
 	{
 		boolean flag = false;
 		HibernateUtil util = null;
 		Session session = null;
 		SessionFactory factory = null;
 		Transaction transaction = null;
 		try
 		{
 			util = HibernateUtil.getInstance();
 			factory = util.getSessionFactory();
 			session = factory.openSession();
 			transaction = session.beginTransaction();
 			session.delete(user);
 			transaction.commit();
 			flag = !flag;
 		}
 		catch (RuntimeException ex)
 		{
 			if(transaction != null)
 			{
 				transaction.rollback();
 			}
 		}
 		finally
 		{
 			session.flush();
 			session.close();
 		}
 		return flag;
 	}
 	
 	/**
 	 * Aggiorna uno {@link IUserData} nel database del
 	 * sistema mytalk
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @param 	user	{@link IUserData} da aggiornare
 	 * @return	true se l'operazione di aggiornamento è
 	 * 			andata a buon fine, false altrimenti
 	 */
 	public boolean update(IUserData user)
 	{
 		boolean flag = false;
 		HibernateUtil util = null;
 		Session session = null;
 		SessionFactory factory = null;
 		Transaction transaction = null;
 		try
 		{
 			util = HibernateUtil.getInstance();
 			factory = util.getSessionFactory();
 			session = factory.openSession();
 			transaction = session.beginTransaction();
 			session.update(user);
 			transaction.commit();
 			flag = !flag;
 		}
 		catch (RuntimeException ex)
 		{
 			if(transaction != null)
 			{
 				transaction.rollback();
 			}
 		}
 		finally
 		{
 			session.flush();
 			session.close();
 		}
 		return flag;
 	}
 	
 	/**
 	 * Inserisce uno {@link IUserData} nel database del
 	 * sistema del sistema mytalk
 	 * 
 	 * @author 	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @param 	user	{@link IUserData} da inserire
 	 * @return	true se l'operazione di inserimento è
 	 * 			andata a buon fine, false altrimenti
 	 */
 	public boolean insert(IUserData user)
 	{
 		boolean flag = false;
 		HibernateUtil util = null;
 		Session session = null;
 		SessionFactory factory = null;
 		Transaction transaction = null;
 		try
 		{
 			util = HibernateUtil.getInstance();
 			factory = util.getSessionFactory();
 			session = factory.openSession();
 			transaction = session.beginTransaction();
 			session.save(user);
 			transaction.commit();
 			flag = !flag;
 		}
 		catch (RuntimeException ex)
 		{
 			if(transaction != null)
 			{
 				transaction.rollback();
 			}
 		}
 		finally
 		{
 			session.flush();
 			session.close();
 		}
 		return flag;
 	}
 	
 	/**
 	 * Restituisce lo {@link IUserData} a cui corrisponde
 	 * l'e-mail data in input
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @param 	mail	stringa rappresentante l'indirizzo
 	 * 					e-mail dell'utente da ricercare
 	 * @return	{@link IUserData} se esiste un utente con l'e-mail
 	 * 			ricevuta in input altrimenti null
 	 */
 	@SuppressWarnings("rawtypes")
 	public IUserData getByEmail(String mail)
 	{
 		HibernateUtil util = null;
 		List user = null;
 		Session session = null;
 		SessionFactory factory = null;
 		String hqlQuery = "from UserData as u where = u.mail = :mail";
 		Transaction transaction = null;
 		try
 		{
 			util = HibernateUtil.getInstance();
 			factory = util.getSessionFactory();
 			session = factory.openSession();
 			transaction = session.beginTransaction();
 			Query query = session.createQuery(hqlQuery);
 			query.setString("mail", mail);
 			user = query.list();
			transaction.commit();
 		}
 		catch (RuntimeException ex)
 		{
 			if(transaction != null)
 			{
 				transaction.rollback();
 			}
 		}
 		finally
 		{
 			session.flush();
 			session.close();
 		}
 		if(user != null && user.size() == 1)
 		{
 			return (IUserData)user.get(0);
 		}
 		else
 		{
 			return null;
 		}
 	}
 }
