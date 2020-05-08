 /**
     Copyright (C) <2013> <coolAlias>
 
     This file is part of coolAlias' Redstone Helper Mod; as such,
     you can redistribute it and/or modify it under the terms of the GNU
     General Public License as published by the Free Software Foundation,
     either version 3 of the License, or (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package coolalias.redstonehelper;
 
 import java.io.File;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import coolalias.redstonehelper.handlers.GuiHandler;
 import coolalias.redstonehelper.handlers.RHPacketHandler;
 import coolalias.redstonehelper.lib.LogHelper;
 import coolalias.redstonehelper.lib.ModInfo;
 import coolalias.redstonehelper.lib.RHKeyBindings;
import coolalias.structuregenmod.handlers.SGTBlockHighlightHandler;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = ModInfo.MOD_ID, name = ModInfo.MOD_NAME, version = ModInfo.VERSION)//, dependencies = "required-after:coolAliasStructureGenMod")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false,
 channels = {ModInfo.CHANNEL}, packetHandler = RHPacketHandler.class)
 
 public class RedstoneHelper
 {
 	@Instance(ModInfo.MOD_ID)
 	public static RedstoneHelper instance = new RedstoneHelper();
 	
 	//@SidedProxy(clientSide = ModInfo.CLIENT_PROXY, serverSide = ModInfo.COMMON_PROXY)
 	//public static CommonProxy proxy;
 	
 	public static final int MOD_ITEM_INDEX_DEFAULT = 8889;
 	
 	private static int modItemIndex, guiIndex = 0;
 	
 	public static final int guiBaseSelectorID = ++guiIndex;
 	
 	public static Item logicHelper;
 	
 	private static int baseBlockID, baseBlockMeta;
 	
 	public static boolean requiresMaterials, highlightIO, isSGTModLoaded;
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		LogHelper.init();
 		
 		Configuration config = new Configuration(new File(event.getModConfigurationDirectory().getAbsolutePath() + "/RedstoneHelper.cfg"));
         config.load();
         
         modItemIndex = config.getItem("modItemIndex", MOD_ITEM_INDEX_DEFAULT).getInt() - 256;
         
         baseBlockID = config.get(Configuration.CATEGORY_BLOCK, "baseBlockID", Block.dirt.blockID).getInt();
         baseBlockMeta = config.get(Configuration.CATEGORY_BLOCK, "baseBlockMeta", 0).getInt();
         
         Property property = config.get(Configuration.CATEGORY_GENERAL, "Materials Required", true);
         property.comment = "If true, consumes all blocks/items used in the circuit generated";
         requiresMaterials = property.getBoolean(true);
         
         property = new Property();
         property = config.get(Configuration.CATEGORY_GENERAL, "Highlight Input/Output", true);
         property.comment = "Marks input / output signal locations with colored wool; if false, base block is used";
         highlightIO = property.getBoolean(true);
         
         isSGTModLoaded = Loader.isModLoaded("structuregen");
         
         if (FMLCommonHandler.instance().getSide().isClient())
         	RHKeyBindings.init(config);
         
         config.save();
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event)
 	{
 		logicHelper = new ItemRedstoneHelper(modItemIndex++).setUnlocalizedName("logicHelper");
 		GameRegistry.addRecipe(new ItemStack(logicHelper), "y", "x", 'y', Block.blockRedstone, 'x', Item.stick);
 		GameRegistry.addShapelessRecipe(new ItemStack(logicHelper), Item.stick, Block.dirt);
 		LanguageRegistry.addName(logicHelper, "Logic Helper");
 		
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 		
 		if (FMLClientHandler.instance().getSide().isClient())
 			MinecraftForge.EVENT_BUS.register(new SGTBlockHighlightHandler());
 	}
 	
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event)
 	{
 	}
 	
 	/**
 	 * Returns id of generic filler block used to build circuits/gates
 	 */
 	public static final int getBaseBlockID() {
 		return baseBlockID;
 	}
 	
 	/**
 	 * Returns metadata value of generic filler block used to build circuits/gates
 	 */
 	public static final int getBaseBlockMeta() {
 		return baseBlockMeta;
 	}
 	
 	/**
 	 * Sets id of generic filler block used to build circuits/gates
 	 */
 	public static final void setBaseBlock(int id) {
 		setBaseBlock(id, 0);
 	}
 	
 	/**
 	 * Sets id and metadata of generic filler block used to build circuits/gates
 	 */
 	public static final void setBaseBlock(int id, int meta) {
 		baseBlockID = id;
 		baseBlockMeta = meta;
 	}
 }
