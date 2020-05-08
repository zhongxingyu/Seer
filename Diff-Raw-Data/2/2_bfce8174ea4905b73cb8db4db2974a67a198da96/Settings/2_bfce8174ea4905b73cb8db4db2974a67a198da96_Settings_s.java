 package com.matejdro.bukkit.monsterhunt;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Settings {
 	
 	public static Settings globals;
 
 	public YamlConfiguration config;
 	private Settings parent;
 
 	public static void loadGlobals(File file) throws FileNotFoundException, IOException, InvalidConfigurationException
 	{
 		globals = new Settings(new YamlConfiguration());
 		globals.config.load(file);
 	}
 
 	public Settings(File file, Settings parent)
 	{
 		this.parent = parent;
 		config = new YamlConfiguration();
 		if (file.exists())
 		{
 			try
 			{
 				config.load(file);
 			} catch (IOException e)
 			{
 				e.printStackTrace();
 			} catch (InvalidConfigurationException e)
 			{
 				Log.warning("Configuration file " + file.getName() + " is invalid. Swaping with parent values.");
 			}
 		}
 		else
 		{
 			Log.warning("Configuration file " + file.getName() + " is missing. Swaping with parent values.");
 		}
 	}
 
 	public Settings(YamlConfiguration config)
 	{
 		this.config = config;
 	}
 
 	public int getInt(Setting setting)
 	{
 		if (config.contains(setting.getString()))
 			return config.getInt(setting.getString());
 		else
 			return parent.getInt(setting);
 	}
 
 	public String getString(Setting setting)
 	{
 		String property = (String) config.get(setting.getString());
 		if (property == null)
 		{
 			property = parent.getString(setting);
 		}
 		return property;
 	}
 
 	public boolean getBoolean(Setting setting)
 	{
 		return config.getBoolean(setting.getString(), parent != null ? parent.getBoolean(setting) : false);
 	}
 
 	public int getPlaceInt(Setting setting, int place)
 	{
 		Integer property = (Integer) config.get(setting.getString() + String.valueOf(place));
 		if (property == null)
 		{
 			property = parent.getPlaceInt(setting, place);
 		}
 		return property;
 	}
 
 	public String getPlaceString(Setting setting, int place)
 	{
 		String property = (String) config.get(setting.getString() + String.valueOf(place));
 		if (property == null)
 		{
 			property = parent.getPlaceString(setting, place);
 		}
 		return property;
 	}
 
 	public int getMonsterValue(String mobname, String killer)
 	{
 		String setting = "Points.Mobs." + mobname + "." + killer;
 		String generalSetting = "Points.Mobs." + mobname + ".General";
 
 		Integer mobValue = (Integer) getFromHierarchy(setting);
 		Integer generalMobValue = (Integer) getFromHierarchy(generalSetting);
 		
 		if (mobValue != null)
 			return mobValue;
 		else if (generalMobValue != null)
 			return generalMobValue;
 		else 
 			return 0;
 	}
 
 	public String getEffectPenalty(String effect, int level)
 	{
 		String setting = "Points.EffectPenalty." + effect.toLowerCase() + "_" + level;
 
 		Object penaltyO = getFromHierarchy(setting);
 		
 		
 		if (penaltyO != null)
 		{
 			if (penaltyO instanceof Integer)
 				return (Integer) penaltyO + "";
 			else
 				return (String) penaltyO;
 		}
 		else
 			return "0";
 	}
 
 	public String getKillMessage(String cause)
 	{
 		String setting = "Messages.KillMessage" + cause;
 		String generalSetting = "Messages.KillMessageGeneral";
 		
 		String killMessage = (String) getFromHierarchy(setting);
 		String generalKillMessage = (String) getFromHierarchy(generalSetting);
 		
 		if (killMessage != null)
 			return killMessage;
 		else if (generalKillMessage != null)
 			return generalKillMessage;
 		else 
 			return "";
 		
 	}
 	
 	private Object getFromHierarchy(String setting)
 	{
 		if (config.isSet(setting))
 		{
 			return config.get(setting);
 		} 
 		else if (parent != null)
 		{
 			return parent.getFromHierarchy(setting);
 		} 
 		else 
 			return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<HuntSpecification> getListOfHunts()
 	{
 		List<Map<String, Object>> huntsRaw = (List<Map<String, Object>>) config.getList("Hunts");
 		List<HuntSpecification> hunts = new ArrayList<HuntSpecification>();
 		if (huntsRaw != null)
 		{
 	        for(Map<String, Object> huntSpecRaw : huntsRaw)
 	        {
 	        	String name = (String) huntSpecRaw.get("Name");
 	        	String displayName = (String) huntSpecRaw.get("DisplayName");
	        	int chance = (int) huntSpecRaw.get("Chance");
 	        	
 	        	String filename = (String) huntSpecRaw.get("Name");
 	        	Settings huntSettings;
 	        	if(name.equals("Default"))
 	        		huntSettings = this;
 	        	else
 	        	huntSettings = new Settings(new File("plugins" + File.separator + "MonsterHunt" + File.separator, filename + ".yml"), this);
 	        	
 	        	hunts.add(new HuntSpecification(name, displayName, chance, huntSettings));
 	        }
 		}
 		return hunts;
 	}
 	
 }
