 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.core.util;
 
 import org.apache.myfaces.scripting.loaders.java.util.DirStrategy;
 import org.apache.myfaces.scripting.loaders.java.util.FileStrategy;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class FileUtils {
     static double _tempMarker = Math.random();
 
     /**
      * Get the file separator for this platform.
      *
      * @return The file separator.
      */
     public static String getFileSeparator() {
         return File.separator;
     }
 
     /**
      * Get the file separator for this platform, properly escaped for usage in a regular expression.
     * workaround for http://bugs.sun.com/view_bug.do?bug_id=4626653 another workaround would be
     * to use the Matcher.quoteReplacement as of http://bugs.sun.com/view_bug.do?bug_id=5024613  instead
     * of using String.replaceAll
      *
      * @return The file separator, escaped for in a regex.
      */
     public static String getFileSeparatorForRegex() {
         String sep = getFileSeparator();
 
         if ("\\".equals(sep)) {
             sep = "\\\\";
         }
 
         return sep;
     }
 
     public static File getTempDir() {
         File tempDir;
 
         String baseTempPath = System.getProperty("java.io.tmpdir");
         String tempDirName = "myfaces_compilation_" + _tempMarker;
 
         tempDir = new File(baseTempPath + File.separator + tempDirName);
         while (tempDir.exists()) {
             tempDirName = "myfaces_compilation_" + System.currentTimeMillis() + Math.random();
             tempDir = new File(baseTempPath + File.separator + tempDirName);
         }
 
         synchronized (FileUtils.class) {
             if (tempDir.exists()) {
                 return tempDir;
             }
             if (tempDir.mkdirs()) {
                 tempDir.deleteOnExit();
             }
         }
         return tempDir;
     }
 
     /**
      * we roll our own tree walker here
      * to avoid a dependency into commons fileutils
      * and to apply an easier pattern than
      * commons fileutils uses
      *
      * @param rootDir  the root dir for our walking
      * @param strategy the strategy to apply to for our walking
      */
     public static void listFiles(File rootDir, Strategy strategy) {
         if (!rootDir.isDirectory()) {
             strategy.apply(rootDir);
             return;
         }
 
         //TODO apply a filter here instead of doing the check directly
         File[] files = rootDir.listFiles();
         for (File file : files) {
             boolean isDirectory = file.isDirectory();
             if (isDirectory && !file.getName().endsWith(".")) {
                 listFiles(file, strategy);
                 strategy.apply(file);
             } else if (!isDirectory) {
                 strategy.apply(file);
             }
         }
     }
 
     /**
      * <p>
      * target path check to check if the targetPath is valid or can be created
      * </p>
      *
      * @param path the path to be investigated
      */
     public static void assertPath(File path) {
         // The destination directory must already exist as javac will not create the destination directory.
         if (!path.exists()) {
             if (!path.mkdirs()) {
                 throw new IllegalStateException("It wasn't possible to create the target " +
                         "directory for the compiler ['" + path.getAbsolutePath() + "'].");
             }
 
             // If we've created the destination directory, we'll delete it as well once the application exits
             path.deleteOnExit();
         }
     }
 
     /**
      * fetches recursively the files under the current root
      *
      * @param sourcePath the source path from which the walker should start from
      * @param fileType   the pattern upon which the file has to be matched to aka *.java etc...
      * @return a list of source files
      */
     public static List<File> fetchSourceFiles(File sourcePath, String fileType) {
         FileStrategy strategy = new FileStrategy(fileType);
         listFiles(sourcePath, strategy);
 
         return strategy.getFoundFiles();
     }
 
     /**
      * fetches the source files from a list of source paths
      *
      * @param sourcePaths the collection of paths to be searched for
      * @param fileType    the filetype to be searched for
      * @return a list of files found
      */
     public static List<File> fetchSourceFiles(Collection<String> sourcePaths, String fileType) {
         FileStrategy strategy = new FileStrategy(fileType);
 
         for (String sourcePath : sourcePaths) {
             File fSourcePath = new File(sourcePath);
             if (fSourcePath.exists()) {
                 listFiles(fSourcePath, strategy);
             }
         }
 
         return strategy.getFoundFiles();
     }
 
     /**
      * fetches the source paths from a given root directory in the format
      * <path>/<appendix>;...
      *
      * @param sourcePath the sourcePath from which the directory traversal should happen from
      * @param appendix   the appendix which has to be appended to every path found
      * @return a string builder of the paths found
      */
     @SuppressWarnings("unused")
     public static StringBuilder fetchSourcePaths(File sourcePath, String appendix) {
         DirStrategy dirStrategy = new DirStrategy();
         listFiles(sourcePath, dirStrategy);
 
         StringBuilder sourcesList = new StringBuilder(512);
 
         String root = sourcePath.getAbsolutePath();
         int rootLen = root.length() + 1;
         for (File foundDir : dirStrategy.getFoundFiles()) {
             String dirName = foundDir.getAbsolutePath();
             sourcesList.append(dirName);
             sourcesList.append(File.separator);
             sourcesList.append(appendix);
         }
         return sourcesList;
     }
 }
