 package com.example.forestgame;
 
 import java.util.Random;
 
 import com.example.forestgame.element.Element;
 import com.example.forestgame.element.TableOfElements;
 
 
 
 public class SlotMatrix {
     
     private Slot[][] matrix;
     private final int ROWS = 6;
     private final int COLUMS = 6;
     private int lastEditedSlotRow;
     private int lastEditedSlotColum;
    private static int NUMBER_OF_ElEMENTS_ON_START = 10;
     private int score;
 
     
     public SlotMatrix() {
 	
 	matrix = new Slot[ROWS][COLUMS];
 	init();
 	viewSlots();
     }
     
     public void putToSlot(Element e, int row, int col) {
 	
 	if (isSlotEmpty(row, col)) {
 	    addToSlot(e, row, col);
 	    lastEditedSlotRow = row;
 	    lastEditedSlotColum = col;
 	    update();
 	}
     }
     
     // has to be used always before using addToSLot(..)
     public boolean isSlotEmpty(int row, int col) {
 	
 	return matrix[row][col].isEmpty();
     }
     
     public void update() {
 	
 	checkSimilarElements();
 	viewSlots();
     }
     
     public void init() {
 	
 	for (int i = 0; i < ROWS; i++) {
 	    for (int j = 0; j < COLUMS; j++) {
 		matrix[i][j] = new Slot();
 	    }
 	}
 	Random random = new Random();
 	for (int i = 0; i < NUMBER_OF_ElEMENTS_ON_START; i++) {
 	    int r = (int) (random.nextDouble()*ROWS);
 	    int c = (int) (random.nextDouble()*COLUMS);
 	    if (isSlotEmpty(r, c)) {
 		addToSlot(TableOfElements.getRandomElement(), r, c); //Not putToSlot(..) because of the update() method
 	    } else {
 		i--;
 	    }
 	}
     }
     
     public void viewSlots() {
 	
 	// method for visualizing textures on GameScene
     }
     
     public int getScore() {
 	
 	return score;
     }
     
     private void clearSlot(int row, int col) {
 	
 	matrix[row][col] = new Slot();
     }
     
     // putting Element into Slot and changing flags if needed
     private void addToSlot(Element e, int r, int c) {
 	
 	matrix[r][c].addElement(e);
 	if (r > 0) {
 	    analyzeNeighbor(r, c, r-1, c);
 	}
 	if (r < ROWS-1) {
 	    analyzeNeighbor(r, c, r+1, c);
 	}
 	if (c > 0) {
 	    analyzeNeighbor(r, c, r, c-1);
 	}
 	if (c < COLUMS-1) {
 	    analyzeNeighbor(r, c, r, c+1);
 	}
     }
     
     // setting hasSimilarNeighbor and readyForNextLevel flags
     // flag readyForNextLevel doesn't have to be set for every Slot in chain, only for the last edited
     private void analyzeNeighbor(int r, int c, int r1, int c1) {
 	Slot s = matrix[r][c];
 	Slot s1 = matrix[r1][c1];
 	if (s1.isSimilarTo(s.getElement())) {
 	    s.hasSimilarNeighbor = true;
 	    if (s1.hasSimilarNeighbor) {
 		s1.readyForNextLevel = true;
 		s.readyForNextLevel = true;
 	    } else {
 		s1.hasSimilarNeighbor = true;
 	    }
 	}
     }
     
     // checking if the last added element is the third (or more) and has to get next level 
     private void checkSimilarElements() {
 	
 	int r = lastEditedSlotRow;
 	int c = lastEditedSlotColum;
	Slot s = matrix[r][c];
	Element e = s.getElement();
	addToSlot(s.getNextLevelElement(), r, c);
 	if (matrix[r][c].readyForNextLevel) {
 	    if (r > 0) {
 		if (matrix[r-1][c].isSimilarTo(e)) {
 		    collectSimilarElements(r, c, r-1, c, e);
 		}
 	    }
 	    if (r < ROWS-1) {
 		if (matrix[r+1][c].isSimilarTo(e)) {
 		    collectSimilarElements(r, c, r+1, c, e);
 		}
 	    }
 	    if (c > 0) {
 		if (matrix[r][c-1].isSimilarTo(e)) {
 		    collectSimilarElements(r, c, r, c-1, e);
 		}
 	    }
 	    if (c < COLUMS-1) {
 		if (matrix[r][c+1].isSimilarTo(e)) {
 		    collectSimilarElements(r, c, r, c+1, e);
 		}
 	    }
 	    update();
 	}
     }
     
     //recoursively collecting the chain of similar elements, removing them from field
     private void collectSimilarElements(int toRow, int toCol, int fromRow, int fromCol, Element e) {
 	
 	graphicalMoving(toRow, toCol, fromRow, fromCol);
 	score =+ matrix[fromRow][fromCol].getScore();
  	clearSlot(fromRow, fromCol);
 	if (fromRow > 0) {
 	    if (matrix[fromRow-1][fromCol].isSimilarTo(e)) {
 		collectSimilarElements(toRow, toCol, fromRow-1, fromCol, e);
 	    }
 	}
 	if (fromRow < ROWS-1) {
 	    if (matrix[fromRow+1][fromCol].isSimilarTo(e)) {
 		collectSimilarElements(toRow, toCol, fromRow+1, fromCol, e);
 	    }
 	}
 	if (fromCol > 0) {
 	    if (matrix[fromRow][fromCol-1].isSimilarTo(e)) {
 		collectSimilarElements(toRow, toCol, fromRow, fromCol-1, e);
 	    }
 	}
 	if (fromCol < COLUMS-1) {
 	    if (matrix[fromRow][fromCol+1].isSimilarTo(e)) {
 		collectSimilarElements(toRow, toCol, fromRow, fromCol+1, e);
 	    }
 	}
     }
     
     private void graphicalMoving(int toRow, int toCol, int fromRow, int fromCol) {
 	
 	// need to do some graphic operations when elements are moving to the last added to change level (next Sprint)
 	
     }
    
 }
 
 class Slot {
     
     private boolean isEmpty;
     boolean hasSimilarNeighbor; // Not private, has only one similar neighbor
     boolean readyForNextLevel; // Not private, has more than 1 similar neighbor
     private Element element;
     
     public Slot() {
 	
 	isEmpty = true;
 	hasSimilarNeighbor = false;
 	readyForNextLevel = false;
     }
     
     public void addElement(Element e) {
 	
 	element = e;
 	isEmpty = false;
     }
     
     public Element getNextLevelElement() {
 	element.changeToNextLvl();
 	hasSimilarNeighbor = false;
 	readyForNextLevel = false;
 	return element;
     }
     
     public boolean isEmpty() {
 	
 	return isEmpty;
     }
     
     public boolean isSimilarTo(Element e) {
 	
 	if (this.isEmpty) {
 	    return false; 
 	} else {
 	    return (this.element.getName() == e.getName());
 	}
     }
     
     public Element getElement() {
 	return element;
     }
     
     public int getScore() {
 	
 	return TableOfElements.getScores(element.getName());
     }
     
 }
