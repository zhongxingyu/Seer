ckage edu.caltech.cs141b.hw2.gwt.collab.server;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Key;
 
 import java.util.Date;
 /**
  * The server side implementation of the RPC service.
  */
 
 
 
 
 @SuppressWarnings("serial")
 public class CollaboratorServiceImpl extends RemoteServiceServlet implements
     CollaboratorService {
   DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
   private static final Logger log = Logger.getLogger(CollaboratorServiceImpl.class.toString());
   
   // Datastore static strings
   private static final String dsDoc = "Document";
   private static final String dsTitle = "title";
   private static final String dsContent = "content";
   
   
   @Override
   public List<DocumentMetadata> getDocumentList() {
     return null;
   }
 
   @Override
   public LockedDocument lockDocument(String documentKey)
       throws LockUnavailable {
     return null;
   }
 
   @Override
   public UnlockedDocument getDocument(String documentKey) {
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     Key docKey = KeyFactory.stringToKey(documentKey);
     Entity doc = datastore.get(docKey);
     String title = (String) doc.getProperty(dsTitle);
     String content = (String) doc.getProperty(dsContent);
     
     UnlockedDocument unlockedDoc = new UnlockedDocument(documentKey, title, content);
     
     return unlockedDoc;
   }
 
   @Override
   public UnlockedDocument saveDocument(LockedDocument doc)
     throws LockExpired {
     
     Date date = new Date();
     Key key = KeyFactory.createKey(dsDoc, 1);
     Entity document = new Entity (dsDoc, key);
     document.setProperty(dsTitle, doc.getTitle());
     document.setProperty(dsContent, doc.getContents());
     
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     datastore.put(document);
     return doc.unlock();
   }
   
   @Override
   public void releaseLock(LockedDocument doc) throws LockExpired {
   }
 
 }
 
 
