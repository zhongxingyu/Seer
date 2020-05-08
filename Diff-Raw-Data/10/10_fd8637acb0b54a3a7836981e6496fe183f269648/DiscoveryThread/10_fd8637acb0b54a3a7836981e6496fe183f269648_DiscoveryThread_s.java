 package oly.netpowerctrl;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.support.v4.content.LocalBroadcastManager;
 import android.widget.Toast;
 
 public class DiscoveryThread extends Thread {
 	
 	public static String BROADCAST_DEVICE_DISCOVERED = "com.nittka.netpowerctrl.DEVICE_DISCOVERED";
 	
 	int recv_port;
 	Activity activity;
 	boolean keep_running;
 	DatagramSocket socket;
 	
 	public DiscoveryThread(int port, Activity act) {
 		recv_port = port;
 		activity = act;
 		socket = null;
 	}
 	
 	public void run() {
 		
 		keep_running = true;
 		while (keep_running) {
 			try {
 				byte[] message = new byte[1500];
 		        DatagramPacket p = new DatagramPacket(message, message.length);
 		        socket = new DatagramSocket(recv_port);
 		        socket.receive(p);
 		        if (! socket.isClosed()) {
 		        	socket.close();
 		        	parsePacket(new String(message, 0, p.getLength()), recv_port);
 		        }
 			} catch (final IOException e) {
 				if (keep_running) { // no message if we were interrupt()ed
 					activity.runOnUiThread(new Runnable() {
 					    public void run() {
 					    	String msg = String.format(activity.getResources().getString(R.string.error_listen_thread_exception), recv_port);
 					    	msg += e.getLocalizedMessage();
 					    	if (recv_port < 1024) msg += activity.getResources().getString(R.string.error_port_lt_1024);
 					    	Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
 					    }
 					});
 				}
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void interrupt() {
	    super.interrupt();
 	    keep_running = false;
	    if (socket != null)
	    	this.socket.close();
 	}
 
 	public void parsePacket(final String message, int recevied_port) {
 		
 		String msg[] = message.split(":");
 		if (msg.length < 3)
 			return;
 		
 		if ((msg.length >= 4) && (msg[3].trim().equals("Err"))) {
 			// error packet received
 			String desc;
 			if (msg[2].trim().equals("NoPass"))
 				desc = activity.getResources().getString(R.string.error_nopass);
 			else desc = msg[2];
 			final String error = activity.getResources().getString(R.string.error_packet_received) + desc;
 			activity.runOnUiThread(new Runnable() {
 			    public void run() {
 			    	Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
 			    }
 			});
 			return;
 		}
 		
 		final DeviceInfo di = new DeviceInfo(activity);
 		di.DeviceName = msg[1].trim();
 		di.HostName = msg[2];
 		di.RecvPort = recevied_port;
 		// leave SendPort as default, as we have no way to know.
 		
 		for (int i=6; i<(msg.length-2); i++) {
 			String outlet[] = msg[i].split(",");
 			if (outlet.length < 1)
 				continue;
 			OutletInfo oi = new OutletInfo();
 			oi.OutletNumber = i-5; // 1-based
 			oi.Description = outlet[0];
 			if (outlet.length > 1)
 				oi.State = outlet[1].equals("1");
 			di.Outlets.add(oi);
 		}
 		
 		activity.runOnUiThread(new Runnable() {
 			public void run() {
 				Intent it = new Intent(BROADCAST_DEVICE_DISCOVERED);
 				it.putExtra("device_info", di);
 		        LocalBroadcastManager.getInstance(activity).sendBroadcast(it);
 			}
 		});
 		
 	}
 }
