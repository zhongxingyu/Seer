 package ru.pondohva.game;
 
import ru.pondohva.game.Field;
 import ru.pondohva.player.*;
 
 public class Game {
 
     private Field newField;
     private boolean theEnd = false;
 
     private Player firstPlayer;
     private Player secondPlayer;
 
     public int stepNumber = 1;
 
     private void doStep() {
         if (stepNumber % 2 != 0) {
             firstPlayer.step();
         } else {
             secondPlayer.step();
         }
         if ((stepNumber == newField.field_size*newField.field_size) || checkWin()) {
             theEnd = true;
         }
         stepNumber++;
     }
 
     private boolean checkWin() {
         //bad-bad hardcode, need algorhitm for this
         char[][] field = newField.getField();
         if (field[0][0] == field[0][1] && field[0][1] == field[0][2] && field[0][0] != ' ')
             return true;
         if (field[1][0] == field[1][1] && field[1][1] == field[1][2] && field[1][0] != ' ')
             return true;
         if (field[2][0] == field[2][1] && field[2][1] == field[2][2] && field[2][0] != ' ')
             return true;
         if (field[0][0] == field[1][0] && field[1][0] == field[2][0] && field[0][0] != ' ')
             return true;
         if (field[0][1] == field[1][1] && field[1][1] == field[2][1] && field[0][1] != ' ')
             return true;
         if (field[0][2] == field[1][2] && field[1][2] == field[2][2] && field[0][2] != ' ')
             return true;
         if (field[0][0] == field[1][1] && field[1][1] == field[2][2] && field[0][0] != ' ')
             return true;
         if (field[0][2] == field[1][1] && field[1][1] == field[2][0] && field[0][2] != ' ')
             return true;
         return false;
     }
 
     public void startGame() {
 
         while (!theEnd) {
             System.out.println("Step: " + stepNumber);
             doStep();
             newField.showField();
         }
     }
 
     public Game (String first, String second, int fsize) {
         newField = new Field(fsize);
        if (first == "Computer") {
             firstPlayer = new Computer(newField, this, 'X');
        } else if (first == "Local") {
             firstPlayer = new Local(newField, this, 'X');
        } else if (first == "Remote") {
             firstPlayer = new Local(newField, this, 'X');
         }
        if (first == "Computer") {
             secondPlayer = new Computer(newField, this, 'O');
        } else if (first == "Local") {
             secondPlayer = new Local(newField, this, 'O');
        } else if (first == "Remote") {
             secondPlayer = new Local(newField, this, 'O');
         }
     }
 }
