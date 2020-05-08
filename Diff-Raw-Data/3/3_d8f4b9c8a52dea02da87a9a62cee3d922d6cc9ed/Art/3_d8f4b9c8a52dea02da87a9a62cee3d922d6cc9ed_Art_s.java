 package com.subfty.krkjam2013.util;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.utils.Logger;
 
 public class Art {
 	
     //ID OF ATLASES
 	public static int A_BACKGROUND = 1;
 	public static int A_ENTITIES = 0;
 	
 	
	private String atlasesSrc[] = { "data/alien.pack",
									""};  
 	public TextureAtlas atlases[] = new TextureAtlas[atlasesSrc.length];
 	
 	public Art(){
 		for(int i=0; i<atlasesSrc.length; i++)
 			atlases[i] = new TextureAtlas(Gdx.files.internal(atlasesSrc[i]));
 		
 	}
 }
