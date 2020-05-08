 package gui;
 
 import game.Updater;
 
 import java.awt.Graphics;
 
 public class GamePanel extends CardPanel {
 
 	//Fields
 		public final static String ID = "GAME_PANEL";
 		
 		private Updater updater;
 		
 	//Constructors
 		public GamePanel(MainPanel mainPanel, Updater u) {
 			super(GamePanel.ID,mainPanel);
 			this.updater = u;
 			this.addKeyListener(Main.inputHandler);
 			this.requestFocus();
 		}
 			
 	//Methods
 		public void paintComponent(Graphics g){
			this.requestFocus();
 			long initTime = System.nanoTime();		
 			
 			this.updater.update();
 			g.drawImage(this.updater.render(),0,0,null);
 			
 			while (System.nanoTime() - initTime < 1000000000 / 60) {			}
 			this.repaint();
 		}
 }
