 package net.soemirno.xmlvalidator;
 
 import java.io.File;
 
 /**
  * Main class responsible for starting application.
  */
 public final class App {
     private static XmlValidatorFactory validatorFactory;
 
     static {
         setDefaultFactory();
     }
 
     private App() {
     }
 
     public static void main(final String[] args) {
         validatorFactory.createValidator(new File(args[0])).validate(new File(args[1]));
     }
 
     static void setValidatorFactory(final XmlValidatorFactory factory) {
         validatorFactory = factory;
     }
 
     static void setDefaultFactory() {
         validatorFactory = new XmlValidatorFactory() {
            public XmlValidator createValidator(final File schema) {
                 return new DefaultXmlValidator(schema);
             }
         };
     }
 }
