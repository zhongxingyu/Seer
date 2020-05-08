 package net.bodz.redist.installer.builtins;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import net.bodz.bas.c.java.io.FileDirs;
 import net.bodz.bas.c.java.io.FileFinder;
import net.bodz.bas.c.java.io.FilePath;
 import net.bodz.bas.c.java.io.IOConfig;
 import net.bodz.bas.c.java.io.PruneFileFilter;
 import net.bodz.redist.installer.AbstractComponent;
 import net.bodz.redist.installer.Attachment;
 import net.bodz.redist.installer.ISession;
 import net.bodz.redist.installer.InstallException;
 
 public class FileCopy
         extends AbstractComponent {
 
     private final String baseName;
     private final String basePath;
 
     // private File localBase;
     private final String removal;
     private final Collection<File> files;
 
     /** pre-construct the full path to the base dir. */
     private final boolean preConstruct = true;
 
     /** <code>false</code> to exclude empty parent directories */
     private final boolean dirStruct = true;
 
     public FileCopy(String baseName, String basePath, File localBase, File... files) {
         this(baseName, basePath, localBase, Arrays.asList(files));
     }
 
     public FileCopy(String baseName, String basePath, File[] files) {
         this(baseName, basePath, null, Arrays.asList(files));
     }
 
     public FileCopy(String baseName, String basePath, FileFinder finder)
             throws IOException {
        this(baseName, basePath, FilePath.findBase(finder.getStart()), finder.listFiles());
     }
 
     /**
      * @param basePath
      *            <code>base-variable/sub/path...</code>, must use / as the file separator.
      * @param localBase
      *            used to construct the subpath of local files, by remove localBase from local
      *            paths. using <code>null</code> to ignore the directory names.
      */
     public FileCopy(String baseName, String basePath, File localBase, Collection<File> files) {
         if (baseName == null)
             throw new NullPointerException("baseName");
         if (files == null)
             throw new NullPointerException("files");
 
         this.baseName = baseName;
         this.basePath = basePath;
 
         // this.localBase = localBase;
         String removal = null;
         if (localBase != null) {
             removal = localBase.getPath();
             if (localBase.isDirectory())
                 removal += File.separator;
         }
 
         this.removal = removal;
         this.files = files;
 
         setSelected(true);
     }
 
     @Override
     public String toString() {
         StringBuilder buf = new StringBuilder(files.size() * 50);
         buf.append("FileCopy(" + getName() + ")\n");
         buf.append("  base=" + baseName //
                 + (basePath == null ? "" : ("::" + basePath)) + "\n");
         buf.append("  removal=" + removal + "\n");
         buf.append("  count=" + files.size() + "\n");
         return buf.toString();
     }
 
     Attachment getAttachment(ISession session, boolean autoCreate) {
         String id = getId();
         String aname = baseName + "/" + id + ".zip";
         Attachment a;
         try {
             a = session.getAttachment(aname, autoCreate);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return a;
     }
 
     @Override
     public long getSize() {
         FileCopyData data = (FileCopyData) getRegistryData();
         if (data == null)
             return 0;
         return data.size;
     }
 
     @Override
     public CJob pack(ISession session) {
         return new СPack(session);
     }
 
     @Override
     public CJob install(ISession session) {
         return new CInstall(session);
     }
 
     @Override
     public CJob uninstall(ISession session) {
         return new CUninstall(session);
     }
 
     private static class NoSVN
             implements PruneFileFilter {
         @Override
         public boolean accept(File pathname) {
             if (".svn".equals(pathname.getName().toLowerCase()))
                 return false;
             return true;
         }
     }
 
     public static final FileFilter NoSVN = new NoSVN();
 
     // file-system dependent.
     static final int dirFileSize = 0;
 
     class СPack
             extends CJob {
 
         public СPack(ISession session) {
             super(session);
         }
 
         @Override
         protected void _run() {
             int count = files.size();
             List<String> list = new ArrayList<String>(count);
             long sum = 0;
 
             Attachment a = getAttachment(session, true);
             logger.infof(tr._("Pack %s files to %s\n"), getId(), a);
 
             ZipOutputStream zout;
             try {
                 zout = a.getZipOut();
             } catch (IOException e) {
                 throwException(e);
                 return;
             }
 
             setProgressSize(count);
             int index = 0;
             for (File f : files) {
                 if (!moveOn(index++))
                     break;
                 String path = f.getPath();
                 if (f.isDirectory())
                     if (!dirStruct)
                         continue;
                     else
                         path += File.separator;
                 String dest;
                 if (removal == null)
                     dest = f.getName();
                 else {
                     // fix: see "path += File.separator" above.
                     if (!path.startsWith(removal)) {
                         String mesg = tr._("File not within the prefix ") + removal + ": " + path;
                         if (recoverException(new InstallException(mesg)))
                             continue; // next file
                         return;
                     }
                     dest = path.substring(removal.length());
                     // zip entry is dir-entry, only if path ends with '/'.
                     // but escapes..?
                     dest = dest.replace(File.separatorChar, '/');
                     if (dest.isEmpty())
                         dest = "/";
                 }
                 if (preConstruct) {
                     if (basePath != null)
                         dest = basePath + "/" + dest;
                 }
                 try {
                     if (f.isDirectory()) {
                         assert dest.endsWith("/");
                         logger.log(tr._("Put entry "), dest);
                         JarEntry entry = new JarEntry(dest);
                         zout.putNextEntry(entry);
                         zout.closeEntry();
                         sum += dirFileSize;
                     } else if (f.isFile()) {
                         long fileSize = f.length();
                         logger.logf(tr._("Put entry %s (size=%d) "), dest, fileSize);
                         JarEntry entry = new JarEntry(dest);
                         // entry.setSize(fileSize);
                         zout.putNextEntry(entry);
                         long remaining = fileSize;
                         int blockSize = IOConfig.readBlockSize;
                         byte[] block = new byte[blockSize];
                         FileInputStream in = new FileInputStream(f);
                         try {
                             while (remaining > 0) {
                                 // progress...?
                                 int cb = Math.min(blockSize, (int) remaining);
                                 cb = in.read(block, 0, cb);
                                 if (cb == -1)
                                     break;
                                 zout.write(block, 0, cb);
                                 remaining -= cb;
                             }
                         } finally {
                             in.close();
                             zout.closeEntry();
                         }
                         if (remaining != 0)
                             logger.warnf("Incorrect file size %d, %d more bytes doesn't exist\n", fileSize, remaining);
                         sum += fileSize;
                     } else {
                         logger.warn(tr._("Ignored file of unknown type: "), f);
                         continue;
                     }
                     list.add(dest);
                 } catch (IOException e) {
                     if (!recoverException(e))
                         return;
                 }
             } // for file
             FileCopyData data = new FileCopyData();
             data.list = list.toArray(new String[0]);
             data.size = sum;
             setRegistryData(data);
         }
 
     }
 
     boolean autoMkdirs = true;
 
     class CInstall
             extends CJob {
 
         public CInstall(ISession session) {
             super(session);
         }
 
         @Override
         protected void _run() {
             File baseDir = session.getFile(baseName);
             if (!preConstruct)
                 if (basePath != null)
                     baseDir = new File(baseDir, basePath);
             logger.infof(tr._("Install %s files to %s\n"), getId(), baseDir);
             FileCopyData data = (FileCopyData) getRegistryData();
             if (data == null || data.list == null)
                 throw new IllegalStateException(tr._("Missing registry data, which contains the file list to copy."));
 
             setProgressSize(data.list.length);
 
             Attachment a = getAttachment(session, false);
             ZipFile zipFile;
             try {
                 zipFile = a.getZipFile();
             } catch (IOException e) {
                 throwException(e);
                 return;
             }
             int index = 0;
             try {
                 for (String name : data.list) {
                     if (!moveOn(index++))
                         break;
                     ZipEntry entry = zipFile.getEntry(name);
                     if (entry == null) {
                         InstallException ex = new InstallException(tr._("Entry isn\'t existed: ") + name);
                         if (recoverException(ex))
                             continue;
                         break;
                     }
                     boolean isdir = false;
                     if (name.endsWith("/")) {
                         isdir = true;
                         name = name.substring(0, name.length() - 1);
                     }
                     File destFile = new File(baseDir, name);
                     if (isdir) {
                         if (autoMkdirs)
                             destFile.mkdirs();
                         else
                             destFile.mkdir();
                         continue;
                     }
                     if (autoMkdirs) {
                         File destParentDir = destFile.getParentFile();
                         if (!destParentDir.isDirectory()) {
                             logger.log(tr._("Create  directory "), destParentDir, "/");
                             destParentDir.mkdirs();
                         }
                     }
                     logger.log(tr._("Extract "), destFile);
                     FileOutputStream destOut = null;
                     try {
                         InputStream entryIn = zipFile.getInputStream(entry);
                         destOut = new FileOutputStream(destFile);
                         long remaining = entry.getSize();
                         int blockSize = IOConfig.readBlockSize;
                         byte[] block = new byte[blockSize];
                         while (remaining > 0) {
                             int cb = Math.min(blockSize, (int) remaining);
                             cb = entryIn.read(block, 0, cb);
                             if (cb == -1)
                                 throw new IOException(tr._("Unexpected end of file: ") + name);
                             destOut.write(block, 0, cb);
                             remaining -= cb;
                         }
                     } catch (IOException e) {
                         if (!recoverException(e))
                             break;
                     } finally {
                         if (destOut != null)
                             try {
                                 destOut.close();
                             } catch (IOException e) {
                                 logger.warn(tr._("Can\'t close dest file "), destFile);
                             }
                     }
                 }
             } finally {
                 try {
                     zipFile.close();
                 } catch (IOException e) {
                 }
             }
         }
     }
 
     class CUninstall
             extends CJob {
 
         public CUninstall(ISession session) {
             super(session);
         }
 
         @Override
         protected void _run() {
             File baseDir = session.getFile(baseName);
             if (!preConstruct) {
                 if (basePath != null)
                     baseDir = new File(baseDir, basePath);
             }
             logger.infof(tr._("Remove %s files from %s\n"), getId(), baseDir);
             Attachment a = getAttachment(session, false);
             ZipInputStream zin = null;
             try {
                 zin = a.getZipIn();
             } catch (IOException e) {
                 throwException(new InstallException(tr._("Failed to read from ") + a, e));
                 return;
             }
             List<String> names = new ArrayList<String>();
             ZipEntry entry;
             try {
                 while ((entry = zin.getNextEntry()) != null)
                     names.add(entry.getName());
                 zin.close();
             } catch (IOException e) {
                 throwException(new InstallException(tr._("Failed to list the archive"), e));
                 return;
             } finally {
                 try {
                     zin.close();
                 } catch (IOException e) {
                     // log..?
                 }
             }
             Collections.reverse(names);
             for (String name : names) {
                 boolean isdir = name.endsWith("/");
                 if (isdir)
                     name = name.substring(0, name.length() - 1);
                 File dest = new File(baseDir, name);
                 if (!dest.exists()) {
                     logger.log(tr._("File doesn\'t exist: "), dest);
                     continue;
                 }
                 if (isdir) {
                     logger.log(tr._("Delete directory "), dest);
                     dest.delete();
                     continue;
                 }
                 logger.log(tr._("Delete file "), dest);
                 if (dest.delete())
                     FileDirs.removeEmptyParents(dest.getParentFile(), baseDir);
                 else {
                     logger.logf(tr._("Can\'t delete file %s, try at exit\n"), dest);
                     dest.deleteOnExit();
                     session.getFlags().set(ISession.REBOOT);
                 }
             }
         }
     }
 
 }
