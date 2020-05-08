 package org.AndroidShareApp.core;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class NetworkListener extends Thread {
 
 	private DatagramSocket mSocket;
 	private byte[] mBuffer;
 
 	public NetworkListener(int listenPort) {
 		try {
 			mSocket = new DatagramSocket(listenPort);
 		} catch (SocketException e) {
 			e.printStackTrace();
 			/* TODO: Emitir o erro. */
 		}
 		mBuffer = new byte[NetworkProtocol.BUFFER_SIZE];
 		setName("NetworkListener");
 	}
 
 	@Override
 	public void run() {
 		while (!isInterrupted()) {
 			try {
 
 				DatagramPacket currPacket = new DatagramPacket(mBuffer,
 						mBuffer.length);
 
 				mSocket.receive(currPacket);
 				parseJSON(new String(mBuffer));
 
 			} catch (IOException e) {
 				e.printStackTrace();
 				/* TODO: Emitir o erro. */
 			}
 		}
 	}
 
 	private void parseJSON(String json) {
 		try {
 			JSONObject obj = new JSONObject(json);
 			int messageType = obj.getInt("messageType");
 
 			switch (messageType) {
 			case (NetworkProtocol.MESSAGE_LIVE_ANNOUNCEMENT): {
 				/*
 				 * In this case, either a person has entered the network or a
 				 * person who already is in the network is announcing that
 				 * he/she is active.
 				 */
 				String name = obj.getString("name");
 				String deviceID = obj.getString("deviceID");
 				NetworkManager.getInstance().addPerson(
						new Person(name, deviceID, false));
 			}
 				break;
 			case (NetworkProtocol.MESSAGE_LEAVING_ANNOUNCEMENT): {
 				/*
 				 * In this case, a person is announcing his/her exit from the
 				 * network.
 				 */
 				String name = obj.getString("name");
 				String deviceID = obj.getString("deviceID");
 
 				NetworkManager.getInstance().deletePerson(
						new Person(name, deviceID, false));
 			}
 				break;
 			case (NetworkProtocol.MESSAGE_SHARING_NOTIFICATION):
 				break;
 			case (NetworkProtocol.MESSAGE_SHARING_DETAILS_REQUEST):
 				break;
 			case (NetworkProtocol.MESSAGE_SHARING_DETAILS_ANSWER):
 				break;
 			case (NetworkProtocol.MESSAGE_SHARING_DETAILS_DENIED):
 				break;
 			case (NetworkProtocol.MESSAGE_DOWNLOAD_REQUEST):
 				break;
 			case (NetworkProtocol.MESSAGE_DOWNLOAD_ACCEPT):
 				break;
 			case (NetworkProtocol.MESSAGE_DOWNLOAD_DENY):
 				break;
 			case (NetworkProtocol.MESSAGE_UPLOAD_REQUEST):
 				break;
 			case (NetworkProtocol.MESSAGE_UPLOAD_ACCEPT):
 				break;
 			case (NetworkProtocol.MESSAGE_UPLOAD_DENY):
 				break;
 			}
 
 		} catch (JSONException e) {
 			System.err.println("Malformed JSON string: \"" + json + "\"\n");
 			return;
 		}
 	}
 }
