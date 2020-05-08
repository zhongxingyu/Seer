 package org.anodyneos.sfs;
 
 import org.xml.sax.SAXException;
 
 public class SfsException extends SAXException {
 
    //public SfsException() {
    //}
 
     public SfsException(Exception e) {
         super(e);
     }
 
     public SfsException(String msg) {
         super(msg);
     }
 
     public SfsException(String msg, Exception e) {
         super(msg, e);
     }
 
 }
