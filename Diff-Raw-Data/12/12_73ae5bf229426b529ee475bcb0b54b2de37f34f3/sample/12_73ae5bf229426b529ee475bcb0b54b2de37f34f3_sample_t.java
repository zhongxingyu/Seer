 import RS40XCB_Java.RS40XCB;
 
 // author: atsushi egashira
 // license: LGPLv3
 
 public class sample {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		int sID = 1;//servo ID
		
 		RS40XCB servo = new RS40XCB("/dev/ttyUSB0", 115200);
 
 		System.out.println("Torque of the servo of ID1 is set to ON\n");
 		servo.torque(sID, true);
 
 		System.out.println("Rotate 100 degrees to the position of maximum speed.");
		servo.move(sID, -1000, 0);
 		try {
 			Thread.sleep(1500);//wait
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("Angle:"+servo.getAngle(sID));
 		
 		System.out.println("Position of 0 degrees to over 1 second.");
 		servo.move(sID, 0, 100);
 		
 		try {
			Thread.sleep(1500);//wait
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		System.out.println("Angle:"+servo.getAngle(sID));
 	}
 
 }
