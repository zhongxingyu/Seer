 package org.pixotic.xcraft.block;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 import org.pixotic.xcraft.lib.Reference;
 import org.pixotic.xcraft.tileentity.TileTest3DBlock;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
public class BlockTest3DBlock extends Block {
     public BlockTest3DBlock(int id, Material par2Material) {
         super(id, par2Material);
         this.setCreativeTab(CreativeTabs.tabBlock);
         this.setUnlocalizedName("test3DBlock");
     }
     
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) {
         this.blockIcon = par1IconRegister.registerIcon(Reference.MOD_ID + ":elerium");
     }
     
     @Override
     public boolean hasTileEntity() { 
         return true;
     }
     
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer playerEntity, int par6, float par7, float par8, float par9)
     {
         if(playerEntity.isSneaking())
             return false;
         
         playerEntity.setEntityHealth(0);
         return true;
     }
 
     
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
     
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
     
    public TileEntity createNewTileEntity(World world, int metadata) {
         return new TileTest3DBlock();
     }
     
     public int getRenderType() {
        return -1;
     }
     
     
 }
