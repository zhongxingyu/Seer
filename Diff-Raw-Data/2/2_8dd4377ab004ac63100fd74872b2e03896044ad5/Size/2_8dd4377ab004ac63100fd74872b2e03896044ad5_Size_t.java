 package org.netvogue.server.aws.core;
 
 public enum Size {
 	S1 (20,16),
 	S2 (57,52),
 	S3 (149,93),
 	S4 (184,138),
 	S5 (4,4),
 	S6 (4,4),
 	S7 (4,4),
 	S8 (4,4),
 	S9 (4,4),
 	S10 (4,4),
 	GThumb(220, 150),
	GAdd(220, 150),
 	GLeft(130, 100),
 	GMain(400, 400),
 	PCThumb(231, 306),
 	PCAdd(210, 300),
 	PCLeft(130, 100),
 	PCMain(400, 400),
 	EThumb(231, 306),
 	EAdd(210, 300),
 	ELeft(130, 100),
 	EMain(400, 400),
 	CThumb(231, 306),
 	CAdd(210, 300),
 	CLeft(130, 100),
 	CMain(400, 400);
 	
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
