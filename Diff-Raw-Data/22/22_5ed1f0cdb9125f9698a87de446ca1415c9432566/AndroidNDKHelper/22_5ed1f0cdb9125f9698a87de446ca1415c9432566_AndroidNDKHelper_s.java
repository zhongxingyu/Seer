 package com.easyndk.classes;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.util.Log;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Handler;
 import android.os.Message;
 
 public class AndroidNDKHelper
 {
 	private static Object callHandler = null;
 	private static Handler NDKHelperHandler = null;
 	
 	private static native void cppNativeCallHandler(String json);
 	private static String __CALLED_METHOD__ = "calling_method_name";
 	private static String __CALLED_METHOD_PARAMS__ = "calling_method_params";
 	private final static int __MSG_FROM_CPP__ = 5; 
 	
 	public static void setNDKReceiver(Object callReceiver)
 	{
 		callHandler = callReceiver;
 		NDKHelperHandler = new Handler()
 		{
         	public void handleMessage(Message msg)
         	{
         		switch(msg.what)
         		{
 	        		case __MSG_FROM_CPP__:
 	        			try
 	        			{
 	        				NDKMessage message = (NDKMessage)msg.obj;
 	        				message.methodToCall.invoke(AndroidNDKHelper.callHandler, message.methodParams);
 	        			}
 	        			catch (IllegalArgumentException e)
 	        			{
 	        				// TODO Auto-generated catch block
 	        				e.printStackTrace();
 	        			}
 	        			catch (IllegalAccessException e)
 	        			{
 	        				// TODO Auto-generated catch block
 	        				e.printStackTrace();
 	        			}
 	        			catch (InvocationTargetException e)
 	        			{
 	        				// TODO Auto-generated catch block
 	        				e.printStackTrace();
 	        			}
 	        		break;
         		}
         	}
         };
 	}
 	
 	public static void sendMessageWithParameters(String methodToCall, JSONObject paramList)
 	{
 		JSONObject obj = new JSONObject();
 		try
 		{
 			obj.put(__CALLED_METHOD__, methodToCall);
 			obj.put(__CALLED_METHOD_PARAMS__, paramList);
 			cppNativeCallHandler(obj.toString());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static String receiveCppMessage(String json, boolean async)
 	{
 		if (json != null)
 		{
 			try
 			{
 				JSONObject obj = new JSONObject(json);
 				if (obj.has(__CALLED_METHOD__))
 				{
 					String methodName = obj.getString(__CALLED_METHOD__);
 					JSONObject methodParams = null;
 					
 					try
 					{
 						methodParams = obj.getJSONObject(__CALLED_METHOD_PARAMS__);
 					}
 					catch (JSONException e)
 					{
 						// if we are finding trouble in looking for params, params might be null
 					}
 					
 					try {
 						Method m = AndroidNDKHelper.callHandler.getClass().getMethod(methodName, new Class[] { JSONObject.class });
 
                         JSONObject retParamsJson =
                                 (JSONObject) m.invoke(AndroidNDKHelper.callHandler, methodParams);
                         if (retParamsJson == null) {
                             retParamsJson = new JSONObject();
                         }
                        Log.v("retParamsJson", "retParamsJson: " + retParamsJson.toString());
                         return retParamsJson.toString();
 					}
 					catch (NoSuchMethodException e) {
                         throw new IllegalStateException(e);
 					}
 					catch (IllegalArgumentException e) {
                        throw new IllegalStateException(e);
 					} catch (InvocationTargetException e) {
                        throw new IllegalStateException(e);
                     } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                     }
                 } else {
                    throw new IllegalArgumentException("Wrong JSON structure");
                 }
 			}
 			catch (JSONException e) {
                throw new IllegalStateException(e);
 			}
		} else {
            return null;
         }
 	}
 }
