 package org.icemobile.samples.springbasic;
 
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 /**
  *
  */
 
@SessionAttributes("QRScanBean")
 public class QRScanBean {
 
     // Name of the parameter in demo is scanOne
     private String scanOne;
 
     public String getScanOne() {
         return scanOne;
     }
 
     public void setScanOne(String scanOne) {
         this.scanOne = scanOne;
     }
 }
