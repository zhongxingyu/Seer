 /*
  * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.jmhertlein.core.reporting.bugs;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Objects;
 import org.bukkit.Server;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 /**
  * The BugReport class represents a collection of information a developer would
  * want when trying to fix an unhandled exception. It is serializable, and is
  * intended to be sent over a network to a remote report-handing server.
  *
  * @author joshua
  */
 public class BugReport {
 
     private FileConfiguration f;
 
     /**
      * Constructs a new bug report based on provided environment variables
      *
      * @param plugin the plugin whose command caused an unhandled exception
      * @param s the Bukkit server the exception ocurred on
      * @param e the Exception object itself
      * @param env any plugin-specific environment data necessary to help
      * diagnose the problem
      */
     public BugReport(Plugin plugin, Server s, Exception e, String env) {
         f = new YamlConfiguration();
         
         f.set("plugin.name", plugin.getName());
         f.set("plugin.version", plugin.getDescription().getVersion());
 
         f.set("server.implementation.version", plugin.getServer().getVersion());
         f.set("server.implementation.name", plugin.getServer().getName());
         f.set("server.bukkit.version", plugin.getServer().getBukkitVersion());
         
         List<String> pluginNames = new LinkedList<>();
         for (Plugin plug : plugin.getServer().getPluginManager().getPlugins()) {
             pluginNames.add(plug.getName());
         }
         f.set("server.plugins", pluginNames);
 
         f.set("os.name", System.getProperty("os.name"));
         f.set("os.arch", System.getProperty("os.arch"));
         f.set("os.version", System.getProperty("os.version"));
         
         f.set("java.vendor.name", System.getProperty("java.vendor"));
         f.set("java.version", System.getProperty("java.version"));
         f.set("java.vendor.url", System.getProperty("java.vendor.url"));
 
         f.set("exception.environment", env);
         f.set("exception.class", e.getClass().toString());
         f.set("exception.message", e.getMessage());
 
         List<String> stack = new LinkedList<>();
         for (StackTraceElement ste : e.getStackTrace()) {
             stack.add(ste.toString());
         }
         f.set("exception.stack", stack);
     }
 
     public BugReport(String s) throws InvalidConfigurationException {
         f = new YamlConfiguration();
         f.loadFromString(s);
     }
     
     
 
     @Override
     public String toString() {
         return f.saveToString();
     }
 
     /**
      * It's not uncommon for the Bukkit server to be unable to reliably
      * determine what the machine's external IP address is. This permits the IP
      * to be updated by the remote server as the report is received.
      *
      * @param ip
      */
     public void setIp(String ip) {
         f.set("server.ip", ip);
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.f);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final BugReport other = (BugReport) obj;
        return this.f.toString().equals(other.f.toString());
     }
 }
