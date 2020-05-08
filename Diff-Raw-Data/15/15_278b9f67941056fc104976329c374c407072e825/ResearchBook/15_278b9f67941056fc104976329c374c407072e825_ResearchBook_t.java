 package mod.industrialscience.modules.research;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
import mod.industrialscience.IndustrialScience;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
 
 public class ResearchBook extends Item {
 
 	public ResearchBook(int id) {
 		super(id);
 		setMaxStackSize(64);
 		setCreativeTab(CreativeTabs.tabMisc);
 		setUnlocalizedName("Research Book");
 		LanguageRegistry.addName(this, "Research Book");
 
 	}
 	public void func_94581_a(IconRegister iconRegister)
 	{
 	         iconIndex = iconRegister.func_94245_a("book");
 	}
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World,
			EntityPlayer par3EntityPlayer) {
		par3EntityPlayer.openGui(IndustrialScience.instance, 1, par2World, par3EntityPlayer.getPlayerCoordinates().posX, par3EntityPlayer.getPlayerCoordinates().posY, par3EntityPlayer.getPlayerCoordinates().posZ);
		System.out.println(par3EntityPlayer.getEntityName());
		return super.onItemRightClick(par1ItemStack, par2World, par3EntityPlayer);
	}
	
 
 }
