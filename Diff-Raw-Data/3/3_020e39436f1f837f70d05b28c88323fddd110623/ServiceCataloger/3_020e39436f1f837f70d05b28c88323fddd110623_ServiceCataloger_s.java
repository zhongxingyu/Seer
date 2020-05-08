 /*
  * Copyright 2009 the original author or authors.
  * Copyright 2009 SorcerSoft.org.
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
 package sorcer.core.provider.cataloger;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Type;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import net.jini.core.discovery.LookupLocator;
 import net.jini.core.entry.Entry;
 import net.jini.core.lookup.ServiceID;
 import net.jini.core.lookup.ServiceItem;
 import net.jini.core.lookup.ServiceMatches;
 import net.jini.core.lookup.ServiceTemplate;
 import net.jini.discovery.LookupDiscoveryManager;
 import net.jini.lookup.LookupCache;
 import net.jini.lookup.ServiceDiscoveryEvent;
 import net.jini.lookup.ServiceDiscoveryListener;
 import net.jini.lookup.ServiceDiscoveryManager;
 import net.jini.lookup.entry.Name;
 import sorcer.core.AdministratableProvider;
 import sorcer.core.provider.Cataloger;
 import sorcer.core.provider.Provider;
 import sorcer.core.SorcerEnv;
 import sorcer.core.ContextManagement;
 import sorcer.core.context.ServiceContext;
 import sorcer.core.exertion.NetTask;
 import sorcer.core.provider.ServiceProvider;
 import sorcer.core.signature.NetSignature;
 import sorcer.jini.lookup.entry.SorcerServiceInfo;
 import sorcer.service.Context;
 import sorcer.service.Service;
 import sorcer.service.Task;
 
 import com.sun.jini.start.LifeCycle;
 import sorcer.util.StringUtils;
 
 import static sorcer.core.SorcerConstants.*;
 
 /**
  * The facility for maintaining a cache of all SORCER OS :: providers {@link sorcer.service.Service}
  * s as specified by
  * <code>provider.template.match=sorcer.service.Service</code> in the
  * <code>sorcer.env</code> configuration file.
  * <p>
  * <ul>
  * <li>It uses <code>ServiceDiscoveryManager</code> with lookup cache.<br>
  * <li>It uses an internal hash map for storing services called
  * {@link CatalogerInfo}
  * <li>The key of the map is an {@link sorcer.core.provider.cataloger.ServiceCataloger.CatalogerInfo.InterfaceList}, the value is the list
  * of service proxies (<code>ServiceItem<code>s)
  * <li>{@link sorcer.core.provider.cataloger.ServiceCataloger.CatalogerInfo.InterfaceList} is a list of interfaces with <code>equals</code>
  * overridden such that for <code>(interfaceList1.equals(interfaceList2)</code>
  * returns <code>true</code> if all elements contained in
  * <code>interfaceList2</code> are contained in <code>interfaceList1</code>.
  * <li><code>get</code> and <code>put</code> method of {@link CatalogerInfo} are
  * overridden to do nothing.
  * </ul>
  * <p>
  * Only access to {@link CatalogerInfo} is via a set of "service-aware" methods.
  * They include
  * <ol>
  * <li><code>addServiceItem(SeviceItem)</code>: adds an entry to this hash map
  * such that the key is the <code>InterfaceList</code> describing the service.
  * value is the serviceItem itself. Value is added always to the first to
  * improve load balancing heuristics (assuming that the latest served service is
  * always removed and added to the end
  * 
  * <li> <code>getServiceItem(String[] interfaces), String providerName))</code>:
  * not only returns the serviceItem with the following specs, but also removes
  * and adds the sericeItem to the end of the list to provide load-balancing
  * 
  * <li> synchronized <code>getServiceItem(ServiceID serviceID)</code> returns a
  * service with a serviceID, with the same load-balancing feature mentioned
  * above
  * 
  * <li> <code>getServiceMethods())</code> returns a hash map with the key as a
  * service interface (those interfaces package name starting with
  * <code>sorcer.</code>) and its value is a list of interface's method names.
  * 
  * <li> <code>getMethodContext(providerName, methodName))</code> returns the
  * template context with which the provider is registered. This template context
  * is pulled out of the service attribute (Entry): {@link SorcerServiceInfo}.
  * </ol>
  */
 @SuppressWarnings({"rawtypes", "unchecked"})
 public class ServiceCataloger extends ServiceProvider implements Cataloger, AdministratableProvider {
 
 	/** Logger for logging information about this instance */
 	//private static Logger logger = Logger.getLogger(ServiceCataloger.class.getName());
 	private static Logger logger;
 
 	public ServiceDiscoveryManager lookupMgr;
 
 	public LookupCache cache;
 
 	protected static CatalogerInfo cinfo;
 
 	private String[] locators = null;
 
 	public LookupLocator[] getLL() throws RemoteException {
 		LookupLocator[] specificLocators = null;
 		String sls = getProperty(P_LOCATORS);
 
 		if (sls != null)
 			locators = StringUtils.tokenize(sls, ",");
 		try {
 			if (locators != null && locators.length > 0) {
 				specificLocators = new LookupLocator[locators.length];
 				for (int i = 0; i < specificLocators.length; i++) {
 					specificLocators[i] = new LookupLocator(locators[i]);
 				}
 			}
 			return specificLocators;
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public String[] getGroups() throws RemoteException {
 		String gs = getProperty(P_GROUPS);
 		String[] groups = (gs == null) ? SorcerEnv.getLookupGroups() : StringUtils
 				.tokenize(gs, ",");
 		return groups;
 	}
 
 	public ServiceTemplate getTemplate() throws RemoteException {
 		String templateMatch = SorcerEnv.getProperty(P_TEMPLATE_MATCH,
 				"" + Service.class);
 		logger.info(P_TEMPLATE_MATCH + ": " + templateMatch);
 		ServiceTemplate template;
 		try {
 			template = new ServiceTemplate(null,
 					new Class[] { Class.forName(templateMatch) }, null);
 			return template;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
     @SuppressWarnings("unused")
 	public ServiceCataloger() throws RemoteException {
 		// do nothing
 	}
 
     @SuppressWarnings("unused")
 	public ServiceCataloger(String[] args, LifeCycle lifeCycle)
 			throws Exception {
 		super(args, lifeCycle);
 		init();
 	}
 
 	public void init() {
 		try {
 			initLogger();
 			LookupLocator[] specificLocators = null;
 			String sls = getProperty(P_LOCATORS);
 
 			if (sls != null)
 				locators = StringUtils.tokenize(sls, " ,");
 			if (locators != null && locators.length > 0) {
 				specificLocators = new LookupLocator[locators.length];
 				for (int i = 0; i < specificLocators.length; i++) {
 					specificLocators[i] = new LookupLocator(locators[i]);
 				}
 			}
 
 			String gs = getProperty(P_GROUPS);
 			String[] groups = (gs == null) ? SorcerEnv.getLookupGroups() : StringUtils
 					.tokenize(gs, ",");			
 			lookupMgr = new ServiceDiscoveryManager(new LookupDiscoveryManager(
 					groups, specificLocators, null), null);
 
 			String templateMatch = SorcerEnv.getProperty(P_TEMPLATE_MATCH,
 					Service.class.getName());
 			ServiceTemplate template = new ServiceTemplate(null,
 					new Class[] { Class.forName(templateMatch) }, null);
 			cinfo = new CatalogerInfo();
 			cache = lookupMgr.createLookupCache(template, null,
 					new CatalogerEventListener());
 
 			logger.info("-----------------------------");
 			logger.info("Matching services that are: " + templateMatch);
 			logger.info(P_GROUPS + ": " + Arrays.toString(groups));
 			logger.info(P_LOCATORS + ": " + Arrays.toString(specificLocators));
 			logger.info("------------------------------");
 		} catch (IOException ex) {
 			ex.printStackTrace();
         } catch (ClassNotFoundException cnfe) {
 			cnfe.printStackTrace();
 		}
 	}
 
 	private void initLogger() {
 		Handler h;
 		try {
 			logger = Logger.getLogger("local."
 					+ ServiceCataloger.class.getName() + "."
 					+ getProviderName());
 			h = new FileHandler(SorcerEnv.getHomeDir()
                     + "/logs/remote/local-Cataloger-" + delegate.getHostName()
 					+ "-" + getProviderName() + ".log", 2000000, 8, true);
             h.setFormatter(new SimpleFormatter());
             logger.addHandler(h);
 			logger.setUseParentHandlers(false);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Returns a Jini ServiceItem containing SORCER service provider based on
 	 * two entries provided. The first entry is a provider's service type, the
 	 * second provider's name. Expected that more entries will be needed to
 	 * identify a provider in the future. See also lookup for a given ServiceID.
 	 * 
 	 * @see sorcer.core.provider.Cataloger#lookup(Class[])
 	 */
 	public ServiceItem lookupItem(String providerName, Class... serviceTypes)
 			throws RemoteException {
 		return cinfo.getServiceItem(serviceTypes, providerName);
 	}
 
 	/**
 	 * Returns a SORCER service provider identified by its primary service type.
 	 * 
 	 * @param serviceTypes
 	 *            - interface of a SORCER provider
 	 * @return a SORCER service provider
 	 * @throws RemoteException
 	 */
 	public Provider lookup(Class... serviceTypes) throws RemoteException {
 		return lookup(null, serviceTypes);
 
 	}
 
 	/**
 	 * * Returns a SORCER service provider identified by its primary service
 	 * type and the provider's name/
 	 * 
 	 * @param providerName
 	 *            - a provider name, a friendly provider's ID.
 	 * @param serviceTypes
 	 *            - interface of a SORCER provider
 	 * @return a SORCER service provider
 	 * @throws RemoteException
 	 */
 	public Provider lookup(String providerName, Class... serviceTypes)
 			throws RemoteException {
 		String pn = providerName;
 		if (ANY.equals(providerName))
 			pn = null;
 		try {
 			ServiceItem sItem = cinfo.getServiceItem(serviceTypes, pn);
 			if (sItem != null && (sItem.service instanceof Provider))
 				return (Provider) sItem.service;
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a SORCER service provider identified by its service ID.
 	 * 
 	 * @param sid
 	 *            - provider's ID
 	 * @return a SORCER service provider
 	 * @throws RemoteException
 	 */
 	public Provider lookup(ServiceID sid) throws RemoteException {
 		if (sid == null)
 			return null;
 		ServiceItem sItem = cinfo.getServiceItem(sid);
 		return (sItem != null && sItem.service instanceof Provider) ? (Provider) sItem.service
 				: null;
 	}
 
 	public Map<String, String> getProviderMethods() throws RemoteException {
 		if (cinfo == null)
 			return new HashMap();
 		else
 			return cinfo.getProviderMethods();
 	}
 
 	/** Returns the list of available providers in this catalog.
 	 * 
 	 * @return
 	 * @throws RemoteException
 	 */
 	public String[] getProviderList() throws RemoteException {
 		Collection<List<ServiceItem>> c = cinfo.values();
 		if (c == null)
 			return new String[0];
 
 		List<String> names = new ArrayList<String>();
 		for (List<ServiceItem> l : c) {
 			for (ServiceItem si : l) {
 				Entry[] attributes = si.attributeSets;
 				for (Entry a : attributes) {
 					if (a instanceof Name) {
 						names.add(((Name) a).name);
 						break;
 					}
 				}
 			}
 		}
 		String[] pns = new String[names.size()];
 		return names.toArray(pns);
 	}
 
 	public String[] getInterfaceList(String providerName)
 			throws RemoteException {
 		if (cinfo == null) {
 			return new String[0];
 		}
 		return cinfo.getInterfaceList(providerName);
 	}
 
 	public String[] getMethodsList(String providerName, String interfaceName)
 			throws RemoteException {
 		if (cinfo == null) {
 			return new String[0];
 		}
 		return cinfo.getMethodsList(providerName, interfaceName);
 	}
 
 	public Boolean deleteContext(String providerName, String interfaceName,
 			String methodName) throws RemoteException {
 		if (cinfo == null) {
 			return false;
 		}
 		return cinfo.deleteContext(providerName, interfaceName, methodName);
 	}
 
 	public String[] getSavedContextList(String providerName,
 			String interfaceName) throws RemoteException {
 		if (cinfo == null) {
 			return new String[0];
 		}
 		return cinfo.getSavedContextList(providerName, interfaceName);
 	}
 
 	public Context getContext(String providerName, String interfaceName,
 			String methodName) throws RemoteException {
 		if (cinfo == null) {
 			return null;
 		}
 		try {
             return cinfo.getContext(providerName, interfaceName,
                     methodName);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "IRFAILED", e);
 		}
 		return null;
 
 	}
 
 	public Boolean saveContext(String providerName, String interfaceName,
 			String methodName, Context theContext) throws RemoteException {
 		if (cinfo == null) {
 			return false;
 		}
 		try {
 			cinfo.saveContext(providerName, interfaceName, methodName,
 					theContext);
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 
 	}
 
 	public Context exertService(String providerName, Class serviceType,
 			String methodName, Context theContext) throws RemoteException {
 		if (cinfo == null) {
 			return new ServiceContext();
 		}
 		try {
 
 			return cinfo.exertService(providerName, serviceType, methodName,
 					theContext);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return new ServiceContext();
 	}
 
 	public Context getContexts(Class serviceType, String method)
 			throws RemoteException {
 		logger.info("interfaceName=" + serviceType + " method=" + method);
 		return cinfo.getContext(serviceType, method);
 	}
 
 	public String getServiceInfo() throws RemoteException {
 		return cinfo.toString();
 	}
 
 	/**
 	 * A customized &quot;sorcer provider&quot; aware a map of the cataloger
 	 * info.
 	 * <p>
 	 * The key of the cataloger info is the <code>InterfaceList</code>. This
 	 * inner <code>InterfaceList</code> is an array list of interface names with
 	 * overridden <code>equals</code> such that it follows the following
 	 * semantics: <code>arrayList1.equals(arrayList2</code> iff all interfaces
 	 * of arrayList2 are contained in arrayList1.<br>
 	 * The key value in <code>InterfaceList</code> is the list of service items
 	 * implementing its key interfaces. Methods <code>get</code> and
 	 * <code>put</code> are overridden to do nothing.
 	 * <p>
 	 * The method <code>addServiceItem(SeviceItem)</code> adds an entry to this map
 	 * such that the key is the <code>InterfaceList</code> describing this
 	 * service, the value is the serviceItem itself.
 	 * <p>
 	 * The method <code>getServiceItem(String[] interfaces, String providerName)</code>
 	 * not only returns the serviceItem with the following specs, but also
 	 * removes and adds the sericeItem to the end of the list to provide
 	 * load-balancing
 	 * <p>
 	 * The method <code>getServiceItem(ServiceID serviceID)</code> returns a service with a
 	 * given serviceID
 	 * <p>
 	 * The method <code>getProviderMethods</code> return a map of provider's
 	 * <code>service methods</code>
 	 */
 	protected static class CatalogerInfo extends ConcurrentHashMap<CatalogerInfo.InterfaceList, List<ServiceItem>> implements
 			Serializable {
 		private static final long serialVersionUID = 1L;
 		Cataloger cataloger = null;
 
 		private class CatalogObservable extends Observable {
 			public void tellOfAction(String action) {
 				logger.info("notifiying observers!");
 				logger.info("num observers" + this.countObservers());
 				setChanged();
 				notifyObservers(action);
 			}
 		}
 
 		private String[] interfaceIgnoreList;
 		private CatalogObservable observable;
 
 		public CatalogerInfo() {
 			super();
 			interfaceIgnoreList = new String[6];
 			interfaceIgnoreList[0] = "sorcer.core.provider.Provider";
 			interfaceIgnoreList[1] = "sorcer.core.AdministratableProvider";
 			interfaceIgnoreList[2] = "java.rmi.Remote";
 			interfaceIgnoreList[3] = "net.jini.core.constraint.RemoteMethodControl";
 			interfaceIgnoreList[4] = "net.jini.security.proxytrust.TrustEquivalence";
 			interfaceIgnoreList[5] = "sorcer.service.RemoteTasker";
 			observable = new CatalogObservable();
 		}
 
 		public void setCataloger(Cataloger cataloger) {
 			this.cataloger = cataloger;
 		}
 
 		public void addObserver(Observer observer) {
 			// lookupMgr.setGUIBrowser(model);
 			observable.addObserver(observer);
 		}
 		
 		public List<ServiceItem> put(InterfaceList key, List<ServiceItem> value) {
 			return null;
 		}
 
 		public List<ServiceItem> get(InterfaceList interfaceList) {
 			for (InterfaceList keyToGet : keySet()) {
 				InterfaceList list = (InterfaceList) keyToGet;
 				if (list.containsAllInterfaces(interfaceList)) {
                     logger.info("Cataloger found matching interface list: " + Arrays.toString(list.toArray()));
 
 					return super.get(keyToGet);
 				}
 			}
 			return null;
 		}
 
 		public List<ServiceItem> getAll(InterfaceList interfaceList) {
 			List<ServiceItem> sItems = new ArrayList<ServiceItem>();
 			for (InterfaceList keyToGet : keySet()) {
 				InterfaceList list = keyToGet;
 				if (list.containsAllInterfaces(interfaceList)) {
 					logger.info("Cataloger found matching interface list: " + list);
 					sItems.addAll(super.get(keyToGet));
 				}
 			}
 			return sItems;
 		}
 
 		public void addServiceItem(ServiceItem sItem) {
 			InterfaceList keyList = new InterfaceList(sItem.service.getClass()
 					.getInterfaces());
 			List<ServiceItem> sItems = super.get(keyList);
 			if (sItems == null)
 				sItems = new Vector();
 			// better heuristics
 			// add it to the head assuming the tail's busy
 			if (!sItems.contains(sItem)) {
 				sItems.add(0, sItem);
 				super.put(keyList, sItems);
 			}
 			logger.info("adding new service, calling notifiy");
 			observable.tellOfAction("UPDATEDPLEASE");
 		}
 
 		public void removeServiceItem(ServiceItem sItem) {
 			InterfaceList searchInterfaceList = new InterfaceList(sItem.service
 					.getClass().getInterfaces());
 			InterfaceList key;
 			List<ServiceItem> value;
 
 			for (Enumeration e = keys(); e.hasMoreElements();) {
 				key = (InterfaceList) e.nextElement();
 				if (key.containsAllInterfaces(searchInterfaceList)) {
 					value = get(key);
 					if (value != null) {
 						if (value.size() == 1)
 							remove(key);
 						else
 							removeFrom(value, sItem);
 					}
 				}
 			}
 			observable.tellOfAction("UPDATEDPLEASE");
 		}
 
 		private void removeFrom(List<ServiceItem> sis, ServiceItem si) {
 			if (si.service == null)
 				return;
 			for (int i = 0; i < sis.size(); i++)
 				if (si.service.equals(((ServiceItem) sis.get(i)).service)) {
 					sis.remove(i);
 					return;
 				}
 		}
 
 		public String toString() {
 			StringBuilder sb = new StringBuilder();
 			InterfaceList key;
 			List<ServiceItem> sItems;
 			Iterator<InterfaceList> it = keySet().iterator();
 			while (it.hasNext()) {
 				key = it.next();
 				sItems = get(key);
 				sb.append("\n");
 				if (sItems != null && sItems.size() > 0
 						&& sItems.get(0) != null) {
 					if (sItems.get(0).attributeSets[0] instanceof Name)
 						sb.append(((Name) (sItems.get(0).attributeSets[0])).name);
 					else
 						sb.append(sItems.get(0).attributeSets[0]);
 
 					for (int i = 1; i < sItems.size(); i++) {
 						if (sItems.get(i).attributeSets[0] instanceof Name)
 							sb.append(",")
 									.append(((Name) (sItems
 											.get(i).attributeSets[0])).name);
 						else
 							sb.append(",")
 									.append(((ServiceItem) sItems.get(i)).attributeSets[0]);
 					}
 					sb.append("==>\n");
 					sb.append(key);
 				}
 			}
 			return sb.toString();
 		}
 
 		/**
 		 * The caller of this method must follow this generic protocol
 		 * <p>
 		 * first parameter = String[] of interfaces<br>
 		 * second parameter = providerName if any
 		 * <p>
 		 * This method provides automatic load balancing by providing the
 		 * serviceItem from the beginning and by removing it and adding to the
 		 * end upon each request.
 		 */
 		public ServiceItem getServiceItem(Class[] interfaces,
 				String providerName) {
 			List<ServiceItem> list = getAll(new InterfaceList(interfaces));
 			logger.info("Cinfo getServiceItem, got: " + list);
 			if (providerName != null && providerName.equals(ANY))
 				providerName = null;
 			if (list == null)
 				return null;
 
 			ServiceItem sItem;
 			// provide load balancing and check if still alive
 			if (providerName == null || providerName.length() == 0) {
 				do {
 					if (list.size() == 0)
 						return null;
 					sItem = list.remove(0);
 					if (sItem != null) {
 						if (isAlive(sItem)) {
 							list.add(sItem);
 							return sItem;
 						} else {
 							// not Alive anymore removing from cataloger
 							// removeServiceItem(sItem);
 						}
 					}
 				} while (sItem != null);
 			} else {
 				net.jini.core.entry.Entry[] attrs;
 				for (int i = 0; i < list.size(); i++) {
 					attrs = list.get(i).attributeSets;
 					for (net.jini.core.entry.Entry et : attrs) {
 						if (et instanceof Name
 								&& providerName.equals(((Name) et).name)) {
 							sItem = list.remove(i);
 							list.add(sItem);
 							return sItem;
 						}
 					}
 				}
 			}
 			return null;
 		}
 
 		// there's no other better way of doing this because of the structure we
 		// maintain.
 		// we need to iterate through each and every one of the list and get the
 		// service
 		public ServiceItem getServiceItem(ServiceID serviceID) {
 			Collection<List<ServiceItem>> c = values();
 			if (c == null)
 				return null;
 			List<ServiceItem> sItems;
 			ServiceItem sItem;
 			for (Iterator<List<ServiceItem>> it = c.iterator(); it.hasNext();) {
 				sItems = it.next();
 				for (int i = 0; i < sItems.size(); i++) {
 					if (serviceID
 							.equals((sItems.get(i)).serviceID)) {
 						sItem = sItems.remove(i);
 						sItems.add(sItem);
 						return sItem;
 					}
 				}
 			}
 			return null;
 		}
 
 		public ServiceItem[] getServiceItems(Class[] interfaces,
 				String providerName, int maxItems) {
 			// if maxItems is less or 0 then get all possible ServiceItems
 			if (maxItems <= 0)
 				maxItems = Integer.MAX_VALUE;
 			if (providerName != null && providerName.equals(ANY))
 				providerName = null;
 			// logger.fine("Looking for interfaces: " + interfaces);
 			List<ServiceItem> list = get(new InterfaceList(interfaces));
 			// logger.fine("Got list: " + list.toString());
 			if (list == null)
 				return null;
 
 			ServiceItem sItem;
 			// provide load balancing
 			if (providerName == null || "".equals(providerName)) {
 				sItem = list.remove(0);
 				list.add(sItem);
 				ArrayList<ServiceItem> arItems = new ArrayList<ServiceItem>();
 				Iterator<ServiceItem> it = list.iterator();
 				while (it.hasNext() && arItems.size() < maxItems) {
 					// Check if provider is still alive
 					ServiceItem si = it.next();
 					if (isAlive(si) && (!arItems.contains(si)))
 						arItems.add(si);
 				}
 				ServiceItem[] sitems = new ServiceItem[arItems.size()];
 				for (int i = 0; i < arItems.size(); i++) {
 					sitems[i] = arItems.get(i);
 				}
 				return sitems;
 			} else {
 				net.jini.core.entry.Entry[] attrs;
 				List<ServiceItem> slist = new ArrayList<ServiceItem>();
 				Iterator<ServiceItem> it = list.iterator();
 				ServiceItem si = null;
 				while (it.hasNext() && slist.size() < maxItems) {
 					si = it.next();
 					attrs = si.attributeSets;
 					if (attrs != null && attrs.length > 0
 							&& (attrs[0] instanceof Name)
 							&& providerName.equals(((Name) attrs[0]).name)) {
 						if (isAlive(si) && (!slist.contains(si))) {
 							slist.add(si);
 						}
 					}
 				}
 				ServiceItem[] sitems = new ServiceItem[slist.size()];
 				for (int i = 0; i < slist.size(); i++) {
 					sitems[i] = slist.get(i);
 				}
 				return sitems;
 			}
 		}
 
 		/**
 		 * Tests if provider is still alive.
 		 * 
 		 * @param si service to check
 		 * @return true if a provider is alive, otherwise false
 		 */
 		private static boolean isAlive(ServiceItem si) {
 			if (si == null)
 				return false;
 			try {
 				String name = ((Provider) si.service).getProviderName();
                 return name != null;
 			} catch (RemoteException e) {
 				logger.warning("Service ID: " + si.serviceID
 						+ " is not Alive anymore");
 				// throw e;
 				return false;
 			}
 		}
 
 		public Map<String, String> getProviderMethods() throws RemoteException {
 			logger.info("Inside GetProviderMethods");
 			observable.tellOfAction("UPDATEDPLEASEPM");
 			Map<String, String> map = new HashMap<String, String>();
 			Collection<List<ServiceItem>> c = values();
 			Type[] clazz;
 			Object service;
 			String serviceName = null;
 			net.jini.core.entry.Entry[] attributes;
 			for (List<ServiceItem> sItems : c) {
 				// get the first service proxy
 				service = sItems.get(0).service;
 				// get proxy interfaces
 				clazz = service.getClass().getInterfaces();
 				attributes = sItems.get(0).attributeSets;
 				for (int i = 0; i < attributes.length; i++) {
 					if (service instanceof Proxy) {
 						if (attributes[i] instanceof Name) {
 							serviceName = ((Name) attributes[i]).name;
 							break;
 						}
 					} else
 						serviceName = service.getClass().getName();
 				}
 				// list only interfaces of the Service type in package name
 				if (service instanceof Service) {
 					if (map.get(serviceName) == null) {
 						map.put(serviceName, StringUtils.arrayToString(clazz)
 								+ ";;" + StringUtils.arrayToString(attributes));
 					}
 				}
 			}
 			logger.info("getProviderMethods>>map:\n" + map);
 			return map;
 		}
 
 		/**
 		 * Returns the list of providers for a given provider name.
 		 * 
 		 * @return
 		 * @throws RemoteException
 		 */
 		public String[] getInterfaceList(String providerName)
 				throws RemoteException {
 			Collection<List<ServiceItem>> c = cinfo.values();
 			if (c == null) {
 				return new String[0];
 			}
 			Type[] interfaceList;
 			Object service;
 			String serviceName = null;
 			net.jini.core.entry.Entry[] attributes;
 			Set<InterfaceList> set = keySet();
 			for (InterfaceList key : set) {
 				List<ServiceItem> sItems = get(key);
 				for (int k = 0; k < sItems.size(); k++) {
 					service = sItems.get(k).service;
 					// logger.info(".........  attributes: "+ service);
 					attributes = sItems.get(k).attributeSets;
 					for (int i = 0; i < attributes.length; i++) {
 						if (service instanceof Proxy) {
 							if (attributes[i] instanceof Name) {
 								serviceName = ((Name) attributes[i]).name;
 								break;
 							}
 						} else
 							serviceName = service.getClass().getName();
 					}
 					if (serviceName.equals(providerName)) {
 
 						interfaceList = service.getClass().getInterfaces();
 
 						int count = 0;
 						for (int i = 0; i < interfaceList.length; i++) {
 							// logger.info("interface "+interfaceList[i].toString());
 							String currentInterface = interfaceList[i]
 									.toString().substring(10); // remove the
 							// interface
 							// part!
 							boolean onList = false;
 							for (int j = 0; j < interfaceIgnoreList.length; j++) {
 								if (currentInterface
 										.equals(interfaceIgnoreList[j])) {
 									onList = true;
 									break;
 								}
 							}
 							if (!onList)
 								count++;
 
 						}
 						String[] toReturn = new String[count];
 						count = 0;
 						for (int i = 0; i < interfaceList.length; i++) {
 							// logger.info("interface "+interfaceList[i].toString());
 							String currentInterface = interfaceList[i]
 									.toString().substring(10); // remove the
 							// interface part!
 							boolean onList = false;
 							for (int j = 0; j < interfaceIgnoreList.length; j++) {
 								if (currentInterface
 										.equals(interfaceIgnoreList[j])) {
 									onList = true;
 									break;
 								}
 							}
 							if (!onList) {
 								toReturn[count] = currentInterface;
 								count++;
 							}
 
 						}
 						return toReturn;
 					}
 				}
 			}
 			return new String[0];
 		}
 
 		/**
 		 * Get provider list is a method to get a hashmap with the list of
 		 * providers and their service id.
 		 * 
 		 * @return
 		 * @throws RemoteException
 		 */
 		public String[] getMethodsList(String providerName, String interfaceName)
 				throws RemoteException {
 			logger.info("Inside Get Methods List");
 			Collection<List<ServiceItem>> c = values();
 			List<ServiceItem> sItems;
 
 			Class[] interfaceList;
 			Object service;
 			String serviceName = null;
 			net.jini.core.entry.Entry[] attributes;
 			logger.info("Provider Name " + providerName + " interface name "
 					+ interfaceName);
 			for (InterfaceList key : keySet()) {
 				sItems = get(key);
 				for (int k = 0; k < sItems.size(); k++) {
 					service = sItems.get(k).service;
 					attributes = sItems.get(k).attributeSets;
 					for (int i = 0; i < attributes.length; i++) {
 						if (service instanceof Proxy) {
 							if (attributes[i] instanceof Name) {
 								serviceName = ((Name) attributes[i]).name;
 								break;
 							}
 						} else
 							serviceName = service.getClass().getName();
 					}
 					if (serviceName.equals(providerName)) {
 						interfaceList = service.getClass().getInterfaces();
 						for (int i = 0; i < interfaceList.length; i++) {
 							if (interfaceList[i].toString().equals(
 									interfaceName)) {
 								logger.info("Found interface" + interfaceName);
 								Method methods[] = interfaceList[i]
 										.getMethods();
 								logger.info("Methods Found" + methods.length);
 								String meths[] = new String[methods.length];
 								for (int j = 0; j < methods.length; j++) {
 									meths[j] = methods[j].getName();
 								}
                                 Set<String> setTemp = new HashSet(Arrays.asList(meths));
                                 return setTemp.toArray(new String[setTemp.size()]);
 							}
 
 						}
 
 					}
 				}
 			}
 			return new String[0];
 		}
 
 		/**
 		 * Get provider list is a method to get a map with the list of
 		 * providers and their service id.
 		 * 
 		 * @return
 		 * @throws RemoteException
 		 */
 		public Context getContext(String providerName, String interfaceName,
 				String methodName) throws RemoteException {
 			logger.info("Inside Provider");
 			Collection<List<ServiceItem>> c = values();
 			if (c == null) {
 				logger.info("Values is Null");
 				return null;
 			}
 			List<ServiceItem> sItems;
 			Object service;
 			String serviceName = null;
 			net.jini.core.entry.Entry[] attributes;
 			logger.info("Provider Name " + providerName + " interface name "
 					+ interfaceName);
 			InterfaceList key;
 			for (Iterator<InterfaceList> it = keySet().iterator(); it.hasNext();) {
 				key = it.next();
 				sItems = get(key);
 				for (int j = 0; j < sItems.size(); j++) {
 					service = sItems.get(j).service;
 					// >>>>>>>>>>>> attributes: "+ service);
 					attributes = sItems.get(j).attributeSets;
 					for (int i = 0; i < attributes.length; i++) {
 						if (service instanceof Proxy) {
 							if (attributes[i] instanceof Name) {
 								serviceName = ((Name) attributes[i]).name;
 								break;
 							}
 						} else
 							serviceName = service.getClass().getName();
 					}
 					if (serviceName.equals(providerName)) {
                         if (service instanceof ContextManagement) {
 							logger.info("service is a provider!");
 							try {
 								ContextManagement temp = (ContextManagement) service;
                                 return temp.getMethodContext(
                                         interfaceName, methodName);
 							} catch (Exception e) {
 								logger.info("error converting to provider"
 										+ e.getMessage());
 							}
 						}
 					}
 				}
 			}
 			return null;
 		}
 
 		public Boolean saveContext(String providerName, String interfaceName,
 				String methodName, Context theContext) throws RemoteException {
 			logger.info("Inside save dataContext");
 			Collection<List<ServiceItem>> c = values();
 			if (c == null) {
 				return null;
 			}
 			List<ServiceItem> sItems;
 			Object service;
 			String serviceName = null;
 			net.jini.core.entry.Entry[] attributes;
 			InterfaceList key;
 			for (Iterator<InterfaceList> it = keySet().iterator(); it.hasNext();) {
 				key = it.next();
 				sItems = get(key);
 				// sItems = (Vector) it.next();
 				for (int j = 0; j < sItems.size(); j++) {
 					service = sItems.get(j).service;
 					attributes = sItems.get(j).attributeSets;
 					for (int i = 0; i < attributes.length; i++) {
 						if (service instanceof Proxy) {
 							if (attributes[i] instanceof Name) {
 								serviceName = ((Name) attributes[i]).name;
 								break;
 							}
 						} else
 							serviceName = service.getClass().getName();
 					}
 					if (serviceName.equals(providerName)) {
                         if (service instanceof Provider) {
 							logger.info("service is a provider!");
 							try {
 								ServiceProvider temp = (ServiceProvider) service;
 								temp.saveMethodContext(interfaceName,
 										methodName, theContext);
 								return true;
 							} catch (Exception e) {
 								logger.info("error converting to provider"
 										+ e.getMessage());
 							}
 						}
 					}
 				}
 			}
 			return null;
 		}
 
 		public Boolean deleteContext(String providerName, String interfaceName,
 				String methodName) {
 			logger.info("Inside delete dataContext");
 			Collection<List<ServiceItem>> c = values();
 			if (c == null) {
 				logger.info("Values is Null");
 				return false;
 			}
 			List<ServiceItem> sItems;
 			Object service;
 			String serviceName = null;
 			net.jini.core.entry.Entry[] attributes;
 			InterfaceList key;
 			for (Iterator<InterfaceList> it = keySet().iterator(); it.hasNext();) {
 				key = it.next();
 				sItems = get(key);
 				for (int j = 0; j < sItems.size(); j++) {
 					service = sItems.get(j).service;
 					attributes = sItems.get(j).attributeSets;
 					for (int i = 0; i < attributes.length; i++) {
 						if (service instanceof Proxy) {
 							if (attributes[i] instanceof Name) {
 								serviceName = ((Name) attributes[i]).name;
 								break;
 							}
 						} else
 							serviceName = service.getClass().getName();
 					}
 					if (serviceName.equals(providerName)) {
 						if (service instanceof Provider) {
 							logger.info("service is a provider!");
 							try {
 								ServiceProvider temp = (ServiceProvider) service;
 								return temp.deleteContext(interfaceName,
 										methodName);
 							} catch (Exception e) {
 								logger.info("error converting to provider"
 										+ e.getMessage());
 							}
 						}
 					}
 				}
 			}
 			return false;
 		}
 
 		public String[] getSavedContextList(String providerName,
 				String interfaceName) {
 			logger.info("Inside get dataContext list");
 			Collection c = values();
 			if (c == null) {
 				return new String[0];
 			}
 			List<ServiceItem> sItems;
 			Object service;
 			String serviceName = null;
 			net.jini.core.entry.Entry[] attributes;
 			logger.info("Provider Name " + providerName + " interface name "
 					+ interfaceName);
 			InterfaceList key;
 			for (Iterator<InterfaceList> it = keySet().iterator(); it.hasNext();) {
 				key = it.next();
 				sItems = get(key);
 				for (int j = 0; j < sItems.size(); j++) {
 					service = sItems.get(j).service;
 					attributes = sItems.get(j).attributeSets;
 					for (int i = 0; i < attributes.length; i++) {
 						if (service instanceof Proxy) {
 							if (attributes[i] instanceof Name) {
 								serviceName = ((Name) attributes[i]).name;
 								break;
 							}
 						} else
 							serviceName = service.getClass().getName();
 					}
 					if (serviceName.equals(providerName)) {
 						if (service instanceof Provider) {
 							logger.info("service is a provider!");
 							try {
 								ServiceProvider temp = (ServiceProvider) service;
 								return temp.currentContextList(interfaceName);
 							} catch (Exception e) {
 								logger.info("error converting to provider"
 										+ e.getMessage());
 							}
 						}
 					}
 				}
 			}
 			return new String[0];
 		}
 
 		public Context exertService(String providerName, Class serviceType,
 				String methodName, Context theContext) throws RemoteException {
 			Collection<List<ServiceItem>> c = values();
 			if (c == null) {
 				return null;
 			}
 			List<ServiceItem> sItems;
 			Object service;
 			String serviceName = null;
 			net.jini.core.entry.Entry[] attributes;
 			InterfaceList key;
 			for (Iterator<InterfaceList> it = keySet().iterator(); it.hasNext();) {
 				key = it.next();
 				sItems = get(key);
 				for (int j = 0; j < sItems.size(); j++) {
 					service = sItems.get(j).service;
 					attributes = sItems.get(j).attributeSets;
 					for (int i = 0; i < attributes.length; i++) {
 						if (service instanceof Proxy) {
 							if (attributes[i] instanceof Name) {
 								serviceName = ((Name) attributes[i]).name;
 								break;
 							}
 						} else
 							serviceName = service.getClass().getName();
 					}
 					if (serviceName.equals(providerName)) {
 						if (service instanceof sorcer.core.provider.Provider) {
 							logger.info("service is a provider!");
 							try {
 								sorcer.core.provider.Provider temp = (sorcer.core.provider.Provider) service;
 								NetSignature method = new NetSignature(
 										methodName, serviceType);
 								Task task = new NetTask(serviceType
 										+ methodName, method);
 								task.setContext(theContext);
 								NetTask task2 = (NetTask) ((Service) temp)
 										.service(task, null);
 								return task2.getContext();
 							} catch (Exception e) {
 								logger.info("error converting to provider"
 										+ e.getMessage());
 							}
 						}
 					}
 				}
 			}
 			return null;
 		}
 
 		private List<String> getSelectorList(Class serviceType) {
 			Method[] methods = serviceType.getMethods();
 			List<String> selectors = new ArrayList<String>(methods.length);
 			for (int k = 0; k < methods.length; k++)
 				selectors.add(methods[k].getName());
 			return selectors;
 		}
 
 		/**
 		 * Returns a list of selectors of <code>serviceType</code> along with
 		 * hash tables per each interface. The hash table key is an interface
 		 * name and the value is a list of all interface selectors.
 		 * 
 		 * @param serviceType
 		 * @return list of selectors of serviceType and its superinterface hash
 		 *         tables
 		 */
 		private List<String> getInterfaceList(Class serviceType) {
 			Type type;
 			Type[] clazz = serviceType.getGenericInterfaces();
 			if (clazz.length == 0) {
 				return getSelectorList(serviceType);
 			}
 			ArrayList ilist = new ArrayList();
 			ilist.addAll(getSelectorList(serviceType));
 			Map<String, List<String>> imap;
 			for (int j = 0; j < clazz.length; j++) {
 				type = clazz[j];
 				imap = new HashMap();
 				imap.put(((Class) type).getName(),
 						getInterfaceList((Class) type));
 				ilist.add(imap);
 			}
 			return ilist;
 		}
 
 		/**
 		 * Returns a list of maps per each interface in the array clazz
 		 * such that they are registered by the service provider as sorcerTypes.
 		 * The hash table key is an interface name and the value is a list of
 		 * all interface selectors.
 		 * 
 		 * @param clazz
 		 *            the list of all types
 		 * @param sorcerTypes
 		 *            the registered types by a service provider
 		 * @return the list of superinterface hash tables
 		 */
 		private List<Map<String, List<String>>> getInterfaceList(Type[] clazz, List<String> sorcerTypes) {
 			Type serviceType;
 			ArrayList ilist = new ArrayList();
 			Map<String, List<String>> imap;
 			for (int j = 0; j < clazz.length; j++) {
 				serviceType = clazz[j];
 				if (sorcerTypes != null
 						&& sorcerTypes
 								.contains(((Class)serviceType).getName())) {
 					imap = new HashMap<String, List<String>>();
 					imap.put(((Class)serviceType).getName(),
 							getSelectorList((Class)serviceType));
 					ilist.add(imap);
 				}
 			}
 			return ilist;
 		}
 
 		public Context getContext(Class serviceType, String method) {
 			InterfaceList iList = new InterfaceList(new Class[] { serviceType });
 			List<ServiceItem> v = get(iList);
 			if (v == null || v.size() == 0)
 				return null;
 			net.jini.core.entry.Entry[] attributeSets = v.get(0).attributeSets;
 			SorcerServiceInfo socSrvType = null;
 			for (int i = 0; i < attributeSets.length; i++)
 				if (attributeSets[i] instanceof SorcerServiceInfo) {
 					socSrvType = (SorcerServiceInfo) attributeSets[i];
 					break;
 				}
 			// return (socSrvType != null) ? socSrvType.getMethodContext(method)
 			// : null;
 			return null;
 		}
 
 		/**
 		 * See above CatalogerInfo for comments.
 		 */
 		public static class InterfaceList extends ArrayList<Class> {
 			private static final long serialVersionUID = 1L;
 
 			public InterfaceList(Class[] clazz) {
 				if (clazz != null && clazz.length > 0)
 					for (int i = 0; i < clazz.length; i++)
 						add(clazz[i]);
 			}
 
 			public boolean containsAllInterfaces(InterfaceList interfaceList) {
 				Set<Class> all = new HashSet<Class>(this);
 				for (int i = 0; i < size(); i++) {
 					all.addAll(Arrays.asList(get(i).getInterfaces()));
 				}
 				return getServiceList(all).containsAll(
 						getServiceList(interfaceList));
 			}
 
 			private List<String> getServiceList(
 					Collection<Class> interfaceCollection) {
 				List<String> serviceList = new ArrayList<String>();
 				for (Class service : interfaceCollection) {
 					serviceList.add("" + service);
 				}
 				return serviceList;
 			}
 
 			public boolean equals(InterfaceList otherList) {
 				return this.containsAllInterfaces(otherList)
 						&& otherList.containsAllInterfaces(this);
 			}
 		}// end of InterfaceList
 	}// end of CatalogerInfo
 
 	// As these are not remote listeners, and the CatalogerInfo is thread safe,
 	// it's not important to spawn a new thread for each change in the service.
 	protected static class CatalogerEventListener implements
 			ServiceDiscoveryListener, Runnable {
 		String msg;
 
 		public CatalogerEventListener() {
 		}
 
 		public CatalogerEventListener(String msg) {
 			this.msg = msg;
 		}
 
 		public void serviceAdded(ServiceDiscoveryEvent ev) {
 			cinfo.addServiceItem(ev.getPostEventServiceItem());
 			refreshScreen("++++ SERVICE ADDED ++++");
 		}
 
 		public void serviceRemoved(ServiceDiscoveryEvent ev) {
 			refreshScreen("++++ SERVICE REMOVING ++++");
 			cinfo.removeServiceItem(ev.getPreEventServiceItem());
 			refreshScreen("++++ SERVICE REMOVED ++++");
 		}
 
 		public void serviceChanged(ServiceDiscoveryEvent ev) {
 			ServiceItem pre = ev.getPreEventServiceItem();
 			ServiceItem post = ev.getPostEventServiceItem();
 
 			// This should not happen
 			if (pre == null && post == null) {
 				logger.info(">>serviceChanged::Null serviceItem! ? ");
 				return;
 			} else if (pre.service == null && post.service != null) {
 				logger.info(">>serviceChanged::The proxy's service is now not null \n");
 				logger.info(">>serviceChanged::Proxy later: post.service ("
 						+ post.service.getClass().getName() + ")\n");
 			} else if (pre.service != null && post.service == null) {
 				logger.info(">>serviceChanged::The service's proxy has become null::check codebase problem");
 				logger.info(">>serviceChanged::Proxy later: pre.service ("
 						+ pre.service.getClass().getName() + ")\n");
 				cinfo.remove(post);
 			} else {
 				logger.info("Service attribute has changed pre=" + pre
 						+ " post=" + post);
 				cinfo.remove(pre);
 				cinfo.addServiceItem(post);
 			}
 			refreshScreen("++++ SERVICE CHANGED ++++");
 		}
 
 		private void refreshScreen(String msg) {
 			new Thread(new CatalogerEventListener(msg)).start();
 		}
 
 		public void run() {
 			if (cinfo != null) {
 				StringBuilder buffer = new StringBuilder(msg).append("\n");
 				buffer.append(cinfo).append("\n");
 				logger.info(buffer.toString());
 			}
 		}
 	}
 
 	/**
 	 * Returns the service Provider from an item matching the template, or null
 	 * if there is no match. If multiple items match the template, it is
 	 * arbitrary as to which service object is returned. If the returned object
 	 * cannot be deserialized, an UnmarshalException is thrown with the standard
 	 * RMI semantics.
 	 * 
 	 * @param tmpl
 	 *            - the template to match
 	 * @return an object that represents a service that matches the specified
 	 *         template
 	 * @throws RemoteException
 	 */
 	public Object lookup(ServiceTemplate tmpl) throws RemoteException {
 		// TODO
 		return null;
 	}
 
 	/**
 	 * Returns at most maxMatches items matching the template, plus the total
 	 * number of items that match the template. The return value is never null,
 	 * and the returned items array is only null if maxMatches is zero. For each
 	 * returned item, if the service object cannot be deserialized, the service
 	 * field of the item is set to null and no exception is thrown. Similarly,
 	 * if an attribute set cannot be deserialized, that element of the
 	 * attributeSets array is set to null and no exception is thrown.
 	 * 
 	 * @param tmpl
 	 *            - the template to match
 	 * @param maxMatches
 	 * @return a ServiceMatches instance that contains at most maxMatches items
 	 *         matching the template, plus the total number of items that match
 	 *         the template. The return value is never null, and the returned
 	 *         items array is only null if maxMatches is zero.
 	 * @throws RemoteException
 	 */
 	public ServiceMatches lookup(ServiceTemplate tmpl, int maxMatches)
 			throws RemoteException {
         List<ServiceItem> result = new LinkedList<ServiceItem>();
         for (Map.Entry<CatalogerInfo.InterfaceList, List<ServiceItem>> entry : cinfo.entrySet()) {
             List<ServiceItem> serviceItems = entry.getValue();
             SRVITEM:
             for (ServiceItem serviceItem : serviceItems) {
                 if (tmpl.serviceID != null && tmpl.serviceID.equals(serviceItem.serviceID)) {
                     result.add(serviceItem);
                     //serviceID is unique, stop on first matching service
                     break;
                 }
 
                 CatalogerInfo.InterfaceList interfaceList = entry.getKey();
                 if (interfaceList.containsAll(Arrays.asList(tmpl.serviceTypes))) {
                     List<Entry> sItemEntryList = Arrays.asList(serviceItem.attributeSets);
                     for (Entry attr : tmpl.attributeSetTemplates) {
                         if (!sItemEntryList.contains(attr)) {
                             continue SRVITEM;
                         }
                     }
                     result.add(serviceItem);
                     if (result.size() >= maxMatches) break;
                 }
             }
         }
         return new ServiceMatches(result.toArray(new ServiceItem[result.size()]), result.size());
     }
 	public String returnString() throws RemoteException {
 		return getClass().getName() + ":" + getProviderName();
 	}
 
 }
