 package nz.ac.victoria.ecs.kpsmart.entities.state;
 
 import java.io.Serializable;
 
 import javax.persistence.Embeddable;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @Entity
 public final class CustomerPrice extends StorageEntity implements Serializable, Price {
 	private static final long serialVersionUID = 1L;
 
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long uniqueLongID;
	
 	@Cascade(CascadeType.ALL)
 	@OneToOne
 	private CustomerPriceID uid;
 	
 	private CustomerPricePK primaryKey = new CustomerPricePK();
 	
 	private float pricePerUnitWeight;
 	
 	private float pricePerUnitVolume;
 	
 	@Override
 	public boolean isNonUnique(StorageEntity entity) {
 		if (!(entity instanceof CustomerPrice))
 			return false;
 		CustomerPrice p = (CustomerPrice) entity;
 		
 		return  this.primaryKey.location.equals(p.primaryKey.location) &&
 				this.primaryKey.direction == p.primaryKey.direction &&
 				this.primaryKey.priority == p.primaryKey.priority;
 	}
 	
 	@Embeddable
 	public static final class CustomerPricePK implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		@ManyToOne
 		private Location location;
 		
 		@Enumerated(EnumType.STRING)
 		private Direction direction = Direction.From;
 		
 		@Enumerated(EnumType.STRING)
 		private Priority priority;
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((direction == null) ? 0 : direction.hashCode());
 			result = prime * result
 					+ ((location == null) ? 0 : location.hashCode());
 			result = prime * result
 					+ ((priority == null) ? 0 : priority.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			CustomerPricePK other = (CustomerPricePK) obj;
 			if (direction != other.direction)
 				return false;
 			if (location == null) {
 				if (other.location != null)
 					return false;
 			} else if (!location.equals(other.location))
 				return false;
 			if (priority != other.priority)
 				return false;
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			return "CustomerPricePK [location=" + location + ", direction="
 					+ direction + ", priority=" + priority + "]";
 		}
 	}
 	
 	public CustomerPrice(Location location, Direction direction, Priority priority) {
 		assert location != null;
 		
 		this.uid = new CustomerPriceID();
 		this.primaryKey.location = location;
 		this.primaryKey.direction = direction;
 		this.primaryKey.priority = priority;
 	}
 	
 	public CustomerPrice(Location startpoint, Location endPoint, Priority priority) {
 		assert startpoint != null && endPoint != null;
 		assert startpoint == null && endPoint == null;
 		
 		this.uid = new CustomerPriceID();
 		this.setStartLocation(startpoint);
 		this.setEndLocation(endPoint);
 		this.setPriority(priority);
 	}
 	
 	CustomerPrice() {
 	}
 	
 	public long getId() {
 		return uid.getId();
 	}
 	
 	public void setId(long id) {
 		uid.setId(id);
 	}
 
 	/**
 	 * May be null to designate new zealand
 	 */
 	public Location getStartLocation() {
 		if (this.getPrimaryKey().direction == Direction.From)
 			return this.getPrimaryKey().location;
 		
 		return null;
 	}
 
 	/**
 	 * May be null to designate new zealand
 	 */
 	public void setStartLocation(Location startLocation) {
 		if (startLocation == null || !startLocation.isInternational())
 			return;
 		
 		this.getPrimaryKey().direction = Direction.From;
 		this.getPrimaryKey().location = startLocation;
 	}
 
 	/**
 	 * May be null to designate new zealand
 	 */
 	public Location getEndLocation() {
 		if (this.getPrimaryKey().direction == Direction.To)
 			return this.getPrimaryKey().location;
 		
 		return null;
 	}
 
 	/**
 	 * May be null to designate new zealand
 	 */
 	public void setEndLocation(Location endLocation) {
 		if (endLocation == null || !endLocation.isInternational())
 			return;
 		
 		this.getPrimaryKey().direction = Direction.To;
 		this.getPrimaryKey().location = endLocation;
 	}
 
 	@Override
 	public Priority getPriority() {
 		return getPrimaryKey().priority;
 	}
 
 	public void setPriority(Priority prority) {
 		this.getPrimaryKey().priority = prority;
 	}
 
 	public CustomerPricePK getPrimaryKey() {
 		return primaryKey;
 	}
 
 	public void setPrimaryKey(CustomerPricePK primaryKey) {
 		this.primaryKey = primaryKey;
 	}
 
 	@Override
 	public float getPricePerUnitWeight() {
 		return pricePerUnitWeight;
 	}
 
 	@Override
 	public void setPricePerUnitWeight(float price) {
 		this.pricePerUnitWeight = price;
 	}
 
 	@Override
 	public float getPricePerUnitVolume() {
 		return pricePerUnitVolume;
 	}
 
 	@Override
 	public void setPricePerUnitVolume(float pricePerUnitVolume) {
 		this.pricePerUnitVolume = pricePerUnitVolume;
 	}
 	
 	@Override
 	@Transient
 	public float getCost(MailDelivery mail) {
 		return Math.max(mail.getVolume() * this.pricePerUnitVolume, mail.getWeight() * this.pricePerUnitWeight);
 	}
 	
 	public Location getLocation() {
 		return primaryKey.location;
 	}
 	
 	public void setLocation(Location l) {
 		primaryKey.location = l;
 	}
 	
 	public Direction getDirection() {
 		return primaryKey.direction;
 	}
 	
 	public void setDirection(Direction d) {
 		primaryKey.direction = d;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + Float.floatToIntBits(pricePerUnitVolume);
 		result = prime * result + Float.floatToIntBits(pricePerUnitWeight);
 		result = prime * result
 				+ ((primaryKey == null) ? 0 : primaryKey.hashCode());
 		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		CustomerPrice other = (CustomerPrice) obj;
 		if (Float.floatToIntBits(pricePerUnitVolume) != Float
 				.floatToIntBits(other.pricePerUnitVolume))
 			return false;
 		if (Float.floatToIntBits(pricePerUnitWeight) != Float
 				.floatToIntBits(other.pricePerUnitWeight))
 			return false;
 		if (primaryKey == null) {
 			if (other.primaryKey != null)
 				return false;
 		} else if (!primaryKey.equals(other.primaryKey))
 			return false;
 		if (uid == null) {
 			if (other.uid != null)
 				return false;
 		} else if (!uid.equals(other.uid))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "CustomerPrice [uid=" + uid + ", primaryKey=" + primaryKey
 				+ ", pricePerUnitWeight=" + pricePerUnitWeight
 				+ ", pricePerUnitVolume=" + pricePerUnitVolume + "]";
 	}

	public long getUniqueLongID() {
		return uniqueLongID;
	}

	public void setUniqueLongID(long uniqueLongID) {
		this.uniqueLongID = uniqueLongID;
	}
 }
