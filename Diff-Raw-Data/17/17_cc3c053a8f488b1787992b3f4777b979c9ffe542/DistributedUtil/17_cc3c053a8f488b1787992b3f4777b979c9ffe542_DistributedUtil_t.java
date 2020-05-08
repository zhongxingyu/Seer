 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.distributed;
 
 import edu.gatech.statics.application.StaticsApplication;
import edu.gatech.statics.modes.distributed.objects.DistributedForceObject;
 import edu.gatech.statics.objects.SimulationObject;
import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 class DistributedUtil {
 
     private DistributedUtil() {
     }
 
     static void grayoutSolvedDistributedObjects() {
        List<SimulationObject> allObjects = StaticsApplication.getApp().getCurrentDiagram().allObjects();
        for (SimulationObject obj : allObjects) {
            if (obj instanceof DistributedForceObject) {
                DistributedForceObject dlObj = (DistributedForceObject) obj;
                dlObj.setDisplayGrayed(dlObj.isSolved());
             }
        }
     }
 }
