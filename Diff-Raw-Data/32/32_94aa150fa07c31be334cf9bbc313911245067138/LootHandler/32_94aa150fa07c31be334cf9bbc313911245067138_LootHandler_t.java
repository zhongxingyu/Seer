 package enosphorous.chateau_romani.handlers;
 
 import enosphorous.chateau_romani.common.Items;
 import enosphorous.chateau_romani.common.Reference;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 
 public class LootHandler {
 	
 	public static void generate_loot () {
 		
 		/**
 		 * @param getInfo(ChestGenHooks) - What type of chest the loot generates in.
 		 * @param ItemStack - The loot that generates in said chest.
 		 * @param int COMES LAST- Rarity of said loot, from 1 to 100, one being as rare as a golden apple.
 		 * @param int - Minimum size of the ItemStack when generated into the chest.
 		 * @param int - Maximum size of the ItemStack when generated into the chest.
 		 */
 		
 		//Chateau Romani
 		ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 11));
 		ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 4));
 		ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 7));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 11));
 		
 		if (Reference.FORCE_DEBUG){
 			System.out.println("[CHATEAU ROMANI] Item 'Chateau Romani' added as loot successfully.");
 		}
 	
 		//Milk Bottle
 		ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 13));
 		ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 21));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 13));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 19));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 13));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(Items.chateau_romani), 1, 1, 31));
 		
 		if (Reference.FORCE_DEBUG){
 			System.out.println("[CHATEAU ROMANI] Item 'Milk Bottle' added as loot successfully.");
 		}
 			
 		//Fermented Grains
 		ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Items.fermented_grain), 1, 3, 23));
 		ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Items.fermented_grain), 1, 4, 33));
 		ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(Items.fermented_grain), 1, 6, 13));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(Items.fermented_grain), 1, 3, 23));
 		
 		if (Reference.FORCE_DEBUG){
 			System.out.println("[CHATEAU ROMANI] Item 'Fermented Grains' added as loot successfully.");
 		}
		
		//Bottled Fire
		ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Items.bottled_fire), 1, 1, 8));
		ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(Items.bottled_fire), 1, 1, 8));
		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(Items.bottled_fire), 1, 1, 8));
		
		if (Reference.FORCE_DEBUG){
			System.out.println("[CHATEAU ROMANI] Item 'Bottled Fire' added as loot successfully.");
		}
 	
 		
 		if (Reference.FORCE_DEBUG){
 			System.out.println("[CHATEAU ROMANI] Dungeon loot generated.");
 		}
 	}
 
 }
