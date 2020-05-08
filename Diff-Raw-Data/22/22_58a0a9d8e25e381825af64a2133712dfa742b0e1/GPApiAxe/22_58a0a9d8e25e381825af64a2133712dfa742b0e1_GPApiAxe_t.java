 package GP1.API;
 
 import forge.ITextureProvider;
 import net.minecraft.server.*;
 
 public class GPApiAxe extends ItemTool implements ITextureProvider
 {
     private static Block blocksEffectiveAgainst[];
     private String texturefile;
 
     public GPApiAxe(int i, EnumToolMaterial enumtoolmaterial)
     {
         super(i, 3, enumtoolmaterial, blocksEffectiveAgainst);
     }
 
     public String getTextureFile()
     {
         return texturefile;
     }
 
     /**
      * Returns the strength of the stack against a given block. 1.0F base, (Quality+1)*2 if correct blocktype, 1.5F if
      * sword
      */
     public float getDestroySpeed(ItemStack itemstack, Block block)
     {
         if (block != null && block.material == Material.WOOD)
         {
             return a;
         }
         else
         {
             return super.getDestroySpeed(itemstack, block);
         }
     }
 
     public GPApiAxe setTextureFile(String s)
     {
         texturefile = s;
         return this;
     }
 
     static
     {
         blocksEffectiveAgainst = (new Block[]
                 {
                     Block.WOOD, Block.BOOKSHELF, Block.LOG, Block.CHEST, Block.DOUBLE_STEP, Block.STEP, Block.PUMPKIN, Block.JACK_O_LANTERN
                 });
     }
 }
