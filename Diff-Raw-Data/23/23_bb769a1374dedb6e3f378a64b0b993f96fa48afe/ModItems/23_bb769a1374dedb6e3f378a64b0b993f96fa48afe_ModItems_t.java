 package com.isocraft.item;
 
import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 import com.isocraft.lib.ItemInfo;
 import com.isocraft.thesis.ThesisSystem;
 import com.isocraft.thesis.data.ThesisDataStrings;
 
 /**
  * ISOCraft
  * 
  * Managing class for the mod items
  * 
  * @author Turnermator13
  */
 
 public class ModItems {
 
 	public static ISOCraftItem PDA;
 	public static ISOCraftItem RecoveredNotes;
 	public static ISOCraftItem TabIcon;
 	public static ISOCraftItem Disk;
 
 	public static void init() {
 		TabIcon = new ItemTabIcon(ItemInfo.TabIcon_id);
 		RecoveredNotes = new ItemRecoveredNotes(ItemInfo.RecoveredNotes_id);
 		PDA = new ItemPDA(ItemInfo.PDA_id);
 		Disk = new ItemDisk(ItemInfo.Disk_id);
 
		ThesisSystem.addShapedRecipe(new ItemStack(Disk), ThesisDataStrings.Thesis_intro_name, "DISK", true, new Object[] { "xax", "xbx", "xcx", Character.valueOf('x'), "plankWood", Character.valueOf('a'), Item.ingotIron, Character.valueOf('b'), Item.redstone, Character.valueOf('c'), Item.paper});
 		ThesisSystem.xmlCraftingReference.get("DISK").setTitle("Disk Crafting").setText("The crafting recipe for the Disk Item");
 	}
 }
