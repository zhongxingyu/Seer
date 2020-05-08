 package server.server.model;
 
 import client.model.utils.Point;
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Romain <ro.foncier@gmail.com>, Bernard <bernard.debecker@gmail.com>
  */
 public class Play {
 
     private String playID;
     private String owner;
     private Integer score;
     private Grid grid;
     private Rack rack;
     private TileBag bag;
     private final boolean isAnonym;
     // Game variables
     protected String lastWord;
     protected int lastWordScore;
     // Stats variables
     private int nbTests = 0;
     private int testsWithSuccess = 0;
     private int testsWithError = 0;
     private int innerIndice = 0;
     // JSON treatment
     private ObjectMapper om = new ObjectMapper();
 
     /**
      * Constructor for Play instance from simply playerID.
      *
      * @param playerID
      */
     public Play(String playerID, boolean anonym) {
         playID = UUID.randomUUID().toString();
         owner = playerID;
         score = 0;
         grid = new Grid();
         bag = new TileBag();
         rack = new Rack(bag);
         this.isAnonym = anonym;
     }
 
     /**
      * @return string representation of Play ID.
      */
     public String getPlayID() {
         return playID;
     }
 
     public String getOwner() {
         return owner;
     }
 
     public boolean isAnonym() {
         return this.isAnonym;
     }
 
     /**
      * Put the player's tiles on the game board.
      *
      * @param args
      * @return a List which contains the tiles coordinates and their index in
      * the rack.
      */
     protected ArrayList<Tile> tilesSetUp(String args) {
         ArrayList result = new ArrayList();
         try {
             JsonNode root = om.readTree(args);
             for (Iterator<JsonNode> it = root.iterator(); it.hasNext();) {
                 JsonNode tile = it.next();
                 JsonNode coord = tile.get("coordinates");
                 JsonNode attrs = tile.get("attributes");
                 Tile t = om.readValue(attrs.toString(), Tile.class);
                 Point p = om.readValue(coord.toString(), Point.class);
 
                 // Tile treatment
                 result.add(t);
                 t.upStatus(); // Set this tile like a new add in the grid.
                 if (!rack.removeTileFromRack(t)) {
                     // Improve this with Exception handler
                     System.out.println("Tile not found in rack :" + t);
                     break;
                 }
                 grid.putInGrid(p.x, p.y, t); // Put this tile on the game board and add it its coordinates.
             }
         } catch (IOException ioe) {
             System.out.println("Error with JSON");
             ioe.printStackTrace();
         }
         return result;
     }
 
     /**
      * Remove the tiles added if the test contains some errors and take them
      * back in Rack
      *
      * @param tilesList
      */
     protected void removeBadTiles(ArrayList<Tile> tilesList) {
         for (int i = 0; i < tilesList.size(); i++) {
             grid.removeInGrid(tilesList.get(i).getX(), tilesList.get(i).getY()); // remove pointer
             rack.putTile(tilesList.get(i));
         }
     }
 
     /**
      * Check neighbors tiles of the current tile, create a word form their
      * letters and count the score of this new word by considering the
      * orientation given in parameter.
      *
      * @param cTile
      * @param orientation
      */
     protected void wordTreatment(Tile cTile, int orientation) {
         // Initialize values
         lastWordScore = 0;
         lastWord = "";
         String p = "", n = "";
 
         // Display Grid and Rack
         //System.out.println(grid.toString());
         //System.out.println(rack.displayRack());
 
         // Initialization from current tile.
         int wordCounter = grid.scoringGrid.checkBonus(cTile, this);
         lastWord += cTile.getLetter();
 
         Tile prev = grid.previousTile(cTile, orientation);
         Tile next = grid.nextTile(cTile, orientation);
 
         while (prev != null || next != null) {
             if (prev != null) {
                 p = prev.getLetter() + p;
                 if (prev.getStatus()) {
                     int nScore = grid.scoringGrid.checkBonus(prev, this);
                     wordCounter = (wordCounter < nScore) ? nScore : wordCounter; // If wordCounter == 3 (Triple word) and the prev is placed on double word case, wordCounter keep this initial value (i.e 3).
                     prev.downStatus(); // set the status of this tile to false.
                 } else {
                     lastWordScore += prev.getValue();
                 }
                 prev = grid.previousTile(prev, orientation);
             }
             if (next != null) {
                 n = n + next.getLetter();
                 if (next.getStatus()) {
                     int nScore = grid.scoringGrid.checkBonus(next, this);
                     wordCounter = (wordCounter < nScore) ? nScore : wordCounter;
                     next.downStatus();
                 } else {
                     lastWordScore += next.getValue();
                 }
                 next = grid.nextTile(next, orientation);
             }
         }
         lastWord = p + lastWord + n;
         lastWordScore *= wordCounter;
         if (lastWord.length() < 2) { // In case where there would be only one character.
             lastWord = "";
             lastWordScore = 0;
         }
     }
 
     /**
      * Calculate the score for the currentWord (in treatment)
      *
      * @param score
      */
     protected void setLastWordScore(int score) {
         lastWordScore += score;
     }
 
     /**
      * Set the score of the player game.
      *
      * @param score
      */
     protected void setScore(int score) {
         this.score += score;
     }
 
     /**
      * Get the score for this play.
      *
      * @return
      */
     protected int getScore() {
         return this.score;
     }
 
     /**
      * Update the rack on the server side by adding new tiles from the bag and
      * format them to send on the client.
      *
      * @param tilesList
      * @return Formated list of tile in JSON :
      * [{"letter":"A","value":2},{"letter":"A","value":2}, ...]
      */
     protected String getNewTiles(int numberTile) {
         String result = "[";
         for (int i = 0; i < numberTile; i++) {
             Tile nTile = bag.getTile();
             rack.putTile(nTile);
             result += nTile;
             if (i < numberTile - 1) {
                 result += ", ";
             }
         }
         return result + "]";
     }
 
     /**
      * Return a String representation of the rack
      *
      * @return JSON : [{"letter": "A", "value": 1, "blank": false}, ...]
      */
     public String getFormatRack() {
         return this.rack.toString();
     }
 
     /**
      * Return a String representation of the grid
      *
      * @return
      */
     public String getGrid() {
         return this.grid.toString();
     }
 
     public String exchangeTiles(String tiles) {
        Logger.getLogger(Play.class.getName()).log(Level.INFO, null, tiles);
         Tile[] tileArray = null;
         try {
             tileArray = om.readValue(tiles, Tile[].class);
         } catch (IOException ex) {
             Logger.getLogger(Play.class.getName()).log(Level.SEVERE, null, ex);
         }
         if (tileArray != null) {
             String newTiles = this.getNewTiles(tileArray.length);
             try {
                 Tile[] newTileArray = om.readValue(newTiles, Tile[].class);
                 for (int i = 0; i < tileArray.length; i++) {
                     int tmp = rack.getTilePos(tileArray[i].getLetter());
                     rack.setTile(tmp-1, newTileArray[i]);
                     bag.putBackTile(tileArray[i]);
                 }
             } catch (IOException ex) {
                 Logger.getLogger(Play.class.getName()).log(Level.SEVERE, null, ex);
             }
             return rack.getRack().toString();
         } else {
             return "";
         }
 
 
     }
 
     //*** Stats Section ***//
     protected void newTest() {
         this.nbTests += 1;
     }
 
     protected int getTestsPlayed() {
         return this.nbTests;
     }
 
     protected void testWithSuccess() {
         this.testsWithSuccess += 1;
     }
 
     protected int getTestsWon() {
         return this.testsWithSuccess;
     }
 
     protected void testWithError() {
         this.testsWithError += 1;
     }
 
     protected int getTestsLost() {
         return this.testsWithError;
     }
 
     protected void increaseIndice() {
         this.innerIndice++;
     }
 
     protected int getInnerIndice() {
         return this.innerIndice;
     }
 }
