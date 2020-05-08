 package client.model;
 
 import client.model.event.ErrorListener;
 import client.model.event.ErrorMessageEvent;
 import client.model.event.GridListener;
 import client.model.event.InitMenuInterfaceEvent;
 import client.model.event.InitRackEvent;
 import client.model.event.MenuListener;
 import client.model.event.RackListener;
 import client.model.event.RackReArrangeEvent;
 import client.model.event.RemoveBadTilesEvent;
 import client.model.event.TileFromGridToGridEvent;
 import client.model.event.TileFromGridToRackEvent;
 import client.model.event.TileFromGridToRackWithShiftEvent;
 import client.model.event.TileFromRackToGridEvent;
 import client.model.event.TileFromRackToRackEvent;
 import client.model.event.TileFromRackToRackWithShiftEvent;
 import client.model.event.TileListener;
 import client.model.event.UpdateAllStatsEvent;
 import client.model.event.UpdateScoreEvent;
 import client.model.event.UpdateStatsEvent;
 import client.model.event.UpdateWordsListEvent;
 import client.model.utils.GameException;
 import client.model.utils.Point;
 import client.service.GameService;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import javax.swing.event.EventListenerList;
 
 /**
  *
  * @author Romain <ro.foncier@gmail.com>, Bernard <bernard.debecker@gmail.com>
  */
 public class Play {
 
     private GameService service;
     private UUID id;
     private Player player;
     private Integer score;
     private Grid grid;
     private Rack rack;
     private Map<String, Tile> newWord = new HashMap<>();
     private boolean firstWord = true;
     private String storedRack;
     private Memento undo;
     private EventListenerList tileListeners;
     private EventListenerList rackListeners;
     private EventListenerList gridListeners;
     private EventListenerList menuListeners;
     private EventListenerList errorListeners;
     // Stats data
     private int TESTS_PLAYED = 0;
     private int TESTS_WON = 0;
     private int TESTS_LOST = 0;
     // Integrity error
     private final static int FIRST_WORD_NUMBER = 1;
     private final static int FIRST_WORD_POSITION = 2;
     private final static int FLOATING_TILES = 3;
     // JSON treatment
     private ObjectMapper om = new ObjectMapper();
 
     /**
      * Constructor for the launcher.
      */
     public Play(String[] args) {
         service = new GameService(args);
         tileListeners = new EventListenerList();
         rackListeners = new EventListenerList();
         gridListeners = new EventListenerList();
         menuListeners = new EventListenerList();
         errorListeners = new EventListenerList();
     }
 
     public void initPlay(String playID, String formatedGrid, String formatedRack, int score) {
         this.id = UUID.fromString(playID);
         this.score = score;
         grid = (formatedGrid.equals("")) ? new Grid() : new Grid(formatedGrid);
         rack = new Rack(formatedRack);
         storedRack = formatedRack;
     }
 
     public String getPlayID() {
         return id.toString();
     }
 
     public String getPlayer() {
         return player.toString();
     }
 
     public Integer getScore() {
         return score;
     }
 
     public void setScore(Integer score) {
         this.score = score;
     }
 
     /**
      * * Methods used for add or remove the different listeners **
      */
     public void addTileListener(TileListener listener) {
         tileListeners.add(TileListener.class, listener);
     }
 
     public void addRackListener(RackListener listener) {
         rackListeners.add(RackListener.class, listener);
     }
 
     public void addGridListener(GridListener listener) {
         gridListeners.add(GridListener.class, listener);
     }
 
     public void addMenuListener(MenuListener listener) {
         menuListeners.add(MenuListener.class, listener);
     }
 
     public void addErrorListener(ErrorListener listener) {
         errorListeners.add(ErrorListener.class, listener);
     }
 
     public void removeTileListener(TileListener listener) {
         tileListeners.remove(TileListener.class, listener);
     }
 
     public void removeRackListener(RackListener listener) {
         rackListeners.remove(RackListener.class, listener);
     }
 
     public void removeGridListener(GridListener listener) {
         gridListeners.remove(GridListener.class, listener);
     }
 
     public void removeMenuListener(MenuListener listener) {
         menuListeners.remove(MenuListener.class, listener);
     }
 
     public void removeErrorListener(ErrorListener listener) {
         errorListeners.remove(ErrorListener.class, listener);
     }
 
     /**
      * * Methods used to notify changes to all listeners **
      */
     public void fireTileMovedFromRackToGrid(int sourcePos, int x, int y, boolean isBlank) {
         TileListener[] listeners = (TileListener[]) tileListeners.getListeners(TileListener.class);
 
         for (TileListener l : listeners) {
             l.tileMovedFromRackToGrid(new TileFromRackToGridEvent(this, sourcePos, x, y, isBlank));
         }
     }
 
     public void fireTileMovedFromRackToRack(int sourcePos, int targetPos) {
         TileListener[] listeners = (TileListener[]) tileListeners.getListeners(TileListener.class);
 
         for (TileListener l : listeners) {
             l.tileMovedFromRackToRack(new TileFromRackToRackEvent(this, sourcePos, targetPos));
         }
     }
 
     public void fireTileMovedFromRackToRackWithShift(int sourcePos, int targetPos) {
         TileListener[] listeners = (TileListener[]) tileListeners.getListeners(TileListener.class);
 
         for (TileListener l : listeners) {
             l.tileMovedFromRackToRackWithShift(new TileFromRackToRackWithShiftEvent(this, sourcePos, targetPos));
         }
     }
 
     public void fireTileMovedFromGridToGrid(int sX, int sY, int tX, int tY) {
         TileListener[] listeners = (TileListener[]) tileListeners.getListeners(TileListener.class);
 
         for (TileListener l : listeners) {
             l.tileMovedFromGridToGrid(new TileFromGridToGridEvent(this, sX, sY, tX, tY));
         }
     }
 
     public void fireTileMovedFromGridToRack(int x, int y, int targetPos, boolean isBlank) {
         TileListener[] listeners = (TileListener[]) tileListeners.getListeners(TileListener.class);
 
         for (TileListener l : listeners) {
             l.tileMovedFromGridToRack(new TileFromGridToRackEvent(this, x, y, targetPos, isBlank));
         }
     }
 
     public void fireTileMovedFromGridToRackWithShift(int x, int y, int targetPos, boolean isBlank) {
         TileListener[] listeners = (TileListener[]) tileListeners.getListeners(TileListener.class);
 
         for (TileListener l : listeners) {
             l.tileMovedFromGridToRackWithShift(new TileFromGridToRackWithShiftEvent(this, x, y, targetPos, isBlank));
         }
     }
 
     public void fireRackReArrange(int[] positions) {
         RackListener[] listeners = (RackListener[]) rackListeners.getListeners(RackListener.class);
 
         for (RackListener l : listeners) {
             l.rackReArrange(new RackReArrangeEvent(this, positions));
         }
     }
 
     public void fireInitRackToPlay(String newRack) {
         RackListener[] listeners = (RackListener[]) rackListeners.getListeners(RackListener.class);
 
         for (RackListener l : listeners) {
             l.initRack(new InitRackEvent(this, newRack, false));
         }
     }
     
     public void fireUpdateRackToPlay(String newRack, boolean reset) {
         RackListener[] listeners = (RackListener[]) rackListeners.getListeners(RackListener.class);
 
         for (RackListener l : listeners) {
             l.updateRack(new InitRackEvent(this, newRack, reset));
         }
     }
 
     public void fireRemoveBadTilesToGrid(String tilesToRemove) {
         GridListener[] listeners = (GridListener[]) gridListeners.getListeners(GridListener.class);
 
         for (GridListener l : listeners) {
             l.removeBadTiles(new RemoveBadTilesEvent(this, tilesToRemove));
         }
     }
 
     public void fireInitMenuInterface(boolean anonymous, String email, String username) {
         MenuListener[] listeners = (MenuListener[]) menuListeners.getListeners(MenuListener.class);
 
         for (MenuListener l : listeners) {
             l.initMenuInterface(new InitMenuInterfaceEvent(this, anonymous, email, username));
         }
     }
     
     public void fireInitMenuLoadPlay(boolean anonymous) {
         MenuListener[] listeners = (MenuListener[]) menuListeners.getListeners(MenuListener.class);
 
         for (MenuListener l : listeners) {
             l.initMenuLoadPlay(anonymous);
         }
     }
 
     public void fireUpdateScore(int score) {
         MenuListener[] listeners = (MenuListener[]) menuListeners.getListeners(MenuListener.class);
 
         for (MenuListener l : listeners) {
             l.updateScore(new UpdateScoreEvent(this, score));
         }
     }
 
     public void fireResetGrid() {
         GridListener[] listeners = (GridListener[]) gridListeners.getListeners(GridListener.class);
 
         for (GridListener l : listeners) {
             l.resetGrid();
         }
     }
 
     public void fireResetRack() {
         RackListener[] listeners = (RackListener[]) rackListeners.getListeners(RackListener.class);
 
         for (RackListener l : listeners) {
             l.resetRack();
         }
     }
 
     public void fireErrorMessage(String error) {
         ErrorListener[] listeners = (ErrorListener[]) errorListeners.getListeners(ErrorListener.class);
 
         for (ErrorListener l : listeners) {
             l.displayError(new ErrorMessageEvent(this, error));
         }
     }
 
     public void fireMenuUpdateStats(boolean validate) {
         MenuListener[] listeners = (MenuListener[]) menuListeners.getListeners(MenuListener.class);
 
         for (MenuListener l : listeners) {
             l.updateStats(new UpdateStatsEvent(this, validate));
         }
     }
     
     public void fireMenuUpdateAllStats(int tp, int tw, int tl) {
         MenuListener[] listeners = (MenuListener[]) menuListeners.getListeners(MenuListener.class);
 
         for (MenuListener l : listeners) {
             l.updateAllStats(new UpdateAllStatsEvent(this, tp, tw, tl));
         }
     }
 
     public void fireMenuUpdateWordsList(String[] data) {
         MenuListener[] listeners = (MenuListener[]) menuListeners.getListeners(MenuListener.class);
 
         for (MenuListener l : listeners) {
             l.updateWordsList(new UpdateWordsListEvent(this, data));
         }
     }
 
     public void fireMenuShowUndoButton() {
         MenuListener[] listeners = (MenuListener[]) menuListeners.getListeners(MenuListener.class);
 
         for (MenuListener l : listeners) {
             l.showUndoButton();
         }
     }
 
     /**
      * Methods used for initGame (as guest or logged)
      */
     /**
      * Init a new play for an anonymous player. format in JSON : {"play_id":
      * "x0x000x0000x0000x00x0", "rack":
      * [{"letter":"A","value":2},{"letter":"A","value":2}, ...], "grid": ..., }
      */
     public void playAsGuest() {
         String response = null;
         player = new Player();
         try {
             response = service.createNewPlay(player.getPlayerID(), true);
         } catch (GameException ge) {
             // catch exception header and fire message to view
         }
 
         try {
             JsonNode root = om.readTree(response);
             initPlay(root.get("play_id").asText(), "", root.get("rack").toString(), 0);
 
             // Dispatch the model modifications to all listeners
             fireInitRackToPlay(root.get("rack").toString());
             fireInitMenuInterface(true, player.getPlayerEmail(), player.getPlayerUsername());
             fireInitMenuLoadPlay(true);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
 
     public void newGame() {
         String response = null;
         try {
             response = service.createNewPlay(player.getPlayerID(), false);
         } catch (GameException ge) {
             // catch exception header and fire message to view
         }
 
         try {
             JsonNode root = om.readTree(response);
             initPlay(root.get("play_id").asText(), "", root.get("rack").toString(), 0);
 
             // Dispatch the model modifications to all listeners
             fireInitRackToPlay(root.get("rack").toString());
             fireInitMenuLoadPlay(false);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
 
     /**
      * Methods used to signup, login and logout a player
      */
     public void signup(String email, String pwd) {
         String response = null;
         try {
             response = service.newPlayer(email, pwd);
         } catch (GameException ge) {
             // catch exception header and fire message to view
         }
 
         try {
             player = om.readValue(response, Player.class);
 
             // Dispatch the model notifications to Menu listener
             fireInitMenuInterface(false, player.getPlayerEmail(), player.getPlayerUsername());
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
 
     public void login(String email, String pwd) {
         String response = null;
         try {
             response = service.loginPlayer(email, pwd);
         } catch (GameException ge) {
             // catch exception header and fire message to view
         }
 
         try {
             player = om.readValue(response, Player.class);
 
             // Dispatch the model notifications to Menu listener
             fireInitMenuInterface(false, player.getPlayerEmail(), player.getPlayerUsername());
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
 
     public void logout() {
         boolean response = false;
         try {
             response = service.logoutPlayer(player.getPlayerID());
         } catch (GameException ge) {
             // Catch exception
         }
 
         if (response) {
             fireResetGrid();
             fireResetRack();
         } else {
             fireErrorMessage("<HTML>The first word should contain at least<BR> one letter on the center of the grid!</HTML>");
         }
     }
 
     /**
      * * Methods used for the movement of tiles **
      */
     private void shiftTilesOnRack(int startPos, int stopPos) {
         rack.shiftTiles(stopPos, stopPos);
     }
 
     private void deplaceTileOnRack(int sourcePos, int targetPos) {
         rack.addTile(targetPos, rack.removeTile(sourcePos));
     }
 
     private void deplaceTileFromRackToGrid(int sourcePos, int x, int y) {
         grid.addTile(x, y, rack.removeTile(sourcePos));
     }
 
     private void deplaceTileFromGridToGrid(int sX, int sY, int tX, int tY) {
         grid.addTile(tX, tY, grid.removeTile(sX, sY));
     }
 
     private void deplaceTileFromGridToRack(int x, int y, int targetPos) {
         rack.addTile(targetPos, grid.removeTile(x, y));
     }
 
     private void deplaceTileFromGridToRackWithShift(int x, int y, int targetPos) {
         rack.shiftTiles(rack.findEmptyParent(targetPos), targetPos);
         rack.addTile(targetPos, grid.removeTile(x, y));
     }
 
     /**
      * * Methods used for the creation of words **
      */
     public void createWord(int sourcePos, int x, int y) {
         newWord.put(x + "#" + y, rack.getTile(sourcePos));
         deplaceTileFromRackToGrid(sourcePos, x, y);
         fireTileMovedFromRackToGrid(sourcePos, x, y, grid.getTile(x, y).isBlank());
         //printDebug();
     }
 
     public void modifiedWord(int sX, int sY, int tX, int tY) {
         newWord.put(tX + "#" + tY, newWord.get(sX + "#" + sY));
         newWord.remove(sX + "#" + sY);
         deplaceTileFromGridToGrid(sX, sY, tX, tY);
         fireTileMovedFromGridToGrid(sX, sY, tX, tY);
         //printDebug();
     }
 
     public void removeLetterFromWord(int x, int y, int targetPos) {
         newWord.remove(x + "#" + y);
         if (rack.getTile(targetPos) != null) {
             deplaceTileFromGridToRackWithShift(x, y, targetPos);
             fireTileMovedFromGridToRackWithShift(x, y, targetPos, rack.getTile(targetPos).isBlank());
         } else {
             deplaceTileFromGridToRack(x, y, targetPos);
             fireTileMovedFromGridToRack(x, y, targetPos, rack.getTile(targetPos).isBlank());
         }
         //printDebug();
     }
 
     public void organizeRack(int sourcePos, int targetPos) {
         if (rack.getTile(targetPos) != null) {
             shiftTilesOnRack(sourcePos, targetPos);
             fireTileMovedFromRackToRackWithShift(sourcePos, targetPos);
         } else {
             deplaceTileOnRack(sourcePos, targetPos);
             fireTileMovedFromRackToRack(sourcePos, targetPos);
         }
     }
 
     // @condition : the rack should be full
     public void reArrangeRack() {
         fireRackReArrange(rack.reArrangeTiles());
     }
    
    public void exchangeTiles() {
        fireExchangeTiles();
    }
 
     public void validateWord() {
         undo = new Memento(score, storedRack, new HashMap<>(newWord), TESTS_PLAYED, TESTS_WON, TESTS_LOST);
         TESTS_PLAYED++;
         Boolean done = (this.firstWord && newWord.size() < 1) ? false : true, check = false;
         if (done) {
             int x = -1, y = -1, orientation = -1, inspector = this.newWord.size();
             Set set = this.newWord.entrySet();
             Iterator i = set.iterator();
             String dataToSend = "[";
             while (done && i.hasNext()) {
                 Map.Entry t = (Map.Entry) i.next();
                 String[] coord = ((String) t.getKey()).split("#");
                 Point p = new Point(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
 
                 // Verification:
                 if (inspector == this.newWord.size()) {
                     x = p.x;
                     y = p.y;
                     done = grid.hasNeighbors(p.x, p.y);
                 } else {
                     int checkO = (x == p.x) ? 1 : (y == p.y) ? 2 : 0;
                     if (inspector == this.newWord.size() - 1) {
                         orientation = checkO;
                     } else {
                         orientation = (orientation == checkO) ? orientation : 0;
                     }
                     done = (grid.hasNeighbors(p.x, p.y) && orientation != 0);
                 }
                 if (this.firstWord && !check) {
                     check = (p.x == 7 && p.y == 7);
                 }
                 dataToSend += "{" + formatData(p, (Tile) t.getValue()) + "}" + ((inspector != 1) ? ", " : "]");
                 inspector--;
             }
 
             if (done && ((!this.firstWord) || (this.firstWord && check))) {
                 String response = null;
                 try {
                     System.out.println("dataToSend : " + dataToSend);
                     response = service.passWord(player.getPlayerID(), this.getPlayID(), orientation, dataToSend);
                     JsonNode root = om.readTree(response);
                     if (root.get("valid").asBoolean()) {
                         // Update model
                         TESTS_WON++;
                         setScore(root.get("score").asInt());
                         rack.reLoadRack(root.get("tiles").toString());
                         this.firstWord = false;
                         newWord = new HashMap<>();
                         System.out.println("Rack JSON : "+rack.getFormatJSON());
                         storedRack = rack.getFormatJSON();
 
                         // Dispatch the model modifications to all listeners
                         fireUpdateScore(root.get("score").asInt());
                         fireUpdateRackToPlay(root.get("tiles").toString(), false);
                         fireMenuUpdateStats(root.get("valid").asBoolean());
                         fireMenuUpdateWordsList(om.readValue(root.get("words").toString(), String[].class));
                         fireMenuShowUndoButton();
                     } else {
                         // Update model
                         TESTS_LOST++;
                         Set rSet = this.newWord.entrySet();
                         setScore(root.get("score").asInt());
                         String newTiles = "[";
                         String tileToRemove = "[";
                         Iterator it = rSet.iterator();
                         while (it.hasNext()) {
                             Map.Entry t = (Map.Entry) it.next();
                             String[] coord = ((String) t.getKey()).split("#");
                             Point p = new Point(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
                             Tile tile = (Tile) t.getValue();
                             removeBadTiles(p, tile);
                             newTiles += (it.hasNext()) ? tile + ", " : tile + "]";
                             tileToRemove += (it.hasNext()) ? p + ", " : p + "]";
                             it.remove();
                         }
                         // Dispatch the model modifications to all listeners
                         fireUpdateScore(root.get("score").asInt());
                         fireUpdateRackToPlay(newTiles, false);
                         fireRemoveBadTilesToGrid(tileToRemove);
                         fireMenuUpdateStats(root.get("valid").asBoolean());
                         fireMenuShowUndoButton();
                     }
                 } catch (GameException | IOException ge) {
                     // Fire errors
                     ge.printStackTrace();
                 }
             } else {
                 if (!check) {
                     fireErrorMessage("<HTML>The first word should contain at least<BR> one letter on the center of the grid!</HTML>");
                 } else {
                     fireErrorMessage("<HTML>The floating letters must be on the same<BR> axis and form a word by touching<BR> a tile placed on the grid.</HTML>");
                 }
             }
         } else {
             fireErrorMessage("<HTML>The first word should contain at least<BR> two letters!</HTML>");
         }
         
         // Display states
         System.out.println();
         System.out.println("Play state");
         System.out.println("Score : "+score+" - "+storedRack+" - "+TESTS_PLAYED+" - "+TESTS_WON+" - "+TESTS_LOST);
         System.out.println("Memento state");
         System.out.println("Score : "+undo.getSavedScore()+" - "+undo.getSavedRack()+" - "+undo.getSavedWord()+" - "+undo.getSavedTP()+" - "+undo.getSavedTW()+" - "+undo.getSavedTL());
     }
 
     private String formatData(Point p, Tile tile) {
         String result = "";
         try {
             result += "\"coordinates\": " + om.writeValueAsString(p) + ", \"attributes\": " + tile;
         } catch (JsonProcessingException e) {
         }
         return result;
     }
 
     private String resetPlay(Map<String, Tile> word, String newRack, int score, int tp, int tw, int tl) {
         String tileToRemove = null;
         if (TESTS_WON > tw) {
             // Update model rack
             rack = new Rack(newRack);
 
             // Update model grid
             Set set = word.entrySet();
             tileToRemove = "[";
             Iterator it = set.iterator();
             while (it.hasNext()) {
                 Map.Entry t = (Map.Entry) it.next();
                 String[] coord = ((String) t.getKey()).split("#");
                 Point p = new Point(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
                 grid.removeTile(p.x, p.y);
                 tileToRemove += (it.hasNext()) ? p + ", " : p + "]";
             }
         }
         // Update model score and stats
         setScore(score);
         TESTS_PLAYED = tp;
         TESTS_WON = tw;
         TESTS_LOST = tl;
         
         return tileToRemove;
     }
 
     private void displayNewWord() {
         System.out.println("New word content : ");
         for (String key : newWord.keySet()) {
             System.out.println(key.toString());
         }
     }
 
     public void setTileBlank(int x, int y, char letter) {
         grid.getTile(x, y).setLetter(letter);
     }
 
     public void backTileBlank(int source) {
         rack.getTile(source).setLetter('?');
     }
 
     private void removeBadTiles(Point p, Tile tile) {
         // Add in rack and remove from Grid
         rack.putTile(tile);
         grid.removeTile(p.x, p.y);
     }
 
     /**
      * * Use these methods later **
      */
     /**
      * Update the rack from the new tiles send by the server.
      *
      * @param positions array of index
      * @param tiles string representation of tiles list
      */
     private void addNewTilesInRack(int[] positions, String tiles) {
         rack.setFormatedTiles(positions, tiles);
     }
 
     private void wordAddWithSuccess(String newRack, int score) {
     }
 
     private void wordAddWithError(int score) {
     }
 
     public boolean isTileBlank(Integer pos) {
         return rack.isTileBlank(pos);
     }
 
     /*public String checkBlankTile() {
      return rack.getBlankTile();
      }*/
     private void printDebug() {
         //grid.printGrid();
         displayNewWord();
     }
 
     // These methods and class are used to offer the Undo feature.
     public void undo() {
         // Reset play
         String resetGrid = resetPlay(undo.getSavedWord(), undo.getSavedRack(), undo.getSavedScore(), undo.getSavedTP(), undo.getSavedTW(), undo.getSavedTL());
         // Fire event to views
         fireUpdateScore(undo.getSavedScore());
         fireMenuUpdateAllStats(undo.getSavedTP(), undo.getSavedTW(), undo.getSavedTL());
         if (resetGrid != null) {
             fireUpdateRackToPlay(undo.getSavedRack(), true);
             fireRemoveBadTilesToGrid(resetGrid);
             fireMenuUpdateWordsList(null);
         }
 
         // Request the server
         /*try {
             service.undo(player.getPlayerID(), this.getPlayID());
         } catch (GameException ge) {
             System.out.println("Error during undo");
         }*/
     }
 
    private void fireExchangeTiles() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

     /**
      * Inner class Memento which allows to offer the undo feature.
      */
     public static class Memento {
 
         private final int score;
         private final String rack;
         private final Map<String, Tile> word;
         private final int t_played;
         private final int t_won;
         private final int t_lost;
 
         public Memento(int score, String rack, Map<String, Tile> newWord, int tp, int tw, int tl) {
             this.score = score;
             this.rack = rack;
             this.word = newWord;
             this.t_played = tp;
             this.t_won = tw;
             this.t_lost = tl;
         }
 
         public int getSavedScore() {
             return this.score;
         }
 
         public String getSavedRack() {
             return this.rack;
         }
 
         public Map<String, Tile> getSavedWord() {
             return this.word;
         }
 
         public int getSavedTP() {
             return this.t_played;
         }
 
         public int getSavedTW() {
             return this.t_won;
         }
 
         public int getSavedTL() {
             return this.t_lost;
         }
     }
 }
