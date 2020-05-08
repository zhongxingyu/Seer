 package org.softwaresynthesis.mytalk.server.dao.util;
 
 import java.util.Iterator;
 import java.util.List;
 import org.hibernate.Hibernate;
 import org.softwaresynthesis.mytalk.server.IMyTalkObject;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.dao.ISessionManager;
 
 /**
  * Permette di inizializzare le collezioni di dati
  * nell'utente
  * 
  * @author 	Andrea Meneghinello
  * @version 3.0
  */
 final class GetUserDataUtil extends GetUtil 
 {
 	/**
 	 * Crea una nuova istanza per l'inizializzazione
 	 * delle collezioni di dati nell'utente
 	 * 
 	 * @param manager 	{@link ISessionManager} gestore della
 	 * 					sessione di comunicazione con il database
 	 */
 	public GetUserDataUtil(ISessionManager manager)
 	{
 		super(manager);
 	}
 	
 	@Override
 	protected final void doInitialize(List<IMyTalkObject> collection) 
 	{
 		Iterator<IMyTalkObject> iterator = collection.iterator();
 		IUserData user = null;
 		while(iterator.hasNext())
 		{
 			user = (IUserData)iterator.next();
 			Hibernate.initialize(user.getAddressBook());
			Hibernate.initialize(user.getMessages());
			Hibernate.initialize(user.getCalls());
 		}
 	}
 }
