 package com.pyrobot.droid;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.ImageView;
 
 public class RobotServer extends Activity {
 
 	private SurfaceView mSurfaceView;
 	private SurfaceHolder holder;
 	private Camera mCamera;
 	private VideoDecodeThread vdt;
 
 	ServerSocket socket;
	public static ArrayList<Socket> clients;
 	ArrayList<InputStream> ins;
 	ArrayList<OutputStream> outs;
 
 	Socket robot;
 	InputStream robotIn;
 	OutputStream robotOut;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.server);
 		init();
 	}
 
 	public void init() {
 		initHolder();
		//connectToRobot();
 		clientAcceptThread.start();
		//relayThread.start();
 	}
 
 	public void connectToRobot() {
 		try {
 			robot = new Socket(ModeSelect.hostname, ModeSelect.port);
 			robotOut = robot.getOutputStream();
 			robotIn = robot.getInputStream();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void initHolder() {
 		mSurfaceView = (SurfaceView) findViewById(R.id.video_surface_view);
 		SurfaceHolder holder = mSurfaceView.getHolder();
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		SurfaceHolderCallback callback = new SurfaceHolderCallback(holder);
 		holder.addCallback(callback);
 	}
 	
 	public Thread relayThread = new Thread() {
 		public void run() {
 			byte[] buffer = new byte[1024];
 			try {
 				for (InputStream in : ins) {
 					if (in.available() > 0) {
 						robotOut.write(in.read(buffer));
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	};
 
 	public Thread clientAcceptThread = new Thread() {
 		public void run() {
 			try {
 				socket = new ServerSocket(5000);
 				clients = new ArrayList<Socket>();
 				outs = new ArrayList<OutputStream>();
 				ins = new ArrayList<InputStream>();
 				while (true) {
 					Socket client = socket.accept();
 					clients.add(client);
 					outs.add(client.getOutputStream());
 					ins.add(client.getInputStream());
 				}
 			} catch (SocketException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	};
 
 }
