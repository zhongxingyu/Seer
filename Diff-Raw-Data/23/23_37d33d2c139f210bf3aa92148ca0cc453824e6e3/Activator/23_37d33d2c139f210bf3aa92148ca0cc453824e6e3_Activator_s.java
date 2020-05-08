package motherbug3;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.log.LogService;
 
 
 import com.buglabs.application.ServiceTrackerHelper;
 import com.buglabs.bug.module.camera.pub.ICamera2Device;
 import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
 import org.osgi.util.tracker.ServiceTracker;
 
 
 public class Activator implements BundleActivator {
 	private static LogService logger = null;
 	private static final String [] services = {					
 		ICamera2Device.class.getName(),
 		ICameraModuleControl.class.getName(),
 		HttpService.class.getName()
 	};	
 	private ServiceTracker serviceTracker;
 	public void start(BundleContext context) throws Exception {
 		// TODO Auto-generated method stub
 		MotherBugApp app = new MotherBugApp (context);
 		serviceTracker = ServiceTrackerHelper.openServiceTracker(context, services, app);
 	
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		// TODO Auto-generated method stub
 		serviceTracker.close();
 	}
 
 	public static Object getLogger() {
 		// TODO Auto-generated method stub
 		return logger;
 	}
 }
