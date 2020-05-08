 package see.functions.bool;
 
 import java.math.BigDecimal;
 
 public class BooleanCastHelper {
     public static boolean toBoolean(Number value) {
        return value.intValue() != 0;
     }
 
     public static BigDecimal fromBoolean(boolean value) {
         return value ? BigDecimal.ONE : BigDecimal.ZERO;
     }
 }
