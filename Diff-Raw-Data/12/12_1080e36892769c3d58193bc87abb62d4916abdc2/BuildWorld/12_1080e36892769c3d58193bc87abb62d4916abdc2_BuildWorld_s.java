 package com.madcowd.buildnhide.entities;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.madcowd.buildnhide.entities.templates.*;
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.utils.Convert;
 import com.punchline.javalib.utils.SoundManager;
 
 public class BuildWorld extends EntityWorld {
 	
 	public BuildWorld(InputMultiplexer input, Camera camera) {
 		super(input, camera, new Vector2(0, 0));
 		
 		SoundManager.playSong("shemelts", 1f, true);
 		
 		debugView.enabled = true;
 		debugView.visible = true; //TODO: Remember to disable this...
 	}
 
 	@Override
 	public void process() {
 		super.process();
 		
 		
 		if(Gdx.input.isKeyPressed(Keys.LEFT))
 			camera.position.add(-5, 0,0);
 		if(Gdx.input.isKeyPressed(Keys.RIGHT))
 			camera.position.add(5, 0,0);
 		
 		if(Gdx.input.isKeyPressed(Keys.UP))
 			camera.position.add(0, 5,0);
 		if(Gdx.input.isKeyPressed(Keys.DOWN))
 			camera.position.add(0, -5,0);
 		
 		if(Gdx.input.isKeyPressed(Keys.X))
 			((OrthographicCamera)camera).zoom += 0.02f;
 		if(Gdx.input.isKeyPressed(Keys.Z))
 			((OrthographicCamera)camera).zoom -= 0.02f;
 	}
 
 	@Override
 	public Rectangle getBounds() {
 		return Convert.pixelsToMeters(
 				new Rectangle(
 					-Gdx.graphics.getWidth() * 2, 
 					-Gdx.graphics.getHeight() / 2, 
 					Gdx.graphics.getWidth() * 4, 
 					Gdx.graphics.getHeight()));
 	}
 
 	@Override
 	protected void positionCamera() { }
 	
 	@Override
 	protected void buildSystems() {		
 		super.buildSystems();
 		
 	}
 
 	@Override
 	protected void buildTemplates() {
 		super.buildTemplates();
 		
 		this.addGroupTemplate("terrain", new TerrainTemplate());
 		//Scenery
 		addTemplate("BigPlanet", new BigPlanetTemplate());
 		addTemplate("SmallPlanet", new SmallPlanetTemplate());
 		addTemplate("BigStar", new BigStarTemplate());
 		addTemplate("SmallStar", new SmallStarTemplate());
 		addGroupTemplate("StarField", new StarFieldTemplate());
 		
 	}
 	
 	@Override
 	protected void buildEntities() {
 		super.buildEntities();
 		
		this.createEntityGroup("terrain", 300);
 		this.createEntityGroup("StarField");
 		
 	}
 
 	@Override
 	protected void buildSpriteSheet() {
 		
 	}
 
 	
 }
