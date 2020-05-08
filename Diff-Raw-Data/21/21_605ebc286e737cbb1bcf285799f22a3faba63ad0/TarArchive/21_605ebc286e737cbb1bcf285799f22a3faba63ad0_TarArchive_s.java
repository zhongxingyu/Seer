 /*
  * Copyright (c) KloudTek Ltd 2013.
  */
 
 package com.kloudtek.buildmagic.tools.util;
 
 import com.kloudtek.buildmagic.tools.createinstaller.deb.DebSymlink;
 import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
 import org.apache.commons.compress.archivers.zip.UnixStat;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.types.ArchiveFileSet;
 import org.apache.tools.ant.types.Resource;
 import org.apache.tools.ant.types.ResourceCollection;
 import org.apache.tools.ant.types.TarFileSet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.zip.GZIPOutputStream;
 
 public class TarArchive {
     private HashSet<String> files = new HashSet<String>();
     private final TarArchiveOutputStream stream;
     private final Project project;
 
     public TarArchive(DataBuffer dataBuffer, Project project) throws IOException {
         this.project = project;
         stream = new TarArchiveOutputStream(new GZIPOutputStream(dataBuffer));
         stream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
     }
 
     private void createParent(String filename, String user, String group, Integer filemode) throws IOException {
         if (filename.startsWith("/")) {
             filename = filename.substring(1, filename.length());
         }
         int idx = filename.lastIndexOf('/');
         if (idx != -1) {
             LinkedList<String> stack = new LinkedList<String>();
             stack.add(filename.substring(0, idx));
             while (!stack.isEmpty()) {
                 String file = stack.removeLast();
                 if (!files.contains(file)) {
                     idx = file.lastIndexOf('/');
                     if (idx != -1) {
                         String parent = file.substring(0, idx);
                         if (!files.contains(parent)) {
                             stack.addLast(file);
                             stack.addLast(parent);
                             continue;
                         }
                     }
                     createDir(file, user, group, filemode);
                 }
             }
         }
     }
 
     public void write(byte[] data, String filename) throws IOException {
         createParent(filename, null, null, null);
         files.add(filename);
         ArchiveHelper.write(stream, data, filename);
     }
 
     public void write(DataBuffer dataBuffer, String filename) throws IOException {
         createParent(filename, null, null, null);
         files.add(filename);
         ArchiveHelper.write(stream, dataBuffer, filename);
     }
 
     public void write(String data, String filename) throws IOException {
         createParent(filename, null, null, null);
         files.add(filename);
         ArchiveHelper.write(stream, data.getBytes(), filename);
     }
 
     public void write(InputStream data, int size, String filename, String user, String group, Integer filemode) throws IOException {
         createParent(filename, null, null, null);
         files.add(filename);
         ArchiveHelper.write(stream, data, size, filename, user, group, filemode);
     }
 
     public void createSymlink(String filename, String target) throws IOException {
         createParent(filename, null, null, null);
         files.add(filename);
         ArchiveHelper.createSymlink(stream, filename, target);
     }
 
     public void createDir(String filename) throws IOException {
         createDir(filename, null, null, null);
     }
 
     public void createDir(String filename, String owner, String group, Integer filemode) throws IOException {
         createParent(filename, null, null, null);
         files.add(filename);
         ArchiveHelper.createDir(stream, filename, owner, group, filemode);
     }
 //
 //    public static void main(String[] args) throws IOException {
 //        FileOutputStream file = new FileOutputStream("_tmp/test.tar");
 //        TarArchiveOutputStream s = new TarArchiveOutputStream(file);
 //        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
 //        s.setLongFileMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
 //        TarArchiveEntry dir = new TarArchiveEntry("test", TarConstants.LF_DIR);
 //        dir.setMode(484);
 //        s.putArchiveEntry(dir);
 //        s.closeArchiveEntry();
 //        s.close();
 //    }
 
     public void write(Collection<ResourceCollection> collections) throws IOException {
         for (final ResourceCollection rsCol : collections) {
             final Iterator rsIt = rsCol.iterator();
             while (rsIt.hasNext()) {
                 final Resource rs = (Resource) rsIt.next();
                 final StringBuilder fnBuf = new StringBuilder();
                 if (rsCol instanceof ArchiveFileSet) {
                     fnBuf.append(((ArchiveFileSet) rsCol).getPrefix(project));
                     if (!rs.getName().startsWith(File.separator)) {
                         fnBuf.append(File.separator);
                     }
                 }
                 fnBuf.append(rs.getName());
                 final String filename = fnBuf.toString();
                 String user;
                 String group;
                 Integer filemode = null;
                 Integer dirmode = null;
                 if (rsCol instanceof TarFileSet) {
                     TarFileSet tarSet = (TarFileSet) rsCol;
                     user = tarSet.getUserName();
                     if (user != null && user.length() == 0) {
                         user = null;
                     }
                     group = tarSet.getGroup();
                     if (group != null && group.length() == 0) {
                         group = null;
                     }
                     if (tarSet.hasFileModeBeenSet()) {
                         filemode = tarSet.getFileMode(project) ^ UnixStat.FILE_FLAG;
                     }
                     if (tarSet.hasDirModeBeenSet()) {
                         dirmode = tarSet.getDirMode(project) ^ UnixStat.DIR_FLAG;
                     }
                 } else if (rsCol instanceof DebSymlink) {
                    createSymlink(filename, ((DebSymlink) rsCol).getResource());
                     continue;
                 } else {
                     user = null;
                     group = null;
                 }
                 createParent(filename, user, group, dirmode);
                 write(rs.getInputStream(), (int) rs.getSize(), filename, user, group, rs.isDirectory() ? dirmode : filemode);
             }
         }
     }
 
     public void close() throws IOException {
         stream.close();
     }
 }
