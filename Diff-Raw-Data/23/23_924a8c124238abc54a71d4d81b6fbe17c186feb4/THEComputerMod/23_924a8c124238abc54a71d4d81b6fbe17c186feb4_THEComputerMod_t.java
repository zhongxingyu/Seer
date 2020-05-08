 package com.thedevteam.thecomputermod;
 
 import java.util.logging.Level;
 
 import org.spout.api.event.Listener;
 import org.spout.api.plugin.CommonPlugin;
 
 import com.thedevteam.thecomputermod.materials.CMMaterials;
 import com.thedevteam.thecomputermod.materials.blocklistener.BlockListener;
 
 public class THEComputerMod extends CommonPlugin{
 
 	Listener blockListener = null;
 	CMMaterials Materials = new CMMaterials();
 	
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onEnable() {
 		blockListener = new BlockListener(this);
 		getGame().getEventManager().registerEvents(blockListener, this);
 		log(Level.INFO, "THEComputerMod has been enabled");
 	}
 
 	public void log(Level warning, String string) {
 		getLogger().log(warning, string);
 	}
 
 }
