 package GP1.API;
 
 import forge.ITextureProvider;
 import net.minecraft.server.*;
 
 public class GPApiPickaxe extends ItemTool implements ITextureProvider
 {
     private static Block blocksEffectiveAgainst[];
     private String texturefile;
 
     public GPApiPickaxe(int i, EnumToolMaterial enumtoolmaterial)
     {
         super(i, 2, enumtoolmaterial, blocksEffectiveAgainst);
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
         if (block == Block.OBSIDIAN)
         {
             return b.d() == 3;
         }
 
         if (block == Block.DIAMOND_BLOCK || block == Block.DIAMOND_ORE)
         {
             return b.d() >= 2;
         }
 
         if (block == Block.GOLD_BLOCK || block == Block.GOLD_ORE)
         {
             return b.d() >= 2;
         }
 
         if (block == Block.IRON_BLOCK || block == Block.IRON_ORE)
         {
             return b.d() >= 1;
         }
 
         if (block == Block.LAPIS_BLOCK || block == Block.LAPIS_ORE)
         {
             return b.d() >= 1;
         }
 
         if (block == Block.REDSTONE_ORE || block == Block.GLOWING_REDSTONE_ORE)
         {
             return b.d() >= 2;
         }
 
         if (block.material == Material.STONE)
         {
             return true;
         }
         else
         {
             return block.material == Material.ORE;
         }
     }
 
     /**
      * Returns the strength of the stack against a given block. 1.0F base, (Quality+1)*2 if correct blocktype, 1.5F if
      * sword
      */
     public float getDestroySpeed(ItemStack itemstack, Block block)
     {
         if (block != null && (block.material == Material.ORE || block.material == Material.STONE))
         {
             return a;
         }
         else
         {
             return super.getDestroySpeed(itemstack, block);
         }
     }
 
     public GPApiPickaxe setTextureFile(String s)
     {
         texturefile = s;
         return this;
     }
 
     static
     {
         blocksEffectiveAgainst = (new Block[]
                 {
                     Block.COBBLESTONE, Block.DOUBLE_STEP, Block.STEP, Block.STONE, Block.SANDSTONE, Block.MOSSY_COBBLESTONE, Block.IRON_ORE, Block.IRON_BLOCK, Block.COAL_ORE, Block.GOLD_BLOCK,
                     Block.GOLD_ORE, Block.DIAMOND_ORE, Block.DIAMOND_BLOCK, Block.ICE, Block.NETHERRACK, Block.LAPIS_ORE, Block.LAPIS_BLOCK, Block.REDSTONE_ORE, Block.GLOWING_REDSTONE_ORE, Block.RAILS,
                     Block.DETECTOR_RAIL, Block.GOLDEN_RAIL
                 });
     }
 }
