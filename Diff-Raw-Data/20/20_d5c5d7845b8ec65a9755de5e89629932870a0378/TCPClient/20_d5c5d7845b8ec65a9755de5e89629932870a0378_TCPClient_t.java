 package com.emdoor.autotest;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.widget.Toast;
 
 public class TCPClient implements Runnable {
 	private static final String TAG = "TCPClient";
 	private Socket socket;
 	private DataInputStream dis;
 	private DataOutputStream dos;
 	private Handler mHandler;
 	private Context mContext;
 	private String content = "";
 	private String serverIP;
 	private int port;
 	private boolean bConnected = false;
 
 	public TCPClient(String serverIP, int port, Context context, Handler handler) {
 		this.mHandler = handler;
 		this.serverIP = serverIP;
 		this.port = port;
 		this.mContext = context;
 		new Thread(this).start();
 
 	}
 
 	public boolean isConnected() {
 		return socket != null ? socket.isConnected() : false;
 	}
 
 	public void WriteByteArray(byte[] data) {
 		if (data == null || data.length == 0) {
 			return;
 		}
 		try {
 			dos.write(data);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void WriteString(String str) {
 
 		if (str == null) {
 			return;
 		}
 		byte[] b = str.getBytes();
 		try {
 			dos.write(b);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void Disconnect(){
 		if(socket!=null){
 			try {
 				socket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		socket=null;
 	}
 	
 	@Override
 	public void run() {
 
 		try {
 			socket = new Socket(serverIP, port);
 			dis = new DataInputStream(socket.getInputStream());
 			dos = new DataOutputStream(socket.getOutputStream());
 			if (socket != null && socket.isConnected()) {
 
 				this.WriteString("I'm ready,"
 						+ WifiHelper.getInstance(mContext).getWifiMAC()
 						+ "\r\n");
 				Message msg = new Message();
 				msg.what = Messages.MSG_CONNECT_SUCCUSS;
 				mHandler.sendMessage(msg);
 			}
 		} catch (UnknownHostException e) {
 			Message msg=new Message();
 			msg.what=Messages.MSG_CONNECT_ERROR;
 			msg.obj=e.getMessage();
 			mHandler.sendMessage(msg);
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			Message msg=new Message();
 			msg.what=Messages.MSG_CONNECT_ERROR;
 			msg.obj=e.getMessage();
 			mHandler.sendMessage(msg);
 			e.printStackTrace();
 			return;
 		} 
 		try {
 			while (true) {
 				if (!socket.isInputShutdown()) {
 					if ((content = dis.readLine()) != null) {
 						if (!content.isEmpty()) {
 							Message msg = new Message();
 							msg.what = Messages.MSG_CMD_RECEIVE;
 							msg.obj = content;
 							Log.d(TAG, "receive data:" + content);
 							mHandler.sendMessage(msg);
 						}
 					} else {
 
 					}
 
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
			try {
				if(socket!=null){
					socket.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//socket=null;
			Message msg=new Message();
			msg.what=Messages.MSG_CONNECT_ERROR;
			msg.obj=e.getMessage();
			mHandler.sendMessage(msg);
 		} finally {
 
 		}
 	}
 
 }
