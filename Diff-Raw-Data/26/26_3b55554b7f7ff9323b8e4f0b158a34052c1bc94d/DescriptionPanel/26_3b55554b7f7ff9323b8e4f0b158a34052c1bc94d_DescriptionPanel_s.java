 package gui;
 
 import game.GameState;
 import game.PlayerShip;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 
 /**
  * The
  * <code>DescriptionPanel</code> class prints game information in top left
  * corner. It prints information such as score, lives, levels, etc.
  *
  * @author Haisin Yip
  * @author Anthony Chin
  *
  */
 //prints game information in top left corner - score, lives, level, etc.
 public class DescriptionPanel extends JPanel {
 
     private GameState gameState;
 
     /**
      * Creates DescriptionPanel with given GameState.
      *
      * @param gs current game state
      */
     public DescriptionPanel(GameState gs) {
         this.gameState = gs;
     }
 
     // Method that actually prints the current score, lives and level
     /**
      * Prints the current score, lives and level.
      *
      * @param g graphic that needs to be painted
      */
     @Override
     public void paint(Graphics g) {
         super.paint(g);

         //set colors for each player differently
         if (gameState.isPlayerTwoTurn()) {
            g.setColor(Color.blue);
         } else {
            g.setColor(Color.red);
         }
 
         String stringInfo;
         stringInfo = "Current Score: " + gameState.getCurrentScore() + "    Level: " + gameState.getLevel();

         //want to make sure player is not null - need to store variable, as player may become null in gameState while printing
         PlayerShip player = gameState.getPlayerShip();
         if (player != null) {
             stringInfo = stringInfo + "    Lives " + player.getLives() + "    Bombs: " + player.getBomb() + "     Shields: " + player.getShieldPoints() + "    X: " + player.getX() + "    Y: " + player.getY() + "    Heading: " + Math.abs(player.getHeading()) % 360;
         }
        g.drawString(stringInfo, 5, 10);
         // reset the graphics color to black
        g.setColor(Color.black);
     }
 }
