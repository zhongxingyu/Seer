 package com.me.mygdxgame;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class Assets{
 	
 	public static Texture splash;
 	public static Texture bg;
 	public static Texture playBtn;
 	public static Texture background;
 	public static Texture megaman;	
 	public static Animation megamanWalk;
 	public static Texture platformBase;
 	public static Texture platformMiddle;
<<<<<<< HEAD
 	public static Texture btn;
=======
 	public static Texture menubg;
 	public static Texture homebg;
 	public static Texture charfullbody;
 	public static BitmapFont font;
>>>>>>> Splashscreen and assets
 	
 	public static Texture loadTexture (String file){
 		return new Texture(Gdx.files.internal(file));
 	}
 		
 	public static void load(){
 		
 		splash = loadTexture("libgdx.png");
 			
 		bg = loadTexture("bg.jpg");	
 		playBtn = loadTexture("playButton.png");
 		
 		megaman = loadTexture("megaman.png");	
 		background = loadTexture("background.jpg");	
 		background = loadTexture("background.jpg");
 		platformBase = loadTexture("bottomGround.png");
 		platformMiddle = loadTexture("floatGroundMiddle.png");
 		menubg = loadTexture("menubg.png");
 		homebg = loadTexture("homebg.png");
 		charfullbody = loadTexture("charfullbody.png");
 		
 		font = new BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false);
 		
 		
 		btn = loadTexture("btn.png");	
 		megamanWalk = new Animation(0.2f, new TextureRegion(megaman, 0, 0, 36, 33), new TextureRegion(megaman, 33, 0, 36, 33));		
 
 	}
 
 }
 
 
 
 
