 package sammko.quantumCraft.core;
 
 import sammko.quantumCraft.items.ItemInitializator;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 public class TabQuantumCraft extends CreativeTabs {
 
 	public TabQuantumCraft() {
 		super(CreativeTabs.getNextID(), "QuantumCraft");
		//FIXME: tooltip in creative GUI is not correct.
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
