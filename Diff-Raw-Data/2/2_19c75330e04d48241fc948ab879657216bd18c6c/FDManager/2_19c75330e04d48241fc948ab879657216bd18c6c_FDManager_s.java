 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.ClientModule.UserConfig;
 import com.dmdirc.config.prefs.PluginPreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesDialogModel;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.events.ClientPrefsOpenedEvent;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.interfaces.config.ConfigProvider;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.implementations.PluginFilesHelper;
 import com.dmdirc.ui.messages.Styliser;
 import com.dmdirc.util.io.StreamUtils;
 
 import com.google.common.base.Strings;
 import com.google.common.html.HtmlEscapers;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.engio.mbassy.listener.Handler;
 
 import static com.dmdirc.util.LogUtils.USER_ERROR;
 
 @Singleton
 public class FDManager implements ConfigChangeListener {
 
     private static final Logger LOG = LoggerFactory.getLogger(FDManager.class);
     /** Global configuration. */
     private final AggregateConfigProvider config;
     /** User configuration. */
     private final ConfigProvider userConfig;
     /** This plugin's settings domain. */
     private final String domain;
     /** Plugin files helper. */
     private final PluginFilesHelper filesHelper;
     private final PluginInfo pluginInfo;
     /** notification timeout. */
     private int timeout;
     /** notification icon. */
     private String icon;
     /** Escape HTML. */
     private boolean escapehtml;
     /** Strip codes. */
     private boolean stripcodes;
 
     @Inject
     public FDManager(
             @GlobalConfig final AggregateConfigProvider config,
             @UserConfig final ConfigProvider userConfig,
             @PluginDomain(FreeDesktopNotificationsPlugin.class) final String domain,
             final PluginFilesHelper filesHelper,
             @PluginDomain(FreeDesktopNotificationsPlugin.class) final PluginInfo pluginInfo) {
         this.domain = domain;
         this.config = config;
         this.userConfig = userConfig;
         this.filesHelper = filesHelper;
         this.pluginInfo = pluginInfo;
     }
 
     /**
      * Used to show a notification using this plugin.
      *
      * @param title   Title of dialog if applicable
      * @param message Message to show
      *
      * @return True if the notification was shown.
      */
     public boolean showNotification(final String title, final String message) {
         if (filesHelper.getFilesDir() == null) {
             return false;
         }
 
         final String[] args = {
             "/usr/bin/env",
             "python",
             filesHelper.getFilesDirString() + "notify.py",
             "-a",
             "DMDirc",
             "-i",
             icon,
             "-t",
             Integer.toString(timeout * 1000),
             "-s",
             Strings.isNullOrEmpty(title) ? "Notification from DMDirc" : prepareString(title),
             prepareString(message)
         };
 
         try {
             final Process myProcess = Runtime.getRuntime().exec(args);
             StreamUtils.readStream(myProcess.getErrorStream());
             StreamUtils.readStream(myProcess.getInputStream());
             try {
                 myProcess.waitFor();
             } catch (InterruptedException e) {
                //Not a proble, carry on
             }
             return true;
         } catch (SecurityException | IOException e) {
             LOG.info(USER_ERROR, "Unable to show notification", e);
         }
 
         return false;
     }
 
     /**
      * Prepare the string for sending to dbus.
      *
      * @param input Input string
      *
      * @return Input string after being processed according to config settings.
      */
     public String prepareString(final String input) {
         String output = input;
         if (stripcodes) {
             output = Styliser.stipControlCodes(output);
         }
         if (escapehtml) {
             output = HtmlEscapers.htmlEscaper().escape(output);
         }
         return output;
     }
 
     private void setCachedSettings() {
         timeout = config.getOptionInt(domain, "general.timeout");
         icon = config.getOption(domain, "general.icon");
         escapehtml = config.getOptionBool(domain, "advanced.escapehtml");
         stripcodes = config.getOptionBool(domain, "advanced.stripcodes");
     }
 
     @Override
     public void configChanged(final String domain, final String key) {
         setCachedSettings();
     }
 
     public void onLoad() {
         config.addChangeListener(domain, this);
         userConfig.setOption(domain, "general.icon", filesHelper.getFilesDirString() + "icon.png");
         setCachedSettings();
         // Extract the files needed
         try {
             filesHelper.extractResourcesEndingWith(".py");
             filesHelper.extractResourcesEndingWith(".png");
         } catch (IOException ex) {
             LOG.warn(USER_ERROR, "Unable to extract files for Free desktop notifications: {}",
                     ex.getMessage(), ex);
         }
     }
 
     public void onUnLoad() {
         config.removeListener(this);
     }
 
 
 
     @Handler
     public void showConfig(final ClientPrefsOpenedEvent event) {
         final PreferencesDialogModel manager = event.getModel();
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
                 pluginInfo.getDomain(), "advanced.stripcodes", "Strip Control Codes",
                 "Strip IRC Control codes from messages?",
                 manager.getConfigManager(), manager.getIdentity()));
 
         manager.getCategory("Plugins").addSubCategory(general);
     }
 
 }
