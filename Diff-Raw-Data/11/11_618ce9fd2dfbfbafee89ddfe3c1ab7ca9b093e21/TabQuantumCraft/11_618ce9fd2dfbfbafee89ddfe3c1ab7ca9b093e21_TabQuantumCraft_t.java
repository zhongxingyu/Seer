 package sammko.quantumCraft.core;
 
 import sammko.quantumCraft.items.ItemInitializator;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 public class TabQuantumCraft extends CreativeTabs {
 
 	public TabQuantumCraft() {
 		super(CreativeTabs.getNextID(), "QuantumCraft");
	}
	
	public String getTranslatedTabLabel(){
		return "Quantum Craft"; 				// That fixes the ingame name on our tab ^^
 	}
 
 	@Override
 	public String getTabLabel()
     {
         return "Quantum Craft";
     }
 	@Override
 	public Item getTabIconItem()
     {
 	    return (Item)ItemInitializator.ItemGammatroniumCrystal;
     }
 
 }
