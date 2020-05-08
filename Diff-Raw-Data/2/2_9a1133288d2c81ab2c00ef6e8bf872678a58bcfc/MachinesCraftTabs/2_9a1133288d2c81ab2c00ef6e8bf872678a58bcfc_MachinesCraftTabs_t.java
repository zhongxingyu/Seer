 package machinesCraftCommon;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 
 public class MachinesCraftTabs extends CreativeTabs {
 	
 	public static CreativeTabs machinesCraftTab = new MachinesCraftTabs("Machines Craft");
 	
 	public MachinesCraftTabs(String label) {
 	    super(label);
 	}
 	
 	@Override
 	public ItemStack getIconItemStack()
     {
        return new ItemStack(MachinesCraft.MechanicBlock); //need fix on this line
     }
 	
 }
