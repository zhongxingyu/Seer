 package to.joe.decapitation;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.SQLException;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.decapitation.command.BountyCommand;
 import to.joe.decapitation.command.SetNameCommand;
 import to.joe.decapitation.command.SpawnHeadCommand;
 import to.joe.decapitation.datastorage.DataStorageInterface;
 import to.joe.decapitation.datastorage.MySQLDataStorageImplementation;
 
 public class Decapitation extends JavaPlugin implements Listener {
 
     public static final int HEAD = 397;
     public static final int HEADBLOCK = 144;
     double allDeaths;
     double killedByPlayer;
     public boolean bounties = false;
     private double tax;
     private DataStorageInterface dsi;
 
     public static Economy economy = null;
 
     private boolean setupEconomy() {
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
         return (economy != null);
     }
 
     public DataStorageInterface getDsi() {
         return dsi;
     }
 
     public double getTax() {
         return tax;
     }
 
     @Override
     public void onEnable() {
         getConfig().options().copyDefaults(true);
         saveConfig();
         allDeaths = getConfig().getDouble("dropSkulls.allDeaths");
         killedByPlayer = getConfig().getDouble("dropSkulls.killedByPlayer");
         tax = getConfig().getDouble("bounty.tax");
         getServer().getPluginManager().registerEvents(this, this);
         getCommand("setname").setExecutor(new SetNameCommand());
         getCommand("spawnhead").setExecutor(new SpawnHeadCommand());
         getCommand("bounty").setExecutor(new BountyCommand(this));
         File f = new File(getDataFolder(), "mysql.sql");
         if (!f.exists()) {
             InputStream in;
             OutputStream out;
             try {
                 in = getResource("mysql.sql");
                 out = new FileOutputStream(f);
                 byte[] buffer = new byte[1024];
                 int len = in.read(buffer);
                 while (len != -1) {
                     out.write(buffer, 0, len);
                     len = in.read(buffer);
                 }
                 in.close();
                 out.close();
             } catch (FileNotFoundException e) {
                 getLogger().log(Level.SEVERE, "Error writing sql file", e);
             } catch (IOException e) {
                 getLogger().log(Level.SEVERE, "Error writing sql file", e);
             }
         }
         if (getConfig().getBoolean("bounty.enabled")) {
             bounties = setupEconomy();
             if (bounties)
                 getLogger().info("Econ detected");
             else
                 getLogger().info("Econ not detected");
         }
         if (bounties) {
             try {
                 dsi = new MySQLDataStorageImplementation(this, getConfig().getString("database.url"), getConfig().getString("database.username"), getConfig().getString("database.password"));
             } catch (SQLException e) {
                 getLogger().log(Level.SEVERE, "Error connecting to mysql database", e);
                 bounties = false;
             }
         }
         if (bounties)
             getLogger().info("Bounties enabled");
         else
             getLogger().info("Bounties not enabled");
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBreak(BlockBreakEvent event) {
         if (!event.isCancelled() && event.getBlock().getTypeId() == HEADBLOCK) {
             String name = new Head(new CraftItemStack(HEAD, 1, (short) 0, (byte) 3), event.getBlock().getLocation()).getName();
             if (name.equals(""))
                 return;
             Head h = null;
             CraftItemStack head = new CraftItemStack(HEAD, 1, (short) 0, (byte) 3);
             head = (CraftItemStack) event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), head).getItemStack();
             h = new Head(head);
             h.setName(name);
             event.getBlock().setTypeId(0);
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onDeath(PlayerDeathEvent event) {
         Player p = event.getEntity();
         Player k = p.getKiller();
        if (p.hasPermission("decapitation.dropheads") && (allDeaths > Math.random() || (killedByPlayer > Math.random()) && k != null) && k.hasPermission("decapitation.collectheads")) {
             CraftItemStack c = new CraftItemStack(HEAD, 1, (short) 0, (byte) 3);
             new Head(c).setName(event.getEntity().getName());
             event.getDrops().add(c);
         }
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         if (bounties) {
             int unclaimedHeads = 0;
             try {
                 unclaimedHeads = dsi.getNumUnclaimedHeads(event.getPlayer().getName());
             } catch (SQLException e) {
                 getLogger().log(Level.SEVERE, "Error getting number of unclaimed heads", e);
             }
             if (unclaimedHeads > 0) {
                 if (unclaimedHeads == 1)
                     event.getPlayer().sendMessage(ChatColor.GOLD + "You have " + unclaimedHeads + " unclaimed head.");
                 else
                     event.getPlayer().sendMessage(ChatColor.GOLD + "You have " + unclaimedHeads + " unclaimed heads.");
                 event.getPlayer().sendMessage(ChatColor.GOLD + "Type /bounty redeem to receive them");
             }
         }
     }
 }
