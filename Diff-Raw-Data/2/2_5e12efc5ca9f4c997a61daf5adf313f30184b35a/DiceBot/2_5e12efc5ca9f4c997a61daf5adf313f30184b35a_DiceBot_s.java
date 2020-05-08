 package org.perilouscodpiece.dicebot;
 
 import java.util.*;
 import java.util.regex.*;
 import org.jibble.pircbot.*;
 
 public class DiceBot extends PircBot {
     private String adminPassword;
     private Random rand;
     private int antifloodthreshold;
 
     public DiceBot(String nick, String adminpass) {
         this.setName(nick);
         this.adminPassword = adminpass;
         this.rand = new Random();
     }
 
     private boolean opsOnChannel(String channel) {
         User[] ulist = getUsers(channel);
         for (int i = 0; i < ulist.length; i++) {
             if (ulist[i].equals(getNick()) && ulist[i].isOp()) {
                 return true;
             }
         }
         return false;
     }
 
     public void setAntiFloodThreshold(String thresh) {
         try {
             this.antifloodthreshold = Integer.parseInt(thresh);
         } catch (NumberFormatException nfe) {
             // todo: log? alert somehow?
         }
     }
 
     public void onPrivateMessage(String sender, String login, String hostname, String message) {
         String[] atoms = message.split("\\s+");
 
         if (message.startsWith("!antiflood")) {
             if (atoms.length == 3) {
                 if (atoms[2].equals(this.adminPassword)) {
                     setAntiFloodThreshold(atoms[1]);
                     sendMessage(sender, "done");
                 }
             }
         }
 
         if (message.startsWith("!join")) {
             if (atoms.length == 3) {
                 if (atoms[2].equals(this.adminPassword)) {
                     joinChannel(atoms[1]);
                     sendMessage(sender, "done");
                 }
             }
         }
 
         if (message.startsWith("!leave")) {
             if (atoms.length == 3) {
                 if (atoms[2].equals(this.adminPassword)) {
                     partChannel(atoms[1]);
                     sendMessage(sender, "done");
                 }
             }
         }
 
         if (message.startsWith("!quit")) {
             if (atoms.length == 2) {
                 if (atoms[1].equals(this.adminPassword)) {
                     sendMessage(sender, "disconnecting from server");
                     quitServer();
                     System.exit(0);
                 }
             }
         }
 
         if (message.startsWith("!op")) {
             if (atoms.length == 4) {
                 if (atoms[3].equals(this.adminPassword)) {
                     if (this.opsOnChannel(atoms[1])) {
                         op(atoms[1], atoms[2]);
                         sendMessage(sender, "done");
                     } else {
                         sendMessage(sender, "ERROR dicebot does not have ops mode in channel " + atoms[1]);
                     }
                 }
             }
         }
     }
 
     public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
         if (targetNick.equals(getNick())) { // not sure if this is required, reading the docs, but it can't hurt...
             joinChannel(channel);
         }
     }
 
     public void onMessage(String channel, String sender, String login, String hostname, String message) {
         if (message.startsWith("!roll") || message.startsWith("!vroll")) {
             String[] atoms = message.split("\\s+");
             Pattern diceSpecifier = Pattern.compile("(\\d*)[dD](\\d+)([+-]\\d+){0,1}"); 
 
             boolean verbose = atoms[0].startsWith("!vroll");
             boolean nototal = false; 
 
             Matcher diceMatcher = null;
             if (atoms.length == 2 ) {
                 diceMatcher = diceSpecifier.matcher(atoms[1]);
             } else if (atoms.length == 3) {
                 nototal = atoms[1].equals("nototal");
                 diceMatcher = diceSpecifier.matcher(atoms[2]);
             } else { // wrong number of args
                 sendMessage(channel, sender + ": sorry, I don't understand.  Try !dicehelp for usage information.");
             }
 
             if (diceMatcher == null || !diceMatcher.matches()) { // we were given a bad dice specifier by the user
                 sendMessage(channel, sender + ": sorry, I don't understand. Try !dicehelp for usage information.");
             } else {
                 // XdY+-Z
                 int X = 0;
                 int Y = 0;
                 int Z = 0;
 
                 try {
                     if (!diceMatcher.group(1).equals("")) {
                         X = Integer.parseInt(diceMatcher.group(1));
                     } else { // if not given, default to 1 as a special form for e.g. "!roll d6"
                         X = 1;
                     }
                     Y = Integer.parseInt(diceMatcher.group(2));
                     if (diceMatcher.group(3) != null) {
                         if (diceMatcher.group(3).matches("\\+.*")) {
                             Z = Integer.parseInt(diceMatcher.group(3).replaceFirst("\\+",""));
                         } else {
                             Z = Integer.parseInt(diceMatcher.group(3));  
                         }
                     }
                 } catch (NumberFormatException nfe) {
                     sendMessage(channel, sender + ": number format exception encountered -- " + nfe.getMessage());
                 }
 
                 if (X < 1 || Y < 1) {
                     sendMessage(channel, sender + ": use positive integers for die size and count");
                 } else {
                     int total = 0;
 
                     if (verbose && X > antifloodthreshold) {
                         sendMessage(channel, sender + ": a verbose roll that large would exceed the current antiflood threshold of " + Integer.toString(antifloodthreshold) + ", so I'm falling back to non-verbose mode");
                         verbose = false;
                     }
 
                     for (int i = 0; i < X; i++) {
                         int roll = this.rand.nextInt(Y) + 1;
                         total += roll;
                         
                         if (verbose) {
                             sendMessage(channel, sender + ": die[" + (i + 1) + "] = " + roll);
                         }
                     }
 
                     total += Z;
 
                     if (verbose) {
                         if (!nototal && X > 1) {
                             sendMessage(channel, sender + ": total = " + total);
                         }
                     } else {
                         sendMessage(channel, sender + ": " + total);
                     }
                 }
             }
         }
 
         if (message.startsWith("!choose")) {
             if (message.matches("!choose\\s*$")) {
                 sendMessage(channel, sender + ": you need to supply one or more options to chose from (comma delimeted)");
             } else {
                 String[] options = message.replace("!choose\\s+","").split("\\s*,+\\s*");
                int idx = this.rand.nextInt(options.length - 1) + 1;
                 sendMessage(channel, sender + ": " + options[idx]);
             }
         }
         
         if (message.startsWith("!dicehelp")) {
             sendMessage(channel, sender + ": say !roll followed by a dice specifier in XdY[+-Z] format (+-Z, if given, applies to the total roll, not each die roll), or !vroll for verbose which shows each die rolled");
         }
     }
 }
