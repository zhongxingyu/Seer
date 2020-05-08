 /*******************************************************************************
  * Copyright (c) 2012 MCForge.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package net.mcforge.util.properties;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import net.mcforge.server.Server;
 import net.mcforge.util.FileUtils;
 
 public class Properties {
     private ArrayList<String> settings = new ArrayList<String>();
     private static boolean init = false;
 
     /**
      * Load server settings
      * <b>This method can only be called once</b>
      * @param server
      *              The server to load for
      * @return
      *        The {@link Properties} object
      */
     public static Properties init(Server server) {
         if (init)
             return null;
         Properties p = new Properties();
         if (!new File("properties").exists())
             new File("properties").mkdir();
         if (!new File("properties/" + server.configpath).exists())
             makeDefaults(server.configpath, server, p);
         else {
             try {
                 p.load(server.configpath);
                 init = true;
             } catch (IOException e) {
                 server.Log("ERROR LOADING CONFIG!");
                 e.printStackTrace();
             }
         }
         return p;
 
     }
 
     /**
      * Reset the init value for System Properties
      */
     public static void reset() {
         init = false;
     }
 
 
     private static Properties getDefaults(Properties p) {
         p.addSetting("Server-Name", "[MCForge] Default Server");
         p.addComment("Server-Name", "The name of the server");
         p.addSetting("WOM-Alternate-Name", "[MCForge] Default Server");
         p.addComment("WOM-Alternate-Name", "The name that will appear in the WoM Direct list.");
         p.addSetting("MOTD", "Welcome!");
         p.addComment("MOTD", "The message players see on the loading screen.");
         p.addSetting("Port", 25565);
         p.addComment("Port", "The port to listen on. The default port is 25565.");
         p.addSetting("Max-Players", 30);
         p.addComment("Max-Players", "The max number of players that are allowed on.");
         p.addSetting("Public", true);
         p.addComment("Public", "Weather the server will be listed on minecraft.net");
         p.addSetting("Verify-Names", true);
         p.addComment("Verify-Names", "Check to see if players are logged in.");
         p.addSetting("Advanced-Sand", true);
         p.addComment("Advanced-Sand", "Weather sand will use the new physics system or the old one. New - Looks nicer, Old - Saves on CPU/Memory");
         p.addSetting("default_update_type", "auto_silent");
        p.addSetting("defaultColor", "&a");
         p.addComment("defaultColor", "The server's default color");
         p.addSetting("WOM-Server-description", "A server");
         p.addComment("WOM-Server-description", "A description of the server");
         p.addSetting("WOM-Server-Flags", "[MCForge]");
         p.addComment("WOM-Server-Flags", "Flags that will appear in the WoM Direct list");
         p.addSetting("MainLevel", "Main");
         p.addComment("MainLevel", "The main level players join when they enter the server");
         p.addSetting("Money-Name", "moneys");
         p.addComment("Money-Name", "The name for currency on this server.");
         p.addSetting("SQL-Driver", "net.mcforge.sql.SQLite");
         p.addComment("SQL-Driver", "The SQL Driver to use. SQLite = net.mcforge.sql.SQLite. MySQL = net.mcforge.sql.MySQL");
         p.addSetting("SQL-table-prefix", "mcf");
         p.addComment("SQL-table-prefix", "The prefix that will appear in table names owned by the server.");
         p.addSetting("MySQL-username", "root");
         p.addComment("MySQL-username", "If using MySQL, the username for MySQL");
         p.addSetting("MySQL-password", "password");
         p.addComment("MySQL-password", "If using MySQL, the password for the username specified in \"MySQL-username\"");
         p.addSetting("MySQL-database-name", "ggsdb");
         p.addComment("MySQL-database-name", "If using MySQL, the database name to use.");
         p.addSetting("MySQL-IP", "127.0.0.1");
         p.addComment("MySQL-IP", "If using MySQL, the IP of the MySQL server");
         p.addSetting("MySQL-Port", 3306);
         p.addComment("MySQL-Port", "If using MySQL, the port for the MySQL server");
         p.addSetting("SQLite-File", "mcf.db");
         p.addComment("SQLite-File", "If using SQLite, the filename it should save as.");
         return p;
     }
     
     private static void makeDefaults(String filename, Server server, Properties p) {
         //TODO Fill in all defaults
         server.Log("System config not found..creating..");
         p = getDefaults(p);
         try {
             p.save(filename);
             init = true;
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Save the properties file
      * @param filename
      *                The file to save it as
      * @throws IOException
      *                    This is thrown if there's an error saving the file
      */
     public void save(String filename) throws IOException {
         String truefile = (filename.indexOf("properties/") != -1 ? filename : "properties/" + filename);
         if (new File(truefile).exists())
             new File(truefile).delete();
         FileUtils.createChildDirectories(truefile);
         new File(truefile).createNewFile();
         PrintWriter out = new PrintWriter(truefile);
         for (String s : settings) {
             if (s.trim().equals(""))
                 continue;
             out.println(s);
             out.println(System.getProperty("line.separator"));
         }
         out.close();
     }
 
     /**
      * Load the properties file. 
      * This will reload the settings in memory.
      * @param filename
      *                The name of the file
      * @throws IOException
      *                     This is thrown if there's an error reading the file
      */
     public void load(String filename) throws IOException {
         if (settings.size() > 0)
             settings.clear();
         String truefile = (filename.indexOf("properties/") != -1 ? filename : "properties/" + filename);
         if (!new File(truefile).exists())
             return;
         FileInputStream fstream = new FileInputStream(truefile);
         DataInputStream in = new DataInputStream(fstream);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String strLine;
         while ((strLine = br.readLine()) != null)   {
             settings.add(strLine);
         }
         in.close();
     }
 
     /**
      * Get the boolean value of the setting <b>key</b>
      * @param key
      *           The setting to lookup
      * @return
      *         returns true if the setting equals "true" (regardless of casing)
      *         returns false if <b>any other value</b> is found
      */
     public boolean getBool(String key) {
         return getValue(key).equalsIgnoreCase("true");
     }
 
     /**
      * Get the int value of the setting <b>key</b>
      * @param key
      *           The setting to lookup
      * @return
      *         The int value of that setting
      * @throws NumberFormatException
      *                              If the setting does not contain a number,
      *                              then a {@link NumberFormatException} is thrown
      */
     public int getInt(String key) throws NumberFormatException {
         int toreturn = -1;
         toreturn = Integer.parseInt(getValue(key));
         return toreturn;
     }
 
     /**
      * Weather this properties file has a value for a setting
      * @param key
      *          The setting to search
      * @return
      *        True if it does have a value
      *        False if the setting doesnt exist.
      */
     public boolean hasValue(String key) {
         return !getValue(key).equals("null");
     }
 
     /**
      * Add a comment on top of a setting.
      * @param key
      *           The setting that the comment will be for.
      * @param comment
      *               The comment to add.
      */
     public void addComment(String key, String comment) {
         if (!comment.startsWith("#"))
             comment = "#" + comment;
         if (!comment.endsWith("."))
             comment += ".";
         for (int i = 0; i < settings.size(); i++) {
             if (settings.get(i).startsWith("#"))
                 continue;
             if (settings.get(i).split("=")[0].trim().equalsIgnoreCase(key)) {
                 settings.add(i, comment);
                 return;
             }
         }
     }
 
     /**
      * Get the value of the setting <b>key</b>
      * If no setting is found, then <b>"null"</b> will be
      * returned, however, the string value returned <b>WONT</b>
      * be null, the string value returned will equal "null"
      * @param 
      *        key The setting to get
      * @return 
      *        The value of that setting, if no value is found, then it will return
      *        "null"
      */
     public String getValue(String key) {
         synchronized(settings) {
             for (String k : settings) {
                 if (k.startsWith("#"))
                     continue;
                 String finalk = k.split("=")[0].trim();
                 if (finalk.equalsIgnoreCase(key))
                     return k.split("=")[1].trim();
             }
             return "null";
         }
     }
 
     /**
      * Change a setting in the properties file
      * This can also be used to add a setting, but its
      * recommended you use {@link #addSetting(String, boolean)}
      * to add a setting.
      * @param 
      *       key The name of the setting
      * @param 
      *       value The new value for this setting
      */
     public void updateSetting(String key, boolean value) {
         updateSetting(key, (value) ? "true" : "false");
     }
 
     /**
      * Change a setting in the properties file
      * This can also be used to add a setting, but its
      * recommended you use {@link #addSetting(String, int)}
      * to add a setting.
      * @param 
      *       key The name of the setting
      * @param
      *       value The new value for this setting
      */
     public void updateSetting(String key, int value) {
         updateSetting(key, "" + value);
     }
 
     /**
      * Change a setting in the properties file
      * This can also be used to add a setting, but its
      * recommended you use {@link #addSetting(String, String)}
      * to add a setting.
      * @param 
      *        key The name of the setting
      * @param 
      *        value The new value for this setting
      */
     public void updateSetting(String key, String value) {
         removeSetting(key);
         addSetting(key, value);
     }
 
     /**
      * Add a setting to the properties file
      * @param 
      *       key The name of the setting
      * @param 
      *       value The default value for this setting
      */
     public void addSetting(String key, boolean value) {
         addSetting(key, (value) ? "true" : "false");
     }
 
     /**
      * Add a setting to the properties file
      * @param 
      *        key The name of the setting
      * @param 
      *        value The default value for this setting
      */
     public void addSetting(String key, int value) {
         addSetting(key, "" + value);
     }
 
     /**
      * Add a setting to the properties file
      * @param 
      *        key The name of the setting
      * @param 
      *        value The default value for this setting
      */
     public void addSetting(String key, String value) {
         synchronized(settings) {
             settings.add(key + " = " + value);
         }
     }
 
     /**
      * Remove a setting from the properties file
      * @param 
      *       key The key of the setting
      */
     public void removeSetting(String key) {
         if (getValue(key).equals("null"))
             return;
         synchronized(settings) {
             for (int i = 0; i < settings.size(); i++) {
                 if (settings.get(i).startsWith("#"))
                     continue;
                 if (settings.get(i).split("=")[0].trim().equalsIgnoreCase(key)) {
                     settings.remove(i);
                     break;
                 }
             }
         }
     }
 }
