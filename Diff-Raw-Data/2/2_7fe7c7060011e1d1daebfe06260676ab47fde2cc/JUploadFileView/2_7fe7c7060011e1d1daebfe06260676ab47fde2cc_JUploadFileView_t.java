 //
 // $Id$
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2007-04-06
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.gui;
 
 import java.io.File;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.Icon;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileView;
 
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * The IconWorker class loads a icon from a file. It's called from a backup
  * thread created by the JUploadFileView class. This allows to load/calculate
  * icons in background. This prevent the applet to be freezed while icons are
  * loading.
  */
 class IconWorker implements Runnable {
 
     /** The current upload policy */
     UploadPolicy uploadPolicy = null;
 
     /** The current file chooser. */
     JFileChooser fileChooser = null;
 
     /** The current file view */
     JUploadFileView fileView = null;
 
     /** The file whose icon must be loaded. */
     File file = null;
 
     /** The icon for this file. */
     Icon icon = null;
 
     /**
     * The constructor only stores the file. The background thread will call the
      * loadIcon method.
      * 
      * @param file The file whose icon must be loaded/calculated.
      */
     IconWorker(UploadPolicy uploadPolicy, JFileChooser fileChooser,
             JUploadFileView fileView, File file) {
         this.uploadPolicy = uploadPolicy;
         this.fileChooser = fileChooser;
         this.fileView = fileView;
         this.file = file;
     }
 
     /**
      * Returns the currently loaded icon for this file.
      * 
      * @return The Icon to be displayed for this file.
      */
     Icon getIcon() {
         return this.icon;
     }
 
     /** Get the icon from the current upload policy, for this file */
     void loadIcon() {
         File dir = null;
         File parent = null;
         try {
             // Maybe it has already been loaded.
             if (this.icon == null && !this.file.isDirectory()) {
                 // Maybe the current directory changed. In this case, we
                 // postpone the loading of this icon.
                 dir = this.fileChooser.getCurrentDirectory();
                 parent = this.file.getParentFile();
                 // If dir and parent are null, they are equals, we calculate the
                 // icon
                 if (parent == null && dir == null) {
                     this.icon = this.uploadPolicy.fileViewGetIcon(this.file);
                     this.fileChooser.repaint();
                 } else if (parent != null) {
                     if (dir.getAbsolutePath().equals(parent.getAbsolutePath())
                             || dir.isDirectory()) {
                         // If it's a directory, we instantly calculate the icon.
                         // The icon has not yet be loaded, and the user is still
                         // in this directory. Let's load the icon.
                         this.icon = this.uploadPolicy
                                 .fileViewGetIcon(this.file);
                         this.fileChooser.repaint();
                     } else {
                         // We don't do it now, but we'll do it later.
                         this.fileView.executorService.execute(this);
                     }
                 }
                 // Otherwise, one of 'parent' or 'dir' is null, we let the icon
                 // to null.
             }
         } catch (NullPointerException e) {
             // No action, we mask the error
             this.uploadPolicy.displayWarn(e.getClass().getName()
                     + " in IconWorker.loadIcon. dir: " + dir + ", parent: "
                     + parent);
         }
     }
 
     /** Implementation of the Runnable interface */
     public void run() {
         loadIcon();
     }
 }
 
 /**
  * This class provides the icon view for the file selector.
  * 
  * @author Etienne Gauthier
  */
 public class JUploadFileView extends FileView {
 
     /** The current upload policy. */
     UploadPolicy uploadPolicy = null;
 
     /** The current file chooser. */
     JFileChooser fileChooser = null;
 
     /** This map will contain all instances of {@link IconWorker}. */
     ConcurrentHashMap<String, IconWorker> hashMap = new ConcurrentHashMap<String, IconWorker>();
 
     ExecutorService executorService = Executors.newSingleThreadExecutor();
 
     /**
      * Creates a new instance.
      * 
      * @param uploadPolicy The upload policy to apply.
      * @param fileChooser The desired file chooser to use.
      */
     public JUploadFileView(UploadPolicy uploadPolicy, JFileChooser fileChooser) {
         this.uploadPolicy = uploadPolicy;
         this.fileChooser = fileChooser;
     }
 
     /**
      * @see javax.swing.filechooser.FileView#getIcon(java.io.File)
      */
     @Override
     public Icon getIcon(File file) {
         IconWorker iconWorker = this.hashMap.get(file.getAbsolutePath());
         if (iconWorker == null) {
             // This file has not been loaded.
             iconWorker = new IconWorker(this.uploadPolicy, this.fileChooser,
                     this, file);
             // We store it in the global Icon container.
             this.hashMap.put(file.getAbsolutePath(), iconWorker);
             // Then, we ask the current Thread to load its icon.
             this.executorService.execute(iconWorker);
         }
         return iconWorker.getIcon();
     }
 
     /**
      * Stop all current and to come thread. To be called when the file chooser
      * is closed.
      */
     public void shutdownNow() {
         this.executorService.shutdownNow();
     }
 }
