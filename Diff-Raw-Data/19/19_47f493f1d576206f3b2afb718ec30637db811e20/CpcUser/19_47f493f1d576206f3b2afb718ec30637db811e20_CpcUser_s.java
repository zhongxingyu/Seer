 package fr.cpcgifts.model;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.users.User;
 
 @PersistenceCapable(detachable="true")
 public class CpcUser implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key key;
 
 	@Persistent
 	private String id;
 
 	@Persistent
 	/** Profil d'utilisateur google. */
 	private User guser;
 
 	@Persistent
 	private String cpcProfileId;
 
 	@Persistent
 	private String cpcNickname;
 
 	@Persistent
 	private String avatarUrl = "/img/avatar.jpg";
 	
 	@Persistent
 	private boolean banned;
 	
 	@Persistent
 	/** Liste des concours créés. */
 	private Set<Key> giveawaySet;
 	
 	@Persistent
 	/** Liste des participations aux concours. */
 	private Set<Key> entrySet;
 	
 	@Persistent
 	/** Liste des concours gagnés */
 	private Set<Key> wonSet;
 	
	@Persistent
 	/**	 Liste des liens vers les profils externes (steam, etc...) */
	private Map<String,String> profiles;
 	
 	
 	public CpcUser(User guser, String cpcProfileId) {
 		super();
 		this.guser = guser;
 
 		// paramètres générés
 		this.id = guser.getUserId();
 
 		String[] splitProfileId = cpcProfileId.split("-",2);
 
 		this.cpcNickname = splitProfileId[1];
 
 		this.cpcProfileId = splitProfileId[0];
 
 		this.giveawaySet = new HashSet<Key>();	
 
 		this.entrySet = new HashSet<Key>();
 		
		this.profiles = new HashMap<String, String>();
 
 	}
 
 
 	public Key getKey() {
 		return key;
 	}
 
 	public void setKey(Key key) {
 		this.key = key;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public User getGuser() {
 		return guser;
 	}
 
 	public void setGuser(User guser) {
 		this.id = guser.getUserId();
 		this.guser = guser;
 	}
 
 	public String getCpcProfileUrl() {
 		return cpcProfileId;
 	}
 
 	public void setCpcProfileUrl(String cpcProfileUrl) {
 		this.cpcProfileId = cpcProfileUrl;
 	}
 
 	public String getCpcNickname() {
 		return cpcNickname;
 	}
 
 	public void setCpcNickname(String cpcNickname) {
 		this.cpcNickname = cpcNickname;
 	}
 
 	public String getCpcProfileId() {
 		return cpcProfileId;
 	}
 
 	public void setCpcProfileId(String cpcProfileId) {
 		this.cpcProfileId = cpcProfileId;
 	}
 
 
 
 	public String getAvatarUrl() {
 		return avatarUrl;
 	}
 
 	public void setAvatarUrl(String avatarUrl) {
 		this.avatarUrl = avatarUrl;
 	}
 
 	public Set<Key> getGiveaways() {
 		if(this.giveawaySet == null)
 			this.giveawaySet = new HashSet<Key>();
 			
 		return giveawaySet;
 	}
 
 	public void setGiveaways(Set<Key> giveaways) {
 		this.giveawaySet = giveaways;
 	}
 
 	public boolean addGiveaway(Key ga) {
 		getGiveaways();
 		
 		return this.giveawaySet.add(ga);
 
 	}
 	
 	public boolean removeGiveaway(Key ga) {
 		getGiveaways();
 		
 		return this.giveawaySet.remove(ga);
 	}
 
 	public Set<Key> getEntries() {
 		if(entrySet == null)
 			entrySet = new HashSet<Key>();
 		
 		return entrySet;
 	}
 
 	public void setEntries(Set<Key> entries) {
 		this.entrySet = entries;
 	}
 	
 	public boolean addEntry(Key k) {
 		getEntries();
 		
 		return this.entrySet.add(k);
 
 	}
 	
 	public boolean removeEntry(Key k) {
 		getEntries();
 		
 		return this.entrySet.remove(k);
 	}
 
 	public Set<Key> getWon() {
 		if(wonSet == null)
 			wonSet = new HashSet<Key>();
 			
 		return wonSet;
 	}
 
 	public void setWon(Set<Key> won) {
 		this.wonSet = won;
 	}
 	
 	public boolean addWon(Key k) {
 		getWon();
 		
 		return this.wonSet.add(k);
 
 	}
 	
 	public boolean removeWon(Key k) {
 		getWon();
 		
 		return this.wonSet.remove(k);
 
 	}
 	
 	public Map<String,String> getProfiles() {
		if(profiles == null)
			this.profiles = new HashMap<String, String>();
 		
		return profiles;
 	}
 	
 	public void addProfile(String key, String link) {
 		getProfiles();
 		
		this.profiles.put(key, link);
 	}
 	
 	
 	public boolean isBanned() {
 		return banned;
 	}
 
 
 	public void setBanned(boolean banned) {
 		this.banned = banned;
 	}
 
 	@Override
 	public String toString() {
 		return "CpcUser [key=" + key + ", id=" + id + ", guser=" + guser
 				+ ", cpcProfileId=" + cpcProfileId + ", cpcNickname="
 				+ cpcNickname + "]";
 	}
 
 }
