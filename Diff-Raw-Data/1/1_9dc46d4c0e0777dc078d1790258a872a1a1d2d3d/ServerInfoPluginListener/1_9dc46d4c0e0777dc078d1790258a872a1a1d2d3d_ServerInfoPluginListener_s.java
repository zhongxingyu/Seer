 package org.meekers.plugins.serverinfo;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 /**
  *
  * @author jaredm
  */
 class ServerInfoPluginListener implements Listener {
 
     public ServerInfoPluginListener(ServerInfo plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
         this.getServerStats("");
     }
     
     @EventHandler
     public void onLogin(PlayerJoinEvent event) {
         this.getServerStats("");
     }
 
     @EventHandler
     public void onLogout(PlayerQuitEvent event) {
         Player p = event.getPlayer();
         String pn = p.getName();
         this.getServerStats(pn);
     }
 
     public void getServerStats(String piq) {
         String playernames;
         ArrayList<String> players;
         players = this.getPlayers(piq);
 
         playernames = "";
         int count = 0;
         for (String pname : players) {
             count++;
             playernames += "\n  " + pname;
 
         }
 
         String sv;
         sv = Bukkit.getVersion();
 
         String sn;
         sn = Bukkit.getServerName();
 
         String motd;
         motd = Bukkit.getMotd();
 
         String plugins = "";
         Plugin[] plugs = Bukkit.getPluginManager().getPlugins();
         
         for (Plugin plug : plugs) {
             plugins += "\n  " + plug.getName();
         }
 
         try {
             // Create file 
             FileWriter fstream = new FileWriter("/tmp/" + sn + ".txt");
             BufferedWriter out;
             out = new BufferedWriter(fstream);
             out.write("Name: " + sn + "\n");
             //out.write("Description: " + motd + "\n");
             out.write("Version: " + sv + "\n");
             out.write("Plugins: " + plugins + "\n");
             out.write("Online: " + playernames + "\n");
 
             //Close the output stream
             out.close();
         } catch (Exception e) {//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
     }
 
     private ArrayList<String> getPlayers(String piq) {
         Player[] list = Bukkit.getOnlinePlayers();
 
         ArrayList<String> ops;
         ops = new ArrayList();
         for (Player op : list) {
             String pn = op.getName();
             String pw = op.getWorld().getName();
 
             if (!pn.equals(piq)) {
                 ops.add(pn + " (" + pw + ")");
             }
         }
         return ops;
     }
 }
