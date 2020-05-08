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
 
 package com.dmdirc.addons.ui_swing.components.addonbrowser;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.updater.manager.UpdateManager;
 import com.dmdirc.util.URLBuilder;
 import com.dmdirc.util.io.ConfigFile;
 import com.dmdirc.util.io.DownloadListener;
 import com.dmdirc.util.io.Downloader;
 import com.dmdirc.util.io.InvalidConfigFileException;
 
 import java.io.IOException;
 import java.nio.file.Paths;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.concurrent.ExecutionException;
 import java.util.stream.Collectors;
 
 import javax.swing.Box;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.text.StyleConstants;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Loads the addon data feed into the addon browser.
  */
 public class DataLoaderWorker
         extends LoggingSwingWorker<Collection<AddonInfo>, Object>
         implements DownloadListener {
 
     /** List to load data into. */
     private final AddonTable table;
     /** Browser window to pass to addon info objects. */
     private final BrowserWindow browserWindow;
     /** Table's parent scrollpane. */
     private final JScrollPane scrollPane;
     /** Downloader progress bar. */
     private final JProgressBar jpb = new JProgressBar(0, 100);
     /** Refresh addons feed? */
     private final boolean download;
     /** Directory to store temporary files in. */
     private final String tempDirectory;
     /** URL Builder to use when loading addon resources. */
     private final URLBuilder urlBuilder;
     /** Factory to use to produce install workers. */
     private final InstallWorkerFactory workerFactory;
     /** The manager to use to retrieve update information. */
     private final UpdateManager updateManager;
     /** Configuration to read settings from. */
     private final AggregateConfigProvider globalConfig;
     /** Downloader to download files. */
     private final Downloader downloader;
     /** The event bus to post errors to. */
     private final DMDircMBassador eventBus;
 
     /**
      * Creates a new data loader worker.
      *
      * @param downloader    Used to download files
      * @param globalConfig  Configuration to read settings from.
      * @param urlBuilder    URL Builder to use when loading addon resources.
      * @param workerFactory Factory to use to produce install workers.
      * @param updateManager Manager to use to retrieve update information.
      * @param tempDirectory The directory to store temporary items in, such as the addons feed.
      * @param eventBus      The event bus to post errors to
      * @param table         Table to load data into
      * @param download      Download new addons feed?
      * @param browserWindow Browser window to pass to table objects
      * @param scrollPane    Table's parent scrollpane
      */
     public DataLoaderWorker(
             final Downloader downloader,
             final AggregateConfigProvider globalConfig,
             final URLBuilder urlBuilder,
             final InstallWorkerFactory workerFactory,
             final UpdateManager updateManager,
             final String tempDirectory,
             final DMDircMBassador eventBus,
             final AddonTable table,
             final boolean download,
             final BrowserWindow browserWindow,
             final JScrollPane scrollPane) {
         super(eventBus);
         this.downloader = downloader;
         this.globalConfig = globalConfig;
         this.urlBuilder = urlBuilder;
         this.workerFactory = workerFactory;
         this.download = download;
         this.table = table;
         this.updateManager = updateManager;
         this.tempDirectory = tempDirectory;
         this.browserWindow = browserWindow;
         this.scrollPane = scrollPane;
         this.eventBus = eventBus;
     }
 
     @Override
     protected Collection<AddonInfo> doInBackground() {
         final JPanel loadingPanel = new JPanel(
                 new MigLayout("fill, alignx 50%, aligny 50%"));
         scrollPane.setViewportView(loadingPanel);
         if (download) {
             final TextLabel label = new TextLabel(
                     "Downloading addon info, please wait...");
             label.setAlignment(StyleConstants.ALIGN_CENTER);
             loadingPanel.add(Box.createVerticalGlue(), "growy, pushy, wrap");
             loadingPanel.add(label, "growx, wrap");
             loadingPanel.add(jpb, "growx, wrap");
             loadingPanel.add(Box.createVerticalGlue(), "growy, pushy");
             try {
                downloader.downloadPage("https://addons.dmdirc.com/feed",
                         Paths.get(tempDirectory, "addons.feed"), this);
             } catch (final IOException ex) {
                 loadingPanel.removeAll();
                 loadingPanel.add(new TextLabel("Unable to download feeds: " + ex.getMessage()));
                 return Collections.emptyList();
             }
         }
 
         loadingPanel.removeAll();
         loadingPanel.add(new TextLabel("Loading addon info, please wait."));
         final ConfigFile data = new ConfigFile(tempDirectory + "addons.feed");
         try {
             data.read();
         } catch (final IOException | InvalidConfigFileException ex) {
             return Collections.emptyList();
         }
 
         return data.getKeyDomains().values().stream()
                 .map(entry -> new AddonInfo(globalConfig, updateManager, urlBuilder, entry))
                 .collect(Collectors.toList());
     }
 
     @Override
     protected void done() {
         if (isCancelled()) {
             return;
         }
 
         Collection<AddonInfo> data;
         try {
             data = get();
         } catch (final InterruptedException ex) {
             data = Collections.emptyList();
         } catch (final ExecutionException ex) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex, ex.getMessage(), ""));
             data = Collections.emptyList();
         }
         final int selectedRow;
         if (table.getRowCount() > 0 && table.getSelectedRow() > 0) {
             selectedRow = table.getSelectedRow();
         } else {
             selectedRow = 0;
         }
         table.getModel().setRowCount(0);
         for (final AddonInfo info : data) {
             table.getModel().addRow(new Object[]{
                 new AddonInfoLabel(info, browserWindow, workerFactory),});
         }
         table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
         scrollPane.setViewportView(table);
     }
 
     @Override
     public void downloadProgress(final float percent) {
         UIUtilities.invokeLater(() -> jpb.setValue((int) percent));
     }
 
     @Override
     public void setIndeterminate(final boolean indeterminate) {
         UIUtilities.invokeLater(() -> jpb.setIndeterminate(indeterminate));
     }
 
 }
