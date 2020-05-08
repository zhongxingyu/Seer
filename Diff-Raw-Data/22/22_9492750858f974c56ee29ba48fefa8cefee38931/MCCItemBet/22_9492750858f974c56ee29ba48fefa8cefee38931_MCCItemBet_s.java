 package me.DDoS.MCCasino.bet;
 
 import me.DDoS.MCCasino.MCCasino;
 import me.DDoS.MCCasino.util.MCCUtil;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author DDos
  */
 public class MCCItemBet implements MCCBet {
     
     private ItemStack item;
     private ItemStack[] wonItems;
 
     public MCCItemBet(ItemStack item) {
 
         this.item = item;
        MCCasino.log.info("Bet amount: " + item.getAmount());
     }
 
     @Override
     public void applyMultiplier(int multiplier) {
 
         if (((item.getAmount() * multiplier)) <= 64) {
 
             wonItems = new ItemStack[1];
             wonItems[0] = new ItemStack(item.getTypeId(), item.getAmount() * multiplier);
             return;
 
         }
 
         int totalItems = (item.getAmount() * multiplier);
         int numberOfFullStacks = totalItems / 64;
         int oddStack = totalItems % 64;
         int totalNumberOfStack = (oddStack > 0) ? (numberOfFullStacks + 1) : numberOfFullStacks;
         int ID = item.getTypeId();
 
         wonItems = new ItemStack[totalNumberOfStack];
 
         for (int i = 0; i < numberOfFullStacks; i++) {
 
             wonItems[i] = new ItemStack(ID, 64);
 
         }
 
         if (oddStack > 0) {
 
            wonItems[wonItems.length - 1] = new ItemStack(ID, oddStack);
 
        }
     }
     
     @Override
     public void giveReward(Player player) {
 
         player.getInventory().addItem(wonItems);
        player.updateInventory();
        MCCUtil.tell(player, "You received " + getTotalItems(wonItems) + " of item '" + wonItems[0].getType().toString().toLowerCase() + "'.");
         
     }
     
    private int getTotalItems(ItemStack[] items) {
 
         int total = 0;
         
        for (ItemStack i : items) {
            MCCasino.log.info("Reward amount: " + i.getAmount());
             total += i.getAmount();
             
         }
         
         return total;
         
     }
 }
