 package com.gravypod.alladmin;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.PlayerSelector;
 import net.minecraft.command.ServerCommandManager;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.server.MinecraftServer;
 import net.minecraftforge.common.MinecraftForge;
 
 import com.gravypod.alladmin.commands.wrapped.BanCommand;
 import com.gravypod.alladmin.commands.wrapped.BanIpCommand;
 import com.gravypod.alladmin.commands.wrapped.DebugCommand;
 import com.gravypod.alladmin.commands.wrapped.DefaultGameModeCommand;
 import com.gravypod.alladmin.commands.wrapped.DeopCommand;
 import com.gravypod.alladmin.commands.wrapped.DifficultyCommand;
 import com.gravypod.alladmin.commands.wrapped.EffectCommand;
 import com.gravypod.alladmin.commands.wrapped.EnchantCommand;
 import com.gravypod.alladmin.commands.wrapped.GameModeCommand;
 import com.gravypod.alladmin.commands.wrapped.GameRuleCommand;
 import com.gravypod.alladmin.commands.wrapped.GiveCommand;
 import com.gravypod.alladmin.commands.wrapped.HelpCommand;
 import com.gravypod.alladmin.commands.wrapped.KickCommand;
 import com.gravypod.alladmin.commands.wrapped.ListCommand;
 import com.gravypod.alladmin.commands.wrapped.OpCommand;
 import com.gravypod.alladmin.commands.wrapped.PardonCommand;
 import com.gravypod.alladmin.commands.wrapped.PardonIpCommand;
 import com.gravypod.alladmin.commands.wrapped.PlaySoundCommand;
 import com.gravypod.alladmin.commands.wrapped.PlayerTimeoutCommand;
 import com.gravypod.alladmin.commands.wrapped.SaveAllCommand;
 import com.gravypod.alladmin.commands.wrapped.SaveOffCommand;
 import com.gravypod.alladmin.commands.wrapped.SaveOnCommand;
 import com.gravypod.alladmin.commands.wrapped.ScoreboardCommand;
 import com.gravypod.alladmin.commands.wrapped.ServerEmoteCommand;
 import com.gravypod.alladmin.commands.wrapped.ServerMessageCommand;
 import com.gravypod.alladmin.commands.wrapped.ServerTpCommand;
 import com.gravypod.alladmin.commands.wrapped.SetSpawnpointCommand;
 import com.gravypod.alladmin.commands.wrapped.ShowSeedCommand;
 import com.gravypod.alladmin.commands.wrapped.SpreadPlayersCommand;
 import com.gravypod.alladmin.commands.wrapped.StopCommand;
 import com.gravypod.alladmin.commands.wrapped.TimeCommand;
 import com.gravypod.alladmin.commands.wrapped.ToggleDownfallCommand;
 import com.gravypod.alladmin.commands.wrapped.WeatherCommand;
 import com.gravypod.alladmin.commands.wrapped.WhitelistCommand;
 import com.gravypod.alladmin.commands.wrapped.XPCommand;
 import com.gravypod.alladmin.files.ConfigFiles;
 import com.gravypod.alladmin.files.PermissionFiles;
 import com.gravypod.alladmin.files.UserFiles;
 import com.gravypod.alladmin.files.WarpFiles;
 import com.gravypod.alladmin.permissions.PermissionManager.CommandPermissions;
 import com.gravypod.alladmin.user.AllAdminConsole;
 import com.gravypod.alladmin.user.AllAdminUser;
 
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.event.FMLServerStoppingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 // used in 1.6.2
 import cpw.mods.fml.common.registry.GameRegistry;
 
 @Mod(modid = "AllAdmin", name = "AllAdmin", version = AllAdmin.version)
 @NetworkMod(clientSideRequired = false)
 public class AllAdmin {
 	
	public static final String version = "0.0.1a";
 	public static boolean running;
 	
 	public static HashMap<String, IUser> users = new HashMap<String, IUser>();
 	
 	public static final I18n localization = new I18n();
 	
 	@Mod.EventHandler
 	public static void preInit(FMLServerStartingEvent event) {
 		
 		running = event.getSide().isServer();
 		
 		if (!running) {
 			return;
 		}
 		
 		MinecraftForge.EVENT_BUS.register(new AllAdminEventHandler());
 		
 		System.out.println("[AllAdmin] Starting! Created by gravypod. Version " + version);
 		
 		if (!getDataDir().exists() || !getDataDir().isDirectory()) {
 			getDataDir().mkdirs();
 			saveConfigs();
 		}
 		
 		ServerCommandManager ch = (ServerCommandManager) MinecraftServer.getServer().getCommandManager();
 		
 		for (AllAdminCommandRegistry reg : AllAdminCommandRegistry.values()) {
 			ch.registerCommand(reg.getCommand());
 		}
 		
 		CommandBase[] baseCommands = new CommandBase[] {
 				new BanCommand(CommandPermissions.BAN),
 				new BanIpCommand(CommandPermissions.BAN),
 				new DebugCommand(CommandPermissions.DEBUG),
 				new DefaultGameModeCommand(CommandPermissions.DEFAULT_GAME_MODE),
 				new DeopCommand(CommandPermissions.OP),
 				new DifficultyCommand(CommandPermissions.DIFFICULTY),
 				new EffectCommand(CommandPermissions.EFFECT),
 				new EnchantCommand(CommandPermissions.ENCHANT),
 				new GameModeCommand(CommandPermissions.GAME_MODE),
 				new ServerEmoteCommand(CommandPermissions.ME),
 				new GameRuleCommand(CommandPermissions.GAME_RULE),
 				new GiveCommand(CommandPermissions.GIVE),
 				new HelpCommand(CommandPermissions.HELP),
 				new KickCommand(CommandPermissions.KICK),
 				new ListCommand(CommandPermissions.LIST),
 				new OpCommand(CommandPermissions.OP),
 				new PardonCommand(CommandPermissions.PARDON),
 				new PardonIpCommand(CommandPermissions.PARDON),
 				new PlayerTimeoutCommand(CommandPermissions.TIMEOUT),
 				new PlaySoundCommand(CommandPermissions.PLAY_SOUND),
 				new SaveAllCommand(CommandPermissions.SAVE_ALL),
 				new SaveOffCommand(CommandPermissions.SAVE_OFF),
 				new SaveOnCommand(CommandPermissions.SAVE_ON),
 				new ScoreboardCommand(CommandPermissions.SCOREBOARD),
 				new ServerMessageCommand(CommandPermissions.SAY),
 				new ServerTpCommand(CommandPermissions.TP),
 				new SetSpawnpointCommand(CommandPermissions.SET_SPAWN),
 				new ShowSeedCommand(CommandPermissions.SEED),
 				new SpreadPlayersCommand(CommandPermissions.SPREAD_PLAYERS),
 				new StopCommand(CommandPermissions.STOP),
 				new TimeCommand(CommandPermissions.TIME),
 				new ToggleDownfallCommand(CommandPermissions.TOGGLE_DOWNFALL),
 				new WeatherCommand(CommandPermissions.WEATHER),
 				new WhitelistCommand(CommandPermissions.WHITELIST),
 				new XPCommand(CommandPermissions.XP)
 				
 		};
 		
 		for (CommandBase base : baseCommands) {
 			String name = base.getCommandName();
 			ch.getCommands().remove(name);
 			ch.getCommands().put(name, base);
 			
 			List<String> alias = base.getCommandAliases();
 			if (alias != null) {
 				for (String a : alias) {
 					ch.getCommands().put(a, base);
 				}
 			}
 		}
 		
 		GameRegistry.registerPlayerTracker(new IPlayerTracker() {
 			@Override
 			public void onPlayerRespawn(EntityPlayer player) {}
 			@Override
 			public void onPlayerLogout(EntityPlayer player) {
 				IUser user = getUser(player.username);
 				removeUser(player.username);
 				if (user != null) {
 					user.logout();
 				}
 			}
 			@Override
 			public void onPlayerLogin(EntityPlayer player) {}
 			@Override
 			public void onPlayerChangedDimension(EntityPlayer player) {}
 		});
 		
 	}
 	
 	@Mod.EventHandler
 	public static void stopping(FMLServerStoppingEvent event) {
 		if (!running) {
 			return;
 		}
 		for (IUser user : users.values()) {
 			user.logout();
 		}
 		saveConfigs();
 	}
 	
 	
 	public static File getDataDir() {
 		return new File("config/AllAdmin");
 	}
 	
 	/**
 	 * Get an {@link IUser}
 	 * @param name - name of the users
 	 * @return IUser who has the name provided or null if no user existed or the plugin has not loaded
 	 */
 	public static IUser getUser(Object sender) {
 		
 		if (!running) {
 			return null;
 		}
 		
 		String name = null;
 		
 		if (sender instanceof EntityPlayerMP) {
 			name = ((EntityPlayerMP) sender).getCommandSenderName();
 		} else if (sender instanceof String) {
 			name = (String) sender;
 		} else {
 			name = "CONSOLE_USER";
 		}
 		
 		name = name.toLowerCase();
 		
 		if (!users.containsKey(name)) {
 			if (sender instanceof EntityPlayerMP) {
 				users.put(name, new AllAdminUser((EntityPlayerMP) sender));
 			} else if (sender instanceof String) {
 				users.put(name, new AllAdminUser(MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername((String) sender)));
 			} else {
 				users.put(name, new AllAdminConsole());	
 			}
 		}
 		
 		return users.get(name);
 		
 	}
 	
 	public static void removeUser(String username) {
 		users.remove(username.toLowerCase());
 	}
 
 	public static String getString(String name) {
 		return localization.getColoredMessage(name);
 	}
 	
 	public static void saveConfigs() {
 		try {
 			PermissionFiles.save();
 			WarpFiles.save();
 			ConfigFiles.save();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
