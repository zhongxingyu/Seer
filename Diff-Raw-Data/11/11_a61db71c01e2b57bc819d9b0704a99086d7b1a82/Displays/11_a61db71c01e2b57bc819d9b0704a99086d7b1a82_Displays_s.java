 package net.stormdev.ucars.utils;
 
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import net.stormdev.ucars.trade.main;
 
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.useful.ucarsCommon.StatValue;
 
 public class Displays implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private static HashMap<ItemStack, DisplayType> displays = new HashMap<ItemStack, DisplayType>();
 	public static DisplayType Entity_Pig = 
 			new DisplayType("Pig", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.PIG);
 					return;
 					}}, false, 2, Material.PORK, Material.PORK, 5, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A pig that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 5 porkchops")));
 	public static DisplayType Entity_Sheep = 
 			new DisplayType("Sheep", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.SHEEP);
 					return;
 					}}, false, 2, Material.WOOL, Material.WOOL, 5, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A sheep that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 5 wool")));
 	public static DisplayType Entity_Chicken = 
 			new DisplayType("Chicken", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.CHICKEN);
 					return;
 					}}, false, 2, Material.EGG, Material.EGG, 5, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A chicken that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 5 eggs")));
 	public static DisplayType Entity_Blaze = 
 			new DisplayType("Blaze", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.BLAZE);
 					return;
 					}}, true, 10, Material.BLAZE_ROD, Material.BLAZE_ROD, 10, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A blaze that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 10 blaze rods",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Bat = 
 			new DisplayType("Bat", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.BAT);
 					return;
 					}}, false, 4, Material.COAL_BLOCK, Material.COAL_BLOCK, 2, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A bat that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 2 coal blocks")));
 	public static DisplayType Entity_Cow = 
 			new DisplayType("Cow", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.COW);
 					return;
 					}}, true, 2, Material.COOKED_BEEF, Material.COOKED_BEEF, 5, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A cow that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 5 cooked beef",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Spider = 
 			new DisplayType("Spider", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.SPIDER);
 					return;
 					}}, true, 4, Material.STRING, Material.STRING, 10, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A spider that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 10 string",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Zombie = 
			new DisplayType("Cow", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.ZOMBIE);
 					return;
					}}, false, 4, Material.COOKED_BEEF, Material.COOKED_BEEF, 5, new ArrayList<String>(
 							Arrays.asList(
									main.colors.getInfo()+"A cow that rides", 
 									main.colors.getInfo()+"your car with you", 
									main.colors.getInfo()+"-Make with 5 cooked beef")));
 	public static DisplayType Entity_Horse = 
 			new DisplayType("Horse", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					Horse h = (Horse) DisplayType.spawnEntityAtCar(car, EntityType.HORSE);
 					h.setAdult();
 					h.setOwner(player);
 					DisplayType.putEntityInCar(car, h);
 					return;
 					}}, true, 10, Material.SADDLE, Material.SADDLE, 2, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A horse that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 2 saddles",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_IronGolem = 
 			new DisplayType("Iron Golem", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.IRON_GOLEM);
 					return;
 					}}, true, 20, Material.IRON_LEGGINGS, Material.IRON_LEGGINGS, 2, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"An Iron Golem that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 2 iron leggings",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_MagmaCube = 
 			new DisplayType("Magma Cube", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.MAGMA_CUBE);
 					return;
 					}}, true, 4, Material.MAGMA_CREAM, Material.MAGMA_CREAM, 2, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A Magma Cube that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 2 magma creams",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Slime = 
 			new DisplayType("Slime", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.SLIME);
 					return;
 					}}, true, 4, Material.SLIME_BALL, Material.SLIME_BALL, 20, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A Slime that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 20 slimeballs",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Skeleton = 
 			new DisplayType("Skeleton", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.SKELETON);
 					return;
 					}}, false, 4, Material.ARROW, Material.ARROW, 20, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A Skeleton that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 20 arrows")));
 	public static DisplayType Entity_Squid = 
 			new DisplayType("Squid", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.SQUID);
 					return;
 					}}, true, 4, Material.INK_SACK, Material.INK_SACK, 20, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A Squid that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 20 inc sacs",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Snowman = 
 			new DisplayType("Snowman", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.SNOWMAN);
 					return;
 					}}, true, 4, Material.SNOW_BALL, Material.SNOW_BALL, 64, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A Snowman that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 64 snow balls",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Villager = 
 			new DisplayType("Villager", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.VILLAGER);
 					return;
 					}}, true, 5, Material.EMERALD, Material.EMERALD, 15, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A Villager that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 15 emeralds",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Witch = 
 			new DisplayType("Witch", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.WITCH);
 					return;
 					}}, true, 6, Material.GLASS_BOTTLE, Material.GLASS_BOTTLE, 15, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A Witch that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 15 glass bottles",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Wolf = 
 			new DisplayType("Wolf", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.WOLF);
 					return;
 					}}, true, 4, Material.BONE, Material.BONE, 8, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"A Wolf that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 8 bones",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Entity_Ocelot = 
 			new DisplayType("Ocelot", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.OCELOT);
 					return;
 					}}, true, 4, Material.RAW_FISH, Material.RAW_FISH, 8, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"An ocelot that rides", 
 									main.colors.getInfo()+"your car with you", 
 									main.colors.getInfo()+"-Make with 8 raw fish",
 									main.colors.getError()+"-Reduced ascending")));
 	public static DisplayType Upgrade_Floatation = 
 			new DisplayType("Floatation Upgrade", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.BOAT);
 					return;
 					}}, false, 20, Material.BOAT, Material.BOAT, 1, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"Makes your car float", 
 									main.colors.getInfo()+"on liquid.", 
 									main.colors.getInfo()+"-Make with a boat")));
 	public static DisplayType Upgrade_Hover = 
 			new DisplayType("Hover Upgrade", new CarFiller(){
 				private static final long serialVersionUID = 1L;
 				public void putInCar(Minecart car, Player player) {
 					DisplayType.putEntityInCar(car, EntityType.BAT);
 					car.setMetadata("trade.hover", new StatValue(true, main.plugin)); //A hover car
 					//car.setMetadata("car.braking", new StatValue(true, main.plugin)); //Landed ATM
 					return;
 					}}, false, 30, Material.FEATHER, Material.FEATHER, 64, new ArrayList<String>(
 							Arrays.asList(
 									main.colors.getInfo()+"Makes your car hover", 
 									main.colors.getInfo()+"-Make with 64 feathers")));
 	public static DisplayType canAdd(ItemStack items){
 		if(displays.size() < 1){
 			listDisplays();
 		}
 		//Compare to displayType stuff
 		for(ItemStack i:displays.keySet()){
 			if(i.getType() == items.getType()
 					&& i.getDurability() == items.getDurability()
 					&& i.getAmount() == items.getAmount()){
 				return displays.get(i);
 			}
 		}
 		return null;
 	}
 	public static void listDisplays(){
 		//For each display type add the item stack
 		Field[] fields = Displays.class.getDeclaredFields();
 		for(Field f:fields){
 			f.setAccessible(true);
 			if(f.getType().equals(DisplayType.class)){
 				DisplayType t = Displays.Entity_Pig;
 				try {
 					t = (DisplayType) f.get(t);
 				} catch (Exception e) {
 					// Error occured
 					e.printStackTrace();
 				}
 				ItemStack items = t.getUpgradeItemStack();
 				displays.put(items, t);
 			}
 		}
 	}
 }
