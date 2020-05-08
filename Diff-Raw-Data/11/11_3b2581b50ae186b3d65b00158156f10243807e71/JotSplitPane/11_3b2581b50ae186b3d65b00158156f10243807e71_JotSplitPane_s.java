 /*
     this file is slightly modified code from
     http://pwnide.googlecode.com/svn/trunk/UICommon/widgets/CustomSplitPane.java
     and features original code by Neil Dickson from the Inventor IDE (http://pwnide.googlecode.com)
 */
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GradientPaint;
 import java.awt.Component;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Point;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 public class JotSplitPane extends JComponent implements MouseMotionListener, MouseListener {
     private Component left;
     private Component right;
 
     private JComponent divider;
     private int dividerX;
     private int dividerWidth;
 
     private Point dragStart;
 
     private boolean isHidden;
     private boolean isHover;
     private boolean isVertical;
 
     public static final Color HOVER_COLOUR    = new Color(27, 27, 27);
     public static final Color HOVER_COLOUR2   = new Color(112, 112, 112);
     public static final Color DIVIDER_COLOUR  = new Color(25, 25, 25);
     public static final Color DIVIDER_COLOUR2 = new Color(110, 110, 110);
 
     public JotSplitPane(Component left, Component right, int dividerX, int dividerWidth) {
         this(left, right, dividerX, dividerWidth, false);
     }
 
     public JotSplitPane(Component left, Component right, int dividerX, int dividerWidth, final boolean isVertical) {
         setLayout(null);
         this.left = left;
         this.right = right;
         this.isVertical = isVertical;
 
         divider = new JPanel() {
             protected void paintComponent(Graphics g) {
                 Graphics2D g2 = (Graphics2D) g;
                 if (isHover) {
                     g2.setPaint(new GradientPaint(0, 0, HOVER_COLOUR, isVertical ? 0 : getWidth(),
                         isVertical ? getHeight() : 0, HOVER_COLOUR2));
                 }
                 else {
                     g2.setPaint(new GradientPaint(0, 0, DIVIDER_COLOUR, isVertical ? 0 : getWidth(),
                         isVertical ? getHeight() : 0, DIVIDER_COLOUR2));
                 }
                 g2.fillRect(0, 0, getWidth(), getHeight());
             }
         };
 
         this.dividerX = dividerX;
         this.dividerWidth = dividerWidth;
         divider.addMouseMotionListener(this);
         divider.addMouseListener(this);
         divider.setCursor(new Cursor(isVertical ? Cursor.S_RESIZE_CURSOR : Cursor.W_RESIZE_CURSOR));
         dragStart = null;
         isHidden = false;
         add(left);
         add(divider);
         add(right);
     }
 
     @Deprecated
     public void reshape(int x, int y, int w, int h) {
         super.reshape(x, y, w, h);
         updateComponentSize();
     }
 
     private void updateComponentSize() {
         int tempX = isHidden ? 0 : dividerX;
         if (!isVertical) {
             left.setBounds(0, 0, tempX, getHeight());
             left.validate();
             left.repaint();
             divider.setBounds(tempX, 0, dividerWidth, getHeight());
             right.setBounds(tempX + dividerWidth, 0, getWidth() - tempX - dividerWidth, getHeight());
         } else {
             left.setBounds(0, 0, getWidth(), getHeight() - tempX - dividerWidth);
             left.validate();
             left.repaint();
             divider.setBounds(0, getHeight() - tempX - dividerWidth ,getWidth(), dividerWidth);
             right.setBounds(0, getHeight() - tempX, getWidth(), tempX);
         }
     }
 
     public void mouseDragged(MouseEvent e) {
         if (dragStart!=null) {
             if (isHidden) {
                 isHidden = false;
                 dividerX = 0;       // Erase saved location so that += will assign the value
             }
             if (!isVertical) {
                 dividerX += e.getPoint().x - dragStart.x;
             } else {
                 dividerX -= e.getPoint().y - dragStart.y;
             }
 
             // Clamp dividerX such that it can't go beyond the edges of this pane
             dividerX = Math.max(0, Math.min((isVertical ? getHeight() : getWidth()) - dividerWidth, dividerX));
             updateComponentSize();
         }
     }
 
     public void setRight(Component newRight) {
         remove(right);
         right = newRight;
         add(newRight);
         updateComponentSize();
     }
 
     public void setDividerX(int newDividerX) {
         dividerX = newDividerX;
         setHidden(false);
     }
 
     public int getDividerX() {
         return dividerX;
     }
 
     public void mouseMoved(MouseEvent e) {}
 
     public void mouseClicked(MouseEvent e) {
         if (e.getClickCount() == 2) {
             setHidden(!isHidden);
         }
     }
 
     public void mousePressed(MouseEvent e) {
         dragStart = e.getPoint();
     }
 
     public void mouseReleased(MouseEvent e) {
         dragStart = null;
     }
 
     public void mouseEntered(MouseEvent e) {
         isHover = true;
         divider.repaint();
     }
 
     public void mouseExited(MouseEvent e) {
         isHover = false;
         divider.repaint();
     }
 
     public void setHidden(boolean isHidden) {
         this.isHidden = isHidden;
         updateComponentSize();
     }
 }
