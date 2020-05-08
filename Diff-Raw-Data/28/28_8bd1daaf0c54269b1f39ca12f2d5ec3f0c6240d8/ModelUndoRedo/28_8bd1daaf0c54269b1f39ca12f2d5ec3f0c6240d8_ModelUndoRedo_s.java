 import java.util.Scanner;
 import java.util.Stack;
 
 
 public class ModelUndoRedo {
    SudokuModel sudokuModel;
     private Stack<String> undoStack;
     private Stack<String> redoStack;
     
    public ModelUndoRedo(SudokuModel sudokuModel) {
         this.sudokuModel = sudokuModel;
         this.undoStack = new Stack<String>();
         this.redoStack = new Stack<String>();
     }
     
     public void generateCalled() {
         this.undoStack.clear();
         this.redoStack.clear();
     }
     
     public void giveCellNumberCalled(int row, int col, int n, int current) {
         if(current==ModelCell.EMPTY) {
             this.undoStack.push("giveCellNumber " + row + " " + col + " " + n);
             this.redoStack.clear();
         } else {
             this.undoStack.push("changeGiveCellNumber " + row + " " + col + " " + current);
             this.redoStack.clear();
         }
     }
     
     public void removeCellNumberCalled(int row, int col, int current) {
         this.undoStack.push("removeCellNumber " + row + " " + col + " " + current);
         this.redoStack.clear();
     }
     
     public void setCellNumberCalled(int row, int col, int n, int current) {
         if(current==ModelCell.EMPTY) {
             this.undoStack.push("setCellNumber " + row + " " + col + " " + n);
             this.redoStack.clear();
         } else {
             this.undoStack.push("changeSetCellNumber " + row + " " + col + " " + current);
             this.redoStack.clear();
         }
     }
     
     public void clearCellNumberCalled(int row, int col, int current) {
         this.undoStack.push("clearCellNumber " + row + " " + col + " " + current);
         this.redoStack.clear();
     }
     
     public Position undo() {
         if(undoStack.isEmpty()) {
             return null;
         }
         Scanner s = new Scanner(undoStack.pop());
         String command = s.next();
         int row = s.nextInt();
         int col = s.nextInt();
         
         // So the undo move doesn't get added to the redo stack
         Stack<String> tmp = this.redoStack;
         this.redoStack = new Stack<String>();
         
         if(command.equals("giveCellNumber")) {
             sudokuModel.removeCellNumber(row, col);
         } else if (command.equals("changeGiveCellNumber")) {
             sudokuModel.giveCellNumber(row, col, s.nextInt());
         } else if (command.equals("removeCellNumber")) {
             sudokuModel.giveCellNumber(row, col, s.nextInt());
         } else if (command.equals("setCellNumber")) {
             sudokuModel.clearCellNumber(row, col);
         } else if (command.equals("changeSetCellNumber")) {
             sudokuModel.setCellNumber(row, col, s.nextInt());
         } else if (command.equals("clearCellNumber")) {
             sudokuModel.setCellNumber(row, col, s.nextInt());
         }
         s.close();
         this.redoStack = tmp;
         this.redoStack.push(undoStack.pop());
         return new Position(row, col);
     }
     
     public Position redo() {
         if(redoStack.isEmpty()) {
             //return;
             return null;
         }
         Scanner s = new Scanner(redoStack.pop());
         String command = s.next();
         int row = s.nextInt();
         int col = s.nextInt();
         
         // So the redo move doesn't get added to the redo stack
         Stack<String> tmp = this.redoStack;
         this.redoStack = new Stack<String>();
         
         if(command.equals("giveCellNumber")) {
             sudokuModel.removeCellNumber(row, col);
         } else if (command.equals("changeGiveCellNumber")) {
             sudokuModel.giveCellNumber(row, col, s.nextInt());
         } else if (command.equals("removeCellNumber")) {
             sudokuModel.giveCellNumber(row, col, s.nextInt());
         } else if (command.equals("setCellNumber")) {
             sudokuModel.clearCellNumber(row, col);
         } else if (command.equals("changeSetCellNumber")) {
             sudokuModel.setCellNumber(row, col, s.nextInt());
         } else if (command.equals("clearCellNumber")) {
             sudokuModel.setCellNumber(row, col, s.nextInt());
         }
         s.close();
         this.redoStack = tmp;
         return new Position(row, col);
     }
     
     public boolean canUndo() {
        return (!redoStack.isEmpty());   
     }
     
     public boolean canRedo() {
        return (!undoStack.isEmpty());
     }
 
 }
