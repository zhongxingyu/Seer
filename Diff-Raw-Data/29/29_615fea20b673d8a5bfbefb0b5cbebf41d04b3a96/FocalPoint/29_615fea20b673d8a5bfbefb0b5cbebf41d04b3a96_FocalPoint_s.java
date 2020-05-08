 package pixlepix.complexmachines.common;
 
 import java.util.Random;
 
 import pixlepix.complexmachines.client.ClientProxy;
 import universalelectricity.core.UniversalElectricity;
 import universalelectricity.prefab.block.BlockAdvanced;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class FocalPoint extends BlockAdvanced
 {
     private Icon connectorIcon;
 
 	public FocalPoint(int id)
     {
         super(id, UniversalElectricity.machine);
        this.setUnlocalizedName("focal point");
         this.setCreativeTab(CreativeTabs.tabMisc);
     }
     
     public FocalPoint()
     {
         super(ComplexMachines.blockStartingID+3, UniversalElectricity.machine);
         this.setStepSound(soundMetalFootstep);
         this.setUnlocalizedName("focal point");
         this.setCreativeTab(CreativeTabs.tabMisc);
     }
         
     /**
      * Called when the block is placed in the world.
      */
     
     @Override
     public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9){
     	par1World.setBlock(par2, par3, par4, ComplexMachines.blockStartingID+8);
     	return true;
     }
     
     @Override
     public int quantityDropped(Random rand){
     	return 0;
     }
     
     @Override
     public void onBlockPlacedBy(World par1World, int x, int y, int z, EntityLiving par5EntityLiving, ItemStack itemStack)
     {	
         
         
         //((FocalPointControledTileEntity) par1World.getBlockTileEntity(x, y, z)).initiate();
         par1World.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
     }
     
     
    /* 
     @Override
     public boolean onMachineActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side,
             float hitX, float hitY, float hitZ)
     {
         if (!par1World.isRemote)
         {
             par5EntityPlayer.openGui(ElectricExpansion.instance, 2, par1World, x, y, z);
             return true;
         }
         
         return true;
     }
     */
     @Override
     public boolean isOpaqueCube()
     {
         return true;
     }
     
     @Override
     public boolean renderAsNormalBlock()
     {
         return false;
     }
     
     @Override
     public TileEntity createTileEntity(World var1, int metadata)
     {
         //return new FocalPointControledTileEntity();
         return null;
     }
     
     
     @Override
     public boolean hasTileEntity(int metadata)
     {
         return false;
     }
     
     //Imported code from EE, unsure if it is needed
     /*
     @SideOnly(Side.CLIENT)
     @Override
     public int getRenderType()
     {
         return ClientProxy.RENDER_ID;
     }
     */
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister)
     {
 
         blockIcon = par1IconRegister.registerIcon("ComplexMachines:FocalPoint"); 
         connectorIcon = par1IconRegister.registerIcon("ComplexMachines:InputSide"); 
     }
     
     @Override
 	public Icon getIcon (int side, int meta)
 	{
 		if (side==meta+2){
 			return connectorIcon;
 		}else{
 			return blockIcon;
 		}
 	}
     
 }
