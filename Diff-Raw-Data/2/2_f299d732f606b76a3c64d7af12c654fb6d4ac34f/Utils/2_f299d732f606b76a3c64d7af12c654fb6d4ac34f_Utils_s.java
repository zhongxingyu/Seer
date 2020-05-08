 package se.helino.mjc.backends.jvm;
 
 import se.helino.mjc.parser.MJType;
 import se.helino.mjc.parser.MJIntType;
 import se.helino.mjc.parser.MJIntArrayType;
 import se.helino.mjc.parser.MJBooleanType;
 import se.helino.mjc.parser.MJIdentifierType;
 
 public class Utils {
     private static int labelCount;
 
     public static String convertType(MJType t) {
         if(t instanceof MJIntType)
             return "I";
         if(t instanceof MJIntArrayType)
             return "[I";
         if(t instanceof MJBooleanType)
            return "B";
         if(t instanceof MJIdentifierType)
             return "L" + t.toString() + ";";
         throw new IllegalStateException("Can't convert type " + t.toString());
     }
 
     public static String toStoreLoadPrefix(MJType t) {
         if(t instanceof MJIntType)
             return "i";
         if(t instanceof MJIntArrayType)
             return "a";
         if(t instanceof MJBooleanType)
             return "i";
         if(t instanceof MJIdentifierType)
             return "a";
         throw new IllegalStateException("Can't convert type " + t.toString());
     }
 
     public static String toTypePrefix(MJType t) {
         if(t instanceof MJIntType)
             return "i";
         if(t instanceof MJIntArrayType)
             return "a";
         if(t instanceof MJBooleanType)
             return "i";
         if(t instanceof MJIdentifierType)
             return "a";
         throw new IllegalStateException("Can't convert type " + t.toString());
     }
 
     public static String createLabel() {
         String label = "L" + labelCount;
         labelCount++;
         return label;
     }
 }
