 package org.cvpcs.android.cwiidconfig.config;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import android.util.Log;
 
 public class PresetManager {
 	private static final String TAG = "CWiiDConfig/PresetManager";
 	
 	// directory where system presets are stored
 	public static final File SYSTEM_PRESET_DIRECTORY = new File("/system/etc/cwiid/wminput");
 	// directory where user presets are stored
 	public static final File USER_PRESET_DIRECTORY = new File("/sdcard/.cwiid/wminput");
 	
 	// directories we know cwiid will scan for config files
 	private static File[] PRESET_SCAN_DIRECTORIES = {
 		SYSTEM_PRESET_DIRECTORY,
 		USER_PRESET_DIRECTORY
 	};
 
 	private static ArrayList<Preset> PRESETS = null;
 
 	public static void scanPresets() {
 		if(PRESETS == null) {
 			PRESETS = new ArrayList<Preset>();
 		}
 
 		PRESETS.clear();
 
 		Preset auto = AutoPreset.getAutoPreset();
 		
 		// scan the known config directories for presets
 		for(File config_dir : PRESET_SCAN_DIRECTORIES) {
 			// sanity check
 			if(!config_dir.exists() || !config_dir.isDirectory()) {
 				continue;
 			}
 
 			// we don't scan recursively because cwiid won't
 			for(File config_file : config_dir.listFiles()) {
 				// ignore directories/sanity check
 				if(!config_file.exists() || !config_file.isFile()) {
 					continue;
 				}
 				
 				// ignore auto-preset config
 				if(config_file.equals(auto.getFile())) {
 					continue;
 				}
 
 				PRESETS.add(new Preset(config_file));
 			}
 		}
 
 		Collections.sort(PRESETS);
 	}
 
 	public static int getNumPresets() {
 		if(PRESETS == null) {
 			scanPresets();
 		}
 
 		return PRESETS.size();
 	}
 	
 	/**
 	 * Gets a preset with the given name.
 	 * 
 	 * Note: if the preset is not found, then it will return a new preset
 	 *       with the 
 	 * @param name
 	 * @return
 	 */
 	public static Preset getPreset(String name) {
 		if(PRESETS == null) {
 			scanPresets();
 		}
 		
 		// check if we have a preset already by that name
 		for(Preset preset : PRESETS) {
 			if(preset.getName().equals(name)) {
 				Log.d(TAG, "getPreset returning existing preset: " + preset.getName());
 				return preset;
 			}
 		}
 		
 		// no preset found, time to create one with that name
 		File newPresetFile = new File(USER_PRESET_DIRECTORY, presetNameToFilename(name));
 		Preset newPreset = new Preset(newPresetFile);
 		newPreset.setName(name);
 		
 		return newPreset;
 	}
 	
 	/**
 	 * Transform the preset's name into a valid filename.
 	 * 
 	 * This replaces all non-alphanumeric characters with underscores.
 	 * 
 	 * @param name Name to convert
 	 * @return Filename returned
 	 */
 	public static String presetNameToFilename(String name) {
 		return name.replaceAll("[^0-9A-Za-z]", "_");
 	}
 
 	/**
 	 * Gets a preset at the given index of the list of presets available
 	 * 
 	 * @param idx index of the preset
 	 * 
 	 * @return The idx'th preset or null if idx is larger than the number of
 	 *         presets.
 	 */
 	public static Preset getPreset(int idx) {
 		if(PRESETS == null) {
 			scanPresets();
 		}
 
 		if(idx < PRESETS.size()) {
 			return PRESETS.get(idx);
 		} else {
 			return null;
 		}
 	}
 }
