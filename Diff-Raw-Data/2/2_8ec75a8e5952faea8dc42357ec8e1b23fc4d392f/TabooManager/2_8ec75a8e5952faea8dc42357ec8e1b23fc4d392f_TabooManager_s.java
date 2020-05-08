 /*
  * This file is part of Taboo.
  *
  * Copyright (c) 2013 CraftFire <http://www.craftfire.com/>
  * Taboo is licensed under the GNU Lesser General Public License.
  *
  * Taboo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Taboo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.taboo;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.lang.reflect.Constructor;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.channels.FileLock;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.craftfire.commons.util.LoggingManager;
 import com.craftfire.commons.yaml.Settings;
 import com.craftfire.commons.yaml.SimpleYamlManager;
 import com.craftfire.commons.yaml.YamlException;
 import com.craftfire.commons.yaml.YamlManager;
 import com.craftfire.commons.yaml.YamlNode;
 
 public class TabooManager {
     private File directory;
     private List<Taboo> taboos;
     private boolean onlyOnce;
     private boolean enableClassLoader;
     private Map<String, Action> actions;
     private LoggingManager logger = new LoggingManager("CraftFire.TabooManager", "[Taboo]");
     private URLClassLoader classLoader = null;
     private Layer layer;
 
     public TabooManager(Layer layer, File directory) {
         this.layer = layer;
         this.directory = directory;
     }
 
     public void load() throws TabooException {
         defaultFile(this.directory, "", "config.yml");
         YamlManager config = new SimpleYamlManager(new File(this.directory, "config.yml"), new Settings().setLogger(this.logger));
         if (!config.load()) {
             throw new TabooException("Failed to load the config");
         }
 
         synchronized (this) {
             loadSettings(config);
             if (this.enableClassLoader) {
                 setupClassLoader();
             }
 
             try {
                 this.actions = loadActions(config);
                 this.taboos = loadTaboos(config);
             } catch (YamlException e) {
                 throw new TabooException("Exception occurred during config loading.", e);
             }
         }
     }
 
     public String processMessage(String message, TabooPlayer player) {
         this.logger.debug("Processing message: \"" + message + "\" by player: " + player.getName());
         Iterator<Taboo> i = this.taboos.iterator();
         boolean onlyOnce = this.onlyOnce;   // For thread safety
         while (i.hasNext()) {
             Taboo taboo = i.next();
             this.logger.debug("Checking taboo " + taboo.getName());
             if (taboo.matches(message, player)) {
                 this.logger.debug("It matches!");
                 executeActions(taboo, player, message);
                 message = taboo.replace(message);
                 if (onlyOnce) {
                     break;
                 }
             }
         }
         return message;
     }
 
     public LoggingManager getLogger() {
         return this.logger;
     }
 
     public void setLoggingManager(LoggingManager loggingManager) {
         if (loggingManager == null) {
             throw new IllegalArgumentException("The loggingManager can't be null!");
         }
         this.logger = loggingManager;
     }
 
     public Layer getLayer() {
         return this.layer;
     }
 
     protected void executeActions(Taboo taboo, TabooPlayer player, String message) {
         this.logger.debug("Executing actions for taboo " + taboo.getName() + " on player " + player.getName());
         Map<String, Action> actions = this.actions; // For thread safety
         for (String actionName : taboo.getActions()) {
             Action action = actions.get(actionName);
             if (action != null) {
                 this.logger.debug("Executing action: " + actionName);
                 try {
                     action.execute(player, taboo, message);
                 } catch (Throwable t) {
                     this.logger.stackTrace(t);
                     this.logger.warning("Exception ocurred when executing action \"" + actionName + "\"");
                 }
             }
         }
     }
 
     protected void loadSettings(YamlManager config) {
         this.onlyOnce = config.getBoolean("match-once");
         this.enableClassLoader = config.getBoolean("enable-actions-classloader");
         this.logger.setDebug(config.getBoolean("debug"));
     }
 
     protected void setupClassLoader() {
         File actionsDir = new File(this.directory, "actions");
         if (!actionsDir.exists()) {
             actionsDir.mkdirs();
         }
         if (actionsDir.isDirectory()) {
             try {
                 this.classLoader = new URLClassLoader(new URL[] { this.directory.toURI().toURL() }, getClass().getClassLoader());
             } catch (MalformedURLException e) {
                 this.logger.stackTrace(e);
                 this.logger.warning("Could not create actions folder classloader: exception occurred");
             }
         } else {
             this.logger.warning("Could not create actions folder classloader: \"actions\" directory doesn't exist and can't be created");
         }
     }
 
     protected Map<String, Action> loadActions(YamlManager config) throws YamlException {
         Map<String, Action> actions = new HashMap<String, Action>();
         ClassLoader classLoader = this.classLoader; // For thread safety
         for (YamlNode node : config.getNode("actions").getChildrenList()) {
             String className = node.getChild("class").getString();
             Class<?> c = null;
             try {
                 c = Class.forName(className);
             } catch (ClassNotFoundException ignore) {
             }
             if (c == null || !Action.class.isAssignableFrom(c)) {
                 try {
                     c = Class.forName("com.craftfire.taboo.actions." + className);
                 } catch (ClassNotFoundException ignore) {
                 }
             }
             if (c == null || !Action.class.isAssignableFrom(c)) {
                 try {
                     if (classLoader != null) {
                         c = Class.forName(className, true, classLoader);
                     }
                 } catch (ClassNotFoundException e) {
                 }
             }
             if (c == null || !Action.class.isAssignableFrom(c)) {
                 this.logger.warning("Can't load action \"" + node.getName() + "\": class \"" + className + "\" not found or not a subclass of Action.");
                 continue;
             }
             Constructor<? extends Action> con;
             try {
                 con = c.asSubclass(Action.class).getConstructor(YamlNode.class);
                 actions.put(node.getName(), con.newInstance(node));
             } catch (Throwable e) {
                 this.logger.stackTrace(e);
                 this.logger.warning("Can't load action \"" + node.getName() + "\": exception during instantiation of class \"" + c.getName() + "\"");
             }
         }
         this.logger.info("Loaded " + actions.size() + " of " + config.getNode("actions").getChildrenCount() + " actions");
         return actions;
     }
 
     protected List<Taboo> loadTaboos(YamlManager config) throws YamlException {
         List<Taboo> taboos = new ArrayList<Taboo>();
         for (YamlNode node : config.getNode("taboos").getChildrenList()) {
             try {
                 taboos.add(new Taboo(this, node));
             } catch (TabooException e) {
                 this.logger.stackTrace(e);
                 this.logger.warning("Unable to create taboo \"" + node.getName() + "\"");
             }
         }
         this.logger.info("Loaded " + taboos.size() + " of " + config.getNode("taboos").getChildrenCount() + " taboos");
         return taboos;
     }
 
     protected void defaultFile(File directory, String resourceDirectory, String file) {
         this.logger.debug("Checking default file " + file + " in " + directory.getPath() + ", default file in " + resourceDirectory);
         if (!directory.exists()) {
             this.logger.info("Creating directory " + directory.getPath());
             directory.mkdirs();
         }
         File actual = new File(directory, file);
         this.logger.debug("Checking if file " + actual.getPath() + " exists");
         if (!actual.exists()) {
             String resourcePath;
             if (resourceDirectory.isEmpty()) {
                 resourcePath = file;
             } else {
                 resourcePath = resourceDirectory + File.separator + file;
             }
             this.logger.debug("File " + actual.getPath() + " doesn't exist. Checking default file in classpath at " + resourcePath);
             InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath);
             if (input != null) {
                 this.logger.debug("Found default file, attempting to copy");
                 RandomAccessFile output = null;
                 FileLock lock = null;
                 try {
                     output = new RandomAccessFile(actual, "rw");
                     lock = output.getChannel().lock();
                     byte[] buf = new byte[8192];
                     int length = 0;
                     while ((length = input.read(buf)) > 0) {
                         output.write(buf, 0, length);
                     }
                     this.logger.info("Written default setup for " + file);
                 } catch (Exception e) {
                     this.logger.stackTrace(e);
                 } finally {
                     try {
                         input.close();
                         if (lock != null) {
                             lock.release();
                         }
                         if (output != null) {
                             output.close();
                         }
                     } catch (Exception e) {
                         this.logger.stackTrace(e);
                     }
                 }
             }
         }
     }
 }
