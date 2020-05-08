 /*
    Kyle Fagan
    CS101-01
    Project 5 - Sodoku Checker
    Due Date: September 12, 2013
    Class description: Sodoku class for project 5
    that evaluates and formats the results to print.
    Checks validity and completeness of the puzzle.
 */
 /*
              Data Table
              ==========
    Variables            Purpose
    =========            =======
    SODOKU_ROWS          constant for number of rows in
                          a sodoku puzzle
    SODOKU_COLUMNS       constant for number of columns
                          in a sodoku puzzle
    sodoku               two dimensional char array that
                          stores the sodoku puzzle
    isValid              boolean value that corresponds
                          to a valid sodoku puzzle
    complete             boolean value that shows
                          completeness
 */
 public class Sodoku {
 
    // constatns becuase sodoku puzzles have a 9x9 grid
 	public final int SODOKU_ROWS = 9;
 	public final int SODOKU_COLUMNS = 9;
 	private char [] [] sodoku = new char[SODOKU_ROWS][SODOKU_COLUMNS];
    private boolean isValid = true;
    private boolean complete = false;
 
    /*
       Algorithm
       for(int i<-0; i < sodoku.length; i++)
          for(int j<-0; j < sodoku[i].length; j++)
             this.sodoku[i][j] = sodoku[i][j]
    */
  	public Sodoku(char [][] sodoku) {
 		// copy into array
  		for(int i=0; i < sodoku.length; i++) {
  			for(int j=0; j < sodoku[i].length; j++) {
  				this.sodoku[i][j] = sodoku[i][j];
  			}
  		}
  	}
    
    /*
       Algorithm
       checkIfComplete()
       checkRows()
       checkColumns()
       checkBoxes()
    */
    public void evaluateSodoku() {
       checkIfComplete();
       checkRows();
       checkColumns();
       checkBoxes();
    }
    
    /*
       Algorithm
       for(int i<-0; i < sodoku.length; i++)
          for(int j<-0; j < sodoku[i].length; j++)
             for(int k <- j+1; k < sodoku[i].length; k++)
                if(sodoku[i][j] == sodoku[i][k] &&
                   ignore ' ' character)
                   
                   isValid = false
 
    */
    // compares the first to rest, then second to rest, etc...
    private void checkRows() {
       for(int i = 0; i < sodoku.length; i++) {
          for(int j = 0; j < sodoku[i].length; j++) {
             for(int k = j+1; k < sodoku[i].length; k++) {
                if(sodoku[i][j] == sodoku[i][k] && !((sodoku[i][j] == ' ') || (sodoku[i][k] == ' '))) {
                   isValid = false;
                }
             }
          }
       }
    }
    
    /*
       Algorithm
       for(int i<-0; i < sodoku.length; i++)
       for(int j<-0; j < sodoku[i].length; j++)
          for(int k <- j+1; k < sodoku[j].length; k++)
             if(sodoku[j][i] == sodoku[k][i] &&
                ignore ' ' character)
                
                isValid = false
    */
    // compares the first to rest, then second to rest, etc...
    private void checkColumns() {
       for(int i = 0; i < sodoku.length; i++) {
          for(int j = 0; j < sodoku[i].length; j++) {
             for(int k = j+1; k < sodoku[j].length; k++) {
                if(sodoku[j][i] == sodoku[k][i] && !((sodoku[j][i] == ' ') || (sodoku[k][i] == ' '))) {
                   isValid = false;
                }
             }
          }
       }
       
    }
    
    /*
       Algorithm
       char [] box <- new char [9]
       box[box.length] <- values from sodoku array from [0][0] to
                          [2][2]
       repeat for each of the nine boxes, changing the bounds to
       appropriate.
       for(int i <- 0; i < box.length; i++)
          for(int j <- i+1; j < box.length; j++)
             if(box[i] == box[j] && ignore space characters)
                isValid <- false
       repeat for each of the nine boxes in a sodoku puzzle
    */
    private void checkBoxes() {
       char [] box = new char [9];
       box[0] = sodoku[0][0];
       box[1] = sodoku[0][1];
       box[2] = sodoku[0][2];
       box[3] = sodoku[1][0];
       box[4] = sodoku[1][1];
       box[5] = sodoku[1][2];
       box[6] = sodoku[2][0];
       box[7] = sodoku[2][1];
       box[8] = sodoku[2][2];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
       box[0] = sodoku[0][3];
       box[1] = sodoku[0][4];
       box[2] = sodoku[0][5];
       box[3] = sodoku[1][3];
       box[4] = sodoku[1][4];
       box[5] = sodoku[1][5];
       box[6] = sodoku[2][3];
       box[7] = sodoku[2][4];
       box[8] = sodoku[2][5];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
       box[0] = sodoku[0][6];
       box[1] = sodoku[0][7];
       box[2] = sodoku[0][8];
       box[3] = sodoku[1][6];
       box[4] = sodoku[1][7];
       box[5] = sodoku[1][8];
       box[6] = sodoku[2][6];
       box[7] = sodoku[2][7];
       box[8] = sodoku[2][8];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
       box[0] = sodoku[3][0];
       box[1] = sodoku[3][1];
       box[2] = sodoku[3][2];
       box[3] = sodoku[4][0];
       box[4] = sodoku[4][1];
       box[5] = sodoku[4][2];
       box[6] = sodoku[5][0];
       box[7] = sodoku[5][1];
       box[8] = sodoku[5][2];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
       box[0] = sodoku[3][3];
       box[1] = sodoku[3][4];
       box[2] = sodoku[3][5];
       box[3] = sodoku[4][3];
       box[4] = sodoku[4][4];
       box[5] = sodoku[4][5];
       box[6] = sodoku[5][3];
       box[7] = sodoku[5][4];
       box[8] = sodoku[5][5];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
       box[0] = sodoku[3][6];
       box[1] = sodoku[3][7];
       box[2] = sodoku[3][8];
       box[3] = sodoku[4][6];
       box[4] = sodoku[4][7];
       box[5] = sodoku[4][8];
       box[6] = sodoku[5][6];
       box[7] = sodoku[5][7];
       box[8] = sodoku[5][8];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
       box[0] = sodoku[6][0];
       box[1] = sodoku[6][1];
       box[2] = sodoku[6][2];
       box[3] = sodoku[7][0];
       box[4] = sodoku[7][1];
       box[5] = sodoku[7][2];
       box[6] = sodoku[8][0];
       box[7] = sodoku[8][1];
       box[8] = sodoku[8][2];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
       box[0] = sodoku[6][3];
       box[1] = sodoku[6][4];
       box[2] = sodoku[6][5];
       box[3] = sodoku[7][3];
       box[4] = sodoku[7][4];
       box[5] = sodoku[7][5];
       box[6] = sodoku[8][3];
       box[7] = sodoku[8][4];
       box[8] = sodoku[8][5];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
       box[0] = sodoku[6][6];
       box[1] = sodoku[6][7];
       box[2] = sodoku[6][8];
       box[3] = sodoku[7][6];
       box[4] = sodoku[7][7];
       box[5] = sodoku[7][8];
       box[6] = sodoku[8][6];
       box[7] = sodoku[8][7];
       box[8] = sodoku[8][8];
       for(int i = 0; i < box.length; i++) {
          for(int j = i+1; j < box.length; j++) {
             if(box[i] == box[j] && !((box[i] == ' ') || (box[j] == ' '))) {
                isValid = false;
             }
          }
 
       }
    }
       
    /*
       for(int i <- 0; i < sodoku.length; i++)
          for(int j <- 0; j < sodoku[i].length; j++)
             char value <- sodoku[i][j]
             if(value == ' ')
                complete <- false
             else
                complete <- true
    */
    private void checkIfComplete() {
       for(int i = 0; i < sodoku.length; i++) {
          for(int j = 0; j < sodoku[i].length; j++) {
             char value = sodoku[i][j];
             if(value == ' ') { // any spaces = !complete
                complete = false;
             }
             else {      // no spaces = complete
                complete = true;
             }
          }
       }
    }
    
    /*
       Algorithm
       if(isValid && complete)
          return "valid and complete
       else if(isValid && not complete)
          return valid and incomplete
       else
          return invalid
    */
    public String conclusion() {
       if(isValid && complete) {
          return "Valid, complete Sodoku.\n";
       }
       else if(isValid && !complete) {
          return "Valid, incomplete Sodoku.\n";
       }
       else {
          return "Invalid Sodoku\n";
       }
    }
    
    /*
       Algorithm
       String toString <- ""
       checkIfComplete()
       return toString = array formatted like a sodoku puzzle
    */
    public String toString() {
       String toString = "";
       checkIfComplete();
       return toString =  
                     sodoku[0][0] + " " + sodoku[0][1] + " " + sodoku[0][2]
                   + " | "
                   + sodoku[0][3] + " " + sodoku[0][4] + " " + sodoku[0][5]
                   + " | "
                   + sodoku[0][6] + " " + sodoku[0][7] + " " + sodoku[0][8]
                   + "\n"
                   + sodoku[1][0] + " " + sodoku[1][1] + " " + sodoku[1][2]
                   + " | "
                   + sodoku[1][3] + " " + sodoku[1][4] + " " + sodoku[1][5]
                   + " | "
                   + sodoku[1][6] + " " + sodoku[1][7] + " " + sodoku[1][8]
                   + "\n"
                   + sodoku[2][0] + " " + sodoku[2][1] + " " + sodoku[2][2]
                   + " | "
                   + sodoku[2][3] + " " + sodoku[2][4] + " " + sodoku[2][5]
                   + " | "
                   + sodoku[2][6] + " " + sodoku[2][7] + " " + sodoku[2][8]
                   + "\n"
                   + "------+-------+------\n"
                   + sodoku[3][0] + " " + sodoku[3][1] + " " + sodoku[3][2]
                   + " | "
                   + sodoku[3][3] + " " + sodoku[3][4] + " " + sodoku[3][5]
                   + " | "
                   + sodoku[3][6] + " " + sodoku[3][7] + " " + sodoku[3][8]
                   + "\n"
                   + sodoku[4][0] + " " + sodoku[4][1] + " " + sodoku[4][2]
                   + " | "
                   + sodoku[4][3] + " " + sodoku[4][4] + " " + sodoku[4][5]
                   + " | "
                   + sodoku[4][6] + " " + sodoku[4][7] + " " + sodoku[4][8]
                   + "\n"
                   + sodoku[5][0] + " " + sodoku[5][1] + " " + sodoku[5][2]
                   + " | "
                   + sodoku[5][3] + " " + sodoku[5][4] + " " + sodoku[5][5]
                   + " | "
                   + sodoku[5][6] + " " + sodoku[5][7] + " " + sodoku[5][8]
                   + "\n"
                   + "------+-------+------\n"
                   + sodoku[6][0] + " " + sodoku[6][1] + " " + sodoku[6][2]
                   + " | "
                   + sodoku[6][3] + " " + sodoku[6][4] + " " + sodoku[6][5]
                   + " | "
                   + sodoku[6][6] + " " + sodoku[6][7] + " " + sodoku[6][8]
                   + "\n"
                   + sodoku[7][0] + " " + sodoku[7][1] + " " + sodoku[7][2]
                   + " | "
                   + sodoku[7][3] + " " + sodoku[7][4] + " " + sodoku[7][5]
                   + " | "
                   + sodoku[7][6] + " " + sodoku[7][7] + " " + sodoku[7][8]
                   + "\n"
                   + sodoku[8][0] + " " + sodoku[8][1] + " " + sodoku[8][2]
                   + " | "
                   + sodoku[8][3] + " " + sodoku[8][4] + " " + sodoku[8][5]
                   + " | "
                   + sodoku[8][6] + " " + sodoku[8][7] + " " + sodoku[8][8]
                   + "\n";
     }  
} 
