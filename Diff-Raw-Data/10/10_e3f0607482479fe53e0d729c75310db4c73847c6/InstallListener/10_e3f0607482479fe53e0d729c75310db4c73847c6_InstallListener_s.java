 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.ui_swing.components.addonbrowser;
 
 import com.dmdirc.Main;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.plugins.PluginManager;
 import com.dmdirc.ui.themes.ThemeManager;
 import com.dmdirc.util.Downloader;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.io.File;
 import java.io.IOException;
 import javax.swing.SwingUtilities;
 
 /**
  * Addon info install listener.
  */
 public class InstallListener implements ActionListener {
 
     /** Addon info. */
     private final AddonInfo info;
     /** Parent window. */
     private final BrowserWindow parentWindow;
 
     /**
      * Instantiates a new install listener.
      *
      * @param info Addoninfo to install
      * @param parentWindow Parent window
      */
     public InstallListener(final AddonInfo info,
             final BrowserWindow parentWindow) {
         this.info = info;
         this.parentWindow = parentWindow;
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         final InstallerWindow installer = new InstallerWindow(parentWindow,
                 info);
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 installer.display(parentWindow);
             }
         });
         try {
             final File file = new File(Main.getConfigDir(),
                     "." + info.getDownload());
             Downloader.downloadPage("http://addons.dmdirc.com/addondownload/"
                             + info.getDownload(), file.getAbsolutePath());
 
             switch (info.getType()) {
                 case TYPE_ACTION_PACK:
                     ActionManager.installActionPack(file.getAbsolutePath());
                     break;
                 case TYPE_PLUGIN:
                     final File newFile = new File(PluginManager
                             .getPluginManager().getDirectory(),
                             info.getTitle() + ".jar");
                     if (file.renameTo(newFile)) {
                         PluginManager.getPluginManager().addPlugin(
                                 newFile.getName());
                     } else {
                         Logger.userError(ErrorLevel.MEDIUM, "Unable to "
                                 + "install addon, failed to move file: "
                                 + file.getAbsolutePath());
                         installer.finished("Unable to "
                                 + "install addon, failed to move file: "
                                 + file.getAbsolutePath());
                     }
                     break;
                 case TYPE_THEME:
                    file.renameTo(new File(ThemeManager.getThemeDirectory()));
                     break;
                 default:
                     Logger.appError(ErrorLevel.HIGH, "Unknown addon type",
                             new IllegalArgumentException("Unknown addon type"));
             }
         } catch (IOException ex) {
             Logger.userError(ErrorLevel.MEDIUM, "Unable to download addon: "
                     + ex.getMessage(), ex);
             installer.finished("Unable to download addon: " + ex.getMessage());
         }
         installer.finished("");
     }
 }
