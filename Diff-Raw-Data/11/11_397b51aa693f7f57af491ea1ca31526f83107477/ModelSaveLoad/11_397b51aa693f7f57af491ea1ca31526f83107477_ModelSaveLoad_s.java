 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
  
  
 public class ModelSaveLoad {
  
     SudokuModel model;
     ModelGrid currentGrid;
     ModelGrid answerGrid;
    
     public ModelSaveLoad(SudokuModel model, ModelGrid currentGrid, ModelGrid answerGrid) {
         this.model = model;
         this.currentGrid = currentGrid;
         this.answerGrid = answerGrid;
     }
     
     public void saveGame(File save) {
         String newline = System.getProperty("line.separator");
         String original = originalToString(newline);
         String candidates = candidatesToString(newline);
         String s = original + newline + "Answer:" + newline + this.gridToString(answerGrid) 
                 + "Current:" + newline + this.gridToString(currentGrid) + newline + candidates;
         try {
             PrintWriter print = new PrintWriter(save);
             print.write(s);
             print.close();
         } catch (FileNotFoundException e) {}
     }
  
     public void loadGame(File save) {
         try {
             model.clearPuzzle();
             Scanner s = new Scanner(save);
             loadOriginal(s);
             loadAnswer(s);
             loadCurrent(s);
             loadCandidates(s);
             s.close();
         } catch (FileNotFoundException e) {
  
         } catch (NoSuchElementException e) {
        
         }
     }
 
 private void loadCandidates(Scanner s) {
 		s.next();
 		for (int i=0; i<ModelGrid.NUM_ROWS; i++) {
 		    for (int j=0; j<ModelGrid.NUM_COLS; j++) {
		            if (s.next() == "[") {
		                    s.skip("[");
 		            }
		            if (s.next() == "]") {
		                    s.skip("]");
		        }
 		            while (s.hasNextInt()) {
 		                currentGrid.grid.get(i).get(j).addCandidate(s.nextInt());
 		            }
  
 		    }
 		}
 	}
 
 	private void loadCurrent(Scanner s) {
 		s.next();
 		for (int i=0; i<ModelGrid.NUM_ROWS; i++) {
 		    for (int j=0; j<ModelGrid.NUM_COLS; j++) {
 		            s.next();
 		            if (s.hasNextInt()) {
 		                    if (currentGrid.grid.get(i).get(j).isGiven() == false) {
 		                currentGrid.grid.get(i).get(j).setNumber(s.nextInt());
 		                    } else {
 		                            s.next();
 		                    }
 		            } else {
 		                    s.next();
 		            }
 		        if ((j+1)%3 == 0) {
 		             s.next();
 		        }
 		    }
 		}
 	}
 
 	private void loadAnswer(Scanner s) {
 		s.next();
 		for (int i=0; i<ModelGrid.NUM_ROWS; i++) {
 		    for (int j=0; j<ModelGrid.NUM_COLS; j++) {
 		            s.next();
 		            if (s.hasNextInt()) {
 		            answerGrid.grid.get(i).get(j).giveNumber(s.nextInt());
 		            } else {
 		                    s.next();
 		            }
 		        if ((j+1)%3 == 0) {
 		             s.next();
 		        }
 		    }
 		}
 	}
 
 	private void loadOriginal(Scanner s) {
 		s.next();
 		for (int i=0; i<ModelGrid.NUM_ROWS; i++) {
 		    for (int j=0; j<ModelGrid.NUM_COLS; j++) {
 		            s.next();
 		            if (s.hasNextInt()) {
 		            currentGrid.grid.get(i).get(j).giveNumber(s.nextInt());
 		            } else {
 		                    s.next();
 		            }
 		        if ((j+1)%3 == 0) {
 		             s.next();
 		        }
 		    }
 		}
 	}
     
     public String gridToString(ModelGrid string) {
         String sudoku = "";
         for (int i=0; i<ModelGrid.NUM_ROWS; i++) {
             for (int j=0; j<ModelGrid.NUM_COLS; j++) {
                 if(string.grid.get(i).get(j).getNumber()==ModelCell.EMPTY) {
                     sudoku = sudoku + " | -";
                 } else {
                    sudoku = sudoku + " | " + string.grid.get(i).get(j).getNumber();
                 }
                 if ((j+1)%3 == 0) {
                         sudoku = sudoku + " | ";
                 }
             }
             sudoku = sudoku + System.getProperty("line.separator");
             if ((i+1)%3 == 0) {
                 sudoku = sudoku + System.getProperty("line.separator");
             }
         }
         return sudoku;
     }
     
 	private String originalToString(String newline) {
 		String original = "Original:" + newline;
         for (int i=0; i<ModelGrid.NUM_ROWS; i++) {
             for (int j=0; j<ModelGrid.NUM_COLS; j++) {
                 if (currentGrid.grid.get(i).get(j).isGiven() == false) {
                         original = original + " | -";
                 } else {
                         original = original + " | " + currentGrid.grid.get(i).get(j).getNumber();
                 }
                 if ((j+1)%3 == 0) {
                          original = original + " | ";
                 }
             }
             original = original + System.getProperty("line.separator");
             if ((i+1)%3 == 0) {
                 original = original + System.getProperty("line.separator");
             }
         }
 		return original;
 	}
 	
 	private String candidatesToString(String newline) {
 		String candidates = "Candidates:" + newline;
         for (int i=0; i<ModelGrid.NUM_ROWS; i++) {
             for (int j=0; j<ModelGrid.NUM_COLS; j++) {
                 candidates = candidates + "[ ";
                 for (int k=1; k<9; k++) {
                     if (currentGrid.grid.get(i).get(j).hasCandidate(k)) {
                         candidates = candidates + k + " ";
                     }
                 }
                 candidates = candidates + "] ";
                 if ((j+1)%3 == 0) {
                          candidates = candidates + " ";
                 }
             }
             candidates = candidates + " ";
         }
 		return candidates;
 	}
 }
