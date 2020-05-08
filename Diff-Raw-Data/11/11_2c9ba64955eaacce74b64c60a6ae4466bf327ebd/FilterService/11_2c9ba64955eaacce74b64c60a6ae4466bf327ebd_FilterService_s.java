 package org.filterinterceptor;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.ServiceLoader;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.filterinterceptor.spi.Filter;
 import org.filterinterceptor.spi.FilteredMethod;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Load JAR of a directory Load all services extends Filter class (user SPI)
  * Provide simple method to access to this filter
  * <p>
  * You must call initFilter before first use.
  */
 public class FilterService {
 
 	private static final Logger logger = LoggerFactory.getLogger(FilterService.class);
 
 	/**
 	 * The place of JAR to load
 	 */
 	private final String jarFolder;
 
 	/**
 	 * Set of filters
 	 */
 	private final Set<Filter<?>> loadedFilters = new HashSet<Filter<?>>();
 
 	/**
 	 * Active filters in a map to fast access and in a list to manage activation
 	 */
 	private Map<String, Filter<?>> activeFilters;
 	/**
 	 * Filters in a list to manage activation
 	 */
 	private List<Filter<?>> allFilters;
 
 	/**
 	 * Object use to lock access to collections for fast access
 	 */
 	private final ReadWriteLock lockToFilterFastAccessCollections = new ReentrantReadWriteLock();
 
 	/**
 	 * Public constructor
 	 * 
 	 * @param jarFolder
 	 *            the folder of JAR containing Filters to load
 	 */
 	public FilterService(String jarFolder) {
 		this.jarFolder = jarFolder;
 	}
 
 	/**
 	 * Get the active filter for one param of one method on one service
 	 * 
 	 * @param serviceClass
 	 *            the service class
 	 * @param methodName
 	 *            the method name
 	 * @return the active filter with the highest priority
 	 */
 	public Filter<?> getActiveFilter(Class<?> serviceClass, String methodName) {
 		Lock lock = lockToFilterFastAccessCollections.readLock();
 		try {
 			lock.lock();
 			return activeFilters.get(getKey(serviceClass, methodName));
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	/**
 	 * Get all active filters which be used
 	 * 
 	 * @return the Map of filters
 	 */
 	public Map<String, Filter<?>> getAllActiveFiltersUsed() {
 		Lock lock = lockToFilterFastAccessCollections.readLock();
 		try {
 			lock.lock();
 			return activeFilters;
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	/**
 	 * Get all filters sorted in an unmodifiable list
 	 * 
 	 * @return the list of filters
 	 */
 	public List<Filter<?>> getAllFilters() {
 		Lock lock = lockToFilterFastAccessCollections.readLock();
 		try {
 			lock.lock();
 			return allFilters;
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	/**
 	 * Load dynamically Filters in JAR files
 	 * 
 	 * @throws IOException
 	 *             on exception during research
 	 */
 	public void initFilters() throws IOException {
 		logger.debug("Search Filters...");
 
 		try {
 			// Search not loaded JAR files and build class loader
 			ClassLoader classLoader = loadExternalJar();
 
 			// Discover and register the available commands
 			buildLoadedFilter(classLoader);
 
 		} catch (RuntimeException e) {
 			logger.error("Can't init filter: " + e.getMessage(), e);
 		}
 
 		// Build fast access collections
 		buildFilterFastAccessCollections();
 
 		logger.debug("Search Filters - End");
 	}
 
 	/**
 	 * Active or desactive a Filter
 	 * 
 	 * @param filter
 	 *            the filter to activate or desactivate
 	 * @param active
 	 *            the active status to set
 	 */
 	public void setFilterActiveStatus(Filter<?> filter, boolean active) {
 		// set active state on filter
 		filter.setActive(active);
 
 		// build fast access collections
 		buildFilterFastAccessCollections();
 	}
 
 	/**
 	 * Change priority of a Filter
 	 * 
 	 * @param filter
 	 *            the filter to change the priority
 	 * @param priority
 	 *            the priority to set
 	 */
 	public void setFilterPriority(Filter<?> filter, int priority) {
 		// set priority on filter
 		filter.setPriority(priority);
 
 		// build fast access collections
 		buildFilterFastAccessCollections();
 	}
 
 	/**
 	 * THis method search and call the filter of a service, and the real service
 	 * if no filter is found
 	 * 
 	 * @param service
 	 *            the real service
 	 * @param extendToInterfaces
 	 *            a boolean indicate if the search of filter is done on filter
 	 *            interfaces too or not
 	 * @param method
 	 *            the method to call
 	 * @param args
 	 *            parameters to give to the method called
 	 * @return the returned object by Filter or the real service
 	 * @throws Throwable
 	 *             come from invoke method of {@link Proxy}
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public Object invoke(Object service, boolean extendToInterfaces, Method method, Object... args) throws Throwable {
 		Object proxy = null;
 		Filter filter = null;
 		try {
 			String methodName = method.getName();
 			Class<?> serviceClass = service.getClass();
 			logger.debug("Search filter on {}.{}", serviceClass.getSimpleName(), methodName);
 
 			// Get the filters
 			filter = getActiveFilter(serviceClass, methodName);
 			// if no filter on service class and extended search to interface is
 			// activated
 			if (filter == null && extendToInterfaces) {
 				for (Class<?> i : serviceClass.getInterfaces()) {
 					filter = getActiveFilter(i, methodName);
 					if (filter != null)
 						break;
 				}
 			}
 
 			if (filter != null) {
 				// Get the new method proxy
 				proxy = filter.getFilterServiceImpl(service);
 			} else {
 				logger.trace("There is no filter on this service");
 			}
 		} catch (RuntimeException e) {
 			logger.error("Exception while try to execute filter: " + e.getMessage(), e);
 		}
 
 		if (proxy != null) {
 			// Call the method proxy
 			Object ret = method.invoke(proxy, args);
 			logger.info("Filter <{}> applied", filter.getDescription());
 			return ret;
 		} else {
 			logger.debug("Invoke real service method");
 			return method.invoke(service, args);
 		}
 	}
 
 	/*
 	 * PRIVATE
 	 */
 
 	/**
 	 * Set all JAR files in a Class Loader
 	 * 
 	 * @return a classLoader containing all JAR files URL
 	 * @throws IOException
 	 *             if JAR folder not found
 	 */
 	private ClassLoader loadExternalJar() throws IOException {
 
 		File baseDirectory = new File(jarFolder);
 		logger.debug("Try to find JAR in folder: {}", baseDirectory.getCanonicalPath());
 		// Check if directory exists
 		if (!baseDirectory.exists() || !baseDirectory.isDirectory())
 			throw new IOException("Extern JAR folder (" + baseDirectory.getCanonicalPath() + ") not found");
 		// Get file with .jar extension
 		File[] jarFiles = baseDirectory.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".jar");
 			}
 		});
 
 		// Add jarFiles to classLoader
 		ClassLoader classLoader;
 		if (jarFiles != null) {
 			List<URL> urlList = new ArrayList<URL>();
 			for (File file : jarFiles) {
 				logger.debug("JAR found: {}", file.getName());
 				URL url = file.toURI().toURL();
 				urlList.add(url);
 			}
			classLoader = new URLClassLoader(urlList.toArray(new URL[0]));
 		} else {
 			classLoader = this.getClass().getClassLoader();
 		}
 		return classLoader;
 	}
 
 	/**
 	 * Load all classes in classLoader with the Service Provider API
 	 * 
 	 * @param classLoader
 	 *            the class loader use by the Service Provider API
 	 */
 	private void buildLoadedFilter(ClassLoader classLoader) {
 
 		logger.debug("Load filters in classpath...");
 		@SuppressWarnings("rawtypes")
 		ServiceLoader<Filter> filterLoader = ServiceLoader.load(Filter.class, classLoader);
 
 		logger.debug("Register filters...");
 		for (Filter<?> newFilter : filterLoader) {
 			// Check that filter has not been already loaded
 			if (!loadedFilters.contains(newFilter)) {
 				loadedFilters.add(newFilter);
 				logger.debug("Filter found on service {}: <{}>", newFilter.getService().getSimpleName(),
 						newFilter.getDescription());
 			}
 		}
 	}
 
 	/**
 	 * Build the map key with service Parameter
 	 * 
 	 * @param serviceClass
 	 *            the class of the service
 	 * @param methodName
 	 *            the method name
 	 * @return the key of the map used to store filter
 	 */
 	private static String getKey(Class<?> serviceClass, String methodName) {
 		String key = String.format("%s.%s", serviceClass.getName(), methodName);
 		logger.trace("Access to key {}", key);
 		return key;
 	}
 
 	/**
 	 * Build a the access collection to improve performances
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void buildFilterFastAccessCollections() {
 
 		Lock lock = lockToFilterFastAccessCollections.writeLock();
 		try {
 			lock.lock();
 
 			// LIST
 			allFilters = new ArrayList<Filter<?>>(loadedFilters);
 			// Sort Filters
 			Collections.sort((List) allFilters);// TODOJ7 Cast for Java 6
 												// compilation error
 			// Create a RO list
 			allFilters = Collections.unmodifiableList(allFilters);
 
 			// MAP
 			activeFilters = new HashMap<String, Filter<?>>();
 			for (Filter<?> filter : loadedFilters) {
 				if (filter.isActive()) {
 					// Iterate on each method
 					// and check that the annotation FilteredMethod is present
 					logger.debug("Read filter methods on {}: <{}>", filter.getService().getSimpleName(),
 							filter.getDescription());
 					for (Method method : filter.getFilterServiceImpl(null).getClass().getDeclaredMethods()) {
 						if (method.getAnnotation(FilteredMethod.class) != null) {
 							String methodName = method.getName();
 							String filterName = getKey(filter.getService(), methodName);
 							logger.debug("Filter <{}>: {}.{} is overrided", new String[] { filter.getDescription(),
 									filter.getService().getSimpleName(), methodName });
 
 							Filter<?> otherFilter = activeFilters.get(filterName);
 							if (otherFilter == null || otherFilter.getPriority() < filter.getPriority()) {
 								logger.trace("New Filter set for key {}", filterName);
 								activeFilters.put(filterName, filter);
 							}
 						}
 					}
 				} else {
 					logger.debug("Filter <{}> is desactivated", filter.getDescription());
 				}
 
 			}
 			// Create a RO map
 			activeFilters = Collections.unmodifiableMap(activeFilters);
 		} finally {
 			lock.unlock();
 		}
 	}
 }
