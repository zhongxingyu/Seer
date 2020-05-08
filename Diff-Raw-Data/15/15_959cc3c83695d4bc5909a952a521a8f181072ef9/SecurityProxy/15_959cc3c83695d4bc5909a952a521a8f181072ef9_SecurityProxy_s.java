 package security;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Arrays;
 
 public class SecurityProxy implements InvocationHandler {
 
 	private final Object obj;
 	private final Object owner;
 
 	public SecurityProxy(Object obj, Object owner) {
 		this.obj = obj;
 		this.owner = owner;
 	}
 
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] aobj)
 			throws Exception {
 		SecurityCallback callback = (SecurityCallback) aobj[aobj.length - 2];
 		Integer tan = (Integer) aobj[aobj.length - 1];
 		Object[] args = Arrays.copyOfRange(aobj, 0, aobj.length - 2);
		if (callback.equals(owner)) {
 			String methodName = method.getName();
 			Class[] parameterTypes = new Class[args.length];
 			int i = 0;
 			for (Object o : args) {
 				parameterTypes[i] = o.getClass();
 				i++;
 			}
 			Method realMethod = obj.getClass().getMethod(methodName,
 					parameterTypes);
 			Integer callbackTan = callback.getTan();
 			if (callbackTan == null || !callbackTan.equals(tan))
 				throw new SecurityObjectException();
 			Object ret = realMethod.invoke(obj, args);
 			return ret;
 
 		} else
 			throw new SecurityObjectException();
 	}
 
 	static public Object newInstance(Object obj, Object owner,
 			Class... interfaces) {
 		return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
 				interfaces, new SecurityProxy(obj, owner));
 	}
 
 }
