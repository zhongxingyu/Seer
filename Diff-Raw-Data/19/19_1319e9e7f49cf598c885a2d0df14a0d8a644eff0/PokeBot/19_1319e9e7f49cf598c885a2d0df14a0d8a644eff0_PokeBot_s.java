 /*
  * Copyright (C) 2013 Lord_Ralex
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
 package org.hoenn.pokebot;
 
 import org.hoenn.pokebot.api.Utilities;
 import org.hoenn.pokebot.api.events.ConnectionEvent;
 import org.hoenn.pokebot.api.exceptions.NickNotOnlineException;
 import org.hoenn.pokebot.api.users.BotUser;
 import org.hoenn.pokebot.permissions.PermissionManager;
 import org.hoenn.pokebot.settings.Settings;
 import org.hoenn.pokebot.input.KeyboardListener;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import jline.console.ConsoleReader;
 import org.hoenn.pokebot.scheduler.Scheduler;
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.exception.IrcException;
 import org.pircbotx.exception.NickAlreadyInUseException;
 
 public final class PokeBot extends Thread {
 
     private static final PircBotX driver;
     public static final String VERSION = "5.0.0";
     private final EventHandler eventHandler;
     private static final PokeBot instance;
     private static final KeyboardListener kblistener;
     private static final Settings globalSettings;
     private static boolean debugMode = false;
     private static final Map<String, String> args = new HashMap<>();
     private static boolean login = true;
     private final PermissionManager permManager;
     private final ExtensionManager extensionManager;
     private final Scheduler scheduler;
 
     static {
        instance = new PokeBot();
        KeyboardListener temp;
        try {
            temp = new KeyboardListener(instance);
        } catch (IOException | NickNotOnlineException ex) {
            temp = null;
            log(Level.SEVERE, "An error occured", ex);
        }
        kblistener = temp;
         if (!(new File("settings", "config.yml").exists())) {
             new File("settings").mkdirs();
             InputStream input = PokeBot.class.getResourceAsStream("/config.yml");
             try {
                 if (input == null) {
                     throw new FileNotFoundException("Jar does not contain config.yml in root");
                 }
                 BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File("settings", "config.yml")));
                 PokeBot.copyInputStream(input, output);
             } catch (FileNotFoundException ex) {
                 log(Level.SEVERE, "Cannot find the config file", ex);
             }
         }
         globalSettings = Settings.loadGlobalSettings();
         driver = new PircBotX();
     }
 
     public static void main(String[] startargs) throws IOException {
         SplitStream out = new SplitStream(System.out);
         SplitStream err = new SplitStream(System.err);
         System.setOut(out);
         System.setErr(err);
         if (startargs.length != 0) {
             for (String arg : startargs) {
                 if (arg.equalsIgnoreCase("-debugmode")) {
                     log(Level.INFO, "Starting with DEBUG MODE ENABLED");
                     debugMode = true;
                 } else if (arg.equalsIgnoreCase("-nologin")) {
                     login = false;
                 } else {
                     String[] argument = arg.split("=");
                     String key, value;
                     key = argument[0];
                     if (argument.length == 1) {
                         value = "true";
                     } else {
                         value = argument[1];
                     }
                     args.put(key, value);
                 }
             }
         }
         try {
             instance.createInstance(args.get("user"), args.get("pass"));
         } catch (IOException | IrcException ex) {
             log(Level.SEVERE, "An error occurred", ex);
         }
         synchronized (instance) {
             try {
                 instance.wait();
             } catch (InterruptedException ex) {
                 log(Level.SEVERE, "The instance was interrupted", ex);
             }
         }
 
         instance.getEventHandler().stopRunner();
 
         Set<Channel> chans = driver.getChannels();
         Channel[] chanArray = chans.toArray(new Channel[0]);
         String[] chanNames = new String[chans.size()];
         for (int i = 0; i < chanArray.length; i++) {
             chanNames[i] = chanArray[i].getName();
         }
 
         for (String chan : chanNames) {
             try {
                 Channel c = driver.getChannel(chan);
                 if (driver.getChannels().contains(c)) {
                     driver.partChannel(c, "Exiting channel");
                 }
             } catch (Exception ex) {
                 log(Level.SEVERE, "An error occured on shutting down", ex);
             }
         }
 
         try {
             driver.quitServer();
         } catch (Exception ex) {
             log(Level.SEVERE, "An error occured on shutting down", ex);
         }
         try {
             driver.disconnect();
         } catch (Exception ex) {
             log(Level.SEVERE, "An error occured on shutting down", ex);
 
         }
         try {
             driver.shutdown(false);
         } catch (Exception ex) {
             log(Level.SEVERE, "An error occured on shutting down", ex);
         }
         System.exit(0);
     }
 
     private PokeBot() {
         eventHandler = new EventHandler(driver);
         extensionManager = new ExtensionManager();
         permManager = new PermissionManager();
         scheduler = new Scheduler();
     }
 
     private void createInstance(String user, String pass) throws IOException, IrcException {
         driver.setEncoding(Charset.forName("UTF-8"));
         String bind = Settings.getGlobalSettings().getString("bind-ip");
         if (bind != null && !bind.isEmpty()) {
             InetAddress addr = InetAddress.getByName(bind);
             driver.setInetAddress(addr);
         }
         driver.setVerbose(true);
         driver.setVersion("AeBot - v" + VERSION);
         driver.setAutoReconnect(false);
         driver.setAutoReconnectChannels(true);
         String nick = user;
         if (nick == null || nick.isEmpty()) {
             nick = globalSettings.getString("nick");
         }
         if (nick == null || nick.isEmpty()) {
             nick = "DebugBot";
         }
         driver.setName(nick);
         driver.setLogin(nick);
 
         log(Level.INFO, "Nick of bot: " + nick);
 
         Utilities.setUtils(driver);
 
         eventHandler.load();
         extensionManager.load();
         boolean eventSuccess = driver.getListenerManager().addListener(eventHandler);
         if (eventSuccess) {
             log(Level.INFO, "Listener hook attached to bot");
         } else {
             log(Level.INFO, "Listener hook was unable to attach to the bot");
         }
         String network = globalSettings.getString("network");
         int port = globalSettings.getInt("port");
         if (network == null || network.isEmpty()) {
             network = "irc.esper.net";
         }
         if (port == 0 || port < 0) {
             port = 6667;
         }
         if (pass == null || pass.isEmpty()) {
             pass = globalSettings.getString("nick-pw");
         }
         log(Level.INFO, "Connecting to: " + network + ":" + port);
         try {
             driver.connect(network, port);
         } catch (NickAlreadyInUseException ex) {
             log(Level.SEVERE, "The nick is already taken");
             driver.changeNick(nick + "_");
             driver.connect(network, port);
             driver.sendMessage("chanserv", "ghost " + nick + " " + pass);
             driver.changeNick(nick);
             if (!globalSettings.getString("nick").equalsIgnoreCase(driver.getNick())) {
                 log(Level.SEVERE, "Could not claim the nick " + nick);
             }
         }
         BotUser bot = BotUser.getBotUser();
         if (pass != null && !pass.isEmpty() && login) {
             bot.sendMessage("nickserv", "identify " + pass);
             log(Level.INFO, "Logging in to nickserv");
         }
         eventHandler.fireEvent(new ConnectionEvent());
         List<String> channels = globalSettings.getStringList("channels");
         if (channels != null && !channels.isEmpty()) {
             for (String chan : channels) {
                 log(Level.INFO, "Joining " + chan);
                 bot.joinChannel(chan);
             }
         } else {
             bot.joinChannel("#ae97");
         }
         log(Level.INFO, "Initial loading complete, engaging listeners");
         eventHandler.startQueue();
         log(Level.INFO, "Starting keyboard listener");
         kblistener.start();
         log(Level.INFO, "All systems operational");
     }
 
     public static PokeBot getInstance() {
         return instance;
     }
 
     public EventHandler getEventHandler() {
         return eventHandler;
     }
 
     public ExtensionManager getExtensionManager() {
         return extensionManager;
     }
 
     public Scheduler getScheduler() {
         return scheduler;
     }
 
     public ConsoleReader getConsole() {
         return kblistener.getJLine();
     }
 
     public static boolean getDebugMode() {
         return debugMode;
     }
 
     public static Map<String, String> getStartupArgs() {
         return args;
     }
 
     public static void copyInputStream(InputStream in, OutputStream out) {
         try {
             byte[] buffer = new byte[1024];
             int len;
             while ((len = in.read(buffer)) >= 0) {
                 out.write(buffer, 0, len);
             }
             in.close();
             out.close();
         } catch (IOException ex) {
             log(Level.SEVERE, "An error occurred on copying the streams", ex);
         }
     }
 
     public static void log(String message) {
         log(Level.INFO, message);
     }
 
     public static void log(Level level, String message) {
         System.out.println("[" + level + "] " + message);
     }
 
     public static void log(Level level, String message, Throwable error) {
         log(level, message);
         error.printStackTrace(System.out);
     }
 
     public PermissionManager getPermManager() {
         return permManager;
     }
 
     private static class SplitStream extends PrintStream {
 
         private final PrintStream first, second;
 
         public SplitStream(PrintStream f) throws FileNotFoundException {
             super(f);
             first = f;
             second = new PrintStream(new FileOutputStream("logs.log", true));
         }
 
         @Override
         public void write(byte[] b) throws IOException {
             first.write(b);
             second.write(b);
         }
 
         @Override
         public void write(int b) {
             first.write(b);
             second.write(b);
         }
 
         @Override
         public void write(byte[] buf, int off, int len) {
             first.write(buf, off, len);
             second.write(buf, off, len);
         }
 
         @Override
         public void close() {
             first.close();
             second.close();
         }
 
         @Override
         public void flush() {
             first.flush();
             second.flush();
         }
     }
 }
