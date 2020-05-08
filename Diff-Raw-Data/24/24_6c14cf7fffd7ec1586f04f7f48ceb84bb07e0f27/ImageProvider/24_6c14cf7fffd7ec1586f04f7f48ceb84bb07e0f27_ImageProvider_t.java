 package superkidsapplication.providers;
 
 import javax.swing.*;
 
 /**
  * The <code>ImageProvider</code> allows access to image icons through the use
  * of a key.
  *
  * @author Ben Duong
  */
 public interface ImageProvider {
 
     /**
      * Get an image icon.
      *
      * @param key the key of the image
      * @return The ImageIcon, null if not found.
      */
     public ImageIcon getImage(String key);
    
    /**
     * Get a list of all image keys.
     *
     * @return The list of keys.
     */
    public List<String> getAllKeys();
 
     /**
      * Refresh the provider's database of images.
      */
     public void refresh();
 }
