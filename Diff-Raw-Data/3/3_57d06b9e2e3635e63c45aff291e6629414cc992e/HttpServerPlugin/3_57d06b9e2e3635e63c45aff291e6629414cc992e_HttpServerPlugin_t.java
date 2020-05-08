 package mobi.monaca.framework.plugin;
 
 import mobi.monaca.framework.MonacaApplication;
 import mobi.monaca.framework.bootloader.LocalFileBootloader;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.framework.util.NetworkUtils;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class HttpServerPlugin extends CordovaPlugin{
 
 	private static final String TAG = HttpServerPlugin.class.getSimpleName();
 	private static MonacaLocalServer localServer;
 
 	@Override
 	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
 		MyLog.v(TAG, "HttpServerPlugin exec action:" + action + ", args:" + args);
 		if(action.equalsIgnoreCase("getServerRoot")){
 			if(localServer != null){
 				callbackContext.success(localServer.getServerRoot());
 			}else{
 				callbackContext.error("Error server is not started yet. Plesae start the server before lcalling this");
 			}
 
 			return true;
 		}
 
 		if(action.equalsIgnoreCase("getIpAddress")){
 			callbackContext.success(NetworkUtils.getIPAddress(true));
 			return true;
 		}
 
 		if(action.equalsIgnoreCase("start")){
 			if(localServer != null){
				localServer.stop();
 			}
 			if (args.length() < 2) {
 				callbackContext.error("either documentRoot or params is not supplied");
 			} else {
 				Runnable serverRunner = new Runnable(){
 					@Override
 					public void run() {
 						try{
 							String rootDir = args.getString(0);
 							JSONObject params = args.getJSONObject(1);
 							int port = params.getInt("port");
 							localServer = new MonacaLocalServer(cordova.getActivity(), rootDir, port);
 							localServer.start();
 							JSONObject result = new JSONObject();
 							result.put("ip", NetworkUtils.getIPAddress(true));
 							result.put("port", port);
 							callbackContext.success(result);
 						}catch (JSONException e) {
 							callbackContext.error(e.getMessage());
 							e.printStackTrace();
 						} catch (Exception e) {
 							callbackContext.error("Cannot start server. error: " + e.getMessage());
 							e.printStackTrace();
 						}
 					}};
 				Runnable fail = new Runnable(){
 					@Override
 					public void run() {
 						callbackContext.error("Cannot start server.");
 					}};
 
 				if (((MonacaApplication)cordova.getActivity().getApplication()).enablesBootloader()) {
 					LocalFileBootloader.setup(cordova.getActivity(), serverRunner, fail);
 				} else {
 					serverRunner.run();
 				}
 			}
 			return true;
 		}else if(action.equalsIgnoreCase("stop")){
 			if(localServer != null){
 				localServer.stop();
 				localServer = null;
 				callbackContext.success("stopped server");
 			}
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
 		MyLog.i(TAG, "Monaca HttpServer plugin onDestroy");
 		if(localServer != null){
 			MyLog.i(TAG, "closing local server");
 			localServer.stop();
 		}
 		super.onDestroy();
 	}
 }
