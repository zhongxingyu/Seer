 package vooga.rts.gui.menus;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.util.Observable;
 import vooga.rts.gui.Menu;
 import vooga.rts.networking.communications.ExpandedLobbyInfo;
import vooga.rts.util.Scale;
 
 public class SetupMenu extends Menu {
 
     ExpandedLobbyInfo myLobbyInfo;
     
     public SetupMenu () {
         super();
 
 
     }
 
     @Override
     public void paint (Graphics2D pen) {
 
         super.paint(pen);
         pen.setFont(new Font("Helvetica", Font.BOLD, 50));
         pen.setColor(Color.white);
         pen.drawString("Click the game to start!", 150, 150);
 
     }
     
     public void handleMouseDown (int x, int y) {
         setChanged();
         notifyObservers();
     }
     
     @Override
     public void update(Observable o, Object a) {
         setChanged();
         notifyObservers();
     }
 
     public void setLobbyInfo (ExpandedLobbyInfo e) {
         myLobbyInfo = e;
     }
 }
