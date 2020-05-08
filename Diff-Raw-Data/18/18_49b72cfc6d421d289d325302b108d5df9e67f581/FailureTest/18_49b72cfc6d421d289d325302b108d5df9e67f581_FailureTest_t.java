 package es.upm.dit.gsi.shanks.model.failure.util.test;
 
 import java.util.List;
 
 import sim.engine.Steppable;
 
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
 import es.upm.dit.gsi.shanks.model.element.device.test.MyDevice;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementFieldException;
 import es.upm.dit.gsi.shanks.model.element.link.test.MyLink;
 import es.upm.dit.gsi.shanks.model.event.failiure.Failure;
 
 /**
  * @author a.carrera
  *
  */
 public class FailureTest extends Failure {
 
    public FailureTest(Steppable launcher) {
        this(FailureTest.class.getName(), launcher, 0.0);
    }
    
     public FailureTest(Steppable launcher, double prob) {
        this(FailureTest.class.getName(), launcher, prob);
    }
    
    public FailureTest(String id, Steppable launcher, double prob) {
         super(FailureTest.class.getName(), launcher, prob);
     }
 
     /* (non-Javadoc)
      * @see es.upm.dit.gsi.shanks.model.failure.Failure#isResolved()
      */
     @Override
     public boolean isResolved() {
         List<NetworkElement> affectedElements = this.getCurrentAffectedElements();
         boolean resolved = false;
         for (NetworkElement element : affectedElements) {
             if (element instanceof MyDevice) {
                 // Checking status / properties of the network element
                 if (!element.getStatus().get(MyDevice.HIGH_TEMP_STATUS)) {
                     resolved = true;
                 } else {
                     resolved = false;
                     break;
                 }
             } else if (element instanceof MyLink) {
                 // Checking status / properties of the network element
                 if(element.getStatus().get(MyLink.OK_STATUS)) {
                     resolved = true;
                 } else {
                     resolved = false;
                     break;
                 }
             }
         }
         return resolved;
     }
 
     @Override
     public void changeOtherFields()
             throws UnsupportedNetworkElementFieldException {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void addPossibleAffected() {
         this.addPossibleAffectedElementState(MyDevice.class, MyDevice.OK_STATUS, false);
         this.addPossibleAffectedElementState(MyDevice.class, MyDevice.NOK_STATUS, false);
         this.addPossibleAffectedElementState(MyDevice.class, MyDevice.HIGH_TEMP_STATUS, true);
         this.addPossibleAffectedElementState(MyLink.class, MyLink.BROKEN_STATUS, false);
         this.addPossibleAffectedElementState(MyLink.class, MyDevice.OK_STATUS, false);
         this.addPossibleAffectedElementProperty(MyDevice.class, MyDevice.TEMPERATURE_PROPERTY, 80.0);
     }
 
     @Override
     public void interactWithNE() {
         // TODO Auto-generated method stub
         
     }
 
 
 
 }
