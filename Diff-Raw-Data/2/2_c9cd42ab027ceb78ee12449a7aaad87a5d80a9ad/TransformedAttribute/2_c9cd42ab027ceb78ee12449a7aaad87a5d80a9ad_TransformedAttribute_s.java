 package org.wandledi.spells;
 
 import org.xml.sax.Attributes;
 import org.wandledi.Attribute;
 
 /**
  *
  * @author Markus Kahl
  */
 public class TransformedAttribute {
 
     private String name;
     private StringTransformation transformation;
 
     public TransformedAttribute(String name, StringTransformation transformation) {
         this.name = name;
         this.transformation = transformation;
     }
 
    /**Tries to perform the transformation to a regular Attrbute.
      *
      * @param attributes The attribute set of which one is to be transformed.
      * @return An Attribute or null if no corresponding Attribute to transform could be found.
      */
     public Attribute toAttribute(Attributes attributes) {
         String value = attributes.getValue(name);
         if (value != null) {
             return new Attribute(name, transformation.transform(attributes.getValue(name)));
         } else {
             return null;
         }
     }
 }
