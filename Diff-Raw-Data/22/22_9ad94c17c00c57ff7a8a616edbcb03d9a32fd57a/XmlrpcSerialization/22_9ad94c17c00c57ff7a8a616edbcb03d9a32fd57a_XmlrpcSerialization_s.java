 /*
 Copyright 2008 Flaptor (flaptor.com) 
 
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 
     http://www.apache.org/licenses/LICENSE-2.0 
 
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */
 
 package com.flaptor.util.remote;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 import javassist.NotFoundException;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.ranges.RangeException;
 
 import com.flaptor.util.Execute;
 import com.flaptor.util.IOUtil;
 import com.flaptor.util.Pair;
 import com.flaptor.util.Triad;
 
 /**
  * Class for creating serialization handlers for xml-rpc
  * if an object is not supported in xml-rpc, it is serialized transmitted
  * as byte[] and deserialized on the other end
  * 
  * @author Martin Massera
  */
 public class XmlrpcSerialization {
 
 	private static Logger logger = Logger.getLogger(Execute.whoAmI());
 
 	private static AtomicInteger classCounter = new AtomicInteger(0);
 	
 	/**
 	 * creates a handler that receives method calls with serialized parameters 
 	 * deserializes them and calls the original handler
 	 * 
 	 * @param originalHandler the original handler. The class must be accesible (public) by this method, else it will throw IllegalAccessException IN THE CLIENT
 	 * @return the return value or the serialized version of the return value if it is not supported by xml-rpc 
 	 */
 	public static Object handler(final Object originalHandler) {
 		try {
 			logger.debug("creating serialization handler for " + originalHandler);
 			
 			String name = "dynamicallyGeneratedSerialization.class" + classCounter.incrementAndGet() + "." + originalHandler.getClass().getCanonicalName();		
 			logger.debug("interface name " + name);
 
 			
 			ClassPool cp = ClassPool.getDefault();
 			CtClass interf = cp.makeInterface(name);
 			List<Method> objectMethods  = Arrays.asList(Object.class.getMethods());
 
 			final Map<Pair<String,Class[]>, Triad<Method,Boolean,boolean[]>> methodMap = new HashMap<Pair<String,Class[]>, Triad<Method,Boolean,boolean[]>>(); 
 			
 			for (Method method : originalHandler.getClass().getMethods()) {
 				if (objectMethods.contains(method)) {
 					logger.debug("skipping method from object: " + method);
 					continue;
 				}
 				logger.debug("processing method " + method.getName());
 				
 				Class retType = method.getReturnType();
 				CtClass newRetType = getCorrespondingClass(retType);
 				boolean serializeReturn = !isSupportedClass(retType);
 
 				logger.debug(retType + " -> " + newRetType);
 
 				Class[] paramTypes = method.getParameterTypes();
 				Class[] newParamTypes = new Class[paramTypes.length];
 				CtClass[] newParamTypesCT = new CtClass[paramTypes.length];
 				boolean[] serialize = new boolean[paramTypes.length];
 				
 				for (int i = 0; i < paramTypes.length; i++) {
 					if (isSupportedClass(paramTypes[i])) {
 						serialize[i] = false;
 						newParamTypes[i] = paramTypes[i];
 					} else {
 						serialize[i] = true;
 						newParamTypes[i] = byte[].class;
 					}
 					
 					newParamTypesCT[i] = getCorrespondingClass(paramTypes[i]);
 					logger.debug(paramTypes[i] + " -> " + newParamTypesCT[i]);
 				}
 				methodMap.put(
 						new Pair<String,Class[]>(method.getName(), newParamTypes),
 						new Triad<Method,Boolean,boolean[]>(method, serializeReturn, serialize));
 				
 				interf.addMethod(new CtMethod(newRetType, method.getName(),newParamTypesCT, interf));
 			}
 			Class handlerInterface = interf.toClass();
 			
 			InvocationHandler handler = new InvocationHandler() {
 				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 					try {
 						if (args == null) args = new Object[0];
 						Triad<Method,Boolean,boolean[]> originalMethodInfo = getMethod(methodMap, method.getName(), args);
 					
						if (originalMethodInfo == null) throw new RuntimeException("original method should not be null");
 						
 						logger.debug("executing " + method);
 						Object[] newArgs = new Object[args.length];
 						for (int i = 0; i < newArgs.length; i++) {
 							if (originalMethodInfo.third()[i]) {
 								newArgs[i] = IOUtil.deserialize((byte[])args[i], false);
 							} else {
 								newArgs[i] = args[i];
 							}
 						}
 						logger.debug("with method " + originalMethodInfo.first());
 						Object ret = originalMethodInfo.first().invoke(originalHandler, newArgs);
 						if (originalMethodInfo.second()) {
 							logger.debug("serializing return: " + ret);
 							ret = IOUtil.serialize(ret, false);
 						}
 						return ret;
 					} catch (InvocationTargetException e) {
 						logger.error(e.getCause());
 						throw e.getCause();
 					}
 				}
 			};
 			return Proxy.newProxyInstance(handlerInterface.getClassLoader(), new Class[]{handlerInterface}, handler);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * returns the original method info corresponding to the method name and arguments
 	 * @param methodMap
 	 * @param name
 	 * @param args
 	 * @return
 	 */
 	private static Triad<Method,Boolean,boolean[]> getMethod(Map<Pair<String,Class[]>, Triad<Method,Boolean,boolean[]>> methodMap, String name, Object[] args) {
 		for (Map.Entry<Pair<String,Class[]>, Triad<Method,Boolean,boolean[]>> entry: methodMap.entrySet()) {
 			String methodName = entry.getKey().first();
 			Class[] methodArgs = entry.getKey().last();
 			
 			if (name.equals(methodName) && args.length == methodArgs.length) {
 				boolean isMethod = true;
 				for (int i = 0; i < args.length; i++) {
					if (!methodArgs[i].isInstance(args[i])) {
 						isMethod = false;
 						break;
 					}
 				}
 				if (isMethod) return entry.getValue(); 
 			}
 		}
 		//should not happen
 		return null;
 	}
 	
 	
 	private static CtClass getCorrespondingClass(Class c) throws NotFoundException {
 		if (isSupportedClass(c)) {
 			return ClassPool.getDefault().get(c.getCanonicalName());
 		} else {
 			return ClassPool.getDefault().get("byte[]");
 		}
 	}
 	
 	public static boolean isSupportedClass(Class c) {
 		return
 			c.isPrimitive() ||
 			c.equals(Integer.class) ||
 			c.equals(Boolean.class) ||
 			c.equals(String.class) ||
 			c.equals(Double.class) ||
 			c.equals(Hashtable.class) ||
 			c.equals(Vector.class) ||
 			c.equals(byte[].class);
 	}
 
 	
 	public static boolean isSupportedClass(Object o) {
 		return 
 			o instanceof Integer ||
 			o instanceof Boolean ||
 			o instanceof String ||
 			o instanceof Double ||
 			o instanceof Hashtable||
 			o instanceof Vector ||
 			o instanceof byte[];
 	}
 }
