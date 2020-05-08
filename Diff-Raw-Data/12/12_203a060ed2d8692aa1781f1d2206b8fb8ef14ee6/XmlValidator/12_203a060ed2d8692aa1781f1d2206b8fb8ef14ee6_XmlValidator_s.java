 package net.soemirno.xmlvalidator;
 
 import java.io.File;
 
 /**
  * Validates xml files based upon schema.
  */
 public interface XmlValidator {
    public void validate(File schema, File xml);
 }
