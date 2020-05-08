 package com.justcloud.osgifier.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.FrameworkUtil;
 
 import com.justcloud.osgifier.annotation.REST;
 import com.justcloud.osgifier.annotation.REST.RESTMethod;
 import com.justcloud.osgifier.annotation.RESTParam;
 import com.justcloud.osgifier.service.Service;
 import com.justcloud.osgifier.service.impl.BundleServiceImpl;
 import com.justcloud.osgifier.service.impl.LogbackServiceImpl;
 
 import flexjson.JSONDeserializer;
 import flexjson.JSONSerializer;
 
 @WebServlet("/service/*")
 public class OsgifierServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -526862454372916367L;
 
 	private List<Class<? extends Service>> serviceClasses;
 	private Map<Class<? extends Service>, Service> instanceCache;
 	private JSONSerializer serializer;
 	private JSONDeserializer<Map<String, ?>> deserializer;
 
 	@Override
 	public void init() throws ServletException {
 		super.init();
 
 		instanceCache = new HashMap<Class<? extends Service>, Service>();
 
 		serializer = new JSONSerializer();
 		deserializer = new JSONDeserializer<Map<String, ?>>();
 
 		serviceClasses = new ArrayList<Class<? extends Service>>();
 
 		serviceClasses.add(BundleServiceImpl.class);
 		if (isLogbackInstalled()) {
 			serviceClasses.add(LogbackServiceImpl.class);
 		}
 		listenBundles();
 
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String path = buildUrl(req);
 		Method m = findRestMethod(RESTMethod.POST, path);
 		@SuppressWarnings("unchecked")
 		Service instance = findInstance((Class<? extends Service>) m
 				.getDeclaringClass());
 		Object args[] = new Object[m.getParameterTypes().length];
 		Map<String, ?> params = new HashMap<String, Object>();
 
 		Map<String, String> resultMap = new HashMap<String, String>();
 
 		try {
 			params = deserializer.deserialize(req.getReader());
 			int i = 0;
 
 			for (Annotation[] annotations : m.getParameterAnnotations()) {
 				RESTParam restAnnotation = null;
 
 				for (Annotation a : annotations) {
 					if (a.annotationType() == RESTParam.class) {
 						restAnnotation = (RESTParam) a;
 						break;
 					}
 				}
 				if (restAnnotation == null) {
 					throw new RuntimeException(
 							"REST method has non REST annotated parameter");
 				}
 				Class<?> targetClass = m.getParameterTypes()[i];
 				Object value = convert(params.get(restAnnotation.value()),
 						targetClass);
 				if (value == null) {
 					throw new RuntimeException("Parameter "
 							+ restAnnotation.value()
 							+ " not found in request for " + path);
 				}
 				args[i++] = value;
 			}
 
 			Object result = m.invoke(instance, args);
 			resultMap.put("outcome", "success");
 			if (result != null) {
 				resultMap.put("result", result.toString());
 			}
 
 		} catch (Exception e) {
 			Throwable t = e;
 			if (e instanceof InvocationTargetException) {
 				t = e.getCause();
 			}
 			StringWriter stringWriter = new StringWriter();
 			PrintWriter writer = new PrintWriter(stringWriter);
 
 			t.printStackTrace(writer);
 
 			resultMap.put("outcome", "error");
 			resultMap.put("message", t.getMessage());
 			resultMap.put("type", t.getClass().getCanonicalName());
 			resultMap.put("stacktrace", stringWriter.getBuffer().toString());
 		}
 
 		resp.setContentType("application/json");
 		resp.setCharacterEncoding("UTF-8");
 
 		serializer.deepSerialize(resultMap, resp.getWriter());
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String path = buildUrl(req);
 
 		resp.setContentType("application/json");
 		resp.setCharacterEncoding("UTF-8");
 
 		if ("/list".equals(path)) {
 			List<String> names = new ArrayList<String>();
 			try {
 				for (Class<? extends Service> sc : serviceClasses) {
 
 					Method m;
 
 					m = sc.getMethod("getName");
 					@SuppressWarnings("unchecked")
 					Service instance = findInstance((Class<? extends Service>) m
 							.getDeclaringClass());
 					String result = instance.getName();
 
 					names.add(result);
 
 				}
 				serializer.deepSerialize(names, resp.getWriter());
 			} catch (Exception e) {
 				Map<String, String> resultMap = new HashMap<String, String>();
 				Throwable t = e;
 				if (e instanceof InvocationTargetException) {
 					t = e.getCause();
 				}
 				StringWriter stringWriter = new StringWriter();
 				PrintWriter writer = new PrintWriter(stringWriter);
 
 				t.printStackTrace(writer);
 
 				resultMap.put("outcome", "error");
 				resultMap.put("message", t.getMessage());
 				resultMap.put("type", t.getClass().getCanonicalName());
 				resultMap
 						.put("stacktrace", stringWriter.getBuffer().toString());
 
 				serializer.deepSerialize(resultMap, resp.getWriter());
 			}
 		} else {
 			Method m = findRestMethod(RESTMethod.GET, path);
 			@SuppressWarnings("unchecked")
 			Service instance = findInstance((Class<? extends Service>) m
 					.getDeclaringClass());
 			try {
 				Object result = m.invoke(instance);
 
 				serializer.deepSerialize(result, resp.getWriter());
 			} catch (Exception e) {
 				Map<String, String> resultMap = new HashMap<String, String>();
 				Throwable t = e;
 				if (e instanceof InvocationTargetException) {
 					t = e.getCause();
 				}
 				StringWriter stringWriter = new StringWriter();
 				PrintWriter writer = new PrintWriter(stringWriter);
 
 				t.printStackTrace(writer);
 
 				resultMap.put("outcome", "error");
 				resultMap.put("message", t.getMessage());
 				resultMap.put("type", t.getClass().getCanonicalName());
 				resultMap
 						.put("stacktrace", stringWriter.getBuffer().toString());
 
 				serializer.deepSerialize(resultMap, resp.getWriter());
 			}
 		}
 
 	}
 
 	public String buildUrl(HttpServletRequest req) {
 		return req.getRequestURI().replace(req.getContextPath(), "")
 				.replace(req.getServletPath(), "");
 	}
 
 	private Service findInstance(Class<? extends Service> key) {
 		Service instance;
 		if (instanceCache.containsKey(key)) {
 			instance = instanceCache.get(key);
			instanceCache.put(key,instance);
 		} else {
 			try {
 				instance = key.newInstance();
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return instance;
 	}
 
 	private Object convert(Object value, Class<?> target) {
 		if (value.getClass() == Integer.class && target == Long.class) {
 			return new Long((Integer) value);
 		} else if (value.getClass() == Long.class && target == Integer.class) {
 			return ((Long) value).intValue();
 		}
 		return target.cast(value);
 	}
 
 	private Method findRestMethod(RESTMethod method, String url) {
 		for (Class<?> clazz : serviceClasses) {
 			Method m = findRestMethod(method, clazz, url);
 			if (m != null) {
 				return m;
 			}
 		}
 		throw new RuntimeException("No mapping for [" + url + "] and method ["
 				+ method + "] found");
 	}
 
 	private Method findRestMethod(RESTMethod method, Class<?> clazz, String url) {
 		for (Method m : clazz.getMethods()) {
 			REST rest = m.getAnnotation(REST.class);
 			if (rest != null && rest.url().equals(url)
 					&& rest.method() == method) {
 				return m;
 			}
 		}
 		return null;
 	}
 
 	private void listenBundles() {
 		final Bundle thisBundle = FrameworkUtil
 				.getBundle(OsgifierServlet.class);
 
 		BundleListener bundleListener = new BundleListener() {
 
 			@Override
 			public void bundleChanged(BundleEvent event) {
 
 				if (isLogbackInstalled()) {
 					serviceClasses.add(LogbackServiceImpl.class);
 				} else {
 					serviceClasses.remove(LogbackServiceImpl.class);
 				}
 
 			}
 		};
 
 		thisBundle.getBundleContext().addBundleListener(bundleListener);
 	}
 
 	private boolean isLogbackInstalled() {
 
 		try {
 			FrameworkUtil.getBundle(LogbackServiceImpl.class).loadClass(
 					"ch.qos.logback.classic.joran.JoranConfigurator");
 			return true;
 		} catch (ClassNotFoundException ex) {
 			return false;
 		}
 	}
 
 }
