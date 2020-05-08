 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.freedesktop_notifications;
 
 import com.dmdirc.config.prefs.PluginPreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesDialogModel;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.plugins.Exported;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.implementations.BaseCommandPlugin;
 
 import dagger.ObjectGraph;
 
 /**
  * This plugin adds freedesktop Style Notifications to dmdirc.
  */
 public class FreeDesktopNotificationsPlugin extends BaseCommandPlugin {
 
     /** This plugin's plugin info. */
     private final PluginInfo pluginInfo;
     /** Manager to show notifications. */
     private FDManager manager;
 
     public FreeDesktopNotificationsPlugin(final PluginInfo pluginInfo) {
         this.pluginInfo = pluginInfo;
     }
 
     @Override
     public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
         super.load(pluginInfo, graph);
         setObjectGraph(graph.plus(new FDModule(pluginInfo)));
         registerCommand(FDNotifyCommand.class, FDNotifyCommand.INFO);
         manager = getObjectGraph().get(FDManager.class);
     }
 
     /**
      * Used to show a notification using this plugin.
      *
      * @param title   Title of dialog if applicable
      * @param message Message to show
      *
      * @return True if the notification was shown.
      */
     @Exported
     public boolean showNotification(final String title, final String message) {
         return manager.showNotification(title, message);
     }
 
     /**
      * Called when the plugin is loaded.
      */
     @Override
     public void onLoad() {
         manager.onLoad();
         super.onLoad();
     }
 
     /**
      * Called when this plugin is Unloaded.
      */
     @Override
     public synchronized void onUnload() {
         manager.onUnLoad();
         super.onUnload();
     }
 
     @Override
     public void showConfig(final PreferencesDialogModel manager) {
         final PreferencesCategory general = new PluginPreferencesCategory(
                 pluginInfo, "FreeDesktop Notifications",
                 "General configuration for FreeDesktop Notifications plugin.");
 
         general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                 pluginInfo.getDomain(), "general.timeout", "Timeout",
                 "Length of time in seconds before the notification popup closes.",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.FILE,
                 pluginInfo.getDomain(), "general.icon", "icon",
                 "Path to icon to use on the notification.",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "advanced.escapehtml", "Escape HTML",
                 "Some Implementations randomly parse HTML, escape it before showing?",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "advanced.strictescape", "Strict Escape HTML",
                 "Strictly escape HTML or just the basic characters? (&, < and >)",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "advanced.stripcodes", "Strip Control Codes",
                 "Strip IRC Control codes from messages?",
                 manager.getConfigManager(), manager.getIdentity()));
 
         manager.getCategory("Plugins").addSubCategory(general);
     }
 
 }
