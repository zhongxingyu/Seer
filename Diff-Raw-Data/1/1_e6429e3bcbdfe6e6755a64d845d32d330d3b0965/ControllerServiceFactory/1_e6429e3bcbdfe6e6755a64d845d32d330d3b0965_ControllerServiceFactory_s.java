 package com.nginious.http.application;
 
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 import com.nginious.http.HttpMethod;
 import com.nginious.http.HttpRequest;
 import com.nginious.http.HttpResponse;
 import com.nginious.http.annotation.Message;
 import com.nginious.http.annotation.Request;
 import com.nginious.http.annotation.Serializable;
 import com.nginious.http.annotation.Service;
 import com.nginious.http.serialize.DeserializerFactory;
 import com.nginious.http.serialize.SerializerFactory;
 import com.nginious.http.websocket.WebSocketBinaryMessage;
 import com.nginious.http.websocket.WebSocketOperation;
 import com.nginious.http.websocket.WebSocketSession;
 import com.nginious.http.websocket.WebSocketTextMessage;
 
 /**
  * Creates subclasses of {@link com.nginious.http.application.ControllerService} runtime for invoking 
  * the appropriate methods of a controller. The {@link com.nginious.http.annotation.Controller},
  * {@link com.nginious.http.annotation.Request} and {@link com.nginious.http.annotation.Message}
  * annotations of a controller class are inspected to determine which methods should be from the
  * generated controller service class.
  * 
  * @author Bojan Pisler, NetDigital Sweden AB
  *
  */
 public class ControllerServiceFactory {
 	
 	private static Logger logger = Logger.getLogger(ControllerServiceFactory.class);
 	
 	private ConcurrentHashMap<Class<?>, ControllerService> controllerServices;
 	
 	private Application application;
 	
 	private ApplicationClassLoader classLoader;
 	
 	private SerializerFactory serializerFactory;
 	
 	private DeserializerFactory deserializerFactory;
 	
 	/**
 	 * Constructs a new controller service factory which uses the specified application class loader
 	 * to load constructed controller service classes.
 	 * 
 	 * @param classLoader the class loader to load constructed class with
 	 */
 	public ControllerServiceFactory(ApplicationClassLoader classLoader) {
 		this.controllerServices = new ConcurrentHashMap<Class<?>, ControllerService>();
 		this.classLoader = classLoader;
 		this.serializerFactory = new SerializerFactory(this.classLoader);
 		this.deserializerFactory = new DeserializerFactory(this.classLoader);
 	}
 	
 	/**
 	 * Sets the application for this controller service factory to the specified application.
 	 * 
 	 * @param application the application
 	 */
 	void setApplication(Application application) {
 		this.application = application;
 	}
 	
 	/**
 	 * Removes the constructed controller service for the specified controller class from this factory.
 	 * 
 	 * @param controllerClazz the controller class to remove constructed controller service for
 	 */
 	void destroyControllerService(Class<?> controllerClazz) {
 		controllerServices.remove(controllerClazz);
 	}
 	
 	/**
 	 * Creates a new controller service for the specified controller object. If a controller service already
 	 * exists for the controller objects class, the existing controller service is returned.
 	 * 
 	 * @param controller the controller object to create a controller service for
 	 * @return the created controller service
 	 * @throws ControllerServiceFactoryException if unable to create controller service
 	 */
 	public ControllerService createControllerService(Object controller) throws ControllerServiceFactoryException {
 		Class<?> controllerClazz = controller.getClass();
 		ControllerService invokerService = (ControllerService)controllerServices.get(controllerClazz);
 		
 		if(invokerService != null) {
 			return invokerService;
 		}
 		
 		try {
 			String intClazzName = createInternalClassName(controllerClazz);
 			String intInvokerClazzName = new StringBuffer(intClazzName).append("Service").toString();		
 			
 			// Create class
 			ClassWriter writer = new ClassWriter(0);
 			String signature = "com/nginious/http/application/ControllerService";
 			writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, intInvokerClazzName, signature, signature, null);
 			
 			// Create constructor
 			MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
 			visitor.visitCode();
 	        visitor.visitVarInsn(Opcodes.ALOAD, 0);
 	        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/nginious/http/application/ControllerService", "<init>", "()V");
 	        visitor.visitInsn(Opcodes.RETURN);
 	        visitor.visitMaxs(2, 2);
 	        visitor.visitEnd();
 	        
 			// Override methods
 	        HashSet<Class<?>> clazzes = new HashSet<Class<?>>();
 	        StringBuffer httpMethods = new StringBuffer();
 	        overrideHttpMethod(writer, controllerClazz, "executeGet", HttpMethod.GET, httpMethods, clazzes);
 	        overrideHttpMethod(writer, controllerClazz, "executePost", HttpMethod.POST, httpMethods, clazzes);
 	        overrideHttpMethod(writer, controllerClazz, "executePut", HttpMethod.PUT, httpMethods, clazzes);
 	        overrideHttpMethod(writer, controllerClazz, "executeDelete", HttpMethod.DELETE, httpMethods, clazzes);
 	        overrideWebSocketMethod(writer, controllerClazz, WebSocketOperation.OPEN);
 	        overrideWebSocketMethod(writer, controllerClazz, WebSocketOperation.CLOSE);
 	        overrideWebSocketMethod(writer, controllerClazz, WebSocketOperation.TEXT);
 	        overrideWebSocketMethod(writer, controllerClazz, WebSocketOperation.BINARY);
 			
 			writer.visitEnd();
 			byte[] clazzBytes = writer.toByteArray();
 			ClassLoader controllerLoader = null;
 			
 			if(classLoader.hasLoaded(controller.getClass())) {
 				controllerLoader = controller.getClass().getClassLoader();
 			} else {
 				controllerLoader = this.classLoader;
 			}
 			
 			Class<?> clazz = loadClass(controllerLoader, intInvokerClazzName.replace('/', '.'), clazzBytes);
 			invokerService = (ControllerService)clazz.newInstance();
 			invokerService.setController(controller);
 			invokerService.setApplication(this.application);
 			invokerService.setClassLoader(this.classLoader);
 			invokerService.setClasses(clazzes);
 			invokerService.setSerializerFactory(this.serializerFactory);
 			invokerService.setDeserializerFactory(this.deserializerFactory);
 			invokerService.setHttpMethods(httpMethods.toString());
 			controllerServices.put(controllerClazz, invokerService);
 			return invokerService;
 		} catch(IllegalAccessException e) {
 			throw new ControllerServiceFactoryException(e);
 		} catch(InstantiationException e) {
 			throw new ControllerServiceFactoryException(e);
 		}
 	}
 	
 	void overrideHttpMethod(ClassWriter writer, 
 			Class<?> controllerClazz, 
 			String controllerMethodName, 
 			HttpMethod httpMethod, 
 			StringBuffer httpMethods,
 			HashSet<Class<?>> clazzes) throws ControllerServiceFactoryException {
 		Method method = findHttpMethod(controllerClazz, httpMethod);
 		
 		if(method != null) {
 			Request request = method.getAnnotation(Request.class);
 			createHttpMethod(writer, method, controllerClazz, controllerMethodName, clazzes, request.async());
 			
 			if(httpMethods.length() > 0) {
 				httpMethods.append(", ");
 			}
 			
 			if(httpMethod.equals(HttpMethod.GET)) {
 				httpMethods.append(HttpMethod.HEAD);
 				httpMethods.append(", ");
 				httpMethods.append(HttpMethod.GET);
 			} else {
 				httpMethods.append(httpMethod.name());
 			}
 		}
 	}
 	
 	void overrideWebSocketMethod(ClassWriter writer, Class<?> controllerClazz, WebSocketOperation operation) throws ControllerServiceFactoryException {
 		Method method = findWebSocketMethod(controllerClazz, operation);
 		
 		if(method != null) {
 			switch(operation) {
 			case OPEN:
 				createWebSocketOpenMethod(writer, method, controllerClazz);
 				break;
 				
 			case CLOSE:
 				createWebSocketCloseMethod(writer, method, controllerClazz);
 				break;
 				
 			case TEXT:
 				createWebSocketTextMessageMethod(writer, method, controllerClazz);
 				break;
 				
 			case BINARY:
 				createWebSocketBinaryMessageMethod(writer, method, controllerClazz);
 				break;
 			}
 		}
 	}
 	
 	void createHttpMethod(ClassWriter writer, 
 			Method controllerMethod, 
 			Class<?> controllerClazz, 
 			String methodName, 
 			HashSet<Class<?>> clazzes, 
 			boolean async) throws ControllerServiceFactoryException {
 		String controllerClazzName = createInternalClassName(controllerClazz);
 		String[] exceptions = { "com/nginious/http/HttpException", "java/io/IOException" };
 		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, methodName, 
 				"(Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;)Lcom/nginious/http/application/HttpServiceResult;", null, exceptions);
 		visitor.visitCode();
 		
 		Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
 		Class<?> returnType = controllerMethod.getReturnType();
 		
 		if(!returnType.equals(Void.class) && !returnType.equals(void.class)) {
 			// Prepared for call to serialize method to handle result, must be on the stack before result object
 			visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		}
 		
 		String controllerMethodSignature = createHttpMethodSignature(controllerClazz, controllerMethod);
 		visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getController", "()Ljava/lang/Object;");
 		visitor.visitTypeInsn(Opcodes.CHECKCAST, controllerClazzName); // Cast controller object to its right type
 		boolean serialize = false;
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(HttpRequest.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 1);
 			} else if(parameterType.equals(HttpResponse.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			} else if(parameterType.isAnnotationPresent(Serializable.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 0);
 				visitor.visitLdcInsn(parameterType.getName());
 				visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
 				visitor.visitVarInsn(Opcodes.ALOAD, 1);
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "deserialize", "(Ljava/lang/Class;Lcom/nginious/http/HttpRequest;)Ljava/lang/Object;");
 				String parameterClazzName = createInternalClassName(parameterType);
 				visitor.visitTypeInsn(Opcodes.CHECKCAST, parameterClazzName);
 				serialize = true;
 				clazzes.add(parameterType);
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				Service mapping = parameterType.getAnnotation(Service.class);
 				visitor.visitVarInsn(Opcodes.ALOAD, 0);
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getApplication", "()Lcom/nginious/http/application/Application;");
 				visitor.visitLdcInsn(mapping.name());
 				visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/application/Application", "getService", "(Ljava/lang/String;)Ljava/lang/Object;");
 				String parameterClazzName = createInternalClassName(parameterType);
 				visitor.visitTypeInsn(Opcodes.CHECKCAST, parameterClazzName);
 			} else {
 				throw new ControllerServiceFactoryException("Unsupported parameter type: " + parameterType.getName());
 			}
 		}
 		
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, controllerClazzName, controllerMethod.getName(), controllerMethodSignature);
 		
 		if(returnType.equals(String.class)) {
 			visitor.visitVarInsn(Opcodes.ALOAD, 1);
 			visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "response",
 					"(Ljava/lang/String;Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;)V");
 		} else if(!returnType.equals(Void.class) && !returnType.equals(void.class)) {
 			visitor.visitVarInsn(Opcodes.ALOAD, 1);
 			visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "serialize", 
 			 		"(Ljava/lang/Object;Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;)V");
 			clazzes.add(returnType);
 		} else if(!async && serialize && (returnType.equals(Void.class) || returnType.equals(void.class))) {
 			visitor.visitVarInsn(Opcodes.ALOAD, 0);
 			visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "serializeVoid", 
 			 		"(Lcom/nginious/http/HttpResponse;)V");
 		}
 		
 		if(async) {
 			visitor.visitFieldInsn(Opcodes.GETSTATIC, "com/nginious/http/application/HttpServiceResult", "ASYNC", "Lcom/nginious/http/application/HttpServiceResult;");
 		} else {
 			Label labelFalse = new Label();
 			Label labelEnd = new Label();
 			
 			visitor.visitVarInsn(Opcodes.ALOAD, 2); // HttpResponse
 			visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpResponse", "isCommitted", "()Z");
 			
 			// Test if committed
 	 		visitor.visitInsn(Opcodes.ICONST_1);
 			visitor.visitJumpInsn(Opcodes.IF_ICMPNE, labelFalse);
 			
 			// True
 			visitor.visitFieldInsn(Opcodes.GETSTATIC, "com/nginious/http/application/HttpServiceResult", "DONE", "Lcom/nginious/http/application/HttpServiceResult;");
 			visitor.visitJumpInsn(Opcodes.GOTO, labelEnd);
 			
 			// False
 			visitor.visitLabel(labelFalse);
 			visitor.visitFieldInsn(Opcodes.GETSTATIC, "com/nginious/http/application/HttpServiceResult", "CONTINUE", "Lcom/nginious/http/application/HttpServiceResult;");
 			
 			visitor.visitLabel(labelEnd);			
 		}
 		
 		visitor.visitInsn(Opcodes.ARETURN);
 		visitor.visitMaxs(7, 6);
 		visitor.visitEnd();
 	}
 	
 	void createWebSocketOpenMethod(ClassWriter writer, Method controllerMethod, Class<?> controllerClazz) throws ControllerServiceFactoryException {
 		String controllerClazzName = createInternalClassName(controllerClazz);
 		String[] exceptions = { "com/nginious/http/HttpException", "java/io/IOException" };
 		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "executeOpen", 
 				"(Lcom/nginious/http/HttpRequest;Lcom/nginious/http/HttpResponse;Lcom/nginious/http/websocket/WebSocketSession;)V", null, exceptions);
 		visitor.visitCode();
 		
 		visitor.visitVarInsn(Opcodes.ALOAD, 1);
 		visitor.visitLdcInsn("se.netdigital.http.websocket.WebSocketSession");
 		visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/nginious/http/HttpRequest", "getAttribute", "(Ljava/lang/String;)Ljava/lang/Object;");
 		visitor.visitTypeInsn(Opcodes.CHECKCAST, "com/nginious/http/websocket/WebSocketSessionImpl");
 		visitor.visitVarInsn(Opcodes.ASTORE, 3);
 		visitor.visitVarInsn(Opcodes.ALOAD, 3);
 		visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/websocket/WebSocketSessionImpl", "setService", "(Lcom/nginious/http/application/ControllerService;)V");
 		
 		Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
 		
 		String controllerMethodSignature = createWebSocketOpenMethodSignature(controllerClazz, controllerMethod);
 		visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getController", "()Ljava/lang/Object;");
 		visitor.visitTypeInsn(Opcodes.CHECKCAST, controllerClazzName); // Cast controller object to its right type
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(HttpRequest.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 1);
 			} else if(parameterType.equals(HttpResponse.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			} else if(parameterType.equals(WebSocketSession.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 3);
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				Service mapping = parameterType.getAnnotation(Service.class);
 				visitor.visitVarInsn(Opcodes.ALOAD, 0);
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getApplication", "()Lcom/nginious/http/application/ApplicationImpl;");
 				visitor.visitLdcInsn(mapping.name());
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ApplicationImpl", "getService", "(Ljava/lang/String;)Ljava/lang/Object;");
 				String parameterClazzName = createInternalClassName(parameterType);
 				visitor.visitTypeInsn(Opcodes.CHECKCAST, parameterClazzName);				
 			} else {
 				throw new ControllerServiceFactoryException("Unsupported parameter type: " + parameterType.getName());
 			}
 		}
 		
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, controllerClazzName, controllerMethod.getName(), controllerMethodSignature);
 		visitor.visitInsn(Opcodes.RETURN);
 		visitor.visitMaxs(7, 6);
 		visitor.visitEnd();
 	}
 	
 	void createWebSocketCloseMethod(ClassWriter writer, Method controllerMethod, Class<?> controllerClazz) throws ControllerServiceFactoryException {
 		String controllerClazzName = createInternalClassName(controllerClazz);
 		String[] exceptions = { "com/nginious/http/websocket/WebSocketException", "java/io/IOException" };
 		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "executeClose", "(Lcom/nginious/http/websocket/WebSocketSession;)V", null, exceptions);
 		visitor.visitCode();
 		
 		Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
 		
 		String controllerMethodSignature = createWebSocketCloseMethodSignature(controllerClazz, controllerMethod);
 		visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getController", "()Ljava/lang/Object;");
 		visitor.visitTypeInsn(Opcodes.CHECKCAST, controllerClazzName); // Cast controller object to its right type
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(WebSocketSession.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 1);
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				Service mapping = parameterType.getAnnotation(Service.class);
 				visitor.visitVarInsn(Opcodes.ALOAD, 0);
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getApplication", "()Lcom/nginious/http/application/ApplicationImpl;");
 				visitor.visitLdcInsn(mapping.name());
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ApplicationImpl", "getService", "(Ljava/lang/String;)Ljava/lang/Object;");
 				String parameterClazzName = createInternalClassName(parameterType);
 				visitor.visitTypeInsn(Opcodes.CHECKCAST, parameterClazzName);				
 			} else {
 				throw new ControllerServiceFactoryException("Unsupported parameter type: " + parameterType.getName());
 			}
 		}
 		
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, controllerClazzName, controllerMethod.getName(), controllerMethodSignature);
 		visitor.visitInsn(Opcodes.RETURN);
 		visitor.visitMaxs(7, 6);
 		visitor.visitEnd();
 	}
 	
 	void createWebSocketTextMessageMethod(ClassWriter writer, Method controllerMethod, Class<?> controllerClazz) throws ControllerServiceFactoryException {
 		String controllerClazzName = createInternalClassName(controllerClazz);
 		String[] exceptions = { "com/nginious/http/websocket/WebSocketException" };
 		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "executeTextMessage", 
 				"(Lcom/nginious/http/websocket/WebSocketTextMessage;Lcom/nginious/http/websocket/WebSocketSession;)V", null, exceptions);
 		visitor.visitCode();
 		
 		Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
 		Class<?> returnType = controllerMethod.getReturnType();
 		
 		if(!returnType.equals(Void.class) && !returnType.equals(void.class)) {
 			// Prepared for call to sendXXXMessage method to handle result, must be on the stack before result object
 			visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		}
 		
 		String controllerMethodSignature = createWebSocketTextMessageMethodSignature(controllerClazz, controllerMethod);
 		visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getController", "()Ljava/lang/Object;");
 		visitor.visitTypeInsn(Opcodes.CHECKCAST, controllerClazzName); // Cast controller object to its right type
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(WebSocketSession.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			} else if(parameterType.equals(WebSocketTextMessage.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 1);
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				Service mapping = parameterType.getAnnotation(Service.class);
 				visitor.visitVarInsn(Opcodes.ALOAD, 0);
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getApplication", "()Lcom/nginious/http/application/ApplicationImpl;");
 				visitor.visitLdcInsn(mapping.name());
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ApplicationImpl", "getService", "(Ljava/lang/String;)Ljava/lang/Object;");
 				String parameterClazzName = createInternalClassName(parameterType);
 				visitor.visitTypeInsn(Opcodes.CHECKCAST, parameterClazzName);				
 			} else {
 				throw new ControllerServiceFactoryException("Unsupported parameter type: " + parameterType.getName());
 			}
 		}
 		
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, controllerClazzName, controllerMethod.getName(), controllerMethodSignature);
 		
 		if(returnType.equals(String.class)) {
 			visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "sendTextMessage",
 					"(Ljava/lang/String;Lcom/nginious/http/websocket/WebSocketSession;)V)");
 		} else if(returnType.equals(byte[].class)) {
 			visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "sendBinaryMessage", 
 			 		"([B;Lcom/nginious/http/websocket/WebSocketSession;)V");
 		}
 		
 		visitor.visitInsn(Opcodes.RETURN);
 		visitor.visitMaxs(7, 6);
 		visitor.visitEnd();
 	}
 	
 	void createWebSocketBinaryMessageMethod(ClassWriter writer, Method controllerMethod, Class<?> controllerClazz) throws ControllerServiceFactoryException {
 		String controllerClazzName = createInternalClassName(controllerClazz);
 		String[] exceptions = { "com/nginious/http/websocket/WebSocketException" };
 		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "executeBinaryMessage", 
 				"(Lcom/nginious/http/websocket/WebSocketBinaryMessage;Lcom/nginious/http/websocket/WebSocketSession;)V", null, exceptions);
 		visitor.visitCode();
 		
 		Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
 		Class<?> returnType = controllerMethod.getReturnType();
 		
 		if(!returnType.equals(Void.class) && !returnType.equals(void.class)) {
 			// Prepared for call to sendXXXMessage method to handle result, must be on the stack before result object
 			visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		}
 		
 		String controllerMethodSignature = createWebSocketBinaryMessageMethodSignature(controllerClazz, controllerMethod);
 		visitor.visitVarInsn(Opcodes.ALOAD, 0);
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getController", "()Ljava/lang/Object;");
 		visitor.visitTypeInsn(Opcodes.CHECKCAST, controllerClazzName); // Cast controller object to its right type
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(WebSocketSession.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			} else if(parameterType.equals(WebSocketBinaryMessage.class)) {
 				visitor.visitVarInsn(Opcodes.ALOAD, 1);
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				Service mapping = parameterType.getAnnotation(Service.class);
 				visitor.visitVarInsn(Opcodes.ALOAD, 0);
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "getApplication", "()Lcom/nginious/http/application/ApplicationImpl;");
 				visitor.visitLdcInsn(mapping.name());
 				visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ApplicationImpl", "getService", "(Ljava/lang/String;)Ljava/lang/Object;");
 				String parameterClazzName = createInternalClassName(parameterType);
 				visitor.visitTypeInsn(Opcodes.CHECKCAST, parameterClazzName);				
 			} else {
 				throw new ControllerServiceFactoryException("Unsupported parameter type: " + parameterType.getName());
 			}
 		}
 		
 		visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, controllerClazzName, controllerMethod.getName(), controllerMethodSignature);
 		
 		if(returnType.equals(String.class)) {
 			visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "sendTextMessage",
 					"(Ljava/lang/String;Lcom/nginious/http/websocket/WebSocketSession;)V)");
 		} else if(returnType.equals(byte[].class)) {
 			visitor.visitVarInsn(Opcodes.ALOAD, 2);
 			visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/nginious/http/application/ControllerService", "sendBinaryMessage", 
 			 		"([B;Lcom/nginious/http/websocket/WebSocketSession;)V");
 		}
 		
 		visitor.visitInsn(Opcodes.RETURN);
 		visitor.visitMaxs(7, 6);
 		visitor.visitEnd();
 	}
 	
 	String createHttpMethodSignature(Class<?> controllerClazz, Method method) throws ControllerServiceFactoryException {
 		StringBuffer signature = new StringBuffer("(");
 		Class<?>[] parameterTypes = method.getParameterTypes();
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(HttpRequest.class)) {
 				signature.append("Lcom/nginious/http/HttpRequest;");
 			} else if(parameterType.equals(HttpResponse.class)) {
 				signature.append("Lcom/nginious/http/HttpResponse;");
 			} else if(parameterType.isPrimitive()) {
 				String controllerClazzName = controllerClazz.getName();
 				throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " takes incompatible type");
 			} else {
 				String intParameterName = createInternalClassName(parameterType);
 				signature.append("L");
 				signature.append(intParameterName);
 				signature.append(";");
 			}
 		}
 		
 		signature.append(")");
 		
 		Class<?> returnType = method.getReturnType();		
 		
 		if(returnType.equals(void.class)) {
 			signature.append("V");
 		} else if(returnType.equals(Void.class)) {
 			signature.append("Ljava/lang/Void;");
 		} else if(returnType.isPrimitive()) {
 			String controllerClazzName = controllerClazz.getName(); 
 			throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " returns primitive type");
 		} else {
 			String intReturnName = createInternalClassName(returnType);
 			signature.append("L");
 			signature.append(intReturnName);
 			signature.append(";");
 		}
 		
 		return signature.toString();
 	}
 	
 	String createWebSocketOpenMethodSignature(Class<?> controllerClazz, Method method) throws ControllerServiceFactoryException {
 		StringBuffer signature = new StringBuffer("(");
 		Class<?>[] parameterTypes = method.getParameterTypes();
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(HttpRequest.class)) {
 				signature.append("Lcom/nginious/http/HttpRequest;");
 			} else if(parameterType.equals(HttpResponse.class)) {
 				signature.append("Lcom/nginious/http/HttpResponse;");
 			} else if(parameterType.equals(WebSocketSession.class)) {
 				signature.append("Lcom/nginious/http/websocket/WebSocketSession;");
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				String intReturnName = createInternalClassName(parameterType);
 				signature.append("L");
 				signature.append(intReturnName);
 				signature.append(";");
 			} else {
 				String controllerClazzName = controllerClazz.getName();
 				throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " takes incompatible type");
 			}
 		}
 		
 		signature.append(")V");
 		
 		Class<?> returnType = method.getReturnType();
 		
 		if(!returnType.equals(void.class)) {
 			String controllerClazzName = controllerClazz.getName();
 			throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " return type is not void");
 		}
 		
 		return signature.toString();
 	}
 	
 	String createWebSocketTextMessageMethodSignature(Class<?> controllerClazz, Method method) throws ControllerServiceFactoryException {
 		StringBuffer signature = new StringBuffer("(");
 		Class<?>[] parameterTypes = method.getParameterTypes();
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(WebSocketSession.class)) {
 				signature.append("Lcom/nginious/http/websocket/WebSocketSession;");
 			} else if(parameterType.equals(WebSocketTextMessage.class)) {
 				signature.append("Lcom/nginious/http/websocket/WebSocketTextMessage;");
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				String intReturnName = createInternalClassName(parameterType);
 				signature.append("L");
 				signature.append(intReturnName);
 				signature.append(";");				
 			} else {
 				String controllerClazzName = controllerClazz.getName();
 				throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " takes incompatible type");
 			}
 		}
 		
 		signature.append(")");
 		
 		Class<?> returnType = method.getReturnType();
 		
 		if(returnType.equals(void.class)) {
 			signature.append("V");
 		} else if(returnType.equals(Void.class)) {
 			signature.append("Ljava/lang/Void;");
 		} else if(returnType.equals(WebSocketTextMessage.class)) {
 			signature.append("Lcom/nginious/http/websocket/WebSocketTextMessage;");
 		} else if(returnType.equals(WebSocketBinaryMessage.class)) {
 			signature.append("Lcom/nginious/http/websocket/WebSocketBinaryMessage;");
 		} else {
 			String controllerClazzName = controllerClazz.getName(); 
 			throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " returns primitive type");
 		}
 		
 		return signature.toString();
 	}
 	
 	String createWebSocketBinaryMessageMethodSignature(Class<?> controllerClazz, Method method) throws ControllerServiceFactoryException {
 		StringBuffer signature = new StringBuffer("(");
 		Class<?>[] parameterTypes = method.getParameterTypes();
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(WebSocketSession.class)) {
 				signature.append("Lcom/nginious/http/websocket/WebSocketSession;");
 			} else if(parameterType.equals(WebSocketBinaryMessage.class)) {
 				signature.append("Lcom/nginious/http/websocket/WebSocketBinaryMessage;");
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				String intReturnName = createInternalClassName(parameterType);
 				signature.append("L");
 				signature.append(intReturnName);
 				signature.append(";");				
 			} else {
 				String controllerClazzName = controllerClazz.getName();
 				throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " takes incompatible type");
 			}
 		}
 		
 		signature.append(")");
 		
 		Class<?> returnType = method.getReturnType();
 		
 		if(returnType.equals(void.class)) {
 			signature.append("V");
 		} else if(returnType.equals(Void.class)) {
 			signature.append("Ljava/lang/Void;");
 		} else if(returnType.equals(WebSocketTextMessage.class)) {
 			signature.append("Lcom/nginious/http/websocket/WebSocketTextMessage;");
 		} else if(returnType.equals(WebSocketBinaryMessage.class)) {
 			signature.append("Lcom/nginious/http/websocket/WebSocketBinaryMessage;");
 		} else {
 			String controllerClazzName = controllerClazz.getName(); 
 			throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " returns primitive type");
 		}
 		
 		return signature.toString();
 	}
 	
 	String createWebSocketCloseMethodSignature(Class<?> controllerClazz, Method method) throws ControllerServiceFactoryException {
 		StringBuffer signature = new StringBuffer("(");
 		Class<?>[] parameterTypes = method.getParameterTypes();
 		
 		for(Class<?> parameterType : parameterTypes) {
 			if(parameterType.equals(WebSocketSession.class)) {
 				signature.append("Lcom/nginious/http/websocket/WebSocketSession;");
 			} else if(parameterType.isAnnotationPresent(Service.class)) {
 				String intReturnName = createInternalClassName(parameterType);
 				signature.append("L");
 				signature.append(intReturnName);
 				signature.append(";");
 			} else {
 				String controllerClazzName = controllerClazz.getName();
 				throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " takes incompatible type");
 			}
 		}
 		
 		signature.append(")V");
 		
 		Class<?> returnType = method.getReturnType();
 		
 		if(!returnType.equals(void.class)) {
 			String controllerClazzName = controllerClazz.getName();
 			throw new ControllerServiceFactoryException("Method " + method.getName() + " in class " + controllerClazzName + " return type is not void");
 		}
 		
 		return signature.toString();
 	}
 	
 	Method findHttpMethod(Class<?> controllerClazz, HttpMethod httpMethod) {
 		Method[] methods = controllerClazz.getMethods();
 		
 		for(Method method : methods) {
 			Request request = method.getAnnotation(Request.class);
 			
 			if(request != null) {
 				HttpMethod[] requestMethods = request.methods();
 				
 				for(HttpMethod requestMethod : requestMethods) {
 					if(requestMethod.equals(httpMethod)) {
 						return method;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	Method findWebSocketMethod(Class<?> controllerClazz, WebSocketOperation operation) {
 		Method[] methods = controllerClazz.getMethods();
 		
 		for(Method method : methods) {
 			Message message = method.getAnnotation(Message.class);
 			
 			if(message != null) {
 				WebSocketOperation[] messageOperations = message.operations();
 				
 				for(WebSocketOperation messageOperation : messageOperations) {
 					if(messageOperation.equals(operation)) {
 						return method;
 					}
 				}
 			}
 		}
 		
 		return null;		
 	}
 	
 	String createInternalClassName(Class<?> clazz) {
 		return clazz.getName().replace('.', '/');
 	}
 
 	String createClassSignature(String clazzName, String controllerClazzName) {
 		StringBuffer signature = new StringBuffer("L");
 		signature.append(clazzName);
 		signature.append("<L");
 		signature.append(controllerClazzName);
 		signature.append(";>;");
 		return signature.toString();
 	}
 	
 	Class<?> loadClass(ClassLoader loader, String className, byte[] b) throws ControllerServiceFactoryException {
     	Class<?> clazz = null;
     	
     	try {
     		Class<?> cls = Class.forName("java.lang.ClassLoader");
     		java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
     		
     		// protected method invocation
     		method.setAccessible(true);
     		
     		try {
     			Object[] args = new Object[] { className, b, new Integer(0), new Integer(b.length)};
     			clazz = (Class<?>)method.invoke(loader, args);
     		} finally {
     			method.setAccessible(false);
     		}
         } catch(Exception e) {
         	logger.warn("Unable to load create controll service class", e);
         	throw new ControllerServiceFactoryException("Unable to load class", e);
         }
         
         return clazz;
     }
 }
