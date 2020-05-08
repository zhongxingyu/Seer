 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  * Copyright 2012 StarTux
  *
  * This file is part of TradeControl.
  *
  * TradeControl is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TradeControl is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TradeControl.  If not, see <http://www.gnu.org/licenses/>.
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 package com.winthier.tradecontrol;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Map;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.inventory.ItemStack;
 
 public class Configuration {
         private TradeControlPlugin plugin;
         private List<TradeFilter> filters = new ArrayList<TradeFilter>();
         private Map<String, TradePattern> trades = new LinkedHashMap<String, TradePattern>();
 
         @SuppressWarnings("unchecked")
         public Configuration(TradeControlPlugin plugin) {
                 this.plugin = plugin;
                 try {
                         // write default config to disk once
                         File file = plugin.getDataFolder();
                         if (!file.exists()) file.mkdir();
                         file = new File(file, "config.yml");
                         if (!file.exists()) {
                                 InputStream in = plugin.getResource("config.yml");
                                 OutputStream out = new FileOutputStream(file);
                                 while (true) {
                                         int b = in.read();
                                         if (b == -1) break;
                                         out.write(b);
                                 }
                                 out.close();
                                 in.close();
                         }
                        plugin.reloadConfig();
                        for (Map.Entry<String, Object> entry : plugin.getConfig().getConfigurationSection("trades").getValues(false).entrySet()) {
                                Map<String, Object> map = ((ConfigurationSection)entry.getValue()).getValues(false);
                                TradePattern pattern = getTradePattern(map);
                                trades.put(entry.getKey(), pattern);
                        }
                        for (Object o : (List<Object>)plugin.getConfig().getList("filters")) {
                                filters.add(getTradeFilter((Map<String, Object>)o));
                        }
                 } catch (Exception e) {
                         e.printStackTrace();
                 }
         }
 
         @SuppressWarnings("unchecked")
         public TradeFilter getTradeFilter(Map<String, Object> map) throws Exception {
                 TradePattern pattern = getTradePattern((Map<String, Object>)map.get("trade"));
                 TradeFilter.Action action = TradeFilter.Action.getAction((String)map.get("action"));
                 TradeFilter result = new TradeFilter(pattern, action);
                 if (action == TradeFilter.Action.REPLACE) {
                         List<TradePattern> replacements = new ArrayList<TradePattern>();
                         for (String name : (List<String>)map.get("replacements")) {
                                 replacements.add(trades.get(name));
                         }
                         result.setReplacements(replacements);
                 }
                 return result;
         }
 
         @SuppressWarnings("unchecked")
         public static TradePattern getTradePattern(Map<String, Object> map) throws Exception {
                 List<Object> buy = (List<Object>)map.get("buy");
                 if (buy.size() > 2 || buy.size() < 1) throw new Exception("Expected 1 or 2 buy items");
                 ItemPattern buy1, buy2 = null, sell;
                 buy1 = getItemPattern((Map<String, Object>)buy.get(0));
                 if (buy.size() == 2) buy2 = getItemPattern((Map<String, Object>)buy.get(1));
                 Object o = map.get("sell");
                 Map sellMap = null;
                 if (o instanceof ConfigurationSection) sellMap = ((ConfigurationSection)o).getValues(false);
                 else if (o instanceof Map) sellMap = (Map<String, Object>)o;
                 sell = getItemPattern(sellMap);
                 return new DefaultTradePattern(buy1, buy2, sell);
         }
 
         @SuppressWarnings("unchecked")
         public static ItemPattern getItemPattern(Map<String, Object> map) throws Exception {
                 ItemStack stack = getItemStack(map);
                 int minAmount = 1;
                 int maxAmount = Integer.MAX_VALUE;
                 if (map.get("amount") != null) {
                         minAmount = maxAmount = (Integer)map.get("amount");
                 }
                 if (map.get("minamount") != null) minAmount = (Integer)map.get("minamount");
                 if (map.get("maxamount") != null) maxAmount = (Integer)map.get("maxamount");
                 return new ItemPattern(stack, minAmount, maxAmount);
         }
 
         @SuppressWarnings("unchecked")
         public static ItemStack getItemStack(Map<String, Object> conf) throws Exception {
                 Material mat = Material.getMaterial(((String)conf.get("type")).toUpperCase());
                 int amount = 1;
                 if (conf.get("amount") != null) {
                         amount = (Integer)conf.get("amount");
                 }
                 int damage = 0;
                 if (conf.get("damage") != null) {
                         damage = (Integer)conf.get("damage");
                 }
                 // System.out.println(mat + ":" + damage + " " + amount);
                 return new ItemStack(mat, amount, (short)damage);
         }
 
         public TradeFilter[] getFilters() {
                 return filters.toArray(new TradeFilter[0]);
         }
 }
