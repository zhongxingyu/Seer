 package ideah.util;
 
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.progress.ProgressIndicator;
 import com.intellij.openapi.projectRoots.Sdk;
 import com.intellij.openapi.projectRoots.SdkAdditionalData;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.openapi.util.SystemInfo;
 import com.intellij.openapi.vfs.VirtualFile;
 import ideah.sdk.HaskellSdkAdditionalData;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 public final class GHCUtil {
 
     public static void addGhcOptions(Module module, List<String> args, @NotNull String initialOptions) {
         String options = getCompilerOptions(module);
         if (options != null) {
             String initial = initialOptions.isEmpty() ? "" : initialOptions + " ";
            args.add("-c");
            args.add(initial + options);
         }
     }
 
     @Nullable
     private static String getCompilerOptions(Module module) {
         Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
         if (sdk != null) {
             SdkAdditionalData data = sdk.getSdkAdditionalData();
             if (data instanceof HaskellSdkAdditionalData) {
                 return ((HaskellSdkAdditionalData) data).getGhcOptions();
             }
         }
         return null;
     }
 
     public static void addGhcOptions(Module module, List<String> args) {
         addGhcOptions(module, args, "");
     }
 
     public static String getExeName(String file) {
         return SystemInfo.isWindows
             ? file + ".exe"
             : file;
     }
 
     public static String getGhcCommandPath(VirtualFile ghcHome) {
         if (ghcHome == null)
             return null;
         VirtualFile virBin = ghcHome.findChild("bin");
         if (virBin == null)
             return null;
         return new File(virBin.getPath(), "ghc").getAbsolutePath();
     }
 
     public static String rootsAsString(@NotNull Module module, boolean tests) {
         VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots(tests);
         StringBuilder buf = new StringBuilder();
         for (VirtualFile root : sourceRoots) {
             buf.append(':').append(root.getPath());
         }
         return buf.substring(1);
     }
 
     @NotNull
     public static GHCVersion getVersion(@Nullable String name) {
         String[] versionStr = name == null ? new String[0] : name.split("[^0-9]");
         List<Integer> parts = new ArrayList<Integer>();
         for (String part : versionStr) {
             if (part.isEmpty())
                 continue;
             try {
                 parts.add(new Integer(part));
             } catch (NumberFormatException nfex) {
                 // ignore
             }
         }
         return new GHCVersion(parts);
     }
 
     static void increaseIndicatorFraction(@Nullable ProgressIndicator indicator, double step) {
         updateIndicator(indicator, step, null);
     }
 
     static void updateIndicatorText(@Nullable ProgressIndicator indicator, String message) {
         updateIndicator(indicator, 0, message);
     }
 
     static void updateIndicator(@Nullable ProgressIndicator indicator, double step, String message) {
         if (indicator != null) {
             indicator.setFraction(indicator.getFraction() + step);
             if (message != null) {
                 indicator.setText(message);
             }
         }
     }
 }
