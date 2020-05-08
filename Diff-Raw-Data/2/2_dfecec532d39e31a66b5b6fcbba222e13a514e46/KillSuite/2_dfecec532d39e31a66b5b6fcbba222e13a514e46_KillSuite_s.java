 package net.krinsoft.killsuite;
 
 import com.fernferret.allpay.AllPay;
 import com.fernferret.allpay.GenericBank;
 import com.pneumaticraft.commandhandler.CommandHandler;
 import net.krinsoft.killsuite.commands.*;
 import net.krinsoft.killsuite.listeners.EntityListener;
 import net.krinsoft.killsuite.listeners.PlayerListener;
 import net.krinsoft.killsuite.listeners.ServerListener;
 import net.krinsoft.killsuite.listeners.WorldListener;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author krinsdeath
  */
 public class KillSuite extends JavaPlugin {
     private boolean debug = false;
     private boolean economy = false;
     private boolean contract = false;
     private boolean report = true;
     private List<String> worlds = new ArrayList<String>();
     private boolean leaders = true;
     private int saveTask;
 
     private FileConfiguration configuration;
     private File configFile;
 
     private FileConfiguration leaderboards;
     private File leaderFile;
 
     private GenericBank bank;
 
     private CommandHandler commandHandler;
     
     private Manager manager;
 
     private EntityListener eListener;
 
     private boolean deathcounter;
 
     @Override
     public void onEnable() {
         long startup = System.currentTimeMillis();
 
         // simple notices registration
         getServer().getMessenger().registerOutgoingPluginChannel(this, "SimpleNotice");
 
         registerConfig();
 
         if (!deathcounter) {
             if (new File("plugins/DeathCounter/users.db").renameTo(new File(getDataFolder(), "users.db"))) {
                 log("Successfully imported SQLite database.");
             }
             if (new File("plugins/DeathCounter/users.yml").renameTo(new File(getDataFolder(), "users.yml"))) {
                 log("Successfully imported YAML database.");
             }
             if (new File("plugins/DeathCounter/config.yml").renameTo(new File(getDataFolder(), "config.yml"))) {
                 log("Imported DeathCounter config file.");
             }
             registerConfig(true);
             deathcounter = true;
             getConfig().set("plugin.imported", true);
             saveConfig();
         }
 
         if (economy) {
             if (validateAllPay()) {
                 debug("Economy successfully hooked.");
             }
         }
         
         // register all the players
         manager = new Manager(this);
 
         registerCommands();
 
         // event listeners
         eListener = new EntityListener(this);
         PlayerListener pListener = new PlayerListener(this);
         ServerListener sListener = new ServerListener(this);
         WorldListener wListener = new WorldListener(this);
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(eListener, this);
         pm.registerEvents(pListener, this);
         pm.registerEvents(sListener, this);
         pm.registerEvents(wListener, this);
 
         saveTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 getManager().save();
                 saveLeaders();
             }
         }, 300L, 300L * 20L);
         
         log("Enabled successfully. (" + (System.currentTimeMillis() - startup) + "ms)");
     }
 
     @Override
     public void onDisable() {
         eListener.save();
         saveLeaders();
         if (profile) { saveProfiler(); }
         getServer().getScheduler().cancelTasks(this);
         getServer().getScheduler().cancelTask(saveTask);
         manager.save();
         manager.disable();
         manager = null;
         bank = null;
         log("Disabled successfully.");
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (label.equalsIgnoreCase("contract")) {
             sender.sendMessage(ChatColor.RED + "Not yet implemented.");
             return false;
         }
         List<String> arguments = new ArrayList<String>(Arrays.asList(args));
         arguments.add(0, label);
         return commandHandler.locateAndRunCommand(sender, arguments);
     }
 
     /**
      * Forces a reload of KillSuite's configuration file
      * @param val true will reload the files; false does nothing
      */
     public void registerConfig(boolean val) {
         if (val) {
             configFile = null;
             configuration = null;
             registerConfig();
         }
     }
 
     void registerConfig() {
         configFile = new File(getDataFolder(), "config.yml");
         if (!configFile.exists()) {
             getConfig().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/config.yml")));
             getConfig().options().copyDefaults(true);
             getConfig().options().header("#########\n" +
                     "#\n" +
                     "# Database:\n" +
                     "#   Available types are YAML, SQLite and MySQL\n" +
                     "#   Database is used for SQLite and MySQL only\n" +
                     "#   YAML entires are stored in users.yml in plugins/DeathCounter/\n" +
                     "#\n" +
                     "# Economy:\n" +
                     "#   players->realism:\n" +
                     "#     If this is true, the money awarded to the killer will be deducted\n" +
                     "#     from the killed player's wallet (default false)\n" +
                     "#   players->percentage:\n" +
                     "#     If this is true, the money awarded to the killer will be a percentage\n" +
                     "#     of the killed player's wallet (default true)\n" +
                     "#   diminish->depth:\n" +
                     "#     For every point underneath this value a player is underground, the 'return' value\n" +
                     "#     will be deducted (as a percentage) from the total payout for that kill\n" +
                     "#   diminish->return:\n" +
                     "#     A value to deduct per kill per point of depth (as a percentage) from each kill\n" +
                     "#     Maximum of 100\n" +
                     "#\n" +
                     "# Contracts:\n" +
                     "#   If contracts is set to true, players will be able to create 'kill' contracts\n" +
                     "#   to have other players killed. When a player with a contract on his head is killed,\n" +
                     "#   the money from the contract is automatically deducted from the contract owner's\n" +
                     "#   account and added to the killer's account, in addition to the normal kill reward. (default true)\n" +
                     "#   Fee:\n" +
                     "#     Fee determines how much it costs to create a contract to kill a player.\n" +
                     "#   Max:\n" +
                     "#     The maximum number of contracts available at a time.\n" +
                     "#\n" +
                     "# Save Interval:\n" +
                     "#   This determines how often (in seconds) the plugin will save all records.\n" +
                     "# Report:\n" +
                     "#   If this is true, the plugin will report kills and earnings to players as they're made\n" +
                     "#\n" +
                     "#########");
             saveConfig();
         }
         if (getConfig().get("economy.animals.ocelot") == null) {
             List<Double> list = new ArrayList<Double>();
             list.add(3.0);
             list.add(5.0);
             getConfig().set("economy.animals.ocelot", list);
             getConfig().set("economy.monsters.irongolem", list);
             saveConfig();
         }
         if (getConfig().get("plugin.leaders") == null) {
             getConfig().set("plugin.leaders", true);
             getConfig().set("plugin.profiler", false);
         }
 
         leaderFile = new File(getDataFolder(), "leaders.yml");
         if (!leaderFile.exists()) {
             getLeaders().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/leaders.yml")));
             getLeaders().options().copyDefaults(true);
             saveLeaders();
         }
         if (getLeaders().get("ocelot") == null) {
             List<String> list = new ArrayList<String>();
             getLeaders().set("ocelot", list);
             getLeaders().set("irongolem", list);
             saveLeaders();
         }
 
         debug = getConfig().getBoolean("plugin.debug", false);
         economy = getConfig().getBoolean("plugin.economy", false);
         contract = getConfig().getBoolean("plugin.contracts", false);
         report = getConfig().getBoolean("plugin.report", true);
         worlds = getConfig().getStringList("plugin.exclude_worlds");
         leaders = getConfig().getBoolean("plugin.leaders", true);
         profile = getConfig().getBoolean("plugin.profiler", false);
     }
 
     private void registerCommands() {
         PermissionsHandler permissionsHandler = new PermissionsHandler();
         commandHandler = new CommandHandler(this, permissionsHandler);
         commandHandler.registerCommand(new DebugCommand(this));
         commandHandler.registerCommand(new LeaderCommand(this));
         commandHandler.registerCommand(new ReloadCommand(this));
         commandHandler.registerCommand(new StatsCommand(this));
         if (contract) {
             //commandHandler.registerCommand(new ContractCommand(this));
         }
     }
     
     public void log(String message) {
         getLogger().info(message);
     }
     
     public void debug(boolean val) {
         getConfig().set("plugin.debug", val);
         debug = val;
         saveConfig();
     }
     
     public void debug(String message) {
         if (debug) {
             StringBuilder msg = new StringBuilder("[Debug] ").append(message);
             getLogger().info(msg.toString());
         }
     }
 
     public void report(Player p, Monster m, double amt, boolean pet) {
         long time = System.nanoTime();
         if (report) {
             String message = ChatColor.YELLOW + "[Kill] " + ChatColor.WHITE + (!pet ? "You" : "Your pet") + " killed a " + m.getFancyName();
             if (getBank() != null && amt > 0) {
                 try {
                     message = message + " worth " + getBank().getFormattedAmount(p, amt, -1);
                 } catch (Exception e) {
                     debug("An error occurred while attempting to fetch the currency format string: " + e.getLocalizedMessage());
                 }
             }
             message += ".";
             if (p.getListeningPluginChannels().contains("SimpleNotice")) {
                 p.sendPluginMessage(this, "SimpleNotice", message.getBytes(java.nio.charset.Charset.forName("UTF-8")));
             } else {
                 p.sendMessage(message);
             }
         }
         if (leaders) {
             updateLeaderboards(p, m);
         }
         profile("report.update", System.nanoTime() - time);
     }
     
     public void displayLeaderboards(CommandSender s, Monster m) {
         if (m == null) { return; }
         s.sendMessage("=== Leaderboards: " + m.getFancyName() + " ===");
         try {
             int place = 1;
             String eKiller;
             int eKills;
             List<String> leaders = getLeaders().getStringList(m.getName());
             if (!leaders.isEmpty()) {
                 for (String entry : leaders) {
                     eKiller = entry.split(":")[0];
                     eKills = Integer.parseInt(entry.split(":")[1]);
                     s.sendMessage(ChatColor.GOLD + "#" + place + " - " + ChatColor.GREEN + eKiller + ChatColor.WHITE + " - " + ChatColor.AQUA + eKills);
                     place++;
                 }
             }
         } catch (NullPointerException e) {
             debug("Something went wrong.");
         } catch (NumberFormatException e) {
             debug("An error occurred while parsing the leader list for '" + m.getName() + "'");
         } catch (IndexOutOfBoundsException e) {
             debug("An error occurred while parsing the leader list for '" + m.getName() + "'");
         }
     }
     
     private void updateLeaderboards(Player p, Monster m) {
         try {
             int kills = manager.getKiller(p.getName()).get(m.getName()); // the provided player's kill count
             List<String> leaders = getLeaders().getStringList(m.getName()); // the provided monster's current leaderboard
             // if the leaderboard is empty, this player is guaranteed to have the highest kill count
             if (leaders.isEmpty()) {
                 debug("Leader list was empty for " + m.getName());
                 leaders.add(0, p.getName() + ":" + kills);
             }
             // iterate through the list of leader:kills
             for (int i = -1; i < leaders.size(); i++) {
                 // will never be true if at least one player exists in the leader list
                 if (i+1 > leaders.size() && i+1 <= 4) {
                     debug("Adding '" + p.getName() + ":" + kills +"' to list");
                     leaders.add(i+1, p.getName() + ":" + kills);
                     break;
                 }
                 // check if this player's name exists in the list already and update it if it does
                 String[] a = leaders.get(i+1).split(":");
                 if (a[0].equals(p.getName())) {
                     leaders.set(i+1, p.getName() + ":" + kills);
                     break;
                 }
                 // check if this player's kills are higher than the loop's current kills
                 int eKills = Integer.parseInt(a[1]);
                 if (kills > eKills) {
                     leaders.add(i+1, p.getName() + ":" + kills);
                     break;
                 }
             }
             // set the updated list
             getLeaders().set(m.getName(), leaders.subList(0, (leaders.size() > 4 ? 4 : leaders.size())));
         } catch (NullPointerException e) {
             debug("Something went wrong.");
         } catch (NumberFormatException e) {
             debug("An error occurred while parsing the leader list for '" + m.getName() + "'");
         } catch (IndexOutOfBoundsException e) {
             debug("An error occurred while parsing the leader list for '" + m.getName() + "'");
         }
     }
     
     public boolean validateAllPay() {
         if (bank != null) { return true; }
         double allpayVersion = 3.1;
         AllPay allpay = new AllPay(this, "[" + this + "] ");
         if (allpay.getVersion() >= allpayVersion) {
             bank = allpay.loadEconPlugin();
             bank.toggleReceipts(false);
             debug("Hooked economy with AllPay v" + allpay.getVersion() + "...");
             return true;
         }
         return false;
     }
     
     public void validateAllPay(boolean val) {
         if (val) {
             economy = false;
             bank = null;
             debug("Economy plugin unhooked.");
             return;
         }
         validateAllPay();
     }
     
     public GenericBank getBank() {
         if (!economy) { return null; }
         if (bank == null) {
             validateAllPay();
         }
         return bank;
     }
     
     public Manager getManager() {
         return manager;
     }
 
     public FileConfiguration getConfig() {
         if (configuration == null) {
             configuration = YamlConfiguration.loadConfiguration(configFile);
         }
         return configuration;
     }
 
     FileConfiguration getLeaders() {
         if (leaderboards == null) {
             leaderboards = YamlConfiguration.loadConfiguration(leaderFile);
         }
         return leaderboards;
     }
     
     void saveLeaders() {
         try {
             leaderboards.save(new File(getDataFolder(), "leaders.yml"));
         } catch (IOException e) {
             debug("Error saving file 'leaders.yml'");
         }
     }
     
     public boolean validWorld(String world) {
         return !worlds.contains(world);
     }
 
     public double diminishReturn(Player killer, double amount) {
         int ret = getConfig().getInt("economy.diminish.return");
         int depth = getConfig().getInt("economy.diminish.depth");
         int player = (int) Math.floor(killer.getLocation().getY());
         double diminish = ((depth - player) * ret);
         diminish = ((diminish > 0 ? diminish : 0) / 100);
         amount = (amount - (amount * (diminish)));
         return (amount > 0 ? amount : 0);
     }
 
     /*
      * Profiler Methods
      */
     private FileConfiguration profiler;
     private boolean profile;
 
     public FileConfiguration getProfiler() {
         if (profiler == null) {
             profiler = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "profiler.yml"));
         }
         return profiler;
     }
 
     public void saveProfiler() {
         try {
            profiler.save(new File(getDataFolder(), "profiler.yml"));
         } catch (IOException e) {
             debug("Error saving file 'profiler.yml'");
         }
     }
 
     public void profile(String section, long time) {
         if (profile) {
             long n = getProfiler().getLong(section);
             int count = getProfiler().getInt(section + ".count", 0) + 1;
             long average = n + time;
             getProfiler().set(section, average);
             getProfiler().set(section + ".count", count);
             getLogger().info(section + " took " + time + "ns [average (over " + count + "): " + (average / count) + "ns (" + (average / count) / 1000000 + "ms)]");
         }
     }
 
 }
