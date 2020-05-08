 package mrkirby153.MscHouses.block;
 
 import java.util.Random;
 
 import mrkirby153.MscHouses.block.tileEntity.TileEntityHouseGen;
 import mrkirby153.MscHouses.core.MscHouses;
import mrkirby153.MscHouses.items.ItemBlockGenerator;
 import mrkirby153.MscHouses.lib.Strings;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 /**
  * 
  * Msc Houses
  *
  * BlockHouse_Base
  *
  * @author mrkirby153
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class BlockHouseGenerator extends BlockContainer{
 
 	public BlockHouseGenerator(int par1) {
 		super(par1, Material.cloth);
 		this.setCreativeTab(MscHouses.tabHouse);
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z,
 			EntityPlayer player, int idk, float what, float these, float are) {
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if (tileEntity == null || player.isSneaking()) {
 			return false;
 		}
 		player.openGui(MscHouses.instance, 0, world, x, y, z);
 		return true;
 	}
 	
 	@Override
 	public String getUnlocalizedName() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("tile.");
 		sb.append(Strings.RESOURCE_PREFIX);
 		sb.append(Strings.BLOCK_HOUSE_GEN_NAME);
 		
 		return sb.toString();
 	}
 
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
 		dropItems(world, x, y, z);
 		super.breakBlock(world, x, y, z, par5, par6);
 	}
 
 	private void dropItems(World world, int x, int y, int z){
 		Random rand = new Random();
 
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if (!(tileEntity instanceof IInventory)) {
 			return;
 		}
 		IInventory inventory = (IInventory) tileEntity;
 
 		for (int i = 0; i < inventory.getSizeInventory(); i++) {
 			ItemStack item = inventory.getStackInSlot(i);
 
 			if (item != null && item.stackSize > 0) {
 				float rx = rand.nextFloat() * 0.8F + 0.1F;
 				float ry = rand.nextFloat() * 0.8F + 0.1F;
 				float rz = rand.nextFloat() * 0.8F + 0.1F;
 
 				EntityItem entityItem = new EntityItem(world,
 						x + rx, y + ry, z + rz,
 						new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));
 
 				if (item.hasTagCompound()) {
 					entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
 				}
 
 				float factor = 0.05F;
 				entityItem.motionX = rand.nextGaussian() * factor;
 				entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
 				entityItem.motionZ = rand.nextGaussian() * factor;
 				world.spawnEntityInWorld(entityItem);
 				item.stackSize = 0;
 			}
 		}
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityHouseGen();
 	}
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		blockIcon = iconRegister.registerIcon("mschouses:houseBase");
 	}
 }
