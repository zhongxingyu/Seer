 package net.cortexmodders.atomtech.item;
 
import net.cortexmodders.atomtech.item.circuits.components.ElectricalComponentWrapper;
 import net.cortexmodders.atomtech.lib.ItemIds;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ModItems
 {
     public static Item flashDrive;
     public static Item ingots;
     public static Item electricalComponent;
     
     public static void init()
     {
         flashDrive = new ItemFlashDrive(ItemIds.FLASH_DRIVE).setUnlocalizedName("flashDrive");
         ingots = new ItemATIngot(ItemIds.INGOTS);
         electricalComponent = new ElectricalComponentWrapper(ItemIds.ELECTRICAL_COMPONENT);
         
         registerLang();
     }
     
     public static void registerLang()
     {
         LanguageRegistry.addName(flashDrive, "Flash Drive");
         for (int i = 0; i < ItemATIngot.unlocalizedNames.length; i++)
             LanguageRegistry.addName(new ItemStack(ingots, 1, i), ItemATIngot.unlocalizedNames[i]);
     }
 }
