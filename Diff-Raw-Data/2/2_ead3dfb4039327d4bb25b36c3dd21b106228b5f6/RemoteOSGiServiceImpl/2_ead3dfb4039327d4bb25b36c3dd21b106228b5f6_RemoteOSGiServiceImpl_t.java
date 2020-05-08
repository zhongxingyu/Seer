 /* Copyright (c) 2006-2007 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Department of Computer Science, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package ch.ethz.iks.r_osgi.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.EventAdmin;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.osgi.service.log.LogService;
 import ch.ethz.iks.r_osgi.DiscoveryListener;
 import ch.ethz.iks.r_osgi.ChannelEndpoint;
 import ch.ethz.iks.r_osgi.RemoteOSGiException;
 import ch.ethz.iks.r_osgi.RemoteOSGiService;
 import ch.ethz.iks.r_osgi.SurrogateRegistration;
 import ch.ethz.iks.r_osgi.Remoting;
 import ch.ethz.iks.r_osgi.Timestamp;
 import ch.ethz.iks.r_osgi.NetworkChannelFactory;
 import ch.ethz.iks.slp.Advertiser;
 import ch.ethz.iks.slp.Locator;
 import ch.ethz.iks.slp.ServiceLocationEnumeration;
 import ch.ethz.iks.slp.ServiceLocationException;
 import ch.ethz.iks.slp.ServiceType;
 import ch.ethz.iks.slp.ServiceURL;
 import ch.ethz.iks.util.ScheduleListener;
 import ch.ethz.iks.util.Scheduler;
 
 /**
  * <p>
  * The R-OSGi core class. Handles remote channels and subscriptions from the
  * local framework. Local services can be released for remoting and then
  * discovered by remote peers.
  * </p>
  * 
  * @author Jan S. Rellermeyer, ETH Zurich
  * @since 0.1
  */
 final class RemoteOSGiServiceImpl implements RemoteOSGiService, Remoting,
 		ScheduleListener {
 
 	/**
 	 * the R-OSGi standard port.
 	 */
 	static int R_OSGI_PORT = 9278;
 
 	/**
 	 * the SLP abstract service type we are interested in.
 	 */
 	static final ServiceType OSGI = new ServiceType("service:osgi");
 
 	/**
 	 * the R-OSGi port property.
 	 */
 	static final String REMOTE_OSGi_PORT = "ch.ethz.iks.r_osgi.port";
 
 	/**
 	 * constant that holds the property string for proxy debug option.
 	 */
 	static final String PROXY_DEBUG_PROPERTY = "ch.ethz.iks.r_osgi.debug.proxyGeneration";
 
 	/**
 	 * constant that holds the property string for message debug option.
 	 */
 	static final String MSG_DEBUG_PROPERTY = "ch.ethz.iks.r_osgi.debug.messages";
 
 	/**
 	 * constant that holds the property string for internal debug option.
 	 */
 	static final String DEBUG_PROPERTY = "ch.ethz.iks.r_osgi.debug.internal";
 
 	/**
 	 * constant that holds the property string for SLP discovery interval time
 	 * in seconds. Default is 20 seconds.
 	 */
 	static final String DISCOVERY_INTERVAL_PROPERTY = "ch.ethz.iks.r_osgi.remote.discoveryInterval";
 
 	/**
 	 * constant that holds the property string for SLP registration lifetime.
 	 * Default is 60 seconds.
 	 */
 	static final String DEFAULT_SLP_LIFETIME_PROPERTY = "ch.ethz.iks.r_osgi.remote.defaultLifetime";
 
 	/**
 	 * the event property contains the sender's url.
 	 */
 	static final String EVENT_SENDER_URL = "sender.url";
 
 	/**
 	 * marker for channel-registered event handlers so that they don't
 	 * contribute to the peer's topic space.
 	 */
 	static final String R_OSGi_INTERNAL = "internal";
 
 	/**
 	 * log proxy generation debug output.
 	 */
 	static boolean PROXY_DEBUG;
 
 	/**
 	 * log message traffic.
 	 */
 	static boolean MSG_DEBUG;
 
 	/**
 	 * log method invocation debug output.
 	 */
 	static boolean DEBUG;
 
 	/**
 	 * discovery interval for SLP.
 	 */
 	private final int DISCOVERY_INTERVAL;
 
 	/**
 	 * default lifetime for SLP registration.
 	 */
 	static int DEFAULT_SLP_LIFETIME;
 
 	/**
 	 * the address of this peer, according to what jSLP reports.
 	 */
 	static String MY_ADDRESS;
 
 	/**
 	 * jSLP advertiser instance.
 	 */
 	private Advertiser advertiser;
 
 	/**
 	 * jSLP locator instance.
 	 */
 	private Locator locator;
 
 	/**
 	 * service reference -> remote service registration
 	 */
 	private static Map serviceRegistrations = new HashMap(1);
 
 	/**
 	 * thread loop variable.
 	 */
 	private static boolean running = true;
 
 	/**
 	 * registered listeners.
 	 */
 	private static Boolean hasListeners = Boolean.FALSE;
 
 	/**
 	 * next transaction id.
 	 */
 	private static short nextXid;
 
 	/**
 	 * all services that have been discovered and are not dead.
 	 */
 	private static Set knownServices = new HashSet(2);
 
 	/**
 	 * the warning list. This list contains all services that have not been
 	 * rediscovered once.
 	 */
 	private List warningList = new ArrayList(2);
 
 	/**
 	 * OSGi log service instance.
 	 */
 	static LogService log;
 
 	/**
 	 * the event admin.
 	 */
 	static EventAdmin eventAdmin;
 
 	/**
 	 * the bundle context.
 	 */
 	static BundleContext context;
 
 	/**
 	 * SLP reregistration scheduler.
 	 */
 	private final Scheduler reregistration = new Scheduler(this);
 
 	/**
 	 * the storage location.
 	 */
 	private final String storage;
 
 	/**
 	 * protocol --> factory.
 	 */
 	private static Map factories = new HashMap(0);
 
 	/**
 	 * Channel ID --> ChannelEndpoint.
 	 */
 	private static Map channels = new HashMap(0);
 
 	/**
 	 * the local topic space.
 	 */
 	private static List topics = new ArrayList(0);
 
 	/**
 	 * creates a new RemoteOSGiServiceImpl instance.
 	 * 
 	 * @throws IOException
 	 *             in case of IO problems.
 	 */
 	RemoteOSGiServiceImpl() throws IOException {
 		// set the debug switches
 		String prop = context.getProperty(PROXY_DEBUG_PROPERTY);
 		PROXY_DEBUG = prop != null ? Boolean.valueOf(prop).booleanValue()
 				: false;
 		prop = context.getProperty(MSG_DEBUG_PROPERTY);
 		MSG_DEBUG = prop != null ? Boolean.valueOf(prop).booleanValue() : false;
 		prop = context.getProperty(DEBUG_PROPERTY);
 		DEBUG = prop != null ? Boolean.valueOf(prop).booleanValue() : false;
 
 		if (log != null) {
 			if (PROXY_DEBUG) {
 				log.log(LogService.LOG_INFO, "PROXY DEBUG OUTPUTS ENABLED");
 			}
 			if (MSG_DEBUG) {
 				log.log(LogService.LOG_INFO, "MESSAGE DEBUG OUTPUTS ENABLED");
 			}
 			if (DEBUG) {
 				log.log(LogService.LOG_INFO, "INTERNAL DEBUG OUTPUTS ENABLED");
 			}
 		} else {
 			if (PROXY_DEBUG || MSG_DEBUG || DEBUG) {
 				System.err
 						.println("NO LOG SERVICE PRESENT, DEBUG PROPERTIES HAVE NO EFFECT ...");
 				PROXY_DEBUG = false;
 				MSG_DEBUG = false;
 				DEBUG = false;
 			}
 		}
 
 		// set port
 		prop = context.getProperty(REMOTE_OSGi_PORT);
 		R_OSGI_PORT = prop != null ? Integer.parseInt(prop) : 9278;
 
 		// set the discovery interval, default is 20 seconds
 		prop = context.getProperty(DISCOVERY_INTERVAL_PROPERTY);
 		DISCOVERY_INTERVAL = prop != null ? Integer.parseInt(prop) * 1000
 				: 30000;
 
 		prop = context.getProperty(DEFAULT_SLP_LIFETIME_PROPERTY);
 		DEFAULT_SLP_LIFETIME = prop != null ? Integer.parseInt(prop) : 90;
 
 		// initialize the transactionID with a random value
 		nextXid = (short) Math.round(Math.random() * Short.MAX_VALUE);
 
 		// get a jSLP advertiser instance
 		final ServiceReference advRef = context
 				.getServiceReference("ch.ethz.iks.slp.Advertiser");
 		if (advRef != null) {
 			advertiser = (Advertiser) context.getService(advRef);
 		} else {
 			throw new IllegalStateException(
 					"No SLP bundle present, cannot start R-OSGi ...");
 		}
 
 		// get a jSLP locator instance
 		final ServiceReference locRef = context
 				.getServiceReference("ch.ethz.iks.slp.Locator");
 		if (locRef != null) {
 			locator = (Locator) context.getService(locRef);
 		} else {
 			throw new IllegalStateException(
 					"No SLP bundle present, cannot start R-OSGi ...");
 		}
 
 		MY_ADDRESS = advertiser.getMyIP().getHostAddress();
 
 		// start the TCP thread
 		new TCPThread().start();
 
 		// start the discovery thread
 		if (DISCOVERY_INTERVAL > 0) {
 			new DiscoveryThread().start();
 		}
 
 		// get private storage
 		final File dir = context.getDataFile("storage");
 		dir.mkdirs();
 		storage = dir.getAbsolutePath();
 
 		// register service listeners
 		try {
 			context.addServiceListener(new ServiceRegistrationListener(), "("
 					+ R_OSGi_REGISTRATION + "=*" + ")");
 
 			// register service listener for EventAdmin service
 			context.addServiceListener(new EventAdminServiceListener(), "("
 					+ Constants.OBJECTCLASS + "=" + EventAdmin.class.getName()
 					+ ")");
 
 			// register service listener for discovery listeners
 			context.addServiceListener(new DiscoveryListenerListener(), "("
 					+ Constants.OBJECTCLASS + "="
 					+ DiscoveryListener.class.getName() + ")");
 
 			// register service listener for NetworkChannelFactory services
 			context.addServiceListener(new NetworkChannelFactoryListener(), "("
 					+ Constants.OBJECTCLASS + "="
 					+ NetworkChannelFactory.class.getName() + ")");
 
 			// register service listener for EventHandlers
 			context.addServiceListener(new EventHandlerListener(), "("
 					+ Constants.OBJECTCLASS + "="
 					+ EventHandler.class.getName() + ")");
 		} catch (InvalidSyntaxException ise) {
 			ise.printStackTrace();
 		}
 
 		// try to get event admin
 		final ServiceReference ref = context
 				.getServiceReference(EventAdmin.class.getName());
 		if (ref != null) {
 			eventAdmin = (EventAdmin) context.getService(ref);
 		} else {
 			System.err
 					.println("NO EVENT ADMIN FOUND. REMOTE EVENT DELIVERY TEMPORARILY DISABLED.");
 		}
 
 		try {
 			// bootstrapping existing services
 			final ServiceReference[] references = context.getServiceReferences(
 					null, "(" + R_OSGi_REGISTRATION + "=*)");
 			if (references != null) {
 				for (int i = 0; i < references.length; i++) {
 					try {
 						registerService(references[i]);
 					} catch (RemoteOSGiException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 			// bootstrapping existing event handlers
 			final ServiceReference[] references2 = context
 					.getServiceReferences(EventHandler.class.getName(), null);
 			if (references2 != null) {
 				for (int i = 0; i < references2.length; i++) {
 					final String[] theTopics = (String[]) references2[i]
 							.getProperty(EventConstants.EVENT_TOPIC);
 					for (int j = 0; j < theTopics.length; j++) {
 						topics.add(theTopics[j]);
 					}
 				}
 			}
 
 			if (DEBUG) {
 				log.log(LogService.LOG_DEBUG, "Local topic space " + topics);
 			}
 		} catch (InvalidSyntaxException doesNotHappen) {
 			doesNotHappen.printStackTrace();
 		}
 	}
 
 	/*
 	 * ------ public methods ------
 	 */
 
 	/**
 	 * get my own IP address. It is advised to use this method whenever refering
 	 * to the peer's own IP address to guarantee consistency with the SLP layer.
 	 * 
 	 * @return the IP address that is configured on SLP layer as own address.
 	 * @see ch.ethz.iks.r_osgi.RemoteOSGiService#getMyIP()
 	 * @since 0.1
 	 * @category RemoteOSGiService
 	 */
 	public InetAddress getMyIP() {
 		return advertiser.getMyIP();
 	}
 
 	/**
 	 * get the service that has been fetched under a certain
 	 * <code>ServiceURL</code>.
 	 * 
 	 * @param url
 	 *            the <code>ServiceURL</code>.
 	 * @return the service object or <code>null</code> if the service is not
 	 *         (yet) present.
 	 * @see ch.ethz.iks.r_osgi.RemoteOSGiService#getFetchedService(ch.ethz.iks.slp.ServiceURL)
 	 * @category RemoteOSGiService
 	 * @since 0.6
 	 */
 	public Object getFetchedService(final ServiceURL url) {
 		final ServiceReference ref = getFetchedServiceReference(url);
 		return ref == null ? null : context.getService(ref);
 	}
 
 	/**
 	 * get the service reference for the service that has been fetched under a
 	 * certain <code>ServiceURL</code>.
 	 * 
 	 * @param url
 	 *            the <code>ServiceURL</code>.
 	 * @return the service reference of the service (or service proxy) or
 	 *         <code>null</code> if the service is not (yet) present.
 	 * @see ch.ethz.iks.r_osgi.RemoteOSGiService#getFetchedServiceReference(ch.ethz.iks.slp.ServiceURL)
 	 * @category RemoteOSGiService
 	 * @since 0.6
 	 */
 	public ServiceReference getFetchedServiceReference(final ServiceURL url) {
 		try {
 			final ServiceReference[] refs = context.getServiceReferences(url
 					.getServiceType().getConcreteTypeName().replace('/', '.'),
 					"(" + REMOTE_HOST + "=" + url.getHost() + ")");
 			if (refs != null) {
 				return refs[0];
 			} else {
 				// might be a bundle_transfer service
 				// TODO: should keep track of serviceURL -> bundle and return
 				// the correct service, if present ...
 				final ServiceReference[] refs2 = context.getServiceReferences(
 						url.getServiceType().getConcreteTypeName().replace('/',
 								'.'), null);
 				if (refs2 != null) {
 					return refs2[0];
 				}
 			}
 		} catch (InvalidSyntaxException doesNotHappen) {
 			doesNotHappen.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * register a (legacy) service for remote access.
 	 * 
 	 * @param ref
 	 *            the service reference
 	 * @param policy
 	 *            the name of a policy.
 	 * @param smartProxy
 	 *            optionally the name of a smart proxy class.
 	 * @param injections
 	 *            optionally an array of names of injection classes.
 	 * @return the array of ServiceURLs under which the service has been
 	 *         registered.
 	 * @throws RemoteOSGiException
 	 *             if the registration request is invalid.
 	 * @see ch.ethz.iks.r_osgi.RemoteOSGiService#registerService(org.osgi.framework.ServiceReference,
 	 *      java.lang.String, java.lang.String, java.lang.String,
 	 *      java.lang.String[])
 	 * @since 0.6
 	 * @category RemoteOSGiService
 	 */
 	private void registerService(final ServiceReference ref)
 			throws RemoteOSGiException {
 		// sanity check
 		if (ref == null) {
 			throw new RemoteOSGiException("Cannot register a null service");
 		}
 
 		final ServiceReference service = Arrays.asList(
 				(String[]) ref.getProperty(Constants.OBJECTCLASS)).contains(
 				SurrogateRegistration.class.getName()) ? (ServiceReference) ref
 				.getProperty(SurrogateRegistration.SERVICE_REFERENCE) : ref;
 
 		try {
 
 			final RemoteServiceRegistration reg;
 
 			final String policy = (String) ref
 					.getProperty(RemoteOSGiService.R_OSGi_REGISTRATION);
 
 			if (policy.equals(RemoteOSGiService.TRANSFER_BUNDLE_POLICY)) {
 
 				// for the moment, don't accept registrations from bundles that
 				// have already been fetched from a remote peer.
 				if (service.getBundle().getLocation().startsWith("r-osgi://")) {
 					return;
 				}
 
 				reg = new BundledServiceRegistration(ref, service, storage);
 
 				if (log != null) {
 					log.log(LogService.LOG_INFO, "REGISTERING "
 							+ Arrays.asList(reg.getURLs())
 							+ " WITH TRANSFER BUNDLE POLICY");
 				}
 
 			} else {
 				// default: proxied service
 				reg = new ProxiedServiceRegistration(ref, service);
 
 				if (log != null) {
 					log.log(LogService.LOG_INFO, "REGISTERING "
 							+ Arrays.asList(reg.getURLs())
 							+ " AS PROXIED SERVICES");
 				}
 
 			}
 
 			serviceRegistrations.put(service, reg);
 
 			final Dictionary attribs = reg.getProperties();
 			final ServiceURL[] urls = reg.getURLs();
 
 			// schedule for registration on SLP layer
 			reregistration.schedule(reg, System.currentTimeMillis()
 					+ (DEFAULT_SLP_LIFETIME - 1) * 1000);
 
 			for (int i = 0; i < urls.length; i++) {
 				advertiser.register(urls[i], attribs);
 			}
 
 			return;
 		} catch (ServiceLocationException e) {
 			e.printStackTrace();
 			throw new RemoteOSGiException(
 					"Error on SLP layer while registering " + service, e);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			throw new RemoteOSGiException("Cannot find class " + service, e);
 		}
 	}
 
 	/**
 	 * connect to a remote OSGi host.
 	 * 
 	 * @param host
 	 *            the address of the remote OSGi peer.
 	 * @param port
 	 *            the port of the remote OSGi peer.
 	 * @param protocol
 	 *            the protocol to be used or <code>null</code> for default.
 	 * @return the array of service urls of services offered by the remote peer.
 	 * @throws RemoteOSGiException
 	 *             in case of errors.
 	 * @since 0.6
 	 */
 	public ServiceURL[] connect(final InetAddress host, final int port,
 			final String protocol) throws RemoteOSGiException {
 		try {
 			final ChannelEndpointImpl channel = new ChannelEndpointImpl(
 					((NetworkChannelFactory) factories.get(protocol)), host,
 					port, protocol);
 
 			final String[] remoteServices = channel.getRemoteServices();
 			final ServiceURL[] result = new ServiceURL[remoteServices.length];
 			for (int i = 0; i < remoteServices.length; i++) {
 				try {
 					result[i] = new ServiceURL(remoteServices[i], 0);
 				} catch (ServiceLocationException e) {
 					e.printStackTrace();
 				}
 			}
 			return result;
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			throw new RemoteOSGiException("Connection to " + protocol + "://"
 					+ host + ":" + port + " failed", ioe);
 		}
 	}
 
 	/**
 	 * fetch the discovered remote service. The service will be fetched from the
 	 * service providing host and a proxy bundle is registered with the local
 	 * framework.
 	 * 
 	 * @param service
 	 *            the <code>ServiceURL</code>.
 	 * @throws RemoteOSGiException
 	 *             if the fetching fails.
 	 * @see ch.ethz.iks.r_osgi.RemoteOSGiService#fetchService(ch.ethz.iks.slp.ServiceURL)
 	 * @since 0.1
 	 * @category RemoteOSGiService
 	 */
 	public void fetchService(final ServiceURL service)
 			throws RemoteOSGiException {
 		try {
 
 			ChannelEndpointImpl channel;
 			try {
 				channel = getChannel(service);
 			} catch (RemoteOSGiException r) {
 				connect(InetAddress.getByName(service.getHost()), service
 						.getPort(), service.getProtocol());
 				channel = getChannel(service);
 			}
 			channel.fetchService(service);
 		} catch (UnknownHostException e) {
 			throw new RemoteOSGiException("Cannot resolve host "
 					+ service.getHost(), e);
 		} catch (IOException ioe) {
 			throw new RemoteOSGiException("Proxy generation error", ioe);
 		} catch (BundleException e) {
 			Throwable nested = e.getNestedException();
 			if (nested != null) {
 				nested.printStackTrace();
 			} else {
 				e.printStackTrace();
 			}
 			throw new RemoteOSGiException(
 					"Could not install the generated bundle ");
 		}
 	}
 
 	/**
 	 * 
 	 * @param sender
 	 *            the sender serviceURL.
 	 * @param timestamp
 	 *            the Timestamp object.
 	 * @return the Timestamp object that has been transformed into this peer's
 	 *         local time.
 	 * @throws RemoteOSGiException
 	 *             if the timestamp transformation fails.
 	 * @since 0.3
 	 * @category RemoteOSGiService
 	 */
 	public Timestamp transformTimestamp(final ServiceURL sender,
 			final Timestamp timestamp) throws RemoteOSGiException {
 		final ChannelEndpointImpl channel = getChannel(sender);
 		return channel.getOffset().transform(timestamp);
 	}
 
 	/**
 	 * get the channel endpoint for a service url.
 	 * 
 	 * @param serviceURL
 	 *            the service url.
 	 * @return the ChannelEndpoint.
 	 * @see ch.ethz.iks.r_osgi.Remoting#getEndpoint(java.lang.String)
 	 * @category Remoting
 	 */
 	public ChannelEndpoint getEndpoint(final String serviceURL) {
 		try {
 			return getChannel(new ServiceURL(serviceURL, 0));
 		} catch (ServiceLocationException e) {
 			e.printStackTrace();
 			throw new RemoteOSGiException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * called, when a scheduled object is due. In this case, it means that a
 	 * reregistration to the SLP layer is necessary.
 	 * 
 	 * @see ch.ethz.iks.util.ScheduleListener#due(ch.ethz.iks.util.Scheduler,
 	 *      long, java.lang.Object)
 	 */
 	public void due(final Scheduler scheduler, final long timestamp,
 			final Object object) {
 		final RemoteServiceRegistration reg = (RemoteServiceRegistration) object;
 
 		try {
 			final ServiceURL[] urls = reg.getURLs();
 			final Dictionary properties = reg.getProperties();
 
 			for (int i = 0; i < urls.length; i++) {
 				advertiser.register(urls[i], properties);
 			}
 			final long next = System.currentTimeMillis()
 					+ ((DEFAULT_SLP_LIFETIME - 1) * 1000);
 			scheduler.reschedule(reg, next);
 		} catch (ServiceLocationException sle) {
 			sle.printStackTrace();
 		}
 	}
 
 	/**
 	 * the method is called when the R-OSGi bundle is about to be stopped.
 	 * removes all registered proxy bundles.
 	 */
 	void cleanup() {
 		ChannelEndpoint[] c = (ChannelEndpoint[]) channels.values().toArray(
 				new ChannelEndpoint[channels.size()]);
 		channels.clear();
 		for (int i = 0; i < c.length; i++) {
 			c[i].dispose();
 		}
 	}
 
 	/**
 	 * get all provided (remote-enabled) services of this peer. TODO: this can
 	 * be optimized to work in an incremental manner
 	 * 
 	 * @return return the services.
 	 */
 	static String[] getServices() {
 		final ArrayList result = new ArrayList();
 		final RemoteServiceRegistration[] regs = (RemoteServiceRegistration[]) serviceRegistrations
 				.values().toArray(
 						new RemoteServiceRegistration[serviceRegistrations
 								.size()]);
 		for (int i = 0; i < regs.length; i++) {
 			ServiceURL[] urls = regs[i].getURLs();
 			for (int j = 0; j < urls.length; j++) {
				result.add(urls[j].toString());
 			}
 		}
 		return (String[]) result.toArray(new String[result.size()]);
 	}
 
 	static RemoteServiceRegistration getService(final ServiceURL url) {
 		final String interfaceName = url.getServiceType().getConcreteTypeName()
 				.replace('/', '.');
 		final String serviceID = url.getURLPath();
 
 		final String filter = "".equals(serviceID) ? null : '('
 				+ Constants.SERVICE_ID + "=" + serviceID.substring(1) + ")";
 
 		try {
 			final ServiceReference[] refs = context.getServiceReferences(
 					interfaceName, filter);
 			if (refs == null) {
 				System.out.println("WARNING: COUND NOT FIND " + filter);
 				return null;
 			}
 			return (RemoteServiceRegistration) serviceRegistrations
 					.get(refs[0]);
 		} catch (InvalidSyntaxException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * get all topics for which event handlers are registered.
 	 * 
 	 * @return the topics.
 	 */
 	static String[] getTopics() {
 		return (String[]) topics.toArray(new String[topics.size()]);
 	}
 
 	/**
 	 * get the next transaction id.
 	 * 
 	 * @return the next xid.
 	 */
 	static synchronized short nextXid() {
 		if (nextXid == -1) {
 			nextXid = 0;
 		}
 		return (++nextXid);
 	}
 
 	/**
 	 * register a channel.
 	 * 
 	 * @param channel
 	 *            the local endpoint of the channel.
 	 */
 	static void registerChannel(final ChannelEndpoint channel) {
 		channels.put(channel.getID(), channel);
 	}
 
 	/**
 	 * unregister a channel.
 	 * 
 	 * @param channel
 	 *            the local endpoint of the channel.
 	 */
 	static void unregisterChannel(final ChannelEndpoint channel) {
 		channels.remove(channel.getID());
 	}
 
 	/**
 	 * update the leases.
 	 */
 	static void updateLeases() {
 		ChannelEndpointImpl[] endpoints = (ChannelEndpointImpl[]) channels
 				.values().toArray(new ChannelEndpointImpl[channels.size()]);
 		final String[] myServices = getServices();
 		final String[] myTopics = getTopics();
 		for (int i = 0; i < endpoints.length; i++) {
 			endpoints[i].renewLease(myServices, myTopics);
 		}
 	}
 
 	/*
 	 * ----- private methods ------
 	 */
 
 	/**
 	 * get the channel endpoint for the service url.
 	 */
 	private ChannelEndpointImpl getChannel(final ServiceURL service) {
 		final String id;
 		try {
 			id = (service.getProtocol() != null ? service.getProtocol()
 					: "r-osgi")
 					+ "://"
 					+ InetAddress.getByName(service.getHost()).getHostAddress()
 					+ ":" + service.getPort();
 		} catch (UnknownHostException uhe) {
 			throw new RemoteOSGiException(uhe.getMessage());
 		}
 		final ChannelEndpointImpl channel = (ChannelEndpointImpl) channels
 				.get(id);
 		if (channel != null) {
 			return channel;
 		}
 		if (DEBUG) {
 			log.log(LogService.LOG_WARNING, "WARNING: No Channel for " + id
 					+ " found.");
 			log.log(LogService.LOG_WARNING, "available channels: " + channels);
 		}
 		throw new RemoteOSGiException("No NetworkChannel to " + id
 				+ " established");
 	}
 
 	/**
 	 * notify discovery handlers.
 	 * 
 	 * @param service
 	 *            the new discovered service.
 	 * @throws RemoteOSGiException
 	 */
 	private void notifyDiscovery(final ServiceURL service)
 			throws RemoteOSGiException {
 		if (DEBUG) {
 			log.log(LogService.LOG_DEBUG, "discovered " + service);
 		}
 
 		final String interfaceName = service.getServiceType()
 				.getConcreteTypeName().replace('/', '.');
 		try {
 			final ServiceReference[] refs = context.getServiceReferences(
 					DiscoveryListener.class.getName(), "(|("
 							+ DiscoveryListener.SERVICE_INTERFACES + "="
 							+ interfaceName + ")(!("
 							+ DiscoveryListener.SERVICE_INTERFACES + "=*)))");
 			if (refs != null) {
 				for (int i = 0; i < refs.length; i++) {
 					((DiscoveryListener) context.getService(refs[i]))
 							.notifyDiscovery(service);
 					if (refs[i].getProperty(DiscoveryListener.AUTO_FETCH) != null) {
 						fetchService(service);
 					}
 				}
 			}
 
 		} catch (InvalidSyntaxException i) {
 			i.printStackTrace();
 		}
 
 		if (log != null) {
 			log.log(LogService.LOG_DEBUG, "DISCOVERED " + service);
 		}
 	}
 
 	/**
 	 * notify discovery handler that a service has been lost.
 	 * 
 	 * @param service
 	 *            the lost service.
 	 */
 	private void notifyServiceLost(final ServiceURL service) {
 		final String interfaceName = service.getServiceType()
 				.getConcreteTypeName().replace('/', '.');
 		try {
 			final ServiceReference[] refs = context.getServiceReferences(
 					DiscoveryListener.class.getName(), "("
 							+ DiscoveryListener.SERVICE_INTERFACES + "="
 							+ interfaceName + ")");
 
 			if (refs != null) {
 				for (int i = 0; i < refs.length; i++) {
 					((DiscoveryListener) context.getService(refs[i]))
 							.notifyServiceLost(service);
 				}
 			}
 		} catch (InvalidSyntaxException i) {
 			i.printStackTrace();
 		}
 	}
 
 	/*
 	 * Threads
 	 */
 
 	/**
 	 * TCPThread, handles incoming tcp messages.
 	 */
 	private final class TCPThread extends Thread {
 		/**
 		 * the socket.
 		 */
 		private final ServerSocket socket;
 
 		/**
 		 * creates and starts a new TCPThread.
 		 * 
 		 * @throws IOException
 		 *             if the server socket cannot be opened.
 		 */
 		private TCPThread() throws IOException {
 			socket = new ServerSocket(R_OSGI_PORT);
 		}
 
 		/**
 		 * thread loop.
 		 * 
 		 * @see java.lang.Thread#run()
 		 */
 		public void run() {
 			while (running) {
 				try {
 					// accept incoming connections and build channel endpoints
 					// for them
 					new ChannelEndpointImpl(socket.accept());
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * DiscoveryThread to periodically query for services.
 	 */
 	private final class DiscoveryThread extends Thread {
 
 		/**
 		 * thread loop.
 		 * 
 		 * @see java.lang.Thread#run()
 		 */
 		public void run() {
 			try {
 				while (running) {
 					// in case nobody listens, don't do any discovery
 					synchronized (hasListeners) {
 						if (hasListeners == Boolean.FALSE) {
 							hasListeners.wait();
 						}
 					}
 
 					try {
 						// initially contains all known services
 						final List lostServices = new ArrayList(knownServices);
 
 						// find all services of type osgi
 						try {
 							final ServiceLocationEnumeration services = locator
 									.findServices(OSGI, null, null);
 
 							while (services.hasMoreElements()) {
 								final ServiceURL service = (ServiceURL) services
 										.next();
 								if (service.getHost().equals(MY_ADDRESS)) {
 									continue;
 								}
 								if (!knownServices.contains(service)) {
 									notifyDiscovery(service);
 									knownServices.add(service);
 								}
 								// seen, so remove from lost list
 								lostServices.remove(service);
 
 							}
 						} catch (InvalidSyntaxException ise) {
 							// does not happen
 						}
 
 						// notify the listeners for all lost services
 						for (Iterator iter = lostServices.iterator(); iter
 								.hasNext();) {
 							ServiceURL lostService = (ServiceURL) iter.next();
 							if (!warningList.contains(lostService)) {
 								warningList.add(lostService);
 							} else {
 								warningList.remove(lostService);
 								knownServices.remove(lostService);
 								// be polite: first notify the listeners and
 								// then unregister the proxy bundle ...
 								notifyServiceLost(lostService);
 
 								// dispose channel
 								ChannelEndpoint c = (ChannelEndpoint) channels
 										.get(lostService);
 								if (c != null) {
 									c.dispose();
 								}
 							}
 						}
 					} catch (ServiceLocationException sle) {
 						sle.printStackTrace();
 					} catch (RemoteOSGiException re) {
 						re.printStackTrace();
 					}
 					Thread.sleep(DISCOVERY_INTERVAL);
 				}
 			} catch (InterruptedException ie) {
 				ie.printStackTrace();
 			}
 		}
 	}
 
 	// listeners
 
 	/**
 	 * listens for all services that are marked for remote access. This is the
 	 * case, if they have the property <code>service.remote.registration</code>
 	 * set to a string that is a policy name.
 	 */
 	private class ServiceRegistrationListener implements ServiceListener {
 		/**
 		 * gets fired whenever a service (with a R-OSGi registration property
 		 * set) has changed.
 		 * 
 		 * @param event
 		 *            the service event.
 		 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
 		 * @category ServiceListener
 		 */
 		public void serviceChanged(final ServiceEvent event) {
 			if (context == null) {
 				return;
 			}
 			final ServiceReference reference = event.getServiceReference();
 
 			switch (event.getType()) {
 			case ServiceEvent.REGISTERED: {
 				try {
 					registerService(reference);
 				} catch (RemoteOSGiException e) {
 					e.printStackTrace();
 				}
 				updateLeases();
 				return;
 			}
 			case ServiceEvent.UNREGISTERING: {
 				serviceRegistrations.remove(reference);
 				updateLeases();
 				return;
 			}
 			}
 		}
 	}
 
 	/**
 	 * Listens for DiscoveryListeners to appear or disappear.
 	 */
 	private class DiscoveryListenerListener implements ServiceListener {
 
 		/**
 		 * called, when a service has changed.
 		 * 
 		 * @param event
 		 *            the service event.
 		 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
 		 */
 		public void serviceChanged(final ServiceEvent event) {
 			final ServiceReference ref = event.getServiceReference();
 
 			switch (event.getType()) {
 			case ServiceEvent.REGISTERED: {
 
 				final String[] ifaces = (String[]) ref
 						.getProperty(DiscoveryListener.SERVICE_INTERFACES);
 				final HashSet interfaces = ifaces == null ? null : new HashSet(
 						Arrays.asList(ifaces));
 				final DiscoveryListener listener = (DiscoveryListener) context
 						.getService(ref);
 
 				synchronized (hasListeners) {
 					hasListeners.notifyAll();
 					hasListeners = Boolean.TRUE;
 				}
 
 				// inform the listener about all matching services
 				// that have already been discovered
 				ServiceURL[] services = (ServiceURL[]) knownServices
 						.toArray(new ServiceURL[knownServices.size()]);
 
 				for (int i = 0; i < services.length; i++) {
 					final String interfaceName = services[i].getServiceType()
 							.getConcreteTypeName().replace('/', '.');
 					if (interfaces == null
 							|| interfaces.contains(interfaceName)) {
 						listener.notifyDiscovery(services[i]);
 					}
 				}
 				return;
 			}
 			case ServiceEvent.UNREGISTERING: {
 				try {
 					final ServiceReference[] refs = context
 							.getServiceReferences(DiscoveryListener.class
 									.getName(), null);
 					if (refs == null) {
 						synchronized (hasListeners) {
 							hasListeners.notifyAll();
 							hasListeners = Boolean.FALSE;
 						}
 					}
 				} catch (NullPointerException npe) {
 					// sometimes happens when the framework is shutting down
 				} catch (InvalidSyntaxException e) {
 					e.printStackTrace();
 				}
 			}
 			}
 		}
 	}
 
 	/**
 	 * Listen for network channel factories.
 	 * 
 	 * @author Jan S. Rellermeyer, ETH Zurich
 	 */
 	private class NetworkChannelFactoryListener implements ServiceListener {
 
 		/**
 		 * called, when a service has changed.
 		 * 
 		 * @param event
 		 *            the service event.
 		 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
 		 */
 		public void serviceChanged(final ServiceEvent event) {
 			final int type = event.getType();
 			final ServiceReference ref = event.getServiceReference();
 
 			if (DEBUG) {
 				log.log(LogService.LOG_DEBUG, "REGISTERING " + ref);
 			}
 
 			if (type == ServiceEvent.REGISTERED) {
 				final Object protocol = ref
 						.getProperty(NetworkChannelFactory.PROTOCOL_PROPERTY);
 				if (protocol == null) {
 					log.log(LogService.LOG_WARNING, "NetworkChannelFactory "
 							+ ref + " has no protocol property and is ignored");
 				} else if (protocol instanceof String) {
 					final NetworkChannelFactory transport = (NetworkChannelFactory) context
 							.getService(ref);
 					factories.put((String) protocol, transport);
 				} else if (protocol instanceof String[]) {
 					final String[] p = (String[]) protocol;
 					final NetworkChannelFactory transport = (NetworkChannelFactory) context
 							.getService(ref);
 					for (int i = 0; i < p.length; i++) {
 						factories.put(p[i], transport);
 					}
 				}
 			} else if (type == ServiceEvent.UNREGISTERING) {
 				final Object protocol = ref
 						.getProperty(NetworkChannelFactory.PROTOCOL_PROPERTY);
 				if (protocol instanceof String) {
 					factories.remove(protocol);
 				} else if (protocol instanceof String[]) {
 					final String[] p = (String[]) protocol;
 					for (int i = 0; i < p.length; i++) {
 						factories.remove(p[i]);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * listens for event admin instances to arrive or disappear. R-OSGi event
 	 * delivery depends on this service so it is important to track this.
 	 * 
 	 * @author Jan S. Rellermeyer, ETH Zurich
 	 */
 	private class EventAdminServiceListener implements ServiceListener {
 
 		/**
 		 * called, when a service has changed.
 		 * 
 		 * @param event
 		 *            the service event.
 		 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
 		 */
 		public void serviceChanged(final ServiceEvent event) {
 			final int type = event.getType();
 			if (type == ServiceEvent.REGISTERED) {
 				eventAdmin = (EventAdmin) context.getService(event
 						.getServiceReference());
 			} else if (type == ServiceEvent.UNREGISTERING) {
 				eventAdmin = null;
 			}
 		}
 	}
 
 	/**
 	 * Listener for EventHandlers.
 	 * 
 	 * @author Jan S. Rellermeyer
 	 */
 	private class EventHandlerListener implements ServiceListener {
 
 		/**
 		 * called, when a service has changed.
 		 * 
 		 * @param event
 		 *            the service event.
 		 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
 		 */
 		public void serviceChanged(final ServiceEvent event) {
 			if (context == null) {
 				return;
 			}
 			final int type = event.getType();
 			final ServiceReference ref = event.getServiceReference();
 			if (ref.getProperty(R_OSGi_INTERNAL) == Boolean.TRUE) {
 				return;
 			}
 			final String[] theTopics = (String[]) ref
 					.getProperty(EventConstants.EVENT_TOPIC);
 
 			// TODO: REMOVE DEBUG OUTPUT
 			System.out.println("------------------------");
 			System.out.println("NEW LISTENER FOR " + java.util.Arrays.asList(theTopics));
 			System.out.println("------------------------");
 			
 			if (type == ServiceEvent.REGISTERED
 					|| type == ServiceEvent.MODIFIED) {
 				for (int i = 0; i < theTopics.length; i++) {
 					topics.add(theTopics[i]);
 				}
 				updateLeases();
 			} else {
 				for (int i = 0; i < theTopics.length; i++) {
 					topics.remove(theTopics[i]);
 				}
 				updateLeases();
 			}
 		}
 	}
 }
