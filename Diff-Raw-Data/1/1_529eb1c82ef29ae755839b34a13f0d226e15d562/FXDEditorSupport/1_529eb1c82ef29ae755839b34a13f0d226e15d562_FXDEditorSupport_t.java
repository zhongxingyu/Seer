 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package org.netbeans.modules.javafx.fxd.dataloader.fxd;
 
 import java.io.File;
 import java.io.IOException;
 import javax.swing.JEditorPane;
 import javax.swing.SwingUtilities;
 import org.netbeans.modules.editor.structure.api.DocumentElement;
 import org.netbeans.modules.javafx.fxd.composer.navigator.SelectionCookie;
 import org.netbeans.modules.javafx.fxd.composer.source.SourceEditorWrapper;
 import org.netbeans.modules.javafx.fxd.composer.source.SourceTopComponent;
 import org.netbeans.modules.javafx.fxd.dataloader.FXDZDataObject;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataObject;
 import org.openide.cookies.CloseCookie;
 import org.openide.cookies.EditCookie;
 import org.openide.cookies.EditorCookie;
 import org.openide.cookies.OpenCookie;
 import org.openide.cookies.PrintCookie;
 import org.openide.cookies.SaveCookie;
 import org.openide.filesystems.FileLock;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.filesystems.JarFileSystem;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.MultiDataObject;
 import org.openide.nodes.CookieSet;
 import org.openide.text.DataEditorSupport;
 import org.openide.windows.CloneableOpenSupport;
 import org.openide.windows.TopComponent;
 
 /**
  *
  * @author Pavel Benes
  */
 public class FXDEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie, SelectionCookie  {
     /** SaveCookie for this support instance. The cookie is adding/removing 
      * data object's cookie set depending on if modification flag was set/unset. */
     private final SaveCookie saveCookie = new SaveCookie() {
         /** Implements <code>SaveCookie</code> interface. */
         public void save() throws IOException {
             FXDEditorSupport.this.saveDocument();
         }
     };
     
     private CookieSet set;
     
     /** Constructor. 
      * @param obj data object to work on
      * @param set set to add/remove save cookie from
      */
     FXDEditorSupport (DataObject obj, MultiDataObject.Entry entry, CookieSet set) {
         super(obj, new Environment(obj, entry));
         this.set = set;
     }
     
     @Override
     public void open() {
         FileObject fo = getDataObject().getPrimaryFile();
         File file = FileUtil.toFile(fo);
         if ( file != null) {
             super.open();
         } else {
             try {
                 JarFileSystem fs = (JarFileSystem) fo.getFileSystem();
                 FileObject fxzFO = FileUtil.toFileObject( fs.getJarFile());
                 FXZDataObject dObj = (FXZDataObject) DataObject.find(fxzFO);
                 dObj.setDefaultView( FXZDataObject.TEXT_VIEW_INDEX);
                dObj.selectEntry(fo.getNameExt());
                 SourceTopComponent.selectPosition( dObj, -1, true);
             } catch( Exception e) {
                 //TODO report
                 e.printStackTrace();
             }
         }
         SwingUtilities.invokeLater( new Runnable() {
             public void run() {
                 EditorCookie ec = getDataObject().getCookie(EditorCookie.class);
                 if ( ec != null) {
                     JEditorPane [] panes = ec.getOpenedPanes();
                     if ( panes != null && panes.length > 0 && panes[0] != null) {
                         SourceEditorWrapper.addErrorStatusBarCell(panes[0]);
                     }
                 }
             }            
         });        
     }
     
     @Override
     public void edit() {
         super.edit();
     }
     
     TopComponent getTopComponent() {
         return allEditors.getArbitraryComponent();        
     }
     
     /** 
      * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
      * @return true if the environment accepted being marked as modified
      *    or false if it has refused and the document should remain unmodified
      */
     @Override
     protected boolean notifyModified () {
         if (!super.notifyModified()) 
             return false;
 
         addSaveCookie();
 
         return true;
     }
 
     /** Overrides superclass method. Adds removing of save cookie. */
     @Override
     protected void notifyUnmodified () {
         super.notifyUnmodified();
 
         removeSaveCookie();
     }
 
     /** Helper method. Adds save cookie to the data object. */
     private void addSaveCookie() {
         DataObject obj = getDataObject();
 
         // Adds save cookie to the data object.
         if(obj.getCookie(SaveCookie.class) == null) {
             set.add(saveCookie);
             obj.setModified(true);
         }
     }
 
     /** Helper method. Removes save cookie from the data object. */
     private void removeSaveCookie() {
         DataObject obj = getDataObject();
         
         // Remove save cookie from the data object.
         SaveCookie cookie = obj.getCookie(SaveCookie.class);
 
         if(cookie != null && cookie.equals(saveCookie)) {
             set.remove(saveCookie);
             obj.setModified(false);
         }
     }
 
     public void updateSelection(FXDZDataObject doj, DocumentElement de, boolean doubleClick) {
         if ( de != null) {
             SourceTopComponent.selectElement(doj, de.getStartOffset(), doubleClick);
         }
     }
     
     /** Nested class. Environment for this support. Extends
      * <code>DataEditorSupport.Env</code> abstract class.
      */
     
     private static class Environment extends DataEditorSupport.Env {
         private static final long serialVersionUID = 2L;
         
         private MultiDataObject.Entry entry;
         
         /** Constructor. */
         public Environment(DataObject obj, MultiDataObject.Entry entry) {
             super(obj);
             this.entry = entry;
         }
 
         
         /** Implements abstract superclass method. */
         protected FileObject getFile() {
             return entry.getFile();
         }
 
         /** Implements abstract superclass method.*/
         protected FileLock takeLock() throws IOException {
             return entry.takeLock();
         }
 
         /** 
          * Overrides superclass method.
          * @return text editor support (instance of enclosing class)
          */
         @Override
         public CloneableOpenSupport findCloneableOpenSupport() {
             return getDataObject().getCookie(FXDEditorSupport.class);
         }
     } // End of nested Environment class.
 }
 
