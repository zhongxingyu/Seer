 /*
  * Copyright 2009 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.hooks.FileParsePostProcessor;
 
 import org.apache.oro.io.GlobFilenameFilter;
 import org.apache.oro.text.GlobCompiler;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 
 /**
  * Handles the resolution of glob paths (*.js) and relative paths.
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class PathResolver {
 
   private final PathRewriter pathRewriter;
   private final Set<FileParsePostProcessor> processors;
   private final File basePath;
 
   @Inject
   public PathResolver(@Named("basePath") File basePath,
       PathRewriter pathRewriter, Set<FileParsePostProcessor> processors) {
     this.basePath = basePath;
     this.pathRewriter = pathRewriter;
     this.processors = processors;
   }
 
   public String resolvePath(String path) {
     Stack<String> resolvedPath = new Stack<String>();
     String[] tokenizedPath = path.split("/");
 
     for (String token : tokenizedPath) {
       if (token.equals("..")) {
         if (!resolvedPath.isEmpty()) {
           resolvedPath.pop();
           continue;
         }
       }
       resolvedPath.push(token);
     }
     return join(resolvedPath);
   }
 
   private String join(Collection<String> collection) {
     StringBuilder sb = new StringBuilder();
     Iterator<String> iterator = collection.iterator();
 
     if (iterator.hasNext()) {
       sb.append(iterator.next());
 
       while (iterator.hasNext()) {
         sb.append("/");
         sb.append(iterator.next());
       }
     }
     return sb.toString();
   }
   
   private Set<FileInfo> consolidatePatches(Set<FileInfo> resolvedFilesLoad) {
     Set<FileInfo> consolidated = new LinkedHashSet<FileInfo>(resolvedFilesLoad.size());
     FileInfo currentNonPatch = null;
     for (FileInfo fileInfo : resolvedFilesLoad) {
       if (fileInfo.isPatch()) {
         if (currentNonPatch == null) {
           throw new IllegalStateException("Patch " + fileInfo
             + " without a core file to patch");
         }
         currentNonPatch.addPatch(fileInfo);
       } else {
         consolidated.add(fileInfo);
         currentNonPatch = fileInfo;
       }
     }
     return consolidated;
   }
 
   public Set<FileInfo> resolve(Set<FileInfo> unResolvedFiles) {
     Set<FileInfo> resolvedFiles = new LinkedHashSet<FileInfo>();
     for (FileInfo fileInfo : unResolvedFiles) {
       String f = fileInfo.getFileName();
       // TODO(corysmith): Replace path rewriter with hooks.
       f = pathRewriter.rewrite(f);
       if (f.startsWith("http://") || f.startsWith("https://")) {
         resolvedFiles.add(new FileInfo(f, -1, false, false, null));
       } else {
         File file = basePath != null
           ? new File(basePath.getAbsoluteFile(), f) : new File(f);
         File testFile = file.getAbsoluteFile();
         File dir = testFile.getParentFile().getAbsoluteFile();
         final String pattern = file.getName();
         String[] filteredFiles = dir.list(new GlobFilenameFilter(pattern,
           GlobCompiler.DEFAULT_MASK | GlobCompiler.CASE_INSENSITIVE_MASK));
 
         if (filteredFiles == null || filteredFiles.length == 0) {
          String error = "The patterns/paths "
            + f
            + " used in the configuration"
            + " file didn't match any file, the files patterns/paths need to be relative to"
            + " the configuration file.";

          System.err.println(error);
          throw new RuntimeException(error);
         }
         Arrays.sort(filteredFiles, String.CASE_INSENSITIVE_ORDER);
 
         for (String filteredFile : filteredFiles) {
           String resolvedFilePath = resolvePath(dir
             .getAbsolutePath().replaceAll("\\\\", "/")
             + "/" + filteredFile.replaceAll("\\\\", "/"));
           File resolvedFile = new File(resolvedFilePath);
 
           resolvedFiles.add(new FileInfo(resolvedFilePath,
               resolvedFile.lastModified(),
               fileInfo.isPatch(), fileInfo.isServeOnly(), null));
         }
       }
     }
     for (FileParsePostProcessor processor : processors) {
       resolvedFiles = processor.process(resolvedFiles);
     }
     return consolidatePatches(resolvedFiles);
   }
 
   public List<Plugin> resolve(List<Plugin> plugins) {
     List<Plugin> resolved = Lists.newLinkedList();
     for (Plugin plugin : plugins) {
       resolved.add(plugin.getPluginFromPath(resolvePath(plugin.getPathToJar())));
     }
     return resolved;
   }
 }
