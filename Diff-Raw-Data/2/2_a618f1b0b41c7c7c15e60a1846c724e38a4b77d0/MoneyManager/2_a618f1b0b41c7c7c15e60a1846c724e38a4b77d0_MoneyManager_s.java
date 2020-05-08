 /**
  *  Name:    MoneyManager.java
  *  Created: 00:04:12 - 16 maj 2013
  * 
  *  Author:  Lucas Arnstrm - LucasEmanuel @ Bukkit forums
  *  Contact: lucasarnstrom(at)gmail(dot)com
  *  
  *
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *
  *  Filedescription:
  *
  * 
  */
 
 package me.lucasemanuel.publiceconomy.managers;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import me.lucasemanuel.publiceconomy.Main;
 import me.lucasemanuel.publiceconomy.threading.ConcurrentMySQLConnection;
 import me.lucasemanuel.publiceconomy.utils.ConsoleLogger;
 
 public class MoneyManager {
 	
 	private Main plugin;
 	private ConsoleLogger logger;
 	
 	private ConcurrentMySQLConnection mysql;
 	
 	private HashMap<String, Double> accounts;
 	private HashMap<String, Double> item_values;
 	
 	public MoneyManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger(instance, "MoneyManager");
 		
 		accounts    = new HashMap<String, Double>();
 		item_values = new HashMap<String, Double>();
 		
 		initiateMySQLConnector();
 		
 		loadValues();
 		loadAccounts();
 		
 		logger.debug("Initiated");
 	}
 	
 	private void initiateMySQLConnector() {
 		String username  = plugin.getConfig().getString("database.auth.username");
 		String password  = plugin.getConfig().getString("database.auth.password");
 		String host      = plugin.getConfig().getString("database.settings.host");
 		int    port      = plugin.getConfig().getInt   ("database.settings.port");
 		String database  = plugin.getConfig().getString("database.settings.database");
 		String tablename = plugin.getConfig().getString("database.settings.tablename");
 		
 		mysql = new ConcurrentMySQLConnection(username, password, host, port, database, tablename);
 		
 		logger.info("Testing connection towards MySQL-server, please wait...");
 		
 		try {
 			mysql.testConnection();
 			
 			logger.info("Successfully connected!");
 		}
 		catch (ClassNotFoundException | SQLException e) {
 			logger.severe("Could not connect to the MySQL-server! Message: " + e.getMessage());
 		}
 	}
 	
 	private void loadAccounts() {
 		
 		HashMap<String, Double> loaded_accounts = plugin.getDataStorage().retrieveAccounts();
 		
 		if(loaded_accounts != null) {
 			for(Entry<String, Double> entry : loaded_accounts.entrySet()) {
 				accounts.put(entry.getKey(), entry.getValue());
 			}
 		}
 	}
 	
 	private void loadValues() {
 		
 		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "item_values.yml"));
 		
 		checkDefaults(config);
 		
 		for(String item_name : config.getKeys(true)) {
 			item_values.put(item_name, config.getDouble(item_name));
 		}
 	}
 	
 	private void checkDefaults(FileConfiguration config) {
 		
 		Set<Material> d_items = EnumSet.noneOf(Material.class);
 		Collections.addAll(d_items, Material.values());
 		
 		Set<Enchantment> d_ench = new HashSet<Enchantment>();
 		Collections.addAll(d_ench, Enchantment.values());
 		
 		boolean save = false;
 		
 		for(Material m : d_items) {
 			if(!config.contains("items." + m.name())) {
 				config.set("items." + m.name(), 0.0);
 				save = true;
 			}
 		}
 		
 		for(Enchantment e : d_ench) {
 			if(!config.contains("enchantments." + e.getName())) {
 				config.set("enchantments." + e.getName(), 20.0);
 				save = true;
 			}
 		}
 		
 		if(save) {
 			try {
 				config.save(this.plugin.getDataFolder() + File.separator + "item_values.yml");
 			} catch (IOException e) {
 				logger.severe("Could not save item_values.yml!");
 			}
 		}
 	}
 
 	public Set<ItemStack> giveMoneyForItems(final String playername, ItemStack[] contents) {
 		
 		Set<ItemStack> worthless = new HashSet<ItemStack>();
 		
 		double money = 0.0d;
 		
 		for(ItemStack i : contents) {
 			
 			if(i == null) 
 				continue;
 			
 			double value = getValue(i, false);
 			
 			if(value == 0.0d) {
 				worthless.add(i);
 			}
 			
 			money += value;
 		}
 		
 		if(money != 0.0) {
 			logger.debug("Adding money=" + money + " to player=" + playername);
 			
 			Bukkit.getPlayerExact(playername).sendMessage("Du tjnade " + ChatColor.GOLD + money + ChatColor.WHITE + " kr.");
 			
 			if(accounts.containsKey(playername)) {
 				money += accounts.get(playername);
 			}
 			
 			accounts.put(playername, money);
 			
 			plugin.getScoreboardManager().updateBalance(playername);
 			
 			final double m = money;
 			
 			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
 				public void run() {
 					plugin.getDataStorage().updateBalance(playername, m);
 					mysql.updateBalance(playername, m);
 				}
 			});
 			
 			logger.debug("Current balance=" + accounts.get(playername));
 		}
 		
 		return worthless;
 	}
 	
 	public double getValue(ItemStack i, boolean single) {
 		double value = 0.0d;
 		
 		double v = item_values.get("items." + i.getType().name());
 		if(!single)
 			v *= i.getAmount();
 		
 		value += v;
 		
 		for(Entry<Enchantment, Integer> entry : i.getEnchantments().entrySet()) {
 			value += item_values.get("enchantments." + entry.getKey().getName()) * entry.getValue();
 		}
 		
 		double max = i.getType().getMaxDurability();
 		
 		if(max != 0.0d)
 			value = (double) Math.round(value * ((max - i.getDurability()) / max) * 100) / 100;
 		
 		logger.debug("value=" + value);
 		
 		return value;
 	}
 	
 	public double getBalance(String playername) {
 		if(accounts.containsKey(playername))
 			return accounts.get(playername);
 		else
 			return 0.0d;
 	}
 	
 	@SuppressWarnings("serial")
 	public void fixLore(final ItemStack is) {
 		ItemMeta im = is.getItemMeta();
 		
 		if(!im.hasLore()) {
 			im.setLore(new ArrayList<String>() {{
 				add("Vrde: " + plugin.getMoneyManager().getValue(is, true) + " kr.");
 			}});
 			
 			is.setItemMeta(im);
 		}
		else if(is.getType().getMaxDurability() != 0.0d) {
 			List<String> lore = im.getLore();
 			
 			Iterator<String> l = lore.iterator();
 			while(l.hasNext()) {
 				if(l.next().startsWith("Vrde: "))
 					l.remove();
 			}
 			
 			lore.add("Vrde: " + plugin.getMoneyManager().getValue(is, true) + " kr.");
 			im.setLore(lore);
 			
 			is.setItemMeta(im);
 		}
 	}
 }
