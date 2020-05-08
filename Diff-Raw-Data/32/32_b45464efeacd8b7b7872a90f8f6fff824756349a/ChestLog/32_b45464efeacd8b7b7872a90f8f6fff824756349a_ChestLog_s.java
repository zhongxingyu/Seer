 package info.bytecraft.api;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 
 public class ChestLog
 {
 
     private int id;
     private String playerName;
 
     private int x;
     private int y;
     private int z;
     private String world;
    
     private Action action;
     
     public enum Action{
         OPEN, TAKE, DEPOSIT;
     };
 
     public ChestLog(String playerName, Location loc, Action action)
     {
         this.setPlayerName(playerName);
         this.setX(loc.getBlockX());
         this.setY(loc.getBlockY());
         this.setZ(loc.getBlockZ());
         this.setWorld(loc.getWorld().getName());
         
         this.setAction(action);
     }
     
     public Location getLocation()
     {
         return new Location(Bukkit.getWorld(world), x, y, z);
     }
 
     public int getId()
     {
         return id;
     }
 
     public void setId(int id)
     {
         this.id = id;
     }
 
     public String getPlayerName()
     {
         return playerName;
     }
 
     public void setPlayerName(String playerName)
     {
         this.playerName = playerName;
     }
 
     public int getX()
     {
         return x;
     }
 
     public void setX(int x)
     {
         this.x = x;
     }
 
     public int getY()
     {
         return y;
     }
 
     public void setY(int y)
     {
         this.y = y;
     }
 
     public int getZ()
     {
         return z;
     }
 
     public void setZ(int z)
     {
         this.z = z;
     }
 
     public String getWorld()
     {
         return world;
     }
 
     public void setWorld(String world)
     {
         this.world = world;
     }
 
     public Action getAction()
     {
         return action;
     }
 
     public void setAction(Action action)
     {
         this.action = action;
     }
 }
