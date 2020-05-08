 package to.joe.decapitation.command;
 
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import to.joe.decapitation.Bounty;
 import to.joe.decapitation.Decapitation;
 import to.joe.decapitation.Head;
 
 public class BountyCommand implements CommandExecutor {
 
     Decapitation plugin;
 
     public BountyCommand(Decapitation decapitation) {
         plugin = decapitation;
     }
     
     private void sendHelp(CommandSender sender) {
         sender.sendMessage(ChatColor.RED + "/bounty search [username] - search for a bounty on a player");
         sender.sendMessage(ChatColor.RED + "/bounty list <page> - list current bounties");
         sender.sendMessage(ChatColor.RED + "/bounty place [username] [price] - place a bounty on a player");
         sender.sendMessage(ChatColor.RED + "/bounty claim - claim the bounty of the head you are holding");
         sender.sendMessage(ChatColor.RED + "/bounty remove [username] - remove the bounty of a player");
         sender.sendMessage(ChatColor.RED + "/bounty listown - list unclaimed bounties you have created");
         sender.sendMessage(ChatColor.RED + "/bounty redeem - claim any heads that are owed to you");
         sender.sendMessage(ChatColor.RED + "Current tax rate is " + plugin.getTax() + "%");
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "Players only");
             return true;
         }
         if (!plugin.bounties) {
             sender.sendMessage(ChatColor.RED + "Bounties are not enabled");
             return true;
         }
         if (args.length == 0) {
             sendHelp(sender);
             return true;
         }
         Player p = (Player) sender;
         if (args.length == 1) {
             if (args[0].equalsIgnoreCase("list")) {
                 try {
                     ArrayList<Bounty> bounties = plugin.getDsi().getBounties(0, 9);
                     if (bounties.size() > 0) {
                         sender.sendMessage(ChatColor.GREEN + "=========" + ChatColor.GOLD + "Bounties [Page 1 of " + (plugin.getDsi().getNumBounties() + 8) / 9 + "]" + ChatColor.GREEN + "=========");
                         for (Bounty b : bounties) {
                             sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted());
                         }
                     } else {
                         sender.sendMessage(ChatColor.RED + "There are no bounties");
                     }
                 } catch (SQLException e) {
                     plugin.getLogger().log(Level.SEVERE, "Error getting list of bounties", e);
                     sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("claim")) {
                 if (p.getItemInHand().getTypeId() != Decapitation.HEAD) {
                     sender.sendMessage(ChatColor.RED + "That's not a head");
                     return true;
                 }
                 Head h = new Head((CraftItemStack) p.getItemInHand());
                 if (!h.isNamed()) {
                     sender.sendMessage(ChatColor.RED + "That head is not named");
                     return true;
                 }
                 try {
                     String hunted = h.getName();
                     Bounty b = plugin.getDsi().getBounty(hunted);
                     if (b != null) {
                         b.setHunter(p.getName());
                         b.setHunted(hunted);
                         b.setTurnedIn(new Timestamp(new Date().getTime()));
                         plugin.getDsi().updateBounty(b);
                         Decapitation.economy.depositPlayer(p.getName(), b.getReward());
                         sender.sendMessage(ChatColor.GREEN + "Sucessfully turned in bounty on " + b.getHunted() + " for " + Decapitation.economy.format(b.getReward()));
                         p.setItemInHand(new ItemStack(0));
                         Player i = plugin.getServer().getPlayer(b.getIssuer());
                         if (i != null) {
                             CraftItemStack c = new CraftItemStack(Decapitation.HEAD, 1, (short) 0, (byte) 3);
                             new Head(c).setName(hunted);
                             int empty = ((Player) sender).getInventory().firstEmpty();
                            if (empty > 35) {
                                 i.sendMessage(ChatColor.RED + "Not enough room in your inventory to give you a skull");
                                 return true;
                             }
                             ((Player) sender).getInventory().setItem(empty, c);
                             b.setRedeemed(new Timestamp(new Date().getTime()));
                             plugin.getDsi().updateBounty(b);
                         }
                     } else {
                         sender.sendMessage(ChatColor.RED + "There does not appear to be a bounty on that head");
                         return true;
                     }
                 } catch (SQLException e) {
                     plugin.getLogger().log(Level.SEVERE, "Error claiming bounty", e);
                     sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                     return true;
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("listown")) {
                 try {
                     ArrayList<Bounty> bounties = plugin.getDsi().getOwnBounties(p.getName());
                     if (bounties.size() > 0) {
                         sender.sendMessage(ChatColor.GREEN + "=========" + ChatColor.GOLD + "Your bounties" + ChatColor.GREEN + "=========");
                         for (Bounty b : bounties) {
                             sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted());
                         }
                     } else {
                         sender.sendMessage(ChatColor.RED + "You have no bounties");
                     }
                 } catch (SQLException e) {
                     plugin.getLogger().log(Level.SEVERE, "Error getting list of bounties", e);
                     sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("redeem")) {
                 try {
                     ArrayList<Bounty> bounties = plugin.getDsi().getUnclaimedBounties(p.getName());
                     for (Bounty b : bounties) {
                         CraftItemStack c = new CraftItemStack(Decapitation.HEAD, 1, (short) 0, (byte) 3);
                         new Head(c).setName(b.getHunted());
                         int empty = ((Player) sender).getInventory().firstEmpty();
                        if (empty > 35) {
                             sender.sendMessage(ChatColor.RED + "Not enough free room");
                             return true;
                         }
                         ((Player) sender).getInventory().setItem(empty, c);
                         b.setRedeemed(new Timestamp(new Date().getTime()));
                         plugin.getDsi().updateBounty(b);
                     }
                 } catch (SQLException e) {
                     plugin.getLogger().log(Level.SEVERE, "Error getting list of unredeemed bounties", e);
                     sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                 }
                 return true;
             }
             sendHelp(sender);
             return true;
         }
         if (args.length == 2) {
             if (args[0].equalsIgnoreCase("search")) {
                 try {
                     ArrayList<Bounty> bounties = plugin.getDsi().getBounties(args[1]);
                     int count = 0;
                     if (bounties.size() > 0) {
                         sender.sendMessage(ChatColor.GREEN + "=========" + ChatColor.GOLD + "Bounties matching " + args[1] + " " + ChatColor.GREEN + "=========");
                         for (Bounty b : bounties) {
                             sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted());
                             count++;
                             if (count == 9)
                                 return true;
                         }
                     } else {
                         sender.sendMessage(ChatColor.RED + "No bounties match your query");
                     }
                 } catch (SQLException e) {
                     plugin.getLogger().log(Level.SEVERE, "Error searching for bounties", e);
                     sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                     return true;
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("list")) {
                 try {
                     int page = Integer.parseInt(args[1]);
                     ArrayList<Bounty> bounties = plugin.getDsi().getBounties((page - 1) * 9, page * 9);
                     if (bounties.size() > 0) {
                         sender.sendMessage(ChatColor.GREEN + "=========" + ChatColor.GOLD + "Bounties [Page 1 of " + (plugin.getDsi().getNumBounties() + 8) / 9 + "]" + ChatColor.GREEN + "=========");
                         for (Bounty b : bounties) {
                             sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted());
                         }
                     } else {
                         sender.sendMessage(ChatColor.RED + "There are no bounties");
                     }
                 } catch (NumberFormatException e) {
                     sender.sendMessage(ChatColor.RED + "That's not a number");
                 } catch (SQLException e) {
                     plugin.getLogger().log(Level.SEVERE, "Error getting list of bounties", e);
                     sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("remove")) {
                 try {
                     Bounty bounty = plugin.getDsi().getBounty(args[1], p.getName());
                     if (bounty != null) {
                         plugin.getDsi().deleteBounty(bounty);
                         Decapitation.economy.depositPlayer(p.getName(), bounty.getReward() - bounty.getReward() * plugin.getTax());
                         sender.sendMessage(ChatColor.GREEN + "Deleted bounty against " + bounty.getHunted() + " for " + Decapitation.economy.format(bounty.getReward()));
                         sender.sendMessage(ChatColor.GREEN + "You have been refunded " + Decapitation.economy.format(bounty.getReward() - bounty.getReward() * plugin.getTax()));
                     } else {
                         sender.sendMessage(ChatColor.RED + "No matches");
                     }
                 } catch (SQLException e) {
                     plugin.getLogger().log(Level.SEVERE, "Error deleting bounty", e);
                     sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                 }
                 return true;
             }
             sendHelp(sender);
             return true;
         }
         if (args.length == 3 && args[0].equalsIgnoreCase("place")) {
             try {
                 Bounty bounty = plugin.getDsi().getBounty(args[1], p.getName());
                 int reward = Integer.parseInt(args[2]);
                 if (Decapitation.economy.has(p.getName(), reward + reward * plugin.getTax())) {
                     if (bounty == null) {
                         bounty = new Bounty(p.getName(), args[1], reward);
                         plugin.getDsi().addBounty(bounty);
                         Decapitation.economy.withdrawPlayer(p.getName(), reward + reward * plugin.getTax());
                         sender.sendMessage(ChatColor.GREEN + "Added bounty against " + args[1]);
                         sender.sendMessage(ChatColor.GREEN + "You have been charged " + Decapitation.economy.format(reward + reward * plugin.getTax()));
                     } else {
                         bounty.setReward(bounty.getReward() + reward);
                         plugin.getDsi().updateBounty(bounty);
                         Decapitation.economy.withdrawPlayer(p.getName(), reward + reward * plugin.getTax());
                         sender.sendMessage(ChatColor.GREEN + "Added money to existing bounty against " + args[1]);
                         sender.sendMessage(ChatColor.GREEN + "You have been charged " + Decapitation.economy.format(reward + reward * plugin.getTax()));
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "You don't have enough money (" + Decapitation.economy.format(reward + reward * plugin.getTax()) + ")");
                 }
             } catch (NumberFormatException e) {
                 sender.sendMessage(ChatColor.RED + "That's not a number");
             } catch (SQLException e) {
                 plugin.getLogger().log(Level.SEVERE, "Error adding bounty", e);
                 sender.sendMessage(ChatColor.RED + "Something went wrong :(");
             }
         }
         sendHelp(sender);
         return true;
     }
 
 }
