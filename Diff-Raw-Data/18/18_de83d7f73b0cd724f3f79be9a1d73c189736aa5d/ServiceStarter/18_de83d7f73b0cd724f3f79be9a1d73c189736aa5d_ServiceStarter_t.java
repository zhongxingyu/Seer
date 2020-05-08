 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sorcer.com.sun.jini.start;
 
 import com.sun.jini.start.NonActivatableServiceDescriptor;
 import com.sun.jini.start.ServiceDescriptor;
 import net.jini.config.Configuration;
 import net.jini.config.ConfigurationException;
 
 import java.net.URL;
 import java.rmi.RMISecurityManager;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.MissingResourceException;
 
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 import javax.security.auth.Subject;
 
 import net.jini.config.ConfigurationProvider;
 
 /**
  * @author Rafał Krupiński
  */
 public class ServiceStarter {
 
 	/**
 	 * Component name for service starter configuration entries
 	 */
 	static final String START_PACKAGE = "com.sun.jini.start";
 
 
 	/**
 	 * Configure logger
 	 */
 	static /*final*/ Logger logger = null;
 
 	static {
 		try {
 			logger =
 					Logger.getLogger(
 							START_PACKAGE + ".service.starter",
 							START_PACKAGE + ".resources.service");
 		} catch (Exception e) {
 			logger =
 					Logger.getLogger(START_PACKAGE + ".service.starter");
 			if (e instanceof MissingResourceException) {
 				logger.info("Could not load logger's ResourceBundle: "
 						+ e);
 			} else if (e instanceof IllegalArgumentException) {
 				logger.info("Logger exists and uses another resource bundle: "
 						+ e);
 			}
 			logger.info("Defaulting to existing logger");
 		}
 	}
 
 	/**
 	 * Array of strong references to transient services
 	 */
 	private ArrayList transient_service_refs;
 
 	/**
 	 * Utility routine that sets a security manager if one isn't already
 	 * present.
 	 */
 	synchronized static void ensureSecurityManager() {
 		if (System.getSecurityManager() == null) {
 			System.setSecurityManager(new RMISecurityManager());
 		}
 	}
 
 	/**
 	 * Generic service creation method that attempts to login via
 	 * the provided <code>LoginContext</code> and then call the
 	 * <code>create</code> overload without a login context argument.
 	 *
 	 * @param descs        The <code>ServiceDescriptor[]</code> that contains
 	 *                     the descriptors for the services to start.
 	 * @param config       The associated <code>Configuration</code> object
 	 *                     used to customize the service creation process.
 	 * @param loginContext The associated <code>LoginContext</code> object
 	 *                     used to login/logout.
 	 * @return Returns a <code>Result[]</code> that is the same length as
 	 *         <code>descs</code>, which contains the details for each
 	 *         service creation attempt.
 	 * @throws Exception If there was a problem logging in/out or
 	 *                   a problem creating the service.
 	 * @see Service
 	 * @see ServiceDescriptor
 	 * @see net.jini.config.Configuration
 	 * @see javax.security.auth.login.LoginContext
 	 */
 
 	private Service[] createWithLogin(
 			final ServiceDescriptor[] descs, final Configuration config,
 			final LoginContext loginContext)
 			throws Exception {
 		logger.entering(ServiceStarter.class.getName(),
 				"createWithLogin", new Object[]{descs, config, loginContext});
 		loginContext.login();
 		Service[] results = null;
 		try {
 			results = (Service[]) Subject.doAsPrivileged(
 					loginContext.getSubject(),
 					new PrivilegedExceptionAction() {
 						public Object run()
 								throws Exception {
 							return create(descs, config);
 						}
 					},
 					null);
 		} catch (PrivilegedActionException pae) {
 			throw pae.getException();
 		} finally {
 			try {
 				loginContext.logout();
 			} catch (LoginException le) {
 				logger.log(Level.FINE, "service.logout.exception", le);
 			}
 		}
 		logger.exiting(ServiceStarter.class.getName(),
 				"createWithLogin", results);
 		return results;
 	}
 
 	/**
 	 * Generic service creation method that attempts to start the
 	 * services defined by the provided <code>ServiceDescriptor[]</code>
 	 * argument.
 	 *
 	 * @param descs  The <code>ServiceDescriptor[]</code> that contains
 	 *               the descriptors for the services to start.
 	 * @param config The associated <code>Configuration</code> object
 	 *               used to customize the service creation process.
 	 * @return Returns a <code>Result[]</code> that is the same length as
 	 *         <code>descs</code>, which contains the details for each
 	 *         service creation attempt.
 	 * @throws Exception If there was a problem creating the service.
 	 * @see Service
 	 * @see ServiceDescriptor
 	 * @see net.jini.config.Configuration
 	 */
 	private Service[] create(final ServiceDescriptor[] descs,
 							 final Configuration config)
 			throws Exception {
 		logger.entering(ServiceStarter.class.getName(), "create",
 				new Object[]{descs, config});
 		ArrayList proxies = new ArrayList();
 
 		Object result = null;
 		Exception problem = null;
 		ServiceDescriptor desc = null;
 		for (int i = 0; i < descs.length; i++) {
 			desc = descs[i];
 			result = null;
 			problem = null;
 			try {
 				if (desc != null) {
 					result = desc.create(config);
 				}
 			} catch (Exception e) {
 				problem = e;
 			} finally {
 				proxies.add(new Service(desc, result, problem));
 			}
 		}
 
 		logger.exiting(ServiceStarter.class.getName(), "create", proxies);
 		return (Service[]) proxies.toArray(new Service[proxies.size()]);
 	}
 
 	/**
 	 * Utility routine that maintains strong references to any
 	 * transient services in the provided <code>Result[]</code>.
 	 * This prevents the transient services from getting garbage
 	 * collected.
 	 */
 	private void maintainNonActivatableReferences(Service[] results) {
 		logger.entering(ServiceStarter.class.getName(),
 				"maintainNonActivatableReferences", (Object[]) results);
 		if (results.length == 0)
 			return;
 		transient_service_refs = new ArrayList();
 		for (int i = 0; i < results.length; i++) {
 			if (results[i] != null &&
 					results[i].result != null &&
 					NonActivatableServiceDescriptor.class.equals(
 							results[i].descriptor.getClass())) {
 				logger.log(Level.FINEST, "Storing ref to: {0}",
 						results[i].result);
 				transient_service_refs.add(results[i].result);
 			}
 		}
 //TODO - kick off daemon thread to maintain refs via LifeCycle object	
 		logger.exiting(ServiceStarter.class.getName(),
 				"maintainNonActivatableReferences");
 		return;
 	}
 
 	/**
 	 * Utility routine that prints out warning messages for each service
 	 * descriptor that produced an exception or that was null.
 	 */
 	private static void checkResultFailures(Service[] results) {
 		logger.entering(ServiceStarter.class.getName(),
 				"checkResultFailures", (Object[]) results);
 		if (results.length == 0)
 			return;
 		for (int i = 0; i < results.length; i++) {
 			if (results[i].exception != null) {
 				logger.log(Level.WARNING,
 						"service.creation.unknown",
 						results[i].exception);
 				logger.log(Level.WARNING,
 						"service.creation.unknown.detail",
 						new Object[]{new Integer(i),
 								results[i].descriptor});
 			} else if (results[i].descriptor == null) {
 				logger.log(Level.WARNING,
 						"service.creation.null", new Integer(i));
 			}
 		}
 		logger.exiting(ServiceStarter.class.getName(),
 				"checkResultFailures");
 	}
 
 	/**
 	 * Workhorse function for both main() entrypoints.
 	 */
 	private Service[] processServiceDescriptors(Configuration... configs) throws Exception {
 		List<Service> resultList = new LinkedList<Service>();
        ServiceDescriptor[][] descriptors = new ServiceDescriptor[configs.length][];
 
        for (int i = 0; i < configs.length; i++) {
            Configuration config = configs[i];
 			ServiceDescriptor[] descs = (ServiceDescriptor[])
 					config.getEntry(START_PACKAGE, "serviceDescriptors",
 							ServiceDescriptor[].class, null);
 			if (descs == null || descs.length == 0) {
 				logger.warning("service.config.empty");
 				continue;
 			}
            descriptors[i] = descs;
         }
 
        for (int i = 0; i < configs.length; i++) {
            Configuration config = configs[i];
            ServiceDescriptor[] descs = descriptors[i];
            if(descs == null)
                continue;
 
 			LoginContext loginContext = (LoginContext)
 					config.getEntry(START_PACKAGE, "loginContext",
 							LoginContext.class, null);
 
 			Service[] results;
 			if (loginContext != null) {
 				results = createWithLogin(descs, config, loginContext);
 			} else {
 				results = create(descs, config);
 			}
 			resultList.addAll(Arrays.asList(results));
 		}
 
 		Service[] results = resultList.toArray(new Service[resultList.size()]);
 		checkResultFailures(results);
 		maintainNonActivatableReferences(results);
 		return results;
 	}
 
 	/**
 	 * The main method for embidding the <code>ServiceStarter</code> application.
 	 * The <code>config</code> argument is queried for the
 	 * <code>com.sun.jini.start.serviceDescriptors</code> entry, which
 	 * is assumed to be a <code>ServiceDescriptor[]</code>.
 	 * The <code>create()</code> method is then called on each of the array
 	 * elements.
 	 *
 	 * @param config the <code>Configuration</code> object.
 	 * @see ServiceDescriptor
 	 * @see com.sun.jini.start.SharedActivatableServiceDescriptor
 	 * @see com.sun.jini.start.SharedActivationGroupDescriptor
 	 * @see NonActivatableServiceDescriptor
 	 * @see net.jini.config.Configuration
 	 */
 	public void main(Configuration config) {
 		ensureSecurityManager();
 		try {
 			logger.entering(ServiceStarter.class.getName(),
 					"main", config);
 			processServiceDescriptors(config);
 		} catch (ConfigurationException cex) {
 			logger.log(Level.SEVERE, "service.config.exception", cex);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "service.creation.exception", e);
 		}
 		logger.exiting(ServiceStarter.class.getName(),
 				"main");
 	}
 
 	/**
 	 * Create a Configuration object from each path in args and create service for each entry.
 	 *
 	 * @param args config file paths
 	 * @throws ConfigurationException
 	 * @return list ofcreated services
 	 */
 	public Service[] startServicesFromPaths(String[] args) throws ConfigurationException {
 		logger.info("Loading from " + Arrays.deepToString(args));
 		Collection<Configuration> configs = new ArrayList<Configuration>(args.length);
 		for (String arg : args) {
 			if (arg == null) continue;
 			URL resource = getClass().getClassLoader().getResource(arg);
 			String options;
 			if (resource == null) {
 				options = arg;
 			} else {
 				options = resource.toExternalForm();
 			}
 			configs.add(ConfigurationProvider.getInstance(new String[]{options}));
 		}
 		try {
 			return processServiceDescriptors(configs.toArray(new Configuration[configs.size()]));
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Error while parsing configuration", e);
 		}
 	}
 
 	public Service[] startServices(String[] args) {
 		ServiceStarter.ensureSecurityManager();
 		try {
 			Configuration config = ConfigurationProvider.getInstance(args);
 			return processServiceDescriptors(config);
 		} catch (ConfigurationException cex) {
 			logger.log(Level.SEVERE, "service.config.exception", cex);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "service.creation.exception", e);
 		} finally {
 			logger.exiting(ServiceStarter.class.getName(),
 					"main");
 		}
 		return new Service[0];
 	}
 }//end class ServiceStarter
