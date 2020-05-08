 package core;
 
 import handler.StackHandler;
 
import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.tinkerforge.BrickletIndustrialQuadRelay;
 
 /**
  * This class starts the ESC using the monoflop relais 
  * during initialization of the robot.
  * Every 3 seconds a request is send to renew the monoflop. If it 
  * fails an communication error is assumed and the relais automatically
  * switches off ESC
  */
 public class DriveMonoflop extends Thread {
 	private Logger logger = Logger.getLogger(DriveMonoflop.class.getName());
 	
 	private boolean shouldRun = true;
 	
 	public void stopDriveMonoflop() throws Exception{
 		shouldRun = false;
 		Thread.sleep(100); // after that someone can safely set the relais again
 	}
 	
 	public void run(){
 		BrickletIndustrialQuadRelay relais = StackHandler.getQuadRelaisBricklet();
 		while(shouldRun){
 			try {
 				relais.setMonoflop((1 << 0) | (1 << 1), (1 << 0) | (1 << 1), 4000);
 				DriveMonoflop.sleep(3000);
 			} catch (Exception e) {
				logger.log(Level.SEVERE, "Drive Monoflop call failed: ", e);
 			}
 		}
 	}
 
 }
