 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Iterator;
 import java.util.TreeMap;
 
 /**
  * Handles online storage of high score data. The top 10 high scores,
  * referenced by score, are stored in a Map<Integer,String>, which is written
  * and read between the client and server via ObjectIOStream.
  * @author Zach Cotter
  */
 public class HighScore {
 
     private TreeMap<Integer, String> map;
     public static final int NUMBER_OF_FINALISTS = 10;
     //TODO censor login
     private static final String ftpUserName = "";//CENSORED, replace null with username
     private static final String ftpPassword = "";//CENSORED, replace null with password
	private static final String hsTxt = "@www.zachcotter.com/zachcotter.com/Tetris/classes/hs.txt";
 
     //TODO: change Integer to Integer Set to allow ties--> restructure as needed
 
     /**
      * Constructs a new HighScore.  Attempts to read data from the server
      * into the Map. If no file is found, a new Map and file are created.
      */
     public HighScore() {
         map = new TreeMap<Integer, String>();
         URL scoreFileUrl = null;
         try {
            scoreFileUrl = new URL("ftp://" + ftpUserName + ":" + ftpPassword + hsTxt);
         }
         catch (MalformedURLException ex) {
             System.out.println(ex + "0");
         }
         readFromFile(scoreFileUrl);
     }
 
     /**
      * Constructs a new HighScore with the given data as the first and only
      * initial Map entry. No data is read from the server, but the new data is
      * written there.  If a file previously existed, it is overwritten by the
      * current data.
      * @param firstName String to be put in the first MapEntry (name)
      * @param firstScore Integer to be put in the first MapEntry (score)
      */
     public HighScore(String firstName, int firstScore){
         map = new TreeMap<Integer, String>();
         map.put(firstScore, firstName);
         save();
     }
 
     /**
      * Adds the given data to the map if the score qualifies as top ten.
      * @param contendent Name of scorer
      * @param score Score to be added if possible
      * @return Whether or not the data was eligible.
      */
     public boolean addListingIfEligible(String contendent, int score){
         map.put(score, contendent);
         if(map.size() > 10){
             map.remove(map.firstKey());
         }
         return true;
     }
 
     /**
      * Writes the Map to the server.
      */
     public void save(){
         URL ftpurl = null;
         try {
             ftpurl =
            new URL("ftp://" + ftpUserName + ":" + ftpPassword + hsTxt);
         }
         catch (MalformedURLException ex) {
             System.out.println(ex + "1");
         }
         try {
             writeToFile(ftpurl);
         }
         catch (IOException ex) {
             System.out.println(ex + "2");
         }
     }
 
     /**
      * Reads data from the server at the given URL using and ObjectOutputStream.
      * @param httpURL URL of server and file location.
      */
     private void readFromFile(URL httpURL){
         try {
             URLConnection connection = httpURL.openConnection();
             connection.connect();
             ObjectInputStream inStream = new ObjectInputStream(connection.getInputStream());
             map = (TreeMap<Integer, String>) inStream.readObject();
         }
         catch (IOException ex) {
             System.out.println(ex + "3");
         }
         catch (ClassNotFoundException ex){
             System.out.println(ex + "4");
         }
     }
 
     /**
      * Writes data to the given FileTransferProtocol server using the given
      * URL, which must include appropriate login information.
      * @param loggedInFTP URL which has an open connection to the data on the server
      * @throws IOException if a connection error occurs.
      */
     private void writeToFile(URL loggedInFTP) throws IOException{
         URLConnection connection = loggedInFTP.openConnection();
         connection.setDoOutput(true);
         ObjectOutputStream outStream = new ObjectOutputStream(connection.getOutputStream());
         outStream.writeObject(map);
         outStream.close();
     }
 
     /**
      * Determines the lowest score in the Map
      * @return int representing lowest score.
      */
     public int getMinimumToQualify(){
         return map.firstKey();
     }
 
 
     /**
      * String representation of data in Map.
      * @return String representation as described above.
      */
     @Override
     public String toString() {
         String str = "";
         Iterator<Integer> scores = map.descendingKeySet().iterator();
         while(scores.hasNext()){
             int i = scores.next();
             str += map.get(i) + ": " + i + "\n";
         }
         return str;
     }
 }
