 package sk.stuba.fiit.perconik.core.services.listeners;
 
 import java.util.Collection;
 import java.util.List;
import java.util.Set;
 import sk.stuba.fiit.perconik.core.Listener;
 import sk.stuba.fiit.perconik.core.ListenerUnregistrationException;
 import sk.stuba.fiit.perconik.core.Resource;
 import sk.stuba.fiit.perconik.core.services.Services;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceManager;
 import sk.stuba.fiit.perconik.utilities.MoreThrowables;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
 
 final class StandardListenerManager extends AbstractListenerManager
 {
 	StandardListenerManager()
 	{
 	}
 
 	@Override
 	protected final ResourceManager manager()
 	{
 		return Services.getResourceService().getResourceManager();
 	}
 	
 	public final <L extends Listener> void unregisterAll(final Class<L> type)
 	{
 		List<Exception> failures = Lists.newLinkedList();
 		
		Set<L> processed = Sets.newIdentityHashSet();
		
 		for (Resource<? extends L> resource: this.manager().assignables(type))
 		{
 			for (L listener: resource.registered(type))
 			{
 				try
 				{
					if (processed.add(listener))
					{
						this.unregister(listener);
					}
					else
					{
						Resource.class.cast(resource).unregister(listener);
					}
 				}
 				catch (Exception failure)
 				{
 					failures.add(failure);
 				}
 			}
 		}
 		
 		if (!failures.isEmpty())
 		{
 			throw MoreThrowables.initializeSuppressor(new ListenerUnregistrationException(), failures);
 		}
 	}
 	
 	public final SetMultimap<Resource<?>, Listener> registrations()
 	{
 		SetMultimap<Resource<?>, Listener> registrations = HashMultimap.create();
 		
 		for (Resource<?> resource: this.manager().assignables(Listener.class))
 		{
 			registrations.putAll(resource, resource.registered(Listener.class));
 		}
 		
 		return registrations;
 	}
 	
 	public final <L extends Listener> Collection<L> registered(final Class<L> type)
 	{
 		List<L> listeners = Lists.newArrayList();
 		
 		for (Resource<? extends L> resource: this.manager().assignables(type))
 		{
 			listeners.addAll(resource.registered(type));
 		}
 		
 		return listeners;
 	}
 	
 	public final boolean registered(final Listener listener)
 	{
 		return this.registrations().containsValue(listener);
 	}
 }
