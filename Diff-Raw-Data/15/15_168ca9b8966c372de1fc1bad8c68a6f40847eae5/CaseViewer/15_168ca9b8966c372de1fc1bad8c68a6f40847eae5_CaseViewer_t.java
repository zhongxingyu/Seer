 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.Cursor;
 import java.awt.Point;
import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 import javax.swing.SwingUtilities;
 
 public class CaseViewer extends JPanel {
     private final static int MAGNIFIER_SIZE = 512;
 
     private final static int SPACING = 10;
 
     private Case theCase;
 
     final private OneView views[] = new OneView[4];
 
    static final Cursor hiddenCursor = Toolkit.getDefaultToolkit()
            .createCustomCursor(
                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                    new Point(), "Hidden Cursor");
 
     final protected MagnifierWindow magnifierWindow;
 
     final private SpringLayout layout = new SpringLayout();
 
     final protected Cursor defaultCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
 
     public CaseViewer() {
         super();
 
         setBackground(null);
 
         setCursor(defaultCursor);
 
         magnifierWindow = new MagnifierWindow(this);
         magnifierWindow.setSize(MAGNIFIER_SIZE, MAGNIFIER_SIZE);
 
         setLayout(layout);
 
         addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 if (e.getButton() == 2) {
                     updateMagnifierPosition(e);
                     setCursor(hiddenCursor);
                     magnifierWindow.setVisible(true);
                 }
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 if (e.getButton() == 2) {
                     setCursor(defaultCursor);
                     magnifierWindow.setVisible(false);
                 }
             }
         });
 
         addMouseMotionListener(new MouseMotionAdapter() {
             @Override
             public void mouseDragged(MouseEvent e) {
                 if (magnifierWindow.isVisible()) {
                     updateMagnifierPosition(e);
                 }
             }
         });
     }
 
     public void setCase(Case c) {
         theCase = c;
 
         views[0] = new OneView(theCase.getRightCC(), "RCC", theCase
                 .getMaximumHeight());
         views[1] = new OneView(theCase.getLeftCC(), "LCC", theCase
                 .getMaximumHeight());
         views[2] = new OneView(theCase.getRightML(), "RML", theCase
                 .getMaximumHeight());
         views[3] = new OneView(theCase.getLeftML(), "LML", theCase
                 .getMaximumHeight());
 
         removeAll();
         add(views[0]);
         add(views[1]);
         add(views[2]);
         add(views[3]);
 
         // add layout constraints
         layout.putConstraint(SpringLayout.WEST, views[0], 1, SpringLayout.WEST,
                 this);
         layout.putConstraint(SpringLayout.WEST, views[1], SPACING,
                 SpringLayout.EAST, views[0]);
         layout.putConstraint(SpringLayout.WEST, views[2], SPACING,
                 SpringLayout.EAST, views[1]);
         layout.putConstraint(SpringLayout.WEST, views[3], SPACING,
                 SpringLayout.EAST, views[2]);
         layout.putConstraint(SpringLayout.EAST, this, 1, SpringLayout.EAST,
                 views[3]);
 
         for (OneView v : views) {
             layout.putConstraint(SpringLayout.NORTH, v, 1, SpringLayout.NORTH,
                     this);
             layout.putConstraint(SpringLayout.SOUTH, v, 1, SpringLayout.SOUTH,
                     this);
         }
 
         revalidate();
         repaint();
 
         magnifierWindow.repaint();
     }
 
     protected void updateMagnifierPosition(MouseEvent e) {
         Point p = new Point(e.getX(), e.getY());
         SwingUtilities.convertPointToScreen(p, e.getComponent());
         magnifierWindow.setLocation(p.x - MAGNIFIER_SIZE / 2, p.y
                 - MAGNIFIER_SIZE / 2);
         magnifierWindow.repaint();
     }
 
     public OneView[] getViews() {
         return views;
     }
 }
