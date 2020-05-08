 /*
  * #%L
  * xcode-maven-plugin
  * %%
  * Copyright (C) 2012 SAP AG
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 package com.sap.prd.mobile.ios.mios;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FilenameUtils;
 
 public class FileUtils
 {
 
   /**
    * 
    * @param parent
    *          The parent directory
    * @param child
    *          The child direcory
    * @return the part of the path that represents the delta between <code>parent</code> and
    *         <code>child</code>.
    * @throws IllegalStateExcpetion
    *           in case <code>child</code> is not a child of <code>parent</code>.
    */
   public static String getDelta(File parent, File child)
   {
 
     final List<String> _baseDir = split(parent.getAbsoluteFile());
     final List<String> _sourceDir = split(child.getAbsoluteFile());
 
     int index = 0;
 
     for (int size = _baseDir.size(); index < size; index++)
       if (!_baseDir.get(index).equals(_sourceDir.get(index)))
         throw new IllegalStateException("Source directory '" + child + "' is not a child of the base directory '"
               + parent + "'.");
 
     StringBuilder path = new StringBuilder();
 
     boolean first = true;
     for (int size = _sourceDir.size(); index < size; index++) {
 
       if (!first)
         path.append(File.separator);
       path.append(_sourceDir.get(index));
       first = false;
     }
 
     return path.toString();
   }
 
   private static List<String> split(File dir)
   {
 
     List<String> result = new ArrayList<String>();
 
     do {
 
       result.add(dir.getName());
       dir = dir.getParentFile();
 
     } while (dir != null);
 
     Collections.reverse(result);
 
     return result;
 
   }
 
   /**
    * Get the relative path from one file to another, specifying the directory separator. If one of
    * the provided resources does not exist, it is assumed to be a file unless it ends with '/' or
    * '\'.
    * 
    * Copied from http://stackoverflow.com/a/3054692/933106.
    * 
    * @param target
    *          targetPath is calculated to this file
    * @param base
    *          basePath is calculated from this file
    * @param separator
    *          directory separator. The platform default is not assumed so that we can test Unix
    *          behaviour when running on Windows (for example)
    * @return
    */
   public static String getRelativePath(String targetPath, String basePath, String pathSeparator)
   {
 
     // Normalize the paths
     String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
     String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);
 
     // Undo the changes to the separators made by normalization
     if (pathSeparator.equals("/")) {
       normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
       normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);
 
     }
     else if (pathSeparator.equals("\\")) {
       normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
       normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);
 
     }
     else {
       throw new IllegalArgumentException("Unrecognised dir separator '" + pathSeparator + "'");
     }
 
     String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
     String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));
 
     // First get all the common elements. Store them as a string,
     // and also count how many of them there are.
     StringBuilder common = new StringBuilder();
 
     int commonIndex = 0;
     while (commonIndex < target.length && commonIndex < base.length
           && target[commonIndex].equals(base[commonIndex])) {
       common.append(target[commonIndex] + pathSeparator);
       commonIndex++;
     }
 
     if (commonIndex == 0) {
       // No single common path element. This most
       // likely indicates differing drive letters, like C: and D:.
       // These paths cannot be relativized.
       throw new PathResolutionException("No common path element found for '" + normalizedTargetPath + "' and '"
             + normalizedBasePath
             + "'");
     }
 
     // The number of directories we have to backtrack depends on whether the base is a file or a dir
     // For example, the relative path from
     //
     // /foo/bar/baz/gg/ff to /foo/bar/baz
     // 
     // ".." if ff is a file
     // "../.." if ff is a directory
     //
     // The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
     // the resource referred to by this path may not actually exist, but it's the best I can do
     boolean baseIsFile = true;
 
     File baseResource = new File(normalizedBasePath);
 
     if (baseResource.exists()) {
       baseIsFile = baseResource.isFile();
 
     }
     else if (basePath.endsWith(pathSeparator)) {
       baseIsFile = false;
     }
 
     StringBuilder relative = new StringBuilder();
 
     if (base.length != commonIndex) {
       int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;
 
       for (int i = 0; i < numDirsUp; i++) {
         relative.append(".." + pathSeparator);
       }
     }
     relative.append(normalizedTargetPath.substring(common.length()));
     return relative.toString();
   }
 
   static class PathResolutionException extends RuntimeException
   {
 
     private static final long serialVersionUID = -6747166042664389937L;
 
     PathResolutionException(String msg)
     {
       super(msg);
     }
   }
   
  public static void createSymbolicLink(final File source, final File target) throws IOException {
     System.out.println("[INFO] Creating symbolic link. Source:" + source.getAbsolutePath() + ", target: " + target.getAbsolutePath() + ".");
     target.getParentFile().mkdirs();
     int returnValue = Forker.forkProcess(System.out, null, "ln", "-sf", source.getAbsolutePath(), target.getAbsolutePath());
     if (returnValue != 0) {
       throw new RuntimeException("Cannot create symbolic link from '" + source + "' to '"  +target + "'. Return value:" + returnValue);
     }
   }
 }
