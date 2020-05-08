 package com.alta189.minemail.config;
 
 import java.io.BufferedReader;
import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.InputStreamReader;
 import java.util.HashMap;
 
 import com.alta189.minemail.MineMail;
 
 public class MainSettingsFile {
 	private MineMail plugin;
 	public String fileName = "settings.properties";
 	public File file;
 	private HashMap<String,String> FileContents = new HashMap<String,String>();
 	
 	//Declare Settings\\
 	public Integer primaryColor = 10;
 	public Integer secondaryColor = 12;
 	public Integer headerColor = 8;
 	public Integer errorColor = 7;
 	public Integer helpColor = 14;
 	public Boolean iConomyEnabled = false;
 	public Double costSend = 10.0;
 	public Double costReceive = 5.0;
 	public Double costLongSend = 20.0;
 	public Boolean OPfree = true;
 
 	
 	public MainSettingsFile(MineMail instance) {
 		this.plugin = instance;
 		this.file = new File(this.plugin.pFolder.getPath() + File.separator + fileName);
 	}
 
 	
 	private void create(String name) {
 	      InputStream input = getClass().getResourceAsStream("defaults/" + name);
 	      if (input != null) {
 	        FileOutputStream output = null;
 	        try
 	        {
 	          output = new FileOutputStream(file);
 	          byte[] buf = new byte[8192];
 	          int length = 0;
 
 	          while ((length = input.read(buf)) > 0) {
 	            output.write(buf, 0, length);
 	          }
 
 	        } catch (Exception e) {
 	          e.printStackTrace();
 	        } finally {
 	          try {
 	            if (input != null)
 	              input.close();
 	          } catch (Exception e) {
 	          }
 	          try {
 	            if (output != null)
 	              output.close();
 	          }
 	          catch (Exception e)
 	          {
 	          }
 	        }
 	      }
 	    }
 	
 	
 	public void load(Boolean force) { //If force is true than it will delete the file and recreate it
 		
 		if (file == null) {
 			this.file = new File(this.plugin.pFolder.getPath() + File.separator + fileName);
 		}
 		
 		if (!file.exists() || force) {
 			if (force) this.delete();
 			//create();
 			create("Settings.properties");
 		}
 		
 		this.FileContents = loadFileContents();
 		
 		
 		//Get the Color Values\\
 		if (this.containsKey("headercolor")) {
 			this.headerColor = this.getPropertyInteger("headercolor");
 		}
 		if (this.containsKey("secondarycolor")) {
 			this.secondaryColor = this.getPropertyInteger("secondarycolor");
 		}
 		if (this.containsKey("primarycolor")) {
 			this.primaryColor = this.getPropertyInteger("primarycolor");
 		}
 		if (this.containsKey("errorcolor")) {
 			this.errorColor = this.getPropertyInteger("errorcolor");
 		}
 		if (this.containsKey("helpcolor")) {
 			this.helpColor = this.getPropertyInteger("helpcolor");
 		}
 		
 		//Check Color values\\
 		if (this.headerColor > 14 || this.headerColor < 0) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, HeaderColor is not valid. MineMail will use default");
 			this.headerColor = 8;
 		}
 		if (this.primaryColor > 14 || this.primaryColor < 0) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, PrimaryColor is not valid. MineMail will use default");
 			this.primaryColor = 8;
 		}
 		if (this.secondaryColor > 14 || this.secondaryColor < 0) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, SecondaryColor is not valid. MineMail will use default");
 			this.secondaryColor = 8;
 		}
 		if (this.errorColor > 14 || this.errorColor < 0) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, ErrorColor is not valid. MineMail will use default");
 			this.errorColor = 8;
 		}
 		if (this.helpColor > 14 || this.helpColor < 0) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, HelpColor is not valid. MineMail will use default");
 			this.helpColor = 14;
 		}
 		
 		//Load iConomy Settings\\
 		if (this.containsKey("turn_iconomy_on")) {
 			this.iConomyEnabled = this.getPropertyBoolean("turn_iconomy_on");
 		}
 		if (this.containsKey("FreeForOps")) {
 			this.OPfree = this.getPropertyBoolean("FreeForOps");
 		}
 		if (this.containsKey("sendprice")) {
 			this.costSend = this.getPropertyDouble("sendprice");
 		}
 		if (this.containsKey("receiveprice")) {
 			this.costReceive = this.getPropertyDouble("receiveprice");
 		}
 		if (this.containsKey("longmessageprice")) {
 			this.costLongSend = this.getPropertyDouble("longmessageprice");
 		}
 		
 		//Check iConomy Settings\\
 		if (this.iConomyEnabled == null) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, Turn_iConomy_ON is not valid. MineMail will use default");
 			this.iConomyEnabled = false;
 		}
 		if (this.OPfree == null) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, FreeForOps is not valid. MineMail will use default");
 			this.OPfree = true;
 		}
 		if (this.costSend == null || this.costSend < 0) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, sendPrice is not valid. MineMail will use default");
 			this.costSend = 10.0;
 		}
 		if (this.costReceive == null || this.costReceive < 0) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, receivePrice is not valid. MineMail will use default");
 			this.costReceive = 5.0;
 		}
 		if (this.costLongSend == null || this.costLongSend < 0) {
 			this.plugin.log.warning(this.plugin.logPrefix + " Error in settings, longMessagePrice is not valid. MineMail will use default");
 			this.costLongSend = 20.0;
 		}
 		
 	}
 	
 	public Boolean containsKey(String key) {
 		return this.FileContents.containsKey(key);
 	}
 	
 	private void delete() {
 		File delFile = file;
 		
 		if (delFile.exists()) {
 			delFile.delete();
 		}
 	}
 
 	private HashMap<String,String> loadFileContents(){
 		HashMap<String,String> result = new HashMap<String,String>();
 		
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String word = null;
 			
 			while((word = br.readLine()) != null) {
 				if ((word.isEmpty()) || (word.startsWith("#")) || (!word.contains(":"))) continue;
 				String[] args = word.split(":");
 				result.put(args[0].toLowerCase(), args[1]);
 			}
 			
 		} catch (IOException ex) {
 			// TODO Auto-generated catch block
 			plugin.log.severe(plugin.logPrefix + "Error loading settings.properties file!");
 		}
 		
 		return result;
 	}
 	
 	public String getPropertyString(String property){
 		if (!file.exists()) {
 			load(false);
 		}
 		try {
 			if (FileContents.containsKey(property.toLowerCase())) {
 				return FileContents.get(property);
 			}
 			
 				
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 		
 	}
 	
 	public Boolean getPropertyBoolean(String property) {
 		if (!file.exists()) {
 			load(false);
 		}
 		try {
 			String result = FileContents.get(property);
 			if (result.equalsIgnoreCase("true") || result.equalsIgnoreCase("false")) {
 				return Boolean.valueOf(result.toLowerCase());
 			} else {
 				return false;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 		
 	}
 
 	public Integer getPropertyInteger(String property) {
 		if (!file.exists()) {
 			load(false);
 		}
 		try {
 			String result = FileContents.get(property);
 			return Integer.valueOf(result);
 				
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 10;
 	}
 	
 	public Double getPropertyDouble(String property) {
 		if (!file.exists()) {
 			load(false);
 		}
 		try {
 			String result = FileContents.get(property);
 			if (!result.contains(".")) result = result + ".0";
 			
 			return Double.valueOf(result);
 				
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return -10.0;
 	}
 	
 }
