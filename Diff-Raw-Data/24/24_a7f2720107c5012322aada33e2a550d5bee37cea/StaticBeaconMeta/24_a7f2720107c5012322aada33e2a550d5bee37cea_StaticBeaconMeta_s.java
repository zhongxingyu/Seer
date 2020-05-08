 package com.untamedears.rourke750.ExpensiveBeacons;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class StaticBeaconMeta{
 	private static int maxSize = 10;
 	private static StaticBeaconStructure arr[] = new StaticBeaconStructure[maxSize];
 
 	
 	public StaticBeaconStructure getStruct(int i){
 		return arr[i];
 	}
 	
	public StaticBeaconStructure overStruct(int i, StaticBeaconStructure a){
		return arr[i]=a;
 	}
 	
 	public int getMaxSize(){
 		return maxSize;
 	}
 }
