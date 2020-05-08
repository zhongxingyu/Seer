 package net.sqdmc.bubbleshield;
 
 import com.massivecraft.factions.Faction;
 import com.massivecraft.factions.Factions;
 
 import net.sqdmc.bubbleshield.ShieldOwner;
 
 public class ShieldOwnerFaction extends ShieldOwner {
 
 	@Override
 	public Faction getFaction() {
 		return shieldOwner;
 	}
 	
 	@Override
 	public String getId() {
 		// TODO Auto-generated method stub
 		return shieldOwner.getId();
 	}
 	
 	public void setFaction(String Id){
 		Faction faction = Factions.i.get(Id);
 		
 		this.shieldOwner = faction;
 	}
 
 	@Override
 	public void sendMessage(String message) {
 		shieldOwner.sendMessage(message);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((shieldOwner == null) ? 0 : shieldOwner.getId().hashCode());
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
         ShieldOwnerFaction other = (ShieldOwnerFaction) obj;
         return shieldOwner.getId().equals(other.shieldOwner.getId());
     }
 
 	public Faction shieldOwner;
 	
 	public ShieldOwnerFaction(Faction factionId) {
 		this.shieldOwner = factionId;
 	}
 	
 	@Override
 	public String toString() {
		return "shieldOwner";
 	}
 }
