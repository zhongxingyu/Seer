 package org.softwaresynthesis.mytalk.server.dao.util;
 
 import java.util.Iterator;
 import java.util.List;
 import org.hibernate.Hibernate;
 import org.softwaresynthesis.mytalk.server.IMyTalkObject;
 import org.softwaresynthesis.mytalk.server.call.ICall;
 import org.softwaresynthesis.mytalk.server.dao.ISessionManager;
 
 /**
  * Permette di inizializzare le collezioni di dati
  * nella chiamata
  * 
  * @author 	Andrea Meneghinello
  * @version 3.0
  */
 final class GetCallUtil extends GetUtil 
 {
 	/**
 	 * Crea una nuova istanza per l'inizializzazione
 	 * delle lista degli utenti coinvolti nella chiamata
 	 * 
 	 * @param manager 	{@link ISessionManager} gestore della
 	 * 					sessione di comunicazione con il database
 	 */
 	public GetCallUtil(ISessionManager manager)
 	{
 		super(manager);
 	}
 	
 	@Override
 	protected final void doInitialize(List<IMyTalkObject> collection) 
 	{
 		ICall call = null;
 		Iterator<IMyTalkObject> iterator = collection.iterator();
 		while(iterator.hasNext())
 		{
 			call = (ICall)iterator.next();
			Hibernate.initialize(call.getCallList());
 		}
 	}
 }
