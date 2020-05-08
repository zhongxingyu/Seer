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
 import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.util.ConfigFile;
 import com.dmdirc.util.InvalidConfigFileException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import javax.swing.JScrollPane;
 
 /**
  * Loads the addon data feed into the addon browser.
  */
 public class DataLoaderWorker 
         extends LoggingSwingWorker<Collection<Map<String, String>>, Object> {
 
     /** List to load data into. */
     private final AddonTable table;
     /** Browser window to pass to addon info objects. */
     private final BrowserWindow browserWindow;
     /** Table's parent scrollpane. */
     private final JScrollPane scrollPane;
 
     /**
      * Creates a new data loader worker.
      *
      * @param table Table to load data into
      * @param browserWindow Browser window to pass to table objects
      * @param scrollPane Table's parent scrollpane
      */
     public DataLoaderWorker(final AddonTable table,
             final BrowserWindow browserWindow, final JScrollPane scrollPane) {
         super();
         
         this.table = table;
         this.browserWindow = browserWindow;
         this.scrollPane = scrollPane;
     }
 
     /** {@inheritDoc} */
     @Override
     protected Collection<Map<String, String>> doInBackground() {
         final ConfigFile data = new ConfigFile(Main.getConfigDir()
                 + File.separator + "addons.feed");
         try {
             data.read();
         } catch (IOException ex) {
            return Collections.<Map<String, String>>emptyList();
         } catch (InvalidConfigFileException ex) {
            return Collections.<Map<String, String>>emptyList();
         }
         return data.getKeyDomains().values();
     }
 
     /** {@inheritDoc} */
     @Override
     protected void done() {
         if (isCancelled()) {
             return;
         }
         Collection<Map<String, String>> data;
         try {
             data = get();
         } catch (InterruptedException ex) {
            data = Collections.<Map<String, String>>emptyList();
         } catch (ExecutionException ex) {
             Logger.appError(ErrorLevel.MEDIUM, ex.getMessage(), ex);
            data = Collections.<Map<String, String>>emptyList();
         }
         final int selectedRow;
         if (table.getRowCount() > 0 && table.getSelectedRow() > 0) {
             selectedRow = table.getSelectedRow();
         } else {
             selectedRow = 0;
         }
         table.getModel().setRowCount(0);
         for (Map<String, String> entry : data) {
             final AddonInfo info = new AddonInfo(entry);
             table.getModel().addRow(new Object[]{
                 new AddonInfoLabel(info, browserWindow),
             });
         }
         table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
         scrollPane.setViewportView(table);
     }
 }
