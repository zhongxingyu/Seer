 package me.bootscreen.workingslots;
 
 import java.io.File;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.*;
 
 
 /**
 * WorkingSlots for CraftBukkit/Spout
 *
 * handels all functions about the config
 * 
 * @author Bootscreen
 *
 */
 
 
 public class FileManager {
 	
 	private static String ordner = "plugins/WorkingSlots";
 	private static File configFile = new File(ordner + File.separator + "config.yml");
 	@SuppressWarnings("deprecation")
 	private static Configuration config;
 
 	@SuppressWarnings("deprecation")
 	private Configuration loadConfig()
 	{
 		try{
 			Configuration config = new Configuration(configFile);
 			config.load();
 			return config;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void createConfig()
 	{
 		new File(ordner).mkdir();
 		
 		if(!configFile.exists())
 		{
 			try 
 			{
 				configFile.createNewFile();
 				
 				config = loadConfig();
 				config.setProperty("Preset_0", 0);
 				config.setProperty("Preset_1", 0);
 				config.setProperty("Preset_2", 0);
 				config.setProperty("Preset_3", 0);
 				config.setProperty("Preset_4", 0);
 				config.setProperty("Preset_5", 0);
 				config.setProperty("Preset_6", 0);
 				config.setProperty("Preset_7", 0);
 				config.setProperty("Preset_8", 0);
 				config.setProperty("Preset_9", 0);
 				config.save();
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		config = loadConfig();
 	}
 	
 	@SuppressWarnings("deprecation")
 	public boolean savePreset(Player player, int number)
 	{
 		String wert = "";
 		if(configFile.exists())
 		{
 			try 
 			{
 				ItemStack item;
 				for(int i = 0; i < 9; i++)
 				{
 					item = player.getInventory().getItem(i);
 
 					if(i != 0)
 					{
 						wert += ",";
 					}
 
 					wert += item.getTypeId();
 						
 					if(item.getType().getMaxDurability() == 0 && item.getDurability() > 0)
 					{
 						wert += "-"+item.getDurability();
 					}									
 				}
 				
 				switch(number)
 				{
 					case 0:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 1:
 						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 2:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 3:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 4:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 5:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 6:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 7:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 8:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					case 9:
						config.setProperty("Preset_1", wert);
 						config.save();
 						return true;
 					default:											
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	public String readString(String key)
 	{
 		String value = config.getString(key,"");
 		return value;
 	}
 	
 	public boolean loadPreset(Player player, int presetnum)
 	{
 		boolean succeed = false;
 
 		try
 		{
 			String[] presets = new String[10];
 			presets[0] = readString("Preset_0");
 			presets[1] = readString("Preset_1");
 			presets[2] = readString("Preset_2");
 			presets[3] = readString("Preset_3");
 			presets[4] = readString("Preset_4");
 			presets[5] = readString("Preset_5");
 			presets[6] = readString("Preset_6");
 			presets[7] = readString("Preset_7");
 			presets[8] = readString("Preset_8");
 			presets[9] = readString("Preset_9");
 	
 			String[] temp;
 			String delimiter = ",";
 			temp = presets[presetnum].split(delimiter);
 			
 			for(int i = 0; i < temp.length && i < 9 ; i++)
 			{
 				String[] temp2;
 				String delimiter2 = "-";
 				temp2 = temp[i].split(delimiter2);
 				int item = Integer.parseInt(temp2[0]);
 				if(temp2.length == 2)
 				{
 					short dmgvalue = Short.parseShort(temp2[1]);
 					player.getInventory().setItem(i, new ItemStack(item, 1, dmgvalue));
 				}
 				else
 				{
 					player.getInventory().setItem(i, new ItemStack(item, 1));
 				}
 			}
 			
 			succeed = true;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			succeed = false;
 		}
 		
 		return succeed;
 	}
 	
 }
