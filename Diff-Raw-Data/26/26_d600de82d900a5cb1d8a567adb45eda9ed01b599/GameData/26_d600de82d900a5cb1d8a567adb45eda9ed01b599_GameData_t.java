 package versioning;
 
 import util.PersistantMap;
 import util.RelativeFile;
 
 /*
  * Created on Mar 2, 2005 By iago
  */
 
 public class GameData {
     
     /**
      * The PersistantMap holding game data
      */
     private PersistantMap games;
     
     public GameData() {
         games = new PersistantMap(new RelativeFile("_GameData.txt"),
                 "These are the important informations for games to log in with "
                 + "-- values are stored in hex.");
         this.initialize();
     }
     
     /**
      * Gets the version byte for <b>game</b>
      */
     public int getVersionByte(String game) {
         return Integer.parseInt(games.getNoWrite(game, "Version byte", "0"), 16);
     }
     
     /**
      * Gets the version hash for <b>game</b>
      */
     public int getVersionHash(String game) {
         int calc = VerHash.getVersion(games.getNoWrite(game, "File1", null));
         if(calc == 0) // File1 is absent
             calc = Integer.parseInt(games.getNoWrite(game, "Version hash", "0"),
                     16);
         return calc;
     }
     
     /**
      * Gets the hash file directory
      */
     public String getHashFileDirectory() {
         return games.getWrite("[default]", "hash directory",
                 System.getProperty("user.home") + "/.hashes/");
     }
     
     /**
      * Gets a String array of files for doing checkrevision
      */
     public String[] getFiles(String game) {
         String prefix = getHashFileDirectory();
         String[] files = new String[4];
         files[0] = prefix + games.getNoWrite(game, "File1", null);
         files[1] = prefix + games.getNoWrite(game, "File2", null);
         files[2] = prefix + games.getNoWrite(game, "File3", null);
         files[3] = prefix + games.getNoWrite(game, "File4", null);
         
         return files;
     }
     
     /**
      * Gets a boolean indicating if a certain game takes two keys to log in
      */
     public boolean hasTwoKeys(String game) {
        if (game.equals("D2XP")) {
             return true;
         } else if (game.equals("W3XP")) {
             return true;
         } else {
             return false;
         }
     }
     
    /**
     * Gets a boolean indicating if a certain game has a server signature
     */
    public boolean hasServerSignature(String game) {
        if (game.equals("WAR3") || game.equals("W3XP"))
            return true;
        else
            return false;
    }
     
     private void initialize() {
         games.getWrite("STAR", "Version byte", "D3");
         games.getWrite("SEXP", "Version byte", "D3");
         games.getWrite("D2DV", "Version byte", "0C");
         games.getWrite("D2XP", "Version byte", "0C");
         games.getWrite("W2BN", "Version byte", "4F");
         games.getWrite("WAR3", "Version byte", "17");
         games.getWrite("W3XP", "Version byte", "17");
         
         games.getWrite("STAR", "File1", "STAR/starcraft.exe");
         games.getWrite("STAR", "File2", "STAR/storm.dll");
         games.getWrite("STAR", "File3", "STAR/battle.snp");
         games.getWrite("STAR", "File4", "STAR/STAR.bin");
         games.getWrite("SEXP", "File1", "STAR/starcraft.exe");
         games.getWrite("SEXP", "File2", "STAR/storm.dll");
         games.getWrite("SEXP", "File3", "STAR/battle.snp");
         games.getWrite("SEXP", "File4", "STAR/STAR.bin");
         games.getWrite("D2DV", "File1", "D2DV/Game.exe");
         games.getWrite("D2DV", "File2", "D2DV/Bnclient.dll");
         games.getWrite("D2DV", "File3", "D2DV/D2Client.dll");
         games.getWrite("D2DV", "File4", "D2DV/D2DV.bin");
         games.getWrite("D2XP", "File1", "D2XP/Game.exe");
         games.getWrite("D2XP", "File2", "D2XP/Bnclient.dll");
         games.getWrite("D2XP", "File3", "D2XP/D2Client.dll");
         games.getWrite("D2XP", "File4", "D2XP/D2XP.bin");
         games.getWrite("W2BN", "File1", "W2BN/Warcraft II BNE.exe");
         games.getWrite("W2BN", "File2", "W2BN/storm.dll");
         games.getWrite("W2BN", "File3", "W2BN/battle.snp");
         games.getWrite("W2BN", "File4", "W2BN/W2BN.bin");
         games.getWrite("WAR3", "File1", "WAR3/war3.exe");
         games.getWrite("WAR3", "File2", "WAR3/Storm.dll");
         games.getWrite("WAR3", "File3", "WAR3/game.dll");
         games.getWrite("WAR3", "File4", "WAR3/WAR3.bin");
         games.getWrite("W3XP", "File1", "WAR3/war3.exe");
         games.getWrite("W3XP", "File2", "WAR3/Storm.dll");
         games.getWrite("W3XP", "File3", "WAR3/game.dll");
         games.getWrite("W3XP", "File4", "WAR3/WAR3.bin");
         
         games.getWrite("STAR", "Version hash", "01010303");
         games.getWrite("SEXP", "Version hash", "01010303");
         games.getWrite("D2DV", "Version hash", "01000c00");
         games.getWrite("D2XP", "Version hash", "01000c00");
         games.getWrite("W2BN", "Version hash", "01010001");
         games.getWrite("WAR3", "Version hash", "01001027");
         games.getWrite("W3XP", "Version hash", "01001027");
     }
     
 }
