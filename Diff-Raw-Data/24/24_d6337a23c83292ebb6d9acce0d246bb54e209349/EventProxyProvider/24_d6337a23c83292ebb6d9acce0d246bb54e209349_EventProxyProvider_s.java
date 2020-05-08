 package org.jackie.event.impl;
 
 import org.jackie.event.Event;
 import org.jackie.event.impl.proxygen.ClassProxyBuilder;
 import static org.jackie.utils.Assert.*;
 import org.jackie.utils.ReflectHelper;
 
 import java.util.Map;
 import java.util.WeakHashMap;
 
 /**
  * @author Patrik Beno
  */
 public class EventProxyProvider {
 
 	class ProxyClassLoader extends ClassLoader {
 		ProxyClassLoader(ClassLoader parent) {
 			super(parent);
 		}
 		Class<Event> getProxy(String classname, byte[] bytecode) {
 			Class<?> cls = defineClass(classname, bytecode, 0, bytecode.length);
 			doAssert(Event.class.isAssignableFrom(cls),
 						"Invalid class: %s. Not compatible with %s", cls, Event.class);
 			//noinspection unchecked
 			return (Class<Event>) cls;
 		}
 	}
 
 	Map<Class, Event> proxies;
 	Map<ClassLoader, ProxyClassLoader> classloaders;
 
 	{
 		proxies = new WeakHashMap<Class, Event>();
 		classloaders = new WeakHashMap<ClassLoader, ProxyClassLoader>();
 	}
 
 	public <T extends Event> T getProxy(Class<T> type) {
 		Event event = proxies.get(type);
 		if (event != null) { return typecast(event, type); }
 
		if (type.isInterface()) {
 			event = new EventDispatcherProxy(type).getProxy();
 		} else {
 			ClassProxyBuilder builder = new ClassProxyBuilder(type);
 			ProxyClassLoader cl = getProxyClassLoader(type);
 			Class<Event> cls = cl.getProxy(builder.classname(), builder.build());
 			event = ReflectHelper.reflectHelper(cls).getConstructor().newInstance();
 		}
 
 		proxies.put(type, event);
 
 		return typecast(event, type);
 	}
 
 	private <T extends Event> ProxyClassLoader getProxyClassLoader(
 			Class<T> type) {
 		ProxyClassLoader cl = classloaders.get(type.getClassLoader());
 		if (cl == null) {
 			cl = new ProxyClassLoader(type.getClassLoader());
 			classloaders.put(cl.getParent(), cl);
 		}
 		return cl;
 	}
 
 
 }
