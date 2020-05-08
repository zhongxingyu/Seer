 package client;
 
 import se.lth.cs.cameraproxy.Axis211A;
 import se.lth.cs.realtime.event.Buffer;
 
 
 public class ImageDispatcher extends Thread{
 	
 	///////////////////////////////////////////////////////////////////////////////////////
 	//	This thread is responsible of taking care about the images delay dipending on    //
 	//	the mode the user wants to have, even if it is auto, and send the delay suitable //
 	//	images to the GUI, and make them available to the user view.                     //
 	///////////////////////////////////////////////////////////////////////////////////////
 	
 	ClientMonitor monitor;
 	GUI gui;
 	
 	private int delayCamera1;
 	private int delayCamera2;
 	
 	private final int delayUpBound = 200; //Change this delay bound to switch to async, when test the program
 	private final int delayLowBound = 100; //Change this delay bound to switch to sync, when test the program
 	//private final int delayThreshold = 5; //Change this delay margin to adjust the acceptable difference delay while synchronized
 	private final int SYNC = 1;
 	private final int ASYNC  = 2;
 	private final int AUTO = 0;
 	private final int CAMERA1 = 1;
 	private final int CAMERA2 = 2;
 	
 	long startTime;
 	long updateTime;
 	int mode = ASYNC;
 	
 	public ImageDispatcher(ClientMonitor m, GUI gui){
 		monitor = m;
 		this.gui = gui;
 	}
 	
 	private void refreshImage(int SynchronizationMode){
 		
 		if(SynchronizationMode==AUTO){
 			mode = controlSynchronization(mode);
 			//gui.setMode(mode);
 		}else{
 			mode = SynchronizationMode;
 		}
 	
 		switch (mode){
 
 		case SYNC:
 			//return the most delayed image and the other with an added delay equal to the difference between both images delays
 			int delayDifference = Math.abs(delayCamera1-delayCamera2);
 			
 			if((System.currentTimeMillis()-updateTime)>=delayDifference){			
				gui.updateCamera1(monitor.bufferCamera1.getJPEG());
				gui.updateCamera2(monitor.bufferCamera2.getJPEG());	
 				updateTime = System.currentTimeMillis();
 			}
 			break;
 		
 		case ASYNC:
 			//return the current image
 			gui.updateCamera1(monitor.bufferCamera1.getJPEG());
 			gui.updateCamera2(monitor.bufferCamera2.getJPEG());			
 			break;
 		}
 	}
 	
 	private int controlSynchronization(int currentMode){
 		//If the delay is bigger than a bound, switch to asynchronous and viceversa
 		//Do it with hysteresis to avoid switching continuously
 		int SyncMode=currentMode;
 		
 		if(Math.abs(delayCamera1-delayCamera2)>delayUpBound){
 			SyncMode = ASYNC;
 		}else if(Math.abs(delayCamera1-delayCamera2)<delayLowBound){
 			SyncMode = SYNC;
 		}
 		return SyncMode;
 	}
 	
 	
 	private void refreshData(){
 		if((System.currentTimeMillis()-startTime)>=1000){
			gui.setFPS(monitor.bufferCamera1.getNumOfImg(), CAMERA1);
			gui.setFPS(monitor.bufferCamera2.getNumOfImg(), CAMERA2);
 			startTime = System.currentTimeMillis();
 		}
 		
		delayCamera1 = monitor.bufferCamera1.getDelay();
		delayCamera2 = monitor.bufferCamera2.getDelay();
 		gui.setDelay(delayCamera1, CAMERA1);
 		gui.setDelay(delayCamera2, CAMERA2);
 	}
 	
 	public void run(){
 		while(true){
 		refreshData();
 		refreshImage(ASYNC);
 		System.out.println("Delay 1: "+delayCamera1+" Delay 2: "+delayCamera2);
 		}
 	}
 }
