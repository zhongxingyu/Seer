 package sbfp.machines;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import sbfp.BlockSub;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 
 public class BlockMachines extends BlockSub{
 	public Icon[][] icons;
	public BlockMachines(int id, Material material, String[] names){
		super(id,material,names);
 		
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register){
 		icons = new Icon[names.length][6];
 		for(int i = 0; i<names.length; i++){
 			for(int j = 0; j<6; j++){
 				this.icons[i][j] = register.registerIcon("sbfp:"+this.names[i]+ForgeDirection.getOrientation(j).toString());
 				//System.out.println("sbfp:"+this.names[i]+ForgeDirection.getOrientation(j).toString());
 			}
 		}
 	}
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
 	{
 		this.dropEntireInventory(world, x, y, z, par5, par6);
 		super.breakBlock(world, x, y, z, par5, par6);
 	}
 
 	/**Drops entire inventory of block
 	 * 
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param par5 Necessary for vanilla minecraft code to not crash, don't know what it's for.
 	 * @param par6 Ditto
 	 */
 	public void dropEntireInventory(World world, int x, int y, int z, int par5, int par6)
 	{
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		
 		if (tileEntity != null)
 		{
 			if (tileEntity instanceof IInventory)
 			{
 				IInventory inventory = (IInventory) tileEntity;
 
 				for (int var6 = 0; var6 < inventory.getSizeInventory(); ++var6)
 				{
 					ItemStack var7 = inventory.getStackInSlot(var6);
 
 					if (var7 != null)
 					{
 						Random random = new Random();
 						float var8 = random.nextFloat() * 0.8F + 0.1F;
 						float var9 = random.nextFloat() * 0.8F + 0.1F;
 						float var10 = random.nextFloat() * 0.8F + 0.1F;
 
 						while (var7.stackSize > 0)
 						{
 							int var11 = random.nextInt(21) + 10;
 							
 							if (var11 > var7.stackSize)
 							{
 								var11 = var7.stackSize;
 							}
 							
 							var7.stackSize -= var11;
 							EntityItem var12 = new EntityItem(world, (x + var8), (y + var9), (z + var10), new ItemStack(var7.itemID, var11, var7.getItemDamage()));
 
 							if (var7.hasTagCompound())
 							{
 								var12.getEntityItem().setTagCompound((NBTTagCompound) var7.getTagCompound().copy());
 							}
 
 							float var13 = 0.05F;
 							var12.motionX = ((float) random.nextGaussian() * var13);
 							var12.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
 							var12.motionZ = ((float) random.nextGaussian() * var13);
 							world.spawnEntityInWorld(var12);
 						}
 					}
 				}
 			}
 		}
 	}
 	@Override
 	public ArrayList<ItemStack> getBlockDropped(World w, int x, int y, int z, int metadata, int fortune){
 		ArrayList<ItemStack> q = new ArrayList<ItemStack>();
 		// insert pick code
 		q.add(new ItemStack(this.blockID,1,metadata));
 		return q;
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta){
 		return icons[meta][side];
 	}
 	
 }
