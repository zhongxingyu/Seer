 package com.thedevteam.thecomputermod.materials;
 
 import org.spout.api.material.Material;
 
 import com.thedevteam.thecomputermod.materials.blocks.Computer;
 
 public class CMMaterials {
	public void initialize(){
		Material.register(new Computer("Computer", 400));
	}
 }
