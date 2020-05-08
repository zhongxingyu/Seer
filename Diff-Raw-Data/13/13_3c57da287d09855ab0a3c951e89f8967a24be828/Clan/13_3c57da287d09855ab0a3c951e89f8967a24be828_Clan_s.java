 package elfville.server.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import elfville.protocol.models.SerializableClan;
 import elfville.protocol.models.SerializableElf;
 import elfville.protocol.models.SerializablePost;
 import elfville.server.Database;
 import elfville.server.SecurityUtils;
 
 /*
  * Clan Model.
  */
 public class Clan extends Model {
 	private static final long serialVersionUID = -696380887203611286L;
 	private String name;
 	private String description;
 	private ConcurrentHashMap<Integer, Post> posts;
 
 	private Elf leader;
 	private ConcurrentHashMap<Integer, Elf> applicants;
 	private ConcurrentHashMap<Integer, Elf> members;
 
 	// private List<Post> posts;
 
 	public Clan(String name, String description) {
 		super();
 		this.name = name;
 		this.description = description;
 		posts = new ConcurrentHashMap<Integer, Post>();
 		members = new ConcurrentHashMap<Integer, Elf>();
 		applicants = new ConcurrentHashMap<Integer, Elf>();
 	}
 
 	public Clan() {
 		super();
 	}
 
 	// make a serializable clan object out of this clan
 	public SerializableClan getSerializableClan() {
 		SerializableClan sClan = new SerializableClan();
 		sClan.clanName = getName();
 		sClan.clanDescription = getDescription();
 		sClan.numSocks = getNumSock();
 		sClan.modelID = SecurityUtils.encryptIntToString(this.getModelID());
 		sClan.applicants = getApplicants();
 		sClan.members = getMembers();
 		sClan.leader = getLeader().getSerializableElf();
 		return sClan;
 	}
 
	public List<SerializablePost> getPosts() {
		List<SerializablePost> postList = new ArrayList<SerializablePost>();
		List<Post> postsList = new ArrayList<Post>(posts.values());
 		Collections.sort(postList);
 		return postList;
 	}
 	
 	public List<SerializableElf> getApplicants() {
 		List<SerializableElf> applicantList = new ArrayList<SerializableElf>();
 		for (ConcurrentHashMap.Entry<Integer, Elf> applicant : applicants.entrySet()) {
 			applicantList.add(applicant.getValue().getSerializableElf());
 		}
 		return applicantList;
 	}
 
 	public List<SerializableElf> getMembers() {
 		List<SerializableElf> memberList = new ArrayList<SerializableElf>();
 		for (ConcurrentHashMap.Entry<Integer, Elf> member : members.entrySet()) {
 			memberList.add(member.getValue().getSerializableElf());
 		}
 		return memberList;
 	}
 	
 	/* The number of socks owned by all clan members combined */
 	public int getNumSock() {
 		int numSock = 0;
 		for (ConcurrentHashMap.Entry<Integer, Elf> member : members.entrySet()) {
 			numSock += member.getValue().getNumSocks();
 		}
 		return numSock;
 	}
 
 	public Elf getLeader() {
 		Elf l;
 		synchronized (this) {
 			l = leader;
 		}
 		return l;
 	}
 
 	public void setLeader(Elf elf) {
 		if (leader != null) {
 			// TODO: throws some errors!
 		}
 		synchronized (this) {
 			leader = elf;
 		}
 		members.put(elf.getModelID(), elf);
 	}
 
 	// A stranger becomes an applicant
 	public void applyClan(Elf elf) {
 		if (applicants.containsKey(elf.getModelID())
 				|| members.containsKey(elf.getModelID())) {
 			return;
 		}
 		while(applicants.get(elf.getModelID()) == null){
 			applicants.put(elf.getModelID(), elf);
 		}
 	}
 
 	// An applicant becomes a member
 	public void joinClan(Elf elf) {
 		if (applicants.containsKey(elf.getModelID())
 				&& !members.containsKey(elf.getModelID())) {
 			members.put(elf.getModelID(), elf);
 			applicants.remove(elf.getModelID());
 		}
 	}
 
 	// the database takes care of cascading delete
 	public void deleteClan() {
 		database.clanDB.delete(this);
 	}
 
 	// The clan leader cannot do this operation
 	public void leaveClan(Elf elf) {
 		if (elf == getLeader()) {
 			return;
 		}
 		for (ConcurrentHashMap.Entry<Integer, Post> post : posts.entrySet()) {
 			if (post.getValue().getElf() == elf) {
 				posts.remove(post.getValue().getModelID());
 			}
 		}
 		members.remove(elf.getModelID());
 	}
 
 	public boolean isLeader(Elf elf) {
 		if (elf == getLeader()) {
 			return true;
 		}
 		return false;
 	}
 
 	// also true if the elf is the leader
 	public boolean isMember(Elf elf) {
 		return members.containsValue(elf);
 	}
 
 	public boolean isApplicant(Elf elf) {
 		//TODO this function returns the wrong thing.
 		// this is why I can't accept applicants (because they
 		// appear to not be applicants) and it is why an applicant
 		// doesn't appear as an applicant to the client.
 		return applicants.containsValue(elf);
 	}
 
 	public Post getPostFromEncrpytedModelID(String encryptedModelID) {
 		return posts.get(SecurityUtils.decryptStringToInt(encryptedModelID));
 	}
 
 	public void createPost(Post post) {
 		posts.put(post.getModelID(), post);
 	}
 
 	public boolean hasPost(Post post) {
 		return posts.containsKey(post.getModelID());
 	}
 
 	public boolean hasPost(String encryptedModelID) {
 		return hasPost(getPostFromEncrpytedModelID(encryptedModelID));
 	}
 
 	public void deletePost(Post post) {
 		if (posts.containsKey(post.getModelID())) {
 			posts.remove(post.getModelID());
 		}
 	}
 
 	public void deletePost(String encryptedModelID) {
 		deletePost(getPostFromEncrpytedModelID(encryptedModelID));
 	}
 
 	// auto generated getters and setters
 	public synchronized String getName() {
 		return name;
 	}
 	public synchronized void setName(String name) {
 		this.name = name;
 	}
 
 	public synchronized String getDescription() {
 		return description;
 	}
 
 	public synchronized void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Override
 	public boolean save() {
 		// TODO add validations
 		if (Database.DB.clanDB.findByModelID(modelID) == null) {
 			Database.DB.clanDB.insert(this);
 		}
 		return true;
 	}
 
 	public void deny(Elf elf) {
 		if (applicants.containsKey(elf.getModelID())
 				&& !members.containsKey(elf.getModelID())) {
 			applicants.remove(elf.getModelID());
 		}
 	}
 }
