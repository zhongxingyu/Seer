 package de.htwg.se.dog.controller.impl;
 
 import de.htwg.se.dog.models.FieldInterface;
 import de.htwg.se.dog.models.GameFieldInterface;
 
 /**
  * Movment strategy implementation , MoveSwitch
  * 
  * @author Michael, Christian
  * 
  */
 
 public class MoveSeven extends Movement {
     private static final int VALUEOFCARD7 = 7;
     private static final int NORMALMOVE = 2;
 
     /**
      * konstruktor that sets the gamefield on which we move
      * 
      * @param gameField
      */
     public MoveSeven(GameFieldInterface gameField) {
         this.gameField = gameField;
     }
 
     /**
      * Trys to Moves Figure "steps" forward, on success it returns true
      * otherwise false and kicks every Player on the fields, the figure moved
      * over
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
 
         boolean retval = false;
         this.setMoveStrategie(VALUEOFCARD7);
         /* Check if all moves are possible */
         if (this.validMove(steps, startfieldnr)) {
             /* Move -------------- */
             int targetField = getTargetfield(steps, startfieldnr);
             super.setMoveStrategie(NORMALMOVE);
             retval = super.move(steps, startfieldnr);
             /* Clean Up field -----*/
             kickAllPlayerOnWay(startfieldnr, targetField);
         }
         return retval;
     }
 
     private void kickAllPlayerOnWay(int startfieldnr, int targetField) {
         int stepsTaken = targetField - startfieldnr;
         if (stepsTaken < 0) {
             stepsTaken += gameField.getFieldSize() + 1;
         }
         FieldInterface[] array = gameField.getGameArray();
         int currentKickField = startfieldnr;
         //kick players on every field figure move over, except housefields
         for (int i = 0; i < stepsTaken - 1; i++) {
             if (!array[currentKickField].isHouse()) {
                 kickPlayer(array, currentKickField);
             }
             currentKickField++;
             if (currentKickField >= gameField.getFieldSize()) {
                 currentKickField -= gameField.getFieldSize();
             }
         }
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
