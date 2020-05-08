 package org.vxp7755.freeforall;
 
 import android.os.Handler;
 import android.os.Message;
import org.vxp7755_nxz3937.freeforall.R;
 
 public class ControllerThread extends Thread {
 
 	private Handler boardHandler;
 	private Board board;
 	private boolean paused;
 	private double speedMultiplier;
 	
 	ControllerThread()
 	{
 		paused          = false;
 		speedMultiplier = 1.0;
 		
 		boardHandler = new Handler(){
 			@Override
 			public void handleMessage( Message msg )
 			{
 				
 			}
 		};
 	}
 }
