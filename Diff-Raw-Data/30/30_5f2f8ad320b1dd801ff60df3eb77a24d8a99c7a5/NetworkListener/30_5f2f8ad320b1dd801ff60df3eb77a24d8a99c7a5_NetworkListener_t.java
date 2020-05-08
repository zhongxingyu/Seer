 package org.AndroidShareApp.core;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
import java.net.Socket;
 import java.net.SocketException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class NetworkListener extends Thread {
 
 	private DatagramSocket mListenSocket;
 	private DatagramSocket mReplySocket;
 	private byte[] mBuffer;
 
 	public NetworkListener(int listenPort, int replyPort) {
 		try {
 			mListenSocket = new DatagramSocket(listenPort);
 			mReplySocket = new DatagramSocket();
 		} catch (SocketException e) {
 			e.printStackTrace();
 			/* TODO: Emitir o erro. */
 		}
 		mBuffer = new byte[NetworkProtocol.BUFFER_SIZE];
 	}
 
 	@Override
 	public void run() {
 		while (!isInterrupted()) {
 			try {
 
 				DatagramPacket currPacket = new DatagramPacket(mBuffer,
 						mBuffer.length);
 
 				mListenSocket.receive(currPacket);
 
 				Log.i("NetworkListener",
 						"Received message from " + currPacket.getAddress());
 
 				parseJSON(new String(mBuffer), currPacket);
 
 			} catch (IOException e) {
 				e.printStackTrace();
 				/* TODO: Emitir o erro. */
 			}
 		}
 	}
 
 	private void parseJSON(String json, DatagramPacket packet) {
 		try {
 			JSONObject obj = new JSONObject(json);
 			int messageType = obj.getInt("messageType");
 			String deviceID;
 
 			switch (messageType) {
 			case (NetworkProtocol.MESSAGE_LIVE_ANNOUNCEMENT): {
 				/*
 				 * In this case, either a person has entered the network or a
 				 * person who already is in the network is announcing that
 				 * he/she is active.
 				 */
 				String name = obj.getString("name");
 				deviceID = obj.getString("deviceID");
 				InetAddress senderIP = packet.getAddress();
 
 				Log.i("NetworkListener", "MESSAGE_LIVE_ANN: " + obj);
 
 				NetworkManager.getInstance().addPerson(
 						new Person(name, deviceID, senderIP));
 			}
 				break;
 			case (NetworkProtocol.MESSAGE_LEAVING_ANNOUNCEMENT): {
 				/*
 				 * In this case, a person is announcing his/her exit from the
 				 * network.
 				 */
 				String name = obj.getString("name");
 				deviceID = obj.getString("deviceID");
 
 				NetworkManager.getInstance().deletePerson(
 						new Person(name, deviceID, null));
 			}
 				break;
 			case (NetworkProtocol.MESSAGE_SHARING_DETAILS_REQUEST):
 				break;
 			case (NetworkProtocol.MESSAGE_SHARING_DETAILS_SEND): {
 
 				JSONArray sharedList = obj.getJSONArray("sharedList");
 				String currPath, currPermissions;
 				deviceID = obj.getString("deviceID");
 
 				Person person = NetworkManager.getInstance()
 						.getPersonByDeviceID(deviceID);
 
 				if (person != null) {
 					Log.i("NetworkListener", "Received sharing details from "
 							+ person.getName());

 					for (int i = 0; i < sharedList.length(); i++) {
 						currPath = sharedList.getJSONObject(i)
 								.getString("path");
 						currPermissions = sharedList.getJSONObject(i)
 								.getString("permissions");
 
 						boolean read = false, write = false;
 
 						if (currPermissions.indexOf('r') != -1)
 							read = true;
 						if (currPermissions.indexOf('w') != -1)
 							write = true;
 
 						person.addSharedWithMeItem(new SharedWithMeItem(
 								currPath, read, write));
 					}
 				}
 			}
 				break;
 			case (NetworkProtocol.MESSAGE_SHARING_DETAILS_DENIED):
 				break;
 			case (NetworkProtocol.MESSAGE_DOWNLOAD_REQUEST): {
 				deviceID = obj.getString("deviceID");
 				String path = obj.getString("path");
 
 				JSONObject reply = new JSONObject();
 				reply.put("deviceID", deviceID);
 				reply.put("path", path);
 
 				if (!hasPermission(deviceID, path) || hasPendingUploads(path)) {
 					reply.put("messageType",
 							NetworkProtocol.MESSAGE_DOWNLOAD_DENY);
 				} else {
 					NetworkManager.getInstance().getFileServer()
 							.addPermission(deviceID, path);
 					reply.put("messageType",
 							NetworkProtocol.MESSAGE_DOWNLOAD_ACCEPT);
 				}
 
 				DatagramPacket replyPacket = new DatagramPacket(reply
 						.toString().getBytes(),
 						reply.toString().getBytes().length,
 						packet.getAddress(),
 						NetworkProtocol.BROADCAST_RECEIVE_PORT);
 
 				try {
 					mReplySocket.send(replyPacket);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
					return;
 				}
 			}
 				break;
			case (NetworkProtocol.MESSAGE_DOWNLOAD_ACCEPT): {
				deviceID = obj.getString("deviceID");
				String path = obj.getString("path");
				String destination = null; /* TODO: Pegar o destino. */
				int size = 0; /* TODO: Como pegar o tamanho??? Ele pode mandar.... */
				size = Integer.valueOf(obj.getString("size"));

				Socket socket = null;
				try {
					socket = new Socket(packet.getAddress(),
							NetworkProtocol.FILE_PORT);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}

				FileClient client = new FileClient(deviceID, path, destination,
						size, socket);
				Thread t = new Thread(client);
				t.start();
			}
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
 			Log.i("NetworkListener",
 					"Malformed JSON string: \"" + e.getMessage() + "\".");
 			return;
 		}
 	}
 
 	private static boolean hasPermission(String deviceID, String path) {
 		// TODO: Ver como fazer isso!
 		return true;
 	}
 
 	private static boolean hasPendingUploads(String path) {
 		// TODO: Verificar como fazer isso.
 		return false;
 	}
 }
