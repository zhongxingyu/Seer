 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * You do not have to change anything in this file. The game moves are executed
  * through calls to the methods mouseClicked(MouseEvent e) and run().
  * 
  * @author Mai Ajspur
  * @version 1.2.2007
  * */
 public class FourConnectGUI extends JComponent implements MouseListener {
 	static final long serialVersionUID = 1234567890;
 
 	private int chosenColumn; // The value of the last column chosen by the
 								// human player.
 	private IGameLogic player1;
 	private IGameLogic player2;
 	private IGameLogic.Winner winner;
 	private int playerTurn;
 	private int[][] gameBoard;
 
 	// Images for drawing the game board
 	private Image part, redPion, bluePion, arrow, arrow_active, background;
 	private Image border_left, border_right, border_top, border_bottom;
 	private Image corner_left_top, corner_left_bottom, corner_right_top,
 			corner_right_bottom;
 	private Image redWon, blueWon;
 
 	/**
 	 * @param player1
 	 *            The implementation of the game logic for player1, ir null for
 	 *            human
 	 * @param player2
 	 *            The implementation of the game logic for player2, ir null for
 	 *            human
 	 * @param cols
 	 *            The columns of the game board
 	 * @param rows
 	 *            The rows of the game board
 	 */
 	public FourConnectGUI(IGameLogic player1, IGameLogic player2, int cols,
 			int rows) {
 		part = Toolkit.getDefaultToolkit().getImage("imgs/maze.png");
 		redPion = Toolkit.getDefaultToolkit().getImage("imgs/redPion.png");
 		bluePion = Toolkit.getDefaultToolkit().getImage("imgs/bluePion.png");
 		arrow = Toolkit.getDefaultToolkit().getImage("imgs/arrow.png");
 		arrow_active = Toolkit.getDefaultToolkit().getImage(
 				"imgs/arrow_active.png");
 		background = Toolkit.getDefaultToolkit()
 				.getImage("imgs/background.png");
 
 		border_left = Toolkit.getDefaultToolkit().getImage(
 				"imgs/border_left.png");
 		border_right = Toolkit.getDefaultToolkit().getImage(
 				"imgs/border_right.png");
 		border_top = Toolkit.getDefaultToolkit()
 				.getImage("imgs/border_top.png");
 		border_bottom = Toolkit.getDefaultToolkit().getImage(
 				"imgs/border_bottom.png");
 		corner_left_top = Toolkit.getDefaultToolkit().getImage(
 				"imgs/corner_left_top.png");
 		corner_left_bottom = Toolkit.getDefaultToolkit().getImage(
 				"imgs/corner_left_bottom.png");
 		corner_right_top = Toolkit.getDefaultToolkit().getImage(
 				"imgs/corner_right_top.png");
 		corner_right_bottom = Toolkit.getDefaultToolkit().getImage(
 				"imgs/corner_right_bottom.png");
 
 		redWon = Toolkit.getDefaultToolkit().getImage("imgs/redwon.png");
 		blueWon = Toolkit.getDefaultToolkit().getImage("imgs/bluewon.png");
 
 		chosenColumn = -1;
 		this.player1 = player1;
 		this.player2 = player2;
 		winner = IGameLogic.Winner.NOT_FINISHED;
 		playerTurn = 1;
 		gameBoard = new int[cols][rows];
 		this.addMouseListener(this);
 	}
 
 	/*
 	 * Draws the current game board and shows if someone won.
 	 */
 	String thinkingText = "Calculating...";
 	public void paint(Graphics g) {
 		this.setDoubleBuffered(true);
 		Insets in = getInsets();
 		g.translate(in.left, in.top);
 
 		//System.out.println("paint();");
 		
 		if (isThinking)
 		g.drawChars(thinkingText.toCharArray(), 0, thinkingText.length(), 20,
 				20);
 		// int[][] gameboard = logic.getGameBoard();
 		int cols = gameBoard.length;
 		int rows = gameBoard[0].length;
 
 		for (int c = 0; c < cols; c++) {
 			for (int r = 0; r < rows; r++) {
 				int player = gameBoard[c][r];
 				if (player == 0) // background
 					g.drawImage(background, 100 + 100 * c, 100 + 100 * r, this);
 				else if (player == 2) // red = player2
 					g.drawImage(redPion, 100 + 100 * c, 100 + 100 * r, this);
 				else
 					// blue = player1
 					g.drawImage(bluePion, 100 + 100 * c, 100 + 100 * r, this);
 				g.drawImage(part, 100 + 100 * c, 100 + 100 * r, this);
 				if (c == 0) {
 					g.drawImage(border_left, 0, 100 + 100 * r, this);
 					g.drawImage(border_right, cols * 100 + 100, 100 + 100 * r,
 							this);
 				}
 			}
 			g.drawImage(border_top, 100 + 100 * c, 0, this);
 			if (c == chosenColumn)
 				g.drawImage(arrow_active, 100 + 100 * c, 10, this);
 			else
 				g.drawImage(arrow, 100 + 100 * c, 10, this);
 			g.drawImage(border_bottom, 100 + 100 * c, rows * 100 + 100, this);
 		}
 		g.drawImage(corner_left_top, 0, 0, this);
 		g.drawImage(corner_left_bottom, 0, rows * 100 + 100, this);
 		g.drawImage(corner_right_top, 100 + 100 * cols, 0, this);
 		g.drawImage(corner_right_bottom, 100 + 100 * cols, rows * 100 + 100,
 				this);
 
 		if (winner == IGameLogic.Winner.PLAYER1)
 			g.drawImage(blueWon, cols * 100 / 2 - 50, rows * 100 / 2 + 25, this);
 		else if (winner == IGameLogic.Winner.PLAYER2)
 			g.drawImage(redWon, cols * 100 / 2 - 50, rows * 100 / 2 + 25, this);
 	}
 
 	private int humanSelectedColumn(MouseEvent e) {
 		if (e.getY() < 60) {
 			for (int x = 0; x < gameBoard.length; x++) {
 				if (100 + x * 100 <= e.getX() && e.getX() < 100 + (x + 1) * 100) {
 					chosenColumn = x;
 				}
 			}
 		}
 		return chosenColumn;
 	}
 
 	private boolean updateBoard(int col, int player) {
 		if (col == -1) {
 			return false;
 		}
 		if (gameBoard[col][0] != 0) {
 			return false;
 		}
 		int r = gameBoard[col].length - 1;
 		while (gameBoard[col][r] != 0)
 			r--;
 		gameBoard[col][r] = player;
 		return true;
 	}
 
 	private void stopThinking(String place) {
 		isThinking = false;
 		//this.addMouseListener(this);
 		System.out.println("done thinking: " + place);
 		repaint();
 	}
 
 	boolean isThinking = false;
 	Component component = this;
 	/*
 	 * When it is the humans turn and he clicks on one of the arrows, the
 	 * corresponding column is chosen and the logic puts a token/coin in the
 	 * column. Then the computer player is prompted to make a move, which is
 	 * done in a new thread.
 	 */
 	
 	
 	private class DoGame implements Runnable
 	{
 		MouseEvent e = null;
 		public DoGame(MouseEvent e) {
 			this.e = e;
 		}
 
 		@Override
 		public void run() {
 			if (winner == IGameLogic.Winner.NOT_FINISHED) {
 				int col = -1;
 				isThinking = true;
 				// repaint();
 				System.out.println("thinking");
 				if (playerTurn == 1) {
 					if (player1 == null) {// human
 						col = humanSelectedColumn(e);
 						if (updateBoard(col, 1)) {
 							player2.insertCoin(col, 1);
 							winner = player2.gameFinished();
 							playerTurn = 2;
 							stopThinking("1");
 							new Thread(new DoGame(null)).start();
 						}
 					} else {// computer
 						col = player1.decideNextMove();
 						if (updateBoard(col, 1)) {
 							if (player2 != null)
 								player2.insertCoin(col, 1);
 							player1.insertCoin(col, 1);
 							winner = player1.gameFinished();
 							playerTurn = 2;
 							stopThinking("2");
 						} else {
 							JOptionPane.showMessageDialog(component,
 									"Player1 chose an invalid move, please debug!",
 									"Invalid Move", JOptionPane.ERROR_MESSAGE);
 							stopThinking("3");
 						}
 					}
 				} else {
 					if (player2 == null) {// human
 						col = humanSelectedColumn(e);
 						if (updateBoard(col, 2)) {
 							player1.insertCoin(col, 2);
 							winner = player1.gameFinished();
 							playerTurn = 1;
 							stopThinking("4");
 							new Thread(new DoGame(null)).start();
 						}
 					} else {// computer
 						col = player2.decideNextMove();
 						if (updateBoard(col, 2)) {
 							if (player1 != null)
 								player1.insertCoin(col, 2);
 							player2.insertCoin(col, 2);
 							winner = player2.gameFinished();
 							playerTurn = 1;
 							stopThinking("5");
 						} else {
 							JOptionPane.showMessageDialog(component,
 									"Player2 chose an invalid move, please debug!",
 									"Invalid Move", JOptionPane.ERROR_MESSAGE);
 							stopThinking("6");
 						}
 					}
 				}
 			}
 			
 		}
 		
 	}
 	public void mouseClicked(MouseEvent ev) {
 		//this.removeMouseListener(this);
 		final MouseEvent e = ev;
 		System.out.println("CLICK");
 		repaint();
 		

		
	   Thread t = new Thread(new DoGame(ev));
		if (!isThinking) t.start();
 		repaint();
 	}
 
 	// Not used methods from the interface of MouseListener
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	public void mouseExited(MouseEvent e) {
 	}
 
 	public void mousePressed(MouseEvent e) {
 	}
 
 	public void mouseReleased(MouseEvent e) {
 	}
 
 }
