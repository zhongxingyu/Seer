 package org.alex73.android.bel;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.alex73.android.common.FileInfo;
 import org.alex73.android.common.FilePerm;
 import org.alex73.android.common.JniWrapper;
 import org.alex73.android.common.zip.ApkUpdater;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.content.pm.ApplicationInfo;
 import android.content.res.Resources;
 import android.os.Build;
 import android.os.Environment;
 import android.os.StatFs;
 import android.util.Xml;
 
 /**
  * FileObserver catch events:
  * 
  * on write new file via FileOutputStream - CLOSE_WRITE for to
  *
  * on write into existing file via FileOutputStream - CLOSE_WRITE for to
  *
  * on rename into existing - MOVED_FROM for from, MOVED_TO for to
  * 
  * on 'mv ..' on one system device into existing - MOVED_FROM for from, MOVED_TO for to
  * 
  * on 'dd ..' on one system device into existing - CLOSE_WRITE for to
  * 
  * lastModifiedTime mustn't be changed, because PackageManagerService checks it on system startup, then checks certificates
  */
 public class LocalStorage {
     final static int BUFFER_SIZE = 32768;
 
     final static String BACKUP_PARTITION = Environment.getExternalStorageDirectory().getPath();
     final static String OUR_DIR = BACKUP_PARTITION + "/i18n-bel/";
     final static String BACKUP_DIR = OUR_DIR + "/backup/";
 
     final static Pattern RE_APKNAME = Pattern.compile("(.+)\\.apk$");
 
     final static String RES_FILE = "resources.arsc";
 
     final static String PATCHED_INFO = "i18n.bel";
 
     final static String UTF8 = "UTF-8";
 
     private List<String[]> mounts;
 
     private String replacerPath;
 
     public LocalStorage() throws Exception {
         BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/mounts"), "UTF-8"));
         try {
             String s;
             mounts = new ArrayList<String[]>();
             while ((s = rd.readLine()) != null) {
                 mounts.add(s.split("\\s+"));
             }
         } finally {
             Utils.mustClose(rd);
         }
     }
 
     public void extractReplacer(Resources resources, ApplicationInfo applicationInfo) throws Exception {
         String CPU_ABI = Build.CPU_ABI;
         MyLog.log("## CPU_ABI = " + CPU_ABI);
         String CPU_ABI2;
         try {
             CPU_ABI2 = (String) Build.class.getDeclaredField("CPU_ABI2").get(null);
         } catch (Exception e) {
             CPU_ABI2 = "none";
         }
         MyLog.log("## CPU_ABI2 = " + CPU_ABI2);
 
         int rid = Utils.getRawResourceById("replacer_" + CPU_ABI.replace('-', '_'));
         if (rid == 0) {
             rid = Utils.getRawResourceById("replacer_" + CPU_ABI2.replace('-', '_'));
         }
         if (rid == 0) {
             throw new Exception("Unknown architecture");
         }
 
         File outFile = new File(applicationInfo.dataDir, "replacer");
         InputStream in = null;
         OutputStream out = null;
 
         try {
             in = resources.openRawResource(rid);
             out = new FileOutputStream(outFile);
             Utils.copy(in, out);
         } finally {
             Utils.mustClose(in);
             Utils.mustClose(out);
         }
         JniWrapper.chmod(outFile, 0755);
         replacerPath = outFile.getPath();
     }
 
     public List<FileInfo> getLocalFiles() throws Exception {
         final List<FileInfo> files = new ArrayList<FileInfo>();
         byte[] packagesXml = ExecSu.cat(new File("/data/system/packages.xml"));
         InputStream in = new ByteArrayInputStream(packagesXml);
         try {
             Xml.parse(in, Xml.Encoding.UTF_8, new DefaultHandler() {
                 public void startElement(String uri, String localName, String qName, Attributes attributes)
                         throws SAXException {
                     if ("package".equals(localName)) {
                         String name = attributes.getValue("name");
                         String codePath = attributes.getValue("codePath");
                         if (name != null && codePath != null) {
                            FileInfo fi = new FileInfo(new File(codePath));
                            fi.packageName = name;
                            files.add(fi);
                         }
                     }
                 }
 
             });
         } finally {
             in.close();
         }
 
         return files;
     }
 
     private void appendApks(File dir, List<File> out, FileFilter filter) {
         File[] files = dir.listFiles(filter);
         if (files != null) {
             for (File f : files) {
                 out.add(f);
             }
         }
     }
 
     public File patchFileToTemp(File f, byte[] translatedResources) throws Exception {
         File fo = new File(OUR_DIR, "tmp");
         fo.getParentFile().mkdirs();
         if (fo.exists()) {
             if (!fo.delete()) {
                 throw new Exception("Error remove " + fo.getPath());
             }
         }
         new ApkUpdater().replace(f, fo, translatedResources);
 
         fo.setLastModified(f.lastModified());
         return fo;
     }
 
     public void moveToOriginal(File newFile, File origFile, Permissions perms) throws Exception {
         long free = JniWrapper.getSpaceNearFile(origFile);
         String mode;
         if (free < newFile.length()) {
             // less space than required
             MyLog.log("## moveToOriginal - less space");
             mode = "write";
         } else {
             // more than required
             MyLog.log("## moveToOriginal - more space");
             mode = "rename";
         }
         ExecSu.exec(replacerPath + " " + mode + " '" + newFile.getPath() + "' '" + origFile.getPath() + "'");
     }
 
     public boolean isFilesEquals(File f1, File f2) throws IOException {
         if (f1.length() != f2.length()) {
             return false;
         }
         boolean equals = true;
         DataInputStream in1 = null;
         DataInputStream in2 = null;
         byte[] buf1 = new byte[BUFFER_SIZE], buf2 = new byte[BUFFER_SIZE];
         try {
             in1 = new DataInputStream(new FileInputStream(f1));
             in2 = new DataInputStream(new FileInputStream(f2));
             long leave = f1.length();
             while (leave > 0) {
                 int partSize = (int) Math.min(BUFFER_SIZE, leave);
                 in1.readFully(buf1, 0, partSize);
                 in2.readFully(buf2, 0, partSize);
                 if (!Arrays.equals(buf1, buf2)) {
                     equals = false;
                     break;
                 }
                 leave -= partSize;
             }
         } finally {
             Utils.mustClose(in1);
             Utils.mustClose(in2);
         }
         return equals;
     }
 
     public StatFs getFreeSpaceForBackups() throws Exception {
         return new StatFs(BACKUP_PARTITION);
     }
 
     public Permissions getPermissions(FileInfo fi) throws Exception {
         Permissions p = new Permissions();
         p.file = new FilePerm();
         p.file.path = fi.localFile.getPath();
 //        JniWrapper.getPermissions(p.file);
 //        p.file.perm ^= 0100000;
 //        if (p.file.perm < 0 || p.file.perm > 0777) {
 //            throw new Exception("Invalid permission for " + fi.localFile + ": " + Long.toOctalString(p.file.perm));
 //        }
 //        p.dir = new FilePerm();
 //        p.dir.path = fi.localFile.getParent();
 //        JniWrapper.getPermissions(p.dir);
 //        p.dir.perm ^= 0040000;
 //        if (p.dir.perm < 0 || p.dir.perm > 0777) {
 //            throw new Exception("Invalid permission for " + fi.localFile + ": " + Long.toOctalString(p.dir.perm));
 //        }
 
         for (String[] m : mounts) {
             if ("/".equals(m[1])) {
                 // root
                 continue;
             }
             if (p.file.path.startsWith(m[1] + '/')) {
                 if (p.mountPoint != null && p.mountPoint.length() > m[1].length()) {
                     continue;
                 }
                 p.mountDevice = m[0];
                 p.mountPoint = m[1];
                 p.mountType = m[2];
                 p.mountOptions = m[3];
 
                 for (String o : p.mountOptions.split(",")) {
                     if (o.equals("ro")) {
                         p.mountedRO = true;
                     }
                     if (o.startsWith("uid=")) {
                         p.mountedUid = true;
                     }
                 }
             }
         }
 
         if (p.mountDevice == null) {
             throw new Exception("Partition not defined for " + fi.localFile.getPath());
         }
 
         return p;
     }
 
     private void storeInfo() throws Exception {
         MyLog.log(ExecSu.execEvenFail("mount"));
         MyLog.log(ExecSu.execEvenFail("df"));
         MyLog.log(ExecSu.execEvenFail("ls -l /system/app/"));
         MyLog.log(ExecSu.execEvenFail("ls -l /system/framework/"));
         MyLog.log(ExecSu.execEvenFail("ls -l /data/app/"));
         MyLog.log("## maxJavaMemory: " + Utils.textSize(Runtime.getRuntime().maxMemory()));
     }
 
     public void backupApk(FileInfo fi) throws Exception {
         File outFile = new File(BACKUP_DIR, fi.packageName + ".apk");
         outFile.getParentFile().mkdirs();
 
         FileOutputStream out = new FileOutputStream(outFile);
         try {
             FileInputStream in = new FileInputStream(fi.localFile);
             try {
                 Utils.copy(in, out);
             } finally {
                 in.close();
             }
         } finally {
             out.close();
         }
     }
 
     public boolean isFileTranslated(File f) throws Exception {
         ZipFile zip = new ZipFile(f);
         try {
             ZipEntry en = zip.getEntry(ApkUpdater.MARK_NAME);
             return en != null;
         } finally {
             zip.close();
         }
     }
 
     /**
      * Check if 'su' can be executed, i.e. if we have root priviledges.
      */
     public boolean checkSu() {
         try {
             storeInfo();
             ExecSu.exec("echo 0");
             return true;
         } catch (Exception ex) {
             return false;
         }
     }
 
     public void openFileAccess(Permissions p) throws Exception {
         if (p.mountedRO) {
             ExecSu.exec("mount -o remount,rw" + " -t " + p.mountType + " " + p.mountDevice + " '" + p.mountPoint + "'");
         }
 //        if (!p.mountedUid) {
 //            ExecSu.exec("chmod 777 '" + p.dir.path + "'");
 //            ExecSu.exec("chmod 666 '" + p.file.path + "'");
 //        }
     }
 
     public void closeFileAccess(Permissions p) throws Exception {
 //        ExecSu.exec("chmod " + Long.toOctalString(p.dir.perm) + " '" + p.dir.path + "'");
 //        ExecSu.exec("chmod " + Long.toOctalString(p.file.perm) + " '" + p.file.path + "'");
 //        ExecSu.exec("chown " + p.file.owner + "." + p.file.group + " '" + p.file.path + "'");
         if (p.mountedRO) {
             ExecSu.execEvenFail("mount -o remount," + p.mountOptions + " -t " + p.mountType + " " + p.mountDevice
                     + " '" + p.mountPoint + "'");
         }
     }
 
     public void setFileAccess(Permissions p, File f) throws Exception {
         ExecSu.exec("chmod " + Long.toOctalString(p.file.perm) + " '" + f.getPath() + "'");
         ExecSu.exec("chown " + p.file.owner + "." + p.file.group + " '" + f.getPath() + "'");
     }
 
     private void exec10su(String cmd) throws Exception {
   int tries = 0;
         while (true) {
             try {
                 ExecSu.exec(cmd);
                 break;
             } catch (Exception ex) {
                 tries++;
                 if (tries < 10) {
                     Thread.sleep(2000);
                     continue;
                 }
                 throw new Exception("Error execute su: " + ex.getMessage());
             }
         }
     }
 
     public static class Permissions {
         public FilePerm file, dir;
         public String mountPoint, mountDevice, mountOptions, mountType;
         public boolean mountedRO, mountedUid;
     }
 }
