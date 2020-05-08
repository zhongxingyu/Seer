 package be.mavicon.intellij.ppimport;
 
 import com.intellij.notification.NotificationType;
 import com.intellij.openapi.roots.ContentIterator;
 import com.intellij.openapi.vfs.VfsUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.openapi.vfs.VirtualFileFilter;
 
 import javax.activation.MimeType;
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.List;
 import java.util.jar.JarOutputStream;
 import java.util.zip.ZipEntry;
 
 /*
  * Copyright 2013 Marc Viaene (Mavicon BVBA)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 
 class PPImporter {
 
     public static final int HTTP_BUFFER_SIZE = 1024;
     public static final int FILE_BUFFER_SIZE = 1024;
 
 
     private PPImporter() {
     }
 
     private static PPImporter instance;
 
     public static PPImporter getInstance() {
         if (instance == null) {
             instance = new PPImporter();
         }
         return instance;
     }
 
     public void doImport(VirtualFile[] virtualFiles, final Target target, final List<String> includeExtentions, final boolean makeJar) {
         PPImportPlugin.doNotify("Starting import to " + target.getProfile(), NotificationType.INFORMATION);
 
         try {
             if (virtualFiles.length == 1 && !virtualFiles[0].isDirectory()) {
                 VirtualFile virtualFile = virtualFiles[0];
                 doImportSingleFile(true, virtualFile, target, includeExtentions);
             } else if (makeJar) {
                 byte[] jarFile = makeJar(virtualFiles, includeExtentions);
                 if (jarFile != null) {
                     InputStream dataIS = new ByteArrayInputStream(jarFile);
                     String contentType = "application/octet-stream";
                    postDataAsynchronous("jar-file", dataIS, contentType, buildURL(target));
                 }
             } else {
                 for (VirtualFile virtualFile : virtualFiles) {
                     if (virtualFile.isDirectory()) {
                         doImportDirectory(virtualFile, target, includeExtentions);
                     } else {
                         doImportSingleFile(false, virtualFile, target, includeExtentions);
                     }
                 }
             }
         } catch (IOException e) {
             PPImportPlugin.doNotify("Import failed with message:\n" + e.getMessage() + "\n\nCheck the server log for more details.", NotificationType.ERROR);
         }
     }
 
     private void doImportSingleFile(boolean asynchronous, VirtualFile virtualFile, Target target, List<String> includeExtentions) throws IOException {
         if (includeExtentions.contains(virtualFile.getExtension())) {
             InputStream dataIS = new FileInputStream(virtualFile.getCanonicalPath());
             String contentType = "text/" + virtualFile.getExtension() + ";charset=" + virtualFile.getCharset();
             if (asynchronous) {
                 postDataAsynchronous(virtualFile.getName(), dataIS, contentType, buildURL(target));
             } else {
                 postData(virtualFile.getName(), dataIS, contentType, buildURL(target));
             }
         } else {
             PPImportPlugin.doNotify("Skipping file " + virtualFile.getName(), NotificationType.INFORMATION);
         }
     }
 
     private void doImportDirectory(VirtualFile virtualFile, final Target target, final List<String> includeExtentions) throws IOException {
         VfsUtil.iterateChildrenRecursively(
                 virtualFile,
                 new VirtualFileFilter() {
                     @Override
                     public boolean accept(VirtualFile virtualFile) {
                         return true;
                     }
                 }, new ContentIterator() {
                     @Override
                     public boolean processFile(VirtualFile aVirtualFile) {
                         try {
                             if (!aVirtualFile.isDirectory()) {
                                 doImportSingleFile(false, aVirtualFile, target, includeExtentions);
                             }
                         } catch (IOException e) {
                             PPImportPlugin.doNotify("Import failed with message:\n" + e.getMessage() + "\n\nCheck the server log for more details.", NotificationType.ERROR);
                         }
                         return true;
                     }
                 }
         );
     }
 
     private byte[] makeJar(VirtualFile[] files, List<String> includeExtentions) throws IOException {
         ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
         JarOutputStream jarOS = new JarOutputStream(byteOS);
         for (VirtualFile file : files) {
             addToJar(jarOS, file, includeExtentions);
         }
         jarOS.close();
         byteOS.close();
         return byteOS.toByteArray();
     }
 
     private void addToJar(JarOutputStream jarOS, VirtualFile file, List<String> includeExtentions) throws IOException {
         if (jarOS != null && file != null) {
             if (file.isDirectory()) {
                 VirtualFile[] children = file.getChildren();
                 for (VirtualFile child : children) {
                     addToJar(jarOS, child, includeExtentions);
                 }
             } else if (includeExtentions.contains(file.getExtension())) {
                 ZipEntry entry = new ZipEntry(file.getCanonicalPath());
                 jarOS.putNextEntry(entry);
                 InputStream is = new FileInputStream(file.getCanonicalPath());
                 byte[] buffer = new byte[FILE_BUFFER_SIZE];
                 do {
                     int bytesRead = is.read(buffer, 0, FILE_BUFFER_SIZE);
                     if (bytesRead == -1) {
                         break;
                     }
                     jarOS.write(buffer, 0, bytesRead);
                 } while (true);
                 is.close();
             }
         }
     }
 
     private void postDataAsynchronous(final String name, final InputStream dataIS, final String contentType, final String url) {
         new Thread(new Runnable() {
             public void run() {
                 postData(name, dataIS, contentType, url);
             }
         }).start();
     }
 
     private void postData(final String name, final InputStream dataIS, final String contentType, final String url) {
         try {
             PPImportPlugin.doNotify("Start importing " + name + " to " + url, NotificationType.INFORMATION);
             URL httpURL = new URL(url);
             HttpURLConnection httpConnection = (HttpURLConnection) httpURL.openConnection();
             httpConnection.setDoOutput(true);
             httpConnection.setRequestProperty("Content-Type", contentType);
             httpConnection.setRequestMethod("PUT");
             httpConnection.connect();
             OutputStream outputStream = httpConnection.getOutputStream();
             byte[] buffer = new byte[HTTP_BUFFER_SIZE];
             do {
                 int bytesRead = dataIS.read(buffer, 0, 1024);
                 if (bytesRead == -1) {
                     break;
                 }
                 outputStream.write(buffer, 0, bytesRead);
             } while (true);
             outputStream.flush();
             outputStream.close();
             dataIS.close();
 
             int responseCode = httpConnection.getResponseCode();
             String responseMessage = httpConnection.getResponseMessage();
             if (responseCode >= 200 && responseCode < 300) {
                 PPImportPlugin.doNotify("Import of " + name + " complete", NotificationType.INFORMATION);
             } else {
                 PPImportPlugin.doNotify("Import failed with message: " + responseCode + " - " + responseMessage + "\nCheck the server log for more details.", NotificationType.ERROR);
             }
         } catch (Exception e) {
             PPImportPlugin.doNotify("Import failed with message: " + e.getMessage() + "\nCheck the server log for more details.", NotificationType.ERROR);
         }
 
     }
 
     private String buildURL(Target target) {
         StringBuilder url = new StringBuilder();
         url.append(target.getUrl());
         url.append("?result=true");
         url.append("&username=").append(target.getUser());
         url.append("&password=").append(target.getPassword());
         return url.toString();
     }
 }
