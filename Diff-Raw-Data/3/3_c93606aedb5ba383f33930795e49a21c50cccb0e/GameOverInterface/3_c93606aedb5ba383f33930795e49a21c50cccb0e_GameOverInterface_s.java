 package cichlid.seprphase3.GUIInterface;
 
 /**
  *
  * @author Tomasz
  *
  */
import java.awt.event.*;
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 
 public class GameOverInterface extends JPanel implements MouseListener {
 
     Animation explosion;
     Button playAgainButton;
     Button leaveButton;
     public Boolean block = true;
 
     GameOverInterface() {
         explosion = new Animation("animations/explosion");
         playAgainButton = new Button("Play Again");
         leaveButton = new Button("Leave Game");
         playAgainButton.setLocation(200, 500);
         leaveButton.setLocation(200, 600);
         this.addMouseListener(this);
     }
 
     @Override
     public void paintComponent(Graphics g) {
         g.drawImage(explosion.stepImage(), 0, 0, null);
         g.setColor(Color.RED);
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         //if (e.getSource().equals(playAgainButton)) {
         block = false;
         //}
 
         //if (e.getSource().equals(leaveButton)) {
         //    System.exit(0);
         //}
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 }
