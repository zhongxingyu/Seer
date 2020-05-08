 /* This file is part of VeinMiner Mod Support.
  *
  *    VeinMiner is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as
  *    published by the Free Software Foundation, either version 3 of
  *     the License, or (at your option) any later version.
  *
  *    VeinMiner is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public
  *    License along with VeinMiner.
  *    If not, see <http://www.gnu.org/licenses/>.
  */
 
 package portablejim.veinminermodsupport;
 
 import bluedart.api.IBreakable;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.ModContainer;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import portablejim.veinminer.api.VeinminerPostUseTool;
 import portablejim.veinminer.api.VeinminerStartCheck;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Random;
 
 import static cpw.mods.fml.common.Mod.Init;
 import static cpw.mods.fml.common.Mod.Instance;
 import static cpw.mods.fml.common.Mod.PostInit;
 
 /**
  * Main mod class to handle events from Veinminer and cancel events when
  * special mod support is wanted.
  */
 
 @Mod(modid = ModInfo.MOD_ID,
         name = ModInfo.MOD_NAME,
         version = ModInfo.VERSION)
 public class VeinMinerModSupport {
 
     private boolean debugMode = false;
 
     @Instance(ModInfo.MOD_ID)
     public static VeinMinerModSupport instance;
 
     public boolean forceConsumerAvailable;
 
     @Init
     public void init(@SuppressWarnings("UnusedParameters") FMLInitializationEvent event) {
         MinecraftForge.EVENT_BUS.register(this);
 
         ModContainer thisMod = Loader.instance().getIndexedModList().get(ModInfo.MOD_ID);
         if(thisMod != null) {
             String fileName = thisMod.getSource().getName();
             if(fileName.contains("-dev") || !fileName.contains(".jar")) {
                 debugMode = true;
                 devLog("DEV VERSION");
             }
         }
         forceConsumerAvailable = false;
     }
 
     @SuppressWarnings("UnusedDeclaration")
     @PostInit
     public void postInit(FMLPostInitializationEvent event) {
         if(Loader.isModLoaded("DartCraft")) {
             devLog("Testing for dartcraft classes and functions.");
             try {
                 Object obj = Class.forName("bluedart.api.IForceConsumer").getMethod("attemptRepair", ItemStack.class);
 
                 // Class present.
                 forceConsumerAvailable = true;
             } catch (ClassNotFoundException e) {
                 devLog("Failed to find Dartcraft force consumer. Disabling repair support");
             } catch (NoSuchMethodException e) {
                 devLog("Failed to find Dartcraft force consumer function. Disabling repair support");
             }
         }
     }
 
     private void devLog(String string) {
         if(debugMode) {
             FMLLog.getLogger().info("[" + ModInfo.MOD_ID + "] " + string);
         }
     }
 
     @ForgeSubscribe
     public void makeToolsWork(VeinminerStartCheck event) {
         Item currentEquippedItem = event.player.getCurrentEquippedItem().getItem();
         if(Loader.isModLoaded("DartCraft")) {
             devLog("Dartcraft detected");
             if(currentEquippedItem instanceof IBreakable) {
                 devLog("Allowed start");
                 event.allowVeinminerStart = true;
             }
         }
         if(Loader.isModLoaded("TConstruct")) {
             devLog("Tinkers Construct detected");
             tinkersConstructToolEvent(event);
         }
     }
 
     private void tinkersConstructToolEvent(VeinminerStartCheck event) {
         ItemStack currentItem = event.player.getCurrentEquippedItem();
 
         if(currentItem == null) {
             devLog("ERROR: Item is null");
             return;
         }
 
         if(!currentItem.hasTagCompound()) {
             devLog("ERROR: No NBT data");
             return;
         }
         NBTTagCompound toolTags = currentItem.getTagCompound().getCompoundTag("InfiTool");
         if(toolTags == null) {
             devLog("ERROR: Not Dartcraft Tool");
             return;
         }
 
         boolean hasLava = toolTags.getBoolean("Lava");
         if(!hasLava) {
             devLog("ERROR: Not lava tool");
             return;
         }
 
         Random r = event.player.worldObj.rand;
         Block block = Block.blocksList[event.blockId];
         if(block == null || event.blockId < 1 || event.blockId > 4095) {
             devLog("ERROR: Block id out of range");
             return;
         }
 
         ItemStack smeltStack = new ItemStack(
                 block.idDropped(event.blockMetadata, r, 0),
                 block.quantityDropped(event.blockMetadata, 0, r),
                 block.damageDropped(event.blockMetadata));
         ItemStack smeltResult = FurnaceRecipes.smelting().getSmeltingResult(smeltStack);
         if(smeltResult == null) {
             devLog("ERROR: No Smelt result");
             return;
         }
 
         devLog("Allowing event");
         event.allowVeinminerStart = true;
     }
 
     @ForgeSubscribe
     public void applyForce(VeinminerPostUseTool event) {
         ItemStack currentEquippedItemStack = event.player.getCurrentEquippedItem();
 
         // Pre-compute if avaliable to short circuit logic if not found.
         // Method called lots (many times a second, possibly thousand times).
         // Reflection is slow, and you'll probably feel it.
         if(forceConsumerAvailable && Loader.isModLoaded("DartCraft")) {
             devLog("Reflecting on Dartcraft run repair method.");
             try {
                 Class IForceConsumer = Class.forName("bluedart.api.IForceConsumer");
                 if(IForceConsumer.isInstance(currentEquippedItemStack.getItem())) {
                     //noinspection unchecked
                     Method attemptRepair = IForceConsumer.getMethod("attemptRepair", ItemStack.class);
                    attemptRepair.invoke(currentEquippedItemStack.getItem(), currentEquippedItemStack);
                     devLog("Repairing dartcraft force consumer");
                 }
             } catch (ClassNotFoundException e) {
                 devLog("Strange, I thought we already found the Dartcraft class.");
                 forceConsumerAvailable = false;
             } catch (NoSuchMethodException e) {
                 devLog("Strange, I thought we already found the Dartcraft class and correct method.");
                 forceConsumerAvailable = false;
             } catch (InvocationTargetException e) {
                 devLog("Trying to repair Dartcraft tools didn't work. It threw a InvocationTargetException.");
                 forceConsumerAvailable = false;
             } catch (IllegalAccessException e) {
                 devLog("Trying to repair Dartcraft tools didn't work. It threw a IllegalAccessException.");
                 forceConsumerAvailable = false;
             }
         }
     }
 }
