 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.gui.equalizer;
 
 /**
  * Basic equalizer model interface that ensures that all necessary values can be accessed and altered.
  *
  * @author   martin.scholl@cismet.de
  * @version  1.0
  */
 public interface EqualizerModel {
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Getter for the range this equalizer model covers (boundaries included). Shall never change.
      *
      * @return  the immutable <code>Range</code> this model covers
      */
     Range getRange();
 
     /**
     * Getter for the category name at the specified index.
      *
      * @param   index  the category index
      *
      * @return  the name of the category at the specified index
      */
     String getEqualizerCategory(final int index);
 
     /**
      * Getter for the total amount of available categories.
      *
      * @return  the total amount of available categories
      */
     int getEqualizerCategoryCount();
 
     /**
     * Getter for the current value at the specified index.
      *
      * @param   index  the category index
      *
      * @return  the current value at the specified index
      */
     int getValueAt(final int index);
 
     /**
      * Setter for the a new value at the specified index. The value has to be within the <code>Range</code> of this
     * model. Otherwise an {@link IllegalArgumentException} shall be thrown.
      *
      * @param  index  the category index
      * @param  value  the new value
      */
     void setValueAt(final int index, final int value);
 
     /**
      * Adds a new <code>EqualizerModelListener</code> to this model. Any registered listener will be informed of model
      * changes. If a listener has already been added any subsequent calls with the same instance shall be ignore. This
      * means that any listener shall only be called once when a change happens. If <code>null</code> is passed nothing
      * shall be done.
      *
      * @param  eml  the <code>EqualizerModelListener</code> to add
      */
     void addEqualizerModelListener(final EqualizerModelListener eml);
 
     /**
      * Remove the specified <code>EqualizerModelListener</code>. It will not be informed of changes anymore. If there is
      * no such listener or if <code>null</code> is passed nothing shall be done.
      *
      * @param  eml  the <code>EqualizerModelListener</code> to remove
      */
     void removeEqualizerModelListener(final EqualizerModelListener eml);
 }
