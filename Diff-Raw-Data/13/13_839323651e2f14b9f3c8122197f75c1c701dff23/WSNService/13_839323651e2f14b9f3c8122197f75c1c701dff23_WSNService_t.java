 package se.kth.ssvl.tslab.wsn.service;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFActionReceiver;
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFCommunication;
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFDB;
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFLogger;
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFService;
 import se.kth.ssvl.tslab.wsn.general.bpf.exceptions.BPFException;
 import se.kth.ssvl.tslab.wsn.general.servlib.storage.Stats;
 import se.kth.ssvl.tslab.wsn.service.bpf.ActionReceiver;
 import se.kth.ssvl.tslab.wsn.service.bpf.Communication;
 import se.kth.ssvl.tslab.wsn.service.bpf.DB;
 import se.kth.ssvl.tslab.wsn.service.bpf.Logger;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.RemoteCallbackList;
 import android.os.RemoteException;
import android.os.StrictMode;
 import android.util.Log;
 import android.widget.Toast;
 
 public class WSNService extends Service implements BPFService {
 	
 	private static String TAG = "Service";
 	private Logger logger;
 	private ActionReceiver action;
 	private Communication comm;
 	private DB db;
 
 	final RemoteCallbackList<WSNServiceInterfaceCallBack> mCallbacks = 
 			new RemoteCallbackList<WSNServiceInterfaceCallBack>();
 	
 	private final WSNServiceInterface.Stub mBinder = new WSNServiceInterface.Stub() {
 		
 		//this is called from ConfigActivity right after binding
 		@Override
 		public void start(String path) throws RemoteException {
 
 			logger.debug(TAG, "Start method in Service called by ConfigActivity");
 			
 			// Init the DB object
 			db = new DB(new File(path + "/database.db"), logger);
 			
 			// Try to init the BPF
 			try {
 				BPF.init(getInstance(), path + "/dtn.config.xml");
 			} catch (BPFException e) {
 				logger.error(TAG,
 						"Couldn't initialize the BPF, exception: " + e.getMessage());
 				System.exit(-1);
 			}
 			
 			//start BPF
 			BPF.getInstance().start();
 		}
 		
 		//this is called from StatisticsActivity right after binding
 		@Override
 		public Map<String, Integer> getStats() throws RemoteException {
 			Stats stats = BPF.getInstance().getStats();
 			Map<String, Integer> s = new HashMap<String, Integer>();
 			s.put("stored", stats.storedBundles());
 			s.put("transmitted", stats.transmittedBundles());
 			s.put("received", stats.receivedBundles());
 			s.put("usage", stats.totalSize());
 			return s;
 		}
 
 		// this is called from StatisticsActivity right after binding
 		@Override
 		public void registerCallBack(WSNServiceInterfaceCallBack cb)
 				throws RemoteException {
 			if(cb!=null){
 	            Log.d(TAG, "registerCallBack registering");
 	            mCallbacks.register(cb);
 	        }
 		}
 
 		// this is called from StatisticsActivity when it stops
 		@Override
 		public void unregisterCallBack(WSNServiceInterfaceCallBack cb)
 				throws RemoteException {
 			if (mCallbacks.unregister(cb)) {
 				Log.d(TAG, "Callback to StatisticsActivity unregistered.");
 			} else {
 				Log.w(TAG, "Trying to unregister callback to StatisticsActivity " +
 						"but couldn't find it in the list of registered callbacks");
 			}
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		logger.debug(TAG, "Binding...");
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		// Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
 
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
 		// Init a logger first of all
 		logger = new Logger();
 
 		// Init the action receiver
 		action = new ActionReceiver();
 
 		// Init the communications object
 		comm = new Communication();
 
 		logger.debug(TAG, "Creating Service");
 		
 	}
 
 	@Override
 	public void onDestroy() {
 		BPF.getInstance().stop();
 		logger.info(TAG, "Stopped BPF");
 		Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onStart(Intent intent, int startid) {
 		// this method is not called when the service is started with bindService().
 		// we use the start() method defined in WSNServiceInterface
 		Toast.makeText(this, "Service Started (onStart)", Toast.LENGTH_LONG).show();
 	}
 	
 	// this method is called from the BPF when new statistics are available
 	@Override
 	public void updateStats(Stats stats) {
 		logger.debug(TAG, "New Stats object received:" +
 				"\nTotal size: " + stats.totalSize() + 
 				"\nStored bundles: " + stats.storedBundles() +
 				"\nTransmitted bundles: " + stats.transmittedBundles() +
 				"\nReceived bundles: " + stats.receivedBundles() );
 		//callback
 		try {
 			int N = mCallbacks.beginBroadcast();
 			//check that callback is registered
 			if (N > 0) {
 				Log.d(TAG, "mCallBacks N value = " + N);
 				// now for time being we will consider only one activity is bound to the service, so hardcode 0
 				mCallbacks.getBroadcastItem(0).updateStats(stats.storedBundles(),
 						stats.transmittedBundles(), stats.receivedBundles(), stats.totalSize());
 				mCallbacks.finishBroadcast();
 			} else {
 				Log.w(TAG, "Callback to StatisticsActivity is not registered. Ignoring stats update.");
 			}
 	    } catch (RemoteException e) {
 	        Log.e(TAG, "There was an error while trying to update stats in StatisticsActivity. " +
 	        		"Might be that the app crashed and did not unregister the callback." +
 	        		"Unregistering callback...");
 	        mCallbacks.unregister(mCallbacks.getBroadcastItem(0));
 	    }
 	}
 
 	@Override
 	public BPFActionReceiver getBPFActionReceiver() {
 		return action;
 	}
 
 	@Override
 	public BPFCommunication getBPFCommunication() {
 		return comm;
 	}
 
 	@Override
 	public BPFDB getBPFDB() {
 		return db;
 	}
 
 	@Override
 	public BPFLogger getBPFLogger() {
 		return logger;
 	}
 
 	private WSNService getInstance() {
 		return this;
 	}
 
 }
