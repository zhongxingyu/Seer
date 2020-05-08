 /**
  * Unified Simulator for Self-Reconfigurable Robots (USSR)
  * (C) University of Southern Denmark 2008
  * This software is distributed under the BSD open-source license.
  * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
  */
 package ussr.samples.atron.simulations;
 
 import java.awt.Color;
 import java.util.List;
 
 import com.jme.math.Matrix3f;
 import com.jmex.physics.DynamicPhysicsNode;
 
 import ussr.comm.Packet;
 import ussr.comm.Receiver;
 import ussr.comm.Transmitter;
 import ussr.model.Sensor;
 import ussr.physics.jme.JMEModuleComponent;
 import ussr.samples.GenericSimulation;
 import ussr.samples.atron.ATRONController;
 
 /**
  * A simple controller for an ATRON car that reports data from the proximity sensors
  * 
  * @author Modular Robots @ MMMI
  *
  */
 public class ATRONCarController1 extends ATRONController {
 	
     /**
      * @see ussr.model.ControllerImpl#activate()
      */
     public void activate() {
     	yield();
     	this.delay(1000); /* rotateContinuous seem to fail sometimes if we do not wait at first */
         byte dir = 1;
         float lastProx = Float.NEGATIVE_INFINITY; /* for printing out proximity data */
         boolean firstTime = true;
         while(true) {
         	
             // Enable stopping the car interactively:
             if(!GenericSimulation.getActuatorsAreActive()) { yield(); firstTime = true; continue; }
             
             // Basic control: first time we enter the loop start rotating and turn the axle
             String name = module.getProperty("name");
             if(firstTime) {
                 firstTime = false;
                if(name.equals("wheel1")) rotateContinuous(dir);
                if(name.equals("wheel2")) rotateContinuous(-dir);
                if(name.equals("wheel3")) rotateContinuous(dir);
                if(name.equals("wheel4")) rotateContinuous(-dir);
                if(name.equals("axleOne5")) {
                     this.rotateDegrees(10);
                 }
             }
 
             // Print out proximity information
             float max_prox = Float.NEGATIVE_INFINITY;
             for(Sensor s: module.getSensors()) {
                 if(s.getName().startsWith("Proximity")) {
                     float v = s.readValue();
                     max_prox = Math.max(max_prox, v);
                 }
             }
             if(name.startsWith("wheel")&&Math.abs(lastProx-max_prox)>0.01) {
                 System.out.println("Proximity "+name+" max = "+max_prox);
                 lastProx = max_prox; 
             }
 
             // Always call yield sometimes
         	yield();
         }
     }
 }
