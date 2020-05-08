 package leapTouch;
 /******************************************************************************\
 * Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved.               *
 * Leap Motion proprietary and confidential. Not for distribution.              *
 * Use subject to the terms of the Leap Motion SDK Agreement available at       *
 * https://developer.leapmotion.com/sdk_agreement, or another agreement         *
 * between Leap Motion and you, your company or other organization.             *
 \******************************************************************************/
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.leapmotion.leap.*;
 
 class SampleListener extends Listener {
     public void onInit(Controller controller) {
         System.out.println("Initialized");
     }
 
     public void onConnect(Controller controller) {
         System.out.println("Connected");
         System.out.println("\nSelect top left point");
     }
 
     public void onDisconnect(Controller controller) {
         //Note: not dispatched when running in a debugger.
         System.out.println("Disconnected");
     }
 
     public void onExit(Controller controller) {
         System.out.println("Exited");
     }
 
     public void onFrame(Controller controller) {
         if(!isCalibrated) {
         	isCalibrated = Calibrate(controller);
         	return;
         }
         System.out.println("Screen Calibrated");
     }
     
     VectorAverage avg = new VectorAverage(10);
     GetSanitizedInput isTouched = new GetSanitizedInput(2*1000000,2*10);
     boolean isDetected = false;
     boolean isCalibrated = false;
     boolean firstCall = true;
     ArrayList<Vector> points = new ArrayList<Vector>(3);
    
    public boolean Calibrate(Controller controller) 
    {
     	if(firstCall) {
     		String verb;
     		switch(points.size()) {
     		case 0:
    			verb = "Please touch and hold on top left corner.";     //p0
     			break;
     		case 1:
    			verb = "Please touch and hold on top right corner."; //p1
     			break;
     		case 2:
    			verb = "Please touch and hold on bototm left corner.";    //p2
    			break;
    		case 3:
    			verb = "Please touch and hold on bottom right corner.";  //p3 
     			break;
     		default:
     			return true;
     		}
     		System.out.println(verb);
     		firstCall = false;
     	}
     	
     	// Get the most recent frame and report some basic information
         Frame frame = controller.frame();
         if(frame.hands().isEmpty()) {
         	if(isDetected) {
         		System.out.println("Lost hand");
         		isDetected=false;
         	}
         	avg = new VectorAverage(10);
             isTouched = new GetSanitizedInput(2*1000000,2*10);
         	return false;
         }
         
         for(int i = 0; i < 1 && i < frame.hands().count(); i++) 
         {
         	if(!isDetected) {
         		System.out.println("Found hand");
         		isDetected=true;
         	}
 	        Vector handPos = frame.hands().get(i).palmPosition();
 	        avg.add(handPos);
 	        Vector averagePos = avg.getAverage();
 	        if(isTouched.checkInput(averagePos, frame.timestamp())) 
 	        {
 	        	isTouched = new GetSanitizedInput(2*1000000,2*10);
 	        	points.add(averagePos);
 	        	System.out.println("Point selected at "+averagePos.getX()+", "+averagePos.getY()+", "+averagePos.getZ());
 	        	firstCall = true;
 	        }
         }
         
        if(points.size()==4) {
         	return true;
         }
         return false;
     }
 }
 
 class Sample {
     public static void main(String[] args) {
         // Create a sample listener and controller
         SampleListener listener = new SampleListener();
         Controller controller = new Controller();
 
         // Have the sample listener receive events from the controller
         controller.addListener(listener);
 
         // Keep this process running until Enter is pressed
         System.out.println("Press Enter to quit...");
         try {
             System.in.read();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // Remove the sample listener when done
         controller.removeListener(listener);
     }
 }
