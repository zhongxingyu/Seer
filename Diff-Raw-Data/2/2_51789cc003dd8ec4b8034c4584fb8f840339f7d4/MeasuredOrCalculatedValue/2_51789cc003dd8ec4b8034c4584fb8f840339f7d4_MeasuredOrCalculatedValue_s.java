 package blh.core.uncategorized;
 
 import blh.core.formulas.Formula;
 
 /**
  *
  * @author thinner
  */
 public class MeasuredOrCalculatedValue<T> {
 
     private T value;
     private Formula<T> formula;
     private FullContext context;
     private boolean isMeasured;
 
     public MeasuredOrCalculatedValue(T value) {
         if (value == null) {
             throw new NullPointerException("Cannot instantiate with null value, please use the constructor with Formula<T> and FullContext");
         }
         this.value = value;
         this.isMeasured = true;
     }
 
     public MeasuredOrCalculatedValue(Formula<T> formula, FullContext context) {
         this.formula = formula;
         this.context = context;
 
         this.value = null;
         this.isMeasured = false;
     }
 
    public boolean isIsMeasured() {
         return isMeasured;
     }
 
     public T value() {
         if (value == null) {
             value = formula.calc(context);
         }
         return value;
     }
     
     public void setMeasuredValue(T value) {
         if (value == null) {
             throw new NullPointerException("Cannot set measured value to null");
         }
         
         this.formula = null;
         this.context = null;
         this.value = value;
     }
 }
