 package MysticWorld;
 
 import MysticWorld.Blocks.BlockHandler;
 import MysticWorld.Items.ItemHandler$1;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class Lang
 {
 	public static void init()
 	{
 		//Creative Tab Name
 		LanguageRegistry.instance().addStringLocalization("itemGroup.MysticWorldTab", "en_US", "Mystic World!");
 		
 		//Blocks
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.OreImbuedStone, 1, 0), "Charred Stone");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.OreImbuedStone, 1, 1), "Moistened Stone");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.OreImbuedStone, 1, 2), "Mossified Stone");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.OreImbuedStone, 1, 3), "Oxygenated Stone");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.OreImbuedStone, 1, 4), "Energized Stone");
 		
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 0), "Bush");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 1), "Poisonous Bush");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 2), "Bush of Weakness");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 3), "Bush of Slowness");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 4), "Bush of Harming");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 5), "Withering Bush");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 6), "Bush of Health");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 7), "Bush of Speed");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 8), "Bush of Flames");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 9), "Bush of Regen");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 10), "Bush of Vision");
 		LanguageRegistry.instance().addName(new ItemStack(BlockHandler.bush, 1, 11), "Bush of Invisibility");
 		
 		//Items
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.staffParts, 1, 0), "Staff");
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.staffParts, 1, 1), "Staff Hilt");
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.staffParts, 1, 2), "Staff Handle");
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.staffParts, 1, 3), "Staff Topper");
 		
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.imbuedShard, 1, 0), "Charred Shard");
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.imbuedShard, 1, 1), "Moistened Shard");
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.imbuedShard, 1, 2), "Mossified Shard");
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.imbuedShard, 1, 3), "Oxygenated Shard");
 		LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.imbuedShard, 1, 4), "Energized Shard");
 		
 		LanguageRegistry.instance().addName(ItemHandler$1.fireStaff, "Fire Staff");
 		LanguageRegistry.instance().addName(ItemHandler$1.waterStaff, "Water Staff");
 		LanguageRegistry.instance().addName(ItemHandler$1.earthStaff, "Earth Staff");
 		LanguageRegistry.instance().addName(ItemHandler$1.airStaff, "Air Staff");
 		LanguageRegistry.instance().addName(ItemHandler$1.energyStaff, "Energy Staff");
 		
 		LanguageRegistry.instance().addName(ItemHandler$1.verditeHelmet, "Verdite Helmet");
 		LanguageRegistry.instance().addName(ItemHandler$1.verditeChestplate, "Verdite Chestplate");
 		LanguageRegistry.instance().addName(ItemHandler$1.verditeLeggings, "Verdite Leggings");
 		LanguageRegistry.instance().addName(ItemHandler$1.verditeBoots, "Verdite Boots");
 		
 		LanguageRegistry.instance().addName(ItemHandler$1.mithrilHelmet, "Mithril Helmet");
 		LanguageRegistry.instance().addName(ItemHandler$1.mithrilChestplate, "Mithril Chestplate");
 		LanguageRegistry.instance().addName(ItemHandler$1.mithrilLeggings, "Mithril Leggings");
 		LanguageRegistry.instance().addName(ItemHandler$1.mithrilBoots, "Mithril Boots");
 		
 		LanguageRegistry.instance().addName(ItemHandler$1.adamantineHelmet, "Adamantine Helmet");
 		LanguageRegistry.instance().addName(ItemHandler$1.adamantineChestplate, "Adamantine Chestplate");
 		LanguageRegistry.instance().addName(ItemHandler$1.adamantineLeggings, "Adamantine Leggings");
 		LanguageRegistry.instance().addName(ItemHandler$1.adamantineBoots, "Adamantine Boots");
 		
 		LanguageRegistry.instance().addName(ItemHandler$1.iridiumHelmet, "Iridium Helmet");
 		LanguageRegistry.instance().addName(ItemHandler$1.iridiumChestplate, "Iridium Chestplate");
 		LanguageRegistry.instance().addName(ItemHandler$1.iridiumLeggings, "Iridium Leggings");
 		LanguageRegistry.instance().addName(ItemHandler$1.iridiumBoots, "Iridium Boots");
 		
 		LanguageRegistry.instance().addName(ItemHandler$1.fireOrb, "Fire Orb");
 		LanguageRegistry.instance().addName(ItemHandler$1.waterOrb, "Water Orb");
 		LanguageRegistry.instance().addName(ItemHandler$1.earthOrb, "Earth Orb");
 		LanguageRegistry.instance().addName(ItemHandler$1.airOrb, "Air Orb");
		LanguageRegistry.instance().addName(ItemHandler$1.earthOrb, "Energy Orb");
 		
 		for(int i=0;i<12;i++)
 		{
 			LanguageRegistry.instance().addName(new ItemStack(ItemHandler$1.bushFruit, 1, i), "Strange Fruit");
 		}
 	}
 }
