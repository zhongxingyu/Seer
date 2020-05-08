 package at.rovo.core.injection;
 
 import java.lang.ref.PhantomReference;
 import java.lang.ref.Reference;
 import java.lang.ref.ReferenceQueue;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.AccessibleObject;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import at.rovo.core.ClassFinder;
 import at.rovo.plugin.InjectionException;
 import at.rovo.annotations.Component;
 import at.rovo.annotations.ComponentId;
 import at.rovo.annotations.Inject;
 import at.rovo.annotations.ScopeType;
 
 /**
  * <p>
  * This class is an implementation of {@link IInjectionController}. Its task is
  * to inject instances of classes into {@link Inject}-annotated fields.
  * </p>
  * <p>
  * Therefore every class is scanned for a {@link Component} annotation and a
  * {@link ComponentId}-annotation if it was declared as component. If the first
  * annotation could not be found and {@link #initialize(Object)} was called, an
  * {@link InjectionException} will be raised as this method only accepts
  * {@link Component}-annotated objects.
  * </p>
  * <p>
  * If a {@link Component}-annotated object does not have a {@link ComponentId}
  * -annotation an {@link InjectionException} will be thrown as every
  * {@link Component} needs a unique {@link ComponentId}. This implementation
  * sets a unique identifier for every instantiated object. First it tries to set
  * the unique identifier via a set-method, which maps the fields name to get
  * plug the field name starting with an upper letter. This is due to inheritance
  * to allow parent-classes to receive their unique identifier. If there is no
  * such method, this implementation injects the identifier directly into the
  * field.
  * </p>
  * <p>
  * If this {@link Component} is valid and contains {@link Inject}-annotations
  * InjectionControllerImpl tries to load the needed classes and to inject valid
  * instances into the annotated fields. Currently only injections into fields
  * are possible. In case an interface is marked as injection-target the
  * application- class path is looked for some classes that implement this
  * interface. If only one implementing class could be found, this class gets
  * instantiated and injected into the field.
  * </p>
  * 
  * @see Component
  * @see ComponentId
  * @see Inject
  * @see ClassFinder#findImplementingClasses(Class, ClassLoader)
  * 
  * @author Roman Vottner
  */
 public enum InjectionControllerImpl implements IInjectionController
 {
 	/** Application of the enum singleton pattern **/
 	INSTANCE;
 	
 	/** The logger of this class **/
 	private static Logger logger = 
 			Logger.getLogger(InjectionControllerImpl.class.getName());
 	
 	/** Keeps track of singleton instances which should get injected into a 
 	 * <code>@Component</code> annotated class **/
 	private final Map<String, WeakReference<Object>> singletonRef = 
 			Collections.synchronizedMap(new HashMap<String, WeakReference<Object>>());
 	
 	/** Contains a phantom reference to keep track of unloading events for 
 	 * instances injected **/
 	private final Map<PhantomReference<Object>, String> initializations = 
 			Collections.synchronizedMap(new LinkedHashMap<PhantomReference<Object>, String>());
 
 	/** Will contain a reference to objects that have been removed by the 
 	 * garbage collector **/
 	private final ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
 	
 	/** The thread that deals with checking if a reference got deleted **/
 	private Thread cleanUpThread = null;
 	
 	/** Keeps track of the number of instantiated classes. The ID will be 
 	 * assigned to the <code>@Component</code> annotated class as its 
 	 * <code>@ComponentId</code>.**/
 	private Long uniqueId = 0L;
 	
 	/** If set to true will terminate the cleanUp thread **/
 	private volatile boolean done = false;
 	/** states if the cleanup state is currently sleeping **/
 	private volatile boolean isSleeping = false;
 	
 	// lock to send a thread to sleep and wake up again
 	final Lock lock = new ReentrantLock();
 	final Condition noReferenceToMonitorAvailable = lock.newCondition();
 	
 	/**
 	 * <p>
 	 * Hide the default constructor.
 	 * </p>
 	 * <p>
 	 * On initializing a background thread will be created that listens if a 
 	 * certain class gets unloaded. It therefore makes use of a 
 	 * {@link ReferenceQueue} which contains a reference if the object was 
 	 * removed by the garbage collector.
 	 * </p>
 	 */
 	private InjectionControllerImpl()
 	{
 		this.cleanUpThread = new Thread(new Runnable() 
 		{
 			public void run()
 			{
 				try
 				{
 					while (!done)
 					{
 						// call to remove blocks until an object is available 
 						// for removal
 						Reference<?> ref = refQueue.remove(500);
 						synchronized (initializations)
 						{
 							// check if the map containing the already 
 							// initialized objects has a reference to an 
 							// unloaded object
 							if (initializations.containsKey(ref))
 							{
 								logger.log(Level.INFO, "Unloading object {0}", 
 										new Object[] { initializations.get(ref) });
 								String name = initializations.remove(ref);
 								
 								if (name != null && name.contains("@"))
 								{
 									name = name.substring(0, name.indexOf("@"));
 									synchronized (singletonRef)
 									{
 										if (singletonRef.containsKey(name))
 										{
 											logger.log(Level.INFO, "Unloading singleton {0}", 
 													new Object[] { name });
 											singletonRef.remove(name);
 										}
 									}
 								}
 							}
 						}
 						
 						// pause the thread if both maps are empty --> no 
 						// injected classes available; either nothing got loaded
 						//  or everything is unloaded
 						if (initializations.isEmpty() && singletonRef.isEmpty())
 						{
 							synchronized (cleanUpThread)
 							{
 								// wait till new references arrive
 								isSleeping = true;
 								Thread.currentThread().wait();
 								noReferenceToMonitorAvailable.await();
 								isSleeping = false;
 							}
 						}
 					}
 				}
 				catch (InterruptedException e)
 				{
 					if (!done)
 						e.printStackTrace();
 				}
 			}
 		});
 		this.cleanUpThread.setName("CleanUp");
 		this.cleanUpThread.setDaemon(true);
 		this.cleanUpThread.start();
 	}
 
 	/**
 	 * <p>
 	 * Signals the cleanUp thread to terminate.
 	 * </p>
 	 */
 	public void close()
 	{
 		this.done = true;
 		
 		if (isSleeping)
 		{
//			synchronized(this.cleanUpThread)
//			{
//				this.cleanUpThread.notify();
//			}
 			// let the clean up thread finish gracefully
//			noReferenceToMonitorAvailable.signal();
 		}
 	}
 	
 	@Override
 	public Object initialize(Object obj) throws InjectionException
 	{
 		// every Object to initialize has to be annotated with "@Component"
 		Class<?> clazz = obj.getClass();
 		if (!clazz.isAnnotationPresent(Component.class))
 			throw new InjectionException("Object '"+obj.toString()+"' is not a component: "+obj);
 		
 		// check for multiple initialization calls for the same objects
 		synchronized (initializations)
 		{
 			if (!this.initializations.containsValue(obj.toString()))
 			{
 				logger.log(Level.INFO, "{0} loaded with class loader: {1}", 
 						new Object[] {obj, obj.getClass().getClassLoader()});
 				PhantomReference<Object> ref = new PhantomReference<>(obj, refQueue);
 				boolean isEmpty = this.initializations.isEmpty();
 				this.initializations.put(ref, obj.toString());
 				// as we've added some data to the map and it was empty 
 				// before wake-up the cleanup thread in order to listen to
 				// receive phantom references if the garbage collector finalizes 
 				// the object
 				if (isEmpty)
 				{
 					synchronized (this.cleanUpThread)
 					{
 						this.cleanUpThread.notify();
 					}
 					// signal that new references to monitor are available
 					noReferenceToMonitorAvailable.signal();
 				}
 				obj = this.initializeObject(obj, true);
 			}
 		}
 		return obj;
 	}
 
 	/**
 	 * <p>
 	 * This method checks an object for inconsistencies like missing or multiple
 	 * {@link ComponentId}-fields and propagates injection of needed instances
 	 * to {@link #injectFields(Object, Field, List)}
 	 * </p>
 	 * 
 	 * @param obj
 	 *            The @Component annotated object to inject elements into @Inject
 	 *            annotated fields
 	 * @param isOriginCall
 	 *            Defines if the call is a origin invocation or an invocation of
 	 *            a ancestor object; true means an origin invocation takes
 	 *            place, false if an ancestor is injected
 	 */
 	private Object initializeObject(Object obj, boolean isOriginCall)
 	{
 		// every Object to initialize has to be annotated with "@Component"
 		Class<?> clazz = obj.getClass();
 		if (!clazz.isAnnotationPresent(Component.class))
 			return null;
 		
 		// every "@Component" annotated class needs one(!) "@ComponentId" of 
 		// type Long
 		int  foundId = 0;
 		Field componentId = null;
 		boolean required = false;
 		List<Field> injectFields = new ArrayList<Field>();
 		
 		// catch the case when a plug-in requires a further plug-in to be loaded 
 		// but the plug-in was not loaded - therefore getDeclaredFields throws a 
 		// NoClassDefFoundError
 		Field[] fields = null;
 		try
 		{
 			fields = clazz.getDeclaredFields();
 		}
 		catch (NoClassDefFoundError e)
 		{
 			logger.log(Level.SEVERE, "Could not find missing dependency plugin for {0}", 
 					new Object[] { e.getLocalizedMessage().substring(1).replace("/",  ".")});
 			throw new InjectionException("Missing dependency plugin could not be found! Plugin contains unresolved dependency to: "+e.getLocalizedMessage().substring(1).replace("/", "."));
 		}
 		// all declared fields loaded
 		for (Field f : fields)
 		{
 			if (f.isAnnotationPresent(ComponentId.class))
 			{
 				if (!f.getType().equals(Long.class))
 				{
 					logger.log(Level.WARNING, "Invalid ComponentId type found");
 					throw new InjectionException("Invalid ComponentId-Type found. ComponentId needs to be of type Long");
 				}
 				foundId++;
 				componentId = f;
 			}
 			if (f.isAnnotationPresent(Inject.class))
 			{
 				Inject inject = f.getAnnotation(Inject.class);
 				required = inject.required();
 				injectFields.add(f);
 			}
 		}
 		if (foundId == 0)
 		{
 			logger.log(Level.WARNING, "No field annotated with @ComponentId found");
 			throw new InjectionException("No field annotated with @ComponentId found.");
 		}
 		else if (foundId > 1)
 		{
 			logger.log(Level.WARNING, "More than one @ComponentId-fields found");
 			throw new InjectionException("More than one @ComponentId-fields found.");
 		}
 		
 		// so we do have a component-class
 		// check if we have a Singleton or Prototype class
 		Component comp = clazz.getAnnotation(Component.class);
 		if (comp.scope().equals(ScopeType.SINGLETON))
 		{
 			// a singleton is created only once
 			synchronized (singletonRef)
 			{
 				if (this.singletonRef.containsKey(obj.getClass().getName()))
 				{
 					if (isOriginCall)
 					{
 						// throw new InjectionException("Cannot initialize further instances of a singleton class: "+obj);
 						WeakReference<?> ref = this.singletonRef.get(obj.getClass().getName());
 						Object o = ref.get();
 						logger.log(Level.INFO, "retrieving singleton {0} from list: {1}", 
 								new Object[] { obj.getClass().getName(), o });
 						if (o != null)
 						{
 							obj = o;
 						}
 						else
 						{
 							logger.log(Level.INFO, "removing {0} from singleton-list - its reference is null", 
 									new Object[] { obj.getClass().getName() });
 							this.singletonRef.remove(obj.getClass().getName());
 							obj = null;
 						}
 					}
 				}
 				else
 				{
 					WeakReference<Object> ref = new WeakReference<>(obj);
 					logger.log(Level.INFO, "adding {0} to singleton-list as {1}", 
 							new Object[] { obj, ref });
 					
 					this.singletonRef.put(obj.getClass().getName(), ref);
 					this.injectFields(obj, componentId, injectFields, required);
 				}
 			}
 		}
 		else
 			this.injectFields(obj, componentId, injectFields, required);
 		
 		return obj;
 	}
 	
 	/**
 	 * Returns all fields of super classes which have been annotated with
 	 * {@Inject}.
 	 * 
 	 * @param obj
 	 *            Instance, fields of super classes should be returned for
 	 * @param required
 	 *            Defines if exceptions should be thrown in case of errors. true
 	 *            specifies that exceptions should be thrown, false prevents
 	 *            exceptions from being propagated
 	 * @return {@link List} of {@link Field}s which have been annotated with
 	 *         {@link Inject} in super classes.
 	 */
 	private List<Field> getSuperClassFields(Object obj, boolean required)
 	{
 		List<Field> superClassFields = new ArrayList<Field>();
 		Class<?> clazz = obj.getClass();
 		for (Class<?> supClass = clazz.getSuperclass(); supClass != null; supClass = supClass.getSuperclass())
 		{
 			if (supClass!= null && supClass.isAnnotationPresent(Component.class))
 			{
 				logger.log(Level.INFO, "Found Super-Class of {0} with Component-annotation: {1}", 
 						new Object[] {clazz.getName(), supClass.getCanonicalName()});
 				int found = 0;
 				for (Field f : supClass.getDeclaredFields())
 				{
 					if (f.isAnnotationPresent(ComponentId.class))
 					{
 						if (!f.getType().equals(Long.class) && required)
 						{
 							logger.log(Level.WARNING, "Invalid ComponentId-Type found. ComponentId needs to be of type Long");
 							throw new InjectionException("Invalid ComponentId-Type found. ComponentId needs to be of type Long");
 						}
 						found++;
 					}
 					if (f.isAnnotationPresent(Inject.class))
 						superClassFields.add(f);
 				}
 				if (found == 0 && required)
 				{
 					logger.log(Level.WARNING, "No field annotated with @ComponentId found.");
 					throw new InjectionException("No field annotated with @ComponentId found.");
 				}
 				else if (found > 1 && required)
 				{
 					logger.log(Level.WARNING, "More than one @ComponentId-fields found.");
 					throw new InjectionException("More than one @ComponentId-fields found.");
 				}
 			}
 		}
 		return superClassFields;
 	}
 	
 	/**
 	 * <p>
 	 * Injects instances into {@link Field}s which have been annotated with
 	 * {@link Inject}. This method sets the {@link ComponentId} for a
 	 * {@link Component}-annotated class too.
 	 * </p>
 	 * <p>
 	 * If the field to inject is an interface an implementing class is being
 	 * looked for via the {@link ClassFinder#findImplementingClasses}-method. If
 	 * none or more than one could be found an InjectionException will be thrown
 	 * if the injection is required
 	 * </p>
 	 * 
 	 * @param obj
 	 *            Object whose field(s) need to be injected.
 	 * @param componentId
 	 *            {@link ComponentId}-annotated field of this object.
 	 * @param injectFields
 	 *            {@link List} of {@link Field}s to inject instances to.
 	 * @see ClassFinder#findImplementingClasses(Class, ClassLoader)
 	 */
 	private void injectFields(Object obj, Field componentId, List<Field> injectFields, boolean required)
 	{
 		try
 		{
 			// and add its annotated fields to this object
 			// as those fields need to be injected too
 			List<Field> superClassFields = this.getSuperClassFields(obj, required);
 			for (Field f : superClassFields)
 				if (!injectFields.contains(f))
 					injectFields.add(f);
 			
 			for (Field f : injectFields)
 			{
 				Inject inject = f.getAnnotation(Inject.class);
 				
 				// Test if a specified sub-type should be injected 
 				// if none was specified use the fields type
 				Class<?> toInject = null;
 				if (!inject.specificType().equals(Inject.DEFAULT.class))
 					toInject = inject.specificType();
 				else
 					toInject = f.getType();
 				logger.log(Level.INFO, "Injecting: {0} into {1}", 
 						new Object[] { toInject.getCanonicalName(), f.getName()});
 				
 				// Set the specified type (or the fields type) as to be loaded
 				Class<?> toLoad = toInject;
 				// I don't handle abstract classes here
 				if (toInject.isInterface())
 				{
 					// As interfaces can't be instantiated we have to find some
 					// implementations of this interface
 					List<Class<?>> implementingClasses = 
 							ClassFinder.INSTANCE.findImplementingClasses(toInject, 
 									obj.getClass().getClassLoader());
 				
 					// If no implementations could be found, raise an error
 					// If there are more than one implementations to use, there
 					// is no certainty of which one to use, so raise an error too
 					if (implementingClasses.isEmpty() && inject.required())
 						throw new InjectionException("No implementation for "
 								+ toInject.getCanonicalName()+" could be found!");
 					else if (implementingClasses.size() > 1 && inject.required())
 						throw new InjectionException("More than one implementation for "
 								+ toInject.getCanonicalName()+" was found. "
 								+ "Please specify which implementation you like to use "
 								+ "bye adding the specificType-attribute to the "
 								+ "annotation.");
 					
 					// as there is only one implementation expected, set its 
 					// class to be loaded instead of the interface-class
 					if (!implementingClasses.isEmpty() || inject.required())
 						toLoad = implementingClasses.get(0);
 					}
 				
 				Object injObj = null;
 				// Test if this class is a SINGLETON-component, if so, try to 
 				// load the object from the cache
 				Component comp = toLoad.getAnnotation(Component.class);
 				// an injection-field for an interface was found which was 
 				// declared as not required, so no exception may be thrown
 				if (comp == null)
 					return;
 				
 				if (comp.scope().equals(ScopeType.SINGLETON))
 				{
 					injObj = this.getSingletonInstance(toLoad);
 					if (injObj == null)
 					{
 						try
 						{
 							Class<?>[] c = null;
 							Method getInstance = toLoad.getMethod("getInstance", c);
 							Object[] o = null;
 							// create a new instance of the singleton class
 							getInstance.invoke(null, o);
 							// as the object was initialized and stored in the singleton list
 							// before, we now should have access to it
 							synchronized (singletonRef)
 							{
 								WeakReference<?> ref = this.singletonRef.get(toLoad.getName());
 								Object _o = ref.get();
 								logger.log(Level.INFO, "retrieving singleton {0} for injection {1}", 
 										new Object[] { obj.getClass().getName(), o });
 								if (_o != null)
 									injObj = _o;
 								else
 								{
 									logger.log(Level.INFO, "removing {0} from singleton-list - can't be injected due to null", 
 											new Object[] { toLoad.getName() });
 									this.singletonRef.remove(toLoad.getName());
 									injObj = null;
 								}
 							}
 						}
 						catch (SecurityException e)
 						{
 							e.printStackTrace();
 						}
 						catch (NoSuchMethodException e)
 						{
 							e.printStackTrace();
 						}
 						catch (InvocationTargetException e)
 						{
 							e.printStackTrace();
 						}
 					}
 				}
 
 				// either this class is scope PROTOTYPE or the SINGLETON was not
 				// loaded before
 				if (injObj == null)
 					// we use the class loader of the original object
 					// if this is a class from a plug-in, the actual class 
 					// loader is the plug-in class loader              
 					injObj = this.loadClass(toLoad, inject.required());
 				
 				// start injecting sub-injections recursively
 				this.initializeObject(injObj, false);
 				
 				String methodName = "set"+f.getName().substring(0, 1).toUpperCase()+f.getName().substring(1);
 				
 				try
 				{
 					Method m = obj.getClass().getMethod(methodName, f.getType());
 					m.setAccessible(true);
 					m.invoke(obj, injObj);
 				}
 				catch(NoSuchMethodException nsmEx)
 				{
 					f.setAccessible(true);
 					f.set(obj, injObj);
 				}
 				catch (InvocationTargetException e)
 				{
 					e.printStackTrace();
 				}
 			}
 			// Set the unique id
 			componentId.setAccessible(true);
 			if (componentId.get(obj) == null)
 			{
 				Long id = ++uniqueId;
 				String compId = "set"+componentId.getName().substring(0, 1).toUpperCase()+componentId.getName().substring(1);
 				try
 				{
 					Method m = obj.getClass().getMethod(compId, Long.class);
 					m.setAccessible(true);
 					m.invoke(obj, id);
 				}
 				catch (NoSuchMethodException e)
 				{
 					componentId.set(obj, id);
 				}
 				catch (Exception e)
 				{
 					throw new InjectionException(e);
 				}
 				logger.log(Level.INFO, "{0} has now id {1}", 
 						new Object[] { obj.toString(), id});
 			}
 		}
 		catch (IllegalArgumentException ex)
 		{
 			throw new InjectionException(ex);
 		}
 		catch (IllegalAccessException ex)
 		{
 			throw new InjectionException(ex);
 		}
 	}
 	
 	/**
 	 * <p>
 	 * Instantiates a new instance of {@link Class} provided by the classToLoad
 	 * argument. Therefore the default-constructor will be used.
 	 * </p>
 	 * <p>
 	 * If this class could not get instantiated correctly a
 	 * {@link InjectionException} will be thrown unless required was set to
 	 * false.
 	 * </p>
 	 * 
 	 * @param classToLoad
 	 *            {@link Class} which needs to be loaded
 	 * @param required
 	 *            If set to true, all exceptions will be forwarded.
 	 * @return An initialized object of the provided {@link Class}
 	 */
 	@SuppressWarnings("unchecked")
 	private <T> T loadClass(Class<T> classToLoad, boolean required)
 	{
 		try
 		{   	
 			synchronized (singletonRef)
 			{
 				if(this.singletonRef.containsKey(classToLoad.getName()))
 				{
 					WeakReference<?> ref = this.singletonRef.get(classToLoad.getName());
 					T t = (T)ref.get();
 					logger.log(Level.INFO, "loading class for singleton {0}: {1}", 
 							new Object[] { classToLoad.getName(), t });
 					return t;
 				}
 				else
 				{
 					Constructor<T>[] cons = (Constructor<T>[])classToLoad.getDeclaredConstructors(); 
 					// Change the accessible property for all constructors
 					AccessibleObject.setAccessible(cons, true); 
 					// iterate through all defined constructors
 					Constructor<T> defaultConstructor = null;
 					for (int i = 0; i < cons.length; i++) 
 					{
 						if (cons[i].getParameterTypes().length == 0) 
 						{
 							defaultConstructor = cons[i];
 							break;
 						}
 					}
 					
 					Object[] o = null;
 					T injObj = defaultConstructor.newInstance(o);
 					if (!classToLoad.isInstance(injObj))
 					{
 						logger.log(Level.WARNING, "Could not instantiate {0}", 
 								new Object[] {classToLoad.getName()});
 						throw new InjectionException("Could not instantiate "+
 								classToLoad.getName()+"!");
 					}
 					
 					logger.log(Level.INFO, "Initialized object: {0} loader: {1}", 
 							new Object[] {injObj, injObj.getClass().getClassLoader()});
 					return injObj;
 				}
 			}
 		}
 		catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException e) 
 		{
 			// if field is required and there are any problems injecting
 			// the field throw an exception
 			logger.log(Level.WARNING, "Couldn't instantiate class - Reason: {0}", 
 					new Object[] {e.getLocalizedMessage()});
 			if (required)
 				throw new InjectionException(e);
 		}
 		return null;
 	}
 	
 	@Override
 	public <T> T getSingletonInstance(Class<T> clazz) throws InjectionException
 	{
 		synchronized (singletonRef)
 		{
 			WeakReference<?> ref = this.singletonRef.get(clazz.getName());
 			if (ref != null)
 			{
 				Object obj = ref.get();
 				logger.log(Level.INFO, "retrieving singleton instance of class {0}: {1}", 
 						new Object[] { clazz.getName(), obj });
 				if (obj != null)
 				{
 					@SuppressWarnings("unchecked")
 					T ret = (T)obj;
 					return ret;
 				}
 			}
 			return null;
 //			throw new InjectionException("Could not inject singleton: "+clazz.getName()+" as singleton is null");
 		}
 	}
 }
