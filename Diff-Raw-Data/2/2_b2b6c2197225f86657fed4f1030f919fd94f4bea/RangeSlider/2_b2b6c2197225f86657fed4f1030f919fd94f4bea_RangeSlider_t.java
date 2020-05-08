 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui;
 
 import javax.swing.JSlider;
 import javax.swing.UIManager;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 
 /**
  * An extension of JSlider to select a range of values using two thumb controls. The thumb controls are used to select
  * the lower and upper value of a range with predetermined minimum and maximum values.
  *
  * <p>Note that RangeSlider makes use of the default BoundedRangeModel, which supports an inner range defined by a value
  * and an extent. The upper value returned by RangeSlider is simply the lower value plus the extent.</p>
  *
 * See https://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/ for more details.
 *
  * @version  $Revision$, $Date$
  */
 public class RangeSlider extends JSlider {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Constructs a RangeSlider with default minimum and maximum values of 0 and 100.
      */
     public RangeSlider() {
         initSlider();
     }
 
     /**
      * Constructs a RangeSlider with the specified default minimum and maximum values.
      *
      * @param  min  The minimum range value.
      * @param  max  The maximum range value.
      */
     public RangeSlider(final int min, final int max) {
         super(min, max);
         initSlider();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Initializes the slider by setting default properties.
      */
     private void initSlider() {
         setOrientation(HORIZONTAL);
     }
 
     /**
      * Overrides the superclass method to install the UI delegate to draw two thumbs.
      */
     @Override
     public void updateUI() {
         // Is it a hack or a feature? Metal UI seems to be incompatible with Netbean's Matisse. So let's ask if
         // Metal-LAF is used, if not, use normal LAF.
 
         if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel) {
             setUI(new MetalRangeSliderUI());
         } else {
             setUI(new RangeSliderUI(this));
         }
 
         // Update UI for slider labels. This must be called after updating the UI of the slider.
         // Refer to JSlider.updateUI().
         updateLabelUIs();
     }
 
     /**
      * Returns the lower value in the range.
      *
      * @return  The value of the lower knob.
      */
     @Override
     public int getValue() {
         return super.getValue();
     }
 
     /**
      * Sets the lower value in the range.
      *
      * @param  value  The new value of the lower knob.
      */
     @Override
     public void setValue(final int value) {
         final int oldValue = getValue();
         if (oldValue == value) {
             return;
         }
 
         // Compute new value and extent to maintain upper value.
         final int oldExtent = getExtent();
         final int newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
         final int newExtent = oldExtent + oldValue - newValue;
 
         // Set new value and extent, and fire a single change event.
         getModel().setRangeProperties(newValue, newExtent, getMinimum(),
             getMaximum(), getValueIsAdjusting());
     }
 
     /**
      * Returns the upper value in the range.
      *
      * @return  The value of the upper knob.
      */
     public int getUpperValue() {
         return getValue() + getExtent();
     }
 
     /**
      * Sets the upper value in the range.
      *
      * @param  value  The new value of the upper knob.
      */
     public void setUpperValue(final int value) {
         // Compute new extent.
         final int lowerValue = getValue();
         final int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);
 
         // Set extent to set upper value.
         setExtent(newExtent);
     }
 }
