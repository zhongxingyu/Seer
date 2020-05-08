 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2012  Trejkaz, Hex Project
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
 
 package org.trypticon.hex.gui.prefs;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.prefs.Preferences;
 import javax.swing.SwingUtilities;
 
 import org.trypticon.hex.gui.HexApplication;
 import org.trypticon.hex.gui.HexFrame;
 import org.trypticon.hex.gui.notebook.Notebook;
 import org.trypticon.hex.gui.notebook.NotebookStorage;
 import org.trypticon.hex.util.LoggerUtils;
 
 /**
  * Workspace state tracker used for Mac platforms.
  *
  * @author trejkaz
  */
 public class AquaWorkspaceStateTracker extends WorkspaceStateTracker {
 
     @Override
     public void save() {
         Preferences topPrefs = getPrefs();
         int count = 0;
 
         for (HexFrame frame : HexFrame.findAllFrames()) {
             URL notebookLocation = frame.getNotebook().getNotebookLocation();
             if (notebookLocation == null) {
                 // User must have explicitly chosen *not* to save the notebook, so toss it.
                 //TODO: Modern Mac apps even restore unsaved documents. That would be nice to add.
                 continue;
             }
 
             Preferences openDocumentPrefs = topPrefs.node("document" + count);
             openDocumentPrefs.put("location", notebookLocation.toExternalForm());
             saveFrameLocation(frame, openDocumentPrefs);
             count++;
         }
 
         topPrefs.putInt("count", count);
     }
 
     @Override
     public boolean restore() {
         Preferences topPrefs = getPrefs();
         int count = topPrefs.getInt("count", -1);
         if (count < 0) {
             return false;
         }
 
         HexApplication application = HexApplication.get();
 
         for (int i = 0; i < count; i++) {
            final Preferences openDocumentPrefs = topPrefs.node("document" + i);
 
             String location = openDocumentPrefs.get("location", null);
             if (location == null) {
                 LoggerUtils.get().warning("Location for open document " + i + " missing, skipping");
                 continue;
             }
 
             final HexFrame frame;
             try {
                 URL url = new URL(location);
                 Notebook notebook = new NotebookStorage().read(url);
                 frame = application.openNotebook(notebook);
             } catch (MalformedURLException e) {
                 LoggerUtils.get().log(Level.WARNING, "Malformed URL found in preferences for document " + i + ": " +
                                                      location + ", skipping", e);
                 continue;
             } catch (IOException e) {
                 LoggerUtils.get().log(Level.WARNING, "Error opening previously-open notebook: " + location + ", skipping");
                 continue;
             }
 
             restoreFrameLocation(frame, openDocumentPrefs);
         }
 
         return true;
     }
 }
