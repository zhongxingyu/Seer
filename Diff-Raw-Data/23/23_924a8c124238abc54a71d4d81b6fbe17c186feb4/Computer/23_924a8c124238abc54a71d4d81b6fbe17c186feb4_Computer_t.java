 package com.thedevteam.thecomputermod.materials.blocks;
 
import org.spout.vanilla.material.block.generic.Solid;
 
public class Computer extends Solid{
 
 	
 	public Computer(String name, int id) {
 		super(name, id);
 		setDrop(getMaterial());
 		setDisplayName("Computer");
 		setDropCount(1);
 		setMaxStackSize(1);
 	}
 
 }
