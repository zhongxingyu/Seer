 package edu.caltech.cs141b.hw2.gwt.collab.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.Document;
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
 	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	private PersistenceManager pm = PMF.get().getPersistenceManager();
 
 	private static final Logger log = Logger
 			.getLogger(CollaboratorServiceImpl.class.toString());
 
 	// Datastore static strings
 	private static final String dsDoc = "Document";
 	private static final String dsTitle = "title";
 	private static final String dsContent = "content";
 	private static final String dsLocked = "locked";
 	private static final String dsLockedTil = "lockedTil";
 
 	@Override
 	public List<DocumentMetadata> getDocumentList() {
 
 		pm = PMF.get().getPersistenceManager();
 		Query query = pm.newQuery(Document.class);
 		List<Document> documentList;
 		ArrayList<DocumentMetadata> docList = new ArrayList<DocumentMetadata>();
 		Transaction t = pm.currentTransaction();
 		try {
 			t.begin();
 			documentList = (List<Document>) query.execute();
 
 			for (Document doc : documentList) {
 				DocumentMetadata metaDoc = new DocumentMetadata(doc.GetKey(),
 						doc.GetTitle());
 				docList.add(metaDoc);
 			}
 			t.commit();
 		} finally {
 			if (t.isActive()) {
 				t.rollback();
 			}
 			query.closeAll();
 			pm.close();
 		}
 
  
  @Override
  public List<DocumentMetadata> getDocumentList() {
	  Query query = pm.newQuery(Document.class);
	  List<Document> documentList;
	  ArrayList<DocumentMetadata> docList = new ArrayList<DocumentMetadata>();
	  try {
		  documentList = (List<Document >) query.execute();
		  System.out.println("Document list = " + documentList);
		  for (Document doc:documentList) {
			  DocumentMetadata metaDoc = new DocumentMetadata(doc.GetKey(),doc.GetTitle());
			  docList.add(metaDoc);
		  }
	  } finally {
	        query.closeAll();
	  }
	System.out.println("Document list = " + docList);
    return docList;
  }
 
 	@Override
 	public UnlockedDocument getDocument(String documentKey) {
 		Key key = KeyFactory.stringToKey(documentKey);
 		UnlockedDocument unlockedDoc;
 		pm = PMF.get().getPersistenceManager();
 		Transaction t = pm.currentTransaction();
 		try {
 			t.begin();
 			Document doc = pm.getObjectById(Document.class, key);
 			unlockedDoc = doc.GetUnlocked();
 			t.commit();
 		} finally {
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 		}
 		return unlockedDoc;
 
 	}
 
 	@Override
 	public UnlockedDocument saveDocument(LockedDocument doc) throws LockExpired {
 		String stringKey = doc.getKey();
 		Document toSave;
 		pm = PMF.get().getPersistenceManager();
 		Transaction t = pm.currentTransaction();
 		try {
 			t.begin();
 			if (stringKey == null) {
 				toSave = new Document(doc);
 			} else {
 				Key key = KeyFactory.stringToKey(stringKey);
 				toSave = pm.getObjectById(Document.class, key);
 				toSave.Update(doc);
 			}
 
 			pm.makePersistent(toSave);
 			t.commit();
 		} finally {
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 		}
 
 		return toSave.GetUnlocked();
 	}
 
 	@Override
 	public void releaseLock(LockedDocument doc) throws LockExpired {
 	}
 
 }
