 package to.joe.j2mc.fun.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.sk89q.worldedit.blocks.ItemType;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.event.MessageEvent;
 import to.joe.j2mc.core.exceptions.BadPlayerMatchException;
 import to.joe.j2mc.fun.J2MC_Fun;
 
 public class ItemCommand extends MasterCommand {
 
     J2MC_Fun plugin;
 
     public ItemCommand(J2MC_Fun fun) {
         super(fun);
         this.plugin = fun;
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (isPlayer) {
             //Check if player is admin
             final boolean isAdmin = sender.hasPermission("j2mc.fun.admin");
             if (args.length == 0) {
                 player.sendMessage(ChatColor.RED + "Correct usage is: /i [item](:damage) (amount)");
                 return;
             }
             //Target player for giving items to other players
             final Player targetPlayer = player;
             //Material to be spawned
             Material itemMaterial = null;
             int itemCount = 1;
             short itemDamage = 0;
             //Split up the first argument by ":" for material vs damage
             final String[] idDamageSplit = args[0].split(":");
             //No 0 (air)
             if (idDamageSplit[0].equals("0")) {
                 idDamageSplit[0] = "1";
             }
             //Look up item in the worldedit item database
             ItemType type = ItemType.lookup(idDamageSplit[0]);
             //If item isn't found in database
             if (type == null) {
                 //Try checking if the material is given by ids
                 int id;
                 try {
                     //Parse argument into id
                     id = Integer.parseInt(idDamageSplit[0]);
                 } catch (NumberFormatException e) {
                     //If material is not found and isn't a number return here with unknown item error
                     player.sendMessage(ChatColor.RED + "Unknown item: '" + args[0] + "'");
                     return;
                 }
                 //At this point we know that the item is given by id but not by material name
                 //Query the material db by id
                 itemMaterial = Material.getMaterial(id);
                 
                 //Check if id isn't null i.e item was found
                 if (itemMaterial == null) {
                     player.sendMessage(ChatColor.RED + "Unknown item: '" + args[0] + "'");
                     return;
                 }
             }
             //If the material is null already. i.e we didn't find it by id, grab it from the material name query
             if (itemMaterial == null) { 
                 itemMaterial = Material.getMaterial(type.getID());
             }
             
             //Set type to itemMaterial to avoid null pointers ;o
             type = ItemType.fromID(itemMaterial.getId());
             
             //If a damage field was specified
             if (idDamageSplit.length == 2) {
                 //Grab the damage and see if its a wool value
                 final String damageString = idDamageSplit[1];
                 final short value = this.toWoolValue(damageString);
                 //If it isn't a wool value manually insert it by parsing the short
                 if (value != 0) {
                     itemDamage = value;
                 } else {
                     try {
                         itemDamage = Short.valueOf(damageString);
                     } catch (final NumberFormatException e) {
                         player.sendMessage(ChatColor.RED + "Invalid number for damage: '" + damageString + "'");
                         return;
                     }
                 }
             }
             //Truncate damage from range 0-15
             if ((itemDamage < 0) || (itemDamage > 15)) {
                 itemDamage = 0;
             }
             //Attempt to parse amount
             if (args.length > 1) {
                 final String countString = args[1];
                 try {
                     itemCount = Integer.parseInt(countString);
                 } catch (final NumberFormatException ex) {
                     player.sendMessage(ChatColor.RED + "Invalid number for amount: '" + countString + "'");
                     return;
                 }
             }
             //If the player is an admin and a player name was provided in the last argument, give them them the items
             if ((args.length == 3) && isAdmin) {
                 final String targetName = args[2];
                 try {
                     J2MC_Manager.getVisibility().getPlayer(targetName, sender);
                 } catch (final BadPlayerMatchException e) {
                     player.sendMessage(ChatColor.RED + e.getMessage());
                     return;
                 }
             }
             //Check if the item can't be spawned against the black list
             if (!isAdmin && this.plugin.summonBlackList.contains(itemMaterial.getId())) {
                 player.sendMessage(ChatColor.RED + "Can't give that to you right now");
                 return;
             }
             //Create the itemstack and add it to the inventory
             targetPlayer.getInventory().addItem(new ItemStack(itemMaterial, itemCount, itemDamage));
             //Send the player a message regarding the item lowercasing the first letter since its gonna be capitalized
            player.sendMessage(ChatColor.YELLOW + "You've been given " + ChatColor.AQUA + itemCount + " " + ChatColor.GOLD + ChatColor.BOLD + type.getName().substring(0, 1).toLowerCase() + type.getName().substring(1));
             //Log the spawning
             this.plugin.getLogger().info("Giving " + player.getName() + " " + itemCount + " " + itemMaterial.toString());
             //If item is on the watch list for summoning send message to irc and admins
             if ((this.plugin.summonWatchList.contains(itemMaterial.getId()) && ((itemCount > 10) || (itemCount < 1)) && !isAdmin) && !player.hasPermission("j2mc.fun.trusted")) {
                 this.plugin.getServer().getPluginManager().callEvent(new MessageEvent(MessageEvent.compile("ADMININFO"), "Detecting summon of " + itemCount + " " + type.getName() + " by " + player.getName()));
                 J2MC_Manager.getCore().adminAndLog(ChatColor.LIGHT_PURPLE + "Detecting summon of " + ChatColor.WHITE + itemCount + " " + ChatColor.LIGHT_PURPLE + type.getName() + " by " + ChatColor.WHITE + player.getName());
             }
         }
     }
 
     public short toWoolValue(String givenColorName) {
         if (givenColorName.equalsIgnoreCase("white")) {
             return 0;
         } else if (givenColorName.equalsIgnoreCase("orange")) {
             return 1;
         } else if (givenColorName.equalsIgnoreCase("magenta")) {
             return 2;
         } else if (givenColorName.equalsIgnoreCase("lightblue")) {
             return 3;
         } else if (givenColorName.equalsIgnoreCase("yellow")) {
             return 4;
         } else if (givenColorName.equalsIgnoreCase("lightgreen")) {
             return 5;
         } else if (givenColorName.equalsIgnoreCase("pink")) {
             return 6;
         } else if (givenColorName.equalsIgnoreCase("gray")) {
             return 7;
         } else if (givenColorName.equalsIgnoreCase("lightgray")) {
             return 8;
         } else if (givenColorName.equalsIgnoreCase("cyan")) {
             return 9;
         } else if (givenColorName.equalsIgnoreCase("purple")) {
             return 10;
         } else if (givenColorName.equalsIgnoreCase("blue")) {
             return 11;
         } else if (givenColorName.equalsIgnoreCase("brown")) {
             return 12;
         } else if (givenColorName.equalsIgnoreCase("darkgreen")) {
             return 13;
         } else if (givenColorName.equalsIgnoreCase("red")) {
             return 14;
         } else if (givenColorName.equalsIgnoreCase("black")) {
             return 15;
         }
         return 0;
     }
 
 }
