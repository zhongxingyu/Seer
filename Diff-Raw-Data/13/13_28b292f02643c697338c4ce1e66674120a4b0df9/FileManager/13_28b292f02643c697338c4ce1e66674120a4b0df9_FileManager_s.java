 package rky.portfolio.io;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.Random;
 
 import rky.portfolio.gambles.Gamble;
 import rky.portfolio.gambles.Luck;
 import rky.portfolio.gambles.Return;
 import rky.portfolio.io.GameData.ClassFavorabilityMap;
 import rky.util.SetMap;
 
 public class FileManager
 {
     private static final String FILE_NAME = "gambles-%d.txt";
     private static final String GAMBLES_HEADER = "# gamble_id, class_id, h_ret, h_prob, m_ret, m_prob, l_ret, l_prob\n";
     private static final String LINKS_HEADER = "# gi, gj\n";
     
     public static String writeInputFile(List<Gamble> gambles, SetMap<Integer, Integer> links) 
     {
         try
         {
             File file = new File(String.format(FILE_NAME, System.currentTimeMillis()));
             file.createNewFile();
             FileWriter fw = new FileWriter(file.getAbsoluteFile());
             BufferedWriter bw = new BufferedWriter(fw);
             
             bw.write(GAMBLES_HEADER);
             for( int i = 0; i < gambles.size(); i++ )
             {
                 Gamble g = gambles.get(i);
                 bw.write(String.format(
                        "%d, %d, %f, %f, %f, %f, %f \n",
                        i, new Random().nextInt() % 16,
                         g.getV(Return.high  ), g.getP(Return.high),
                         g.getV(Return.medium), g.getP(Return.medium),
                         g.getV(Return.low   ), g.getP(Return.low)
                 ));
             }
             
             links = SetMap.distinct(links);
             
             bw.write("\n"+LINKS_HEADER);
             for (Integer key : links.keySet())
             {
                 for (Integer val : links.get(key))
                 {
                     bw.write(String.format("%d, %d \n", key, val));
                 }
             }
             
             bw.close();
             
             return file.getAbsolutePath();
         }
         catch (IOException e)
         {
             throw new RuntimeException("Unable to generate input file.");
         }
     }
     
     // For game mode 1
     public static GameData readGameData(String clientInputFile)
     {   
        return readGameData(clientInputFile, "src/main/resources/favorability.txt");
     }
     
     // For game mode 2
     public static GameData readGameData(String clientInputFile, String classFavoribilityFile)
     {
         GameData gd = new GameData();
         
         BufferedReader br = null;
         try
         {
             br = new BufferedReader(new FileReader(clientInputFile));
             String line;
             while ((line = br.readLine()) != null)
             {
             	if( line.length() == 0 || line.charAt(0) == '#')
             		continue;
             	
                 String[] tokens = line.split(",");
                 
                 if (tokens.length > 2) // parsing gambles 
                 {
                     Integer gambleId = Integer.parseInt(tokens[0].trim());
                     Integer classId = Integer.parseInt(tokens[1].trim());   // !!!
                     Double hv = Double.parseDouble(tokens[2].trim());
                     Double hp = Double.parseDouble(tokens[3].trim());
                     Double mv = Double.parseDouble(tokens[4].trim());
                     Double mp = Double.parseDouble(tokens[5].trim());
                     Double lv = Double.parseDouble(tokens[6].trim());
                     
                     Gamble gamble = new Gamble(hv, hp, mv, mp, lv);
                     
                     gd.gambles.put(gambleId, gamble);
                     gd.ids.put(gamble, gambleId);
                     gd.gambleClasses.put(gamble, classId);
                 }
                 else
                 {
                     Integer gi = Integer.parseInt(tokens[0].trim());  // !!!
                     Integer gj = Integer.parseInt(tokens[1].trim());
                     gd.links.put ( 
                         gd.gambles.get(gi), 
                         gd.gambles.get(gj)
                     );
                 }
             }
         }
         catch (IOException e)
         {
             throw new RuntimeException("Unable to read input file.");
         }
         finally
         {
             try
             {
                 br.close();
             } catch (Exception ex) { }
         }
         
         readClassFavoribilityFile(classFavoribilityFile, gd.classFavorability);
         
         return gd;
     }
     
     public static void readClassFavoribilityFile(String classFavoribilityFile, ClassFavorabilityMap map)
     {
         try
         {
             BufferedReader br = new BufferedReader(new FileReader(classFavoribilityFile));
             String line;
             while ((line = br.readLine()) != null)
             {
                 Integer round = null;
                 Luck luck = null;
                 String classes = null;
                 if (line.contains("Round"))
                 {
                     round = Integer.parseInt(br.readLine());
                     // Read Favorable
                     luck = br.readLine().contains("Unfavorable") ? Luck.unfavorable : Luck.favorable;
                     classes = br.readLine();
                     for (String c : classes.split("\\s+"))
                     {
                         int classId = Integer.parseInt(c, 2);
                         map.put(round, classId, luck);
                     }
                     // Read Unfavorable
                     luck = br.readLine().contains("Unfavorable") ? Luck.unfavorable : Luck.favorable;
                     classes = br.readLine();
                     for (String c : classes.split("\\s+"))
                     {
                         int classId = Integer.parseInt(c, 2);
                         map.put(round, classId, luck);
                     }
                 }
             }
             br.close();
         }
         catch (Exception e)
         {
             throw new RuntimeException("Unable to read class favoribility file.");
         }
     }
     
 }
