 package com.devcamp.messagetimer;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import com.devcamp.messagetimer.model.Message;
 import com.devcamp.messagetimer.sender.Sender;
 import com.devcamp.messagetimer.tools.Database;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 public class MessageBroadcastReceiver extends BroadcastReceiver
 {
 
 	@Override
 	public void onReceive(final Context context, final Intent intent)
 	{
 		new Thread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				
 				try
 				{
 					onReceiveImpl(context,intent);
 				}
 				catch(Exception e)
 				{
 					//TODO whatTODO
 				}
 			}
 		},"MessageReceiver.onReceive").start();
 	}
 	
 	private void onReceiveImpl(Context context, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException
 	{
 		Throwable error = null;
 		Message m = null;
 		try
 		{
 			Long id = intent.getLongExtra(MT.MESSAGE_ID, 0);
 			if(id != 0)
 			{
 				Database d = new Database(context);
 				m = d.getMessages(id).get(0);
 				sendMessage(context, m);
 			}
 		}
 		catch(Throwable t)
 		{
 			error = t;
 		}
 		showNotification(context, m, error);
 	}
 	
 	private void sendMessage(Context context,Message m) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException
 	{
 		Sender s = getSender(context, m.service);
 		s.sendMessage(m.phoneNumber, m.message);
 	}
 	
 	private Sender getSender(Context context, String service) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException
 	{
 		Class c = Class.forName(service);
 //		Sender s = (Sender) c.newInstance();
 		Constructor cst = c.getConstructors()[0];
 		Sender s = (Sender) cst.newInstance(context);
 		return s;
 	}
 	
 	/**
 	 * Shows notification
 	 * @param c 
 	 * @param m message which has been sent
 	 * @param t if any error occur, pass error to notify user, can be null
 	 */
 	private void showNotification(Context c, Message m, Throwable t)
 	{
 		NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);				
 		PendingIntent pi = PendingIntent.getBroadcast(c, 0, new Intent(), 0);
 		Notification n = new Notification(android.R.drawable.stat_notify_more, m.contact, System.currentTimeMillis());	
//		n.flags = Notification.FLAG_ONGOING_EVENT;
 		String title = String.format("%s (%s)",m.contact,m.phoneNumber);
 		String summary = null;
 		if(t == null)
 			summary = c.getString(R.string.txtMessageHasBeenSent);
 		else
 			summary = "ERROR:" + t.getMessage();
 		
 		n.setLatestEventInfo(c, title, summary, pi);
 		nm.notify(1, n);
 	}
 }
