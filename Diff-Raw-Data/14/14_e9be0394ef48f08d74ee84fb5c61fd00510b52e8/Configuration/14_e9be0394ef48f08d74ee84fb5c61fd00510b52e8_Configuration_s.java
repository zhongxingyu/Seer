 package com.narrowtux.PistonChest;
 
 import java.io.File;
 
 import com.narrowtux.Utils.FlatFileReader;
 
 public class Configuration {
 	private boolean protectChests = true;
 	private boolean useFirstItem = true;
 	private FlatFileReader reader;
 	public Configuration(){
 		reader = new FlatFileReader(new File(PCMain.instance.getDataFolder(), "PistonChest.cfg"), false);
 		load();
 		reader.write();
 	}
 	private void load() {
 		protectChests = reader.getBoolean("protectchests", true);
		useFirstItem = reader.getBoolean("usefirstitem", true);
 	}
 	
 	public boolean hasChestProtection(){
 		return protectChests;
 	}
 	
 	public boolean usesFirstItem(){
 		return useFirstItem;
 	}
 }
