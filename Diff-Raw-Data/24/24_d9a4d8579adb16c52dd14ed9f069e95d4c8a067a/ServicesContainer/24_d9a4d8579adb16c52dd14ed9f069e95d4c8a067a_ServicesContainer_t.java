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

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        
        if(comp instanceof ObjectService){
            if(checkDuplcateComponet((ObjectService)comp)){
                return;
            }
        }
        super.addImpl(comp, constraints, index);
    }

    private boolean checkDuplcateComponet(ObjectService os){
        boolean isDuplcate = false;
        Component [] comps = getComponents();
        for (Component childs : comps) {
            ObjectService childObjectServ = (ObjectService)childs;
            if(os.getOID().equals(childObjectServ.getOID())){
                isDuplcate = true;
                break;
            }
        }
        return isDuplcate;
    }
 }
