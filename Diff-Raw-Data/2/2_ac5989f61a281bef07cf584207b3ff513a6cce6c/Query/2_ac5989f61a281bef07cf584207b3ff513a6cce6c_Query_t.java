 /*
  *  McDesktopInfo - A Bukkit plugin + Windows Sidebar Gadget
  *  Copyright (C) 2012  Damarus
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
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.damarus.mcdesktopinfo.queries;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.Plugin;
 
 import de.damarus.mcdesktopinfo.Config;
 
 public abstract class Query {
 
     private long                lastExec  = 0;
     private String              lastValue = "";
 
     private final Configuration config;
     private final Plugin        plugin;
     private final String        query;
     private final Server        server;
     private final boolean       hasTimeout;
 
     protected Query(String query, boolean hasTimeout) {
         this.server = Bukkit.getServer();
         this.plugin = server.getPluginManager().getPlugin(Config.PLUGIN_NAME);
         this.config = plugin.getConfig();
         this.query = query;
         this.hasTimeout = hasTimeout;
 
         if(!(config.getStringList("userQueries").contains(query) || config.getStringList("adminQueries").contains(query) || config.getStringList("disabledQueries").contains(query))) {
             List<String> disabled = plugin.getConfig().getStringList("disabledQueries");
             disabled.add(query);
             config.set("disabledQueries", disabled);
         }
     }
 
     protected abstract String exec(HashMap<String, String> params);
 
     public String execute(HashMap<String, String> params) {
         if(isAdminOnly() && config.getString("adminPw").isEmpty()) return "";
        if(hasTimeout() && System.currentTimeMillis() - lastExec < plugin.getConfig().getInt("valueTimeout")) return lastValue;
         lastExec = System.currentTimeMillis();
         return lastValue = exec(params);
     }
 
     public Plugin getPlugin() {
         return plugin;
     }
 
     public String getQuery() {
         return query;
     }
 
     public Server getServer() {
         return server;
     }
 
     public boolean hasTimeout() {
         return hasTimeout;
     }
 
     public boolean isAdminOnly() {
         return plugin.getConfig().getStringList("adminQueries").contains(query);
     }
 
     public boolean isDisabled() {
         return plugin.getConfig().getStringList("disabledQueries").contains(query);
     }
 
     public boolean isUserExecutable() {
         return plugin.getConfig().getStringList("userQueries").contains(query);
     }
 }
