 package fr.aumgn.tobenamed.config;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 import fr.aumgn.tobenamed.util.TBNUtil;
 
 public class Bonus {
 
     private int id = 1;
     private byte data = 0;
     private short amount = 1;
     private short randAmountDiff = 0;
 
     public Bonus(int id, int amount, int randAmountDiff) {
         this(id, 0, amount, randAmountDiff);
     }
 
     public Bonus(int id, int data, int amount, int randAmountDiff) {
         this.id = id;
         this.data = (byte) data;
         this.amount = (short) amount;
         this.randAmountDiff = (short) randAmountDiff;
     }
 
     public ItemStack toItemStack() {
         short actualAmount;
         if (randAmountDiff == 0) {
             actualAmount = amount;
         } else {
             int amountDiff = TBNUtil.getRandom().nextInt(randAmountDiff * 2);
             amountDiff -= randAmountDiff;
             int max = Material.getMaterial(id).getMaxStackSize();
            actualAmount =(short) Math.min(max, amount + amountDiff);
         }
         return new ItemStack(id, actualAmount, (short) 0, data);
     }
 }
