 package org.eclipse.b3.build.internal;
 
 import org.eclipse.b3.backend.core.B3InternalError;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.b3.build.BuildUnit;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.equinox.p2.metadata.Version;
 
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Type;

 public class BuildUnitUtils {
 	// private static class BuildUnitProxyFactory implements Opcodes {
 	// @SuppressWarnings("unchecked")
 	// public static Class<? extends BuildUnit> createBuildUnitInterface(BuildUnit unit, B3DynamicClassLoader
 	// classLoader) {
 	// ClassWriter cw = new ClassWriter(0);
 	// cw.visit(V1_5,
 	// ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE, // access
 	// dottedToInternal(unit.getName()), // interface name in internal form
 	// null, // generics
 	// "java/lang/Object", // superclass
 	// new String[] { // extended interfaces
 	// "org/eclipse/b3/build/build/BuildUnit"
 	// });
 	// cw.visitEnd();
 	// byte bytes[] = cw.toByteArray();
 	// Class<? extends BuildUnit> clazz = (Class<? extends BuildUnit>)((classLoader.defineClass(unit.getName(),
 	// bytes)));
 	// return clazz;
 	// }
 	// public static String dottedToInternal(String str) {
 	// return str.replaceAll("\\.", "/");
 	// }
 	// }
 	// private static class DynamicClassLoader extends ClassLoader {
 	// public DynamicClassLoader() {
 	// super(BuildUnitUtils.class.getClassLoader());
 	// }
 	// public Class<?> defineClass(String name, byte[] b) {
 	// return defineClass(name, b, 0, b.length);
 	// }
 	//
 	// }
 	public static class BuildUnitProxy implements InvocationHandler {
 		public static Object newInstance(BuildUnit unit) {
 			if(unit instanceof Proxy)
 				throw new IllegalArgumentException(
 					"Can not create a BuildUnit Proxy for instance already being a Proxy instance!");
 			Class<?> interfaces[] = unit.getClass().getInterfaces();
 			EList<Type> implementsList = unit.getImplements();
 			Class<?> extended[] = new Class<?>[interfaces.length + 1 + implementsList.size()];
 			// add all interfaces implemented by BuildUnit
 			for(int i = 0; i < interfaces.length; i++)
 				extended[i] = interfaces[i];
 			// add the special interface for the BuildUnit instance
 			extended[interfaces.length] = getBuildUnitInterface(unit);
 			// add all interfaces declared to be implemented by the Build Unit
 			int limit = implementsList.size();
 			for(int i = 0; i < limit; i++)
 				extended[i + interfaces.length + 1] = TypeUtils.getRaw(implementsList.get(i)); // TODO: TYPESYSTEM
 																								// improvement, use Raw
 																								// for now
 			return Proxy.newProxyInstance(dynamicClassLoader, extended, new BuildUnitProxy(unit));
 		}
 
 		private BuildUnit unit;
 
 		private BuildUnitProxy(BuildUnit unit) {
 			if(unit == null)
 				throw new IllegalArgumentException("Can not create a BuildUnitProxy with a null BuildUnit.");
 			this.unit = unit;
 		}
 
 		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 			// simple implementation - all interfaces except BuildUnit are marker interfaces
 			// so all calls can go straight through
 			Object result = null;
 			try {
 				result = method.invoke(unit, args);
 				return result;
 			}
 			catch(IllegalArgumentException e) {
 				throw e; // to be able to debug
 			}
 		}
 
 	}
 
 	public static ClassLoader dynamicClassLoader = new BuildUtilsDynamicClassLoader(
 		BuildUnitUtils.class.getClassLoader());
 
 	public final static String BUILDUNIT_INTERFACE_PREFIX = "b3_BuildUnit_";
 
 	/**
 	 * Creates a proxy instance for a BuildUnit. The created proxy implements the special build unit
 	 * instance interface, as well as all interfaces listed in the build unit.
 	 * 
 	 * @param unit
 	 * @param iface
 	 * @return
 	 */
 	public static synchronized BuildUnit createBuildUnitProxy(BuildUnit unit) {
 		return (BuildUnit) BuildUnitProxy.newInstance(unit);
 
 	}
 
 	/**
 	 * Dynamically get/load/create a marker interface for a BuildUnit that is named after the build unit.
 	 * 
 	 * @param unit
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static Class<? extends BuildUnit> getBuildUnitInterface(BuildUnit unit) {
 		try {
 			return (Class<? extends BuildUnit>) dynamicClassLoader.loadClass(getBuildUnitInterfaceName(unit));
 		}
 		catch(ClassNotFoundException e) {
 			throw new B3InternalError("B3 Interal error - Could not create a BuildUnit interface", e);
 		}
 	}
 
 	/**
 	 * Returns a string that can be used to load an interface for a particular build unit.
 	 * 
 	 * @param unit
 	 * @return
 	 */
 	public static String getBuildUnitInterfaceName(BuildUnit unit) {
 		return getBuildUnitInterfaceName(unit.getName(), unit.getVersion());
 	}
 
 	/**
 	 * Constructs an interface name for a unit based on its name and version.
 	 * 
 	 * @param unitName
 	 * @param version
 	 *            (may be null if the unit does not have a version).
 	 * @return
 	 */
 	public static String getBuildUnitInterfaceName(String unitName, Version version) {
 		StringBuffer buf = new StringBuffer();
 		buf.append(BUILDUNIT_INTERFACE_PREFIX);
 		buf.append(unitName);
 		if(version != null) {
 			buf.append("_");
			version.toString(buf);
 		}
 		return buf.toString();
 
 	}
 }
