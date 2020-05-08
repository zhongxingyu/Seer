 /**
  * BuildCraft is open-source. It is distributed under the terms of the
  * BuildCraft Open Source License. It grants rights to read, modify, compile
  * or run the code. It does *NOT* grant the right to redistribute this software
  * or its modifications in any form, binary or source, except if expressively
  * granted by the copyright holder.
  */
 
 package net.enkun.mods.CompactLaser;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 import buildcraft.BuildCraftCore;
 import buildcraft.BuildCraftSilicon;
 import buildcraft.BuildCraftTransport;
 import buildcraft.api.bptblocks.BptBlockInventory;
 import buildcraft.api.bptblocks.BptBlockRotateMeta;
 import buildcraft.api.recipes.AssemblyRecipe;
 import buildcraft.core.DefaultProps;
 import buildcraft.core.Version;
 import buildcraft.core.proxy.CoreProxy;
 import net.enkun.mods.CompactLaser.BlockCompactLaser;
 import net.enkun.mods.CompactLaser.CommonProxy;
 import net.enkun.mods.CompactLaser.TileLaser;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(name = "CompactLaser", version = "0.2", useMetadata = false, modid = "CompactLaser", dependencies = "required-after:BuildCraft|Core;required-after:BuildCraft|Silicon;required-after:BuildCraft|Transport")
 @NetworkMod(channels = { "CompactLaser" }, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
 public class CompactLaser {
 	public static BlockCompactLaser CompactLaserBlock;
 	public int CompactLaserBlockId;
 
 	@Instance("CompactLaser")
 	public static CompactLaser instance;
 
 	@Init
 	public void load(FMLInitializationEvent evt) {
 		CoreProxy.proxy.registerTileEntity(TileLaser.class, "net.enkun.mods.CompactLaser.TileLaser");
 
 		new BptBlockRotateMeta(CompactLaserBlock.blockID, new int[] { 2, 5, 3, 4 }, true);
 
 		CommonProxy.proxy.registerRenderers();
 		
 		CoreProxy.proxy.addCraftingRecipe(new ItemStack(CompactLaserBlock),
 				new Object[] { "LLL", "L L", "LLL", Character.valueOf('L'), BuildCraftSilicon.laserBlock });
 		
 	}
 
 	@PreInit
 	public void initialize(FMLPreInitializationEvent evt) {
 		Configuration cfg = new Configuration(evt.getSuggestedConfigurationFile());
 		cfg.load();
		Property PropCompactLaserBlock = cfg.get("", "CompactLaser", 1300);
 		CompactLaserBlockId  = PropCompactLaserBlock.getInt();
 
 		CompactLaserBlock = new BlockCompactLaser(CompactLaserBlockId);
 		CompactLaserBlock.setBlockName("CompactLaser");
 		LanguageRegistry.addName(CompactLaserBlock, "Compact Laser");
 		GameRegistry.registerBlock(CompactLaserBlock, "CompactLaser");
 
 	}
 }
