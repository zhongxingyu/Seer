 package com.khs.sherpa.servlet;
 
 import static com.khs.sherpa.util.Util.msg;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.logging.Logger;
 
 import javax.annotation.security.DenyAll;
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.khs.sherpa.annotation.Endpoint;
 import com.khs.sherpa.exception.SherpaActionNotFoundException;
 import com.khs.sherpa.exception.SherpaPermissionExcpetion;
 import com.khs.sherpa.exception.SherpaRuntimeException;
 import com.khs.sherpa.json.service.AuthenticationException;
 import com.khs.sherpa.json.service.JSONService;
 import com.khs.sherpa.json.service.SessionStatus;
 import com.khs.sherpa.json.service.SessionToken;
 import com.khs.sherpa.util.Constants;
 
 class SherpaRequest {
 
 	private static Logger LOG = Logger.getLogger(SherpaRequest.class.getName());
 	
 	private String endpoint;
 	private String action;
 	
 	private Object target;
 	private Object responseData;
 	
 	private HttpServletRequest servletRequest;
 	private HttpServletResponse servletResponse;
 	
 	private SessionStatus sessionStatus = null;
 	private JSONService service;
 	private Settings settings;
 	
 	private String[] roles = null;
 	
 	public String getEndpoint() {
 		return endpoint;
 	}
 	
 	public void setEndpoint(String endpoint) {
 		this.endpoint = endpoint;
 	}
 	
 	public String getAction() {
 		return action;
 	}
 	
 	public void setAction(String action) {
 		this.action = action;
 	}
 
 	public Object getResponseData() {
 		return responseData;
 	}
 
 	public void setTarget(Object target) {
 		this.target = target;
 	}
 
 	public void setServletRequest(HttpServletRequest servletRequest) {
 		this.servletRequest = servletRequest;
 	}
 
 	public void setSessionStatus(SessionStatus sessionStatus) {
 		this.sessionStatus = sessionStatus;
 	}
 
 	public void setRoles(String[] roles) {
 		this.roles = roles;
 	}
 
 	public void setService(JSONService service) {
 		this.service = service;
 	}
 
 	public Settings getSettings() {
 		return settings;
 	}
 
 	public void setSettings(Settings settings) {
 		this.settings = settings;
 	}
 
 	public HttpServletResponse getServletResponse() {
 		return servletResponse;
 	}
 
 	public void setServletResponse(HttpServletResponse servletResponse) {
 		this.servletResponse = servletResponse;
 	}
 
 	public void loadRequest(HttpServletRequest request, HttpServletResponse response) {
 		this.setEndpoint(request.getParameter("endpoint"));
 		this.setAction(request.getParameter("action"));
 		this.setServletRequest(request);
 		this.setServletResponse(response);
 	}
 	
 	private boolean isAuthRequest(HttpServletRequest request) {
 		String endpoint = request.getParameter("endpoint");
		if(endpoint.equals("null")) {
 			endpoint =  null;
 		}
 		
 		String action = request.getParameter("action");
 		
 		if(endpoint == null && action.equals(Constants.AUTHENTICATE_ACTION)) {
 			return true;
 		}
 		return false;
 		
 	}
 	
 	private void validateMethod(Method method) throws SherpaRuntimeException {
 		if(target.getClass().getAnnotation(Endpoint.class).authenticated()) {
 			if(!sessionStatus.equals(SessionStatus.AUTHENTICATED)) {
 				throw new SherpaPermissionExcpetion("token is invalid or has expired");
 			} else if(method.isAnnotationPresent(DenyAll.class)) {
 				throw new SherpaPermissionExcpetion("method ["+method.getName()+"] in class ["+target.getClass().getCanonicalName()+"] has `@DenyAll` annotation" );
 			} else if(method.isAnnotationPresent(RolesAllowed.class)) {
 				for(String role: method.getAnnotation(RolesAllowed.class).value()) {
 					for(String r: roles) {
 						if(role.equals(r)) {
 							return;
 						}
 					}
 				}
 				throw new SherpaPermissionExcpetion("method ["+method.getName()+"] in class ["+target.getClass().getCanonicalName()+"] has `@RolesAllowed` annotation" );
 			}
 		}
 	}
 	
 	private Object invokeMethod(Method method) {
 		try {
 			return method.invoke(target, this.getParams(method));
 		} catch (Exception e) {
 			throw new SherpaActionNotFoundException("unable to execute method ["+method.getName()+"] in class ["+target.getClass().getCanonicalName()+"]");
 		}
 	}
 	
 	private Object[] getParams(Method method) {
 		RequestMapper map = new RequestMapper(settings);
 		Class<?>[] types = method.getParameterTypes();
 		Object[] params = null;
 		// get parameters
 		if (types.length > 0) {
 			Annotation[][] parameters = method.getParameterAnnotations();
 			// Annotation[] annotations = parameters[0];
 			params = new Object[types.length];					
 			int i = 0;
 			for (Annotation[] annotations : parameters) {
 				for (Annotation annotation : annotations) {
 					Object result = map.map(method.getClass().getName(),method.getName(),types[i], servletRequest, annotation);					
 					params[i] = result;
 					i++;
 				}
 			}
 
 		}
 		return params;
 	}
 	
 	private Method findMethod(String name) {
 		Method[] methods = target.getClass().getMethods();
 		for (Method m : methods) {
 			if(m.getName().equals(name)) {
 				return m;
 			}
 		}
 		throw new SherpaActionNotFoundException("no method found ["+name+"] in class ["+target.getClass().getCanonicalName()+"]");
 	}
 	
 	public void run() {
 		
 		if(isAuthRequest(servletRequest)) {
 			String userid = servletRequest.getParameter("userid");
 			String password = servletRequest.getParameter("password");
 			try {
 				SessionToken token = this.service.authenticate(userid, password);
 				this.service.getTokenService().activate(userid, token);
 				log(msg("authenticated"), userid, "*****");
 				this.service.map(this.getResponseOutputStream(), token);
 			} catch (AuthenticationException e) {
 				this.service.error("Authentication Error Invalid Credentials", this.getResponseOutputStream());
 				log(msg("invalid authentication"), userid, "*****");
 			}
 			
 			return;
 		} else {
			String token = servletRequest.getHeader("token");
			String userid = servletRequest.getHeader("userid");
			sessionStatus = this.service.validToken(token, userid);
 		}
 		
 		if(target == null) {
 			throw new SherpaRuntimeException("@Endpoint not found initialized");
 		}
 		
 		Method method = this.findMethod(action);
 
 		try {
 			this.validateMethod(method);
 			this.service.map(this.getResponseOutputStream(), this.invokeMethod(method));
 		} catch (SherpaRuntimeException e) {
 			throw e;
 		}
 		
 	}
 	
 	private void log(String action, String email, String token) {
 		LOG.info(msg(String.format("Executed - %s,%s,%s ", action, email, token)));
 	}
 	
 	private OutputStream getResponseOutputStream() {
 		try {
 			return servletResponse.getOutputStream();
 		} catch (IOException e) {
 			return null;
 		}
 	}
 }
