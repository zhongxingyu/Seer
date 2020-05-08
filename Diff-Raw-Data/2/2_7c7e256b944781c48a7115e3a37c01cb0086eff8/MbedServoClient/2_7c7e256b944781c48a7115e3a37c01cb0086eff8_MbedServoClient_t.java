 package nl.uva.sd2;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 import android.util.Log;
 
 public class MbedServoClient implements IMbedNetwork {
 	private InputStream in;
 	private OutputStream out;
 	private MainActivity main;
 	private Socket sock;
 	private boolean isRunning;
 	
 	public MbedServoClient(MainActivity main) {
 		this.main = main;
 		isRunning = true;
 		new Thread(this).start();
 	}
 	
 	@Override
 	public void run() {
 		Log.i("SD2", "RUN CLIENT");
 		try {
 			sock = new Socket("192.168.1.217", 23568);
			in = sock.getInputStream();
			out = sock.getOutputStream();
 		} catch(Exception e) {
 			Log.e("SD2", "Could not connect", e);
 			main.onError("Network error", "Could not connect to the server.");
 			return;
 		}
 		
 		Log.i("SD2", "I connected to " + sock.getInetAddress());
 		
 		int nread;
 		byte[] buf = new byte[1];
 		while(isRunning) {
 			try {
 				nread = in.read(buf);
 				if(nread < 0) {
 					out.close();
 					in.close();
 					sock.close();
 					in = null;
 					out = null;
 				}
 				
 				if(nread > 0) {
 					// Update progressbar
 					main.newValue(buf[0] & 0xFF);
 				}
 			} catch(IOException e) {
 				Log.e("SD2", "Could not read from conn", e);
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public void newValue(int value) {
 		if(out != null){
 			try {
 				out.write(value);
 				out.flush();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public void stop() {
 		isRunning = false;
 		if(sock != null && in != null && out != null) {
 			try {
 				in.close();
 				out.close();
 				sock.close();
 			} catch(IOException idontcare) {
 			}
 		}
 	}	
 }
