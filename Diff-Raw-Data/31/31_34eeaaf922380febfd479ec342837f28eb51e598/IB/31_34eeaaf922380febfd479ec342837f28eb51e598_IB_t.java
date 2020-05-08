 package obt.block;
 
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 
 public class IB extends ItemBlock
 {
     public IB(int par1) {
         super(par1);
         setHasSubtypes(true);
     }
     
     @Override
     public String getUnlocalizedName(ItemStack itemStack){
         String name = "";
         
         switch(itemStack.getItemDamage()){
             case 0:
                 name = "Magic";
                 break;
             case 1:
                 name = "Dull";
                 break;
            case 2:
                name = "Infin";
                break;
             default:
                 name = "broken";
                 break;
         }
         
         return super.getUnlocalizedName() + "." + name;
     }
     
     @Override
     public int getMetadata(int par1){
         return par1;
     }
 }
