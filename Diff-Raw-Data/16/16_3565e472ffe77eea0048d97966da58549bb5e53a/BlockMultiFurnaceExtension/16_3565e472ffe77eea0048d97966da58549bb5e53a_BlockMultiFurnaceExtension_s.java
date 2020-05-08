 package playacem.allrondism.block;
 
 import java.util.Random;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import playacem.allrondism.Allrondism;
 import playacem.allrondism.core.util.LogHelper;
 import playacem.allrondism.lib.ExtensionData;
 import playacem.allrondism.lib.Reference;
 import playacem.allrondism.lib.Strings;
 import playacem.allrondism.tileentity.ICoreExtension;
 import playacem.allrondism.tileentity.TileEntityMultiFurnaceDummy;
 import playacem.allrondism.tileentity.TileEntityMultiFurnaceCore;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 /**
  * Allrondism
  * 
  * BlockMultiFurnaceExtension
  * 
  * @author Playacem
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 public class BlockMultiFurnaceExtension extends BlockContainerAM {
 
     private String[] names = Strings.MULTI_FURNACE_EXTENSIONS;
     @SideOnly(Side.CLIENT)
     private Icon[] icons = new Icon[names.length];
     
     public BlockMultiFurnaceExtension(int id) {
         super(id, Material.rock);
         setStepSound(soundStoneFootstep);
         setUnlocalizedName(Strings.MULTI_FURNACE_EXTENSION_NAME);
         setCreativeTab(Allrondism.tabsAM);
     }
 
     @Override
     public TileEntity createNewTileEntity(World world) {
 
         return new TileEntityMultiFurnaceDummy();
     }
 
     @Override
     public int idDropped(int meta, Random random, int zero) {
         if(meta == ExtensionData.DUMMY_META) {
             return ModBlocks.storageBlock.blockID;
         }
         return this.blockID;
     }
     
     @Override
     public int damageDropped(int meta) {
         if(meta == ExtensionData.DUMMY_META) {
             return 1;
         }
         return meta;
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public void registerIcons(IconRegister iconReg) {
         
         icons[ExtensionData.DUMMY_META] = iconReg.registerIcon(Reference.MOD_ID.toLowerCase() + ":" + "storageBlockCobble");
         for (int i = 1; i < icons.length; i++) {
            icons[i] = iconReg.registerIcon(Reference.MOD_ID.toLowerCase()
                    + ":" + getUnlocalizedName2() + names[i]);
         }
     }
 
     @Override
     public Icon getIcon(int side, int meta) {
 
        return icons[Math.min(meta, icons.length)];
     }
     
     @Override
     public void breakBlock(World world, int x, int y, int z, int par5, int par6)
     {
         ICoreExtension extension = (ICoreExtension)world.getBlockTileEntity(x, y, z);
         
         if(extension != null && extension.getCore() != null) {
             LogHelper.debug("Invalidated the MultiBlock");
             extension.getCore().invalidateMultiBlock();
         }
             
         
         super.breakBlock(world, x, y, z, par5, par6);
     }
     
     @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
         if(player.isSneaking())
             return false;
         
         ICoreExtension extension = (ICoreExtension)world.getBlockTileEntity(x, y, z);
         
         if(extension != null && extension.getCore() != null) {
             TileEntityMultiFurnaceCore core = extension.getCore();
            return core.getBlockType().onBlockActivated(world, core.xCoord, core.yCoord, core.zCoord, player, par6, par7, par8, par9);
         }
         
         return true;
     }
 }
