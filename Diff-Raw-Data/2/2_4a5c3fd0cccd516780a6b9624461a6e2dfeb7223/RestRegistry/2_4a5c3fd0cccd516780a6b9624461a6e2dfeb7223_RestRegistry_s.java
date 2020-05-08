 package com.voxelperfect.restlite;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 
 import com.voxelperfect.restlite.PathTreeNode.DataRef;
 
 public class RestRegistry {
 
 	static RestRegistry INSTANCE;
 
 	public static RestRegistry getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new RestRegistry();
 		}
 		return INSTANCE;
 	}
 
 	public static class RequestHandler {
 		Object handlerInstance;
 		Method handlerMethod;
 		Class<?>[] handlerMethodParamTypes;
 		Annotation[][] handlerMethodParamAnnotations;
 	}
 
 	HashMap<String, Object> handlerInstances;
 
 	HashSet<Class<?>> validHandlerReturnTypes;
 
 	HashMap<String, PathTreeNode<RequestHandler>> handlerRoots;
 
 	private RestRegistry() {
 
 		this.handlerInstances = new HashMap<String, Object>(20);
 
 		this.handlerRoots = new HashMap<String, PathTreeNode<RequestHandler>>(
 				10);
 		handlerRoots.put("get", new PathTreeNode<RequestHandler>(""));
 		handlerRoots.put("post", new PathTreeNode<RequestHandler>(""));
 		handlerRoots.put("put", new PathTreeNode<RequestHandler>(""));
 		handlerRoots.put("delete", new PathTreeNode<RequestHandler>(""));
 	}
 
 	public DataRef<RequestHandler> getHandler(String httpMethod, String path) {
 
 		PathTreeNode<RequestHandler> root = handlerRoots.get(httpMethod
 				.toLowerCase());
 		return (root != null) ? root.getDataRef(path) : null;
 	}
 
 	public void registerHandler(String urlPrefix, Class<?> clazz)
 			throws InstantiationException, IllegalAccessException {
 
 		Path pathAnnotation = (Path) clazz.getAnnotation(Path.class);
 		if (pathAnnotation != null) {
 			Object instance = handlerInstances.get(clazz.getName());
 			if (instance == null) {
 				instance = clazz.newInstance();
 				handlerInstances.put(clazz.getName(), instance);
 			}
 
 			String rootPath = urlPrefix + pathAnnotation.value();
 			Method[] methods = clazz.getMethods();
 			for (Method method : methods) {
 				PathTreeNode<RequestHandler> root = getHandlerRoot(method);
 				if (root != null) {
 					String path = rootPath;
 					pathAnnotation = (Path) method.getAnnotation(Path.class);
 					if (pathAnnotation != null) {
 						path += pathAnnotation.value();
 					}
 					registerPath(path, instance, method);
 				}
 			}
 		}
 	}
 
 	protected boolean paramsMatch(Class<?>[] paramTypes, Class<?>[] validTypes) {
 
 		boolean match = true;
 		int count = validTypes.length;
 		for (int i = 0; i < count; i++) {
 			if (!paramTypes[i].equals(validTypes[i])) {
 				match = false;
 				break;
 			}
 		}
 		return match;
 	}
 
 	protected void registerPath(String path, Object instance, Method method) {
 
 		PathTreeNode<RequestHandler> root = getHandlerRoot(method);
 		RequestHandler entry = new RequestHandler();
 		entry.handlerInstance = instance;
 		entry.handlerMethod = method;
 		entry.handlerMethodParamTypes = method.getParameterTypes();
 		entry.handlerMethodParamAnnotations = method.getParameterAnnotations();
 		root.setData(path, entry);
 	}
 
 	protected PathTreeNode<RequestHandler> getHandlerRoot(Method method) {
 
 		PathTreeNode<RequestHandler> root = null;
 
 		if (method.getAnnotation(GET.class) != null) {
 			root = handlerRoots.get("get");
 		} else if (method.getAnnotation(POST.class) != null) {
 			root = handlerRoots.get("post");
 		} else if (method.getAnnotation(PUT.class) != null) {
			root = handlerRoots.get("pit");
 		} else if (method.getAnnotation(DELETE.class) != null) {
 			root = handlerRoots.get("delete");
 		}
 
 		return root;
 	}
 }
