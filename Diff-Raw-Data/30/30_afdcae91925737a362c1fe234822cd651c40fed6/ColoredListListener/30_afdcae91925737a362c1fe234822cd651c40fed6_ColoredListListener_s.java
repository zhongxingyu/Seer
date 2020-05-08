 package com.meggawatts.MeggaChat;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 import ru.tehkode.permissions.PermissionUser;
 
 public class ColoredListListener implements Listener {
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         PermissionUser target = PermissionsEx.getUser(player);
         if (target.getOption("ListColor").equals("0")) {
             player.setPlayerListName(ChatColor.BLACK + player.getName());
         }
         if (target.getOption("ListColor").equals("1")) {
             player.setPlayerListName(ChatColor.DARK_BLUE + player.getName());
         }
         if (target.getOption("ListColor").equals("2")) {
             player.setPlayerListName(ChatColor.DARK_GREEN + player.getName());
         }
         if (target.getOption("ListColor").equals("3")) {
             player.setPlayerListName(ChatColor.DARK_AQUA + player.getName());
         }
         if (target.getOption("ListColor").equals("4")) {
             player.setPlayerListName(ChatColor.DARK_RED + player.getName());
         }
         if (target.getOption("ListColor").equals("5")) {
             player.setPlayerListName(ChatColor.DARK_PURPLE + player.getName());
         }
         if (target.getOption("ListColor").equals("6")) {
             player.setPlayerListName(ChatColor.GOLD + player.getName());
         }
         if (target.getOption("ListColor").equals("7")) {
             player.setPlayerListName(ChatColor.GRAY + player.getName());
         }
         if (target.getOption("ListColor").equals("8")) {
             player.setPlayerListName(ChatColor.DARK_GRAY + player.getName());
         }
         if (target.getOption("ListColor").equals("9")) {
             player.setPlayerListName(ChatColor.BLUE + player.getName());
         }
         if (target.getOption("ListColor").equals("a")) {
             player.setPlayerListName(ChatColor.GREEN + player.getName());
         }
         if (target.getOption("ListColor").equals("b")) {
             player.setPlayerListName(ChatColor.AQUA + player.getName());
         }
         if (target.getOption("ListColor").equals("c")) {
             player.setPlayerListName(ChatColor.RED + player.getName());
         }
         if (target.getOption("ListColor").equals("d")) {
             player.setPlayerListName(ChatColor.LIGHT_PURPLE + player.getName());
         }
         if (target.getOption("ListColor").equals("e")) {
             player.setPlayerListName(ChatColor.YELLOW + player.getName());
         }
         if (target.getOption("ListColor").equals("f")) {
             player.setPlayerListName(ChatColor.WHITE + player.getName());
         }
         if (target.getOption("ListColor").equals("k")) {
             player.setPlayerListName(ChatColor.MAGIC + player.getName());
         }
         if (target.getOption("ListColor").equals("l")) {
             player.setPlayerListName(ChatColor.BOLD + player.getName());
         }
         if (target.getOption("ListColor").equals("m")) {
             player.setPlayerListName(ChatColor.STRIKETHROUGH + player.getName());
         }
         if (target.getOption("ListColor").equals("n")) {
             player.setPlayerListName(ChatColor.UNDERLINE + player.getName());
         }
         if (target.getOption("ListColor").equals("o")) {
             player.setPlayerListName(ChatColor.ITALIC + player.getName());
         }
         if (target.getOption("ListColor").equals("r")) {
             player.setPlayerListName(ChatColor.RESET + player.getName());
         }

     }
 }
