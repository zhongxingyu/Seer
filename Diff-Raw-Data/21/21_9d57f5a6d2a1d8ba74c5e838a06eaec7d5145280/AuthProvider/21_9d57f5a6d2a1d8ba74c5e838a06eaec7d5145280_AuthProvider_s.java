 package net.robbytu.banjoserver.framework.auth;
 
 import net.robbytu.banjoserver.framework.Main;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.potion.PotionEffectType;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AuthProvider {
     private static List<String> authenticatedUsers = new ArrayList<String>();
     public static boolean enabled = false;
 
     public static boolean isAuthenticated(String player) {
         return (enabled) ? (authenticatedUsers.contains(player.toUpperCase())) : true;
     }
 
     public static void addAuthenticatedUser(String player) {
         Main.plugin.getLogger().info("Adding authenticated player to cache, ENABLED=" + ((enabled) ? "true" : "false") + ", PLAYER=" + player + ", INSERT_NEW=" + ((authenticatedUsers.contains(player)) ? "false" : "true"));
 
         if(isAuthenticated(player)) return;
 
         authenticatedUsers.add(player.toUpperCase());
 
         if(Main.plugin.getConfig().isBoolean("spawn.authenticated.is_set") && (Main.plugin.getServer().getPlayer(player) instanceof Player)) {
             int x = Main.plugin.getConfig().getInt("spawn.authenticated.x");
             int y = Main.plugin.getConfig().getInt("spawn.authenticated.y");
             int z = Main.plugin.getConfig().getInt("spawn.authenticated.z");
 
             float pitch = Main.plugin.getConfig().getInt("spawn.authenticated.pitch");
             float yaw = Main.plugin.getConfig().getInt("spawn.authenticated.yaw");
 
             String world = Main.plugin.getConfig().getString("spawn.authenticated.world");
 
             Location location = new Location(Main.plugin.getServer().getWorld(world), x, y, z, yaw, pitch);
 
             Main.plugin.getServer().getPlayer(player).teleport(location);
 
             Player p_player = Main.plugin.getServer().getPlayer(player);
             p_player.removePotionEffect(PotionEffectType.INVISIBILITY);
 
             p_player.getInventory().clear(); // He, it's the hub server - wa do ya want?
 
             try {
                 Connection conn = Main.conn;
 
                 PreparedStatement statement = conn.prepareStatement("SELECT material, book_title, book_content, amount FROM bs_defaultinvs WHERE server LIKE ?");
                 statement.setString(1, Main.plugin.getServer().getServerName());
                 ResultSet result = statement.executeQuery();
 
                 while(result.next()) {
                     Material material = Material.getMaterial(result.getInt(1));
                     ItemStack stack = new ItemStack(material, result.getInt(4));
 
                     if(material == Material.WRITTEN_BOOK) {
                         BookMeta meta = (BookMeta) stack.getItemMeta();
 
                         meta.setAuthor("Banjoserver");
                         meta.setTitle(result.getString(2));
                         meta.setPages(result.getString(3));
 
                         stack.setItemMeta(meta);
                     }
                     p_player.getInventory().addItem(stack);
                 }
             }
             catch(Exception ignored) {}
         }
     }
 
     public static void removeAuthenticatedUser(String player) {
         if(!enabled) return;
 
         if(isAuthenticated(player)) authenticatedUsers.remove(player.toUpperCase());
 
         Main.plugin.getServer().getPlayer(player).getInventory().clear();
 
         if(Main.plugin.getConfig().getBoolean("spawn.unauthenticated.is_set", false) == true && (Main.plugin.getServer().getPlayer(player) instanceof Player)) {
             Main.plugin.getLogger().info("Adjusting spawn for user");
 
             int x = Main.plugin.getConfig().getInt("spawn.unauthenticated.x");
             int y = Main.plugin.getConfig().getInt("spawn.unauthenticated.y");
             int z = Main.plugin.getConfig().getInt("spawn.unauthenticated.z");
 
             float pitch = Main.plugin.getConfig().getInt("spawn.unauthenticated.pitch");
             float yaw = Main.plugin.getConfig().getInt("spawn.unauthenticated.yaw");
 
             String world = Main.plugin.getConfig().getString("spawn.unauthenticated.world");
 
             Location location = new Location(Main.plugin.getServer().getWorld(world), x, y, z, yaw, pitch);
 
             Main.plugin.getServer().getPlayer(player).teleport(location);
         }
     }
 }
