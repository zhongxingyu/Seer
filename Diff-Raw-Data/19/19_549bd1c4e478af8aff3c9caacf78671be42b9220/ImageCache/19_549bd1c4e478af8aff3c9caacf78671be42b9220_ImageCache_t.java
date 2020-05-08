 package it.qubixic.component.dynamicImageLoader;
 
 import java.util.Vector;
 import javax.microedition.lcdui.Image;
 
 public class ImageCache {
     
     private static Vector cachedImages = new Vector() ;
     
     private ImageCache() {}
     
     /**
      * Adds an image to the cache
      * @param image 
      */
    public static void addImage(Image image) throws OutOfMemoryError {
         if (!cachedImages.contains(image)){
            try {
                cachedImages.addElement(image);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                clear();
            }
         }
     }
     
     /**
      * Adds a set of images to the cache
      * @param images the set of images to be 
      * added to the cache
      */
     public static void addImages(Vector images) {
         if (images != null && images.size() > 0) {
             for (int imagesCounter = 0 ; imagesCounter < images.size();
                     imagesCounter++) {
                try {
                    addImage((Image) images.elementAt(imagesCounter));
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    clear();
                    break ;
                }
             }
         }
     }
     
     /**
      * Clears the entire cache of stored images
      */
     public static void clear() {
         cachedImages.removeAllElements();
     }
     
     /**
      * removes an image from the cache
      * @param image 
      */
     public static void removeImage(Image image) {
         cachedImages.removeElement(image);
     }
     
     /**
      * removes a set of images from the cache
      * @param images 
      */
     public static void removeImages (Vector images) {
         if(images != null && images.size() > 0) {
             for (int imagesCounter = 0 ; imagesCounter < images.size();
                     imagesCounter++) {
                 removeImage((Image)images.elementAt(imagesCounter));
             }
         }
     }
 }
