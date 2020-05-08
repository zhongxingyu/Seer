 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.zack6849.alphabot.api;
 
 import com.zack6849.alphabot.Main;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Config
 {
 
     private boolean debug;
     private boolean autoNickChange;
     private boolean autoReconnectServer;
     private boolean autoRejoinChannel;
     private boolean autoAcceptInvite;
     private boolean useSSL;
     private boolean verifySSL;
     private String trigger;
     private String serverHostame;
     private String serverPassword;
     private String serverPort;
     private String botNickname;
     private String botUsername;
     private String botIdent;
     private String botPassword;
     private String ctcpFinger;
     private String ctcpVersion;
     private List<String> channels;
     private List<String> loggedChannels;
     private String permissionDenied;
     private Properties properties;
 
     public void load()
     {
         try
         {
             properties = new Properties();
             File config = new File("bot.properties");
             if (!config.exists())
             {
                 System.out.println("[!!] No configuration file found! generating a new one! [!!]");
                 BufferedReader s = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/bot.properties")));
                 String tmp = "";
                 config.createNewFile();
                 BufferedWriter out = new BufferedWriter(new FileWriter(config));
                 while ((tmp = s.readLine()) != null)
                 {
                     out.write(tmp);
                     out.flush();
                     out.newLine();
                 }
                 out.close();
                 System.out.println("[!!] Done! [!!]");
             }
             properties.load(new FileInputStream("bot.properties"));
             this.setBotNickname(properties.getProperty("bot-nickname"));
             this.setBotUsername(properties.getProperty("bot-username"));
             this.setBotIdent(properties.getProperty("bot-ident"));
             this.setBotPassword(properties.getProperty("bot-password"));
             this.setTrigger(properties.getProperty("bot-trigger"));
             this.setCtcpFinger(properties.getProperty("ctcp-finger-reply"));
             this.setCtcpVersion(properties.getProperty("ctcp-version-reply"));
             this.setDebug(Boolean.parseBoolean(properties.getProperty("debug")));
             this.setAutoNickChange(Boolean.parseBoolean(properties.getProperty("auto-nickchange")));
             this.setAutoReconnectServer(Boolean.parseBoolean(properties.getProperty("auto-reconnect")));
             this.setAutoRejoinChannel(Boolean.parseBoolean(properties.getProperty("auto-rejoin")));
             this.setAutoAcceptInvite(Boolean.parseBoolean(properties.getProperty("auto-accept-invite")));
             this.setChannels(Arrays.asList(properties.getProperty("channels").split(" ")));
             this.setLoggedChannels(Arrays.asList(properties.getProperty("channels-log").split(" ")));
             this.setUseSSL(Boolean.parseBoolean(properties.getProperty("use-ssl")));
             this.setVerifySSL(Boolean.parseBoolean(properties.getProperty("verfy-ssl")));
             this.setServerHostame(properties.getProperty("server-hostname"));
             this.setServerPort(properties.getProperty("server-port"));
             this.setServerPassword(properties.getProperty("server-password"));
             this.setPermissionDenied(properties.getProperty("permission-denied"));
 
         } catch (IOException ex)
         {
             Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * @return the debug
      */
     public boolean isDebug()
     {
         return debug;
     }
 
     /**
      * @param debug the debug to set
      */
     public void setDebug(boolean debug)
     {
         this.debug = debug;
     }
 
     /**
      * @return the autoNickChange
      */
     public boolean isAutoNickChange()
     {
         return autoNickChange;
     }
 
     /**
      * @param autoNickChange the autoNickChange to set
      */
     public void setAutoNickChange(boolean autoNickChange)
     {
         this.autoNickChange = autoNickChange;
     }
 
     /**
      * @return the autoReconnectServer
      */
     public boolean isAutoReconnectServer()
     {
         return autoReconnectServer;
     }
 
     /**
      * @param autoReconnectServer the autoReconnectServer to set
      */
     public void setAutoReconnectServer(boolean autoReconnectServer)
     {
         this.autoReconnectServer = autoReconnectServer;
     }
 
     /**
      * @return the autoRejoinChannel
      */
     public boolean isAutoRejoinChannel()
     {
         return autoRejoinChannel;
     }
 
     /**
      * @param autoRejoinChannel the autoRejoinChannel to set
      */
     public void setAutoRejoinChannel(boolean autoRejoinChannel)
     {
         this.autoRejoinChannel = autoRejoinChannel;
     }
 
     /**
      * @return the autoAcceptInvite
      */
     public boolean isAutoAcceptInvite()
     {
         return autoAcceptInvite;
     }
 
     /**
      * @param autoAcceptInvite the autoAcceptInvite to set
      */
     public void setAutoAcceptInvite(boolean autoAcceptInvite)
     {
         this.autoAcceptInvite = autoAcceptInvite;
     }
 
     /**
      * @return the useSSL
      */
     public boolean isUseSSL()
     {
         return useSSL;
     }
 
     /**
      * @param useSSL the useSSL to set
      */
     public void setUseSSL(boolean useSSL)
     {
         this.useSSL = useSSL;
     }
 
     /**
      * @return the verifySSL
      */
     public boolean isVerifySSL()
     {
         return verifySSL;
     }
 
     /**
      * @param verifySSL the verifySSL to set
      */
     public void setVerifySSL(boolean verifySSL)
     {
         this.verifySSL = verifySSL;
     }
 
     /**
      * @return the trigger
      */
     public String getTrigger()
     {
         return trigger;
     }
 
     /**
      * @param trigger the trigger to set
      */
     public void setTrigger(String trigger)
     {
         this.trigger = trigger;
     }
 
     /**
      * @return the serverHostame
      */
     public String getServerHostame()
     {
         return serverHostame;
     }
 
     /**
      * @param serverHostame the serverHostame to set
      */
     public void setServerHostame(String serverHostame)
     {
         this.serverHostame = serverHostame;
     }
 
     /**
      * @return the serverPassword
      */
     public String getServerPassword()
     {
         return serverPassword;
     }
 
     /**
      * @param serverPassword the serverPassword to set
      */
     public void setServerPassword(String serverPassword)
     {
         this.serverPassword = serverPassword;
     }
 
     /**
      * @return the serverPort
      */
     public String getServerPort()
     {
         return serverPort;
     }
 
     /**
      * @param serverPort the serverPort to set
      */
     public void setServerPort(String serverPort)
     {
         this.serverPort = serverPort;
     }
 
     /**
      * @return the botNickname
      */
     public String getBotNickname()
     {
         return botNickname;
     }
 
     /**
      * @param botNickname the botNickname to set
      */
     public void setBotNickname(String botNickname)
     {
         this.botNickname = botNickname;
     }
 
     /**
      * @return the botUsername
      */
     public String getBotUsername()
     {
         return botUsername;
     }
 
     /**
      * @param botUsername the botUsername to set
      */
     public void setBotUsername(String botUsername)
     {
         this.botUsername = botUsername;
     }
 
     /**
      * @return the botIdent
      */
     public String getBotIdent()
     {
         return botIdent;
     }
 
     /**
      * @param botIdent the botIdent to set
      */
     public void setBotIdent(String botIdent)
     {
         this.botIdent = botIdent;
     }
 
     /**
      * @return the botPassword
      */
     public String getBotPassword()
     {
         return botPassword;
     }
 
     /**
      * @param botPassword the botPassword to set
      */
     public void setBotPassword(String botPassword)
     {
         this.botPassword = botPassword;
     }
 
     /**
      * @return the ctcpFinger
      */
     public String getCtcpFinger()
     {
         return ctcpFinger;
     }
 
     /**
      * @param ctcpFinger the ctcpFinger to set
      */
     public void setCtcpFinger(String ctcpFinger)
     {
         this.ctcpFinger = ctcpFinger;
     }
 
     /**
      * @return the ctcpVersion
      */
     public String getCtcpVersion()
     {
         return ctcpVersion;
     }
 
     /**
      * @param ctcpVersion the ctcpVersion to set
      */
     public void setCtcpVersion(String ctcpVersion)
     {
         this.ctcpVersion = ctcpVersion;
     }
 
     /**
      * @return the channels
      */
     public List<String> getChannels()
     {
         return channels;
     }
 
     /**
      * @param channels the channels to set
      */
     public void setChannels(List<String> channels)
     {
         this.channels = channels;
     }
 
     /**
      * @return the loggedChannels
      */
     public List<String> getLoggedChannels()
     {
         return loggedChannels;
     }
 
     /**
      * @param loggedChannels the loggedChannels to set
      */
     public void setLoggedChannels(List<String> loggedChannels)
     {
         this.loggedChannels = loggedChannels;
     }
     /**
      * @return the permissionDenied
      */
     public String getPermissionDenied()
     {
         return permissionDenied;
     }
 
     /**
      * @param permissionDenied the permissionDenied to set
      */
     public void setPermissionDenied(String permissionDenied)
     {
         this.permissionDenied = permissionDenied;
     }
 
     public boolean isAdmin(String username, String hostmask)
     {
         List<String> admins = Arrays.asList(properties.getProperty("bot-ops").split(" "));
         boolean hostmatch = false;
         boolean nickmatch = false;
         String nick;
         String hostname;
         for (String host : admins)
         {
            System.out.println("Checking " + host + " for admin!");
             nick = host.split("\\|")[0];
             hostname = host.split("\\|")[1];
            Pattern p = Pattern.compile(hostname.replaceAll("\\*", ".+"));
             Matcher m = p.matcher(hostmask);
             if (m.find())
            {
                 hostmatch = true;
             }
            p = Pattern.compile(nick.replaceAll("\\*", ".+"));
             m = p.matcher(nick);
             if(m.find()){
                 nickmatch = true;
             }
         }
         return nickmatch && hostmatch;
     }
 }
