 package com.jsync;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class ResourceCopier {
 
     public static void copy(S3Resource in, S3Resource out) throws IOException {
         System.out.println(" + remote copy: " + in.getKey() + " -> " + out.getKey());
         in.copy(out);
     }
 
     public static void copy(IResource in, IResource out) throws IOException {
         if (in instanceof S3Resource && out instanceof S3Resource) {
             ResourceCopier.copy((S3Resource) in, (S3Resource) out);
         } else {
 
             out.mkdirs();
 
             int uid = in.getUid();
             int gid = in.getGid();
             int mode = in.getMode();
             long atime = in.getAccessTime();
             long mtime = in.getModifiedTime();
 
             if (uid != -1)   out.setUid(uid);
             if (gid != -1)   out.setGid(gid);
             if (mode != -1)  out.setMode(mode);
             if (atime != -1) out.setAccessTime(atime);
             if (mtime != -1) out.setModifiedTime(mtime);
 
             out.setMimeType(in.getMimeType());
             out.setSize(in.getSize());
 
             if (in.isSymlink()) {
                 if (!out.exists() || !in.getSymlinkTarget().equals(out.getSymlinkTarget())) {
                     System.out.println(" + link " + in + " -> " + out);
                     out.createSymlink(in.getSymlinkTarget());
                     out.finishWrite();
                 }
             } else {
                 System.out.println(" + copy: " + in + " -> " + out);
 
                 InputStream reader = in.getReader();
                 OutputStream writer = out.getWriter();
                 if (reader != null && writer != null) {
                     byte[] buf = new byte[1024];
                     int c;
                     while ((c = reader.read(buf)) != -1) {
                         if (c > 0) writer.write(buf, 0, c);
                     }
                     writer.flush();
                     reader.close();
                     writer.close();
                     out.finishWrite();
                 }
             }
         }
     }
 
     private static boolean filesDiffer(IResource f1, IResource f2) {
         boolean datesDiffer = (f1.lastModified() > f2.lastModified() && f1.getModifiedTime() > f2.getModifiedTime());
         return (datesDiffer || f1.getSize() != f2.getSize());
     }
 
     public static void performDeletions(IResource source, IResource destination, IResource backup) {
         if (destination.isDirectory()) {
             for (IResource resource : destination.list()) {
                 IResource counterpart = source.join(resource.getName());
                System.out.println(counterpart);
                 IResource backupTo = null;
                 if (backup != null) {
                     backupTo = backup.join(resource.getName());
                 }
                 if (!counterpart.exists()) {
                     if (backupTo != null) {
                         if (resource.isFile()) {
                             try {
                                 copy(resource, backupTo);
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                         } else {
                             backupTo.mkdirs();
                         }
                     }
                     if (resource.isDirectory()) {
                         performDeletions(counterpart, resource, backupTo);
                     } else {
                         System.out.println(" - deleting: " + resource);
                         resource.delete();
                     }
                 } else {
                     if (resource.isDirectory()) {
                         performDeletions(counterpart, resource, backupTo);
                     }
                 }
             }
         }
     }
 
     public static void copyDirectory(IResource source, IResource destination, IResource backupPath) throws IOException {
         if (source.isDirectory()) {
             if (!destination.exists()) destination.mkdirs();
             //System.out.println("copy directory: " + source + " -> " + destination);
             for (IResource resource : source.list()) {
                 if (resource.isDirectory()) {
                     IResource nbackup = null;
                     if (backupPath != null) {
                         nbackup = backupPath.join(resource.getName());
                     }
                     if (resource.isSymlink()) {
                         IResource nlink = destination.join(resource.getName());
                         if (!nlink.exists() || !nlink.getSymlinkTarget().equals(resource.getSymlinkTarget())) {
                             copy(resource, nlink);
                         }
                     } else {
                         ResourceCopier.copyDirectory(resource, destination.join(resource.getName()), nbackup);
                     }
                 } else {
                     IResource realdest = destination.join(resource.getName());
                     if (ResourceCopier.filesDiffer(resource, realdest)) {
                         if (!resource.isSymlink() && backupPath != null && realdest.exists()) {
                             if (!backupPath.exists()) backupPath.mkdirs();
                             IResource realbackup = backupPath.join(realdest.getName());
                             ResourceCopier.copy(realdest, realbackup);
                         }
                         ResourceCopier.copy(resource, realdest);
                     }
                 }
             }
         }
     }
 }
