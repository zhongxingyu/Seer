 package net.minecraft.src;
 
 public class BlockBookOfTraits extends Block{
 	public BlockBookOfTraits(int i){
 		super(i, Material.wood);
         this.blockIndexInTexture = 26;
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
         this.setLightOpacity(0);
         this.setCreativeTab(CreativeTabs.tabDeco);
 	}
 
	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player){
 		ModLoader.openGUI(player, new GuiTraits(player));
 
 		return true;
 	}
 }
