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
 
 import com.google.inject.Inject;
 import com.google.jstestdriver.hooks.FileLoadPostProcessor;
 import com.google.jstestdriver.hooks.FileLoadPreProcessor;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * A simple loader for files.
  * @author corysmith@google.com (Cory Smith)
  */
 public class ProcessingFileLoader implements FileLoader {
   private final JsTestDriverFileFilter filter;
   private final FileReader reader;
   private final Set<FileLoadPostProcessor> postprocessors;
   private final Set<FileLoadPreProcessor> preProcessors;
 
   @Inject
   public ProcessingFileLoader(JsTestDriverFileFilter filter,
                               FileReader reader,
                               Set<FileLoadPostProcessor> postprocessors,
                               Set<FileLoadPreProcessor> preProcessors) {
     this.filter = filter;
     this.reader = reader;
     this.postprocessors = postprocessors;
     this.preProcessors = preProcessors;
   }
 
  //TODO(corysmith): Remove shouldReset.
   public List<FileInfo> loadFiles(Collection<FileInfo> filesToLoad, boolean shouldReset) {
     List<FileInfo> loadedFiles = new LinkedList<FileInfo>();
     try {
       for (FileInfo file : preProcessFiles(filesToLoad)) {
         FileInfo processed = loadFile(shouldReset, file);
         processed = postProcessFile(processed);
         loadedFiles.add(processed);
       }
     } catch (RuntimeException e) {
       e.printStackTrace();
       throw e;
     }
     return loadedFiles;
   }
 
   private FileInfo loadFile(boolean shouldReset, FileInfo file) {
     if (!file.canLoad()) {
       return file;
     }
     StringBuilder fileContent = new StringBuilder();
     long timestamp = file.getTimestamp();
     fileContent.append(filter.filterFile(reader.readFile(file.getFileName()), !shouldReset));
     List<FileInfo> patches = file.getPatches();
     if (patches != null) {
       for (FileInfo patch : patches) {
         fileContent.append(reader.readFile(patch.getFileName()));
       }
     }
     return new FileInfo(file.getFileName(),
                         timestamp,
                         false,
                         file.isServeOnly(),
                         fileContent.toString());
   }
 
   private FileInfo postProcessFile(FileInfo processed) {
     for (FileLoadPostProcessor hook : postprocessors) {
       processed = hook.process(processed);
     }
     return processed;
   }
   
   private List<FileInfo> preProcessFiles(Collection<FileInfo> filesToLoad) {
     List<FileInfo> files = new LinkedList<FileInfo>(filesToLoad);
     for (FileLoadPreProcessor processor : preProcessors) {
       files = processor.process(files);
     }
     return files;
   }
 }
