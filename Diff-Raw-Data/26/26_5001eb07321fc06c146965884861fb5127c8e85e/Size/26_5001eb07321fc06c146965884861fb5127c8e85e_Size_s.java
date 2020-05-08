 package org.netvogue.server.aws.core;
 
 public enum Size {
 	//Profile pic
 	PThumb(220, 306),
 	PTop(130, 100),
 	
 	//Gallery
 	GAdd(220, 150),
 	GThumb(220, 306),
 	GLeft(130, 100),
 	GMain(450, 677),
 	
 	//Print campaign
 	PCAdd(220, 150),
 	PCThumb(220, 306),
 	PCLeft(90, 72),
 	PCMain(450, 677),
 	
 	//Editorial
	EAdd(210, 150),
 	EThumb(220, 306),
 	ELeft(130, 100),
 	EMain(450, 677),
 	
 	//Collections
	CAdd(210, 150),
 	CThumb(231, 306),
 	CLeft(130, 100),
 	CMain(550, 550),
 	
 	//Styles
 	SAdd(130, 151),
 	SThumb(220, 320),
 	SLeft(130, 100),
 	SMain(550, 550),
 	
 	//Linesheets
 	LAdd(130, 151),
 	LThumb(220, 320),
 	LLeft(130, 100),
 	LMain(550, 550);
 	
 	private final int width;   // in pixels
     private final int height; // in pixels
     
 	Size(int width,int height ) {
 		this.width = width;
 		this.height = height;
 	}
 
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 }
