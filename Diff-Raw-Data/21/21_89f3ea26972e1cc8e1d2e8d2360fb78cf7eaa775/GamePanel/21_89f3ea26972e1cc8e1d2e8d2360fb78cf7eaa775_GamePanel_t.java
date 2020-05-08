 package gui;
 
 import java.awt.Graphics;
 
 public class GamePanel extends CardPanel {
 
 	//Fields
 		public final static String ID = "GAME_PANEL";
 		
 	//Constructors
 		public GamePanel(MainPanel mainPanel) {
 			super(GamePanel.ID,mainPanel);
			
			this.addKeyListener(Main.inputHandler);
			this.requestFocus();
 		}
 			
 	//Methods
 		public void paintComponent(Graphics g){
 			long initTime = System.nanoTime();		
 			
 			g.fillRect(0, 0, 200, 300);
 			
 			while (System.nanoTime() - initTime < 1000000000 / 60) {			}
 			this.repaint();
 		}
 }
