 package elfville.server.model;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import elfville.protocol.models.SerializablePost;
 
 /*
  * Post Model
  */
 public class Post extends Model implements Comparable<Post> {
 	private static final long serialVersionUID = 6422767335685038776L;
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
 
 	public SerializablePost toSerializablePost() {
 		SerializablePost sPost = new SerializablePost();
 		sPost.title = getTitle();
 		sPost.content = getContent();
 		sPost.createdAt = getCreatedAt();
 		sPost.upvotes = getNumUpsock();
 		sPost.downvotes = getNumDownsock();
 		sPost.username = elf.getElfName();
 		sPost.elfModelID = elf.getEncryptedModelID();
 		sPost.modelID = getEncryptedModelID();
 		return sPost;
 	}
 	
 	public SerializablePost toSerializablePost(Elf elf){
 		SerializablePost sPost= toSerializablePost();
 		sPost.iVoted= this.downsockedElves.contains(elf) || this.upsock(elf);
 		sPost.myPost= elf.equals(this.elf);
 		return sPost;
 	}
 
 	public void delete() {
 		database.postDB.delete(this);
 	}
 
 	public boolean upsock(Elf upsockingElf) {
 		//each elf can only upsock OR downsock a single post
 		if (!upsockedElves.contains(upsockingElf) && !downsockedElves.contains(upsockingElf)) {
 			upsockedElves.add(upsockingElf);
 			return true;
 		}
 		return false;
 	}
 
 	// Each post cannot have < 0 socks.
 	public boolean downsock(Elf downsockingElf) {
 		//each elf can only upsock OR downsock a single post
 		if (!downsockedElves.contains(downsockingElf) && !upsockedElves.contains(downsockingElf)) {
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
 
 	public synchronized Elf getElf() {
 		return elf;
 	}
 
 	public synchronized void setElf(Elf elf) {
 		this.elf = elf;
 	}
 
 	public synchronized String getTitle() {
 		return title;
 	}
 
 	public synchronized void setTitle(String title) {
 		this.title = title;
 	}
 
 	public synchronized String getContent() {
 		return content;
 	}
 
 	public synchronized void setContent(String content) {
 		this.content = content;
 	}
 
 	@Override
 	public boolean save() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int compareTo(Post arg0) {
 		if (getNumSock() > arg0.getNumSock()) {
 			return -1;
 		} else if (getNumSock() < arg0.getNumSock()) {
 			return 1;
 		} else {
			return new Integer(arg0.getModelID()).compareTo(modelID);
 		}
 	}
 }
