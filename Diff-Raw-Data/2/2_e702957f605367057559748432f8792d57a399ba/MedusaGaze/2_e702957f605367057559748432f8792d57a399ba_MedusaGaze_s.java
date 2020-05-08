 package ca.agnate.medusagaze;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import ca.agnate.medusagaze.MedusaPlayerListener;
 import java.util.List;
 import java.util.LinkedList;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 public class MedusaGaze extends JavaPlugin {
 
     // Weapons
     public static final List<Material> WEAPONS_TYPE = new LinkedList<Material>();
     public static final List<Material> SWORDS_TYPE = new LinkedList<Material>();
     public static final List<Material> AXES_TYPE = new LinkedList<Material>();
     public static final List<Material> PICKAXES_TYPE = new LinkedList<Material>();
     public static final List<Material> SPADES_TYPE = new LinkedList<Material>();
     public static final List<Material> HOES_TYPE = new LinkedList<Material>();
     
     private static PermissionHandler permissionHandler;
     
     private static List<String> permissionOPs;
     
     public static final String COMMAND_WORLDGAZE = "medusagaze.worldgaze";
     
     static {
         // Set up Permission nodes.  OP-only nodes are added to:  permissionNodes_OP
         // Everyone-nodes are added to:  permissionNodes_All
         permissionOPs.add( COMMAND_WORLDGAZE );
         
         // Weapons
         SWORDS_TYPE.add(Material.WOOD_SWORD);
         SWORDS_TYPE.add(Material.STONE_SWORD);
         SWORDS_TYPE.add(Material.GOLD_SWORD);
         SWORDS_TYPE.add(Material.IRON_SWORD);
         SWORDS_TYPE.add(Material.DIAMOND_SWORD);
 
         AXES_TYPE.add(Material.WOOD_AXE);
         AXES_TYPE.add(Material.STONE_AXE);
         AXES_TYPE.add(Material.GOLD_AXE);
         AXES_TYPE.add(Material.IRON_AXE);
         AXES_TYPE.add(Material.DIAMOND_AXE);
 
         PICKAXES_TYPE.add(Material.WOOD_PICKAXE);
         PICKAXES_TYPE.add(Material.STONE_PICKAXE);
         PICKAXES_TYPE.add(Material.GOLD_PICKAXE);
         PICKAXES_TYPE.add(Material.IRON_PICKAXE);
         PICKAXES_TYPE.add(Material.DIAMOND_PICKAXE);
 
         SPADES_TYPE.add(Material.WOOD_SPADE);
         SPADES_TYPE.add(Material.STONE_SPADE);
         SPADES_TYPE.add(Material.GOLD_SPADE);
         SPADES_TYPE.add(Material.IRON_SPADE);
         SPADES_TYPE.add(Material.DIAMOND_SPADE);
 
         HOES_TYPE.add(Material.WOOD_HOE);
         HOES_TYPE.add(Material.STONE_HOE);
         HOES_TYPE.add(Material.GOLD_HOE);
         HOES_TYPE.add(Material.IRON_HOE);
         HOES_TYPE.add(Material.DIAMOND_HOE);
 
         WEAPONS_TYPE.addAll(SWORDS_TYPE);
         WEAPONS_TYPE.addAll(AXES_TYPE);
         WEAPONS_TYPE.addAll(PICKAXES_TYPE);
         WEAPONS_TYPE.addAll(SPADES_TYPE);
         WEAPONS_TYPE.addAll(HOES_TYPE);
     }
 
     public void onDisable() {
         System.out.println("[" + this + "] Medusa has left your server... for now.  (disabled)");
     }
 
     public void onEnable() {
         // Set up commands for users.
         setupCommands();
         setupPermissions();
 
         PluginManager pm = getServer().getPluginManager();
         final PlayerListener playerListener = new MedusaPlayerListener(this);
 
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Lowest, this);
         pm.registerEvent(Event.Type.INVENTORY_OPEN, playerListener, Priority.Lowest, this);
         pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Monitor, this);
 
         System.out.println("[" + this + "] Medusa has reared her beautiful, wretched face! Hide your illegal tools!  (enabled)");
     }
 
     private void setupCommands() {
         MedusaGazeCommands commandExecutor = new MedusaGazeCommands(this);
         getCommand("medusa").setExecutor(commandExecutor);
     }
     
     private void setupPermissions() {
         if (permissionHandler != null)
             return;
     
         Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
         if (permissionsPlugin == null) {
             System.out.println("[" + this + "] Permissions not detected, using OPs.");
             return;
         }
         
         permissionHandler = ((Permissions) permissionsPlugin).getHandler();
         System.out.println("[" + this + "] Permissions detected ("+((Permissions)permissionsPlugin).getDescription().getFullName()+")");
     }
     
     public boolean has(Player p, String s)
     {
         //return (permissionHandler == null || permissionHandler.has(p, s));
         return hasSuperPerms(p, s) || hasNijikoPerms(p, s) || hasOPPerm(p, s);
     }
     
     public boolean hasOPPerm (Player p, String node) {
         // If the node requires OP status, and the player has OP, then true.
        return( permissionOPs.contains(node) && p.isOp() );
     }
     
     public boolean hasSuperPerms(Player p, String s)
     {
         String[] nodes = s.split("\\.");
         
         String perm = "";
         for (int i = 0; i < nodes.length; i++)
         {
             perm += nodes[i] + ".";
             if (p.hasPermission(perm + "*"))
                 return true;
         }
         
         return p.hasPermission(s);
     }
 
     public boolean hasNijikoPerms(Player p, String s)
     {
         return permissionHandler != null && permissionHandler.has(p, s);
     }
 
     public boolean isProperMaterial(Material mat) {
         if (WEAPONS_TYPE.contains(mat))
             return true;
 
         if (SWORDS_TYPE.contains(mat))
             return true;
 
         if (AXES_TYPE.contains(mat))
             return true;
 
         if (PICKAXES_TYPE.contains(mat))
             return true;
 
         if (HOES_TYPE.contains(mat))
             return true;
 
         return false;
     }
 
     public void gazeUponInventory(Inventory inv) {
         for (ItemStack item : inv.getContents()) {
             if (isProperMaterial(item.getType()) && item.getDurability() < 0) {
                 // Found a hacked weapon/tool, so convert it to stone.
                 item.setDurability((short) 0);
                 item.setType(Material.STONE);
                 item.setAmount(1);
             }
         }
     }
 
     public void releaseMedusaOntoTheWorld() {
         // Get the list of worlds.
         // List<World> worlds = getServer().getWorlds();
 
         // worlds.
     }
 }
