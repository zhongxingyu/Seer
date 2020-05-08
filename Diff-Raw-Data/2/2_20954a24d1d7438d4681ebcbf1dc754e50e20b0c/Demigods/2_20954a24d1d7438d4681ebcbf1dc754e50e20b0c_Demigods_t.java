 package com.censoredsoftware.Demigods.Engine;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.plugin.Plugin;
 
 import com.censoredsoftware.Demigods.DemigodsPlugin;
 import com.censoredsoftware.Demigods.Engine.Command.DevelopmentCommands;
 import com.censoredsoftware.Demigods.Engine.Command.GeneralCommands;
 import com.censoredsoftware.Demigods.Engine.Command.MainCommand;
 import com.censoredsoftware.Demigods.Engine.Conversation.DConversation;
 import com.censoredsoftware.Demigods.Engine.Exception.DemigodsStartupException;
 import com.censoredsoftware.Demigods.Engine.Language.Translation;
 import com.censoredsoftware.Demigods.Engine.Listener.*;
 import com.censoredsoftware.Demigods.Engine.Module.ConfigModule;
 import com.censoredsoftware.Demigods.Engine.Module.MessageModule;
 import com.censoredsoftware.Demigods.Engine.Object.*;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.SchedulerUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class Demigods
 {
 	// Public Static Access
 	public static DemigodsPlugin plugin;
 	public static ConversationFactory conversation;
 
 	// Public Modules
 	public static ConfigModule config;
 	public static MessageModule message;
 
 	// Public Dependency Plugins
 	public static WorldGuardPlugin worldguard;
 
 	// The Game Data
 	protected static Deque<Deity> deities;
 	protected static Deque<Task.List> quests;
 	protected static Deque<Structure> structures;
 	protected static Deque<DConversation> conversasions;
 
 	// The Engine Default Text
 	public static Translation text;
 
 	public interface ListedDeity
 	{
 		public Deity getDeity();
 	}
 
 	public interface ListedTaskSet
 	{
 		public Task.List getTaskSet();
 	}
 
 	public interface ListedStructure
 	{
 		public Structure getStructure();
 	}
 
 	public interface ListedConversation
 	{
 		public DConversation getConversation();
 	}
 
 	public Demigods(DemigodsPlugin instance, final ListedDeity[] deities, final ListedTaskSet[] taskSets, final ListedStructure[] structures, final ListedConversation[] conversations) throws DemigodsStartupException
 	{
 		// Allow static access.
 		plugin = instance;
 		conversation = new ConversationFactory(instance);
 
 		// Setup public modules.
 		config = new ConfigModule(instance, true);
 		message = new MessageModule(instance, config.getSettingBoolean("misc.tag_messages"));
 
 		// Define the game data.
 		Demigods.deities = new ArrayDeque<Deity>()
 		{
 			{
 				for(ListedDeity deity : deities)
 					add(deity.getDeity());
 			}
 		};
 		Demigods.quests = new ArrayDeque<Task.List>()
 		{
 			{
 				for(ListedTaskSet taskSet : taskSets)
 					add(taskSet.getTaskSet());
 			}
 		};
 		Demigods.structures = new ArrayDeque<Structure>()
 		{
 			{
 				for(ListedStructure structure : structures)
 					add(structure.getStructure());
 			}
 		};
 		Demigods.conversasions = new ArrayDeque<DConversation>()
 		{
 			{
 				for(DConversation.Required conversation : DConversation.Required.values())
 					add(conversation.getConversation());
 				if(conversations != null) for(ListedConversation conversation : conversations)
 					add(conversation.getConversation());
 			}
 		};
 
 		Demigods.text = getTranslation();
 
 		// Initialize soft data.
 		new DataUtility();
 		if(!DataUtility.isConnected())
 		{
 			message.severe("Demigods was unable to connect to a Redis server.");
 			message.severe("A Redis server is required for Demigods to run.");
 			message.severe("Please install and configure a Redis server. (" + ChatColor.UNDERLINE + "http://redis.io" + ChatColor.RESET + ")");
 			instance.getServer().getPluginManager().disablePlugin(instance);
 			throw new DemigodsStartupException();
 		}
 
 		// Initialize metrics.
 		try
 		{
 			// (new Metrics(instance)).start();
 		}
 		catch(Exception ignored)
 		{}
 
 		// Finish loading the plugin based on the game data.
 		loadDepends(instance);
 		loadListeners(instance);
 		loadCommands();
 
 		// Finally, regenerate structures
 		Structure.Util.regenerateStructures();
 
 		// Start game threads.
 		SchedulerUtility.startThreads(instance);
 
 		if(runningSpigot()) message.info(("Spigot found, will use extra API features."));
 	}
 
 	/**
 	 * Get the translation involved.
 	 * 
 	 * @return The translation.
 	 */
 	public Translation getTranslation()
 	{
 		// Default to EnglishCharNames
 		return new TextUtility.English();
 	}
 
 	protected static void loadListeners(DemigodsPlugin instance)
 	{
 		// Engine
 		instance.getServer().getPluginManager().registerEvents(new BattleListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new CommandListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new EntityListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new FlagListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new GriefListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new InventoryListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new PlayerListener(), instance);
 		instance.getServer().getPluginManager().registerEvents(new TributeListener(), instance);
 
 		// Deities
 		for(Deity deity : getLoadedDeities())
 		{
 			if(deity.getAbilities() == null) continue;
 			for(Ability ability : deity.getAbilities())
 			{
 				if(ability.getListener() != null) instance.getServer().getPluginManager().registerEvents(ability.getListener(), instance);
 			}
 		}
 
 		// Tasks
 		for(Task.List quest : getLoadedQuests())
 		{
 			if(quest.getTasks() == null) continue;
 			for(Task task : quest)
 			{
 				if(task.getListener() != null) instance.getServer().getPluginManager().registerEvents(task.getListener(), instance);
 			}
 		}
 
 		// Structures
 		for(Structure structure : getLoadedStructures())
 		{
 			if(structure.getUniqueListener() == null) continue;
 			instance.getServer().getPluginManager().registerEvents(structure.getUniqueListener(), instance);
 		}
 
 		// Conversations
 		for(DConversation conversation : getLoadedConversations())
 		{
 			if(conversation.getUniqueListener() == null) continue;
 			instance.getServer().getPluginManager().registerEvents(conversation.getUniqueListener(), instance);
 		}
 
 	}
 
 	protected static void loadCommands()
 	{
 		DCommand.Util.registerCommand(new MainCommand());
 		DCommand.Util.registerCommand(new GeneralCommands());
 		DCommand.Util.registerCommand(new DevelopmentCommands());
 	}
 
 	protected static void loadDepends(DemigodsPlugin instance)
 	{
 		// WorldGuard
 		Plugin depend = instance.getServer().getPluginManager().getPlugin("WorldGuard");
 		if(depend instanceof WorldGuardPlugin) worldguard = (WorldGuardPlugin) depend;
 	}
 
 	public static Deque<Deity> getLoadedDeities()
 	{
 		return Demigods.deities;
 	}
 
 	public static Deque<Task.List> getLoadedQuests()
 	{
 		return Demigods.quests;
 	}
 
 	public static Deque<Structure> getLoadedStructures()
 	{
 		return Demigods.structures;
 	}
 
 	public static Deque<DConversation> getLoadedConversations()
 	{
 		return Demigods.conversasions;
 	}
 
 	public static boolean runningSpigot()
 	{
 		try
 		{
 			Bukkit.getServer().getWorlds().get(0).spigot();
 			return true;
 		}
		catch(Throwable ignored)
 		{}
 		return false;
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 }
