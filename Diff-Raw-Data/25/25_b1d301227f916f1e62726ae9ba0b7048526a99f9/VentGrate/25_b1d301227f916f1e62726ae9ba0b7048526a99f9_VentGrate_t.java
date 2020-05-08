 package com.fastquake.textbasedgame.gameobject;
 
 public class VentGrate extends GameObject{
 	boolean electrified;
 	
 	public VentGrate(){
		this.name = "grate";
		this.openableName = "grate";
		this.otherNames = new String[]{"vent grate", "vent"};
 		electrified = true;
 		this.examineText = "It's shiny and made of metal. As you approach it, you hear a faint buzzing sound emanating from it.";
 	}
 }
