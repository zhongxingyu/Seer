 package playacem.allrondism.block;
 
 import java.util.Random;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import playacem.allrondism.Allrondism;
 import playacem.allrondism.lib.Reference;
 import playacem.allrondism.lib.Strings;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.EnumPlantType;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IPlantable;
 import static net.minecraftforge.common.EnumPlantType.*;
 
 /**
  * Allrondism
  * 
  * BlockPlantRose
  * 
  * @author Playacem
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 public class BlockPlantRose extends BlockAM implements IPlantable {
 
     private String[] names = Strings.ROSES;
     private Icon[] icons = new Icon[names.length];
     
     public BlockPlantRose(int id) {
         super(id, Material.plants);
         setHardness(0.0F);
         setStepSound(soundGrassFootstep);
         setUnlocalizedName(Strings.PLANT_ROSE_NAME);
         setCreativeTab(Allrondism.tabsAM);
         this.setTickRandomly(true);
         
         float f = 0.2F;
         this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 3.0F, 0.5F + f);
     }
 
     public boolean canPlaceBlockAt(World world, int x, int y, int z) {
         return super.canPlaceBlockAt(world, x, y, z) && canBlockStay(world, x, y, z);
     }
     
     public boolean canBlockStay(World world, int x, int y, int z) {
         Block soil = blocksList[world.getBlockId(x, y - 1, z)];
         return (soil != null && soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this));
     }
     
     public boolean canSustainPlant(World world, int x, int y, int z, ForgeDirection direction, IPlantable plant) {
         int id = world.getBlockId(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        return id == Block.grass.blockID || id == Block.dirt.blockID || id == Block.tilledField.blockID || (id == ModBlocks.storageBlock.blockID && meta == 0);
     }
     
     public boolean canThisPlantGrowOnThisBlockID(int id) {
        return id == Block.grass.blockID || id == Block.dirt.blockID || id == Block.tilledField.blockID || (id == ModBlocks.storageBlock.blockID && ModBlocks.storageBlock.getUnlocalizedName() == "storageDirt");
     }
     
     /* GameUpdate relevant stuff */
 
     /**
      * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
      * their own) Args: x, y, z, neighbor blockID
      */
     public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
         super.onNeighborBlockChange(world, x, y, z, neighborBlockID);
         this.checkRoseChange(world, x, y, z);
     }
 
     /**
      * Ticks the block if it's been scheduled
      */
     public void updateTick(World world, int x, int y, int z, Random par5Random) {
         this.checkRoseChange(world, x, y, z);
     }
     
    protected final void checkRoseChange(World world, int x, int y, int z) {
        int lightVal = world.getFullBlockLightValue(x, y, z);
         int newMetadata = 0;
         
         if (!this.canBlockStay(world, x, y, z)) {
             this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
             world.setBlockToAir(x, y, z);
         }else {
            if(lightVal > 10) { newMetadata = 1; }
             /** Flag 1 will cause a block update. 
              *  Flag 2 will send the change to clients (you almost always want this). 
              *  Flag 4 prevents the block from being re-rendered, if this is a client world. 
              *  Flags can be added together. */
             world.setBlockMetadataWithNotify(x, y, z, newMetadata , 3); 
         }
     }
     
     /* IPlantable stuff */
     
     @Override
     public EnumPlantType getPlantType(World world, int x, int y, int z) {
         return Plains;
     }
 
     @Override
     public int getPlantID(World world, int x, int y, int z) {
         return blockID;
     }
 
     @Override
     public int getPlantMetadata(World world, int x, int y, int z) {
         return world.getBlockMetadata(x, y, z);
     }
 
     /* Render relevant stuff */
     
     /**
      * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
      * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
      */
     public boolean isOpaqueCube() {
         return false;
     }
 
     /**
      * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, flowers, etc)
      */
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     /**
      * The type of render function that is called for this block
      */
     public int getRenderType() {
         return 1;
     }
     
     /** Returns the collision box. Returning null means no collision box ==> You can walk through this block. */
     public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
         return null;
     }
     
     /* Game and texture relevant stuff */
 
     @Override
     public int damageDropped(int meta) {
         return meta;
     }
     
     @SideOnly(Side.CLIENT)
     @Override
     public void registerIcons(IconRegister iconReg) {
         for (int i = 0; i < icons.length; i++) {
             icons[i] = iconReg.registerIcon(Reference.MOD_ID.toLowerCase()
                     + ":" + getUnlocalizedName2() + names[i]);
         }
     }
     
     @Override
     public Icon getIcon(int side, int meta) {
         return icons[Math.min(meta, icons.length)];
     }
 
 }
