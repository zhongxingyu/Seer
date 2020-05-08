 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.prerelease.core;
 
 import net.oneandone.maven.plugins.prerelease.util.ChangesXml;
 import net.oneandone.sushi.fs.DirectoryNotFoundException;
 import net.oneandone.sushi.fs.FileNotFoundException;
 import net.oneandone.sushi.fs.ListException;
 import net.oneandone.sushi.fs.MkfileException;
 import net.oneandone.sushi.fs.OnShutdown;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.xml.XmlException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 /**
  * The archive stores prereleases for one given groupId/artifactId.
  *
  * ~/.m2/prereleases          <- default storage for all archives. This is the primary storage, swap also uses a secondary storage.
  *   groupId/artifactId.LOCK  <- optional, indicates that a process operates on this archive
  *   groupId/artifactId/      <- archive directory
  *     |- revision1           <- prerelease directory, ready to promote; promoting the prerelease removes this directory
  *     |     |- tags
  *     |     |    - <tagname>
  *     |     |- artifacts
  *     |      - prerelease.properties
  *     |- revision2
  *     :
  *     |- REMOVE              <- a renamed prerelease directory
  *     :     :                   optional - only when the last create call failed (because the mvn call failed) or promote succeeded
  *     :     |- CAUSE         <- why this directory is to be removed
  *     :     :
  *
  *  Prerelease directories have the svn revision number as their directory name; it may be a symlink to an arbitrary location
  *
  */
 public class Archive implements AutoCloseable {
     public static List<FileNode> directories(List<FileNode> storages, MavenProject project) {
         List<FileNode> directories;
 
         directories = new ArrayList<>(storages.size());
         for (FileNode storage : storages) {
             directories.add(storage.join(project.getGroupId(), project.getArtifactId()));
         }
         return directories;
     }
 
     public static Archive tryOpen(List<FileNode> directories) {
         try {
             return open(directories, -1, null);
         } catch (IOException e) {
             return null;
         }
     }
 
     public static Archive open(List<FileNode> directories, int timeout, Log log) throws IOException {
         Archive archive;
 
         archive = new Archive(directories);
         archive.open(timeout, log);
         return archive;
     }
 
     private final List<FileNode> directories;
     private boolean opened = false;
     private boolean closed = false;
 
     private Archive(List<FileNode> directories) {
         if (directories.size() == 0) {
             throw new IllegalArgumentException();
         }
         this.directories = directories;
     }
 
     public Target target(long revision) {
         String name;
         FileNode prerelease;
 
         name = Long.toString(revision);
        for (int i = directories.size() - 1; i >= 0; i++) {
             prerelease = directories.get(i).join(name);
             if (i == 0 || prerelease.exists()) {
                 return new Target(prerelease, revision);
             }
         }
         throw new IllegalStateException();
     }
 
     /** */
     public TreeMap<Long, FileNode> list() throws ListException, DirectoryNotFoundException {
         TreeMap<Long, FileNode> result;
         long revision;
 
         result = new TreeMap<>();
         for (FileNode directory : directories) {
             if (directory.exists()) {
                 for (FileNode prerelease : directory.list()) {
                     if (!prerelease.getName().equals(Target.REMOVE)) {
                         revision = Long.parseLong(prerelease.getName());
                         result.put(revision, prerelease);
                     }
                 }
             }
         }
         return result;
     }
 
     public long latest() throws ListException, DirectoryNotFoundException {
         return list().lastKey();
     }
 
     //--
 
     private FileNode lockFile() {
         FileNode primary;
 
         primary = directories.get(0);
         return primary.getParent().join(primary.getName() + ".LOCK");
     }
 
     /**
      * @param timeout in seconds; -1 to try only once and never wait.
      * @param log may be null
      */
     private void open(int timeout, Log log) throws IOException {
         FileNode file;
         int seconds;
 
         if (opened) {
             throw new IllegalStateException();
         }
         file = lockFile();
         try {
             seconds = 0;
             while (true) {
                 // every time - if someone wiped the primary storage directory
                 file.getParent().mkdirsOpt();
                 try {
                     file.mkfile();
                     OnShutdown.get().deleteAtExit(file);
                     opened = true;
                     file.writeString(Integer.toString(pid()));
                     if (log != null) {
                         log.debug("locked for pid " + pid());
                     }
                     return;
                 } catch (MkfileException e) {
                     if (seconds > timeout) {
                         if (log != null) {
                             log.warn("Lock timed out after " + seconds + "s.");
                         }
                         throw e;
                     }
                     if (seconds % 10 == 0) {
                         if (log != null) {
                             log.info("Waiting for " + file + ": " + seconds + "s");
                             log.debug(e);
                         }
                     }
                     seconds++;
                     Thread.sleep(1000);
                 }
             }
         } catch (InterruptedException e) {
             if (log != null) {
                 log.warn("interrupted");
             }
         }
     }
 
     @Override
     public void close() throws Exception {
         FileNode file;
 
         if (!opened) {
             throw new IllegalStateException("not opened");
         }
         if (closed) {
             throw new IllegalStateException("already closed");
         }
         file = lockFile();
         file.deleteFile();
         // because another thread waiting for this lock might create this file again.
         // The shutdown hook must not delete the file created by this other thread.
         OnShutdown.get().dontDeleteAtExit(file);
         closed = true;
     }
 
     //--
 
     private static int pid = 0;
 
     public static int pid() {
         String str;
         int idx;
 
         if (pid == 0) {
             // see http://blog.igorminar.com/2007/03/how-java-application-can-discover-its.html?m=1
             str = ManagementFactory.getRuntimeMXBean().getName();
             idx = str.indexOf('@');
             if (idx == -1) {
                 throw new IllegalStateException("cannot guess pid from " + str);
             }
             pid = Integer.parseInt(str.substring(0, idx));
         }
         return pid;
     }
 
     /** @return true when a changes file was found */
     public static boolean adjustChangesOpt(FileNode workingCopy, String version) throws XmlException, IOException, SAXException {
         ChangesXml changes;
 
         try {
             changes = ChangesXml.load(workingCopy);
         } catch (FileNotFoundException e) {
             return false;
         }
         changes.releaseDate(version, new Date());
         changes.save();
         return true;
     }
 
     /**
      * @param keep number of prereleases after this method
      */
     public void wipe(int keep) throws IOException {
         TreeMap<Long, FileNode> prereleases;
         FileNode d;
 
         if (keep < 1) {
             throw new IllegalArgumentException("keep " + keep);
         }
         for (FileNode directory : directories) {
             d = directory.join(Target.REMOVE);
             if (d.isDirectory()) {
                 d.deleteTree();
             }
         }
         prereleases = list();
         while (prereleases.size() > keep) {
             prereleases.remove(prereleases.firstKey()).deleteTree();
         }
     }
 }
