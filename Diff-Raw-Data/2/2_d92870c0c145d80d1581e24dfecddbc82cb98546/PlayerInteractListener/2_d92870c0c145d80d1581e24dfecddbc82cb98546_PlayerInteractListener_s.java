 package me.chaseoes.supercraftbrothers.listeners;
 
 import me.chaseoes.supercraftbrothers.SCBGame;
 import me.chaseoes.supercraftbrothers.SCBGameManager;
 import me.chaseoes.supercraftbrothers.classes.SCBClass;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class PlayerInteractListener implements Listener {
 
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasBlock() && event.getClickedBlock().getType() == Material.WALL_SIGN) {
             Sign s = (Sign) event.getClickedBlock().getState();
             if (s.getLine(0).equalsIgnoreCase("") && !s.getLine(1).equalsIgnoreCase("") && s.getLine(2).equalsIgnoreCase("") && s.getLine(3).equalsIgnoreCase("")) {
                 String className = ChatColor.stripColor(s.getLine(1));
                 SCBClass sc = new SCBClass(className);
                 sc.apply(SCBGameManager.getInstance().getCraftBrother(event.getPlayer()));
             }
 
             if (s.getLine(0).equalsIgnoreCase("SuperCraftBros")) {
                 String mapName = ChatColor.stripColor(s.getLine(1));
                SCBGameManager.getInstance().getGame(mapName).joinGame(event.getPlayer());
             }
         }
 
         for (SCBGame game : SCBGameManager.getInstance().getAllGames()) {
 
         }
     }
 
 }
