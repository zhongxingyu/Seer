 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.distributed;
 
 import edu.gatech.statics.application.StaticsApplication;
import edu.gatech.statics.modes.distributed.objects.DistributedForce;
 import edu.gatech.statics.objects.SimulationObject;
 
 /**
  *
  * @author Calvin Ashmore
  */
 class DistributedUtil {
 
     private DistributedUtil() {
     }
 
     static void grayoutSolvedDistributedObjects() {
        /*for (SimulationObject obj : StaticsApplication.getApp().getCurrentDiagram().allObjects()) {
            if (obj instanceof DistributedForce) {
                DistributedForce dl = (DistributedForce) obj;
                dl.setDisplayGrayed(dl.isSolved());
             }
        }*/
     }
 }
