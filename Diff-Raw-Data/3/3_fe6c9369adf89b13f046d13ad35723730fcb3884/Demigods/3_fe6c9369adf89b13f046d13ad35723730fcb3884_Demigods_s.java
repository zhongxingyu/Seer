 package com.censoredsoftware.demigods.engine;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.censoredsoftware.core.bukkit.ListedConversation;
 import com.censoredsoftware.core.module.Configs;
 import com.censoredsoftware.core.module.Messages;
 import com.censoredsoftware.demigods.DemigodsPlugin;
 import com.censoredsoftware.demigods.engine.command.DevelopmentCommands;
 import com.censoredsoftware.demigods.engine.command.GeneralCommands;
 import com.censoredsoftware.demigods.engine.command.MainCommand;
 import com.censoredsoftware.demigods.engine.conversation.Required;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.data.ThreadManager;
 import com.censoredsoftware.demigods.engine.element.Ability;
 import com.censoredsoftware.demigods.engine.element.Deity;
 import com.censoredsoftware.demigods.engine.element.Structure.MassiveStructurePart;
 import com.censoredsoftware.demigods.engine.element.Structure.Structure;
 import com.censoredsoftware.demigods.engine.element.Task;
 import com.censoredsoftware.demigods.engine.exception.DemigodsStartupException;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.language.TranslationManager;
 import com.censoredsoftware.demigods.engine.listener.*;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.util.Structures;
 import com.google.common.collect.Sets;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class Demigods
 {
 	// Public Static Access
 	public static DemigodsPlugin plugin;
 	public static ConversationFactory conversation;
 	public static Messages message;
 	public static Configs config;
 
 	// Public Dependency Plugins
 	public static WorldGuardPlugin worldguard;
 
 	// The Game Data
 	protected static Map<String, Deity> deities;
 	protected static Set<Structure> structures;
 	protected static Set<Task.List> quests;
 	protected static Set<ListedConversation> conversasions;
 
 	// Disabled Worlds
 	protected static Set<String> disabledWorlds;
 	protected static Set<String> commands;
 
 	// The engine Default Text
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
 
 	public Demigods(DemigodsPlugin instance, final ListedDeity[] deities, final ListedTaskSet[] taskSets, final ListedStructure[] structures, final ListedConversation.ConversationData[] conversations, final Listener EpisodeListener) throws DemigodsStartupException
 	{
 		// Allow static access.
 		plugin = instance;
 		conversation = new ConversationFactory(instance);
 
 		// Setup utilities.
 		config = new Configs(instance, true);
 		message = new Messages(instance);
 
 		if(!loadWorlds(instance))
 		{
 			message.severe("Demigods was unable to load any worlds.");
			message.severe("At least 1 world must be enabled.");
			message.severe("Configure at least 1 world for Demigods.");
 			instance.getServer().getPluginManager().disablePlugin(instance);
 			throw new DemigodsStartupException();
 		}
 
 		Demigods.deities = new HashMap<String, Deity>()
 		{
 			{
 				for(ListedDeity deity : deities)
 					put(deity.getDeity().getInfo().getName().toLowerCase(), deity.getDeity());
 			}
 		};
 		Demigods.quests = new HashSet<Task.List>()
 		{
 			{
 				for(ListedTaskSet taskSet : taskSets)
 					add(taskSet.getTaskSet());
 			}
 		};
 		Demigods.structures = new HashSet<Structure>()
 		{
 			{
 				for(ListedStructure structure : structures)
 					add(structure.getStructure());
 			}
 		};
 		Demigods.conversasions = new HashSet<ListedConversation>()
 		{
 			{
 				for(Required conversation : Required.values())
 					add(conversation.getConversation());
 				if(conversations != null) for(ListedConversation.ConversationData conversation : conversations)
 					add(conversation.getConversation());
 			}
 		};
 
 		Demigods.text = getTranslation();
 
 		// Initialize data
 		new DataManager();
 		if(!DataManager.isConnected())
 		{
 			message.severe("Demigods was unable to connect to a Redis server.");
 			message.severe("A Redis server is required for Demigods to run.");
 			message.severe("Please install and configure a Redis server. (" + ChatColor.UNDERLINE + "http://redis.io" + ChatColor.RESET + ")");
 			instance.getServer().getPluginManager().disablePlugin(instance);
 			throw new DemigodsStartupException();
 		}
 
 		// Update usable characters
 		DCharacter.Util.updateUsableCharacters();
 
 		// Initialize metrics
 		try
 		{
 			// (new Metrics(instance)).start();
 		}
 		catch(Exception ignored)
 		{}
 
 		// Finish loading the plugin based on the game data
 		loadDepends(instance);
 		loadListeners(instance);
 		loadCommands(instance);
 
 		// Start game threads
 		ThreadManager.startThreads(instance);
 
 		// Finally, regenerate structures
 		Structures.regenerateStructures();
 
 		if(isRunningSpigot()) message.info(("Spigot found, will use extra API features."));
 	}
 
 	/**
 	 * Get the translation involved.
 	 * 
 	 * @return The translation.
 	 */
 	public Translation getTranslation()
 	{
 		// Default to EnglishCharNames
 		return new TranslationManager.English();
 	}
 
 	public static boolean loadWorlds(DemigodsPlugin instance)
 	{
 		disabledWorlds = Sets.newHashSet();
 		for(String world : config.getSettingArrayListString("restrictions.disabled_worlds"))
 		{
 			if(instance.getServer().getWorld(world) != null) disabledWorlds.add(world);
 		}
 		if(instance.getServer().getWorlds().size() == disabledWorlds.size()) return false;
 		return true;
 	}
 
 	protected static void loadListeners(DemigodsPlugin instance)
 	{
 		PluginManager register = instance.getServer().getPluginManager();
 
 		// engine
 		register.registerEvents(new BattleListener(), instance);
 		register.registerEvents(new CommandListener(), instance);
 		register.registerEvents(new EntityListener(), instance);
 		register.registerEvents(new FlagListener(), instance);
 		register.registerEvents(new GriefListener(), instance);
 		register.registerEvents(new InventoryListener(), instance);
 		register.registerEvents(new PlayerListener(), instance);
 		register.registerEvents(new TributeListener(), instance);
 
 		// disabled worlds
 		if(!disabledWorlds.isEmpty()) register.registerEvents(new DisabledWorldListener(), instance);
 
 		// Deities
 		for(Deity deity : getLoadedDeities().values())
 		{
 			if(deity.getAbilities() == null) continue;
 			for(Ability ability : deity.getAbilities())
 			{
 				if(ability.getListener() != null) register.registerEvents(ability.getListener(), instance);
 			}
 		}
 
 		// Tasks
 		for(Task.List quest : getLoadedQuests())
 		{
 			if(quest.getTasks() == null) continue;
 			for(Task task : quest)
 			{
 				if(task.getListener() != null) register.registerEvents(task.getListener(), instance);
 			}
 		}
 
 		// Structures
 		for(Structure structure : getLoadedStructures())
 		{
 			if(structure instanceof MassiveStructurePart || structure.getUniqueListener() == null) continue;
 			register.registerEvents(structure.getUniqueListener(), instance);
 		}
 
 		// Conversations
 		for(ListedConversation conversation : getLoadedConversations())
 		{
 			if(conversation.getUniqueListener() == null) continue;
 			register.registerEvents(conversation.getUniqueListener(), instance);
 		}
 	}
 
 	protected static void loadCommands(DemigodsPlugin instance)
 	{
 		commands = Sets.newHashSet();
 		MainCommand main = new MainCommand();
 		GeneralCommands general = new GeneralCommands();
 		DevelopmentCommands development = new DevelopmentCommands();
 		main.register(instance, false);
 		general.register(instance, false);
 		development.register(instance, true);
 		commands.addAll(main.getCommands());
 		commands.addAll(general.getCommands());
 		commands.addAll(development.getCommands());
 		commands.add("dg");
 		commands.add("demigod");
 	}
 
 	protected static void loadDepends(DemigodsPlugin instance)
 	{
 		// WorldGuard
 		Plugin depend = instance.getServer().getPluginManager().getPlugin("WorldGuard");
 		if(depend instanceof WorldGuardPlugin) worldguard = (WorldGuardPlugin) depend;
 	}
 
 	public static Map<String, Deity> getLoadedDeities()
 	{
 		return Demigods.deities;
 	}
 
 	public static Set<Task.List> getLoadedQuests()
 	{
 		return Demigods.quests;
 	}
 
 	public static Set<Structure> getLoadedStructures()
 	{
 		return Demigods.structures;
 	}
 
 	public static Set<ListedConversation> getLoadedConversations()
 	{
 		return Demigods.conversasions;
 	}
 
 	public static boolean isRunningSpigot()
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
 
 	public static boolean isDisabledWorld(Location location)
 	{
 		return disabledWorlds.contains(location.getWorld().getName());
 	}
 
 	public static boolean isDisabledWorld(World world)
 	{
 		return disabledWorlds.contains(world.getName());
 	}
 
 	public static boolean isDemigodsCommand(String command)
 	{
 		return commands.contains(command);
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 }
