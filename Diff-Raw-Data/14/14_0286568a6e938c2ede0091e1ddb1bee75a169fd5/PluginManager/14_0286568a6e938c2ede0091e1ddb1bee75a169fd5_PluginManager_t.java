 /*
  * Copyright (C) 2007-2013 Crafter Software Corporation.
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
 package org.craftercms.plugin;
 
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Sumer Jabri
  * @author Carlos Ortiz.
  */
 public interface PluginManager {
     /**
      * Initialize the plugin manager.
      *
      * @param pluginFolders   folders that contain plugins
      * @param contextRegistry API contexts registry for various plugin types
      * @throws PluginException
      */
     void init(List<String> pluginFolders, Map<String, Context> contextRegistry) throws PluginException;
 
     /**
     * Destroy the plugin manager.
      *
      * @throws PluginException
      */
     void destroy() throws PluginException;
 
     /**
      * Register a Context for the Given plugin type.
      * A context will be to the plugin to call {@link Plugin#init(Context)}
      * <b>Each plugin type will probably have a different context</b>
      *
      * @param type    Plugin Type.
      * @param context Context impl.
      * @throws PluginException
      */
     void registerContext(final String type, final Context context) throws PluginException;
 
     /**
      * Install a plugin on disk and load it as disabled.
      *
      * @param plugin input stream of the plugin binary
      * @throws PluginException
      */
     void installPlugin(final InputStream plugin) throws PluginException;
 
     /**
      * Deactivate, destroy and delete a plugin from disk.
      *
      * @param pluginId ID of the plugin to remove
      * @throws PluginException
      */
     void uninstallPlugin(final String pluginId) throws PluginException;
 
     /**
      * Load a plugin from disk if not already loaded and initialize it by calling its init method, upon success
      * set the state of the plugin to enabled.
      * If the init method of the plugin fails, the plugin is marked as disabled
      *
      * @param pluginId plugin Id
      * @throws PluginException
      */
     void enablePlugin(String pluginId) throws PluginException;
 
     /**
      * Call the destroy method of the plugin and mark it as disabled.
      *
      * @param pluginId
      * @throws PluginException
      */
     void disablePlugin(String pluginId) throws PluginException;
 
     /**
      * Call the activate method of the plugin and mark it as active.
      *
      * @param pluginId
      * @throws PluginException
      */
     void activatePlugin(String pluginId) throws PluginException;
 
     /**
     * Call the deactivate method of the plugin and mark it as inactive.
      *
      * @param pluginId
      * @throws PluginException
      */
     void deactivatePlugin(String pluginId) throws PluginException;
 
     /**
      * Return the plugin information.
      *
      * @param pluginId Id of the plugin
      * @return plugin information of type {@link PluginInfo}
      * @throws PluginException
      */
     PluginInfo getPluginInfo(String pluginId) throws PluginException;
 
     /**
      * List all plugins in the system.
      *
      * @return list of all plugins' {@link PluginInfo}
      * @throws PluginException
      */
     List<PluginInfo> listAllPlugins() throws PluginException;
 
     /**
      * List all plugins of a certain type.
      *
      * @param pluginType type of plugin
      * @return list of {@link PluginInfo}
      * @throws PluginException
      */
     List<PluginInfo> listPluginsByType(final String pluginType) throws PluginException;
 
     /**
      * List all plugins by plugin state.
      *
      * @param state plugin state, see {@link PluginManager.PluginState}
      * @return list of {@link PluginInfo}
      * @throws PluginException
      */
     List<PluginInfo> listPluginsByState(final PluginState state) throws PluginException;
 
     /**
      * List plugins given a type and state.
      *
      * @param pluginType plugin type
      * @param state      plugin state
      * @return list of {@link PluginInfo}
      * @throws PluginException
      */
     List<PluginInfo> listPluginsByTypeByState(final String pluginType, final PluginState state) throws PluginException;
 
     /**
      * Get a plugin instance cast to the class of the plugin.
      *
      * @param pluginId Plugin id to get
      * @param clazz    Class to be Cast
      * @param <T>      Class to be wanted.
      * @return A instance of the given class that implements {@link Plugin}
      * @throws PluginException
      */
     <T extends Plugin> T getPlugin(final String pluginId, final Class<T> clazz) throws PluginException;
 
     /**
      * The states of a plugin.
      */
     enum PluginState {
         /**
         * Enabled and Active.
          */
         ENABLED_ACTIVE,
         /**
         * Enabled and Inactive.
          */
         ENABLED_INACTIVE,
         /**
         * Disabled.
          */
         DISABLED
     }
 }
