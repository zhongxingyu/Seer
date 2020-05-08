 package com.udev.process;
 
 import com.udev.domain.Cell;
 import com.udev.domain.Field;
 import com.udev.domain.figures.Figure;
 import com.udev.domain.figures.RotationState;
 import com.udev.domain.figures.SFigure;
 
 import java.util.List;
 
 /**
  * User: oleg.krupenya
  * Date: 10/8/13
  * Time: 7:44 PM
  */
 public class SFigureRotationManager implements RotationManager {
     /**
      * Rotates the figure.
      *
      * @param figure The figure to rotate.
      * @param field  The Field.
      */
     public void rotate(Figure figure, Field field) {
         Cell[][] data = field.getCells();
         List<Cell> cells = figure.getCells();
         RotationState state = figure.getRotationState();
         if (state == RotationState.HORIZONTAL && isPossibleToRotateVertically(figure, field)) {
             Cell firstCell = cells.get(0);
             data[firstCell.getI()][firstCell.getJ()] = new Cell(firstCell.getI(), firstCell.getJ(), Field.ZERO);
             data[firstCell.getI() - 2][firstCell.getJ()] = new Cell(firstCell.getI() - 2, firstCell.getJ(), Field.ONE);
             firstCell.setI(firstCell.getI() - 2);
             firstCell.setJ(firstCell.getJ());
 
             Cell secondCell = cells.get(1);
             data[secondCell.getI()][secondCell.getJ()] = new Cell(secondCell.getI(), secondCell.getJ(),
                     Field.ZERO);
             data[secondCell.getI() - 1][secondCell.getJ() - 1] = new Cell(secondCell.getI() - 1, secondCell.getJ() - 1,
                     Field.ONE);
             secondCell.setI(secondCell.getI() - 1);
             secondCell.setJ(secondCell.getJ() - 1);
 
             Cell fourthCell = cells.get(3);
             data[fourthCell.getI()][fourthCell.getJ()] = new Cell(fourthCell.getI(), fourthCell.getJ(),
                     Field.ZERO);
             data[fourthCell.getI() + 1][fourthCell.getJ() - 1] = new Cell(fourthCell.getI() + 1, fourthCell.getJ() - 1,
                     Field.ONE);
             fourthCell.setI(fourthCell.getI() + 1);
             fourthCell.setJ(fourthCell.getJ() - 1);
             figure.setRotationState(RotationState.VERTICAL);
         } else if (state == RotationState.VERTICAL && isPossibleToRotateHorizontally(figure, field)) {
             Cell firstCell = cells.get(0);
             data[firstCell.getI()][firstCell.getJ()] = new Cell(firstCell.getI(), firstCell.getJ(), Field.ZERO);
             data[firstCell.getI() + 2][firstCell.getJ()] = new Cell(firstCell.getI() + 2, firstCell.getJ(), Field.ONE);
             firstCell.setI(firstCell.getI() + 2);
             firstCell.setJ(firstCell.getJ());
 
             Cell secondCell = cells.get(1);
             data[secondCell.getI()][secondCell.getJ()] = new Cell(secondCell.getI(), secondCell.getJ(),
                     Field.ZERO);
             data[secondCell.getI() + 1][secondCell.getJ() + 1] = new Cell(secondCell.getI() + 1, secondCell.getJ() + 1,
                     Field.ONE);
             secondCell.setI(secondCell.getI() + 1);
             secondCell.setJ(secondCell.getJ() + 1);
 
             Cell fourthCell = cells.get(3);
             data[fourthCell.getI() - 1][fourthCell.getJ() + 1] = new Cell(fourthCell.getI() - 1, fourthCell.getJ() + 1,
                     Field.ONE);
             fourthCell.setI(fourthCell.getI() - 1);
             fourthCell.setJ(fourthCell.getJ() + 1);
             figure.setRotationState(RotationState.HORIZONTAL);
         }
     }
 
     private boolean isPossibleToRotateHorizontally(Figure figure, Field field) {
         boolean canRotate = true;
         Cell[][] data = field.getCells();
         List<Cell> cells = figure.getCells();
         Cell firstCell = cells.get(0);
         if (data[firstCell.getI() + 2][firstCell.getJ()].getData() == Field.ONE || firstCell.getJ() + 3 >= 10
                 || data[firstCell.getI() + 1][firstCell.getJ() + 3].getData() == Field.ONE) {
             canRotate = false;
         }
         return canRotate;
     }
 
     private boolean isPossibleToRotateVertically(Figure figure, Field field) {
         boolean canRotate = true;
         Cell[][] data = field.getCells();
         List<Cell> cells = figure.getCells();
         Cell firstCell = cells.get(0);
         if (data[firstCell.getI() - 1][firstCell.getJ()].getData() == Field.ONE ||
                 data[firstCell.getI() - 2][firstCell.getJ()].getData() == Field.ONE) {
             canRotate = false;
         }
         return canRotate;
     }
 }
