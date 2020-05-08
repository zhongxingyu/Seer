 package mods.recipear;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.EnumChatFormatting;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartedEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
@Mod(modid = "Recipear2", name = "Recipear2", version = "2.0.0", dependencies="required-after:Forge@[9.10,);before:NotEnoughItems")
 public class Recipear 
 {
 	public static boolean debug = true;
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) 
 	{
 		BannedRecipes.AddBannedRecipeType("WORKBENCH","FURNACE");
 		new RecipearLogger().logger = event.getModLog();
 		new RecipearConfig(event);
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) 
 	{
 		long startTime = System.currentTimeMillis();
 		//RecipearConfig.debug = false;
 		RecipearLogger.info("Starting in " + event.getSide().toString() + " Mode");
 		
 		String supported_types = "Supported TYPES are";
 		
 		for(String type : BannedRecipes.getBannedRecipeTypes()) {
 			supported_types += " " + type;
 		}
 		
 		RecipearLogger.info(supported_types);
 		
 		RecipearVanilla recipear = new RecipearVanilla(); 
 		if(BannedRecipes.GetBannedRecipeAmount() > 0) {
 			RecipearLogger.info("Removed " + recipear.RemoveRecipes(event.getSide()) + " Workbench recipe(s)");
 			RecipearLogger.info("Removed " + recipear.RemoveFurnaceRecipes() + " Furnace recipe(s)");
 		}
 		long endTime = System.currentTimeMillis();
 		RecipearLogger.info("Finished in " + (endTime - startTime) + "ms");
 	}
 
 	@EventHandler
 	void ServerStartedEvent(FMLServerStartedEvent event) {
 		if((BannedRecipes.GetBannedRecipeAmount() > 0) && (RecipearConfig.removeingame)) {
 			RecipearLogger.info("Starting Player Tracker and Tick Handler for Removing items from player");
 			GameRegistry.registerPlayerTracker(new RecipearPlayerEvent());
 			TickRegistry.registerScheduledTickHandler(new RecipearPlayerTick(), Side.SERVER);
 		}
 	}
 
 	public static void RemoveBannedItemsFromInventory(EntityPlayer player) {
 
 		if (!RecipearUtil.isOP(player)) {
 
 			ItemStack[] whole_inventory = RecipearUtil.concat(
 					player.inventory.mainInventory,
 					player.inventory.armorInventory);
 
 			for (ItemStack RECIPE_OUTPUT : whole_inventory) {
 				if (RECIPE_OUTPUT != null) 
 				{
 					String DISPLAYNAME = RECIPE_OUTPUT.getDisplayName();
 					if(LanguageRegistry.instance().getStringLocalization(DISPLAYNAME).length() > 0)
 						DISPLAYNAME = LanguageRegistry.instance().getStringLocalization(DISPLAYNAME);
 
 					if (BannedRecipes.Check(RECIPE_OUTPUT.itemID, RECIPE_OUTPUT.getItemDamage(), "INVENTORY") 
 							|| BannedRecipes.Check(DISPLAYNAME, "INVENTORY"))
 					{
 						player.sendChatToPlayer(
 								ChatMessageComponent.createFromText(EnumChatFormatting.YELLOW + DISPLAYNAME
 										+ " is banned, removing from inventory..."));
 						player.inventory.clearInventory(RECIPE_OUTPUT.itemID, RECIPE_OUTPUT.getItemDamage());
 					}
 				}
 			}
 
 			whole_inventory = null;
 		}
 	}
 }
