 package cryptocast.client.filechooser;
 
 import java.nio.file.Path;
 
 /**
  * A list element in our file chooser, representing an element on the file system.
  */
 public interface ListElement {
     /**
      * @return the path of the element
      */
    public Path getPath();
 
     /**
      * @return The icon associated with this element
      */
    public Resource getIcon();
 }
