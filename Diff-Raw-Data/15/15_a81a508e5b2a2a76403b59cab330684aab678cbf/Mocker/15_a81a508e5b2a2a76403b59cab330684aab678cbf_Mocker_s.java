 package cz.zcu.kiv.bp.unimocker;
 
 //import ifcs.IPrinter;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.lang.reflect.ConstructorUtils;
 import org.apache.commons.lang.reflect.MethodUtils;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.springframework.osgi.context.BundleContextAware;
 import org.xml.sax.SAXException;
 
 import cz.zcu.kiv.bp.datatypes.bindings.TCustomTypeData;
 import cz.zcu.kiv.bp.datatypes.bindings.TCustomTypesSupport;
 import cz.zcu.kiv.bp.datatypes.bindings.TExternalFactory;
 import cz.zcu.kiv.bp.datatypes.bindings.TImportedType;
 import cz.zcu.kiv.bp.datatypes.bindings.TListOfValuesOfImportedTypes;
 import cz.zcu.kiv.bp.datatypes.bindings.TValueOfImportedType;
 import cz.zcu.kiv.bp.datatypes.bindings.adapted.CustomTypesRegistry;
 import cz.zcu.kiv.bp.probe.IProbe;
 import cz.zcu.kiv.bp.probe.NoSuchBundleException;
 import cz.zcu.kiv.bp.unimocker.bindings.IScenario;
 import cz.zcu.kiv.bp.unimocker.bindings.Scenario;
 import cz.zcu.kiv.bp.unimocker.bindings.TCodeInjection;
 import cz.zcu.kiv.bp.unimocker.bindings.TSimulatedService;
 import cz.zcu.kiv.bp.unimocker.bindings.adapted.BundlesMap;
 import cz.zcu.kiv.bp.unimocker.bindings.adapted.Invocation;
 import cz.zcu.kiv.bp.unimocker.bindings.adapted.InvokedMethod;
 
 /**
  * IMocker implementation. Implements local mockup builder.
  * When mockup is created, it's also registered as OSGi service.
  * @author Michal
  */
 public class Mocker implements IMocker, BundleContextAware
 {
 	private Mocker _ = this;
 
 	/**
 	 * OSGi bundle context
 	 */
 	private BundleContext context;
 	
 	/**
 	 * UniMockerBinding loader
 	 */
 	private IScenario scenarioProject;
 	
 	/**
 	 * Custom types support structure
 	 */
 	private TCustomTypesSupport custTypesStruct;
 	
 	private Map<String, Object> customTypeValues = new HashMap<>();
 	
 	private Map<String, Class<?>> customTypeClasses = new HashMap<>();
 	
     /**
      * ServiceRegistrations for mocks registered as OSGi services.
      */
     private List<ServiceRegistration<?>> serviceRegistrations = new LinkedList<ServiceRegistration<?>>();
 	
 	/**
 	 * OSGI environment probe
 	 */
 	private IProbe envProbe;
     
     /**
      * EnvProbe setter
      */
     public void setEnvProbe(IProbe envProbe)
     {
     	_.envProbe = envProbe;
     }
 	
 	/**
      * Tries to load described classes from the given bundle without
      * throwing exception in case of non-existing class.
      * @param mockedBundle which should be probed
      * @param classesToFind array of class names to find
      * @return array of loaded classes or null if loading was not successful
      */
 	private Class<?>[] findClassesToMock(Bundle mockedBundle, String[] classesToFind)
 	{
 		Class<?>[] ret = null;
 		try
 		{
//			ret = _.findClassesInBundle(mockedBundle, classesToFind);
 			ret = _.envProbe.findClassesInBundle(mockedBundle, classesToFind);
 		}
 		catch (ClassNotFoundException ignore)
 		{
 			System.out.printf(
 				"Class \"%s\" has not been found in bundle \"%s\".%n",
 				ignore.getMessage(),
 				mockedBundle
 			);
 		}
 		return ret;
 	}
 	
 	/**
 	 * Creates mockup as instance of Java Proxy from given class and UniHandler object.
 	 * Handler object is filled with invocation expectation. Object is registered as OSGi service.
 	 * Service registration descriptor is stored for proper unregistration when bundle stops.
 	 * @param clazz class to mock
 	 * @param returns map of invocation expectations
 	 */
 	private void createService( Class<?> clazz, Object serviceImpl)
 	{
 		// register object as service
 		// TODO implement the possibility to add property map for service object
 		ServiceRegistration<?> reg = _.context.registerService(clazz.getCanonicalName(), serviceImpl, null);
 		_.serviceRegistrations.add(reg);
 	}
 
 	/**
 	 * Creates mockup as instance of Java Proxy from given class and UniHandler object.
 	 * Handler object is filled with invocation expectation. 
 	 * @param clazz
 	 * @param returns
 	 * @param ignoreUndefMethods
 	 * @param ignoreUndefPossibs
 	 * @return create mockup object
 	 */
 	private Object createMockup(
 		Class<?> clazz,
 		Map<Method, Map<Object[], Object>> returns,
 		Map<Method, TCodeInjection> injections,
 		boolean ignoreUndefMethods,
 		boolean ignoreUndefPossibs)
 	{
 		// prepare handler
 		UniHandler handler = new UniHandler(clazz, returns, ignoreUndefMethods, ignoreUndefPossibs);
 		handler.setEnvProbe(_.envProbe);
 		handler.setInjectedCode(injections);
 		
 		// create mockup object
 		Object mockup = Proxy.newProxyInstance(
 			clazz.getClassLoader(),
 			new Class<?> [] { clazz },
 			handler
 		);
 		return mockup;
 	}
 
 	/**
 	 * Build invocation expectations map for mockup object. Throws NoSuchMethodException when mocked
 	 * class does not have method that match description in description map.
 	 * @param clazz Class that will be mocked.
 	 * @param simulation Expectations description as binding object.
 	 * @return Expectations map
 	 * @throws NoSuchMethodException
 	 */
 	private Map<Method, Map<Object[], Object>> buildInvocationPossibilitiesForClass(
 		Class<?> clazz,
 		TSimulatedService simulation)
 	throws NoSuchMethodException
 	{
 		// Contains return values for all described methods.
 		Map<Method, Map<Object[], Object>> returns = new HashMap<>();
 		for (InvokedMethod methodToFind : simulation.getMethods())
 		{
 			for (Invocation inv : methodToFind.getInvocations())
 			{
 				Class<?>[] types = inv.getArguments().getTypes();
 				Object[] values = inv.getArguments().toArray();
 				_.replaceCustomTypes(types, values);
 				
 				// Tries to match method w/o wrapper classes for primitive types.
 				Method foundMethod = MethodUtils.getMatchingAccessibleMethod(
 					clazz,
 					methodToFind.getName(),
 					types
 				);
 				if (foundMethod == null)
 				{ // class does not provide required method
 					throw new NoSuchMethodException(
 						String.format(
 							"Class %s does not provide any method %s with arguments %s",
 							clazz.getCanonicalName(),
 							methodToFind.getName(),
 							Arrays.deepToString(types)
 						)
 					);
 				}
 				System.out.printf(
 					"class: %s method: %s/wanted method: %s (%s)%n",
 					clazz,
 					foundMethod,
 					methodToFind.getName(),
 					Arrays.deepToString(types)
 				);
 				// If this method has not yet been described, create new collection.
 				if (!returns.containsKey(foundMethod))
 				{ // new method
 					returns.put(foundMethod, new HashMap<Object[], Object>());
 				}
 				// Contains return values for current method and all possible invocation arguments. 
 				Map<Object[], Object> methodsInvocationPossibilities = returns.get(foundMethod);
 				methodsInvocationPossibilities.put(
 					values,
 					inv.getReturnValue().getValue()
 				);
 			}
 		}
 
 		return returns;
 	}
 
 	/**
 	 * bundles destroy method - Unregister all created mockup services.
 	 * @throws Exception
 	 */
 	public void destroy() throws Exception
 	{		
 		// unregister mockups
 		for (ServiceRegistration<?> reg : _.serviceRegistrations)
 		{
 			System.out.println("Unregistering service: " + reg);
 			reg.unregister();
 		}
 	}
 
 	/**
 	 * Iterates over the loaded scenario and creates described mockups.
 	 * Mockups are filled with described invocation possibilities and their return values.
 	 */
 	@Override
 	public void mock()
 	{
 		if (_.scenarioProject == null)
 		{ // scenario has not been loaded
 			throw new IllegalStateException("Mockup scenario has not been loaded."); 
 		}
 		BundlesMap scenario = _.scenarioProject.getSimulatedComponents();
 
 		for (Entry<String, HashMap<String, List<TSimulatedService>>> bundle : scenario.entrySet())
 		{
 			Bundle mockedBundle;
 			try
 			{
 				mockedBundle = _.envProbe.findBundle(bundle.getKey());
 			}
 			catch (NoSuchBundleException e)
 			{ // bundle described in scenario has not been found in current context
 				System.out.printf("Bundle %s has not been found! Skipping bundle.%n", bundle.getKey());
 				continue;
 			}
 
 			String[] classesToFind = bundle.getValue().keySet().toArray(new String[0]);
 			Class<?>[] mockedClasses = _.findClassesToMock(mockedBundle, classesToFind);
 			if (mockedClasses == null || mockedClasses.length != classesToFind.length)
 			{ // not all classes that should be mocked has been found
 				System.out.printf(
 					"Not all classes to mock has been found.%nrequired: %s%nfound: %s%n",
 					Arrays.toString(classesToFind),
 					mockedClasses == null ? Arrays.deepToString(mockedClasses) : "none"
 				);
 				continue;
 			}
 			
 			// try to create mockup objects
 			for (Class<?> clazz : mockedClasses)
 			{
 				try
 				{
 					// find all described invocations for current class
 					List<TSimulatedService> simulation = bundle.getValue().get(clazz.getName());
 					for (TSimulatedService service : simulation)
 					{
 						Map<Method, Map<Object[], Object>> returns = _.buildInvocationPossibilitiesForClass(clazz, service);
 						Map<Method, TCodeInjection> injections = _.buildCodeInjections(clazz, service);
 						
 						Object srv = _.createMockup(
 							clazz,
 							returns,
 							injections,
 							service.isIgnoreUndefinedMethods(),
 							service.isIgnoreUndefinedPossibilities()
 						);
 							
 						// building mockup
 						_.createService(clazz, srv);
 					}
 				}
 				catch (NoSuchMethodException ex)
 				{ // described method not been found in current class
 					System.out.printf("%s -> skipping%n", ex.getMessage());
 				}
 			}
 		}
 	}
 	
 	private Map<Method, TCodeInjection> buildCodeInjections(
 		Class<?> clazz,
 		TSimulatedService service)
 	throws NoSuchMethodException
 	{
 		Map<Method, TCodeInjection> ret = new HashMap<>();
 		
 		for (InvokedMethod method : service.getMethods())
 		{
 			// invoked method has to have at least one invocation
 			if (method.getInvocations().size() < 1)
 			{
 				throw new IllegalStateException("Mocked method has no invocation described.");
 			}
 			Invocation inv = method.getInvocations().get(0);
 			Class<?>[] types = inv.getArguments().getTypes();
 			Object[] values = inv.getArguments().toArray();
 			_.replaceCustomTypes(types, values);
 			
 			// Tries to match method w/o wrapper classes for primitive types.
 			Method mockedMethod = MethodUtils.getMatchingAccessibleMethod(
 				clazz,
 				method.getName(),
 				types
 			);
 			if (mockedMethod == null)
 			{ // class does not provide required method
 				if (!service.isIgnoreUndefinedMethods())
 				{ // scenario says not to ignore undefined methods
 					throw new NoSuchMethodException(
 						String.format(
 							"Class %s does not provide any method %s with arguments %s",
 							clazz.getCanonicalName(),
 							method.getName(),
 							Arrays.deepToString(types)
 						)
 					);
 				}
 				else
 				{ // scenario says to ignore undefined methods
 				  // go to next method
 					continue;
 				}
 			}
 			
 			TCodeInjection injectedCode = method.getInjectedCode();
 			if (injectedCode != null)
 			{
 				ret.put(mockedMethod, injectedCode);
 			}
 		}
 		
 		return ret;
 	}
 
 	private void replaceCustomTypes(Class<?>[] types, Object[] values)
 	{
 		if (types.length != values.length)
 		{
 			throw new IllegalStateException(
 				"Types and values must have same length."
 			);
 		}
 		for (int i = 0; i < types.length; i++)
 		{
 			if (types[i] == TCustomTypeData.class)
 			{
 				if (!(values[i] instanceof TCustomTypeData))
 				{
 					throw new IllegalStateException(
 						"Values argumetn has to contain instances of TCustomTypeData on the possitions," +
 						"where types argument contains Class<TCustomTypeData>."
 					);
 				}
 				TCustomTypeData parameterValue = (TCustomTypeData) values[i];
 				types[i] = _.customTypeClasses.get(parameterValue.getRef().getId());
 				values[i] = _.customTypeValues.get(parameterValue.getRef().getId());
 			}
 		}
 	}
 	
 	@Override
 	public void diag()
 	{
 		if (_.scenarioProject == null)
 		{
 			System.out.println("Mockup scenario has not been loaded.");
 			return;
 		}
 		_.scenarioProject.diag();
 	}
 
 	@Override
 	public void loadFile(String fileName)
     throws JAXBException, SAXException, IOException
 	{
 		System.out.println("Nacitam soubor: " + fileName);
 		_.scenarioProject = new Scenario();
 		_.scenarioProject.loadFile(fileName);
 		
 		_.custTypesStruct = _.scenarioProject.getCustomTypesSupportStructure();
 		if (_.custTypesStruct == null)
 		{ // custom types has not been defined so we create empty description maps, so that we don't have to check existence of those maps
 			_.custTypesStruct = new TCustomTypesSupport();
 			_.custTypesStruct.setListOfTypes(new CustomTypesRegistry());
 			_.custTypesStruct.setListOfValues(new TListOfValuesOfImportedTypes());
 		}
 		_.loadCustomTypesAndValues();
 	}
 
 	private void loadCustomTypesAndValues() throws JAXBException
 	{
 		for (TValueOfImportedType value : _.custTypesStruct.getListOfValues().getValues())
 		{
 			System.out.println("loading type " + value.getType());
 			String valueId = value.getId();
 			String typeName = value.getType();
 			TImportedType typeDescription = _.custTypesStruct.getListOfTypes().get(typeName);
 			_.checkTypeNameEquality(typeName, typeDescription);
 
 			
 			Object[] argumentValues = value.getArguments().toArray();
 			Class<?>[] argumentTypes = value.getArguments().getTypes();
 			// prepare empty data for case the loading fails
 			Object customValue = null;
 			Class<?> customClazz = void.class;
 			// string describing OSGi bundle
 			String exportingBundleName = String.format(
 				"%s:%s",
 				typeDescription.getBundle(),
 				typeDescription.getVersion()
 			);
 			try
 			{
 				Bundle bndl = _.envProbe.findBundle(exportingBundleName);
 				customClazz = _.envProbe.findClassInBundle(bndl, typeDescription.getCannonicalName());
 
 				if ((customClazz.isInterface() || Modifier.isAbstract(customClazz.getModifiers())) && typeDescription.getFactory().getExternal() == null)
 				{ // Can not directly instantiate interface
 					throw new InstantiationException(String.format(
 						"Custom type %s is either interface of abstract class but factory class is not defined. Unable to create instance.",
 						customClazz
 					));
 				}
 				
 				if (typeDescription.getFactory().getConstructor() != null)
 				{ // creating instance by constructor
 					customValue = ConstructorUtils.invokeConstructor(
 						customClazz,
 						argumentValues,
 						argumentTypes
 					);
 				}
 				else if (typeDescription.getFactory().getStaticMember() != null)
 				{ // creating an instance by static factory method on the same class
 					String factoryMethodName = typeDescription.getFactory().getStaticMember().getMethod();
 					
 					customValue = _.invokeStaticFactoryMethod(
 						argumentValues,
 						argumentTypes,
 						customClazz,
 						factoryMethodName
 					);
 				}
 				else if (typeDescription.getFactory().getExternal() != null)
 				{ // creating instance of interface
 					TExternalFactory fact = typeDescription.getFactory().getExternal();
 					Bundle factBundle = _.envProbe.findBundle(fact.getBundle().getName() + ":" + fact.getBundle().getVersion());
 					Class<?> factClazz = _.envProbe.findClassInBundle(factBundle, fact.getMethod().getClazz());
 					String factoryMethodName = fact.getMethod().getName();
 					
 					customValue = _.invokeStaticFactoryMethod(
 						argumentValues,
 						argumentTypes,
 						factClazz,
 						factoryMethodName
 					);
 				}
 				else
 				{ // some strange conditions caused that document contains other type of factory type
 				  // for creating instances of given class, document should never be successfully validated
 				  // and therefore the loading should never get here
 					throw new JAXBException(String.format(
 						"Unknow factory type for custom type %s.",
 						customClazz.getCanonicalName()
 					));
 				}
 			}
 			catch (NoSuchBundleException ex)
 			{ // bundle not active
 				System.out.printf(
 					"Bundle % not found or not accessible. Empty data stored.%n",
 					exportingBundleName
 				);
 			}
 			catch (ClassNotFoundException e)
 			{ // bundle does not contain described class
 				System.out.printf(
 					"Class %s or it's factory class not found or not accessible in bundle %s. Empty data storred.%n\tException: %s%n",
 					typeDescription.getCannonicalName(),
 					exportingBundleName,
 					e.getMessage()
 				);
 			}
 			catch (NoSuchMethodException e)
 			{ //  described class does not have compatible method / constructor
 				System.out.printf(
 					"Class %s has no compatible constructor/Method for arguments %s. Empty data storred.%n\tException: %s%n",
 					typeName,
 					_.printTypes(argumentTypes),
 					e.getMessage()
 				);
 			}
 			catch (InstantiationException e)
 			{ // instantiation failed
 				System.out.printf("Unable to create new instance of class %s .%n\tException: %s%n", typeName, e.getMessage());
 			}
 			catch (IllegalArgumentException e)
 			{
 				System.out.printf(
 					"Invocation of factory method for class %s with arguments %s (%s) has failled.%n\tException: %s%n",
 					customClazz.getCanonicalName(),
 					_.printTypes(argumentTypes),
 					_.printValues(argumentValues),
 					e.getMessage()
 				);
 				
 				Integer[] i = new Integer[]{0, 1, 2};
 				System.out.println(_.printValues(new Object[]{i}));
 			}
 			catch (Throwable e)
 			{
 				System.out.printf("Unable to create new instance of class %s .%n\tException: %s%n", typeName, e.getMessage());
 				e.printStackTrace();
 			}
 			finally
 			{ // always store the value, because the scenario will try to load that value
 			  // and we would have to check it's existence
 				_.customTypeValues.put(valueId, customValue);
 				_.customTypeClasses.put(valueId, customClazz);
 			}
 		}
 	}
 
 	private Object invokeStaticFactoryMethod(
 		Object[] argumentValues,
 		Class<?>[] argumentTypes,
 		Class<?> factClazz,
 		String factoryMethodName)
 	throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
 	{
 		Object customValue;
 		Method factoryMethod = MethodUtils.getMatchingAccessibleMethod(
 			factClazz,
 			factoryMethodName,
 			argumentTypes
 		);
 		if (factoryMethod == null)
 		{ // custom type class does not have any method with described name
 			throw new NoSuchMethodException(String.format(
 				"Factory method %s not found in class %s.%n",
 				factoryMethodName,
 				factClazz
 			));
 		}
 		if (!Modifier.isStatic(factoryMethod.getModifiers()))
 		{ // class has method with describe name, but it is not static
 			throw new NoSuchMethodException(String.format(
 				"Factory method %s found in class %s is not static.%n",
 				factoryMethodName,
 				factClazz
 			));
 		}
 		customValue = factoryMethod.invoke(null, argumentValues);
 		return customValue;
 	}
 
 	private void checkTypeNameEquality(String typeName, TImportedType typeDescription)
 	throws JAXBException
 	{
 		if (typeDescription == null)
 		{ // referencing key references non-existent type import
 		  // document should never be successfully validated and therefore the loading should never get here
 			throw new JAXBException(
 				"Type attribute of VALUE element has to reference existing " +
 			    "TYPE element. Unable to continue with current scenario."
 			);
 		}
 		if (!typeName.equals(typeDescription.getCannonicalName()))
 		{ // some strange conditions caused that referencing key differs from referenced key,
 		  // document should never be successfully validated and therefore the loading should never get here
 			throw new JAXBException(
 				"Type attribute of VALUE element has to be equal to cannonical-name " +
 			    "of TYPE element. Unable to continue with current scenario."
 			);
 		}
 	}
 
 	/**
 	 * Implodes array of Class<?> using it's getName() method.
 	 * @param types
 	 * @return list of comma separated class names 
 	 */
 	private String printTypes(Class<?>[] types)
 	{
 		StringBuilder sb = new StringBuilder();
 		for (Class<?> type : types)
 		{
 			sb.append(type.getCanonicalName());
 			sb.append("; ");
 		}
 		return sb.length() == 0 ? void.class.getName() : sb.toString();
 	}
 
 	private String printValues(Object[] values)
 	{
 		StringBuilder sb = new StringBuilder();
 		
 		for (Object o : values)
 		{
 			if (o == null)
 			{
 				sb.append("null");
 			}
 			else if (o.getClass().isArray())
 			{
 				sb.append(Arrays.deepToString((Object[]) o) + "( array of: " + o.getClass().getComponentType().getCanonicalName() + ")");
 			}
 			else
 			{
 				sb.append(o.toString());
 			}
 			sb.append("; ");
 		}
 		
 		return sb.toString();
 	}
 
 	@Override
 	public void setBundleContext(BundleContext context)
 	{
 		_.context = context;
 	}
 
 }
