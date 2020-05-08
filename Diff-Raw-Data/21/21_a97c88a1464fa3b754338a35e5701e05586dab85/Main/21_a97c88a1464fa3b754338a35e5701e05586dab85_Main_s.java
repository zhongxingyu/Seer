 package rky.portfolio;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 import rky.portfolio.Player.Players;
 import rky.portfolio.gambles.Gamble;
 import rky.portfolio.gambles.GambleGenerator;
 import rky.portfolio.io.FileManager;
 import rky.portfolio.io.GameData;
 import rky.portfolio.io.IoManager;
 import rky.portfolio.io.Message;
 import rky.util.CommandLineUtils;
 import rky.util.SetMap;
 
 public class Main
 {
     
     public static void main(String[] args)
     {
         // Parse and validate arguments
         Map<String,String> argMap = CommandLineUtils.simpleCommandLineParser(args);
         validate(argMap);
         
        // Initialize connections with Players
         Integer port = Integer.parseInt(argMap.get("-p"));
         IoManager io = new IoManager(port);
         Integer numPlayers = Integer.parseInt(argMap.get("-n"));
         Players players = io.connect(numPlayers);
         
         // Maybe generate a new set of gambles
         String clientInputFile = argMap.get("-f");
         if (clientInputFile == null)
         {
             List<Gamble> gambles = GambleGenerator.generateGambles();
             SetMap<Integer, Integer> links = GambleGenerator.generateLinks(gambles);
             clientInputFile = FileManager.writeInputFile(gambles, links);
         }
         
         // Generate game data
         GameData gameData = null;
         String classFavorabilityFile = argMap.get("-c");
         if (classFavorabilityFile == null)
         {
             gameData = FileManager.readGameData(clientInputFile);
         }
         else
         {
             gameData = FileManager.readGameData(clientInputFile, classFavorabilityFile);
         }
         
         // Send game initialization message to all players
         String mode = argMap.get("-m");
         Message init = new Message(clientInputFile, mode, getNumRounds(mode));
         for (Player p : players)
         {
             p.send(init);
         }
         
         // Run game
         GameLoop loop = new GameLoop( gameData, players, getModeEnum(mode) );
         loop.run();
         ScoreBoard scores = loop.getScoreBoard();
         
        // Send game over message
         for (Player p : players)
         {
             p.send(Message.createGameOver(scores.getFinalScore(p)));
         }
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
             if (m == null || (!m.equals("mode-1") && !m.equals("mode-2")))
             {
                 throw new Exception("Invalid game mode. Accepted values are mode-1 or mode-2.");
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
         System.err.println("\t -m The game mode. Accepted values are mode-1 or mode-2.");
         System.err.println("\t -c Optional. For game mode 2. The class favorability file as specified by Prof. Shasha. ex http://cs.nyu.edu/courses/Fall12/CSCI-GA.2965-001/portattformat");
         System.err.println("\t -g Optional. Display the gui? Accepted values are true or false. Defaults to false.");
         System.err.println("\t -f Optional. Use this file of gambles and links. If absent a new file is generated. ");
        System.err.println("\nex: java -jar portfolio-1.0.0.jar -p 54321 -n 5 -m 2 -c /favorability/file.txt -g false -f /gambles/file.txt");
     }
     
     public static String getModeString(ScoreBoard.GameMode mode)
     {
         if (mode == ScoreBoard.GameMode.mode1)
         {
             return "mode-1";
         }
         return "mode-2";
     }
     
     public static ScoreBoard.GameMode getModeEnum(String mode)
     {
         if (mode.equals("mode-1"))
         {
             return ScoreBoard.GameMode.mode1;
         }
         
         return ScoreBoard.GameMode.mode2;
     }
     
     private static String getNumRounds(String mode)
     {
         Integer numRounds = 5;
         ScoreBoard.GameMode enumMode = getModeEnum(mode);
        if(ScoreBoard.GameMode.mode2 == enumMode) {
             numRounds = 200;
         }
         return numRounds.toString();
     }
 }
