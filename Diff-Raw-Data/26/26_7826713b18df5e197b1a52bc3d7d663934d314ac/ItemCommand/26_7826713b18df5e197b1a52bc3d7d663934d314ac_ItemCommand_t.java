 package to.joe.j2mc.fun.command;
 
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.event.MessageEvent;
 import to.joe.j2mc.core.exceptions.BadPlayerMatchException;
 import to.joe.j2mc.fun.J2MC_Fun;
 
 public class ItemCommand extends MasterCommand{
     
     J2MC_Fun plugin;
     
     public ItemCommand(J2MC_Fun fun){
         super(fun);
         this.plugin = fun;
     }
     
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if(isPlayer && player.hasPermission("j2mc.fun")){
             boolean isAdmin = J2MC_Manager.getPermissions().isAdmin(player.getName());
                 if (args.length == 0) {
                     player.sendMessage(ChatColor.RED + "Correct usage is: /i [item](:damage) (amount)");
                     return;
                 }
                 Player targetPlayer = player;
                 Material itemMaterial = null;
                 int itemCount = 1;
                 String[] idDamageSplit = null;
                 Byte itemDamage = null;
                 if (args.length > 0) {
                     idDamageSplit = args[0].split(":");
                     if (idDamageSplit[0].equals("0")) {
                         idDamageSplit[0] = "1";
                     }
                     itemMaterial = Material.matchMaterial(idDamageSplit[0]);
                     if (idDamageSplit.length == 2) {
                         final String damageString = idDamageSplit[1];
                         byte value = this.toWoolValue(damageString);
                        if (value != (byte) 100) {
                             itemDamage = value;
                         } else {
                             try {
                                 itemDamage = Byte.valueOf(damageString);
                             } catch (final NumberFormatException e) {
                                 player.sendMessage("No such damage value. Giving you damage=0");
                             }
                         }
                     }
                 }
                 if (args.length > 1) {
                     final String countString = args[1];
                     try {
                         itemCount = Integer.parseInt(countString);
                     } catch (final NumberFormatException ex) {
                         player.sendMessage(ChatColor.RED + countString + " is not a number");
                         return;
                     }
                 }
                 if ((args.length == 3) && isAdmin) {
                     final String targetName = args[2];
                     try{
                         J2MC_Manager.getVisibility().getPlayer(targetName, sender);
                     }catch(BadPlayerMatchException e){
                         player.sendMessage(ChatColor.RED + e.getMessage());
                         return;
                     }
                 }
                 if (itemMaterial == null) {
                     player.sendMessage(ChatColor.RED + "Unknown item");
                     return;
                 }
                 if (!isAdmin && plugin.SummonBlackList.contains(itemMaterial.getId())) {
                     player.sendMessage(ChatColor.RED + "Can't give that to you right now");
                     return;
                 }
                 if (itemDamage != null) {
                     targetPlayer.getInventory().addItem(new ItemStack(itemMaterial, itemCount, (short) 0, itemDamage));
                 } else {
                     targetPlayer.getInventory().addItem(new ItemStack(itemMaterial, itemCount));
                 }
                 player.sendMessage("Given " + targetPlayer.getDisplayName() + " " + itemCount + " " + itemMaterial.toString());
                 plugin.getLogger().info("Giving " + player.getName() + " " + itemCount + " " + itemMaterial.toString());
                 if ((plugin.SummonWatchList.contains(itemMaterial.getId()) && ((itemCount > 10) || (itemCount < 1)) && !isAdmin) && !J2MC_Manager.getPermissions().hasFlag(player.getName(), 'y')) {
                     HashSet<String> targets = new HashSet<String>();
                     targets.add("ADMININFO");
                     plugin.getServer().getPluginManager().callEvent(new MessageEvent(targets, "Detecting summon of " + itemCount + " " + itemMaterial.toString() + " by " + player.getName()));   
                     J2MC_Manager.getCore().adminAndLog(ChatColor.LIGHT_PURPLE + "Detecting summon of " + ChatColor.WHITE + itemCount + " " + ChatColor.LIGHT_PURPLE + itemMaterial.toString() + " by " + ChatColor.WHITE + player.getName());
                 }
         }
     }
     
     public byte toWoolValue(String givenColorName){
         if(givenColorName.equalsIgnoreCase("white")){
             return (byte) 0;
         }else if(givenColorName.equalsIgnoreCase("orange")){
             return (byte) 1;
         }else if(givenColorName.equalsIgnoreCase("magenta")){
             return (byte) 2;
         }else if(givenColorName.equalsIgnoreCase("lightblue")){
             return (byte) 3;
         }else if(givenColorName.equalsIgnoreCase("yellow")){
             return (byte) 4;
         }else if(givenColorName.equalsIgnoreCase("lightgreen")){
             return (byte) 5;
         }else if(givenColorName.equalsIgnoreCase("pink")){
             return (byte) 6;
         }else if(givenColorName.equalsIgnoreCase("gray")){
             return (byte) 7;
         }else if(givenColorName.equalsIgnoreCase("lightgray")){
             return (byte) 8;
         }else if(givenColorName.equalsIgnoreCase("cyan")){
             return (byte) 9;
         }else if(givenColorName.equalsIgnoreCase("purple")){
             return (byte) 10;
         }else if(givenColorName.equalsIgnoreCase("blue")){
             return (byte) 11;
         }else if(givenColorName.equalsIgnoreCase("brown")){
             return (byte) 12;
         }else if(givenColorName.equalsIgnoreCase("darkgreen")){
             return (byte) 13;
         }else if(givenColorName.equalsIgnoreCase("red")){
             return (byte) 14;
         }else if(givenColorName.equalsIgnoreCase("black")){
             return (byte) 15;
         }
        return (byte) 100;
     }
     
 }
