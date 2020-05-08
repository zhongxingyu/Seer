 /*
  * This file is part of QuarterBukkit-Integration.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * QuarterBukkit-Integration is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * QuarterBukkit-Integration is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with QuarterBukkit-Integration. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.quarterbukkit;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.InvalidDescriptionException;
 import org.bukkit.plugin.InvalidPluginException;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.UnknownDependencyException;
 
 /**
  * This class is used for integrating QuarterBukkit into a {@link Plugin}.
  */
 public class QuarterBukkitIntegration {
 
     private static final String PLUGIN_NAME = "QuarterBukkit-Plugin";
 
     private static final String TITLE_TAG   = "title";
     private static final String LINK_TAG    = "link";
     private static final String ITEM_TAG    = "item";
 
     private static URL          feedUrl;
 
     static {
 
         URL feed = null;
         try {
             feed = new URL("http://dev.bukkit.org/server-mods/quarterbukkit/files.rss");
         }
         catch (MalformedURLException e) {
             Bukkit.getLogger().severe("Error while initalizing URL (" + e + ")");
         }
 
         feedUrl = feed;
 
     }
 
     /**
      * Call this method in onEnable() for integrating QuarterBukkit into your plugin.
      * It creates a config where the user has to turn a value to "Yes" for the actual installation.
      * The class notfies him on the console and every time an op joins to the server.
      * If QuarterBukkit couldn't be installed or can only be installed after a restart, the calling {@link Plugin} is disabled.
      * 
      * @param plugin The {@link Plugin} which tries to integrate QuarterBukkit.
      * @return True if QuarterBukkit can be used after the call, false if not.
      */
     public static boolean integrate(final Plugin plugin) {
 
         if (new File("plugins/" + PLUGIN_NAME + "_extract").exists()) {
             deleteRecursive(new File("plugins/" + PLUGIN_NAME + "_extract"));
         }
 
         File installConfigFile = new File("plugins/" + PLUGIN_NAME, "install.yml");
 
         try {
             if (!Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME)) {
                 if (!installConfigFile.exists()) {
                     YamlConfiguration installConfig = new YamlConfiguration();
                     installConfig.set("install-" + PLUGIN_NAME, true);
                     installConfig.save(installConfigFile);
                 } else {
                     YamlConfiguration installConfig = YamlConfiguration.loadConfiguration(installConfigFile);
                     if (installConfig.isBoolean("install-" + PLUGIN_NAME) && installConfig.getBoolean("install-" + PLUGIN_NAME)) {
                         installConfigFile.delete();
                         install(new File("plugins", PLUGIN_NAME + ".jar"));
                         return true;
                     }
                 }
             } else {
                 return true;
             }
 
             new Timer().schedule(new TimerTask() {
 
                 @Override
                 public void run() {
 
                     Bukkit.broadcastMessage(ChatColor.YELLOW + "===============[ " + PLUGIN_NAME + " Installation ]===============");
                     Bukkit.broadcastMessage(ChatColor.RED + "For using " + plugin.getName() + " which requires " + PLUGIN_NAME + ", you should " + ChatColor.DARK_AQUA + "restart" + ChatColor.RED + " the server!");
                 }
             }, 100, 3 * 1000);
         }
         catch (UnknownHostException e) {
             Bukkit.getLogger().warning("Can't connect to dev.bukkit.org for installing " + PLUGIN_NAME + "!");
         }
         catch (Exception e) {
             Bukkit.getLogger().severe("An error occurred while installing " + PLUGIN_NAME + " (" + e + ")");
             e.printStackTrace();
         }
 
         Bukkit.getPluginManager().disablePlugin(plugin);
         return false;
     }
 
     private static void install(File target) throws IOException, XMLStreamException, UnknownDependencyException, InvalidPluginException, InvalidDescriptionException {
 
         Bukkit.getLogger().info("===============[ " + PLUGIN_NAME + " Installation ]===============");
         Bukkit.getLogger().info("Installing " + PLUGIN_NAME + " ...");
 
         Bukkit.getLogger().info("Downloading " + PLUGIN_NAME + " ...");
         File zipFile = new File(target.getParentFile(), PLUGIN_NAME + "_download.zip");
         URL url = new URL(getFileURL(getFeedData().get("link")));
         InputStream inputStream = url.openStream();
         OutputStream outputStream = new FileOutputStream(zipFile);
         outputStream.flush();
 
         byte[] tempBuffer = new byte[4096];
         int counter;
         while ( (counter = inputStream.read(tempBuffer)) > 0) {
             outputStream.write(tempBuffer, 0, counter);
             outputStream.flush();
         }
 
         inputStream.close();
         outputStream.close();
 
         Bukkit.getLogger().info("Extracting " + PLUGIN_NAME + " ...");
         File unzipDir = new File(target.getParentFile(), PLUGIN_NAME + "_extract");
         unzipDir.mkdirs();
         unzip(zipFile, unzipDir);
        File unzipInnerDir = unzipDir.listFiles()[0];
        copy(new File(unzipInnerDir, target.getName()), target);
         zipFile.delete();
         deleteRecursive(unzipDir);
 
         Bukkit.getLogger().info("Loading " + PLUGIN_NAME + " ...");
         Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(target));
 
         Bukkit.getLogger().info("Successfully installed " + PLUGIN_NAME + "!");
         Bukkit.getLogger().info("Enabling other plugins ...");
     }
 
     private static void unzip(File zip, File destination) throws ZipException, IOException {
 
         ZipFile zipFile = new ZipFile(zip);
 
         for (ZipEntry zipEntry : Collections.list(zipFile.entries())) {
             File file = new File(destination, zipEntry.getName());
             byte[] BUFFER = new byte[0xFFFF];
 
             if (zipEntry.isDirectory()) {
                 file.mkdirs();
             } else {
                 new File(file.getParent()).mkdirs();
 
                 InputStream inputStream = zipFile.getInputStream(zipEntry);
                 OutputStream outputStream = new FileOutputStream(file);
 
                 for (int lenght; (lenght = inputStream.read(BUFFER)) != -1;) {
                     outputStream.write(BUFFER, 0, lenght);
                 }
                 if (outputStream != null) {
                     outputStream.close();
                 }
                 if (inputStream != null) {
                     inputStream.close();
                 }
             }
         }
 
         zipFile.close();
     }
 
     private static void copy(File source, File destination) throws FileNotFoundException, IOException {
 
         if (source.isDirectory()) {
             destination.mkdirs();
 
             for (File entry : source.listFiles()) {
                 copy(new File(source, entry.getName()), new File(destination, entry.getName()));
             }
         } else {
             byte[] buffer = new byte[32768];
 
             InputStream inputStream = new FileInputStream(source);
             OutputStream outputStream = new FileOutputStream(destination);
 
             int numberOfBytes;
             while ( (numberOfBytes = inputStream.read(buffer)) > 0) {
                 outputStream.write(buffer, 0, numberOfBytes);
             }
 
             inputStream.close();
             outputStream.close();
         }
     }
 
     private static void deleteRecursive(File file) {
 
         if (file.isDirectory()) {
             for (File entry : file.listFiles()) {
                 deleteRecursive(entry);
             }
         }
 
         file.delete();
     }
 
     private static String getFileURL(String link) throws IOException {
 
         URL url = new URL(link);
         URLConnection connection = url.openConnection();
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
         String line;
         while ( (line = reader.readLine()) != null) {
             if (line.contains("<li class=\"user-action user-action-download\">")) {
                 return line.split("<a href=\"")[1].split("\">Download</a>")[0];
             }
         }
         connection = null;
         reader.close();
 
         return null;
     }
 
     private static Map<String, String> getFeedData() throws IOException, XMLStreamException {
 
         Map<String, String> returnMap = new HashMap<String, String>();
         String title = null;
         String link = null;
 
         XMLInputFactory inputFactory = XMLInputFactory.newInstance();
         InputStream inputStream = feedUrl.openStream();
         XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);
 
         while (eventReader.hasNext()) {
             XMLEvent event = eventReader.nextEvent();
             if (event.isStartElement()) {
                 if (event.asStartElement().getName().getLocalPart().equals(TITLE_TAG)) {
                     event = eventReader.nextEvent();
                     title = event.asCharacters().getData();
                     continue;
                 }
                 if (event.asStartElement().getName().getLocalPart().equals(LINK_TAG)) {
                     event = eventReader.nextEvent();
                     link = event.asCharacters().getData();
                     continue;
                 }
             } else if (event.isEndElement()) {
                 if (event.asEndElement().getName().getLocalPart().equals(ITEM_TAG)) {
                     returnMap.put("title", title);
                     returnMap.put("link", link);
                     return returnMap;
                 }
             }
         }
 
         return returnMap;
     }
 
 }
