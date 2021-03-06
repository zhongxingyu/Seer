 //License: GPL. Copyright 2007 by Immanuel Scholz and others
 package org.openstreetmap.josm.actions;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.osm.OsmPrimitive;
 import org.openstreetmap.josm.gui.ExtendedDialog;
 import org.openstreetmap.josm.gui.layer.GpxLayer;
 import org.openstreetmap.josm.gui.layer.Layer;
 import org.openstreetmap.josm.gui.layer.OsmDataLayer;
 import org.openstreetmap.josm.io.GpxWriter;
 import org.openstreetmap.josm.io.OsmWriter;
 import org.openstreetmap.josm.tools.Shortcut;
 
 public abstract class SaveActionBase extends DiskAccessAction {
 
     private Layer layer;
 
     public SaveActionBase(String name, String iconName, String tooltip, Shortcut shortcut, Layer layer) {
         super(name, iconName, tooltip, shortcut);
         this.layer = layer;
     }
 
     public void actionPerformed(ActionEvent e) {
         doSave();
     }
 
     public Boolean doSave() {
         Layer layer = this.layer;
         if (layer == null && Main.map != null && (Main.map.mapView.getActiveLayer() instanceof OsmDataLayer
                 || Main.map.mapView.getActiveLayer() instanceof GpxLayer))
             layer = Main.map.mapView.getActiveLayer();
         if (layer == null)
             layer = Main.main.editLayer();
 
         if (!checkSaveConditions(layer))
             return false;
 
 
         File file = getFile(layer);
         if (file == null)
             return false;
 
         save(file, layer);
 
         layer.name = file.getName();
         layer.associatedFile = file;
         Main.parent.repaint();
         return true;
     }
 
     protected abstract File getFile(Layer layer);
 
     /**
      * Checks whether it is ok to launch a save (whether we have data,
      * there is no conflict etc.)
      * @return <code>true</code>, if it is safe to save.
      */
     public boolean checkSaveConditions(Layer layer) {
         if (layer == null) {
             JOptionPane.showMessageDialog(Main.parent, tr("Internal Error: cannot check conditions for no layer. Please report this as a bug."));
             return false;
         }
         if (Main.map == null) {
             JOptionPane.showMessageDialog(Main.parent, tr("No document open so nothing to save."));
             return false;
         }
 
         if (layer instanceof OsmDataLayer && isDataSetEmpty((OsmDataLayer)layer) && 1 != new ExtendedDialog(Main.parent, tr("Empty document"), tr("The document contains no data."), new String[] {tr("Save anyway"), tr("Cancel")}, new String[] {"save.png", "cancel.png"}).getValue()) {
             return false;
         }
         if (layer instanceof GpxLayer && ((GpxLayer)layer).data == null) {
             return false;
         }
         if (!Main.map.conflictDialog.conflicts.isEmpty()) {
             int answer = new ExtendedDialog(Main.parent,
                 tr("Conflicts"),
                 tr("There are unresolved conflicts. Conflicts will not be saved and handled as if you rejected all. Continue?"),
                 new String[] {tr("Reject Conflicts and Save"), tr("Cancel")},
                 new String[] {"save.png", "cancel.png"}).getValue();
 
             if (answer != 1) return false;
         }
         return true;
     }
 
     public static File openFileDialog(Layer layer) {
         JFileChooser fc = createAndOpenFileChooser(false, false, layer instanceof GpxLayer ? tr("Save GPX file") : tr("Save OSM file"));
         if (fc == null)
             return null;
 
         File file = fc.getSelectedFile();
 
         String fn = file.getPath();
         if (fn.indexOf('.') == -1) {
             FileFilter ff = fc.getFileFilter();
             if (ff instanceof ExtensionFileFilter)
                 fn += "." + ((ExtensionFileFilter)ff).defaultExtension;
             else if (layer instanceof GpxLayer)
                 fn += ".gpx";
             else
                 fn += ".osm";
             file = new File(fn);
         }
         return file;
     }
 
     private static void copy(File src, File dst) throws IOException {
         FileInputStream srcStream;
         FileOutputStream dstStream;
         try {
             srcStream = new FileInputStream(src);
             dstStream = new FileOutputStream(dst);
         } catch (FileNotFoundException e) {
             JOptionPane.showMessageDialog(Main.parent, tr("Could not back up file.")+"\n"+e.getMessage());
             return;
         }
         byte buf[] = new byte[1<<16];
         int len;
         while ((len = srcStream.read(buf)) != -1) {
             dstStream.write(buf, 0, len);
         }
         srcStream.close();
         dstStream.close();
     }
 
     public static void save(File file, Layer layer) {
         if (layer instanceof GpxLayer) {
             save(file, (GpxLayer)layer);
             ((GpxLayer)layer).data.storageFile = file;
         } else if (layer instanceof OsmDataLayer) {
             save(file, (OsmDataLayer)layer);
         }
     }
 
     public static void save(File file, OsmDataLayer layer) {
         File tmpFile = null;
         try {
             if (ExtensionFileFilter.filters[ExtensionFileFilter.GPX].acceptName(file.getPath())) {
                 GpxExportAction.exportGpx(file, layer);
             } else if (ExtensionFileFilter.filters[ExtensionFileFilter.OSM].acceptName(file.getPath())) {
                 // use a tmp file because if something errors out in the
                 // process of writing the file, we might just end up with
                 // a truncated file.  That can destroy lots of work.
                 if (file.exists()) {
                     tmpFile = new File(file.getPath() + "~");
                     copy(file, tmpFile);
                 }
                OsmWriter w = new OsmWriter(new PrintWriter(file, "UTF-8"), false, layer.data.version);
                 w.header();
                 w.writeDataSources(layer.data);
                 w.writeContent(layer.data);
                 w.footer();
                 w.close();
                 // FIXME - how to close?
                 if (!Main.pref.getBoolean("save.keepbackup") && (tmpFile != null))
                     tmpFile.delete();
             } else {
                 JOptionPane.showMessageDialog(Main.parent, tr("Unknown file extension."));
                 return;
             }
             layer.cleanData(null, false);
         } catch (IOException e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(Main.parent, tr("An error occurred while saving.")+"\n"+e.getMessage());
 
             try {
                 // if the file save failed, then the tempfile will not
                 // be deleted.  So, restore the backup if we made one.
                 if (tmpFile != null && tmpFile.exists()) {
                     copy(tmpFile, file);
                 }
             } catch (IOException e2) {
                 e2.printStackTrace();
                 JOptionPane.showMessageDialog(Main.parent, tr("An error occurred while restoring backup file.")+"\n"+e2.getMessage());
             }
         }
     }
 
     public static void save(File file, GpxLayer layer) {
         File tmpFile = null;
         try {
             if (ExtensionFileFilter.filters[ExtensionFileFilter.GPX].acceptName(file.getPath())) {
 
                 // use a tmp file because if something errors out in the
                 // process of writing the file, we might just end up with
                 // a truncated file.  That can destroy lots of work.
                 if (file.exists()) {
                     tmpFile = new File(file.getPath() + "~");
                     copy(file, tmpFile);
                 }
                 FileOutputStream fo = new FileOutputStream(file);
                 new GpxWriter(fo).write(layer.data);
                 fo.flush();
                 fo.close();
 
                 if (!Main.pref.getBoolean("save.keepbackup") && (tmpFile != null)) {
                     tmpFile.delete();
                 }
             } else {
                 JOptionPane.showMessageDialog(Main.parent, tr("Unknown file extension."));
                 return;
             }
         } catch (IOException e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(Main.parent, tr("An error occurred while saving.")+"\n"+e.getMessage());
         }
         try {
             // if the file save failed, then the tempfile will not
             // be deleted.  So, restore the backup if we made one.
             if (tmpFile != null && tmpFile.exists()) {
                 copy(tmpFile, file);
             }
         } catch (IOException e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(Main.parent, tr("An error occurred while restoring backup file.")+"\n"+e.getMessage());
         }
     }
 
     /**
      * Check the data set if it would be empty on save. It is empty, if it contains
      * no objects (after all objects that are created and deleted without being
      * transfered to the server have been removed).
      *
      * @return <code>true</code>, if a save result in an empty data set.
      */
     private boolean isDataSetEmpty(OsmDataLayer layer) {
         for (OsmPrimitive osm : layer.data.allNonDeletedPrimitives())
             if (!osm.deleted || osm.id > 0)
                 return false;
         return true;
     }
 }
