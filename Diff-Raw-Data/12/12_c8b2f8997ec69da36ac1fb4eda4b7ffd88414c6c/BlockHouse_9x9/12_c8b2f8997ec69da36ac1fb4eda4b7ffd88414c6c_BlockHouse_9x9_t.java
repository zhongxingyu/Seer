 package mrkirby153.MscHouses;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class BlockHouse_9x9 extends Block{
 
 
 	
 	public BlockHouse_9x9(int par1, int par2) {
 		super(par1, par2, Material.ground);
 		this.setCreativeTab(MscHouses.tabHouse);
 		this.setHardness(1F);
 		this.setResistance(0F);
 		GameRegistry.registerBlock(this, "Block9x9");
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
 		MscHouses.h.ninebynine(par1World, par2, par3, par4);
		System.out.println("Blah Blah Blah");
		if(par5EntityPlayer != null  && par5EntityPlayer.getItemInUse() != null && par5EntityPlayer.getItemInUse().itemID == MscHouses.HouseTool.itemID){
			ItemStack item = par5EntityPlayer.getItemInUse();
		//	item.damageItem(1, par5EntityPlayer);
			item.setItemDamage(item.getItemDamage() +1);
		}
		if(!par1World.isRemote){
			par5EntityPlayer.sendChatToPlayer("Please install your door and bed. Found in the chest.");
		}
 		return true;
 	}
 
 }
