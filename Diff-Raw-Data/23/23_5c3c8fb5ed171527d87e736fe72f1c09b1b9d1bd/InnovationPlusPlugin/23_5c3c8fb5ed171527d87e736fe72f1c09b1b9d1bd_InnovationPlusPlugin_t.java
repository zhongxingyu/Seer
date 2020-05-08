 package mobi.monaca.framework.plugin.innovationplus;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaInterface;
 import org.apache.cordova.api.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 public class InnovationPlusPlugin extends CordovaPlugin {
 	public static final int ERROR_NO_AUTH_KEY = -20;
 	public static final int ERROR_WITH_EXCEPTION = -40;
 	public static final int ERROR_INVALID_PARAMETER = -60;
	public static final int ERROR_NO_APPLICATION_ID = -80;
 	@Override
 	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
 		//MyLog.d("ipp","execute");
 		String[] originalAction = action.split("\\.");
 		//MyLog.d("ipp", "action len is " + originalAction.length);
 		if (originalAction.length != 2) {
 			return false;
 		}

 		String executorClassName = InnovationPlusPlugin.class.getPackage().getName() + "." + originalAction[0];
 		String newAction = originalAction[1];
 

 		try {
 			Class<?> executorClass = Class.forName(executorClassName);
 			Constructor<?> constructor = executorClass.getConstructor(CordovaInterface.class);
 			CordovaPluginExecutor executor = (CordovaPluginExecutor) constructor.newInstance(cordova);

			if (KeyPreferenceUtil.getApplicationId(cordova.getActivity()) == null) {
				callbackContext.error(ERROR_NO_APPLICATION_ID);
				return true;
			}
 			return executor.execute(newAction, args, callbackContext);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (ClassCastException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 }
