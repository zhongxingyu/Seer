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
 package net.oneandone.maven.plugins.prerelease;
 
 import net.oneandone.maven.plugins.prerelease.core.Archive;
 import net.oneandone.maven.plugins.prerelease.core.Descriptor;
 import net.oneandone.maven.plugins.prerelease.core.Target;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.fs.filter.Filter;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Wipes archives and moves prereleases to the next storage. You usually have two storages, primary and secondary.
  * Useful when you use ram disks: use the ramdisk as primary storage, and a hardisk as secondary storage.
  */
 @Mojo(name = "swap", requiresProject = false)
 public class Swap extends Base {
     @Override
     public void doExecute() throws Exception {
         Set done;
         List<FileNode> storages;
         List<Node> archives;
         Archive archive;
         String relative;
         FileNode dest;
 
         done = new HashSet<>();
         storages = storages();
         for (Node storage : storages()) {
             archives = storage.find("*/*");
             for (Node candidate : archives) {
                 if (!candidate.isDirectory()) {
                     continue;
                 }
                 relative = candidate.getRelative(storage);
                 if (!done.add(relative)) {
                     // already processed
                     continue;
                 }
                 archive = Archive.tryOpen(directories(storages, relative));
                 if (archive == null) {
                     getLog().info("skipped because it is locked: " + relative);
                 } else {
                     try {
                         archive.wipe(keep);
                         for (FileNode src : archive.list().values()) {
                             if (!src.join("prerelease.properties").readString().contains("prerelease=")) {
                                 // This property was introduced in 1.6, together with multiple storage support
                                 getLog().info("skipped -- prerelease version too old");
                                 continue;
                             }
                             dest = nextStorage(storages, src);
                             if (dest == null) {
                                 getLog().info("already in final storage: " + src);
                             } else {
                                dest = dest.join(relative, src.getName());
                                 dest.getParent().mkdirsOpt();
                                 src.move(dest);
                                 getLog().info("swapped " + src.getAbsolute() + " -> " + dest.getAbsolute());
                             }
                         }
                     } finally {
                         archive.close();
                     }
                 }
 
             }
         }
         getLog().info(done.size() + " archives processed.");
     }
 
     private static FileNode nextStorage(List<FileNode> storages, FileNode prerelease) {
         FileNode storage;
 
         for (int i = 0, max = storages.size(); i < max; i++) {
             storage = storages.get(i);
             if (prerelease.hasAnchestor(storage)) {
                 if (i + 1 < max) {
                     return storages.get(i + 1);
                 } else {
                     return null;
                 }
             }
         }
         throw new IllegalStateException(prerelease.getAbsolute());
     }
 
     private static List<FileNode> directories(List<FileNode> storages, String relative) {
         List<FileNode> result;
 
         result = new ArrayList<>();
         for (FileNode storage : storages) {
             result.add(storage.join(relative));
         }
         return result;
     }
 }
