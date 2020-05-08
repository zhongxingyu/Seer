 package com.andrewma.vision.webserver.core;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import android.util.Log;
 
 import com.andrewma.vision.webserver.core.annotations.Action;
 
 public abstract class Controller {
 	private Map<String, Method> actions = new HashMap<String, Method>();
 	private final String TAG;
 
 	public Controller() {
 		TAG = getClass().getSimpleName();
 		
 		final Method[] methods = getClass().getMethods();
 		for (Method method : methods) {
 			final Class<?> returnType = method.getReturnType();
 			if (method.isAnnotationPresent(Action.class)) {
 				if(returnType.equals(Result.class)) {
 					actions.put(method.getName().toLowerCase(), method);
 				} else {
 					Log.e(TAG, method.getName() + " needs to have Result type to be an action");
 				}
 			}
 		}
 	}
 
 	protected Object execute(String actionString, String idString,
 			Properties params) {
 		try {
 			final Method action = actions.get(actionString.toLowerCase());
 			final Class<?>[] paramTypes = action.getParameterTypes();
 
 			if (paramTypes == null || paramTypes.length == 0) {
 				return actions.get(actionString.toLowerCase()).invoke(this);
 			} else {
 				if (int.class.equals(paramTypes[0])) {
 					int id = Integer.parseInt(idString);
 					return actions.get(actionString.toLowerCase()).invoke(this,
 							id);
 				} else {
 					Object param = mapParamsToObject(params, paramTypes[0]);
 					return actions.get(actionString.toLowerCase()).invoke(this,
 							param);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
			return ErrorResult(e.getClass().getName());
 		}
 	}
 
 	private <E> E mapParamsToObject(Properties params, Class<E> typeToMap) {
 		E result;
 		try {
 			result = typeToMap.newInstance();
 			for (Enumeration<Object> e = params.keys(); e.hasMoreElements();) {
 				final String fieldName = e.nextElement().toString();
 				final String fieldValue = params.get(fieldName).toString();
 				if (fieldValue != null && !fieldValue.equals("")) {
 					final Field field = typeToMap.getField(fieldName);
 					final Class<?> fieldType = field.getType();
 					if (float.class.equals(fieldType)) {
 						field.setFloat(result, Float.parseFloat(fieldValue));
 					} else if (int.class.equals(fieldType)) {
 						field.setInt(result, Integer.parseInt(fieldValue));
 					} else if (boolean.class.equals(fieldType)) {
 						field.setBoolean(result,
 								"true".equals(fieldValue.toLowerCase()));
 					} else if (String.class.equals(fieldType)) {
 						field.set(result, fieldValue);
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		return result;
 	}
 	
 	public Result Result(String status, String mimeType, Object data) {
 		return new Result(status, mimeType, data);
 	}
 	
 	/**
 	 * Helper function to return a 500 internal error {@link Result}
 	 * @param error
 	 * @return
 	 */
 	public Result ErrorResult(String error) {
 		Log.e(TAG, "ErrorResult: " + error);
 		return new Result(NanoHTTPD.HTTP_INTERNALERROR, VisionHTTPD.MIME_JSON, error);
 	}
 	
 	/**
 	 * Helper function to return a 404 not found code {@link Result}
 	 * @param message
 	 * @return
 	 */
 	public Result NotFoundResult(String message) {
 		Log.w(TAG, "NotFoundResult: " + message);
 		return new Result(NanoHTTPD.HTTP_NOTFOUND, VisionHTTPD.MIME_JSON, message);
 	}
 	
 	/**
 	 * Object to hold the result of an API call
 	 */
 	public class Result {
 		
 		public final String status;
 		public final String mimeType;
 		public final Object data;
 		
 		private Result(String status, String mimeType, Object data) {
 			this.status = status;
 			this.mimeType = mimeType;
 			this.data = data;
 		}
 	}
 }
