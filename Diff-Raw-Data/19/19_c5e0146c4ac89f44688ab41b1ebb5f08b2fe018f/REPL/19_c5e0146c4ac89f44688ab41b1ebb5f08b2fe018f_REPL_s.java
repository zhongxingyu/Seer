 // pilaje REPL class
 // Jacob Peck
 // 20120811
 
 package suschord.pilaje;
 
 import suschord.pilaje.*;
 import java.util.*;
 import java.io.*;
 
 public class REPL {
   public static final String version = "pilaje 20120813";
  public static boolean running = true;
   private static final BufferedReader inreader = new BufferedReader(new InputStreamReader(System.in));
   
   public static void start() {
     System.out.println(version);
     Parser.initialize();
     
     while (running) {
       run_input(get_input());
     }
     System.exit(0);
   }
   
   public static String get_input() {
     prompt();
     String line = "";
     try {
       line = inreader.readLine();
     } catch (Exception ex) {}
     return line;
   }
   
   public static void prompt() {
     System.out.print(Parser.currentStack.name + "[" + Parser.currentStack.size() + "]> ");
   }
   
   public static void run_input(String input) {
     Parser.run_input(input);
   }
 }
