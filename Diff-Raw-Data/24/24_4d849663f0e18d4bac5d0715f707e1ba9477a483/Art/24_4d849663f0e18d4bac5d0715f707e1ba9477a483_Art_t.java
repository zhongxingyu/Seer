 package com.sampleshooter;
 
 import java.io.File;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class Art {
 	public static TextureRegion[][] player;
 	public static TextureRegion[][] ball;
 	public static TextureRegion[][] bomb;
	public static TextureRegion intro;
 	public static TextureRegion[][] line;
 	public static TextureRegion[][] note_full;
 	public static TextureRegion[][] kuula;
 	public static TextureRegion[][] note_perus;
 
 	public static void load()
 	{		
 		// Pit muuttaa suhteelliseksi assets directoriin androidilla. Ohje:
 		// http://code.google.com/p/libgdx/wiki/FileHandling
 		player = split("../sampleshooter/assets/player.png",64,64);
 		ball = split("../sampleshooter/assets/ball.png",16,16);
 		bomb = split("../sampleshooter/assets/bomb.png",16,16);
 		line = split("../sampleshooter/assets/nuottiviiva.png",4,512);
 		note_full = split("../sampleshooter/assets/Nuotti_koko.png",64,64);
		note_perus = split("../sampleshooter/assets/Nuotti_perus.png",128,64);
 		kuula = split("../sampleshooter/assets/kuula.png",32,32);
		intro = load("../sampleshooter/assets/StartupScreen_crop.png",1024,512);
 	}
 	
 	public static TextureRegion load (String name, int width, int height) {
 		Texture texture = new Texture(Gdx.files.internal(name));
 		TextureRegion region = new TextureRegion(texture, 0, 0, width, height);
 		region.flip(false, true);
 		return region;
 	}
 
 	private static TextureRegion[][] split (String name, int width, int height) {
 		return split(name, width, height, false);
 	}
 
 	private static TextureRegion[][] split (String name, int width, int height, boolean flipX) {
 		Texture texture = new Texture(Gdx.files.internal(name));
 
 
 		int xSlices = texture.getWidth() / width;
 		int ySlices = texture.getHeight() / height;
 		//System.out.println(xSlices+" "+ySlices);
 		TextureRegion[][] res = new TextureRegion[xSlices][ySlices];
 		for (int x = 0; x < xSlices; x++) {
 			for (int y = 0; y < ySlices; y++) {
 				res[x][y] = new TextureRegion(texture, x * width, y * height, width, height);
 				res[x][y].flip(flipX, true);
 			}
 		}
 		return res;
 	}
 
 }
