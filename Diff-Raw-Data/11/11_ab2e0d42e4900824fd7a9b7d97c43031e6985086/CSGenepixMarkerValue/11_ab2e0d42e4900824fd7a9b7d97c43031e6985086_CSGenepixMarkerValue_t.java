 package org.geworkbench.bison.datastructure.bioobjects.microarray;
 
 import org.geworkbench.engine.parsers.GenepixParseContext;
 import org.geworkbench.engine.parsers.NCIParseContext;
 
 import java.io.ObjectStreamField;
 import java.io.Serializable;
 import java.util.HashMap;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust Inc.</p>
  * @author First Genetic Trust Inc.
  * @version 1.0
  */
 
 /**
  * Implementation of {@link org.geworkbench.engine.model.microarray.GenepixMarkerValue
  * GenepixMarkerValue}.
  */
 public class CSGenepixMarkerValue extends CSMarkerValue implements DSGenepixMarkerValue, Serializable {
     double ch1f = 0d;
     double ch2f = 0d;
     double ch1b = 0d;
     double ch2b = 0d;
     double ratio = 0d;
     boolean missing = false;
     /**
      * Serializable fields.
      */
     private final static ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("ch1f", double.class), new ObjectStreamField("ch2f", double.class), new ObjectStreamField("ch1b", double.class), new ObjectStreamField("ch2b", double.class), new ObjectStreamField("ratio", double.class)};
 
     public CSGenepixMarkerValue(double val) {
 
     }
 
     public CSGenepixMarkerValue(CSGenepixMarkerValue gmvi) {
         ch1f = gmvi.ch1f;
         ch2f = gmvi.ch2f;
         ch1b = gmvi.ch1b;
         ch2b = gmvi.ch2b;
         ratio = gmvi.ratio;
         setValue(gmvi.value);
         setConfidence(gmvi.confidence);
         setMissing(gmvi.isMissing());
     }
 
     /**
      * Constructs a <code>GenepixMarkerValue</code> object from the contents of
      * the <code>GenepixParseContext</code> argument.
      *
      * @param context The parse context used for the initialization.
      */
     public CSGenepixMarkerValue(GenepixParseContext context) {
         HashMap columns = context.getColumnsToUse();
         Object value = null;
         if (columns.containsKey("ID")) {
             value = columns.get("ID");
             //      if (value instanceof String)
             //      this.markerInfo = new MarkerInfoImpl((String)value);
         }
 
         // Notice that the order in which we treat median and mean values implies
         // that if both median and mean measurements are available, only the
         // latter will be used.
         if (columns.containsKey("F532 Median")) {
             value = columns.get("F532 Median");
             if (value instanceof Double)
                 this.ch1f = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("B532 Median")) {
             value = columns.get("B532 Median");
             if (value instanceof Double)
                 this.ch1b = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("F635 Median")) {
             value = columns.get("F635 Median");
             if (value instanceof Double)
                 this.ch2f = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("B635 Median")) {
             value = columns.get("B635 Median");
             if (value instanceof Double)
                 this.ch2b = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("F532 Mean")) {
             value = columns.get("F532 Mean");
             if (value instanceof Double)
                 this.ch1f = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("B532 Mean")) {
             value = columns.get("B532 Mean");
             if (value instanceof Double)
                 this.ch1b = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("F635 Mean")) {
             value = columns.get("F635 Mean");
             if (value instanceof Double)
                 this.ch2f = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("B635 Mean")) {
             value = columns.get("B635 Mean");
             if (value instanceof Double)
                 this.ch2b = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("Ratio of Means")) {
             value = columns.get("Ratio of Means");
             if (value instanceof Double)
                 this.ratio = ((Double) value).doubleValue();
         }
 
         computeSignal();
     }
 
     /**
      * Constructs a <code>GenepixMarkerValue</code> object from the contents of
      * the <code>NCIParseContext</code> argument.
      *
      * @param context The parse context used for the initialization.
      */
     public CSGenepixMarkerValue(org.geworkbench.engine.parsers.NCIParseContext context) {
         HashMap columns = context.getColumnsToUse();
         Object value = null;
         if (columns.containsKey("ID")) {
             value = columns.get("ID");
             //      if (value instanceof String)
             //      this.markerInfo = new MarkerInfoImpl((String)value);
         }
 
         // Notice that the order in which we treat median and mean values implies
         // that if both median and mean measurements are available, only the
         // latter will be used.
         if (columns.containsKey("F532 Median")) {
             value = columns.get("F532 Median");
             if (value instanceof Double)
                 this.ch1f = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("B532 Median")) {
             value = columns.get("B532 Median");
             if (value instanceof Double)
                 this.ch1b = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("F635 Median")) {
             value = columns.get("F635 Median");
             if (value instanceof Double)
                 this.ch2f = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("B635 Median")) {
             value = columns.get("B635 Median");
             if (value instanceof Double)
                 this.ch2b = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("F532 Mean")) {
             value = columns.get("F532 Mean");
             if (value instanceof Double)
                 this.ch1f = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("B532 Mean")) {
             value = columns.get("B532 Mean");
             if (value instanceof Double)
                 this.ch1b = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("F635 Mean")) {
             value = columns.get("F635 Mean");
             if (value instanceof Double)
                 this.ch2f = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("B635 Mean")) {
             value = columns.get("B635 Mean");
             if (value instanceof Double)
                 this.ch2b = ((Double) value).doubleValue();
         }
 
         if (columns.containsKey("Ratio of Means")) {
             value = columns.get("Ratio of Means");
             if (value instanceof Double)
                 this.ratio = ((Double) value).doubleValue();
         }
 
         computeSignal();
     }
 
     /**
      * Sets the Signal Value at 635 nm wavelength
      *
      * @param ch1f Signal
      */
     public void setCh1Fg(double ch1f) {
         this.ch1f = ch1f;
         computeSignal(); // Update the signal value.
     }
 
     /**
      * Sets the Background Value at 635 nm wavelength
      *
      * @param ch1b Background
      */
     public void setCh1Bg(double ch1b) {
         this.ch1b = ch1b;
         computeSignal(); // Update the signal value.
     }
 
     /**
      * Sets the Signal Value at 532 nm wavelength
      *
      * @param ch2f Signal
      */
     public void setCh2Fg(double ch2f) {
         this.ch2f = ch2f;
         computeSignal(); // Update the signal value.
     }
 
     /**
      * Sets the Background Value at 532 nm wavelength
      *
      * @param ch2b Background
      */
     public void setCh2Bg(double ch2b) {
         this.ch2b = ch2b;
         computeSignal(); // Update the signal value.
     }
 
     /**
      * Gets the Signal Value at 635 nm wavelength
      *
      * @return Signal Value
      */
     public double getCh1Fg() {
         return ch1f;
     }
 
     /**
      * Gets the Background Value at 635 nm wavelength
      *
      * @return Background Value
      */
     public double getCh1Bg() {
         return ch1b;
     }
 
     /**
      * Gets the Signal Value at 532 nm wavelength
      *
      * @return Signal Value
      */
     public double getCh2Fg() {
         return ch2f;
     }
 
     /**
      * Gets the Background Value at 532 nm wavelength
      *
      * @return Background Value
      */
     public double getCh2Bg() {
         return ch2b;
     }
 
     public double getValue() {
         return value;
     }
 
     public double getConfidence() {
         return confidence;
     }
 
     public void setConfidence(double confidence) {
         super.setConfidence(confidence);
     }
 
     public boolean isMissing() {
         return missing;
     }
 
     public void setMissing(boolean isMissing) {
         missing = isMissing;
     }
 
     public DSMarkerValue deepCopy() {
        return new CSGenepixMarkerValue(this);
     }
 
     /**
      * Calculate the signal value from the channel values.
      */
     private void computeSignal() {
         if (ch2f != ch2b)
             this.setValue((ch1f - ch1b) / (ch2f - ch2b));
         else
             this.setValue(ch1f - ch1b);
         setMissing(false);
     }
 
     /**
      * This method returns the dimensionality of the marker. E.g., Genotype markers are 2-dimensional
      * while Allele/Haplotype markers are 1-dimensional
      *
      * @return int the dimensionality of the marker.
      */
     public int getDimensionality() {
         return 1;
     }
 
     public int compareTo(Object o) {
         return Double.compare(((CSGenepixMarkerValue) o).getValue(), getValue());
     }
 }
