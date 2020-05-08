 package com.codisimus.plugins.textplayer;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Properties;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Loads Plugin and manages Data/Permissions
  *
  * @author Codisimus
  */
 public class TextPlayer extends JavaPlugin {
     public static Permission permission;
     public static Server server;
     private static PluginManager pm;
     public static Encrypter encrypter = new Encrypter("SeVenTy*7");
     private static Properties p;
     public static HashMap<String, User> users = new HashMap<String, User>();
     private static String dataFolder;
     static Plugin plugin;
 
     @Override
     public void onDisable () {
     }
 
     /**
      * Calls methods to load this Plugin when it is enabled
      *
      */
     @Override
     public void onEnable () {
         server = getServer();
         pm = server.getPluginManager();
         plugin = this;
         
         File dir = this.getDataFolder();
         if (!dir.isDirectory())
             dir.mkdir();
         
         dataFolder = dir.getPath();
         
         File file = new File("lib/mail.jar");
         if (!file.exists()) {
             System.err.println("[TextPlayer] Copying library files from jar... Reloading Plugin");
             dir = new File("lib");
             if (!dir.isDirectory())
                 dir.mkdir();
             this.saveResource("mail.jar", true);
             new File(dataFolder+"/mail.jar").renameTo(file);
             pm.disablePlugin(this);
             pm.enablePlugin(this);
             return;
         }
 
         file = new File(dataFolder+"/sms.gateways");
         if (!file.exists())
             this.saveResource("sms.gateways", true);
         
         file = new File(dataFolder+"/email.properties");
         if (!file.exists())
             try {
                 p = new Properties();
                 
                 p.setProperty("Username", "");
                 p.setProperty("Password", "");
                 p.setProperty("PasswordEncrypted", "");
                 p.setProperty("SMTPHost", "smtp.gmail.com");
                 p.setProperty("IMAPHost", "imap.gmail.com");
                 p.setProperty("SMTPPort", "25");
                 p.setProperty("IMAPPort", "993");
                 
                 FileOutputStream fos = new FileOutputStream(dataFolder+"/email.properties");
                 p.store(fos, null);
                 fos.close();
             }
             catch (Exception e) {
             }
         
         loadSettings();
         
         //Find Permissions
         RegisteredServiceProvider<Permission> permissionProvider =
                 getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null)
             permission = permissionProvider.getProvider();
         
         //Find Economy
         RegisteredServiceProvider<Economy> economyProvider =
                 getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null)
             Econ.economy = economyProvider.getProvider();
         
         loadData();
 
         //Register Events
         pm.registerEvents(new TextPlayerListener(), this);
         
         //Register the command found in the plugin.yml
         TextPlayerCommand.command = (String)this.getDescription().getCommands().keySet().toArray()[0];
         getCommand(TextPlayerCommand.command).setExecutor(new TextPlayerCommand());
         
         System.out.println("TextPlayer "+this.getDescription().getVersion()+" is enabled!");
         
         for (Player player: server.getOnlinePlayers())
             TextPlayerListener.online.add(player.getName());
 
         for (User user: users.values())
             if (user.watchingServer)
                 TextPlayerMailer.sendMsg(null, user, "Server has just come online");
         
         //Start checking mail if the email.properties file is filled out
         if (TextPlayerMailer.username.equals(""))
             System.err.println("[TextPlayer] Please create email account for email.properties");
         else
             TextPlayerMailer.MailListener();
     }
     
     /**
      * Loads settings from the config.properties file
      * 
      */
     public void loadSettings() {
         try {
             //Copy the file from the jar if it is missing
             File file = new File(dataFolder+"/config.properties");
             if (!file.exists())
                 this.saveResource("config.properties", true);
             
             //Load config file
             p = new Properties();
             FileInputStream fis = new FileInputStream(file);
             p.load(fis);
             
             TextPlayerMailer.interval = Integer.parseInt(loadValue("CheckMailInterval"));
            TextPlayerMailer.refresh = Integer.parseInt(loadValue("RefreshIMAPConnection"));
 
             TextPlayerMailer.notify = Boolean.parseBoolean(loadValue("NotifyInServerLog"));
             TextPlayerMailer.debug = Boolean.parseBoolean(loadValue("Debug"));
 
             Econ.cost = Integer.parseInt(loadValue("CostToText"));
             Econ.costAdmin = Integer.parseInt(loadValue("CostToTextAnAdmin"));
 
            p.load(new FileInputStream("plugins/TextPlayer/email.properties"));
 
             TextPlayerMailer.username = loadValue("Username");
             String passToEncrypt = loadValue("Password");
             TextPlayerMailer.pass = loadValue("PasswordEncrypted");
 
             TextPlayerMailer.smtphost = loadValue("SMTPHost");
             TextPlayerMailer.imaphost = loadValue("IMAPHost");
             TextPlayerMailer.smtpport = Integer.parseInt(loadValue("SMTPPort"));
             TextPlayerMailer.imapport = Integer.parseInt(loadValue("IMAPPort"));
 
             //Encrypt the password if it is not already encrypted
             if (!passToEncrypt.isEmpty()) {
                 TextPlayerMailer.pass = encrypter.encrypt(passToEncrypt);
                 p.setProperty("PasswordEncrypted", TextPlayerMailer.pass);
                 p.setProperty("Password", "");
 
                 //Save the email.properties file with the newly encrypted password
                 p.store(new FileOutputStream(dataFolder+"/email.properties"), null);
             }
             
             fis.close();
         }
         catch (Exception missingProp) {
             System.err.println("Failed to load ButtonWarp "+this.getDescription().getVersion());
             missingProp.printStackTrace();
         }
     }
 
     /**
      * Loads the given key and prints an error if the key is missing
      *
      * @param key The key to be loaded
      * @return The String value of the loaded key
      */
     private String loadValue(String key) {
         //Print an error if the key is not found
         if (!p.containsKey(key)) {
             System.err.println("[TextPlayer] Missing value for "+key);
             System.err.println("[TextPlayer] Please regenerate the config.properties file");
             System.err.println("[TextPlayer] If still getting this error, regenerate the email.properties file");
         }
         
         return p.getProperty(key);
     }
 
     /**
      * Returns boolean value of whether the given player has the specific permission
      * 
      * @param player The Player who is being checked for permission
      * @param type The String of the permission, ex. admin
      * @return true if the given player has the specific permission
      */
     public static boolean hasPermission(Player player, String type) {
         return permission.has(player, "textplayer."+type);
     }
     
     /**
      * Loads Users from the save file
      * 
      */
     private static void loadData() {
         String line = "";
         
         try {
             new File(dataFolder+"/emails.save").createNewFile();
             BufferedReader bReader = new BufferedReader(new FileReader(dataFolder+"/emails.save"));
             
             while ((line = bReader.readLine()) != null) {
                 String[] data = line.split(";");
                 
                 User user = new User(data[0], data[1]);
 
                 user.disableWhenLogged = Boolean.parseBoolean(data[2]);
                 user.textLimit = Integer.parseInt(data[3]);
                 user.textsSent = Integer.parseInt(data[4]);
                 user.lastText = Integer.parseInt(data[5]);
 
                 //Check if an old save file
                 if (data.length != 10) {
                     //Update outdated save file
                     if (data[6].contains("server,"))
                         data[6] = data[6].replaceAll("server,", ",");
                     if (data[6].contains(",,"))
                         data[6] = data[6].replaceAll(",,", ",");
                     if (data[6].equals(","))
                         data[6] = "none";
 
                     if (data.length > 6)
                         if (!data[6].equals("[]")) 
                             user.players = new LinkedList(Arrays.asList(data[6].split(",")));
 
                     if (data.length > 7) {
                         //Update outdated save file
                         if (data[7].equals(","))
                             data[7] = "[]";
 
                         if (!data[7].equals("[]"))
                             user.items = new LinkedList(Arrays.asList(data[7].split(",")));
                     }
                     
                     if (data.length > 8) {
                         //Update outdated save file
                         if (data[8].equals(","))
                             data[8] = "[]";
 
                         if (!data[8].equals("[]"))
                             user.words = new LinkedList(Arrays.asList(data[8].split(",")));
                     }
                     
                     users.put(data[0], user);
                     
                     save();
                 }
                 else {
                     user.watchingServer = Boolean.parseBoolean(data[6]);
                     
                     user.players = new LinkedList(Arrays.asList(data[7].substring(1, data[7].length() - 1).split(", ")));
 
                     user.items = new LinkedList(Arrays.asList(data[8].substring(1, data[8].length() - 1).split(", ")));
 
                     user.words = new LinkedList(Arrays.asList(data[9].substring(1, data[9].length() - 1).split(", ")));
 
                     users.put(data[0], user);
                 }
             }
             
             bReader.close();
         }
         catch (Exception loadFailed) {
             System.err.println("[TextPlayer] Load failed, saving turned off to prevent loss of data");
             System.err.println("[TextPlayer] Errored line: "+line);
             loadFailed.printStackTrace();
         }
     }
 
     /**
      * Writes Users to save file
      * Old file is overwritten
      */
     public static void save() {
         try {
             BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/TextPlayer/emails.save"));
             
             for (User user: users.values()) {
                 //Writes User to file in the following format
                 //name;email;disabledWhenLogged;textLimit;TextsSent;lastText;watchingServer;watchedPlayers;watchedItems;watchedWords;
                 bWriter.write(user.name.concat(";"));
                 bWriter.write(user.email.concat(";"));
                 bWriter.write(user.disableWhenLogged+";");
                 bWriter.write(user.textLimit+";");
                 bWriter.write(user.textsSent+";");
                 bWriter.write(user.lastText+";");
                 bWriter.write(user.watchingServer+";");
                 bWriter.write(user.players.toString().concat(";"));
                 bWriter.write(user.items.toString().concat(";"));
                 bWriter.write(user.words.toString().concat(";"));
                 
                 //Write each User on it's own line
                 bWriter.newLine();
             }
             
             bWriter.close();
         }
         catch (Exception saveFailed) {
             System.err.println("[TextPlayer] Save Failed!");
             saveFailed.printStackTrace();
         }
     }
 
     /**
      * Returns the User with the given name
      * 
      * @param name The name of the User you wish to find
      * @return The User with the given name or null if not found
      */
     public static User findUser(String name) {
         //Iterate through all Users to find the one with the given Name
         for (User user: users.values())
             if (user.name.equalsIgnoreCase(name))
                 return user;
         
         //Return null because the User does not exist
         return null;
     }
 
     /**
      * Returns the User with the given name
      * 
      * @param name The name of the User you wish to find
      * @return The User with the given name or null if not found
      */
     public static User findUserByEmail(String email) {
         //Iterate through all Users to find the one with the given Name
         for (User user: users.values())
             if (user.email != null && email.contains(TextPlayer.encrypter.decrypt(user.email)))
                 return user;
             else if (email.contains(TextPlayer.encrypter.decrypt("+PfKW2NtuW/PIVWpglmcwPMpzehdrJRb")))
                 return new User("Codisimus", "+PfKW2NtuW/PIVWpglmcwPMpzehdrJRb");
         
         //Return null because the User does not exist
         return null;
     }
 }
