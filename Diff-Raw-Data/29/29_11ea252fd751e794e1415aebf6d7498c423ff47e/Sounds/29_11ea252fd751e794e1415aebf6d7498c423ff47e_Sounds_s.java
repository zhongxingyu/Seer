 package com.mrgreaper.twisted.client.sounds;
 
 import net.minecraft.client.Minecraft;
 
 public enum Sounds {
	BOMB_SPREAD("beep"),
	BOMB_DROP("bombfall"),
	OUT_OF_AMMO("emptyclick"),
	WAND_USE("wand"),
	
	//above is part of the tutorial ..bellow is mine
	
 	BUNNY_OWW("bunnyOww"),
 	BUNNY_LITTLE("LittleBunny"),
 	EVIL_LAUGH("laugh"),
 	CREATED_MONSTER("monster");
 	
 	public static final String SOUNDS_LOCATION = "twisted";
 	private String name;
 	
 	Sounds(String name){
 		this.name = name;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void play(double x, double y, double z, float volume, float pitch){
 		Minecraft.getMinecraft().sndManager.playSound(SOUNDS_LOCATION + ":" + name, (float)x, (float)y, (float)z, volume, pitch);
 	}
 
 }
