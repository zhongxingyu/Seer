 package org.netbeans.modules.javafx.editor.preview;
 
 import java.awt.Graphics;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 import org.netbeans.modules.javafx.project.JavaFXProject;
 import org.netbeans.spi.project.support.ant.PropertyEvaluator;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 import org.openide.modules.InstalledFileLocator;
 import org.openide.nodes.Node;
 import org.openide.util.NbBundle;
 import org.openide.util.RequestProcessor;
 import org.openide.util.Utilities;
 import org.openide.windows.TopComponent;
 import org.openide.windows.WindowManager;
 
 /**
  * Top component which displays JavaFX Preview.
  */
 public final class JavaFXPreviewTopComponent extends TopComponent implements PropertyChangeListener {
 
     private static JavaFXPreviewTopComponent instance;
     /** path to the icon used by the component and its open action */
 //    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
 
     private static final String PREFERRED_ID = "JavaFXPreviewTopComponent"; //NOI18N
 
     private static final File fxHome = InstalledFileLocator.getDefault().locate("javafx-sdk", "org.netbeans.modules.javafx.platform", false); //NOI18N
     private static final File previewLib = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-javafx-preview.jar", "org.netbeans.modules.javafx.editor", false); //NOI18N
 
     private BufferedImage bi;
     private DataObject oldD;
     
     private final RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
         public void run() {
             if (oldD != null) {
                 oldD.removePropertyChangeListener(JavaFXPreviewTopComponent.this);
                 oldD = null;
             }
             Node[] sel = TopComponent.getRegistry().getActivatedNodes();
             if (sel.length == 1) {
                 DataObject d = sel[0].getLookup().lookup(DataObject.class);
                 if (d != null) {
                     FileObject f = d.getPrimaryFile();
                     if (f.isData())  bi = null;
                     if ("fx".equals(f.getExt()) || "java".equals(f.getExt())) { //NOI18N
                         d.addPropertyChangeListener(JavaFXPreviewTopComponent.this);
                         oldD = d;
                         Project p = FileOwnerQuery.getOwner(f);
                         if (p instanceof JavaFXProject) {
                             PropertyEvaluator ev = ((JavaFXProject)p).evaluator();
                             FileObject srcRoots[] = ((JavaFXProject)p).getFOSourceRoots();
                             StringBuffer src = new StringBuffer();
                             String className = null;
                             for (FileObject srcRoot : srcRoots) {
                                 if (src.length() > 0) src.append(';');
                                 src.append(FileUtil.toFile(srcRoot).getAbsolutePath());
                                 if (FileUtil.isParentOf(srcRoot, f)) {
                                     className = FileUtil.getRelativePath(srcRoot, f);
                                     className = className.substring(0, className.length() - f.getExt().length() - 1).replace('/', '.');
                                 }
                             }
                             String args[] = new String[] {
                                 fxHome + "/bin/javafxpackager" + (Utilities.isWindows() ? ".exe" : ""), //NOI18N
                                 "-src", //NOI18N
                                 src.toString(),
                                 "-d", //NOI18N
                                 ev.getProperty("dist.dir")+"/preview", //NOI18N
                                 "-appclass", //NOI18N
                                 className,
                                 "-appname", //NOI18N
                                 "preview", //NOI18N
                                 "-encoding", //NOI18N
                                 ev.getProperty("source.encoding"), //NOI18N
                                 "-cp", //NOI18N
                                 ev.getProperty("javac.classpath"), //NOI18N
                             };
                             File basedir = FileUtil.toFile(p.getProjectDirectory());
                             try {
                                 Process pr = Runtime.getRuntime().exec(args, null, basedir);
                                 pr.waitFor();
                                 if (pr.exitValue() == 0) {
                                     args = new String[] {
                                         fxHome + "/bin/javafx" + (Utilities.isWindows() ? ".exe" : ""), //NOI18N
                                         "-cp", //NOI18N
                                         previewLib.getAbsolutePath() + File.pathSeparator + ev.getProperty("dist.dir")+"/preview/preview.jar", //NOI18N
                                         "org.netbeans.javafx.preview.Main", //NOI18N
                                         className
                                     };
                                     pr = Runtime.getRuntime().exec(args, null, basedir);
                                     bi = ImageIO.read(pr.getInputStream());
                                     pr.waitFor();
                                 };
                             } catch (Exception ex) {
                                 Logger.getAnonymousLogger().log(Level.INFO, ex.getLocalizedMessage(), ex);
                             }
                         }
                     }
                 }
             }
             repaint();
 
         }
     });
 
     @Override
     public void paintComponent(Graphics g) {
         BufferedImage b = bi;
         if (b != null) g.drawImage(b, 0, 0, null);
         else {
             g.clearRect(0, 0, getWidth(), getHeight());
             String noPreview = NbBundle.getMessage(JavaFXPreviewTopComponent.class, "MSG_NoPreview"); //NOI18N
             Rectangle2D r = g.getFontMetrics().getStringBounds(noPreview, g);
             g.drawString(noPreview, (getWidth()-(int)r.getWidth())/2, (getHeight()-(int)r.getHeight())/2);
         }
     }
 
     private JavaFXPreviewTopComponent() {
         initComponents();
         setName(NbBundle.getMessage(JavaFXPreviewTopComponent.class, "CTL_JavaFXPreviewTopComponent")); //NOI18N
         setToolTipText(NbBundle.getMessage(JavaFXPreviewTopComponent.class, "HINT_JavaFXPreviewTopComponent")); //NOI18N
 //        setIcon(Utilities.loadImage(ICON_PATH, true));
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
        setLayout(new java.awt.GridLayout());
     }// </editor-fold>//GEN-END:initComponents
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
     /**
      * Gets default instance. Do not use directly: reserved for *.settings files only,
      * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
      * To obtain the singleton instance, use {@link #findInstance}.
      */
     public static synchronized JavaFXPreviewTopComponent getDefault() {
         if (instance == null) {
             instance = new JavaFXPreviewTopComponent();
         }
         return instance;
     }
 
     /**
      * Obtain the JavaFXPreviewTopComponent instance. Never call {@link #getDefault} directly!
      */
     public static synchronized JavaFXPreviewTopComponent findInstance() {
         TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
         if (win == null) {
             Logger.getLogger(JavaFXPreviewTopComponent.class.getName()).warning("Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); //NOI18N
             return getDefault();
         }
         if (win instanceof JavaFXPreviewTopComponent) {
             return (JavaFXPreviewTopComponent) win;
         }
         Logger.getLogger(JavaFXPreviewTopComponent.class.getName()).warning("There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior."); //NOI18N
         return getDefault();
     }
 
     @Override
     public int getPersistenceType() {
         return TopComponent.PERSISTENCE_ALWAYS;
     }
 
     @Override
     public void componentOpened() {
         TopComponent.getRegistry().addPropertyChangeListener(this);
         task.schedule(150);
     }
 
     @Override
     public void componentClosed() {
         TopComponent.getRegistry().removePropertyChangeListener(this);
     }
 
     public void propertyChange(PropertyChangeEvent ev) {
         if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(ev.getPropertyName()) || DataObject.PROP_MODIFIED.equals(ev.getPropertyName())) {
             task.schedule(150);
         }
     }
 
     /** replaces this in object stream */
     @Override
     public Object writeReplace() {
         return new ResolvableHelper();
     }
 
     @Override
     protected String preferredID() {
         return PREFERRED_ID;
     }
 
     final static class ResolvableHelper implements Serializable {
 
         private static final long serialVersionUID = 1L;
 
         public Object readResolve() {
             return JavaFXPreviewTopComponent.getDefault();
         }
     }
 }
