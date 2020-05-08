 package com.precipicegames.autoenchanter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Listener;
 
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.precipicegames.autoenchanter.listeners.BlockLstn;
 import com.precipicegames.autoenchanter.listeners.EntityLstn;
 import com.precipicegames.autoenchanter.listeners.PlayerLstn;
 
 public class Autoenchanter extends JavaPlugin{
     public YamlConfiguration config;
     public HashMap<Player, ItemStatus> trackedItems;
     
 	public void onDisable() {
         // TODO: Place any custom disable code here.
         System.out.println(this + " is now disabled!");
     }
 
     public void onEnable() {
     	this.getDataFolder().mkdirs();
     	File f = new File(this.getDataFolder(),"config.yml");
     	try {
 			f.createNewFile();
 		} catch (IOException e1) {
     		System.out.println(this + ": Error creating configuration file");
     		return;
 		}
     	
 
     	config = new YamlConfiguration();
     	try {
 			config.load(f);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		trackedItems = new HashMap<Player, ItemStatus>();
 		
 		
 		//Register Block break event
 		Listener block = new BlockLstn(this);
 		Listener plListen = new PlayerLstn(this);
 		Listener eListen = new EntityLstn(this);
     	this.getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, block , Priority.Monitor,this);
     	this.getServer().getPluginManager().registerEvent(Type.BLOCK_PLACE, block , Priority.Monitor,this);
     	//this.getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, plListen , Priority.Monitor,this);
     	this.getServer().getPluginManager().registerEvent(Type.ENTITY_DAMAGE, eListen , Priority.Monitor,this);
     	this.getServer().getPluginManager().registerEvent(Type.PLAYER_PICKUP_ITEM, plListen , Priority.Monitor,this);
     	this.getServer().getPluginManager().registerEvent(Type.PLAYER_FISH, plListen , Priority.Monitor,this);
     	
     	
         System.out.println(this + " is now enabled!");
     }
     
     
     public ConfigurationSection basicConfigurationHandler(String eventName, Player player, Material m)
     {
 		if(!config.isConfigurationSection(m.toString()))
 			return null;
 		
 		ConfigurationSection c = config.getConfigurationSection(m.toString());
 		if(!c.isConfigurationSection(eventName))
 			return null;
 		
 		return c.getConfigurationSection(eventName);
     }
     
     public void basicActionHandler(ConfigurationSection subc, Player player, ItemStack I)
     {
 		UniqueItem item = new UniqueItem(I);
 		String EnchantName = subc.getString("enchant");
 		if(EnchantName == null)
 			return;
 		Enchantment e = Enchantment.getByName(EnchantName);
 		if(e == null)
 			return;
 		
 		int maxlevel = subc.getInt("maxLevel", e.getMaxLevel());
 		Double rate = subc.getDouble("rate",0.1);
 		Double levelratefactor = subc.getDouble("levelFactor",0.1);
 		Double levelcurvefactor = subc.getDouble("levelCurveFactor",2);
 		Double levelup = subc.getDouble("levelRequirement",10);
 		
 		
 		System.out.println(this + " We have a working event!\n" + rate);
 		System.out.println(levelratefactor);
 		System.out.println(levelcurvefactor);
 		System.out.println(levelup);
 		System.out.println(maxlevel);
 		
 		
 		if(item.get().getEnchantmentLevel(e) >= maxlevel)
 			return;
 
 		if(!trackedItems.containsKey(player))
 			trackedItems.put(player, new ItemStatus());
 		
 		if(!trackedItems.get(player).containsKey(item))
 			trackedItems.get(player).put(item, new EnchantDetails());
 		
 		if(!trackedItems.get(player).get(item).containsKey(e))
 			trackedItems.get(player).get(item).put(e, new Double(0.0));
 		
 		Double trackedlevel = trackedItems.get(player).get(item).get(e);
		//double lvl = item.get().getEnchantmentLevel(e);
 		//double newlevel = new Double(trackedlevel + rate - (rate * ((rate*lvl+1)/(levelratefactor*Math.pow(lvl,levelcurvefactor) + rate*lvl+1))));
 		Double newlevel = new Double(trackedlevel + rate);
 		System.out.println("old level: " + trackedlevel + " new level: " + newlevel);
		if(newlevel >= levelup)
 		{
 			trackedItems.get(player).get(item).remove(e);
 			item.get().addEnchantment(e, item.get().getEnchantmentLevel(e) + 1);
 			//trackedItems.get(player).get(item).put(e , new Double(0.0));
 			player.sendMessage(ChatColor.GREEN + "Congrats you have leveled up an Item!");
 		}
 		else
 		{
 			trackedItems.get(player).get(item).put(e, newlevel);
 			System.out.println("saved level: " + trackedItems.get(player).get(item).get(e));
 			System.out.println("amount of items: " + trackedItems.get(player).size() + " hashcode: " + item.hashCode());
 		}
 		return;
     }
 }
