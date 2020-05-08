 package net.yeputons.cscenter.dbfall2013.clients;
 
 import net.yeputons.cscenter.dbfall2013.engines.hashtrie.HashTrieEngine;
 
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Egor Suvorov
  * Date: 27.09.13
  * Time: 20:27
  * To change this template use File | Settings | File Templates.
  */
 public class ConsoleClient {
     protected static void showHelp() {
         System.out.printf(
                 "Available commands (case-sensitive, excess spaces are forbidden):\n" +
                 "  quit  - closes the client\n" +
                 "  help  - displays this help\n" +
                 "  clear - removes all records\n" +
                 "  size  - prints number of records\n" +
                "  keys  - prints keys of all records\n" +
                 "  put <key> <value> - adds a new record or updates an old one\n" +
                 "  get <key> - prints value of the corresponding record\n" +
                 "  del <key> - deletes the corresponding record\n" +
                 "All keys are case-sensetive. All keys and data are treated as sequences of bytes.\n" +
                 "If you need to put space or backslash to your key/data use escaping: '\\_' for space,\n" +
                 "and '\\\\' for backslash. '\\n' '\\r' are also available\n" +
                 "\n" +
                 "You can also run this application with '--batch' option which disables\n" +
                 "interactivity stuff (like greeting and >>>)\n"
         );
     }
 
     protected static ByteBuffer parseArgument(String s) {
         StringBuilder res = new StringBuilder();
         for (int i = 0; i < s.length(); i++)
             if (s.charAt(i) == '\\') {
                 if (i + 1 >= s.length())
                     throw new IllegalArgumentException("Unfinished escaped sequence at the end of '" + s + "'");
                 switch (s.charAt(i + 1)) {
                     case '_': res.append(' '); break;
                     case '\\': res.append('\\'); break;
                     case 'n': res.append('\n'); break;
                     case 'r': res.append('\r'); break;
                     default: throw new IllegalArgumentException("Invalid escape sequence found: \\" + s.charAt(i + 1));
                 }
                 i++;
             } else {
                 res.append(s.charAt(i));
             }
         return ByteBuffer.wrap(res.toString().getBytes());
     }
 
     public static void main(String[] args) throws Exception {
         boolean batchMode = false;
         if (Arrays.asList(args).contains("--batch"))
             batchMode = true;
 
         HashTrieEngine engine = new HashTrieEngine(new File("storage.trie"));
         //LogFileEngine engine = new LogFileEngine(new File("storage.log"));
 
         if (!batchMode) {
             System.out.println("Welcome to " + ConsoleClient.class.getName() + "!");
             System.out.println("Type 'help' for help");
         }
 
         Scanner in = new Scanner(System.in);
         boolean quit = false;
         while (!quit) {
             if (!batchMode) {
                 System.out.print(">>> ");
             }
             String line;
             try {
                 line = in.nextLine();
             } catch (NoSuchElementException e) {
                 break;
             }
             if (line.isEmpty()) continue;
 
             if (line.equals("quit")) {
                 break;
             } else if (line.equals("help")) {
                 showHelp();
             } else if (line.equals("clear")) {
                 engine.clear();
             } else if (line.equals("size")) {
                 System.out.println(engine.size());
             } else if (line.equals("keys")) {
                for (ByteBuffer key : engine.keySet()) {
                    String s = new String(key.array());
                    System.out.println(s);
                 }
             } else {
                 String[] tokens = line.split(" ");
                 if (tokens[0].equals("put") && tokens.length == 3) {
                     ByteBuffer key = parseArgument(tokens[1]);
                     ByteBuffer value = parseArgument(tokens[2]);
                     engine.put(key, value);
                 } else if (tokens[0].equals("get") && tokens.length == 2) {
                     ByteBuffer key = parseArgument(tokens[1]);
                     ByteBuffer value = engine.get(key);
                     if (value == null) {
                         System.out.println("ERROR: Such key is not presented in DB");
                     } else {
                         System.out.println(new String(value.array()));
                     }
                 } else if (tokens[0].equals("del") && tokens.length == 2) {
                     ByteBuffer key = parseArgument(tokens[1]);
                     engine.remove(key);
                 } else {
                     System.out.println("Invalid command. Try again or type 'help' for help");
                 }
             }
         }
         in.close();
 
         engine.close();
     }
 }
