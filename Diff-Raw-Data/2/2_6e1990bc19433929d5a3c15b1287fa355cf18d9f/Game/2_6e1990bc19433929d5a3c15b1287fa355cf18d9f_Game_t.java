 package CoreProgramPacket;
 
 import java.util. * ;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import DataPacket.MonsterCell;
 import DataPacket.PlayerCell;
 import DataPacket.Position;
 import DataPacket.Room;
 import DataPacket.State;
 import UI_1DPacket.Command;
 import UI_2DPacket.Tile;
 
 
 /**
  * This class is the main class of the "World of Zuul" application.
  * "World of Zuul" is a very simple, text based adventure game. Users can walk
  * around some scenery. That's all. It should really be extended to make it more
  * interesting!
  *
  * This main class creates and initialises all the others: it creates all rooms,
  * creates the parser and starts the game. It also evaluates and executes the
  * commands that the parser returns.
  *
  * @author Bruno Colantonio, Nishant Bhasin, Mohamed Ahmed, Yongquinchuan Du
  * @version Oct 23rd, 2012
  */
 
 public class Game extends Observable {
 
     private Room currentRoom;
     //private List < Item > inventory;
     public Room outside, theatre, pub, lab, office, cafe, basement, previousRoom, initialRoom;
     public Exit oEast, oWest, oSouth, oUp, oDown, tEast, pWest, lNorth, lWest, ofEast, cDown, bUp;
     public ItemCell basementItem;
 
     // Monster Variables
     private HashMap < Room, MonsterCell > monsters; // Will be used later
     public HashMap < MonsterCell, Room > monster_map;
     public MonsterCell monTowards, monTowards2, monTowards3;
     
     protected Cell[][] playingField;
 
     protected ArrayList < Avatar > movableTile;
     
     protected int undoIndex = 0;
     protected int undoCount = 0;
     protected static ImageIcon monsterimage = new ImageIcon("img/mon-tile.png");
     protected static ImageIcon monstertowards = new ImageIcon("img/mon-towards.png");
 
     private Stack < State > stateStack;
     private Stack < State > redoStateStack;
     private State currentState;
 
     /**
      * Create the game and initialise its internal map.
      */
     public Game() {
         createRooms();
         //inventory = new ArrayList < Item > ();
         playingField = new Cell[currentRoom.getHeight()][currentRoom.getWidth()];
         movableTile = new ArrayList < Avatar > ();
         populateItemMap();
         syncItemMapAndField(movableTile);
         
         currentState = new State();
         
         stateStack = new Stack < State > ();
         redoStateStack = new Stack < State >();
         
     }
 
     /**
      * Create all the rooms and link their exits together.
      */
     private void createRooms() {
 
         // create the rooms
         outside = new Room("outside the main entrance of the university");
         theatre = new Room("in a lecture theatre");
         pub = new Room("in the campus pub");
         lab = new Room("in a computing lab");
         office = new Room("in the computing admin office");
         cafe = new Room("in the cafe");
         basement = new Room("in the basement");
 
         monTowards = new MonsterCell(new Position(1, 1), this, "monster1");
         monTowards2 = new MonsterCell(new Position(9, 9), this, "monster2");
 
         oEast = new Exit(new Position(5, 9), this, "east", pub);
         oWest = new Exit(new Position(5, 1), this, "west", theatre);
         oSouth = new Exit(new Position(9, 5), this, "south", lab);
         oUp = new Exit(new Position(3, 3), this, "up", cafe);
         oDown = new Exit(new Position(7, 7), this, "down", basement);
         tEast = new Exit(new Position(5, 9), this, "east", outside);
         pWest = new Exit(new Position(5, 1), this, "west", outside);
         lNorth = new Exit(new Position(1, 5), this, "north", outside);
         lWest = new Exit(new Position(5, 1), this, "west", office);
         ofEast = new Exit(new Position(5, 9), this, "east", lab);
         cDown = new Exit(new Position(7, 7), this, "down", outside);
         bUp = new Exit(new Position(3, 3), this, "up", outside);
 
         basementItem = new ItemCell(new Position(3, 3), this, "apple");
 
         ItemCell chick = new ItemCell(new Position(1,5), this, "chicken");
         ItemCell burger = new ItemCell(new Position (1,7), this, "burger");
         ItemCell burg = new ItemCell(new Position (1,7), this, "burger");
         cafe.addItem(chick);
         cafe.addItem(burger);
         basement.addItem(burg);
 
         // Initialise room exits
         outside.setExit(oEast);
         outside.setExit(oWest);
         outside.setExit(oSouth);
         outside.setExit(oUp);
         outside.setExit(oDown);
         theatre.setExit(tEast);
         pub.setExit(pWest);
         lab.setExit(lNorth);
         lab.setExit(lWest);
         office.setExit(ofEast);
         cafe.setExit(cDown);
         basement.setExit(bUp);
         // basement.addItem(basementItem);
         // adding monster
         cafe.addMonster(monTowards);
         // lab.addMonster(monTowards3);
         theatre.addMonster(monTowards2);
         monsters = new HashMap < Room, MonsterCell > ();
         monsters.put(cafe, monTowards);
         // monsters.put(lab, monTowards3);
         monsters.put(theatre, monTowards);
         initialRoom = outside;
         previousRoom = currentRoom = outside;
 //Adding items to cafe
         
 
     }
 
     /**
      * Main play routine. Loops until end of play.
      */
     public void play(Position pos) {
         int exitCol, exitRow, heroCol, heroRow;
         Position roomChangePos = null;
         JOptionPane Jpane;
         Jpane = new JOptionPane();
         Avatar hero = movableTile.get(0);
         Position nextPos;
         nextPos = hero.getNextPosition(pos);
 
         // push the whole playingFile to cellsStack
         //===========================================
         //this is using to temporarily avoid bug
         //currentState = new State(playingField, (ArrayList<Item>) inventory);
       
         //currentState = new State(new Cell[currentRoom.getHeight()][currentRoom.getWidth()], new ArrayList < Item > ());
         currentState = new State();
         stateStack.push(currentState);
         
         for(Exit e: currentRoom.getExit()) {
             if(e.getPosition().equals(nextPos)) {
 
                removeExits();
             	removeItems();
                 exitRow = e.getPosition().getRow();
                 exitCol = e.getPosition().getCol();
                 heroRow = currentRoom.getHeight() - 1 - exitRow;
                 heroCol = currentRoom.getWidth() - 1 - exitCol;
 
                 roomChangePos = new Position(heroRow, heroCol);
 
                 this.previousRoom = currentRoom;
                 currentRoom = e.getNextRoom();
                 System.out.println(currentRoom);
             }
         }
         if(roomChangePos != null) {
             nextPos = roomChangePos;
         }
         hero.setPosition(nextPos);
         // something wrong with following logic but leave it for temperatory
         if(currentRoom.getMonster().size() != 0) {
             Avatar mon = movableTile.get(1);
             Position monCurrent = mon.getPosition();
             Position monNextPosition = mon.getNextPosition(hero.getPosition());
             for(Exit e: currentRoom.getExit()) {
                 if(e.getPosition().equals(monNextPosition)) {
                     monNextPosition = monCurrent;
                     mon.setPosition(monNextPosition);
                     removeMonster();
                 } else {
                     mon.setPosition(monNextPosition);
                 }
             }
 
             if(mon.collidesWith(movableTile.get(0))) {
                 hero.setPosition(new Position(1, 1));
                 currentRoom = initialRoom;
                 System.out.println(currentRoom);
                 Jpane.showMessageDialog(null, "You died. You have" + hero.getLives() + " lives left ");
                 System.out.println("Game Over");
 
             } else if(movableTile.get(0).getLives() == 0) {
                 Jpane.showMessageDialog(null, "Game Over. You have 0 lives LEFT !!");
                 System.out.println("Game Over");
             }
         } else {
             removeMonster();
         }
 
         syncItemMapAndField(movableTile);
         if(checkWin()) {
             setChanged();
             notifyObservers("Congratulations: You win!");
         }
 
     }
 
     /**
      * "Pick was entered. Process to see what we can pick up"
      */
     private void pick(ItemCell item) {
        /* if(!command.hasSecondWord()) { // if user forgot to enter what to pick
             System.out.println("Pick what?");
             return;
         } else {
             String item = command.getSecondWord();
             if(currentRoom.getItem() == null) { // if there is no such item
                 // in the current room
                 System.out.println("There is no such item in this room.");
 
             } else {
                 // inventory.add(currentRoom.getItem(item)); //add item to
                 // inventory
                 System.out.println("You added " + item + " to your inventory");
                 currentRoom.removeItem(item); // remove item from room
             }
         }*/
     }
 
     /**
      * unPick is called when you picked up an item and you undo, removing the
      * item from inventory and putting it back in the room
      */
 /*    private void unPick(Room itemRoom) {
         int invSize = inventory.size();
         if(invSize >= 1) { // if inventory if greater than 1 (which it should
             // always be)
             System.out.println("You unpicked " + inventory.get(invSize - 1).getDescription() + " and returned " + itemRoom);
             currentRoom.addItem(inventory.get(invSize - 1)); // return the item
             // to the room
             inventory.remove(invSize - 1); // remove item from inventory
         }
     }*/
 
     public ItemCell getItem(Position position) throws IndexOutOfBoundsException {
         if(position.getRow() < 0 || position.getRow() >= currentRoom.getHeight()) throw new IndexOutOfBoundsException("Row out of bounds." + position.getRow());
         else if(position.getCol() < 0 || position.getCol() >= currentRoom.getWidth()) throw new IndexOutOfBoundsException("Col out of bounds" + position.getCol());
         else {
             if(playingField[position.getRow()][position.getCol()] instanceof ItemCell) return(ItemCell) playingField[position.getRow()][position.getCol()];
         }
         return null;
     }
 
     /**
      * Returns the tile at said position or throws an exception if the position
      * is invalid.
      *
      * @param position
      *            The position of interest
      * @return Tile at position, null if no item
      */
     public Cell getCell(Position position) throws IndexOutOfBoundsException {
         if(position.getRow() < 0 || position.getRow() >= currentRoom.getHeight()) throw new IndexOutOfBoundsException("Row out of bounds.");
         else if(position.getCol() < 0 || position.getCol() >= currentRoom.getWidth()) throw new IndexOutOfBoundsException("Col out of bounds");
         else return playingField[position.getRow()][position.getCol()];
     }
 
     public void placeItem(ItemCell item) {
         playingField[item.getPosition().getRow()][item.getPosition().getCol()] = item;
     }
 
     public String toString() {
         String s = "";
 
         for(int i = 0; i < currentRoom.getWidth(); i++) {
             for(int j = 0; j < currentRoom.getHeight(); j++) {
                 s += " " + playingField[i][j].toString();
             }
             s += "\n";
         }
         return s;
     }
 
     protected void populateItemMap() {
 
         for(int row = 0; row < currentRoom.getHeight(); row++) {
             for(int col = 0; col < currentRoom.getWidth(); col++) {
                 if(row == 0 || col == 0 || row == currentRoom.getHeight() - 1 || col == currentRoom.getWidth() - 1) {
                     playingField[row][col] = new WallCell(new Position(row, col), this);
                 } else {
                     playingField[row][col] = new Cell(new Position(row, col), this);
                 }
             }
         }
 
         movableTile.add(new PlayerCell(new Position(5, 4), this, 5));
         for(MonsterCell m: monsters.values()) {
             movableTile.add(m);
         }
         
         if(!currentRoom.getItem().isEmpty()) 
 		{
 			for(ItemCell i : currentRoom.getItem())
 			{
 				playingField[i.getPosition().getRow()][i.getPosition().getCol()] = i;//currentRoom.getItem().get(0);
 			System.out.println(currentRoom.getItem().get(0).getPosition().toString());
 			}
 		}
 
     }
 
     /**
      * Tells if the room has changed or not.
      *
      * @return
      */
     public boolean currentRoomChanged() {
         Room r = previousRoom;
         if(this.currentRoom != r) {
             return true;
         } else {
             return false;
         }
 
     }
 
     public void syncItemMapAndField(ArrayList < Avatar > movableTile) {
     	
     	if(currentRoom.getItem()!=null){
 			for(ItemCell i : currentRoom.getItem()){
 				System.out.println(i.toString());
 				playingField[i.getPosition().getRow()][i.getPosition().getCol()] = i;
 			}
 		}
         
     	//playingField = new Cell[currentRoom.getWidth()][currentRoom.getHeight()];
         this.removeAvatar();
         for(Exit e: currentRoom.getExit()) {
             playingField[e.getPosition().getRow()][e.getPosition().getCol()] = e;
         }
 
 
         for(Avatar m: movableTile) { // ---------FIX
             // THIS!!!--------------------------------------------------------------------------------------
             // only place if alive
             if(currentRoom.getMonster().size() != 0 && m instanceof MonsterCell && (m == currentRoom.getMonster().get(0))) {
                 playingField[m.getPosition().getRow()][m.getPosition().getCol()] = m;
             }
             if(m.getLives() != 0 && m instanceof PlayerCell) {
                 playingField[m.getPosition().getRow()][m.getPosition().getCol()] = m;
 
             }
         }
 
         setChanged();
         notifyObservers("update");
     }
 
     public void removeExits() {
         for(int row = 0; row < currentRoom.getHeight(); row++) {
             for(int col = 0; col < currentRoom.getWidth(); col++) {
                 if(playingField[row][col] instanceof Exit) {
                     playingField[row][col] = new Cell(new Position(row, col), this);
                 }
             }
         }
     }
     
     public void removeItems(){
 		for(int row = 0; row < currentRoom.getHeight(); row++){
 			for(int col = 0; col < currentRoom.getWidth(); col++){
 				if(playingField[row][col] instanceof ItemCell){
 					playingField[row][col] = new Cell(new Position(row,col), this);
 				}
 			}
 		}
 	}
 
     public void removeMonster() {
         for(int row = 0; row < currentRoom.getHeight(); row++) {
             for(int col = 0; col < currentRoom.getWidth(); col++) {
                 if(playingField[row][col] instanceof MonsterCell) {
                     playingField[row][col] = new Cell(new Position(row, col), this);
                 }
             }
         }
     }
 
     public void removeAvatar() {
         for(int row = 0; row < currentRoom.getHeight(); row++) {
             for(int col = 0; col < currentRoom.getWidth(); col++) {
                 if(playingField[row][col] instanceof MonsterCell || playingField[row][col] instanceof PlayerCell) {
                     playingField[row][col] = new Cell(new Position(row, col), this);
                 }
             }
         }
     }
     
 
     public Room getCurrentRoom() {
         return currentRoom;
     }
 
     /**
      * returns the Hero of the current game
      *
      * @return Hero
      */
     public PlayerCell getUser() {
         return(PlayerCell) movableTile.get(0);
     }
 
     public void resetPlayingField() {}
 
     protected boolean checkWin() {
         return false;
     }
 
     public void restartGame() {}
 
     /*
      * undo method called by click button inventory life monster
      */
     public void undo() {
         /*redoStateStack.push(currentState);
         currentState = stateStack.pop();*/
         System.out.println("undo");
         // call update method
         /*
          *
          *
          *
          */
 
     }
 
     /*
      * redo mehtod called by click redo button if there is a new
      */
     public void redo() {
    /*     stateStack.push(currentState);
         currentState = redoStateStack.pop();*/
 
         System.out.println("redo");
         // call update method
         /*
          *
          *
          *
          */
 
     }
 }
