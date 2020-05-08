 package com.mtihc.regionselfservice.v2.plugin;
 
 import java.io.File;
 import java.util.List;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.mtihc.regionselfservice.v2.plots.IPlotManagerConfig;
 import com.mtihc.regionselfservice.v2.plugin.util.YamlFile;
 
 public class PlotManagerConfig extends YamlFile implements IPlotManagerConfig {
 
 	
 	
 	public PlotManagerConfig(JavaPlugin plugin, File file) {
 		super(file, plugin.getLogger());
 	}
 
 	public PlotManagerConfig(JavaPlugin plugin, String filePath) {
		super(filePath, plugin.getLogger());
 	}
 
 	public List<String> getFirstLineForRent() {
 		return getConfig().getStringList("sign_first_line.for_rent");
 	}
 
 	public List<String> getFirstLineForSale() {
 		return getConfig().getStringList("sign_first_line.for_sale");
 	}
 }
