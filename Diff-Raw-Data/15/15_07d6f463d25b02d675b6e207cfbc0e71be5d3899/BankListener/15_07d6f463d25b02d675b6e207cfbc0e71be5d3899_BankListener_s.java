 /**
  * Bank Plugin Listener
  * @author Doug Frazer
  */
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.LinkedList;
 import java.text.DecimalFormat;
 
 public class BankListener extends PluginListener { 
 
     private ItemCache cache;
 
     public BankListener (ItemCache cache) {
         this.cache = cache;        
     }
 
     public boolean onCraftInventoryChange(Player p) {
         BankDB database = new BankDB();
         if (database.getInventoryLock(p))
             return true;
         return false;
     }
 
     public boolean onCommand (Player player, String[] split) {
         BankPlayer myBank = new BankPlayer(player);
         final int MaxBanksPerPage = 6;
 
         if (split[0].equalsIgnoreCase("/bank") || 
             split[0].equalsIgnoreCase("/withdraw") || 
             split[0].equalsIgnoreCase("/deposit")) { 
             if (!player.canUseCommand(split[0])) {
                 return false;
             }
 
             // Check their distance from the bank
             if (!myBank.checkDistanceFromBank()) {
                 player.sendMessage("You must be closer to the bank to interact with it.");
                 player.sendMessage("Warp to the bank using /warp bank.");
                 return true;
             }
         }
         
         if (split[0].equalsIgnoreCase("/bank")) {
             // Figure out what page they want to see
             int page = 1;
             if (split.length == 2) {
                 // they specified a page
                 try { page = Integer.parseInt(split[1]); }
                 catch (NumberFormatException e) {
                     player.sendMessage("Syntax: /bank <page number>");
                 }
             }
 
             LinkedList<BankItem> bank = myBank.inv.getBank(player);
             page = page - 1;
             page = page*myBank.inv.MaxItemsPerPage;
             int i = 0;
             if(myBank.inv == null || bank.size() == 0) {
                 player.sendMessage("Your bank is empty.");
                 return true;
             } else {
                 while(page+i < bank.size()) {
                     BankItem currItem = bank.get(page+i);
                     int curr_index = page + i + 1;
 
                     player.sendMessage(Colors.Green + curr_index + ") " + Colors.Rose +
                                        currItem.quantity + 
                                        "x " + 
                                        cache.getItemName(currItem.item_id));
                     i++;
                     if (i == myBank.inv.MaxItemsPerPage) 
                         break;
                 }
                 int curr_page = page/myBank.inv.MaxItemsPerPage + 1;
                 int max_pages = (myBank.inv.item_count + myBank.inv.MaxItemsPerPage - 1)/myBank.inv.MaxItemsPerPage;
                 if (curr_page <= max_pages)
                     player.sendMessage(Colors.LightBlue + "Total bank items: " + myBank.inv.item_count + Colors.Blue +
                                        " [ Page " + curr_page + " of " + max_pages + " ]");
                 else
                     player.sendMessage("You only have " + max_pages + " pages of items.");
             }
 
             return true;
 
         } else if (split[0].equalsIgnoreCase("/deposit")) { 
             if (split.length == 1) { 
                 BankItem[] deposited = myBank.deposit();
                 if (deposited == null) {
                     player.sendMessage("Put items in your crafting square to deposit them");
                     return true;
                 }
                 for (BankItem i : deposited) {
                     if (i == null) continue;
                     if (i.item_id == 0) continue;
                     player.sendMessage("Successfully deposited " + i.quantity +
                                        " of " + cache.getItemName(i.item_id));
                 }
                 return true;
             } else {
                 player.sendMessage("Syntax: /deposit");
                 return true;
             }
 		} else if (split[0].equalsIgnoreCase("/withdraw")) {
             int index = 0;
             int amount = 0;
 
             if (split.length == 2) {
                 // no amount specified
                 if (split[1].equals("")) {
                     player.sendMessage("Syntax: /withdraw <index> <amount>");
                     return true;
                 } else { 
                     try {
                         index = Integer.parseInt(split[1]);
                     } catch (NumberFormatException e) {
                         player.sendMessage("Syntax: /withdraw <index> <amount>");
                         return true;
                     }
                     amount = -1;
                 }
             } else if (split.length == 3) {
                 // index and amount specified, pass these values on
                 try {
                     amount = Integer.parseInt(split[2]);
                     index = Integer.parseInt(split[1]);
                 } catch (NumberFormatException e) {
                     player.sendMessage("Syntax: /withdraw <index> <amount>");
                     return true;
                 }
             } else {
                 player.sendMessage("Syntax: /withdraw <index> <amount>");
                 return true;
             }
             
             BankItem withdrawn = myBank.withdraw(index-1, amount);
             if (withdrawn == null) return false;
             player.sendMessage("Successfully withdrew " + withdrawn.quantity +
                                 " of " + cache.getItemName(withdrawn.item_id));
             return true;
         }  else if (split[0].equalsIgnoreCase("/listbank") || 
                     split[0].equalsIgnoreCase("/listbanks")) {
 
             int i = 0;
             if (split.length == 2) {
                 try { i = Integer.parseInt(split[1]); i -= 1;}
                 catch (NumberFormatException e) {
                     player.sendMessage("Syntax: /listbanks <page>");
                     return true;
                 }
             } else if (split.length > 2) {
                 player.sendMessage("Syntax: /listbanks <page>");
                 return true;
             }
             LinkedList<BankLocation> locs = myBank.getBanks();
             BankLocation bank;
             i = i*MaxBanksPerPage;
             int num_banks = locs.indexOf(locs.getLast());
             int bank_pages = num_banks/MaxBanksPerPage+1;
             int curr_page = i/MaxBanksPerPage + 1;
             int j = 0;
 
             while (j < MaxBanksPerPage) {
                 try { bank = locs.get(i); }
                 catch (IndexOutOfBoundsException e) {
                     player.sendMessage("There are only " + bank_pages + " pages of banks.");
                     return true;
                 }
                 Location currLoc = player.getLocation();
                 double distance = 0;
                 distance = Math.sqrt(Math.pow(bank.x - currLoc.x,2) +
                                      Math.pow(bank.y - currLoc.y,2) +
                                      Math.pow(bank.z - currLoc.z,2));
                 if (distance < bank.distance) {
                     player.sendMessage(Colors.Blue + bank.name + 
                                        Colors.Green + " within range.");
                 } else {
                     String direction = "";
                     double xd,zd = 0;
                     double rotX = Math.toDegrees(Math.atan((currLoc.z - bank.z)/(currLoc.x - bank.x)));
                     xd = bank.x - currLoc.x;
                     if (xd > 0) {
                         // Some way south...
                         if (rotX < -68.5) direction = "East";
                         else if (rotX < -22.5) direction = "Southeast";
                         else if (rotX < 22.5) direction = "South";
                         else if (rotX < 68.5) direction = "Southwest";
                         else direction = "West";
                     } else {
                         // some way north
                         if (rotX < -68.5) direction = "West";
                         else if (rotX < -22.5) direction = "Northwest";
                         else if (rotX < 22.5) direction = "North";
                         else if (rotX < 68.5) direction = "Northeast";
                         else direction = "East";
                     }
                     DecimalFormat df = new DecimalFormat("#.#");
                     player.sendMessage(Colors.Blue + bank.name + Colors.Gray + "  " 
                                        + df.format(distance) + "  blocks "
                                        + direction);
                 }
                 j++;
                 if (i == locs.indexOf(locs.getLast()))
                     break;
                 else
                     i++;
             }
             num_banks += 1;
             player.sendMessage(Colors.LightBlue + "Total banks found: " + num_banks + "  " + Colors.Blue + "[ Page " + curr_page + " / " + bank_pages + " ]");
             return true;
 
         } else if (split[0].equalsIgnoreCase("/setbank")) {
             if (split.length == 1 || split.length == 2 || split.length > 3) {
                 player.sendMessage("Syntax: /setbank [name] [distance]");
                 return true;
             }
             int distance = 0;
             try { distance = Integer.parseInt(split[2]); }
             catch (NumberFormatException e) {
                 player.sendMessage("Syntax: /setbank [name] [distance]");
             }
             if (myBank.setBank(split[1], (double)distance))
                 player.sendMessage("Successfully added bank " + split[1] + " at your current location.");
             else
                 player.sendMessage("Failed to add bank " + split[1]);
             return true;
         } else if (split[0].equalsIgnoreCase("/removebank")) {
             BankDB database = new BankDB();
             if (split.length != 2) {
                 player.sendMessage("Syntax: /removebank [name]");
                 return true;
             }
             if(database.removeBank(split[1]))
                 player.sendMessage("Successfully removed bank " + split[1]);
             else
                 player.sendMessage("Failed to remove bank " + split[1]);
         }
     
         return false;
     }
     
 
 }
