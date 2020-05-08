 package com.BASeCamp.SurvivalChests;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 
 import BASeCamp.Configuration.INIFile;
 //interfaces with a INIFile class to read plugin settings.
 public class Configurator {
 
 	private INIFile _Settings;
 	private BCRandomizer _Owner;
 	
 	public Configurator(BCRandomizer pOwner,INIFile source){
 		_Settings = source;
 		_Owner=pOwner;
 		
 	}
 	public Configurator(BCRandomizer pOwner) throws IOException{
 		
 		_Owner=pOwner;
 		_Settings = new INIFile(_Owner.acquireStream("survivalchests.ini"));
 		
 		
 	}
 	public INIFile getSettings() { return _Settings;}
 	public void setSettings(INIFile value){_Settings=value;}
 	public void save() throws IOException{
 		
 		_Settings.Save(BCRandomizer.pluginMainDir + "\\survivalchests.ini" );
 		
 		
 	}
 //name of the block that makes chests static. Defaults to "WOOL".
 	public String getContainerStaticBlockName() {
 		
 		
 		
 		return _Settings.getValue("blocks", "containerstatic", "WOOL").getValue();
 		
 		
 		
 	}
 	public String getContainerPackedBlockName(){
 		return _Settings.getValue("blocks", "containerpacked", "CLAY").getValue();
 		
 	}
 	public String getButtonBlockName() {
 		
 		return _Settings.getValue("blocks","buttonblock","GOLD").getValue();
 		
 	}
 	public Material getContainerStatic(){
 		
 		Material Acquired=StringToMaterial(getContainerStaticBlockName());
 		return Acquired==null?Material.WOOL:Acquired;
 		
 	}
 	public Material getContainerPacked() {
 		
 		Material Acquired=StringToMaterial(getContainerPackedBlockName());
 		return Acquired==null?Material.CLAY:Acquired;
 		
 	}
 	public Material getButtonBlock() {
 		
 		Material Acquired=StringToMaterial(getButtonBlockName());
 		return Acquired==null?Material.GOLD_BLOCK:Acquired;
 	}
 	private boolean isMaterial(String test){
 		return StringToMaterial(test)!=null;
 		
 	}
 	private Material StringToMaterial(String MaterialName){
 		
		System.out.println("lookin for:" + MaterialName);
 		if(MaterialName.length() > "Material.".length() &&  MaterialName.substring(0, "Material.".length()).equals("Material.")){
 			MaterialName = MaterialName.substring("Material.".length());
 		}
 		Material returnMaterial = Material.getMaterial(MaterialName);
 		
 		
 		//log it...
		if(returnMaterial!=null)
 			_Owner.getLogger().log(Level.INFO, "Material for input:" + MaterialName + " = " + 
		returnMaterial==null?"null":returnMaterial.toString());
 		
 		return returnMaterial;
 		
 		
 		
 	}
 	
 	
 }
