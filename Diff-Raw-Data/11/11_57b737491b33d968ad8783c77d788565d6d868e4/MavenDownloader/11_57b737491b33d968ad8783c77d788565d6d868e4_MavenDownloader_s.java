 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.sdk.index;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.IPath;
 import org.nuxeo.ide.common.IOUtils;
 import org.nuxeo.ide.sdk.model.Artifact;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class MavenDownloader {
 
     private static Pattern METADATA_TIMESTAMP_PATTERN = Pattern.compile("<timestamp>([^<]+)</timestamp>");
 
     private static Pattern METADATA_BUILDNUMBER_PATTERN = Pattern.compile("<buildNumber>([^<]+)</buildNumber>");
 
     private static String NUXEO_SNAPSHOT = "https://maven.nuxeo.org/nexus/content/groups/public-snapshot";
 
     private static String NUXEO_PUBLIC = "https://maven.nuxeo.org/nexus/content/groups/public";
 
     private static String NUXEO_TP_PUBLIC = "https://maven.nuxeo.org/nexus/content/groups/thirdparty-releases";
 
     private static String MAVEN_CENTRAL = "http://repo1.maven.org/maven2";
 
     public static FileRef downloadSourceJar(Artifact artifact)
             throws IOException {
         // URL[] urls = null;
         IPath path = artifact.getSourceJarPathObject();
         File file = new File(System.getProperty("user.home")
                + ".m2/repository/" + path);
         if (file.isFile()) {
             return new FileRef(file);
         }
         file = null;
         if (artifact.isSnapshot()) {
             file = downloadNuxeoSnapshotSourceJar(artifact);
         } else if (artifact.getGroupId().startsWith("org.nuxeo")) {
             file = downloadNuxeoSourceJar(artifact);
         } else {
             file = downloadLibrarySourceJar(artifact);
         }
         // file = downloadFile(urls);
         if (file != null) {
             FileRef ref = new FileRef(file);
             ref.setName(artifact.getArtifactId() + '-' + artifact.getVersion()
                     + "-sources.jar");
             ref.setTemp(true);
             return ref;
         }
         return null;
     }
 
     public static File downloadSourceJar(String repoUrl, Artifact artifact)
             throws IOException {
         URL url = new URL(repoUrl + '/' + artifact.getSourceJarPath());
         return downloadFile(url);
     }
 
     public static File downloadNuxeoSourceJar(Artifact artifact)
             throws IOException {
         return downloadSourceJar(NUXEO_PUBLIC, artifact);
     }
 
     public static File downloadNuxeoSnapshotSourceJar(Artifact artifact)
             throws IOException {
         String buildId = getSnapshotName(artifact);
         if (buildId == null) {
             return null;
         }
         String version = artifact.getVersion();
         int i = version.indexOf("SNAPSHOT");
         if (i > 0) {
             version = version.substring(0, i) + buildId;
         }
         IPath path = artifact.getSourceJarPathObject();
         path = path.removeLastSegments(1);
         path = path.append(artifact.getArtifactId() + '-' + version
                 + "-sources.jar");
         URL url = new URL(NUXEO_SNAPSHOT + '/' + path.toString());
         return downloadFile(url);
     }
 
     public static File downloadLibrarySourceJar(Artifact artifact)
             throws IOException {
         File file = downloadSourceJar(NUXEO_TP_PUBLIC, artifact);
         if (file == null) {
             return downloadSourceJar(MAVEN_CENTRAL, artifact);
         }
         return file;
     }
 
     public static File downloadFile(URL[] urls) throws IOException {
         for (URL url : urls) {
             File file = downloadFile(url);
             if (file != null) {
                 return file;
             }
         }
         return null;
     }
 
     public static File downloadFile(URL url) throws IOException {
         // System.out.println("trying to download: " + url);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setDoOutput(false);
         conn.setDoInput(true);
         try {
             InputStream in = conn.getInputStream();
             try {
                 int code = conn.getResponseCode();
                 if (code > 399) {
                     return null;
                 }
                 File file = File.createTempFile("maven-jar-source-", ".tmp");
                 IOUtils.copyToFile(in, file, true);
                 return file;
             } finally {
                 in.close();
             }
         } catch (FileNotFoundException e) {
             return null;
         }
     }
 
     public static String getSnapshotName(Artifact artifact) throws IOException {
         URL url = new URL(NUXEO_SNAPSHOT + '/' + artifact.getMetadataPath());
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setDoOutput(false);
         conn.setDoInput(true);
         InputStream in = conn.getInputStream();
         int code = conn.getResponseCode();
         if (code > 399) {
             return null;
         }
         String meta = IOUtils.read(in);
         String result = null;
         Matcher m = METADATA_TIMESTAMP_PATTERN.matcher(meta);
         if (m.find()) {
             result = m.group(1);// + '-' + m.group(2);
         }
         if (result == null) {
             return null;
         }
         m = METADATA_BUILDNUMBER_PATTERN.matcher(meta);
         if (m.find()) {
             result += '-' + m.group(1);
             return result;
         }
         return null;
     }
 
     public static class FileRef {
         protected boolean temp;
 
         protected String name;
 
         protected File file;
 
         public FileRef(File file) {
             this.file = file;
         }
 
         public String getName() {
             return name;
         }
 
         public void setTemp(boolean temp) {
             this.temp = temp;
         }
 
         public boolean isTemp() {
             return temp;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public File getFile() {
             return file;
         }
 
         public File installTo(File dir) throws IOException {
             dir.mkdirs();
             try {
                 if (name == null) {
                     name = file.getName();
                 }
                 File to = new File(dir, name);
                 IOUtils.copyFile(file, to, true);
                 return to;
             } finally {
                 dispose();
             }
         }
 
         public void dispose() {
             if (temp) {
                 file.delete();
             }
         }
     }
 
 }
