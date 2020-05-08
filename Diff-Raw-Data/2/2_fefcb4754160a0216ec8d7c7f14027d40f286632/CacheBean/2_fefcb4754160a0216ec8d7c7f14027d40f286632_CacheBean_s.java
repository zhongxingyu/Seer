 package com.bananity.caches;
 
 
 // Bananity Classes
 import com.bananity.constants.StorageConstantsBean;
 import com.bananity.util.SearchTerm;
 
 // Caches
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.Weigher;
 
 // Java utils
 import java.util.ArrayList;
 import java.util.HashMap;
 
 // Bean Setup
 import javax.ejb.EJB;
 import javax.ejb.Startup;
 import javax.ejb.Singleton;
 import javax.annotation.PostConstruct;
 
 // Concurrency Management
 import javax.ejb.Lock;
 import javax.ejb.LockType;
 import javax.ejb.DependsOn;
 import javax.ejb.ConcurrencyManagement;
 import javax.ejb.ConcurrencyManagementType;
 
 // Log4j
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.PropertyConfigurator;
 
 
 /**
  *  This class is the central handler of the search caches.
  *  The object follows the Singleton pattern.
  *
  *  @author  Andreu Correa Casablanca
  *  @version 0.4
  *
  *  @see com.bananity.constants.StorageConstantsBean
  */
 @Startup
 @Singleton
 @DependsOn({"StorageConstantsBean"})
 @ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
 public class CacheBean {
 
 	/**
 	 *  Log4j reference
 	 */
 	private static Logger log;
 
 	/**
 	 * Reference to the storage constants bean, needed to access loaded settings values
 	 */
 	@EJB
 	private StorageConstantsBean scB;
 
 	/**
 	 *  Associative list (by collection name) of results caches
 	 */
 	private HashMap<String, Cache<String, ArrayList<SearchTerm>>> resultCaches;
 
 	/**
 	 *  Associative list (by collection name) of tokens caches
 	 */
 	private HashMap<String, Cache<String, ArrayList<SearchTerm>>> tokensCaches;
 
 	/**
 	 *  This method initializes the logger reference and the caches associative lists
 	 */
 	@Lock(LockType.WRITE)
 	@PostConstruct
 		void init() throws Exception {
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			PropertyConfigurator.configure(classLoader.getResource("log4j.properties"));
 			log = Logger.getLogger(CacheBean.class);
 
 			tokensCaches = new HashMap<String, Cache<String, ArrayList<SearchTerm>>>();
 			resultCaches = new HashMap<String, Cache<String, ArrayList<SearchTerm>>>();
 
 			for (String collName : scB.getIndexedCollections()) {
 				ArrayList<String> resultCacheSizeSrc = scB.getResultsCacheSize().get(collName);
 				ArrayList<String> tokensCacheSizeSrc = scB.getTokensCacheSize().get(collName);
 
 				if (tokensCacheSizeSrc == null || tokensCacheSizeSrc.size() != 1) {
 					throw new Exception("¡Tokens Cache Settings not defined for collection \""+collName+"\"!");
 				}
 
 				if (resultCacheSizeSrc == null || resultCacheSizeSrc.size() != 1) {
 					throw new Exception("¡Result Cache Settings not defined for collection \""+collName+"\"!");
 				}
 
 				int tokensCacheSize = Integer.parseInt(tokensCacheSizeSrc.get(0));
 				int resultCacheSize = Integer.parseInt(tokensCacheSizeSrc.get(0));
 
 				Cache<String, ArrayList<SearchTerm>> tokensCache = CacheBuilder.newBuilder()
 					.maximumWeight(tokensCacheSize)
 					.weigher(new Weigher<String, ArrayList<SearchTerm>>() {
 						public int weigh (String k, ArrayList<SearchTerm> v) {
 							// Tenemos en cuenta el tamaño de clave y de los punteros (4 por puntero, 1 por fin de línea)
 							int size = k.length() + 5 + v.size()*20;
 
 							for (SearchTerm vi : v) {
 								size += aproximateSubstringsWeigh(vi.toString().length());
 							}
 
 							return size;
 						}
 					})
 					.build();
 
 				Cache<String, ArrayList<SearchTerm>> resultCache = CacheBuilder.newBuilder()
 					.maximumWeight(resultCacheSize)
 					.weigher(new Weigher<String, ArrayList<SearchTerm>>() {
 						public int weigh (String k, ArrayList<SearchTerm> v) {
 							// Tenemos en cuenta el tamaño de clave y de los punteros (4 por puntero, 1 por fin de línea)
							int size += k.length() + 5 + v.size()*20;
 
 							for (SearchTerm vi : v) {
 								size += aproximateSubstringsWeigh(vi.toString().length());
 							}
 
 							return size;
 						}
 					})
 					.build();
 
 				tokensCaches.put(collName, tokensCache);
 				resultCaches.put(collName, resultCache);
 			}
 		}
 
 	/**
 	 *  Returns a tokens cache associated to a collection
 	 *
 	 *  @param 	collName 	The collection name
 	 *  @return 			Associated tokens cache to the referenced collection
 	 */
 	@Lock(LockType.READ)
 		public Cache<String, ArrayList<SearchTerm>> getTokensCache (String collName) {
 			return tokensCaches.get(collName);
 		}
 
 	/**
 	 *  Returns a results cache associated to a collection
 	 *
 	 *  @param 	collName 	The collection name
 	 *  @return 			Associated results cache to the referenced collection
 	 */
 	@Lock(LockType.READ)
 		public Cache<String, ArrayList<SearchTerm>> getResultCache (String collName) {
 			return resultCaches.get(collName);
 		}
 
 	/**
 	 *  @return Associative list (by collection name) of tokens caches
 	 */
 	@Lock(LockType.READ)
 		public HashMap<String, Cache<String, ArrayList<SearchTerm>>> getTokensCaches () {
 			return tokensCaches;
 		}
 
 	/**
 	 *  @return Associative list (by collection name) of results caches
 	 */
 	@Lock(LockType.READ)
 		public HashMap<String, Cache<String, ArrayList<SearchTerm>>> getResultCaches () {
 			return resultCaches;
 		}
 
 	@Lock(LockType.READ)
 		private int aproximateSubstringsWeigh (final int length) {
 			return (length*(length - 1))*2;
 		}
 }
