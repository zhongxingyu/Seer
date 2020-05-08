 package shadowhax.crystalluscraft.block.item;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemBlockWithMetadata;
 import net.minecraft.item.ItemStack;
import shadowhax.crystalluscraft.block.BlockCrystalOre;
 
 public class ItemBlockCrystalOre extends ItemBlockWithMetadata {
 
     private Block theBlock;
 
     public ItemBlockCrystalOre(int par1, Block par2Block) {
 
         super(par1, par2Block);
         this.theBlock = par2Block;
     }
 
     @Override
     public String getUnlocalizedName(ItemStack stack) {
 
         String s = "";
 
         switch(stack.getItemDamage()) {
 
             case 0: {
 
                 s = "lux";
                 break;
             }
 
             case 1: {
 
                 s = "ros";
                 break;
             }
 
             case 2: {
 
                 s = "natura";
                 break;
             }
 
             case 3: {
 
                 s = "obitus";
                 break;
             }
 
             case 4: {
 
                 s = "ignis";
                 break;
             }
 
             case 5: {
 
                 s = "zephyr";
                 break;
             }
         }
 
        if(theBlock instanceof BlockCrystalOre) {

            ((BlockCrystalOre)theBlock).name = s;
        }

         return "tile." + this.theBlock.getUnlocalizedName() + "." + s;
     }
 }
