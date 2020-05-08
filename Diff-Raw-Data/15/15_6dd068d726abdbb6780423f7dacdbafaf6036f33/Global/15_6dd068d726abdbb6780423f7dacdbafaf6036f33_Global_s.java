 import com.errplane.api.Errplane;
 import com.errplane.examples.standalone.ErrplaneFlusher;
 
 import play.Application;
 import play.GlobalSettings;
 
 public class Global extends GlobalSettings {
	
 	private ErrplaneFlusher flusher;
	
 	@Override
 	public void onStart(Application app) {
 
 		// try to initialize Errplane
 		if (!initErrplane()) {
 			System.out.println("Errplane initialization failed!");
 			System.out.println("Make sure you have set the environment variables: EP_APP, EP_API, and EP_ENV!");
 			return;
 		}
 
 		// fire up the flusher
 		flusher = new ErrplaneFlusher();
 		flusher.startFlusher();
 
 		// fire up the heartbeat at the default 30 second interval
 		flusher.heartbeat("errplane-java/playApplicationHB");
		
        Errplane.report("errplane-java/playApplicationStartup");
 	}  
 
 	@Override
 	public void onStop(Application app) {
        Errplane.report("errplane-java/playApplicationShutdown");
        
 		// clear Errplane reports
 		flusher.stopFlusher();
 	}
 
 	private static boolean initErrplane() {
 
 		String appKey = System.getenv("EP_APP");
 		String apiKey = System.getenv("EP_API");
 		String env = System.getenv("EP_ENV");
 		return Errplane.init(appKey, apiKey, env);
 	}
 }
