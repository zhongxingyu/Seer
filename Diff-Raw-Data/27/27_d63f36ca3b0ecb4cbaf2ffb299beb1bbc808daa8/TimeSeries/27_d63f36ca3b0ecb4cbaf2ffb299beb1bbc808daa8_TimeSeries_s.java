 package org.codinglabs.timeseries_lib.entities;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import lombok.Getter;
 import lombok.SneakyThrows;
 import lombok.extern.log4j.Log4j;
 
 import org.codinglabs.timeseries_lib.exceptions.TSLOutOfRangeException;
 
 /**
  * Time series class
  * 
  * @author ericzhang
  */
 @Log4j
 public class TimeSeries {
 
     /**
      * Memorandum
      * 
     * Cache result of computation.
      */
     protected Map<String, Double> memo;
 
     /**
      * Time points
      */
     protected double[] points;
 
     /**
      * Number of points
      */
     @Getter
     protected int number;
 
     /**
      * Constructor
      * 
      * The default number is 128
      */
     public TimeSeries() {
         this(128);
     }
 
     /**
      * Constructor
      * 
      * @param _number
      *            Number of points
      */
     public TimeSeries(int _number) {
         if (_number <= 0) {
             this.number = 128;
 
             log.warn("Number of points must greater than zero，use the default value 128");
         } else {
             this.number = _number;
         }
         this.points = new double[this.number];
 
         this.memo = new HashMap<String, Double>();
     }
 
     /**
      * Set time point value
      * 
      * @param index
      *            Point index
      * @param value
      *            Time point value
      */
     @SneakyThrows
     public void setTimePoint(int index, double value) {
         if (index < 0 || index >= this.number) {
             throw new TSLOutOfRangeException();
         }
 
         this.points[index] = value;
 
         if (this.memo.size() > 0) {
             this.memo.clear();
         }
     }
 
     /**
      * Get average
      * 
      * @return Average
      */
     public double getAverage() {
         if (!this.memo.containsKey("avg")) {
             double total = 0D;
 
             for (double tp : this.points) {
                 total += tp;
             }
 
             this.memo.put("avg", total / this.number);
         }
 
         return this.memo.get("avg");
     }
 
     /**
      * Get variance
      * 
      * @return Variance
      */
     public double getVariance() {
         if (!this.memo.containsKey("var")) {
             double avg = this.getAverage();
             double total = 0D;
 
             for (double tp : this.points) {
                 total += (tp - avg) * (tp - avg);
             }
 
             this.memo.put("var", total / this.number);
         }
 
         return this.memo.get("var");
     }
 
     /**
      * Get auto-covariance
      * 
     * This function use average of all points to computer auto-covariance, the
     * result may be not match its math mathematical definition.
     * 
      * @param k
      *            Lag
      * @return Auto-covariance
      */
     @SneakyThrows
     public double getAutocovariance(int k) {
         if (!this.memo.containsKey("acov" + k)) {
             if (k < 0 || k >= this.number) {
                 throw new TSLOutOfRangeException();
             }
 
             if (k == 0) {
                 return this.getVariance();
             }
 
             double total = 0D;
             double avg = this.getAverage();
             for (int i = k; i < this.number; i++) {
                 total += (this.points[i - k] - avg) * (this.points[i] - avg);
             }
 
            this.memo.put("acov" + k, total / (this.number - k));
         }
 
         return this.memo.get("acov" + k);
     }
 
     /**
      * Get auto-correlation
      * 
      * @param k
      *            Lag
      * @return Auto-correlation
      */
     @SneakyThrows
     public double getAutocorrelation(int k) {
         if (!this.memo.containsKey("acor" + k)) {
            this.memo
                    .put("acor" + k,
                            this.getAutocovariance(k)
                                    / (this.getVariance() * this.number / (this.number - k)));
         }
 
         return this.memo.get("acor" + k);
     }
 
 }
