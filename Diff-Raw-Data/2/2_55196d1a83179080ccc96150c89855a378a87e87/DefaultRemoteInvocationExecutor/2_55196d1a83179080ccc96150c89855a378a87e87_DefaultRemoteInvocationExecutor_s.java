 package com.playtika.springframework.remoting.protobuf.support;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.apache.commons.lang.ClassUtils;
 import org.springframework.util.Assert;
 
 import com.google.protobuf.Message;
 import com.playtika.remoting.protobuf.MessageUnwrapper;
 import com.playtika.remoting.protobuf.MessageWithClass;
 import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;
 import com.playtika.springframework.remoting.protobuf.ProtobufMessageUnwrapper;
 import com.playtika.springframework.remoting.protobuf.ProtobufRemoteInvocationException;
 
 /**
  * 
  * @author Alex Borisov
  * 
  */
 public class DefaultRemoteInvocationExecutor implements
 		RemoteInvocationExecutor {
 
 	private MessageUnwrapper messageUnwrapper = new ProtobufMessageUnwrapper();
 
 	@Override
 	public Message invoke(RemoteInvocation invocation, Object targetObject)
 			throws NoSuchMethodException, IllegalAccessException,
 			InvocationTargetException, ProtobufRemoteInvocationException {
 
 		Assert.notNull(invocation, "RemoteInvocation must not be null");
 		Assert.notNull(targetObject, "Target object must not be null");
 
 		String methodName = invocation.getMethod();
 		int argsCount = invocation.getArgumentCount();
 		Class<?>[] types = new Class[argsCount];
 		Object[] arguments = new Message[argsCount];
 		
 		unwrapArguments(invocation, argsCount, types, arguments);
 		Object result = invoke(targetObject, methodName, types, arguments);		
 		return castResult(result);
 	}
 
 	private Object invoke(Object targetObject, String methodName,
 			Class<?>[] types, Object[] arguments) throws NoSuchMethodException,
 			IllegalAccessException, InvocationTargetException {
 		Method method = targetObject.getClass().getMethod(methodName, types);
 		Object result = method.invoke(targetObject, arguments);
 		return result;
 	}
 
 	private void unwrapArguments(RemoteInvocation invocation, int argsCount,
 			Class<?>[] types, Object[] arguments) {
 		for (int index = 0; index < argsCount; index++) {
 
 			MessageWithClass msgWithClass = messageUnwrapper
 					.fromWrapper(invocation.getArgument(index), false);
			types[index] = msgWithClass.getClass();
 			arguments[index] = msgWithClass.getMessage();
 		}
 	}
 
 	private Message castResult(Object result) throws IllegalAccessException,
 			InvocationTargetException {
 		
 		if (result != null) {
 			if (ClassUtils.isAssignable(result.getClass(), Message.class)) {
 				return (Message) result;
 			} else {
 				throw new ProtobufRemoteInvocationException("Result of remote invocation should be a subclass of Message.class. " + result.getClass());
 			}
 		} else {
 			return null;
 		}
 	}
 }
