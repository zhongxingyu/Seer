 package com.gmail.ne0nx3r0.rareitems;
 
 import com.gmail.ne0nx3r0.rareitems.inventory.VirtualChest;
 import com.gmail.ne0nx3r0.rareitems.item.RareItem;
 import com.gmail.ne0nx3r0.utils.MaterialName;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class RareItemsCommandExecutor implements CommandExecutor 
 {
     @Override
     public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
         if (!(cs instanceof Player)){
             RareItems.logger.log(Level.INFO,"RI is not console-enabled.");
         }
         
         Player p = (Player) cs;
         
         if(args.length == 0)
         {
             p.sendMessage("Usage:");
             p.sendMessage("/ri claim - check-out/in RareItems");
             p.sendMessage("/ri return - check-in a RareItem you lost or traded away");
             
             return true;
         }
         else
         {
             if(args[0].equalsIgnoreCase("claim"))
             {
                 if(RareItems.USE_PERMISSIONS 
                 && !p.hasPermission("rareitems.claim") 
                 && !p.isOp())
                 {
                     p.sendMessage(ChatColor.RED+"You do not have permission to do this.");
                     
                     return true;
                 }
                 
                 if(RareItems.pm.hasRareItems(p))
                 {
                     VirtualChest vc = new VirtualChest(p.getName()+"'s RareItems");
 
                     Inventory inv = vc.getInventory();
 
                     RareItems.pm.fillWithCheckedInItems(p,inv);
 
                     p.openInventory(vc.getInventory());
                 }
                 else
                 {
                     p.sendMessage("You don't have any rare items! Ouch.");
                 }
 
                 return true;
             }
             else if(args[0].equalsIgnoreCase("return"))
             {
                 if(RareItems.USE_PERMISSIONS 
                 && !p.hasPermission("rareitems.return")
                 && !p.isOp())
                 {
                     p.sendMessage(ChatColor.RED+"You do not have permission to do this.");
                     
                     return true;
                 }
                 
                 if(args.length < 2)
                 {
                     Set<Integer> checkedOutRareItems = RareItems.pm.getCheckedOutRareItemIds(p).keySet();
                     
                     if(checkedOutRareItems != null && !checkedOutRareItems.isEmpty())
                     {
                         p.sendMessage(ChatColor.YELLOW+"Here are the items you can return:");
 
                         if(checkedOutRareItems != null)
                         {
                             for(Iterator<Integer> it = checkedOutRareItems.iterator(); it.hasNext();)
                             {
                                 int rid = it.next();
                                 RareItem ri = RareItems.pm.getRareItem(p, rid);
                                 if(ri != null)
                                 {
                                     p.sendMessage(ChatColor.YELLOW+"RID: "+ChatColor.WHITE+ri.getId() + " " + ri.getDisplayName());
                                 }
                                 else
                                 {
                                     Integer[] riData = RareItems.pm.getCheckedOutRareItemData(p, rid);
 
                                     p.sendMessage(ChatColor.YELLOW+"RID: "+ChatColor.WHITE+rid+" ("+MaterialName.getMaterialDisplayName(riData[0], riData[0].byteValue()) +")");
                                 }
                             }
                         }
 
                         p.sendMessage(ChatColor.YELLOW+"Use /ri return <rid> to exchange an it");
                     }
                     else
                     {
                         p.sendMessage(ChatColor.YELLOW+"You don't have any items checked out!");
                     }
                 }
                 else
                 {
                     int rid = 0;
                     
                     try
                     {
                         rid = Integer.parseInt(args[1]);
                     }
                     catch(Exception ex)
                     {
                         p.sendMessage(ChatColor.RED+"Invalid RID");
                         
                         return true;
                     }
                     
                     if(RareItems.pm.isCheckedOut(p,rid))
                     {
                         Integer[] riData = RareItems.pm.getCheckedOutRareItemData(p, rid);
                         
                         for(ItemStack is : p.getInventory().getContents())
                         {
                             if(is != null 
                             && is.getTypeId() == riData[0]
                             && is.getData().getData() == riData[1].byteValue()
                             && RareItems.pm.isRareItem(p,is,rid))
                             {
                                 p.getInventory().removeItem(is);
                                 
                                 /* 
                                  * It seems cruel to attempt check-in after 
                                  * removing the item, however it's better
                                  * than creating a duping glitch if someone
                                  * finds a way to make check-in fail. 
                                  * 
                                  * Additionally this is unlikely to fail since
                                  * it was just verified using isCheckedOut
                                 */
                                 if(RareItems.pm.checkInRareItem(rid, p))
                                 {
                                     p.sendMessage("Checked in RID"+rid+"!");
 
                                     return true;
                                 }
                             }
                         }
                         
                         //if it wasn't found in the last look for a clean copy
                         for(ItemStack is : p.getInventory().getContents())
                        {
                             //Basically the same check for a clean item
                             if(is != null 
                             && is.getTypeId() == riData[0]
                             && is.getData().getData() == riData[1].byteValue())
                             {
                                 p.getInventory().removeItem(is);
                                 
                                 if(RareItems.pm.checkInRareItem(rid, p))
                                 {
                                     p.sendMessage("Checked in RID"+rid+"!");
 
                                     return true;
                                 }
                             }
                         }
                         
                         p.sendMessage("You don't seem to have the item, or a clean copy of it!");
                         
                         return true;
                     }
                     else
                     {
                         p.sendMessage("RID"+rid+" is not checked out!");
                     }
                 }
             }
         }
         
         return false;
     }//End onCommand
 }
