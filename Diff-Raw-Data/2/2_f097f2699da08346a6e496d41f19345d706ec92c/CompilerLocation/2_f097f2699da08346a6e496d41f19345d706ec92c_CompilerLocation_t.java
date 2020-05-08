 package ideah.util;
 
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.projectRoots.Sdk;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.openapi.util.SystemInfo;
 import com.intellij.openapi.util.io.StreamUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.openapi.wm.StatusBar;
 
 import java.io.*;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
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
 
     private interface HsCallback {
 
         void run(ZipInputStream zis, ZipEntry entry) throws IOException;
     }
 
     private static void listHaskellSources(HsCallback callback) throws IOException {
         InputStream is = CompilerLocation.class.getResourceAsStream("/ask_ghc.jar");
         ZipInputStream zis = new ZipInputStream(is);
         while (true) {
             ZipEntry entry = zis.getNextEntry();
             if (entry == null)
                 break;
             callback.run(zis, entry);
             zis.closeEntry();
         }
         zis.close();
     }
 
     private static boolean needRecompile(File compilerExe) throws IOException {
         if (compilerExe.exists()) {
             if (sourcesLastModified == null) {
                 final Long[] maxModified = new Long[1];
                 listHaskellSources(new HsCallback() {
                     public void run(ZipInputStream zis, ZipEntry entry) {
                         long lastModified = entry.getTime();
                         if (maxModified[0] == null) {
                             maxModified[0] = lastModified;
                         } else {
                             maxModified[0] = Math.max(maxModified[0].longValue(), lastModified);
                         }
                     }
                 });
                 sourcesLastModified = maxModified[0];
             }
             return sourcesLastModified != null && sourcesLastModified.longValue() > compilerExe.lastModified();
         } else {
             return true;
         }
     }
 
     public static synchronized CompilerLocation get(Module module) {
        if (module == null)
            return null;
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
                 if (!compileHs(module.getProject(), pluginPath, ghcHome, compilerExe))
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
 
     public static String rootsAsString(Module module, boolean tests) {
         VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots(tests);
         StringBuilder buf = new StringBuilder();
         for (VirtualFile root : sourceRoots) {
             buf.append(':').append(root.getPath());
         }
         return buf.substring(1);
     }
 
     private static boolean compileHs(Project project, final File pluginPath, VirtualFile ghcHome, File exe) throws IOException, InterruptedException {
         exe.delete();
         VirtualFile ghcBin = ghcHome.findChild("bin");
         if (ghcBin == null)
             return false;
         StatusBar.Info.set("Compiling " + MAIN_FILE + "...", project);
         try {
             listHaskellSources(new HsCallback() {
                 public void run(ZipInputStream zis, ZipEntry entry) throws IOException {
                     File outFile = new File(pluginPath, entry.getName());
                     OutputStream os = new FileOutputStream(outFile);
                     try {
                         StreamUtil.copyStreamContent(zis, os);
                     } finally {
                         os.close();
                     }
                 }
             });
             String mainHs = MAIN_FILE + ".hs";
             ProcessLauncher launcher = new ProcessLauncher(
                 true, null,
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
         } finally {
             StatusBar.Info.set("Done compiling " + MAIN_FILE, project);
         }
     }
 }
