 package cc.thedudeguy.xpinthejar.databases;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import com.avaje.ebean.validation.NotEmpty;
 import com.avaje.ebean.validation.NotNull;
 
 @Entity()
@Table(name="xp_bank")
 public class Bank {
 
 	@Id
 	private int id;
 	
 	@NotNull
     private int x;
 
     @NotNull
     private int y;
 
     @NotNull
     private int z;
 
     @NotEmpty
     private String worldName;
     
     @NotNull
     private int xp;
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public int getZ() {
 		return z;
 	}
 
 	public void setZ(int z) {
 		this.z = z;
 	}
 
 	public String getWorldName() {
 		return worldName;
 	}
 
 	public void setWorldName(String worldName) {
 		this.worldName = worldName;
 	}
 
 	public int getXp() {
 		return xp;
 	}
 
 	public void setXp(int xp) {
 		this.xp = xp;
 	}
 	
 	public void add(int amount) {
 		setXp(getXp() + amount);
 	}
 	
 	public void deduct(int amount) {
 		setXp(getXp() - amount);
 	}
 }
