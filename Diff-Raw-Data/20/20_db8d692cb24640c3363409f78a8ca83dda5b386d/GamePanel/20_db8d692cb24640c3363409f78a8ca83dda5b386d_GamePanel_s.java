 package com.hack.balls.swing;
 
 import java.awt.Graphics;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.hack.balls.engine.PhysicsEngine;
 import com.hack.balls.model.Ball;
 
 public class GamePanel extends JPanel{
 
 	private JLabel scoreLabel;
 	private PhysicsEngine physicsEngine;
 	
 	public GamePanel(MainFrame mainFrame) {
 		
 		scoreLabel = new JLabel("Score: 2");
 		add(scoreLabel);
 		physicsEngine = new PhysicsEngine(MainFrame.X_SIZE, MainFrame.Y_SIZE);
 		
 		Thread physicsThread = new Thread(physicsEngine);
 		physicsThread.start();
 		
 		Runnable gameRun = new Runnable() {
 			public void run() {
 				while(true) {
					repaint();
 				}
 			}
 		};
 		Thread gameThread = new Thread(gameRun);
		gameThread.start();
 		
 	}
 	
	public void paint(Graphics g) {
 		System.out.println("Paiting");
 		System.out.println("cool");
 		System.out.println(physicsEngine.getBalls());
 		for( Ball ball : physicsEngine.getBalls()) {
			
			g.drawOval(ball.getX(), ball.getY(), ball.getRadius() *2, ball.getRadius() *2);
 		}
 		
		
 	}
 	
 	
 	
 	
 }
