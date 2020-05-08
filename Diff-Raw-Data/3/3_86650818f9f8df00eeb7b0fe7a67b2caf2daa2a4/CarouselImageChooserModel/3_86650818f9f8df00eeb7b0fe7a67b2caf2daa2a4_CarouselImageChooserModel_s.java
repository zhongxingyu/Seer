 package ch9k.plugins.carousel;
 
 import ch9k.chat.event.ConversationEventFilter;
 import ch9k.core.Model;
 import ch9k.core.settings.Settings;
 import ch9k.core.settings.SettingsChangeEvent;
 import ch9k.core.settings.SettingsChangeListener;
 import ch9k.core.settings.SettingsChangeListener;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import ch9k.plugins.ProvidedImage;
 import ch9k.plugins.event.NewProvidedImageEvent;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashSet;
 import java.util.Set;
 import javax.swing.Timer;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Class representing the image chooser data.
  */
 public class CarouselImageChooserModel extends Model
         implements EventListener, SettingsChangeListener {
     /**
      * The selection model.
      */
     private CarouselImageModel model;
 
     /**
      * The plugin settings.
      */
     private Settings settings;
 
     /**
      * List to store the images.
      */
     private List<ProvidedImage> images;
 
     /**
      * We keep a set of images currently in the carousel, so we don't
      * accidentaly have two equal images.
      */
     private Set<ProvidedImage> imageSet;
 
     /**
      * Next selection.
      */
     int nextSelection;
 
     /**
      * Current selection.
      */
     double currentSelection;
 
     /**
      * Previous selection. We use a double because it can be halfway between
      * two selections.
      */
     double previousSelection;
 
     /**
      * Timer for animations.
      */
     private Timer timer;
 
     /**
      * Timer ticks passed.
      */
     private int ticks;
 
     /**
      * Constructor.
      * @param settins The plugin settings.
      * @param model The plugin selection model.
      */
     public CarouselImageChooserModel(Settings settings,
             CarouselImageModel model) {
         this.model = model;
         this.settings = settings;
         this.images = new ArrayList<ProvidedImage>();
         imageSet = new HashSet<ProvidedImage>();
         nextSelection = 0;
         currentSelection = 0.0;
         previousSelection = 0.0;
 
         /* Timer to update the animation. */
         timer = new Timer(50, new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 ticks++;
                 double diff = (double) nextSelection - previousSelection;
                 currentSelection = previousSelection +
                         (double) ticks * diff / 20.0;
                 if(ticks >= 20) {
                     currentSelection = (double) nextSelection;
                     timer.stop();
                 }
                 fireStateChanged();
             }
         });
 
         ticks = 0;
 
         /* Register as listener to receive new images. */
         EventFilter filter = new ConversationEventFilter(
                 NewProvidedImageEvent.class, model.getConversation());
         EventPool.getAppPool().addListener(this, filter);
 
         /* Listen to settings changes. */
         settings.addSettingsListener(this);
     }
 
     /**
      * Disable the plugin.
      */
     public void disablePlugin() {
         EventPool.getAppPool().removeListener(this);
     }
 
     /**
      * Obtain an image by index.
      * @param index Index of the image to obtain.
      * @return The requested image.
      */
     public ProvidedImage getProvidedImage(int index) {
         if(index < 0 || index >= images.size()) {
             return null;
         } else {
             return images.get(index);
         }
     }
 
     /**
      * Obtain an image by index.
      * @param index Index of the image to obtain.
      * @return The requested image.
      */
     public Image getImage(int index) {
         ProvidedImage image = getProvidedImage(index);
         if(image == null) {
             return null;
         } else {
             return image.getImage();
         }
     }
 
     /**
      * Set the next selection.
      * @param nextSelection The next selection.
      */
     public void setNextSelection(int nextSelection) {
         if(this.nextSelection != nextSelection &&
                 nextSelection >= 0 && nextSelection < images.size()) {
             previousSelection = currentSelection;
             this.nextSelection = nextSelection;
 
             /* Start timer for animation. */
             timer.stop();
             ticks = 0;
             timer.start();
         }
     }
 
     /**
      * Obtain the next selection.
      * @return The next selection index.
      */
     public int getNextSelection() {
         return nextSelection;
     }
 
     /**
      * Obtain the current selection.
      * @return The current selection index.
      */
     public double getCurrentSelection() {
         return currentSelection;
     }
 
     /**
      * Add a new provided image.
      * @param image Image to add.
      */
     private void addImage(ProvidedImage image) {
         synchronized(this) {
             /* Return if we have the image already. */
             if(imageSet.contains(image)) return;
 
             /* Reject foobar images. */
             if(image.getImage() == null) return;
 
             /* If we already have enough images, we need to remove the old image
              * from the set. */
             if(images.size() >=
                     settings.getInt(CarouselPreferencePane.MAX_IMAGES)) {
                 ProvidedImage old = images.get(0);
                 imageSet.remove(old);
                 images.remove(0);
             }
 
             /* Insert the new image. */
             images.add(image);
             imageSet.add(image);
 
             /* Update positions. */
             fireStateChanged();
         }
     }
 
     /**
      * Resize the images array.
      * @param size New size.
      */
     private void resizeImagesArray(int size) {
         synchronized(this) {
             List<ProvidedImage> old = images;
             images = new ArrayList<ProvidedImage>();
             for(int i = 0; i < size && i < old.size(); i++) {
                 images.add(old.get(i));
             }
 
             if(nextSelection >= size) {
                 setNextSelection(size - 1);
             } else {
                 fireStateChanged();
             }
         }
     }
 
     @Override
     public void handleEvent(Event e) {
         NewProvidedImageEvent event = (NewProvidedImageEvent) e;
         final ProvidedImage image = event.getProvidedImage();
         new Thread(new Runnable() {
             public void run() {
                 image.ensureLoaded();
                 addImage(image);
             }
         }).start();
     }
 
     @Override
     public void settingsChanged(SettingsChangeEvent changeEvent) {
         resizeImagesArray(Integer.parseInt(changeEvent.getValue()));
     }
 }
