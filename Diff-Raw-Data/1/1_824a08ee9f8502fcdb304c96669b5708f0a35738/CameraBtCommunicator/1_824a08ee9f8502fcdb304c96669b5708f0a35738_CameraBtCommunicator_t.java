 package org.programus.nxt.android.lookie_rc.comm;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 import java.util.UUID;
 
 import org.programus.lookie.lib.comm.CameraCommand;
 import org.programus.lookie.lib.utils.Constants;
 import org.programus.lookie.lib.utils.SimpleQueue;
 import org.programus.nxt.android.lookie_rc.parts.FriendBtDevice;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothSocket;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class CameraBtCommunicator {
 	private final static String TAG = "CAM Comm";
 	private Handler handler;
 	private BluetoothAdapter btAdapter;
 	private BluetoothSocket socket;
 	
 	private ObjectOutputStream out;
 	private ObjectInputStream in;
 	
 	private CameraCommandReceiver receiver;
 	private CameraCommandSender sender;
 	private SimpleQueue<CameraCommand> sendQ = DataBuffer.getInstance().getCamSendQueue();
 	
 	public CameraBtCommunicator(BluetoothAdapter btAdapter, Handler handler) {
 		this.btAdapter = btAdapter;
 		this.handler = handler;
 	}
 	
 	public void connect(final FriendBtDevice fbdevice, final List<FriendBtDevice> deviceList) {
 		Thread t = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				if (fbdevice.getDevice() != null) {
 					if (socket == null) {
 						connectDevice(fbdevice);
 					}
 				}
 				
 				if (socket == null) {
 					btAdapter.cancelDiscovery();
 					for (FriendBtDevice device : deviceList) {
 						if (socket == null && device.getDevice() != null) {
 							connectDevice(device);
 						}
 					}
 				}
 				
 				if (socket == null) {
 					notifyConnected(null);
 				}
 			}
 		});
 		t.start();
 	}
 	
 	private void connectDevice(final FriendBtDevice device) {
 		BluetoothSocket tmp = null;
 		if (socket == null) {
 			Log.d(TAG, String.format("Start connecting try: %s", device));
 			try {
 				synchronized(device) {
 					tmp = device.getDevice().createRfcommSocketToServiceRecord(UUID.fromString(Constants.CAMERA_UUID));
 				}
 			} catch (IOException e) {
 				Log.w(TAG, "create socket failed.", e);
 			}
 		}
 		Log.d(TAG, String.format("Tried: %s, Result: %s", device, String.valueOf(tmp)));
 		try {
 			tmp.connect();
 			if (tmp != null && socket == null) {
 				socket = tmp;
 				afterConnected();
 				notifyConnected(device);
 			}
 		} catch (IOException e) {
 			Log.w(TAG, "Connect failed: " + device);
 		}
 	}
 	
 	private void afterConnected() throws IOException {
 		this.out = new ObjectOutputStream(this.socket.getOutputStream());
 		this.in = new ObjectInputStream(this.socket.getInputStream());
 		
 		this.receiver = new CameraCommandReceiver(this.in, this.handler);
 		Thread t = new Thread(this.receiver, "Cam Cmd Reader");
 		t.start();
 		
 		this.sender = new CameraCommandSender(this.out, this.handler);
 		Thread ts = new Thread(this.sender, "Cam Cmd Sender");
 		ts.start();
 	}
 	
 	public void sendCommand(CameraCommand cmd) {
 		synchronized(this.sendQ) {
 			this.sendQ.offer(cmd);
 		}
 	}
 	
 	private void notifyConnected(FriendBtDevice device) {
 		Bundle b = new Bundle();
 		b.putInt(Constants.KEY_CAMERA_CONNECT_STATUS, device != null ? Constants.CONN_STATUS_CONNECTED : Constants.CONN_STATUS_DISCONNECTED);
 		b.putString(Constants.KEY_CAMERA_DEVICE, device != null ? device.toString() : "null");
 		
 		Message msg = new Message();
 		msg.setData(b);
 		msg.what = Constants.MSG_WHAT_CAMERA_CONNECT;
 		handler.sendMessage(msg);
 	}
 	
 	public void end() throws IOException {
 		this.sender.end();
 		this.receiver.end();
 		Thread.yield();
 		this.socket.close();
		this.socket = null;
 	}
 }
