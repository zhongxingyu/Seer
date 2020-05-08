 package RedPointMaven;
 
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.TreeMap;
 
 public class Roster {
     //use a TreeMap to order roster_list alphabetically by key
     TreeMap<String, Player> roster_list;
     private Set<String> playerCodeKeySet;
 
     public Roster() {
         roster_list = new TreeMap<String, Player>();
         roster_list.put("AdaBur", new Player("Adam Burish", "DunKei"));
         roster_list.put("AndLad", new Player("Andrew Ladd", "JoeQue"));
         roster_list.put("AntNie", new Player("Antti Niemi", "JonToe"));
         roster_list.put("BreSea", new Player("Brent Seabrook", "KriVer"));
         roster_list.put("BryBic", new Player("Bryan Bickell", "MarHos"));
         roster_list.put("BriCam", new Player("Brian Campbell", "NikHja"));
         roster_list.put("CriHue", new Player("Cristobal Huet", "PatKan"));
         roster_list.put("DavBol", new Player("Dave Bolland", "PatSha"));
         roster_list.put("DunKei", new Player("Duncan Keith", "TomKop"));
         roster_list.put("JoeQue", new Player("Joel Quenneville", "TroBro"));
         roster_list.put("JonToe", new Player("Jonathan Toews", "AdaBur"));
         roster_list.put("KriVer", new Player("Kris Versteeg", "AndLad"));
         roster_list.put("MarHos", new Player("Marian Hossa", "AntNie"));
         roster_list.put("NikHja", new Player("Niklas Hjalmarsson", "BreSea"));
         roster_list.put("PatKan", new Player("Patrick Kane", "BryBic"));
         roster_list.put("PatSha", new Player("Patrick Sharp", "BriCam"));
         roster_list.put("TomKop", new Player("Tomas Kopecky", "CriHue"));
         roster_list.put("TroBro", new Player("Troy Brouwer", "DavBol"));
     }
 
     //inner class
     private class Player {
         String playerName;
         ArrayList<String> pastGiveesCodes;
 
         private Player(String playerName, String giveeCodeYearZero) {
             this.playerName = playerName;
             pastGiveesCodes = new ArrayList<String>();
             pastGiveesCodes.add(0, giveeCodeYearZero);
         }
 
         //return playerName
         private String getPlayerName() {
             return playerName;
         }
 
         //add a giveeCode to array of past givees
         private boolean addGiveeCode(String giveeCode) {
             return pastGiveesCodes.add(giveeCode);
         }
 
         //return a giveeCode given a year
         private String returnGiveeCode(int giftYear) {
             return pastGiveesCodes.get(giftYear);
         }
 
         //set a giveeCode in a given year
         private String setGiveeCode(String giveeCode, int year) {
             return pastGiveesCodes.set(year, giveeCode);
         }
     }
 
     private Player returnPlayer(String playerCode) {
         return roster_list.get(playerCode);
     }
 
     public String setGiveeCode(String playerCode, String giveeCode, int year) {
         return this.returnPlayer(playerCode).setGiveeCode(giveeCode, year);
     }
 
     public String returnGiveeCode(String playerCode, int year) {
         return this.returnPlayer(playerCode).returnGiveeCode(year);
     }
 
     //add a new empty year ("none") to each Player's givee array
     public void addNewYear() {
         playerCodeKeySet = roster_list.keySet();
 
         for (String aKey : playerCodeKeySet) {
             this.returnPlayer(aKey).addGiveeCode("none");
         }
     }
 
     public void printGivingRoster(int year) {
         /*
         uses key:value pair functionality of keySet.
         returns a msg if no match (playerCode = "none")
         where last giver/givee in Hats fail a test.
         */
         String playerName;
         String giveeCode;
         String giveeName;
         playerCodeKeySet = roster_list.keySet();
 
         for (String aKey : playerCodeKeySet) {
             playerName = this.returnPlayer(aKey).getPlayerName();
             giveeCode = this.returnPlayer(aKey).returnGiveeCode(year);
             if (giveeCode.equals("none")) {
                 giveeName = "...nobody!! (last giver/givee pairing and a test failed - a puzzle logic error)";
             } else {
                giveeName = this.returnPlayer(giveeCode).playerName;
             }
             System.out.println(playerName + " is buying for " + giveeName);
         }
     }
 }
