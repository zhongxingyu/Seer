 package MysticWorld.Items;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import MysticWorld.MysticWorld;
 import MysticWorld.Blocks.BlockHandler;
 import MysticWorld.Lib.ItemIds;
 import MysticWorld.Lib.Strings;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.liquids.LiquidDictionary;
 import net.minecraftforge.liquids.LiquidStack;
 
 public class ItemHandler$1 {
 	
 	public static Item staffParts;
 	
 	public static Item imbuedShard;
 	 
 	public static Item fireStaff;
 	public static Item waterStaff;
 	public static Item earthStaff;
 	public static Item airStaff;
 	public static Item energyStaff;
 	
 	public static Item fireStaffPower;
 	public static Item waterStaffPower;
 	public static Item earthStaffPower;
 	public static Item airStaffPower;
 	public static Item energyStaffPower;
 	
 	public static Item staffPower;
 	
 	public static Item fireOrb;
 	public static Item waterOrb;
 	public static Item earthOrb;
 	public static Item airOrb;
 	public static Item energyOrb;
 
 	public static Item bushFruit;
 	
 	public static Item verditeHelmet;
 	public static Item verditeChestplate;
 	public static Item verditeLeggings;
 	public static Item verditeBoots;
 	
 	public static Item adamantineHelmet;
 	public static Item adamantineChestplate;
 	public static Item adamantineLeggings;
 	public static Item adamantineBoots;
 	
 	public static Item mithrilHelmet;
 	public static Item mithrilChestplate;
 	public static Item mithrilLeggings;
 	public static Item mithrilBoots;
 	
 	public static Item iridiumHelmet;
 	public static Item iridiumChestplate;
 	public static Item iridiumLeggings;
 	public static Item iridiumBoots;
 
 	
 	public static void init(){
 
 		staffParts = new ItemStaffParts(ItemIds.STAFF_PARTS).setUnlocalizedName(Strings.STAFF_NAME);
 		
 		imbuedShard = new ItemImbuedShard(ItemIds.IMBUED_SHARD).setUnlocalizedName(Strings.IMBUED_SHARD_NAME);
 		
 		fireStaff = new ItemStaffFire(ItemIds.FIRE_STAFF).setUnlocalizedName(Strings.FIRE_STAFF_NAME);
 		waterStaff = new ItemStaffWater(ItemIds.WATER_STAFF).setUnlocalizedName(Strings.WATER_STAFF_NAME);
 		earthStaff = new ItemStaffEarth(ItemIds.EARTH_STAFF).setUnlocalizedName(Strings.EARTH_STAFF_NAME);
 		airStaff = new ItemStaffAir(ItemIds.AIR_STAFF).setUnlocalizedName(Strings.AIR_STAFF_NAME);
 		energyStaff = new ItemStaffEnegy(ItemIds.ENERGY_STAFF).setUnlocalizedName(Strings.ENERGY_STAFF_NAME);
 		
 		staffPower = new ItemStaffPower(ItemIds.STAFF_POWER).setUnlocalizedName(Strings.POWER_STAFF_NAME);
 		
 		fireOrb = new ItemOrbFire(ItemIds.FIRE_ORB).setUnlocalizedName(Strings.FIRE_ORB_NAME);
 		waterOrb = new ItemOrbWater(ItemIds.WATER_ORB).setUnlocalizedName(Strings.WATER_ORB_NAME);
 		earthOrb = new ItemOrbEarth(ItemIds.EARTH_ORB).setUnlocalizedName(Strings.EARTH_ORB_NAME);
 		airOrb = new ItemOrbAir(ItemIds.AIR_ORB).setUnlocalizedName(Strings.AIR_ORB_NAME);
		energyOrb = new ItemOrbEnergy(ItemIds.ENERGY_ORB).setUnlocalizedName(Strings.EARTH_ORB_NAME);
 		
 		bushFruit = new ItemBushFruit(ItemIds.BUSH_FRUIT, 2).setUnlocalizedName(Strings.BUSH_FRUIT_NAME);
 		
 		verditeHelmet = new ItemVerditeArmor(ItemIds.VERDITE_HELMET, EnumArmor.Verdite, MysticWorld.proxy.addArmor(EnumArmor.Verdite.name()), 0).setUnlocalizedName(Strings.VERDITE_HELMET_NAME);
 		verditeChestplate = new ItemVerditeArmor(ItemIds.VERDITE_CHESTPLATE, EnumArmor.Verdite, MysticWorld.proxy.addArmor(EnumArmor.Verdite.name()), 1).setUnlocalizedName(Strings.VERDITE_CHESTPLATE_NAME);
 		verditeLeggings = new ItemVerditeArmor(ItemIds.VERDITE_LEGGINGS, EnumArmor.Verdite, MysticWorld.proxy.addArmor(EnumArmor.Verdite.name()), 2).setUnlocalizedName(Strings.VERDITE_LEGGINGS_NAME);
 		verditeBoots = new ItemVerditeArmor(ItemIds.VERDITE_BOOTS, EnumArmor.Verdite, MysticWorld.proxy.addArmor(EnumArmor.Verdite.name()), 3).setUnlocalizedName(Strings.VERDITE_BOOTS_NAME);
 		
 		mithrilHelmet = new ItemMithrilArmor(ItemIds.MITHRIL_HELMET, EnumArmor.Mithril, MysticWorld.proxy.addArmor(EnumArmor.Mithril.name()), 0).setUnlocalizedName(Strings.MITHRIL_HELMET_NAME);
 		mithrilChestplate = new ItemMithrilArmor(ItemIds.MITHRIL_CHESTPLATE, EnumArmor.Mithril, MysticWorld.proxy.addArmor(EnumArmor.Mithril.name()), 1).setUnlocalizedName(Strings.MITHRIL_CHESTPLATE_NAME);
 		mithrilLeggings = new ItemMithrilArmor(ItemIds.MITHRIL_LEGGINGS, EnumArmor.Mithril, MysticWorld.proxy.addArmor(EnumArmor.Mithril.name()), 2).setUnlocalizedName(Strings.MITHRIL_LEGGINGS_NAME);
 		mithrilBoots = new ItemMithrilArmor(ItemIds.MITHRIL_BOOTS, EnumArmor.Mithril, MysticWorld.proxy.addArmor(EnumArmor.Mithril.name()), 3).setUnlocalizedName(Strings.MITHRIL_BOOTS_NAME);
 		
 		adamantineHelmet = new ItemAdamantineArmor(ItemIds.ADAMANTINE_HELMET, EnumArmor.Adamantine, MysticWorld.proxy.addArmor(EnumArmor.Adamantine.name()), 0).setUnlocalizedName(Strings.ADAMANTINE_HELMET_NAME);
 		adamantineChestplate = new ItemAdamantineArmor(ItemIds.ADAMANTINE_CHESTPLATE, EnumArmor.Adamantine, MysticWorld.proxy.addArmor(EnumArmor.Adamantine.name()), 1).setUnlocalizedName(Strings.ADAMANTINE_CHESTPLATE_NAME);
 		adamantineLeggings = new ItemAdamantineArmor(ItemIds.ADAMANTINE_LEGGINGS, EnumArmor.Adamantine, MysticWorld.proxy.addArmor(EnumArmor.Adamantine.name()), 2).setUnlocalizedName(Strings.ADAMANTINE_LEGGINGS_NAME);
 		adamantineBoots = new ItemAdamantineArmor(ItemIds.ADAMANTINE_BOOTS, EnumArmor.Adamantine, MysticWorld.proxy.addArmor(EnumArmor.Adamantine.name()), 3).setUnlocalizedName(Strings.ADAMANTINE_BOOTS_NAME);
 		
 		iridiumHelmet = new ItemIridiumArmor(ItemIds.IRIDIUM_HELMET, EnumArmor.Iridium, MysticWorld.proxy.addArmor(EnumArmor.Iridium.name()), 0).setUnlocalizedName(Strings.IRIDIUM_HELMET_NAME);
 		iridiumChestplate = new ItemIridiumArmor(ItemIds.IRIDIUM_CHESTPLATE, EnumArmor.Iridium, MysticWorld.proxy.addArmor(EnumArmor.Iridium.name()), 1).setUnlocalizedName(Strings.IRIDIUM_CHESTPLATE_NAME);
 		iridiumLeggings = new ItemIridiumArmor(ItemIds.IRIDIUM_LEGGINGS, EnumArmor.Iridium, MysticWorld.proxy.addArmor(EnumArmor.Iridium.name()), 2).setUnlocalizedName(Strings.IRIDIUM_LEGGINGS_NAME);
 		iridiumBoots = new ItemIridiumArmor(ItemIds.IRIDIUM_BOOTS, EnumArmor.Iridium, MysticWorld.proxy.addArmor(EnumArmor.Iridium.name()), 3).setUnlocalizedName(Strings.IRIDIUM_BOOTS_NAME);
 	}
 }
