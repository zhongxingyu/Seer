 package en.trudan.ConstructionSites.Data;
 
 import java.io.Serializable;
 
import org.bukkit.inventory.ItemStack;

 public class ConstructionSite implements Serializable{
 
 	private static final long serialVersionUID = -2514406717532111827L;
 	private static MaterialStorage ms;
 	
 	public ConstructionSite(){
 		
 		ms = new MaterialStorage();
 		
 	}
 	
 	public MaterialStorage getMaterialStorage(){
 		return ms;
 	}
 
 }
