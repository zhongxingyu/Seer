 package edgewalker.minecraftessentials.items;
 
 
 import java.util.List;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class ItemIronOre extends Item 
 {
 	public ItemIronOre(int itemId) 
 	{
 		super(itemId);		
 		this.maxStackSize = 64;
 		this.setCreativeTab(CreativeTabs.tabMaterials);
 		this.setUnlocalizedName("Iron Ore");
 	}
 		
 	public void registerIcons(IconRegister iconRegister)
 	{
 		itemIcon = iconRegister.registerIcon("MinecraftEssentials:oreIron");
 	}
 }
