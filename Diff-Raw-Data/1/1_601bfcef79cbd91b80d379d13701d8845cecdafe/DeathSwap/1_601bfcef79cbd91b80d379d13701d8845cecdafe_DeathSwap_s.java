 package com.hawkfalcon.deathswap;
 
 import com.hawkfalcon.deathswap.game.Join;
 import com.hawkfalcon.deathswap.game.Leave;
 import com.hawkfalcon.deathswap.game.NewGame;
 import com.hawkfalcon.deathswap.game.Swap;
 import com.hawkfalcon.deathswap.game.WinGame;
 import com.hawkfalcon.deathswap.utilities.Loc;
 import com.hawkfalcon.deathswap.utilities.MetricsLite;
 import com.hawkfalcon.deathswap.utilities.Utility;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class DeathSwap extends JavaPlugin {
 
     public Utility utility;
     public Loc loc = new Loc(this);
     public NewGame newGame = new NewGame(this);
     public WinGame winGame = new WinGame(this);
     public Join join = new Join(this);
     public Leave leave = new Leave(this);
     public Swap swap = new Swap(this);
 
     public HashMap<String, String> match = new HashMap<String, String>();
     public HashMap<String, String> accept = new HashMap<String, String>();
     public ArrayList<String> game = new ArrayList<String>();
     public ArrayList<String> lobby = new ArrayList<String>();
     public ArrayList<String> startgame = new ArrayList<String>();
 
     Random rand = new Random();
 
     public boolean protect = false;
     public int min;
     public int max;
     public int randNum;
 
     public File dataFile;
     public YamlConfiguration data;
 
     public void onEnable() {
         loadData();
         saveDefaultConfig();
         transferInfoToData();
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             getLogger().warning("Error Submitting stats!");
         }
         utility = new Utility(this, getConfig().getString("prefix", "[" + ChatColor.GOLD + "Death" + ChatColor.GREEN + "Swap" + ChatColor.WHITE + "]"));
         getServer().getPluginManager().registerEvents(new DSListener(this), this);
         getCommand("ds").setExecutor(new DSCommand(this));
         startTimer();
         min = getConfig().getInt("min_time");
         max = getConfig().getInt("max_time");
     }
 
     public void onDisable() {
         if (!game.isEmpty()) {
             for (String name : game) {
                 Player player = getServer().getPlayerExact(name);
                 utility.teleport(player, 1);
                 utility.restorePlayer(player);
             }
         }
     }
 
     /**
      * Starts the swap timer
      */
     public void startTimer() {
         new BukkitRunnable() {
 
             @Override
             public void run() {
                 randNum = rand.nextInt(max - min + 1) + min;
                 swap.switchPlayers();
                 startTimer();
             }
 
         }.runTaskLater(this, randNum * 20L);
     }
 
     public void loadData() {
         File dataF = new File(getDataFolder(), "data.yml");
         OutputStream out = null;
         InputStream defDataStream = getResource("data.yml");
         if (!dataF.exists()) {
             try {
                 getDataFolder().mkdir();
                 dataF.createNewFile();
                 if (defDataStream != null) {
                     out = new FileOutputStream(dataF);
                     int read = 0;
                     byte[] bytes = new byte[1024];
 
                     while ((read = defDataStream.read(bytes)) != -1) {
                         out.write(bytes, 0, read);
                     }
                    return;
                 }
             } catch (IOException e) {
                 getLogger().severe("Couldn't create data file.");
                 e.printStackTrace();
             } finally {
                 if (defDataStream != null) {
                     try {
                         defDataStream.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
                 if (out != null) {
                     try {
                         out.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
 
                 }
             }
         }
         YamlConfiguration conf = YamlConfiguration.loadConfiguration(dataF);
         data = conf;
         dataFile = dataF;
     }
 
     public void saveData() {
         try {
             data.save(dataFile);
         } catch (IOException e) {
             getLogger().severe("Couldn't save data file.");
             e.printStackTrace();
         }
     }
 
     public void transferInfoToData() {
         Set<String> keys = getConfig().getKeys(false);
         if (keys.contains("lobby_spawn")) {
             data.set("lobby_spawn", getConfig().getString("lobby_spawn"));
         }
         if (keys.contains("end_spawn")) {
             data.set("end_spawn", getConfig().getString("end_spawn"));
         }
         saveData();
     }
 }
