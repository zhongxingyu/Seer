 package assets.tacotek.common;
 
 import net.minecraftforge.common.Configuration;
 
 public class IDsHelper {
 
 	//Config File Categories
 	public static String blockids = "Block IDs";
 	public static String itemids = "Item IDs";
 	public static String liquidids = "Liquid IDs";
 	
 	//Blocks
 	public static int tacoBoxID;
 	
 	//Items
 	public static int cheeseID;
 	public static int doughID;
 	public static int exchangeOMaticID;
 	public static int flourID;
 	public static int saltID;
 	public static int tacoID;
 	public static int toasterID;
 	public static int tortillaID;
 	public static int uncookedTortillaID;
 	public static int toastID;
 	
 	//Armor (Items)
 	public static int tux_headID;
 	public static int tux_chestID;
 	public static int tux_legsID;
 	public static int tux_bootsID;
 	
 	
 	//liquids
 	//public static int liquidConcreteStillID;
 	
 	protected static void runConfiguration(Configuration config) {
 		//Block IDs
 		tacoBoxID = config.get(blockids, "Taco Box ID", 701).getInt();
 		
 		//Item IDs
 		cheeseID = config.get(itemids, "Cheese ID", 9001).getInt();
 		doughID = config.get(itemids, "Dough ID", 9002).getInt();
		exchangeOMaticID = config.get(itemids, "Dough ID", 9003).getInt();
 		flourID = config.get(itemids, "Flour ID", 9004).getInt();
 		saltID = config.get(itemids, "Salt ID", 9005).getInt();
 		tacoID = config.get(itemids, "Taco ID", 9006).getInt();
 		toasterID = config.get(itemids, "Toaster ID", 9007).getInt();
 		tortillaID = config.get(itemids, "Tortilla ID", 9008).getInt();
 		uncookedTortillaID = config.get(itemids, "Uncooked Tortilla ID", 9009).getInt();
 		toastID = config.get(itemids, "Toast ID", 9010).getInt();
 		
 		//Armor IDs
 		tux_headID = config.get(itemids, "Tux_head ID", 9011).getInt();
 		tux_chestID = config.get(itemids, "Tux_chest ID", 9012).getInt();
 		tux_legsID = config.get(itemids, "Tux_legs ID", 9013).getInt();
 		tux_bootsID = config.get(itemids, "Tux_boots ID", 9014).getInt();
 		
 		//Liquid IDs
 		//liquidConcreteStillID = config.get(liquidids, "Liquid Concrete Still ID", 2001).getInt();
 	}
 }
