 package com.roshka.raf.route;
 
 import java.lang.reflect.Field;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.roshka.raf.context.RAFContext;
 import com.roshka.raf.exception.RAFException;
 import com.roshka.raf.request.RAFRequest;
 
 public class RouteExecutorManager {
 
 	public static Object executeRoute(RAFContext rc, HttpServletRequest sreq, HttpServletResponse sresp, Route r, Object... params)
 		throws RAFException
 	{
 		Object _instance;
 		try {
 			_instance = r.getActionClass().newInstance();
 			List<Field> contextFields = r.getContextFields();
 			for (Field field : contextFields) {
				field.set(_instance, rc);
 			}
 			List<Field> requestFields = r.getRequestFields();
 			if (requestFields != null && requestFields.size() > 0) {
 				RAFRequest rreq = new RAFRequest();
 				rreq.setRequest(sreq);
 				rreq.setResponse(sresp);
 				rreq.setRafContext(rc);
 				for (Field field: requestFields) {
 					field.set(_instance, rreq);
 				}
 				
 			}
 			return r.getActionMethod().getMethod().invoke(_instance, params);
 		} catch (Throwable e) {
 			throw new RAFException(RAFException.ERRCODE_UNEXPECTED_EXCEPTION, "Unexpected Exception: " + e.getMessage());
 		} 
 	}
 	
 }
