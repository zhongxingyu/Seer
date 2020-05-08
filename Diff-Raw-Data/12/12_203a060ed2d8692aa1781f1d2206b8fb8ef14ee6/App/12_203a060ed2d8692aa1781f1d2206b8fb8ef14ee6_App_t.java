 package net.soemirno.xmlvalidator;
 
 import java.io.File;
 
 /**
  * Responsible for starting application.
  */
public abstract class App {
 
     private static XmlValidator validator;
 
     public static void main(String[] args) {
         validator.validate(new File(args[0]), new File(args[1]));
     }
 
    static void setValidator(XmlValidator aValidator) {
         validator = aValidator;
     }
 }
