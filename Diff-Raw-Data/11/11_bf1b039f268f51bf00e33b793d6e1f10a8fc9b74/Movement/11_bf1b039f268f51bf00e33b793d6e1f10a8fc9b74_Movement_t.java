 package de.htwg.se.dog.controller.impl;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import de.htwg.se.dog.controller.MovementStrategy;
 import de.htwg.se.dog.models.FieldInterface;
 import de.htwg.se.dog.models.FigureInterface;
 import de.htwg.se.dog.models.GameFieldInterface;
 import de.htwg.se.dog.models.PlayerInterface;
 
 public class Movement implements MovementStrategy {
 
     private static final int VALUEOFCARD7 = 7;
     private static final int ELEVEN = 11;
     private static final int EMPTYFIELD = -5;
     private static final int BLOCKEDFIELD = -6;
    private static final int OVERHOUSE = -8;
 
     private Movement strategie;
     protected GameFieldInterface gameField;
 
     public Movement() {}
 
     public Movement(GameFieldInterface gameField) {
         this.gameField = gameField;
     }
 
     @Override
     public boolean move(int steps, int fromNr) {
         return strategie.move(steps, fromNr);
     }
 
     @Override
     public boolean validMove(int steps, int startfieldnr) {
         return strategie.validMove(steps, startfieldnr);
 
     }
 
     /**
      * Set move-strategie that is used for a card
      * 
      * @param card
      */
     public void setMoveStrategie(int strategieNr) {
         if (strategieNr != ELEVEN) {
             strategie = new MoveNormal(gameField);
         } else {
             strategie = new MoveSwitch(gameField);
         }
     }
 
     /**
      * checks if figure is on field
      * 
      * @param field
      *        which should be checked
      * @return true if field is empty, otherwise false;
      */
     public boolean fieldEmpty(FieldInterface field) {
         boolean returnval = false;
         if (field.getFigure() == null) {
             returnval = true;
         }
         return returnval;
     }
 
     /**
      * Remove Player From fieldID and return it to Player if field at fieldID is
      * empty, it does nothing
      * 
      * @param array
      *        gamefieldarray
      * @param fieldID
      *        fieldnumber where figure should be kick from
      */
     protected void kickPlayer(FieldInterface[] array, int fieldID) {
         if (!fieldEmpty(array[fieldID])) {
             // get Owner of figure
             PlayerInterface tempPlayer = array[fieldID].getFigure().getOwner();
             FigureInterface figure = array[fieldID].removeFigure();
             tempPlayer.updateFigurePos(figure.getFignr(), -1);
             // remove figure from field and add it to Playerlist
             tempPlayer.addFigure(figure);
         }
     }
 
     /**
      * returns the fieldID of the Target field, if not a vaild move, -1 is
      * returned.
      * 
      * @param gamefield
      * @param steps
      *        number of steps figure wants to take
      * @param startfieldnr
      *        startfield number from where figure wants to move
      * @return returns number of targetfield, if startfield is empty it returns
      *         -5 or if field is blocked it returns -6
      */
     private int getTargetfield(FieldInterface[] array, int steps,
             int startfieldnr) {
         int absSteps = Math.abs(steps);
         int currentfieldID = EMPTYFIELD;
        boolean isHousefield = false;
         // TODO Monitor whether if-Statment is necessary or not
         if (!fieldEmpty(array[startfieldnr])) {
            isHousefield = array[startfieldnr].isHouse();
             int playerNr = array[startfieldnr].getFigureOwnerNr();
             currentfieldID = nextField(startfieldnr, steps);
             int nextfieldID = currentfieldID;
             // Loop to move steps
             while (absSteps > 0) {
                 currentfieldID = nextfieldID;
                 nextfieldID = nextField(currentfieldID, steps);
                 int currentFieldOwner = array[currentfieldID].getOwner();
                 // Check if field is Blocked
                 if (array[currentfieldID].isBlocked()) {
                     currentfieldID = BLOCKEDFIELD;
                     absSteps = 0;
                 }
                 // Check if nobody owns next field
                 absSteps = adjustSteps(steps, absSteps, array, playerNr,
                         nextfieldID, currentFieldOwner);
             }
         }
        if (!array[currentfieldID].isHouse() && isHousefield) {
            currentfieldID = OVERHOUSE;
        }
         return currentfieldID;
     }
 
     /**
      * wrapper methode
      * 
      * @param steps
      * @param startfieldnr
      * @return
      */
     protected int getTargetfield(int steps, int startfieldnr) {
         return getTargetfield(gameField.getGameArray(), steps, startfieldnr);
     }
 
     private int adjustSteps(int steps, int psteps, FieldInterface[] array,
             int playerNr, int nextfieldID, int currentFieldOwner) {
         int absSteps = psteps;
         if (currentFieldOwner == 0 || currentFieldOwner == playerNr
                 && steps > 0) {
             absSteps--;
             // Check if next field is own House and current field ist last
             // in house
         }
         if (currentFieldOwner == playerNr && absSteps > 0
                 && array[nextfieldID].getOwner() != playerNr && steps > 0) {
             absSteps += gameField.getHouseCount();
         }
         return absSteps;
     }
 
     /**
      * returns the next field, if direction is >= 0 it returns the next fieldNr
      * otherwise the previous fieldNr
      * 
      * @param fieldSize
      *        Size of the Gamefield
      * @param currentfieldID
      * @param direction
      * @return
      */
     protected int nextField(int currentfieldID, int direction) {
         int fieldSize = gameField.getFieldSize();
         int field;
         if (direction >= 0) {
             field = (currentfieldID + 1) % fieldSize;
         } else {
             field = ((currentfieldID - 1) + fieldSize) % fieldSize;
         }
         return field;
     }
 
     public boolean moveStart(PlayerInterface player) {
         boolean retval = false;
         int startFieldNr = gameField.calculatePlayerStart(player.getPlayerID());
         FieldInterface[] array = gameField.getGameArray();
         if (possibleMoveStart(array, startFieldNr, player)) {
             kickPlayer(array, startFieldNr);
             array[startFieldNr].putFigure(player.removeFigure(), startFieldNr);
             array[startFieldNr].setBlocked(true);
             retval = true;
         }
         return retval;
     }
 
     public boolean possibleMoveStart(FieldInterface[] array, int startFieldNr,
             PlayerInterface player) {
         return !array[startFieldNr].isBlocked()
                 && !player.getFigureList().isEmpty();
     }
 
     /**
      * checks if it is possible to switch the figure on fieldnr with any other
      * enemy figure
      * 
      * @param fieldnr
      *        fieldnumber where figure to check stands
      * @return true if possible figure is found, otherwise false
      */
     public boolean anySwitchMove(int fieldnr) {
         boolean retval = false;
         int sourceFigOwner = gameField.getGameArray()[fieldnr].getFigureOwnerNr();
         for (FieldInterface field : gameField.getGameArray()) {
             if (field.getFigure() != null && !field.isBlocked() && field.getFigureOwnerNr() != sourceFigOwner) {
                 retval = true;
                 break;
             }
         }
         return retval;
     }
 
     /* ------------------------------------------------------------------------- */
     /* Move Seven Methodes */
     /* ------------------------------------------------------------------------- */
 
     /**
      * Move-Method which executes every move given in "moves" or non if one move
      * is not possible
      * 
      * @param gamefield
      * @param moves
      *        Map of moves you want to execute, while the key is the
      *        startfieldnr and the value is the number of steps from this
      *        startfieldnr
      * @return true if all moves could be executed, otherwise false
      */
     public boolean move(Map<Integer, Integer> moves) {
         boolean retval = true;
         this.setMoveStrategie(VALUEOFCARD7);
         /* Check if all moves are possible */
         for (Entry<Integer, Integer> field : moves.entrySet()) {
             if (!this.move(field.getValue(), field.getKey())) {
                 retval = false;
                 break;
             }
         }
         /* Move -------------- */
         for (Entry<Integer, Integer> field : moves.entrySet()) {
             for (int i = 0; i <= field.getValue(); i++) {
                 this.move(1, field.getKey());
             }
         }
         return retval;
     }
 
     /**
      * Checks if the Player p can do a move with the card 7
      * 
      * @param gamefield
      *        the current gamefield played on
      * @param p
      *        the player that wants to move
      * @return true if the player can move with the card
      */
     public boolean anyValidMove(PlayerInterface p) {
 
         FieldInterface[] array = gameField.copyField();
         setMoveStrategie(VALUEOFCARD7);
         LinkedList<Integer> figures = new LinkedList<Integer>(
                 p.getFigureRegister());
         Collections.sort(figures, Collections.reverseOrder());
 
         int steps = VALUEOFCARD7;
         int remaining = 0;
         boolean returnval = false;
 
         if (figures.isEmpty()) {
             steps = 0;
         }
 
         Integer currentField = figures.pollFirst();
 
         while (steps > 0) {
             if (getTargetfield(array, steps, currentField) < 0) {
                 steps--;
                 remaining++;
             } else {
                 int target = getTargetfield(array, steps, currentField);
                 FigureInterface fig = array[currentField].removeFigure();
                 array[target].setBlocked(true);
                 array[target].putFigure(fig);
 
                 if (remaining == 0) {
                     returnval = true;
                     break;
                 }
                 currentField = figures.pollFirst();
 
                 steps = remaining;
                 remaining = 0;
                 if (currentField == null) {
                     break;
                 }
             }
         }
         return returnval;
     }
 }
