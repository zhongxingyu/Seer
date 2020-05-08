 package stargazer.minecraft.samples.blockwithdrops;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 public class SampleItem extends Item 
 {
  public static final int SAMPLE_ITEM_ID = 4097;
  
 	public SampleItem(int itemId) 
 	{
	  // Item IDs are normally offset up by 256 (to avoid conflicting with base items representing
	  // blocks.)  Since we aren't going to conflict, eliminate the offset.
		super(itemId - 256);
		
 		this.maxStackSize = 64;
 		this.setCreativeTab(CreativeTabs.tabMisc);
 		this.setUnlocalizedName("sampleItem");
 	}
 		
 	public void registerIcons(IconRegister iconRegister)
 	{
 		itemIcon = iconRegister.registerIcon("SampleBreakableBlock:SampleItem");
 	}
 }
