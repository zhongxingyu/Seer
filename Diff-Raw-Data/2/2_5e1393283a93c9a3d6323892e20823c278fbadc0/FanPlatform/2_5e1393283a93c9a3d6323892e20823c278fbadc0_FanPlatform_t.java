 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.platform;
 
 import fan.sys.Sys;
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 import net.colar.netbeans.fan.actions.FanExecution;
 import net.colar.netbeans.fan.indexer.FanIndexerFactory;
 import javax.swing.JOptionPane;
 import net.colar.netbeans.fan.wizard.FanGlobalSettingsController;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.java.classpath.GlobalPathRegistry;
 import org.netbeans.api.options.OptionsDisplayer;
 import org.netbeans.spi.java.classpath.support.ClassPathSupport;
 import org.netbeans.spi.options.OptionsPanelController;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 
 /**
  * Provides acces to "plaform" settings
  * For exampel FAN_HOME etc...
  * @author thibautc
  */
 public class FanPlatform
 {
 
     private static boolean configWarningAlreadyDisplayed = false;
 
     private Set<ClassPath> sourcePaths = null;
     public static final boolean IS_WIN = System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;
     public static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().startsWith("mac");
     private static final String ARCH = System.getProperty("os.arch").toLowerCase();
     private static FanPlatform instance = new FanPlatform();
     public final static String FAN_CLASS = "fanx.tools.Fan";
     public final static String FANT_CLASS = "fanx.tools.Fant";
     public final static String FAN_SH = "fansh";
     public final static String FAN_TALES_POD_NAME = "tales";
     public final static String FAN_TALES_CREATE_CMD = "new";
     public final static String FAN_TALES_RUN_CMD = "run";
     private String fanHome;
     private String podsDir;
     private String fanSrc;
     private String talesHome;
 
     private FanPlatform()
     {
         readSettings();
     }
 
     private void readSettings()
     {
         fanHome = FanPlatformSettings.getInstance().get(FanPlatformSettings.PREF_FAN_HOME);
         if (fanHome != null)
         {
             if (!fanHome.endsWith(File.separator))
             {
                 fanHome += File.separator;
             }
             //fanBin = fanHome + "bin" + File.separator + (IS_WIN ? "fan.exe" : "fan");
             //fanshBin = fanHome + "bin" + File.separator + (IS_WIN ? "fansh.exe" : "fansh");
             fanSrc = fanHome + "src" + File.separator;
             podsDir = fanHome + "lib" + File.separator + "fan" + File.separator;
 
             // Set fan.home property, so we casn use Fan code later
             System.setProperty("fan.home", fanHome);
             // boot fan env - sometimes throws an exception !
             Sys.boot();
         }
         // tales
         talesHome = FanPlatformSettings.getInstance().get(FanPlatformSettings.PREF_TALES_HOME);
         // force updating paths
         sourcePaths = null;
     }
 
     public static void updateFromSettings()
     {
         instance.readSettings();
     }
 
     public static boolean checkFanHome(String path)
     {
         if (path != null && !"".equals(path))
         {
             File f = new File(path);
             if (f.exists() && f.isDirectory())
             {
                 File exe = new File(path + File.separator + "bin", "fan");
                 return exe.exists() && exe.isFile();
             }
         }
         return false;
     }
 
     public static boolean isConfigured()
     {
         // send to settings the first time isConfigured is called
         synchronized (FanPlatform.class)
         {
             if (instance.fanHome == null && !configWarningAlreadyDisplayed)
             {
                 configWarningAlreadyDisplayed = true;
                 OptionsDisplayer.getDefault().open(FanGlobalSettingsController.ID);
             }
         }
         return instance != null && instance.fanHome != null;
     }
 
     public static FanPlatform getInstance()
     {
         isConfigured();
         // throw an exception ?
         return instance;
     }
 
     public String getFanSrcPath()
     {
         return fanSrc;
     }
 
     /**
      * Add Fan Source items (pods src)
      * @return
      */
     public synchronized Set<ClassPath> getSourceClassPaths()
     {
         // Do it only once.
         if (sourcePaths == null && fanSrc != null)
         {
             sourcePaths = new HashSet<ClassPath>();
             File f = new File(fanSrc);
             File[] files = f.listFiles();
             for (File file : files)
             {
                 if (file.isDirectory() && new File(file, "build.fan").exists())
                 {
                     addFolder(file);
                 }
             }
             GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, sourcePaths.toArray(new ClassPath[sourcePaths.size()]));
         }
         return sourcePaths;
     }
 
     private void addFolder(File folder)
     {
         if (folder.exists() && folder.isDirectory())
         {
             ClassPath jcp = ClassPathSupport.createClassPath(folder.getAbsolutePath());
             sourcePaths.add(jcp);
         }
     }
 
     /**
      * Same as buildFanCall(fanExec, -1);
      * @param fanExec
      */
     public void buildFanCall(FanExecution fanExec)
     {
         buildFanCall(fanExec, false, null);
     }
 
     /**
      * Updated the FanExecution object such as the fanlaunch shell script would
      * IE: set classpath, library path etc ....
      * @param fanExec
      * @param debugPort (-1 = no debugger)
      */
     public void buildFanCall(FanExecution fanExec, boolean enableDebug, String extraClasspath)
     {
         // We will be spawning a new java VM
         String separator = System.getProperty("file.separator");
         String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
         fanExec.setCommand(path);
 
         // classpath
         //String classpath = System.getProperty("java.class.path");
         String classpath = buildFanClasspath(extraClasspath);
         fanExec.addCommandArg("-cp");
         fanExec.addCommandArg(classpath);
 
         //Set lib path
         String libPath = buildLibraryPath();
         fanExec.addCommandArg("-Djava.library.path=" + libPath);
 
         //Set fan.home
         fanExec.addCommandArg("-Dfan.home=" + fanHome);
 
         //Enable debugger
         if (enableDebug)
         {
             String debugPort = FanPlatformSettings.getInstance().get(FanPlatformSettings.PREF_DEBUG_PORT, "8000");
             // java debugger
             fanExec.addCommandArg("-Xdebug");
             fanExec.addCommandArg("-Xrunjdwp:transport=dt_socket,address=" + debugPort + ",server=y,suspend=y");
             // fan debug
             fanExec.addCommandArg("-Dfan.debug=true");
         }
 
         // custom options
         String option = FanPlatformSettings.getInstance().get(FanPlatformSettings.PREF_RUN_OPTIONS, "-Xmx128m");
         String[] options = option.split(" ");
         for (String opt : options)
         {
             fanExec.addCommandArg(opt);
         }
 
         //OSX only flag needed for SWT (as in fanlaunch)
         if (IS_MAC)
         {
             fanExec.addCommandArg("-XstartOnFirstThread");
         }
     }
 
     /**
      * Builds fan std classpath (jars)
      * @return
      */
     private String buildFanClasspath(String extraClasspath)
     {
         String cp = "";
         String cpSeparator = IS_WIN ? ";" : ":";
         String s = File.separator;
         // sys.jar
         cp += fanHome + "lib" + s + "java" + s + "sys.jar";
         // add jars in lib/java/ext
         String extDir = fanHome + "lib" + s + "java" + s + "ext";
         File dir = new File(extDir);
         if (dir.exists() && dir.isDirectory())
         {
             File[] jars = dir.listFiles();
             for (File jar : jars)
             {
                 if (jar.isFile() && jar.getName().toLowerCase().endsWith(".jar"))
                 {
                     cp += cpSeparator + extDir + s + jar.getName();
                 }
             }
         }
         // add jars file in lib/java/ext/{os}
         String os = "linux";
         if (IS_MAC)
         {
             os = "mac";
         } else if (IS_WIN)
         {
             os = "win";
         }
         extDir += s + os;
         dir = new File(extDir);
         if (dir.exists() && dir.isDirectory())
         {
             File[] jars = dir.listFiles();
             for (File jar : jars)
             {
                 if (jar.isFile() && jar.getName().toLowerCase().endsWith(".jar"))
                 {
                     cp += cpSeparator + extDir + s + jar.getName();
                 }
             }
         }
         if(extraClasspath != null)
             cp +=cpSeparator + extraClasspath;
         return cp;
     }
 
     private String buildLibraryPath()
     {
         String s = File.separator;
         String extDir = fanHome + "lib" + s + "java" + s + "ext";
         String os = "linux";
         if (IS_MAC)
         {
             os = "mac";
         } else if (IS_WIN)
         {
             os = "win";
         }
         extDir += s + os;
         return extDir;
     }
 
     public FileObject getFanHome()
     {
         if ("".equals(fanHome) || fanHome == null)
         {
             JOptionPane.showMessageDialog(null, "Fantom SDK path is not defined\nDefine in Options/Misc - Fantom Tab");
             return null;
         }
         File f = new File(fanHome);
         return FileUtil.toFileObject(FileUtil.normalizeFile(f));
     }
 
     public FileObject getTalesHome()
     {
         if ("".equals(talesHome) || talesHome == null)
         {
             JOptionPane.showMessageDialog(null, "Tales path is not defined\nDefine in Options/Misc - Fantom Tab");
             return null;
         }
         File f = new File(talesHome);
         return FileUtil.toFileObject(FileUtil.normalizeFile(f));
     }
 
     public boolean isTalesPresent()
     {
         return talesHome != null && talesHome.length() > 0 && new File(talesHome).exists();
     }
 
     public FileObject getFanSrcHome()
     {
         if ("".equals(fanHome) || fanHome == null)
         {
             JOptionPane.showMessageDialog(null, "Fantom SDK path is not defined\nDefine in Tools|Options, Fantom Tab");
             return null;
         }
         File f = new File(fanSrc);
         return FileUtil.toFileObject(FileUtil.normalizeFile(f));
     }
 
     public static void update()
     {
         // called when FAN_HOME is changed/updated.
         updateFromSettings();
         FanIndexerFactory.getIndexer().indexAll(true);
     }
 
     public String getPodsDir()
     {
         return podsDir;
     }
 
     public static String getTalesExtraClasspath()
     {
         String cp = "";
         if (FanPlatform.isConfigured() && FanPlatform.getInstance().isTalesPresent())
         {
             String cpSeparator = FanPlatform.IS_WIN ? ";" : ":";
             String s = File.separator;
             // add jars in tales_home/jars
            String jarDir = FanPlatform.getInstance().getTalesHome().getPath() + s + "jars";
             File dir = new File(jarDir);
             if (dir.exists() && dir.isDirectory())
             {
                 File[] jars = dir.listFiles();
                 for (File jar : jars)
                 {
                     if (jar.isFile() && jar.getName().toLowerCase().endsWith(".jar"))
                     {
                         cp += cpSeparator + jarDir + s + jar.getName();
                     }
                 }
             }
         }
         return cp;
     }
 
 }
