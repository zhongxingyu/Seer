 /**
 *	NotificationManager.java
 *
 *	@author Johan
 */
 
 package se.chalmers.watchme.notifications;
 
 import java.util.Calendar;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 
 public class NotificationClient {
 	
 	private Context ctx;
 	private NotificationService service;
 	private boolean isBound;
 	
 	private ServiceConnection connection;
 	
 	public NotificationClient(Context context) {
 		this.ctx = context;
 		this.isBound = false;
 		this.connection = new ServiceConnection() {
 
 			public void onServiceConnected(ComponentName name, IBinder s) {
 				service = ((NotificationService.ServiceBinder) s).getService();
 				
 			}
 
 			public void onServiceDisconnected(ComponentName name) {
 				service = null;
 			}
 			
 		};
 	}
 	
 	public void connectToService() {
		this.ctx.bindService(new Intent(this.ctx, NotificationService.class), null, Context.BIND_AUTO_CREATE);
 		this.isBound = true;
 	}
 	
	
	public void setDateForNotification(Calendar date) {
		if(this.service != null){
			this.service.setAlarm(date);
		}
	}
	
 	public void disconnectService() {
 		if(this.isBound) {
 			this.ctx.unbindService(connection);
 			this.isBound = false;
 		}
 	}
 	
 }
