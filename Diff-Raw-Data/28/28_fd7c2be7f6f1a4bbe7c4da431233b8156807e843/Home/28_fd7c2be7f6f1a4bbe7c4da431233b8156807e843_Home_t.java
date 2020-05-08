 package uk.co.oliwali.MultiHome;
 
 import com.avaje.ebean.validation.NotNull;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 @Entity()
 @Table(name="multihome")
 public class Home {
 	
     @Id
     private int id;
     @NotNull
     private String player;
     @NotNull
     private String world;
     @NotNull
     private double x;
     @NotNull
     private double y;
     @NotNull
     private double z;
 	@NotNull
     private float pitch;
     @NotNull
     private float yaw;
     
     public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(String player) {
 		this.player = player;
 	}
 
 	public String getWorld() {
 		return world;
 	}
 
 	public void setWorld(String world) {
 		this.world = world;
 	}
 
 	public double getX() {
 		return x;
 	}
 
 	public void setX(double x) {
 		this.x = x;
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public void setY(double y) {
 		this.y = y;
 	}
 
 	public double getZ() {
 		return z;
 	}
 
 	public void setZ(double z) {
 		this.z = z;
 	}
 
 	public float getYaw() {
 		return yaw;
 	}
 
 	public void setYaw(float yaw) {
 		this.yaw = yaw;
 	}
 	
	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
     public void setLocation(Location location) {
         this.world = location.getWorld().getName();
         this.x = location.getX();
         this.y = location.getY();
         this.z = location.getZ();
         this.pitch = location.getPitch();
         this.yaw = location.getYaw();
     }
     
     public Location getLocation() {
         World world = Bukkit.getServer().getWorld(this.world);
         return new Location(world, x, y, z, yaw, pitch);
     }
 
 }
