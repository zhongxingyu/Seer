 package org.wyona.yanel.impl.jelly;
 
 import org.wyona.yanel.core.api.attributes.creatable.AbstractResourceInputItem;
 
 
 public abstract class TextualInputItemSupport extends AbstractResourceInputItem {
     
     private String value;
     
     public TextualInputItemSupport(String name) {
         super(name);
     }
     
     public TextualInputItemSupport(String name, String value) {
         super(name);
         if(value != null && "".equals(value.trim())){
             value = null;
         }
         
         this.value = value;
     }
     
     public Object getValue() {
         return this.value;
     }
 
     /**
      * Set value
      */
     public void doSetValue(Object value) {
         if(value == null){
             this.value = null;
             return;
         }
         
        if (!(value instanceof String)) throw new IllegalArgumentException("Value of input item '" + name + "' is not a string: " + value.toString());
         
         if( "".equals(((String)value).trim())){
             value = null;
         }
         
         this.value = (String)value;
     }
 }
