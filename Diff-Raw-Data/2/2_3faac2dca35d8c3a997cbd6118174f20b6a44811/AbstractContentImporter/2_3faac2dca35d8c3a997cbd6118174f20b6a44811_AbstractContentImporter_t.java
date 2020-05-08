 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.client.jme.content;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
 import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
 import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
 import org.jdesktop.wonderland.client.cell.utils.CellUtils;
 import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 
 /**
  * An abstract base class that content importers may use to help implement the
  * ContentImporterSPI. This class takes care of querying whether the asset
  * already exists and utility methods to create the cell based upon the asset.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @ExperimentalAPI
 public abstract class AbstractContentImporter implements ContentImporterSPI {
 
     private static Logger logger = Logger.getLogger(AbstractContentImporter.class.getName());
 
     /**
      * @inheritDoc()
      */
     public String importFile(File file, String extension) {
 
         final JFrame frame = JmeClientMain.getFrame().getFrame();
 
         // Next check whether the content already exists and ask the user if
         // the upload should still proceed. By default (result == 0), the
         // system will upload (and overwrite) and files.
         int result = JOptionPane.YES_OPTION;
         String uri = isContentExists(file);
         if (uri != null) {
             Object[] options = {"Replace", "Use Existing", "Cancel" };
             String msg = "The file " + file.getName() + " already exists in" +
                    " the content repository. Do you wish to replace it and " +
                     "continue?";
             String title = "Replace Content?";
             result = JOptionPane.showOptionDialog(frame, msg, title,
                     JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 
             logger.warning("RESULT = " + result);
 
             // If the user hits Cancel or a "closed" action (e.g. Escape key)
             // then just return
             if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                 return null;
             }
         }
 
         // If the content exists and we do not want to upload a new version,
         // then simply create it and return.
         if (result == JOptionPane.NO_OPTION) {
             createCell(uri);
             return uri;
         }
 
         // Display a dialog showing a wait message while we import. We need
         // to do this in the SwingWorker thread so it gets drawn
         JOptionPane waitMsg = new JOptionPane("Please wait while " +
                 file.getName() + " is being uploaded");
         final JDialog dialog = waitMsg.createDialog(frame, "Uploading Content");
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 dialog.setVisible(true);
             }
         });
 
         logger.warning("Uploading content");
         
         // Next, do the actual upload of the file. This should display a
         // progress dialog if the upload is going to take a long time.
         try {
             uri = uploadContent(file);
         } catch (java.io.IOException excp) {
             logger.log(Level.WARNING, "Failed to upload content file " +
                     file.getAbsolutePath(), excp);
 
             final String fileName = file.getName();
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     dialog.setVisible(false);
                     JOptionPane.showMessageDialog(frame,
                             "Failed to upload content file " + fileName,
                             "Upload Failed", JOptionPane.ERROR_MESSAGE);
                 }
             });
             return null;
         } finally {
             // Close down the dialog indicating success
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     dialog.setVisible(false);
                 }
             });
         }
         
         // Finally, go ahead and create the cell.
         createCell(uri);
         return uri;
     }
 
     /**
      * If the content already exists in the user's content repository, return
      * it's URI. If the content does not exist, return null.
      *
      * @param file The File of the imported content
      * @return The URL of the existing content, null if it does not exist
      */
     public abstract String isContentExists(File file);
 
     /**
      * Uploads the content file. Throws IOException upon error. Returns a URI
      * that describes the location of the content
      *
      * @param file The content file to upload
      * @return A URI that represents the uploaded content
      * @throw IOException Upon upload error
      */
     public abstract String uploadContent(File file) throws IOException;
 
     /**
      * Create a cell based upon the uri of the content and the file extension
      * of the uploaded file.
      *
      * @param uri The URI of the uploaded content
      * @param extension The file extension of the content
      */
     public void createCell(String uri) {
         // Figure out what the file extension is from the uri, looking for
         // the final '.'.
         String extension = getFileExtension(uri);
         if (extension == null) {
             logger.warning("Could not find extension for " + uri);
             return;
         }
         
         // Next look for a cell type that handles content with this file
         // extension and create a new cell with it.
         CellRegistry registry = CellRegistry.getCellRegistry();
         Set<CellFactorySPI> factories = registry.getCellFactoriesByExtension(extension);
         if (factories == null) {
             final JFrame frame = JmeClientMain.getFrame().getFrame();
             logger.warning("Could not find cell factory for " + extension);
             JOptionPane.showMessageDialog(frame,
                     "Unable to launch Cell that supports " + uri,
                     "Launch Failed", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         CellFactorySPI factory = factories.iterator().next();
 
         // Get the cell server state, injecting the content URI into it via
         // the properties
         Properties props = new Properties();
         props.put("content-uri", uri);
         CellServerState state = factory.getDefaultCellServerState(props);
 
         // Create the new cell at a distance away from the avatar
         try {
             CellUtils.createCell(state);
         } catch (CellCreationException excp) {
             logger.log(Level.WARNING, "Unable to create cell for uri " + uri, excp);
         }
     }
 
     /**
      * Utility routine to fetch the file extension from the URI, or null if
      * none can be found.
      */
     private String getFileExtension(String uri) {
         // Figure out what the file extension is from the uri, looking for
         // the final '.'.
         int index = uri.lastIndexOf(".");
         if (index == -1) {
             return null;
         }
         return uri.substring(index + 1);
     }
 }
