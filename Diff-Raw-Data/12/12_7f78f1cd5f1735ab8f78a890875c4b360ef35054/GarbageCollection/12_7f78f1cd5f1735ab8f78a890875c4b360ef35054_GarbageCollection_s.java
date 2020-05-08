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
 package net.oneandone.lavender.publisher;
 
import com.jcraft.jsch.JSchException;
 import net.oneandone.lavender.config.Cluster;
 import net.oneandone.lavender.config.Docroot;
 import net.oneandone.lavender.config.Host;
 import net.oneandone.lavender.config.Net;
 import net.oneandone.lavender.config.Settings;
 import net.oneandone.lavender.index.Index;
 import net.oneandone.lavender.index.Label;
 import net.oneandone.sushi.cli.Console;
 import net.oneandone.sushi.cli.Option;
 import net.oneandone.sushi.cli.Value;
 import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.ssh.SshNode;
import net.oneandone.sushi.io.OS;
import net.oneandone.sushi.util.Strings;
 
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Traverses all docroots to delete unreferenced files and empty directory.
  * References a defined by the lavendelized paths in the indexes.
  */
 public class GarbageCollection extends Base {
     @Value(name = "cluster", position = 1)
     private String clusterName;
 
     @Option("dryrun")
     private boolean dryrun = false;
 
     public GarbageCollection(Console console, Settings settings, Net net) {
         super(console, settings, net);
     }
 
     @Override
     public void invoke() throws IOException {
         Cluster cluster;
         Node hostroot;
         Index index;
         Set<String> references;
         Node docroot;
 
         cluster = net.cluster(clusterName);
         for (Host host : cluster.hosts) {
             hostroot = host.open(console.world);
             for (Docroot docrootObj : cluster.docroots) {
                 docroot = docrootObj.node(hostroot);
                 if (docroot.exists()) {
                     references = new HashSet<>();
                     console.info.println(host);
                     console.info.print("collecting references ...");
                     for (Node file : docrootObj.indexDirectory(hostroot).list()) {
                         index = Index.load(file);
                         for (Label label : index) {
                            if (!references.add(label.getLavendelizedPath())) {
                                throw new IllegalStateException("duplicate path: " + label.getLavendelizedPath());
                            }
                         }
                     }
                     console.info.println("done: " + references.size());
                     gc(docroot, references);
                 }
             }
         }
     }
 
     private void gc(Node base, Set<String> references) throws IOException {
         gcFiles(base, references);
         gcDirectories(base);
     }
 
     private void gcFiles(Node base, Set<String> references) throws IOException {
         List<String> paths;
         int found;
 
         console.info.print("scanning files ...");
         paths = Verify.find(base, "-type", "f");
         console.info.println(" done: " + paths.size());
         found = 0;
         for (String path : paths) {
             if (references.contains(path)) {
                 found++;
             } else {
                 console.verbose.println("rm " + path);
                 if (!dryrun) {
                     base.join(path).deleteFile();
                 }
             }
         }
         if (found != references.size()) {
             throw new IllegalStateException(found + "/" + references.size() + " files found in " + base);
         }
         console.info.println((paths.size() - references.size()) + " unreferenced files deleted.");
     }
 
     private void gcDirectories(Node base) throws IOException {
         List<String> paths;
 
         console.info.print("scanning empty directories ...");
         paths = Verify.find(base, "-type", "d", "-empty");
         console.info.println(" done: " + paths.size());
         for (String path : paths) {
             rmdir(base, base.join(path));
         }
         console.info.println(paths.size() + " empty directories deleted.");
     }
 
     private void rmdir(Node base, Node dir) throws IOException {
         while (true) {
             if (dir.equals(base)) {
                 return;
             }
             console.verbose.println("rmdir " + dir.getPath());
             if (dryrun) {
                 return;
             }
             dir.deleteDirectory();
             dir = dir.getParent();
             if (dir.list().size() > 0) {
                 return;
             }
         }
     }
 }
