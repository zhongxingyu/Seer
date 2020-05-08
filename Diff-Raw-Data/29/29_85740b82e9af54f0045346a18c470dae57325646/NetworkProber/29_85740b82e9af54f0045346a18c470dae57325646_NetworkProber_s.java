 package com.mofirouz.javapushmail;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  *
  * @author Mo Firouz
  * @since 16/10/11
  */
 public abstract class NetworkProber {
 
     private final static int SLEEP_TIME = 5000; // wait 5secs between each probe
     private JavaPushMailAccount mail;
     private String host;
     private int port = 993;
     private String name = "NetworkProber";
     private int pingFailureCount = 0;
     private int sessionFailureCount = 0;
     private long lastBeat = -1;
    protected Timer timer;
 
     public NetworkProber(String host, int port, String accountName) {
         System.err.println("host: " + host);
         this.host = host;
         this.port = port;
         this.name = "NetworkProper-" + accountName;
     }
 
     public NetworkProber(JavaPushMailAccount mail) {
         this.mail = mail;
         this.host = mail.getServerAddress();
         this.port = mail.getServerPort();
         this.name = "NetworkProper-" + mail.getAccountName();
     }
 
     private boolean probe() {
         boolean status = true;
 
         Socket socket = new Socket();
         try {
             socket = new Socket(host, port);
             status = true;
             pingFailureCount = 0;
         } catch (Exception ex) {
             status = false;
             pingFailureCount++;
         } finally {
             if (socket != null) {
                 try {
                     socket.close();
                 } catch (Exception e) {
                 }
             }
         }
         return status;
     }
 
     private boolean probeWithSessionCheck() {
         boolean status = probe();
         if (status) {
             if (mail.isSessionValid()) {
                 sessionFailureCount = 0;
                 return true;
             } else {
                 sessionFailureCount++;
                 return false;
             }
         }
         return false;
     }
 
     private void periodicProber() {
         TimerTask task = new TimerTask() {
 
             @Override
             public void run() {
                 if (lastBeat != -1) {
                     long currentTime = System.currentTimeMillis();
                     if (currentTime - lastBeat > (SLEEP_TIME + 1)) { // missed beat, probably because of sleep...
                         lastBeat = currentTime;    
                         missedBeat();
                         return;
                     } 
                     lastBeat = currentTime;
                 } else {
                     lastBeat = System.currentTimeMillis();
                 }
                 
                 if (mail == null)
                     onNetworkChange(probe());
                 else
                     onNetworkChange(probeWithSessionCheck());
             }
         };
         stop();
         timer = new Timer(name, true);
         timer.scheduleAtFixedRate(task, Calendar.getInstance().getTime(), SLEEP_TIME);
     }
 
     public void start() {
         Runnable r = new Runnable() {
 
             public void run() {
                 periodicProber();
             }
         };
         sessionFailureCount = 0;
         pingFailureCount = 0;
         Thread t = new Thread(r, name);
         t.setDaemon(true);
         t.start();
     }
 
     public void stop() {
         if (timer == null)
             return;
 
         timer.cancel();
         timer.purge();
         timer = null;
     }
 
     public int getPingFailureCount() {
         return pingFailureCount;
     }
 
     public int getSessionFailureCount() {
         return sessionFailureCount;
     }
 
     public abstract void onNetworkChange(boolean status);
     
     public abstract void missedBeat();
 }
