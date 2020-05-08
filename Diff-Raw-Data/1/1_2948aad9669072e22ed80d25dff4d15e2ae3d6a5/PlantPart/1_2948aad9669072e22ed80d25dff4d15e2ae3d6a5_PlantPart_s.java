 package de.hswt.hrm.plant.ui;
 
 import java.net.URL;
 
 import javax.annotation.PostConstruct;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.swt.widgets.Composite;
 
 public class PlantPart {
 
     @PostConstruct
     public void postConstruct(Composite parent) {
         URL url = PlantPart.class.getClassLoader().getResource(
                 "de/hswt/hrm/plant/ui/xwt/PlantView" + IConstants.XWT_EXTENSION_SUFFIX);
         try {
 
             // Obtain root element of the XWT file
             XWT.load(parent, url);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 
     }
   
 }
