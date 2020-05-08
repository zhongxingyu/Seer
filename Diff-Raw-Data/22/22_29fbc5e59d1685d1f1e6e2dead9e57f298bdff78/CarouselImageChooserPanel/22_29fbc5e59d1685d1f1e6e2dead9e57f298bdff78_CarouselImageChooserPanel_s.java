 package ch9k.plugins.carousel;
 
 import ch9k.chat.event.ConversationEventFilter;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import ch9k.plugins.ProvidedImage;
 import ch9k.plugins.event.NewProvidedImageEvent;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.Image;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.HashSet;
 import java.util.Set;
 import javax.swing.JPanel;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import javax.imageio.ImageIO;
 import java.io.IOException;
 import java.awt.geom.AffineTransform;
 
 /**
  * Panel in which the user can select an image.
  */
 public class CarouselImageChooserPanel extends JPanel
         implements ChangeListener, MouseWheelListener, MouseListener {
     /**
      * Number of images visible. MUST BE ODD.
      */
     private static final int NUM_IMAGES = 5;
 
     /**
      * Spacing between images (in pixels).
      */
     private static final double SPACING = 10.0;
 
     /**
      * The selection model.
      */
     private CarouselImageModel model;
 
     /**
      * The chooser model.
      */
     private CarouselImageChooserModel chooserModel;
 
     /**
      * Gradient for reflections.
      */
     private static Image gradient = null;
     
     static {
         try {
             gradient = ImageIO.read(
                     CarouselImageChooserPanel.class.getResource(
                             "/ch9k/plugins/carousel/gradient.png"));
         } catch (IOException exception) {
             // Ignore.
         }
     }
 
     /**
      * Constructor.
      * @param model The selection model of the plugin.
      */
     public CarouselImageChooserPanel(CarouselImageModel model) {
         super();
         this.model = model;
         this.chooserModel = new CarouselImageChooserModel(model); 
 
         setBackground(new Color(50, 50, 50));
 
         chooserModel.addChangeListener(this);
         addMouseWheelListener(this);
         addMouseListener(this);
     }
 
     /**
      * Disable the plugin.
      */
     public void disablePlugin() {
         chooserModel.disablePlugin();
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D graphics = (Graphics2D) g;
 
         /* Do not forget to draw two extra images. */
         int index = (int) chooserModel.getCurrentSelection();
         double offset = (double) index - chooserModel.getCurrentSelection();
         for(int i = -1; i <= NUM_IMAGES; i++) {
             Image image = chooserModel.getImage(index + i - NUM_IMAGES / 2);
             drawImage(graphics, image, i, offset);
         }
     }
 
     private void drawImage(Graphics2D graphics, Image image, int index,
             double offset) {
         if(image == null) {
             return;
         }
 
         double width = (double) getWidth();
         double height = (double) getHeight();
 
         double imageMaxWidth = width / (double) NUM_IMAGES;
         double imageMaxHeight = height * 0.6;
         double x = imageMaxWidth * ((double) index + offset + 0.5);
 
         /* Find out the aspect ratio of the image. */
         double imageAspect =
                 (double) image.getWidth(null) / image.getHeight(null);
 
         /* Scale by width. */
         double imageWidth = imageMaxWidth - 2 * SPACING;
         double imageHeight = imageWidth / imageAspect;
 
         /* We're wrong, scale by imageHeight. */
         if(imageHeight > imageMaxHeight) {
             imageHeight = imageMaxHeight;
             imageWidth = imageAspect * imageHeight;
         }
 
         /* Build a transformation for the image. */
         AffineTransform transform = new AffineTransform();
         transform.translate((int) x, height * 0.7);
         transform.scale(imageWidth / (double) image.getWidth(null),
                 imageHeight / (double) image.getHeight(null));
         transform.translate(- (double) image.getWidth(null) * 0.5,
                 -image.getHeight(null));
         graphics.drawImage(image, transform, null);
 
         /* Scale and transform. */
         transform.translate(0.0, image.getHeight(null) * 1.6);
         transform.scale(1.0, - 0.6);
         graphics.drawImage(image, transform, null);
 
         /* Scale back to original format. */
         transform.scale((double) image.getWidth(null) / imageWidth,
                 (double) image.getHeight(null) / imageHeight);
 
         /* Scale to gradient format. */
         transform.scale(imageWidth / (double) gradient.getWidth(null),
                 imageHeight / (double) gradient.getHeight(null));
         graphics.drawImage(gradient, transform, null);
     }
 
     @Override
     public void stateChanged(ChangeEvent event) {
         repaint();
     }
 
     @Override
     public void mouseWheelMoved(MouseWheelEvent event) {
         if(event.getWheelRotation() > 0) {
             chooserModel.setNextSelection(chooserModel.getNextSelection() + 1);
         } else {
             chooserModel.setNextSelection(chooserModel.getNextSelection() - 1);
         }
     }
 
     @Override
     public void mouseClicked(MouseEvent event) {
         double width = (double) getWidth();
         double imageWidth = width / (double) NUM_IMAGES;
 
         boolean found = false;
 
         int i = 1;
         while(i <= NUM_IMAGES && !found) {
             if((double) event.getX() < (double) i * imageWidth) {
                 /* Determine the index of the next selection. Rely on the fact
                  * that NUM_IMAGES is odd. */
                 int index = chooserModel.getNextSelection() +
                         i - NUM_IMAGES / 2 - 1;
                 chooserModel.setNextSelection(index);
                 model.setProvidedImage(chooserModel.getProvidedImage(index));
                 found = true;
             }
             i++;
         }
     }
 
     @Override
     public void mouseEntered(MouseEvent event) {
     }
 
     @Override
     public void mouseExited(MouseEvent event) {
     }
 
     @Override
     public void mousePressed(MouseEvent event) {
     }
 
     @Override
     public void mouseReleased(MouseEvent event) {
     }
 }
