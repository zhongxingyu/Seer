 package com.tehbeard.forge.schematic.shell;
 
 import net.minecraft.command.CommandHandler;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 
 import com.tehbeard.forge.schematic.shell.items.SetSquareItem;
 import com.tehbeard.forge.schematic.shell.network.ShellPacketManager;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
@Mod(modid = Reference.MODID,name="LibSchematic.sh",version=Reference.VERSION,useMetadata=true)
 @NetworkMod(packetHandler=ShellPacketManager.class,channels=Reference.BASE_CHANNEL)
 public class LibSchematicShell {
     
     @SidedProxy(serverSide="com.tehbeard.forge.schematic.shell.CommonProxy",clientSide="com.tehbeard.forge.schematic.shell.ClientProxy")
     public static CommonProxy proxy;
     public static final SetSquareItem setSquareItem = new SetSquareItem(30000);
     
     public static CreativeTabs tabLibSch = new CreativeTabs("tabLibSchematic"){
     	 public ItemStack getIconItemStack() {
     		 return new ItemStack(setSquareItem,1,0);
     	 }
     	
     };
     
     
     //public static final 
     @EventHandler
     public void init(FMLInitializationEvent event){
         
         proxy.registerItems();
         
         proxy.registerGUI();
         
 
     }
     
     @EventHandler
     public void preServerStart(FMLServerAboutToStartEvent event){
         proxy.registerCommands(((CommandHandler)event.getServer().getCommandManager()));
     }
 
 }
