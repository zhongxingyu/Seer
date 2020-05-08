 package com.tassadar.lorrismobile;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 import android.util.SparseArray;
 
 import com.tassadar.lorrismobile.connections.Connection;
 import com.tassadar.lorrismobile.modules.Tab;
 
 public class SessionService extends Service {
 
     public interface SessionServiceListener {
         public void onConnsLoad(ArrayList<ContentValues> values);
         public void onTabsLoad(ArrayList<ContentValues> values);
     }
 
     public class SessionBinder extends Binder {
         SessionService getService() {
             return SessionService.this;
         }
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return m_binder;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         return START_STICKY;
     }
 
     @Override
     public void onCreate() {
         Log.i("Lorris", "Starting SessionService");
 
         m_notificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
         showNotification();
     }
 
     @Override
     public void onDestroy() {
         m_notificationMgr.cancel(R.string.service);
     }
 
     private void showNotification() {
         String text = String.format(getText(R.string.service).toString(),
                 SessionMgr.getActiveSession().getName());

         Notification notification = new Notification(R.drawable.ic_launcher, text,
                 System.currentTimeMillis());
 
         notification.flags |= Notification.FLAG_ONGOING_EVENT;
 
         Intent intent = new Intent(this, WorkspaceActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
 
         notification.setLatestEventInfo(this, getText(R.string.app_name),
                        text, contentIntent);
 
         startForeground(R.string.service, notification);
     }
 
     public void saveSession(Session s, SparseArray<Tab> tabs,
             SparseArray<Connection> conns) {
             Log.i("Lorris", "Spawning save thread\n");
 
             for(int i = 0; i < conns.size(); ++i)
                 conns.valueAt(i).addRef();
             new SaveSessionThread(s, tabs, conns).start();
     }
 
     private class SaveSessionThread extends Thread {
         private SparseArray<Tab> m_tabs;
         private SparseArray<Connection> m_conns;
         private Session m_session;
 
         public SaveSessionThread(Session s, SparseArray<Tab> tabs,
                 SparseArray<Connection> conns) {
             m_tabs = tabs;
             m_conns = conns;
             m_session = s;
         }
 
         @Override
         public void run() {
             synchronized(m_session) {
                 m_session.acquireDBRef();
                 m_session.saveBase();
                 m_session.saveTabs(m_tabs);
                 m_session.saveConns(m_conns);
                 m_session.releaseDBRef();
 
                 for(int i = 0; i < m_conns.size(); ++i)
                     m_conns.valueAt(i).rmRef();
 
                 Log.i("Lorris", "Session " + m_session.getName() + " saved\n");
             }
         }
     }
 
     public void loadSession(Session s, SessionServiceListener listener) {
         new LoadSessionThread(s, listener).start();
     }
 
     private class LoadSessionThread extends Thread {
         private Session m_session;
         private WeakReference<SessionServiceListener> m_listener;
 
         public LoadSessionThread(Session s, SessionServiceListener listener) {
             m_session = s;
             m_listener = new WeakReference<SessionServiceListener>(listener);
         }
 
         @Override
         public void run() {
             ArrayList<ContentValues> values = m_session.loadConnections();
 
             SessionServiceListener listener = m_listener.get();
             if(listener != null)
                 listener.onConnsLoad(values);
 
             values = m_session.loadTabs();
             listener = m_listener.get();
             if(listener != null)
                 listener.onTabsLoad(values);
         }
     }
 
     private final IBinder m_binder = new SessionBinder();
     private NotificationManager m_notificationMgr;
 }
