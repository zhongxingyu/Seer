 package com.example.lejosbluetooth;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.graphics.Point;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.example.requests.RequestCommand;
 import com.example.requests.RequestExitLejosServer;
 import com.example.requests.RequestSensorReading;
 import com.example.requests.RequestStartSweep;
 import com.example.requests.RequestStopSweep;
 
 /**
  * 1. sends requests to the Lejos server
  * 2. sends automatically timed requests to the Lejos server
  * @author docere@gmail.com
  *
  */
 
 public class Controller{
 	private Point newPoint = null;
 	private double lastAngle = 0;
 	
 
 	/**
 	 * used for timed requests from NXT data and posting UI updates based on this information
 	 */
 	public static Timer timer = null;
 	
 	/**
 	 * handler for timer thread to post UI updates via an implementation of Runnable
 	 */
 	final Handler myHandler = new Handler();
 	/**
 	 * implementation of Runnable to update the UI (obviously within the UI thread) which will be posted
 	 * by the timer thread.  This is posted by the Handler
 	 */
 	final Runnable runnableUI_Update =  new Runnable() {
 		
 		@Override
 		public void run() {
 //			update ui here
 			if(newPoint!=null)
 			{
 				textView.setText(Double.toString(lastAngle));
 				textView.invalidate();
 				drawView.drawLineToPoint(newPoint);
 				
 			}
 
 		}
 	};
 	
 	
 
 	private CustomView drawView = null;
 	private TextView textView = null;
 
 	private boolean isSweeping;
 
 	public CustomView getDrawView() {
 		return drawView;
 	}
 	
 	public void setTextView(TextView textView)
 	{
 		this.textView = textView;
 	}
 
 	public void setDrawView(CustomView drawPanel) {
 		this.drawView = drawPanel;
 	}
 
 	private static Controller controller = null;
 
 	private Controller() {
 	}
 
 	public void readSensor() {
 		if(BluetoothAsyncTask.singleton().getdOut()==null)return;
 		if(BluetoothAsyncTask.singleton().getdIn()==null)return;
 
 		newPoint = requestSensorReading();
 		Log.v(MainActivity.TAG, newPoint.toString());
 	}
 	
 	public static Controller singleton() {
 		if (controller == null) {
 			controller = new Controller();
 		}
 
 		return controller; 
 	}
 	
 	public Point requestSensorReading()
 	{
 		DataOutputStream out = BluetoothAsyncTask.singleton().getdOut();
 		DataInputStream in = BluetoothAsyncTask.singleton().getdIn();
 		if(out == null)return null;
 		
 		Point point = new Point();
 		
 		try {
 			out.writeByte(new RequestSensorReading().getCmd());
 			out.flush();
 
 			
 			double angle = in.readInt();
 			double distance = in.readInt();
 			double radians = angle/180*Math.PI;
 			this.lastAngle  = radians;
 			
 			point.x = (int) (Math.cos(radians)*distance*0.2);
 			point.y = (int) (Math.sin(radians)*distance*0.2);
 			
 			if(distance*0.2 < 100)
 			{
 				SoundPlayer.singleton().playSound(SoundPlayer.Sound.S1);
 			}
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return point;
 		
 	}
 	
 	public void startSweep()
 	{
 		if(isSweeping)return;
 		
 		isSweeping = true;
 		sendSimpleRequest(new RequestStartSweep());
 		timer = new Timer();
 		timer.schedule(new TimerTask() {
 			
 			@Override
 			public void run() {
 				readSensor();
 				myHandler.post(runnableUI_Update);
 			}
 
 		}, 500, 50);
 
 		
 	}
	
	publi
 	
 	public void stopSweep()
 	{
 		isSweeping = false;
 		sendSimpleRequest(new RequestStopSweep());
 		timer.cancel();
 	}
 	
 	public void terminateServer()
 	{
 		sendSimpleRequest(new RequestExitLejosServer());
 	}
 	
 	
 	private void sendSimpleRequest(RequestCommand reqCmd)
 	{
 		DataOutputStream out = BluetoothAsyncTask.singleton().getdOut();
 		if(out == null)return;
 		
 		try {
 			out.writeByte(reqCmd.getCmd());
 			out.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 }
