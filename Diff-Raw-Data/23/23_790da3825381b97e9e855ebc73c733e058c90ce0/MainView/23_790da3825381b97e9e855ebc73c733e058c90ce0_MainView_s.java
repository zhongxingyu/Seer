 package com.game.rania.view;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 
 import com.badlogic.gdx.Gdx;
 //import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.game.rania.RaniaGame;
 import com.game.rania.model.element.DynamicObject;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.RegionID;
 import com.game.rania.model.element.StaticObject;
 
 public class MainView {
 	
 	//camera
 	private Camera camera = null;
 	private final float widthSize = 800.0f;
 	
 	//sprites
 	private SpriteBatch spriteBatch = null;
 	private SpriteBatch spriteBatchHUD = null;
 	//private FPSLogger fpsLog;
 	
 	//textures
 	private HashMap<String, Texture> textures = new HashMap<String, Texture>();
 	private EnumMap<RegionID, TextureRegion> textureRegions = new EnumMap<RegionID, TextureRegion>(RegionID.class);
 	
 	public MainView(){
 		//create camera
 		camera = new Camera(widthSize);
 		
 		spriteBatch = new SpriteBatch();
 		spriteBatchHUD = new SpriteBatch();
 		//fpsLog = new FPSLogger();
 	}
 	
 	public TextureRegion loadTexture(String fileTexture, RegionID id) {
 		Texture texture = textures.get(fileTexture);
 		if (texture == null)
 		{
 			texture = new Texture(Gdx.files.internal(fileTexture));
 			textures.put(fileTexture, texture);
 		}
 		TextureRegion region = new TextureRegion(texture);
 		textureRegions.put(id, region);
 		return region;
 	}
 	
 	public TextureRegion loadTexture(String fileTexture, RegionID id, int x, int y, int width, int height) {
 		Texture texture = textures.get(fileTexture);
 		if (texture == null)
 		{
 			texture = new Texture(Gdx.files.internal(fileTexture));
 			textures.put(fileTexture, texture);
 		}
 		TextureRegion region = new TextureRegion(texture, x, y, width, height);
 		textureRegions.put(id, region);
 		return region;
 	}
 	
 	public void unloadTexture(String fileTexture){
 		textures.remove(fileTexture);
 	}
 	
 	public Texture getTexture(String fileTexture){
 		return textures.get(fileTexture);
 	}
 	
 	public TextureRegion getTextureRegion(RegionID id){
 		return textureRegions.get(id);
 	}
 	
 	public Texture getTexture(RegionID id){
 		TextureRegion regTexture = textureRegions.get(id);
 		if (regTexture == null)
 			return null;
 		return regTexture.getTexture();
 	}
 	
 	public Camera getCamera(){
 		return camera;
 	}
 	
 	public void setCamera(Camera cam){
 		camera = cam;
 	}
 	
 	public void clear(){
 		textureRegions.clear();
 		for(Texture texture : textures.values()){
 			texture.dispose();
 		}
 		textures.clear();
 	}
 	
 	public void draw(){
 		if (camera == null)
 			return;
 
 		//update camera
 		camera.update();
 		
 		//start render
 		spriteBatch.setProjectionMatrix(camera.combined);
 		spriteBatch.begin();
 
 		//render static objects
 		for (StaticObject object : RaniaGame.mController.getStaticObjects()) {
 			object.draw(spriteBatch);
 		}
 
 		//render dynamic objects
 		for (DynamicObject object : RaniaGame.mController.getDynamicObjects()) {
 			object.draw(spriteBatch);
 		}
 		
 		//render player
 		DynamicObject player = RaniaGame.mController.getPlayer();
 		if (player != null)
 		{
 			camera.position.set(player.position.x, player.position.y, 0);
 			player.draw(spriteBatch);
 		}
 		
 		//end render
 		spriteBatch.end();
 		
 		spriteBatchHUD.begin();
 		//render HUD objects
 		for (Object object : RaniaGame.mController.getHUDObjects()) {
 			object.draw(spriteBatchHUD);
 		}
 		spriteBatchHUD.end();
 		
 		//fpsLog.log();
 	}
 }
