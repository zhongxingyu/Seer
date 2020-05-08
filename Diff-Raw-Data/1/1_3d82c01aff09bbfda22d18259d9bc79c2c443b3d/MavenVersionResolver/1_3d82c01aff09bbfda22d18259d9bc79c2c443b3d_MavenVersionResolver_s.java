 package org.kevoree.resolver.util;
 
 import org.kevoree.log.Log;
 import org.kevoree.resolver.api.MavenArtefact;
 import org.kevoree.resolver.api.MavenVersionResult;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by duke on 16/05/13.
  */
 public class MavenVersionResolver {
 
     private static final String buildLatestTag = "<latest>";
     private static final String buildEndLatestTag = "</latest>";
 
     private static final String buildReleaseTag = "<release>";
     private static final String buildEndreleaseTag = "</release>";
 
     private static final String buildMavenTag = "<buildNumber>";
     private static final String buildEndMavenTag = "</buildNumber>";
 
     private static final String timestampMavenTag = "<timestamp>";
     private static final String timestampEndMavenTag = "</timestamp>";
 
     private static final String lastUpdatedMavenTag = "<lastUpdated>";
     private static final String lastUpdatedEndMavenTag = "</lastUpdated>";
 
     private static final String snapshotVersionClassifierMavenTag = "<classifier>";
     private static final String snapshotVersionClassifierEndMavenTag = "</classifier>";
 
     private static final String snapshotVersionExtensionMavenTag = "<extension>";
     private static final String snapshotVersionExtensionEndMavenTag = "</extension>";
 
     private static final String snapshotVersionValueMavenTag = "<value>";
     private static final String snapshotVersionValueEndMavenTag = "</value>";
 
     private static final String snapshotVersionUpdatedMavenTag = "<updated>";
     private static final String snapshotVersionUpdatedEndMavenTag = "</updated>";
 
     public static final String metaFile = "maven-metadata.xml";
     private static final String localmetaFile = "maven-metadata-local.xml";
 
     public MavenVersionResult resolveVersion(MavenArtefact artefact, String remoteURL, boolean localDeploy) throws IOException {
 
         StringBuilder builder = new StringBuilder();
         builder.append(remoteURL);
         String sep = File.separator;
         if (remoteURL.startsWith("http")) {
             sep = "/";
         }
         if (!remoteURL.endsWith(sep)) {
             builder.append(sep);
         }
         if (remoteURL.startsWith("http") || remoteURL.startsWith("https")) {
             builder.append(artefact.getGroup().replace(".", "/"));
         } else {
             builder.append(artefact.getGroup().replace(".", File.separator));
         }
         builder.append(sep);
         builder.append(artefact.getName());
         builder.append(sep);
         builder.append(artefact.getVersion());
         builder.append(sep);
         if (localDeploy) {
             builder.append(localmetaFile);
         } else {
             builder.append(metaFile);
         }
         URL metadataURL = new URL("file:///" + builder.toString());
         if (remoteURL.startsWith("http") || remoteURL.startsWith("https")) {
             metadataURL = new URL(builder.toString());
         }
         URLConnection c = metadataURL.openConnection();
 
         c.setRequestProperty("User-Agent", "Kevoree");
 
 
         InputStream in = c.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         StringBuilder resultBuilder = new StringBuilder();
         String line = reader.readLine();
         resultBuilder.append(line);
         while ((line = reader.readLine()) != null) {
             resultBuilder.append(line);
         }
         String result = resultBuilder.toString();
         in.close();
         MavenVersionResult versionResult = new MavenVersionResult();
         boolean found = false;
         Pattern pattern = Pattern.compile("<snapshotVersion> *(.(?!(</snapshotVersion>)))* *</snapshotVersion>");
         Matcher matcher = pattern.matcher(result);
         int index = 0;
         while (matcher.find(index) && !found) {
             String snapshotVersion = matcher.group().trim();
             if ((!snapshotVersion.contains(snapshotVersionClassifierMavenTag)
                     || (snapshotVersion.contains(snapshotVersionClassifierMavenTag)
                     && !"sources".equalsIgnoreCase(snapshotVersion.substring(snapshotVersion.indexOf(snapshotVersionClassifierMavenTag) + snapshotVersionClassifierMavenTag.length(), snapshotVersion.indexOf(snapshotVersionClassifierEndMavenTag)))))
                     && snapshotVersion.contains(snapshotVersionValueMavenTag)
                     && snapshotVersion.contains(snapshotVersionUpdatedMavenTag)
                     && (!snapshotVersion.contains(snapshotVersionExtensionMavenTag)
                     || artefact.getExtension().equalsIgnoreCase(snapshotVersion.substring(snapshotVersion.indexOf(snapshotVersionExtensionMavenTag) + snapshotVersionExtensionMavenTag.length(), snapshotVersion.indexOf(snapshotVersionExtensionEndMavenTag))))
                     ) {
                 versionResult.setValue(snapshotVersion.substring(snapshotVersion.indexOf(snapshotVersionValueMavenTag) + snapshotVersionValueMavenTag.length(), snapshotVersion.indexOf(snapshotVersionValueEndMavenTag)));
                 versionResult.setLastUpdate(snapshotVersion.substring(snapshotVersion.indexOf(snapshotVersionUpdatedMavenTag) + snapshotVersionUpdatedMavenTag.length(), snapshotVersion.indexOf(snapshotVersionUpdatedEndMavenTag)));
                 found = true;
             }
             index += snapshotVersion.length();
         }
 
         versionResult.setUrl_origin(remoteURL);
         versionResult.setNotDeployed(localDeploy);
         if (!found) {
             if (result.contains(timestampMavenTag) && result.contains(timestampEndMavenTag) && result.contains(buildMavenTag) && result.contains(buildEndMavenTag) && result.contains(lastUpdatedMavenTag) && result.contains(lastUpdatedEndMavenTag)) {
                 versionResult.setValue(result.substring(result.indexOf(timestampMavenTag) + timestampMavenTag.length(), result.indexOf(timestampEndMavenTag)) + "-" + result.substring(result.indexOf(buildMavenTag) + buildMavenTag.length(), result.indexOf(buildEndMavenTag)));
                 versionResult.setLastUpdate(result.substring(result.indexOf(lastUpdatedMavenTag) + lastUpdatedMavenTag.length(), result.indexOf(lastUpdatedEndMavenTag)));
                 return versionResult;
             } else {
                 return null;
             }
         } else {
             return versionResult;
         }
     }
 
     private File buildCacheFile(MavenArtefact artefact, String basePath, String remoteURL) {
         StringBuilder builder = new StringBuilder();
         builder.append(basePath);
         String sep = File.separator;
         if (!basePath.endsWith(sep)) {
             builder.append(sep);
         }
         builder.append(artefact.getGroup().replace(".", File.separator));
         builder.append(sep);
         builder.append(artefact.getName());
         builder.append(sep);
         builder.append(metaFile);
         builder.append("-");
         String cleaned = remoteURL.replace("/", "_").replace(":", "_").replace(".", "_");
         builder.append(cleaned);
         return new File(builder.toString());
     }
 
 
     public String foundRelevantVersion(MavenArtefact artefact, String cachePath, String remoteURL, boolean localDeploy) {
         String askedVersion = artefact.getVersion().toLowerCase();
         Boolean release = false;
         Boolean lastest = false;
         if (askedVersion.equalsIgnoreCase("release")) {
             release = true;
         }
         if (askedVersion.equalsIgnoreCase("latest")) {
             lastest = true;
         }
         if (!release && !lastest) {
             return null;
         }
 
         StringBuilder builder = new StringBuilder();
         builder.append(remoteURL);
         String sep = File.separator;
         if (remoteURL.startsWith("http") || remoteURL.startsWith("https")) {
             sep = "/";
         }
         if (!remoteURL.endsWith(sep)) {
             builder.append(sep);
         }
         if (remoteURL.startsWith("http") || remoteURL.startsWith("https")) {
             builder.append(artefact.getGroup().replace(".", "/"));
         } else {
             builder.append(artefact.getGroup().replace(".", File.separator));
         }
         builder.append(sep);
         builder.append(artefact.getName());
         builder.append(sep);
 
         if (localDeploy) {
             builder.append(localmetaFile);
         } else {
             builder.append(metaFile);
         }
         File cacheFile = null;
         FileWriter resultBuilder = null;
         if (remoteURL.startsWith("http://") || remoteURL.startsWith("https://")) {
             cacheFile = buildCacheFile(artefact, cachePath, remoteURL);
         }
         StringBuffer buffer = new StringBuffer();
         try {
             URL metadataURL = new URL("file:///" + builder.toString());
             if (remoteURL.startsWith("http") || remoteURL.startsWith("https")) {
                 metadataURL = new URL(builder.toString());
             }
             URLConnection c = metadataURL.openConnection();
             c.setRequestProperty("User-Agent", "Kevoree");
             InputStream in = c.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             String line = reader.readLine();
             if (line != null) {
                 if (remoteURL.startsWith("http://") || remoteURL.startsWith("https://")) {
                     try {
                         resultBuilder = new FileWriter(cacheFile);
                     } catch (IOException e) {
                         Log.error("Can't create cache file {}", e, cacheFile.getAbsolutePath());
                     }
                 }
             }
             buffer.append(line);
             buffer.append("\n");
             if (resultBuilder != null) {
                 resultBuilder.append(line);
                 resultBuilder.append("\n");
             }
             while ((line = reader.readLine()) != null) {
                 buffer.append(line);
                 buffer.append("\n");
                 if (resultBuilder != null) {
                     resultBuilder.append(line);
                     resultBuilder.append("\n");
                 }
             }
             in.close();
             if (resultBuilder != null) {
                 resultBuilder.flush();
                 resultBuilder.close();
             }
         } catch (MalformedURLException ignored) {
         } catch (IOException ignored) {
         } finally {
             String flatFile = null;
             if (buffer.length() != 0) {
                 flatFile = buffer.toString();
             } else {
                 if (cacheFile != null && cacheFile.exists()) {
                     BufferedReader br;
                     try {
                         br = new BufferedReader(new FileReader(cacheFile));
                         String sCurrentLine;
                         while ((sCurrentLine = br.readLine()) != null) {
                             buffer.append(sCurrentLine);
                             buffer.append("\n");
                         }
                         flatFile = buffer.toString();
                     } catch (Exception e) {
                         Log.error("Maven Resolver internal error !", e);
                     }
                 } else {
                     return null;
                 }
             }
             try {
                 if (release) {
                     if (flatFile.contains(buildReleaseTag) && flatFile.contains(buildEndreleaseTag)) {
                         return flatFile.substring(flatFile.indexOf(buildReleaseTag) + buildReleaseTag.length(), flatFile.indexOf(buildEndreleaseTag));
                     }
                 }
                 if (lastest) {
                     if (flatFile.contains(buildLatestTag) && flatFile.contains(buildEndLatestTag)) {
                         return flatFile.substring(flatFile.indexOf(buildLatestTag) + buildLatestTag.length(), flatFile.indexOf(buildEndLatestTag));
                     }
                 }
             } catch (Exception e) {
                 Log.error("Maven Resolver internal error !", e);
             }
         }
 
         return null;
     }
 
 
 }
