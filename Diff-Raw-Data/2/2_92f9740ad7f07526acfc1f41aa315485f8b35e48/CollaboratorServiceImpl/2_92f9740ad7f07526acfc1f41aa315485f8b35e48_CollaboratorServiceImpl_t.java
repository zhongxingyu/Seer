 package edu.caltech.cs141b.hw2.gwt.collab.server;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
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
 public class CollaboratorServiceImpl extends RemoteServiceServlet
 implements CollaboratorService {
 
 	public static final int LOCK_TIMEOUT = 30;     // Seconds
 
 	@SuppressWarnings("unused")
 	private static final Logger log = Logger.getLogger(CollaboratorServiceImpl.class.toString());
 
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
 	public LockedDocument lockDocument(String documentKey) throws LockUnavailable {
 		Key key = KeyFactory.stringToKey(documentKey);
 		Document persistedDoc = null;
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Transaction txn = pm.currentTransaction();
 		try {
 			txn.begin();
 
 			// Get persisted doc.
 			persistedDoc = pm.getObjectById(Document.class, key);
 
 			// Figure out if a document is available to be locked. If it is,
 			// lock it and persist the new timestamp; otherwise, throw an exception.
 			Date currentDate = new Date();
 			Date lockedUntil = persistedDoc.getLockedUntil();
 			if (lockedUntil != null && currentDate.before(lockedUntil)) {
 				throw new LockUnavailable("Document locked until " + persistedDoc.getLockedUntil());
 			}
 			
 			persistedDoc.setLockedBy(getThreadLocalRequest().getRemoteAddr());
 			Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.SECOND, LOCK_TIMEOUT);
 			persistedDoc.setLockedUntil(cal.getTime());
 			pm.makePersistent(persistedDoc);
 			
 			txn.commit();
 
 			return new LockedDocument(
 					persistedDoc.getLockedBy(), 
 					persistedDoc.getLockedUntil(), 
 					KeyFactory.keyToString(persistedDoc.getKey()),
 					persistedDoc.getTitle(),
 					persistedDoc.getContents());
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
 						persistedDoc.getContents());
 			}
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public UnlockedDocument saveDocument(LockedDocument doc) throws LockExpired {
 		// Find key from doc.
 		Key key = null;
 		if (doc.getKey() != null) {
 			key = KeyFactory.stringToKey(doc.getKey());
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
 
 			// If persistedDoc is null, then the Document object should be persisted
 			// so that a key will automatically be generated. Otherwise, take the
 			// object, check credentials, modify some fields, and persist again.
 			if (persistedDoc == null) {
 				persistedDoc = new Document(doc.getTitle(), doc.getContents());
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
 					throw new LockExpired();
 				}
 
 				persistedDoc.setTitle(doc.getTitle());
 				persistedDoc.setContents(doc.getContents());
 				persistedDoc.setLockedBy(null);
 				persistedDoc.setLockedUntil(null);
 			}
 			pm.makePersistent(persistedDoc);
 
 			txn.commit();
 
 			return new UnlockedDocument(
 					KeyFactory.keyToString(persistedDoc.getKey()), 
 					persistedDoc.getTitle(), 
 					persistedDoc.getContents());
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	@Override
 	public void releaseLock(LockedDocument doc) throws LockExpired {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Transaction txn = pm.currentTransaction();
 		try {
 			txn.begin();
 
 			// Get persisted document.
 			Key key = KeyFactory.stringToKey(doc.getKey());
 			Document persistedDoc = pm.getObjectById(Document.class, key);
 
 			// We quickly check if the lock has expired, else continue on with
 			// saving and unlocking the document.
 			Date currentDate = new Date();
 			Date lockedUntil = persistedDoc.getLockedUntil();
 			String ipAddress = getThreadLocalRequest().getRemoteAddr();
			String lockedBy = persistedDoc.getLockedBy();
 			// A lock is not expired if: 
 			// 1) lockedUntil is set AND after now, AND
 			// 2) lockedBy is set AND is this user
 			if (!((lockedUntil != null && currentDate.before(lockedUntil)) &&
 					(lockedBy != null && lockedBy.equals(ipAddress)))) {
 				throw new LockExpired();
 			}
 			
 			// Release the lock on the document; update lockedBy and lockedUntil.
 			persistedDoc.setLockedBy(null);
 			persistedDoc.setLockedUntil(null);
 			pm.makePersistent(persistedDoc);
 
 			txn.commit();
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 			}
 			pm.close();
 		}
 	}
 }
