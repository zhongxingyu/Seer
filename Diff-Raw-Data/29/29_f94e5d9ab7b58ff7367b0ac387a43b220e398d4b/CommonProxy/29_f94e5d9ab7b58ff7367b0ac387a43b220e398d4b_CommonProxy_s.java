 package Enderware.common;
 
 import net.minecraftforge.common.MinecraftForge;
 import Enderware.listener.Eventhandler;
 import Enderware.listener.PlayerTracker;
 import Enderware.listener.Tickhandler;
 import Enderware.modsupport.computercraft.peripheral.CommandPeripheral;
 import Enderware.permissions.PermissionManager;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class CommonProxy {
 public void registerRenders(){GameRegistry.registerTileEntity(CommandPeripheral.class, "CommandPeripheral");}
 public void registerHandlers(){
 	MinecraftForge.EVENT_BUS.register(new Eventhandler());
 	GameRegistry.registerPlayerTracker(new PlayerTracker());
 	
 	TickRegistry.registerTickHandler(new Tickhandler(),Side.SERVER);
 }
 public void registerPermissions(){
 	PermissionManager.instance.registerPermission("Colors", 2);
 	PermissionManager.instance.registerPermission("test", 2);
	PermissionManager.instance.registerPermission("ChangeConfigs", 10);
 	PermissionManager.instance.registerGroup(" Admin", 10);
 	
 }
 }
