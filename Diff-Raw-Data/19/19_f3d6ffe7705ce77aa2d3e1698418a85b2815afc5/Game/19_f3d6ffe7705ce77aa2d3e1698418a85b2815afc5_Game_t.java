 import javax.swing.*;
 import java.awt.*;
 
 class Game implements Runnable {
 	
 	private Gui gui;
 	private Board gameBoard;
 	
 	public Game() {
 
 		gameBoard = new Board();
 		gui = new Gui(gameBoard);
 		// GUI setup
 		JFrame gameFrame = new JFrame("Yet-Another-Java-Tetris-Clone");
 		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Container content = gameFrame.getContentPane();
 		content.add(gui);
 
 		// Gravity Timer
 		Thread gt = new Thread(new GravityTimer(gameBoard));
 		gt.start();
 
 		// Input Listener Setup
 		InputListener l = new InputListener(gameBoard);
 		Thread lt = new Thread(l);
 		lt.start();
 		gameFrame.addKeyListener(l);
 
 		gameFrame.pack();
 		gameFrame.setVisible(true);
 		
 		// TODO gravity timer
 		// TODO scoreboard / thread that also handles lines.
 	}
 	
 	public void run() {
 
 		gameBoard.add();
 		while (true) {
 
			try { Thread.sleep(34); } catch(InterruptedException e) { }
 			gui.repaint();
 			Thread.yield();
 			// breakpoint
 		}
 	}
 }
