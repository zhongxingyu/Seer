 /*
  * This file is part of MinecartRevolution-Core.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * MinecartRevolution-Core is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MinecartRevolution-Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MinecartRevolution-Core. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.minecartrevolution.core.util;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import com.quartercode.quarterbukkit.api.FileUtils;
 import com.quartercode.quarterbukkit.api.exception.ExceptionHandler;
 import com.quartercode.quarterbukkit.api.exception.InstallException;
 import com.quartercode.quarterbukkit.api.query.FilesQuery;
 import com.quartercode.quarterbukkit.api.query.FilesQuery.ProjectFile;
 import com.quartercode.quarterbukkit.api.query.FilesQuery.VersionParser;
 import com.quartercode.quarterbukkit.api.query.QueryException;
 
 public class JarUpdater implements Updater {
 
     private final Plugin        plugin;
    private final int           projectId;
     private final VersionParser versionParser;
 
     public JarUpdater(Plugin plugin, int projectId, VersionParser versionParser) {
 
         this.plugin = plugin;
         this.projectId = projectId;
         this.versionParser = versionParser;
     }
 
     @Override
     public Plugin getPlugin() {
 
         return plugin;
     }
 
     @Override
     public ProjectFile getLatestVersion() throws QueryException, Exception {
 
         plugin.getLogger().info("Querying server mods api ...");
 
         // Get latest version
         List<ProjectFile> avaiableFiles = new ArrayList<ProjectFile>();
         avaiableFiles = new FilesQuery(projectId, versionParser).execute();
 
         return avaiableFiles.size() == 0 ? null : avaiableFiles.get(avaiableFiles.size() - 1);
     }
 
     @Override
     public boolean changeVersion(ProjectFile version) throws QueryException, Exception {
 
         plugin.getLogger().info("Installing " + plugin.getName() + " " + version.getVersion());
 
         // Variables
         File pluginJar = new File(plugin.getDataFolder().getParentFile(), File.separator + plugin.getName() + ".jar");
 
         // Disable plugin
         Bukkit.getPluginManager().disablePlugin(plugin);
 
         // Download jar
         FileUtils.download(version.getLocation().toURL(), pluginJar);
 
         // Load plugin from file
         try {
             Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(pluginJar));
         } catch (Exception e) {
             ExceptionHandler.exception(new InstallException(plugin, e, "Error while reloading the plugin with the new jar"));
             return false;
         }
 
         plugin.getLogger().info("Successfully changed version of " + plugin.getName() + " to " + version.getVersion() + "!");
         return true;
     }
 
 }
