 package ch9k.plugins.carousel;
 
 import ch9k.chat.Conversation;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import ch9k.plugins.NewProvidedImageEvent;
import ch9k.plugins.ProvidedImage;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
 import javax.swing.JPanel;
 
 /**
  * Panel in which the user can select an image.
  */
 public class CarouselImageChooserPanel extends JPanel implements EventListener {
     /**
      * The conversation.
      */
     private Conversation conversation;
 
     /**
      * The selection model.
      */
     private CarouselImageModel model;
 
     /**
      * The image items.
      */
     private CarouselImageChooserItem[] images;
 
     /**
      * Columns in the grid.
      */
     private final static int COLUMNS = 6;
 
     /**
      * Index of where to add the next image.
      */
     private int nextImageIndex;
 
     /**
     * We keep a set of images currently in the carousel, so we don't
     * accidentaly have two equal images.
     */
    private Set<ProvidedImage> imageSet;

    /**
      * Constructor.
      * @param conversation The conversation.
      * @param model The selection model of the plugin.
      */
     public CarouselImageChooserPanel(Conversation conversation,
             CarouselImageModel model) {
         super(new GridLayout(0, COLUMNS));
         this.conversation = conversation;
         this.model = model;
 
         setBackground(new Color(50, 50, 50));
 
         Dimension labelSize = new Dimension(100, 100);
         images = new CarouselImageChooserItem[COLUMNS * 6];
         for(int i = 0; i < images.length; i++ ) {
             images[i] = new CarouselImageChooserItem();
             images[i].setPreferredSize(labelSize);
             add(images[i]);
         }
 
         nextImageIndex = 0;
 
        imageSet = new HashSet<ProvidedImage>();

         EventFilter filter = new EventFilter(NewProvidedImageEvent.class);
         EventPool.getAppPool().addListener(this, filter);
     }
 
     /**
      * Disable the plugin.
      */
     public void disablePlugin() {
         EventPool.getAppPool().removeListener(this);
     }
 
     @Override
     public void handleEvent(Event e) {
         /* Return if the event is not relevant. */
         final NewProvidedImageEvent event = (NewProvidedImageEvent) e;
         if(conversation != event.getConversation()) return;
 
        /* Return if we have the image already. */
        ProvidedImage image = event.getProvidedImage();
        if(imageSet.contains(image)) return;

        /* We need to remove the old image from the set. */
        ProvidedImage old = images[nextImageIndex].getProvidedImage();
        if(old != null) imageSet.remove(old);

         /* Set the new image. */
        images[nextImageIndex].setProvidedImage(image);
        imageSet.add(image);
 
         /* Increment the image index. */
         nextImageIndex = (nextImageIndex + 1) % images.length;
     }
 
     private class CarouselImageChooserItem extends ImagePanel {
         public CarouselImageChooserItem() {
             super(false, true);
             addMouseListener(this);
         }
 
         @Override
         public void mouseClicked(MouseEvent event) {
             model.setProvidedImage(getProvidedImage());
         }
     }
 }
