 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bedit.gaming.wormstats.resourcebundle;
 
 import java.util.Locale;
 import java.util.ResourceBundle;
 import javax.faces.context.FacesContext;
 
 /**
  *
  * @author benjamin
  */
 public class ResourceBundleUtils {
 
    public static String getResourceBundleString(String messageBundleName,
            String key) {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         Locale locale = facesContext.getViewRoot().getLocale();
         ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName,
                 locale);
         return bundle.getString(key);
     }
 }
