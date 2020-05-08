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
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.ClientModule.UserConfig;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.interfaces.config.ConfigProvider;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.plugins.implementations.PluginFilesHelper;
 import com.dmdirc.ui.messages.Styliser;
 import com.dmdirc.util.io.StreamReader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.inject.Inject;
import javax.inject.Singleton;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
@Singleton
 public class FDManager implements ConfigChangeListener {
 
     /** Global configuration. */
     private final AggregateConfigProvider config;
     /** User configuration. */
     private final ConfigProvider userConfig;
     /** This plugin's settings domain. */
     private final String domain;
     /** Plugin files helper. */
     private final PluginFilesHelper filesHelper;
     /** notification timeout. */
     private int timeout;
     /** notification icon. */
     private String icon;
     /** Escape HTML. */
     private boolean escapehtml;
     /** Strict escape. */
     private boolean strictescape;
     /** Strip codes. */
     private boolean stripcodes;
 
     @Inject
     public FDManager(@GlobalConfig final AggregateConfigProvider config,
             @UserConfig ConfigProvider userConfig,
             @PluginDomain(FreeDesktopNotificationsPlugin.class) final String domain,
             final PluginFilesHelper filesHelper) {
         this.domain = domain;
         this.config = config;
         this.userConfig = userConfig;
         this.filesHelper = filesHelper;
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
 
         final ArrayList<String> args = new ArrayList<>();
 
         args.add("/usr/bin/env");
         args.add("python");
         args.add(filesHelper.getFilesDirString() + "notify.py");
         args.add("-a");
         args.add("DMDirc");
         args.add("-i");
         args.add(icon);
         args.add("-t");
         args.add(Integer.toString(timeout * 1000));
         args.add("-s");
 
         if (title != null && !title.isEmpty()) {
             args.add(prepareString(title));
         } else {
             args.add("Notification from DMDirc");
         }
         args.add(prepareString(message));
 
         try {
             final Process myProcess = Runtime.getRuntime().exec(args.toArray(new String[]{}));
             final StringBuffer data = new StringBuffer();
             new StreamReader(myProcess.getErrorStream()).start();
             new StreamReader(myProcess.getInputStream(), data).start();
             try {
                 myProcess.waitFor();
             } catch (InterruptedException e) {
             }
             return true;
         } catch (SecurityException | IOException e) {
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
             if (strictescape) {
                 output = StringEscapeUtils.escapeHtml(output);
             } else {
                 output = output.replace("&", "&amp;");
                 output = output.replace("<", "&lt;");
                 output = output.replace(">", "&gt;");
             }
         }
 
         return output;
     }
 
     private void setCachedSettings() {
         timeout = config.getOptionInt(domain, "general.timeout");
         icon = config.getOption(domain, "general.icon");
         escapehtml = config.getOptionBool(domain, "advanced.escapehtml");
         strictescape = config.getOptionBool(domain, "advanced.strictescape");
         stripcodes = config.getOptionBool(domain, "advanced.stripcodes");
     }
 
     /** {@inheritDoc} */
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
             filesHelper.extractResoucesEndingWith(".py");
             filesHelper.extractResoucesEndingWith(".png");
         } catch (IOException ex) {
             Logger.userError(ErrorLevel.MEDIUM,
                     "Unable to extract files for Free desktop notifications: " + ex.getMessage(), ex);
         }
     }
 
     public void onUnLoad() {
         config.removeListener(this);
     }
 
 }
