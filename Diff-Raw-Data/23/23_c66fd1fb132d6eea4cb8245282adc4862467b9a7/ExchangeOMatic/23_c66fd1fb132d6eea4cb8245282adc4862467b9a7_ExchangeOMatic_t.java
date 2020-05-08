 package assets.tacotek.Items;
 
 import java.util.Arrays;
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import assets.tacotek.common.tacotek;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ExchangeOMatic extends GenericElectric {
 
 	private static int convertID = net.minecraft.block.Block.stone.blockID;
 	private static int useEnergy = 100;
 	private static final Integer[] canChange = {1, 2, 3, 4, 12, 13, 24, 48, 60, 80, 82, 87, 88, 110, 159, 170};
 	private static final Integer[] expensive = {12, 24, 80, 82, 87, 110, 159, 170};
 	
 	public ExchangeOMatic(int id) {
 		super(id, 31337, 2, 100);
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg) {
 		this.itemIcon = reg.registerIcon(tacotek.modID + ":" + this.getUnlocalizedName() );
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void addInformation(ItemStack itemStack, EntityPlayer player, List dataList, boolean bool) {
		dataList.add("Changes Block Types");
 	}
 	
 	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10) {
 		boolean isServer = player instanceof EntityPlayerMP;
 		int blockClicked = world.getBlockId(x, y, z);
 		
 		if (!canTakeDamage(stack, useEnergy)) {
 			if (!isServer)
 				player.addChatMessage("8Not Enough Power!!");
 			return false;
 		}
 		if (player.isSneaking()) {
 			if (convertID != net.minecraft.block.Block.stone.blockID && blockClicked == net.minecraft.block.Block.stone.blockID) {
 				convertID = net.minecraft.block.Block.stone.blockID;
 				if (!isServer)
 					player.addChatMessage("8Exchange O' Matic Now in Stone mode!");
 			}
 			else if (convertID != net.minecraft.block.Block.dirt.blockID && blockClicked == net.minecraft.block.Block.dirt.blockID) {
 				convertID = net.minecraft.block.Block.dirt.blockID;
 				if (!isServer)
 					player.addChatMessage("8Exchange O' Matic Now in Dirt mode!");
 			}
 			return false;
 		}
 		
 		//If you clicked same block or block type not allowed, block
 		if (blockClicked == convertID || !Arrays.asList(canChange).contains(blockClicked)) {
 			return false;
 		}
 		
 		//If an exotic type, use more power.
 		if (Arrays.asList(expensive).contains(blockClicked)) {
 			useEnergy = 1000;
 		} else useEnergy = 100;
 		
 		//Assuming player can edit that block, EDIT IT!
 		if (player.canPlayerEdit(x, y, z, side, stack)) {
 			damage(stack, useEnergy, player);
 			world.setBlock(x, y, z, convertID);
 			world.notifyBlockOfNeighborChange(x, y, z, convertID);
 			return true;
 		}
 		else return false;
 	}
 }
