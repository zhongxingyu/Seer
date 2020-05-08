 /*******************************************************************************
  * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
  * Licensed as open source with restrictions. Please see attached LICENSE.txt.
  ******************************************************************************/
 
 package com.kaijin.AdvInvMan;
 
 import net.minecraft.block.Block;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class Info
 {
 	public static final String VERSION = "@VERSION@";
 	public static final String BUILD_NUMBER = "@BUILD_NUMBER@";
 
 	public static final String GUI_PNG = "/com/kaijin/AdvancedInventoryManagement/textures/guiInventoryStocker.png";
 
 	public static final String MOD_ID = "AdvancedInventoryManagement";
 	public static final String MOD_NAME = "Advanced Inventory Management";
	public static final String MOD_DEPENDENCIES = "required-after:Forge@[7.8.0.696,)";
 	public static final String PROXY_CLIENT = "com.kaijin.AdvInvMan.ClientProxy";
 	public static final String PROXY_SERVER = "com.kaijin.AdvInvMan.CommonProxy";
 	public static final String PACKET_CHANNEL = "kaijinAdvInvMan";
 
 	public static final String KEY_GUI_INPUT = "kaijin.invStocker.guiStrings.input";
 	public static final String KEY_GUI_OUTPUT = "kaijin.invStocker.guiStrings.output";
 	public static final String KEY_GUI_READY = "kaijin.invStocker.guiStrings.ready";
 	public static final String KEY_GUI_NOSCAN = "kaijin.invStocker.guiStrings.noScan";
 	public static final String KEY_GUI_INVALID = "kaijin.invStocker.guiStrings.invalid";
 	public static final String KEY_GUI_WORKING = "kaijin.invStocker.guiStrings.working";
 	public static final String KEY_GUI_HALTED = "kaijin.invStocker.guiStrings.halted";
 	public static final String KEY_GUI_REMOVE = "kaijin.invStocker.guiStrings.remove";
 	public static final String KEY_GUI_INSERT = "kaijin.invStocker.guiStrings.insert";
 	public static final String KEY_GUI_REPLACE = "kaijin.invStocker.guiStrings.replace";
 	public static final String KEY_GUI_NORMAL = "kaijin.invStocker.guiStrings.normal";
 	public static final String KEY_GUI_CLEAR = "kaijin.invStocker.guiButton.clear";
 	public static final String KEY_GUI_SCAN = "kaijin.invStocker.guiButton.scan";
 	public static final String KEY_GUI_MODE = "kaijin.invStocker.guiButton.mode";
 
 	// Global variables
 	public static boolean isDebugging;
 
 	public static Block blockInventoryStocker;
 
 	public static int blockIDInventoryStocker;
 
 	public static void registerStrings()
 	{
 		final LanguageRegistry lang = LanguageRegistry.instance();
 		lang.addName(Info.blockInventoryStocker, "Inventory Stocker");
 		lang.addStringLocalization(KEY_GUI_INPUT, "Input");
 		lang.addStringLocalization(KEY_GUI_OUTPUT, "Output");
 		lang.addStringLocalization(KEY_GUI_READY, "Ready");
 		lang.addStringLocalization(KEY_GUI_NOSCAN, "No Scan");
 		lang.addStringLocalization(KEY_GUI_INVALID, "Invalid");
 		lang.addStringLocalization(KEY_GUI_CLEAR, "Clear");
 		lang.addStringLocalization(KEY_GUI_SCAN, "Scan");
 		lang.addStringLocalization(KEY_GUI_WORKING, "Working");
 		lang.addStringLocalization(KEY_GUI_HALTED, "Halted");
 		lang.addStringLocalization(KEY_GUI_REMOVE, "Remove");
 		lang.addStringLocalization(KEY_GUI_INSERT, "Insert");
 		lang.addStringLocalization(KEY_GUI_REPLACE, "Replace");
 		lang.addStringLocalization(KEY_GUI_NORMAL, "Normal");
 		lang.addStringLocalization(KEY_GUI_MODE, "Mode");
 	}
 }
