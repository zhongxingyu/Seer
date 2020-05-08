 package main;
 
import java.applet.Applet;
import processing.core.*;

 import fisica.*;
 
 public class BlobMan {
	private static PApplet applet;
 	private FBlob body;
 	private FBody leftArm;
 	private FBody rightArm;
 	private FBody head;
 	private FBody hat;
 
 	public BlobMan (String phoneNum) {
 		// phoneNum must be a 7-character String of numbers, a phone number minus the area code
		// call setApplet before you instantiate this class
		
 		/* MAP THE PHONE NUMBER TO CHARACTER ATTRIBUTES
 		 * DIGIT_INDEX		ATTRIBUTE
 		 * 		0				Special Move
 		 * 		1				Sound
 		 * 		2				Arm Shape
 		 * 		3				Head Type
 		 * 		4-6				Colors
 		 */
 		
		createBody();
 	}
 	
 	public void createBody() {
		body = new FBlob();
		//body.vertex(applet.width/2, 0);
		body.vertex(0, 0);
		//body.vertex(0, applet.height);
 		
 	}
	
	public static void setApplet(PApplet applet) {
		BlobMan.applet = applet;
	}
	
	
 }
