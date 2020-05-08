 package org.softwaresynthesis.mytalk.server.dao.util;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.softwaresynthesis.mytalk.server.IMyTalkObject;
 import org.softwaresynthesis.mytalk.server.dao.ISessionManager;
 
 /**
  * Classe base astratta per le operazione di Insert,
  * Delete, Update di un oggetto sulla base di dati
  * del sistema MyTalk
  * 
  * @author 	Andrea Meneghinello
  * @version	3.0
  */
abstract class ModifyUtil 
 {
 	private ISessionManager manager;
 	
 	/**
 	 * Crea un istanza di ModifyUtil
 	 * 
 	 * @param 	manager	{@link ISessionManager} gestore delle sessioni
 	 * 					di comunicazione con la base di dati
 	 */
 	public ModifyUtil(ISessionManager manager)
 	{
 		this.manager = manager;
 	}
 	
 	/**
 	 * Esegue il comando sulla base di dati
 	 * 
 	 * @param 	object	{@link IMyTalkObject} oggetto soggetto del comando
 	 * @return	true se l'operazione è andata a buon fine, false altrimenti
 	 */
 	public final boolean execute(IMyTalkObject object)
 	{
 		boolean result = false;
 		Session session = null;
 		SessionFactory factory = null;
 		Transaction transaction = null;
 		try
 		{
 			factory = this.manager.getSessionFactory();
 			session = factory.openSession();
 			transaction = session.beginTransaction();
 			this.doAction(session, object);
 			transaction.commit();
 			result = true;
 		}
 		catch (RuntimeException ex)
 		{
 			if (transaction != null)
 			{
 				transaction.rollback();
 			}
 			result = false;
 		}
 		finally
 		{
 			if (session != null)
 			{
 				session.flush();
 				session.close();
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Operazione specifica sulla base di dati
 	 * @param 	object	{@link IMyTalkObject} oggetto soggetto del comando
 	 */
 	protected abstract void doAction(Session session, IMyTalkObject object);
 }
