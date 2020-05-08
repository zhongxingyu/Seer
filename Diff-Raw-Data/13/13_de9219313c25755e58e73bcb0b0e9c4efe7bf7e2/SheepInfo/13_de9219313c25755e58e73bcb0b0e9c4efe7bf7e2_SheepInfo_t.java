 
 package net.loadingchunks.plugins.SheepInfo;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.util.config.Configuration;
 import com.nijiko.coelho.iConomy.iConomy;
 
 /**
  * GuardWolf Ban System plugin for Bukkit
  *
  * @author Cue
  */
 public class SheepInfo extends JavaPlugin {
 	public final HashMap<String, String> siConfig = new HashMap<String, String>();
 	public final SIHTTPD httpd = new SIHTTPD(this);
 	public List<World> worlds;
 	public iConomy iConomy = null;
	public boolean disableMoney = false;
 
     public void onDisable() {
         System.out.println("[SHEEPINFO] Stopping...");
         this.httpd.Kill();
         System.out.println("[SHEEPINFO] Killed HTTPD Server.");
     }
 
     public void onEnable() {        
         // Register events
         
         PluginManager pm = getServer().getPluginManager();
         
         // Get the config.
         
         System.out.println("[SHEEPINFO] Loading config file plugins/SheepInfo/config.yml...");
         Configuration _config = new Configuration(new File("plugins/SheepInfo/config.yml"));
         
         _config.load();
         
         System.out.println("Loaded SheepInfo Config Successfully!");
         
         siConfig.put("bind_addr", _config.getString("sheep.bind.address"));
         siConfig.put("bind_port", _config.getString("sheep.bind.port"));
         
         siConfig.put("keys_ip", _config.getString("sheep.keys.ip"));
         
         System.out.println("SheepInfo Config saved to memory.");
         
         worlds = this.getServer().getWorlds();
         System.out.println("[SHEEPINFO] Generated Worlds List.");
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         
         System.out.println("[SHEEPINFO] Starting SheepInfo HTTPD on " + siConfig.get("bind_addr") + ":" + siConfig.get("bind_port"));
         if(!httpd.Listen())
         	System.out.println("[SHEEPINFO] Error starting SheepInfo HTTPD!");
         else
         	System.out.println("[SHEEPINFO] SheepInfo HTTPD successfully listening!");
         
         Plugin p = this.getServer().getPluginManager().getPlugin("iConomy");
         if(p != null)
         {
         	System.out.println("[SHEEPINFO] iConomy detected, initialising link...");
         	if (!this.getServer().getPluginManager().isPluginEnabled(p))
         		this.getServer().getPluginManager().enablePlugin(p);
         	iConomy = (iConomy) p;
         } else {
         	System.out.println("[SHEEPINFO] iConomy not found! :(");
        	this.disableMoney = true;
         }
     }
 }
