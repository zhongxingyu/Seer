 /**
  *
  * Copyright 2013 the original author or authors.
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
 package sorcer.util;
 
 import net.jini.core.entry.Entry;
 import net.jini.core.lookup.ServiceID;
 import net.jini.core.lookup.ServiceItem;
 import net.jini.core.lookup.ServiceTemplate;
 import sorcer.core.*;
 import sorcer.service.*;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * A utility class that provides access to SORCER services and some
  * infrastructure services. It extends the <code>ServiceAccessor</code>
  * functionality.
  * 
  * The <code>getService</code> methods directly use ServiceAccessor calls while
  * the <code>getProvider</code> methods use a SORCER Cataloger's cached services
  * with round robin load balancing.
  * 
  * The individual SORCER services should be accessed using this utility since it
  * uses local cached proxies for frequently used SORCER infrastructure services,
  * for example: Cataloger, JavaSpace, Jobber. ProviderAccessor normally uses
  * Cataloger if available, otherwise it uses Jini lookup services as implemented
  * by <code>ServiceAccessor</code>.
  * 
  * @see ServiceAccessor
  */
 @SuppressWarnings("rawtypes")
 public class ProviderAccessor extends ServiceAccessor implements
 		DynamicAccessor {
 
 	static Logger logger = Logger.getLogger(ProviderAccessor.class.getName());
 
 	protected static ProviderAccessor accessor;
 
 	/**
 	 * Used for local caching to speed up getting frequently needed service
 	 * providers. Calls to discover JavaSpace takes a lot of time.
 	 */
 	protected static Cataloger cataloger;
 
     protected static ProviderNameUtil providerNameUtil = new SorcerProviderNameUtil();
 
     protected static Map<Class, Object> cache = new HashMap<Class, Object>();
 
	protected ProviderAccessor() {
 		// Nothing to do, uses the singleton design pattern
 	}
 
	protected ProviderAccessor(String[] lookupGroups) {
		ProviderAccessor.lookupGroups = lookupGroups;
	}

	public static ProviderAccessor getAccessor() {
		if (accessor == null)
			accessor = new ProviderAccessor();
		ProviderAccessor.lookupGroups = Sorcer.getLookupGroups();
		return accessor;
	}

	/**
 	 * Initializes the cache of frequently used SORCER services. That includes a
 	 * Cataloger, Jobber, and Spacer used by this ProviderAccessor.
 	 * 
 	 */
 	static {
 		try {
 			openDiscoveryManagement(lookupGroups);
 		} catch (IOException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 	}
 
 	/**
 	 * Returns a SORCER service provider matching a registered serviceID,
 	 * serviceTypes, attribute set, and Jini groups.
 	 *
 	 * @param serviceID
 	 *            a service provider ID
 	 * @param serviceTypes
 	 *            service types to match
 	 * @param attrSets
 	 *            a list of attributes describing the requested service
 	 * @param groups
 	 *            Jini lookup service groups
 	 * @return a SORCER provider service
 	 */
 	public static Object getService(ServiceID serviceID, Class[] serviceTypes,
 			Entry[] attrSets, String[] groups) {
 		ServiceTemplate st = new ServiceTemplate(serviceID, serviceTypes,
 				attrSets);
 		return getService(st, null, groups);
 	}
 
 	/**
 	 * Returns a SORCER service provider registered with serviceID.
 	 *
 	 * @param serviceID
 	 *            a service provider ID
 	 * @return a SORCER provider service
 	 */
 	public static Object getService(ServiceID serviceID) {
 		return getService(serviceID, null, null, getLookupGroups());
 	}
 
 	/**
 	 * Returns a SORCER service provider registered with the most significant
 	 * and the least significant bits.
 	 *
 	 * @param mostSig
 	 *            most significant bits
 	 * @param leastSig
 	 *            least significant bits
 	 * @return a SORCER provider service
 	 */
 	public static Object getService(long mostSig, long leastSig) {
 		ServiceID serviceID = new ServiceID(mostSig, leastSig);
 		return getService(serviceID, null, null, getLookupGroups());
 	}
 
 	/**
 	 * Returns a SORCER service provider with the specified name and service
 	 * type, using a Cataloger if available, otherwise using Jini lookup
 	 * services.
 	 *
 	 * @param providerName
 	 *            the name of service provider
 	 * @param serviceType
 	 *            a provider service type (interface)
 	 * @return a SORCER provider service
 	 */
 	public static Provider getProvider(String providerName, Class serviceType) {
 		Provider servicer = null;
 		if (providerName != null && providerName.equals(SorcerConstants.ANY))
 			providerName = null;
 		try {
 			//servicer = (Service)ProviderLookup.getService(providerName, serviceType);
 			cataloger = getCataloger();
 			if (cataloger != null) {
 				int tryNo = 0;
 				while (tryNo < LUS_REAPEAT) {
 					servicer = cataloger.lookup(providerName, serviceType);
 					//servicer = (Service)cataloger.lookupItem(providerName, serviceType).service;
 					if (servicer != null)
 						break;
 
 					Thread.sleep(WAIT_FOR);
 					tryNo++;
 				}
 			}
 			// fall back on Jini LUS
 			if (servicer == null) {
 				servicer = (Provider) getService(providerName, serviceType);
 			}
 		} catch (Throwable ex) {
 			logger.throwing(ProviderAccessor.class.getName(), "getProvider", ex);
 			ex.printStackTrace();
 		}
 		return servicer;
 	}
 
 
 	/**
 	 * Returns a SORCER service provider with the specified signature, using a
 	 * Cataloger if available, otherwise using Jini lookup services.
 	 *
 	 * @param signature
 	 *            the signature of service provider
 	 * @return a SORCER provider service
 	 */
 	public static Provider getProvider(Signature signature) {
 		return ProviderAccessor.getProvider(signature.getProviderName(),
 				signature.getServiceType());
 	}
 
 	/**
 	 * Returns a SORCER Provider with the specified name, service type, and a
 	 * codebase where the interface class can be downloaded from.
 	 *
 	 * @param providerName
 	 *            The name of the provider to search for
 	 * @param serviceType
 	 *            The interface to look for
 	 * @param codebase
 	 *            The location where to download the class from
 	 * @return a SORCER Provider
 	 */
 	public static Provider getProvider(String providerName,
 			Class serviceType, String codebase) {
 		return (Provider) getService(providerName, serviceType, codebase);
 	}
 
 	/**
 	 * Returns a SORCER service provider with the specified service ID using a
 	 * Cataloger if available, otherwise using Jini lookup services.
 	 *
 	 * @param serviceID
 	 *            serviceID of the desired service
 	 * @return a SORCER provider service
 	 */
 	public static Provider getProvider(ServiceID serviceID) {
 		try {
 			cataloger = getCataloger();
 			if (cataloger != null)
 				return cataloger.lookup(serviceID);
 			else
 				return (Provider) getService(serviceID);
 		} catch (Exception ex) {
 			logger.throwing(ProviderAccessor.class.getName(), "getProvider", ex);
 			return null;
 		}
 	}
 
 	/**
 	 * Returns a SORCER service provider with the specified service type, using
 	 * a Cataloger if available, otherwise using Jini lookup services.
 	 *
 	 * @param serviceType
 	 *            a provider service type (interface)
 	 * @return a SORCER provider service
 	 */
 	public static Provider getProvider(Class serviceType) {
 		return getProvider(null, serviceType);
 	}
 
 	/**
 	 * Returns a SORCER service provider matching a given attributes.
 	 *
 	 * @param attributes
 	 *            attribute set to match
 	 * @return a SORCER provider
 	 */
 	public static Provider getProvider(Entry[] attributes) {
 		return (Provider) getService(null, null, attributes, getLookupGroups());
 	}
 
 	/**
 	 * Returns a SORCER service provider matching a given list of implemented
 	 * service types (interfaces).
 	 *
 	 * @param serviceTypes
 	 *            a set of service types to match
 	 * @return a SORCER provider
 	 */
 	public static Provider getProvider(Class[] serviceTypes) {
 		return (Provider) getService(null, serviceTypes, null, getLookupGroups());
 	}
 
 	/**
 	 * Returns a SORCER Cataloger Service.
 	 *
 	 * This method searches for either a JINI or a RMI Cataloger service.
 	 *
 	 * @return a Cataloger service proxy
 	 * @throws AccessorException
      * @see sorcer.core.Cataloger
 	 */
 	public static Cataloger getCataloger() throws AccessorException {
         return getCataloger(providerNameUtil.getName(Cataloger.class)) ;
 	}
 
 	/**
 	 * Returns a SORCER Cataloger service provider using JINI discovery.
 	 *
 	 * @return a SORCER Cataloger
 	 */
     public static Cataloger getCataloger(String serviceName) {
         boolean catIsOk;
         try {
             catIsOk = isAlive((Provider) cataloger);
         } catch (Exception re) {
 			catIsOk = false;
 		}
 		try {
 			if (catIsOk) {
 				return cataloger;
 			} else
                 return cataloger = ServiceAccessor.getService(serviceName,
 						Cataloger.class);
 		} catch (Exception e) {
 			logger.throwing(ProviderAccessor.class.getName(), "getProvider", e);
 			return null;
 		}
 	}
 
 	/**
 	 * Returns a SORCER service using a cached Cataloger instance by this
 	 * ProviderAccessor.
 	 *
 	 * @param primaryInterface
 	 *            - service type of requested provider
 	 * @return a requested service or null if a Catloger is not available
 	 * @throws RemoteException
 	 * @throws AccessorException
 	 */
 	public static Provider lookup(Class primaryInterface)
 			throws RemoteException, AccessorException {
 		return lookup(null, primaryInterface);
 	}
 
 	/**
 	 * Returns a SORCER service using a cached Cataloger instance by this
 	 * ProviderAccessor. However if it not possible uses a ServiceAccessor to
 	 * get a requested service form Jini lookup services directly. This approach
 	 * allows for SORCER requestors and providers to avoid continuous usage of
 	 * lookup discovery for each needed service that is delegated to a SORCER
 	 * Cataloger service.
 	 *
 	 * @param providerName
 	 *            - a name of requested service
 	 * @param primaryInterface
 	 *            - service type of requested provider
 	 * @return a requested service or null if a Cataloger is not available
 	 * @throws AccessorException
 	 */
 	public static Provider lookup(String providerName, Class primaryInterface)
 			throws AccessorException {
 		try {
 			// check if the cataloger is alive then return a reqested service
 			// provider
 			if (ProviderAccessor.isAlive((Provider) cataloger))
 				return cataloger.lookup(providerName, primaryInterface);
 			else {
 				// try to get a new cataloger and lookup again
 				cataloger = getService(providerNameUtil.getName(Cataloger.class), Cataloger.class);
 				if (cataloger != null) {
 					logger.info("Got service provider from Cataloger");
 					return cataloger.lookup(providerName, primaryInterface);
 				} else {
 					// just get a provider without a Cataloger, use directly
 					// LUSs
 					logger.severe("No SORCER cataloger available");
 					return (Provider) getService(providerName, primaryInterface);
 				}
 			}
 		} catch (RemoteException ex) {
 			logger.throwing(ProviderAccessor.class.getName(), "lookup", ex);
 			return null;
 		}
 	}
 
 	/**
 	 * Used by the {@link Accessor} facility.
 	 * 
 	 * @throws SignatureException
 	 * 
 	 * @see sorcer.service.DynamicAccessor#getServicer(sorcer.service.Signature)
 	 */
 	public Service getServicer(Signature signature) throws SignatureException {
 		return getProvider(signature.getProviderName(),
 				signature.getServiceType());
 	}
 
 	/**
 	 * Used by the {@link Accessor} facility.
 	 * 
 	 * @throws SignatureException
 	 * 
 	 * @see sorcer.service.DynamicAccessor#getServiceItem(sorcer.service.Signature)
 	 */
 	public ServiceItem getServiceItem(Signature signature)
 			throws SignatureException {
 		logger.info("Using ProviderAccessor.getServicer");
 		return getServiceItem(signature.getProviderName(),
 				signature.getServiceType());
 	}
 
 }
