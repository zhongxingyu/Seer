 package com.eagerlogic.cubee.client.style.styles;
 
 import com.google.gwt.dom.client.Element;
 
 /**
  *
  * @author dipacs
  */
 public enum ETextOverflow implements IStyle {
 
     CLIP("clip"),
     ELLIPSIS("ellipsis");
 
     private final String css;
 
     private ETextOverflow(String css) {
         this.css = css;
     }
 
     public final String toCSS() {
         return css;
     }
 
     @Override
     public void apply(Element element) {
    	element.getStyle().setProperty("textOverflow", css);
     }
 
 }
