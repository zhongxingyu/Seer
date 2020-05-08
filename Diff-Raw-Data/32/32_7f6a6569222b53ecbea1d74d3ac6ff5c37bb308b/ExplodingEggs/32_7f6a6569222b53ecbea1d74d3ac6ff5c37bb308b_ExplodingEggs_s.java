 package pro.homiecraft;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import pro.homiecraft.Metrics.Graph;
 import pro.homiecraft.Commands.Admin;
 import pro.homiecraft.Commands.ee;
 
 public class ExplodingEggs extends JavaPlugin {
 	public static ExplodingEggs pluginST;
 	public Logger log = Logger.getLogger("Minecraft");
 
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		//pm.registerEvents(new Egg(), this);
 		pm.registerEvents(new Explode(), this);
 		pm.registerEvents(new PlayerJoin(), this);
		//pm.registerEvents(new PlayerLeave(), this);
 		pm.registerEvents(new WorldChange(), this);
 		
 		ExplodingEggs.pluginST = this;
 		
 		getCommands(this);
 		configSetup();
 		
 		initMetrics();
 		
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 		this.reloadConfig();
 	}
 	
 	public void getCommands(ExplodingEggs ExplodingEggs){
 		this.getCommand("ee").setExecutor(new ee());
 		this.getCommand("explodingeggs").setExecutor(new Admin());
 	}
 	
 	public void configSetup(){
 		if (this.getConfig().contains("ExplodingEggs.Damage.Disable-Block-Damage")){
 			this.getConfig().set("ExplodingEggs.Damage.Disable-Block-Damage", "Do not use this one. Use 'Block-Damage'. You can remove this setting from the config!");
 		}
 	}
 	
 	public void initMetrics(){
 		try {
 		    Metrics metrics = new Metrics(this);
 		    
 		    Graph randomGraph = metrics.createGraph("Random Explode");
 		    Graph enabledGraph = metrics.createGraph("Enabled by Default");
 		    Graph blockDMG = metrics.createGraph("Block DMG");
 		    
 		    randomGraph.addPlotter(new Metrics.Plotter("Enabled") {
 				
 				@Override
 				public int getValue() {
 					int i = 0;
 					boolean stat = ExplodingEggs.pluginST.getConfig().getBoolean("ExplodingEggs.Explosion.Random-Explode");
 					if (stat == true){
 						i++;
 					}
 					return i;
 				}
 			});
 		    
 		    randomGraph.addPlotter(new Metrics.Plotter("Disabled") {
 				
 				@Override
 				public int getValue() {
 					int i = 0;
 					boolean stat = ExplodingEggs.pluginST.getConfig().getBoolean("ExplodingEggs.Explosion.Random-Explode");
 					if (stat == false){
 						i++;
 					}
 					return i;
 				}
 			});
 		    
 		    enabledGraph.addPlotter(new Metrics.Plotter("Enabled") {
 				
 				@Override
 				public int getValue() {
 					int i = 0;
 					boolean stat = ExplodingEggs.pluginST.getConfig().getBoolean("ExplodingEggs.Explosion.Enabled-By-Default");
 					if (stat == true){
 						i++;
 					}
 					return i;
 				}
 			});
 		    
 		    enabledGraph.addPlotter(new Metrics.Plotter("Disabled") {
 				
 				@Override
 				public int getValue() {
 					int i = 0;
 					boolean stat = ExplodingEggs.pluginST.getConfig().getBoolean("ExplodingEggs.Explosion.Enabled-By-Default");
 					if (stat == false){
 						i++;
 					}
 					return i;
 				}
 			});
 		    
 		    blockDMG.addPlotter(new Metrics.Plotter("Enabled") {
 				
 				@Override
 				public int getValue() {
 					int i = 0;
 					boolean stat = ExplodingEggs.pluginST.getConfig().getBoolean("ExplodingEggs.Damage.Block-Damage");
 					if (stat == true){
 						i++;
 					}
 					return i;
 				}
 			});
 		    
 		    blockDMG.addPlotter(new Metrics.Plotter("Disabled") {
 				
 				@Override
 				public int getValue() {
 					int i = 0;
 					boolean stat = ExplodingEggs.pluginST.getConfig().getBoolean("ExplodingEggs.Damage.Block-Damage");
 					if (stat == false){
 						i++;
 					}
 					return i;
 				}
 			});
 		    
 		    metrics.start();
 		} catch (IOException e) {
 		    // Failed to submit the stats :-(
 		}
 	}
 }
