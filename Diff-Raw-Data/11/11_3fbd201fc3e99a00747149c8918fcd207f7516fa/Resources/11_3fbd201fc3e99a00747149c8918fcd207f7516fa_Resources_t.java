 package sk.stuba.fiit.perconik.core;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import sk.stuba.fiit.perconik.core.services.Services;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceManager;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceNamesSupplier;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceProvider;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceService;
 import sk.stuba.fiit.perconik.utilities.MoreThrowables;
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.SetMultimap;
 
 /**
  * Static accessor methods pertaining to the resources core. 
  * 
  * <p>This class provides access to the underlying functionality
  * of the currently active {@code ResourceService}.
  * 
  * @see Resource
  * @see Listeners
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public final class Resources
 {
 	// TODO add javadocs
 	
 	private Resources()
 	{
 		throw new AssertionError();
 	}
 
 	static final ResourceService service()
 	{
 		return Services.getResourceService();
 	}
 	
 	static final ResourceProvider provider()
 	{
 		return service().getResourceProvider();
 	}
 	
 	static final ResourceManager manager()
 	{
 		return service().getResourceManager();
 	}
 	
 	public static final Resource<?> forName(final String name)
 	{
 		return provider().forName(name);
 	}
 	
 	public static final <L extends Listener> Set<Resource<L>> forType(final Class<L> type)
 	{
 		return provider().forType(type);
 	}
 	
 	public static final <L extends Listener> void register(final Class<L> type, final Resource<? super L> resource)
 	{
 		manager().register(type, resource);
 	}
 
 	public static final <L extends Listener> void registerAll(final Class<L> type, Iterable<Resource<? super L>> resources)
 	{
 		List<Exception> failures = Lists.newLinkedList();
 		
 		ResourceManager manager = manager();
 		
 		for (Resource<? super L> resource: resources)
 		{
 			try
 			{
 				manager.register(type, resource);
 			}
 			catch (Exception failure)
 			{
 				failures.add(failure);
 			}
 		}
 		
 		if (!failures.isEmpty())
 		{
 			throw MoreThrowables.initializeSuppressor(new ResourceRegistrationException(), failures);
 		}
 	}
 
 	public static final void registerAll(final ResourceNamesSupplier supplier)
 	{
 		List<Exception> failures = Lists.newLinkedList();
 		
 		for (Entry<Class<? extends Listener>, Collection<String>> entry: supplier.get().asMap().entrySet())
 		{
 			registerAllByNames(entry.getKey(), entry.getValue(), failures);
 		}
 		
 		if (!failures.isEmpty())
 		{
 			throw MoreThrowables.initializeSuppressor(new ResourceRegistrationException(), failures);
 		}
 	}
 	
 	private static final <L extends Listener> void registerAllByNames(final Class<L> type, final Iterable<String> names, final List<Exception> failures)
 	{
 		ResourceProvider provider = provider();
 		ResourceManager  manager  = manager();
 		
 		BiMap<String, Resource<L>> resources = toResourceNameMap(provider.forType(type));
 		
 		for (String name: names)
 		{
 			Resource<L> resource = resources.get(name);
 			
 			if (resource == null)
 			{
				throw new ResourceNotFoundException("Resource provider does not know resource by name " + name + " for type " + type.getName());
 			}
 			
 			try
 			{
 				manager.register(type, resource);
 			}
 			catch (Exception failure)
 			{
 				failures.add(failure);
 			}
 		}
 	}
 	
 	public static final <L extends Listener> void unregister(final Class<L> type, final Resource<? super L> resource)
 	{
 		manager().unregister(type, resource);
 	}
 	
 	public static final <L extends Listener> void unregisterAll()
 	{
 		unregisterAll(Listener.class);
 	}
 	
 	public static final <L extends Listener> void unregisterAll(final Class<L> type)
 	{
 		manager().unregisterAll(type);
 	}
 
 	public static final <L extends Listener> Set<Resource<? extends L>> assignable(final Class<L> type)
 	{
 		return manager().assignables(type);
 	}
 
 	public static final <L extends Listener> Set<Resource<? super L>> registrable(final Class<L> type)
 	{
 		return manager().registrables(type);
 	}
 
 	public static final SetMultimap<Class<? extends Listener>, Resource<?>> registrations()
 	{
 		return manager().registrations();
 	}
 
 	public static final boolean isRegistered(final Class<? extends Listener> type, final Resource<?> resource)
 	{
 		return manager().registered(type, resource);
 	}
 	
 	public static final <L extends Listener> BiMap<String, Resource<L>> toResourceNameMap(final Set<Resource<L>> resources)
 	{
 		BiMap<String, Resource<L>> map = HashBiMap.create(resources.size());
 		
 		for (Resource<L> resource: resources)
 		{
 			map.put(resource.getName(), resource);
 		}
 		
 		return map;
 	}
 }
