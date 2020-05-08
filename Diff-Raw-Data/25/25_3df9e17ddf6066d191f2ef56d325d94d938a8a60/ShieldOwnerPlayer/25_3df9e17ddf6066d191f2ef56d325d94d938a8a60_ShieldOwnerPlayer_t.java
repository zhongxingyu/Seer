 package net.sqdmc.bubbleshield;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 public class ShieldOwnerPlayer extends ShieldOwner {
 
 	public Player shieldOwner;
 	public OfflinePlayer shieldOwnerOff;
 	
 	public ShieldOwnerPlayer(String name) {
 		Player player = Bukkit.getPlayer(name);
		OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(name);
 		
 		this.shieldOwner = player;
		this.shieldOwnerOff = offPlayer;
 	}
 	
 	public ShieldOwnerPlayer(Player player) {
 		this.shieldOwner = player;
 		this.shieldOwnerOff = player;
 	}
 	
 	@Override
 	public String getOwner() {
		if (this.shieldOwnerOff.isOnline())
			return this.shieldOwner.getName();
		else
			return this.shieldOwnerOff.getName();
 	}
 
 	public void setPlayer(Player player){
 		this.shieldOwner = player;
 		this.shieldOwnerOff = player;
 	}
 
 	@Override
 	public void sendMessage(String message) {
		if (this.shieldOwner != null && this.shieldOwnerOff.isOnline()) {
			this.shieldOwner.sendMessage(message);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((shieldOwner == null) ? 0 : shieldOwner.hashCode());
 		return result;
 	}
 
     @Override
     public boolean equals(Object obj) {
         // TODO Auto-generated method stub
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         ShieldOwnerPlayer other = (ShieldOwnerPlayer) obj;
         return shieldOwner.equals(other.shieldOwner);
     }
 	
 	@Override
 	public String toString() {
		return this.shieldOwner.getName();
 	}
 }
