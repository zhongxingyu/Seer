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
 
 import net.oneandone.maven.plugins.prerelease.util.Subversion;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.launcher.Failure;
 import net.oneandone.sushi.util.Strings;
 import net.oneandone.sushi.xml.Selector;
 import net.oneandone.sushi.xml.XmlException;
 import org.apache.maven.plugin.logging.Log;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 public class WorkingCopy {
     /** TODO: memory consumption */
     public static WorkingCopy load(FileNode workingCopy) throws IOException, SAXException, XmlException {
         World world;
         String output;
         Document doc;
         String path;
         Selector selector;
         Element wcStatus;
         List<FileNode> modifications;
         SortedSet<Long> revisions;
         SortedSet<Long> changes;
         // maps paths to the change revision
         Map<String, Long> maybePendings;
         List<String> pendings;
         long revision;
         long change;
         String props;
 
         output = Subversion.launcher(workingCopy, "--xml", "-v", "--show-updates", "status").exec();
         world = workingCopy.getWorld();
         doc = world.getXml().getBuilder().parseString(output);
         selector = world.getXml().getSelector();
         modifications = new ArrayList<>();
         revisions = new TreeSet<>();
         changes = new TreeSet<>();
         maybePendings = new HashMap<>();
         for (Element entry : selector.elements(doc, "status/target/entry")) {
             path = entry.getAttribute("path");
             wcStatus = selector.element(entry, "wc-status");
             props = wcStatus.getAttribute("props");
             if ("normal".equals(wcStatus.getAttribute("item")) && ("none".equals(props) || "normal".equals(props))) {
                 revision = Long.parseLong(wcStatus.getAttribute("revision"));
                 revisions.add(revision);
                 change = Long.parseLong(selector.element(wcStatus, "commit").getAttribute("revision"));
                 changes.add(change);
                 if (selector.elementOpt(entry, "repos-status") != null) {
                     maybePendings.put(entry.getAttribute("path"), change);
                 }
             } else {
                 modifications.add(workingCopy.join(path));
             }
         }
         if (changes.size() == 0) {
            throw new IOException("Cannot determine svn status - is this directory under svn?");
         }
         if (revisions.size() == 0) {
             throw new IllegalStateException();
         }
         if (changes.last() > revisions.last()) {
             throw new IllegalStateException(changes.last() + " vs " + revisions.last());
         }
         pendings = new ArrayList<>();
         for (Map.Entry<String, Long> entry : maybePendings.entrySet()) {
             if (entry.getValue() < revisions.last()) {
                 output = Subversion.launcher(workingCopy, "log", "--xml", "-q", "-r" + (entry.getValue() + 1) + ":" + revisions.last(),
                         entry.getKey()).exec();
                 doc = world.getXml().getBuilder().parseString(output);
                 if (selector.elements(doc, "log/logentry").size() > 0) {
                     pendings.add(entry.getKey());
                 }
             }
         }
         return new WorkingCopy(workingCopy, modifications, revisions, changes, pendings);
     }
 
     //--
 
     public final FileNode directory;
     public final List<FileNode> modifications;
     public final SortedSet<Long> revisions;
     public final SortedSet<Long> changes;
     public final List<String> pendingUpdates;
 
     public WorkingCopy(FileNode directory, List<FileNode> modifications, SortedSet<Long> revisions, SortedSet<Long> changes,
                        List<String> pendingUpdates) {
         this.directory = directory;
         this.modifications = modifications;
         this.revisions = revisions;
         this.changes = changes;
         this.pendingUpdates = pendingUpdates;
     }
 
     public long revision() {
         return changes.last();
     }
 
     public void check() throws UncommitedChanges, PendingUpdates {
         if (!modifications.isEmpty()) {
             throw new UncommitedChanges(modifications);
         }
         if (!pendingUpdates.isEmpty()) {
             throw new PendingUpdates(revisions.last(), pendingUpdates);
         }
     }
 
     public Descriptor checkCompatibility(Descriptor descriptor) throws Exception {
         String svnurlWorkspace;
 
         svnurlWorkspace = Subversion.workspaceUrl(directory);
         svnurlWorkspace = Strings.removeRightOpt(svnurlWorkspace, "/");
         if (!svnurlWorkspace.equals(descriptor.svnOrig)) {
             throw new SvnUrlMismatch(svnurlWorkspace, descriptor.svnOrig);
         }
         if (revision() != descriptor.revision) {
             throw new RevisionMismatch(revision(), descriptor.revision);
         }
         return descriptor;
     }
 
     public void update(Log log) throws Failure {
         log.info(Subversion.launcher(directory, "update").exec());
     }
 }
