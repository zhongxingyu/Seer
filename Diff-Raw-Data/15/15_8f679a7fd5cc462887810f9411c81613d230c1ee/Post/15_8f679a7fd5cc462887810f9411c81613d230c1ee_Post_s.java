 package elfville.server.model;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import elfville.protocol.models.SerializablePost;
 
 /*
  * Post Model
  */
 public class Post extends Model {
 	private Elf elf;
 	private String title;
 	private String content;
 
 	private Set<Elf> upsockedElves;
 	private Set<Elf> downsockedElves;
 
 	//TODO: this should not take a serializable post as an argument.  code must be refactored
 	public Post(SerializablePost postRequest, Elf elf) {
 		super();
 		// elf= postRequest.username;
 		title = postRequest.title;
 		content = postRequest.content;
 		this.elf = elf;
 		upsockedElves = Collections.synchronizedSet(new HashSet<Elf>());
 		downsockedElves = Collections.synchronizedSet(new HashSet<Elf>());
 	}
 
 	public SerializablePost getSerializablePost() {
 		SerializablePost sPost = new SerializablePost();
 		sPost.title = getTitle();
 		sPost.content = getContent();
 		sPost.createdAt = getCreatedAt();
 		sPost.upvotes = getNumUpsock();
 		sPost.downvotes = getNumDownsock();
 		sPost.username = elf.getElfName();
 		sPost.elfID = elf.getEncryptedModelID();
 		sPost.modelID = getEncryptedModelID();
 		return sPost;
 	}
 
 	public void delete() {
 		database.postDB.delete(this);
 	}
 
 	public boolean upsock(Elf upsockingElf) {
		if (!upsockedElves.contains(upsockingElf)) {
 			upsockedElves.add(upsockingElf);
 			return true;
 		}
 		return false;
 	}
 
 	// Each post cannot have < 0 socks.
 	public boolean downsock(Elf downsockingElf) {
		if (!downsockedElves.contains(downsockingElf) && getNumSock() > 0) {
 			downsockedElves.add(downsockingElf);
 			return true;
 		}
 		return false;
 	}
 
 	public int getNumUpsock() {
 		return upsockedElves.size();
 	}
 
 	public int getNumDownsock() {
 		return downsockedElves.size();
 	}
 
 	public int getNumSock() {
 		return upsockedElves.size() - downsockedElves.size();
 	}
 
 	/* auto generated getters and setters */
 
 	public Elf getElf() {
 		Elf e;
 		synchronized (this) {
 			e = elf;
 		}
 		return e;
 	}
 
 	public void setElf(Elf elf) {
 		synchronized (this) {
 			this.elf = elf;
 		}
 	}
 
 	public String getTitle() {
 		String t;
 		synchronized (this) {
 			t = title;
 		}
 		return t;
 	}
 
 	public void setTitle(String title) {
 		synchronized (this) {
 			this.title = title;
 		}
 	}
 
 	public String getContent() {
 		String c;
 		synchronized (this) {
 			c = content;
 		}
 		return c;
 	}
 
 	public void setContent(String content) {
 		synchronized (this) {
 			this.content = content;
 		}
 	}
 }
