 package elfville.server.model;
 
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.util.Date;
 
 import elfville.server.Database;
 import elfville.server.SecurityUtils;
 
 public abstract class Model implements Serializable {
 	private static final long serialVersionUID = -3671088963465928601L;
 
 	protected static Database database = Database.getInstance();
 
 	private final Date createdAt;
 	protected final int modelID;
 	private int checksum;
 
 	public Model() {
 		modelID = database.getAndIncrementCountID();
 		createdAt = new Date();
 	}
 
 	/**
 	 * Construct a checksum using reflection.
 	 * 
 	 * @return an XOR of all this object's field hash codes
 	 */
 	public final int getChecksum() {
 		Field[] fields = this.getClass().getDeclaredFields();
 		int c = 0;
 		for (Field f : fields) {
			try {
				c = c ^ f.get(this).hashCode();
			} catch (IllegalAccessException e) {
				// ignore it...
			}
 		}
 		return c;
 	}
 
 	/**
 	 * Use this to enforce database integrity. If you load an object from
 	 * storage and this returns true, the object is corrupted. If you modify an
 	 * object this will return true if it has not been persisted yet.
 	 * 
 	 * @return whether this object is corrupted / hasn't been saved yet
 	 */
 	public final boolean isDirty() {
 		return checksum != getChecksum();
 	}
 
 	public enum ClanElfRelationship {
 		APPLICANT, MEMBER, LEADER
 	}
 
 	public int getModelID() {
 		return modelID;
 	}
 
 	public String getEncryptedModelID() {
 		return SecurityUtils.encryptIntToString(getModelID());
 	}
 
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	/**
 	 * Write a copy of this object to the output stream. All setters should run
 	 * this to persist changes. Controllers should only need to call this method
 	 * explicitly if you have created a new object!
 	 */
 	public void save() {
 		checksum = getChecksum();
 		database.persist(this);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return (obj instanceof Model) && ((Model) obj).modelID == modelID;
 	}
 
 	@Override
 	public int hashCode() {
 		return modelID;
 	}
 
 }
