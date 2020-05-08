 package stargazer.minecraft.samples.blockwithdrops;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 public class SampleItem extends Item 
 {
 	public SampleItem(int itemId) 
 	{
		super(itemId);		
 		this.maxStackSize = 64;
 		this.setCreativeTab(CreativeTabs.tabMisc);
 		this.setUnlocalizedName("sampleItem");
 	}
 		
 	public void registerIcons(IconRegister iconRegister)
 	{
 		itemIcon = iconRegister.registerIcon("SampleBreakableBlock:SampleItem");
 	}
 }
