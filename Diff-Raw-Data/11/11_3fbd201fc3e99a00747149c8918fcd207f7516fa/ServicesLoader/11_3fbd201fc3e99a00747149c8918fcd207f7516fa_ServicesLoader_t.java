 package sk.stuba.fiit.perconik.core.plugin;
 
 import static sk.stuba.fiit.perconik.core.utilities.LogHelper.log;
 import java.util.List;
 import sk.stuba.fiit.perconik.core.ListenerRegistrationException;
 import sk.stuba.fiit.perconik.core.Listeners;
 import sk.stuba.fiit.perconik.core.ResourceRegistrationException;
 import sk.stuba.fiit.perconik.core.Resources;
 import sk.stuba.fiit.perconik.core.services.ServiceSnapshot;
 import sk.stuba.fiit.perconik.core.services.Services;
 import com.google.common.collect.ImmutableList;
 
 final class ServicesLoader
 {
 	private final ResourceExtentionProcessor resources;
 	
 	private final ListenerExtentionProcessor listeners;
 	
 	ServicesLoader()
 	{
 		this.resources = new ResourceExtentionProcessor();
 		this.listeners = new ListenerExtentionProcessor();
 	}
 	
 	final List<ResolvedService<?>> load()
 	{
 		ResolvedResources resource = this.resources.process();
 		ResolvedListeners listener = this.listeners.process();
 		
 		List<ResolvedService<?>> data = ImmutableList.of(resource, listener);
 		
 		Services.setResourceService(resource.service);
 		Services.setListenerService(listener.service);
 		
 		ServiceSnapshot.take().servicesInStartOrder().startSynchronously();
 
 		try
 		{
 			Resources.registerAll(resource.supplier);
 			Listeners.registerAll(listener.supplier);
 		}
 		catch (ResourceRegistrationException failure)
 		{
 			log.error(failure, "Unexpected error during initial registration of resources");
 		}
 		catch (ListenerRegistrationException failure)
 		{
 			log.error(failure, "Unexpected error during initial registration of listeners");
 		}
		catch (Exception failure)
		{
			log.error(failure, "Unexpected error during initial registration of resources and listeners");
		}
 		
 		return data;
 	}
 }
