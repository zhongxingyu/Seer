 package com.roshka.raf.route;
 
 import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
 import java.net.HttpURLConnection;
 import java.util.List;
 
 import com.roshka.raf.exception.RAFException;
 import com.roshka.raf.request.RAFRequest;
 
 public class RouteExecutorManager {
 
 	public static Object executeRoute(RAFRequest rreq, Route r, Object... params)
 		throws RAFException
 	{
 		Object _instance;
 		try {
 			_instance = r.getActionClass().newInstance();
 			List<Field> contextFields = r.getContextFields();
 			for (Field field : contextFields) {
 				field.set(_instance, field.getType().cast(rreq.getRafContext()));
 			}
 			List<Field> requestFields = r.getRequestFields();
 			if (requestFields != null && requestFields.size() > 0) {
 				for (Field field: requestFields) {
 					field.set(_instance, rreq);
 				}
 				
 			}
 			return r.getActionMethod().getMethod().invoke(_instance, params);
 		} catch (Throwable e) {
			
			if (e instanceof InvocationTargetException) {
				InvocationTargetException ite = (InvocationTargetException)e;
				if (ite.getTargetException() instanceof RAFException) {
					throw (RAFException)ite.getTargetException();
				}
			} 				
 			throw new RAFException(HttpURLConnection.HTTP_INTERNAL_ERROR, RAFException.ERRCODE_UNEXPECTED_EXCEPTION, "Unexpected Exception: " + e.getMessage());
 		} 
 	}
 	
 }
