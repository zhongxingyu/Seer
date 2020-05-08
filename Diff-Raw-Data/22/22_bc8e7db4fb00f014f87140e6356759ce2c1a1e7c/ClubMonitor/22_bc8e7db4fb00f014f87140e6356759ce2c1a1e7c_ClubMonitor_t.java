 package entropia.clubmonitor;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.Locale;
import java.util.Timer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class ClubMonitor {
     static {
 	Config.loadDefaults();
     }
     private static final Logger logger = LoggerFactory.getLogger(ClubMonitor.class);
     private static Thread xmppThread;
     private static Thread fileHandlerThread;
     private static Thread netioPoller;
     private static Thread mpdThread;
     private static Thread clubBusTriggerThread;
    private static Timer fhemTriggerTimer;
     private static WebServer webServer;
     
     private static final SyncService SYNC_SERVICE = new SyncService();
     
     public static void start(String[] args) throws Exception {
 	if (args.length == 1) {
 	    loadConfig(new File(args[0]));
 	} else if (args.length > 1) {
 	    throw new IllegalArgumentException();
 	}
 	Config.startupCheck();
 	setupPerThreadExceptionHandler();
 	if (Config.isSSLEnabled()) {
 	    SSLConfig.setupSSL();
 	}
 	TernaryStatusRegister.OVERRIDE_WINDOWS.off();
 	
 	if (Config.isNetIOEnabled()) {
 	    netioPoller = HauptRaumNetIOHandler.startNetIOPoller(SYNC_SERVICE);
 	}
 	if (Config.isXMPPEnabled()) {
 	    xmppThread = XMPPThread.startXMPPThread();
 	    fileHandlerThread = FileHandlerThread.startFileHandlerThread();
 	}
 	if (Config.isMPDEnabled()) {
 	    mpdThread = MpdNotifier.startMpdNotifierThread();
 	}
 	if (Config.isClubBusEnabled()) {
 	    clubBusTriggerThread = ClubBusTrigger.startClubBusTrigger();
 	}
 	if (Config.isFhemEnabled()) {
	    fhemTriggerTimer = FhemTimerTask.startFhemTrigger();
 	}
 	webServer = new WebServer(SYNC_SERVICE);
 	webServer.startWebServer();
     }
 
     private static void setupPerThreadExceptionHandler() {
         Thread.setDefaultUncaughtExceptionHandler(
                 new UncaughtExceptionHandler() {
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 logger.error(t.toString(), e);
             }
         });
     }
 
     private static void loadConfig(File properties) throws IOException {
 	if (!properties.canRead() || !properties.isFile()) {
 	    throw new IllegalArgumentException("config file could not be read");
 	}
 	Config.load(properties);
     }
     
     // currently unused
     public static void stop() {
 	if (xmppThread != null) {
 	    xmppThread.interrupt();
 	}
 	if (netioPoller != null) {
 	    netioPoller.interrupt();
 	}
 	if (fileHandlerThread != null) {
 	    fileHandlerThread.interrupt();
 	}
 	if (mpdThread != null) {
 	    mpdThread.interrupt();
 	}
 	if (webServer != null) {
 	    webServer.stopWebServer();
 	}
 	if (clubBusTriggerThread != null) {
 	    clubBusTriggerThread.interrupt();
 	}
	if (fhemTriggerTimer != null) {
	    fhemTriggerTimer.cancel();
 	}
     }
     
     public static void main(String[] args) throws Exception {
         Locale.setDefault(Locale.US);
 	if (args.length == 1 && "--print-template".equals(args[0])) {
 	    System.out.println(Config.getConfigTemplate());
 	    return;
 	}
 	ClubMonitor.start(args);
     }
 }
