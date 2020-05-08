 package coeditor;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 
 import storage.KeyValueStore;
 
 public class Document {
 	String headText;
 	String docId;
 	int headRevision;
 	int committedRevision;
 	ArrayList<RevisionRecord> revisionList;
 	Map<Integer, Client> activeUsers;
 	boolean isOpen;
 	KeyValueStore storage;
 	
 	public Document(String documentId, KeyValueStore storage) {
 		this.storage = storage;
 		this.docId = documentId;
 		this.revisionList = new ArrayList<RevisionRecord> ();
 		this.activeUsers = new Hashtable<Integer, Client> ();
 		this.headRevision = -1;
 		this.committedRevision = 0;
 	}
 	
 	public void open(int connectionId, String clientId) {
 		
 		Client newClient = new Client(clientId);
 		
 		if (!isOpen) {
 			//TODO: connect to S3 to fetch the document.
 			headText = storage.getDocument(docId);
 			
 			Change initChange = new Change(headText);
 			ChangeSet initChangeSet = new ChangeSet(0, headText.length());
 			
 			initChangeSet.addChange(initChange);
 			
 			this.addRevisionRecord(clientId, initChangeSet);
 
 			newClient.latestVersion = headRevision;
 			
 			isOpen = true;
 		} else {
 			//TODO
 		}		
 		
 		this.addUser(connectionId, newClient);
 	}
 	
 	public void save() {
 		
 		updateHeadtext();
 		
 		try {
 	    storage.putDocument(docId, headText);
     } catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
     }
 	}
 	
 	public void close(int connectionId) {
 		save();
 		
 		this.activeUsers.remove(connectionId);
 		
 		if (this.activeUsers.size() == 0)
 			isOpen = false;
 	}
 	
 	public void create(int conenctionId, String clientId) {
 		try {
 	    storage.createBlankDocument(docId);
     } catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
     }
 		
 		open(conenctionId, clientId);
 	}
 	
 	public ChangeSet applyChangeSet(ChangeSet cs, int revisionNumber) {
 		ChangeSet result = cs;
 		
		for (int i = revisionNumber + 1; i <= headRevision; i++) {
 			result = ChangeSet.follows(this.revisionList.get(i).changeSet, cs);
 		}
 		
 		return result;
 	}
 	
 	public void addRevisionRecord(String clientId, ChangeSet cs) {
 		headRevision++;
 		this.revisionList.add(new RevisionRecord(clientId, headRevision, cs));
 	}
 	
 	private void updateHeadtext() {
 		int length = revisionList.size();
 		for (int i = committedRevision; i < committedRevision + 10 && i < length; i++) {
 			System.out.println("headText: " + headText);
 			headText = revisionList.get(i).changeSet.applyTo(headText);
 		}
 		committedRevision = Math.min(committedRevision + 10, revisionList.size());
 	}
 	
 	public void addUser(Integer clientId, Client user) {
 		activeUsers.put(clientId, user);
 	}
 	
 	public void removeUser(Integer clientId) {
 		activeUsers.remove(clientId);
 	}
 	
 	public Set<Integer> getActiveUser() {
 		return this.activeUsers.keySet();
 	}
 	
 	public static void main(String[] args) {
 		KeyValueStore s3 = new KeyValueStore();
 		Document doc = new Document("test_doc", s3);
		doc.create(1, "xxx");
		//doc.open(1, "xxx");
		doc.open(2, "lzh");
		ChangeSet init = new ChangeSet(0, 8);
 		ChangeSet a = new ChangeSet(8, 5);
 		ChangeSet b = new ChangeSet(8, 5);
 		
 		init.addChange(new Change("baseball"));
 		
 		a.addChange(new Change(0, 1));
 		a.addChange(new Change("si"));
 		a.addChange(new Change(7));
 		
 		b.addChange(new Change(0));
 		b.addChange(new Change("e"));
 		b.addChange(new Change(6));
 		b.addChange(new Change("ow"));
 		
 		
 		doc.addRevisionRecord("xxx", init);
 		doc.addRevisionRecord("xxx", a);
		System.out.println(doc.headRevision);
		ChangeSet prime = doc.applyChangeSet(b, 1);
		doc.addRevisionRecord("lzh", prime);
		//doc.addRevisionRecord("xxx", b);
 		System.out.println(doc.headText);
 		System.out.println(doc.docId);
 		doc.save();
 		System.out.println(doc.headText);
 		System.out.println(doc.committedRevision);
 		
 		System.out.println(doc.isOpen);
 		System.out.println(doc.revisionList);
 		System.out.println(doc.activeUsers);
 	  //s3.deleteKey("test_doc");
 	}
 }
