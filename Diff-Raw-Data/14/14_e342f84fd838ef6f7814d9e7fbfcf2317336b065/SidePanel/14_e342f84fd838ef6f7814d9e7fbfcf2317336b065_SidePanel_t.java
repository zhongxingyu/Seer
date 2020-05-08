 
 package gui;
 
 import game.GameState;
import game.PlayerShip;
 import java.awt.Color;
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
         g.setColor(Color.red);
         g.drawString("Current Score " + gameState.getHighScore(), 5, 10);
         g.drawString("Level " + gameState.getLevel(), 95, 10);
         
        PlayerShip player = gameState.getPlayerShip();
        if(player != null){
            g.drawString("Lives " + player.getLives(), 140, 10);
            g.drawString("x " + player.getX(), 180, 10);
            g.drawString("y " + player.getY(), 210, 10);
            g.drawString("heading " + Math.abs(player.getHeading())%360, 250, 10);
            g.drawString("Shields: " + player.getShieldPoints(), 330, 10);
         }
         // reset the graphics color to black
         g.setColor(Color.black);
     }
 }
