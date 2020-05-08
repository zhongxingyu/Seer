 package fr.ethilvan.launcher.updater;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileUtils;
 
 import fr.ethilvan.launcher.Launcher;
 import fr.ethilvan.launcher.config.Config;
 import fr.ethilvan.launcher.ui.TaskDialog;
 import fr.ethilvan.launcher.util.OS;
 
 public class Updater {
 
     private final TaskDialog dialog;
     private final UpdateChecker updateChecker;
     private final Set<String> tags;
 
     private File tmpDir;
     private int downloadsCount;
     private boolean done = false; 
 
     public Updater(UpdateChecker checker, TaskDialog dialog) {
         this.updateChecker = checker;
         this.dialog = dialog;
         this.tags = new HashSet<String>();
 
         tags.add(OS.get().name().toLowerCase());
         if (checker.isFirstUpdate()) {
             tags.add("first");
         }
         Config config = Launcher.get().getConfig();
         if (config.getUseDefaultConfig()) {
             tags.add("config");
         }
         tags.add(config.getUseLatestLWJGL() ? "lwjgl" : "lwjglold");
        Logger.getLogger(Updater.class.getName())
                .info("Update tags : " + tags);
     }
 
     public boolean perform() {
         dialog.setStatus("Mise à jour", null);
 
         tmpDir = new File(Launcher.get().getGameDirectory(), ".tmp");
         if (tmpDir.exists()) {
             FileUtils.deleteQuietly(tmpDir);
         }
         try {
             FileUtils.forceMkdir(tmpDir);
         } catch (IOException exc) {
             Logger.getLogger(Updater.class.getName())
                     .log(Level.SEVERE, "Unable to create temporary directory"
                             + "to store downloaded files", exc);
         }
 
         Update packageList = getPackageList();
         if (packageList == null) {
             return false;
         }
 
         removeAll(packageList.toRemoves);
         downloadAll(packageList.packages);
         return true;
     }
 
     private Update getPackageList() {
        Logger.getLogger(Updater.class.getName())
                .info("Fetching packages list.");
         UpdateDownload updateList = new UpdateDownload(dialog);
         try {
             Launcher.get().download(updateList);
         } catch (IOException exc) {
             Logger.getLogger(Updater.class.getName())
                     .log(Level.SEVERE, "Cannot fetch download list", exc);
             return null;
         }
 
         try {
             updateList.waitForDone();
             return updateList.getPackageList();
         } catch (InterruptedException exc) {
             Logger.getLogger(Updater.class.getName())
                     .log(Level.SEVERE, "Cannot fetch download list", exc);
             return null;
         }
     }
 
     public synchronized void decrementDownloads() {
         downloadsCount--;
     }
 
     private void removeAll(String[] toRemoves) {
         File dir = Launcher.get().getGameDirectory();
         for (String toRemove : toRemoves) {
             File file = new File(dir, toRemove);
             Logger.getLogger(Updater.class.getName())
                     .info("Removing " + toRemove);
             try {
                 FileUtils.forceDelete(file);
             } catch (FileNotFoundException exc) {
             } catch (IOException exc) {
                 Logger.getLogger(Updater.class.getName())
                         .log(Level.SEVERE, "Unable to remove " + toRemove);
             }
         }
     }
 
     private void downloadAll(Package[] ppackages) {
         downloadsCount = ppackages.length;
 
         for (Package ppackage : ppackages) {
             if (!ppackage.isNeeded(tags)) {
                 decrementDownloads();
                 continue;
             }
 
             PackageDownload download = PackageDownload.create(this, dialog,
                     tmpDir, ppackage);
             try {
                 Logger.getLogger(Updater.class.getName())
                         .info("Downloading " + ppackage.name);
                 Launcher.get().download(download);
             } catch (IOException exc) {
                 Logger.getLogger(Updater.class.getName()).log(Level.SEVERE,
                         "Cannot download " + ppackage.name, exc);
                 decrementDownloads();
             }
         }
 
         while (!done) {
             try {
                 Thread.sleep(100);
             } catch (InterruptedException _) {
             }
         }
     }
 
     public void onDownloadComplete(Package ppackage) {
         try {
             File tmpFile = ppackage.getTemp(tmpDir);
             File targetPath = new File(Launcher.get().getGameDirectory(),
                     ppackage.path);
             InputStream input = new FileInputStream(tmpFile);
             ppackage.getFilter().filter(dialog, input, targetPath);
         } catch (IOException exc) {
             Logger.getLogger(Updater.class.getName())
                     .log(Level.SEVERE, "Unable to read downloaded file", exc);
         }
 
         if (downloadsCount == 0) {
             FileUtils.deleteQuietly(tmpDir);
             updateChecker.updatePerformed();
             done = true;
         }
     }
 }
