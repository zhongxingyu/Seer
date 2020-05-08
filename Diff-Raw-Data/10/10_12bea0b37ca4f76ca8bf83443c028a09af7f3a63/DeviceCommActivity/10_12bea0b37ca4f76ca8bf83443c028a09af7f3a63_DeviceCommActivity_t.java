 package com.cs456.a2;
 
 import java.io.File;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.DialogInterface;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 
 public class DeviceCommActivity extends Activity {
 	
 	// Member fields
     private Search search;
     private ArrayList<String> wlanList;
    private File sdCardRoot = Environment.getExternalStorageDirectory();
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         search = new Search();
         search.startScan();
         wlanList = new ArrayList<String>();
         
      // Register for broadcasts when a device is discovered
         IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
         this.registerReceiver(search.getmReceiver(), filter);
         
      // Register for broadcasts when discovery has finished
         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         this.registerReceiver(search.getmReceiver(), filter);
         
         TextView output = (TextView) findViewById(R.id.output);
         output.setText("Nothing here yet");
         
         
         //////////////////////////
         ////// This is TEMP //////
         //////////////////////////
 //        TextView janson = (TextView)findViewById(R.id.jansonTest);
         // Get the directory path to the SD card
 //        janson.setText(FileListing.getSortedFileListString(sdCardRoot));
         
 //        Button jansonbtn = (Button)findViewById(R.id.jansonbtn);
 //        jansonbtn.setOnClickListener(new OnClickListener() {
 //			
 //			@Override
 //			public void onClick(View v) {
 //				Logger.getInstance().log("Pressed the Button");				
 //			}
 //		});
         
     }
     
     /***
      * Testing code
      */
     public String getLocalIpAddress() {
         try {
             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                 NetworkInterface intf = en.nextElement();
                 for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                     InetAddress inetAddress = enumIpAddr.nextElement();
                     if (!inetAddress.isLoopbackAddress()) {
                         return inetAddress.getHostAddress().toString();
                     }
                 }
             }
         } catch (SocketException ex) {
             //Log.e(LOG_TAG, ex.toString());
         }
         return null;
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         // Make sure we're not doing discovery anymore
         if (search.getBtAdapter() != null) {
             search.stopScan();
         }
 
         // Unregister broadcast listeners
         this.unregisterReceiver(search.getmReceiver());
         
 		Logger.getInstance().closeLogFile();
     }
     
    
     // Creates a dialog box stating there was an error, and prints the text which the calling code provides it
     private void handleError(String error) {
 		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
 		alertBuilder.setMessage(error);
 		alertBuilder.setCancelable(false);
 		
 		alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 		
 		AlertDialog alert = alertBuilder.create();
 		alert.setTitle("Error!");
 		alert.show();
 	}
  
     
     public void startSearch(View view) {
     	TextView tv = (TextView) findViewById(R.id.output2);
     	tv.setText("");
     	TextView tv2 = (TextView) findViewById(R.id.output);
     	tv2.setText("Starting Scan");
     	search.startScan();
     	
     	ArrayList<String> mList = search.getBtMACList();
     	for (String txt: mList) {
     		tv.append(txt);
     	}
     	tv2.setText("Finished Scan");
     	
     	TextView tv3 = (TextView) findViewById(R.id.output3);
     	tv3.setText("Starting query");
     	for (int i = 0; i < mList.size();i++) {
     		String[] content = BLSQuery.query(mList.get(i));
     		tv.append("\n-------\n");
     		if (content == null) {
     			//ERROR::
     			return;
     		}
     		
     		for (int j = 0; j<content.length; j++) {
     			if (content[j]!=null) {
     				tv.append(content[j]);
     				if (j==1)
     					wlanList.add(content[j]);
     			}
     		}
     	}
     	
     	tv3.setText("done query");
     }
     
     public void startServer(View view) {
     	TextView tv = (TextView) findViewById(R.id.startServer);
     	serverRelated=tv;
     	tv.setText("starting");
     	if(!serverListenerRunning) {
     		Object pass = new MyServer().execute();
     	}
     }
  
     private Handler handler = new Handler();
     TextView serverRelated;
     TextView clientRelated;
     private boolean serverListenerRunning = false;
     private boolean clientListenerRunning = false;
     private final int SOCKET_PORT = 62009;
     
     private final String EXIT_MESSAGE = "Goodbye Cruel World";
     private final String START_MESSAGE = "Hello Cruel World";
     private final String SEND_FILE_LIST_MESSAGE = "File List Cruel World";
     private final String END_FILE_LIST_MESSAGE = "No More File List Cruel World";
     
     //Singleton
     private class MyServer extends AsyncTask {
 
 		@Override
 		protected Object doInBackground(Object... arg0) {
 			ServerSocket server = null;
 			Socket client = null;
 			BufferedReader in = null;
 			BufferedWriter out = null;
 			try {
 				serverListenerRunning = true;
 				server = new ServerSocket();
 			
 				server.setReuseAddress(true);
 				
 				handler.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						serverRelated.setText("Listening: "+getLocalIpAddress());
 					}
 				});
 				
 				server.bind(new InetSocketAddress(SOCKET_PORT));
 	
 				client = server.accept();
 				
 				handler.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						serverRelated.setText("Server is done");
 					}
 				});
 				
 				
 				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 				out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
 				
 				PrintWriter pw = new PrintWriter(out);
 				
 				String line = null;
 
 				while (true) {
 					handler.post(new Runnable() {
 
 						@Override
 						public void run() {
 							serverRelated.setText("Pre Read Line");
 						}
 					});
 					line = in.readLine();
 					handler.post(new Runnable() {
 						
 						@Override
 						public void run() {
 							serverRelated.setText("Read Line");
 						}
 					});
 					
 					if(line == null) {
 						break;
 					}
 					
 					if(START_MESSAGE.equals(line)) {
 						pw.println(START_MESSAGE);
 						pw.flush();
 						break;
 					}
 				}
 				
 				if(line != null) {
 					while (true) {
 						line = in.readLine();
 
 						if (SEND_FILE_LIST_MESSAGE.equals(line)) {
							pw.println(FileListing.getSortedFileListString(sdCardRoot));
 							pw.println(END_FILE_LIST_MESSAGE);
 							pw.flush();
 						} else if (EXIT_MESSAGE.equals(line)) {
 							pw.println(EXIT_MESSAGE);
 							pw.flush();
 							break;
 						}
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			finally {
 				serverListenerRunning = false;
 				try {
 					if(in != null) in.close();
 					if(out != null) out.close();
 					if(client != null) client.close();
 					if(server != null) server.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}				
 			}
 			return null;
 		}
     	
     }
     
     private class MyClient extends AsyncTask {
 
 		@Override
 		protected Object doInBackground(Object... arg0) {
 			BufferedReader in = null;
 			BufferedWriter out = null;
 			Socket socket = null;
 			try {
 				clientListenerRunning = true;
 				
				socket = new Socket("192.168.2.8", SOCKET_PORT);
 				
 				handler.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						clientRelated.setText("Socket connected");
 					}
 				});
 				
 				
 				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 				
 				PrintWriter pw = new PrintWriter(out);
 				
 				String line = null;
 				
 				while (true) {
 					pw.println(START_MESSAGE);
 					pw.flush();
 					
 					line = in.readLine();
 					
 					if(line == null) {
 						break;
 					}
 					
 					if(START_MESSAGE.equals(line)) {
 						break;
 					}
 				}
 				String fileList = "XXX";
 				
 				if(line != null) {
 					pw.println(SEND_FILE_LIST_MESSAGE);
 					pw.flush();
 					
 					while (true) {
 						
 						line = in.readLine();
 						
 						if(END_FILE_LIST_MESSAGE.equals(line)) {
 							pw.println(EXIT_MESSAGE);
 							pw.flush();
 							break;
 						}
 						else if(line == null) break;
 						else {
 							fileList += line + "\n";
 						}
 					}
 					
 					line = in.readLine();
 				}
 				
 				final String test = fileList;
 				
 				handler.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						clientRelated.setText(test);
 					}
 				});
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			finally {
 				clientListenerRunning = false;
 				try {
 					if(in != null) in.close();
 					if(out != null) out.close();
 					if(socket != null) socket.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}				
 			}
 			return null;
 		}
     	
     }
     
     
   
     public void startConnect(View view) {
     	TextView tv2 = (TextView) findViewById(R.id.myTest);
     	clientRelated=tv2;
     	if(!clientListenerRunning) {
     		Object pass = new MyClient().execute();
     	}
     }
  
 }
