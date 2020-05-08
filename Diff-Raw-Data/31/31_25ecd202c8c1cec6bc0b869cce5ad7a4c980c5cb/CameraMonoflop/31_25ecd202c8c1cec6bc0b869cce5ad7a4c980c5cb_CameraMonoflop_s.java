 package core;
 
 import handler.StackHandler;
 
 import java.util.logging.Logger;
 
 import com.tinkerforge.BrickletDualRelay;
 
 /**
  * This class starts the camera using the monoflop relais 
  * during initialization of the robot.
  * Every 3 seconds a request is send to renew the monoflop. If it 
  * fails an communication error is assumed and the relais automatically
  * switches off the camera
  */
 public class CameraMonoflop extends Thread {
 	private Logger logger = Logger.getLogger(CameraMonoflop.class.getName());
 	
 	private boolean shouldRun = true;
 	
 	public void stopCameraMonoflop() throws Exception{
 		shouldRun = false;
 		Thread.sleep(100); // after that someone can safely set the relais again
 	}
 	
 	public void run(){
 		BrickletDualRelay relais = StackHandler.getDualRelaisBricklet();
 		while(shouldRun){
 			try {
 				relais.setMonoflop((short)1, true, 4000);
 				CameraMonoflop.sleep(3000);
 			} catch (Exception e) {
				logger.severe("Camera Monoflop call failed");
 			}
 		}
 	}
 
 }
