 package com.sosmedia.communitychest;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CommunityChestMain extends JavaPlugin {
 	public File fileInventory;
 	public File fileNames;
 	private DeconBlock deconBlock;
 	private HashMap<String, CardboardBox[]> chests = new HashMap<String, CardboardBox[]>();
 	private ArrayList<DeconBlock> list = new ArrayList<DeconBlock>();
 	private HashMap<String, ArrayList<DeconBlock>> signs = new HashMap<String, ArrayList<DeconBlock>>();
 	protected final ChestListener cl = new ChestListener(this);
 	CommandListener commandListener = new CommandListener(this);
 	@Override
 	public void onEnable() {
 		loadFile1();
 		loadFile2();
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {}	
 		getServer().getPluginManager().registerEvents(cl, this);
 		getCommand("cc").setExecutor(commandListener);
		getCommand("communitychest").setExecutor(commandListener);
 	}
 
 	@SuppressWarnings("unchecked")
 	//loads the String, CardboardBox[] map
 	public HashMap<String, CardboardBox[]> loadFile1() {
 		File fileDir = new File("plugins" + File.separator + getDescription().getName() + File.separator);
 		fileInventory = new File("plugins" + File.separator + getDescription().getName() + File.separator + "chestinventory.ser");
 		if(!fileInventory.exists()) {
 			if(!fileDir.exists()) {
 				fileDir.mkdir();
 			}
 			try {
 				fileInventory.createNewFile();
				//chests.put("server", new CardboardBox[54]);
 				saveFile(chests, fileInventory);
 			} catch (IOException e) {
 				// let them know
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				FileInputStream fis = new FileInputStream(fileInventory);
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				chests = (HashMap<String, CardboardBox[]>) ois.readObject();
 				ois.close();
 				return chests;
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	@SuppressWarnings("unchecked")
 	//loads the String, DeconBlock map
 	public HashMap<String, ArrayList<DeconBlock>> loadFile2() {
 		list.add(deconBlock);
 		File fileDir = new File("plugins" + File.separator + getDescription().getName() + File.separator);
 		fileNames = new File("plugins" + File.separator + getDescription().getName() + File.separator + "chestnames.ser");
 		if(!fileNames.exists()) {
 			if(!fileDir.exists()) {
 				fileDir.mkdir();
 			}
 			try {
 				fileNames.createNewFile();
				//signs.put("server", list);
 				saveFile(signs, fileNames);
 			} catch (IOException e) {
 				// let them know
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				FileInputStream fis = new FileInputStream(fileNames);
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				signs = (HashMap<String, ArrayList<DeconBlock>>) ois.readObject();
 				ois.close();
 				return signs;
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 	public boolean saveFile(Object obj, File file) {
 		try {
 			FileOutputStream fos = new FileOutputStream(file);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(obj);
 			oos.flush();
 			oos.close();
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 
 	@Override
 	public void onDisable() {
 
 	}
 }
