 package de.hotmail.gurkilein.bankcraft;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 
 
 public class OldDataImportHandler {
 
 	private Bankcraft bankcraft;
 
 	public OldDataImportHandler(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 	}
 
 	public boolean importOldData() {
 		//TODO
 		bankcraft.getLogger().info("Searching for flatfile data...");
 		
 		{
 		//Import Money Data
 		bankcraft.getLogger().info("Importing money data...");
 		
 		File accountsFolder = new File("plugins"+System.getProperty("file.separator")+"Bankcraft"+System.getProperty("file.separator")+"Accounts");
 		double amount;
		String line;
 		
 		for (File playerFile: accountsFolder.listFiles()) {
 			if (playerFile.getName().endsWith("db")) {
 			try {
 			FileReader fr = new FileReader(playerFile);
 			BufferedReader br = new BufferedReader(fr);
			line = br.readLine();
			if (line.equals("0.00")) 
			amount = 0;	
			else
 			amount = Double.parseDouble(br.readLine());
 			br.close();
 			fr.close();
 			
 			bankcraft.getMoneyDatabaseInterface().setBalance(playerFile.getName().split("\\.")[0], amount);
 			} catch (Exception e) {
 				bankcraft.getLogger().info("Could not import data from "+playerFile.getName());
 				e.printStackTrace();
 			}
 			}
 		}
 		}
 		
 		
 		{
 		//Import Experience Data
 		bankcraft.getLogger().info("Importing money data...");
 		
 		File xpAccountsFolder = new File("plugins"+System.getProperty("file.separator")+"Bankcraft"+System.getProperty("file.separator")+"XPAccounts");
 		int amount;
		String line;
 		
 		for (File playerFile: xpAccountsFolder.listFiles()) {
 			
 			if (playerFile.getName().endsWith("db")) {
 			try {
 			FileReader fr = new FileReader(playerFile);
 			BufferedReader br = new BufferedReader(fr);
			line = br.readLine();
			if (line.equals("0.00")) 
			amount = 0;	
			else
			amount = Integer.parseInt(line);
 			br.close();
 			fr.close();
 			
 			bankcraft.getExperienceDatabaseInterface().setBalance(playerFile.getName().split("\\.")[0], amount);
 			} catch (Exception e) {
 				bankcraft.getLogger().info("Could not import data from "+playerFile.getName());
 				e.printStackTrace();
 			}
 			}
 		}
 		}
 		
 		{
 		//Import Sign Data
 		bankcraft.getLogger().info("Importing sign data...");
 		
 		File signFile = new File("plugins"+System.getProperty("file.separator")+"Bankcraft"+System.getProperty("file.separator")+"banks.db");
 		String line;
 		String [] lineArray;
 		
 		try{
 			
 		FileReader fr = new FileReader(signFile);
 		BufferedReader br = new BufferedReader(fr);
 		
 		while ((line = br.readLine()) != null) {
 			lineArray = line.split(":",6);
 			bankcraft.getSignDatabaseInterface().createNewSign(Integer.parseInt(lineArray[0]), Integer.parseInt(lineArray[1]), Integer.parseInt(lineArray[2]), bankcraft.getServer().getWorld(lineArray[3]), Integer.parseInt(lineArray[4]), lineArray[5]);
 
 		}
 		
 		
 		br.close();
 		fr.close();
 		} catch (Exception e) {
 			bankcraft.getLogger().info("Could not import sign data");
 			e.printStackTrace();
 		}
 
 		
 	}
 		bankcraft.getLogger().info("Finished import of old data! Remember to set importOldData to false in the config.yml!!!");
 		return true;
 	}
 }
