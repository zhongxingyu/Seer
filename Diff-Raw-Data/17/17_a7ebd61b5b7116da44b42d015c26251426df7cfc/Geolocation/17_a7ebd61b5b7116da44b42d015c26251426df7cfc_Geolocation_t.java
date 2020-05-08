 package mobi.monaca.framework.plugin.innovationplus;
 
 import jp.innovationplus.ipp.client.IPPGeoLocationClient;
 import jp.innovationplus.ipp.core.IPPQueryCallback;
 import jp.innovationplus.ipp.jsontype.IPPGeoLocation;
 import mobi.monaca.framework.util.MyLog;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaInterface;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Geolocation extends CordovaPluginExecutor{
 	private static final String TAG = Geolocation.class.getSimpleName();
 	public Geolocation(CordovaInterface cordova) {
 		super(cordova);
 	}
 
 	@Override
 	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
 		final String authKey = KeyPreferenceUtil.getAuthKey(context);
 
 		if (authKey.equals("")) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_NO_AUTH_KEY);
 			return true;
 		}
 
 		if (action.equals("retrieveOwnResource")) {
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					retrieveOwnResource(args, authKey, callbackContext);
 				}
 			});
 			return true;
 		}
 
 		if (action.equals("retrieveResource")) {
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					retrieveResource(args, authKey, callbackContext);
 				}
 			});
 			return true;
 		}
 
 		if (action.equals("createResource")) {
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					if (1 < args.length()) {
 						createPluralResources(args, authKey, callbackContext);
 					} else {
 						createResource(args, authKey, callbackContext);
 					}
 				}
 			});
 			return true;
 		}
 
 		if (action.equals("deleteResource")) {
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					deleteResource(args, authKey, callbackContext);
 				}
 			});
 			return true;
 		}
 
 		if (action.equals("retrieveQueryResource")) {
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					retrieveQueryResource(args, authKey, callbackContext);
 				}
 			});
 			return true;
 		}
 		return false;
 	}
 
 	private JSONObject buildJsonFromIPPGeoLocation(IPPGeoLocation arg0) throws JSONException {
 		JSONObject resultJson = new JSONObject();
 		resultJson.put("resource_id", arg0.getResource_id());
 		resultJson.put("latitude", arg0.getLatitude());
 		resultJson.put("longitude", arg0.getLongitude());
 		resultJson.put("provider", arg0.getProvider());
 		resultJson.put("timestamp", arg0.getTimestamp());
 		return resultJson;
 	}
 
 	private IPPGeoLocation buildIPPGeoLocationFromJson(JSONObject requestJson) {
 		IPPGeoLocation newRes = new IPPGeoLocation();
 		try {
 			newRes.setLatitude(requestJson.getDouble("latitude"));
 			newRes.setLongitude(requestJson.getDouble("longitude"));
 			newRes.setProvider(requestJson.getString("provider"));
 			newRes.setTimestamp(requestJson.getLong("timestamp"));
 
 			return newRes;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private void retrieveQueryResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		JSONObject param = args.optJSONObject(0);
 		if (param == null) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		IPPGeoLocationClient.QueryCondition query = new IPPGeoLocationClient.QueryCondition();
 		try {
 			JSONArray bound = param.getJSONArray("bound");
 			try {
 				query.setBound(bound.getDouble(0), bound.getDouble(1), bound.getDouble(2), bound.getDouble(3));
 			} catch (JSONException e) {
 				callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 				return ;
 			}
 		} catch (JSONException e) {
 			MyLog.d(TAG, "no bound");
 		}
 
 		try {
 			JSONArray radiusSquare = param.getJSONArray("radiusSquare");
 			try {
 				query.setBoundByRadiusSquare(radiusSquare.getDouble(0), radiusSquare.getDouble(1), radiusSquare.getInt(2));
 			} catch (JSONException e) {
 				callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 				return;
 			}
 		} catch (JSONException e) {
 			MyLog.d(TAG, "no radiusSquare");
 		}
 		try {
 			int count = param.getInt("count");
 			query.setCount(count);
 		} catch (JSONException e) {
 		}
 
 		try {
 			long since = param.getLong("since");
 			query.setSince(since);
 		} catch (JSONException e) {
 		}
 
 		try {
 			long until = param.getLong("until");
 			query.setUntil(until);
 		} catch (JSONException e) {
 		}
 
		if (param.optBoolean("self", false)) {
			query.setSelf();
		}

 		IPPGeoLocationClient client = new IPPGeoLocationClient(context);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.setAuthKey(authKey);
 		client.query(query, new IPPQueryCallback<IPPGeoLocation[]>() {
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 
 			@Override
 			public void ippDidFinishLoading(IPPGeoLocation[] arg0) {
 				JSONObject result = new JSONObject();
 				try {
 					MyLog.d(TAG, "ippDidFinishLoading" + arg0.length);
 					result.put("resultCount", arg0.length);
 
 					JSONArray resultArray = new JSONArray();
 					for (int i = 0; i < arg0.length; i++) {
 						resultArray.put(buildJsonFromIPPGeoLocation(arg0[i]));
 					}
 					result.put("result", resultArray);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				callbackContext.success(result);
 			}
 		});
 	}
 
 	private void deleteResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		String resourceId = args.optString(0);
 		if (resourceId == null || resourceId.equals("")) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		IPPGeoLocationClient client = new IPPGeoLocationClient(context);
 		client.setAuthKey(authKey);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.delete(resourceId, new IPPQueryCallback<String>() {
 			@Override
 			public void ippDidError(int arg0) {
 				MyLog.d(TAG, "ippDidError:" + arg0);
 				callbackContext.error(arg0);
 			}
 			@Override
 			public void ippDidFinishLoading(String arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading:");
 				callbackContext.success(arg0);
 			}
 		});
 	}
 
 	private void createPluralResources(JSONArray requestArray, String authKey, final CallbackContext callbackContext) {
 		final int length = requestArray.length();
 		IPPGeoLocation[] postData = new IPPGeoLocation[length];
 		try {
 			for (int i = 0; i < length; i++) {
 				postData[i] = buildIPPGeoLocationFromJson(requestArray.getJSONObject(i));
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		IPPGeoLocationClient client = new IPPGeoLocationClient(context);
 		client.setAuthKey(authKey);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.createAll(postData, new IPPQueryCallback<Void>() {
 			@Override
 			public void ippDidFinishLoading(Void arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading:");
 				try {
 					callbackContext.success(new JSONObject().put("resultCount", length));
 				} catch (JSONException e) {
 					e.printStackTrace();
 					callbackContext.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION);
 				}
 			}
 
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 		});
 	}
 
 	private void createResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		JSONObject requestJson;
 		try {
 			requestJson = args.getJSONObject(0);
 		} catch (JSONException e) {
 			// maybe this is array nesting
 			try {
 				createPluralResources(args.getJSONArray(0), authKey, callbackContext);
 			} catch (JSONException e1) {
 				callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 				e1.printStackTrace();
 			}
 			return;
 		}
 		IPPGeoLocation newRes = buildIPPGeoLocationFromJson(requestJson);
 
 		if (newRes == null) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		IPPGeoLocationClient client = new IPPGeoLocationClient(context);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.setAuthKey(authKey);
 		client.create(newRes, new IPPQueryCallback<String>() {
 			@Override
 			public void ippDidError(int arg0) {
 				MyLog.d(TAG, "ippDidError:" + arg0);
 				callbackContext.error(arg0);
 			}
 			@Override
 			public void ippDidFinishLoading(String arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading:");
 				callbackContext.success(arg0);
 			}
 		});
 	}
 
 	private void retrieveResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		String resourceId = args.optString(0);
 		if (resourceId == null || resourceId.equals("")) {
 			retrieveOwnResource(args, authKey, callbackContext);
 			return;
 		}
 
 		IPPGeoLocationClient client = new IPPGeoLocationClient(context);
 		client.setAuthKey(authKey);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.get(resourceId, new IPPQueryCallback<IPPGeoLocation>() {
 			@Override
 			public void ippDidFinishLoading(IPPGeoLocation arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading:");
 				try	{
 					JSONObject resultJson = buildJsonFromIPPGeoLocation(arg0);
 					callbackContext.success(resultJson);
 				} catch (JSONException e) {
 					e.printStackTrace();
 					callbackContext.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION);
 				}
 			}
 
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 		});
 	}
 
 	private void retrieveOwnResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		IPPGeoLocationClient client = new IPPGeoLocationClient(context);
 		client.setAuthKey(authKey);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.get(new IPPQueryCallback<IPPGeoLocation>() {
 			@Override
 			public void ippDidFinishLoading(IPPGeoLocation arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading:");
 				try	{
 					JSONObject resultJson = buildJsonFromIPPGeoLocation(arg0);
 					callbackContext.success(resultJson);
 				} catch (JSONException e) {
 					e.printStackTrace();
 					callbackContext.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION);
 				}
 			}
 
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 		});
 	}
 
 }
