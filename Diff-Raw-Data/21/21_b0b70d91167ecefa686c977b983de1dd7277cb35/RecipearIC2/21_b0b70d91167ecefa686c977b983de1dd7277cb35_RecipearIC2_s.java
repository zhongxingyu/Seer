 package mods.recipear.modules;
 
 import ic2.api.recipe.IRecipeInput;
 import ic2.api.recipe.RecipeOutput;
 import ic2.api.recipe.Recipes;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import mods.recipear.BannedRecipes;
 import mods.recipear.Recipear;
 import mods.recipear.RecipearLogger;
 import mods.recipear.RecipearUtil;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 @Mod(modid = "Recipear2|IC2", name = "IC2", version = "1.0", dependencies="required-after:Recipear2@[2.0,)")
 public class RecipearIC2 {
 
 	private boolean ic2 = false;
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) 
 	{
 		BannedRecipes.AddBannedRecipeType("CENTRIFUGE", "COMPRESSOR", "EXTRACTOR", "MACERATOR", "OREWASHING");
 	}
 
 	@EventHandler
 	public void PostInit(FMLPostInitializationEvent event) 
 	{
 		ic2 = Loader.isModLoaded("IC2");
 
 		if(ic2) {
 			if(BannedRecipes.GetBannedRecipeAmount() > 0) {
 				long startTime = System.currentTimeMillis();
 				RecipearLogger.info("[IC2] Starting in " + event.getSide().toString() + " Mode");
 				RecipearLogger.info("[IC2] Removed " + RemoveFromMachines(Recipes.centrifuge.getRecipes(), "CENTRIFUGE")  + " CENTRIFUGE recipe(s)");
 				RecipearLogger.info("[IC2] Removed " + RemoveFromMachines(Recipes.compressor.getRecipes(), "COMPRESSOR")  + " COMPRESSOR recipe(s)");
 				RecipearLogger.info("[IC2] Removed " + RemoveFromMachines(Recipes.extractor.getRecipes(), "EXTRACTOR")  + " EXTRACTOR recipe(s)");
 				RecipearLogger.info("[IC2] Removed " + RemoveFromMachines(Recipes.macerator.getRecipes(), "MACERATOR")  + " MACERATOR recipe(s)");
 				RecipearLogger.info("[IC2] Removed " + RemoveFromMachines(Recipes.oreWashing.getRecipes(), "OREWASHING")  + " OREWASHING recipe(s)");
 				RecipearLogger.info("[IC2] Finished in " + (System.currentTimeMillis() - startTime) + "ms");
 			}
 		} else {
 			RecipearLogger.info("[IC2] Could not find IC2");
 		}
 	}
 
 	public int RemoveFromMachines(Map recipes, String machine) {
 		int countRemoved = 0;
 		int countChecked = 0;
 		{
 			int NBTTAGSCOUNT = 0, ITEMID, METADATA;
 			String DISPLAYNAME = "Unknown";
 			RecipeOutput RECIPE_OUTPUT_RAW, RECIPE_OUTPUT_NEW;
 			ItemStack RECIPE_OUTPUT;
 
 			RecipearLogger.info("[IC2] Scanning through " + recipes.size() + " recipe(s) for " + machine);
 			
 			for (Iterator itr = recipes.entrySet().iterator(); itr.hasNext();) {
 				try {
 				Map.Entry entry = (Map.Entry) itr.next();
 				Object RECIPE_INPUT_RAW = (Object) entry.getKey();
 				RECIPE_OUTPUT_RAW = (RecipeOutput) entry.getValue();
 				if(RECIPE_INPUT_RAW == null) continue;
 				if(RECIPE_OUTPUT_RAW == null) continue;
 
 				String RECIPE_INPUT_DISPLAYNAME = "N/A";
 
 				if(RECIPE_INPUT_RAW instanceof IRecipeInput) {
 					IRecipeInput recipe = (IRecipeInput)RECIPE_INPUT_RAW;
 					
 					for(ItemStack itemstack : recipe.getInputs()) {
 						
 						try {
 						
 						if(itemstack == null) continue;
						if(itemstack.getDisplayName() == null) continue;
 						
						String temp = "[" + RecipearUtil.getLanguageRegistryEntry(itemstack.getDisplayName()) + ":" + itemstack.getItemDamage() + ", AMOUNT: " + recipe.getAmount() + "]"; 	
 
 						RECIPE_INPUT_DISPLAYNAME = (RECIPE_INPUT_DISPLAYNAME.equals("N/A")) ? temp : ", " + temp;
 						
 						} catch (Exception ex) {
 							RecipearLogger.warning("[IC2] Failed to fetch name for " + itemstack.itemID + ":" + itemstack.getItemDamage());
 						}
 					}
 				}
 
 				//List<ItemStack> RECIPE_OUTPUT_FOUND = new ArrayList<ItemStack>();
 				//List<ItemStack> RECIPE_OUTPUT_OLD = new ArrayList<ItemStack>();
 
 				RecipearLogger.debug("[IC2] INPUT: " + RECIPE_INPUT_DISPLAYNAME);
 
 				boolean found = false;
 
 				for(Iterator<ItemStack> items_itr  = RECIPE_OUTPUT_RAW.items.iterator(); items_itr.hasNext();) {
 					RECIPE_OUTPUT = items_itr.next();
 
 					if (RECIPE_OUTPUT == null) 
 						continue;
 
 					//RECIPE_OUTPUT_OLD.add(RECIPE_OUTPUT);
 
 					ITEMID = RECIPE_OUTPUT.itemID;
 					METADATA = RECIPE_OUTPUT.getItemDamage();
 					if(RECIPE_OUTPUT.getTagCompound() != null)
 						NBTTAGSCOUNT = RECIPE_OUTPUT.getTagCompound().getTags().size();
 					
 					try {
						DISPLAYNAME = RecipearUtil.getLanguageRegistryEntry(RECIPE_OUTPUT.getDisplayName());
 					} catch (Exception ex) {
 						RecipearLogger.warning("[IC2] Failed to fetch name for " + ITEMID + ":" + METADATA);
 					}
 						
 					RecipearLogger.debug("[IC2] OUTPUT: " + DISPLAYNAME + ", ID: " + ITEMID + ", METADATA: " + METADATA + ", NBTCOUNT: " + NBTTAGSCOUNT);
 
 					if((!Recipear.outputting) && BannedRecipes.Check(ITEMID, METADATA, machine) || 
 							BannedRecipes.Check(DISPLAYNAME.replaceAll("\\s+","").toLowerCase(), machine)) {
 						RecipearLogger.info("[IC2] Removing entry(" + RECIPE_INPUT_DISPLAYNAME + ") to craft: " + DISPLAYNAME + ", ID: " + ITEMID + ", METADATA: " + METADATA);
 						//RECIPE_OUTPUT_FOUND.add(RECIPE_OUTPUT);
 						found = true;
 						countRemoved++;
 					}
 				}
 				
 				countChecked++;
 
 				if(found) {
 					//RECIPE_OUTPUT_OLD.removeAll(RECIPE_OUTPUT_FOUND);
 					//RECIPE_OUTPUT_RAW.items = RECIPE_OUTPUT_OLD;
 					itr.remove();
 				}
 				
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 			return countRemoved;
 		}
 	}
 }
