 package de.zib.gndms.infra.system;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import com.google.common.collect.Maps;
 import de.zib.gndms.stuff.GNDMSInjector;
 import de.zib.gndms.stuff.GNDMSInjectorSpring;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import de.zib.gndms.infra.access.ServiceHomeProvider;
 import de.zib.gndms.infra.service.GNDMPersistentServiceHome;
 import de.zib.gndms.infra.service.GNDMServiceHome;
 import de.zib.gndms.infra.service.GNDMSingletonServiceHome;
 import de.zib.gndms.kit.access.GNDMSBinding;
 import de.zib.gndms.kit.configlet.DefaultConfiglet;
 import de.zib.gndms.logic.access.TaskActionProvider;
 import de.zib.gndms.logic.model.gorfx.*;
 import de.zib.gndms.kit.access.InstanceProvider;
 import de.zib.gndms.model.common.ConfigletState;
 import de.zib.gndms.model.common.ModelUUIDGen;
 import de.zib.gndms.model.common.types.factory.IndustrialPark;
 import de.zib.gndms.model.common.types.factory.KeyFactory;
 import de.zib.gndms.model.common.types.factory.KeyFactoryInstance;
 import de.zib.gndms.model.common.types.factory.RecursiveKeyFactory;
 import de.zib.gndms.stuff.BoundInjector;
 import de.zib.gndms.kit.configlet.ConfigletProvider;
 import de.zib.gndms.kit.configlet.Configlet;
 import de.zib.gndms.kit.system.SystemInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.jetbrains.annotations.NotNull;
 
 import javax.annotation.PostConstruct;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Query;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * ThingAMagic.
 *
 * @see GNDMSystem
 * @author  try ste fan pla nti kow zib
 * @version $Id$
 *
 *          User: stepn Date: 03.09.2008 Time: 16:50:06
 */
 public class GNDMSystemDirectory implements SystemDirectory, BeanFactoryAware {
 
     private @NotNull final Logger logger = LoggerFactory.getLogger(GNDMSystemDirectory.class);
     private static final int INITIAL_CAPACITY = 32;
     private static final long INSTANCE_RETRIEVAL_INTERVAL = 250L;
 
     private final @NotNull String systemName;
 
     /**
      * stores several instances needed for the <tt>GNDMSystem</tt>
      */
     private final @NotNull Map<String, Object> instances;
 
 
 	private final Map<String, Configlet> configlets = Maps.newConcurrentHashMap();
 
 	@SuppressWarnings({ "FieldCanBeLocal" })
 	private final Wrapper<Object> sysHolderWrapper;
 
 	private final @NotNull ModelUUIDGen uuidGen;
 
 	private final @NotNull BoundInjector boundInjector = new BoundInjector();
 
     private GNDMSInjector Injector;
 
 
 
 	@SuppressWarnings({ "ThisEscapedInObjectConstruction" })
 	GNDMSystemDirectory(
 	      final @NotNull String sysNameParam,
 	      final @NotNull ModelUUIDGen uuidGenParam,
 	      final Wrapper<Object> systemHolderWrapParam )
     {
         instances = new HashMap<String, Object>(INITIAL_CAPACITY);
         systemName = sysNameParam;
 		uuidGen = uuidGenParam;
 	    sysHolderWrapper = systemHolderWrapParam;
     }
 
 
     @PostConstruct
     void init () {
 
         boundInjector.setInjector(Injector);
         GNDMSBinding.setDefaultInjector(Injector);
 
 	    final QuoteCalculatorMetaFactory calcMF = new QuoteCalculatorMetaFactory();
 		calcMF.setInjector(injector);
 	    calcMF.setWrap(sysHolderWrapper);
 	    orqPark = new OfferTypeIndustrialPark<AbstractQuoteCalculator<?>>(calcMF);
 
 	    final TaskFlowActionMetaFactory taskFlowMF = new TaskFlowActionMetaFactory();
 	    taskFlowMF.setWrap(sysHolderWrapper);
 		taskFlowMF.setInjector(injector);
 	    taskActionPark = new OfferTypeIndustrialPark<TaskFlowAction<?>>( taskFlowMF );
     }
 
 
     @SuppressWarnings({ "MethodWithTooExceptionsDeclared" })
     @NotNull
     public AbstractQuoteCalculator<?> newORQCalculator(
         final @NotNull EntityManagerFactory emf,
         final @NotNull String offerTypeKey)
         throws ClassNotFoundException, IllegalAccessException, InstantiationException,
         NoSuchMethodException, InvocationTargetException {
         EntityManager em = emf.createEntityManager();
         try {
             if (offerTypeKey == null)
                 throw new IllegalArgumentException("Unknow offer type: " + offerTypeKey);
             AbstractQuoteCalculator<?> orqc = orqPark.getInstance(offerTypeKey);
             orqc.setConfigletProvider( this );
             return orqc;
         }
         finally {
             if (! em.isOpen())
                 em.close();
         }
     }
 
 
     /**
      * Waits until an instance on {@link #instances} with the key <tt>name</tt>, has been registered and returns it.
      *  
      * @param clazz the class the instance belongs to
      * @param name the name of the instance as denoted on the map {@link #instances}
      * @param <T> the class the instance will be casted to
      * @return an instance from the <tt>instances</tt> map
      */
 	public @NotNull  <T> T waitForInstance(@NotNull Class<T> clazz, @NotNull String name) {
         T instance;
         try { instance = getInstance(clazz, name); }
         catch (IllegalStateException e) { instance = null; }
         while (instance != null) {
             try {
                 Thread.sleep(INSTANCE_RETRIEVAL_INTERVAL);
             }
             catch (InterruptedException e) {
                 // intended
             }
             try { instance = getInstance(clazz, name); }
             catch (IllegalStateException e) { instance = null; }
         }
         return instance;
     }
 
 
     /* Adds an instance to the {@link #instances} map.
      * The name which will be mapped to the instance must not end with the keywords "HOME","Resource" or "ORQC".
      *
      * <p> Except them, there are more keywords which are not allowed.
      * See {@link #addInstance_ }, as this method will be invoked
 
      *
      * @param name the name which is to be mapped to the specified instance
      * @param obj the instance to be associated with the specified name
      */
     public synchronized void addInstance(@NotNull String name, @NotNull Object obj) {
         if (name.endsWith("Home") || name.endsWith("Resource") || name.endsWith("ORQC"))
             throw new IllegalArgumentException("Reserved instance name");
 
         addInstance_(name, obj);
 
     }
 
     
     /**
      * Adds an instance to the {@link #instances} map.
      * The name which will be mapped to the instance must not be equal to the keywords "out","err","args","em" or "emg".
      *
      * @param name the name which is to be mapped to the specified instance
      * @param obj the instance to be associated with the specified name
      */
     private void addInstance_(final String name, final Object obj) {
         if ("out".equals(name) || "err".equals(name) || "args".equals(name) || "em".equals(name)
             || "emg".equals(name))
             throw new IllegalArgumentException("Reserved instance name");
 
         if (instances.containsKey(name))
             throw new IllegalStateException("Name clash in instance registration: " + name);
         else
             instances.put(name, obj);
 
         logger.debug(getSystemName() + " addInstance: '" + name + '\'');        
     }
 
 
     /**
      * Returns the instance, which has been registered on {@link #instances} with the name <tt>name</tt>.
      * The instace will be casted to the parameter <tt>T</tt> of <tt>clazz</tt>.
      *
      * @param clazz the class the instance belongs to
      * @param name the name of the instance as denoted on the map {@link #instances}
      * @param <T> the class the instance will be casted to
      * @return an instance from the <tt>instances</tt> map
      */
     public synchronized @NotNull <T> T getInstance(@NotNull Class<? extends T> clazz,
                                                    @NotNull String name)
     {
         final Object obj = instances.get(name);
         if (obj == null)
             throw new
                   IllegalStateException("Null instance retrieved or invalid or unregistered name");
         return clazz.cast(obj);
     }
 
     /**
      * Creates a new EntityManager using <tt>emf</tt>.
      *
      * <p>Calls {@link #loadConfigletStates(javax.persistence.EntityManager)} and
      * {@link #createOrUpdateConfiglets(de.zib.gndms.model.common.ConfigletState[])} to load all configlets managed by
      * this EntityManager and update the {@link #configlets} map.
      * Old Configlets will be removed and shutted down using {@link #shutdownConfiglets()} 
      *
      * @param emf the factory the EntityManager will be created of
      */
 	public synchronized void reloadConfiglets(final EntityManagerFactory emf) {
 		ConfigletState[] states;
 		EntityManager em = emf.createEntityManager();
 		try {
 			states = loadConfigletStates(em);
 			createOrUpdateConfiglets(states);
 			shutdownOldConfiglets(em);
 		}
 		finally { if (em.isOpen()) em.close(); }
 	}
 
     /**
      * Loads all <tt>ConfigletStates</tt> managed by a specific <tt>EntityManager</tt> into an array.
      *
      * <p>Performs the query "listAllConfiglets" on the database and returns an array containing the result.
      *
      * @param emParam an EntityManager managing ConfigletStates
      * @return an array containing all ConfigletStates of the database
      */
 	@SuppressWarnings({ "unchecked", "JpaQueryApiInspection", "MethodMayBeStatic" })
 	private synchronized ConfigletState[] loadConfigletStates(final EntityManager emParam) {
 		final ConfigletState[] states;
 		emParam.getTransaction().begin();
 		try {
 			Query query = emParam.createNamedQuery("listAllConfiglets");
 			final List<ConfigletState> list = (List<ConfigletState>) query.getResultList();
 			Object[] states_ = list.toArray();
 			states = new ConfigletState[states_.length];
 			for (int i = 0; i < states_.length; i++)
 				states[i] = (ConfigletState) states_[i];
 			return states;
 		}
 		finally {
 			if (emParam.getTransaction().isActive())
 				emParam.getTransaction().commit();
 		}
 	}
 
     /**
      * Iterates through the <tt>ConfigletState</tt> array and either
      * updates the <tt>state</tt> of the corresponding <tt>Configlet</tt>, if already stored in the {@link #configlets} map,
      * or creates a new <tt>Configlet</tt> using {@link #createConfiglet(de.zib.gndms.model.common.ConfigletState)}
      * and stores it together with the name of the Configlet in the map.
      *
      * @param statesParam an array containing several ConfigletStates to be stored in the <tt>configlets</tt> map
      */
 	private synchronized void createOrUpdateConfiglets(final ConfigletState[] statesParam) {
 		for (ConfigletState configletState : statesParam) {
 			final String name = configletState.getName();
 			if (configlets.containsKey(name)) {
 				configlets.get(name).update(configletState.getState());
 			}
 			else {
 				final Configlet configlet = createConfiglet(configletState);
 				configlets.put(name, configlet);
 			}
 		}
 	}
 
     /**
      * Creates a <tt>Configlet</tt> out of a ConfigletState.
      *
      * <p>The created instance uses {@link #logger} as its <tt>Logger</tt> object.
      * The name and state of the new Configlet is taken from <tt>configParam</tt>. 
      *
      * @param configParam A ConfigletState to be converted to a Configlet
      *
      * @return a Configlet out of a ConfigletState
      */
 	@SuppressWarnings({ "FeatureEnvy" })
 	private synchronized Configlet createConfiglet(final ConfigletState configParam) {
 		try {
 			final Class<? extends Configlet> clazz = Class.forName(configParam.getClassName()).asSubclass(Configlet.class);
 			final Configlet instance = clazz.newInstance();
 			instance.init(logger, configParam.getName(), configParam.getState());
 			return instance;
 		}
 		catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 		catch (InstantiationException e) {
 			throw new RuntimeException(e);
 		}
 		catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 
     /**
      * Removes old configlets from the {@link #configlets} map.
      *
      * Checks for every <tt>Configlet</tt> in the map, if still exists in the database.
      * If not, the map entry will be removed and <tt>shutdown()</tt> invoked on the old configlet entry. 
      *
      * @param emParam an EntityManager managing <tt>Configlet</tt>s
      */
 	@SuppressWarnings({ "SuspiciousMethodCalls" })
 	private synchronized void shutdownOldConfiglets(final EntityManager emParam) {
 		Set<String> set = configlets.keySet();
 		Object[] keys = set.toArray();
 		for (Object name : keys) {
 			emParam.getTransaction().begin();
 			try {
 				if (emParam.find(ConfigletState.class, name) == null) {
 					Configlet let = configlets.get(name);
 					configlets.remove(name);
 					let.shutdown();
 				}
 			}
 			catch (RuntimeException e) {
 				logger.warn( "", e);
 			}
 			finally {
 				if (emParam.getTransaction().isActive())
 					emParam.getTransaction().commit();
 			}
 		}
 	}
 
     /**
      * Shuts down all configlets stored in the {@link #configlets} map
      */
 	synchronized void shutdownConfiglets() {
 		for (Configlet configlet : configlets.values())
 		    try {
 			    configlet.shutdown();
 		    }
 		    catch (RuntimeException e) {
 			    logger.warn( "", e);
 		    }		
 	}
 
     /**
      * Retrieves the configlet stored with the key <tt>name</tt> from {@link #configlets} map, casts it to a <tt>T</tt> class
      * and returns it.
      *
      * @param clazz the class the Configlet belongs to
      * @param name the name of the Configlet
      * @param <T> the class the instance will be casted to 
      * @return a casted configlet from the <tt>configlets</tt> map
      */
 	public <T extends Configlet> T getConfiglet(final @NotNull Class<T> clazz, final @NotNull String name) {
 		return clazz.cast(configlets.get(name));
 	}
 
 
     public @NotNull String getSystemName() {
         return systemName;
     }
 
     /**
      * Returns the value set for the environment variable <tt>GNDMS_TMP</tt>. If nothing denoted,
      * it will return the value of the enviroment variable <tt>TMPDIR</tt> instead.
      * If also not denoted, "/tmp" will be returned.
      *
     * @return the temp directory of the GNDMSystem according to enviroment variables
      */
 	@NotNull
 	@SuppressWarnings({ "HardcodedFileSeparator" })
 	public String getSystemTempDirName() {
 		String tmp = System.getenv("GNDMS_TMP");
 		tmp = tmp == null ?  ""  : tmp.trim();
 		if (tmp.length() == 0) {
 			tmp = System.getenv("TMPDIR");
 			tmp = tmp == null ?  ""  : tmp.trim();
 		}
 		if (tmp.length() == 0) {
 			tmp = "/tmp";
 		}
 		return tmp;
 	}
 
 
 // TODO test if this can be done with spring if yes remove
 //   /**
 //     * Binds certain classes to {@code this} or other corresponding instances
 //     *
 //     * @param binder binds several classe with certain fields.
 //     */
 //	public void configure(final @NotNull Binder binder) {
 //		// binder.bind(EntityManagerFactory.class).toInstance();
 //		binder.bind(BoundInjector.class).toInstance(boundInjector);
 //		binder.bind(SystemDirectory.class).toInstance(this);
 //		binder.bind( SystemInfo.class).toInstance(this);
 //		binder.bind(InstanceProvider.class).toInstance(this);
 //		binder.bind(ServiceHomeProvider.class).toInstance(this);
 //		binder.bind(TaskActionProvider.class).toInstance(this);
 //		binder.bind(ORQCalculatorProvider.class).toInstance(this);
 //		binder.bind(ConfigletProvider.class).toInstance(this);
 //		binder.bind(ModelUUIDGen.class).toInstance(uuidGen);
 //	}
 
     public final String DEFAULT_SUBGRID_NAME="gndms";
 
     @NotNull
     public String getSubGridName() {
         final DefaultConfiglet defaultConfiglet = getConfiglet(DefaultConfiglet.class, "gridconfig");
         if (defaultConfiglet == null)
             return DEFAULT_SUBGRID_NAME;
         else
             return defaultConfiglet.getMapConfig().getOption("subGridName", DEFAULT_SUBGRID_NAME);
     }
 
 
     private static class OfferTypeIndustrialPark<T extends KeyFactoryInstance<String, T>>
             extends IndustrialPark<String, String, T> {
 
         private OfferTypeIndustrialPark(
                 final @NotNull
                 KeyFactory<String, RecursiveKeyFactory<String, T>> factoryParam) {
             super(factoryParam);
         }
 
 
         @NotNull
         @Override
         public String mapKey(final @NotNull String keyParam) {
             return keyParam;
         }
     }
 
 	public @NotNull
     GNDMSInjector getSystemAccessInjector() {
 		return boundInjector.getInjector();
 	}
 }
