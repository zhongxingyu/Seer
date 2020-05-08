 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.*;
 
 import javax.swing.JComponent;
 import javax.swing.JWindow;
 import javax.swing.SwingUtilities;
 
 public class MagnifierWindow extends JWindow {
     protected class Magnifier extends JComponent {
         final private Font font = Font.decode(null);
 
         final static private int BORDER_SIZE = 4;
 
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
 
             // figure out rectangle
             Point cP = SwingUtilities.convertPoint(this, getWidth() / 2,
                     getHeight() / 2, viewer);
             Rectangle rect = SwingUtilities.convertRectangle(this, getBounds(),
                     viewer);
 
             // get all components
             int half = getWidth() / 2;
 
             for (OneView ov : viewer.getViews()) {
                 if (ov.getBounds().intersects(rect)) {
                     Point p2 = SwingUtilities.convertPoint(viewer, cP, ov);
 
                     Image img = ov.getImage();
                     Point imgP = ov.getImagePoint(p2);
 
                     int sx = imgP.x - half;
                     int sy = imgP.y - half;
                     g.drawImage(img, -sx, -sy, null);
                 }
             }
 
             // draw label
             Component c = SwingUtilities.getDeepestComponentAt(viewer, cP.x,
                     cP.y);
             if (c instanceof OneView) {
                 OneView centerView = (OneView) c;
 
                 String label = centerView.getViewName();
                 g.setFont(font);
                 FontMetrics fm = g.getFontMetrics();
                 int sw = SwingUtilities.computeStringWidth(fm, label);
                 int sh = fm.getHeight();
                 int sx = half - sw / 2;
                 int sy = getHeight() - sh - BORDER_SIZE;
                 g.setColor(Color.BLACK);
                 g.fillRect(sx - 2, sy, sw + 4, sh);
                 g.setColor(Color.WHITE);
                 g.drawString(label, sx, sy + sh - fm.getDescent()
                         - fm.getLeading());
             }
 
             // draw border
             Graphics2D g2 = (Graphics2D) g;
             g2.setColor(Color.GRAY);
             g2.setStroke(new BasicStroke(BORDER_SIZE));
             g2.drawRect(BORDER_SIZE / 2, BORDER_SIZE / 2, getWidth()
                     - BORDER_SIZE, getHeight() - BORDER_SIZE);
 
             // draw center
             if (false) {
                 g2.setStroke(new BasicStroke());
                 g.setColor(Color.RED);
                 g.drawArc(half - 2, half - 2, 4, 4, 0, 360);
             }
         }
     }
 
     final protected CaseViewer viewer;
 
     public MagnifierWindow(CaseViewer viewer) {
         super();
         setBackground(Color.BLACK);
         getContentPane().setBackground(null);
         this.viewer = viewer;
 
         add(new Magnifier());
     }
 }
