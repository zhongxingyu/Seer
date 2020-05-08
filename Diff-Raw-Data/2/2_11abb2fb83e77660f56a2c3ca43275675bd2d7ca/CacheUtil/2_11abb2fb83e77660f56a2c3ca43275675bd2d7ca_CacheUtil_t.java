 package suite.util;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import suite.util.FunUtil.Fun;
 
 public class CacheUtil {
 
 	private Map<Key, Object> results = new ConcurrentHashMap<>();
 
 	private static class Key {
 		private Object bean;
 		private Method method;
 		private Object arguments[];
 
 		public Key(Object bean, Method method, Object arguments[]) {
 			this.bean = bean;
 			this.method = method;
 			this.arguments = arguments;
 		}
 
 		public int hashCode() {
 			int result = 1;
			result = 31 * result + Arrays.deepHashCode(arguments);
 			result = 31 * result + Util.hashCode(bean);
 			result = 31 * result + Util.hashCode(method);
 			return result;
 		}
 
 		public boolean equals(Object object) {
 			if (object instanceof Key) {
 				Key other = (Key) object;
 				return System.identityHashCode(bean) == System.identityHashCode(other.bean) //
 						&& Util.equals(method, other.method) //
 						&& Arrays.deepEquals(arguments, other.arguments);
 			} else
 				return false;
 		}
 	}
 
 	public <I, O> Fun<I, O> proxy(Fun<I, O> fun) {
 		@SuppressWarnings("unchecked")
 		Fun<I, O> proxy = (Fun<I, O>) proxy(Fun.class, fun);
 		return proxy;
 	}
 
 	public <I> I proxy(Class<I> interface_, I object) {
 		return proxy(interface_, object, new HashSet<Method>(Arrays.asList(interface_.getMethods())));
 	}
 
 	public <I> I proxyByMethodNames(Class<I> interface_, I object, Set<String> methodNames) {
 		Set<Method> methods = new HashSet<>();
 
 		for (Method method : interface_.getMethods())
 			if (methodNames.contains(method.getName()))
 				methods.add(method);
 
 		return proxy(interface_, object, methods);
 	}
 
 	public <I> I proxy(Class<I> interface_, final I object, final Collection<Method> methods) {
 		InvocationHandler handler = new InvocationHandler() {
 			public Object invoke(Object proxy, Method method, Object ps[]) throws Exception {
 				Key key = methods.contains(method) ? new Key(object, method, ps) : null;
 				boolean isCached = key != null && results.containsKey(key);
 				Object result;
 
 				if (!isCached)
 					try {
 						results.put(key, result = method.invoke(object, ps));
 					} catch (InvocationTargetException ite) {
 						Throwable th = ite.getTargetException();
 						throw th instanceof Exception ? (Exception) th : ite;
 					}
 				else
 					result = results.get(key);
 
 				return result;
 			}
 		};
 
 		@SuppressWarnings("unchecked")
 		final Class<I> clazz = (Class<I>) object.getClass();
 		ClassLoader classLoader = clazz.getClassLoader();
 		Class<?> classes[] = { interface_ };
 
 		@SuppressWarnings("unchecked")
 		I proxied = (I) Proxy.newProxyInstance(classLoader, classes, handler);
 		return proxied;
 	}
 
 }
