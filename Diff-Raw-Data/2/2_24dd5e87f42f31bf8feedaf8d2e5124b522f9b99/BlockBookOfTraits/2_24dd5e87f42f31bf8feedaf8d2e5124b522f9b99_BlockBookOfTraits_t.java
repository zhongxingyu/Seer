 package net.minecraft.src;
 
 public class BlockBookOfTraits extends Block{
 	public BlockBookOfTraits(int i){
 		super(i, Material.wood);
         this.blockIndexInTexture = 26;
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
         this.setLightOpacity(0);
         this.setCreativeTab(CreativeTabs.tabDeco);
 	}
 
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
 		ModLoader.openGUI(player, new GuiTraits(player));
 
 		return true;
 	}
 }
