 package herbologica.block.crop;
 
 import herbologica.ArsHerbologica;
 import herbologica.lib.HerbologicaIDs;
 import herbologica.lib.Reference;
 import herbologica.tileentity.crop.TileEntityBush;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockLeavesBase;
 import net.minecraft.block.ITileEntityProvider;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.EnumPlantType;
 import net.minecraftforge.common.IPlantable;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * Ars Herbologica
  * 
  * BlockHerbologicaBush
  * 
  * @author Myo-kun
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class BlockHerbologicaBush extends BlockLeavesBase implements IPlantable,ITileEntityProvider {
 	
 	@SideOnly(Side.CLIENT)
     private Icon[] icons;
 	private Random random;
 	
 	public BlockHerbologicaBush(int id) {
 		super(id, Material.leaves, false);
 		setCreativeTab(ArsHerbologica.herbologicaTab);
 		setStepSound(Block.soundGrassFootstep);
 		setTickRandomly(true);
 		random = new Random();
 	}
 	
 	@Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister register) {
         icons = new Icon[HerbologicaIDs.BUSH_BLOCK_UNLOCALIZED.size()];
         for (int i = 0; i < icons.length; i++) {
             icons[i] = register.registerIcon(Reference.MOD_ID + ":" + HerbologicaIDs.BUSH_BLOCK_UNLOCALIZED.get(i));
         }
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int side, int meta) {
         return icons[meta];
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void getSubBlocks(int id, CreativeTabs tab, List list) {
         for (int i = 0; i < HerbologicaIDs.BUSH_BLOCK_UNLOCALIZED.size(); i++) {
             list.add(new ItemStack(id, 1, i));
         }
     }
     
     @Override
    public void setBlockBoundsBasedOnState(IBlockAccess block, int x, int y, int z) {
    	
    	TileEntityBush tileentity = (TileEntityBush)block.getBlockTileEntity(x, y, z);
     	
         float minX;
         float minY = 0F;
         float minZ;
         float maxX;
         float maxY;
         float maxZ;
         
        if (tileentity.growthStage < 4) {
             minX = minZ = 0.25F;
             maxX = maxZ = 0.75F;
             maxY = 0.5F;
         }
         else 
        if (tileentity.growthStage < 8) {
             minX = minZ = 0.125F;
             maxX = maxZ = 0.875F;
             maxY = 0.75F;
         }
         else {
             minX = minZ = 0.0F;
             maxX = maxZ = 1.0F;
             maxY = 1.0F;
         }
         setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
         
     }
     
     @Override
     public int damageDropped(int meta) {
         return meta;
     }
     
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float j, float k, float l) {
     	
    	TileEntityBush tileentity = (TileEntityBush)world.getBlockTileEntity(x, y, z);
    	
    	if (tileentity.growthStage < 15) {
    		tileentity.growthStage++;
     		return true;
     	}
     	return false;
     }
 	
 	@Override
 	public EnumPlantType getPlantType(World world, int x, int y, int z) {
 		return EnumPlantType.Plains;
 	}
 	
 	@Override
 	public int getPlantID(World world, int x, int y, int z) {
 		return this.blockID;
 	}
 	
 	@Override
 	public int getPlantMetadata(World world, int x, int y, int z) {
 		return world.getBlockMetadata(x, y, z);
 	}
 	
 	@Override
 	public TileEntity createNewTileEntity(World world) {
		return new TileEntityBush();
 	}
 	
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 	
 	public void setGraphicsLevel(boolean flag) {
 		graphicsLevel = flag;
 	}
 
 }
