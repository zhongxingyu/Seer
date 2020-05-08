 package org.lightmare.jpa;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.PersistenceException;
 
 import org.apache.log4j.Logger;
 import org.lightmare.ejb.meta.ConnectionSemaphore;
 import org.lightmare.jndi.NamingUtils;
 
 /**
  * Creates and caches {@link EntityManagerFactory} for each ejb bean
  * {@link Class}'s appropriate field (annotated by @PersistenceContext)
  * 
  * @author Levan
  * 
  */
 public class JPAManager {
 
 	// Keeps unique EntityManagerFactories builded by unit names
 	private static final ConcurrentMap<String, ConnectionSemaphore> CONNECTIONS = new ConcurrentHashMap<String, ConnectionSemaphore>();
 
 	private List<String> classes;
 
 	private String path;
 
 	private URL url;
 
 	private Map<?, ?> properties;
 
 	private boolean swapDataSource;
 
 	boolean scanArchives;
 
 	private static final Logger LOG = Logger.getLogger(JPAManager.class);
 
 	private JPAManager() {
		throw new AssertionError(String.format(
				"Call of %s class constructor is not allowed", getClass()
						.getName()));
 	}
 
 	public static boolean checkForEmf(String unitName) {
 
 		boolean check = unitName != null && !unitName.isEmpty();
 
 		if (check) {
 			check = CONNECTIONS.containsKey(unitName);
 		}
 
 		return check;
 	}
 
 	private static ConnectionSemaphore createSemaphore(String unitName) {
 
 		ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 		ConnectionSemaphore current = null;
 		if (semaphore == null) {
 			semaphore = new ConnectionSemaphore();
 			semaphore.setUnitName(unitName);
 			semaphore.setInProgress(true);
 			semaphore.setCached(true);
 			current = CONNECTIONS.putIfAbsent(unitName, semaphore);
 		}
 		if (current == null) {
 			current = semaphore;
 		}
 		current.incrementUser();
 
 		return current;
 	}
 
 	public static ConnectionSemaphore setSemaphore(String unitName,
 			String jndiName) {
 
 		ConnectionSemaphore semaphore = null;
 
 		if (unitName != null && !unitName.isEmpty()) {
 
 			semaphore = createSemaphore(unitName);
 			if (jndiName != null && !jndiName.isEmpty()) {
 				ConnectionSemaphore existent = CONNECTIONS.putIfAbsent(
 						jndiName, semaphore);
 				if (existent == null) {
 					semaphore.setJndiName(jndiName);
 				}
 			}
 		}
 
 		return semaphore;
 	}
 
 	private static void awaitConnection(ConnectionSemaphore semaphore) {
 		synchronized (semaphore) {
 			boolean inProgress = semaphore.isInProgress()
 					&& !semaphore.isBound();
 			while (inProgress) {
 				try {
 					semaphore.wait();
 					inProgress = semaphore.isInProgress()
 							&& !semaphore.isBound();
 				} catch (InterruptedException ex) {
 					inProgress = false;
 					LOG.error(ex.getMessage(), ex);
 				}
 			}
 		}
 
 	}
 
 	public static boolean isInProgress(String jndiName) {
 
 		ConnectionSemaphore semaphore = CONNECTIONS.get(jndiName);
 		boolean inProgress = semaphore != null;
 		if (inProgress) {
 			inProgress = semaphore.isInProgress() && !semaphore.isBound();
 			if (inProgress) {
 				awaitConnection(semaphore);
 			}
 		}
 		return inProgress;
 	}
 
 	/**
 	 * Creates {@link EntityManagerFactory} by hibernate or by extended builder
 	 * {@link Ejb3ConfigurationImpl} if entity classes or persistence.xml file
 	 * path are provided
 	 * 
 	 * @see Ejb3ConfigurationImpl#configure(String, Map) and
 	 *      Ejb3ConfigurationImpl#createEntityManagerFactory()
 	 * 
 	 * @param unitName
 	 * @return {@link EntityManagerFactory}
 	 */
 	@SuppressWarnings("deprecation")
 	private EntityManagerFactory buildEntityManagerFactory(String unitName)
 			throws IOException {
 		EntityManagerFactory emf;
 		Ejb3ConfigurationImpl cfg;
 
 		boolean checkForPath = checkForPath();
 		boolean checkForURL = checkForURL();
 		boolean checkForClasses = checkForClasses();
 		if (checkForPath || checkForURL) {
 			Enumeration<URL> xmls;
 			ConfigLoader configLoader = new ConfigLoader();
 			if (checkForPath) {
 				try {
 					xmls = configLoader.readFile(path);
 				} catch (IOException ex) {
 					throw new PersistenceException(ex);
 				}
 			} else {
 				xmls = configLoader.readURL(url);
 			}
 			if (checkForClasses) {
 				cfg = new Ejb3ConfigurationImpl(classes, xmls);
 			} else {
 				cfg = new Ejb3ConfigurationImpl(xmls);
 			}
 			cfg.setShortPath(configLoader.getShortPath());
 		} else {
 			cfg = new Ejb3ConfigurationImpl(classes);
 		}
 
 		cfg.setSwapDataSource(swapDataSource);
 		cfg.setScanArchives(scanArchives);
 
 		Ejb3ConfigurationImpl configured = cfg.configure(unitName, properties);
 
 		emf = configured != null ? configured.buildEntityManagerFactory()
 				: null;
 		return emf;
 	}
 
 	/**
 	 * Checks if entity classes are provided
 	 * 
 	 * @return boolean
 	 */
 	private boolean checkForClasses() {
 		return classes != null && !classes.isEmpty();
 	}
 
 	/**
 	 * Checks if entity persistence.xml path is provided
 	 * 
 	 * @return boolean
 	 */
 	private boolean checkForPath() {
 		return path != null && !path.isEmpty();
 	}
 
 	/**
 	 * Checks if entity persistence.xml {@link URL} is provided
 	 * 
 	 * @return boolean
 	 */
 	private boolean checkForURL() {
 		return url != null && !url.toString().isEmpty();
 	}
 
 	/**
 	 * Checks if entity classes or persistence.xml path are provided
 	 * 
 	 * @param classes
 	 * @return boolean
 	 */
 	private boolean checkForBuild() {
 		return checkForClasses() || checkForPath() || checkForURL();
 	}
 
 	/**
 	 * Checks if entity classes or persistence.xml file path are provided to
 	 * create {@link EntityManagerFactory}
 	 * 
 	 * @see #buildEntityManagerFactory(String, String, Map, List)
 	 * 
 	 * @param unitName
 	 * @param properties
 	 * @param path
 	 * @param classes
 	 * @return {@link EntityManagerFactory}
 	 * @throws IOException
 	 */
 	private EntityManagerFactory createEntityManagerFactory(String unitName)
 			throws IOException {
 		EntityManagerFactory emf;
 		if (checkForBuild()) {
 			emf = buildEntityManagerFactory(unitName);
 		} else if (properties == null) {
 			emf = Persistence.createEntityManagerFactory(unitName);
 		} else {
 			emf = Persistence.createEntityManagerFactory(unitName, properties);
 		}
 
 		return emf;
 	}
 
 	/**
 	 * Binds {@link EntityManagerFactory} to {@link javax.naming.InitialContext}
 	 * 
 	 * @param jndiName
 	 * @param unitName
 	 * @param emf
 	 * @throws IOException
 	 */
 	private void bindJndiName(ConnectionSemaphore semaphore) throws IOException {
 		boolean bound = semaphore.isBound();
 		if (!bound) {
 			String jndiName = semaphore.getJndiName();
 			if (jndiName != null && !jndiName.isEmpty()) {
 				NamingUtils namingUtils = new NamingUtils();
 				try {
 					Context context = namingUtils.getContext();
 					if (context.lookup(jndiName) == null) {
 						namingUtils.getContext().bind(jndiName,
 								semaphore.getEmf());
 					}
 					semaphore.setBound(true);
 				} catch (NamingException ex) {
 					throw new IOException(String.format(
 							"could not bind connection %s",
 							semaphore.getUnitName()), ex);
 				}
 			} else {
 				semaphore.setBound(true);
 			}
 		}
 	}
 
 	public void setConnection(String unitName) throws IOException {
 		ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 		if (semaphore.isInProgress()) {
 			EntityManagerFactory emf = createEntityManagerFactory(unitName);
 			semaphore.setEmf(emf);
 			semaphore.setInProgress(false);
 			bindJndiName(semaphore);
 		} else if (semaphore.getEmf() == null) {
 			throw new IOException(String.format(
 					"Connection %s was not in progress", unitName));
 		} else {
 			bindJndiName(semaphore);
 		}
 	}
 
 	public static ConnectionSemaphore getConnection(String unitName)
 			throws IOException {
 
 		ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 		if (semaphore != null) {
 			awaitConnection(semaphore);
 		}
 
 		return semaphore;
 	}
 
 	public static EntityManagerFactory getEntityManagerFactory(String unitName)
 			throws IOException {
 
 		EntityManagerFactory emf = null;
 		ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 		if (semaphore != null) {
 			awaitConnection(semaphore);
 			emf = semaphore.getEmf();
 		}
 
 		return emf;
 
 	}
 
 	private static void unbindConnection(ConnectionSemaphore semaphore) {
 
 		String jndiName = semaphore.getJndiName();
 		if (jndiName != null && semaphore.isBound()) {
 			NamingUtils namingUtils = new NamingUtils();
 			try {
 				Context context = namingUtils.getContext();
 				if (context.lookup(jndiName) != null) {
 					context.unbind(jndiName);
 				}
 			} catch (NamingException ex) {
 				LOG.error(ex.getMessage(), ex);
 			} catch (IOException ex) {
 				LOG.error(ex.getMessage(), ex);
 			}
 		}
 	}
 
 	private static void closeConnection(ConnectionSemaphore semaphore) {
 		int users = semaphore.decrementUser();
 		if (users <= 0) {
 			EntityManagerFactory emf = semaphore.getEmf();
 			closeEntityManagerFactory(emf);
 			unbindConnection(semaphore);
 			CONNECTIONS.remove(semaphore.getUnitName());
 		}
 	}
 
 	public static void removeConnection(String unitName) {
 
 		ConnectionSemaphore semaphore = CONNECTIONS.get(unitName);
 		if (semaphore != null) {
 			awaitConnection(semaphore);
 			closeConnection(semaphore);
 			NamingUtils namingUtils = new NamingUtils();
 			try {
 				String jndiName = NamingUtils.createJndiName(unitName);
 				namingUtils.unbind(jndiName);
 			} catch (IOException ex) {
 				LOG.error(String.format(
 						"Could not unbind jndi name %s cause %s", unitName,
 						ex.getMessage()), ex);
 			}
 		}
 
 	}
 
 	private static void closeEntityManagerFactory(EntityManagerFactory emf) {
 
 		if (emf != null && emf.isOpen()) {
 			emf.close();
 		}
 	}
 
 	/**
 	 * Closes all existing {@link EntityManagerFactory} instances kept in cache
 	 */
 	public static void closeEntityManagerFactories() {
 		Collection<ConnectionSemaphore> semaphores = CONNECTIONS.values();
 		EntityManagerFactory emf;
 		for (ConnectionSemaphore semaphore : semaphores) {
 			emf = semaphore.getEmf();
 			closeEntityManagerFactory(emf);
 		}
 
 		CONNECTIONS.clear();
 	}
 
 	public static class Builder {
 
 		private JPAManager manager;
 
 		public Builder() {
 			manager = new JPAManager();
 			manager.scanArchives = true;
 		}
 
 		public Builder setClasses(List<String> classes) {
 			manager.classes = classes;
 			return this;
 		}
 
 		public Builder setURL(URL url) {
 			manager.url = url;
 			return this;
 		}
 
 		public Builder setPath(String path) {
 			manager.path = path;
 			return this;
 		}
 
 		public Builder setProperties(Map<?, ?> properties) {
 			manager.properties = properties;
 			return this;
 		}
 
 		public Builder setSwapDataSource(boolean swapDataSource) {
 			manager.swapDataSource = swapDataSource;
 			return this;
 		}
 
 		public Builder setScanArchives(boolean scanArchives) {
 			manager.scanArchives = scanArchives;
 			return this;
 		}
 
 		public JPAManager build() {
 			return manager;
 		}
 	}
 
 }
