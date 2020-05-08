 package net.kevxu.senselib;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 
 /**
  * Manager class. It makes sure only one instance of LocationService, 
  * OrientationService and/or StepDetector is created. Use get*Instance() methods
  * to get an instance of the service you needed, register your listener, then
  * call start() to start the SenseLib. After SenseLib is no longer needed, 
  * remember to call stop().
  * 
  * @author Kaiwen Xu
  */
 public class Sense {
 	
 	/**
 	 * Orientation service.
 	 */
 	public static final int SERVICE_ORIENTATION = 0x1;
 	
 	/**
 	 * Step detector service. Since it relies on orientation service, orientation
 	 * service is implied.
 	 */
 	public static final int SERVICE_STEP_DETECTOR = 0x3;
 	
 	/**
 	 * Location service. Since it relies on both orientation service and step
 	 * detector service, both of them are implied.
 	 */
 	public static final int SERVICE_LOCATION = 0x7;
 	
 	/**
 	 * All services available.
 	 */
 	public static final int SERVICE_ALL = 0xFFFFFFFF;
 
 	private Context mContext;
 	
 	private List<SensorService> mServices;
 
 	private OrientationService mOrientationService;
 	private StepDetector mStepDetector;
 	private LocationService mLocationService;
 	
 	/**
	 * Constructor for SenseLib. Enable all available services.
 	 * 
 	 * @param context Context.
 	 * @throws SensorNotAvailableException
 	 * 				If sensor required by a specific service is not present,
 	 * 				SensorNotAvailableException will be thrown. 
 	 */
 	public Sense(Context context) throws SensorNotAvailableException {
 		this(context, SERVICE_ALL);
 	}
 	
 	/**
 	 * Constructor for SenseLib. Choose the services you want to enable.
 	 * 
 	 * @param context Context.
 	 * @param services
 	 * 				Bit masked argument. Choose the services you want to enable.
 	 * 				For example, if you want to enable orientation service and
 	 * 				location service, you can pass the argument as
 	 * 				SERVICE_ORIENTATION|SERVICE_LOCATION.
 	 * @throws SensorNotAvailableException
 	 * 				If sensor required by a specific service is not present,
 	 * 				SensorNotAvailableException will be thrown. 
 	 */
 	public Sense(Context context, int services) throws SensorNotAvailableException {
 		mContext = context;
 		mServices = new ArrayList<SensorService>();
 
 		if ((services & SERVICE_ORIENTATION) == SERVICE_ORIENTATION) {
 			mOrientationService = new OrientationService(mContext);
 			mServices.add(mOrientationService);
 		}
 		
 		if ((services & SERVICE_STEP_DETECTOR) == SERVICE_STEP_DETECTOR) {
 			mStepDetector = new StepDetector(mContext, mOrientationService);
 			mServices.add(mStepDetector);
 		}
 		
 		if ((services & SERVICE_LOCATION) == SERVICE_LOCATION) {
 			mLocationService = new LocationService(mContext, mStepDetector);
 			mServices.add(mLocationService);
 		}
 	}
 
	/**
	 * Call to start all the services you chose.
	 */
 	public void start() {
 		for (SensorService service : mServices) {
 			if (service != null) {
 				service.start();
 			}
 		}
 	}
 
	/**
	 * Call to stop all the services you chose.
	 */
 	public void stop() {
 		for (SensorService service : mServices) {
 			if (service != null) {
 				service.stop();
 			}
 		}
 	}
 
 	public OrientationService getOrientationServiceInstance() {
 		return mOrientationService;
 	}
 
 	public StepDetector getStepDetectorInstance() {
 		return mStepDetector;
 	}
 
 	public LocationService getLocationServiceInstance() {
 		return mLocationService;
 	}
 	
 	// Clean up if you forgot to do so.
 	@Override
 	protected void finalize() {
 		stop();
 	}
 
 }
