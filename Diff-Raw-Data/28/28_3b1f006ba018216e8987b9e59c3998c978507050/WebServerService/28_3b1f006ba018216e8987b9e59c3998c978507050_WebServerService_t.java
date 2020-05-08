 package info.guardianproject.iocipher.server;
 
 
 import java.io.File;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 public class WebServerService extends Service
 {
 	private final static String TAG = "IOCipherServer";
 
 	private MdnsManager mdns;
 	
 	 // Binder given to clients
     private final IBinder mBinder = new LocalBinder();
 
     private Thread mWsThread;
     
 
 	private int mPort;
 	private boolean mUseSsl;
	private String mIpAddress;
	
     /**
      * Class used for the client Binder.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with IPC.
      */
     public class LocalBinder extends Binder {
     	WebServerService getService() {
             // Return this instance of LocalService so clients can call public methods
             return WebServerService.this;
         }
     }
 
     @Override
     public IBinder onBind(Intent intent) {
     	
     	
     	
         return mBinder;
     }
 	
 	class MyServ extends Acme.Serve.Serve {
 		// Overriding method for public access
                     public void setMappingTable(PathTreeDictionary mappingtable) { 
                           super.setMappingTable(mappingtable);
                     }
                     // add the method below when .war deployment is needed
                     public void addWarDeployer(String deployerFactory, String throttles) {
                           super.addWarDeployer(deployerFactory, throttles);
                     }
             };
             
 	MyServ srv;
 
 	public MyServ getWebServer ()
 	{
 		return srv;
 	}
 	
 	private void startNotification ()
 	{
 		//This constructor is deprecated. Use Notification.Builder instead
		Notification notice = new Notification(R.drawable.iocipher, "Active: " + mIpAddress, System.currentTimeMillis());
 
 		Intent intent = new Intent(WebServerService.this, IOCipherServerActivity.class);
 
 		PendingIntent pendIntent = PendingIntent.getActivity(WebServerService.this, 0, intent, 0);
 
 		//This method is deprecated. Use Notification.Builder instead.
		notice.setLatestEventInfo(WebServerService.this, "IOCipherServer", "Active: " + mIpAddress, pendIntent);
 
 		notice.flags |= Notification.FLAG_NO_CLEAR;
 		
 		startForeground(mPort,notice);
	
 	}
	
	public void startServer (int port, boolean useSSL, String ipAddress) throws Exception
 	{
 		
 		mPort = port;
 		mUseSsl = useSSL;
		mIpAddress = ipAddress;
 		
 		startNotification();
 		
 		mWsThread = new Thread ()
 		{
 			
 			public void run ()
 			{
 				try
 				{
 					if (mdns == null)
 						mdns = new MdnsManager(WebServerService.this);
 					
 					mdns.register("iocs", "_webdavs._tcp.local", "iocipherwebdav", 8888, "path=/sdcard");
 					mdns.register("iocs-https", "_https._tcp.local", "iocipherweb", 8888, "path=/public");
 			
 					
 					srv = new MyServ();
 					
 					File filePublic = new java.io.File("/sdcard/public");
 					
 					if (!filePublic.exists())
 					{
 						filePublic.mkdir();
 					}
 					
 			 		// setting aliases, for an optional file servlet
 		            Acme.Serve.Serve.PathTreeDictionary aliases = new Acme.Serve.Serve.PathTreeDictionary();
 		            aliases.put("/public/*", filePublic);
 		            aliases.put("/public/*", filePublic);
 		            
 		            
 			//  note cast name will depend on the class name, since it is anonymous class
 		            srv.setMappingTable(aliases);
 					// setting properties for the server, and exchangeable Acceptors
 					java.util.Properties properties = new java.util.Properties();
 					properties.put("port", mPort);
 					properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
 					
 					if (mUseSsl)
 					{
 						properties.setProperty("secure", "true");
 						//properties.setProperty("socketFactory", "Acme.Serve.SSLServerSocketFactory");
 						properties.setProperty("acceptorImpl", "Acme.Serve.SSLAcceptor");
 						
 						File fileKS = new File(WebServerService.this.getFilesDir(),"iocipher.bks");
 						String password = "changeme";
 						
 						if (!fileKS.exists())
 						{
 							String alias = "twjs";
 							String cn = "localhost";
 							String on = "iocipher";
 							
 							KeyStoreGenerator.generateKeyStore(fileKS, alias, 2048, password, cn, on, on, "New York", "New York", "US");
 					
 						}
 						
 				
 						properties.setProperty("keystoreFile",fileKS.getAbsolutePath());
 						properties.setProperty("keystorePass",password);
 						properties.setProperty("keystoreType",CACertManager.KEYSTORE_TYPE);
 					}
 					
 					srv.arguments = properties;
 					
 					srv.addServlet("/*", new FileServlet());
 
 //					srv.addDefaultServlets(null); // optional file servlet
 					
 					String davUser = "admin";
 					String davPwd = "admin";
 					DavServlet dServlet = new DavServlet(new File("/sdcard"),"sdcard", davUser, davPwd);
 					
 					srv.addServlet("/sdcard/*", dServlet);
 					
 					srv.serve();
 				}
 				catch (Exception e)
 				{
 					handleException ("error starting server", e);
 				}
 		
 			}
 		};
 		
 		mWsThread.start();
 
 		
 	}
 	
 	private void handleException (String msg, Exception e)
 	{
 		Log.e(TAG, msg, e);
 	}
 	
 	public void stopServer ()
 	{
 		
 
     	if (mWsThread.isAlive())
     	{
     		mWsThread.interrupt();
     		mWsThread = null;
     	}
     	
 		if (srv != null)
 		{
 			srv.notifyStop();
 			srv = null;
 		}
 		
 		if (mdns != null)
 		{
 			mdns.unregister("iocs");
 			mdns.unregister("iocs-https");
 		}
 		
 		stopForeground(true);
 		
 	}
 
 	public boolean isServerRunning ()
 	{
 		if (mWsThread != null && mWsThread.isAlive())
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		stopServer ();
 	}
 
 	/*
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		
 		if (srv == null)
 		{
 			new Thread(this).start();
 			
 		}
 		
 		return Service.START_STICKY;
 	}*/
 }
