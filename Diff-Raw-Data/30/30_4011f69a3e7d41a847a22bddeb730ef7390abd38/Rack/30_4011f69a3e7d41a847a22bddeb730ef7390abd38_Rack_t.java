 package client.model;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.IOException;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Bernard <bernard.debecker@gmail.com>, Romain <ro.foncier@gmail.com>
  */
 public class Rack {
 
     ObjectMapper om = new ObjectMapper();
     @JsonProperty("rack")
     private Tile[] rack = new Tile[7];
     
     public Rack() {}
 
     public Rack(String formatedRack) {
         try {
             this.loadRack(formatedRack);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
 
     /**
      * Load tiles on the rack from formated data.
      * where formatedRack match something like : [{"letter":"A","value":2},{"letter":"A","value":2}, ...]
      * @param formatedRack
      */
     private void loadRack(String formatedRack) throws IOException {
         //System.out.println("New rack : " + formatedRack);
         rack = om.readValue(formatedRack, Tile[].class);
     }
 
     public Tile getTile(Integer position) {
         return rack[position];
     }
 
     public void addTile(Integer position, Tile tile) {
         rack[position] = tile;
     }
 
     public Tile removeTile(Integer position) {
         Tile result = rack[position];
         rack[position] = null;
         return result;
     }
 
     /**
      * Put a new Tile in rack where is possible to add it
      * @param newTile 
      */
     protected void putTile(Tile newTile) {
         boolean found = false;
         int i = 0;
         while (!found && i < rack.length) {
             if (rack[i] == null) {
                if ((newTile.isBlank()) && (newTile.getLetter() != '?')) {
                     newTile.setBlank();
                 }
                 rack[i] = newTile;
                 found = true;
             }
             i++;
         }
     }
 
     public void reLoadRack(String formatedRack) {
       System.out.println("reloadrack" + formatedRack);
         Tile[] tileList = null;
         try {
             tileList = om.readValue(formatedRack, Tile[].class);
            System.out.println("tilelist" + tileList);
             for (int i = 0; i < tileList.length; i++) {
                addTile(i,tileList[i]);
             }
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
 
     /**
      * Allows to shift tiles on rack. From tile located on rack to rack or grid
      * to rack.
      *
      * @param startPos, stopPos
      */
     public void shiftTiles(int startPos, int stopPos) {
         // STEP 1 : Check the direction of shift and set index
         int DEC = (startPos - stopPos < 0) ? 1 : -1;
 
         // STEP 2 : Save the first element in a temp variable
         Tile tmpTile = rack[startPos];
 
         // STEP 3 : Loop over the rack to shift tiles.
         while (startPos != stopPos) {
             rack[startPos] = rack[startPos + DEC];
             startPos += DEC;
         }
 
         // STEP 4 : Remove the last element to drop the dragged element.
         rack[startPos] = tmpTile;
     }
 
     public int findEmptyParent(int targetPos) {
         int index = 1;
         int vacantPosition = -1;
         while (vacantPosition == -1 && index < 7) {
             if ((targetPos + index < 7) && (rack[targetPos + index] == null)) {
                 vacantPosition = targetPos + index;
             } else {
                 if ((targetPos - index >= 0) && (rack[targetPos - index] == null)) {
                     vacantPosition = targetPos - index;
                 }
             }
             index++;
         }
         return vacantPosition;
     }
 
     /**
      * This method allows to re-arrange the tiles on rack.
      *
      * @condition the rack should be full
      * @return array of new index
      */
     public int[] reArrangeTiles() {
         int[] result = {0, 1, 2, 3, 4, 5, 6};
         Tile[] newRack = new Tile[rack.length];
         Random random = new Random();
         random.nextInt();
         for (int from = 0; from < result.length; from++) {
             int to = from + random.nextInt(result.length - from);
             swap(result, from, to);
         }
 
         // Update the new rack
         for (int i = 0; i < rack.length; i++) {
             newRack[i] = rack[result[i]];
         }
         rack = newRack;
         return result;
     }
 
     private void swap(int[] index, int from, int to) {
         int tmp = index[from];
         index[from] = index[to];
         index[to] = tmp;
     }
 
     /**
      * @param position
      * @return JSON format of the current tile located at this
      * position on the rack : {"letter":"A","value":2}
      */
     public String getFormatedTile(Integer position) {
         return rack[position].toString();
     }
 
     /**
      * @param positions
      * @return JSON format of the all tiles located at their
      * respective position on the rack : [{"letter":"A","value":2},{"letter":"A","value":2}, ...]
      */
     public String getFormatedTiles(int[] positions) {
         String formatedTiles = "[";
         for (int i = 0; i < positions.length; i++) {
             formatedTiles += rack[positions[i]].toString();
             formatedTiles += (i < positions.length - 1) ? "," : "]";
         }
         return formatedTiles;
     }
     
     public String getFormatJSON() {
         String formatedTiles = "[";
         for (int i = 0; i < rack.length; i++) {
             formatedTiles += rack[i].toString();
             formatedTiles += (i < rack.length - 1) ? "," : "]";
         }
         return formatedTiles;
     }
 
     /**
      * Update the rack from the new tiles send by the server.
      * @param positions array of index
      * @param tiles JSON format of tiles list :  [{"letter":"A","value":2},{"letter":"A","value":2}, ...]
      */
     public void setFormatedTiles(int[] positions, String tiles) {
         Tile[] tileList = null;
         try {
             tileList = om.readValue(tiles, Tile[].class);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
         for (int i = 0; i < positions.length; i++) {
             rack[positions[i]] = tileList[i];
         }
     }
 
     public boolean isTileBlank(Integer position) {
         return rack[position].getValue() == 0;
     }
     
     public String toDisplay() {
         String result = "";
         for (int i = 0; i < 7; i++) {
             result += (rack[i] != null) ? rack[i].toDisplay() : "   ";
         }
         result += "\n_____ _____ _____ _____ _____ _____ _____\n"
                 + "  1     2     3     4     5     6     7\n";
         return result;
     }
 
     /**
      * Check if the rack contains blank tiles.
      * @return formated list of blank tiles index with the canvas :
      * [index]:[index]: ...
      */
     /*
     public String getBlankTile() {
         String result = "";
         for (int i = 0; i < rack.length; i++) {
             if (isTileBlank(i)) {
                 result += (i < rack.length - 1) ? "" + i + ":" : "" + i;
             }
         }
         return result;
     }*/
 }
