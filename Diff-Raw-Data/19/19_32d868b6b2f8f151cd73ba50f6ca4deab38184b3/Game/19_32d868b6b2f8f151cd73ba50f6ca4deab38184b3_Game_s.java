 package ru.pondohva.tictactoe;
 
 import java.util.ArrayList;
 
 public class Game {
 
     private Field newField;
     private boolean theEnd = false;
     private Player firstPlayer, secondPlayer;
     private int stepNumber = 1;
     private ArrayList<Field> history;
 
     @SuppressWarnings("unused")
     public int getStepNumber() {
 		return stepNumber;
 	}
 
     private void writeHistory(Field currentField) {
         Field test = new Field(currentField);
         history.add(test);
     }
 
     @SuppressWarnings("unused")
     private void setStep(int toStep) {
         this.newField = history.get(toStep - 2);
         // set new history
         this.stepNumber = toStep;
         doStep();
     }
 
	private void doStep() {
         if (stepNumber % 2 != 0) {
             while(!firstPlayer.step());
         } else {
             while(!secondPlayer.step());
         }
         if ((stepNumber == newField.field_size*newField.field_size) || checkWin()) {
             theEnd = true;
         }
         writeHistory(newField);
         stepNumber++;
     }
 
     private boolean checkWin() {
         char[][] field = newField.getField();
         int matchCount;
         char ex;
         for (int i = 0; i < newField.field_size; i++) {
             ex = field[i][0];
             matchCount = 0;
             for (int j = 1; j < newField.field_size; j++) {
                 if (field[i][j] != ' ' && ex == field[i][j]) {
                     matchCount++;
                 }
             }
             if (matchCount == newField.field_size - 1) {
                 System.out.println("Winner " + ex);
                 return true;
             }
         }
 
 
         for (int j = 0; j < newField.field_size; j++) {
             matchCount = 0;
             ex = field[0][j];
             if (ex == ' ') {
                 break;
             }
             for (int i = 1; i < newField.field_size; i++) {
                 if (ex == field[i][j]) {
                     matchCount++;
                 }
             }
             if (matchCount == newField.field_size - 1) {
                 System.out.println("Winner " + ex);
                 return true;
             }
         }
         matchCount = 0;
         ex = field[0][0];
         for (int i = 1; i < newField.field_size; i++) {
             if (ex == ' ') {
                 break;
             }
             if (field[i][i] == ex) {
                 matchCount++;
             }
         }
         if (matchCount == newField.field_size - 1) {
             System.out.println("Winner is " + ex);
             return true;
         }
         matchCount = 0;
         ex = field[0][newField.field_size - 1];
         for (int i = 1; i < newField.field_size; i++) {
             if (ex == ' ') {
                 break;
             }
             if (field[i][newField.field_size - 1 - i] == ex) {
                 matchCount++;
             }
         }
         if (matchCount == newField.field_size - 1) {
             System.out.println("Winner is " + ex);
             return true;
         }
 
         return false;
     }
 
     public void startGame() {
         while (!theEnd) {
             System.out.println("Step: " + stepNumber);
             doStep();
             newField.showField();
         }
         System.out.println();
         System.out.println("HISTORY:");
         for (Field i: history) {
             i.showField();
             System.out.println();
         }
     }
 
     public Game (String first, String second, int fsize) {
         history = new ArrayList<Field>();
         newField = new Field(fsize);
         if (first.equals("Computer")) {
             firstPlayer = new Computer(newField, 'X');
         } else if (first.equals("Local")) {
             firstPlayer = new Local(newField, 'X');
         } else if (first.equals("Remote")) {
             firstPlayer = new Local(newField, 'X');
         }
         if (second.equals("Computer")) {
             secondPlayer = new Computer(newField, 'O');
         } else if (second.equals("Local")) {
             secondPlayer = new Local(newField, 'O');
         } else if (second.equals("Remote")) {
             secondPlayer = new Local(newField, 'O');
         }
     }
 }
