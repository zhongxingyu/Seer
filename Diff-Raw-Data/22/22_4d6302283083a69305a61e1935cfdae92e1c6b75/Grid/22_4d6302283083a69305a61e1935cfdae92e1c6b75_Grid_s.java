 package edu.ncsu.csc216.fire;
 
 import java.util.ArrayList;
 
 /**
  * A grid holding all the Cell objects
  * @author Andrew Kofink and James Bruening
  */
 public class Grid {
 	
 	/**
 	 * 2D array of cells creating the grid
 	 */
 	private Cell[][] cells;
 	
 	/**
 	 * Holds the side length of the grid
 	 */
 	private int sideLength;
 	
 	/**
 	 * Holds the probability of the burning trees
 	 */
 	private double probCatch;
 	
 	/**
 	 * The cell in the center of the grid
 	 */
 	private static final int CENTER = 5;
 	
 	/**
 	 * Builds the grid of trees and empty spaces with burning
 	 * center tree
 	 * @param sideLength The length of the side of the grid
 	 * @param probCatch The probability that the fire will spread
 	 */
 	public Grid(int sideLength, double probCatch) {
 		this.sideLength = sideLength + 2;
 		this.probCatch = probCatch;
 		cells = new Cell[this.sideLength][this.sideLength];
 		for (int i = 0; i < this.sideLength; i++) {
 			for (int j = 0; j < this.sideLength; j++) {
 				if (i == 0 || i == this.sideLength - 1 || j == 0 || j == this.sideLength - 1) {
 					cells[i][j] = new Cell(Cell.EMPTY);
 				} else if (i == CENTER && j == CENTER) {
 					cells[i][j] = new Cell(Cell.BURNING);
 				} else {
 					cells[i][j] = new Cell(Cell.TREE);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Gets the grid of cells
 	 * @return The cells in a 2D array
 	 */
 	public Cell[][] getGrid() {
 		return cells;
 	}
 	
 	/**
 	 * Tests if burning is done
 	 * @return whether fire is done spreading
 	 */
 	public boolean done() {
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				if (cells[i][j].getState() == Cell.BURNING) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Increments to the time step
 	 */
 	public void nextTimestep() {
 		ArrayList<Cell[]> toBurn = new ArrayList<Cell[]>();
 		ArrayList<Cell> toEmpty = new ArrayList<Cell>();
 		
 		for (int i = 1; i < sideLength - 1; i++) {
 			for (int j = 1; j < sideLength - 1; j++) {
 				if (cells[i][j].getState() == Cell.TREE) {
					if (cells[i - 1][j].getState() == Cell.BURNING || cells[i + 1][j].getState() == Cell.BURNING || cells[i][j - 1].getState() == Cell.BURNING || cells[i][j + 1].getState() == Cell.BURNING) {
						Cell[] centerAndAdjacentCells = {cells[i][j], cells[i + 1][j], cells[i - 1][j], cells[i][j + 1], cells[i][j - 1]};
 						toBurn.add(centerAndAdjacentCells);
 					}
 				} else if (cells[i][j].getState() == Cell.BURNING) {
 					toEmpty.add(cells[i][j]);
 				}
 			}
 		}
 		
 		for (int i = 0; i < toBurn.size(); i++) {
 			int j = 0;
			toBurn.get(i)[j].spread(probCatch, toBurn.get(i)[j++], toBurn.get(i)[j++], toBurn.get(i)[j++], toBurn.get(i)[j++]);
 		}
 		
 		for (int i = 0; i < toEmpty.size(); i++) {
 			toEmpty.get(i).setState(Cell.EMPTY);
 		}
 	}
 }
