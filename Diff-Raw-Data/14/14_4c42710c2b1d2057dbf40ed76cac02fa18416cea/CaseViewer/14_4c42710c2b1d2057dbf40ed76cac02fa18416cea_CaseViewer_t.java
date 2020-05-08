 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 
 import javax.swing.Box;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import edu.cmu.cs.diamond.opendiamond.Util;
 
 public class CaseViewer extends JPanel {
     private final static int MAGNIFIER_SIZE = 512;
 
     private final static int SPACING = 10;
 
     private Case theCase;
 
     final private OneView views[] = new OneView[4];
 
    final Cursor hiddenCursor = getToolkit().createCustomCursor(
            new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY),
            new Point(), "Hidden Cursor");
 
     final private Box hBox = Box.createHorizontalBox();
 
     final protected MagnifierWindow magnifierWindow;
 
     private double scale;
 
     final protected Cursor defaultCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
 
     public CaseViewer() {
         super();
 
         setBackground(null);
 
         setCursor(defaultCursor);
 
         magnifierWindow = new MagnifierWindow(this);
         magnifierWindow.setSize(MAGNIFIER_SIZE, MAGNIFIER_SIZE);
 
         setMinimumSize(new Dimension(800, 600));
         setPreferredSize(new Dimension(800, 600));
 
         setLayout(new BorderLayout());
 
         add(hBox);
 
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
 
         addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e) {
                 updateScale();
             }
         });
     }
 
     protected void updateScale() {
         int width = 0;
         int height = 0;
 
         for (OneView view : views) {
             BufferedImage img = view.getImage();
             width += img.getWidth();
             height = Math.max(img.getHeight(), height);
         }
 
         Insets in = getInsets();
 
         scale = Util.getScaleForResize(width, height, getWidth() - in.left
                 - in.right - (SPACING * (views.length - 1)), getHeight()
                 - in.top - in.bottom);
 
         for (OneView view : views) {
             view.setScale(scale);
         }
 
         repaint();
     }
 
     public void setCase(Case c) {
         theCase = c;
 
         views[0] = new OneView(theCase.getRightCC(), "RCC");
         views[1] = new OneView(theCase.getLeftCC(), "LCC");
         views[2] = new OneView(theCase.getRightML(), "RML");
         views[3] = new OneView(theCase.getLeftML(), "LML");
 
        updateScale();

         hBox.removeAll();
         hBox.add(views[0]);
         hBox.add(Box.createHorizontalStrut(SPACING));
         hBox.add(views[1]);
         hBox.add(Box.createHorizontalStrut(SPACING));
         hBox.add(views[2]);
         hBox.add(Box.createHorizontalStrut(SPACING));
         hBox.add(views[3]);
 
        hBox.revalidate();
 
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
 
     public double getScale() {
         return scale;
     }
 }
