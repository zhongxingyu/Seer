 package com.censoredsoftware.demigods.engine;
 
 import com.censoredsoftware.censoredlib.helper.CensoredCentralizedClass;
 import com.censoredsoftware.censoredlib.helper.QuitReasonHandler;
 import com.censoredsoftware.censoredlib.helper.WrappedCommand;
 import com.censoredsoftware.censoredlib.helper.WrappedConversation;
 import com.censoredsoftware.demigods.engine.ability.Ability;
 import com.censoredsoftware.demigods.engine.command.DevelopmentCommands;
 import com.censoredsoftware.demigods.engine.command.GeneralCommands;
 import com.censoredsoftware.demigods.engine.command.MainCommand;
 import com.censoredsoftware.demigods.engine.conversation.Prayer;
 import com.censoredsoftware.demigods.engine.data.ThreadManager;
 import com.censoredsoftware.demigods.engine.data.TributeManager;
 import com.censoredsoftware.demigods.engine.deity.Alliance;
 import com.censoredsoftware.demigods.engine.deity.Deity;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.listener.*;
 import com.censoredsoftware.demigods.engine.mythos.Mythos;
 import com.censoredsoftware.demigods.engine.mythos.MythosSet;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.player.Skill;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.util.Abilities;
 import com.censoredsoftware.demigods.engine.util.Configs;
 import com.censoredsoftware.demigods.engine.util.Messages;
 import com.censoredsoftware.demigods.panel.DemigodsWebPanel;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.conversations.Prompt;
 import org.bukkit.event.Listener;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.ServicesManager;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 import org.mcstats.MetricsLite;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class Demigods extends CensoredCentralizedClass
 {
 	// Constants
 	public static String SAVE_PATH;
 	public static Scoreboard BOARD;
 	public static DemigodsWebPanel WEB_PANEL;
 
 	// Public Static Access
 	public static final DemigodsPlugin PLUGIN;
 	public static final ConversationFactory CONVERSATION_FACTORY;
 	public static final Translation LANGUAGE;
 	public static final ScoreboardManager SCOREBOARD_MANAGER;
 
 	// Disabled Stuff
 	public static ImmutableSet<String> DISABLED_WORLDS;
 	public static ImmutableSet<String> COMMANDS;
 
 	// Mythos
 	public static final Mythos MYTHOS;
 
 	// Instance of this class;
 	private static Demigods demigods;
 
 	private Demigods()
 	{}
 
 	// Load what is possible to load right away.
 	static
 	{
 		// Allow static access.
 		PLUGIN = (DemigodsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Demigods");
 
 		// Load the Mythos.
 		ServicesManager servicesManager = PLUGIN.getServer().getServicesManager();
 		Collection<RegisteredServiceProvider<Mythos>> mythosProviders = servicesManager.getRegistrations(Mythos.class);
 		if(Iterables.any(mythosProviders, new Predicate<RegisteredServiceProvider<Mythos>>()
 		{
 			@Override
 			public boolean apply(RegisteredServiceProvider<Mythos> mythosProvider)
 			{
 				return mythosProvider.getProvider().isPrimary();
 			}
 		}))
 		{
 			// Decide the primary mythos
 			Mythos reiningMythos = null;
 			int reiningPriority = 5;
 
 			Set<Mythos> workingSet = Sets.newHashSet();
 			for(RegisteredServiceProvider<Mythos> mythosProvider : mythosProviders)
 			{
 				Mythos mythos = mythosProvider.getProvider();
 				workingSet.add(mythos);
 				Messages.info("The " + mythos.getTitle() + " mythos created by " + mythos.getAuthor() + " has loaded!");
 				if(!mythosProvider.getProvider().isPrimary()) continue;
				if(mythosProvider.getPriority().ordinal() < reiningPriority) // not really sure how Bukkit handles priority, presuming the same way as EventPriority
 				{
 					reiningMythos = mythos;
 					reiningPriority = mythosProvider.getPriority().ordinal();
 				}
 			}
 
 			if(reiningMythos != null)
 			{
 				workingSet.remove(reiningMythos);
 				if(reiningMythos.allowSecondary() || workingSet.isEmpty()) MYTHOS = reiningMythos;
 				else MYTHOS = new MythosSet(reiningMythos, workingSet);
 			}
 			else MYTHOS = null;
 		}
 		else MYTHOS = null;
 
 		// Conversation factory static access.
 		CONVERSATION_FACTORY = new ConversationFactory(PLUGIN);
 
 		// Language data.
 		LANGUAGE = new Translation();
 
 		// Scoreboard manager.
 		SCOREBOARD_MANAGER = Bukkit.getScoreboardManager();
 		BOARD = SCOREBOARD_MANAGER.getNewScoreboard();
 	}
 
 	// Load everything else.
 	protected static boolean load()
 	{
 		// Initialize metrics
 		try
 		{
 			(new MetricsLite(PLUGIN)).start();
 		}
 		catch(Exception ignored)
 		{}
 
 		if(MYTHOS == null)
 		{
 			Messages.severe("Demigods was unable to load a Mythos.");
 			Messages.severe("Please install a Mythos plugin or place");
 			Messages.severe("the default Demigods-Greek.jar into the");
 			Messages.severe("plugins/Demigods/plugins directory.");
 			return false;
 		}
 
 		try
 		{
 			demigods = new Demigods();
 
 			// Start the data
 			SAVE_PATH = PLUGIN.getDataFolder() + "/data/"; // Don't change this.
 
 			if(!PLUGIN.getServer().getOnlineMode())
 			{
 				Messages.severe("Demigods might not work in offline mode.");
 				Messages.severe("We depend on Mojang's servers for ids.");
 				Messages.severe("Any player who joins and is not premium");
 				Messages.severe("may be kicked from the game.");
 			}
 
 			// Check for world load errors
 			if(demigods.loadWorlds() > 0)
 			{
 				Messages.severe("Demigods was unable to confirm any worlds.");
 				Messages.severe("This may be caused by misspelt world names.");
 				Messages.severe("Multi-world plugins can cause this message,");
 				Messages.severe("and in that case this may be a false alarm.");
 			}
 
 			// Load listeners, commands, permissions, and the scoreboard
 			demigods.loadListeners();
 			demigods.loadCommands();
 			demigods.loadPermissions(true);
 			demigods.loadScoreBoard();
 
 			// Update usable characters
 			DCharacter.Util.updateUsableCharacters();
 
 			// Start threads
 			ThreadManager.startThreads();
 
 			// Regenerate structures
 			Structure.Util.regenerateStructures();
 
 			// Initialize tribute tracking
 			TributeManager.initializeTributeTracking();
 
 			if(Util.isRunningSpigot()) Messages.info(("Spigot found, will use extra API features."));
 			else Messages.warning(("Without Spigot, some features may not work."));
 
 			// Handle online characters
 			for(DCharacter character : DCharacter.Util.loadAll())
 				character.getMeta().cleanSkills();
 
 			// Start panel if allowed
 			if(Configs.getSettingBoolean("panel.use") && Bukkit.getPluginManager().getPlugin("CensoredLib-Web") != null)
 			{
 				WEB_PANEL = new DemigodsWebPanel();
 				WEB_PANEL.runServer();
 			}
 
 			return DemigodsPlugin.READY = true;
 		}
 		catch(Exception errored)
 		{
 			errored.printStackTrace();
 		}
 		return false;
 	}
 
 	protected int loadWorlds()
 	{
 		Set<String> disabledWorlds = Sets.newHashSet();
 		int erroredWorlds = 0;
 		for(String world : Configs.getSettingList("restrictions.disabled_worlds"))
 		{
 			disabledWorlds.add(world);
 			erroredWorlds += Bukkit.getServer().getWorld(world) == null ? 1 : 0;
 		}
 		DISABLED_WORLDS = ImmutableSet.copyOf(disabledWorlds);
 		return erroredWorlds;
 	}
 
 	protected void loadListeners()
 	{
 		PluginManager register = Bukkit.getServer().getPluginManager();
 
 		// Base Game
 		if(MYTHOS.useBaseGame()) for(BaseGameListener baseGameListener : BaseGameListener.values())
 			register.registerEvents(baseGameListener.getListener(), PLUGIN);
 
 		// Mythos
 		for(Listener listener : MYTHOS.getListeners())
 			register.registerEvents(listener, PLUGIN);
 
 		// Disabled worlds
 		if(!DISABLED_WORLDS.isEmpty()) register.registerEvents(new ZoneListener(), PLUGIN);
 
 		// Abilities
 		for(Ability ability : Abilities.getLoadedAbilities())
 			if(ability.getListener() != null) register.registerEvents(ability.getListener(), PLUGIN);
 
 		// Structures
 		for(Structure structure : Collections2.filter(MYTHOS.getStructures(), new Predicate<Structure>()
 		{
 			@Override
 			public boolean apply(Structure structure)
 			{
 				return structure.getUniqueListener() != null;
 			}
 		}))
 			if(structure.getUniqueListener() != null) register.registerEvents(structure.getUniqueListener(), PLUGIN);
 
 		// Conversations
 		for(WrappedConversation conversation : Collections2.filter(Collections2.transform(Sets.newHashSet(DemigodsConversation.values()), new Function<DemigodsConversation, WrappedConversation>()
 		{
 			@Override
 			public WrappedConversation apply(DemigodsConversation conversation)
 			{
 				return conversation.getConversation();
 			}
 		}), new Predicate<WrappedConversation>()
 		{
 			@Override
 			public boolean apply(WrappedConversation conversation)
 			{
 				return conversation.getUniqueListener() != null;
 			}
 		}))
 			if(conversation.getUniqueListener() != null) register.registerEvents(conversation.getUniqueListener(), PLUGIN);
 
 		// Special Items
 		// for(DivineItem divineItem : DivineItem.values())
 		// {
 		// if(divineItem.getUniqueListener() != null) register.registerEvents(divineItem.getUniqueListener(), PLUGIN);
 		// if(divineItem.getRecipe() != null) PLUGIN.getServer().addRecipe(divineItem.getRecipe());
 		// }
 
 		// Quit reason.
 		Bukkit.getServer().getLogger().addHandler(new QuitReasonHandler());
 	}
 
 	protected void loadScoreBoard()
 	{
 		// Alliances
 		for(final Alliance alliance : MYTHOS.getAlliances())
 		{
 			// Register the team
 			Team team = BOARD.registerNewTeam(alliance.getName());
 
 			// Define team properties
 			team.setAllowFriendlyFire(Configs.getSettingBoolean("restrictions.friendly_fire"));
 			team.setCanSeeFriendlyInvisibles(true);
 
 			// In-use characters (including offline)
 			for(DCharacter character : DCharacter.Util.getCharactersWithPredicate(new Predicate<DCharacter>()
 			{
 				@Override
 				public boolean apply(DCharacter character)
 				{
 					return character.isActive() && character.getAlliance().equals(alliance);
 				}
 			}))
 				team.addPlayer(character.getOfflinePlayer());
 		}
 
 		// Register the Mortal alliance
 		Team mortals = BOARD.registerNewTeam("Mortal");
 
 		// Define team properties
 		mortals.setAllowFriendlyFire(Configs.getSettingBoolean("restrictions.friendly_fire"));
 		mortals.setCanSeeFriendlyInvisibles(true);
 
 		// All mortals (including offline)
 		for(OfflinePlayer mortal : DPlayer.Util.getMortals())
 			mortals.addPlayer(mortal);
 	}
 
 	protected void loadCommands()
 	{
 		Set<String> commands = Sets.newHashSet();
 		for(DemigodsCommand command : DemigodsCommand.values())
 			commands.addAll(command.getCommand().getCommands());
 		commands.add("demigod");
 		commands.add("dg");
 		commands.add("c");
 		commands.add("o");
 		commands.add("l");
 		commands.add("a");
 		commands.add("n");
 		COMMANDS = ImmutableSet.copyOf(commands);
 	}
 
 	protected void loadPermissions(boolean load)
 	{
 		final PluginManager register = Bukkit.getServer().getPluginManager();
 
 		if(load)
 		{
 			// Default
 			for(DemigodsPermission demigodsPermission : DemigodsPermission.values())
 			{
 				Permission permission = demigodsPermission.getPermission();
 				// catch errors to avoid any possible buggy permissions
 				try
 				{
 					for(Map.Entry<String, Boolean> entry : permission.getChildren().entrySet())
 						register.addPermission(new Permission(entry.getKey(), entry.getValue() ? PermissionDefault.TRUE : PermissionDefault.FALSE));
 					register.addPermission(permission);
 				}
 				catch(Exception ignored)
 				{}
 			}
 
 			// Mythos
 			for(Permission permission : MYTHOS.getPermissions())
 			{
 				// catch errors to avoid any possible buggy permissions
 				try
 				{
 					for(Map.Entry<String, Boolean> entry : permission.getChildren().entrySet())
 						register.addPermission(new Permission(entry.getKey(), entry.getValue() ? PermissionDefault.TRUE : PermissionDefault.FALSE));
 					register.addPermission(permission);
 				}
 				catch(Exception ignored)
 				{}
 			}
 
 			// Alliances, Deities, and Abilities
 			for(final Alliance alliance : MYTHOS.getAlliances())
 			{
 				register.addPermission(new Permission(alliance.getPermission(), "The permission to use the " + alliance.getName() + " alliance.", alliance.getPermissionDefault(), new HashMap<String, Boolean>()
 				{
 					{
 						for(Deity deity : Alliance.Util.getLoadedDeitiesInAlliance(alliance))
 						{
 							register.addPermission(new Permission(deity.getPermission(), alliance.getPermissionDefault()));
 							put(deity.getPermission(), alliance.getPermissionDefault().equals(PermissionDefault.TRUE));
 						}
 					}
 				}));
 			}
 
 			// Skill types
 			for(Skill.Type skill : Skill.Type.values())
 				register.addPermission(skill.getPermission());
 		}
 		else
 		{
 			// Default
 			for(DemigodsPermission demigodsPermission : DemigodsPermission.values())
 			{
 				Permission permission = demigodsPermission.getPermission();
 				// catch errors to avoid any possible buggy permissions
 				try
 				{
 					for(Map.Entry<String, Boolean> entry : permission.getChildren().entrySet())
 						register.removePermission(new Permission(entry.getKey(), entry.getValue() ? PermissionDefault.TRUE : PermissionDefault.FALSE));
 					register.removePermission(permission);
 				}
 				catch(Exception ignored)
 				{}
 			}
 
 			// Mythos
 			for(Permission permission : MYTHOS.getPermissions())
 			{
 				// catch errors to avoid any possible buggy permissions
 				try
 				{
 					for(Map.Entry<String, Boolean> entry : permission.getChildren().entrySet())
 						register.removePermission(new Permission(entry.getKey(), entry.getValue() ? PermissionDefault.TRUE : PermissionDefault.FALSE));
 					register.removePermission(permission);
 				}
 				catch(Exception ignored)
 				{}
 			}
 
 			// Alliances, Deities, and Abilities
 			for(final Alliance alliance : MYTHOS.getAlliances())
 			{
 				register.removePermission(new Permission(alliance.getPermission(), "The permission to use the " + alliance.getName() + " alliance.", alliance.getPermissionDefault(), new HashMap<String, Boolean>()
 				{
 					{
 						for(Deity deity : Alliance.Util.getLoadedDeitiesInAlliance(alliance))
 						{
 							register.removePermission(new Permission(deity.getPermission(), alliance.getPermissionDefault()));
 							put(deity.getPermission(), alliance.getPermissionDefault().equals(PermissionDefault.TRUE));
 						}
 					}
 				}));
 			}
 
 			// Skill types
 			for(Skill.Type skill : Skill.Type.values())
 				register.removePermission(skill.getPermission());
 		}
 	}
 
 	protected static void unloadPermissions()
 	{
 		demigods.loadPermissions(false);
 	}
 
 	public static class Util
 	{
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
 
 		public static boolean isDemigodsCommand(String command)
 		{
 			return COMMANDS.contains(command);
 		}
 	}
 
 	// Listeners
 	public enum BaseGameListener
 	{
 		BATTLE(new BattleListener()), CHAT(new ChatListener()), ENTITY(new EntityListener()), FLAG(new FlagListener()), GRIEF(new GriefListener()), PLAYER(new PlayerListener()), TRIBUTE(new TributeListener());
 
 		private Listener listener;
 
 		private BaseGameListener(Listener listener)
 		{
 			this.listener = listener;
 		}
 
 		public Listener getListener()
 		{
 			return listener;
 		}
 	}
 
 	// Permissions
 	public enum DemigodsPermission
 	{
 		BASIC(new Permission("demigods.basic", "The very basic permissions for Demigods.", PermissionDefault.TRUE, new HashMap<String, Boolean>()
 		{
 			{
 				put("demigods.basic.create", true);
 				put("demigods.basic.forsake", true);
 			}
 		})), ADMIN(new Permission("demigods.admin", "The admin permissions for Demigods.", PermissionDefault.OP)), PVP_AREA_COOLDOWN(new Permission("demigods.bypass.pvpareacooldown", "Bypass the wait for leaving/entering PVP zones.", PermissionDefault.FALSE));
 
 		private Permission permission;
 
 		private DemigodsPermission(Permission permission)
 		{
 			this.permission = permission;
 		}
 
 		public Permission getPermission()
 		{
 			return permission;
 		}
 	}
 
 	// Conversations
 	public enum DemigodsConversation
 	{
 		PRAYER(new Prayer());
 
 		private final WrappedConversation conversationInfo;
 
 		private DemigodsConversation(WrappedConversation conversationInfo)
 		{
 			this.conversationInfo = conversationInfo;
 		}
 
 		public WrappedConversation getConversation()
 		{
 			return this.conversationInfo;
 		}
 
 		// Can't touch this. Naaaaaa na-na-na.. Ba-dum, ba-dum.
 		public static interface Category extends Prompt
 		{
 			public String getChatName(ConversationContext context);
 
 			public boolean canUse(ConversationContext context);
 		}
 	}
 
 	// Commands
 	public enum DemigodsCommand
 	{
 		MAIN(new MainCommand()), GENERAL(new GeneralCommands()), DEVELOPMENT(new DevelopmentCommands());
 
 		private WrappedCommand command;
 
 		private DemigodsCommand(WrappedCommand command)
 		{
 			this.command = command;
 		}
 
 		public WrappedCommand getCommand()
 		{
 			return command;
 		}
 	}
 }
