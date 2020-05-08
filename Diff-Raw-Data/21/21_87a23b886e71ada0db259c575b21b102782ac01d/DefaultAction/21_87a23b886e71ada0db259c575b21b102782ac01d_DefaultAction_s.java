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
     *
      */
     private DefaultAction() {
         this(null);
     }
 
     /**
     *
      */
     private DefaultAction(String value) {
         this.value = value;
     }
 
     /**
      * @return the value
      */
     public String getValue() {
         return StringUtils.defaultString(value, name());
     }
 }
