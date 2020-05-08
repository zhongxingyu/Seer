 package com.mtihc.regionselfservice.v2.plugin;
 
 import java.io.File;
import java.io.InputStream;
 import java.util.List;
 
import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.mtihc.regionselfservice.v2.plots.IPlotManagerConfig;
 import com.mtihc.regionselfservice.v2.plugin.util.YamlFile;
 
 public class PlotManagerConfig extends YamlFile implements IPlotManagerConfig {
 
 	
 	
	private JavaPlugin plugin;

 	public PlotManagerConfig(JavaPlugin plugin, File file) {
 		super(file, plugin.getLogger());
		this.plugin = plugin;
 	}
 
 	public PlotManagerConfig(JavaPlugin plugin, String filePath) {
		this(plugin, new File(filePath));
 	}
 
 	public List<String> getFirstLineForRent() {
 		return getConfig().getStringList("sign_first_line.for_rent");
 	}
 
 	public List<String> getFirstLineForSale() {
 		return getConfig().getStringList("sign_first_line.for_sale");
 	}
	
	@Override
	public void reload() {
		super.reload();
		InputStream resource = plugin.getResource("config.yml");
		if (resource != null) {
			YamlConfiguration defConfig = YamlConfiguration
				.loadConfiguration(resource);
			getConfig().options().copyDefaults(true);
			getConfig().setDefaults(defConfig);
			save();
		}
	}
 }
