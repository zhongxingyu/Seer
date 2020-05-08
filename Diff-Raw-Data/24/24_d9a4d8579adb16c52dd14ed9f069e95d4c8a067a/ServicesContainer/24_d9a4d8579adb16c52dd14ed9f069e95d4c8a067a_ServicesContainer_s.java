 package app.gui.svgComponents;
 
 import app.gui.svgComponents.displayobjects.ObjectService;
 import java.awt.Component;
 import java.awt.geom.AffineTransform;
 
 /**
  *
  * @author wara
  */
 public class ServicesContainer extends SynchronizedSVGLayer{
 
     public ServicesContainer(Canvas can){
         super(can);
     }
 
     @Override
     public void updateComponent() {
 
         AffineTransform at = getTransform();
         if( !(at.equals(new AffineTransform())) ){
             Component [] comps = getComponents();            
             for (Component c : comps) {
                 ((ObjectService)c).transformCoordinate(at);
             }
             System.out.println(getClass().getCanonicalName()+" updateComponentsCoordinates component count "+comps.length);
         }
         needUpdate = false;
     }
 }
