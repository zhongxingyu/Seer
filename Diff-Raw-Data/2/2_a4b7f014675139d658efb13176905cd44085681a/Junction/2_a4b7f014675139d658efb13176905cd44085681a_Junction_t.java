 /*
  * Copyright (C) 2010 Stanford University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package edu.stanford.junction.provider.bluetooth;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.util.Log;
 
 import edu.stanford.junction.JunctionException;
 import edu.stanford.junction.api.activity.ActivityScript;
 import edu.stanford.junction.api.activity.JunctionActor;
 import edu.stanford.junction.api.messaging.MessageHeader;
 
 public class Junction extends edu.stanford.junction.Junction {
 	private static String TAG = "junction";
 	public static String NS_JX = "jx";
 	public static String JX_JOINED = "joined";
 	public static String JX_CREATOR = "creator";
 	private static String JX_BT_SYS_MSG = "btsysmsg";
 	private static String JX_SCRIPT = "ascript";
 	
 	private ActivityScript mActivityScript;
 	private Set<ConnectedThread> mConnections;
 	// reusable variable for tracking connections we need to remove
 	private Set<ConnectedThread> mRemoveConnections;
 	
 	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
 	private AcceptThread mAcceptThread;
 	private ConnectThread mConnectThread;
 	private ConnectedThread mConnectedThread;
 	private boolean mIsHub;
 	private URI mUri;
 	private String mSession;
 	private String mSwitchboard;
 	
 	private boolean mIsActivityCreator;
 	private final Object mJoinLock = new Object();
 	private boolean mJoinComplete = false;
 	
 	public Junction(URI uri, ActivityScript script, final JunctionActor actor) throws JunctionException {
 		mActivityScript = script;
 		mUri = uri;		
 		mSwitchboard = uri.getAuthority();
 		
 		setActor(actor);
 
 		if (mSwitchboard.equals(mBtAdapter.getAddress())) {
 			Log.d(TAG, "starting new junction hub");
 			mIsHub = true;
 			mSession = BluetoothSwitchboardConfig.APP_UUID.toString();
 			mConnections = new HashSet<ConnectedThread>();
 			mRemoveConnections = new HashSet<ConnectedThread>();
 			mAcceptThread = new AcceptThread();
 			mAcceptThread.start();
 		} else {
 			Log.d(TAG, "connecting to junction hub: " + mSwitchboard);
 			mIsHub = false;
 			mSession = uri.getPath().substring(1);
 			BluetoothDevice hub = mBtAdapter.getRemoteDevice(mSwitchboard);
			mConnectThread = new ConnectThread(hub, UUID.fromString(mSession));
 			mConnectThread.start();
 		}
 		if (mIsHub) {
 			triggerActorJoin(mIsHub);
 		} else {
 			synchronized (mJoinLock) {
 				if (!mJoinComplete) {
 					try {
 						mJoinLock.wait(20000);
 					} catch (InterruptedException e) {
 						// Ignored
 					}
 				}
 				if (mJoinComplete) {
 					triggerActorJoin(mIsActivityCreator);
 				} else {
 					throw new JunctionException("Failed to join session, handshake not complete.");
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void disconnect() {
 		if (mIsHub) {
 			for (ConnectedThread conn : mConnections) {
 				conn.cancel();
 			}
 			try {
 				mAcceptThread.cancel();
 			} catch (Exception e) {}
 		} else {
 			try {
 				mConnectedThread.cancel();
 			} catch (Exception e) {}
 		}
 	}
 
 	@Override
 	public URI getAcceptedInvitation() {
 		return mUri;
 	}
 
 	@Override
 	public ActivityScript getActivityScript() {
 		return mActivityScript;
 	}
 
 	@Override
 	public URI getBaseInvitationURI() {
 		return mUri;
 	}
 
 	@Override
 	public String getSessionID() {
 		return mSession;
 	}
 
 	@Override
 	public String getSwitchboard() {
 		return mSwitchboard;
 	}
 
 	@Override
 	public void doSendMessageToActor(String actorID, JSONObject message) {
 		JSONObject jx;
 		if (message.has(NS_JX)) {
 			jx = message.optJSONObject(NS_JX);
 		} else {
 			jx = new JSONObject();
 			try {
 				message.put(NS_JX, jx);
 			} catch (JSONException j) {}
 		}
 		try {
 			jx.put("from", getActor().getActorID());
 			jx.put("targetActor", actorID);
 		} catch (Exception e) {}
 		byte[] bytes = message.toString().getBytes();
 		
 		if (mIsHub) {
 			// TODO: make header proper. Add sender, etc.
             // Try to roll this into framework?
             String from = "me";
             MessageHeader header = new MessageHeader(Junction.this,message,from);
             
             synchronized (Junction.this) {
             	mRemoveConnections.clear();
             	Junction.this.triggerMessageReceived(header, message);
             	for (ConnectedThread conn : mConnections) {
             		try {
             			conn.write(bytes, bytes.length);
             		} catch (IOException e) {
             			mRemoveConnections.add(conn);
             		}
                 }
             	if (mRemoveConnections.size() > 0) {
             		Log.d(TAG, "Removing " + mRemoveConnections.size() + " dead connections.");
             		mConnections.removeAll(mRemoveConnections);
             		mRemoveConnections.clear();
             	}
             }
 		} else {
 			try {
 				mConnectedThread.write(bytes, bytes.length);
 			} catch (IOException e) {
 				Log.d(TAG, "Dead connection detected.");
 				disconnect();
 			}
 		}
 	}
 
 	@Override
 	public void doSendMessageToRole(String role, JSONObject message) {
 		JSONObject jx;
 		if (message.has(NS_JX)) {
 			jx = message.optJSONObject(NS_JX);
 		} else {
 			jx = new JSONObject();
 			try {
 				message.put(NS_JX, jx);
 			} catch (JSONException j) {}
 		}
 		try {
 			jx.put("from", getActor().getActorID());
 			jx.put("targetRole", role);
 		} catch (Exception e) {}
 		byte[] bytes = message.toString().getBytes();
 		
 		if (mIsHub) {
 			// TODO: make header proper. Add sender, etc.
             // Try to roll this into framework?
             String from = "me";
             MessageHeader header = new MessageHeader(Junction.this,message,from);
             
             synchronized (Junction.this) {
             	mRemoveConnections.clear();
             	Junction.this.triggerMessageReceived(header, message);
             	for (ConnectedThread conn : mConnections) {
             		try {
             			conn.write(bytes, bytes.length);
             		} catch (IOException e) {
             			mRemoveConnections.add(conn);
             		}
                 }
             	if (mRemoveConnections.size() > 0) {
             		Log.d(TAG, "Removing " + mRemoveConnections.size() + " dead connections.");
             		mConnections.removeAll(mRemoveConnections);
             		mRemoveConnections.clear();
             	}
             }
 		} else {
 			try {
 				mConnectedThread.write(bytes, bytes.length);
 			} catch (IOException e) {
 				Log.d(TAG, "Dead connection detected.");
 				disconnect();
 			}
 		}
 	}
 
 	@Override
 	public void doSendMessageToSession(JSONObject message) {
 		Log.d(TAG,"writing to session: " + message);
 		JSONObject jx;
 		try {
 			if (message.has(NS_JX)) {
 				jx = message.getJSONObject(NS_JX);
 			} else {
 				jx = new JSONObject();
 				message.put(NS_JX, jx);
 			}
 			jx.put("from", getActor().getActorID());
 		} catch (Exception e ) {
 			// Ignored
 		}
 		
 		byte[] bytes = message.toString().getBytes();
 		
 		if (mIsHub) {
 			synchronized (Junction.this) {
             	mRemoveConnections.clear();
             	
             	MessageHeader header = new MessageHeader(this, message, getActor().getActorID());
             	Junction.this.triggerMessageReceived(header, message);
             	for (ConnectedThread conn : mConnections) {
             		try {
             			conn.write(bytes, bytes.length);
             		} catch (IOException e) {
             			mRemoveConnections.add(conn);
             		}
                 }
             	if (mRemoveConnections.size() > 0) {
             		Log.d(TAG, "Removing " + mRemoveConnections.size() + " dead connections.");
             		mConnections.removeAll(mRemoveConnections);
             		mRemoveConnections.clear();
             	}
             }
 		} else {
 			try {
 				Log.d(TAG, "client is writing " + message.toString());
 				mConnectedThread.write(bytes, bytes.length);
 			} catch (IOException e) {
 				Log.e(TAG, "could not write message over bluetooth", e);
 				disconnect();
 			}
 		}
 	}
 
 	@Override
 	public JunctionActor getActor() {
 		return mOwner;
 	}
 	
 	private class AcceptThread extends Thread {
         // The local server socket
         private final BluetoothServerSocket mmServerSocket;
 
         public AcceptThread() {
             BluetoothServerSocket tmp = null;
             
             // Create a new listening server socket
             try {
                 tmp = mBtAdapter.listenUsingRfcommWithServiceRecord(
                 		BluetoothSwitchboardConfig.APP_NAME, BluetoothSwitchboardConfig.APP_UUID);
             } catch (IOException e) {
                 Log.e(TAG, "listen() failed", e);
             }
             mmServerSocket = tmp;
         }
 
         public void run() {
             Log.d(TAG, "BEGIN mAcceptThread" + this);
             setName("AcceptThread");
             BluetoothSocket socket = null;
 
             // Listen to the server socket always
             while (true) {
                 try {
                     // This is a blocking call and will only return on a
                     // successful connection or an exception
                 	Log.d(TAG, "waiting for bluetooth client...");
                     socket = mmServerSocket.accept();
                     Log.d(TAG, "Client connected!");
                 } catch (IOException e) {
                     Log.e(TAG, "accept() failed", e);
                     break;
                 }
 
                 // If a connection was accepted
                 if (socket == null) {
                 	break;
                 }
                 
                 synchronized (Junction.this) {
                     ConnectedThread conThread = new ConnectedThread(socket);
                     
                     JSONObject aScriptObj = new JSONObject();
                     JSONObject aScriptMsg = new JSONObject();
                     try {
 	                    aScriptObj.put(JX_BT_SYS_MSG, true);
 	                    aScriptObj.put(JX_JOINED, true);
 	                    if (mActivityScript != null) {
 	                    	aScriptObj.put(JX_SCRIPT, mActivityScript.getJSON());
 	                    }
 	                    // Hub owner is the activity creator; joiner is not.
 	                    aScriptObj.put(JX_CREATOR, false);
 	                    aScriptMsg.put(NS_JX, aScriptObj);
 	                    
                     } catch (JSONException e) {}
                     
                     byte[] bytes = aScriptMsg.toString().getBytes();
                     try {
                     	Log.d(TAG, "Sending welcome packet.");
                     	conThread.write(bytes,bytes.length);
                     	Log.d(TAG, "Done.");
                     } catch (IOException e) {
                     	Log.e(TAG, "Error writing welcome message");
                     	return;
                     }
                     
                     conThread.start();
                     mConnections.add(conThread);
                 }
             }
             Log.i(TAG, "END mAcceptThread");
         }
 
         public void cancel() {
             Log.d(TAG, "cancel " + this);
             try {
                 mmServerSocket.close();
             } catch (IOException e) {
                 Log.e(TAG, "close() of server failed", e);
             }
             
             for (ConnectedThread conn : mConnections) {
             	try {
         			conn.cancel();
         		} catch (Exception e) {}
             }
         }
     }
 	
     /**
      * This thread runs while attempting to make an outgoing connection
      * with a device. It runs straight through; the connection either
      * succeeds or fails.
      */
     private class ConnectThread extends Thread {
         private final BluetoothSocket mmSocket;
         private final BluetoothDevice mmDevice;
         private final String mmSession;
         
         public ConnectThread(BluetoothDevice device, UUID uuid) {
             mmDevice = device;
             mmSession = uuid.toString();
             
             BluetoothSocket tmp = null;
 
             // Get a BluetoothSocket for a connection with the
             // given BluetoothDevice
             try {
                 tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
             } catch (IOException e) {
                 Log.e(TAG, "create() failed", e);
             }
             mmSocket = tmp;
         }
 
         public void run() {
             Log.i(TAG, "BEGIN mConnectThread");
             setName("ConnectThread");
 
             // Always cancel discovery because it will slow down a connection
             //mAdapter.cancelDiscovery();
 
             // Make a connection to the BluetoothSocket
             try {
                 // This is a blocking call and will only return on a
                 // successful connection or an exception
             	Log.d(TAG,"trying to connect socket for " + mmDevice.getAddress() + " at " + mmSession);
                 mmSocket.connect();
             } catch (IOException e) {
                 Log.e(TAG,"failed to connect to bluetooth socket", e);
                 try {
                     mmSocket.close();
                 } catch (IOException e2) {
                     Log.e(TAG, "unable to close() socket during connection failure", e2);
                 }
                 
                 return;
             }
             Log.d(TAG,"socket connected");
             // Reset the ConnectThread because we're done
             synchronized (Junction.this) {
                 mConnectThread = null;
             }
 
             // Start the connected thread
             mConnectedThread = new ConnectedThread(mmSocket);
             mConnectedThread.start();
         }
 
         public void cancel() {
             try {
                 mmSocket.close();
             } catch (IOException e) {
                 Log.e(TAG, "close() of connect socket failed", e);
             }
         }
     }
 
     /**
      * This thread runs during a connection with a remote device.
      * It handles all incoming and outgoing transmissions.
      */
     private class ConnectedThread extends Thread {
         private final BluetoothSocket mmSocket;
         private final InputStream mmInStream;
         private final OutputStream mmOutStream;
 
         public ConnectedThread(BluetoothSocket socket) {
             Log.d(TAG, "create ConnectedThread");
             mmSocket = socket;
             InputStream tmpIn = null;
             OutputStream tmpOut = null;
 
             // Get the BluetoothSocket input and output streams
             try {
                 tmpIn = socket.getInputStream();
                 tmpOut = socket.getOutputStream();
             } catch (IOException e) {
                 Log.e(TAG, "temp sockets not created", e);
             }
 
             mmInStream = tmpIn;
             mmOutStream = new BufferedOutputStream(tmpOut, 8192);
         }
 
         public void run() {
             Log.i(TAG, "BEGIN mConnectedThread");
             byte[] buffer = new byte[2048];
             int bytes;
 
             // Keep listening to the InputStream while connected
             while (true) {
                 try {
                     // Read from the InputStream
                     bytes = mmInStream.read(buffer);
                     
                     if (mIsHub) {
 	                    mRemoveConnections.clear();
 	                	for (ConnectedThread conn : mConnections) {
 	                		try {
 	                			conn.write(buffer, buffer.length);
 	                		} catch (IOException e) {
 	                			mRemoveConnections.add(conn);
 	                		}
 	                    }
 	                	if (mRemoveConnections.size() > 0) {
 	                		Log.d(TAG, "Removing " + mRemoveConnections.size() + " dead connections.");
 	                		mConnections.removeAll(mRemoveConnections);
 	                		mRemoveConnections.clear();
 	                	}
                     }
                     
                     // TODO: won't work with something over 2k
                     String jsonStr = new String(buffer,0,bytes);
                     
                     Log.d(TAG, "ConnectedThread got message " + jsonStr);
                     
                     JSONObject json = new JSONObject(jsonStr);
                     String from = "[Unknown]";
                     
                     if (json.has(NS_JX)) {
                     	JSONObject sys = json.getJSONObject(NS_JX);
                     	if (sys.has(JX_BT_SYS_MSG) && sys.has(JX_JOINED)) {
                         	if (!mIsHub) {
                         		 if (sys.has(JX_SCRIPT)) {
                         			 mActivityScript = new ActivityScript(sys.getJSONObject(JX_SCRIPT));
                         		 }
                         		 if (sys.has(JX_CREATOR)) {
                         			 synchronized(mJoinLock) {
                         				 mIsActivityCreator = sys.getBoolean(JX_CREATOR);
                         				 mJoinComplete = true;
                         				 mJoinLock.notify();
                         			 }
                         		 }
                         	}
                         	continue;
                     	}
                     	if (sys.has("targetActor") && 
                     			!getActor().getActorID().equals(sys.getString("targetActor"))) {
                     		continue;
                     	}
                     	if (sys.has("from")) {
                     		from = sys.getString("from");
                     	}
                     }
                     
                     MessageHeader header = new MessageHeader(Junction.this, json, from);
                     Junction.this.triggerMessageReceived(header, json);
                 } catch (IOException e) {
                     Log.e(TAG, "disconnected", e);
                     //connectionLost();
                     break;
                 } catch (JSONException e2) {
                 	Log.e(TAG, "not JSON", e2);
                 }
             }
         }
 
         /**
          * Write to the connected OutStream.
          * @param buffer  The bytes to write
          */
         public void write(byte[] buffer, int bytes) throws IOException {
             mmOutStream.write(buffer, 0, bytes);
             mmOutStream.flush();
         }
 
         public void cancel() {
             try {
             	synchronized(Junction.this) {
 	            	if (mIsHub) {
 	            		mConnections.remove(this);
 	            	}
             	}
                 mmSocket.close();
             } catch (IOException e) {
                 Log.e(TAG, "close() of connect socket failed", e);
             }
         }
     }
 	
 }
