 package org.lightmare.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.lightmare.ejb.EjbConnector;
 import org.lightmare.remote.rpc.RPCall;
 import org.lightmare.remote.rpc.wrappers.RpcWrapper;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 
 /**
  * Listener class for serialization and de-serialization (both java and json) of
  * objects and call bean {@link Method}s connection to bean remotely
  * 
  * @author Levan
  * 
  */
 public class RpcUtils {
 
 	public static final int PROTOCOL_SIZE = 20;
 
 	public static final int INT_SIZE = 4;
 
 	public static final int BYTE_SIZE = 1;
 
 	private static final ObjectMapper MAPPER = new ObjectMapper();
 
 	private static boolean mapperConfigured;
 
 	private static ObjectMapper getMapper() {
 		if (mapperConfigured) {
 			return MAPPER;
 		} else {
 			synchronized (RpcUtils.class) {
 				MAPPER.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
 			}
 
 			return MAPPER;
 		}
 	}
 
 	public static byte[] serialize(Object value) throws IOException {
 
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		ObjectOutputStream objectStream = new ObjectOutputStream(stream);
 
 		try {
 
 			objectStream.writeObject(value);
 			byte[] data = stream.toByteArray();
 
 			return data;
 
 		} finally {
 			stream.close();
 			objectStream.close();
 		}
 	}
 
 	public static <T> String write(Object value) throws IOException {
 		String data;
 		try {
 			data = getMapper().writeValueAsString(value);
 			return data;
 		} catch (JsonGenerationException ex) {
 			throw new IOException(ex);
 		} catch (JsonMappingException ex) {
 			throw new IOException(ex);
 		} catch (IOException ex) {
 			throw new IOException(ex);
 		}
 	}
 
 	public static Object deserialize(byte[] data) throws IOException {
 
 		ByteArrayInputStream stream = new ByteArrayInputStream(data);
 		ObjectInputStream objectStream = new ObjectInputStream(stream);
 		try {
 
 			Object value = objectStream.readObject();
 
 			return value;
 
 		} catch (ClassNotFoundException ex) {
 
 			throw new IOException(ex);
 
 		} finally {
 			stream.close();
 			objectStream.close();
 		}
 	}
 
 	public static <T> T read(String data, Class<T> valueClass)
 			throws IOException {
 		T value;
 		try {
 			value = getMapper().readValue(data, valueClass);
 			return value;
 		} catch (JsonGenerationException ex) {
 			throw new IOException(ex);
 		} catch (JsonMappingException ex) {
 			throw new IOException(ex);
 		} catch (IOException ex) {
 			throw new IOException(ex);
 		}
 	}
 
 	public static Object callRemoteMethod(Object proxy, Method method,
 			Object[] arguments) throws IOException {
 
 		RpcWrapper wrapper = new RpcWrapper();
 		wrapper.setBeanName(proxy.getClass().getSimpleName());
 		wrapper.setMethodName(method.getName());
 		wrapper.setParamTypes(method.getParameterTypes());
 		wrapper.setInterfaceClass(proxy.getClass());
 		wrapper.setParams(arguments);
 
 		RPCall rpCall = new RPCall();
 
 		return rpCall.call(wrapper);
 	}
 
 	public static Object callBeanMethod(RpcWrapper wrapper) throws IOException {
 
 		String beanName = wrapper.getBeanName();
 		String methodName = wrapper.getMethodName();
 		Class<?>[] paramTypes = wrapper.getParamTypes();
 		Class<?> interfaceClass = wrapper.getInterfaceClass();
 		Object[] params = wrapper.getParams();
 
 		try {
 
 			Object bean = new EjbConnector().connectToBean(beanName,
 					interfaceClass);
 			Method beanMethod = bean.getClass().getDeclaredMethod(methodName,
 					paramTypes);
 			return beanMethod.invoke(bean, params);
 
 		} catch (IllegalArgumentException ex) {
 			throw new IOException(ex);
 		} catch (InvocationTargetException ex) {
 			throw new IOException(ex);
 		} catch (NoSuchMethodException ex) {
 			throw new IOException(ex);
 		} catch (SecurityException ex) {
 			throw new IOException(ex);
 		} catch (InstantiationException ex) {
 			throw new IOException(ex);
 		} catch (IllegalAccessException ex) {
 			throw new IOException(ex);
 		}
 	}
 }
