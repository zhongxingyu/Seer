 package jDistsim.utils.resource;
 
import jDistsim.utils.logging.Logger;

import javax.imageio.ImageIO;
 import java.awt.*;
import java.io.IOException;
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
        try {
            return ImageIO.read(url);
        } catch (IOException exception) {
            Logger.log(exception);
            return null;
        }
     }
 }
