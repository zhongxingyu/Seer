 package org.geworkbench.bison.datastructure.complex.panels;
 
 import org.geworkbench.bison.datastructure.properties.DSIdentifiable;
 import org.geworkbench.bison.datastructure.properties.DSNamed;
 
 import java.util.List;
 
 /**
  * Specifies a list of {@link DSNamed} objects, accessible by label.
  */
 public interface DSItemList <T extends DSNamed> extends List<T>, DSIdentifiable {
     /**
      * Gets an item by label.
      *
      * @param label the label of the requested object.
      * @return the requested object, or <code>null</code> if it was not found.
      */
     T get(String label);
 
 }
