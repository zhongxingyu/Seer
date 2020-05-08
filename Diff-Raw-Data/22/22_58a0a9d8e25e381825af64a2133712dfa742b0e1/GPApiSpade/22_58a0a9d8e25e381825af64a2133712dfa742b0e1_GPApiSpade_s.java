 package GP1.API;
 
 import forge.ITextureProvider;
import java.util.Hashtable;
 import net.minecraft.server.*;
 
 public class GPApiSpade extends ItemTool implements ITextureProvider
 {
    @SuppressWarnings({ "unused", "rawtypes" })
	private static Hashtable breakableBlocks = new Hashtable();
     private static Block blocksEffectiveAgainst[];
     private String texturefile;
 
     public GPApiSpade(int i, EnumToolMaterial enumtoolmaterial)
     {
         super(i, 1, enumtoolmaterial, blocksEffectiveAgainst);
     }
 
     public String getTextureFile()
     {
         return texturefile;
     }
 
     /**
      * Returns if the item (tool) can harvest results from the block type.
      */
     public boolean canDestroySpecialBlock(Block block)
     {
         if (block == Block.SNOW)
         {
             return true;
         }
         else
         {
             return block == Block.SNOW_BLOCK;
         }
     }
 
     public GPApiSpade setTextureFile(String s)
     {
         texturefile = s;
         return this;
     }
 
     static
     {
         blocksEffectiveAgainst = (new Block[]
                 {
                     Block.GRASS, Block.DIRT, Block.SAND, Block.GRAVEL, Block.SNOW, Block.SNOW_BLOCK, Block.CLAY, Block.SOIL, Block.SOUL_SAND, Block.MYCEL
                 });
     }
 }
