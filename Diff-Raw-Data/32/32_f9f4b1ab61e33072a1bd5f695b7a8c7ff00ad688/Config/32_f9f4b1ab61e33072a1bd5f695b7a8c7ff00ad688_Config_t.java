 /*
  * This file is part of AmbientSeasons (https://github.com/Olloth/AmbientSeasons).
  * 
  * AmbientSeasons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.ambientseasons.util;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import me.ambientseasons.AmbientSeasons;
 
 import org.bukkit.World;
 import org.bukkit.util.config.Configuration;
 
 public class Config {
 	private final String QUANDARY = "http://www.retributiongames.com/quandary/files/Quandary_4.1_";
 
 	// Hashmap file location
 	private File hudMap;
 
 	private AmbientSeasons plugin;
 	private File directory;
 	private File configFile;
 	private Configuration config;
 	
 	public Config (AmbientSeasons plugin) {
 		this.plugin = plugin;
 		
 		directory = plugin.getDataFolder();
 		configFile = new File(directory, "config.yml");
 		if (!directory.exists())
 			directory.mkdir();
 		if (!configFile.exists()) {
 			try {
 				configFile.createNewFile();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		load();
 
 		loadMap();
 	}
 
 	public void load() {
 		config = new Configuration(configFile);
 		config.load();
 
 		getWorlds();
 		config.save();
 		config.load();
 
 		getSeasons();
 		config.save();
 		config.load();
 
 		getDateMessage();
 		config.save();
 		config.load();
 
 		getHUDPosition();
 		config.save();
 		config.load();
		
		System.out.println(config.getString("worlds." + "world" + ".months." + "May" + ".days"));
 	}
 
 	public List<String> getWorlds() {
 
 		if (config.getKeys("worlds") == null) {
 			List<String> months = new ArrayList<String>();
 			months.add("January");
 			months.add("February");
 			months.add("March");
 			months.add("April");
 			months.add("May");
 			months.add("June");
 			months.add("July");
 			months.add("August");
 			months.add("September");
 			months.add("October");
 			months.add("November");
 			months.add("December");
 
 			ArrayList<String> weekdays = new ArrayList<String>();
 			weekdays.add("Sunday");
 			weekdays.add("Monday");
 			weekdays.add("Tuesday");
 			weekdays.add("Wednesday");
 			weekdays.add("Thursday");
 			weekdays.add("Friday");
 			weekdays.add("Saturday");
 
 			String mainWorld = plugin.getServer().getWorlds().get(0).getName();
 
 			for (String month : months) {
 				config.setProperty("worlds." + mainWorld + ".months." + month + ".season", "Djilba");
 				config.setProperty("worlds." + mainWorld + ".months." + month + ".days", "30");
 				config.save();
 				config.load();
 			}
 
 			config.setProperty("worlds." + mainWorld + ".weekdays", weekdays);
 			config.save();
 			config.load();
 
 		}
 
 		return config.getKeys("worlds");
 	}
 
 	public List<String> getSeasons() {
 		List<String> seasons = new ArrayList<String>();
 		seasons.add("Djilba");
 		seasons.add("Kamba");
 		seasons.add("Birak");
 		seasons.add("Bunuru");
 		seasons.add("Djeran");
 		seasons.add("Makuru");
 		if (config.getKeys("seasons") == null) {
 			for (String season : seasons) {
 				config.setProperty("seasons." + season + ".URL", QUANDARY + season + ".zip");
 
 				if (season.equals("Djilba"))
 					config.setProperty("seasons." + season + ".season_type", "Spring");
 				else if (season.equals("Kamba"))
 					config.setProperty("seasons." + season + ".season_type", "Spring");
 				else if (season.equals("Birak"))
 					config.setProperty("seasons." + season + ".season_type", "Summer");
 				else if (season.equals("Bunuru"))
 					config.setProperty("seasons." + season + ".season_type", "Summer");
 				else if (season.equals("Djeran"))
 					config.setProperty("seasons." + season + ".season_type", "Fall");
 				else if (season.equals("Makuru"))
 					config.setProperty("seasons." + season + ".season_type", "Winter");
 				else
 					config.setProperty("seasons." + season + ".season_type", "Spring");
 
 				config.save();
 				config.load();
 			}
 		}
 
 		return config.getKeys("seasons");
 	}
 
 	public List<String> getMonths(World world) {
 		return config.getKeys("worlds." + world.getName() + ".months");
 	}
 
 	public List<String> getWeekdays(World world) {
 		return config.getStringList("worlds." + world.getName() + ".weekdays", null);
 	}
 
 	public String getSeason(String month, World world) {
 		return config.getString("worlds." + world.getName() + ".months." + month + ".season");
 	}
 
 	public int getMonthLength(String month, World world) {
		try{
			return Integer.parseInt(config.getString("worlds." + world.getName() + ".months." + month + ".days"));
		} catch(Exception e) {
			return 30;
		}
 	}
 
 	public String getSeasonURL(String season) {
 		String string = config.getString("seasons." + season + ".URL", QUANDARY + "Djilba.zip");
 
 		return string;
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
 
 	public int getHUDPosition() {
 		return config.getInt("HUD_Y", 10);
 	}
 
 	public String getDateMessage() {
 		return config.getString("date_message", "{WEEKDAY} the {DATE}{MOD} of {MONTH} {YEAR}AN");
 	}
 
 	public int getFontSize() {
 		return config.getInt("font_size", 10);
 	}
 
 	public Configuration getConfig() {
 		return config;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadMap() {
 		hudMap = new File(directory, "settings.bin");
 		if (!hudMap.exists()) {
 			System.out.println("Making new settings file");
 			saveMap();
 		} else {
 			plugin.setHUDEnable((HashMap<String, Boolean>) HMapSL.load(hudMap.getPath()));
 		}
 	}
 
 	/**
 	 * Save the hash map HUDEnable
 	 */
 	public void saveMap() {
 		HMapSL.save(plugin.getHUDEnable(), hudMap.getPath());
 
 	}
 
 }
