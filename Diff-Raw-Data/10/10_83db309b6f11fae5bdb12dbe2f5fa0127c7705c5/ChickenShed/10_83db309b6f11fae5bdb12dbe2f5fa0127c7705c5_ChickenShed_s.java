 package vazkii.chickenshed;
 
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.passive.EntityChicken;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDropsEvent;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import vazkii.chickenshed.handler.ConfigurationHandler;
 
 @Mod(modid = "ChickenShed", name = "Chicken Shed", version="1.1.2")
 public class ChickenShed {
 	
	@PreInit
 	public void onPreInit(FMLPreInitializationEvent event) {
 		// Init the config, passing in the configuration file FML suggests for this mod
 		ConfigurationHandler.initConfig(event.getSuggestedConfigurationFile());
 		}
 	
 	@Instance("ChickenShed")
 	public static ChickenShed instance; // Instance of the mod, only one of this mod exists
 	
 	
	@Init
 	public void init(FMLInitializationEvent event) {
 		if(!ConfigurationHandler.masterDisable) // Check if the entire mod is enabled, if so register the forge hooks
 			MinecraftForge.EVENT_BUS.register(instance);
 	}
 	
 	
 	@ForgeSubscribe
 	@SuppressWarnings("unused")
 	public void onLivingUpdate(LivingUpdateEvent event) {
 		if(event.entity.worldObj.isRemote) 
 			return; // Break the method if the world is a client
 		
 		if(event.entity instanceof EntityChicken) {
 			EntityChicken chicken = (EntityChicken)event.entity;
 
 			if(chicken.isChild() && !ConfigurationHandler.chicksDropFeathers)
 				return; // If the chicken is a baby chicken and that feature is disable, breaks the method
 			
 			if(!ConfigurationHandler.timedWithEgg) { // If the drops are supposed to be when it's not timed with the egg
 				if(chicken.worldObj.rand.nextInt((6000 + ConfigurationHandler.configurableTimeForEachFeather)) == 0) // Pseudo-random method of timing the feather drops
 					chicken.dropItem(Item.feather.itemID, 1);
 			} else { // Otherwise...
 				if(chicken.timeUntilNextEgg == 0 && chicken.worldObj.rand.nextInt(100) < 100)
 					chicken.dropItem(Item.feather.itemID, 1);
 			}
 		}
 	}
 	
 	
 	@ForgeSubscribe
 	@SuppressWarnings("unused")
 	public void onLivingDrops(LivingDropsEvent event) {
 		if(event.entity.worldObj.isRemote)
 			return; // Break the method if the world is a client
 		
 		if(event.entity instanceof EntityChicken && ConfigurationHandler.modifyDeathDrops) {
 			EntityChicken chicken = (EntityChicken)event.entity;
 			
 			if(chicken.isChild() && !ConfigurationHandler.chicksDropFeathers)
 				return; // If the chicken is a baby chicken and that feature is disable, breaks the method
 			
 			boolean setFeather = false; // Do the drops have a feather?
 			for(EntityItem item : event.drops) {
 				if(item != null) {
 					// The watchable object 10 is the itemstack of the item entity
 					ItemStack originalStack = item.getDataWatcher().getWatchableObjectItemStack(10);
 					ItemStack stack = originalStack.copy();
 					if(stack != null && stack.itemID == Item.feather.itemID) {
 						stack.stackSize = MathHelper.getRandomIntegerInRange(item.worldObj.rand, 1, 1);
 						item.getDataWatcher().updateObject(10, stack); // Update the object with the new stack
 						setFeather = true; // A feather was found
 					}
 				}
 			}
 			
 			if(!setFeather && 1 > 0) { // If a feather wasn't found, it adds one, if the minimum isn't 0 already that is
 				EntityItem item = new EntityItem(chicken.worldObj, chicken.posX, chicken.posY, chicken.posZ);
 				int stackSize = MathHelper.getRandomIntegerInRange(item.worldObj.rand, 1, 1);
 				ItemStack stack = new ItemStack(Item.feather.itemID, stackSize, 0);
 				item.getDataWatcher().updateObject(10, stack);
 				event.drops.add(item);
 			}
 		}
 	}
 }
