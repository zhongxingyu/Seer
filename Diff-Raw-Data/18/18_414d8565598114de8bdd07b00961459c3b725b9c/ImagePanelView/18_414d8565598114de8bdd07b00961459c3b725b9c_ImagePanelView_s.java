 package src.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 import src.nonui.AppController;
 import src.utils.Point;
 
 /**
  * View for the image panel.
  */
 public class ImagePanelView extends JPanel implements MouseListener, MouseMotionListener {
     // JPanel is serializable, so we need some ID to avoid compiler warnings.
     private static final long serialVersionUID = 1L;
 
     // String to display when there is no image opened for editing.
     private static final String NO_IMAGE_STRING = "Please open an image for editing.";
 
     private final AppController controller;
 
     // Image that is being worked on.
     private BufferedImage image = null;
 
     public ImagePanelView(AppController appController) {
         this.controller = appController;
 
         Dimension panelSize = new Dimension(800, 600);
         setSize(panelSize);
         setMinimumSize(panelSize);
         setPreferredSize(panelSize);
         setMaximumSize(panelSize);
 
         setBorder(BorderFactory.createLineBorder(Color.black));
 
         addMouseListener(this);
         addMouseMotionListener(this);
 
         setVisible(true);
     }
 
     // TODO: This should probably not call into the controller. At any rate, it
     // needs tidied.
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         if (image == null) {
             FontMetrics fm = getFontMetrics(getFont());
             Rectangle2D textsize = fm.getStringBounds(NO_IMAGE_STRING, g);
             int textWidth = new Double(textsize.getWidth()).intValue();
             int textHeight = new Double(textsize.getHeight()).intValue();
 
             int xPos = (getWidth() - textWidth) / 2;
             int yPos = (getHeight() - textHeight) / 2 + fm.getAscent();
             g.setFont(new Font(getFont().getFamily(), getFont().getStyle(), 16));
             g.drawString(NO_IMAGE_STRING, xPos, yPos);
         } else {
             g.drawImage(image, 0, 0, null);
 
             Graphics2D graphics2D = (Graphics2D) g;
 
             List<List<Point>> completedPoints = controller.getCompletedPolygonsPoints();
             for (List<Point> points : completedPoints) {
                 drawPolygon(points, graphics2D, Color.BLUE);
                 drawLine(points.get(0), points.get(points.size() - 1), graphics2D, Color.BLUE);
             }
 
             List<List<Point>> selectedPoints = controller.getSelectedPolygonsPoints();
             for (List<Point> points : selectedPoints) {
                 drawPolygon(points, graphics2D, Color.GREEN);
                 drawLine(points.get(0), points.get(points.size() - 1), graphics2D, Color.GREEN);
             }
 
             List<Point> currentPoints = controller.getCurrentPolygonPoints();
             if (currentPoints != null) {
                 drawPolygon(currentPoints, graphics2D, Color.PINK);
             }
 
             List<Point> editedPoints = controller.getEditedPolygonPoints();
             if (editedPoints != null) {
                 drawPolygon(editedPoints, graphics2D, Color.YELLOW);
                 drawLine(editedPoints.get(0), editedPoints.get(editedPoints.size() - 1),
                         graphics2D, Color.YELLOW);
             }
         }
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         int x = e.getX();
         int y = e.getY();
 
         if (image == null || e.getButton() != MouseEvent.BUTTON1 || !withinImageBounds(x, y)) {
             return;
         }
 
         controller.imageMouseClick(x, y, e.getClickCount() == 2);
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         int x = e.getX();
         int y = e.getY();
 
         if (image == null || e.getButton() != MouseEvent.BUTTON1 || !withinImageBounds(x, y)) {
             return;
         }
 
         controller.imageMousePress(x, y);
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         controller.imageMouseReleased();
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         int x = e.getX();
         int y = e.getY();
 
         if (image == null) {
             return;
         }
 
         // Make sure that the drag-to point is within the image bounds.
         Math.max(0, Math.min(x, image.getWidth()));
         y = Math.max(0, Math.min(y, image.getHeight()));
 
         controller.imageMouseDrag(x, y);
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
     }
 
     /**
      * Sets the image that is to be rendered in the panel.
      * 
      * @param helpImage the image to draw
      */
     public void setImage(BufferedImage newImage) {
         image = newImage;
 
         if (image != null) {
             boolean scaled = false;
             int width = image.getWidth();
             int height = image.getHeight();
 
             if (width > 800 || height > 600) {
                 width = (width > 800) ? 800 : ((width * 600) / height);
                 height = (height > 600) ? 600 : ((height * 800) / width);
             } else if (width < 800 || height < 600) {
                 width = (width < 800) ? 800 : ((width * height) / 600);
                 height = (height < 600) ? 600 : ((height * width) / 800);
             }
 
             if (scaled) {
                 Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_FAST);
                 image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                 image.getGraphics().drawImage(scaledImage, 0, 0, this);
             }
         }
 
         repaint();
     }
 
     /**
      * Checks that a point is within the bounds of the image.
      * 
      * @param x the x coordinate of the point to check
      * @param y the y coordinate of the point to check
      * 
      * @return true if the point is within the bounds of the image, false
      *         otherwise
      */
     private boolean withinImageBounds(int x, int y) {
         return x >= 0 && x <= image.getWidth() && y >= 0 && y <= image.getHeight();
     }
 
     /**
      * Draws an unfinished polygon (i.e. with no line between the last and first
      * vertices).
      * 
      * @param points the points of the polygon to be drawn
      * @param graphics2d the graphics pane to draw it on
      * @param colour the colour to make the lines
      */
     private static void drawPolygon(List<Point> points, Graphics2D graphics2d, Color colour) {
         Color originalColour = graphics2d.getColor();
         graphics2d.setColor(colour);
         for (int i = 0; i < points.size(); i++) {
             Point currentVertex = points.get(i);
             if (i != 0) {
                 Point prevVertex = points.get(i - 1);
                 drawLine(prevVertex, currentVertex, graphics2d, colour);
             }
             graphics2d.fillOval(currentVertex.getX() - 5, currentVertex.getY() - 5, 10, 10);
         }
         graphics2d.setColor(originalColour);
     }
 
     /**
      * Draws a line between two points.
      * 
      * @param p1 the first point
      * @param p2 the second point
      * @param graphics2d the graphics pane to draw it on
      * @param colour the colour to make the line
      */
     private static void drawLine(Point p1, Point p2, Graphics2D graphics2d, Color colour) {
         Color originalColour = graphics2d.getColor();
         graphics2d.setColor(colour);
         graphics2d.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
         graphics2d.setColor(originalColour);
     }
 }
