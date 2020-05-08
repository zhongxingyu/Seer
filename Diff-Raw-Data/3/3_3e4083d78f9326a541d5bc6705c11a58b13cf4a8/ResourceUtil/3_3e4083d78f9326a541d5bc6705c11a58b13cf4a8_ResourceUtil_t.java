 /*******************************************************************************
  * This file is part of Champions.
  *
  *     Champions is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Champions is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Champions.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.github.championsdev.champions.library.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.zip.ZipFile;
 
 /**
  * @author B2OJustin
  */
 public class ResourceUtil {
 
     /**
      * Retrieves a list of files in the path contained within the jar file.
      *
      * @param jarClass Any class within the target jar
      * @param path The path within the jar file, relative to the root directory.
      * @return List of the files within the jar path.
      * @throws IOException
      */
     public static ArrayList<String> getJarFiles(Class<?> jarClass, String path) throws IOException {
         String jarPath = jarClass.getResource("/" + path).getPath();
         jarPath = jarPath.substring(5, jarPath.indexOf("!"));
 
         ArrayList<String> fileNameList = new ArrayList<>();
 
         JarFile jar = new JarFile(jarPath);
         Enumeration<JarEntry> entries = jar.entries();
         while(entries.hasMoreElements()) {
             String fileName = entries.nextElement().getName();
             if(fileName.startsWith(path) && !fileName.endsWith("/")) {
                 fileNameList.add(fileName);
             }
         }
        jar.close();
         return fileNameList;
     }
 
     public static int copyDirectoryFromJar(Class<?> jarClass, String srcDir, String toDir, boolean overwrite) throws IOException {
         int filesCopied = 0;
         try {
             for(String fileName : getJarFiles(jarClass, srcDir)) {
                 URI fileUri = jarClass.getResource("/" + fileName).toURI();
                 File file = new File(fileName.replace(srcDir, toDir));
                 if(file.exists() && !overwrite)
                     continue;
                 FileUtils.copyURLToFile(fileUri.toURL(), file);
                 System.out.println(String.format("Copied %s to %s", fileName, fileName.replace(srcDir, toDir)));
                 filesCopied++;
             }
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
         return filesCopied;
     }
 
     public static InputStream getResourceFromJar(File fromFile, String insideFile) {
         if (fromFile.exists() && fromFile.isFile()) {
             if (fromFile.getName().endsWith(".jar")) {
                 try {
                     JarFile jarFile = new JarFile(fromFile);
                     InputStream returnMe = jarFile.getInputStream(jarFile.getJarEntry(insideFile));
                     jarFile.close();
                     return returnMe;
                 } catch (IOException e) {
                     return null;
                 }
             }
         }
         return null;
     }
 
     public static InputStream getResourceFromZip(File fromFile, String insideFile) {
         if (fromFile.exists() && fromFile.isFile()) {
             if (fromFile.getName().endsWith(".zip")) {
                 try {
                     ZipFile zipFile = new ZipFile(fromFile);
                     InputStream returnMe = zipFile.getInputStream(zipFile.getEntry(insideFile));
                     zipFile.close();
                     return returnMe;
                 } catch (IOException e) {
                     return null;
                 }
             }
         }
         return null;
     }
 
     public static boolean isJarFile(File file) {
         return (file != null && !file.isDirectory() && file.getName().endsWith(".jar"));
     }
 
     public static boolean isZipFile(File file) {
         return (file != null && !file.isDirectory() && file.getName().endsWith(".zip"));
     }
 
 }
