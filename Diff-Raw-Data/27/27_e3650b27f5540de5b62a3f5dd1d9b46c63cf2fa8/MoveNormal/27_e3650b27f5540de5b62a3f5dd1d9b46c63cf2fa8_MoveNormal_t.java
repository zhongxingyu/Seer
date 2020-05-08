 package de.htwg.se.dog.controller.impl;
 
 import de.htwg.se.dog.models.FieldInterface;
 import de.htwg.se.dog.models.FigureInterface;
 import de.htwg.se.dog.models.GameFieldInterface;
 import de.htwg.se.dog.models.PlayerInterface;
 
 /**
  * implementation of Movement for normal cards
  * 
  * @author Michael,Christian
  * 
  */
 public class MoveNormal extends Movement {
    private final GameFieldInterface gameField;
 
     /**
      * konstruktor that sets the gamefield on which we move
      * 
      * @param gameField
      */
     public MoveNormal(GameFieldInterface gameField) {
        super(gameField);
         this.gameField = gameField;
     }
 
     /**
      * Trys to Moves Figure "steps" forward, on success it returns true
      * otherwise false
      * 
      * @param gamefield
      * @param steps
      *        number of steps figure wants to take
      * @param startfieldnr
      *        from where figure wants to move
      * @return true if figure could be moved, otherwise false
      */
     @Override
     public boolean move(int steps, int startfieldnr) {
         boolean valid = false;
         FieldInterface[] array = gameField.getGameArray();
         // check if startfield is not empty and the move is valid
         if (!fieldEmpty(array[startfieldnr]) && validMove(steps, startfieldnr)) {
             int targetfield = getTargetfield(steps, startfieldnr);
             FigureInterface temp = array[startfieldnr].removeFigure();
             kickPlayer(array, targetfield);
             // Move Figure from startfield to Targetfield
             array[targetfield].putFigure(temp);
             PlayerInterface ownerF1 = temp.getOwner();
             ownerF1.updateFigurePos(temp.getFignr(), targetfield);
             valid = true;
             if (array[targetfield].isHouse()) {
                 array[targetfield].setBlocked(true);
             }
         }
 
         return valid;
     }
 
     /**
      * Returns true if suggested move is valid
      * 
      * @param gamefield
      * @param steps
      *        number of steps figure want to take
      * @param startfieldnr
      *        from where figure wants to move
      * @return true if move is valid, otherwise false
      */
     @Override
     public boolean validMove(int steps, int startfieldnr) {
         boolean valid = false;
         if (getTargetfield(steps, startfieldnr) >= 0) {
             valid = true;
         }
         return valid;
     }
 }
