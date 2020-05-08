 package RedPointMaven;
 
 import java.util.Set;
 import java.util.TreeMap;
 
 public class Roster {
     //use a TreeMap to order roster_list alphabetically
     TreeMap<String, Player> roster_list;
     private Set<String> myKeySet;
 
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
 
     private Player returnPlayer(String playerCode) {
         return roster_list.get(playerCode);
     }
 
     public String setGivee(String playerCode, String givee, int year) {
         return this.returnPlayer(playerCode).setGivee(givee, year);
     }
 
     public String returnGivee(String playerCode, int year) {
         return this.returnPlayer(playerCode).returnGivee(year);
     }
 
     //add a new empty year ("none") to each Player's givee array
     public void addNewYear() {
         myKeySet = roster_list.keySet();
 
         for (String aKey : myKeySet) {
             this.returnPlayer(aKey).addGivee("none");
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
         myKeySet = roster_list.keySet();
 
         for (String aKey : myKeySet) {
             playerName = this.returnPlayer(aKey).getPlayerName();
             giveeCode = this.returnPlayer(aKey).returnGivee(year);
             if (giveeCode.equals("none")) {
                 giveeName = "...nobody!! (last giver/givee pairing and a test failed - a puzzle logic error)";
             } else {
                giveeName = this.returnPlayer(aKey).getPlayerName();
             }
             System.out.println(playerName + " is buying for " + giveeName);
         }
     }
 }
