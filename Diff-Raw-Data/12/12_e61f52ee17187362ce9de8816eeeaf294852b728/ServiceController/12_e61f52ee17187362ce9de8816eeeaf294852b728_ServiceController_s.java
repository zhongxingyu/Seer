 package at.roadrunner.android.activity;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
import at.roadrunner.android.ApplicationController;
 import at.roadrunner.android.R;
 import at.roadrunner.android.couchdb.CouchDBService;
 import at.roadrunner.android.service.LoggingService;
 import at.roadrunner.android.service.MonitoringService;
 import at.roadrunner.android.service.ReplicatorService;
 
 public class ServiceController extends Activity implements OnClickListener  {
 	
 	private TextView _loggingStatus;
 	private TextView _monitoringStatus;
 	private TextView _replicatingStatus;
 	private Button _stopAllServices;
 	private Button _startAllServices;
 	private Button _stopRoadrunner;
	private ApplicationController _applicationController;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_service_controller);
 		
 		_loggingStatus = (TextView) findViewById(R.id.logging_status);
 		_monitoringStatus = (TextView) findViewById(R.id.monitoring_status);
 		_replicatingStatus = (TextView) findViewById(R.id.replicating_status);
 		
 		_stopAllServices = (Button) findViewById(R.id.stop_all_services);
 		_stopAllServices.setOnClickListener(this);
 		
 		_startAllServices = (Button) findViewById(R.id.start_all_services);
 		_startAllServices.setOnClickListener(this);
 		
 		_stopRoadrunner = (Button) findViewById(R.id.stop_roadrunner);
 		_stopRoadrunner.setOnClickListener(this);
 		
		_applicationController = (ApplicationController) getApplicationContext();
		
 		refreshStatus();
 	}
 	
 	private void refreshStatus()  {
 		_loggingStatus.setText(R.string.service_not_running);
 		_monitoringStatus.setText(R.string.service_not_running);
 		_replicatingStatus.setText(R.string.service_not_running);
 		
 		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 			if (LoggingService.class.getName().equals(service.service.getClassName())) {
 				_loggingStatus.setText(R.string.service_running);
 	        }  else if (MonitoringService.class.getName().equals(service.service.getClassName())) {
 	        	_monitoringStatus.setText(R.string.service_running);
 	        }  else if (ReplicatorService.class.getName().equals(service.service.getClassName())) {
 	        	_replicatingStatus.setText(R.string.service_running);
 	        }
 	    }
 	}
 	
 	@Override
 	public void onClick(View view) {
 		switch (view.getId())  {
 			case R.id.stop_all_services:
 				stopAllServices(this);
 				break;
 			case R.id.start_all_services:
 				startAllServices(this);
 				break;
 			case R.id.stop_roadrunner:
 				stopAllServices(this);
 				cancelNotification(this);
 				finish();
 		}
 		refreshStatus();
 	}
 	
 	public void showRoadrunnerNotification(Context context)  {
 		NotificationManager _notificationMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
 		cancelNotification(context);
 		
 		Notification notification = new Notification(R.drawable.ic_roadrunner_notification,
 				context.getString(R.string.service_label), System.currentTimeMillis());
 		
 		notification.flags = Notification.FLAG_ONGOING_EVENT;
 
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, ServiceController.class), 0);
 		
 		notification.setLatestEventInfo(context, context.getString(R.string.service_label),
 				context.getString(R.string.service_infotext), contentIntent);
 		
 		_notificationMgr.notify(R.id.service_notification_id, notification);	
 	}
 	
 	private void cancelNotification(Context context)  {
 		NotificationManager _notificationMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
 		_notificationMgr.cancel(R.id.service_notification_id);
 	}
 
 	public void startAllServices(Context context) {
 		CouchDBService.startCouchDB(context);
 		
 		context.startService(new Intent(context, MonitoringService.class));
 		context.startService(new Intent(context, LoggingService.class));
 		context.startService(new Intent(context, ReplicatorService.class));
 		
 		showRoadrunnerNotification(context);
 	}
 	
 	public void stopAllServices(Context context)  {
 		context.stopService(new Intent(context, MonitoringService.class));
 		context.stopService(new Intent(context, LoggingService.class));
 		context.stopService(new Intent(context, ReplicatorService.class));
 		
 		CouchDBService.stopCouchDB(context);
 	}
 }
