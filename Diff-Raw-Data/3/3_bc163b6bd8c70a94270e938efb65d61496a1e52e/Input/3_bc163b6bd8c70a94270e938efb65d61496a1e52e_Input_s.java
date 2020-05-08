 package se.lth.student.eda040.a1.data;
 
 import java.io.IOException;
 import android.util.Log;
 
 import se.lth.student.eda040.a1.network.*;
 
 public class Input extends Thread {
 	private ClientMonitor monitor;
 	private ClientProtocol protocol;
 
 	public Input(ClientMonitor monitor, ClientProtocol protocol) {
 		this.monitor = monitor;
 		this.protocol = protocol;
 	}
 
 	public void run() {
 		Image image;
 		while (!interrupted()) {
 			try {
 				Log.d("Input", "Checking for connection in monitor");
 				monitor.connectionCheck(protocol.getCameraId());
 				Log.d("Input", "In started to fetch images");
 				image = protocol.awaitImage();
 				monitor.putImage(image, protocol.getCameraId());
 			} catch (IOException e) {
 				try {
 					protocol.disconnect();
 				} catch (IOException ioe) {
 					System.err.println("Could not disconnect some how.");
 				}
				interrupt();
 			}
 		}
 	}
 }
