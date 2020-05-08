 package org.lightmare.ejb.meta;
 
 import static org.lightmare.jpa.JPAManager.closeEntityManagerFactories;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.persistence.EntityManagerFactory;
 
 import org.lightmare.ejb.exceptions.BeanInUseException;
 import org.lightmare.jpa.JPAManager;
 
 /**
  * Container class to save {@link MetaData} for bean interface {@link Class} and
  * connections {@link EntityManagerFactory}es for unit names
  * 
  * @author Levan
  * 
  */
 public class MetaContainer {
 
 	// Cached bean meta data
 	private static final ConcurrentMap<String, MetaData> EJBS = new ConcurrentHashMap<String, MetaData>();
 
 	/**
 	 * Adds {@link MetaData} to cache on specified bean name if absent and
 	 * returns previous value on this name or null if such value does not exists
 	 * 
 	 * @param beanName
 	 * @param metaData
 	 * @return
 	 */
 	public static MetaData addMetaData(String beanName, MetaData metaData) {
 		return EJBS.putIfAbsent(beanName, metaData);
 	}
 
 	/**
 	 * Check if {@link MetaData} is ceched for specified bean name if true
 	 * throws {@link BeanInUseException}
 	 * 
 	 * @param beanName
 	 * @param metaData
 	 * @throws BeanInUseException
 	 */
 	public static void checkAndAddMetaData(String beanName, MetaData metaData)
 			throws BeanInUseException {
 		MetaData tmpMeta = addMetaData(beanName, metaData);
		if (tmpMeta != null) {
 			throw new BeanInUseException(String.format(
 					"bean %s is alredy in use", beanName));
 		}
 	}
 
 	/**
 	 * Checks if bean with associated name deployed and if yes if is deployment
 	 * in progress
 	 * 
 	 * @param beanName
 	 * @return boolean
 	 */
 	public static boolean checkMetaData(String beanName) {
 		boolean check;
 		MetaData metaData = EJBS.get(beanName);
 		check = metaData == null;
 		if (!check) {
 			check = metaData.isInProgress();
 		}
 		return check;
 	}
 
 	/**
 	 * Checks if bean with associated name deployed
 	 * 
 	 * @param beanName
 	 * @return boolean
 	 */
 	public boolean checkBean(String beanName) {
 		return EJBS.containsKey(beanName);
 	}
 
 	/**
 	 * Waits while {@link MetaData#isInProgress()} is true
 	 * 
 	 * @param metaData
 	 * @throws IOException
 	 */
 	public static void awaitMetaData(MetaData metaData) throws IOException {
 		boolean inProgress = metaData.isInProgress();
 		if (inProgress) {
 			synchronized (metaData) {
 				while (inProgress) {
 					try {
 						metaData.wait();
 						inProgress = metaData.isInProgress();
 					} catch (InterruptedException ex) {
 						throw new IOException(ex);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets deployed bean {@link MetaData} by name without checking deployment
 	 * progress
 	 * 
 	 * @param beanName
 	 * @return {@link MetaData}
 	 */
 	public static MetaData getMetaData(String beanName) {
 		return EJBS.get(beanName);
 	}
 
 	/**
 	 * Check if {@link MetaData} with associated name deployed and if it is
 	 * waits while {@link MetaData#isInProgress()} true before return it
 	 * 
 	 * @param beanName
 	 * @return {@link MetaData}
 	 * @throws IOException
 	 */
 	public static MetaData getSyncMetaData(String beanName) throws IOException {
 		MetaData metaData = getMetaData(beanName);
 		if (metaData == null) {
 			throw new IOException(String.format("Bean %s is not deployed",
 					beanName));
 		}
 		awaitMetaData(metaData);
 
 		return metaData;
 	}
 
 	/**
 	 * Removed {@link MetaData} from cache
 	 * 
 	 * @param beanName
 	 */
 	public static void removeMeta(String beanName) {
 		EJBS.remove(beanName);
 	}
 
 	public static EntityManagerFactory getConnection(String unitName)
 			throws IOException {
 		return JPAManager.getEntityManagerFactory(unitName);
 	}
 
 	public static void closeConnections() {
 		closeEntityManagerFactories();
 	}
 
 	public static Iterator<MetaData> getBeanClasses() {
 		return EJBS.values().iterator();
 	}
 }
