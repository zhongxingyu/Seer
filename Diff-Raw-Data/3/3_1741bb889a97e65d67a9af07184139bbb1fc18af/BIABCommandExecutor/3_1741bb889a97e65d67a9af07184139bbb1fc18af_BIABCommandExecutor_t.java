 package com.norcode.bukkit.buildinabox;
 
 import com.norcode.bukkit.schematica.Selection;
 import com.norcode.bukkit.schematica.exceptions.SchematicSaveException;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabExecutor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.metadata.FixedMetadataValue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 public class BIABCommandExecutor implements TabExecutor {
     BuildInABox plugin;
     public BIABCommandExecutor(BuildInABox buildInABox) {
         this.plugin = buildInABox;
     }
 
 
     public boolean onCommand(CommandSender sender, Command command, String alias, String[] params) {
         LinkedList<String> args = new LinkedList<String>(Arrays.asList(params));
         if (args.size() == 0) {
             return false;
         }
         String action = args.pop().toLowerCase();
         if (action.equals("give")) {
             cmdGive(sender, args);
             return true;
         } else if (action.equals("save")) {
             cmdSave(sender, args);
             return true;
         } else if (action.equals("list")) {
             cmdList(sender, args);
             return true;
         } else if (action.equals("delete")) {
             cmdDelete(sender, args);
             return true;
         } else if (action.startsWith("setdisplayname")) {
             cmdSetDisplayName(sender, args);
             return true;
         } else if (action.startsWith("setdesc")) {
             cmdSetDescription(sender, args);
             return true;
         } else if (action.startsWith("permanent")) {
             cmdPermanent(sender, args);
             return true;
         } else if (action.equals("pos1")) {
             cmdSetSelectionPoint(1, sender, args);
            return true;
         } else if (action.equals("pos2")) {
             cmdSetSelectionPoint(2, sender, args);
            return true;
         }
         sender.sendMessage(BuildInABox.getErrorMsg("unexpected-argument", action));
         return true;
     }
 
     private void cmdSetSelectionPoint(int point, CommandSender sender, LinkedList<String> args) {
         if (!sender.hasPermission("biab.select")) {
             sender.sendMessage(BuildInABox.getErrorMsg("no-permission"));
             return;
         }
         if (!(sender instanceof Player)) {
             sender.sendMessage(BuildInABox.getErrorMsg("cannot-use-from-console"));
             return;
         }
         Player p = (Player) sender;
         Selection sel = plugin.getPlayerSession(p).getSelection();
         Location l = p.getLocation().getBlock().getLocation();
         switch (point) {
             case 1:
                 sel.setPt1(l);
                 p.sendMessage(BuildInABox.getSuccessMsg("selection-pt1-set", l.toVector()));
                 break;
             case 2:
                 sel.setPt2(p.getLocation().getBlock().getLocation());
                 p.sendMessage(BuildInABox.getSuccessMsg("selection-pt2-set", l.toVector()));
                 break;
             default:
                 return;
         }
 
     }
 
     private void cmdPermanent(CommandSender sender, LinkedList<String> args) {
         if (!sender.hasPermission("biab.permanent")) {
             sender.sendMessage(BuildInABox.getErrorMsg("no-permission"));
             return;
         }
         if (!(sender instanceof Player)) {
             sender.sendMessage(BuildInABox.getErrorMsg("cannot-use-from-console"));
             return;
         }
         sender.sendMessage(BuildInABox.getNormalMsg("punch-to-make-permanent"));
         ((Player)sender).setMetadata("biab-permanent-timeout", new FixedMetadataValue(plugin, System.currentTimeMillis()+3000));
         return;
     }
 
     private void cmdDelete(CommandSender sender, LinkedList<String> args) {
         if (!sender.hasPermission("biab.delete")) {
             sender.sendMessage(BuildInABox.getErrorMsg("no-permission"));
             return;
         }
         if (args.size() == 1) {
             String planName = args.pop();
             BuildingPlan plan = BuildInABox.getInstance().getDataStore().getBuildingPlan(planName);
             if (plan == null) {
                 sender.sendMessage(BuildInABox.getErrorMsg("unknown-building-plan", planName));
             } else {
                 BuildInABox.getInstance().getDataStore().deleteBuildingPlan(plan);
                 sender.sendMessage(BuildInABox.getSuccessMsg("building-plan-deleted", planName));
             }
         } else {
             sender.sendMessage(BuildInABox.getNormalMsg("cmd-delete-usage"));
         }
         
     }
 
     private void cmdSetDescription(CommandSender sender, LinkedList<String> args) {
         if (!sender.hasPermission("biab.save")) {
             sender.sendMessage(BuildInABox.getErrorMsg("no-permission"));
             return;
         }
         if (args.size() < 2) {
             sender.sendMessage(BuildInABox.getNormalMsg("cmd-setdesc-usage"));
             return;
         }
         BuildingPlan plan = BuildInABox.getInstance().getDataStore().getBuildingPlan(args.peek());
         if (plan == null) {
             sender.sendMessage(BuildInABox.getErrorMsg("unknown-building-plan", args.peek()));
             return;
         }
         args.pop();
         plan.description = parseDescription(args);
         BuildInABox.getInstance().getDataStore().saveBuildingPlan(plan);
         sender.sendMessage(BuildInABox.getSuccessMsg("description-saved", plan.getName()));
     }
 
     private void cmdSetDisplayName(CommandSender sender, LinkedList<String> args) {
         if (!sender.hasPermission("biab.save")) {
             sender.sendMessage(BuildInABox.getErrorMsg("no-permission"));
             return;
         }
         if (args.size() < 2) {
             sender.sendMessage(BuildInABox.getNormalMsg("cmd-setdisplayname-usage"));
             return;
         }
         BuildingPlan plan = BuildInABox.getInstance().getDataStore().getBuildingPlan(args.peek());
         if (plan == null) {
             sender.sendMessage(BuildInABox.getErrorMsg("unknown-building-plan", args.peek()));
             return;
         }
         args.pop();
         String dn = "";
         while (!args.isEmpty()) {
             dn += ChatColor.translateAlternateColorCodes('&', args.pop()) + " ";
         }
         dn = dn.trim();
         if (dn.equals("")) {
             dn = plan.getName();
         }
         plan.setDisplayName(dn);
         BuildInABox.getInstance().getDataStore().saveBuildingPlan(plan);
         sender.sendMessage(BuildInABox.getSuccessMsg("building-plan-saved", plan.getName()));
     }
 
     private List<String> parseDescription(LinkedList<String> args) {
         List<String> lines = new ArrayList<String>();
         String line = "";
         String w;
         while (!args.isEmpty()) {
             w = args.pop().trim();
             if (w.equals("|")) {
                 lines.add(line);
                 line = "";
             } else {
                 line += ChatColor.translateAlternateColorCodes('&', w) + " ";
             }
         }
         if (!line.equals(" ")) {
             lines.add(line);
         }
         return lines;
     }
     private void cmdList(CommandSender sender, LinkedList<String> args) {
         int page = 1;
         if (args.size() > 0) {
             try {
                 page = Integer.parseInt(args.peek());
             } catch (IllegalArgumentException ex) {
                 sender.sendMessage(BuildInABox.getErrorMsg("invalid-page", args.peek()));
                 return;
             }
         }
         int numPages = (int) Math.ceil(plugin.getDataStore().getAllBuildingPlans().size() / 8.0f);
         if (numPages == 0) {
             sender.sendMessage(BuildInABox.getNormalMsg("no-building-plans"));
             return;
         }
         List<BuildingPlan> plans = new ArrayList<BuildingPlan>(plugin.getDataStore().getAllBuildingPlans());
         List<String> lines = new ArrayList<String>();
         lines.add(BuildInABox.getNormalMsg("available-building-plans", page, numPages));
         for (int i=8*(page-1);i<8*(page);i++) {
             if (i<plans.size()) {
                 lines.add(ChatColor.GOLD + " * " + ChatColor.GRAY + plans.get(i).getName() + " - " + plans.get(i).getDisplayName());
             }
         }
         sender.sendMessage(lines.toArray(new String[lines.size()]));
     }
 
     public void cmdSave(CommandSender sender, LinkedList<String> args) {
         if (!sender.hasPermission("biab.save")) {
             sender.sendMessage(BuildInABox.getErrorMsg("no-permission"));
             return;
         } else if (!(sender instanceof Player)) {
             sender.sendMessage(BuildInABox.getErrorMsg("cannot-use-from-console"));
             return;
         }
         String buildingName = args.pop();
         try {
             int isNumeric = Integer.parseInt(buildingName, 16);
             sender.sendMessage(BuildInABox.getErrorMsg("invalid-building-plan-name", buildingName));
             return;
         } catch (IllegalArgumentException ex) {}
         BuildingPlan plan = null;
         try {
             plan = BuildingPlan.fromClipboard(plugin, (Player) sender, buildingName);
         } catch (SchematicSaveException e) {
             sender.sendMessage(BuildInABox.getErrorMsg("save-failed"));
             return;
         }
         String displayName = "";
         while (args.size() > 0 && !args.peek().equals("|")) {
             displayName += args.pop() + " ";
         }
         if (!displayName.equals("")) {
             plan.setDisplayName(displayName.trim());
         }
         if (args.size() > 0) {
             plan.setDescription(parseDescription(args));
         }
         if (plan != null) {
             sender.sendMessage(BuildInABox.getSuccessMsg("building-plan-saved", buildingName));
             plugin.getDataStore().saveBuildingPlan(plan);
         }
     }
 
     public void cmdGive(CommandSender sender, LinkedList<String> args) {
         Player targetPlayer = null;
         if (args.size() >= 2) {
             String name = args.pop();
             List<Player> matches = plugin.getServer().matchPlayer(name);
             if (matches.size() == 1) {
                 targetPlayer = matches.get(0);
             } else {
                 sender.sendMessage(BuildInABox.getErrorMsg("unknown-player", name));
                 return;
             }
         }
         if (targetPlayer == null) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage(BuildInABox.getErrorMsg("cannot-give-to-console"));
                 return;
             } else {
                 targetPlayer = (Player) sender;
             }
         }
         if (args.size() == 1) {
             String planName = args.pop().toLowerCase();
             BuildingPlan plan = plugin.getDataStore().getBuildingPlan(planName);
             if (plan == null) {
                 sender.sendMessage(BuildInABox.getErrorMsg("unknown-building-plan", planName));
                 return;
             }
             if (!sender.hasPermission("biab.give."+plan.getName().toLowerCase())) {
                 sender.sendMessage(BuildInABox.getErrorMsg("no-permission"));
                 return;
             }
             //ChestData data = plugin.getDataStore().createChest(plan.getName());
             ItemStack stack = new ItemStack(plugin.cfg.getChestBlockId(), 1);
             ItemMeta meta = plugin.getServer().getItemFactory().getItemMeta(Material.getMaterial(plugin.cfg.getChestBlockId()));
             meta.setDisplayName(plan.getDisplayName());
             List<String> lore = new ArrayList<String>();
             lore.add(BuildInABox.LORE_PREFIX + BuildInABox.LORE_HEADER);
             lore.add(ChatColor.BLACK + plan.getName());
             lore.addAll(plan.getDescription());
             meta.setLore(lore);
             stack.setItemMeta(meta);
             if (targetPlayer.getInventory().addItem(stack).size() > 0) {
                 targetPlayer.getWorld().dropItem(targetPlayer.getLocation(), stack);
             }
             sender.sendMessage(BuildInABox.getSuccessMsg("cmd-give-success", plan.getDisplayName(), targetPlayer.getName()));
         } else {
             sender.sendMessage(BuildInABox.getNormalMsg("cmd-give-usage"));
         }
     }
 
     public List<String> onTabComplete(CommandSender sender, Command cmd,
             String label, String[] params) {
       LinkedList<String> args = new LinkedList<String>(Arrays.asList(params));
       LinkedList<String> results = new LinkedList<String>();
       String action = null;
       if (args.size() >= 1) {
           action = args.pop().toLowerCase();
       } else {
           return results;
       }
       
       if (args.size() == 0) {
           if ("permanent".startsWith(action) && sender.hasPermission("biab.permanent")) {
               results.add("permanent");
           }
           if ("setdescription".startsWith(action) && sender.hasPermission("biab.save")) {
               results.add("setdescription");
           }
           if ("list".startsWith(action)) {
               results.add("list");
           } 
           if ("save".startsWith(action) && sender.hasPermission("biab.save")) {
               results.add("save");
           }
           if ("give".startsWith(action)) {
               results.add("give");
           }
           if ("pos1".startsWith(action) && sender.hasPermission("biab.select")) {
               results.add("pos1");
           }
           if ("pos2".startsWith(action) && sender.hasPermission("biab.select")) {
               results.add("pos2");
           }
       } else if (args.size() == 1) {
           if (action.equals("save") || action.equals("give") || action.equals("setdisplayname") || action.equals("setdescription")) {
               if (sender.hasPermission("biab." + action)) {
                   for (BuildingPlan plan: plugin.getDataStore().getAllBuildingPlans()) {
                       if (plan.getName().toLowerCase().startsWith(args.peek().toLowerCase())) {
                           results.add(plan.getName());
                       }
                   }
                   if (action.equals("give")) {
                       for (Player p: plugin.getServer().getOnlinePlayers()) {
                           if (p.getName().toLowerCase().startsWith(args.peek().toLowerCase())) {
                               results.add(p.getName());
                           }
                       }
                   }
               }
           }
       } else if (args.size() == 2) {
           if (action.equals("give")) {
               for (BuildingPlan plan: plugin.getDataStore().getAllBuildingPlans()) {
                   if (plan.getName().toLowerCase().startsWith(args.peek().toLowerCase())) {
                       results.add(plan.getName());
                   }
               }
           }
       }
       return results;
     }
 }
