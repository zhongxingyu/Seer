 package nl.mdlware.confluence.plugins.citation;
 
 /**
  * Description for the class Validator:
  * <p/>
  * Example usage:
  * <p/>
  * <pre>
  *
  * </pre>
  *
  * @author mdkr
  * @version Copyright (c) 2012 HAN University, All rights reserved.
  */
 final class Validator {
    private Validator() {
    }

     public static boolean isSet(Object field) {
         return field != null && !"".equals(field);
     }
 }
