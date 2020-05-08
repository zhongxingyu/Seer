 package jDistsim.utils.resource;
 
 import java.awt.*;
 import java.net.URL;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 26.9.12
  * Time: 14:47
  */
 public class Resources {
 
     private static Resources resource = new Resources();
     private static final String resourceRootDirectory = "/jDistsim/resources/";
 
     public static Image getImage(String resourceName) {
         URL url = resource.getClass().getResource(resourceRootDirectory + resourceName);
        return Toolkit.getDefaultToolkit().getImage(url);
     }
 }
