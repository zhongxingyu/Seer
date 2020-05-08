 package tektor.minecraft.chalith.items;
 
 import java.util.List;
 
 import tektor.minecraft.chalith.ChalithBase;
 import tektor.minecraft.chalith.entity.TrapRuneTileEntity;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityHanging;
 import net.minecraft.entity.item.EntityItemFrame;
 import net.minecraft.entity.item.EntityPainting;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.EnumRarity;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntitySign;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.Direction;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 public class TrapRune extends Item {
 
 	
 	private Icon[] icon = new Icon[2];
 
 	public TrapRune(int par1) {
 		super(par1);
 		this.setMaxDamage(0);
 		this.setHasSubtypes(true);
 		this.setMaxStackSize(1);
 		this.setCreativeTab(CreativeTabs.tabTools);
 		this.setUnlocalizedName("fireTrapRune2");
 	}
 	
 	@Override
 	public Icon getIconFromDamage(int par1)
 	{
 		return icon[par1];
 	}
 	
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		icon[0] = par1IconRegister.registerIcon("chalith:fireTrapRune");
 		icon[1] = par1IconRegister.registerIcon("chalith:iceTrapRune");
 	}
 	
 	@Override
 	public String getUnlocalizedName(ItemStack stack)
 	{
 		switch(stack.getItemDamage())
 		{
 		case 0: return "fireTrapRune";
 		case 1: return "iceTrapRune";
 		default: return "??";
 		}
 		
 	}
 	@Override
  	public String getItemDisplayName(ItemStack par1ItemStack)
  	{
  		switch(par1ItemStack.getItemDamage())
  		{
  		case 0: return "Fire TrapRune";
 		case 1: return "Ice TrapRune";
 		default: return "??";
  		}
  	}
 
 	/**
 	 * Callback for item usage. If the item does something special on right
 	 * clicking, he will have one of those. Return True if something happen and
 	 * false if it don't. This is for ITEMS, not BLOCKS
 	 */
 	public boolean onItemUse(ItemStack par1ItemStack,
 			EntityPlayer par2EntityPlayer, World par3World, int par4, int par5,
 			int par6, int par7, float par8, float par9, float par10) {
 		if (par7 == 0) {
 			return false;
 		} else if (!par3World.getBlockMaterial(par4, par5, par6).isSolid()) {
 			return false;
 		} else {
 			if (par7 == 1) {
 				++par5;
 			}
 
 			if (par7 == 2) {
 				--par6;
 			}
 
 			if (par7 == 3) {
 				++par6;
 			}
 
 			if (par7 == 4) {
 				--par4;
 			}
 
 			if (par7 == 5) {
 				++par4;
 			}
 
 			if (!par2EntityPlayer.canPlayerEdit(par4, par5, par6, par7,
 					par1ItemStack)) {
 				return false;
 			} else if (par3World.isRemote) {
 				return true;
 			} else {
 
 				par3World.setBlock(par4, par5, par6,
 						ChalithBase.fireTrapRune.blockID, par1ItemStack.getItemDamage(), 3);
 
 				--par1ItemStack.stackSize;
 				
 				TrapRuneTileEntity entity = (TrapRuneTileEntity) par3World
 						.getBlockTileEntity(par4, par5, par6);
 				if (entity != null) {;
 					entity.owner = par2EntityPlayer.getEntityName();
 					entity.side = par7;
					//par3World.markBlockForUpdate(par4, par5, par6);
 										
 				}
 				
 			}
 			return true;
 		}
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void getSubItems(int par1, CreativeTabs tab, List subItems) {
 		
 			subItems.add(new ItemStack(this, 1, 0));
 			subItems.add(new ItemStack(this, 1, 1));
 	}
 }
