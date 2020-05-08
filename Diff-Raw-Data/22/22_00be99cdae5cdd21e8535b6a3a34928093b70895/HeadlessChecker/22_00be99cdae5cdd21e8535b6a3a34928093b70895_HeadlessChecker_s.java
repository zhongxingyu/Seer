 package org.jbehave.threaded.swing;
 
 import org.jbehave.core.exception.PendingException;
 
 public class HeadlessChecker {
 
    public void check() {
        if ("true".equals(System.getProperty("java.awt.headless"))) {
             throw new PendingException("Cannot verify behaviour when performing in headless mode.");
         }
     }
 
 }
