 /*
  * Game.java
  * 
  * Created on March 9, 2004, 9:03 AM
  */
 
 package versioning;
 
 import util.Buffer;
 import util.PadString;
 import util.RelativeFile;
 
 import java.io.IOException;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Vector;
 
 import cdkey.Decode;
 
 import exceptions.LoginException;
 
 public class Game
 {
 
     private static GameData gameData;
 
     private String game;
 
     static {
         gameData = new GameData();
     }
 
     public Game(String game) throws LoginException {
         this.game = getCodeFromLongName(game);
     }
     
     /**
      * Takes a user-inputted game name and shortens it to the 4-letter code.
      * @throws LoginException Long name not recognized
      */
     private String getCodeFromLongName(String game) throws LoginException {
        if (game == null)
            return "";


         game = game.toLowerCase();
         game = game.replace("iiii", "4"); // who knows?
         game = game.replace("iii", "3");
         game = game.replace("ii", "2");
         game = game.replace(" ", "");
         game = game.replace(":", "");
         
         // StarCraft
         if (game.equals("starcraft")
         || game.equals("star")
         || game.equals("sc"))
             return "STAR";
         // StarCraft: Brood War
         if (game.equals("sexp")
         || game.equals("broodwar"))
             return "SEXP";
         // WarCraft II: Battle.net Edition
         if (game.equals("w2bn")
         || game.equals("war2")
         || game.equals("warcraft2")
         || game.equals("warcraft2bne")
         || game.equals("wc2"))
             return "W2BN";
         // Diablo II
         if (game.equals("d2dv")
         || game.equals("d2")
         || game.equals("diablo2"))
             return "D2DV";
         // Diablo II: Lord of Destruction
         if (game.equals("d2xp")
         || game.equals("lod")
         || game.equals("diablo2:lod"))
             return "D2XP";
         // WarCraft III: Reign of Chaos
         if (game.equals("war3")
         || game.equals("warcraft3")
         || game.equals("warcraft3roc"))
             return "WAR3";
         // WarCraft III: The Frozen Throne
         if(game.equals("w3xp")
         || game.equals("tft")
         || game.equals("warcraft3tft"))
             return "W3XP";
         
         throw new LoginException("Game name not understood - " + game +
                 "\nValid options: " + getGames().toString());
     }
 
     public int getVersionByte() {
         return gameData.getVersionByte(game);
     }
 
     public int getVersionHash() {
         return gameData.getVersionHash(game);
     }
 
     public int getGameCode() {
         return (game.charAt(0) << 24) |
                (game.charAt(1) << 16) |
                (game.charAt(2) << 8) |
                (game.charAt(3) << 0);
     }
 
     public String getName() {
         return game;
     }
 
     public String getExeInfo() {
         // return "starcraft.exe 03/28/03 04:21:56 1064960";
 
         RelativeFile f = new RelativeFile(gameData.getFiles(game)[0]);
 
         // Set up a calendar to point at the last modified time
         Calendar c = Calendar.getInstance();
         c.setTime(new Date(f.lastModified()));
 
         StringBuffer exeInfo = new StringBuffer();
 
         // Write to the exeInfo buffer
         exeInfo.append(f.getName()).append(" ");
 
         // date
         exeInfo.append(PadString.padNumber(c.get(Calendar.MONTH), 2)).append("/");
         exeInfo.append(PadString.padNumber(c.get(Calendar.DAY_OF_MONTH), 2)).append("/");
         exeInfo.append(PadString.padNumber((c.get(Calendar.YEAR) % 100), 2)).append(" ");
 
         // time
         exeInfo.append(PadString.padNumber(c.get(Calendar.HOUR_OF_DAY), 2)).append(":");
         exeInfo.append(PadString.padNumber(c.get(Calendar.MINUTE), 2)).append(":");
         exeInfo.append(PadString.padNumber(c.get(Calendar.SECOND), 2)).append(" ");
 
         // size
         exeInfo.append(f.length());
 
         return exeInfo.toString();
     }
 
     public int doCheckRevision(byte[] formula, String mpqFile) throws LoginException, IOException {
         return CheckRevision.doCheckRevision(mpqFile, gameData.getFiles(game), formula);
     }
 
     /**
      * Gets a Vector of all possible games.
      * 
      * @return A Vector of all possible games.
      */
     public static Vector<String> getGames() {
         Vector<String> v = new Vector<String>();
         v.add("Starcraft");
         v.add("Brood War");
         v.add("Warcraft II");
         v.add("Diablo II");
         v.add("Diablo II: LoD");
         v.add("Warcraft III");
         v.add("Warcraft III: TFT");
 
         return v;
     }
 
     /**
      * Gets the number of keys, using spawn, and key hash for the cdkey.
      */
     public Buffer getKeyBuffer(String cdkey1, String cdkey2, int clientToken, int serverToken) throws LoginException {
         Buffer ret = new Buffer();
 
         if(!gameData.hasTwoKeys(game)) {
             ret.addDWord(1);
             ret.addDWord(0);
             ret.add(getKeyBlock(cdkey1, clientToken, serverToken));
         } else {
             ret.addDWord(2);
             ret.addDWord(0);
             ret.add(getKeyBlock(cdkey1, clientToken, serverToken));
             ret.add(getKeyBlock(cdkey2, clientToken, serverToken));
         }
         return ret;
     }
 
     private Buffer getKeyBlock(String cdkey, int clientToken, int serverToken) throws LoginException {
         Decode key = Decode.getDecoder(cdkey);
                
         Buffer ret = new Buffer();
         // For Each Key:
         // (DWORD) Key Length
         ret.addDWord(cdkey.length());
         // (DWORD) Product
         ret.addDWord(key.getProduct());
         // (DWORD) CDKEY Value 1
         ret.addDWord(key.getVal1());
         // (DWORD) Unknown (0)
         ret.addDWord(0);
         // (DWORD[5]) Hashed Key Data
 
         int[] hashedKey = key.getKeyHash(clientToken, serverToken);
         for(int i = 0; i < 5; i++)
             ret.addDWord(hashedKey[i]);
 
         System.out.println("Buffer:\n" + ret.toString());
 
         return ret;
     }
 
     public String toString() {
         return game + " (" + getExeInfo() + ", Version Hash = 0x" + Integer.toHexString(gameData.getVersionHash(game))
                 + ", Version Byte = 0x" + Integer.toHexString(gameData.getVersionByte(game)) + ")";
     }
 }
