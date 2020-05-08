 package assets.fyresmodjam;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockPillar extends BlockContainer
 {
     protected BlockPillar(int par1) {
         super(par1, Material.rock);
         this.setLightOpacity(0);
         //this.setTickRandomly(true);
         //this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
     }
 
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) {
         this.blockIcon = par1IconRegister.registerIcon("fyresmodjam:pillar");
     }
 
     public int idDropped(int par1, Random par2Random, int par3) {
         return 0;
     }
 
     public int idPicked(World par1World, int par2, int par3, int par4) {
         return ModjamMod.itemPillar.itemID;
     }
 
     public boolean canHarvestBlock(EntityPlayer par1EntityPlayer, int par2) {
         return false;
     }
     
     public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)  {
     	if(par1World.getBlockMetadata(par2, par3, par4) == 1) {par3--;}
     	
     	TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);
     	
     	if(te != null && te instanceof TileEntityPillar && (!par5EntityPlayer.getEntityData().hasKey("Blessing") || !par5EntityPlayer.getEntityData().getString("Blessing").equals(((TileEntityPillar) te).blessing))) {
     		
     		boolean skip = false;
     		
     		for(int i = 0; i < par1World.loadedEntityList.size(); i++) {
     			Entity e = (Entity) par1World.loadedEntityList.get(i);
     			
     			if(e instanceof EntityMob) {
     				double xDiff = par2 - e.posX;
     				double yDiff = par3 - e.posY;
     				double zDiff = par4 - e.posZ;
     				double dist = Math.sqrt(xDiff * xDiff + (yDiff > 6 ? yDiff * yDiff : 0) + zDiff * zDiff);
     				
     				if(dist <= 14) {skip = true;}
     			}
     		}
     		
     		if(!skip) {
     			EntityStatHelper.giveStat(par5EntityPlayer, "Blessing", ((TileEntityPillar) te).blessing);
     			
     			if(par1World.isRemote) {
     				//Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("Activated blessing of the " + ((TileEntityPillar) te).blessing + ".");
     			} else {
     				par1World.playSoundAtEntity(par5EntityPlayer, "fyresmodjam:pillarActivated", 1.0F, 1.0F);
    				//par1World.playSound(te.xCoord, te.yCoord, te.zCoord, "fyresmodjam:pillarActivated", 1.0F, 1.0F, true);
     			}
     		} else {
     			if(!par1World.isRemote) {
     				//Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("\u00A7cCannot activate pillar with monsters nearby!");
     			}
     		}
     	}
     	
     	return true;
     }
 
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
         super.onNeighborBlockChange(par1World, par2, par3, par4, par5);
 
         if (!par1World.isRemote) {
             int i1 = par1World.getBlockMetadata(par2, par3, par4);
 
             if (i1 == 0) {
                 if (par1World.isAirBlock(par2, par3 + 1, par4) || par1World.getBlockId(par2, par3 + 1, par4) != par1World.getBlockId(par2, par3, par4)) {
                     //this.dropBlockAsItem(par1World, par2, par3, par4, i1, 0);
                     par1World.setBlockToAir(par2, par3, par4);
                 }
             } else {
                 if (par1World.isAirBlock(par2, par3 - 1, par4) || par1World.getBlockId(par2, par3 - 1, par4) != par1World.getBlockId(par2, par3, par4)) {
                     par1World.setBlockToAir(par2, par3, par4);
                 }
             }
         }
     }
 
     @Override
     public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
         return par3 >= 255 ? false : (super.canPlaceBlockAt(par1World, par2, par3, par4) && super.canPlaceBlockAt(par1World, par2, par3 + 1, par4));
     }
 
     public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
         int l = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
 
         if (l == 0) {
             this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
         } else {
             this.setBlockBounds(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         }
     }
 
     @SideOnly(Side.CLIENT)
     public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
         return false;
     }
 
     public boolean isOpaqueCube() {
         return false;
     }
 
     public boolean hasTileEntity(int metadata) {
         return metadata == 0;
     }
 
     @Override
     public TileEntity createNewTileEntity(World world) {
         return new TileEntityPillar();
     }
 
     public int getMobilityFlag() {
         return 2;
     }
 
     public void onBlockHarvested(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer) {
         if (par6EntityPlayer.capabilities.isCreativeMode && par5 >= 2 && par1World.getBlockId(par2, par3 - 1, par4) == this.blockID) {
             par1World.setBlockToAir(par2, par3 - 1, par4);
         } else {
             super.onBlockHarvested(par1World, par2, par3, par4, par5, par6EntityPlayer);
         }
     }
     
    @Override
     public int getLightValue(IBlockAccess world, int x, int y, int z) {
         Block block = blocksList[world.getBlockId(x, y, z)];
         TileEntity te = world.getBlockTileEntity(x, y, z);
         
         if(block != null && block != this)  {
             return block.getLightValue(world, x, y, z);
         }
         
        if(ModjamMod.pillarGlow && Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().theWorld.isRemote) {
        	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
         	
         	if(player != null && te != null && te instanceof TileEntityPillar && ((TileEntityPillar) te).blessing != null && ((TileEntityPillar) te).blessing.equals(player.getEntityData().getString("Blessing"))) {
         		return 4;
         	}
         }
         
         return 0;
     }
 }
