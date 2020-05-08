 package com.censoredsoftware.demigods.engine;
 
 import com.censoredsoftware.demigods.engine.ability.Ability;
 import com.censoredsoftware.demigods.engine.command.DevelopmentCommands;
 import com.censoredsoftware.demigods.engine.command.GeneralCommands;
 import com.censoredsoftware.demigods.engine.command.MainCommand;
 import com.censoredsoftware.demigods.engine.conversation.Prayer;
 import com.censoredsoftware.demigods.engine.data.ThreadManager;
 import com.censoredsoftware.demigods.engine.data.TributeManager;
 import com.censoredsoftware.demigods.engine.deity.Alliance;
 import com.censoredsoftware.demigods.engine.deity.Deity;
import com.censoredsoftware.demigods.engine.helper.MojangIdGrabber;
 import com.censoredsoftware.demigods.engine.helper.QuitReasonHandler;
 import com.censoredsoftware.demigods.engine.helper.WrappedCommand;
 import com.censoredsoftware.demigods.engine.helper.WrappedConversation;
 import com.censoredsoftware.demigods.engine.item.DivineItem;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.listener.ZoneListener;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.Skill;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.util.Configs;
 import com.censoredsoftware.demigods.engine.util.Messages;
 import com.censoredsoftware.demigods.greek.GreekMythos;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Sets;
 import org.bukkit.Bukkit;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.conversations.Prompt;
 import org.bukkit.event.Listener;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.ServicesManager;
 import org.mcstats.MetricsLite;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class Demigods {
     // Constants
     public static String SAVE_PATH;
 
     // Public Static Access
     public static final DemigodsPlugin PLUGIN;
     public static final ConversationFactory CONVERSATION_FACTORY;
     public static final Translation LANGUAGE;
 
     // Disabled Stuff
     public static ImmutableSet<String> DISABLED_WORLDS;
     public static ImmutableSet<String> COMMANDS;
 
     // Mythos
     public static final Mythos MYTHOS;
 
     // Load what is possible to load right away.
     static {
         // Allow static access.
         PLUGIN = (DemigodsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Demigods");
 
         // Load the Mythos.
         ServicesManager servicesManager = PLUGIN.getServer().getServicesManager();
         RegisteredServiceProvider<Mythos> mythosProvider = servicesManager.getRegistration(Mythos.class);
         if (mythosProvider != null) {
             MYTHOS = mythosProvider.getProvider();
             Messages.info("The " + MYTHOS.getName() + " mythos created by " + MYTHOS.getAuthor() + " has loaded!");
         } else MYTHOS = new GreekMythos();
 
         // Conversation factory static access.
         CONVERSATION_FACTORY = new ConversationFactory(PLUGIN);
 
         // Language data.
         LANGUAGE = new Translation();
 
         // Initialize metrics
         try {
             (new MetricsLite(PLUGIN)).start();
         } catch (Exception ignored) {
         }
     }
 
     // Load everything else.
     protected static void load() {
         // Start the data
         SAVE_PATH = PLUGIN.getDataFolder() + "/data/"; // Don't change this.
 
         if (!PLUGIN.getServer().getOnlineMode()) {
             Messages.severe("Demigods might not work in offline mode.");
             Messages.severe("We depend on Mojang's servers for ids.");
             Messages.severe("Any player who joins and is not premium");
             Messages.severe("may be kicked from the game.");
         }
 
         // Check for world load errors
         if (loadWorlds() > 0) {
             Messages.severe("Demigods was unable to confirm any worlds.");
             Messages.severe("This may be caused by misspelt world names.");
             Messages.severe("Multi-world plugins can cause this message,");
             Messages.severe("and in that case this may be a false alarm.");
         }
 
         // Load listeners, commands, and permissions
         loadListeners();
         loadCommands();
         loadPermissions();
 
        // Load Mojang Id Grabber
        MojangIdGrabber.load();

         // Update usable characters
         DCharacter.Util.updateUsableCharacters();
 
         // Start threads
         ThreadManager.startThreads();
 
         // Regenerate structures
         Structure.Util.regenerateStructures();
 
         // Initialize tribute tracking
         TributeManager.initializeTributeTracking();
 
         if (Util.isRunningSpigot()) Messages.info(("Spigot found, will use extra API features."));
         else Messages.warning(("Without Spigot, some features may not work."));
 
         // Handle online characters
         for (DCharacter character : DCharacter.Util.loadAll())
             character.getMeta().cleanSkills();
     }
 
     private static int loadWorlds() {
         Set<String> disabledWorlds = Sets.newHashSet();
         int erroredWorlds = 0;
         for (String world : Configs.getSettingList("restrictions.disabled_worlds")) {
             disabledWorlds.add(world);
             erroredWorlds += Bukkit.getServer().getWorld(world) == null ? 1 : 0;
         }
         DISABLED_WORLDS = ImmutableSet.copyOf(disabledWorlds);
         return erroredWorlds;
     }
 
     private static void loadListeners() {
         PluginManager register = Bukkit.getServer().getPluginManager();
 
         // Mythos
         for (Listener listener : MYTHOS.getListeners())
             register.registerEvents(listener, PLUGIN);
 
         // Disabled worlds
         if (!DISABLED_WORLDS.isEmpty()) register.registerEvents(new ZoneListener(), PLUGIN);
 
         // Abilities
         for (Ability ability : Ability.Util.getLoadedAbilities())
             if (ability.getListener() != null) register.registerEvents(ability.getListener(), PLUGIN);
 
         // Structures
         for (Structure structure : Sets.filter(MYTHOS.getStructures(), new Predicate<Structure>() {
             @Override
             public boolean apply(Structure structure) {
                 return structure.getUniqueListener() != null;
             }
         }))
             if (structure.getUniqueListener() != null) register.registerEvents(structure.getUniqueListener(), PLUGIN);
 
         // Conversations
         for (WrappedConversation conversation : Collections2.filter(Collections2.transform(Sets.newHashSet(DemigodsConversation.values()), new Function<DemigodsConversation, WrappedConversation>() {
             @Override
             public WrappedConversation apply(DemigodsConversation conversation) {
                 return conversation.getConversation();
             }
         }), new Predicate<WrappedConversation>() {
             @Override
             public boolean apply(WrappedConversation conversation) {
                 return conversation.getUniqueListener() != null;
             }
         }))
             if (conversation.getUniqueListener() != null)
                 register.registerEvents(conversation.getUniqueListener(), PLUGIN);
 
         // Special Items
         for (DivineItem divineItem : DivineItem.values()) {
             if (divineItem.getUniqueListener() != null) register.registerEvents(divineItem.getUniqueListener(), PLUGIN);
             if (divineItem.getRecipe() != null) PLUGIN.getServer().addRecipe(divineItem.getRecipe());
         }
 
         // Quit reason.
         Bukkit.getServer().getLogger().addHandler(new QuitReasonHandler());
     }
 
     private static void loadCommands() {
         Set<String> commands = Sets.newHashSet();
         for (DemigodsCommand command : DemigodsCommand.values())
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
 
     private static void loadPermissions() {
         final PluginManager register = Bukkit.getServer().getPluginManager();
 
         // Mythos
         for (Permission permission : MYTHOS.getPermissions()) {
             // catch errors to avoid any possible buggy permissions
             try {
                 for (Map.Entry<String, Boolean> entry : permission.getChildren().entrySet())
                     register.addPermission(new Permission(entry.getKey(), entry.getValue() ? PermissionDefault.TRUE : PermissionDefault.FALSE));
                 register.addPermission(permission);
             } catch (Exception ignored) {
             }
         }
 
         // Alliances, Deities, and Abilities
         for (final Alliance alliance : MYTHOS.getAlliances()) {
             register.addPermission(new Permission(alliance.getPermission(), "The permission to use the " + alliance.getName() + " alliance.", alliance.getPermissionDefault(), new HashMap<String, Boolean>() {
                 {
                     for (Deity deity : Alliance.Util.getLoadedDeitiesInAlliance(alliance)) {
                         register.addPermission(new Permission(deity.getPermission(), alliance.getPermissionDefault()));
                         put(deity.getPermission(), alliance.getPermissionDefault().equals(PermissionDefault.TRUE));
                     }
                 }
             }));
         }
 
         // Skill types
         for (Skill.Type skill : Skill.Type.values())
             register.addPermission(skill.getPermission());
     }
 
     protected static void unloadPermissions() {
         final PluginManager register = Bukkit.getServer().getPluginManager();
 
         // Mythos
         for (Permission permission : MYTHOS.getPermissions()) {
             // catch errors to avoid any possible buggy permissions
             try {
                 for (Map.Entry<String, Boolean> entry : permission.getChildren().entrySet())
                     register.removePermission(new Permission(entry.getKey(), entry.getValue() ? PermissionDefault.TRUE : PermissionDefault.FALSE));
                 register.removePermission(permission);
             } catch (Exception ignored) {
             }
         }
 
         // Alliances, Deities, and Abilities
         for (final Alliance alliance : MYTHOS.getAlliances()) {
             register.removePermission(new Permission(alliance.getPermission(), "The permission to use the " + alliance.getName() + " alliance.", alliance.getPermissionDefault(), new HashMap<String, Boolean>() {
                 {
                     for (Deity deity : Alliance.Util.getLoadedDeitiesInAlliance(alliance)) {
                         register.removePermission(new Permission(deity.getPermission(), alliance.getPermissionDefault()));
                         put(deity.getPermission(), alliance.getPermissionDefault().equals(PermissionDefault.TRUE));
                     }
                 }
             }));
         }
 
         // Skill types
         for (Skill.Type skill : Skill.Type.values())
             register.removePermission(skill.getPermission());
     }
 
     public static class Util {
         public static boolean isRunningSpigot() {
             try {
                 Bukkit.getServer().getWorlds().get(0).spigot();
                 return true;
             } catch (Throwable ignored) {
             }
             return false;
         }
 
         public static boolean isDemigodsCommand(String command) {
             return COMMANDS.contains(command);
         }
     }
 
     // Conversations
     public enum DemigodsConversation {
         PRAYER(new Prayer());
 
         private final WrappedConversation conversationInfo;
 
         private DemigodsConversation(WrappedConversation conversationInfo) {
             this.conversationInfo = conversationInfo;
         }
 
         public WrappedConversation getConversation() {
             return this.conversationInfo;
         }
 
         // Can't touch this. Naaaaaa na-na-na.. Ba-dum, ba-dum.
         public static interface Category extends Prompt {
             public String getChatName(ConversationContext context);
 
             public boolean canUse(ConversationContext context);
         }
     }
 
     // Commands
     public enum DemigodsCommand {
         MAIN(new MainCommand()), GENERAL(new GeneralCommands()), DEVELOPMENT(new DevelopmentCommands());
 
         private WrappedCommand command;
 
         private DemigodsCommand(WrappedCommand command) {
             this.command = command;
         }
 
         public WrappedCommand getCommand() {
             return command;
         }
     }
 }
