 package ideah.util;
 
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.projectRoots.Sdk;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.openapi.util.SystemInfo;
 import com.intellij.openapi.util.io.FileUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 public final class CompilerLocation {
 
     private static final Logger LOG = Logger.getInstance("ideah.util.CompilerLocation");
     private static final String MAIN_FILE = "ask_ghc";
 
     private static Long sourcesLastModified = null;
 
     public final String exe;
     public final String libPath;
 
     private CompilerLocation(String exe, String libPath) {
         this.exe = exe;
         this.libPath = libPath;
     }
 
     private static String getExeName(String file) {
         return SystemInfo.isWindows
                 ? file + ".exe"
                 : file;
     }
 
     private static File[] listHaskellSources() throws URISyntaxException {
         File pluginHaskellDir = new File(CompilerLocation.class.getResource("/ask_ghc/").toURI());
         return pluginHaskellDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".hs");
             }
         });
     }
 
     private static boolean needRecompile(File compilerExe) throws URISyntaxException {
         if (compilerExe.exists()) {
             if (sourcesLastModified == null) {
                 Long maxModified = null;
                 File[] haskellSources = listHaskellSources();
                 for (File file : haskellSources) {
                     long lastModified = file.lastModified();
                     if (maxModified == null) {
                         maxModified = lastModified;
                     } else {
                         maxModified = Math.max(maxModified.longValue(), lastModified);
                     }
                 }
                 sourcesLastModified = maxModified;
             }
             return sourcesLastModified != null && sourcesLastModified.longValue() > compilerExe.lastModified();
         } else {
             return true;
         }
     }
 
     public static synchronized CompilerLocation get(Module module) {
         Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
         if (sdk == null)
             return null;
         VirtualFile ghcHome = sdk.getHomeDirectory();
         if (ghcHome == null)
             return null;
         VirtualFile ghcLib = ghcHome;
         VirtualFile packageConfD = ghcLib.findChild("package.conf.d");
         if (packageConfD == null) {
             ghcLib = ghcHome.findChild("lib");
         }
         if (ghcLib == null)
             return null;
         try {
             File pluginPath = new File(new File(System.getProperty("user.home"), ".ideah"), sdk.getVersionString());
             pluginPath.mkdirs();
             File compilerExe = new File(pluginPath, getExeName(MAIN_FILE));
             if (needRecompile(compilerExe)) {
                 if (!compileHs(pluginPath, ghcHome, compilerExe))
                     return null;
             }
             if (compilerExe.exists()) {
                 return new CompilerLocation(compilerExe.getAbsolutePath(), ghcLib.getPath());
             } else {
                 return null;
             }
         } catch (Exception ex) {
             LOG.error(ex);
             return null;
         }
     }
 
     public static String rootsToString(VirtualFile[] roots) {
         StringBuilder sourceRoots = new StringBuilder();
         for (VirtualFile root : roots) {
             sourceRoots.append(":").append(root.getPath());
         }
         return sourceRoots.substring(1);
     }
 
     private static boolean compileHs(File pluginPath, VirtualFile ghcHome, File exe) throws IOException, InterruptedException, URISyntaxException {
         VirtualFile ghcBin = ghcHome.findChild("bin");
         if (ghcBin == null)
             return false;
         File[] haskellSources = listHaskellSources();
         for (File file : haskellSources) {
             File outFile = new File(pluginPath, file.getName());
             FileUtil.copy(file, outFile);
         }
         String mainHs = MAIN_FILE + ".hs";
         ProcessLauncher launcher = new ProcessLauncher(
             true,
             new File(ghcBin.getPath(), "ghc").getAbsolutePath(),
             "--make", "-cpp", "-O", "-package", "ghc",
             "-i" + pluginPath.getAbsolutePath(),
             new File(pluginPath, mainHs).getAbsolutePath()
         );
         for (int i = 0; i < 3; i++) {
             if (exe.exists())
                 return true;
             Thread.sleep(100);
         }
         String stdErr = launcher.getStdErr();
         LOG.error("Compiling " + mainHs + ":\n" + stdErr);
         return false;
     }
 }
