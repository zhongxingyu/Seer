 package rky.portfolio;
 
 import java.io.File;
 import java.util.Map;
 
 import rky.util.CommandLineUtils;
 
 public class Main
 {
 
     public static void main(String[] args)
     {
         Map<String,String> arguments = CommandLineUtils.simpleCommandLineParser(args);
         validate(arguments);
     }
     
     private static void validate(Map<String, String> args)
     {
         try
         {
             if (args.size() < 3 || args.size() > 6) 
             {
                 throw new Exception("Invalid number of arguments.");
             }
             
             // port
            String p = args.get("-p");
             if (p == null || !p.matches("[\\d]+"))
             {
                 throw new Exception("Invalid port.");
             }
 
             // num players
            String n = args.get("-n");
             if (n == null || !n.matches("[\\d]+"))
             {
                 throw new Exception("Invalid number of expected players.");
             }
 
             // game mode
            String m = args.get("-m");
             if (m == null || (!m.equals("1") && !m.equals("2")))
             {
                 throw new Exception("Invalid game mode. Accepted values are 1 or 2.");
             }
             
             // gui
            String g = args.get("-g");
             if (g == null)
             {
                 args.put("-g", "false");
             }
             else if (!g.equals("true") && !g.equals("false"))
             {
                 throw new Exception("Invalid gui param. Accepted values are true or false.");
             }
 
             // optional client input file (gambles and links)
            String f = args.get("-f");
             if (f != null && !(new File(f).isFile()))
             {
                 throw new Exception("Invalid client input file specified. Maybe use absolute an path? (Or omit for a new random file.)");
             }
             
             // class favorability file 
            String c = args.get("-c");
             if (c != null && !(new File(c).isFile()))
             {
                 throw new Exception("Invalid class favorability file specified. Maybe use absolute an path? (Or omit for the standard file.)");
             }
         }
         catch (Exception e)
         {
             usage(e.getMessage());
             System.exit(-1);
         }
     }
 
     private static void usage(String message)
     {
         System.err.println("\nInvalid/missing arguments. " + message);
         System.err.println("\t -p The port number to listen on.");
         System.err.println("\t -n The number of expected players.");
         System.err.println("\t -m The game mode. Accepted values are 1 or 2.");
         System.err.println("\t -c Optional. For game mode 2. The class favorability file as specified by Prof. Shasha. ex http://cs.nyu.edu/courses/Fall12/CSCI-GA.2965-001/portattformat");
         System.err.println("\t -g Optional. Display the gui? Accepted values are true or false. Defaults to false.");
         System.err.println("\t -f Optional. Use this file of gambles and links. If absent a new file is generated. ");
         System.err.println("\nex: java -jar portfolio-1.0.0.jar -p 54321 -n 5 -m 2 -c /favorability/file.txt -g false -f /gambles/file.txt ");
     }
     
 }
