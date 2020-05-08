 package org.centny.jetty4a;
 
 import org.centny.cny4a.util.Util;
 import org.centny.jetty4a.server.ADnsDynamic;
 import org.centny.jetty4a.server.J4AServer;
 import org.centny.jetty4a.server.JettyCfgAndroid;
 import org.centny.jetty4a.server.api.JettyServer;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;
 
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.app.Notification.Builder;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContextWrapper;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 
 public class J4AService extends Service {
 	static {
 		JettyCfgAndroid.loadAll();
 	}
 
 	public static enum ServerStatus {
 		Stopped, Stopping, Starting, Started
 	}
 
 	private static int listenPort__ = 0;
 	private static J4AServer sharedServer__;
 	private static ServerStatus serverStatus__ = ServerStatus.Stopped;
 	private static Handler handler = null;
 
 	//
 	public static String localListener(ContextWrapper ctx) {
 		String ip = Util.localIpAddress(ctx);
 		if (ip == null || sharedServer__ == null || sharedServer__.isStopped()) {
 			ip = "0.0.0.0";
 		}
 		return ip + ":" + listenPort();
 	}
 
 	public static void send(ServerStatus status) {
 		serverStatus__ = status;
 		if (handler == null) {
 			return;
 		}
 		try {
 			Message msg = new Message();
 			msg.obj = status;
 			handler.sendMessage(msg);
 		} catch (Exception e) {
 
 		}
 	}
 
 	public static boolean isSharedServerStartted() {
 		if (sharedServer__ == null) {
 			return false;
 		} else {
 			return sharedServer__.isStarted();
 		}
 	}
 
 	public static int listenPort() {
 		return listenPort__;
 	}
 
 	public static ServerStatus serverStatus() {
 		return serverStatus__;
 	}
 
 	public static void setHandler(Handler h) {
 		handler = h;
 		send(serverStatus__);
 	}
 
 	private Logger log = Log.getLogger(J4AService.class);
 
 	@Override
 	public void onCreate() {
 	}
 
 	@SuppressLint("NewApi")
 	private void displayNotificationMessage(String message) {
 		Intent itn = new Intent(Intent.ACTION_MAIN);
 		itn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		itn.addCategory(Intent.CATEGORY_LAUNCHER);
 		itn.setClass(this, JettyActivity.class);
 		PendingIntent pit = PendingIntent.getActivity(this, 0, itn, 0);
 		Builder builder = new Notification.Builder(this);
 		builder.setContentIntent(pit);
 		builder.setSmallIcon(R.drawable.ic_launcher);
 		builder.setWhen(System.currentTimeMillis());
 		builder.setAutoCancel(true);
 		builder.setContentTitle("J4A Server");
 		builder.setContentText(message);
 		startForeground(108080, builder.build());
 	}
 
 	private void dimissNotificationMessage() {
 		stopForeground(true);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (isSharedServerStartted()) {
 			return 0;
 		}
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					JettyCfgAndroid.initJServerWs(getApplicationContext());
 					try {
 						try {
 							String sport = System
 									.getProperty("J4A_LISTEN_PORT");
 							listenPort__ = Integer.parseInt(sport);
 						} catch (Exception e) {
 							listenPort__ = 8080;
 							log.debug("listen port configure not found,using default:"
 									+ listenPort__);
 						}
 						sharedServer__ = (J4AServer) JettyServer
 								.createServer(J4AServer.class);
 						QueuedThreadPool threadPool = new QueuedThreadPool();
 						threadPool.setMaxThreads(10);
 						SelectChannelConnector connector = new SelectChannelConnector();
 						connector.setThreadPool(threadPool);
 						connector.setPort(listenPort__);
 						sharedServer__.addConnector(connector);
 						log.info("initial shared server in port:"
 								+ listenPort__);
 					} catch (Exception e) {
 						log.warn("initial shared server error", e);
 						sharedServer__ = null;
 					}
 					ADnsDynamic dd = ADnsDynamic.sharedInstance();
 					// dd.setHost("git.dnsd.me");
 					// dd.setUsr("centny@gmail.com");
 					// dd.setPwd("wsh123456");
 					// dd.setPeriod(300000);
 					dd.loadDnsConfig();
 					dd.loadExtListener();
 					dd.startTimer();
 					dd.startNetworkListener(J4AService.this);
 					send(ServerStatus.Starting);
 					sharedServer__.start();
 					send(ServerStatus.Started);
 					displayNotificationMessage("Jetty Server Running...");
 				} catch (Exception e) {
 					send(ServerStatus.Stopped);
 					log.warn("start server error", e);
 				}
 			}
 		}).start();
 
 		return 0;
 	}
 
 	@Override
 	public void onDestroy() {
 		if (sharedServer__ == null) {
 			this.log.warn("shared server is not initial,call createSharedServer first");
 			return;
 		}
 		if (!sharedServer__.isStarted()) {
 			this.log.warn("shared server is not started,call start first");
 			return;
 		}
 		try {
 			send(ServerStatus.Stopping);
 			sharedServer__.stop();
 			sharedServer__.join();
 			send(ServerStatus.Stopped);
 		} catch (Exception e) {
 			send(ServerStatus.Stopped);
 			this.log.warn("stop server error", e);
 		}
 		dimissNotificationMessage();
 		ADnsDynamic.sharedInstance().stopNetworkListener();
		ADnsDynamic.sharedInstance().stopTimer();
 	}
 }
