 package com.bukkit.Magick.ARGAntiPirate;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.bukkit.entity.Player;
 
 public class ARG_Rank {
 	static String		maindirectory	= "argantipirate/";
 	static File			playerRanksFile	= new File(maindirectory + "playerranks.data");
 	private Properties	PlayerRanks		= new Properties();
 	private ARGAntiPirate plugin;
 	
 	public ARG_Rank() {
 		try {
 			if (!playerRanksFile.exists()) {
 				new File(maindirectory).mkdir();
 				playerRanksFile.createNewFile();
 			}
 			FileInputStream inn = new FileInputStream(playerRanksFile);
 			PlayerRanks.load(inn);
 		} catch (IOException e) {
 			e.printStackTrace();
 			plugin.getServer().broadcastMessage("Problem creating Player Rank File");
 		}
 	}
 	
 	public boolean Load(){
 		try {
 
 			FileInputStream inn = new FileInputStream(playerRanksFile);
 			PlayerRanks.load(inn);
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	public boolean Save(){
 		try {
 
 			FileOutputStream inn = new FileOutputStream(playerRanksFile);
 			PlayerRanks.store(inn,null);
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public int getRank(Player p) {
 		String player = p.getName().toLowerCase();
 		String prank = PlayerRanks.getProperty(player);
		if (prank == null) {
 			return -1;
 		} else {
 			return Integer.parseInt(prank);
 		}
 	}
 
 	public boolean isAdmin(String player) {
 		String prank = PlayerRanks.getProperty(player);
 		if (Integer.parseInt(prank) >= 4) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean reloadRanks() {
 		try {
 			PlayerRanks.store(new FileOutputStream(playerRanksFile),null);
 			PlayerRanks.load(new FileInputStream(playerRanksFile));
 			return true;
 		} catch (IOException e) {
 			plugin.getServer().broadcastMessage("Problem reloading Rank data.");
 			return false;
 		}
 	}
 
 	public int setRank(String[] args) {
 		try {
 			PlayerRanks.setProperty(args[0].toLowerCase(), args[1]);
 			PlayerRanks.store(new FileOutputStream(playerRanksFile), null);
 			return Integer.valueOf(args[1]);
 		} catch (IOException e) {
 			plugin.getServer().broadcastMessage("Problem setting Rank data.");
 			return -1;
 		}
 	}
 	
 	public void save() {
 		try {
 			PlayerRanks.store(new FileOutputStream(playerRanksFile), null);
 		} catch (IOException ex) {
 			plugin.getServer().broadcastMessage("Problem saving Rank data.");
 		}
 	}
 }
