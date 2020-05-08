 package edu.caltech.cs141b.hw2.gwt.collab.server;
 
 import java.util.Date;
 import java.util.Random;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Transaction;
 
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.caltech.cs141b.hw2.gwt.collab.client.TokenService;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class TokenServiceImpl extends RemoteServiceServlet implements
 		TokenService {
	private static final int LOGIN_STRING_LEN = 16;
 	
 	@Override
 	public String login() {
 		Random r = new Random();
 		String randStr = "";
 		
 		for (int i = 0; i < LOGIN_STRING_LEN; i++) {
 			randStr += (char) (r.nextInt(95) + 32);
 		}
 		
		System.out.println(randStr);
		return getChannelService().createChannel(randStr);
 		
 	}
 	
 	private ChannelService getChannelService() {
 		return ChannelServiceFactory.getChannelService();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#releaseLock
 	 * (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
 	 */
 	@Override
 	public void releaseLock(LockedDocument doc) throws LockExpired {
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Transaction t = pm.currentTransaction();
 		try {
 			// Starting transaction...
 			t.begin();
 
 			// Get the doc's key
 			Key key = KeyFactory.stringToKey(doc.getKey());
 
 			// Use the key to retrieve the doc
 			Document toSave = pm.getObjectById(Document.class, key);
 
 			// Get the lock information - saveDocument can only be called if
 			// there is a lock or if it's a new document
 			String lockedBy = toSave.getLockedBy();
 
 			// Get the IP Address
 			String identity = getThreadLocalRequest().getRemoteAddr();
 
 			// Make sure that the person unlocking is the person who locked the
 			// doc.
 			if (lockedBy.equals(identity)) {
 				// Unlock it
 				toSave.unlock();
 
 				// And store it in the Datastore
 				pm.makePersistent(toSave);
 			} else {
 				// Otherwise, throw an exception
 				throw new LockExpired("You no longer have the lock");
 			}
 
 			// ...Ending transaction
 			t.commit();
 		} finally {
 			// Do some cleanup
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#lockDocument
 	 * (java.lang.String)
 	 */
 	@Override
 	public LockedDocument lockDocument(String documentKey)
 			throws LockUnavailable {
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Transaction t = pm.currentTransaction();
 		try {
 			// Starting transaction...
 			t.begin();
 
 			// Create the key
 			Key key = KeyFactory.stringToKey(documentKey);
 
 			// Get the document from the Datastore
 			Document toSave = pm.getObjectById(Document.class, key);
 
 			// If the doc is locked...
 			if (toSave.isLocked()) {
 				// But it should be unlocked...
 				// System.out.println(toSave.getLockedUntil());
 				if (toSave.getLockedUntil().before(
 						new Date(System.currentTimeMillis()))) {
 					// Unlock it
 					toSave.unlock();
 				} else {
 					// Otherwise notify the client that it lock is unavailable
 					throw new LockUnavailable("Document is locked for "
 							+ (toSave.getLockedUntil().getTime() - System
 									.currentTimeMillis()) / 1000L
 							+ " more seconds.");
 				}
 			}
 
 			// At this point, the doc should be unlocked
 			// Get the IP Address of the user
 			String identity = getThreadLocalRequest().getRemoteAddr();
 
 			// Lock the document for 30 seconds
 			toSave
 					.lock(new Date(System.currentTimeMillis() + 30000L),
 							identity);
 			// Write this to the Datastore
 			pm.makePersistent(toSave);
 
 			// ...Ending transaction
 			t.commit();
 
 			// Return the locked document
 			return toSave.getLockedDoc();
 		} finally {
 			// Do some cleanup
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 		}
 	}
 }
