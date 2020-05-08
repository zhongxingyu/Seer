 
 package me.heldplayer.GitIRC.client;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
import me.heldplayer.GitIRC.ConsoleMessageReciever;
 import me.heldplayer.GitIRC.MessageReciever;
 
 public class IncomingMessage {
     private String source = "";
     private String type = "";
     private String[] args = null;
     private String argString = "";
 
     public IncomingMessage(String base) {
         String editable = base.trim();
 
         if (editable.startsWith(":")) {
             if (editable.indexOf(" ") < 0) {
                 return;
             }
 
             source = editable.substring(1, editable.indexOf(" "));
 
             editable = editable.substring(source.length() + 1).trim();
         }
 
         String[] split = editable.split(" ");
 
         if (split.length <= 0) {
             return;
         }
 
         type = split[0];
 
         editable = editable.substring(type.length()).trim();
 
         argString = editable;
         args = argString.split(" ");
 
         //System.err.println("Source: " + source + "\tType: " + type + "\tArgs: " + argString);
     }
 
     public boolean parse(MessageReciever reciever) {
         if (type.equalsIgnoreCase("PING")) {
             reciever.send("PONG " + argString);
             return true;
         }
         if (type.equalsIgnoreCase("PONG")) {
             return true;
         }
         if (type.equalsIgnoreCase("NOTICE")) {
             String result = "";
             for (int i = 1; i < args.length; i++) {
                 if (i != 1) {
                     result += " ";
                 }
                 result += args[i];
             }
 
             if (result.startsWith(":")) {
                 result = result.substring(1);
             }
 
             System.err.println("[NOTICE: " + args[0] + "]: " + result);
             return true;
         }
         if (type.equalsIgnoreCase("CAP")) {
             if (args.length < 3) {
                 return false;
             }
             if (args[0].equalsIgnoreCase("*") && args[1].equalsIgnoreCase("LS")) {
                 String result = "";
                 for (int i = 2; i < args.length; i++) {
                     if (i != 2) {
                         result += " ";
                     }
                     result += args[i];
                 }
 
                 if (result.startsWith(":")) {
                     result = result.substring(1);
                 }
 
                 String[] caps = result.split(" ");
 
                 for (String cap : caps) {
                     if (cap.equalsIgnoreCase("multi-prefix")) {
                         reciever.send("CAP REQ :multi-prefix");
                         return true;
                     }
                 }
                 return false;
             }
             else if (args[0].equalsIgnoreCase(reciever.getNick()) && args[1].equalsIgnoreCase("ACK")) {
                 reciever.send("CAP END");
                 return true;
             }
             return false;
         }
         if (type.equalsIgnoreCase("PRIVMSG")) {
             String result = "";
             for (int i = 1; i < args.length; i++) {
                 if (i != 1) {
                     result += " ";
                 }
                 result += args[i];
             }
 
             if (result.startsWith(":")) {
                 result = result.substring(1);
             }
 
             System.out.println("[" + args[0] + "] <" + source.split("!")[0] + "> " + result);
             //reciever.send("PRIVMSG " + args[0] + " :" + result);
             return true;
         }
         if (type.equalsIgnoreCase("MODE")) {
             String result = "";
             for (int i = 1; i < args.length; i++) {
                 if (i != 1) {
                     result += " ";
                 }
                 result += args[i];
             }
 
             if (result.startsWith(":")) {
                 result = result.substring(1);
             }
 
             System.out.println("[" + args[0] + "] " + source.split("!")[0] + " sets mode " + result);
             return true;
         }
         if (type.equalsIgnoreCase("KICK")) {
             String result = "";
             for (int i = 2; i < args.length; i++) {
                 if (i != 2) {
                     result += " ";
                 }
                 result += args[i];
             }
 
             if (result.startsWith(":")) {
                 result = result.substring(1);
             }
 
             System.out.println("[" + args[0] + "] " + source.split("!")[0] + " has kicked " + args[1] + " (" + result + ")");
             return true;
         }
         if (type.equalsIgnoreCase("QUIT")) {
             String result = "";
             for (int i = 0; i < args.length; i++) {
                 if (i != 0) {
                     result += " ";
                 }
                 result += args[i];
             }
 
             if (result.startsWith(":")) {
                 result = result.substring(1);
             }
 
             System.out.println(source.split("!")[0] + " has quit (" + result + ")");
             return true;
         }
         if (type.equalsIgnoreCase("PART")) {
             String result = "";
             for (int i = 1; i < args.length; i++) {
                 if (i != 1) {
                     result += " ";
                 }
                 result += args[i];
             }
 
             if (result.startsWith(":")) {
                 result = result.substring(1);
             }
 
             System.out.println("[" + args[0] + "] " + source.split("!")[0] + " parts (" + result + ")");
             return true;
         }
         if (type.equalsIgnoreCase("JOIN")) {
             System.out.println("[" + args[0] + "] " + source.split("!")[0] + " has joined");
             return true;
         }
 
         if (type.equalsIgnoreCase("001")) {
             reciever.send("USERHOST " + reciever.getNick());
             return true;
         }
         if (type.equalsIgnoreCase("332")) {
             String result = "";
             for (int i = 2; i < args.length; i++) {
                 if (i != 2) {
                     result += " ";
                 }
                 result += args[i];
             }
 
             if (result.startsWith(":")) {
                 result = result.substring(1);
             }
 
             System.out.println("[" + args[1] + "] Topic: " + result);
             return true;
         }
         if (type.equalsIgnoreCase("333")) {
             if (args.length < 3) {
                 return false;
             }
 
             SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 
             System.out.println("[" + args[1] + "] Set by " + args[2] + " on " + format.format(new Date(Long.parseLong(args[3]) * 1000L)));
             return true;
         }
         if (type.equalsIgnoreCase("372")) {
             String result = "";
             for (int i = 1; i < args.length; i++) {
                 if (i != 1) {
                     result += " ";
                 }
                 result += args[i];
             }
 
             if (result.startsWith(":")) {
                 result = result.substring(1);
             }
 
             System.out.println(result);
             return true;
         }
         if (type.equalsIgnoreCase("302")) {
             String host = "";
 
             for (int i = 1; i < args.length; i++) {
                 if (i == 1)
                     host += args[i].substring(1);
                 else
                     host += " " + args[i];
             }
 
             System.out.println(args[0] + " is " + host);
 
             System.out.println("Performing startup commands...");
 
             try {
                 FileReader fileReader = new FileReader(new File(IncomingMessage.class.getResource("/perform.txt").toURI()));
 
                 BufferedReader reader = new BufferedReader(fileReader);
 
                 while (reader.ready()) {
                     String line = reader.readLine();
 
                    ConsoleMessageReciever rec = (ConsoleMessageReciever) reciever;

                    rec.inputBuffer.put(rec.index++, String.format(line, reciever.getNick()));
                 }
 
                 reader.close();
                 fileReader.close();
             }
             catch (URISyntaxException e) {}
             catch (IOException e) {}
 
             return true;
         }
 
         return false;
     }
 }
