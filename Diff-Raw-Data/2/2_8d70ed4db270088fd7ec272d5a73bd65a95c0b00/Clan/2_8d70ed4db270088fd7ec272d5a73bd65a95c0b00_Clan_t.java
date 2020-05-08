 package elfville.server.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import elfville.protocol.models.SerializableClan;
 import elfville.protocol.models.SerializableElf;
 import elfville.server.Database;
 import elfville.server.SecurityUtils;
 
 /**
  * Clan Model.
  */
 public class Clan extends Model implements Comparable<Clan> {
 	private static final long serialVersionUID = -696380887203611286L;
 	private final String name;
 	private final String description;
 	private final List<Integer> postIDs = Collections
 			.synchronizedList(new ArrayList<Integer>());
 
 	private final int leaderID;
 	private final Set<Integer> applicants = Collections
 			.synchronizedSet(new HashSet<Integer>());
 	private final Set<Integer> members = Collections
 			.synchronizedSet(new HashSet<Integer>());
 
 	public Clan(String name, String description, Elf leader) {
 		super();
 		this.name = name;
 		this.description = description;
 		leaderID = leader.modelID;
 		members.add(leader.modelID);
 	}
 
 	// make a serializable clan object out of this clan
 	public SerializableClan toSerializableClan() {
 		SerializableClan sClan = new SerializableClan();
 		sClan.clanName = getName();
 		sClan.clanDescription = getDescription();
 		sClan.numSocks = getNumSock();
 		sClan.modelID = getEncryptedModelID();
 		sClan.applicants = getApplicants();
 		sClan.members = getMembers();
 		sClan.leader = getLeader().toSerializableElf();
 		return sClan;
 	}
 
 	public List<Post> getPosts() {
 		Collections.sort(postIDs);
 		ArrayList<Post> posts = new ArrayList<Post>(postIDs.size());
 		for (Integer id : postIDs) {
 			posts.add(Database.getInstance().postDB.findByModelID(id));
 		}
 		return posts;
 	}
 
 	public List<SerializableElf> getApplicants() {
 		Integer[] appList = applicants.toArray(new Integer[0]);
 		Arrays.sort(appList);
 
 		List<SerializableElf> applicantList = new ArrayList<SerializableElf>(
 				applicants.size());
 		for (Integer elfID : appList) {
 			applicantList.add(Elf.get(elfID).toSerializableElf());
 		}
 		return applicantList;
 	}
 
 	public Set<SerializableElf> getMembers() {
 		Set<SerializableElf> memberList = new HashSet<SerializableElf>();
 		for (Integer elfID : members) {
 			memberList.add(Elf.get(elfID).toSerializableElf());
 		}
 		return memberList;
 	}
 
 	/* The number of socks owned by all clan members combined */
 	public int getNumSock() {
 		int numSock = 0;
 		for (Integer elfID : members) {
 			numSock += Elf.get(elfID).getNumSocks();
 		}
 		return numSock;
 	}
 
 	public Elf getLeader() {
 		return Elf.get(leaderID);
 	}
 
 	// A stranger becomes an applicant
 	public void apply(Elf elf) {
 		if (!members.contains(elf.modelID)) {
 			applicants.add(elf.modelID);
 			save();
 		}
 	}
 
 	// An applicant becomes a member
 	public void join(Elf elf) {
 		if (applicants.contains(elf.modelID)) {
 			members.add(elf.modelID);
 			applicants.remove(elf.modelID);
 			save();
 			System.out.println("elf " + elf.getName() + " is accepted at "
 					+ this.name);
 		}
 	}
 
 	// the database takes care of cascading delete
 	public void delete() {
 		Database.getInstance().clanDB.remove(this);
 		Database.getInstance().persist(new Deletion(this));
 	}
 
 	// The clan leader cannot do this operation
 	public void leaveClan(Elf elf) {
 		if (isLeader(elf)) {
 			return;
 		}
		for (Integer pid : postIDs.subList(0, postIDs.size())) {
 			Post p = Database.getInstance().postDB.findByModelID(pid);
 			if (p.getElf().equals(elf)) {
 				postIDs.remove(pid);
 			}
 		}
 		members.remove(elf.modelID);
 		save();
 	}
 
 	public boolean isLeader(Elf elf) {
 		return elf.modelID == leaderID;
 	}
 
 	// also true if the elf is the leader
 	public boolean isMember(Elf elf) {
 		return members.contains(elf.modelID);
 	}
 
 	public boolean isApplicant(Elf elf) {
 		return applicants.contains(elf.modelID);
 	}
 
 	public Post getPostFromEncryptedModelID(String encryptedModelID) {
 		int id = SecurityUtils.decryptStringToInt(encryptedModelID);
 		if (postIDs.contains(id)) {
 			return Database.getInstance().postDB.findByModelID(id);
 		}
 		return null;
 	}
 
 	public void createPost(Post post) {
 		post.clanID = modelID;
 		postIDs.add(post.modelID);
 		Database.getInstance().postDB.add(post);
 		save();
 		post.save();
 	}
 
 	public void deletePost(Post post) {
 		postIDs.remove(postIDs.indexOf(post.modelID));
 		Database.getInstance().postDB.remove(post.modelID);
 		save();
 	}
 
 	public void deletePost(String encryptedModelID) {
 		deletePost(getPostFromEncryptedModelID(encryptedModelID));
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	@Override
 	public void save() {
 		super.save();
 		Database.getInstance().clanDB.add(this);
 	}
 
 	public void deny(Elf elf) {
 		applicants.remove(elf.modelID);
 		save();
 	}
 
 	public static Clan get(String name) {
 		return Database.getInstance().clanDB.findByName(name);
 	}
 
 	@Override
 	public int compareTo(Clan c) {
 		return name.compareTo(c.name);
 	}
 
 }
