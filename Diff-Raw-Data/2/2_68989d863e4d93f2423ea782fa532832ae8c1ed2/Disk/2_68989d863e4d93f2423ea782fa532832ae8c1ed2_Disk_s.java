 package eu.stratuslab.storage.persistence;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.MapKey;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Query;
 
 import org.simpleframework.xml.ElementMap;
 
 import eu.stratuslab.storage.disk.resources.BaseResource.DiskVisibility;
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 import eu.stratuslab.storage.disk.utils.MiscUtils;
 
 @Entity
 @SuppressWarnings("serial")
 @NamedQueries({
 		@NamedQuery(name = "allDisks", query = "SELECT NEW eu.stratuslab.storage.persistence.DiskView(d.uuid, d.tag, d.size, d.usersCount, d.owner, d.quarantine, d.identifier) FROM Disk d ORDER BY d.creation DESC"),
 		@NamedQuery(name = "allDisksByUser", query = "SELECT NEW eu.stratuslab.storage.persistence.DiskView(d.uuid, d.tag, d.size, d.usersCount, d.owner, d.quarantine, d.identifier) FROM Disk d WHERE d.owner = :user ORDER BY d.creation DESC"),
 		@NamedQuery(name = "allDisksByIdentifier", query = "SELECT NEW eu.stratuslab.storage.persistence.DiskView(d.uuid, d.tag, d.size, d.usersCount, d.owner, d.quarantine, d.identifier) FROM Disk d WHERE d.identifier = :identifier ORDER BY d.creation DESC") })
 public class Disk implements Serializable {
 
 	public enum DiskType {
 		/**
 		 * seed/cache of machine image seed (managed by Marketplace)
 		 */
 		MACHINE_IMAGE_ORIGINE,
 		/**
 		 * snapshot / cow of machine image (managed by Marketplace)
 		 */
 		MACHINE_IMAGE_LIVE,
 		/**
 		 * seed/cache of data image seed (managed by Marketplace)
 		 */
 		DATA_IMAGE_ORIGINE,
 		/**
 		 * snapshot / cow of machine image (managed by Marketplace)
 		 */
 		DATA_IMAGE_LIVE,
 		/**
 		 * simple read only data (raw data managed by user)
 		 */
 		DATA_IMAGE_RAW_READONLY,
 		/**
 		 * simple read/write data (raw data managed by user)
 		 */
 		DATA_IMAGE_RAW_READ_WRITE
 	}
 	
 	public static final String STATIC_DISK_TARGET = "static";
     public static final String UUID_KEY = "uuid";
     public static final String DISK_VISIBILITY_KEY = "visibility";
 	public static final String DISK_SIZE_KEY = "size";
 	public static final String DISK_IDENTIFER_KEY = "Marketplace_id";
 
 	public static Disk load(String uuid) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Disk disk = em.find(Disk.class, uuid);
 		em.close();
 		return disk;
 	}
 
 	public Disk store() {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		EntityTransaction transaction = em.getTransaction();
 		transaction.begin();
 		Disk obj = em.merge(this);
 		transaction.commit();
 		em.close();
 		return obj;
 	}
 
 	public void remove() {
 		remove(getUuid());
 	}
 
 	public static void remove(String uuid) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		EntityTransaction transaction = em.getTransaction();
 		transaction.begin();
 		Disk fromDb = em.find(Disk.class, uuid);
 		if (fromDb != null) {
 			em.remove(fromDb);
 		}
 		transaction.commit();
 		em.close();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<DiskView> listAll() {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Query q = em.createNamedQuery("allDisks");
 		List<DiskView> list = q.getResultList();
 		em.close();
 		return list;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<DiskView> listAllByUser(String user) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Query q = em.createNamedQuery("allDisksByUser");
 		q.setParameter("user", user);
 		List<DiskView> list = q.getResultList();
 		em.close();
 		return list;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static boolean identifierExists(String identifier) {
 		if("".equals(identifier)) {
 			return false;
 		}
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Query q = em.createNamedQuery("allDisksByIdentifier");
 		q.setParameter("identifier", identifier);
 		List<DiskView> list = q.getResultList();
 		return list.size() > 0;
 	}
 
 	public Disk() {
 		uuid = DiskUtils.generateUUID();
 	}
 
 	@Id
 	private String uuid;
 
 	private String owner = "";
 	private ArrayList<String> group_ = new ArrayList<String>();
 	private DiskVisibility visibility = DiskVisibility.PRIVATE;
 
 	private String creation = MiscUtils.getTimestamp();
 	private String deletion = ""; // deleted timestamp
 
 	private int usersCount = 0;
 	
 	private String tag = "";
 	private long size = -1;
 	private String quarantine = ""; // quarantine start date
 
 	private String identifier = ""; // Marketplace identifier
 	
 	private String homeUrl = ""; // Marketplace url
 
 	private boolean seed = false; // original... don't delete!
 	
 	private String baseDiskUuid;
 	
 	private DiskType type = DiskType.DATA_IMAGE_RAW_READ_WRITE;
 	
 	@MapKey(name = "id")
 	@OneToMany(mappedBy = "disk", fetch=FetchType.EAGER)
 	@ElementMap(name = "mounts", required = false, data = true, valueType = Mount.class)
 	private Map<String, Mount> mounts = new HashMap<String, Mount>(); // key is vmId
 
 	public String getUuid() {
 		return uuid;
 	}
 
 	public void setUuid(String uuid) {
		this.uuid = (uuid == null ? uuid : uuid.trim());
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public DiskVisibility getVisibility() {
 		return visibility;
 	}
 
 	public void setVisibility(DiskVisibility visibility) {
 		this.visibility = visibility;
 	}
 
 	public String getCreation() {
 		return creation;
 	}
 
 	public int getUsersCount() {
 		return usersCount;
 	}
 
 	public void setUsersCount(int count) {
 		this.usersCount = count;
 	}
 
 	public String getTag() {
 		return tag;
 	}
 
 	public void setTag(String tag) {
 		this.tag = tag;
 	}
 
 	/**
 	 * In GB
 	 */
 	public long getSize() {
 		return size;
 	}
 
 	/**
 	 * @param size in GB
 	 */
 	public void setSize(long size) {
 		this.size = size;
 	}
 
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	public void setIdentifier(String identifier) {
 		this.identifier = identifier;
 	}
 
 	public int incrementUserCount() {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		EntityTransaction transaction = em.getTransaction();
 		transaction.begin();
 		Disk disk = em.merge(this);
 		disk.setUsersCount(disk.getUsersCount() + 1);
 		int count = disk.getUsersCount();
 		transaction.commit();
 		em.close();
 		return count;
 	}
 
 	public int decrementUserCount() {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		EntityTransaction transaction = em.getTransaction();
 		transaction.begin();
 		Disk disk = em.merge(this);
 		disk.setUsersCount(disk.getUsersCount() - 1);
 		int count = disk.getUsersCount();
 		transaction.commit();
 		em.close();
 		return count;
 	}
 
 	public void setBaseDiskUuid(String uuid) {
 		this.baseDiskUuid = uuid;
 	}
 
 	public String getBaseDiskUuid() {
 		return baseDiskUuid;
 	}
 
 	public int getMountsCount() {
 		return mounts.size();
 	}
 
 	public String diskTarget(String mountId) {
 
 		if (mounts.containsKey(mountId)) {
 			return STATIC_DISK_TARGET;
 		}
 
 		return mounts.get(mountId).getDevice();
 	}
 
 	public Map<String, Mount> getMounts() {
 		return mounts;
 	}
 
 	public String getQuarantine() {
 		return quarantine;
 	}
 
 	public void setQuarantine(String quarantineStartDate) {
 		this.quarantine = quarantineStartDate;
 	}
 
 	public void setSeed(boolean isSeed) {
 		this.seed = isSeed;
 	}
 
 	public boolean isSeed() {
 		return seed;
 	}
 
 	public String getHomeUrl() {
 		return homeUrl;
 	}
 
 	public void setHomeUrl(String homeUrl) {
 		this.homeUrl = homeUrl;
 	}
 
 	public String getDeletion() {
 		return deletion;
 	}
 
 	public void setDeletion(String deletion) {
 		this.deletion = deletion;
 	}
 
 	public void setType(DiskType type) {
 		this.type = type;
 	}
 
 	public DiskType getType() {
 		return type;
 	}
 
 	public String getGroup() {
 		StringBuffer group = new StringBuffer();
 		for(String user : this.group_) {
 			group.append(user + ", ");
 		}
 		return group.toString();
 	}
 
 	public void addGroupShare(String username) {
 		group_.add(username);
 	}
 
 	public boolean groupContainsUser(String username) {
 		return group_.contains(username);
 	}
 
 	public void setGroup(String group) {
 		ArrayList<String> list = new ArrayList<String>();
 		for(String user : group.split(",")) {
 			String trimmed = user.trim();
 			if(!"".equals(trimmed)) {
 				list.add(user.trim());
 			}
 		}
 		group_ = list;
 	}
 
 }
