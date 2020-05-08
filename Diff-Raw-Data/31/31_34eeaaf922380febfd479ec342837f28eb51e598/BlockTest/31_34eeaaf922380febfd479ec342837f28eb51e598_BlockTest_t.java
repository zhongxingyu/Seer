 package obt.block;
 
 import java.awt.List;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.util.Icon;
 import obt.lib.Reference;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 
 public class BlockTest extends BlockOb 
 {
     @SideOnly(Side.CLIENT)
    private String[] oreNames = new String[]{"copper","silver","infin"};
     @SideOnly(Side.CLIENT)
    private static String[] iconNames = new String[]{"testBlock0", "testBlock1", "testBlock2"};
     public BlockTest(int id, Material par2Material)
     {
           super(id, par2Material);
     }
     @SideOnly(Side.CLIENT)
     private Icon[] icons;
     
     /*@SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister){
         icons = new Icon[Reference.ORE_PICKAXE_STONE_HARVEST_TOTAL];
         
         for(int i = 0; i < icons.length; i++){
             icons[i] = par1IconRegister.registerIcon(Reference.MOD_ID
                     + ":"
                     + (this.getUnlocalizedName().substring(5))+i);
         }
     }*/
     
     @SideOnly(Side.CLIENT)
     @Override
     public void registerIcons(IconRegister par1IconRegister)
     {
         this.icons = new Icon[iconNames.length];
 
         for (int i = 0; i < iconNames.length; ++i)
         {
             this.icons[i] = par1IconRegister.registerIcon(Reference.MOD_ID + ":" + iconNames[i]);
         }
     }
     
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int par1, int par2){
         return icons[par2];
     }
     
     @SideOnly(Side.CLIENT)
     public void getSubBlocks(int par1, CreativeTabs part2CreativeTabs, List par3List){
         for(int i = 0; i < 2; i++){
             //par3List.add(new ItemStack(par1, 1, i));
         }
     }
 }
