 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2010 SorcerSoft.org.
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
 
 package sorcer.util;
 
 import net.jini.core.entry.Entry;
 import net.jini.core.lookup.ServiceItem;
 import net.jini.core.lookup.ServiceRegistrar;
 import net.jini.core.lookup.ServiceTemplate;
 import net.jini.discovery.DiscoveryEvent;
 import net.jini.discovery.DiscoveryListener;
 import net.jini.discovery.LookupDiscovery;
 import net.jini.lookup.entry.Name;
 import sorcer.core.SorcerConstants;
 import sorcer.service.*;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.logging.Logger;
 
 /**
  * A class which supports a simple Jini multicast lookup. It doesn't register
  * with any ServiceRegistrars it simply interrogates each one that's discovered
  * for a ServiceItem associated with the passed type/signature of a provider.
  */
 public class ProviderLookup implements DiscoveryListener, DynamicAccessor,
 		SorcerConstants {
 	private ServiceTemplate template;
 
 	private LookupDiscovery discoverer;
 
 	private Object proxy;
 
 	static final long WAIT_FOR = Sorcer.getLookupWaitTime();
 
 	static final int MAX_TRIES = 5;
 
 	static final private Logger logger = Log.getTestLog();
 
 	private int tries = 0;
 
 	public static void init() {
 		Accessor.setAccessor(new ProviderLookup());
 	}
 
 	public ProviderLookup() {
 		// do noting
 	}
 
 	/*
 	 * Returns a @link{Service} with the given signtures.
 	 * 
 	 * @see sorcer.service.DynamicAccessor#getServicer(sorcer.service.Signature)
 	 */
 	public Service getServicer(Signature signature) {
 		return getService(signature);
 	}
 
 	public static Service getService(Signature signature) {
 		ProviderLookup lookup = new ProviderLookup(signature.getProviderName(),
 				signature.getServiceType());
 		return (Service) lookup.getService();
 	}
 
 	/**
 	 * Returns a service provider with the specified service type.
 	 * 
 	 * @param serviceType
 	 *            a provider service type (interface)
 	 * @return a service provider
 	 */
 	public final static Object getService(Class serviceType) {
 		return getService(null, serviceType);
 	}
 
 	
 	/**
 	 * Returns a service provider with the specified name and service type.
 	 * 
 	 * @param providerName
 	 *            The name of the provider to search for
 	 * @param serviceType
 	 *            The interface to look for
 	 * 
 	 * @return a service provider
 	 */
 	public final static Object getService(String providerName,
 			Class serviceType) {
 		ProviderLookup lookup = new ProviderLookup(providerName, serviceType);
 		return lookup.getService();
 	}
 	
 	/**
 	 * Returns a SORCER service provider with the specified name and service
 	 * type.
 	 * 
 	 * @param providerName
 	 *            the name of service provider
 	 * @param serviceType
 	 *            a provider service type (interface)
 	 * @return a SORCER service provider
 	 */
 	public final static Service getProvider(String providerName,
 			String serviceType) {
 		return (Service) getService(providerName, serviceType);
 	}
 
 	/**
 	 * Returns a service provider with the specified name and service type.
 	 * 
 	 * @param providerName
 	 *            The name of the provider to search for
 	 * @param serviceType
 	 *            The interface to look for
 	 * 
 	 * @return a service provider
 	 */
 	public final static Object getService(String providerName,
 			String serviceType) {
 		Class type;
 		try {
 			type = Class.forName(serviceType);
 		} catch (ClassNotFoundException cnfe) {
 			//logger.throwing("ProviderLookup", "getService", cnfe);
 			return null;
 		}
 		ProviderLookup lookup = new ProviderLookup(providerName, type);
 		return lookup.getService();
 	}
 
 	/**
 	 * Returns a SORCER service provider with the specified service type, using
 	 * a Cataloger if availabe, otherwise using Jini lookup services.
 	 * 
 	 * @param serviceType
 	 *            a provider service type (interface)
 	 * @return a SORCER service provider
 	 */
 	public final static Service getProvider(String serviceType) {
 		return getProvider(null, serviceType);
 	}
 
 	/**
	 * @param aServiceInterface
 	 *            the class of the type of service you are looking for. Class is
 	 *            usually an interface class.
 	 */
 	ProviderLookup(Class serviceInterface) {
 		this(null, serviceInterface);
 	}
 
 	ProviderLookup(String providerName, Class serviceInterface) {
 		Class[] serviceTypes = new Class[] { serviceInterface };
 		Entry[] attrs = null;
 		String pn = providerName;
 		if (pn != null && pn.equals(ANY))
 			pn = null;
 		if (providerName != null) {
 			attrs = new Entry[] { new Name(pn) };
 		}
 		template = new ServiceTemplate(null, serviceTypes, attrs);
 	}
 
 	/**
 	 * Having created a Lookup (which means it now knows what type of service
 	 * you require), invoke this method to attempt to locate a service of that
 	 * type. The result should be cast to the interface of the service you
 	 * originally specified to the constructor.
 	 * 
 	 * @return proxy for the service type you requested - could be an rmi stub
 	 *         or a smart proxy.
 	 */
 	Object getService() {
 		proxy = lookupProxy();
 		if (proxy != null) {
 			terminate();
 			return proxy;
 		}
 		terminate();
 		return null;
 	}
 
 	private Object lookupProxy() {
 		synchronized (this) {
 			if (discoverer == null) {
 				try {
 					discoverer = new LookupDiscovery(getGroups());
 					// discoverer = new
 					// LookupDiscovery(LookupDiscovery.ALL_GROUPS);
 					//logger.finer("service lookup for groups: " + Arrays.toString(getGroups()));
 					//.logger.finer("WAIT_FOR: " + WAIT_FOR);
 					discoverer.addDiscoveryListener(this);
 				} catch (IOException ioe) {
 					logger.finer("Failed to lookup proxy: " + template);
 					logger.throwing(getClass().getName(), "getService", ioe);
 				}
 			}
 		}
 		return waitForProxy();
 	}
 
 	/**
 	 * Location of a service causes the creation of some threads. Call this
 	 * method to shut those threads down either before exiting or after a proxy
 	 * has been returned from getService().
 	 */
 	void terminate() {
 		synchronized (this) {
 			if (discoverer != null)
 				discoverer.terminate();
 		}
 	}
 
 	/**
 	 * Caller of getService ends up here, blocked until we find a proxy.
 	 * 
 	 * @return the newly downloaded proxy
 	 */
 	private Object waitForProxy() {
 		synchronized (this) {
 			while (proxy == null && tries < MAX_TRIES) {
 				try {
 					wait(WAIT_FOR);
 					tries++;
 					logger.fine("has tried times: " + tries + " for "
 							+ template);
 				} catch (InterruptedException ie) {
 					logger.throwing(getClass().getName(), "waitForProxy", ie);
 					proxy = null;
 					return proxy;
 				}
 
 			}
 			return proxy;
 		}
 	}
 
 	/**
 	 * Invoked to inform a blocked client waiting in waitForProxy that one is
 	 * now available.
 	 * 
 	 * @param proxy
 	 *            the newly downloaded proxy
 	 */
 	private void signalGotProxy(Object proxy) {
 		synchronized (this) {
 			if (this.proxy == null) {
 				this.proxy = proxy;
 				notify();
 			}
 		}
 	}
 
 	/**
 	 * Everytime a new ServiceRegistrar is found, we will be called back on this
 	 * interface with a reference to it. We then ask it for a service instance
 	 * of the type specified in our constructor.
 	 */
 	public void discovered(DiscoveryEvent event) {
 		synchronized (this) {
 			if (proxy != null)
 				return;
 		}
 		ServiceRegistrar[] regs = event.getRegistrars();
 		for (int i = 0; i < regs.length; i++) {
 			ServiceRegistrar reg = regs[i];
 			Object foundProxy = null;
 			try {
 				foundProxy = reg.lookup(template);
 				if (foundProxy != null) {
 					signalGotProxy(foundProxy);
 					break;
 				}
 			} catch (RemoteException re) {
 				logger.finer("ServiceRegistrar barfed");
 				logger.throwing(getClass().getName(), "discovered", re);
 			}
 		}
 	}
 
 	/**
 	 * When a ServiceRegistrar "disappears" due to network partition etc. we
 	 * will be advised via a call to this method - as we only care about new
 	 * ServiceRegistrars, we do nothing here.
 	 */
 	public void discarded(DiscoveryEvent anEvent) {
 		// do nothing for now
 	}
 
 	/**
 	 * Returns a list of groups as defined in the SORCER environment
 	 * configuration, the sorcer.env file.
 	 * 
 	 * @return a list of group names
 	 * @see Sorcer
 	 */
 	protected static String[] getGroups() {
 		return Sorcer.getLookupGroups();
 	}
 
 	/**
 	 * Added for compatibility with DynamicAccessor. This method is implemented
 	 * in { @link sorcer.util.ProviderAccessor } and { @link
 	 * sorcer.servme.QosProviderAccessor }
 	 */
 	public ServiceItem getServiceItem(Signature signature)
 			throws SignatureException {
 		throw new SignatureException("Not implemented by this service accessor");
 	}
 
 }
