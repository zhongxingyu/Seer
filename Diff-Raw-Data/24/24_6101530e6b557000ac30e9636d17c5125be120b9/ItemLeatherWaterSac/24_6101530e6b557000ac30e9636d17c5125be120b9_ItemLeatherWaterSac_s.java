 package emris.mods.LeatherWaterSacTFC;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import TFC.Core.Player.TFC_PlayerServer;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemLeatherWaterSac extends Item {
 
 	public static int itemID;
 
 	public ItemLeatherWaterSac(int par1) {
 		super(par1);
 		
 		maxStackSize = 1;
 		setCreativeTab(CreativeTabs.tabMisc);
 		setIconIndex(0);
 		setItemName("itemLeatherWaterSac");
 		setMaxDamage(12);
 		hasSubtypes = false;
 		itemID = this.shiftedIndex;
 	}
 	
 	@Override
 	public String getTextureFile() {
 		return CommonProxy.ITEMS_PNG;
 	}
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World,	EntityPlayer par3EntityPlayer) {
 		MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(par2World, par3EntityPlayer, true);
 		if (mop == null) {
 			if(par1ItemStack.getItemDamage() == par1ItemStack.getMaxDamage()){
 				if(par3EntityPlayer instanceof EntityPlayerMP) { par3EntityPlayer.sendChatToPlayer("Empty!"); }
 			} else {
 				par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
 			}
 
 			return par1ItemStack;
 		} else {
 			if(mop.typeOfHit == EnumMovingObjectType.TILE) {
 				int x = mop.blockX;
 				int y = mop.blockY;
 				int z = mop.blockZ;
 				
 				if(!par2World.canMineBlock(par3EntityPlayer, x,  y,  z)) {
 					return par1ItemStack;
 				}
 				
 				if(!par3EntityPlayer.canPlayerEdit(x, y, z, mop.sideHit, par1ItemStack)) {
 					return par1ItemStack;
 				}
 				
 				if(par2World.getBlockMaterial(x, y, z) == Material.water) {
 					if(par1ItemStack.getItemDamage() > 0) {
 						par1ItemStack.setItemDamage(par1ItemStack.getItemDamage() - 1);
 					} else {
 						par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
 					}
 				} else {
 					if(par1ItemStack.getItemDamage() == par1ItemStack.getMaxDamage()){
 						if(par3EntityPlayer instanceof EntityPlayerMP) { par3EntityPlayer.sendChatToPlayer("Empty!"); }
 					} else {
 						par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
 					}
 				}
 			}
 			return par1ItemStack;
 		}
 	}
 
 	@Override
 	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
 		return EnumAction.drink;
 	}
 
 	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
         return 32;
     }
     
 	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
         return false;
     }
     
 	@Override
 	public ItemStack onFoodEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
 		if(!par2World.isRemote) {
 			if(par1ItemStack.getItemDamage() != par1ItemStack.getMaxDamage()) {
				par1ItemStack.setItemDamage(par1ItemStack.getItemDamage() + 1);
 				if(par3EntityPlayer instanceof EntityPlayerMP) {
 					EntityPlayerMP p = (EntityPlayerMP)par3EntityPlayer;
					TFC_PlayerServer.getFromEntityPlayer(par3EntityPlayer).getFoodStatsTFC().restoreWater(p, 2400);
 				}
 			}
 		}
 		return par1ItemStack;
 	}
 
 	@SideOnly(Side.CLIENT)
     public boolean requiresMultipleRenderPasses() {
         return true;
     }
 	
 	
 	
 }
