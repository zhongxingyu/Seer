 package com.arpia49;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.Toast;
 
 public class Evento {
 	private static Context contexto = null;
 	private int id;
 	private int fuerte;
 	private static Handler handler=null;
     private final int NOTIFICATION_ID = 1;
 
 	public Evento(final int id, final Boolean fuerte, Activity val) {
 		contexto = val.getApplicationContext();
 		this.id = id;
 		if(fuerte){
 			this.fuerte = 105;
 		}else{
 			this.fuerte = 80;
 		}
 		if(handler==null){
 			handler = new Handler() {
 				@Override
 				public void handleMessage(Message msg) {
 					Notificacion nuevaNotificacion = new Notificacion.Builder(
 							System.currentTimeMillis(),
							ListaAlarmas.element(msg.what).getNombre(),
							ListaAlarmas.element(msg.what).getUbicacion(),
							ListaAlarmas.element(msg.what).getId())
 							.build(true);
 					triggerNotification();
 					Toast.makeText(contexto, "¡Alarma detectada!",
 							Toast.LENGTH_SHORT).show();
 				}
 			};
 		}
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public int getFuerte() {
 		return fuerte;
 	}
 
 	public static Handler getHandler() {
 		return handler;
 	}
 	
     private void triggerNotification()
     {
         CharSequence title = "Timbre 2.0";
         CharSequence message = "Click para ver las notificaciones de alertas";
  
         NotificationManager notificationManager = (NotificationManager)contexto.getSystemService(contexto.NOTIFICATION_SERVICE);
         Notification notification = new Notification(R.drawable.icon, "¡Alarma detectada!", System.currentTimeMillis());
         notification.defaults |= Notification.DEFAULT_VIBRATE;
         notification.defaults |= Notification.DEFAULT_LIGHTS;
 
         Intent notificationIntent = new Intent(contexto, VistaNotificaciones.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(contexto, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_AUTO_CANCEL);
  
         notification.setLatestEventInfo(contexto, title, message, pendingIntent);
         notificationManager.notify(NOTIFICATION_ID, notification);
     }
 }
