 package game;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Random;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 @SuppressWarnings("serial")
 public class Board extends JPanel{
 	// variables
 	private Bubble[][] bubbleBoard;
 	static final int ROWS = 15;
 	static final int COLS = 8;
 	private boolean end;
 	public Reticule reticule;
 
 	// methods
 	public Board() {
 		end = false;
 		new ArrayList<Bubble>();
 		reticule = new Reticule();
 		bubbleBoard = new Bubble[ROWS][COLS];
 		Bubble temp = null;
 		Random rng = new Random();
 		for (int row = 0; row < ROWS; ++row) {
 			for (int col = 0; col < COLS; ++col) {
 				temp = new Bubble(row, col);
 				temp.setEmpty(false);
 				temp.setBubbleColor(Integer.toString((rng.nextInt(5))));
 				bubbleBoard[row][col] = temp;
 			}
 		}
 	}
 
 
 	public void swap(Bubble one, Bubble two) {
 		if(one.isEmpty() || two.isEmpty()) {
 			return;
 		}
 		Color tempColor = one.getBubbleColor();
 		one.setBubbleColor(two.getBubbleColor());
 		two.setBubbleColor(tempColor);
 		bubbleBoard[one.getRow()][one.getCol()] = one;
 		bubbleBoard[two.getRow()][two.getCol()] = two;
 	}
 	@Override
 	public void paintComponent(Graphics g) {
 		g.setColor(Color.GRAY);
 		g.fillRect(0, 0, 5000, 5000);
 		for (int i = 0; i < ROWS; i++) {
 			for (int j = 0; j < COLS; j++) {
 				bubbleBoard[i][j].draw(g);
 			}
 		}
 		reticule.draw(g);
 	}
 
 	public void detectLinear() {
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
 				if (!bubbleBoard[row][col].isEmpty()) {
					if (row + 2 < ROWS && bubbleBoard[row][col].getBubbleColor().equals(bubbleBoard[row + 1][col].getBubbleColor()) && bubbleBoard[row + 1][col].getBubbleColor().equals(bubbleBoard[row + 2][col].getBubbleColor())) {
 						System.out.println("Passing vertical");
 						System.out.println("Setting " + Integer.toString(row) + " " + Integer.toString(col) + " to empty.");
 						System.out.println("Setting " + Integer.toString(row+1) + " " +  Integer.toString(col) + " to empty.");
 						System.out.println("Setting " + Integer.toString(row+2) + " " + Integer.toString(col) + " to empty.");
 						bubbleBoard[row][col].setEmpty(true);
 						bubbleBoard[row + 1][col].setEmpty(true);
 						bubbleBoard[row + 2][col].setEmpty(true);
 						bubbleBoard[row][col].setBubbleColor(Color.PINK);
 						bubbleBoard[row + 1][col].setBubbleColor(Color.PINK);
 						bubbleBoard[row + 2][col].setBubbleColor(Color.PINK);
 					}
					else if (col + 2 < COLS && bubbleBoard[row][col].getBubbleColor().equals(bubbleBoard[row][col + 1].getBubbleColor()) && bubbleBoard[row][col + 1].getBubbleColor().equals(bubbleBoard[row][col + 2].getBubbleColor())) {
 						System.out.println("Passing horizontal");
 						System.out.println("Setting " + Integer.toString(row) + " " + Integer.toString(col) + " to empty.");
 						System.out.println("Setting " + Integer.toString(row) + " " +  Integer.toString(col+1) + " to empty.");
 						System.out.println("Setting " + Integer.toString(row) + " " + Integer.toString(col+2) + " to empty.");
 						bubbleBoard[row][col].setEmpty(true);
 						bubbleBoard[row][col + 1].setEmpty(true);
 						bubbleBoard[row][col + 2].setEmpty(true);
 						bubbleBoard[row][col].setBubbleColor(Color.PINK);
 						bubbleBoard[row][col + 1].setBubbleColor(Color.PINK);
 						bubbleBoard[row][col + 2].setBubbleColor(Color.PINK);
 					}
 					else {
 						continue;
 					}
 				}
 			}
 		}
 	}
 
 //	public void fallHelper(Bubble aBubble, int row, int col) {
 //		if (aBubble.isEmpty()) {
 //			if (row == 0) {
 //				aBubble = new Bubble(aBubble.getRow(), aBubble.getCol());
 //				aBubble.setEmpty(false);
 //			}
 //			else {
 //				if (bubbleBoard[row-1][col].isEmpty())
 //					fallHelper(aBubble, row-1, col);
 //				else {
 //					aBubble.setBubbleColor(bubbleBoard[row-1][col].getBubbleColor());
 //					aBubble.setEmpty(false);
 //					bubbleBoard[row-1][col].setEmpty(true);
 //				}
 //			}
 //
 //		}
 //	}
 
 	public void fallMaster() {
 		Random rng = new Random();
 		for(int i = 0; i < ROWS; i++) {
 			for (int j = 0; j < COLS; j++) {
 				if (bubbleBoard[i][j].isEmpty()) {
 					bubbleBoard[i][j].setBubbleColor(Integer.toString(rng.nextInt(9)));
 					bubbleBoard[i][j].setEmpty(false);
 				}
 			}
 		}
 	}
 
 	public void checkEnd() {
 
 	}
 
 	public int calculateScore() {
 		return 0;
 	}
 	public Bubble getBubbleAt(int r, int c) {
 		return bubbleBoard[r][c];
 	}
 }
