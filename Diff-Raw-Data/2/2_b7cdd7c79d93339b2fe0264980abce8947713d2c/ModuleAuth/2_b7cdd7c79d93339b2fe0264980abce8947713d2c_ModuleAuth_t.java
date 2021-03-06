 package com.ForgeEssentials.auth;
 
 import java.util.HashSet;
 
 import net.minecraftforge.common.MinecraftForge;
 
 import com.ForgeEssentials.api.ForgeEssentialsRegistrar.PermRegister;
 import com.ForgeEssentials.api.modules.FEModule;
 import com.ForgeEssentials.api.modules.FEModule.Config;
 import com.ForgeEssentials.api.modules.FEModule.Init;
 import com.ForgeEssentials.api.modules.FEModule.PreInit;
 import com.ForgeEssentials.api.modules.FEModule.ServerInit;
 import com.ForgeEssentials.api.modules.FEModule.ServerStop;
 import com.ForgeEssentials.api.modules.event.FEModuleInitEvent;
 import com.ForgeEssentials.api.modules.event.FEModulePreInitEvent;
 import com.ForgeEssentials.api.modules.event.FEModuleServerInitEvent;
 import com.ForgeEssentials.api.modules.event.FEModuleServerStopEvent;
 import com.ForgeEssentials.api.permissions.IPermRegisterEvent;
 import com.ForgeEssentials.api.permissions.RegGroup;
 import com.ForgeEssentials.core.ForgeEssentials;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @FEModule(name = "AuthLogin", parentMod = ForgeEssentials.class, configClass = AuthConfig.class)
 public class ModuleAuth
 {
 	@Config
 	public static AuthConfig			config;
 
 	public static boolean				forceEnabled;
 	public static boolean				checkVanillaAuthStatus;
 
 	public static boolean				enabled;
 
 	public static boolean				allowOfflineReg;
 
 	public static VanillaServiceChecker	vanillaCheck;
 	private static EncryptionHelper		pwdEnc;
 
 	private static LoginHandler			loginHandler;
 	private static EventHandler			eventHandler;
 
 	public static HashSet<String>		unLogged		= new HashSet<String>();
 	public static HashSet<String>		unRegistered	= new HashSet<String>();
 
 	public static String				salt			= EncryptionHelper.generateSalt();
 
 	@PreInit
 	public void preInit(FEModulePreInitEvent e)
 	{
 		// No Auth Module on client
 		if (e.getFMLEvent().getSide().isClient())
 		{
 			e.getModuleContainer().isLoadable = false;
 		}
 	}
 
 	@Init
 	public void load(FEModuleInitEvent e)
 	{
 		pwdEnc = new EncryptionHelper();
 		eventHandler = new EventHandler();
 
 		loginHandler = new LoginHandler();
 		GameRegistry.registerPlayerTracker(loginHandler);
 	}
 
 	@ServerInit
 	public void serverStarting(FEModuleServerInitEvent e)
 	{
 		e.registerServerCommand(new CommandAuth());
 
 		if (checkVanillaAuthStatus && !forceEnabled)
 		{
 			vanillaCheck = new VanillaServiceChecker();
 			TickRegistry.registerScheduledTickHandler(vanillaCheck, Side.SERVER);
 		}
 
 		MinecraftForge.EVENT_BUS.register(eventHandler);
 	}
 
 	@PermRegister
 	public static void regierPerms(IPermRegisterEvent event)
 	{
 		event.registerPermissionLevel("ForgeEssentials.ModuleAuth.admin", RegGroup.OWNERS);
		event.registerPermissionLevel("ForgeEssentials.ModuleAuth", RegGroup.GUESTS);
 	}
 
 	@ServerStop
 	public void serverStop(FEModuleServerStopEvent e)
 	{
 		MinecraftForge.EVENT_BUS.unregister(eventHandler);
 	}
 
 	public static boolean vanillaMode()
 	{
 		return FMLCommonHandler.instance().getSidedDelegate().getServer().isServerInOnlineMode();
 	}
 
 	public static String encrypt(String str)
 	{
 		return pwdEnc.sha1(str);
 	}
 }
