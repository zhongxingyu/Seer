 package com.hackhalo2.creative;
 
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Pixl extends JavaPlugin {
     public PluginDescriptionFile pdf;
     public String version = null;
     public HashMap<Player, Boolean> toggled = new HashMap<Player, Boolean>();
     public HashMap<Player, Integer> set = new HashMap<Player, Integer>();
     public HashMap<Player, Boolean> breakMode = new HashMap<Player, Boolean>();
     public HashMap<Player, Boolean> shatterMode = new HashMap<Player, Boolean>();
 
     private final PixlPlayer pListener = new PixlPlayer(this);
     private final PixlBlock bListener = new PixlBlock(this);
 
     // The list of materials that can be PixlBroken by limited PixlBreak users.
     public final Material[] limitedUserMaterials = {
          // Stairs
          Material.WOOD_STAIRS, Material.COBBLESTONE_STAIRS, Material.SMOOTH_STAIRS, Material.BRICK_STAIRS,
 
          // Ores
          Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE,
         Material.DIAMOND_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE,
 
          // Other hard-to-break stuff.
          Material.OBSIDIAN, Material.WEB, Material.BRICK, Material.FENCE, Material.IRON_FENCE, Material.SMOOTH_BRICK
     };
 
     // The list of materials that count as pickaxes for PixlBreak.
     public final Material[] pickaxeMaterials = {
         Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
         Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE
     };
     
     //The List of materials that have special case drops
     public final Material[] specialCaseMaterials = {
 	Material.GLASS, Material.GLOWSTONE    
     };
 
     public void onEnable() {
 
         pdf = this.getDescription();
         version = pdf.getVersion();
         //Set up the Plugin Manager
         PluginManager pm = getServer().getPluginManager();
         //Register Listeners
         pm.registerEvent(Event.Type.PLAYER_JOIN, pListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_KICK, pListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, pListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_DAMAGE, bListener, Event.Priority.Normal, this);
         System.out.println("[Pixl] "+version+" Loaded");
 
         getCommand("pixl").setExecutor(new PixlCommand(this));
     }
 
     public void onDisable() {
 	toggled     = null;
 	set         = null;
 	breakMode   = null;
 	shatterMode = null;
     }
 
     public void setToggle(final Player p, final boolean v) { toggled.put(p, v); }
     public boolean isToggled(final Player p) {
         if(toggled.containsKey(p)) { return toggled.get(p); }
         else { return false; }
     }
 
     public void setBreak(final Player p, final boolean v) { breakMode.put(p, v); }
     public boolean breakMode(final Player p) {
         if(breakMode.containsKey(p)) { return breakMode.get(p); }
         else { return false; }
     }
     
     public void setShatter(final Player p, final boolean v) { shatterMode.put(p, v); }
     public boolean shatterMode(final Player p) {
         if(shatterMode.containsKey(p)) { return shatterMode.get(p); }
         else { return false; }
     }
 
     public void removeValue(final Player p) { set.remove(p); }
     public void setValue(final Player p, final int v) { set.put(p, v); }
     public Integer isSet(final Player p) {
         if(set.containsKey(p)) { return set.get(p); }
         else { return null; }
     }
 
     public boolean checkPermissions(Player p, String s, boolean f) {
        if(isToggled(p) || breakMode(p) || shatterMode(p) || f) { //check to see if player is toggled or forced
             return p.hasPermission(s);
         }
         return false;
     }
 
     public boolean isPickaxe(Material material) {
         for (Material pick : pickaxeMaterials) {
             if (pick.equals(material)) {
                 return true;
             }
         }
 
         return false;
     }
 }
