 package org.netbeans.modules.javafx.editor.preview;
 
 import java.awt.Graphics;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 import org.netbeans.modules.javafx.project.JavaFXProject;
 import org.netbeans.spi.project.support.ant.PropertyEvaluator;
 import org.netbeans.spi.project.support.ant.PropertyUtils;
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
     private static final Logger log = Logger.getLogger("org.netbeans.javafx.preview"); //NOI18N
 
     private static final File fxHome = InstalledFileLocator.getDefault().locate("javafx-sdk", "org.netbeans.modules.javafx.platform", false); //NOI18N
     private static final File previewLib = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-javafx-preview.jar", "org.netbeans.modules.javafx.editor", false); //NOI18N
 
     private BufferedImage bi;
     private DataObject oldD;
     private Process pr;
     private int timer;
 
     private final RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
         public void run() {
             synchronized (JavaFXPreviewTopComponent.this) {
                 if (pr != null) {
                     pr.destroy();
                     timer = 0;
                     task.schedule(150);
                     return;
                 }
             }
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
                     if ("fx".equals(f.getExt())) { //NOI18N
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
                                     className = className.substring(0, className.length() - 3).replace('/', '.');
                                 }
                             }
                             String cp = ev.getProperty("javac.classpath"); //NOI18N
                             cp = cp == null ? "" : cp.trim();
                             String enc = ev.getProperty("source.encoding");  //NOI18N
                             if (enc == null || enc.trim().length() == 0) enc = "UTF-8"; //NOI18N
                             File basedir = FileUtil.toFile(p.getProjectDirectory());
                             File build = PropertyUtils.resolveFile(basedir, "build/compiled"); //NOI18N
                             ArrayList<String> args = new ArrayList<String>();
                             args.add(fxHome + "/bin/javafxc" + (Utilities.isWindows() ? ".exe" : "")); //NOI18N
                             args.add("-cp"); //NOI18N
                             args.add(build.getAbsolutePath() + File.pathSeparator + cp);
                             args.add("-sourcepath"); //NOI18N
                             args.add(src.toString());
                             args.add("-d"); //NOI18N
                             args.add(build.getAbsolutePath());
                             args.add("-encoding"); //NOI18N
                             args.add(enc.trim());
                             args.add(FileUtil.toFile(f).getAbsolutePath());
                             try {
                                 build.mkdirs();
                                 log.info(args.toString());
                                 synchronized (JavaFXPreviewTopComponent.this) {
                                     pr = Runtime.getRuntime().exec(args.toArray(new String[args.size()]), null, basedir);
                                 }
                                 if (log.isLoggable(Level.INFO)) {
                                     ByteArrayOutputStream err = new ByteArrayOutputStream();
                                     InputStream in = pr.getErrorStream();
                                     try {
                                         FileUtil.copy(in, err);
                                     } catch (IOException e) {
                                         log.severe(e.getLocalizedMessage());
                                     } finally {
                                         try {
                                             in.close();
                                         } catch (IOException e) {}
                                     }
                                     log.info(err.toString());
                                 }
                                 pr.waitFor();
                                 String jvmargs = ev.getProperty("run.jvmargs"); //NOI18N
                                 String appargs = ev.getProperty("application.args");  //NOI18N
                                 if (pr.exitValue() == 0) {
                                     args = new ArrayList<String>();
                                     args.add(fxHome + "/bin/javafx" + (Utilities.isWindows() ? ".exe" : "")); //NOI18N
                                     args.add("-javaagent:" + previewLib.getAbsolutePath());//NOI18N
                                     args.add("-Xbootclasspath/p:" + previewLib.getAbsolutePath());//NOI18N
                                     args.add("-Dcom.apple.backgroundOnly=true"); //NOI18N
                                     args.add("-Dapple.awt.UIElement=true"); //NOI18N
                                     if (jvmargs != null) for (String ja : jvmargs.trim().split("\\s+")) if (ja.length()>0) args.add(ja); //NOI18N
                                     args.add("-cp"); //NOI18N
                                     args.add(src.toString() + File.pathSeparator + build.getAbsolutePath() + File.pathSeparator + cp); //NOI18N
                                     args.add(className);
                                     if (appargs != null) for (String aa : appargs.trim().split("\\s+")) args.add(aa); //NOI18N
                                     log.info(args.toString());
                                     synchronized (JavaFXPreviewTopComponent.this) {
                                         pr = Runtime.getRuntime().exec(args.toArray(new String[args.size()]), null, basedir);
                                     }
                                     if (log.isLoggable(Level.INFO)) {
                                         RequestProcessor.getDefault().execute(new Runnable() {
                                             public void run() {
                                                 ByteArrayOutputStream err = new ByteArrayOutputStream();
                                                 Process p = pr;
                                                 if (p == null) return;
                                                 InputStream in = p.getErrorStream();
                                                 try {
                                                     final byte[] BUFFER = new byte[4096];
                                                     int len;
                                                     while (pr != null && timer > 0) {
                                                         while ((len = in.available()) > 0) {
                                                             len = in.read(BUFFER, 0, BUFFER.length > len ? len : BUFFER.length);
                                                             err.write(BUFFER, 0, len);
                                                         }
                                                         Thread.sleep(50);
                                                     }
                                                 } catch (IOException e) {
                                                     log.severe(e.getLocalizedMessage());
                                                 } catch (InterruptedException ie) {
                                                 } finally {
                                                     try {
                                                         in.close();
                                                     } catch (IOException e) {}
                                                 }
                                                 log.info(err.toString());
                                             }
                                         });
                                     }
                                     InputStream in = pr.getInputStream();
                                     timer = 200;
                                     while (timer-- > 0 && in.available() == 0) Thread.sleep(50);
                                     if (in.available() > 0) bi = ImageIO.read(in);
                                 }
                             } catch (Exception ex) {
                                 //ignore
                             } finally {
                                 synchronized (JavaFXPreviewTopComponent.this) {
                                    pr.destroy();
                                     pr = null;
                                 }
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
 
         setLayout(new java.awt.BorderLayout());
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
