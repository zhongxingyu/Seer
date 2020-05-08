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
 			headText = storage.getDocument(docId);
 			
 			if (this.revisionList.isEmpty()) {
 				Change initChange = new Change(headText);
 				ChangeSet initChangeSet = new ChangeSet(0, headText.length());
 				
 				initChangeSet.addChange(initChange);
 				
 				this.addRevisionRecord(clientId, initChangeSet);
 			} else {
 				updateHeadtext();
 			}
 			isOpen = true;
 		} else {
 			//TODO
 			System.out.println("the doc has been opened, update and send new headText");
 			updateHeadtext();
 		}		
 		
 		newClient.latestVersion = headRevision;
 		
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
 		for (int i = committedRevision; i <= headRevision; i++) {
 			headText = revisionList.get(i).changeSet.applyTo(headText);
 			System.out.println("headText: " + headText);
 		}
 		committedRevision = headRevision;
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
 	
 	public void updateClientVersion() {
 		for (Client c: this.activeUsers.values()) {
 			c.latestVersion = this.headRevision;
 		}
 	}
 	
 	public static void main(String[] args) {
 		KeyValueStore s3 = new KeyValueStore();
 		Document doc = new Document("test_doc", s3);
 		doc.create(1, "xxx");
 		//doc.open(1, "xxx");
 		//doc.open(2, "lzh");
 		ChangeSet init = new ChangeSet(0, 8);
 		init.addChange(new Change("baseball"));
 		
 		/*ChangeSet a = new ChangeSet(8, 5);
 		ChangeSet b = new ChangeSet(8, 5);
 		
 		
 		
 		a.addChange(new Change(0, 1));
 		a.addChange(new Change("si"));
 		a.addChange(new Change(7));
 		
 		b.addChange(new Change(0));
 		b.addChange(new Change("e"));
 		b.addChange(new Change(6));
 		b.addChange(new Change("ow"));*/
 		
 		ChangeSet a = new ChangeSet(8, 7);
 		a.addChange(new Change(0, 6));
 		
 		doc.addRevisionRecord("xxx", init);
 		doc.addRevisionRecord("xxx", a);
 		System.out.println(doc.headRevision);
 		//ChangeSet prime = doc.applyChangeSet(b, 1);
 		//doc.addRevisionRecord("lzh", prime);
 		//doc.addRevisionRecord("xxx", b);
 		System.out.println(doc.headRevision);
 		System.out.println(doc.committedRevision);
 		//System.out.println(init.applyTo(""));
 		doc.save();
 		System.out.println(doc.headText);
 		System.out.println(doc.committedRevision);
 		
 		System.out.println(doc.isOpen);
 		System.out.println(doc.revisionList);
 		System.out.println(doc.activeUsers);
 	  //s3.deleteKey("test_doc");
 	}
 }
