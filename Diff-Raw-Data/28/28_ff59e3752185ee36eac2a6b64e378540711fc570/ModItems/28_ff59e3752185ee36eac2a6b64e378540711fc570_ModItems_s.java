 package com.isocraft.item;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 
 import com.isocraft.ISOCraft;
 import com.isocraft.lib.ItemIds;
 import com.isocraft.thesis.ThesisSystem;
 
 /**
  * ISOCraft
  * 
  * Managing class for the mod items
  * 
  * @author Turnermator13
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class ModItems {
     
     public static ISOCraftItem PDA;
     public static ISOCraftItem RecoveredNotes;
     public static ISOCraftItem TabIcon;
     public static ISOCraftItem Disk;
 
     public static void init() {
         TabIcon = new ItemTabIcon(ItemIds.C_TAB_ICON);
         RecoveredNotes = new ItemRecoveredNotes(ItemIds.C_RECOVERED_NOTES);
         PDA = new ItemPDA(ItemIds.C_PDA);
         Disk = new ItemDisk(ItemIds.C_DISK);
         
        ThesisSystem.addShapedRecipe(new ItemStack(Disk), ISOCraft.ELECTRIC.getName(), "DISK", true, new Object[] { "xs" , "sx", Character.valueOf('s'), Block.dirt, Character.valueOf('x'), /*"stairWood"*/Block.cobblestone}); //  <-- EXAMPLE SHAPED RECIPE
     }
 }
