 package sim;
 
 import sim.storage.util.ReplicaLevel;
 
 public class Block {
 
	public final static int BLOCK_SIZE = 4096;
 	public final static Block NULL = new Block(Long.MIN_VALUE, null, Double.MIN_VALUE, Integer.MIN_VALUE);
 
 	private long id;
 	private ReplicaLevel repLevel;
 	private double accessTime;
 	private int ownerDiskId;
 	private int primaryDiskId;
 
 
 	/**
 	 * TODO this constructor may be delete in the future.
 	 */
 	public Block(long id, ReplicaLevel repLevel, double accessTime, int ownerDiskId) {
 		this.id = id;
 		this.repLevel = repLevel;
 		this.accessTime = accessTime;
 		this.ownerDiskId = ownerDiskId;
 	}
 
 	public Block(long id, double accessTime, int primaryDiskId) {
 		this.id = id;
 		this.accessTime = accessTime;
 		this.primaryDiskId = primaryDiskId;
 	}
 
 	public double getAccessTime() {
 		return accessTime;
 	}
 
 	public void setAccessTime(double accessTime) {
 		this.accessTime = accessTime;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public ReplicaLevel getRepLevel() {
 		return repLevel;
 	}
 
 	public void setRepLevel(ReplicaLevel repLevel) {
 		this.repLevel = repLevel;
 	}
 
 	public int getOwnerDiskId() {
 		return ownerDiskId;
 	}
 
 	public void setOwnerDiskId(int ownerDiskId) {
 		this.ownerDiskId = ownerDiskId;
 	}
 
 	public int getPrimaryDiskId() {
 		return primaryDiskId;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == this) return true;
 		if (!(obj instanceof Block)) return false;
 		Block target = (Block)obj;
 		return  target.getId() == this.getId();
 	}
 
 	@Override
 	public int hashCode() {
 		return (int)this.getId();
 	}
 }
