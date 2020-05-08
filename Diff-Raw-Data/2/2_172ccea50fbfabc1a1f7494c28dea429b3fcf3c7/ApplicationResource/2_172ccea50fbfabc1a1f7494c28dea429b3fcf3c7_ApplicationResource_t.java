 package mobi.monaca.framework.plugin.innovationplus;
 
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Set;
 
 import jp.innovationplus.ipp.client.IPPApplicationResourceClient;
 import jp.innovationplus.ipp.core.IPPQueryCallback;
 import jp.innovationplus.ipp.jsontype.IPPApplicationResource;
 import mobi.monaca.framework.util.MyLog;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaInterface;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ApplicationResource extends CordovaPluginExecutor {
 	private static final String TAG = ApplicationResource.class.getSimpleName();
 	public ApplicationResource(CordovaInterface cordova) {
 		super(cordova);
 	}
 
 	@Override
 	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
 		final String authKey = KeyPreferenceUtil.getAuthKey(context);
 
 		if (authKey.equals("")) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_NO_AUTH_KEY);
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
 		if (action.equals("retrieveQueryResource")) {
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					retrieveQueryResource(args, authKey, callbackContext);
 				}
 			});
 			return true;
 
 		}
 		if (action.equals("createResource")) {
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					createResource(args, authKey, callbackContext);
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
 
 		return false;
 	}
 	private void deleteResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		String resourceId;
 		String resourceName = null;
 		try {
 			resourceName = args.getString(1);
 		} catch (JSONException e) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			e.printStackTrace();
 			return;
 		}
 
 		try {
 			resourceId = args.getString(0);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 		IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 		client.setAuthKey(authKey);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.delete(resourceName, resourceId, new IPPQueryCallback<String>() {
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 			@Override
 			public void ippDidFinishLoading(String arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading :" + arg0);
 				callbackContext.success(arg0);
 			}
 		});
 	}
 
 	private void createResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		JSONObject content;
 		String resourceName = null;
 		try {
 			resourceName = args.getString(1);
 		} catch (JSONException e) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			e.printStackTrace();
 			return;
 		}
 		try {
 			content = args.getJSONObject(0);
 		} catch (JSONException e) {
 			// maybe JSONArray, goto createPluralResource
 			createPluralResource(args, authKey, callbackContext);
 			return;
 		}
 
 		MyLog.d(TAG, content.toString());
 		IPPApplicationResource resource = new IPPApplicationResource();
 		try {
 			resource.putAll(new ObjectMapper().readValue(content.toString(), LinkedHashMap.class));
 		} catch (Exception e) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION);
 			e.printStackTrace();
 			return;
 		}
 
 		IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 		client.setAuthKey(authKey);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		//client.setDebugMessage(true);
 		client.create(resourceName, resource, new IPPQueryCallback<String>() {
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 			@Override
 			public void ippDidFinishLoading(String arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading :" + arg0);
 				callbackContext.success(arg0);
 			}
 		});
 	}
 
 	private void createPluralResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		JSONArray content;
 		try {
 			content = args.getJSONArray(0);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		final int length = content.length();
 		if (length < 1) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		IPPApplicationResource[] resources = new IPPApplicationResource[length];
 		try {
 			for (int i = 0; i < length; i++) {
 				resources[i] = new IPPApplicationResource();
 				resources[i].putAll(new ObjectMapper().readValue(content.getJSONObject(i).toString(), LinkedHashMap.class));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 		client.setAuthKey(authKey);
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.createAll(args.optString(1), resources, new IPPQueryCallback<Void>() {
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 			@Override
 			public void ippDidFinishLoading(Void arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading");
 				try {
 					callbackContext.success(new JSONObject().put("resultCount", length));
 				} catch (JSONException e) {
 					e.printStackTrace();
 					callbackContext.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION);
 				}
 			}
 		});
 	}
 
 	private void retrieveResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 		String resourceId;
 		try {
 			resourceId = args.getString(0);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		String resourceName = null;
 		try {
 			resourceName = args.getString(1);
 		} catch (JSONException e) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			e.printStackTrace();
 			return;
 		}
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.get(resourceName, resourceId, new IPPQueryCallback<IPPApplicationResource>() {
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 			@Override
 			public void ippDidFinishLoading(IPPApplicationResource arg0) {
 				MyLog.d(TAG, "ippDidFinishLoading :");
 				try {
 					JSONObject result = buildFromIPPAppricationResource(arg0);
 					callbackContext.success(result);
 				} catch (JSONException e) {
 					e.printStackTrace();
 					callbackContext.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION);
 				}
 			}
 		});
 	}
 
 	private JSONObject buildFromIPPAppricationResource(IPPApplicationResource arg0) throws JSONException {
 		JSONObject result = new JSONObject();
 		Set<String> keySet = arg0.keySet();
 		for (String key : keySet) {
 			result.put(key, arg0.get(key));
 		}
 		return result;
 	}
 
 	private void retrieveQueryResource(JSONArray args, String authKey, final CallbackContext callbackContext) {
 		String resourceName = null;
 		try {
 			resourceName = args.getString(1);
 		} catch (JSONException e) {
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			e.printStackTrace();
 			return;
 		}
 
 		IPPApplicationResourceClient client = new IPPApplicationResourceClient(context);
 		JSONObject param;
 		try {
 			param = args.getJSONObject(0);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 			return;
 		}
 
 		IPPApplicationResourceClient.QueryCondition condition = new IPPApplicationResourceClient.QueryCondition();
 
 		if (param.has("query")) {
 			JSONObject query = null;
 			try {
 				query = param.getJSONObject("query");
 				Iterator<String> i = query.keys();
 				String key;
 				while (i.hasNext() && (key = i.next()) != null) {
 					condition.eq(key, query.opt(key));
 					if (i.hasNext()) {
 						condition.and();
 					}
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 				callbackContext.error(InnovationPlusPlugin.ERROR_INVALID_PARAMETER);
 				return;
 			}
 		}
 
 		try {
 			condition.setCount(param.getInt("count"));
 		} catch (JSONException e) {
 		}
 		try {
 			condition.setSince(param.getLong("since"));
 		} catch (JSONException e) {
 		}
 		try {
			condition.setUntil(param.getLong("until"));
 		} catch (JSONException e) {
 		}
 		client.setApplicationId(KeyPreferenceUtil.getApplicationId(context));
 		client.query(resourceName, condition, new IPPQueryCallback<IPPApplicationResource[]>() {
 			@Override
 			public void ippDidError(int i) {
 				MyLog.d(TAG, "ippDidError:" + i);
 				callbackContext.error(i);
 			}
 			@Override
 			public void ippDidFinishLoading(IPPApplicationResource[] arg0) {
 				JSONObject response = new JSONObject();
 				try {
 					int length = arg0.length;
 					response.put("resultCount", length);
 					JSONArray resultArray  = new JSONArray();
 					for (int i = 0; i < length; i++) {
 						resultArray.put(buildFromIPPAppricationResource(arg0[i]));
 					}
 					response.put("result", resultArray);
 					callbackContext.success(response);
 				} catch (JSONException e) {
 					e.printStackTrace();
 					callbackContext.error(InnovationPlusPlugin.ERROR_WITH_EXCEPTION);
 				}
 
 			}
 		});
 	}
 }
