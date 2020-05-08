 package dominoes;
 
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: timothy
  * Date: 16/03/2013
  * Time: 19:27
  */
 public class BoneWidget extends Canvas {
     private Bone bone;
     private String playerType;
     private int size = 120;
     private boolean portraitOrientation = false;
     private boolean hidden = false;
 
     private Color background = Color.black;
 
     long entered = 0;
     long inComponent = 0;
 
     public BoneWidget(Bone bone, String playerType, int size) {
         this.bone = bone;
         this.playerType = playerType;
         this.size = size;
 
         // Orientation depends on playerType
         if (this.playerType == "dominoes.players.Player" || this.playerType == "dominoes.players.ComputerPlayer") {
             portraitOrientation = true;
             reshape(0, 0, size / 2, size);
             if (this.playerType == "dominoes.players.ComputerPlayer") {
                 hidden = true;
             }
         } else {
             reshape(0, 0, size, size / 2);
         }
     }
 
     public boolean mouseEnter(Event e, int x, int y) {
         entered = e.when;
         background = Color.red;
         repaint();
         return true;
     }
 
     public boolean mouseExit(Event e, int x, int y) {
         inComponent = e.when - entered;
         background = Color.black;
         repaint();
         return true;
     }
 
     public void paint(Graphics g) {
         drawBackground(g);
         drawForeground(g);
     }
 
     private void drawForeground(Graphics g) {
         if (hidden) {
             drawForegroundHidden(g);
         } else {
             drawPips(g);
         }
     }
 
     private void drawPips(Graphics g) {
         switch (bone.left()) {
             case 1: drawOnePip(g, false);   break;
             case 2: drawTwoPips(g, false);  break;
             case 3: drawThreePips(g, false);break;
             case 4: drawFourPips(g, false); break;
             case 5: drawFivePips(g, false); break;
             case 6: drawSixPips(g, false);  break;
         }
         switch (bone.right()) {
             case 1: drawOnePip(g, true);    break;
             case 2: drawTwoPips(g, true);   break;
             case 3: drawThreePips(g, true); break;
             case 4: drawFourPips(g, true);  break;
             case 5: drawFivePips(g, true);  break;
             case 6: drawSixPips(g, true);   break;
         }
     }
 
     private void drawForegroundHidden(Graphics g) {
         g.setColor(Color.white);
         int xpoints[] = {size / 4, size * 3 / 8, size / 4, size / 8};
         int ypoints[] = {size * 3 / 8, size / 2, size * 5 / 8, size / 2};
         g.fillPolygon(xpoints, ypoints, 4);
     }
 
     private void drawBackground(Graphics g) {
         g.setColor(background);
         if (portraitOrientation) {
             g.fillRoundRect(0, 0, size / 2, size, size / 20, size / 20);
         } else {
             g.fillRoundRect(0, 0, size, size / 2, size / 20, size / 20);
         }
         g.setColor(Color.white);
         if (portraitOrientation) {
             g.drawLine(0, size / 2, size / 2, size / 2);
         } else {
             g.drawLine(0 + size / 2, 0, size / 2, size / 2);
         }
     }
 
     private Dimension getOffset(boolean offset) {
         if (offset) {
             if (portraitOrientation) {
                 return new Dimension(0, size / 2);
             } else {
                 return new Dimension(size / 2, 0);
             }
         } else {
             return new Dimension(0, 0);
         }
     }
 
     private void drawOnePip(Graphics graphics, boolean offset) {
         graphics.setColor(Color.green);
         Dimension off = getOffset(offset);
         graphics.fillOval(off.width + 25, off.height + 25, size / 12, size / 12);
     }
 
     private void drawTwoPips(Graphics graphics, boolean offset) {
         graphics.setColor(Color.blue);
         Dimension off = getOffset(offset);
         if (portraitOrientation) {
             graphics.fillOval(off.width + 40, off.height + 10, size / 12, size / 12);
             graphics.fillOval(off.width + 10, off.height + 40, size / 12, size / 12);
         } else {
             graphics.fillOval(off.width + 10, off.height + 10, size / 12, size / 12);
             graphics.fillOval(off.width + 40, off.height + 40, size / 12, size / 12);
         }
     }
 
     private void drawThreePips(Graphics graphics, boolean offset) {
         graphics.setColor(Color.pink);
         Dimension off = getOffset(offset);
         graphics.fillOval(off.width + 25, off.height + 25, size / 12, size / 12);
         if (portraitOrientation) {
             graphics.fillOval(off.width + 40, off.height + 10, size / 12, size / 12);
             graphics.fillOval(off.width + 10, off.height + 40, size / 12, size / 12);
         } else {
             graphics.fillOval(off.width + 10, off.height + 10, size / 12, size / 12);
             graphics.fillOval(off.width + 40, off.height + 40, size / 12, size / 12);
         }
     }
 
     private void drawFourPips(Graphics graphics, boolean offset) {
         graphics.setColor(Color.red);
         Dimension off = getOffset(offset);
         graphics.fillOval(off.width + 10, off.height + 10, size / 12, size / 12);
         graphics.fillOval(off.width + 40, off.height + 10, size / 12, size / 12);
         graphics.fillOval(off.width + 10, off.height + 40, size / 12, size / 12);
         graphics.fillOval(off.width + 40, off.height + 40, size / 12, size / 12);
     }
 
     private void drawFivePips(Graphics graphics, boolean offset) {
         graphics.setColor(Color.cyan);
         Dimension off = getOffset(offset);
         graphics.fillOval(off.width + 25, off.height + 25, size / 12, size / 12);
         graphics.fillOval(off.width + 10, off.height + 10, size / 12, size / 12);
         graphics.fillOval(off.width + 40, off.height + 10, size / 12, size / 12);
         graphics.fillOval(off.width + 10, off.height + 40, size / 12, size / 12);
         graphics.fillOval(off.width + 40, off.height + 40, size / 12, size / 12);
     }
 
     private void drawSixPips(Graphics graphics, boolean offset) {
         graphics.setColor(Color.yellow);
         Dimension off = getOffset(offset);
         graphics.fillOval(off.width + 10, off.height + 10, size / 12, size / 12);
         graphics.fillOval(off.width + 40, off.height + 10, size / 12, size / 12);
         graphics.fillOval(off.width + 10, off.height + 40, size / 12, size / 12);
         graphics.fillOval(off.width + 40, off.height + 40, size / 12, size / 12);
         if (portraitOrientation) {
             graphics.fillOval(off.width + 10, off.height + 25, size / 12, size / 12);
             graphics.fillOval(off.width + 40, off.height + 25, size / 12, size / 12);
         } else {
             graphics.fillOval(off.width + 25, off.height + 10, size / 12, size / 12);
             graphics.fillOval(off.width + 25, off.height + 40, size / 12, size / 12);
         }
     }
 }
