 package mods.cc.rock.creativetabs;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 
 public class CreativeTabCC extends CreativeTabs
 {
 
     public CreativeTabCC(String label)
     {
         super(label);
     }
 
     public CreativeTabCC(int par1, String par2str)
     {
         super(par1, par2str);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     //Give item id of item to display as tab icon
     public int getTabIconItemIndex()
     {
         return Block.cake.blockID;
     }
     
 }
