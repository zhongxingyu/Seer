 package cz.zcu.kiv.bp.unimocker;
 
 //import ifcs.IPrinter;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.lang.reflect.MethodUtils;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.springframework.osgi.context.BundleContextAware;
 import org.xml.sax.SAXException;
 
 import cz.zcu.kiv.bp.unimocker.bindings.IScenario;
 import cz.zcu.kiv.bp.unimocker.bindings.Scenario;
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
      * ServiceRegistrations for mocks registered as OSGi services.
      */
     private List<ServiceRegistration<?>> serviceRegistrations = new LinkedList<ServiceRegistration<?>>();
 	
     /**
      * Tries to found bundle described by string in format symbolic.name:Major.Minor.Micro.
      * Stops with the first bundle that has matching description! 
      * @param description symbolic.name:Major.Minor.Micro fromated bundle description
      * @return found Bundle instance or null when not found
      */
     private Bundle findBundle(String description)
 	{
 		Bundle ret = null;
 		
 		for (Bundle bundle : _.context.getBundles())
 		{
             String key = String.format(
             	"%s:%s.%s.%s",
             	bundle.getSymbolicName(),
             	bundle.getVersion().getMajor(),
             	bundle.getVersion().getMinor(),
             	bundle.getVersion().getMicro()
             );
             if (description.equalsIgnoreCase(key))
             {
             	ret = bundle;
             	break;
             }
 		}
 		
 		return ret;
 	}
 
     /**
      * Tries to load given described from the given bundle.
      * @param bundle which should be probed
      * @param classesToFind array of class names to find
      * @return array of loaded classes
      * @throws ClassNotFoundException when bundles classloader fails to load class
      */
 	private Class<?>[] findClassesInBundle(Bundle bundle, String[] classesToFind) throws ClassNotFoundException
 	{
 		List<Class<?>> ret = new ArrayList<>(classesToFind.length);
 
 		for (String className : classesToFind)
 		{
 			ret.add(bundle.loadClass(className));
 		}
 		
 		return ret.toArray(new Class<?>[0]);
 	}
 
 	/**
      * Tries to load given described from the given bundle.
      * @param mockedBundle which should be probed
      * @param classesToFind array of class names to find
      * @return array of loaded classes or null if loading was not successful
      */
 	private Class<?>[] findClassesToMock(Bundle mockedBundle, String[] classesToFind)
 	{
 		Class<?>[] ret = null;
 		try
 		{
 			ret = _.findClassesInBundle(mockedBundle, classesToFind);
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
 		boolean ignoreUndefMethods,
 		boolean ignoreUndefPossibs)
 	{
 		// create mockup object
 		Object mockup = Proxy.newProxyInstance(
 			clazz.getClassLoader(),
 			new Class<?> [] { clazz },
 			new UniHandler(clazz, returns, ignoreUndefMethods, ignoreUndefPossibs)
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
 				Class<?>[] parameterTypes = inv.getArguments().getTypes();
 				// Tries to match method w/o wrapper classes for primitive types.
 				Method foundMethod = MethodUtils.getMatchingAccessibleMethod(
 					clazz,
 					methodToFind.getName(),
 					parameterTypes
 				);
 				if (foundMethod == null)
 				{ // class does not provide required method
 					throw new NoSuchMethodException(
 						String.format(
 							"Class %s does not provide any method %s with arguments %s",
 							clazz.getCanonicalName(),
 							methodToFind.getName(),
 							Arrays.deepToString(parameterTypes)
 						)
 					);
 				}
 				System.out.printf(
 					"class: %s method: %s/wanted method: %s (%s)%n",
 					clazz,
 					foundMethod,
 					methodToFind.getName(),
 					Arrays.deepToString(parameterTypes)
 				);
 				// If this method has not been yet described, create new collection.
 				if (!returns.containsKey(foundMethod))
 				{ // new method
 					returns.put(foundMethod, new HashMap<Object[], Object>());
 				}
 				// Contains return values for current method and all possible invocation arguments. 
 				Map<Object[], Object> methodsInvocationPossibilities = returns.get(foundMethod);
 				methodsInvocationPossibilities.put(
 					inv.getArguments().toArray(),
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
 
 		for (Entry<String, HashMap<String, TSimulatedService>> bundle : scenario.entrySet())
 		{
 			Bundle mockedBundle = _.findBundle(bundle.getKey());
 			if (mockedBundle == null)
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
 					TSimulatedService simulation = bundle.getValue().get(clazz.getName());
 					Map<Method, Map<Object[], Object>> returns = _.buildInvocationPossibilitiesForClass(clazz, simulation);
 					
 					TSimulatedService descr = bundle.getValue().get(clazz.getCanonicalName());
 	
 					Object srv = _.createMockup(
 						clazz,
 						returns,
 						descr.isIgnoreUndefinedMethods(),
 						descr.isIgnoreUndefinedPossibilities()
 					);
 					
 					// building mockup
 					_.createService(clazz, srv);
 				}
 				catch (NoSuchMethodException ex)
 				{ // described method not been found in current class
 					System.out.printf("%s -> skipping%n", ex.getMessage());
 				}
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
 		_.scenarioProject = new Scenario();
 		_.scenarioProject.loadFile(fileName);
 	}
 
 	@Override
 	public void setBundleContext(BundleContext context)
 	{
 		_.context = context;
 	}
 
 }
