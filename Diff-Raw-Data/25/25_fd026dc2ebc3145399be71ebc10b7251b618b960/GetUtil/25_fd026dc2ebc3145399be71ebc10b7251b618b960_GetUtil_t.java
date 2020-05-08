 package org.softwaresynthesis.mytalk.server.dao.util;
 
 import java.util.List;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.softwaresynthesis.mytalk.server.IMyTalkObject;
 import org.softwaresynthesis.mytalk.server.dao.ISessionManager;
 
 /**
  * Classe base astratta per eseguire operazioni di
  * select sulla base di dati del sistema MyTalk
  * 
  * @author 	Andrea Meneghinello
  * @version	3.0
  */
public abstract class GetUtil 
 {
 	private ISessionManager manager;
 	
 	/**
 	 * Crea un istanza di GetUtil
 	 * 
 	 * @param 	manager	{@link ISessionManager} gestore delle sessioni
 	 * 					di comunicazione con la base di dati
 	 */
 	public GetUtil(ISessionManager manager)
 	{
 		this.manager = manager;
 	}
 	
 	/**
 	 * Esegue il comando sulla base di dati
 	 * 
 	 * @param 	query
 	 * @return	{@link List} restituisce il result set	
 	 */
 	@SuppressWarnings("unchecked")
 	public final List<IMyTalkObject> execute(String query)
 	{
 		List<IMyTalkObject> collection = null;
 		Query hqlQuery = null;
 		Session session = null;
 		SessionFactory factory = null;
 		Transaction transaction = null;
 		try
 		{
 			factory = this.manager.getSessionFactory();
 			session = factory.openSession();
 			hqlQuery = session.createQuery(query);
 			transaction = session.beginTransaction();
 			collection = (List<IMyTalkObject>)hqlQuery.list();
 			this.doInitialize(collection);
 			transaction.commit();
 		}
 		catch (RuntimeException ex)
 		{
 			if (transaction != null)
 			{
 				transaction.rollback();
 			}
 			collection = null;
 		}
 		finally
 		{
 			if (session != null)
 			{
 				session.flush();
 				session.close();
 			}
 		}
 		return collection;
 	}
 	
 	/**
 	 * Ha il compito di inizializzare le collezioni di
 	 * dati contenute negli oggetti del sistema MyTalk
 	 * 
 	 * @param 	collection	{@link List} collezione di oggetti
 	 * 						da inizializzare
 	 */
 	protected abstract void doInitialize(List<IMyTalkObject> collection);
 }
