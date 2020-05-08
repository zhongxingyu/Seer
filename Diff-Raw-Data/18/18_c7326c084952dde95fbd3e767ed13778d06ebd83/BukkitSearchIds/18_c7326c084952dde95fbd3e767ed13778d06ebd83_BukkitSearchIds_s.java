 /*
  * This file is part of SearchIds.
  *
  * Copyright © 2012-2013 Visual Illusions Entertainment
  *
  * SearchIds is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * SearchIds is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with SearchIds.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.minecraft.server.mod.bukkit.plugin.searchids;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.security.CodeSource;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import javax.xml.parsers.ParserConfigurationException;
 import net.visualillusionsent.searchids.DataParser;
 import net.visualillusionsent.searchids.Result;
 import net.visualillusionsent.searchids.SearchIds;
 import net.visualillusionsent.searchids.SearchIdsProperties;
 import net.visualillusionsent.searchids.UpdateThread;
 import net.visualillusionsent.utils.ProgramStatus;
 import net.visualillusionsent.utils.VersionChecker;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.xml.sax.SAXException;
 
 public final class BukkitSearchIds extends JavaPlugin implements SearchIds {
 
     public static DataParser parser;
     private UpdateThread updateThread;
     private final VersionChecker vc;
     private float version;
     private short build;
     private String buildTime;
     private ProgramStatus status;
 
     public BukkitSearchIds() {
         readManifest();
        vc = new VersionChecker(getName(), String.valueOf(version), String.valueOf(build), "http://visualillusionsent.net/minecraft/plugins/", status, false);
     }
 
     public final void onEnable() {
         checkStatus();
         checkVersion();
         if (!SearchIdsProperties.initProps()) {
             getLogger().severe("Could not initialize properties file");
             return;
         }
         if (parser == null) {
             try {
                 parser = new DataParser(this);
             }
             catch (ParserConfigurationException ex) {}
             catch (SAXException ex) {}
         }
         if (!initData()) {
             getLogger().severe("Could not init the search data from: " + SearchIdsProperties.dataXml + ". Please check that the file exists and is not corrupt.");
             if (!SearchIdsProperties.autoUpdate) {
                 getLogger().severe("Set auto-update-data=true in 'config/SearchIds/SearchIds.cfg' to automatically download the search data file " + SearchIdsProperties.dataXml);
             }
             return;
         }
 
         if (SearchIdsProperties.autoUpdate) {
             if (updateThread == null) {
                 updateThread = new UpdateThread(this);
             }
             updateThread.start();
         }
     }
 
     public final void onDisable() {
         if (updateThread != null) {
             updateThread.stop();
             updateThread = null;
         }
         parser = null;
     }
 
     private final boolean initData() {
         if ((SearchIdsProperties.dataXml == null) || (SearchIdsProperties.dataXml.equals(""))) {
             return false;
         }
 
         File f = new File(SearchIdsProperties.dataXml);
         if ((!updateData(SearchIdsProperties.updateSource)) && (!f.exists())) {
             return false;
         }
 
         return parser.search("test") != null;
     }
 
     public final boolean updateData(String Source) {
         if (SearchIdsProperties.autoUpdate) {
             try {
                 URL url = new URL(Source);
                 getLogger().info("Updating data from " + Source + "...");
                 InputStream is = url.openStream();
                 FileOutputStream fos = null;
                 fos = new FileOutputStream(SearchIdsProperties.dataXml);
                 int oneChar;
                 while ((oneChar = is.read()) != -1) {
                     fos.write(oneChar);
                 }
                 is.close();
                 fos.close();
                 getLogger().info("Update Successful!");
                 return true;
             }
             catch (MalformedURLException e) {
                 if (Source.equals(SearchIdsProperties.updateSource)) {
                     getLogger().warning("Update from " + Source + " Failed. Attempting Alternate Source...");
                     return updateData(SearchIdsProperties.updateSourceALT);
                 }
                 else {
                     getLogger().warning("Update from " + Source + " Failed.");
                     return false;
                 }
             }
             catch (IOException e) {
                 getLogger().warning("Could not update search data.");
                 return false;
             }
         }
         return true;
     }
 
     final void printSearchResults(Player player, ArrayList<Result> results, String query) {
         if (results != null && !results.isEmpty()) {
             player.sendMessage("§bSearch results for \"" + query + "\":");
             Iterator<Result> itr = results.iterator();
             String line = "";
             int num = 0;
             while (itr.hasNext()) {
                 num++;
                 Result result = itr.next();
                 line += (SearchIdsProperties.rightPad(result.getFullValue(), result.getValuePad()) + " " + SearchIdsProperties.delimiter + " " + SearchIdsProperties.rightPad(result.getName(), SearchIdsProperties.nameWidth));
                 if (num % 2 == 0 || !itr.hasNext()) {
                     player.sendMessage("§6" + line.trim());
                     line = "";
                 }
                 if (num > 16) {
                     player.sendMessage("§6Not all results are displayed. Make your term more specific!");
                     break;
                 }
             }
         }
         else {
             player.sendMessage("§cNo results found.");
         }
     }
 
     final void printConsoleSearchResults(ArrayList<Result> results, String query) {
         if (results != null && !results.isEmpty()) {
             System.out.println("Search results for \"" + query + "\":");
             Iterator<Result> itr = results.iterator();
             String line = "";
             int num = 0;
             while (itr.hasNext()) {
                 num++;
                 Result result = itr.next();
                 line += (SearchIdsProperties.rightPad(result.getFullValue(), result.getValuePad()) + " " + SearchIdsProperties.delimiter + " " + SearchIdsProperties.rightPad(result.getName(), SearchIdsProperties.nameWidth));
                 if (num % 2 == 0 || !itr.hasNext()) {
                     System.out.println(line.trim());
                     line = "";
                 }
                 if (num > 16) {
                     System.out.println("Not all results are displayed. Make your term more specific!");
                     break;
                 }
             }
         }
         else {
             System.out.println("No results found.");
         }
     }
 
     @Override
     public void info(String msg) {
         getLogger().info(msg);
     }
 
     @Override
     public void warning(String msg) {
         getLogger().warning(msg);
     }
 
     @Override
     public void severe(String msg) {
         getLogger().severe(msg);
     }
 
     private final Manifest getManifest() throws Exception {
         Manifest toRet = null;
         Exception ex = null;
         JarFile jar = null;
         try {
             jar = new JarFile(getJarPath());
             toRet = jar.getManifest();
         }
         catch (Exception e) {
             ex = e;
         }
         finally {
             if (jar != null) {
                 try {
                     jar.close();
                 }
                 catch (IOException e) {}
             }
             if (ex != null) {
                 throw ex;
             }
         }
         return toRet;
     }
 
     private final void readManifest() {
         try {
             Manifest manifest = getManifest();
             Attributes mainAttribs = manifest.getMainAttributes();
             version = Float.parseFloat(mainAttribs.getValue("Version").replace("-SNAPSHOT", ""));
             build = Short.parseShort(mainAttribs.getValue("Build"));
             buildTime = mainAttribs.getValue("Build-Time");
             try {
                 status = ProgramStatus.valueOf(mainAttribs.getValue("ProgramStatus"));
             }
             catch (IllegalArgumentException iaex) {
                 status = ProgramStatus.UNKNOWN;
             }
         }
         catch (Exception ex) {
             version = -1.0F;
             build = -1;
             buildTime = "19700101-0000";
         }
     }
 
     private final void checkStatus() {
         if (status == ProgramStatus.UNKNOWN) {
             getLogger().severe(String.format("%s has declared itself as an 'UNKNOWN STATUS' build. Use is not advised and could cause damage to your system!", getName()));
         }
         else if (status == ProgramStatus.ALPHA) {
             getLogger().warning(String.format("%s has declared itself as a 'ALPHA' build. Production use is not advised!", getName()));
         }
         else if (status == ProgramStatus.BETA) {
             getLogger().warning(String.format("%s has declared itself as a 'BETA' build. Production use is not advised!", getName()));
         }
         else if (status == ProgramStatus.RELEASE_CANDIDATE) {
             getLogger().info(String.format("%s has declared itself as a 'Release Candidate' build. Expect some bugs.", getName()));
         }
     }
 
     private final void checkVersion() {
         Boolean islatest = vc.isLatest();
         if (islatest == null) {
             getLogger().warning("VersionCheckerError: " + vc.getErrorMessage());
         }
         else if (!vc.isLatest()) {
             getLogger().warning(vc.getUpdateAvailibleMessage());
             getLogger().warning(String.format("You can view update info @ http://wiki.visualillusionsent.net/%s#ChangeLog", getName()));
         }
     }
 
     public final float getRawVersion() {
         return version;
     }
 
     public final short getBuildNumber() {
         return build;
     }
 
     public final String getBuildTime() {
         return buildTime;
     }
 
     public final VersionChecker getVersionChecker() {
         return vc;
     }
 
     private final String getJarPath() {
         try {
             CodeSource codeSource = this.getClass().getProtectionDomain().getCodeSource();
             return codeSource.getLocation().toURI().getPath();
         }
         catch (URISyntaxException ex) {}
         return "plugins/SearchIds.jar";
     }
 }
