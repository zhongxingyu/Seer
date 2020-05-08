 package com.mutableconst.android.dashboard_manager;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.telephony.SmsManager;
 import android.widget.Toast;
 
 import com.mutableconst.chatserver.gui.MainActivity;
 import com.mutableconst.protocol.Protocol;
 
 public class AndroidConnection {
 
	private String serverAddress = "192.168.1.138";
 	private Socket socket;
 	private PendingIntent pi;
 	private BufferedReader in;
 	private PrintWriter out;
 	private Activity mainContext;
 	private final SmsManager sms;
 	private ConcurrentLinkedQueue<String> requests;
 
 	public AndroidConnection(final Activity mainContext) {
 		requests = new ConcurrentLinkedQueue<String>();
 		pi = PendingIntent.getActivity(mainContext, 0, new Intent(mainContext, MainActivity.class), 0);
 		sms = SmsManager.getDefault();
 		this.mainContext = mainContext;
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while (true) {
 					try {
 						if (socket == null || socket.isConnected() == false) {
 							sendToast("Creating a New Socket");
 							socket = new Socket(serverAddress, 9090);
 							sendToast("To TOAST MAN");
 							in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 							out = new PrintWriter(socket.getOutputStream(), true);
 						}
 						if (requests.isEmpty() == false) {
 							sendToast("Sending Text Message" + requests.peek());
 							out.println(requests.poll());
 						}
 						if (in.ready()) {
 							//handleResponse(Protocol.getProtocol().decodeRawRequest(in.readLine()));
 							System.out.println(in.read());
 						}
 						System.out.println("Falling out of loop");
 						Thread.sleep(500);
 					} catch (SocketException e) {
 						socket = null;
 						in = null;
 						out = null;
 					} catch (UnknownHostException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						sendToast(e.getMessage());
 						e.printStackTrace();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 		}).start();
 	}
 
 	private void handleResponse(HashMap<String, String> decodedResponse) {
 		if (decodedResponse != null) {
 			if (decodedResponse.get(Protocol.TYPE) == Protocol.TEXT_MESSAGE_TYPE) {
 				sms.sendTextMessage(decodedResponse.get(Protocol.PHONE), null, decodedResponse.get(Protocol.MESSAGE), pi, null);
 				sendToast("To: " + decodedResponse.get(Protocol.PHONE) + " " + "Message:" + decodedResponse.get(Protocol.MESSAGE));
 			}
 		}
 	}
 
 	private void sendToast(final String message) {
 		mainContext.runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(mainContext, message, Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 
 	public boolean addRequest(String request) {
 		if (requests != null) {
 			requests.add(request);
 			return true;
 		} else {
 			sendToast("Not connected");
 			return false;
 		}
 	}
 }
