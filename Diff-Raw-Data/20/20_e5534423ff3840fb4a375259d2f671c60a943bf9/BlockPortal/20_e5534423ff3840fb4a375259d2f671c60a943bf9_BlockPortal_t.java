 package jk_5.nailed.blocks;
 
 import com.google.common.collect.Lists;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import jk_5.nailed.blocks.tileentity.TileEntityPortalController;
 import jk_5.nailed.map.teleport.TeleportHelper;
 import jk_5.nailed.map.teleport.TeleportOptions;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IIconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.init.Blocks;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class BlockPortal extends NailedBlock {
 
     public BlockPortal(){
         super("portal", Material.field_151570_A); //Material.portal
         this.func_149675_a(true);
         this.func_149715_a(1);
         this.func_149722_s();
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void func_149651_a(IIconRegister register){
         this.field_149761_L = register.registerIcon("nailed:portal");
     }
 
     @Override
     public AxisAlignedBB func_149668_a(World world, int par2, int par3, int i){
         return null;
     }
 
     @Override
     public void func_149719_a(IBlockAccess blockAccess, int x, int y, int z){
         float xmin = 0.25F;
         float xmax = 0.75F;
         float ymin = 0.25F;
         float ymax = 0.75F;
         float zmin = 0.25F;
         float zmax = 0.75F;
         if(BlockPortalController.isValidLinkPortalBlock(blockAccess.func_147439_a(x - 1, y, z)) > 0){
             xmin = 0.0F;
         }
         if(BlockPortalController.isValidLinkPortalBlock(blockAccess.func_147439_a(x + 1, y, z)) > 0){
             xmax = 1.0F;
         }
         if(BlockPortalController.isValidLinkPortalBlock(blockAccess.func_147439_a(x, y - 1, z)) > 0){
             ymin = 0.0F;
         }
         if(BlockPortalController.isValidLinkPortalBlock(blockAccess.func_147439_a(x, y + 1, z)) > 0){
             ymax = 1.0F;
         }
         if(BlockPortalController.isValidLinkPortalBlock(blockAccess.func_147439_a(x, y, z - 1)) > 0){
             zmin = 0.0F;
         }
         if(BlockPortalController.isValidLinkPortalBlock(blockAccess.func_147439_a(x, y, z + 1)) > 0){
             zmax = 1.0F;
         }
         this.func_149676_a(xmin, ymin, zmin, xmax, ymax, zmax);
     }
 
     @Override
     public boolean func_149662_c(){
         return false;
     }
 
     @Override
     public boolean func_149686_d(){
         return false;
     }
 
     @Override
     public int func_149645_b(){
         return 0;
     }
 
     @Override
     public int func_149635_D(){
         return 0x3333FF;
     }
 
     @Override
     public int func_149741_i(int i){
         return 0x3333FF;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public boolean func_149646_a(IBlockAccess world, int x, int y, int z, int side){
         Block block = world.func_147439_a(x, y, z);
         return block != this && super.func_149646_a(world, x, y, z, side);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public int func_149720_d(IBlockAccess blockAccess, int x, int y, int z){
         World world = Minecraft.getMinecraft().theWorld;
         TileEntity entity = BlockPortalController.getTileEntity(world, x, y, z);
         if((entity != null) && ((entity instanceof TileEntityPortalController))){
             TileEntityPortalController controller = (TileEntityPortalController) entity;
             return controller.getColor();
         }
         return func_149635_D();
     }
 
     @Override
     public void func_149695_a(World par1World, int par2, int par3, int par4, Block block){
         if(par1World.isRemote) return;
         validate(par1World, new ChunkCoordinates(par2, par3, par4));
     }
 
     @Override
     public int func_149745_a(Random rand){
         return 0;
     }
 
     @Override
     public int func_149701_w(){
         return 1;
     }
 
     @Override
     public void func_149670_a(World world, int x, int y, int z, Entity entity){
         if(world.isRemote) return;
         TileEntity tileentity = BlockPortalController.getTileEntity(world, x, y, z);
         if((tileentity == null) || (!(tileentity instanceof TileEntityPortalController))){
             world.func_147468_f(x, y, z);
             return;
         }
         TileEntityPortalController container = (TileEntityPortalController) tileentity;
         if(container.getDestination() == null){
             world.func_147468_f(x, y, z);
             return;
         }
         TeleportOptions options = container.getDestination();
         options.setMaintainMomentum(true);
        options.setSound("nailed:teleport.portal");
         TeleportHelper.travelEntity(world, entity, options);
     }
 
     @Override
     public void func_149674_a(World world, int i, int j, int k, Random rand){
         if(world.isRemote) return;
         this.func_149695_a(world, i, j, k, Blocks.air);
     }
 
     public static void validate(World world, ChunkCoordinates start){
         if(world.isRemote) return;
         List<ChunkCoordinates> blocks = Lists.newLinkedList();
         blocks.add(start);
         while(blocks.size() > 0){
             ChunkCoordinates coords = blocks.remove(0);
             if(world.func_147439_a(coords.posX, coords.posY, coords.posZ) == NailedBlocks.portal){
                 validate(world, coords.posX, coords.posY, coords.posZ, blocks);
             }
         }
     }
 
     private static void validate(World world, int i, int j, int k, Collection<ChunkCoordinates> blocks){
         if(!isValidPortal(world, i, j, k)){
             world.func_147449_b(i, j, k, Blocks.air); //setBlock
             BlockPortalController.addSurrounding(blocks, i, j, k);
         }
     }
 
     public static boolean isValidPortal(World world, int i, int j, int k){
         if(world.isRemote) return true;
         if(!checkPortalTension(world, i, j, k)) return false;
         return BlockPortalController.getTileEntity(world, i, j, k) != null;
     }
 
     public static boolean checkPortalTension(World world, int i, int j, int k){
         if(world.isRemote) return true;
         int score = 0;
         if((BlockPortalController.isValidLinkPortalBlock(world.func_147439_a(i + 1, j, k)) > 0) && (BlockPortalController.isValidLinkPortalBlock(world.func_147439_a(i - 1, j, k)) > 0)){
             score++;
         }
         if((BlockPortalController.isValidLinkPortalBlock(world.func_147439_a(i, j + 1, k)) > 0) && (BlockPortalController.isValidLinkPortalBlock(world.func_147439_a(i, j - 1, k)) > 0)){
             score++;
         }
         if((BlockPortalController.isValidLinkPortalBlock(world.func_147439_a(i, j, k + 1)) > 0) && (BlockPortalController.isValidLinkPortalBlock(world.func_147439_a(i, j, k - 1)) > 0)){
             score++;
         }
         return score > 1;
     }
 }
