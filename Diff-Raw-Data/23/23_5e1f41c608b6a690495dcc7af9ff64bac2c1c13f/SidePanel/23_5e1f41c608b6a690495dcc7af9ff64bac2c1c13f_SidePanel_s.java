 
 package gui;
 
 import game.GameState;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 /**
  *
  * @author Anthony Chin
  * 
  */
 public class SidePanel extends JPanel {
     private GameState gameState;
     public SidePanel(GameState gs){
         this.gameState = gs;
     }
    
     // Method to print the highscore, lives and level
     @Override
     public void paint (Graphics g){
         super.paint(g);
         
         g.drawString("Highscore " + gameState.getHighScore(), 5, 10);
         if(gameState.getPlayerShip() != null){
            g.drawString("Lives " + gameState.getPlayerShip().getLives(), 75, 10);
            g.drawString("x " + gameState.getPlayerShip().getX(), 150, 10);
            g.drawString("y " + gameState.getPlayerShip().getY(), 180, 10);
            g.drawString("heading " + gameState.getPlayerShip().getY(), 210, 10);            
         }
        else {
            g.drawString("Lives " + 0, 75, 10);    
        }
        g.drawString("Level " + gameState.getLevel(), 120, 10);
     }
 }
