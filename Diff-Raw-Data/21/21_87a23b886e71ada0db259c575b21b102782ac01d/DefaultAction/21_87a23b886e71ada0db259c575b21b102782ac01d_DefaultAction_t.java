 /**
  *
  */
 package com.github.jwadamson.gravatar;
 
 import org.apache.commons.lang.StringUtils;
 
 public enum DefaultAction {
 
     //*****************************************************************************
     // CLASS
     //*****************************************************************************
 
     IMAGE(""),
     DO_404("404"),
     MM,
     IDENTICON,
     MONSTERID,
     WAVATAR,
     RETRO;
 
     //*****************************************************************************
     // INSTANCE
     //*****************************************************************************
 
     private final String value;
 
     /**
     * Default Action constructor with value equal to the variable name.
      */
     private DefaultAction() {
         this(null);
     }
 
     /**
     * Default Action constructor with explicit value.
      */
     private DefaultAction(String value) {
         this.value = value;
     }
 
     /**
     * The value for the default action parameter of the Uri
      * @return the value
      */
     public String getValue() {
         return StringUtils.defaultString(value, name());
     }
 }
