 package it.giacomos.android.osmer.gcm;
 
 import it.giacomos.android.osmer.OsmerActivity;
 import it.giacomos.android.osmer.R;
 import it.giacomos.android.osmer.network.state.Urls;
 import it.giacomos.android.osmer.preferences.Settings;
 import it.giacomos.android.osmer.rainAlert.SyncImages;
 import it.giacomos.android.osmer.rainAlert.SyncImagesListener;
 import it.giacomos.android.osmer.service.RadarSyncAndRainGridDetectService;
 import it.giacomos.android.osmer.service.sharedData.NotificationData;
 import it.giacomos.android.osmer.service.sharedData.NotificationDataFactory;
 import it.giacomos.android.osmer.service.sharedData.RainNotification;
 import it.giacomos.android.osmer.service.sharedData.ReportRequestNotification;
 import it.giacomos.android.osmer.service.sharedData.ServiceSharedData;
 
 import java.util.ArrayList;
 
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class GcmBroadcastReceiver extends BroadcastReceiver implements SyncImagesListener
 {
 	@Override
 	public void onReceive(Context ctx, Intent intent) 
 	{
 		Bundle extras = intent.getExtras();
 		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
 		// The getMessageType() intent parameter must be the intent you received
 		// in your BroadcastReceiver.
 		String messageType = gcm.getMessageType(intent);
 
 		if (!extras.isEmpty()) 
 		{  // has effect of unparcelling Bundle
 			/*
 			 * Filter messages based on message type. Since it is likely that GCM
 			 * will be extended in the future with new message types, just ignore
 			 * any message types you're not interested in, or that you don't
 			 * recognize.
 			 */
 			if (GoogleCloudMessaging.
 					MESSAGE_TYPE_SEND_ERROR.equals(messageType)) 
 			{
 				Log.e("GcmBroadcastReceiver.onReceive" , extras.toString());
 			} 
 			else if (GoogleCloudMessaging.
 					MESSAGE_TYPE_DELETED.equals(messageType)) 
 			{
 				Log.e("GcmBroadcastReceiver.onReceive" , extras.toString());
 
 			}
 			else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) 
 			{
 				boolean notified = false;
 				String dataAsString = intent.getExtras().getString("message");
 				long timestampSeconds = 0;
 				long currentTimestampSecs = System.currentTimeMillis() / 1000;
 				/* many notifications may have been queued and delivered within a short time.
 				 * Check that two notifications do not arrive too close to each other.
 				 */
 				Settings s = new Settings(ctx);
 				boolean useInternalRainDetection = s.useInternalRainDetection();
 				boolean rainNotificationEnabled = s.rainNotificationEnabled();
 				try
 				{	
 					timestampSeconds = Long.parseLong(intent.getExtras().getString("timestamp"));
 				}
 				catch(NumberFormatException e)
 				{
 
 				}
 
 				ServiceSharedData sharedData = ServiceSharedData.Instance(ctx);
 				NotificationManager mNotificationManager =
 						(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
 
 				NotificationDataFactory notificationDataFactory = new NotificationDataFactory();
 				if(useInternalRainDetection && rainNotificationEnabled && 
 						notificationDataFactory.isNewRadarImageNotification(dataAsString))
 				{
 					if(currentTimestampSecs - timestampSeconds < 1 * 60) /* the notification has been recently sent */
 					{
 						Log.e("GcmBroadcastReceiver.onReceive", "****** RADAR SYNC ******* data timestamp "
 								+ timestampSeconds + " is " + (currentTimestampSecs - timestampSeconds) + " seconds old " + 
 								" current ts is " + currentTimestampSecs);
 
 						/* Start the radar sync and rain detect service. It will compare the two last radar images
 						 * and try to guess if it is going to rain in the next future. The service takes care of
 						 * notifications when calculations are completed.
 						 */
 						Intent radarSyncRainDetectService = new Intent(ctx, RadarSyncAndRainGridDetectService.class);
 						radarSyncRainDetectService.putExtra("timestamp", timestampSeconds);
 						ctx.startService(radarSyncRainDetectService);
 					}
 					else
 						Log.e("GcmBroadcastReceiver.onReceive", "!!! RADAR SYNC !!! data timestamp "+ timestampSeconds + " is " 
 								+ (currentTimestampSecs - timestampSeconds) + " seconds: TOO old");
 				}
 				else
 				{
 					ArrayList<NotificationData> notifications = notificationDataFactory.parse(dataAsString);
 					for(NotificationData notificationData : notifications)
 					{
 						/* Rain alert notifications are marked valid only if they represent an alert 
 						 * (there's a chance it's going to rain).
 						 */
 						String message = "";
 						int iconId = -1, ledColor = 0;
 						// Creates an explicit intent for an Activity in your app
 						Intent resultIntent = null;
 
 						if(notificationData.isValid() && notificationData.isRainAlert())
 						{
 							/* internally managed? discard rain notification data here */
 							if(!useInternalRainDetection && rainNotificationEnabled)
 							{
 								RainNotification rainNotif = (RainNotification) notificationData;
 								if(!rainNotif.IsGoingToRain())
 								{
 									Log.e("GcmBroadcastReceiver.onReceive", "rain alert notification to be cancelled");
 									mNotificationManager.cancel(rainNotif.getTag(), rainNotif.getId());
 									Log.e("GcmBroadcastReceiver.onReceive", "RAIN notification setting notified " + notificationData.getTag() + ", " + notified);
 									sharedData.updateCurrentRequest(notificationData, notified);
 								}
 								else
 								{
 									iconId = R.drawable.ic_launcher_statusbar_rain;
 									ledColor = Color.RED; /* red notification */
 									float dbZ = rainNotif.getLastDbZ();
 									/* allocate a result intent */
 									resultIntent = new Intent(ctx, OsmerActivity.class);
 									resultIntent.putExtra("NotificationRainAlert", true);
 
 									if(dbZ < 27)
 									{
 										message = ctx.getResources().getString(R.string.notificationRainAlert);
 									}
 									else if(dbZ < 42)
 									{
 										message = ctx.getResources().getString(R.string.notificationRainModerate);
 									}
 									else
 									{
 										message = ctx.getResources().getString(R.string.notificationRainIntense);
 									}
 								}
 							}
 						} /* end rain notification alert */
 						else if(notificationData.isValid()) /* not a rain alert */
 						{
 							boolean alreadyNotifiedEqual = sharedData.alreadyNotifiedEqual(notificationData);
 							boolean arrivesTooLate = sharedData.arrivesTooLate(notificationData);
 							if(!alreadyNotifiedEqual && !arrivesTooLate && !sharedData.arrivesTooEarly(notificationData, ctx))
 
 							{
 								Log.e("GcmBroadcastReceiver.onReceive", "notification can be considereth new " + notificationData.username);
 								/* allocate a result intent */
 								resultIntent = new Intent(ctx, OsmerActivity.class);
 								/* request or report */
 								if(notificationData.isRequest())
 								{
 									resultIntent.putExtra("NotificationReportRequest", true);
 									ReportRequestNotification rrnd = (ReportRequestNotification) notificationData;
									message = ctx.getResources().getString(R.string.notificationNewReportRequest) 
 											+ " " + notificationData.username;
 									if(rrnd.locality.length() > 0)
 										message += " - " + rrnd.locality;
 
 									iconId = R.drawable.ic_launcher_statusbar_request_new;
 									ledColor = Color.BLUE; /* blue notification */
 								}
 
 								else
 								{
 									resultIntent.putExtra("NotificationReport", true);
 									message = ctx.getResources().getString(R.string.notificationNewReportArrived) 
 											+ " "  + notificationData.username;
 									iconId = R.drawable.ic_launcher_statusbar_report_new;
 									ledColor = Color.GREEN;
 								}
 							}
 						}
 						else /* notification is not valid */
 						{
 							Toast.makeText(ctx, "Notification not valid! " + 
 									dataAsString, Toast.LENGTH_LONG).show();
 						}
 
 						/* if we entered one of the cases above, resultIntent will be not null
 						 * We can build and post the notification. 
 						 */
 						if(resultIntent != null)
 						{
 							/* latitude and longitude needed in every kind of notification */
 							resultIntent.putExtra("ptLatitude", notificationData.latitude);
 							resultIntent.putExtra("ptLongitude", notificationData.longitude);
 
 							int notificationFlags = Notification.DEFAULT_SOUND| Notification.FLAG_SHOW_LIGHTS;
 							NotificationCompat.Builder notificationBuilder =
 									new NotificationCompat.Builder(ctx)
 							.setSmallIcon(iconId)
 							.setAutoCancel(true)
 							.setTicker(message)
 							.setLights(ledColor, 1000, 1000)
 							.setContentTitle(ctx.getResources().getString(R.string.app_name))
 							.setContentText(message).setDefaults(notificationFlags);
 
 							// The stack builder object will contain an artificial back stack for the
 							// started Activity.
 							// This ensures that navigating backward from the Activity leads out of
 							// your application to the Home screen.
 							TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
 							// Adds the back stack for the Intent (but not the Intent itself)
 							stackBuilder.addParentStack(OsmerActivity.class);
 							// Adds the Intent that starts the Activity to the top of the stack
 							stackBuilder.addNextIntent(resultIntent);
 
 							PendingIntent resultPendingIntent =
 									stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT);
 
 							notificationBuilder.setContentIntent(resultPendingIntent);
 							notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
 							// mId allows you to update the notification later on.
 
 							int notifId = notificationData.getId();
 							String notifTag = notificationData.getTag();
 							Notification notification = notificationBuilder.build();
 							/* remove previous similar notifications if present */
 							mNotificationManager.notify(notifTag, notifId,  notification);
 							notified = true;
 							/* update notification data */
 							Log.e("GcmBroadcastReceiver.onReceive", "notification setting notified " + notificationData.getTag() + ", " + notified);
 							sharedData.updateCurrentRequest(notificationData, notified);
 						}
 					} /* for(NotificationData notificationData : notifications) */
 				} 
 			} /* else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))  */
 		}  /* !extras.isEmpty */
 	} 
 
 
 
 	@Override
 	public void onImagesSynced(String[] filepaths) 
 	{
 		if(filepaths != null)
 		{
 			Log.e("GcmBroadcastReceiver.onImagesSynced", " saved images " + filepaths[0] + " and " + filepaths[1]);
 		}
 		else
 			Log.e("GcmBroadcastReceiver.onImagesSynced", " failed to saved images!");
 
 	}
 
 }
