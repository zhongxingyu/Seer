 package net.milanaleksic.guitransformer.providers.impl;
 
 import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
 
 import java.util.*;
 
 /**
  * User: Milan Aleksic
  * Date: 5/14/12
  * Time: 10:02 AM
  */
 public class SimpleResourceBundleProvider implements ResourceBundleProvider{
 
     private ResourceBundle resourceBundle = null;
 
     public SimpleResourceBundleProvider() {
         try {
             resourceBundle = ResourceBundle.getBundle("messages", new Locale("en"));//NON-NLS
         } catch (MissingResourceException exc) {
            System.err.println("WARNING: Could not find the messages resource bundle");
         }
     }
 
     @Override
     public ResourceBundle getResourceBundle() {
         return resourceBundle;
     }
 
 }
