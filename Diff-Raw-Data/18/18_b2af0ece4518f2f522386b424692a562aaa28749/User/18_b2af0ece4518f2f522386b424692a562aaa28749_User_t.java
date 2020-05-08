 package mobi.monaca.framework.plugin.innovationplus;
 
 import jp.innovationplus.ipp.client.IPPLoginClient;
 import jp.innovationplus.ipp.core.IPPException;
 import jp.innovationplus.ipp.core.IPPQueryCallback;
 import jp.innovationplus.ipp.jsontype.IPPLoginResult;
 import mobi.monaca.framework.util.MyLog;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaInterface;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.widget.Toast;
 
 //TODO not tested
 public class User extends CordovaPluginExecutor{
 
 	public User(CordovaInterface cordovaInterface) {
 		super(cordovaInterface);
 	}
 
	public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		MyLog.d("user", "execute");
 		if (action.equals("login")) {
 			JSONObject loginSet = args.optJSONObject(0);
 			if (loginSet != null) {
 				doLogin(loginSet, callbackContext);
 				return true;
 			}
 		}
 		if (action.equals("getAuthKey")) {
 			callbackContext.success(AuthKeyPreferenceUtil.getAuthKey(context));
 			return true;
 		}
 		if (action.equals("removeAuthKey")) {
			MyLog.d("user", "remove");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AuthKeyPreferenceUtil.removeAuthKey(context);
					if (AuthKeyPreferenceUtil.getAuthKey(context).equals("")) {
						callbackContext.success();
					} else {
						MyLog.d("user", "unexpected error occured");
						callbackContext.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION);
					}
				}});
 			return true;
 		}
 
 		return false;
 	}
 
 	private void doLogin(final JSONObject loginSet, final CallbackContext callback) throws JSONException {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				IPPLoginClient loginClient = new IPPLoginClient(context);
 				try {
 					loginClient.login(loginSet.getString("username"), loginSet.getString("password"), new IPPQueryCallback<IPPLoginResult>() {
 						@Override
 						public void ippDidFinishLoading(IPPLoginResult arg0) {
 							MyLog.d("user", "finished loading");
 							JSONObject result = new JSONObject();
 							try {
 								String authKey = arg0.getAuth_key();
 								result.put("auth_key", authKey);
 								AuthKeyPreferenceUtil.saveAuthKey(context, authKey);
 								callback.success(result);
 							} catch (JSONException e) {
 								callback.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION); // this code is defined by this plugin.
 								e.printStackTrace();
 							}
 						}
 						@Override
 						public void ippDidError(int arg0) {
 							MyLog.d("user", "ippDidError :" + arg0);
 							callback.error(arg0);
 						}
 					});
 				} catch (IPPException e) {
 					e.printStackTrace();
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		});
 
 	}
 }
