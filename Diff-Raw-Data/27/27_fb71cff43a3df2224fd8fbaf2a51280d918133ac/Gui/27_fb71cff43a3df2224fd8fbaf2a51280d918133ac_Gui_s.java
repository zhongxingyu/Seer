 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.bootstrap;
 
 import edu.stanford.ejalbert.BrowserLauncher;
 import java.awt.Desktop;
 import java.awt.Image;
 import java.awt.SystemTray;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.jtray.*;
 import org.glassfish.embeddable.GlassFishException;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author kevin
  */
 
 public class Gui extends JFrame {
     private static final org.slf4j.Logger log = LoggerFactory.getLogger(GF.class.getName());
     
     private static final boolean WANT_SYSTRAY = true;
     
     public static String config_path = GF.DEFAULT_CONFIG_PATH;
     
     private static GF glassfish = new GF();
     
     public static void main(final String args[]) throws Throwable {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
                     Gui gui = new Gui(args);
                 } catch (Throwable ex) {
                     log.error("New GUI error:", ex);
                 }
             }
         });
     }
     
     public interface ToAddTo {
         void add(JMenuItem item) ;
     }
     
     enum GlassfishState {STOPPED, STARTING, RUNNING, FAILED}
     
     GlassfishState gfState ;
     
     final JMenu mWebAlbums;
     final JMenuItem miState;
     final JMenuItem miStart;
     final JMenuItem miQuit;
     final JMenuItem miShutdown;
     final JMenuItem miLaunch;
     
     final JMenu mWebAlbumsFS;
     final JMenuItem miMount;
     final JMenuItem miUmount;
     final JMenuItem miOpen;
     
     final JMenu mConfig;
     final JMenu mConfigPath;
     final JMenuItem miCfgPathRoot;
     final JMenuItem miCfgPathFS;
     final JMenuItem miCfgPathLibFS;
     final JMenuItem miOpenRoot;
     final JMenuItem miCfgLoad;
     final JMenuItem miCfgSave;
     final JMenuItem miCfgVerify;
     
     JTrayIcon icon = null;
     
     //Obtain the image URL
     protected static Image createImage(String path, String description) {
         URL imageURL = Gui.class.getResource(path);
         
         if (imageURL == null) {
             System.err.println("Resource not found: " + path);
             return null;
         } else {
             return (new ImageIcon(imageURL, description)).getImage();
         }
     }
     
     public Gui(String args[]) throws Throwable {
         String title = "WebAlbums3.5-dev GUI Bootloader";
         boolean systrayed = false;
         
         ToAddTo toAddTo;
         if (WANT_SYSTRAY && SystemTray.isSupported()) {
             systrayed = true;
             final JPopupMenu jpop = new JPopupMenu();
 
             JTrayIcon.initSystemTray();
 
             Image img = createImage("/images/favicon-orange.png", "WebAlbums Favicon");
             icon = new JTrayIcon(img, null, jpop);
             
             toAddTo = new ToAddTo() {
                 public void add(JMenuItem item) {
                     if (item != null) {
                         jpop.add(item);
                     } else {
                         jpop.addSeparator();
                     }
                 }
             };
             
             
         } else {
             this.setTitle(title);
             this.setLocationRelativeTo(null);
             this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             
             final JMenuBar mb = new JMenuBar();   
             this.setJMenuBar(mb);
             
             toAddTo = new ToAddTo() {
                 public void add(JMenuItem item) {
                     if (item != null) {
                         mb.add(item);
                     }
                 }
             };
         }
         
         ToAddTo toAddToWebAlbums;
         if (!systrayed) {
             mWebAlbums = new JMenu();
             mWebAlbums.setText("WebAlbums");
             toAddTo.add(mWebAlbums);
             
             toAddToWebAlbums = new ToAddTo() {
                 public void add(JMenuItem item) {
                     if (item != null) {
                         mWebAlbums.add(item);
                     }
                 }
             };
             
         } else {
             mWebAlbums = null;
             toAddToWebAlbums = toAddTo;
         }
         
         miState = new JMenuItem();
         toAddToWebAlbums.add(miState);
         
         toAddToWebAlbums.add(null);
         toAddToWebAlbums.add(null);
         
         miStart = new JMenuItem();
         toAddToWebAlbums.add(miStart);
         miStart.addActionListener(new StartActionListener());
         miStart.setText("Start");
         
         toAddToWebAlbums.add(null);
         
         miLaunch = new JMenuItem();
         if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
             toAddToWebAlbums.add(miLaunch);
             miLaunch.addActionListener(new LaunchActionListener());
             miLaunch.setText("Launch");
 
             toAddToWebAlbums.add(null);
         }
         
         miShutdown = new JMenuItem();
         toAddToWebAlbums.add(miShutdown);
         miShutdown.addActionListener(new QuitActionListener(false));
         miShutdown.setText("Shutdown");
         
         miQuit = new JMenuItem();
         toAddToWebAlbums.add(miQuit);
         miQuit.addActionListener(new QuitActionListener(true));
         
         toAddTo.add(null);
         toAddTo.add(null);
         
         mWebAlbumsFS = new JMenu();
         mWebAlbumsFS.setText("Filesystem");
         toAddTo.add(mWebAlbumsFS);
                 
         miMount = new JMenuItem();
         miMount.setText("Mount"); 
         mWebAlbumsFS.add(miMount);
         miMount.addActionListener(new MountActionListener(true));
         
         mWebAlbumsFS.addSeparator();
         
         miOpen = new JMenuItem();
         if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
             miOpen.setText("Open"); 
             mWebAlbumsFS.add(miOpen);
             miOpen.addActionListener(new OpenActionListener(new StringGetPointer() {
 
                 public String getString() {
                     return GF.cfg.webAlbumsFS;
                 }
             }));
 
             mWebAlbumsFS.addSeparator();
         }
         
         miUmount = new JMenuItem();
         miUmount.setText("Unmount"); mWebAlbumsFS.add(miUmount);
         miUmount.addActionListener(new MountActionListener(false));
         
         mConfig = new JMenu();
         mConfig.setText("Configuration");
         toAddTo.add(mConfig);
         
         mConfigPath = new JMenu();
         mConfigPath.setText("Paths");
         mConfig.add(mConfigPath);
         
         miCfgPathRoot = new JMenuItem();
         miCfgPathRoot.setText("WebAlbums root"); 
         mConfigPath.add(miCfgPathRoot);
         miCfgPathRoot.addActionListener(new PathActionListener(new StringPointer() {
 
             public void setString(String str) {
                 GF.cfg.root_path = str;
             }
 
             public String getString() {
                 return GF.cfg.root_path;
             }
         }, true, null));
         
         miCfgPathFS = new JMenuItem();
         miCfgPathFS.setText("Filesystem mount");
         mConfigPath.add(miCfgPathFS);
         miCfgPathFS.addActionListener(new PathActionListener(new StringPointer() {
 
             public void setString(String str) {
                 GF.cfg.webAlbumsFS = str;
             }
 
             public String getString() {
                 return GF.cfg.webAlbumsFS;
             }
         }, true, null));
         
         mConfig.addSeparator();
         
         miCfgPathLibFS = new JMenuItem();
        miCfgPathLibFS.setText("libjnetfs.so");
         mConfigPath.add(miCfgPathLibFS);
         miCfgPathLibFS.addActionListener(new PathActionListener(new StringPointer() {
 
             public void setString(String str) {
                 GF.cfg.libJnetFs = new File(str).getParent();
             }
 
             public String getString() {
                 return GF.cfg.libJnetFs;
             }
         }, false, "libJnetFS.so"));
         
         miOpenRoot = new JMenuItem();
         if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
             mConfigPath.addSeparator();
             
            miOpenRoot.setText("Open root folder"); 
             mConfigPath.add(miOpenRoot);
             miOpenRoot.addActionListener(new OpenActionListener(new StringGetPointer() {
 
                 public String getString() {
                    return GF.cfg.root_path + File.pathSeparator + "data";
                 }
             }));
 
         }
         
         mConfig.addSeparator();
         
         miCfgLoad = new JMenuItem();
         miCfgLoad.setText("Load"); 
         mConfig.add(miCfgLoad);
         miCfgLoad.addActionListener(new PathActionListener(new StringPointer() {
 
             public void setString(String str) {
                 Gui.config_path = str;
                 try {
                     GF.Config.load(Gui.config_path);
                 } catch (Exception ex) {
                     log.error("Load config error: {}", ex);
                 }
             }
 
             public String getString() {
                 return Gui.config_path;
             }
         }, false, ".xml"));
         
         mConfig.addSeparator();
         
         miCfgSave = new JMenuItem();
         miCfgSave.setText("Save");
         mConfig.add(miCfgSave);
         miCfgSave.addActionListener(new PathActionListener(new StringPointer() {
 
             public void setString(String str) {
                 Gui.config_path = str;
                 try {
                     GF.cfg.save(Gui.config_path);
                 } catch (Exception ex) {
                     log.error("Save config error: {}", ex);
                 }
             }
 
             public String getString() {
                 return Gui.config_path;
             }
         }, false, ".xml"));
         
         mConfig.addSeparator();
         
         miCfgVerify = new JMenuItem();
         miCfgVerify.setText("Verify");
         mConfig.add(miCfgVerify);
         miCfgVerify.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent ae) {
                 Map<String, Boolean> checkpoints = new HashMap<String, Boolean>();
                 
                 checkpoints.put("Root path is directory", new File(GF.cfg.root_path).isDirectory());
                 checkpoints.put("FS path is directory", new File(GF.cfg.webAlbumsFS).isDirectory());
                 checkpoints.put("EAR file exists", new File(GF.cfg.webAlbumsEAR).isFile());
                checkpoints.put("FS library file exists", new File(GF.cfg.libJnetFs).isFile());
                 checkpoints.put("Database configuration file exists", new File(GF.cfg.sunResourcesXML).isFile());
                 
                 //GF.cfg.port is free
                 
                 StringBuilder res = new StringBuilder();
                 for (String key : checkpoints.keySet()) {
                     res.append(key).append(" : ").append(checkpoints.get(key)).append("\n");
                 }
                 
                 JOptionPane.showMessageDialog(Gui.this, res.toString());
             }
         });
                 
         glassfishStopped();
         
         if (!SystemTray.isSupported()) {
             this.setVisible(true);
         }
         
         for (String arg : args) {
             if ("--start".equals(arg) || "-s".equals(arg)) {
                 miStart.doClick();
             }
         }
     }
 
     private void glassfishStarting() {
         miCfgPathRoot.setEnabled(false);
         miCfgLoad.setEnabled(false);
         miStart.setEnabled(false);
         miQuit.setText("Force quit");
         miState.setText("Starting");
         miCfgPathLibFS.setEnabled(false);
         miCfgVerify.setEnabled(false);
         icon.setIcon(createImage("/images/favicon-orange.png", "WebAlbums Favicon"));
         gfState = GlassfishState.STARTING;
     }
     
     private void glassfishRunning() {
         gfState = GlassfishState.RUNNING;
         miQuit.setText("Shutdown & Quit");
         miShutdown.setEnabled(true);
         miLaunch.setEnabled(true);
         mWebAlbumsFS.setEnabled(true);
         miMount.setEnabled(true);
         miState.setText("Running");
         if (icon != null) {
             icon.setIcon(createImage("/images/favicon.png", "WebAlbums Favicon"));
         }
     }
     
     private void glassfishStopped() {
         gfState = GlassfishState.STOPPED;
         
         miState.setText("Stopped");
         miCfgVerify.setEnabled(true);
         miStart.setEnabled(true);
         miLaunch.setEnabled(false);
         miShutdown.setEnabled(false);
         miQuit.setText("Quit");
         miQuit.setEnabled(true);
         
         mWebAlbumsFS.setEnabled(false);
         miMount.setEnabled(false);
         miOpen.setEnabled(false);
         miUmount.setEnabled(false);
         
         miCfgLoad.setEnabled(true); 
         miCfgSave.setEnabled(true);
         miCfgPathLibFS.setEnabled(true);
         miCfgPathFS.setEnabled(true);
         miCfgPathRoot.setEnabled(true); 
         
         if (icon != null) {
             icon.setIcon(createImage("/images/favicon-blue.png", "WebAlbums Favicon"));
         }
     }
     
     private void glassfishFailed() {
         try {
             Gui.glassfish.terminate();
         } catch (GlassFishException ex) {
             log.error("Stop glassfish error: {}", ex);
         }
         glassfishStopped();
         miState.setText("Crashed");
         
         gfState = GlassfishState.FAILED;
         if (icon != null) {
             icon.setIcon(createImage("/images/favicon-red.png", "WebAlbums Favicon"));
         }
     }
     
     private class StartActionListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             new Thread(new Runnable() {
                 public void run() {
                     try {
                         glassfishStarting();
                         glassfish.start();
                         glassfishRunning();
                     } catch (Throwable ex) {
                         glassfishFailed();
                         log.error("Start glassfish error: {}", ex);
                     }
                 }
             }).start();
        }
     }
     
     private class QuitActionListener implements ActionListener {
         private final boolean quit;
         
         public QuitActionListener(boolean quit) {
             this.quit = quit;
         }
         
         public void actionPerformed(ActionEvent event) {
             if (gfState == GlassfishState.RUNNING) {
                 new Thread(new Runnable() {
                     public void run() {
                         try {
                             Gui.glassfish.terminate();
                             glassfishStopped();
                             if (QuitActionListener.this.quit) {
                                 log.error("That's all, folks!");
                                 System.exit(0);
                             }
                         } catch (GlassFishException ex) {
                             log.error("Stop glassfish error: {}", ex);
                             glassfishFailed();
                         }
                     }
                 }).start();
             } else if (this.quit) {
                 log.error("That's all, folks!");
                 System.exit(0);
             }
        }
     }
     
     private interface StringGetPointer {
         String getString();
     }
     
     private interface StringPointer extends StringGetPointer {
         void setString(String str);
     }
     
     private class PathActionListener implements ActionListener {
         private final StringPointer ptr;
         private final boolean dirOnly;
         private final String ext;
         PathActionListener(StringPointer ptr, boolean dirOnly, String ext) {
             this.ptr = ptr;
             this.dirOnly = dirOnly;
             this.ext = ext;
         }
         public void actionPerformed(ActionEvent event) {
             final JFileChooser fc = new JFileChooser();
             
             if (this.dirOnly) {
                 fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             }
             fc.setAcceptAllFileFilterUsed(false);
             
             if(ptr.getString() != null)  {
                 fc.setCurrentDirectory(new File(ptr.getString()));
             }
             
             fc.addChoosableFileFilter(new FileFilter() {
                 @Override
                 public boolean accept(File file) {
                     if (dirOnly) {
                         return file.isDirectory();
                     } else if (!file.isDirectory() && ext != null) {
                         return file.getName().endsWith(ext);
                     } else {
                         return true;
                     }
                 }
 
                 @Override
                 public String getDescription() {
                     if (dirOnly) {
                         return "Directories" ;
                     } else if (ext != null) {
                         return "'"+ext+"' files";
                     } else {
                         return "Any file";
                     }
                 }
             });
             int returnVal = fc.showOpenDialog(Gui.this);
 
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = fc.getSelectedFile();
                 
                 ptr.setString(file.getAbsolutePath());
             }
        }
     }
     
     private class LaunchActionListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             if (gfState == GlassfishState.RUNNING) {
                 try {
                     new BrowserLauncher().openURLinBrowser("http://localhost:"+GF.cfg.port+"/WebAlbums3.5-dev");
                 } catch (Exception ex) {
                     log.error("Launch website error: {}", ex);
                 }
             }
        }
     }
     
     private class OpenActionListener implements ActionListener {
         private final StringGetPointer ptr;
         public OpenActionListener(StringGetPointer ptr) {
             this.ptr = ptr;
         }
     
         public void actionPerformed(ActionEvent event) {
            if (gfState == GlassfishState.RUNNING) {
                 try {
                    Desktop.getDesktop().open(new File(ptr.getString()));    
                } catch (Exception ex) {
                    log.error("Open FS error: {}", ex);
                 }
             }
        }
     }
     
     private class MountActionListener implements ActionListener {        
         private final boolean do_mount;
         MountActionListener(boolean do_mount) {
             this.do_mount = do_mount;
         }
         private boolean mounted = false;
         
         private synchronized boolean setMounted(Boolean mounted) {
             boolean wasMounted = this.mounted;
             if (mounted != null) {
                 this.mounted = mounted;
                 Gui.this.miMount.setEnabled(!mounted);
                 
                 Gui.this.miUmount.setEnabled(mounted);
                 Gui.this.miOpen.setEnabled(mounted);
                 Gui.this.miCfgPathFS.setEnabled(mounted);
             }
             return wasMounted;
         }
         
         public boolean isMounted() {
             return setMounted(null);
         }
         
         public void actionPerformed(ActionEvent event) {
             doTrigger(do_mount, GF.cfg.webAlbumsFS);
         }
         
         public void doTrigger(final boolean mount, String mountPoint) {
             try {
                 mountPoint = mountPoint == null ? "" : mountPoint;
                 URL url = new URL("http://localhost:"+GF.cfg.port+"/WebAlbums3-FS/Launch?"+(mount ? "" : "umount=true")+"path="+mountPoint);
                 final URLConnection conn = url.openConnection();
 
                 new Thread (new Runnable() {
 
                     public void run() {
                         if (mount) {
                             setMounted(true);
                         }
                         try {
                             // open the stream and put it into BufferedReader
                             BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                             while ((br.readLine()) != null) ;
                         } catch (IOException ex) {}
                         //will be set by mount thread only
                         if (mount) {
                             setMounted(false);
                         }
                     }
                 }).start();
                 
             } catch (IOException ex) {
                 log.error("Mount FS error: {}", ex);
             }
        }
     }
 }
