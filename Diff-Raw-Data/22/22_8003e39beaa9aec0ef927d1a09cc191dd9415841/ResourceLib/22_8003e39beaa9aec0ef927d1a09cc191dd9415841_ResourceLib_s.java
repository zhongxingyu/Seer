 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version. This program is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details. You should have received a copy of the GNU
  * Lesser General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>
  */
 package slimevoid.tmf.core.lib;
 
 import net.minecraft.util.ResourceLocation;
 import cpw.mods.fml.client.FMLClientHandler;
 
 public class ResourceLib {
 
 	private static final String				ASSET_PREFIX			= "/assets/"
 																		+ CoreLib.MOD_RESOURCES
 																		+ "/";
 
 	private static final String				TEXTURE_PREFIX			= CoreLib.MOD_RESOURCES
 																		+ ":";
 	private static final String				PATH_PREFIX				= "textures/";
 
 	// public static final String BLOCK_TEXTURE_PATH = PATH_PREFIX +
 	// "terrain.png";
 
 	// public static final String RESOURCE_SPRITE_PATH = PATH_PREFIX +
 	// "resources/resources.png";
 
 	private static final String				ARMOR_PREFIX			= PATH_PREFIX
 																		+ "armor/";
 
 	public static final String				IRON_MINING_HELMET		= ARMOR_PREFIX
 																		+ "tmfiron.png";
 	public static final String				GOLD_MINING_HELMET		= ARMOR_PREFIX
 																		+ "tmfgold.png";
 	public static final String				DIAMOND_MINING_HELMET	= ARMOR_PREFIX
 																		+ "tmfdiamond.png";
 
 	private static final String				GUI_PREFIX				= PATH_PREFIX
 																		+ "gui/";
 
 	// public static final String ITEM_SPRITE_PATH = GUI_PREFIX + "items.png";
 	public static final String				GUI_TOOLBELT_PATH		= GUI_PREFIX
 																		+ "toolbeltGui.png";
 	public static final ResourceLocation	GUI_TOOLBELT			= new ResourceLocation(CoreLib.MOD_RESOURCES, GUI_TOOLBELT_PATH);
 	public static final String				GUI_GEOEQUIP_PATH		= GUI_PREFIX
 																		+ "geoequip.png";
 	public static final ResourceLocation	GUI_GEOEQUIP			= new ResourceLocation(CoreLib.MOD_RESOURCES, GUI_GEOEQUIP_PATH);
 	public static final String				GUI_GRINDER_PATH		= GUI_PREFIX
 																		+ "grinder.png";
 	public static final ResourceLocation	GUI_GRINDER				= new ResourceLocation(CoreLib.MOD_RESOURCES, GUI_GRINDER_PATH);
 	public static final String				GUI_REFINERY_PATH		= GUI_PREFIX
 																		+ "refinery.png";
 	public static final ResourceLocation	GUI_REFINERY			= new ResourceLocation(CoreLib.MOD_RESOURCES, GUI_REFINERY_PATH);
 	public static final String				GUI_AUTOMIXTABLE_PATH	= GUI_PREFIX
 																		+ "automixtable.png";
 	public static final ResourceLocation	GUI_AUTOMIXTABLE		= new ResourceLocation(CoreLib.MOD_RESOURCES, GUI_AUTOMIXTABLE_PATH);
 
 	private static final String				MACHINE_PREFIX			= ASSET_PREFIX
 																		+ PATH_PREFIX
 																		+ "machines/";
 
 	// public static final String MACHINE_TEXTURE_PATH = MACHINE_PREFIX +
 	// "machines.png";
 	public static final String				RECIPES_REFINERY		= MACHINE_PREFIX
 																		+ "refinery.json";
 	public static final String				RECIPES_GRINDER			= MACHINE_PREFIX
 																		+ "grinder.json";
 
 	private static final String				MODEL_PREFIX			= MACHINE_PREFIX
 																		+ "models/";
 	private static final String				HD_PREFIX				= "hd/";
 
 	private static final String				TRACKER_PREFIX			= GUI_PREFIX
 																		+ "tracker/";
 
 	private static final String				TRACKER_BG_PATH			= TRACKER_PREFIX
 																		+ "trackerBG.png";
 	public static final ResourceLocation	TRACKER_BG				= new ResourceLocation(CoreLib.MOD_RESOURCES, TRACKER_BG_PATH);
 
 	private static final String				TRACKER_CONTACT_PATH	= TRACKER_PREFIX
 																		+ "contact.png";
 	public static final ResourceLocation	TRACKER_CONTACT			= new ResourceLocation(CoreLib.MOD_RESOURCES, TRACKER_CONTACT_PATH);
 
 	private static final String				TRACKER_SWEEP_PATH		= TRACKER_PREFIX
 																		+ "trackerSweep.png";
 	public static final ResourceLocation	TRACKER_SWEEP			= new ResourceLocation(CoreLib.MOD_RESOURCES, TRACKER_SWEEP_PATH);
 
 	private static final String				RESOURCE_PREFIX			= ASSET_PREFIX
 																		+ "resources/";
 
 	public static final String				DUST_LIB				= RESOURCE_PREFIX
 																		+ "mixedDust.js";
 
 	public static final String				RECIPE_PATH				= ASSET_PREFIX
 																		+ "recipes";
 
 	public static final String				MINERAL_AXCIUM			= TEXTURE_PREFIX
 																		+ ItemLib.MINERAL_AXCIUM;
 	public static final String				MINERAL_BISOGEN			= TEXTURE_PREFIX
 																		+ ItemLib.MINERAL_BISOGEN;
 	public static final String				MINERAL_CYDRINE			= TEXTURE_PREFIX
 																		+ ItemLib.MINERAL_CYDRINE;
 
 	public static final String				DUST_AXCIUM				= TEXTURE_PREFIX
 																		+ ItemLib.DUST_AXCIUM;
 	public static final String				DUST_BISOGEN			= TEXTURE_PREFIX
 																		+ ItemLib.DUST_BISOGEN;
 	public static final String				DUST_CYDRINE			= TEXTURE_PREFIX
 																		+ ItemLib.DUST_CYDRINE;
 	public static final String				DUST_MIXED				= TEXTURE_PREFIX
 																		+ ItemLib.DUST_MIXED;
 	public static final String				MOTION_SENSOR			= TEXTURE_PREFIX
 																		+ ItemLib.MOTION_SENSOR;
 	public static final String				MINING_TOOLBELT			= TEXTURE_PREFIX
 																		+ ItemLib.MINING_TOOLBELT;
 
 	public static String getModelPath(boolean hasHDModel) {
 		if (hasHDModel
 			&& FMLClientHandler.instance().getClient().gameSettings.fancyGraphics
 			&& FMLClientHandler.instance().getClient().gameSettings.advancedOpengl) {
 			return MODEL_PREFIX + HD_PREFIX;
 		}
 		return MODEL_PREFIX;
 	}
 
 }
