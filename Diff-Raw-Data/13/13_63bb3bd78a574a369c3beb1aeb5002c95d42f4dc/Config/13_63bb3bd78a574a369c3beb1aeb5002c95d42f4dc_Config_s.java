 package hunternif.mc.dota2items;
 
 import hunternif.mc.dota2items.block.BlockCycloneContainer;
 import hunternif.mc.dota2items.item.BandOfElvenskin;
 import hunternif.mc.dota2items.item.BeltOfStrength;
 import hunternif.mc.dota2items.item.BladeOfAlacrity;
 import hunternif.mc.dota2items.item.BladesOfAttack;
 import hunternif.mc.dota2items.item.BlinkDagger;
 import hunternif.mc.dota2items.item.BootsOfSpeed;
 import hunternif.mc.dota2items.item.Bracer;
 import hunternif.mc.dota2items.item.Broadsword;
 import hunternif.mc.dota2items.item.Butterfly;
 import hunternif.mc.dota2items.item.Chainmail;
 import hunternif.mc.dota2items.item.Circlet;
 import hunternif.mc.dota2items.item.Claymore;
 import hunternif.mc.dota2items.item.Cloak;
 import hunternif.mc.dota2items.item.DemonEdge;
 import hunternif.mc.dota2items.item.DivineRapier;
 import hunternif.mc.dota2items.item.Dota2Item;
 import hunternif.mc.dota2items.item.Dota2Logo;
 import hunternif.mc.dota2items.item.Eaglesong;
 import hunternif.mc.dota2items.item.EnergyBooster;
 import hunternif.mc.dota2items.item.EulsScepter;
 import hunternif.mc.dota2items.item.GauntletsOfStrength;
 import hunternif.mc.dota2items.item.GlovesOfHaste;
 import hunternif.mc.dota2items.item.GoldCoin;
 import hunternif.mc.dota2items.item.HelmOfIronWill;
 import hunternif.mc.dota2items.item.Hyperstone;
 import hunternif.mc.dota2items.item.IronBranch;
 import hunternif.mc.dota2items.item.ItemRecipe;
 import hunternif.mc.dota2items.item.MantleOfIntelligence;
 import hunternif.mc.dota2items.item.MithrilHammer;
 import hunternif.mc.dota2items.item.MysticStaff;
 import hunternif.mc.dota2items.item.NullTalisman;
 import hunternif.mc.dota2items.item.OblivionStaff;
 import hunternif.mc.dota2items.item.OgreClub;
 import hunternif.mc.dota2items.item.Perseverance;
 import hunternif.mc.dota2items.item.Platemail;
 import hunternif.mc.dota2items.item.PointBooster;
 import hunternif.mc.dota2items.item.Quarterstaff;
 import hunternif.mc.dota2items.item.QuellingBlade;
 import hunternif.mc.dota2items.item.Reaver;
 import hunternif.mc.dota2items.item.RingOfHealth;
 import hunternif.mc.dota2items.item.RingOfProtection;
 import hunternif.mc.dota2items.item.RingOfRegen;
 import hunternif.mc.dota2items.item.RobeOfTheMagi;
 import hunternif.mc.dota2items.item.SacredRelic;
 import hunternif.mc.dota2items.item.SagesMask;
 import hunternif.mc.dota2items.item.SlippersOfAgility;
 import hunternif.mc.dota2items.item.SoulBooster;
 import hunternif.mc.dota2items.item.StaffOfWizardry;
 import hunternif.mc.dota2items.item.StoutShield;
 import hunternif.mc.dota2items.item.TalismanOfEvasion;
 import hunternif.mc.dota2items.item.Tango;
 import hunternif.mc.dota2items.item.UltimateOrb;
 import hunternif.mc.dota2items.item.Vanguard;
 import hunternif.mc.dota2items.item.VitalityBooster;
 import hunternif.mc.dota2items.item.VoidStone;
 import hunternif.mc.dota2items.item.WraithBand;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class Config {
 	private static Map<Class<?>, CfgInfo> map = new HashMap<Class<?>, CfgInfo>();
 	
 	@Retention(RetentionPolicy.RUNTIME)
 	public static @interface Recipe {
 		public Class<?>[] ingredients();
 	}
 	
 	public static void preLoad(Configuration config) {
 		try {
 			config.load();
 			Field[] fields = Config.class.getFields();
 			for (Field field : fields) {
 				if (field.getType().equals(CfgInfo.class)) {
 					CfgInfo info = (CfgInfo)field.get(null);
 					int id = info.id;
 					if (info.isBlock) {
 						id = config.getBlock(field.getName(), id).getInt();
 					} else {
 						id = config.getItem(field.getName(), id).getInt();
 					}
 					info.id = id;
 					map.put(info.clazz, info);
 				}
 			}
 		} catch(Exception e) {
			FMLLog.severe(Dota2Items.ID, Level.SEVERE, "Failed to load config");
 		} finally {
 			config.save();
 		}
 	}
 	
 	public static void load() {
 		try {
 			List<Field> itemsWithRecipes = new ArrayList<Field>();
 			Field[] fields = Config.class.getFields();
 			// Parse fields to instantiate the items:
 			for (Field field : fields) {
 				if (field.getType().equals(CfgInfo.class)) {
 					CfgInfo info = (CfgInfo)field.get(null);
 					info.instance = info.clazz.getConstructor(int.class).newInstance(info.id);
 					if (info.isBlock) {
 						((Block)info.instance).setUnlocalizedName(field.getName());
						GameRegistry.registerBlock((Block)info.instance, field.getName());
 						LanguageRegistry.addName(info.instance, info.name);
 					} else {
 						((Item)info.instance).setUnlocalizedName(field.getName());
 						LanguageRegistry.addName(info.instance, info.name);
						GameRegistry.registerItem((Item)info.instance, field.getName());
 						Dota2Items.itemList.add((Item)info.instance);
 					}
 					if (info.clazz.getAnnotation(Recipe.class) != null) {
 						itemsWithRecipes.add(field);
 					}
 				}
 			}
 			// Parse fields one more time to set their recipes:
 			for (Field field : itemsWithRecipes) {
 				CfgInfo info = (CfgInfo) field.get(null);
 				Recipe annotation = info.clazz.getAnnotation(Recipe.class);
 				List<Dota2Item> recipeForItem = new ArrayList<Dota2Item>();
 				
 				List<ItemStack> recipeForCraft = new ArrayList<ItemStack>();
 				
 				for (Class<?> clazz : annotation.ingredients()) {
 					Dota2Item item = (Dota2Item) forClass(clazz).instance;
 					recipeForItem.add(item);
 					recipeForCraft.add(new ItemStack(item, item.defaultQuantity));
 				}
 				((Dota2Item) info.instance).setRecipe(recipeForItem);
 				
 				if (((Dota2Item)info.instance).isRecipeItemRequired()) {
 					recipeForCraft.add(ItemRecipe.forItem(info.getID(), false));
 				}
 				ItemStack craftResult = new ItemStack((Dota2Item)info.instance, ((Dota2Item)info.instance).defaultQuantity);
 				GameRegistry.addShapelessRecipe(craftResult, recipeForCraft.toArray());
 				
 				FMLLog.log(Dota2Items.ID, Level.INFO, "Added recipe for Dota 2 item " + info.name);
 			}
 		} catch(Exception e) {
			FMLLog.log(Dota2Items.ID, Level.SEVERE, "Failed to instantiate items");
 		}
 	}
 	
 	public static CfgInfo dota2Logo = 			new CfgInfo (Dota2Logo.class, 27000, "Dota 2 Logo");
 	public static CfgInfo blinkDagger = 		new CfgInfo (BlinkDagger.class, 27001, "Blink Dagger");
 	public static CfgInfo tango = 				new CfgInfo (Tango.class, 27002, "Tango");
 	public static CfgInfo quellingBlade = 		new CfgInfo (QuellingBlade.class, 27003, "Quelling Blade");
 	public static CfgInfo eulsScepter = 		new CfgInfo (EulsScepter.class, 27004, "Eul's Scepter of Divinity");
 	public static CfgInfo ringOfProtection = 	new CfgInfo (RingOfProtection.class, 27005, "Ring of Protection");
 	public static CfgInfo bootsOfSpeed = 		new CfgInfo (BootsOfSpeed.class, 27006, "Boots of Speed");
 	public static CfgInfo goldCoin = 			new CfgInfo (GoldCoin.class, 27007, "Gold Coin");
 	public static CfgInfo voidStone = 			new CfgInfo (VoidStone.class, 27008, "Void Stone");
 	public static CfgInfo sagesMask = 			new CfgInfo (SagesMask.class, 27009, "Sage's Mask");
 	public static CfgInfo staffOfWizardry = 	new CfgInfo (StaffOfWizardry.class, 27010, "Staff of Wizardry");
 	public static CfgInfo recipe = 				new CfgInfo (ItemRecipe.class, 27011, "Recipe");
 	public static CfgInfo ironBranch = 			new CfgInfo (IronBranch.class, 27012, "Iron Branch");
 	public static CfgInfo gauntletsOfStrength = new CfgInfo (GauntletsOfStrength.class, 27013, "Gauntlets of Strength");
 	public static CfgInfo slippersOfAgility =	new CfgInfo (SlippersOfAgility.class, 27014, "Slippers of Agility");
 	public static CfgInfo mantleOfIntelligence =new CfgInfo (MantleOfIntelligence.class, 27015, "Mantle of Intelligence");
 	public static CfgInfo circlet = 			new CfgInfo (Circlet.class, 27016, "Circlet");
 	public static CfgInfo beltOfStrength = 		new CfgInfo (BeltOfStrength.class, 27017, "Belt of Strength");
 	public static CfgInfo bandOfElvenskin = 	new CfgInfo (BandOfElvenskin.class, 27018, "Band of Elvenskin");
 	public static CfgInfo robeOfTheMagi = 		new CfgInfo (RobeOfTheMagi.class, 27019, "Robe of the Magi");
 	public static CfgInfo ogreClub = 			new CfgInfo (OgreClub.class, 27020, "Ogre Club");
 	public static CfgInfo bladeOfAlacrity = 	new CfgInfo (BladeOfAlacrity.class, 27021, "Blade of Alacrity");
 	public static CfgInfo ultimateOrb = 		new CfgInfo (UltimateOrb.class, 27022, "Ultimate Orb");
 	public static CfgInfo bladesOfAttack = 		new CfgInfo (BladesOfAttack.class, 27023, "Blades of Attack");
 	public static CfgInfo chainmail = 			new CfgInfo (Chainmail.class, 27024, "Chainmail");
 	public static CfgInfo helmOfIronWill = 		new CfgInfo (HelmOfIronWill.class, 27025, "Helm of Iron Will");
 	public static CfgInfo broadsword = 			new CfgInfo (Broadsword.class, 27026, "Broadsword");
 	public static CfgInfo quarterstaff = 		new CfgInfo (Quarterstaff.class, 27027, "Quarterstaff");
 	public static CfgInfo claymore = 			new CfgInfo (Claymore.class, 27028, "Claymore");
 	public static CfgInfo platemail = 			new CfgInfo (Platemail.class, 27029, "Platemail");
 	public static CfgInfo mithrilHammer = 		new CfgInfo (MithrilHammer.class, 27030, "Mithril Hammer");
 	public static CfgInfo stoutShield = 		new CfgInfo (StoutShield.class, 27031, "Stout Shield");
 	public static CfgInfo ringOfRegen = 		new CfgInfo (RingOfRegen.class, 27032, "Ring of Regen");
 	public static CfgInfo glovesOfHaste = 		new CfgInfo (GlovesOfHaste.class, 27033, "Gloves of Haste");
 	public static CfgInfo cloak = 				new CfgInfo (Cloak.class, 27034, "Cloak");
 	public static CfgInfo talismanOfEvasion = 	new CfgInfo (TalismanOfEvasion.class, 27035, "Talisman of Evasion");
 	public static CfgInfo wraithBand = 			new CfgInfo (WraithBand.class, 27036, "Wraith Band");
 	public static CfgInfo nullTalisman = 		new CfgInfo (NullTalisman.class, 27037, "Null Talisman");
 	public static CfgInfo bracer = 				new CfgInfo (Bracer.class, 27038, "Bracer");
 	public static CfgInfo ringOfHealth = 		new CfgInfo (RingOfHealth.class, 27039, "Ring of Health");
 	public static CfgInfo hyperstone = 			new CfgInfo (Hyperstone.class, 27040, "Hyperstone");
 	public static CfgInfo demonEdge = 			new CfgInfo (DemonEdge.class, 27041, "Demon Edge");
 	public static CfgInfo sacredRelic = 		new CfgInfo (SacredRelic.class, 27042, "Sacred Relic");
 	public static CfgInfo reaver = 				new CfgInfo (Reaver.class, 27043, "Reaver");
 	public static CfgInfo eaglesong = 			new CfgInfo (Eaglesong.class, 27044, "Eaglesong");
 	public static CfgInfo mysticStaff = 		new CfgInfo (MysticStaff.class, 27045, "Mystic Staff");
 	public static CfgInfo vitalityBooster =		new CfgInfo (VitalityBooster.class, 27046, "Vitality Booster");
 	public static CfgInfo energyBooster = 		new CfgInfo (EnergyBooster.class, 27047, "Energy Booster");
 	public static CfgInfo pointBooster = 		new CfgInfo (PointBooster.class, 27048, "Point Booster");
 	public static CfgInfo soulBooster = 		new CfgInfo (SoulBooster.class, 27049, "Soul Booster");
 	public static CfgInfo perseverance = 		new CfgInfo (Perseverance.class, 27050, "Perseverance");
 	public static CfgInfo oblivionStaff = 		new CfgInfo (OblivionStaff.class, 27051, "Oblivion Staff");
 	public static CfgInfo vanguard = 			new CfgInfo (Vanguard.class, 27052, "Vanguard");
 	public static CfgInfo butterfly = 			new CfgInfo (Butterfly.class, 27053, "Butterfly");
 	public static CfgInfo divineRapier = 		new CfgInfo (DivineRapier.class, 27054, "Divine Rapier");
 	
 	public static CfgInfo cycloneContainer = new CfgInfo(BlockCycloneContainer.class, 2700, "Cyclone Container", true);
 	
 	public static class CfgInfo {
 		protected Class<?> clazz;
 		protected int id;
 		public String name;
 		public boolean isBlock;
 		public Object instance;
 		public CfgInfo(Class<?> clazz, int defaultID, String englishName, boolean isBlock) {
 			this.clazz = clazz;
 			this.id = defaultID;
 			this.name = englishName;
 			this.isBlock = isBlock;
 		}
 		public CfgInfo(Class<?> clazz, int defaultID, String englishName) {
 			this(clazz, defaultID, englishName, false);
 		}
 		public int getID() {
 			return isBlock ? ((Block)instance).blockID : ((Item)instance).itemID;
 		}
 	}
 
 	public static CfgInfo forClass(Class<?> clazz) {
 		return map.get(clazz);
 	}
 }
