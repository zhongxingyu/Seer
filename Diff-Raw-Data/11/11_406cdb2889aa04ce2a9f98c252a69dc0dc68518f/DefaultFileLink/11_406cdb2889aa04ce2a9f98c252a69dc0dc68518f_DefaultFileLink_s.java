 package com.mvplugin.downloader;
 
 import com.mvplugin.downloader.api.FileLink;
 import com.mvplugin.downloader.api.VersionType;
 import org.bukkit.ChatColor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Date;
 
 /**
  * Our default implementation of a FileLink.
  */
 class DefaultFileLink implements FileLink {
 
     private static final String LS = "\n";
 
     private final String pluginName;
 
     private final URL filePageURL;
 
     private final String version;
     private final URL downloadURL;
     private final long fileSize;
     private final String fileName;
     private final String gameVersion;
     private final VersionType versionType;
     private final int downloadCount;
     private final Date uploadDate;
     private final String changeLog;
     private final String knownCaveats;
     private final String md5CheckSum;
 
     DefaultFileLink(final String link, final String pluginName) throws MalformedURLException, IOException {
         this.pluginName = pluginName;
         this.filePageURL = new URL(link);
         final URLConnection urlConn = filePageURL.openConnection();
         BufferedReader reader = null;
 
         // Used for detecting elements
         int sizeLine = -1;
         int titleLine = -1;
         int gameVersionLine = -1;
         int versionTypeLine = -1;
         int downloadCountLine = -1;
         int uploadDateLine = -1;
         int md5Line = -1;
         int changeLogLine = -1;
         int knownCaveatsLine = -1;
 
         // Temp storage
         String downloadLink = "";
         String fileName = "";
         String gameVersion = "";
         long fileSize = -1;
         String title = "";
         String versionType = "";
         String downloadCount = "";
         String uploadDate = "";
         String md5 = "";
         final StringBuilder changeLog = new StringBuilder();
         final StringBuilder knownCaveats = new StringBuilder();
         try {
             reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
 
             int counter = 0;
             String line;
             while((line = reader.readLine()) != null) {
                 counter++;
                 // Search for the download link
                 if(line.contains("<li class=\"user-action user-action-download\">")) {
                     // Get the raw link
                     downloadLink = line.split("<a href=\"")[1].split("\">Download</a>")[0];
                     String[] split = downloadLink.split("/");
                     fileName = split[split.length - 1].trim();
                 }
                 // Search for size
                 else if (line.contains("<dt>Size</dt>")) {
                     sizeLine = counter + 1;
                 } else if(counter == sizeLine) {
                     String size = line.replaceAll("<dd>", "").replaceAll("</dd>", "").trim();
                     int multiplier = size.contains("MiB") ? 1048576 : 1024;
                     size = size.replace(" KiB", "").replace(" MiB", "");
                     fileSize = (long)(Double.parseDouble(size) * multiplier);
                 }
                 // Search for version title
                 else if (line.contains("<header class=\"main-header\"><div class=\"line\">")) {
                     titleLine = counter + 2;
                 } else if (counter == titleLine) {
                     title = line.trim();
                 }
                 // Search for game version
                 else if (line.contains("<dt>Game version</dt>")) {
                     gameVersionLine = counter + 1;
                 } else if (counter == gameVersionLine) {
                    gameVersion = line.replaceAll("<dd>.*<li>", "").replaceAll("</li>.*</dd>", "").trim();
                 }
                 // Search for version Type
                 else if (line.contains("<dt>Type</dt>")) {
                     versionTypeLine = counter + 1;
                 } else if (counter == versionTypeLine) {
                    versionType = line.replaceAll("<dd>.*-b\">", "").replaceAll("</span></dd>", "").trim();
                 }
                 // Search for download count
                 else if (line.contains("<dt>Downloads</dt>")) {
                     downloadCountLine = counter + 1;
                 } else if (counter == downloadCountLine) {
                    downloadCount = line.replaceAll("<dd>.*e=\"", "").replaceAll("\">.*</dd>", "").trim();
                 }
                 // Search for download count
                 else if (line.contains("<dt>Uploaded on</dt>")) {
                     uploadDateLine = counter + 1;
                 } else if (counter == uploadDateLine) {
                    uploadDate = line.replaceAll("<dd>.*epoch=\"", "").replaceAll("\"\\sdata.*</dd>", "").trim();
                 }
                 // Search for md5
                 else if (line.contains("<dt>MD5</dt>")) {
                     md5Line = counter + 1;
                 } else if (counter == md5Line) {
                     md5 = line.replaceAll("<dd>", "").replaceAll("</dd>", "").trim();
                 }
                 // Search for change log
                 else if (line.contains("<div class=\"content-box\"><div class=\"content-box-inner\"><h3>Change log</h3>")) {
                     changeLog.append(parseHtmlLine(line.replace("<div class=\"content-box\"><div class=\"content-box-inner\"><h3>Change log</h3>", ""))).append(LS);
                     changeLogLine = counter + 1;
                 } else if (counter == changeLogLine) {
                     if (line.contains("</div></div>")) {
                         continue;
                     }
                     changeLog.append(parseHtmlLine(line)).append(LS);
                     changeLogLine++;
                 }
                 // Search for known caveats
                 else if (line.contains("<div class=\"content-box\"><div class=\"content-box-inner\"><h3>Known caveats</h3>")) {
                     knownCaveats.append(parseHtmlLine(line.replace("<div class=\"content-box\"><div class=\"content-box-inner\"><h3>Known caveats</h3>", ""))).append(LS);
                     knownCaveatsLine = counter + 1;
                 } else if (counter == knownCaveatsLine) {
                     if (line.contains("</div></div>")) {
                         continue;
                     }
                     knownCaveats.append(parseHtmlLine(line)).append(LS);
                     knownCaveatsLine++;
                 }
             }
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
         if (downloadLink == null) {
             // TODO make this better
             throw new IOException();
         }
         this.downloadURL = new URL(downloadLink);
         this.fileSize = fileSize;
         this.version = title;
         this.fileName = fileName;
         this.gameVersion = gameVersion;
         VersionType tempType = VersionType.UNKNOWN;
         try {
             tempType = VersionType.valueOf(versionType.toUpperCase());
         } catch (IllegalArgumentException ignore) { }
         this.versionType = tempType;
         int downloads = -1;
         try {
             downloads = Integer.parseInt(downloadCount);
         } catch (NumberFormatException ignore) { }
         this.downloadCount = downloads;
         long epochTime = -1;
         try {
             epochTime = Long.parseLong(uploadDate);
         } catch (NumberFormatException ignore) { }
         if (epochTime == -1) {
             this.uploadDate = new Date();
         } else {
             this.uploadDate = new Date(epochTime);
         }
         this.md5CheckSum = md5;
         this.changeLog = changeLog.toString();
         this.knownCaveats = knownCaveats.toString();
     }
 
     private String parseHtmlLine(final String line) {
         return line
                 .replaceAll("<li>", " * ")
                 .replaceAll("<strong>", ChatColor.BOLD.toString())
                 .replaceAll("</strong>", ChatColor.RESET.toString())
                 .replaceAll("<em>", ChatColor.ITALIC.toString())
                 .replaceAll("</em>", ChatColor.RESET.toString())
                 //.replaceAll("<a\\s.*\">", "")
                 .replaceAll("<.+?>", "");
     }
 
     @Override
     public String getPluginName() {
         return pluginName;
     }
 
     @Override
     public URL getDownloadLink() {
         return downloadURL;
     }
 
     @Override
     public String getVersion() {
         return version;
     }
 
     @Override
     public String getGameVersion() {
         return gameVersion;
     }
 
     @Override
     public String getFileName() {
         return fileName;
     }
 
     @Override
     public long getFileSize() {
         return fileSize;
     }
 
     @Override
     public VersionType getType() {
         return versionType;
     }
 
     @Override
     public String getMD5CheckSum() {
         return md5CheckSum;
     }
 
     @Override
     public int getDownloadCount() {
         return downloadCount;
     }
 
     @Override
     public Date getUploadedDate() {
         return uploadDate;
     }
 
     @Override
     public String getChangeLog() {
         return changeLog;
     }
 
     @Override
     public String getKnownCaveats() {
         return knownCaveats;
     }
 }
