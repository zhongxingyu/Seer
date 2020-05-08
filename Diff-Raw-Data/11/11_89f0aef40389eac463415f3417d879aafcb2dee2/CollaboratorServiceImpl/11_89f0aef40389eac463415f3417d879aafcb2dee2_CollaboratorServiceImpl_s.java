 package edu.caltech.cs141b.hw2.gwt.collab.server;
 
 import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withCountdownMillis;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Queue;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import com.google.appengine.api.channel.ChannelMessage;
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Text;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.DocRequestorResult;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.Messages;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class CollaboratorServiceImpl extends RemoteServiceServlet
 implements CollaboratorService {
 
 	public static final int LOCK_TIMEOUT = 30;     // Seconds
 	public static final String DELIMITER = "~";
 
 	private Hashtable<String, String> tokenToClient = 
 			new Hashtable<String, String>();
 	private Hashtable<String, Queue<String>> docToQueue =
 			new Hashtable<String, Queue<String>>();    // Queues are of clientIds
 	private Hashtable<String, String> docToTaskNames =
 			new Hashtable<String, String>();
 	private Object instantiationLock = new Object();
 
 	@SuppressWarnings("unused")
 	private static final Logger log = Logger.getLogger(CollaboratorServiceImpl.class.toString());
 
 	public CollaboratorServiceImpl() {
 		CollaboratorServiceCommon.setService(this);
 	}
 
 	@Override
 	public String setUpChannel() {
 		String clientId = UUID.randomUUID().toString();
 		String token = ChannelServiceFactory.getChannelService()
 				.createChannel(clientId);
 		tokenToClient.put(token, clientId);
 		return token;
 	}
 
 	@Override
 	public List<DocumentMetadata> getDocumentList() {
 		List<DocumentMetadata> docMetaList = new ArrayList<DocumentMetadata>();
 
 		// Get documents by querying all Document.class types
 		List<Document> documents = null;
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query q = pm.newQuery(Document.class);
 
 			@SuppressWarnings("unchecked")
 			List<Document> documentsTemp = (List<Document>) q.execute();
 			documents = documentsTemp;
 
 			// Convert answer into list of metadata and return it.
 			for (Document doc : documents) {
 				String docKey = KeyFactory.keyToString(doc.getKey());
 				String docTitle = doc.getTitle();
 				docMetaList.add(new DocumentMetadata(docKey, docTitle));
 			}
 			return docMetaList;
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public DocRequestorResult requestDocument(String documentKey, String token) {
 		int numPeopleInFront = -1;
 		String clientId = tokenToClient.get(token);
 		lazyInstantiationsForDoc(documentKey);
 		Queue<String> clientQueue = docToQueue.get(documentKey);
 		synchronized (clientQueue) {
 			numPeopleInFront = clientQueue.size();
 			clientQueue.add(clientId);
 		}
 		if (numPeopleInFront == 0) {
 			sendMessage(clientId, Messages.CODE_LOCK_READY + documentKey);
 		}
 		return new DocRequestorResult(documentKey, numPeopleInFront);
 	}
 
 	@Override
 	public String unrequestDocument(String documentKey, String token) {
 		// Remove the client from the timeout queue.
 		removeFromTaskQueue(documentKey);
 
 		// Remove clientId from the document queue.
 		String clientToRemove = tokenToClient.get(token);
 		Queue<String> clientIds = docToQueue.get(documentKey);
 		synchronized (clientIds) {
 			Iterator<String> clientsItr = clientIds.iterator();
 			int i = 0;
 			while (clientsItr.hasNext()) {
 				String clientId = clientsItr.next();
 				if (clientId.equals(clientToRemove)) {
 					clientsItr.remove();
 					break;
 				}
 				i++;
 			}
 			// Notify the rest of the people that they have moved up one position.
 			while (clientsItr.hasNext()) {
 				String clientId = clientsItr.next();
 				sendMessage(clientId, Messages.CODE_LOCK_NOT_READY + 
 						String.valueOf(i) + DELIMITER + documentKey);
 				i++;
 			}
 		}
 
 		return documentKey;
 	}
 
 	@Override
 	public LockedDocument lockDocument(String docKey, String token) throws LockUnavailable {
 		// Lock the document.
 		Key key = KeyFactory.stringToKey(docKey);
 		Document persistedDoc = null;
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Transaction txn = pm.currentTransaction();
 		try {
 			txn.begin();
 
 			// Get persisted doc.
 			persistedDoc = pm.getObjectById(Document.class, key);
 
 			// We need to check the queue to see that we're actually at the front.
 			// This prevents malicious users from spamming this method call and cutting the line.
 			Queue<String> clientIds = docToQueue.get(docKey);
 			synchronized (clientIds) {
 				if (clientIds != null && clientIds.size() != 0 &&
 						!clientIds.peek().equals(tokenToClient.get(token))) {
 					throw new LockUnavailable(false, docKey, persistedDoc.getLockedUntil(), clientIds.peek());
 				}
 			}
 
 			// Figure out if a document is available to be locked. If it is,
 			// lock it and persist the new timestamp; otherwise, throw an exception.
 			Date currentDate = new Date();
 			Date lockedUntil = persistedDoc.getLockedUntil();
 			if (lockedUntil != null && currentDate.before(lockedUntil)) {
 				throw new LockUnavailable(true, docKey, lockedUntil, persistedDoc.getLockedBy()); 
 			}
 
 			persistedDoc.setLockedBy(token);
 			Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.SECOND, LOCK_TIMEOUT);
 			persistedDoc.setLockedUntil(cal.getTime());
 			pm.makePersistent(persistedDoc);
 
 			txn.commit();
 
 			// Set up scheduled task so that after a timeout period, we just
 			// move onto the next person in the queue.
 			String taskName = UUID.randomUUID().toString();
 			QueueFactory.getDefaultQueue().add(
 					withCountdownMillis(LOCK_TIMEOUT * 1000)
 					.url("/task/lockExpiration")
 					.param("docKey", docKey)
 					.taskName(taskName));
 			docToTaskNames.put(docKey, taskName);
 
 			return new LockedDocument(
 					persistedDoc.getLockedBy(), 
 					persistedDoc.getLockedUntil(), 
 					docKey,
 					persistedDoc.getTitle(),
 					persistedDoc.getContents().getValue());
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	@Override
 	public UnlockedDocument getDocument(String documentKey) {
 		Document persistedDoc = null;
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Key key = KeyFactory.stringToKey(documentKey);
 			persistedDoc = pm.getObjectById(Document.class, key);
 
 			if (persistedDoc == null) {
 				return null;
 			} else {
 				return new UnlockedDocument(
 						documentKey,
 						persistedDoc.getTitle(),
 						persistedDoc.getContents().getValue());
 			}
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public UnlockedDocument saveDocument(LockedDocument doc, String token) throws LockExpired {
 		// Find key from doc.
 		String keyStr = doc.getKey();
 		Key key = null;
 		if (keyStr != null) {
 			key = KeyFactory.stringToKey(keyStr);
 
 			// Remove the client from the timeout queue.
 			removeFromTaskQueue(keyStr);
 		}
 
 		// Persist Document JDO.
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Transaction txn = pm.currentTransaction();
 		try {
 			txn.begin();
 
 			// Get persisted document.
 			Document persistedDoc = null;
 			if (key != null) {
 				persistedDoc = pm.getObjectById(Document.class, key);
 			}
 
 			// If persistedDoc is null, then the Document object should be persisted,
 			// so that a key will automatically be generated. Otherwise, take the
 			// object, check credentials, modify some fields, and persist again.
 			if (persistedDoc == null) {
 				persistedDoc = new Document(doc.getTitle(), new Text(doc.getContents()));
 			} else {
 				Date currentDate = new Date();
 				Date lockedUntil = persistedDoc.getLockedUntil();
				String ipAddress = getThreadLocalRequest().getRemoteAddr();
 				String lockedBy = persistedDoc.getLockedBy();
 				// A lock is not expired if: 
 				// 1) lockedUntil is set AND after now, AND
 				// 2) lockedBy is set AND is this user
 				if (!((lockedUntil != null && currentDate.before(lockedUntil)) &&
						(lockedBy != null && lockedBy.equals(ipAddress)))) {
 					throw new LockExpired(keyStr);
 				}
 
 				persistedDoc.setTitle(doc.getTitle());
 				persistedDoc.setContents(new Text(doc.getContents()));
 				persistedDoc.setLockedBy(null);
 				persistedDoc.setLockedUntil(null);
 			}
 			pm.makePersistent(persistedDoc);
 
 			txn.commit();
 
 			// Update queue for the doc and notify the person next in line.
 			keyStr = KeyFactory.keyToString(persistedDoc.getKey());
 			lazyInstantiationsForDoc(keyStr);
 			pollDocQueue(keyStr, false);
 
 			// Pack up UnlockedDocument to return.
 			return new UnlockedDocument(
 					keyStr,
 					persistedDoc.getTitle(), 
 					persistedDoc.getContents().getValue());
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	@Override
 	public String releaseLock(LockedDocument doc, String token) throws LockExpired {
 		// Remove the client from the timeout queue.
 		String keyStr = doc.getKey();
 		removeFromTaskQueue(keyStr);
 
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Transaction txn = pm.currentTransaction();
 		try {
 			txn.begin();
 
 			// Get persisted document.
 			Key key = KeyFactory.stringToKey(keyStr);
 			Document persistedDoc = pm.getObjectById(Document.class, key);
 
 			// We quickly check if the lock has expired, else continue on with
 			// saving and unlocking the document.
 			Date currentDate = new Date();
 			Date lockedUntil = persistedDoc.getLockedUntil();
			String ipAddress = getThreadLocalRequest().getRemoteAddr();
			String lockedBy = doc.getLockedBy();
 			// A lock is not expired if: 
 			// 1) lockedUntil is set AND after now, AND
 			// 2) lockedBy is set AND is this user
 			if (!((lockedUntil != null && currentDate.before(lockedUntil)) &&
					(lockedBy != null && lockedBy.equals(ipAddress)))) {
 				throw new LockExpired(keyStr);
 			}
 
 			// Release the lock on the document; update lockedBy and lockedUntil.
 			persistedDoc.setLockedBy(null);
 			persistedDoc.setLockedUntil(null);
 			pm.makePersistent(persistedDoc);
 
 			txn.commit();
 			
 			// Update queue for the doc and notify the person next in line.
 			pollDocQueue(keyStr, false);
 
 			return keyStr;
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	protected void pollDocQueue(String documentKey, boolean lockExpired) {
 		// Update queue for the doc.
 		Queue<String> clientIds = docToQueue.get(documentKey);
 		synchronized (clientIds) {
 			String clientId = clientIds.poll();
 			if (clientId != null) {
 				if (lockExpired) {
 					sendMessage(clientId, Messages.CODE_LOCK_EXPIRED + documentKey);
 				}
 				if (!clientIds.isEmpty()) {
 					Iterator<String> clientsItr = clientIds.iterator();
 					// Notify the first person that doc is ready to be locked.
 					clientId = clientsItr.next();
 					sendMessage(clientId, Messages.CODE_LOCK_READY + documentKey);
 					// Notify the rest that the number of people left has changed.
 					int i = 1;
 					while (clientsItr.hasNext()) {
 						clientId = clientsItr.next();
 						sendMessage(clientId, Messages.CODE_LOCK_NOT_READY + 
 								String.valueOf(i) + DELIMITER + documentKey);
 						i++;
 					}
 				}
 			}
 		}
 	}
 
 	private void sendMessage(String clientId, String message) {
 		ChannelService channelService = ChannelServiceFactory.getChannelService();
 		channelService.sendMessage(new ChannelMessage(clientId, message));
 	}
 
 	private void lazyInstantiationsForDoc(String documentKey) {
 		// Lazy instantiation for the queues and corresponding locks.
 		if (!docToQueue.containsKey(documentKey)) {
 			synchronized (instantiationLock) {
 				if (!docToQueue.containsKey(documentKey)) {
 					docToQueue.put(documentKey, new ArrayDeque<String>());
 				}
 			}
 		}
 	}
 
 	private void removeFromTaskQueue(String documentKey) {
 		String taskName = docToTaskNames.get(documentKey);
 		if (taskName != null) {
 			QueueFactory.getDefaultQueue().deleteTask(taskName);
 		}
 	}
 }
