 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.gui.equalizer;
 
 import java.util.EventObject;
 
 /**
  * The <code>EqualizerModelEvent</code> shall provide info about the nature of the {@link EqualizerModel} change.
  *
  * @author   martin.scholl@cismet.de
  * @version  1.0
  */
 public final class EqualizerModelEvent extends EventObject {
 
     //~ Instance fields --------------------------------------------------------
 
     private final int index;
     private final int oldValue;
     private final int newValue;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new EqualizerModelEvent object. Properties of this event will be set to defaults:<br/>
      * <br/>
      *
      * <ul>
      *   <li><code>index=-1</code></li>
      *   <li><code>oldValue=Integer.MIN_VALUE</code></li>
      *   <li><code>newValue=Integer.MIN_VALUE</code></li>
      * </ul>
      *
      * @param  source  the <code>EqualizerModel</code> that caused the event
      */
     public EqualizerModelEvent(final Object source) {
         super(source);
 
         this.index = -1;
         this.oldValue = Integer.MIN_VALUE;
         this.newValue = Integer.MIN_VALUE;
     }
 
     /**
      * Creates a new EqualizerModelEvent object.
      *
     * @param  source    the source of the event
     * @param  index     the index that changed
     * @param  oldValue  the value before the change at the given index
     * @param  newValue  the value after the change at the given index
      */
     public EqualizerModelEvent(final Object source, final int index, final int oldValue, final int newValue) {
         super(source);
 
         this.index = index;
         this.oldValue = oldValue;
         this.newValue = newValue;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Getter for the index that changed. A value of <code>-1</code> indicates that it is not known which index actually
      * changed and thus the value getters will not provide actual valid values.
      *
      * @return  the index that was changed or <code>-1</code> if the actual index is not known
      */
     public int getIndex() {
         return index;
     }
 
     /**
      * Getter for the value that was available at {@link #getIndex()} before the change.
      *
      * @return  the value that was available at <code>getIndex()</code> before the change.
      */
     public int getOldValue() {
         return oldValue;
     }
 
     /**
      * Getter for the value that is available at {@link #getIndex()} after the change.
      *
      * @return  the value that is available at <code>getIndex()</code> after the change.
      */
     public int getNewValue() {
         return newValue;
     }
 
     @Override
     public String toString() {
         return super.toString() + ":[index=" + index + "|oldValue=" + oldValue + "|newValue=" + newValue + "]"; // NOI18N
     }
 }
