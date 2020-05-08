 package pl.com.it_crowd.utils;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 /**
  * Class for scaling BigDecimal objects
  */
 public final class BigDecimalUtils {
 // -------------------------- STATIC METHODS --------------------------
 
     /**
      * Scale BigDecimal object to prefer scale. If scale is different than scale of object scale to default scale
      *
      * @param value BigDecimal object
      * @param scale value of prefer scale
      *
      * @return BigDecimal object with applay scale
      */
     public static BigDecimal scale(BigDecimal value, int scale)
     {
         if (value != null && value.scale() != scale) {
            return value.setScale(2, RoundingMode.HALF_UP);
         } else {
             return value;
         }
     }
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     private BigDecimalUtils()
     {
     }
 }
