 package steamcraft.blocks;
 
 import java.lang.reflect.Field;
 
 import net.minecraft.block.BlockFurnace;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockMainFurnace extends BlockFurnace{
 
 	private static boolean keepFurnaceInventory;
 	protected Icon furnaceIconTop;
 	protected Icon furnaceIconFront;
 	protected boolean isActive;
 	private String[] names = new String[3];
 	protected BlockMainFurnace(int par1, boolean par2, String side, String top, String front) {
 		super(par1, par2);
 		this.isActive = par2;
 		this.names[0] = side;
 		this.names[1] = top;
 		this.names[2] = front;
 	}
 	@Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int par1, int par2)
     {
         return par1 == 1 ? this.furnaceIconTop : (par1 == 0 ? this.furnaceIconTop : (par1 != par2 ? this.blockIcon : this.furnaceIconFront));
     }
     @Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister)
 	{
 	    this.blockIcon = par1IconRegister.registerIcon("steamcraft:"+names[0]);
 	    this.furnaceIconFront = par1IconRegister.registerIcon("steamcraft:"+names[2]+(this.isActive ? "active" : "idle"));
 	    this.furnaceIconTop = par1IconRegister.registerIcon("steamcraft:"+names[1]);
 	}
     public static void updateFurnaceBlockState(boolean flag, World world, int i, int j, int k, int activeID, int idleID, boolean sounds)
     {
         int l = world.getBlockMetadata(i, j, k);
         TileEntity tileentity = world.getBlockTileEntity(i, j, k);
         setKeepInventory(true);
         if(flag)
         {
         	if(sounds){
         	world.playSoundEffect((float)i + 0.5F, (float)j + 0.5F, (float)k + 0.5F, "mob.ghast.fireball", 1.0F, 0.8F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F);
 			world.playSoundEffect((float)i + 0.5F, (float)j + 0.5F, (float)k + 0.5F, "mob.zombiepig.zpigdeath", 0.1F, 0.1F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.6F);
 			world.playSoundEffect((float)i + 0.5F, (float)j + 0.5F, (float)k + 0.5F, "fire.ignite", 1.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
         	}
             world.setBlock(i, j, k, activeID);
         } else
         {
         	if(sounds)
         		world.playSoundEffect((float)i + 0.5F, (float)j + 0.5F, (float)k + 0.5F, "ambient.cave.cave", 0.1F, 0.1F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
             world.setBlock(i, j, k, idleID);
         }
         setKeepInventory(false);
         world.setBlockMetadataWithNotify(i, j, k, l, 2);
         if(tileentity != null)
         {
             tileentity.validate();
             world.setBlockTileEntity(i, j, k, tileentity);
         }
     }
     public static void setKeepInventory(boolean value){
     	try {
			Field f = BlockFurnace.class.getDeclaredField("keepFurnaceInventory");
 			if(!f.isAccessible())
 				f.setAccessible(true);
 			f.set(null,value);
 		} catch (RuntimeException  e) {
 			e.printStackTrace();
 		} catch (ReflectiveOperationException e) {
 			e.printStackTrace();
 		}
     }
 }
