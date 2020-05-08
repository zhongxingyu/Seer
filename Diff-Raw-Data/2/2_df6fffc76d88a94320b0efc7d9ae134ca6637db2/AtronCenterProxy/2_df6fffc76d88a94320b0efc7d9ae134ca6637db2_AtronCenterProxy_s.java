 package onlineLearning.atron;
 
 import java.awt.Color;
 
 import ussr.samples.atron.ATRONController;
 
 public class AtronCenterProxy {
 	private ATRONController controller;
 	
 	public AtronCenterProxy(AtronSkillController controller) {
 		this.controller = controller;
 	}
 	private float getTime() {
     	return controller.getTime();
     }
     private int getID() {
     	return controller.getModule().getID();
     }
     public boolean safeRotate(int dir) { //rotates unless it get stuck then i rotates back
     	//controller.rotateToDegree(rad);
     	throw new RuntimeException("unimplemented");
     }
     public boolean safeToDegreeRotate(float rad, float safeRad) { //rotates unless it get stuck then i rotates back
     	int counter =0;
     	initIsStuck(rad);
     	while(!hasRotatedTo(rad)) {
 			controller.rotateToDegree(rad);
 			if(isStuck(rad)) {
 				initIsStuck(safeRad);
 				System.out.println(getID()+": Is stuck (going to "+rad+") rolling back rotate "+safeRad);
 				counter=0;
 				rad = safeRad;
 				safeRad = -1;
 				controller.getModule().setColor(Color.WHITE);
 			}
			controller.ussrYield();
 		    counter++;
 		}
     	// if(counter!=0) System.out.println(getID()+": at right position after "+counter+" steps");
     	return true;
     }
     
     private void initIsStuck(float rad) {
     	updateIsStuck = new TimeOutManager(0.05f, controller); //20 times per sec;
     	isStuck = false;
     	isStuckCounter = 0;
     	oldError = controller.getAngularPosition()-rad;;
     }
     TimeOutManager updateIsStuck;
     boolean isStuck = false;
     int isStuckCounter = 0;
     float oldError = 0;
 	private boolean isStuck(float rad) {
 		if(updateIsStuck.isTimeout()) {
 			float error = controller.getAngularPosition()-rad;
 			float improvement = oldError-error;
 			oldError = error;
 			if((improvement<1*Math.PI/180)&&(Math.abs(error)>2*Math.PI/180)) {
 				isStuckCounter++;
 			}
 			if((improvement>1*Math.PI/180)) {
 				isStuckCounter = (isStuckCounter>0)?isStuckCounter-1:0; 
 			}
 			if(Math.abs(error)<1*Math.PI/180) {
 				isStuckCounter=0;
 			}
 			if(isStuckCounter>20) {
 				isStuckCounter=21;
 				isStuck = true;
 			}
 			else {
 				isStuck=false;
 			}
 			
 			//if(getID()==6) System.out.println("{"+error+", "+isStuckCounter+", "+improvement+"}, ");	
 			updateIsStuck.reset();
 		}
 		return isStuck;
 	}
 	private boolean hasRotatedTo(float rad) {
 		float error = Math.abs(controller.getAngularPosition()-rad);
 		if(error>Math.PI/180) { //more than one degree error
 			return false;
 		}
 		if(controller.isRotating()) {
 			return false;
 		}
 		
 		return true;
 	}
 }
