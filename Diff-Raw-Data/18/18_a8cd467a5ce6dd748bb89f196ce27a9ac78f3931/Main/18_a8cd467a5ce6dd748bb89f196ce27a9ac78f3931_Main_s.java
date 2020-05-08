 package me.Paldiu.NNO;
 
 import java.util.logging.Logger;
 import me.Paldiu.NNO.Listeners.PlayerListener;
 import org.bukkit.plugin.java.JavaPlugin;
 import me.Paldiu.NNO.Commands.*;
 
 public class Main extends JavaPlugin
 {
     public Censor censor = new Censor(this);
    
     public static final Logger log = Logger.getLogger("Minecraft");
     
     @Override
	public void onEnable() {
                getCommand("censor").setExecutor(censor);
                this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
                 
		log.info(String.format("[%s] Version: %s by %s has been Enabled!", getDescription().getName(), getDescription().getVersion(), getDescription().getAuthors()));
	}
     
     @Override
     public void onDisable()
     {
         log.info("Censors v1.00 Alpha has been disabled.");
     }
}
