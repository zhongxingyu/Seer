 package abdjekt;
 
 import java.io.File;
 import static java.lang.System.*;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 public class Main {
 
     public static String verb;
     public static String subject;
     public static String withCheck;
     public static int withIndex;
     public static String object;
     public static World world;
     public static ArrayList<Item> spawned;
 
     public static void main(String[] args) {
         File dir = new File(System.getenv("APPDATA") + "\\abdjekt\\");
         dir.mkdir();
 
         Scanner keyboard = new Scanner(in);
         spawned = new ArrayList<Item>();
         spawned.add(new Item("foo"));
        world = new World(3);
         File file = new File(System.getenv("APPDATA") + "\\abdjekt\\foo.abj");
 
         out.println("Welcome to Abdjekt!");
         out.println("To spawn an object, use: spawn <noun>. To remove them, use: remove <noun>");
         out.println("To look at what you have spawned, type: look.");
         out.println("To remove everything that is spawned, type: clean.");
         out.println("The syntax for commands is <verb> <noun> with <noun>.");
         out.println("To quit the game, simply type 'quit'.");
         out.println("There are two gamemodes: sandbox and hardcore.");
         out.println("In sandbox you can spawn and do whatever you want, within the bounds of the current content.");
         out.println("In hardcore, there are limits to what you can spawn, you have to obtain them from the environment, and crafting.");
         out.println("To change the gamemode, type 'mode 1' (sandbox) or 'mode 2' (hardcore). The game starts on hardcore.");
         out.println("This game is NOT case-sensative.");
         out.println("Please send us suggestions at kicneoj@gmail.com!");
         out.println("NOTE: This programme requires an internet connetion!");
         out.println();
         //TODO Remove more welcome!
 
         if (Double.parseDouble(Game.getClientVersion()) < Double.parseDouble(Game.getCurrentVersion())) {
             out.println("IMPORTANT: YOU'RE CLIENT IS OUT OF DATE!\nDOWNLOAD THE NEWEST VERSION AT http://kicneoj.webs.com/abdjekt/download.html");
             out.println();
         }
         Game.printNews();
         out.println();
 
         while (true) {
 
             out.print("> ");
             String inputLine = keyboard.nextLine();
             Scanner inputsplit = new Scanner(inputLine);
             String inputArray[] = new String[6];
             for (int i = 0; i < inputArray.length; i++) {
                 try {
                     inputArray[i] = inputsplit.next().toLowerCase();
                 } catch (NoSuchElementException eo) {
                 }
             }
             verb = inputArray[0];
 
             int withCounter = 0;
             withIndex = -1;
             for (int i = 2; i <= inputArray.length - 1; i++) {
                 if (inputArray[i] != null && inputArray[i].equals("with")) {
                     withCheck = inputArray[i];
                     withCounter++;
                     withIndex = i;
                 }
             }
             if (withIndex == -1) {
                 subject = inputArray[1];
                 if (inputArray[2] != null && !inputArray[2].equals("")) {
                     subject += " " + inputArray[2];
                 }
             } else {
                 if (withIndex >= 2) {
                     subject = inputArray[1];
                     if (withIndex == 3) {
                         subject += " " + inputArray[2];
                     }
                 }
             }
 
             if (withIndex == -1) {
                 object = "";
             } else {
                 object = inputArray[withIndex + 1];
                 if (inputArray[withIndex + 2] != null) {
                     object += " " + inputArray[withIndex + 2];
                 }
             }
 
             if (withCounter >= 2) {
                 out.println("Invalid syntax.");
                 continue;
             }
 
             if (verb == null) {
                 System.out.println("Do what?");
                 continue;
             }
 
             if (verb.equals("quit")) {
                 //               System.out.println(""); //TODO add quit text
                 break;
             }
             if (verb.equals("clean")) {
                 world.clear();
                 System.out.println("Everything around you disappears in a flash of light.");
                 continue;
             }
             if (subject == null && !verb.equals("look")) {
                 System.out.println(verb + " what?");
                 continue;
             }
             if (!verb.equals("spawn") && !verb.equals("remove") && !verb.equals("look") && !verb.equals("mode") && !verb.equals("make")) {
                 if (withCheck == null) {
                     System.out.println("Invalid syntax.");
                     continue;
                 }
             }
             if (!verb.equals("spawn") && !verb.equals("remove") && !verb.equals("look") && !verb.equals("mode") && !verb.equals("make")) {
                 if (object == null) {
                     System.out.println(verb + " " + subject + " with what?");
                     continue;
                 }
             }
             if (verb.equals("look")) {
                 if (world.show() != null && !world.show().equals("")) {
                     out.println("Around you, you can see " + world.show() + ".");
                 } else {
                     out.println("There are no objects around you.");
                 }
                 continue;
             }
             if (!verb.equals("spawn") && !verb.equals("remove") && !verb.equals("look") && !verb.equals("mode") && !verb.equals("mode") && !verb.equals("make")) {
                 if (!withCheck.equals("with")) {
                     out.println("Invalid syntax.");
                     continue;
                 }
             }
             if (verb.equals("mode")) {
                 if (subject.equals("1")) {
                     if (Game.getMode() != 1) {
                         Game.setFree();
                         System.out.println("Mode changed to 'Sandbox'.");
                     } else {
                         System.out.println("Mode is already set to Sandbox.");
                     }
                 } else if (subject.equals("2")) {
                     if (Game.getMode() != 2) {
                         Game.setNonFree();
                         System.out.println("Mode changed to 'Hardcore'.");
                     } else {
                         System.out.println("Mode is already set to Hardcore.");
                     }
                 } else {
                     System.out.println("Unknown mode setting.");
                 }
                 continue;
             }
             if (object.equals("")) {
                 object = "foo";
             }
             if (!Item.exists(subject) || subject.equalsIgnoreCase("foo")) {
                 System.out.println("What is a " + subject + "?");
                 continue;
             }
             abdjektReader.process(Game.newItem(subject), Game.newItem(object), verb);
         }
     }
 }
