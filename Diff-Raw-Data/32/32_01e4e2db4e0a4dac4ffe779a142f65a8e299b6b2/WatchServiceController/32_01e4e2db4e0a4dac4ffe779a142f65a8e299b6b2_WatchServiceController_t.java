 package ch.meyerdaniel.osgi.fss.service.intern;
 
 import static java.nio.file.StandardOpenOption.READ;
 import static java.util.Objects.requireNonNull;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.channels.Channels;
 import java.nio.channels.FileChannel;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Objects;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.util.tracker.ServiceTracker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import ch.meyerdaniel.osgi.fss.api.FileFilter;
 import ch.meyerdaniel.osgi.fss.api.FileService;
 import ch.meyerdaniel.osgi.fss.filter.DefaultFileFilter;
 import ch.meyerdaniel.osgi.fss.util.XMLUtil;
 
 /**
  * This class is used for managing file watchers and processing their events.
  * </p> The default file watcher monitors recursively the relative directory
  * <code>/load</code> and includes files ending with <code>*.xml</code>,
  * <code>*.properties</code>, <code>*.cfg</code> and <code>*.jar</code> for
  * further processing.
  * 
  * 
  * @author Daniel Meyer
  * 
  */
 public class WatchServiceController {
 
 	private static final Logger log = LoggerFactory.getLogger(WatchServiceController.class);
 
 	private final ConcurrentHashMap</* Service PID */String, Set<ManagedService>> managedServices;
 
 	private final ConcurrentHashMap</* Service PID */String, Properties> configurations;
 
 	private final ConcurrentHashMap</* Path */String, Future<Boolean>> watchServices;
 
 	private final ExecutorService executor;
 
 	private BundleContext context;
 
 	private ServiceTracker managedServiceTracker;
 
 	private final FileService fileService;
 
 	/**
 	 * @param context
 	 *            Is used for setting up service trackers.
 	 * @param fileService
 	 *            Is used for reading files.
 	 */
 	public WatchServiceController(BundleContext context, FileService fileService) {
 		this.context = context;
 		this.fileService = fileService;
 		watchServices = new ConcurrentHashMap<>();
 		configurations = new ConcurrentHashMap<>();
 		managedServices = new ConcurrentHashMap<>();
 		executor = Executors.newCachedThreadPool();
 
 		installManagedServiceTracker();
 		installDefaultWatcher();
 	}
 
 	private void installManagedServiceTracker() {
 		managedServiceTracker = new ServiceTracker(context, ManagedService.class.getName(), null) {
 
 			@Override
 			public Object addingService(ServiceReference reference) {
 				final ManagedService service = (ManagedService) reference.getBundle().getBundleContext().getService(reference);
 				final Object servicePid = reference.getProperty(Constants.SERVICE_PID);
 				if (servicePid != null) {
 					registerManagedServiceAndNotify(service, servicePid.toString());
 				}
 				return service;
 			}
 
 			@Override
 			public void remove(ServiceReference reference) {
 				ManagedService service = (ManagedService) reference.getBundle().getBundleContext().getService(reference);
 				Object servicePid = reference.getProperty(Constants.SERVICE_PID);
 				if (servicePid != null) {
 					removeManagedService(service);
 				}
 				super.remove(reference);
 			}
 		};
 		managedServiceTracker.open();
 	}
 
 	private void installDefaultWatcher() {
 		try {
 			FileFilter fileFilter = new DefaultFileFilter();
 			fileFilter.addFilePattern("**/*.properties");
 			fileFilter.addFilePattern("**/*.cfg");
 			fileFilter.addFilePattern("**/*.xml");
 			fileFilter.addFilePattern("**/*.jar");
 			executor.submit(new ConfigurableWatchService("Watcher for /load", fileService.newWatchService(), WatchServiceController.this, fileService.getFileSystem().getPath(".", "/load"), fileFilter).init());
 		} catch (IOException e) {
 			log.error("", e);
 		}
 	}
 
 	/**
 	 * Processes the specified file.
 	 * 
 	 * @param filePath
 	 *            Any supported file.
 	 */
 	public synchronized void processFile(Path filePath) {
 		if (Files.exists(filePath)) {
 			String fileName = filePath.getName(filePath.getNameCount() - 1).toString();
 			if (fileName.endsWith(".cfg") || fileName.endsWith(".properties")) {
 				processJavaPropertyFile(filePath, getServicePid(fileName));
 			} else if (fileName.endsWith(".xml")) {
 				processXMLConfigurationFile(filePath, getServicePid(fileName));
 			} else if (fileName.endsWith(".jar")) {
 				processBundle(filePath);
 			}
 		}
 	}
 
 	private void processJavaPropertyFile(Path configFile, String servicePid) {
		log.debug(MessageFormat.format("Process Java property file configuration with service pid: {0}.", servicePid));

 		final Properties prop;
 		try {
 			prop = fileService.readProperties(configFile);
 		} catch (IOException e) {
 			return;
 		}
 
 		if (/* Configuration from FileInstall */servicePid.startsWith("org.apache.felix.fileinstall")) {
 			if (!prop.isEmpty()) {
 				if (prop.containsKey("felix.fileinstall.dir") && prop.containsKey("felix.fileinstall.filter")) {
 					try {
 						FileFilter fileFilter = new DefaultFileFilter();
 						fileFilter.addFilePattern(prop.getProperty("felix.fileinstall.filter"));
 						initalizeWatcher("FileInstall", configFile, fileService.getFileSystem().getPath(".", prop.getProperty("felix.fileinstall.dir")), fileFilter);
 					} catch (IOException e) {
 						log.error("", e);
 					}
 				}
 			}
 		} /* Configuration for managed services */else {
 			if (!prop.isEmpty()) {
 				try {
 					prop.put("lastmodifiedtime", Files.getLastModifiedTime(configFile));
 				} catch (IOException e) {
 					log.error("", e);
 				}
 			}
 
 			configurations.put(servicePid, prop);
 
 			if (managedServices.containsKey(servicePid)) {
 				for (final ManagedService service : managedServices.get(servicePid)) {
 					executor.submit(new Runnable() {
 
 						@Override
 						public void run() {
 							try {
 								service.updated((Properties) prop.clone());
 							} catch (ConfigurationException e) {
 								log.error("", e);
 							}
 						}
 					});
 				}
 			}
 		}
 	}
 
 	private void initalizeWatcher(String name, Path configFile, Path root, FileFilter fileFilter) throws IOException {
 		terminateFileWatcherIdentifiedByConfigFile(configFile.getFileName().toString());
 		watchServices.put(configFile.getFileName().toString(), executor.submit(new ConfigurableWatchService(name, fileService.newWatchService(), this, root, fileFilter).init()));
 	}
 
 	@SuppressWarnings("unchecked")
 	private void processXMLConfigurationFile(Path configFile, String servicePid) {
		log.debug(MessageFormat.format("Process XML configuration with service pid: {0}.", servicePid));
 		if (/* Watcher Configuration */servicePid.startsWith("ch.meyerdaniel.osgi.fss")) {
 
 			try {
 				Document doc = fileService.readXMLFile(configFile);
 				NodeList result = XMLUtil.getNodeList(doc, "//watchservice");
 
 				requireNonNull(result, "Element watchservice is missing.");
 
 				for (int i = 0; i < result.getLength(); i++) {
 					Node serviceNode = result.item(i);
 
 					String name = XMLUtil.getAttributeValue(serviceNode, "name");
 					String relativePath = XMLUtil.getAttributeValue(serviceNode, "relativePath");
 					String filterClassName = XMLUtil.getAttributeValue(XMLUtil.getUniqueNode(serviceNode, "//filter"), "class");
 
 					requireNonNull(name, "Name of watch service is missing.");
 					requireNonNull(relativePath, "Relative path is missing.");
 					requireNonNull(filterClassName, "Class for filter is missing.");
 
 					Class<FileFilter> filterClass = null;
 					try {
 						filterClass = context.getBundle().loadClass(filterClassName);
 					} catch (ClassNotFoundException e) {
 						for (Bundle bundle : context.getBundles()) {
 							try {
 								filterClass = bundle.loadClass(filterClassName);
 								break;
 							} catch (ClassNotFoundException e2) {
 								// continue
 							}
 						}
 					}
 
 					if (filterClass == null) {
 						throw new ClassNotFoundException(filterClassName);
 					}
 
 					FileFilter fileFilter = filterClass.newInstance();
 
 					NodeList filterList = XMLUtil.getNodeList(serviceNode, "//filter/patterns/pattern");
 
 					for (int k = 0; k < filterList.getLength(); k++) {
 						String pattern = XMLUtil.getTextContent(filterList.item(k));
 
 						Objects.requireNonNull(pattern, k + ". pattern is null.");
 						fileFilter.addFilePattern(pattern);
 
 					}
 
 					initalizeWatcher(name, configFile, fileService.getFileSystem().getPath(".", relativePath), fileFilter);
 				}
 			} catch (Exception e) {
 				log.error("", e);
 			}
 
 		} /* Configuration for managed services */else {
 
			try {
				Document doc = fileService.readXMLFile(configFile);
				final Properties prop = new Properties();
				prop.put("lastmodifiedtime", Files.getLastModifiedTime(configFile));
				prop.put("xmlfile", doc);
 
				configurations.put(servicePid, prop);
 
				if (managedServices.containsKey(servicePid)) {
 					for (final ManagedService service : managedServices.get(servicePid)) {
 						executor.submit(new Runnable() {
 
 							@Override
 							public void run() {
 								try {
 									service.updated(prop);
 								} catch (ConfigurationException e) {
 									log.error("", e);
 								}
 							}
 						});
 					}
 				}
			} catch (Exception e) {
				log.error("", e);
 			}
 		}
 	}
 
 	private void processBundle(Path child) {
		log.debug(MessageFormat.format("Process bundle {0}.", child));

 		try (InputStream is = Channels.newInputStream(FileChannel.open(child, READ))) {
 			context.installBundle(child.getFileName().toString(), is).start();
 		} catch (IOException e) {
 			// do nothing
 		} catch (BundleException e) {
 			log.error("", e);
 		}
 	}
 
 	/**
 	 * Processes the specified deleted file.
 	 * 
 	 * @param child
 	 *            Any supported and deleted file.
 	 */
 	public synchronized void processDeletedFile(Path child) {
 		String fileName = child.getName(child.getNameCount() - 1).toString();
 
 		if (fileName.endsWith(".cfg") || fileName.endsWith(".properties")) {
 			processDeletedJavaPropertyFile(child, fileName);
 		} else if (fileName.endsWith(".jar")) {
 			processDeletedBundle(child);
 		}
 	}
 
 	private void processDeletedJavaPropertyFile(Path child, String fileName) {
 		String servicePid = getServicePid(fileName);
 
 		if (servicePid.startsWith("ch.meyerdaniel.osgi.fs") || servicePid.startsWith("org.apache.felix.fileinstall")) {
 			terminateFileWatcherIdentifiedByConfigFile(child.getFileName().toString());
 		} else if (managedServices.containsKey(servicePid)) {
 			for (final ManagedService service : managedServices.get(servicePid)) {
 				executor.submit(new Runnable() {
 
 					@Override
 					public void run() {
 						try {
 							service.updated(null);
 						} catch (ConfigurationException e) {
 							log.error("", e);
 						}
 					}
 				});
 			}
 		}
 	}
 
 	private void processDeletedBundle(Path child) {
 		for (Bundle bundle : context.getBundles()) {
 			if (bundle.getLocation().equals(child.getFileName().toString())) {
 				try {
 					bundle.uninstall();
 				} catch (BundleException e) {
 					log.error("", e);
 				}
 				break;
 			}
 		}
 	}
 
 	private void terminateFileWatcherIdentifiedByConfigFile(String fileName) {
 		if (watchServices.containsKey(fileName)) {
 			log.debug(MessageFormat.format("Stop file watcher based on configuration file {0}.", fileName));
 			watchServices.remove(fileName).cancel(true);
 			log.debug(MessageFormat.format("Currently active watchers are {0}.", Arrays.toString(watchServices.keySet().toArray())));
 		}
 	}
 
 	private String getServicePid(String fileName) {
 		return fileName.substring(0, fileName.lastIndexOf("."));
 	}
 
 	private void registerManagedServiceAndNotify(final ManagedService service, final String servicePid) {
		log.debug(MessageFormat.format("Register managed service with service pid: {0}.", servicePid));
 		Set<ManagedService> services = managedServices.get(servicePid);
 		if (services == null) {
 			services = Collections.newSetFromMap(new ConcurrentHashMap<ManagedService, Boolean>());
 			managedServices.put(servicePid, services);
 		}
 		services.add(service);
 
 		if (configurations.containsKey(servicePid)) {
 			executor.submit(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						service.updated(configurations.get(servicePid));
 					} catch (ConfigurationException e) {
 						log.error("", e);
 					}
 				}
 			});
 		}
 	}
 
 	private void removeManagedService(ManagedService service) {
 		for (Set<ManagedService> services : managedServices.values()) {
 			if (services.contains(service)) {
 				services.remove(service);
 				if (services.isEmpty()) {
 					managedServices.remove(services);
 				}
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Stops this controller.
 	 */
 	public void shutdown() {
 		executor.shutdownNow();
 		configurations.clear();
 		managedServices.clear();
 		managedServiceTracker.close();
 		context = null;
 
 	}
 }
