 package sokobanMod.common;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.item.EntityFallingSand;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * Sokoban Mod
  * @author MineMaarten
  * www.minemaarten.com
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 
 public class BlockTargetBox extends BlockContainer{
 
     /** Do blocks fall instantly to where they stop or do they fall over time */
     public static boolean fallInstantly = false;
 
     public BlockTargetBox(int par1, Material par3Material){
         super(par1, par3Material);
         setHardness(0.0F);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister){
         blockIcon = par1IconRegister.registerIcon("sokobanMod:BlockTargetBox");
     }
 
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9){
         int targetX = x;
         int targetY = y;
         int targetZ = z;
         switch(side){
             case 0:
                 return false;// we can't move the box up
             case 1:
                 return false;// or down.
             case 2:
                 targetZ++;
                 break;
             case 3:
                 targetZ--;
                 break;
             case 4:
                 targetX++;
                 break;
             case 5:
                 targetX--;
         }
        if(world.isAirBlock(targetX, targetY, targetZ) && !world.isRemote) {
             world.playSoundEffect(x, y, z, "sokobanmod:movingBox", 1.0F, 1.0F);
             TileEntity TE = world.getBlockTileEntity(x, y, z);
             if(TE instanceof TileEntityTargetBox) {
                 TileEntityTargetBox tbTE = (TileEntityTargetBox)TE;
                 EntityMovingTargetBox movingBox = new EntityMovingTargetBox(world, x + 0.5D, y + 0.5D, z + 0.5D, side, tbTE.minX, tbTE.minY, tbTE.minZ, tbTE.maxX, tbTE.maxY, tbTE.maxZ, tbTE.levelDropped);
                 world.spawnEntityInWorld(movingBox);
             }
             world.setBlock(x, y, z, 0);
             return true;
         }
         return false;
     }
 
     // Forge function
     @Override
     public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z){
         Random rand = new Random();
         TileEntity TE = world.getBlockTileEntity(x, y, z);
         if(TE instanceof TileEntityTargetBox) {
             TileEntityTargetBox boxTE = (TileEntityTargetBox)TE;
 
             //only drop a level generator when the level isn't solved yet (which is the case when there's no loot generator left.
             boolean lootGeneratorFound = SokobanUtils.removeLevel(world, boxTE.minX, boxTE.minY, boxTE.minZ, boxTE.maxX, boxTE.maxY, boxTE.maxZ, rand);
             if(lootGeneratorFound) dropBlockAsItem_do(world, x, y, z, new ItemStack(SokobanMod.ItemLevelGeneratorTutorial.itemID, 1, boxTE.levelDropped));
         }
         return super.removeBlockByPlayer(world, player, x, y, z);
     }
 
     @Override
     public int quantityDropped(Random rand){
         return 0;
     }
 
     @Override
     public TileEntity createNewTileEntity(World var1){
         return new TileEntityTargetBox();
     }
 
     // SAND CODE (couldn't extend the tile entity because we had to extend BlockContainer.
     //TODO refactoring: We can implement ITileEntityProvider, so we can extend blockSand.
     /**
      * Called whenever the block is added into the world. Args: world, x, y, z
      */
     @Override
     public void onBlockAdded(World world, int x, int y, int z){
         world.scheduleBlockUpdate(x, y, z, blockID, this.tickRate());
     }
 
     /**
      * Lets the block know when one of its neighbor changes. Doesn't know which
      * neighbor changed (coordinates passed are their own) Args: x, y, z,
      * neighbor blockID
      */
     @Override
     public void onNeighborBlockChange(World world, int x, int y, int z, int par5){
         world.scheduleBlockUpdate(x, y, z, blockID, this.tickRate());
     }
 
     /**
      * Ticks the block if it's been scheduled
      */
     @Override
     public void updateTick(World world, int x, int y, int z, Random par5Random){
         if(!world.isRemote) {
             tryToFall(world, x, y, z);
         }
     }
 
     /**
      * If there is space to fall below will start this block falling
      */
     private void tryToFall(World world, int x, int y, int z){
         if(canFallBelow(world, x, y - 1, z) && y >= 0) {
             byte var8 = 32;
 
             if(!fallInstantly && world.checkChunksExist(x - var8, y - var8, z - var8, x + var8, y + var8, z + var8)) {
                 if(!world.isRemote) {
                     TileEntity TE = world.getBlockTileEntity(x, y, z);
                     if(TE instanceof TileEntityTargetBox) {
                         TileEntityTargetBox TETB = (TileEntityTargetBox)TE;
                         EntityFallingTargetBox var9 = new EntityFallingTargetBox(world, x + 0.5F, y + 0.5F, z + 0.5F, blockID, world.getBlockMetadata(x, y, z), TETB.minX, TETB.minY, TETB.minZ, TETB.maxX, TETB.maxY, TETB.maxZ, TETB.levelDropped);
                         onStartFalling(var9);
                         world.spawnEntityInWorld(var9);
                     }
                 }
             } else {
 
                 int targetY = y;
                 while(canFallBelow(world, x, targetY - 1, z) && targetY > 0) {
                     --targetY;
                 }
 
                 if(targetY > 0) {
                     world.setBlock(x, targetY, z, blockID);
 
                     TileEntity targetTE = world.getBlockTileEntity(x, targetY, z);
                     TileEntity oldTE = world.getBlockTileEntity(x, y, z);
                     if(targetTE instanceof TileEntityTargetBox && oldTE instanceof TileEntityTargetBox) {
                         TileEntityTargetBox targetTargetBox = (TileEntityTargetBox)targetTE;
                         TileEntityTargetBox oldBox = (TileEntityTargetBox)oldTE;
                         targetTargetBox.setLevelBounds(oldBox.minX, oldBox.minY, oldBox.minZ, oldBox.maxX, oldBox.maxY, oldBox.maxZ);// copy the data to the new TE.
                         if(targetTargetBox.minX != 0 && targetTargetBox.minY != 0 && targetTargetBox.minZ != 0 && // when we are checking an already known level
                         (x < targetTargetBox.minX || x > targetTargetBox.maxX || targetY < targetTargetBox.minY || targetY > targetTargetBox.maxY || z < targetTargetBox.minZ || z > targetTargetBox.maxZ)) removeBlockByPlayer(world, null, x, targetY, z); // and the block is outside the level boundaries remove the ENTIRE level.
                     }
                     world.setBlock(x, y, z, 0);
                 }
             }
         }
     }
 
     /**
      * Called when the falling block entity for this block is created
      */
     protected void onStartFalling(EntityFallingSand par1EntityFallingSand){}
 
     /**
      * How many world ticks before ticking
      */
     public int tickRate(){
         return 5;
     }
 
     /**
      * Checks to see if the sand can fall into the block below it
      */
     public static boolean canFallBelow(World world, int x, int y, int z){
         int var4 = world.getBlockId(x, y, z);
         if(var4 == 0) {
             return true;
         } else if(var4 == Block.fire.blockID) {
             return true;
         } else {
             Material var5 = Block.blocksList[var4].blockMaterial;
             return var5 == Material.water ? true : var5 == Material.lava;
         }
     }
 
     /**
      * Called when the falling block entity for this block hits the ground and
      * turns back into a block
      */
     public void onFinishFalling(World world, int x, int y, int z, int par5){
 
     }
     // END OF SAND CODE
 
 }
