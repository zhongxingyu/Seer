 package cz.emo4d.mcontrol;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.sql.ClientInfoStatus;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.net.DhcpInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.util.Log;
 
 public class NetService extends Service {
 
 	@Override
 	public void onDestroy() {
 		try {
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		close();
 		super.onDestroy();
 	}
 
 	public class ServiceBinder extends Binder {
 		public NetService getService() {
 			return NetService.this;
 		}
 	};
 
 	private final IBinder mBinder = new ServiceBinder();
 
 	private Socket socket = null;
 	private DatagramSocket socket1 = null;
 	private DatagramSocket socket2 = null;
 	private BufferedReader in;
 	private PrintWriter out;
 
 	private String mHost = null;
 	public final static int MODE_MASTER = 1;
 	public final static int MODE_SLAVE = 2;
 	private int mMode = 0;
 
 	private final static int SERVERPORT = 5869;
 	private static final int DISCOVERYPORT = 10869;
 	private static final String discoveryData = "ZenDiscovery";
 	private final static String SERIALIZER_DELIMITER = "&&&&";
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 
 		return mBinder;
 	}
 	
 	public void setMode (int m) {
 		mMode = m;
 	}
 
 	public void runAutoDiscovery() {
 		DiscoveryClass exe = new DiscoveryClass();
 		exe.execute();
 	}
 
 	private class DiscoveryClass extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			WifiManager wifi = (WifiManager) getApplicationContext()
 					.getSystemService(Context.WIFI_SERVICE);
 			DhcpInfo dhcp = wifi.getDhcpInfo();
 			// handle null somehow
 
 			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
 			byte[] quads = new byte[4];
 			for (int k = 0; k < 4; k++)
 				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
 
 			boolean discovering = true;
 
 			while (discovering) {
 
 				try {
 					socket1 = new DatagramSocket();
 					socket1.setBroadcast(true);
 					DatagramPacket packet = new DatagramPacket(
 							discoveryData.getBytes(), discoveryData.length(),
 							InetAddress.getByAddress(quads), DISCOVERYPORT);
 					socket1.send(packet);
 
 					Log.i("Discovery", "before recv");
 
 					socket2 = new DatagramSocket(DISCOVERYPORT);
 					byte[] buf = new byte[1024];
 					packet = new DatagramPacket(buf, buf.length);
 					socket2.setSoTimeout(2000);
 					socket2.receive(packet);
 
 					mHost = new String(packet.getData());
 
 					int pos = mHost.indexOf(buf[400]);
 					mHost = mHost.substring(0, pos);
 					Log.i("Discovery", mHost);
 
 					discovering = false;
 				} catch (SocketTimeoutException e) {
 
 				} catch (Exception e) {
 					e.printStackTrace();
 
 				} finally {
 					socket1.close();
 					socket2.close();
 				}
 
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void param) {
 			openConnection();
 			super.onPostExecute(param);
 
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 			super.onProgressUpdate(values);
 
 		}
 	}
 
 	public boolean openConnection() {
 
 		OpenConnTask oc = new OpenConnTask();
 		oc.execute();
 
 		return true;
 	}
 
 	public boolean getSocketState() {
 		if (socket == null)
 			return false;
 		else
 			return true;
 	}
 
 	public void close() {
 		Socket s = socket;
 		socket = null;
 
 		try {
 			s.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean proceedIncomingData(String incoming) {
 		Log.i("INCOMING:", incoming);
 		
		int pos = incoming.indexOf(SERIALIZER_DELIMITER);
		String second = incoming.substring(0, pos);
		String third = incoming.substring(pos
				+ SERIALIZER_DELIMITER.length());
		
		if (Integer.parseInt(third) == EventTypes.VIBRATE) {
 			
 			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 			v.vibrate(300);
 		}
 		return true;
 	}
 
 	private class OpenConnTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			InetAddress serverAddr = null;
 			try {
 				serverAddr = InetAddress.getByName(mHost);
 			} catch (UnknownHostException e1) {
 				e1.printStackTrace();
 				return null;
 			}
 
 			// open socket
 			try {
 				socket = new Socket(serverAddr, SERVERPORT);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 
 			// prepare output buffer
 			try {
 				out = new PrintWriter(new BufferedWriter(
 						new OutputStreamWriter(socket.getOutputStream())), true);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// run input listener
 			try {
 				in = new BufferedReader(new InputStreamReader(
 						socket.getInputStream()));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			//send message about connection
 			if (mMode == MODE_MASTER)
 				sendControlEvent(EventTypes.CONNECT, 0, 0);
 			else
 				sendControlEvent(EventTypes.CONNECT, 1, 1);
 
 			InputListener il = new InputListener();
 			il.executeOnExecutor(THREAD_POOL_EXECUTOR);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void param) {
 			super.onPostExecute(param);
 
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 			super.onProgressUpdate(values);
 
 		}
 
 	}
 
 	private class InputListener extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			while (socket != null) {
 				try {
 					if (in.ready()) {
 						String serverMessage = in.readLine();
 						if (serverMessage != null)
 							proceedIncomingData(serverMessage);
 					} else {
 						//Log.i("ConnIn", "Sleeping");
 						try {
 							Thread.sleep(100);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 					break;
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void param) {
 			super.onPostExecute(param);
 
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 			super.onProgressUpdate(values);
 
 		}
 
 	}
 
 	public boolean sendControlEvent(int type, int valueX, int valueY) {
 		String serialized = new String();
 
 		float X = (float) valueX / (float) 250;
 		float Y = (float) valueY / (float) 250;
 
 		// X = X * X * Math.signum(X);
 		// Y = Y * Y * Math.signum(Y);
 
 		serialized = Integer.toString(type) + SERIALIZER_DELIMITER
 				+ Float.toString(X) + SERIALIZER_DELIMITER + Float.toString(Y);
 
 		SendAsync exe = new SendAsync(serialized);
 		exe.execute();
 
 		return true;
 	}
 
 	private class SendAsync extends AsyncTask<Void, Void, Void> {
 
 		private String message;
 
 		public SendAsync(String s) {
 			//Log.i("SEN","CONSTRUCTOR");
 			message = s;
 		}
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			//Log.i("SEN", "1");
 			if (socket != null) {
 				//Log.i("SEN", "2");
 				out.println(message);
 				//Log.i("SEN", "3");
 				out.flush();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void param) {
 			super.onPostExecute(param);
 
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 			super.onProgressUpdate(values);
 
 		}
 	}
 
 }
