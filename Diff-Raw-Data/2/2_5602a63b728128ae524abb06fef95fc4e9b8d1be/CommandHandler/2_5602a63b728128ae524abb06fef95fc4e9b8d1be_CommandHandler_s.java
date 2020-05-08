 package net.xenosmc.mob;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabExecutor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CommandHandler implements TabExecutor {
 
     XenosMobController plugin;
 
     public CommandHandler(XenosMobController plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String lbl, String[] args) {
 
         if (!(sender instanceof Player)) {
             sender.sendMessage("This command can only be run in-game.");
             return true;
         }
         if (args.length == 0) {
             return false;
         }
 
         if (args[0].toLowerCase().equals("create")) {
             if (args.length < 7) {
                 return false;
             }
 
             String name = args[1];
             if (this.plugin.getSpawnPoints().containsKey(name.toLowerCase())) {
                 sender.sendMessage("A Spawn point by that name already exists.");
                 return true;
             }
 
             EntityType et = EntityType.fromName(args[2]);
             if (et == null) {
                 sender.sendMessage(ChatColor.RED + "Unknown entity type: " + args[2]);
                 return true;
             }
 
             double health;
             try {
                 health = Double.parseDouble(args[3]);
             } catch (IllegalArgumentException ex) {
                 sender.sendMessage("Invalid value for health: " + args[3]);
                 return true;
             }
 
             double damage;
             try {
                 damage = Double.parseDouble(args[4]);
             } catch (IllegalArgumentException ex) {
                 sender.sendMessage("Invalid value for damage: " + args[4]);
                 return true;
             }
 
             long respawnTimer;
             try {
                 respawnTimer = Long.parseLong(args[5]);
             } catch (IllegalArgumentException ex) {
                 sender.sendMessage("Invalid value for Respawn Timer: " + args[5]);
                 return true;
             }
 
             int minPlayers;
             try {
                 minPlayers = Integer.parseInt(args[6]);
             } catch (IllegalArgumentException ex) {
                 sender.sendMessage("Invalid value for Min. Players: " + args[6]);
                 return true;
             }
 
             String customName = null;
             if (args.length >= 8) {
                 customName = args[7].replace("_", " ");
             }
 
 
             Location loc = ((Player) sender).getLocation();
             SpawnPoint p = new SpawnPoint(name);
             p.setLocation(loc);
             p.setHealth(health);
             p.setDamage(damage);
             p.setEnabled(true);
             p.setMinPlayers(minPlayers);
             p.setRespawnTimer(respawnTimer);
             p.setEntityType(et);
             p.setCustomName(customName);
             plugin.addSpawnPoint(p);
             sender.sendMessage("SpawnPoint '" + name + "' created.");
             return true;
         } else if (args[0].equalsIgnoreCase("enable")) {
             if (args.length != 2) return false;
             SpawnPoint pt = plugin.getSpawnPoints().get(args[1].toLowerCase());
             if (pt == null) {
                 sender.sendMessage("Unknown Spawn Point: " + args[1]);
                 return true;
             } else if (pt.isEnabled()) {
                 sender.sendMessage("Spawn Point '" + pt.getName() + "' is already enabled.");
                 return true;
             }
             plugin.enableSpawnPoint(pt);
             sender.sendMessage("Spawn Point '" + pt.getName() + "' enabled.");
             return true;
         } else if (args[0].equalsIgnoreCase("disable")) {
             if (args.length != 2) return false;
             SpawnPoint pt = plugin.getSpawnPoints().get(args[1].toLowerCase());
             if (pt == null) {
                 sender.sendMessage("Unknown Spawn Point: " + args[1]);
                 return true;
             } else if (!pt.isEnabled()) {
                 sender.sendMessage("Spawn Point '" + pt.getName() + "' is already disabled.");
                 return true;
             }
             plugin.disableSpawnPoint(pt);
             sender.sendMessage("Spawn Point '" + pt.getName() + "' enabled.");
             return true;
         } else if (args[0].equalsIgnoreCase("delete")) {
             if (args.length != 2) return false;
             SpawnPoint pt = plugin.getSpawnPoints().get(args[1].toLowerCase());
             if (pt == null) {
                 sender.sendMessage("Unknown Spawn Point: " + args[1]);
                 return true;
             }
             plugin.removeSpawnPoint(pt);
 
             sender.sendMessage("Spawn Point '" + pt.getName() + "' deleted.");
             return true;
         } else if (args[0].equalsIgnoreCase("equip")) {
             if (args.length != 2) return false;
             SpawnPoint pt = plugin.getSpawnPoints().get(args[1].toLowerCase());
             if (pt == null) {
                 sender.sendMessage("Unknown Spawn Point: " + args[1]);
                 return true;
             }
             Player p = ((Player) sender);
             pt.setHelmet(p.getEquipment().getHelmet() == null ? null : p.getEquipment().getHelmet().clone());
            pt.setChestplate(p.getEquipment().getChestplate().clone());
             pt.setLeggings(p.getEquipment().getLeggings() == null ? null : p.getEquipment().getLeggings().clone());
             pt.setBoots(p.getEquipment().getBoots()== null ? null : p.getEquipment().getBoots().clone());
             pt.setHand(p.getItemInHand()== null ? null : p.getEquipment().getItemInHand().clone());
             ConfigurationSection sect = plugin.getConfig().getConfigurationSection("spawn-points." + pt.getName());
             sect.set("helmet", pt.getHelmet());
             sect.set("chestplate", pt.getChestplate());
             sect.set("leggings", pt.getLeggings());
             sect.set("boots", pt.getBoots());
             sect.set("hand", pt.getHand());
             plugin.saveConfig();
             sender.sendMessage("Spawn Point '" + pt.getName() + "' Equipment set.");
             return true;
         } else if (args[0].equalsIgnoreCase("list")) {
             String list = "";
             SpawnPoint pt;
             for (String name: plugin.getSpawnPoints().keySet()) {
                 pt = plugin.getSpawnPoints().get(name);
                 list += pt.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
                 list += pt.getName() + ChatColor.GRAY + ", ";
             }
             if (list.endsWith(", ")) {
                 list = list.substring(0, list.length()-4);
             }
             sender.sendMessage(new String[] {
                 ChatColor.WHITE + "Available Spawn Points (" + ChatColor.GREEN + "ENABLED" + ChatColor.WHITE + "/" + ChatColor.RED + "DISABLED" + ChatColor.WHITE + ")",
                 list
             });
             return true;
         }
         return false;
     }
 
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String lbl, String[] args) {
         List<String> results = new ArrayList<String>();
         if (args.length == 1) {
             if ("create".startsWith(args[0].toLowerCase())) results.add("create");
             if ("enable".startsWith(args[0].toLowerCase())) results.add("enable");
             if ("disable".startsWith(args[0].toLowerCase())) results.add("disable");
             if ("delete".startsWith(args[0].toLowerCase())) results.add("delete");
             if ("list".startsWith(args[0].toLowerCase())) results.add("list");
             if ("equip".startsWith(args[0].toLowerCase())) results.add("equip");
         } else if (args[0].equalsIgnoreCase("create")) {
             if (args.length == 3) {
                 // entity type
                 for (EntityType et: EntityType.values()) {
                     if (et.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                         results.add(et.getName());
                     }
                 }
             }
         } else if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable") ||  args[0].equalsIgnoreCase("delete")) {
             if (args.length == 2) {
                 for (String name: plugin.getSpawnPoints().keySet()) {
                     if (name.startsWith(args[1].toLowerCase())) {
                         results.add(plugin.getSpawnPoints().get(name).getName());
                     }
                 }
             }
         }
         return results;
     }
 }
