 package no.runsafe.framework.event;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.event.listener.EventRouterFactory;
 import no.runsafe.framework.event.listener.Factories;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.timer.IScheduler;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.picocontainer.Startable;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class EventEngine implements Startable
 {
 	static
 	{
 		factories = new HashMap<Class<? extends IRunsafeEvent>, EventRouterFactory>();
 	}
 
	public EventEngine(IOutput output, IScheduler scheduler, PluginManager manager, RunsafePlugin plugin)
	{
		this(output, scheduler, null, manager, plugin);
	}

 	public EventEngine(
		IOutput output, IScheduler scheduler, IRunsafeEvent[] events, PluginManager manager, RunsafePlugin plugin)
 	{
 		eventSubscribers = events;
 		this.scheduler = scheduler;
 		this.output = output;
 		this.pluginManager = manager;
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void start()
 	{
		if (eventSubscribers != null)
 		{
			Factories.Register();
			for (Listener listener : getListeners())
			{
				pluginManager.registerEvents(listener, plugin);
				output.finer("Registered event listener %s", listener.getClass().getName());
			}
 		}
 //		List<IConfigurationChanged> configListeners = getComponents(IConfigurationChanged.class);
 //		if (configListeners != null && configListeners.size() > 0)
 //			getComponent(IConfiguration.class).setListeners(configListeners);
 
 //		if (eventListeners != null)
 //			output.outputDebugToConsole(String.format("Registered %d event listeners", eventListeners.size()), Level.FINE);
 
 	}
 
 	@Override
 	public void stop()
 	{
 	}
 
 	public static void Register(EventRouterFactory factory)
 	{
 		if (!factories.containsKey(factory.getInterface()))
 			factories.put(factory.getInterface(), factory);
 	}
 
 	private List<Listener> getListeners()
 	{
 		ArrayList<Listener> listeners = new ArrayList<Listener>();
 		for (IRunsafeEvent sub : eventSubscribers)
 			listeners.addAll(getRouters(sub));
 		return listeners;
 	}
 
 	private List<Listener> getRouters(IRunsafeEvent subscriber)
 	{
 		ArrayList<Listener> routers = new ArrayList<Listener>();
 		for (Class<? extends IRunsafeEvent> type : factories.keySet())
 			if (type.isAssignableFrom(subscriber.getClass()))
 				routers.add(factories.get(type).getListener(output, scheduler, subscriber));
 		return routers;
 	}
 
 	private static final Map<Class<? extends IRunsafeEvent>, EventRouterFactory> factories;
	private final IRunsafeEvent[] eventSubscribers;
 	private final IScheduler scheduler;
 	private final IOutput output;
 	private final PluginManager pluginManager;
 	private final RunsafePlugin plugin;
 }
