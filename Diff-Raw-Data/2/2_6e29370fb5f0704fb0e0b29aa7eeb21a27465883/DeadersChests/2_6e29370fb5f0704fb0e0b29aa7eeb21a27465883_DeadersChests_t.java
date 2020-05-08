 package deaderschests;
 /*
  * Released under Creative Commons Attribution, Non-commercial, Share alike license.  No permission is required to include in mod packs.
  */
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.Collections;
 import java.util.Arrays;
 
 import net.minecraft.command.CommandHandler;
 import net.minecraft.command.ICommandManager;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.inventory.InventoryLargeChest;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.nbt.CompressedStreamTools;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.WorldServer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.ServerChatEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarted;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartedEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.common.network.NetworkMod;
 
 @Mod(modid = "DeadersChests", name="DeadersChests", version = "1.0")
 
 public class DeadersChests {
 	
 	public static Material[] replaceableBlocks = new Material[]{Material.air,Material.water,Material.web,Material.fire,Material.lava, Material.leaves,Material.vine};
 	
 	@Instance("DeadersChests")
 	public static DeadersChests instance;
 	
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		MinecraftForge.EVENT_BUS.register(instance);
 	}
 	
 	@ForgeSubscribe
 	public void onDeath(LivingDeathEvent event)
 	{
 		if(event.entityLiving instanceof EntityPlayer && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(((EntityPlayer)event.entityLiving).username) != null)
 		{
 			EntityPlayer player = (EntityPlayer)event.entityLiving;
 			InventoryPlayer playerinv = player.inventory;
 			WorldServer world = (WorldServer)DimensionManager.getWorld(player.dimension);
 			int invsize = playerinv.mainInventory.length - Collections.frequency(Arrays.asList(playerinv.mainInventory), null);
 			invsize += playerinv.armorInventory.length - Collections.frequency(Arrays.asList(playerinv.armorInventory), null);
 			IInventory deaderschest = null;
 			TileEntityChest inv1 = null;
 			TileEntityChest inv2 = null;
 			
 			if (!(player.posY < 1.0D || player.posY > world.getHeight())){
 				inv1 = placeChest(playerinv,world, (int)player.posX, (int)player.posY, (int)player.posZ);
 
 				if (inv1 != null && invsize >= 27) {
 					int[] secondCoords = findOpenAdj(world,(int)player.posX, (int)player.posY, (int)player.posZ);
 					if (secondCoords != null) {
 						inv2 = placeChest(playerinv,world,secondCoords[0],secondCoords[1],secondCoords[2]);
 						if (inv2 == null) {
 							player.addChatMessage("Only one chest available!  The rest will be dropped!");
 						}
 						else {
 							deaderschest = new InventoryLargeChest("Large Chest", inv1, inv2);
 						}
 					}
 					else {
 						player.addChatMessage("No free adjacent space for second chest!");
 					}
 				}
 
 				if (deaderschest == null && inv1 != null){
 					deaderschest = inv1;
 				}
 				
 				if(deaderschest != null) {
 					String message = fillChest(deaderschest, playerinv, invsize);
 					if (message != null) {
 						player.addChatMessage(message);
 					}
 					player.addChatMessage("Chest filled!");
 				}
 				else {
 					//print error about no chests
 					player.addChatMessage("No chests available, inventory dropped!");
 				}
 			}
 			else {
 				player.addChatMessage("Chest would be in the void, items dropped!");
 			}
 		}
 	}
 	
 	public int[] findOpenAdj(WorldServer w,int posx,int posy,int posz) {
 		int[] retval = null;
 		int[] orig = {posx, posy, posz};
 		if (canReplace(w,posx+1,posy,posz) && noAdjChest(posx+1,posy,posz, orig, w)) {
 			retval = new int[]{posx+1,posy,posz};
 		}
 		else if (canReplace(w,posx-1,posy,posz) && noAdjChest(posx-1, posy, posz, orig, w)) {
 			retval = new int[]{posx-1,posy,posz};
 		}
 		else if (canReplace(w,posx,posy,posz+1) && noAdjChest(posx, posy, posz+1,orig,w)) {
 			retval = new int[]{posx,posy,posz+1};
 		}
 		else if (canReplace(w,posx,posy,posz-1) && noAdjChest(posx, posy, posz-1,orig,w)) {
 			retval = new int[]{posx,posy,posz-1};
 		}
 		
 		return retval;
 	}
 	
 	public boolean noAdjChest(int x, int y, int z, int[] orig, WorldServer w) {
 		boolean retval = true;
 		if (w.getBlockId(x-1, y, z) == Block.chest.blockID && x-1 != orig[0])
         {
             retval = false;
         }
 
 		if (w.getBlockId(x+1, y, z) == Block.chest.blockID && x+1 != orig[0])
         {
             retval = false;
         }
 
 		if (w.getBlockId(x, y, z-1) == Block.chest.blockID && z-1 != orig[2])
         {
             retval = false;
         }
 
 		if (w.getBlockId(x, y, z+1) == Block.chest.blockID && z+1 != orig[2])
         {
             retval = false;
         }
 		return retval;
 	}
 	
 	public boolean canReplace(WorldServer w,int posx, int posy, int posz) {
 		boolean retval = false;
 		
 		if (Collections.frequency(Arrays.asList(replaceableBlocks), w.getBlockMaterial(posx, posy, posz)) > 0){
 			retval = true;
 		}
 		return retval;
 	}
 	
 	private TileEntityChest placeChest(InventoryPlayer inv, WorldServer w, int posX, int posY, int posZ) {
 		TileEntityChest retval = null;
		if ((inv.hasItem(Block.chest.blockID) || inv.getCurrentItem().itemID == Block.chest.blockID) && canReplace(w,posX,posY,posZ)) {
 			inv.consumeInventoryItem(Block.chest.blockID);
 			w.setBlock(posX, posY, posZ, Block.chest.blockID);
 			retval = (TileEntityChest)w.getBlockTileEntity(posX, posY, posZ);
 		}
 		return retval;
 	}
 	
 	private String fillChest(IInventory chest, InventoryPlayer inv, int isize) {
 		String retval = null;
 		int invcounter = 0;
 		if (isize > chest.getSizeInventory()) {
 			retval = "Inventory larger than max chest size, some items dropped.";
 		}
 		for(int i = 0;i<inv.mainInventory.length;++i) {
 			if (inv.mainInventory[i] != null && invcounter < chest.getSizeInventory()) {
 				chest.setInventorySlotContents(invcounter, inv.mainInventory[i]);
 				invcounter++;
 				inv.mainInventory[i] = null;
 			}
 		}
 	
 		for(int i = 0;i < inv.armorInventory.length;++i) {
 			if (inv.armorInventory[i] != null && invcounter < chest.getSizeInventory()) {
 				chest.setInventorySlotContents(invcounter, inv.armorInventory[i]);
 				invcounter++;
 				inv.armorInventory[i] = null;
 			}
 		}
 		return retval;
 	}
 }
