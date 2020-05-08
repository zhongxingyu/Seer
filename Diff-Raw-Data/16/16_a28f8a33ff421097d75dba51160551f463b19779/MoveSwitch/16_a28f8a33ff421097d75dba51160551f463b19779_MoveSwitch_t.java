 package de.htwg.se.dog.controller.impl;
 
 import de.htwg.se.dog.models.FieldInterface;
 import de.htwg.se.dog.models.FigureInterface;
 import de.htwg.se.dog.models.GameFieldInterface;
 import de.htwg.se.dog.models.PlayerInterface;
 
 public class MoveSwitch extends Movement {
 

     public MoveSwitch(GameFieldInterface gameField) {
         this.gameField = gameField;
     }
 
     /**
      * switches 2 figures, if targetfig is on a housefield or not on both fields
      * is a figure, it does nothing
      * 
      * @param gamefield
      * @param steps
      *            number of steps to field of Figure 2
      * @param fromNr
      *            fieldnumber of Figure 1
      * @return true if switch was successful, otherwise false
      */
     @Override
     public boolean move(int steps, int fromNr) {
         int toNr = steps;
         boolean valid = false;
         FieldInterface[] array = gameField.getGameArray();
         if (figuresOnBothFieldsAndNotHousefields(fromNr, toNr, array)) {
             /* switch Figures */
             FigureInterface f1 = array[fromNr].removeFigure();
             FigureInterface f2 = array[toNr].removeFigure();
             array[fromNr].putFigure(f2);
             array[toNr].putFigure(f1);
             /* update FirgurePositions */
             PlayerInterface ownerF1 = f1.getOwner();
             PlayerInterface ownerF2 = f2.getOwner();
             ownerF2.updateFigurePos(f2.getFignr(), fromNr);
             ownerF1.updateFigurePos(f1.getFignr(), toNr);
             valid = true;
         }
         return valid;
     }
 
     protected boolean figuresOnBothFieldsAndNotHousefields(int fromNr, int toNr, FieldInterface[] array) {
         boolean figureOnAfield = !fieldEmpty(array[fromNr]);
         boolean figureOnBfield = !fieldEmpty(array[toNr]);
         boolean figuresOnBothFields = figureOnAfield && figureOnBfield;
         boolean anotHouseField = !array[fromNr].isHouse();
         boolean bnotHouseField = !array[toNr].isHouse();
         boolean bothNotHouseFields = anotHouseField && bnotHouseField;
         return figuresOnBothFields && bothNotHouseFields;
     }
 
     /**
      * checks if it is possible to switch 2 figures
      */
     @Override
     public boolean validMove(int steps, int fromNr) {
         boolean ok = false;
         int toNr = steps;
         FieldInterface[] array = gameField.getGameArray();
         if (figuresOnBothFieldsAndNotHousefields(fromNr, toNr, array)) {
             ok = true;
         }
         return ok;
     }
 }
