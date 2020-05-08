 package tech.bouncingblockmod;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.command.ICommand;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.util.ChatMessageComponent;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 /**
  * <i>The Bouncing Block Mod was made in a joint effort by TechnicalParadox & Gim949
  * <p>"This mod adds new blocks to the game that allow for even more possibilities in
  * the world of Minecraft!"</p></i>
  * 
  * @author <i>TechnicalParadox & Gim949</i>
  */
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class bouncingblocks { //Recipes!!!!!
 	
 	//Adds a new creative tab to the game
 	public static CreativeTabs tab = new TabBounce(CreativeTabs.getNextID(), Reference.MOD_NAME);
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event){
 		BBConfig.Config(event);
 		
 		BBBlocks.registers();
 	}
 	
 	/**
 	 * <p>A better way of registering your block. It sets the UnlocalizedName and registers 
 	 * the block with {@link GameRegistry}</p>
 	 * 
 	 * @param block
 	 * @param register
 	 */
 	public static void registerBlock(net.minecraft.block.Block block, String register){
 		GameRegistry.registerBlock(block, register);
 		block.setUnlocalizedName(register);
 	}
 	
 	//Command Stuff starts here ============================================
 	
 	@EventHandler
 	public void serverStarting(FMLServerStartingEvent event){
 		event.registerServerCommand(new BBCommand());
 	}
 	
 	public class BBCommand implements ICommand{
 		public List commands;
 		
 		@Override
 		public int compareTo(Object o) {
 			return 0;
 		}
 
 		/**
 		 * The name of the command
 		 */
 		@Override
 		public String getCommandName() {
 			return "bouncingblocks";
 		}
 
 		/**
 		 * Displays the usage and in /help
 		 */
 		@Override
 		public String getCommandUsage(ICommandSender icommandsender) {
 			return "/bouncingblocks Tells what version of the Bouncing blocks mod you have (Allias: /bb)";
 		}
 
 		/**
 		 * Our main command (bouncingblocks) and an allias to the main command (bb)
 		 */
 		@Override
 		public List getCommandAliases() {
 			commands = new ArrayList();
 			commands.add("bouncingblocks");
 			commands.add("bb");
 			
 			return this.commands;
 		}
 
 		/**
 		 * Executing the command
 		 */
 		@Override
 		public void processCommand(ICommandSender icommandsender, String[] astring) {
			/* error on this line */icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("\2478Bouncing block Mod Version: " + Reference.MOD_VERSION));
 		}
 
 		@Override
 		public boolean canCommandSenderUseCommand(ICommandSender icommandsender) {
 			//Always keep true
 			return true;
 		}
 
 		@Override
 		public List addTabCompletionOptions(ICommandSender icommandsender, String[] astring) {
 			return null;
 		}
 
 		@Override
 		public boolean isUsernameIndex(String[] astring, int i) {
 			return false;
 		}
 	}
 	
 	//Command Stuff ends here ===================================================
 }
