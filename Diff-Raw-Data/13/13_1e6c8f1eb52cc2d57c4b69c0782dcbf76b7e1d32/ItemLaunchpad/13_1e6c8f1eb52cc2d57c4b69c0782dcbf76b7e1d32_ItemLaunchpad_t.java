 package spazzysmod.item;
 
 import spazzysmod.SpazzysmodBase;
 import spazzysmod.blocks.SpazzysBlocks;
 import spazzysmod.creativetab.SpazzysTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ItemLaunchpad extends Item {
 
 	public ItemLaunchpad(int par1) {
 		super ( par1 );
 		this.setCreativeTab ( SpazzysTabs.tabSolarSystem );
 		this.setMaxDamage ( 8 );
		this.setMaxStackSize ( 1 );
		this.func_111206_d ( SpazzysmodBase.MODID + ":launchPad" );
 	}
 
 	@Override
 	public boolean onItemUse ( ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10 ) {
 		if ( par3World.getBlockId ( par4, par5, par6 ) == SpazzysBlocks.launchPad.blockID ) {
			this.setDamage ( par1ItemStack, par1ItemStack.getItemDamage() - 1 );
			par3World.setBlockToAir ( par4, par5, par6 );
 
			return true;
 		} else {
 			par3World.setBlock ( par4, par5 + 1, par6, SpazzysBlocks.launchPad.blockID );
 			par1ItemStack.damageItem ( 1, par2EntityPlayer );
 
 			return true;
 		}
 	}
 }
