 /** 
  Project "com.quui.chat.core" (C) 2006 Fabian Steeg
 
  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package com.quui.chat.ui.irc;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.Properties;
 
 import sun.reflect.generics.tree.BottomSignature;
 
 import com.quui.chat.GlobalProperties;
 import com.quui.chat.commands.RubyCaller;
 import com.quui.chat.wordgame.WordGame;
 import com.quui.chat.wordgame.WordGameScores;
 import com.sun.org.apache.bcel.internal.verifier.structurals.UninitializedObjectType;
 
 /**
  * Based on Testbot extends BasicIRCBot by David Seager
  * http://www-128.ibm.com/developerworks/library/j-javabot/index.html
  * 
  * Hollaka IRC - A Ruby-scriptable Java IRC Bot with a Word Game ;D
  * 
  * @author Fabian Steeg (fsteeg)
  */
 public class Hollaka extends SuperBot {
 
     private String name, description, restMessage;
 
     private Properties properties;
 
     public boolean wordGameRunning = false;
 
     public int wordScore = 0;
 
     public String wordWord = "", wordStarter;
 
     public Properties wordScoreProperties = null;
 
     public boolean sawGameDone = true;
 
     public boolean rubyIsDone = true;
 
     // TODO externalize
     String scriptsDirectory = "src-ruby";
 
     RubyCaller rubyCaller;
 
     private String nextCommand = null;
 
     private boolean initialized = false;
 
     /**
      * Instantiate a new bot
      */
     public Hollaka(String configFile) {
         super();
         initWordScoreProperties();
         initProperties(configFile);
         super.setBotDescription(description);
         super.setBotName(name);
         initIRCBot();
     }
 
     private void initProperties(String propertiesLocation) {
         properties = new Properties();
         try {
             FileInputStream in = new FileInputStream(propertiesLocation);
             properties.load(in);
             in.close();
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Failed to load properties file: "
                     + propertiesLocation + ".");
         }
         this.name = properties.getProperty("name");
         this.server = properties.getProperty("server");
         this.channels = properties.getProperty("channels").split(",");
         this.description = properties.getProperty("description");
         this.restMessage = properties.getProperty("more");
     }
 
     private void initWordScoreProperties() {
         wordScoreProperties = new Properties();
         try {
 
             FileInputStream in = new FileInputStream(
                     WordGameScores.wordScorePropertiesLocation);
             wordScoreProperties.load(in);
             in.close();
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Failed to load properties file: "
                     + WordGameScores.wordScorePropertiesLocation + ".");
         }
     }
 
     /**
      * @param args
      *            Nothing or topicFolder, topicFile, wnLocation, logLocation
      */
     // public static void main(String[] args) {
     // initIRCBot();
     // }
     /**
      * Process a private message
      */
     protected void srv_privmsg(String sender, String message, String channel) {
         System.out.println("Message: " + message + " from: " + sender
                 + " on channel: " + channel);
         System.out.println("Word is running: " + wordGameRunning
                 + ", Word is done: " + sawGameDone + ", WordWord: " + wordWord);
         if (message.equalsIgnoreCase(name + " " + "word")
         // || message.equals(Commands.prefix + "hardword")
         ) {
             if (!this.sawGameDone)
                 this.send_privmsg(channel, "A game is running in " + channel
                         + ".");
             else {
                 this.wordStarter = sender;
                 saveFreq(sender);
                 // System.out.println("Will play word");
                 new WordGame(this).play(channel, message.contains("hard"));
             }
             return;
         }
         if (message.equals("!rehash") || !initialized) {
             System.out.println("rehashing");
             initialized = true;
             rubyCaller = new RubyCaller(scriptsDirectory);
         }
         if (message.equals(this.wordWord) && this.wordGameRunning) {
             this.send_privmsg(channel, "Yes, " + sender
                     + ", that's right! It's '" + this.wordWord + "', "
                     + this.wordScore + " Points!");
             saveScore(sender);
             this.wordGameRunning = false;
 
             return;
         }
         if (message.toLowerCase().contains(this.name.toLowerCase())
                 || message.equalsIgnoreCase("!help") || !rubyIsDone) {
             if (message.equalsIgnoreCase("!help"))
                 message = name + " " + message.substring(1);
             String process = "";
             String commandSansPrefix = message.toLowerCase().replaceFirst(
                     name.toLowerCase(), "").trim();
             if (commandSansPrefix.equalsIgnoreCase("more"))
                 process = restMessage;
 
             // String process = Commands.process(message);
             String method = null;
             System.out.println("Command sans prefix: " + commandSansPrefix);
             if (process.equals("")) {
                 String argument = null;
                 if (!rubyIsDone) {
                     if (nextCommand == null) {
                         rubyIsDone = true;
                     } else {
                         method = nextCommand;
                         argument = commandSansPrefix;
                     }
                     // keep doing what ruby is saying
                     process = execute(method, argument);
 
                 } else if (commandSansPrefix.contains(" ")) {
 
                     if (nextCommand != null) {
                         method = nextCommand;
                         argument = commandSansPrefix;
                     } else {
                         method = commandSansPrefix.split(" ")[0];
                         argument = commandSansPrefix
                                 .substring(commandSansPrefix.indexOf(' '));
                     }
                     // ruby is not doing anything, call with argument
                     process = execute(method, argument);
                 } else {
                     method = nextCommand != null ? nextCommand
                             : commandSansPrefix.toLowerCase().replaceAll(
                                     name.toLowerCase(), "").trim();
                     // call with empty argument
                     process = execute(method, "");
                 }
 
             }
             // if (!rubyIsDone)
             // currentCommand = method;
             // else
             // currentCommand = null;
 
             System.out.println("Command result: " + process);
             int i = 0;
             String[] split = process.split("#");
             for (String m : split) {
                 // if(m.startsWith("["))
                 // continue;
                 // FIXME
                 if (m.length() > 350)
                     m = m.substring(0, 350) + "...";
                 this.send_privmsg(channel, m/*.trim()*/);
                 try {
                     Thread.sleep(2500);
                 } catch (InterruptedException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 i++;
                 if (i == 3) {
                     String substring = process.substring(process.indexOf(m)
                             + m.length());
                     if (substring != null
                             && !substring.replaceAll("#", "").trim().equals("")) {
                         // System.out.println("SUBSTRING: " + substring);
                         restMessage = substring;
                         this.send_privmsg(channel, "...");
                     } else
                         restMessage = "[ no more ]";
                     break;
                 }
 
             }
             return;
         }
     }
 
     private String execute(String method, String argument) {
         try {
             String process;
             Object[] exec = rubyCaller.exec(method, argument);
             process = (String) exec[0];
             rubyIsDone = exec[1] == null;
             nextCommand = (String) exec[1];
             return process;
         } catch (RuntimeException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return "";
     }
 
     private void saveScore(String sender) {
         if (!sender.equals(this.wordStarter))
             saveFreq(sender);
         String oldScore = wordScoreProperties.getProperty(sender);
         String[] vals = oldScore.split(",");
         int old = 0;
         if (oldScore != null) {
             old = Integer.parseInt(vals[0]);
         }
         wordScoreProperties.setProperty(sender, "" + (old + this.wordScore)
                 + ", " + vals[1].trim());
         try {
             FileOutputStream out = new FileOutputStream(
                     WordGameScores.wordScorePropertiesLocation);
             wordScoreProperties.store(out, "Last winner: " + sender);
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void saveFreq(String sender) {
         String oldFreq = wordScoreProperties.getProperty(sender);
         if (oldFreq == null)
             oldFreq = "0, 0";
         String[] vals = oldFreq.split(",");
         int freq = 0;
         if (oldFreq != null) {
             freq = Integer.parseInt(vals[1].trim());
             freq++;
         }
         wordScoreProperties.setProperty(sender, "" + vals[0] + ", " + freq);
         try {
             FileOutputStream out = new FileOutputStream(
                     WordGameScores.wordScorePropertiesLocation);
             wordScoreProperties.store(out, "Last player: " + sender);
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 }
