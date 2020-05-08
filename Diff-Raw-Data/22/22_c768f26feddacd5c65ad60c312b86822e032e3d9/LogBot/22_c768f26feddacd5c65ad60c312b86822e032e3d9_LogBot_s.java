 package org.jibble.logbot;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jibble.pircbot.Colors;
 import org.jibble.pircbot.PircBot;
 
 public class LogBot extends PircBot {
     private static final Logger logger = Logger.getLogger(LogBot.class.getName());
     private static final Pattern urlPattern = Pattern.compile("(?i:\\b((http|https|ftp|irc)://[^\\s]+))");
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
     private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("H:mm");
     
     public static final String GREEN = "irc-green";
     public static final String BLACK = "irc-black";
     public static final String BROWN = "irc-brown";
     public static final String NAVY = "irc-navy";
     public static final String BRICK = "irc-brick";
     public static final String RED = "irc-red";
     
    private String channel;
    private String server;
    private String nick;
     private String joinMessage;
     
     private File baseOutDir;
     private String controlPassword;
     private HashMap<String, State> channelStates = new HashMap<String, State>();
     private HashMap<String, File> channelDirectories = new HashMap<String, File>();
     
     enum State {
         LISTENING, IGNORING
     }
     
     private static final String ALL_CHANNELS = "ALL_CHANNELS";
     private static final String ALL_LISTENING_CHANNELS = "LISTEN_CHANNELS";
     private static final String ALL_IGNORING_CHANNELS = "IGNORE_CHANNELS";
     
     public LogBot(String name, File outDir, String joinMessage, String controlPassword) {
         setName(name);
         setVerbose(true);
         this.baseOutDir = outDir;
         this.baseOutDir.mkdirs();
         this.joinMessage = joinMessage;
         this.controlPassword = controlPassword;
         setupOutputDir(name);
     }
     
     private boolean setupOutputDir(String channel) {
         channel = stripHash(channel);
         File outDir = new File(baseOutDir.toString() + File.separator + channel);
         outDir.mkdirs();
         if (!outDir.isDirectory()) {
             logger.warning("Cannot make output directory (" + outDir + ")");
             return false;
         }
 
         try {
             LogBot.copy(new File("html/header.inc.php"), new File(outDir, "header.inc.php"));
             LogBot.copy(new File("html/footer.inc.php"), new File(outDir, "footer.inc.php"));
             LogBot.copy(new File("html/index.php"), new File(outDir, "index.php"));
         } catch (IOException e) {
             logger.warning("Couldn't copy necessary php files to output directory (" + outDir + ")");
             return false;
         }
 
         try {
            writeConfigPhp(outDir);
         } catch (IOException e) {
             logger.warning("Couldn't write necessary php config file to output directory (" + outDir + ")");
             return false;
         }
         channelDirectories.put(channel, outDir);
         return true;
     }
     
     private String stripHash(String channel) {
         if (channel.startsWith("#")) {
             return channel.substring(1);
         }
         return channel;
     }
 
    private void writeConfigPhp(File outDir) throws IOException {
         BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outDir, "config.inc.php")));
         writer.write("<?php");
         writer.newLine();
        writer.write("    $server = \"" + server + "\";");
         writer.newLine();
        writer.write("    $channel = \"" + channel + "\";");
         writer.newLine();
        writer.write("    $nick = \"" + nick + "\";");
         writer.newLine();
         writer.write("?>");
         writer.flush();
         writer.close();
     }
     
     private Set<String> allListeningChannels() {
         HashSet<String> channels = new HashSet<String>();
         for (String channel : channelStates.keySet()) {
             if (channelStates.get(channel) == State.LISTENING) {
                 channels.add(channel);
             }
         }
         return channels;
     }
     
     private Set<String> allIgnoringChannels() {
         HashSet<String> channels = new HashSet<String>();
         for (String channel : channelStates.keySet()) {
             if (channelStates.get(channel) == State.IGNORING) {
                 channels.add(channel);
             }
         }
         return channels;
     }
     
     private synchronized void append(String color, String line, String chan) {
         line = Colors.removeFormattingAndColors(line);
         
         line = line.replaceAll("&", "&amp;");
         line = line.replaceAll("<", "&lt;");
         line = line.replaceAll(">", "&gt;");
         
         Matcher matcher = urlPattern.matcher(line);
         line = matcher.replaceAll("<a href=\"$1\">$1</a>");
         
         Set<String> channels;
         if (chan.equals(ALL_CHANNELS)) {
             channels = channelStates.keySet();
         } else if (chan.equals(ALL_LISTENING_CHANNELS)) {
             channels = allListeningChannels();
         } else if (chan.equals(ALL_IGNORING_CHANNELS)) {
             channels = allIgnoringChannels();
         } else {
             channels = new HashSet<String>();
             chan = stripHash(chan);
             if (channelStates.get(chan) == State.IGNORING) {
                 // don't log anything if we're ignoring the channel
                 return;
             }
             channels.add(chan);
         }
         
                 
         try {
             Date now = new Date();
             String date = DATE_FORMAT.format(now);
             String time = TIME_FORMAT.format(now);
             for (String channel : channels) {
                 File file = new File(channelDirectories.get(channel), date + ".log");
                 BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                 String entry = "<span class=\"irc-date\">[" + time + "]</span> <span class=\"" + color + "\">" + line + "</span><br />";
                 writer.write(entry);
                 writer.newLine();
                 writer.flush();
                 writer.close();
             }
         }
         catch (IOException e) {
             System.out.println("Could not write to log: " + e);
         }
     }
     
     @Override
     public void onAction(String sender, String login, String hostname, String target, String action) {
         append(BRICK, "* " + sender + " " + action, target);
     }
     
     @Override
     public void onJoin(String channel, String sender, String login, String hostname) {
         // append(GREEN, "* " + sender + " (" + login + "@" + hostname + ") has joined " + channel);
         append(GREEN, "* " + sender + " has joined " + channel, channel);
         if (sender.equals(getNick())) {
             sendNotice(channel, joinMessage);
         }
         else {
             sendNotice(sender, joinMessage);
         }
     }
     
     @Override
     public void onMessage(String channel, String sender, String login, String hostname, String message) {
         append(BLACK, "<" + sender + "> " + message, channel);
         
         message = message.toLowerCase();
         if (message.startsWith(getNick().toLowerCase()) && message.indexOf("help") > 0) {
             sendMessage(channel, joinMessage);
         }
     }
     
     @Override
     public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
         append(GREEN, "* " + sourceNick + " sets mode " + mode, channel);
     }
     
     @Override
     public void onNickChange(String oldNick, String login, String hostname, String newNick) {
        append(GREEN, "* " + oldNick + " is now known as " + newNick, channel);
     }
     
     @Override
     public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
         append(BROWN, "-" + sourceNick + "- " + notice, target);
     }
     
     @Override
     public void onPart(String channel, String sender, String login, String hostname) {
         append(GREEN, "* " + sender + " has left " + channel, channel);
     }
     
     @Override
     public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
         append(RED, "[" + sourceNick + " PING]", target);
     }
     
     @Override
     public void onPrivateMessage(String sender, String login, String hostname, String message) {
         if (message.endsWith(controlPassword)) {
             // authenticated command. process it
             System.out.println("It's authenticated!");
             String command = message.replace(controlPassword, "");
             processCommand(command, sender);
             append(BLACK, "<- *" + sender + "* " + command, this.getNick());
         } else {
             // non-authenticated command
             if (message.toLowerCase().equals("help")) {
                 printHelpMessage(sender);
             } else {
                 // sendMessage(sender, "Not authenticated. Please include the control password at the end of the command");
             }
             append(BLACK, "<- *" + sender + "* " + message, this.getNick());
         }
     }
     
     @Override
     public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
         // TODO it's too bad we don't know which channels this user is on...
         append(NAVY, "* " + sourceNick + " Quit (" + reason + ")", ALL_CHANNELS);
     }
     
     @Override
     public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
         append(RED, "[" + sourceNick + " TIME]", target);
     }
     
     @Override
     public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
         if (changed) {
             append(GREEN, "* " + setBy + " changes topic to '" + topic + "'", channel);
         }
         else {
             append(GREEN, "* Topic is '" + topic + "'", channel);
             append(GREEN, "* Set by " + setBy + " on " + new Date(date), channel);
         }
     }
     
     @Override
     public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
         append(RED, "[" + sourceNick + " VERSION]", target);
     }
     
     @Override
     public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
         append(GREEN, "* " + recipientNick + " was kicked from " + channel + " by " + kickerNick, channel);
         if (recipientNick.equalsIgnoreCase(getNick())) {
             joinChannel(channel);
         }
     }
     
     @Override
     public void onDisconnect() {
         append(NAVY, "* Disconnected.", ALL_CHANNELS);
         while (!isConnected()) {
             try {
                 reconnect();
             }
             catch (Exception e) {
                 try {
                     Thread.sleep(10000);
                 }
                 catch (Exception anye) {
                     // Do nothing.
                 }
             }
         }
     }
     
     public static void copy(File source, File target) throws IOException {
         BufferedInputStream input = new BufferedInputStream(new FileInputStream(source));
         BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(target));
         int bytesRead = 0;
         byte[] buffer = new byte[1024];
         while ((bytesRead = input.read(buffer, 0, buffer.length)) != -1) {
             output.write(buffer, 0, bytesRead);
         }
         output.flush();
         output.close();
         input.close();
     }
     
     private void printHelpMessage(String user) {
         sendMessage(user, "I understand the following commands:");
         sendMessage(user, "  'help'             - prints this message");
         sendMessage(user, "  'join [channel]'   - join the specified channel and start logging"); // TODO implement
         sendMessage(user, "  'leave [channel]'  - leave the specified channel and stop logging"); // TODO implement
         sendMessage(user, "  'ignore [channel]' - stop logging, but don't leave, the specified channel"); // TODO implement
         sendMessage(user, "  'listen [channel]' - start logging the specified channel again"); // TODO implement
     }
     
     public boolean join(String channel) {
         String strippedChannel = stripHash(channel);
         if (! channelStates.containsKey(strippedChannel)) {
             if (setupOutputDir(channel)) {
                 joinChannel(channel);
                 channelStates.put(strippedChannel, State.LISTENING);
                 return true;
             }
         } else {
             return true;
         }
         return false;
     }
     
     public boolean leave(String channel) {
         String strippedChannel = stripHash(channel);
         if (channelStates.containsKey(strippedChannel)) {
             partChannel(channel);
             channelStates.remove(channel);
         }
         return true;
     }
     
     private void ignore(String channel) {
         sendAction(channel, "is no longer recording this channel");
         channelStates.put(stripHash(channel), State.IGNORING);
     }
     
     private void listen(String channel) {
         channelStates.put(stripHash(channel), State.LISTENING);
         sendAction(channel, "is now recording this channel");
     }
     
     private void processCommand(String fullCommand, String user) {
         String[] parts = fullCommand.split("\\s+");
         if (parts.length < 2) {
             // all commands right now require 2 parts -- a command and a channel
             printHelpMessage(user);
             return;
         }
         String command = parts[0];
         String channel = parts[1];
         if (! channel.startsWith("#")) {
             channel = "#" + channel;
         }
         if (command.equalsIgnoreCase("join")) {
             sendMessage(user, "joining " + channel);
             boolean joinSucceeded = join(channel);
             if (! joinSucceeded) {
                 sendMessage(user, "failed to join " + channel);
             }
         } else if (command.equalsIgnoreCase("leave")) {
             sendMessage(user, "leaving " + channel);
             leave(channel);
         } else if (command.equalsIgnoreCase("ignore")) {
             sendMessage(user, "ignoring " + channel);
             ignore(channel);
         } else if (command.equalsIgnoreCase("listen")) {
             sendMessage(user, "listening to " + channel);
             listen(channel);
         } else {
             printHelpMessage(user);
         }
     }
     
 }
