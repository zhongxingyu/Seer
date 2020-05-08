 package au.com.addstar.pandora.modules;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventException;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredListener;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 import au.com.addstar.pandora.EventHelper;
 import au.com.addstar.pandora.MasterPlugin;
 import au.com.addstar.pandora.Module;
 
 public class EventManipulator implements Module, Listener
 {
 	private Plugin mPlugin;
 	private File mFile;
 	
 	private Pattern mSelectorPattern = Pattern.compile("(\\w+)\\s*\\[\\s*(\\w+|\\*)\\s*,\\s*(\\w+|\\*)\\s*,\\s*(\\w+|\\*)\\s*,\\s*(\\w+|\\*)\\s*\\]");
 	
 	@EventHandler
 	private void onPluginLoad(PluginEnableEvent event)
 	{
 		
 	}
 	
 	@Override
 	public void onEnable()
 	{
 		mFile = new File(mPlugin.getDataFolder(), "EventManipulator.yml");
 		if(!mFile.exists())
 			mPlugin.saveResource("EventManipulator.yml", false);
 		
 		Bukkit.getScheduler().runTask(mPlugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				EventHelper.buildEventMap();
 				
 				try
 				{
 					manipulateEvents();
 				}
 				catch ( Exception e )
 				{
 					mPlugin.getLogger().severe("Failed to load EventManipulator.yml:");
 					e.printStackTrace();
 				}
 			}
 		});
 		
 	}
 	
 	public void manipulateEvents() throws InvalidConfigurationException, IOException
 	{
 		YamlConfiguration config = new YamlConfiguration();
 		config.load(mFile);
 		
 		Multimap<Class<? extends Event>, Manipulator> manipulators = HashMultimap.create(); 
 		
 		// First load all the manipulators
 		for(String selectorString : config.getKeys(false))
 		{
 			if(!config.isConfigurationSection(selectorString))
 			{
 				mPlugin.getLogger().severe("[EventManipulator] Bad selector: " + selectorString);
				mPlugin.getLogger().severe("[EventManipulator] Selector has no manipulator options");
 				return;
 			}
 			
 			Matcher match = mSelectorPattern.matcher(selectorString);
 			
 			if(!match.matches())
 			{
 				mPlugin.getLogger().severe("[EventManipulator] Bad selector: " + selectorString);
				mPlugin.getLogger().severe("[EventManipulator] Error in selector");
 				return;
 			}
 			Selector selector;
 			
 			try
 			{
 				selector = new Selector(match);
 				
 				if(selector.getEventClass() == null)
 				{
 					mPlugin.getLogger().severe("[EventManipulator] Bad selector: " + selectorString);
 					mPlugin.getLogger().severe("[EventManipulator] Unknown event name " + selector.eventName);
 					return;
 				}
 			}
 			catch(IllegalArgumentException e)
 			{
 				mPlugin.getLogger().severe("[EventManipulator] Bad selector: " + selectorString);
 				mPlugin.getLogger().severe("[EventManipulator] " + e.getMessage());
 				return;
 			}
 			
 			try
 			{
 				Manipulator manipulator = new Manipulator(selector, config.getConfigurationSection(selectorString));
 				manipulators.put(selector.getEventClass(), manipulator);
 			}
 			catch(IllegalArgumentException e)
 			{
 				mPlugin.getLogger().severe("[EventManipulator] Bad manipulator for: " + selectorString);
 				mPlugin.getLogger().severe("[EventManipulator] " + e.getMessage());
 				return;
 			}
 		}
 		
 		// Now process them
 		for(Class<? extends Event> eventClass : manipulators.keySet())
 		{
 			HandlerList handlers = EventHelper.getHandlers(eventClass);
 			if(handlers == null)
 				return;
 			
 			Collection<Manipulator> applicableManipulators = manipulators.get(eventClass);
 			
 			ArrayList<RegisteredListener> listeners = new ArrayList<RegisteredListener>();
 			for(RegisteredListener listener : handlers.getRegisteredListeners())
 			{
 				handlers.unregister(listener);
 				listeners.add(listener);
 			}
 			
 			Collections.sort(listeners, new DependencySorter(applicableManipulators));
 			
 			for(RegisteredListener listener : listeners)
 			{
 				for(Manipulator m : applicableManipulators)
 				{
 					if(m.getSelector().matches(listener))
 					{
 						listener = m.wrap(listener);
 						break;
 					}
 				}
 				
 				handlers.register(listener);
 			}
 			
 			mPlugin.getLogger().info("[EventManipulator] Manipulated handlers for " + eventClass.getSimpleName());
		
 			handlers.bake();
 		}
 	}
 	
 	@Override
 	public void onDisable()
 	{
 	}
 
 	@Override
 	public void setPandoraInstance( MasterPlugin plugin )
 	{
 		mPlugin = plugin;
 	}
 
 	public static class Selector
 	{
 		public String pluginName;
 		public String eventName;
 		public String listenerName;
 		public EventPriority priority;
 		public Boolean ignoreCanceled;
 		
 		private Class<? extends Event> mEventClass;
 		
 		public Selector(Matcher match) throws IllegalArgumentException
 		{
 			pluginName = match.group(1);
 			eventName = match.group(2);
 			if(eventName.equals("*"))
 				throw new IllegalArgumentException("Event name cannot be *");
 			
 			listenerName = match.group(3);
 			if(listenerName.equals("*"))
 				listenerName = null;
 			
 			if(!match.group(4).equals("*"))
 			{
 				priority = EventPriority.valueOf(match.group(4).toUpperCase());
				if(priority == null)
					throw new IllegalArgumentException("Invalid event priority " + match.group(4));
 			}
 			
 			if(!match.group(5).equals("*"))
 				ignoreCanceled = Boolean.valueOf(match.group(5));
 		}
 		
 		public Class<? extends Event> getEventClass()
 		{
 			if(mEventClass != null)
 				return mEventClass;
 			
 			try
 			{
 				Class<?> clazz = Class.forName(eventName);
 				if(Event.class.isAssignableFrom(clazz))
 					mEventClass = clazz.asSubclass(Event.class);
 			}
 			catch(ClassNotFoundException e)
 			{
 			}
 			
 			mEventClass = EventHelper.parseEvent(eventName);
 			return mEventClass;
 		}
 
 		public boolean matches(RegisteredListener listener)
 		{
 			if(!listener.getPlugin().getName().equalsIgnoreCase(pluginName))
 				return false;
 			
 			if(listenerName != null)
 			{
 				if(!listener.getListener().getClass().getSimpleName().equalsIgnoreCase(listenerName) && 
 				   !listener.getListener().getClass().getName().equalsIgnoreCase(listenerName))
 				{
 					return false;
 				}
 			}
 			
 			if(priority != null && listener.getPriority() != priority)
 				return false;
 			
 			if(ignoreCanceled != null && listener.isIgnoringCancelled() != ignoreCanceled)
 				return false;
 			
 			return true;
 		}
 	}
 	
 	public static class Manipulator
 	{
 		private List<String> mBefore;
 		private List<String> mAfter;
 		
 		private EventPriority mNewPriority;
 		private Boolean mNewIgnoreCancel;
 		
 		private Selector mSelector;
 		
 		public Manipulator(Selector selector, ConfigurationSection section) throws IllegalArgumentException
 		{
 			mSelector = selector;
 			
 			boolean valid = false;
 			
 			if(section.isString("move"))
 			{
 				valid = true;
 				mBefore = new ArrayList<String>();
 				mAfter = new ArrayList<String>();
 				
 				String[] depends = section.getString("move").trim().split(",");
 				for(String dependString : depends)
 				{
 					String[] depend = dependString.split(":");
 					if(depend.length != 2)
 						throw new IllegalArgumentException("Invalid dependancy: " + dependString);
 					
 					if(depend[0].equalsIgnoreCase("before"))
 						mBefore.add(depend[1]);
 					else if(depend[0].equalsIgnoreCase("after"))
 						mAfter.add(depend[1]);
 					else
 						throw new IllegalArgumentException("Invalid dependancy: " + dependString);
 				}
 			}
 			
 			if(section.isString("priority"))
 			{
 				valid = true;
 				mNewPriority = EventPriority.valueOf(section.getString("priority").trim().toUpperCase());
 				if(mNewPriority == null)
 					throw new IllegalArgumentException("Invalid event priority " + section.getString("priority"));
 			}
 			
 			if(section.isBoolean("ignoresCancel"))
 			{
 				valid = true;
 				mNewIgnoreCancel = section.getBoolean("ignoresCancel");
 			}
 			
 			if(!valid)
 				throw new IllegalArgumentException("At least one of 'move','priority', or 'ignoresCancel' MUST be present.");
 		}
 		
 		public Selector getSelector()
 		{
 			return mSelector;
 		}
 		
 		public int compareTo(Plugin plugin)
 		{
 			String name = plugin.getName();
 			
 			if(mAfter != null)
 			{
 				for(String pluginName : mAfter)
 				{
 					if(pluginName.equals("*") || pluginName.equalsIgnoreCase(name))
 						return 1;
 				}
 			}
 			
 			if(mBefore != null)
 			{
 				for(String pluginName : mBefore)
 				{
 					if(pluginName.equals("*") || pluginName.equalsIgnoreCase(name))
 						return -1;
 				}
 			}
 			
 			return 0;
 		}
 		
 		public RegisteredListener wrap(RegisteredListener listener)
 		{
 			if(mNewIgnoreCancel == null && mNewPriority == null)
 				return listener;
 			
 			EventPriority priority = listener.getPriority();
 			if(mNewPriority != null)
 				priority = mNewPriority;
 			
 			boolean ignoreCancel = listener.isIgnoringCancelled();
 			if(mNewIgnoreCancel != null)
 				ignoreCancel = mNewIgnoreCancel;
 			
 			return new WrapperRegisteredListener(listener, priority, ignoreCancel);
 		}
 	}
 	
 	private static class DependencySorter implements Comparator<RegisteredListener>
 	{
 		private Collection<Manipulator> mManipulators;
 		public DependencySorter(Collection<Manipulator> manipulators)
 		{
 			mManipulators = manipulators;
 		}
 		
 		@Override
 		public int compare( RegisteredListener o1, RegisteredListener o2 )
 		{
 			for(Manipulator m : mManipulators)
 			{
 				if(m.getSelector().matches(o1))
 				{
 					int val = m.compareTo(o2.getPlugin());
 					if(val != 0)
 						return val;
 				}
 			}
 			return 0;
 		}
 	}
 	
 	private static class WrapperRegisteredListener extends RegisteredListener
 	{
 		private RegisteredListener mExisting;
 		
 		public WrapperRegisteredListener(RegisteredListener existing, EventPriority priority, boolean ignoreCancel)
 		{
 			super(existing.getListener(), null, priority, existing.getPlugin(), ignoreCancel);
 			mExisting = existing;
 		}
 		
 		@Override
 		public void callEvent( Event event ) throws EventException
 		{
 			mExisting.callEvent(event);
 		}
 	}
 }
