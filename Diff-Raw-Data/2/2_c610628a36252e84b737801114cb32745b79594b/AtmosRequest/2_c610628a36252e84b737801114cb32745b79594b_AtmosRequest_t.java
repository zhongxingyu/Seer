 package com.skp.milonga.servlet;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.mozilla.javascript.NativeObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.springframework.web.bind.support.WebRequestDataBinder;
 import org.springframework.web.context.request.ServletWebRequest;
 
 /**
  * HttpServletRequest replacement class
  * 
  * @author kminkim
  *
  */
 public class AtmosRequest extends NativeObject {
 	
 	public static final String SESSION = "session";
 
 	private static final long serialVersionUID = -3554241802444383767L;
 	
	private transient ServletWebRequest servletWebRequest;
 
 	public AtmosRequest(ServletWebRequest servletWebRequest) {
 		super();
 		this.servletWebRequest = servletWebRequest;
 		initializeRequestParameters();
 		this.defineFunctionProperties(new String[]{"bindAs"}, this.getClass(), ScriptableObject.DONTENUM);
 	}
 
 	@Override
 	public Object get(String name, Scriptable start) {
 		Object value = super.get(name, start);
 		
 		if (SESSION.equals(name)) {
 			return getSession(value);
 		}
 		
 		return value;
 	}
 	
 	public Object bindAs(String className) {
 		Object value = null;
 		try {
 			Class<?> clazz = Class.forName(className);
 			Object object = clazz.newInstance();
 			WebRequestDataBinder binder = new WebRequestDataBinder(object);
 			binder.setIgnoreUnknownFields(false);
 			binder.bind(servletWebRequest);
 			value = binder.getTarget();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return value;
 	}
 
 	private void initializeRequestParameters() {
 		@SuppressWarnings("unchecked")
 		Map<String, String[]> parameterMap = servletWebRequest.getRequest()
 				.getParameterMap();
 		Iterator<Map.Entry<String, String[]>> parameterIterator = parameterMap
 				.entrySet().iterator();
 		while (parameterIterator.hasNext()) {
 			String key = parameterIterator.next().getKey();
 			String[] value = parameterMap.get(key);
 			defineProperty(key, value[0], 0);
 		}
 	}
 
 	private Object getSession(Object value) {
 		if (value.equals(Scriptable.NOT_FOUND)) {
 			AtmosSession session = new AtmosSession(servletWebRequest
 					.getRequest().getSession());
 			defineProperty(SESSION, session, 0);
 			return session;
 		}
 		return value;
 	}
 	
 }
