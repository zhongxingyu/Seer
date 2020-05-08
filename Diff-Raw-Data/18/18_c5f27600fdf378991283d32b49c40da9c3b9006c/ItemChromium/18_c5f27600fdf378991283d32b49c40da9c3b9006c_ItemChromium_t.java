 package edgewalker.minecraftessentials.items;
 
 
import java.util.List;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
 
 public class ItemChromium extends Item 
 {
 	public ItemChromium(int itemId) 
 	{
 		super(itemId);		
 		this.maxStackSize = 64;
 		this.setCreativeTab(CreativeTabs.tabMaterials);
 		this.setUnlocalizedName("Chromium");
 	}
 		
 	public void registerIcons(IconRegister iconRegister)
 	{
 		itemIcon = iconRegister.registerIcon("MinecraftEssentials:chromium");
 	}
 	
 	// Adds text to the item tooltip
  
  public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
   {
      par3List.add("Mix with iron and charcoal to make steel");
   }
 }
