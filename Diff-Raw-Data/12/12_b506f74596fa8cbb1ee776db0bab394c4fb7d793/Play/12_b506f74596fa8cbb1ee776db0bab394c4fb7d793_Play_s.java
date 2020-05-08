 import java.util.*;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 
 public class Play extends JFrame implements ActionListener, MouseMotionListener, MouseListener{
 
 	Board playBoard;
 
 	String difficulty;
 
 	boolean gameOver = false;
 
 	public static void main(String[] args) {
 
 		JFrame secret = new JFrame();
 
 		JFrame newGameWindow = new JFrame("MINESWEEPER: CHOOSE A DIFFICULTY");
 		
 		
		Play game = new Play("easy");
 		game.playBoard.initializeBoard();
 		
 		//JFrame window = new JFrame(""+game.getDifficulty());
 		
 
 	}
 
 	public Play(String difficulty){
 
 		this.difficulty = difficulty;
 
 		if(difficulty.contains("easy")){
 			playBoard = new Board(8,8);
 		}
 
 		if(difficulty.contains("medium")){
 			playBoard = new Board(16,16);
 		}
 
 		if(difficulty.contains("hard")){
 			playBoard = new Board(30,16);
 		}
 		
 		Display d = new Display();
 		setTitle(""+this.getDifficulty()+"     Flag Count: "+this.playBoard.getFlagCount());
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.add(d);
 		this.setSize(30*this.playBoard.getWidth()+5,30*this.playBoard.getHeight()+25);
 		this.setVisible(true);
 		this.setResizable(false);
 
 		this.addMouseListener(this);
 
 	}
 
 	public String getDifficulty(){
 
 		return difficulty;
 	}
 
 	public boolean gameOver(){
 
 		if(playBoard.getWin() || playBoard.getLose())
 
 			gameOver = true;
 
 		return gameOver;
 	}
 
 	public class Display extends JPanel{
 
 		public void paintComponent(Graphics g){
 
 			super.paintComponent(g);
 			this.setBackground(Color.WHITE);
 
 			playBoard.paintBoard(g);
 
 
 		}
 
 	}
 
 	public void mouseClicked(MouseEvent e) {
 
 
 		//TODO
 		//after game over left click and new game starts
 
 		//TODO 
 		//make window popup asking if they want an easy, medium, or hard game --- closes --- opens game
 
 		if(!gameOver()){
 
 			int x = e.getX()/30;
 			int y = (e.getY()-30)/30;
 
 			if(SwingUtilities.isLeftMouseButton(e)){
 	
 					playBoard.open(x, y);
 				
 				if(gameOver())
 					gameOverTitle();
 			}
 			
 			else if(SwingUtilities.isRightMouseButton(e)){
 
 				playBoard.markFlagged(x, y);
 			}
 
 		}
 		if(!gameOver)
 			updateFlagTitle();
 		repaint();
 	}
 
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void updateFlagTitle(){
 
 		setTitle(""+getDifficulty()+"     Flag Count: "+playBoard.getFlagCount());
 
 	}
 
 	public void gameOverTitle(){
 
 		if(playBoard.getWin())
 
 			setTitle("GAME OVER! YOU WIN!");
 
 		else
 			setTitle("GAME OVER! YOU LOSE");
 
 	}
 
 }
 
 
