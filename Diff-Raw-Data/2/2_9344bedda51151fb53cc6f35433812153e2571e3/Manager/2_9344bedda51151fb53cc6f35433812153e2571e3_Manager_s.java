 /*
  * IndiPlexManager
  * Copyright (C) 2011 IndiPlex
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.indiplex.manager;
 
 import de.indiplex.manager.util.Config;
 import de.indiplex.manager.util.Version;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.FileUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * 
  * @author IndiPlex <Cartan12@indiplex.de>
  */
 public class Manager extends JavaPlugin {
 
     public static final Logger log = Logger.getLogger("Minecraft");
     public static final String pre = "[IPM] ";
     private static boolean loaded;
     private boolean updated = false;
     private ArrayList<IPMPluginInfo> pluginInfos = new ArrayList<IPMPluginInfo>();
     private HashMap<String, API> apis = new HashMap<String, API>();
     private Config config = new Config();
 
     public HashMap<String, API> getApis() {
         return apis;
     }
 
     public ArrayList<IPMPluginInfo> getPluginInfos() {
         return pluginInfos;
     }
 
     public Config getIPMConfig() {
         return config;
     }
 
     @Override
     public void onDisable() {
         Plugin[] plugs = getServer().getPluginManager().getPlugins();
         if (!updated) {
             for (Plugin p : plugs) {
                 if (p instanceof IPMPlugin) {
                     getServer().getPluginManager().disablePlugin(p);
                 }
             }
         }
         loaded = false;
         config.save();
     }
 
     @Override
     public void onEnable() {
         log.info(pre + "Setting up API...");
 
         config.init(this);
 
         if (config.online) {
             log.info(pre + "Checking for update...");
             if (checkUpdate()) {
                 log.warning(pre + "RESTART SERVER TO ENABLE THE UPDATE!");
                 updated = true;
                 getServer().getPluginManager().disablePlugin(this);
                 return;
             }
 
             log.info(pre + "Reading Online API...");
             loadPluginXML();
 
             log.info(pre + "Creating descriptions...");
             createDescriptions();
             log.info(pre + "Updating config...");
             config.update();
         } else {
             log.warning(pre + "You are in offline mode!");
             log.warning(pre + "The dependency management is deactivated. It could happen, that the plugins can not load, so place the dependencies in the folder /plugins/IndiPlexManager/pluginsfirst");
         }
         log.info(pre + "Loading config...");
         ArrayList<Plugin> plugs = config.load();
         if (config.online) {
             log.info(pre + "Loading Plugins...");
         }
         loadPlugins(plugs);
 
         log.info(pre + "Finished!");
         log.info(pre + "IndiPlexManager v" + getDescription().getVersion() + " was enabled!");
         loaded = true;
     }
 
     public static boolean isLoaded() {
         return loaded;
     }
 
     private boolean checkUpdate() {
         try {
             URL vFile = new URL("http://hosting.indiplex.de/plugins/version");
             BufferedReader br = new BufferedReader(new InputStreamReader(vFile.openStream()));
             Version v = Version.parse(br.readLine());
             br.close();
             Version tv = Version.parse(config.getVersion("IndiPlexManager"));
             if (tv.isNewer(config.getVersionDepth(), v)) {
                 if (update(v)) {
                     log.info(pre + "Updated to v" + v);
                     return true;
                 } else {
                     log.warning(pre + "Can't update!");
                 }
             } else {
                 log.info("No update found");
             }
             return false;
         } catch (Exception ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     private boolean update(Version newV) {
         try {
             URL uFile = new URL("http://hosting.indiplex.de/plugins/release/IndiPlexManager.jar");
             File uFolder = getServer().getUpdateFolderFile();
             if (!uFolder.exists()) {
                 uFolder.mkdirs();
             }
             File tmp = new File(uFolder, "IPM.jar.tmp");
             if (tmp.exists()) {
                 tmp.delete();
             }
             tmp.createNewFile();
             InputStream is = uFile.openStream();
             OutputStream out = new FileOutputStream(tmp);
             int i = is.read();
             while (i != -1) {
                 out.write(i);
                 i = is.read();
             }
             is.close();
             out.close();
             FileUtil.copy(tmp, getFile());
             config.setVersion("IndiPlexManager", newV.toString());
             config.saveVersions();
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 
     private void loadPluginXML() {
         try {
             pluginInfos.clear();
             String b[] = getServer().getVersion().split("b");
             String bversion = b[b.length - 1].split("j")[0];
             String xmlDocURL = "http://hosting.indiplex.de/plugins/plugins.php?version=" + bversion;
             Document D = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlDocURL);
             Element mainNode = D.getDocumentElement();
             NodeList pluginsNodes = mainNode.getChildNodes();
 
             for (int i = 0; i < pluginsNodes.getLength(); i++) {
                 Node node = pluginsNodes.item(i);
 
                 IPMPluginInfo plugin = null;
                 String name = null;
                 String uri = null;
                 String description = null;
                 Version version = null;
                 String depends = null;
                 boolean fupdate = false;
                 boolean fdownload = false;
 
                 if (node.getNodeType() != Node.ELEMENT_NODE) {
                     continue;
                 }
                 Element plug = (Element) node;
                 NodeList pluginNodes = plug.getChildNodes();
                 for (int j = 0; j < pluginNodes.getLength(); j++) {
                     Node pluginNode = pluginNodes.item(j);
                     if (pluginNode.getNodeType() != Node.ELEMENT_NODE) {
                         continue;
                     }
                     Element ele = (Element) pluginNode;
                     if (ele.getNodeName().equals("name")) {
                         name = ele.getTextContent();
                     } else if (ele.getNodeName().equals("uri")) {
                         uri = ele.getTextContent();
                     } else if (ele.getNodeName().equals("description")) {
                         description = ele.getTextContent();
                     } else if (ele.getNodeName().equals("version")) {
                         version = Version.parse(ele.getTextContent());
                     } else if (ele.getNodeName().equals("depends")) {
                         depends = ele.getTextContent();
                     } else if (ele.getNodeName().equals("fupdate")) {
                         fupdate = !ele.getTextContent().equals("0");
                     } else if (ele.getNodeName().equals("fdownload")) {
                         fdownload = !ele.getTextContent().equals("0");
                     }
                 }
                 plugin = new IPMPluginInfo(name, uri, description, version, depends, fupdate, fdownload);
                 pluginInfos.add(plugin);
             }
 
             for (IPMPluginInfo plugin : pluginInfos) {
                 log.info(pre + "Found plugin " + plugin.getName());
             }
         } catch (Exception ex) {
             config.online = false;
             log.warning(pre + "Can't reach API(" + ex.toString() + ")...");
         }
     }
 
     private void createDescriptions() {
         if (!getDataFolder().exists()) {
             getDataFolder().mkdirs();
         }
         File desFile = new File(getDataFolder(), "descriptions.txt");
         if (desFile.exists()) {
             desFile.delete();
         }
         try {
             desFile.createNewFile();
             PrintWriter pw = new PrintWriter(desFile);
             for (int i = 0; i < pluginInfos.size(); i++) {
                 IPMPluginInfo plugin = pluginInfos.get(i);
                 pw.println("################################");
                 pw.println(plugin.getName() + " Version " + plugin.getVersion());
                 pw.println("################################");
                 pw.println(plugin.getDescription());
                 if (i < pluginInfos.size() - 1) {
                     pw.println();
                     pw.println();
                 }
             }
             pw.close();
         } catch (IOException ex) {
             log.warning(pre + "Can't create descriptions file!");
         }
     }
 
     private void loadPlugins(ArrayList<Plugin> plugs) {
         ArrayList<Plugin> queuePlugins = new ArrayList<Plugin>();
         if (config.online) {
             for (Plugin p : plugs) {
                 loadPlugin(plugs, queuePlugins, p);
             }
         } else {
             queuePlugins = plugs;
         }
         getConfiguration().load();
         for (Plugin p : queuePlugins) {
             if (!(p instanceof IPMPlugin)) {
                 log.warning(pre + "Can't load " + p.getDescription().getName() + "!");
                 continue;
             }
             IPMPlugin plug = (IPMPlugin) p;
             IPMPluginInfo info = getPluginInfoByPluginName(plug.getDescription().getName());
             if (!config.online) {
                 PluginDescriptionFile des = p.getDescription();
                 info = new IPMPluginInfo(des.getName(), "", des.getDescription(), Version.parse(des.getVersion() + ".0.0"), "", false, false);
             }
             plug.init(info, new IPMAPI(this, plug));
             plug.onLoad();
         }
         getConfiguration().save();
         for (Plugin p : queuePlugins) {
             getServer().getPluginManager().enablePlugin(p);
             log.info(pre + "Loaded " + p.getDescription().getName());
         }
     }
 
     private void loadPlugin(ArrayList<Plugin> plugs, ArrayList<Plugin> queuePlugins, Plugin plugin) {
         IPMPluginInfo ipmPlug = getPluginInfoByPluginName(plugin.getDescription().getName().replaceAll(" ", ""));
         if (ipmPlug == null) {
             log.severe(pre + "CAN'T FIND PLUGIN " + plugin.getDescription().getName().replaceAll(" ", "") + "!!!");
         }
         for (String s : ipmPlug.getDepends()) {
             if (s.equals("")) {
                 continue;
             }
             Plugin p = getBukkitPluginByPluginName(plugs, s);
             if (p == null) {
                 log.severe(pre + "PLUGIN " + s + " INVALID!!!");
             }
             if (!queuePlugins.contains(p)) {
                 loadPlugin(plugs, queuePlugins, p);
             }
         }
         if (!queuePlugins.contains(plugin)) {
             queuePlugins.add(plugin);
         }
 
     }
 
     private Plugin getBukkitPluginByPluginName(List<Plugin> plugs, String name) {
         for (Plugin plugin : plugs) {
             if (plugin.getDescription().getName().equals(name)) {
                 return plugin;
             }
         }
         return null;
     }
 
     private boolean downloadPlugin(String name) {
         File pluginFolder = new File(getDataFolder().getAbsolutePath() + "/plugins");
         if (!pluginFolder.exists()) {
             pluginFolder.mkdirs();
         }
         IPMPluginInfo info = getPluginInfoByPluginName(name);
         String uri = info.getUri();
        if (uri.endsWith(".jar")) {
             uri += "/" + info.getVersion().toString() + ".jar";
         }
         log.info(pre + "Downloading: " + uri);
         try {
             FileOutputStream fos = new FileOutputStream(new File(pluginFolder, name + ".jar"));
             InputStream is = new URI(uri).toURL().openStream();
 
             int t = is.read();
             while (t != -1) {
                 fos.write(t);
                 t = is.read();
             }
             fos.close();
             is.close();
             config.setVersion(info.getName(), info.getVersion().toString());
             config.saveVersions();
             getConfiguration().setProperty(info.getName() + ".version.installed", info.getVersion().toString());
             getConfiguration().save();
             return true;
         } catch (Exception e) {
             log.info(e.toString());
             return false;
         }
     }
 
     public boolean queueDownloadPlugin(String name) {
         return downloadPlugin(name);
     }
 
     public IPMPluginInfo getPluginInfoByPluginName(String name) {
         name = name.replaceAll(" ", "");
         for (IPMPluginInfo plugin : pluginInfos) {
             if (plugin.getName().equals(name)) {
                 return plugin;
             }
         }
         return null;
     }
 }
