 package marrc;
 
 import java.util.Random;
 
 import vision.*;
 import mobile.*;
 
 public class Driver implements Runnable{
 
 	final int SPEED = 200; // mm/s
 	
 	Capture camera;
 	Detect detector;
 	Manager m;
 	Mover mv;
 	boolean qrDetected, bumped, locked,  auto;
 	private String qrSeen; // should be a set of strings for multiple qr codes, but...
 	
 	Thread photographer, detective;
 	
 	private long nextMove;
 	
 	Driver(Manager m) {
 		this.m = m;
 		m.registerDriver(this);
 		mv = m.getMover();
 		nextMove = 0;
 		qrDetected = false;
 		bumped = false;
 		locked = false;
 		auto = false;
 		camera = new Capture(m);
 		if(camera.success()) {
 			photographer = new Thread(camera);
 			photographer.start();
 			m.registerCapture(camera);
 		} else {
 			m.registerCapture(null);
 		}
 	}
 	
 	public boolean isLocked() { return locked; }
 	
 	public void run() {
 		driveRandomly();
 //		int[] qrLoc;
 //		while(true) {
 //			while ((qrLoc = detector.detect("img/0.jpg")) == null) {
 //				driveRandomly();
 //				//Thread.sleep(500);
 //			}
 //			
 //			driveTowards(qrLoc);
 //		}
 	}
 	
 	public boolean hasQR() {
 		return qrSeen.equals("yes");
 	}
 	
 	private void slightRight(int time) {
 		mv.right(SPEED);
 	}
 	
 	private void slightLeft(int time) {
 		mv.left(SPEED);
 	}
 	
 	private void driveWait(int time) {
 		boolean localLock = !locked; // if its not locked, lock locally
 		locked = true;
 		long waitTime = System.currentTimeMillis() + time;
 		nextMove = waitTime + 10;
 		while (waitTime > System.currentTimeMillis()) { Thread.yield(); }
 		if (localLock) { locked = false; } // don't release if it wasn't locked locally
 	}
 	
 	private void driveWaitWithInterrupt (int time) {
 		int count = 0;
 		while(count < time && (!qrDetected && !bumped)) {
 			driveWait(10);
 			count += 10;
 		}
 	}
 	
 	public void setAutomatic() {
 		auto = true;
 	}
 	
 	public void eventOccured(String event) {
 		if(event.contains("QR")) {
 			if(qrDetected || qrSeen.equals("yes")) { return; }
 			qrDetected = true;
 			qrSeen = "yes";
 			System.out.println("Saw something!!");
 			driveTowardsQR();				
 			qrDetected = false;
 		} else if (event.contains("bump")) {
			System.out.print("Trying bump...");
 			if(bumped) { return; }
			System.out.println("\tbumping");
 			bumped = true;
 			locked = true;
 			mv.stop();
 			mv.reverse(SPEED);
 			driveWait(200);
 			mv.stop();
 			if(event.contains("center")) {
 				if(Math.random() > 0.5) {
 					slightRight(500);
 				} else {
 					slightLeft(500);
 				}
 			} else {
 				if(event.contains("left")) {
 					slightRight(200);
 				} else if (event.contains("right")) {
 					slightLeft(200);
 				}
 			}
 			mv.stop();
 			driveWait(500);
 			bumped = false;
 			locked = false;
 			if(auto) { driveRandomly(); }
 		}
 	}
 	
 	private void driveRandomly() {
 //		System.out.println("New call to driveRandomly");
 		Random r = new Random();
 		while(!qrDetected && !bumped) {
 			if ((bumped || qrDetected) || (locked || nextMove > System.currentTimeMillis())) { Thread.yield(); continue; }
 			// makes sure driving is mostly forward
 			switch(r.nextInt(5)) {
 			case 1: // turn left
 				mv.left(SPEED);
 				driveWaitWithInterrupt(250 + r.nextInt(250)); // turn this direction for up to a half second
 				break;
 			case 2: // turn right
 				mv.right(SPEED);
 				driveWaitWithInterrupt(250 + r.nextInt(250)); // turn this direction for up to a half second
 				break;
 			default: // forward
 				mv.forward(SPEED);
 				driveWaitWithInterrupt(500 + r.nextInt(500)); // drive this direction for up to a second
 			}
 		}
 	}
 	
 	private void driveTowardsQR() {
 		while (!bumped) {
 			driveTowards(m.getQRX());
 		}
 	}
 	
 	private void driveTowards(int i) {
 		if (i < 0) { return; }
 		// loc[0] is x coordinate, disregard y
 		if (i < (Capture.imageWidth / 3)) {
 			slightRight(50);
 //			System.out.println("Right");
 		} else if (i > (2*Capture.imageWidth / 3)) {
 			slightLeft(50);
 //			System.out.println("Left");
 		} else {
 //			System.out.println("Forward");
 			mv.forward(SPEED);
 			driveWaitWithInterrupt(50);
 		}
 	}
 	
 }
