 /*******************************************************************************
  * Copyright (c) 2010 Oracle.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution. 
  * The Eclipse Public License is available at
  *     http://www.eclipse.org/legal/epl-v10.html
  * and the Apache License v2.0 is available at 
  *     http://www.opensource.org/licenses/apache2.0.php.
  * You may elect to redistribute this code under either of these licenses.
  *
  * Contributors:
  *     Hal Hildebrand - Initial JMX support 
  ******************************************************************************/
 
 package org.eclipse.gemini.mgmt.framework.codec;
 
 import static org.eclipse.gemini.mgmt.codec.Util.LongArrayFrom;
 import static org.eclipse.gemini.mgmt.codec.Util.getBundleExportedPackages;
 import static org.eclipse.gemini.mgmt.codec.Util.getBundleFragments;
 import static org.eclipse.gemini.mgmt.codec.Util.getBundleHeaders;
 import static org.eclipse.gemini.mgmt.codec.Util.getBundleImportedPackages;
 import static org.eclipse.gemini.mgmt.codec.Util.getBundleState;
 import static org.eclipse.gemini.mgmt.codec.Util.getBundlesRequiring;
 import static org.eclipse.gemini.mgmt.codec.Util.getDependencies;
 import static org.eclipse.gemini.mgmt.codec.Util.isBundleFragment;
 import static org.eclipse.gemini.mgmt.codec.Util.isBundlePersistentlyStarted;
 import static org.eclipse.gemini.mgmt.codec.Util.isBundleRequired;
 import static org.eclipse.gemini.mgmt.codec.Util.isRequiredBundleRemovalPending;
 import static org.eclipse.gemini.mgmt.codec.Util.longArrayFrom;
 import static org.eclipse.gemini.mgmt.codec.Util.serviceIds;
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.management.openmbean.CompositeData;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.management.openmbean.CompositeType;
 import javax.management.openmbean.OpenDataException;
 import javax.management.openmbean.TabularData;
 import javax.management.openmbean.TabularDataSupport;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 import org.osgi.jmx.Item;
 import org.osgi.jmx.framework.BundleStateMBean;
 import org.osgi.service.packageadmin.PackageAdmin;
 import org.osgi.service.startlevel.StartLevel;
 
 import org.eclipse.gemini.mgmt.codec.Util;
 import org.eclipse.gemini.mgmt.framework.CustomBundleStateMBean;
 
 
 /**
  * <p>
  * This class represents the CODEC for the composite data representing a single
  * OSGi <link>Bundle</link>.
  * <p>
  * It serves as both the documentation of the type structure and as the
  * codification of the mechanism to convert to/from the CompositeData.
  * <p>
  * The structure of the composite data is:
  * <table border="1">
  * <tr>
  * <td>Location</td>
  * <td>String</td>
  * </tr>
  * <tr>
  * <td>Identifier</td>
  * <td>long</td>
  * </tr>
  * <tr>
  * <td>SymbolicName</td>
  * <td>String</td>
  * </tr>
  * <tr>
  * <td>StartLevel</td>
  * <td>int</td>
  * </tr>
  * <tr>
  * <td>State</td>
  * <td>String</td>
  * </tr>
  * <tr>
  * <td>LastModified</td>
  * <td>long</td>
  * </tr>
  * <tr>
  * <td>PersistentlyStarted</td>
  * <td>boolean</td>
  * </tr>
  * <tr>
  * <td>RemovalPending</td>
  * <td>boolean</td>
  * </tr>
  * <tr>
  * <td>Required</td>
  * <td>boolean</td>
  * </tr>
  * <tr>
  * <td>Fragment</td>
  * <td>boolean</td>
  * </tr>
  * <tr>
  * <td>RegisteredServices</td>
  * <td>Array of long</td>
  * </tr>
  * <tr>
  * <td>ServicesInUse</td>
  * <td>Array of long</td>
  * </tr>
  * <tr>
  * <td>Headers</td>
  * <td>TabularData of Key/Value String pairs</td>
  * </tr>
  * <tr>
  * <td>ExportedPackages</td>
  * <td>Array of String</td>
  * </tr>
  * <tr>
  * <td>ImportedPackages</td>
  * <td>Array of String</td>
  * </tr>
  * <tr>
  * <td>Fragments</td>
  * <td>Array of long</td>
  * </tr>
  * <tr>
  * <td>Hosts</td>
  * <td>Array of long</td>
  * </tr>
  * <tr>
  * <td>RequiredBundles</td>
  * <td>Array of long</td>
  * </tr>
  * <tr>
  * <td>RequiringBundles</td>
  * <td>Array of long</td>
  * </tr>
  * </table>
  */
 public class OSGiBundle {
 	
 	private static final String VALUE = "Value";
 
 	private static final String KEY = "Key";
 
 	private static final String[] HEADER_PROPERTY_ITEM_NAMES = new String[] {
 			KEY, VALUE };
 	private BundleContext bc;
 	private PackageAdmin admin;
 	private StartLevel sl;
 	private Bundle b;
 
 	/**
 	 * Construct an OSGiBundle from the encoded CompositeData
 	 * 
 	 * @param data
 	 *            - the encoded representation of the bundle
 	 */
 	@SuppressWarnings("boxing")
 	public OSGiBundle(CompositeData data) {
 		this((String) data.get(BundleStateMBean.LOCATION), ((Long) data
 				.get(BundleStateMBean.IDENTIFIER)).longValue(), (String) data
 				.get(BundleStateMBean.SYMBOLIC_NAME), (String) data
 				.get(BundleStateMBean.VERSION), ((Integer) data
 				.get(BundleStateMBean.START_LEVEL)).intValue(), (String) data
 				.get(BundleStateMBean.STATE), ((Long) data
 				.get(BundleStateMBean.LAST_MODIFIED)).longValue(),
 				(Boolean) data.get(BundleStateMBean.PERSISTENTLY_STARTED),
 				(Boolean) data.get(BundleStateMBean.REMOVAL_PENDING),
 				(Boolean) data.get(BundleStateMBean.REQUIRED), (Boolean) data
 						.get(BundleStateMBean.FRAGMENT),
 				longArrayFrom((Long[]) data
 						.get(BundleStateMBean.REGISTERED_SERVICES)),
 				longArrayFrom((Long[]) data
 						.get(BundleStateMBean.SERVICES_IN_USE)),
 				mapFrom((TabularData) data.get(BundleStateMBean.HEADERS)),
 				(String[]) data.get(BundleStateMBean.EXPORTED_PACKAGES),
 				(String[]) data.get(BundleStateMBean.IMPORTED_PACKAGES),
 				longArrayFrom((Long[]) data.get(BundleStateMBean.FRAGMENTS)),
 				longArrayFrom((Long[]) data.get(BundleStateMBean.HOSTS)),
 				longArrayFrom((Long[]) data
 						.get(BundleStateMBean.REQUIRED_BUNDLES)),
 				longArrayFrom((Long[]) data
 						.get(BundleStateMBean.REQUIRING_BUNDLES)));
 
 	}
 
 	/**
 	 * Construct an OSGiBundle representation
 	 * 
 	 * @param bc
 	 *            - the BundleContext to be used.
 	 * @param admin
 	 *            - the PackageAdmin service
 	 * @param sl
 	 *            - the StartLevel service
 	 * @param b
 	 *            - the Bundle to represent
 	 */
 	public OSGiBundle(BundleContext bc, PackageAdmin admin, StartLevel sl,
 			Bundle b) {
 		this.bc = bc;
 		this.admin = admin;
 		this.sl = sl;
 		this.b = b;
 
 	}
 
 	/**
 	 * Construct and OSGiBundle
 	 * 
 	 * @param location
 	 * @param identifier
 	 * @param symbolicName
 	 * @param version
 	 * @param startLevel
 	 * @param state
 	 * @param lastModified
 	 * @param persistentlyStarted
 	 * @param removalPending
 	 * @param required
 	 * @param fragment
 	 * @param registeredServices
 	 * @param servicesInUse
 	 * @param headers
 	 * @param exportedPackages
 	 * @param importedPackages
 	 * @param fragments
 	 * @param hosts
 	 * @param requiredBundles
 	 * @param requiringBundles
 	 */
 	public OSGiBundle(String location, long identifier, String symbolicName,
 			String version, int startLevel, String state, long lastModified,
 			boolean persistentlyStarted, boolean removalPending,
 			boolean required, boolean fragment, long[] registeredServices,
 			long[] servicesInUse, Map<String, String> headers,
 			String[] exportedPackages, String[] importedPackages,
 			long[] fragments, long[] hosts, long[] requiredBundles,
 			long[] requiringBundles) {
 		this.location = location;
 		this.identifier = identifier;
 		this.symbolicName = symbolicName;
 		this.version = version;
 		this.startLevel = startLevel;
 		this.state = state;
 		this.lastModified = lastModified;
 		this.persistentlyStarted = persistentlyStarted;
 		this.removalPending = removalPending;
 		this.required = required;
 		this.fragment = fragment;
 		this.registeredServices = registeredServices;
 		this.servicesInUse = servicesInUse;
 		this.headers = headers;
 		this.exportedPackages = exportedPackages;
 		this.importedPackages = importedPackages;
 		this.fragments = fragments;
 		this.hosts = hosts;
 		this.requiredBundles = requiredBundles;
 		this.requiringBundles = requiringBundles;
 	}
 
 	/**
 	 * Answer the TabularData representing the list of OSGiBundle state
 	 * 
 	 * @param bundles
 	 *            - the list of bundles to represent
 	 * @param mask 
 	 * 
 	 * @return the Tabular data which represents the list of bundles
 	 */
 	public static TabularData tableFrom(ArrayList<OSGiBundle> bundles, int mask) {
 		TabularDataSupport table = new TabularDataSupport(
 				Item.tabularType("BUNDLES", "A list of bundles",
 						OSGiBundle.computeBundleType(mask), new String[] { BundleStateMBean.IDENTIFIER }));
 		for (OSGiBundle bundle : bundles) {
 			table.put(bundle.asCompositeData(mask));
 		}
 		return table;
 	}
 	
 	private static CompositeType computeBundleType(int mask) {
 		List<Item> bundleTypes = new ArrayList<Item>();
 		if((mask | CustomBundleStateMBean.LOCATION) == mask) {
 		bundleTypes.add(BundleStateMBean.LOCATION_ITEM);
 		}
 //		if((mask | IDENTIFIER) == mask) {
 		bundleTypes.add(BundleStateMBean.IDENTIFIER_ITEM);
 //		}
 		if((mask | CustomBundleStateMBean.SYMBOLIC_NAME) == mask) {
 		bundleTypes.add(BundleStateMBean.SYMBOLIC_NAME_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.VERSION) == mask) {
 		bundleTypes.add(BundleStateMBean.VERSION_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.START_LEVEL) == mask) {
 		bundleTypes.add(BundleStateMBean.START_LEVEL_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.STATE) == mask) {
 		bundleTypes.add(BundleStateMBean.STATE_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.LAST_MODIFIED) == mask) {
 		bundleTypes.add(BundleStateMBean.LAST_MODIFIED_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.PERSISTENTLY_STARTED) == mask) {
 		bundleTypes.add(BundleStateMBean.PERSISTENTLY_STARTED_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.REMOVAL_PENDING) == mask) {
 		bundleTypes.add(BundleStateMBean.REMOVAL_PENDING_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.REQUIRED) == mask) {
 		bundleTypes.add(BundleStateMBean.REQUIRED_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.FRAGMENT) == mask) {
 		bundleTypes.add(BundleStateMBean.FRAGMENT_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.REGISTERED_SERVICES) == mask) {
 		bundleTypes.add(BundleStateMBean.REGISTERED_SERVICES_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.SERVICES_IN_USE) == mask) {
 		bundleTypes.add(BundleStateMBean.SERVICES_IN_USE_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.HEADERS) == mask) {
 		bundleTypes.add(BundleStateMBean.HEADERS_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.EXPORTED_PACKAGES) == mask) {
 		bundleTypes.add(BundleStateMBean.EXPORTED_PACKAGES_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.IMPORTED_PACKAGES) == mask) {
 		bundleTypes.add(BundleStateMBean.IMPORTED_PACKAGES_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.FRAGMENTS) == mask) {
 		bundleTypes.add(BundleStateMBean.FRAGMENTS_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.HOSTS) == mask) {
 		bundleTypes.add(BundleStateMBean.HOSTS_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.REQUIRING_BUNDLES) == mask) {
 		bundleTypes.add(BundleStateMBean.REQUIRING_BUNDLES_ITEM);
 		}
 		if((mask | CustomBundleStateMBean.REQUIRED_BUNDLES) == mask) {
 		bundleTypes.add(BundleStateMBean.REQUIRED_BUNDLES_ITEM);
 		}
 
 		CompositeType currentCompositeType = Item.compositeType("BUNDLE",
 				"This type encapsulates OSGi bundles",bundleTypes.toArray(new Item[]{}));
 		return currentCompositeType;
 	}
 
 	/**
 	 * Answer the TabularData representing the list of bundle headers for a
 	 * bundle
 	 * 
 	 * @param b
 	 * @return the bundle headers
 	 */
 	@SuppressWarnings("unchecked")
 	public static TabularData headerTable(Bundle b) {
 		TabularDataSupport table = new TabularDataSupport(
 				BundleStateMBean.HEADERS_TYPE);
 		Dictionary map = b.getHeaders();
 		for (Enumeration headers = map.keys(); headers.hasMoreElements();) {
 			String key = (String) headers.nextElement();
 			table.put(headerData(key, (String) map.get(key)));
 		}
 		return table;
 	}
 
 	/**
 	 * Answer the TabularData representing the supplied map of bundle headers
 	 * 
 	 * @param headers
 	 * @return the bundle headers
 	 */
 	public static TabularData headerTable(Map<String, String> headers) {
 		TabularDataSupport table = new TabularDataSupport(
 				BundleStateMBean.HEADERS_TYPE);
 		for (Map.Entry<String, String> entry : headers.entrySet()) {
 			table.put(headerData(entry.getKey(), entry.getValue()));
 		}
 		return table;
 	}
 
 	private static CompositeData headerData(String key, String value) {
 		Object[] itemValues = new Object[HEADER_PROPERTY_ITEM_NAMES.length];
 		itemValues[0] = key;
 		itemValues[1] = value;
 
 		try {
 			return new CompositeDataSupport(BundleStateMBean.HEADER_TYPE,
 					HEADER_PROPERTY_ITEM_NAMES, itemValues);
 		} catch (OpenDataException e) {
 			throw new IllegalStateException(
 					"Cannot form bundle header open data", e);
 		}
 	}
 
 	/**
 	 * Answer the receiver encoded as CompositeData
 	 * @param mask 
 	 * 
 	 * @return the CompositeData encoding of the receiver.
 	 */
 	@SuppressWarnings("boxing")
 	public CompositeData asCompositeData(int mask) {
 		Map<String, Object> items = new HashMap<String, Object>();
 		if((mask | CustomBundleStateMBean.LOCATION) == mask) {
 		items.put(BundleStateMBean.LOCATION, getLocation());
 		}
 //		if((mask | IDENTIFIER) == mask) {
 		items.put(BundleStateMBean.IDENTIFIER, getIdentifier());
 //		}
 		if((mask | CustomBundleStateMBean.SYMBOLIC_NAME) == mask) {
 		items.put(BundleStateMBean.SYMBOLIC_NAME, getSymbolicName());
 		}
 		if((mask | CustomBundleStateMBean.VERSION) == mask) {
 		items.put(BundleStateMBean.VERSION, getVersion());
 		}
 		if((mask | CustomBundleStateMBean.START_LEVEL) == mask) {
 		items.put(BundleStateMBean.START_LEVEL, getStartLevel());
 		}
 		if((mask | CustomBundleStateMBean.STATE) == mask) {
 		items.put(BundleStateMBean.STATE, getState());
 		}
 		if((mask | CustomBundleStateMBean.LAST_MODIFIED) == mask) {
 		items.put(BundleStateMBean.LAST_MODIFIED, getLastModified());
 		}
 		if((mask | CustomBundleStateMBean.PERSISTENTLY_STARTED) == mask) {
 		items.put(BundleStateMBean.PERSISTENTLY_STARTED, isPersistentlyStarted());
 		}
 		if((mask | CustomBundleStateMBean.REMOVAL_PENDING) == mask) {
 		items.put(BundleStateMBean.REMOVAL_PENDING, isRemovalPending());
 		}
 		if((mask | CustomBundleStateMBean.REQUIRED) == mask) {
 		items.put(BundleStateMBean.REQUIRED, isRequired());
 		}
 		if((mask | CustomBundleStateMBean.FRAGMENT) == mask) {
 		items.put(BundleStateMBean.FRAGMENT, isFragment());
 		}
 		if((mask | CustomBundleStateMBean.REGISTERED_SERVICES) == mask) {
 		items.put(BundleStateMBean.REGISTERED_SERVICES,
 				LongArrayFrom(getRegisteredServices()));
 		}
 		if((mask | CustomBundleStateMBean.SERVICES_IN_USE) == mask) {
 		items.put(BundleStateMBean.SERVICES_IN_USE,
 				LongArrayFrom(getServicesInUse()));
 		}
 		if((mask | CustomBundleStateMBean.HEADERS) == mask) {
 		items.put(BundleStateMBean.HEADERS, headerTable(getHeaders()));
 		}
 		if((mask | CustomBundleStateMBean.EXPORTED_PACKAGES) == mask) {
 		items.put(BundleStateMBean.EXPORTED_PACKAGES, getExportedPackages());
 		}
 		if((mask | CustomBundleStateMBean.IMPORTED_PACKAGES) == mask) {
 		items.put(BundleStateMBean.IMPORTED_PACKAGES, getImportedPackages());
 		}
 		if((mask | CustomBundleStateMBean.FRAGMENTS) == mask) {
 		items.put(BundleStateMBean.FRAGMENTS, LongArrayFrom(getFragments()));
 		}
 		if((mask | CustomBundleStateMBean.HOSTS) == mask) {
 		items.put(BundleStateMBean.HOSTS, LongArrayFrom(getHosts()));
 		}
 		if((mask | CustomBundleStateMBean.REQUIRING_BUNDLES) == mask) {
 		items.put(BundleStateMBean.REQUIRING_BUNDLES,
 				LongArrayFrom(getRequiringBundles()));
 		}
 		if((mask | CustomBundleStateMBean.REQUIRED_BUNDLES) == mask) {
 		items.put(BundleStateMBean.REQUIRED_BUNDLES,
 				LongArrayFrom(getRequiredBundles()));
 		}
 
 		try {
 			return new CompositeDataSupport(OSGiBundle.computeBundleType(mask), items);
 		} catch (OpenDataException e) {
 			throw new IllegalStateException("Cannot form bundle open data", e);
 		}
 	}
 
 	/**
 	 * @return The list of exported packages by this bundle, in the form of
 	 *         <packageName>;<version>
 	 * 
 	 */
 	public String[] getExportedPackages() {
 		if (exportedPackages == null) {
 			exportedPackages = getBundleExportedPackages(b, admin); 
 		}
 		return exportedPackages;
 	}
 
 	/**
 	 * @return the list of identifiers of the bundle fragments which use this
 	 *         bundle as a host
 	 */
 	public long[] getFragments() {
 		if (fragments == null) {
 			fragments = getBundleFragments(b, admin);			
 		}
 		return fragments;
 	}
 
 	/**
 	 * @return the map of headers for this bundle
 	 */
 	public Map<String, String> getHeaders() {
 		if (headers == null) {
 			headers = getBundleHeaders(b);
 		}
 		return headers;
 	}
 
 	/**
 	 * @return list of identifiers of the bundles which host this fragment
 	 */
 	public long[] getHosts() {
 		if (hosts == null) {
 			hosts = Util.bundleIds(admin.getHosts(b));
 		}
 		return hosts;
 	}
 
 	/**
 	 * @return the identifier of this bundle
 	 */
 	public long getIdentifier() {
 		if (identifier == 0) {
 			identifier = b.getBundleId();
 		}
 		return identifier;
 	}
 
 	/**
 	 * @return The list of imported packages by this bundle, in the form of
 	 *         <packageName>;<version>
 	 */
 	public String[] getImportedPackages() {
 		if (importedPackages == null) {
 			importedPackages = getBundleImportedPackages(b, bc, admin);
 		}
 		return importedPackages;
 	}
 
 	/**
 	 * @return the last modified time of this bundle
 	 */
 	public long getLastModified() {
 		if (lastModified == 0) {
 			lastModified = b.getLastModified();
 		}
 		return lastModified;
 	}
 
 	/**
 	 * @return the name of this bundle
 	 */
 	public String getLocation() {
 		if (location == null) {
 			location = b.getLocation();
 		}
 		return location;
 	}
 
 	/**
 	 * @return the list of identifiers of the services registered by this bundle
 	 */
 	public long[] getRegisteredServices() {
 		if (registeredServices == null) {
 			registeredServices = serviceIds(b.getRegisteredServices());
 		}
 		return registeredServices;
 	}
 
 	/**
 	 * @return the list of identifiers of bundles required by this bundle
 	 */
 	public long[] getRequiredBundles() {
 		if (requiredBundles == null) {
 			requiredBundles = getDependencies(b,admin);
 		}
 		return requiredBundles;
 	}
 
 	/**
 	 * @return the list of identifiers of bundles which require this bundle
 	 */
 	public long[] getRequiringBundles() {
 		if (requiringBundles == null) {
 			requiringBundles = getBundlesRequiring(b, bc, admin);
 		}
 		return requiringBundles;
 	}
 
 	/**
 	 * @return the list of identifiers of services in use by this bundle
 	 */
 	public long[] getServicesInUse() {
 		if (servicesInUse == null) {
 			servicesInUse = serviceIds(b.getServicesInUse());
 		}
 		return servicesInUse;
 	}
 
 	/**
 	 * @return the start level of this bundle
 	 */
 	public int getStartLevel() {
 		if (startLevel == 0) {
 			startLevel = sl.getBundleStartLevel(b);
 		}
 		return startLevel;
 	}
 
 	/**
 	 * @return the state of this bundle
 	 */
 	public String getState() {
 		if (state == null) {
 			state = getBundleState(b);
 		}
 		return state;
 	}
 
 	/**
 	 * @return the symbolic name of this bundle
 	 */
 	public String getSymbolicName() {
 		if (symbolicName == null) {
 			symbolicName = b.getSymbolicName();
 		}
 		return symbolicName;
 	}
 
 	/**
 	 * @return the version of this bundle
 	 */
 	public String getVersion() {
 		if (version == null) {
 			version = b.getVersion().toString();
 		}
 		return version;
 	}
 
 	/**
 	 * @return true if this bundle represents a fragment
 	 */
 	public boolean isFragment() {
 		if (fragment == null) {
 			fragment = isBundleFragment(b, admin);
 		}
 		return fragment;
 	}
 
 	/**
 	 * @return true if this bundle is persistently started
 	 */
 	public boolean isPersistentlyStarted() {
 		if (persistentlyStarted == null) {
 			persistentlyStarted = isBundlePersistentlyStarted(b, sl);
 		}
 		return persistentlyStarted;
 	}
 
 	/**
 	 * @return true if this bundle is pending removal
 	 */
 	public boolean isRemovalPending() {
 		if (removalPending == null) {
			removalPending = isRequiredBundleRemovalPending(b, bc, admin);
 		}
 		return removalPending;
 	}
 
 	/**
 	 * @return true if this bundle is required
 	 */
 	public boolean isRequired() {
 		if (required == null) {
 			required = isBundleRequired(b, bc, admin);
 		}
 		return required;
 	}
 
 	@SuppressWarnings( { "unchecked", "cast" })
 	private static Map<String, String> mapFrom(TabularData data) {
 		Map<String, String> headers = new HashMap<String, String>();
 		Set<List<?>> keySet = (Set<List<?>>) data.keySet();
 		for (List<?> key : keySet) {
 			headers.put((String) key.get(0), (String) data.get(
 					new Object[] { key.get(0) }).get(VALUE));
 
 		}
 		return headers;
 	}
 
 	private String[] exportedPackages;
 	private Boolean fragment;
 	private long[] fragments;
 	private Map<String, String> headers;
 	private long[] hosts;
 	private long identifier;
 	private String[] importedPackages;
 	private long lastModified;
 	private String location;
 	private Boolean persistentlyStarted;
 	private long[] registeredServices;
 	private Boolean removalPending;
 	private Boolean required;
 	private long[] requiredBundles;
 	private long[] requiringBundles;
 	private long[] servicesInUse;
 	private int startLevel;
 	private String state;
 	private String symbolicName;
 	private String version;
 }
