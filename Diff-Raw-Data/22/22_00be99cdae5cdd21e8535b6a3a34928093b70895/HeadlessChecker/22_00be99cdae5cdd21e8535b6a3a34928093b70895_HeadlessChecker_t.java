 package org.jbehave.threaded.swing;
 
 import org.jbehave.core.exception.PendingException;
 
 public class HeadlessChecker {
 
    public void check() throws PendingException {
        if (isHeadless()) {
             throw new PendingException("Cannot verify behaviour when performing in headless mode.");
         }
     }
 
    public boolean isHeadless() {
        return "true".equals(System.getProperty("java.awt.headless"));
    }

 }
