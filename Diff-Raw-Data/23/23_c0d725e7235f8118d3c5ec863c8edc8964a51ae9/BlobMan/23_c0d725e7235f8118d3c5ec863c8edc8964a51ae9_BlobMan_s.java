 package main;
 
 import java.applet.Applet;
 import processing.core.*;
 
 import fisica.*;
 
 public class BlobMan {
 	private static PApplet applet;
 	private static FWorld world;
 	private FCircle body;
 	private FBody leftArm;
 	private FBody rightArm;
 	private FBody head;
 	private FBody hat;
 	private float w;
 	private float h;
 	private boolean isCircleHat;
 	private int[] phoneNumber;
 	private static float spawnX;
 	private static float spawnY;
 	private boolean facingR;
 	private int jumpPower;
 	private static float spawnOffset;
 
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
 		jumpPower = 0;
 		
 		createBody();
 		createHead(phoneNumber[3]);
 		createArms();
 		setColors();
 		
 		body.setPosition(spawnX, spawnY);
 		spawnOffset -= 100;
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
 		if (headCode >= 8) createHeadHelp(3);
 		else if (headCode >= 5) createHeadHelp(2);
 		else if (headCode >= 2) createHeadHelp(1);
 		else createHeadHelp(0);
 	}
 	
 	private int[] colorChart(int code) {
 		int[] color = new int[3];
 		switch (code) {
 		case 0: 
 			color[0] = 242; color[1] = 164; color[2] = 190; break; //pink
 		case 1:
 			color[0] = 255; color[1] = 241; color[2] = 233; break; //light beige
 		case 2:
 			color[0] = 196; color[1] = 62; color[2] = 87; break; // red
 		case 3:
 			color[0] = 31; color[1] = 59; color[2] = 107; break; // blue
 		case 4:
 			color[0] = 2; color[1] = 13; color[2] = 43; break; // dark purple
 		case 5:
 			color[0] = 166; color[1] = 120; color[2] = 56; break; // brown
 		case 6:
 			color[0] = 148; color[1] = 148; color[2] = 148; break; // gray
 		case 7:
 			color[0] = 239; color[1] = 157; color[2] = 51; break; // orange
 		case 8:
 			color[0] = 147; color[1] = 198; color[2] = 234; break; // light blue
 		case 9:
 			color[0] = 89; color[1] = 162; color[2] = 46; break; // green
 		}
 		return color;
 	}
 	
 	private void createArms() {
 		leftArm = new FBox(45, 15);
 		leftArm.setRotation(-3.1415f/3f);
 		leftArm.setRotatable(false);
 		leftArm.setGrabbable(false);
 		leftArm.setPosition(body.getX() - 48, body.getY()-10);
 		world.add(leftArm);
 		
 		rightArm = new FBox(45, 15);
 		rightArm.setRotation(3.1415f/3f);
 		rightArm.setRotatable(false);
 		rightArm.setGrabbable(false);
 		rightArm.setPosition(body.getX() + 48, body.getY() - 10);
 		world.add(rightArm);
 	}
 	
 	private void setColors() {
 		int[] hatRGB = colorChart(phoneNumber[4]);
 		int[] headRGB = colorChart(phoneNumber[5]);
 		int[] bodyRGB = colorChart(phoneNumber[6]);
 		
 		hat.setFill(hatRGB[0], hatRGB[1], hatRGB[2]);
 		head.setFill(headRGB[0], headRGB[1], headRGB[2]);
 		body.setFill(bodyRGB[0], bodyRGB[1], bodyRGB[2]);
 		leftArm.setFill(headRGB[0], headRGB[1], headRGB[2]);
 		rightArm.setFill(headRGB[0], headRGB[1], headRGB[2]);
 	}
 	
 	public static void setAppletWorld(PApplet applet, FWorld world) {
 		BlobMan.applet = applet;
 		BlobMan.world = world;
 	}
 	
 	public void update() {
 		checkJump();
 		
		if (facingR) head.setPosition(body.getX()+15, body.getY() - 60);
		else head.setPosition(body.getX() - 15, body.getY() - 60);
 		leftArm.setPosition(body.getX() - 48, body.getY()-10);
 		rightArm.setPosition(body.getX() + 48, body.getY() - 10);
 		if (isCircleHat == true) hat.setPosition(head.getX(), head.getY() - 35);
 		else hat.setPosition(head.getX(), head.getY() - 20);
 	}
 	
 	
 	private void checkJump() {
 		float vx;
 		if (jumpPower > 0) {
 			float vxx = body.getVelocityX();
 			float vyy = body.getVelocityY();
 			if (facingR == true) vx = w/10;
 			else vx = -w/10;
			body.adjustVelocity(vx - vxx, -h*3/8 + vyy);
 			jumpPower --;
 		}
 	}
 	
 	public void jump() {
 		jumpPower = 10;
 	}
 	
 	public void faceR() {
 		facingR = true;
 		head.setRotation(0);
 	}
 	
 	public void faceL() {
 		facingR = false;
 		head.setRotation(3.1415f);
 	}
 	
 	public static void setSpawn(float x, float y) {
 		spawnX = x;
 		spawnY = y;
 	}
 	
 	public boolean checkWin() {
 		return false;
 	}
 	
 	
 }
