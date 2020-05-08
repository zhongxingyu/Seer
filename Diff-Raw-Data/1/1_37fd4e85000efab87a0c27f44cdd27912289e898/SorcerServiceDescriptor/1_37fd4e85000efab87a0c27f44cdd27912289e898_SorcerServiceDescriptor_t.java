 /*
  * Copyright 2008 the original author or authors.
  * Copyright 2005 Sun Microsystems, Inc.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.provider.boot;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.rmi.MarshalledObject;
 import java.rmi.RMISecurityManager;
 import java.security.AllPermission;
 import java.security.Permission;
 import java.security.Policy;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.jini.config.Configuration;
 import net.jini.export.ProxyAccessor;
 import net.jini.security.BasicProxyPreparer;
 import net.jini.security.ProxyPreparer;
 import net.jini.security.policy.DynamicPolicyProvider;
 import net.jini.security.policy.PolicyFileProvider;
 
 import com.sun.jini.config.Config;
 import com.sun.jini.start.AggregatePolicyProvider;
 import com.sun.jini.start.ClassLoaderUtil;
 import com.sun.jini.start.HTTPDStatus;
 import com.sun.jini.start.LifeCycle;
 import com.sun.jini.start.LoaderSplitPolicyProvider;
 import com.sun.jini.start.ServiceDescriptor;
 import com.sun.jini.start.ServiceProxyAccessor;
 import org.rioproject.config.PlatformCapabilityConfig;
 import org.rioproject.config.PlatformLoader;
 import sorcer.boot.load.Activator;
 import sorcer.boot.util.ClassPathVerifier;
 import sorcer.boot.util.JarClassPathHelper;
 import sorcer.core.SorcerEnv;
 
 /**
  * The SorcerServiceDescriptor class is a utility that conforms to the
  * Jini&trade; technology ServiceStarter framework, and will start a service
  * using the {@link sorcer.provider.boot.CommonClassLoader} as a shared,
  * non-activatable, in-process service. Clients construct this object with the
  * details of the service to be launched, then call <code>create</code> to
  * launch the service in invoking object's VM.
  * <P>
  * This class provides separation of the import codebase (where the server
  * classes are loaded from) from the export codebase (where clients should load
  * classes from for stubs, etc.) as well as providing an independent security
  * policy file for each service object. This functionality allows multiple
  * service objects to be placed in the same VM, with each object maintaining a
  * distinct codebase and policy.
  * <P>
  * Services need to implement the following "non-activatable constructor":
  * <blockquote>
  * 
  * <pre>
  * &lt;impl&gt;(String[] args, LifeCycle lc)
  * </pre>
  * 
  * </blockquote>
  * 
  * where,
  * <UL>
  * <LI>args - are the service configuration arguments
  * <LI>lc - is the hosting environment's {@link LifeCycle} reference.
  * </UL>
  * 
  * @author Dennis Reedy, updated for SORCER by M. Sobolewski
  */
 public class SorcerServiceDescriptor implements ServiceDescriptor {
 	static String COMPONENT = "sorcer.provider.boot";
 	static Logger logger = Logger.getLogger(COMPONENT);
 	/**
 	 * The parameter types for the "activation constructor".
 	 */
 	private static final Class[] actTypes = { String[].class, LifeCycle.class };
 	private String codebase;
 	private String policy;
 	private String classpath;
 	private String implClassName;
 	private String[] serverConfigArgs;
 	private LifeCycle lifeCycle;
 	private static LifeCycle NoOpLifeCycle = new LifeCycle() { // default, no-op
 		// object
 		public boolean unregister(Object impl) {
 			return false;
 		}
 	};
 	private static AggregatePolicyProvider globalPolicy = null;
 	private static Policy initialGlobalPolicy = null;
 
 	private static JarClassPathHelper classPathHelper = new JarClassPathHelper();
     private static Activator activator = new Activator();
     private static boolean platformLoaded = false;
 
     private static AtomicInteger allDescriptors = new AtomicInteger(0);
     private static AtomicInteger startedServices = new AtomicInteger(0);
     private static AtomicInteger erredServices = new AtomicInteger(0);
 
     {
         allDescriptors.incrementAndGet();
     }
 
     /**
 	 * Object returned by
 	 * {@link SorcerServiceDescriptor#create(net.jini.config.Configuration)
 	 * SorcerServiceDescriptor.create()} method that returns the proxy and
 	 * implementation references for the created service.
 	 */
 	public static class Created {
 		/** The reference to the proxy of the created service */
 		public final Object proxy;
 		/** The reference to the implementation of the created service */
 		public final Object impl;
 
 		/**
 		 * Constructs an instance of this class.
 		 * 
 		 * @param impl
 		 *            reference to the implementation of the created service
 		 * @param proxy
 		 *            reference to the proxy of the created service
 		 */
 		public Created(Object impl, Object proxy) {
 			this.proxy = proxy;
 			this.impl = impl;
 		}
 	}
 
 	/**
 	 * Create a SorcerServiceDescriptor, assigning given parameters to their
 	 * associated, internal fields.
 	 *
 	 * @param descCodebase
 	 *            location where clients can download required service-related
 	 *            classes (for example, stubs, proxies, etc.). Codebase
 	 *            components must be separated by spaces in which each component
 	 *            is in <code>URL</code> format.
 	 * @param policy
 	 *            server policy filename or URL
 	 * @param classpath
 	 *            location where server implementation classes can be found.
 	 *            Classpath components must be separated by path separators.
 	 * @param implClassName
 	 *            name of server implementation class
 	 * @param address
 	 *            code server address used for the codebase
 	 * @param lifeCycle
 	 *            <code>LifeCycle</code> reference for hosting environment
 	 * @param serverConfigArgs
 	 *            service configuration arguments
 	 */
 	public SorcerServiceDescriptor(String descCodebase, String policy,
 			String classpath, String implClassName, String address,
 			// Optional Args
 			LifeCycle lifeCycle, String... serverConfigArgs) {
 //		if (descCodebase == null || policy == null || classpath == null
 //				|| implClassName == null)
 //			throw new NullPointerException("Codebase, policy, classpath, and "
 //					+ "implementation cannot be null");
 		if (descCodebase != null && descCodebase.indexOf("http://") < 0) {
 			String[] jars = Booter.toArray(descCodebase);
 			try {
 				if (address == null)
 					this.codebase = Booter.getCodebase(jars, "" + Booter.getPort());
 				else
 					this.codebase = Booter.getCodebase(jars, address, "" + Booter.getPort());
 			} catch (UnknownHostException e) {
 				logger.severe("Cannot get hostanme for: " + codebase);
 			}
 		}
 		else
 			this.codebase = descCodebase;
 		this.policy = policy;
 		this.classpath = setClasspath(classpath);
 		this.implClassName = implClassName;
 		this.serverConfigArgs = serverConfigArgs;
 		this.lifeCycle = (lifeCycle == null) ? NoOpLifeCycle : lifeCycle;
 	}
 
 	public SorcerServiceDescriptor(String descCodebase, String policy,
 			String classpath, String implClassName,
 			// Optional Args
 			LifeCycle lifeCycle, String... serverConfigArgs) {
 		this(descCodebase, policy, classpath, implClassName, null, lifeCycle, serverConfigArgs);
 	}
 
 	/**
 	 * Create a SorcerServiceDescriptor. Equivalent to calling the other
 	 * overloaded constructor with <code>null</code> for the
 	 * <code>LifeCycle</code> reference.
 	 * 
 	 * @param codebase
 	 *            location where clients can download required service-related
 	 *            classes (for example, stubs, proxies, etc.). Codebase
 	 *            components must be separated by spaces in which each component
 	 *            is in <code>URL</code> format.
 	 * @param policy
 	 *            server policy filename or URL
 	 * @param classpath
 	 *            location where server implementation classes can be found.
 	 *            Classpath components must be separated by path separators.
 	 * @param implClassName
 	 *            name of server implementation class
 	 * @param serverConfigArgs
 	 *            service configuration arguments
 	 */
 	public SorcerServiceDescriptor(String codebase, String policy,
 			String classpath, String implClassName,
 			// Optional Args
 			String... serverConfigArgs) {
 		this(codebase, policy, classpath, implClassName, null, serverConfigArgs);
 	}
 
 	/**
 	 * Codebase accessor method.
 	 * 
 	 * @return The codebase string associated with this service descriptor.
 	 */
 	public String getCodebase() {
 		return codebase;
 	}
 
 	/**
 	 * Policy accessor method.
 	 *
 	 * @return The policy string associated with this service descriptor.
 	 */
 	public String getPolicy() {
 		return policy;
 	}
 
 	/**
 	 * <code>LifeCycle</code> accessor method.
 	 *
 	 * @return The <code>LifeCycle</code> object associated with this service
 	 *         descriptor.
 	 */
 	public LifeCycle getLifeCycle() {
 		return lifeCycle;
 	}
 
 	/**
 	 * LifCycle accessor method.
 	 *
 	 * @return The classpath string associated with this service descriptor.
 	 */
 	public String getClasspath() {
 		return classpath;
 	}
 
 	/**
 	 * Implementation class accessor method.
 	 *
 	 * @return The implementation class string associated with this service
 	 *         descriptor.
 	 */
 	public String getImplClassName() {
 		return implClassName;
 	}
 
 	/**
 	 * Service configuration arguments accessor method.
 	 *
 	 * @return The service configuration arguments associated with this service
 	 *         descriptor.
 	 */
 	public String[] getServerConfigArgs() {
 		return (serverConfigArgs != null) ? serverConfigArgs.clone() : null;
 	}
 
 	synchronized void ensureSecurityManager() {
 		if (System.getSecurityManager() == null) {
 			System.setSecurityManager(new RMISecurityManager());
 		}
 	}
 
 	/**
 	 * @see com.sun.jini.start.ServiceDescriptor#create
 	 */
 	public Object create(Configuration config) throws Exception {
         try {
             return docreate(config);
         } catch (Exception x) {
             erredServices.incrementAndGet();
            logger.log(Level.SEVERE, "Error creating service", x);
             throw x;
         } finally {
             int i = startedServices.incrementAndGet();
             logger.info("Started " + i + '/' + allDescriptors.get() + " services; " + erredServices.get() + " errors");
         }
     }
 
 	private Object docreate(Configuration config) throws Exception {
 		ensureSecurityManager();
 		Object proxy = null;
 
         {
         /* Warn user of inaccessible codebase(s) */
             String codebase = getCodebase();
             if (codebase != null && codebase.startsWith("http"))
                 HTTPDStatus.httpdWarning(codebase);
         }
 
 		/* Set common JARs to the CommonClassLoader */
 		String defaultDir = null;
 		String fs = File.separator;
 		String sorcerHome = SorcerEnv.getHomeDir().getPath();
 		if (sorcerHome == null) {
 			logger.info("'sorcer.home' not defined, no default platformDir");
 		} else {
             defaultDir = sorcerHome+fs+"configs"+fs+"platform"+fs+"sorcer";
             if (!new File(defaultDir).exists())
                 defaultDir = sorcerHome+fs+"lib"+fs+"rio"+fs+"config"+fs+"platform";
 		}
 
 		CommonClassLoader commonCL = CommonClassLoader.getInstance();
 		// Don't load Platform Class Loader when it was already loaded before
         if (!platformLoaded) {
             loadPlatform(config, defaultDir, commonCL);
             platformLoaded = true;
         }
 
         Thread currentThread = Thread.currentThread();
 		ClassLoader currentClassLoader = currentThread.getContextClassLoader();
 
 
 		ClassAnnotator annotator = null;
         if (getCodebase() != null) {
 
             annotator = new ClassAnnotator(ClassLoaderUtil
                     .getCodebaseURLs(getCodebase()), new Properties());
         }
 
 		ServiceClassLoader jsbCL = new ServiceClassLoader(ServiceClassLoader
 				.getURIs(ClassLoaderUtil.getClasspathURLs(getClasspath())),
 				annotator, commonCL);
 		if (logger.isLoggable(Level.FINE))
 			ClassLoaderUtil.displayClassLoaderTree(jsbCL);
 		new ClassPathVerifier().verifyClassPaths(jsbCL);
 
 		/*
 		 * ServiceClassLoader jsbCL = new
 		 * ServiceClassLoader(ClassLoaderUtil.getClasspathURLs(getClasspath()),
 		 * annotator, commonCL);
 		 */
 		currentThread.setContextClassLoader(jsbCL);
 		/* Get the ProxyPreparer */
 		ProxyPreparer servicePreparer = (ProxyPreparer) Config.getNonNullEntry(
 				config, COMPONENT, "servicePreparer", ProxyPreparer.class,
 				new BasicProxyPreparer());
 		synchronized (SorcerServiceDescriptor.class) {
 			/* supplant global policy 1st time through */
 			if (globalPolicy == null) {
 				initialGlobalPolicy = Policy.getPolicy();
 				globalPolicy = new AggregatePolicyProvider(initialGlobalPolicy);
 				Policy.setPolicy(globalPolicy);
 				if (logger.isLoggable(Level.FINEST))
 					logger.log(Level.FINEST, "Global policy set: {0}",
 							globalPolicy);
 			}
 			DynamicPolicyProvider service_policy = new DynamicPolicyProvider(
 					new PolicyFileProvider(getPolicy()));
 			LoaderSplitPolicyProvider splitServicePolicy = new LoaderSplitPolicyProvider(
 					jsbCL, service_policy, new DynamicPolicyProvider(
 							initialGlobalPolicy));
 			/*
 			 * Grant "this" code enough permission to do its work under the
 			 * service policy, which takes effect (below) after the context
 			 * loader is (re)set.
 			 */
 			splitServicePolicy.grant(SorcerServiceDescriptor.class, null,
 					new Permission[] { new AllPermission() });
 			globalPolicy.setPolicy(jsbCL, splitServicePolicy);
 		}
 		Object impl;
 		try {
 
 			Class implClass;
 			implClass = Class.forName(getImplClassName(), false, jsbCL);
 			if (logger.isLoggable(Level.FINEST))
 				logger.finest("Attempting to get implementation constructor");
 			Constructor constructor = implClass
 					.getDeclaredConstructor(actTypes);
 			if (logger.isLoggable(Level.FINEST))
 				logger
 						.log(Level.FINEST,
 								"Obtained implementation constructor: ",
 								constructor);
 			constructor.setAccessible(true);
 			impl = constructor.newInstance(getServerConfigArgs(), lifeCycle);
 
 			if (logger.isLoggable(Level.FINEST))
 				logger.log(Level.FINEST,
 						"Obtained implementation instance: {0}", impl);
 			if (impl instanceof ServiceProxyAccessor) {
 				proxy = ((ServiceProxyAccessor) impl).getServiceProxy();
 			} else if (impl instanceof ProxyAccessor) {
 				proxy = ((ProxyAccessor) impl).getProxy();
 			} else {
 				proxy = null; // just for insurance
 			}
 			if (proxy != null) {
 				proxy = servicePreparer.prepareProxy(proxy);
 			}
 			if (logger.isLoggable(Level.FINEST))
 				logger.log(Level.FINEST, "Proxy =  {0}", proxy);
 			// TODO - factor in code integrity for MO
             proxy = (new MarshalledObject(proxy)).get();
             currentThread.setContextClassLoader(currentClassLoader);
         } finally {
 			currentThread.setContextClassLoader(currentClassLoader);
 		}
 		return (new Created(impl, proxy));
 	}
 
     private void loadPlatform(Configuration config, String defaultDir, CommonClassLoader commonCL) throws Exception {
         PlatformLoader platformLoader = new PlatformLoader();
         List<URL> urlList = new LinkedList<URL>();
 
         String platformDir = (String) config.getEntry(COMPONENT, "platformDir",
                 String.class, defaultDir);
         logger.finer("Platform dir: " + platformDir);
         PlatformCapabilityConfig[] caps = platformLoader.parsePlatform(platformDir);
 
         logger.finer("Capabilities: " + Arrays.toString(caps));
         for (PlatformCapabilityConfig cap : caps) {
             if (cap.getCommon()) {
                 URL[] urls = cap.getClasspathURLs();
                 urlList.addAll(Arrays.asList(urls));
             }
         }
 
         URL[] commonJARs = urlList.toArray(new URL[urlList.size()]);
 
 			/*
 			 * if(commonJARs.length==0) throw new
 			 * RuntimeException("No commonJARs have been defined");
 			 */
         if (logger.isLoggable(Level.FINEST)) {
             StringBuffer buffer = new StringBuffer();
             for (int i = 0; i < commonJARs.length; i++) {
                 if (i > 0)
                     buffer.append("\n");
                 buffer.append(commonJARs[i].toExternalForm());
             }
             logger.finest("commonJARs=\n" + buffer.toString());
         }
 
         commonCL.addCommonJARs(commonJARs);
 
         activate(commonCL, commonJARs);
     }
 
     private void activate(CommonClassLoader commonCL, URL[] commonJARs) throws Exception {
         Thread thread = Thread.currentThread();
         ClassLoader contextClassLoader = thread.getContextClassLoader();
         try{
             thread.setContextClassLoader(commonCL);
             activator.activate(commonJARs);
         }finally {
             thread.setContextClassLoader(contextClassLoader);
         }
 	}
 
 	/*
 	 * Iterate through the classpath, for each jar see if there is a ClassPath
 	 * manifest setting. If there is, append the settings to the classpath
 	 */
 	private String setClasspath(String cp) {
 		String[] inClassPathArr = cp.split(File.pathSeparator);
 		Set<String> paths = new HashSet<String>();
 		for (String s : inClassPathArr) {
 			paths.add(s);
 			paths.addAll(classPathHelper.getClassPathFromJar(new File(s)));
 		}
 		return Booter.getClasspath(paths.toArray(new String[paths.size()]));
 	}
 
     public String toString() {
 		return "SorcerServiceDescriptor{"
 				+ "codebase='"
 				+ codebase
 				+ '\''
 				+ ", policy='"
 				+ policy
 				+ '\''
 				+ ", classpath='"
 				+ classpath
 				+ '\''
 				+ ", implClassName='"
 				+ implClassName
 				+ '\''
 				+ ", serverConfigArgs="
 				+ (serverConfigArgs == null ? null : Arrays
 						.asList(serverConfigArgs)) + ", lifeCycle=" + lifeCycle
 				+ '}';
 	}
 }
