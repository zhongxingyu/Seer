 package org.jackie.context;
 
 import org.jackie.utils.Assert;
 import org.jackie.utils.Log;
 import static org.jackie.context.ContextManager.context;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.NoSuchElementException;
 import java.io.IOException;
 import java.net.URL;
 
 /**
  * @author Patrik Beno
  */
 public class ServiceManager implements ContextObject {
 
 	static private final String RESOURCE = "META-INF/org.jackie/services.properties";
 
 //	static private final ServiceManager INSTANCE = new ServiceManager();
 
 	static public ServiceManager serviceManager() {
 		ServiceManager instance = context(ServiceManager.class);
 		if (instance == null) {
			context().set(ServiceManager.class, instance=new ServiceManager());
 		}
 		return instance;
 	}
 
 	static public <T extends Service> T service(Class<T> type) {
 		return serviceManager().getService(type);
 	}
 
 	protected Map<Class,Class> classByInterface;
 	protected Map<Class,Service> instanceByInterface;
 
 	{
 		classByInterface = new HashMap<Class, Class>();
 		instanceByInterface = new HashMap<Class, Service>();
 		loadResources();
 	}
 
 	public <T extends Service> T getService(Class<T> type) {
 		Service instance = instanceByInterface.get(type);
 		if (instance != null) {
 			return type.cast(instance);
 		}
 
 		Class cls = classByInterface.get(type);
 		if (cls == null) {
 			throw new NoSuchElementException(type.getName());
 		}
 
 		try {
 			Log.debug("Creating service %s (implementation: %s)", type.getName(), cls.getName());
 			instance = (Service) cls.newInstance();
 			instanceByInterface.put(type, instance);
 
 			return type.cast(instance);
 
 		} catch (InstantiationException e) {
 			throw Assert.notYetHandled(e);
 		} catch (IllegalAccessException e) {
 			throw Assert.notYetHandled(e);
 		}
 
 	}
 
 	void loadResources() {
 		try {
 			ClassLoader cl = Thread.currentThread().getContextClassLoader();
 			Enumeration<URL> e = cl.getResources(RESOURCE);
 			while (e.hasMoreElements()) {
 				URL url = e.nextElement();
 				Log.debug("Loading service mapping from %s", url);
 				Properties props = new Properties();
 				props.load(url.openStream());
 				load(props);
 			}
 
 		} catch (IOException e) {
 			throw Assert.notYetHandled(e);
 		}
 	}
 
 	void load(Properties props) {
 		Enumeration<?> names = props.propertyNames();
 		while (names.hasMoreElements()) {
 			String name = (String) names.nextElement();
 			String implname = props.getProperty(name);
 			try {
 				Class iface = load(name);
 				Class impl = load(implname);
 				classByInterface.put(iface, impl);
 			} catch (Exception e) {
 				Assert.logNotYetImplemented();
 			}
 		}
 	}
 
 	Class load(String clsname) {
 		try {
 			ClassLoader cl = Thread.currentThread().getContextClassLoader();
 			Class<?> cls = Class.forName(clsname, false, cl);
 			return cls;
 		} catch (ClassNotFoundException e) {
 			throw Assert.notYetHandled(e);
 		}
 	}
 
 }
