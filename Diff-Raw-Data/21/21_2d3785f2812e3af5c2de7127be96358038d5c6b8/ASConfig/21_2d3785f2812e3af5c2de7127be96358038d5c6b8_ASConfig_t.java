 package me.ambientseasons.util;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.getspout.spoutapi.block.SpoutWeather;
 
 import me.ambientseasons.AmbientSeasons;
 
 public class ASConfig {
 	private File hudMap;
 
 	private AmbientSeasons plugin;
 	private FileConfiguration config;
 
 	public ASConfig(AmbientSeasons plugin) {
 		this.plugin = plugin;
		this.plugin.getConfig().options().copyDefaults(true);
		
                if (!this.plugin.getConfigFile().exists())
                    this.plugin.saveConfig();
                
                this.config = this.plugin.getConfig();
                
 		loadMap();
 	}
 
 
 	public Set<String> getWorlds() {
 		return config.getConfigurationSection("worlds").getKeys(false);
 	}
 
 	public Set<String> getSeasons() {
 		return config.getConfigurationSection("seasons").getKeys(false);
 	}
 
 	public SpoutWeather getSeasonBiomeWeather(String season, String biome) {
 		SpoutWeather weather = SpoutWeather.RESET;
 		String weatherString = config.getString("seasons." + season + ".Biome_Weather." + biome, "RESET");
 		weather = SpoutWeather.valueOf(weatherString);
 		return weather;
 	}
 
 	public Set<String> getMonths(World world) {
 		return config.getConfigurationSection("worlds." + world.getName() + ".months").getKeys(false);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> getWeekdays(World world) {
 		return config.getList("worlds." + world.getName() + ".weekdays", null);
 	}
 
 	public String getSeason(String month, World world) {
 		return config.getString("worlds." + world.getName() + ".months." + month + ".season");
 	}
 
 	public int getMonthLength(String month, World world) {
 		return config.getInt("worlds." + world.getName() + ".months." + month + ".days", 30);
 	}
 
 	public String getSeasonURL(String season) {
 		return config.getString("seasons." + season + ".URL", "http://www.retributiongames.com/quandary/files/Quandary_4.1_Djilba.zip");
 	}
 
 	public Boolean isWorldEnabled(World world) {
 		return getWorlds().contains(world.getName());
 	}
 
 	public int getNumberOfMonths(World world) {
 		return getMonths(world).size();
 	}
 
 	public int getNumberOfWeekdays(World world) {
 		return getWeekdays(world).size();
 	}
 
 	public int getHUDY() {
 		return config.getInt("HUD_Y", 10);
 	}
 
 	public int getHUDX() {
 		return config.getInt("HUD_X", 0);
 	}
 
 	public String getDateMessage() {
 		return config.getString("date_message", "{WEEKDAY} the {DATE}{MOD} of {MONTH} {YEAR}AN");
 	}
 
 	public String getShortDateMessage() {
 		return config.getString("short_date_message", "{DATE}/{MONTH}/{YEAR}");
 	}
 
 	public int getFontSize() {
 		return config.getInt("font_size", 30);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadMap() {
 		hudMap = new File(plugin.getDataFolder(), "settings.bin");
 		if (!hudMap.exists()) {
 			System.out.println("Making new settings file");
 			saveMap();
 		} else {
 			plugin.setHUDEnable((HashMap<String, Boolean>) HMapSL.load(hudMap.getPath()));
 		}
 	}
 
 	public void saveMap() {
 		HMapSL.save(plugin.getHUDEnable(), hudMap.getPath());
 
 	}
 }
