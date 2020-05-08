 package FuzzCraft.Blocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.util.Icon;
 
 public class Colorizor extends Block{
     
     private Icon sides, bottom, top, front;
     
     public Colorizor (int par1) {
         super(par1, Material.iron);
         this.setCreativeTab(CreativeTabs.tabDecorations);
         this.setResistance(20.0F);
         this.setHardness(1.5F);
         this.setUnlocalizedName("colorizorblock");
     }
     
     @Override
     public void registerIcons(IconRegister par1IconRegister) {
         sides = par1IconRegister.registerIcon("FuzzCraft:colorizor_side");
         bottom = par1IconRegister.registerIcon("FuzzCraft:colorizor_bottom");
         top = par1IconRegister.registerIcon("FuzzCraft:colorizor_top");
         front = par1IconRegister.registerIcon("FuzzCraft:colorizor_side");
     }
 
    public Icon getBlockTextureFromSideandMetadata(int i, int j) {
         if (i == 0)
             return bottom;
         if (i == 1)
             return top;
         if (i == 3)
             return front;
         else
             return sides;
      }
 }
