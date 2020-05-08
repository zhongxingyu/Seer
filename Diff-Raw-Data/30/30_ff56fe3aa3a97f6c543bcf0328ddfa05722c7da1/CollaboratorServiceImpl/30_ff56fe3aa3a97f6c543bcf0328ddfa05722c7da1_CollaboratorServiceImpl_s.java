 package edu.caltech.cs141b.hw2.gwt.collab.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import com.google.appengine.api.channel.ChannelMessage;
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class CollaboratorServiceImpl extends RemoteServiceServlet implements
 		CollaboratorService {
 
 	// This is the time in seconds that clients can lock a document
 	private static final int LOCK_TIME = 30;
 
 	// This object maps doc keys to queues of client IDs, which are the clients
 	// waiting for the document.
 	private static Map<String, List<String>> queueMap;
 
 	// This will map document keys to who owns the document. Either "server" or
 	// the clientID.
 	private static Map<String, String> tokenMap;
 
 	// Contains the currently-locked documents.
 	private static ArrayList<String> lockedDocuments = new ArrayList<String>();
 
 	// This is a bit of a hack that takes advantage of the fact all the fields
 	// are static. We need this static field to allow ClearLockServlet to access
 	// some of the non static functions of this class.
 	private static CollaboratorServiceImpl server = new CollaboratorServiceImpl();
 
 	/**
 	 * The constructor for this server. We are not sure how GAE splits up work
 	 * on its servers, so we made sure all the maps were thread safe. Also, the
 	 * cron job might lead to concurrent modification.
 	 */
 	public CollaboratorServiceImpl() {
 		queueMap = Collections
 				.synchronizedMap(new HashMap<String, List<String>>());
 		tokenMap = Collections.synchronizedMap(new HashMap<String, String>());
 	}
 
 	/**
 	 * Cleans lock for an individual document
 	 * 
 	 * @param docKey
 	 *            The dockey for the doc we want to clean the locks of.
 	 */
 	public void cleanLock(String docKey) {
 		int notInList = -1;
 		int docIndex = lockedDocuments.indexOf(docKey);
 
 		if (docIndex != notInList) {
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 
 			try {
 				// Create the key
 				Key key = KeyFactory.stringToKey(docKey);
 
 				// Get the document from the Datastore
 				Document doc = pm.getObjectById(Document.class, key);
 
 				// If the doc is locked and the lock expired
 				if (doc.isLocked()
 						&& doc.getLockedUntil().before(
 								new Date(System.currentTimeMillis()))) {
 					String previousClient = tokenMap.get(docKey);
 					server.receiveToken(previousClient, docKey);
 				}
 
 			} finally {
 				// Do some cleanup
 				pm.close();
 			}
 		}
 	}
 
 	/**
 	 * Cleans locks for all currently locked documents
 	 */
 	public static void cleanLocks() {
 		// Clean up documents if there are document currently locked
 		if (!lockedDocuments.isEmpty()) {
 			ArrayList<String> toClear = new ArrayList<String>();
 
 			for (String docKey : lockedDocuments) {
 				PersistenceManager pm = PMF.get().getPersistenceManager();
 				Transaction t = pm.currentTransaction();
 				try {
 					// Starting transaction...
 					t.begin();
 
 					// Create the key
 					Key key = KeyFactory.stringToKey(docKey);
 
 					// Get the document from the Datastore
 					Document doc = pm.getObjectById(Document.class, key);
 
 					// If the doc is locked and the lock expired
 					if (doc.isLocked()
 							&& doc.getLockedUntil().before(
 									new Date(System.currentTimeMillis()))) {
 						toClear.add(docKey);
 						if (queueMap.containsKey(docKey)) {
 							toClear.add(docKey);
 
 						}
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
 
 			// Check if there are clients waiting for the document
 			for (String docKey : toClear) {
 				String previousClient = tokenMap.get(docKey);
				System.out.println("Clearing for " + docKey + " and client = " + previousClient);
 				server.receiveToken(previousClient, docKey);
 			}
 		}
 
 	}
 
 	/**
 	 * Get the list of the documents in the datastore.
 	 * 
 	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocumentList()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<DocumentMetadata> getDocumentList() {
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		// Make the Document query
 		Query query = pm.newQuery(Document.class);
 
 		ArrayList<DocumentMetadata> docList = new ArrayList<DocumentMetadata>();
 
 		try {
 			// Query the Datastore and iterate through all the Documents in the
 			// Datastore
 			for (Document doc : (List<Document>) query.execute()) {
 				// Get the document metadata from the Documents
 				DocumentMetadata metaDoc = new DocumentMetadata(doc.getKey(),
 						doc.getTitle());
 				docList.add(metaDoc);
 			}
 
 			// Return the list of docs
 			return docList;
 		} finally {
 			// Do cleanup
 			query.closeAll();
 			pm.close();
 		}
 	}
 
 	/**
 	 * Retrieves the given doc from the datastore.
 	 * 
 	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocument
 	 *      (java.lang.String)
 	 */
 	@Override
 	public UnlockedDocument getDocument(String documentKey) {
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		try {
 			// Create the key for this document
 			Key key = KeyFactory.stringToKey(documentKey);
 			// Use the key to retrieve the document
 			Document doc = pm.getObjectById(Document.class, key);
 
 			// Return the unlocked document
 			return doc.getUnlockedDoc();
 		} finally {
 			// Do cleanup
 			pm.close();
 		}
 	}
 
 	/**
 	 * Saves the given document in the datastore.
 	 * 
 	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#saveDocument
 	 *      (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
 	 */
 	@Override
 	public UnlockedDocument saveDocument(String clientID, LockedDocument doc)
 			throws LockExpired {
 		System.out.println(clientID + " is saving");
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Document toSave;
 		// Get the document's key
 		String stringKey = doc.getKey();
 
 		Transaction t = pm.currentTransaction();
 		try {
 			// If the doc has no key, it's a new document, so create a new
 			// document
 
 			// Starting transaction...
 			t.begin();
 
 			if (stringKey == null) {
 				// First unlock it
 				toSave = new Document(doc.unlock());
 			} else {
 
 				// Create the key
 				Key key = KeyFactory.stringToKey(stringKey);
 
 				// Get the document corresponding to the key
 				toSave = pm.getObjectById(Document.class, key);
 
 				// Get the lock information - saveDocument can only be called if
 				// there is a lock or if it's a new document
 				String lockedBy = toSave.getLockedBy();
 				Date lockedUntil = toSave.getLockedUntil();
 
 				// Get the client's ID
 				String identity = clientID;
 
 				// Check that the person trying to save has the lock and that
 				// the lock hasn't expired
 				if (lockedBy.equals(identity)
 						&& lockedUntil.after(new Date(System
 								.currentTimeMillis()))) {
 					// If both are fulfilled, update and unlock the doc
 					toSave.update(doc);
 					toSave.unlock();
 
 				} else {
 					// Otherwise, throw an exception
 					throw new LockExpired();
 				}
 			}
 
 			// Now write the results to Datastore
 			pm.makePersistent(toSave);
 
 			// ...Ending transaction
 			t.commit();
 
 			// Return the unlocked document
 			return toSave.getUnlockedDoc();
 		} finally {
 			// Do some cleanup
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 
 			// Now, take the token back
 			receiveToken(clientID, stringKey);
 		}
 	}
 
 	/**
 	 * Called whenever a client returns a token for an item. This function will
 	 * update the token map and then send a token if possible.
 	 * 
 	 * @param clientID
 	 *            The ID of the client sending the message
 	 * @param docKey
 	 *            The key of the document whose token we are receiving
 	 */
 	private void receiveToken(String clientID, String docKey) {
 		// Return the token
 		tokenMap.put(docKey, "server");
 		System.out.println(clientID + " giving back token");
 
 		// If there is no document waiting for the document remove from list of
 		// locked docs
 		if (!queueMap.containsKey(docKey) && lockedDocuments.contains(docKey)) {
 			lockedDocuments.remove(docKey);
 		}
 
 		// Now, try to send a token if we can
 		clientID = pollNextClient(docKey);
 		if (clientID != null) {
 			sendToken(clientID, docKey);
 		}
 	}
 
 	/**
 	 * Send a token for the docKey out to the specified client
 	 * 
 	 * @param clientID
 	 *            The ID of the client sending the message
 	 * @param docKey
 	 *            The key of the document whose token we are receiving
 	 */
 	private void sendToken(final String clientID, final String docKey) {
 		System.out.println("sending token to " + clientID);
 		// Now, set the correct clientID. We are "giving" them the token here.
 		tokenMap.put(docKey, clientID);
 
 		// Add to list of locked documents if not already in there
 		if (!lockedDocuments.contains(docKey)) {
 			lockedDocuments.add(docKey);
 		}
 
 		// Now, let's lock the document
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Document toSave;
 		Date endTime;
 
 		Transaction t = pm.currentTransaction();
 		try {
 			// Starting transaction...
 			t.begin();
 			// Create the key
 			Key key = KeyFactory.stringToKey(docKey);
 
 			// Get the document corresponding to the key
 			toSave = pm.getObjectById(Document.class, key);
 			endTime = new Date(System.currentTimeMillis() + LOCK_TIME * 1000);
 			toSave.lock(endTime, clientID);
 
 			pm.makePersistent(toSave);
 
 			t.commit();
 		} finally {
 			// Do some cleanup
 			if (t.isActive()) {
 				t.rollback();
 			}
 
 			pm.close();
 		}
 
 		// Finally, inform the client that the doc is locked and ready for them
 		getChannelService().sendMessage(new ChannelMessage(clientID, docKey));
 
 	}
 
 	/**
 	 * Release the lock of the given document.
 	 * 
 	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#releaseLock
 	 *      (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
 	 */
 	@Override
 	public void releaseLock(String clientID, LockedDocument doc)
 			throws LockExpired {
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
 
 			// Get the client's identity
 			String identity = clientID;
 
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
 
 			// Indicate that the token has been returned
 			receiveToken(clientID, doc.getKey());
 
 		} finally {
 			// Do some cleanup
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	/**
 	 * Adds the client ID to the correct document queue
 	 * 
 	 * @param clientID
 	 *            The ID of the client who is in the queue
 	 * @param docKey
 	 *            The key of the document that the client is waiting for
 	 */
 	private void addToDocQueue(String clientID, String documentKey) {
 		// if we dont already have this doc key in our map, add it in there
 		if (!queueMap.containsKey(documentKey)) {
 			queueMap.put(documentKey, Collections
 					.synchronizedList(new LinkedList<String>()));
 		}
 
 		// add the client to the queue for that particular doc
 		List<String> queue = queueMap.get(documentKey);
 		queue.add(clientID);
 
 		System.out.println(queue.size());
 		queueMap.put(documentKey, queue);
 
 		System.out.print("\nQueue ");
 		for (String s : queue) {
 			System.out.print(s + ", ");
 		}
 
 		System.out.println();
 
 		// this is the position of the newly added client in the queue
 		int pos = queue.size();
 
 		// inform the client which place in line it is
 		getChannelService().sendMessage(
 				new ChannelMessage(clientID, "position: " + pos));
 	}
 
 	/**
 	 * Gets the next client waiting for the specified document.
 	 * 
 	 * @param documentKey
 	 *            The document we want to find the next client for
 	 * @return A client ID of the next client waiting on this doc
 	 */
 	private String pollNextClient(String documentKey) {
 		List<String> queue = queueMap.get(documentKey);
 		if (queue != null && !queue.isEmpty()) {
 			return queue.remove(0);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Removes a client from the specified doc queue
 	 * 
 	 * @param clientID
 	 *            The client we are trying to remove
 	 * @param documentKey
 	 *            The document we are trying to dequeue the client
 	 * @return Whether the client was removed successfully
 	 */
 	private boolean removeClient(String clientID, String documentKey) {
 		List<String> queue = queueMap.get(documentKey);
 		boolean inQueue = false;
 		if (queue != null) {
 			inQueue = queue.remove(clientID);
 			while (queue.remove(clientID)) {
 			}
 		}
 
 		return inQueue;
 	}
 
 	/**
 	 * Locks the given document.
 	 * 
 	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#lockDocument
 	 *      (java.lang.String)
 	 */
 	@Override
 	public void lockDocument(String clientID, String documentKey) {
 		// Handle the case where the token map doesn't have the docKey - no
 		// client has tried to access it yet
 		System.out.println(clientID + " Trying to lock");
 		if (!tokenMap.containsKey(documentKey))
 			tokenMap.put(documentKey, "server");
 
 		// Check if we have the token. If we do, send the token out immediately
 		if (tokenMap.get(documentKey).equals("server"))
 			sendToken(clientID, documentKey);
 		else
 			addToDocQueue(clientID, documentKey);
 	}
 
 	/**
 	 * Called when the client first connects to the app.
 	 * 
 	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#login(java
 	 *      .lang.String)
 	 */
 	@Override
 	public String login(String clientID) {
 		return getChannelService().createChannel(clientID);
 	}
 
 	/**
 	 * Returns a channel service that can be used to do channel operations
 	 * 
 	 * @return A channel service that can be used to do channel operations
 	 */
 	private ChannelService getChannelService() {
 		return ChannelServiceFactory.getChannelService();
 	}
 
 	/**
 	 * Get the locked doc with the given key.
 	 * 
 	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#
 	 *      getLockedDocument(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public LockedDocument getLockedDocument(String clientID, String documentKey)
 			throws LockUnavailable {
 
 		System.out.println(clientID + " getting locked document");
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
 
 			String identity = clientID;
 
 			// If the doc is locked and you own it...
 			if (!toSave.isLocked()
 					|| !toSave.getLockedBy().equals(identity)
 					|| toSave.getLockedUntil().before(
 							new Date(System.currentTimeMillis()))) {
 				throw new LockUnavailable("This lock is not yours");
 			}
 
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
 
 	/**
 	 * Remove the given client from the given document queue. (This client is no
	 * longer waiting on a lock for that doc.)
 	 * 
 	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#leaveLockQueue
 	 *      (java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void leaveLockQueue(String clientID, String documentKey) {
 		removeClient(clientID, documentKey);
 	}
 
 	@Override
 	public void logout(String clientID) {
		System.out.println("logging out");
 		for (String docKey : queueMap.keySet()) {
			removeClient(clientID, docKey);
 		}
		
		/*for ()
		for (String s : queue) {
			
			System.out.print(s + ", ");
		}*/
 	}
 
 }
