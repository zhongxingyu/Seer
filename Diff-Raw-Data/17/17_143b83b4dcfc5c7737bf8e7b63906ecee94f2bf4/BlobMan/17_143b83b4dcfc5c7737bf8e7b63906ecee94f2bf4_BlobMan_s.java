 package main;
 
 import java.applet.Applet;
 import processing.core.*;
 
 import fisica.*;
 
 public class BlobMan {
 	private static PApplet applet;
 	private static FWorld world;
 	private FCircle body;
 	// TIME CRUNCH... private FBody leftArm;
 	// TIME CRUNCH... private FBody rightArm;
 	private FBody head;
 	private FBody hat;
 	private float w;
 	private float h;
 	private boolean isCircleHat;
 	private int[] phoneNumber;
 
 	public BlobMan (int[] phoneNumber) {
 		// phoneNum must be a 7-character String of numbers, a phone number minus the area code
 		// call setAppletWorld before you instantiate this class
 		
 		/* MAP THE PHONE NUMBER TO CHARACTER ATTRIBUTES
 		 * DIGIT_INDEX		ATTRIBUTE
 		 * 		0				Special Move
 		 * 		1				Sound
 		 * 		2				Arm Shape
 		 * 		3				Head Type
 		 * 		4-6				Colors
 		 */
 		
 		this.phoneNumber = phoneNumber;
 		w = applet.width;
 		h = applet.height;
 		
 		createBody();
		createHead(3);
 	}
 	
 	private void createBody() {
 		body = new FCircle(70);
 		body.setPosition(w/2, h/2);
 		
 		world.add(body);
 	}
 	
 	private void createHeadHelp(int code) {
 		float hx;
 		float hy;
 		switch (code) {
 		case 3: // Square head, circle hat
 			head = new FBox(40, 40);
 			hat = new FCircle(25);
 			isCircleHat = true;
 			break;
 			
 		case 2 : // Round head, triangle hat
 			head = new FCircle(40);
 			hat = new FPoly();
 			hx = head.getX();
 			hy = head.getY();
 			isCircleHat = false;
 			
 			((FPoly)hat).vertex(hx - 20, hy -2);
 			((FPoly)hat).vertex(hx + 20, hy -2);
 			((FPoly)hat).vertex(hx, hy - 14);
 			break;
 			
 		case 1 : // Square head, triangle hat
 			head = new FBox(40, 40);
 			hat = new FPoly();
 			hx = head.getX();
 			hy = head.getY();
 			isCircleHat = false;
 			
 			((FPoly)hat).vertex(hx - 20, hy - 2);
 			((FPoly)hat).vertex(hx + 20, hy - 2);
 			((FPoly)hat).vertex(hx, hy - 14);
 			break;
 			
 		case 0 : // Round head, round hat
 			head = new FCircle(40);
 			hat = new FCircle(25);
 			isCircleHat = true;
 			break;
 		}	
 		head.setGrabbable(false);
 		head.setPosition(applet.width/2, applet.height/2 - 45);
 		world.add(head);
 		
 		hat.setGrabbable(false);
 		world.add(hat);
 
 	}
 	
 	private void createHead(int headCode) {
 		// Determine what numerical range the digit falls in and call createHeadHelp
		if (headCode.compareTo("8") >= 0) createHeadHelp(3);
		else if (headCode.compareTo("5") >= 0) createHeadHelp(2);
		else if (headCode.compareTo("2") >= 0) createHeadHelp(1);
 		else createHeadHelp(0);
 	}
 	
 	private int colorChart(int code) {
 		int color = 0x000000;
 		switch (code) {
 		case 0: 
 			color = 0xa1a4b2;
 			break;
 		case 1:
 			color = 0x51a3a1;
 			break;
 		case 2:
 			color = 0x329ad3;
 			break;
 		case 3:
 			color = 0x06b558;
 			break;
 		case 4:
 			color = 0xb20e01;
 			break;
 		case 5:
 			color = 0xeedbd1;
 			break;
 		case 6:
 			color = 0xd9afc7;
 			break;
 		case 7:
 			color = 0x182474;
 			break;
 		case 8:
 			color = 0xad988f;
 			break;
 		case 9:
 			color = 0xead337;
 			break;
 		}
 		return color;
 	}
 	
	private void setColors(String colorCode) {
		if (colorCode.compareTo("0") ==
 	}
 	
 	public static void setAppletWorld(PApplet applet, FWorld world) {
 		BlobMan.applet = applet;
 		BlobMan.world = world;
 	}
 	
 	public void update() {
 		head.setPosition(body.getX(), body.getY() - 60);
 		if (isCircleHat == true) hat.setPosition(head.getX(), head.getY() - 35);
 		else hat.setPosition(head.getX(), head.getY() - 20);
 	}
 	
 	
 }
