 package gwydion0917.gwycraft;
 
 
 import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
 
 public class GwycraftTab extends CreativeTabs
 {
	public GwycraftTab(String label)
	{
		super(label);
	}
	
	@Override
	public Item getTabIconItem()
	{
		return Gwycraft.itemFlawedGems;
	}	
 }
