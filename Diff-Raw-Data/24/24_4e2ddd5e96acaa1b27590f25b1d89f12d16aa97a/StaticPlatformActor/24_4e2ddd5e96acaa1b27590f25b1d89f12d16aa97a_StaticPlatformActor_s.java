 package com.pix.mind.actors;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.box2d.bodies.StaticPlatform;
 
 public class StaticPlatformActor extends Image {
 	StaticPlatform staticPlatform;
 	public Color color;
 	boolean active;
 
 	public StaticPlatformActor(StaticPlatform platform, Color color,
 			boolean active) {
 		super(PixMindGame.getSkin().getDrawable(
 				PixMindGame.platformColorToTexture.get(color)));
 		staticPlatform = platform;
 		initialize(color, active);
 	}
 
 	public StaticPlatformActor(World world, float posX, float posY,
 			float width, float height, Color color, boolean active) {
 		super(PixMindGame.getSkin().getDrawable(
 				PixMindGame.platformColorToTexture.get(color)));
 		staticPlatform = new StaticPlatform(world, posX, posY, width, height);
 		initialize(color, active);
 	}
 
 	private void initialize(Color color, boolean active) {
 		staticPlatform.fixture.setUserData(this);
 		this.setSize(staticPlatform.PlatformWidth * PixMindGame.BOX_TO_WORLD
 				* 2, staticPlatform.PlatformHeight * PixMindGame.BOX_TO_WORLD
 				* 2);
 		this.setPosition(staticPlatform.getPosX(), staticPlatform.getPosY());
 		this.color = color;
 		this.active = active;
 		this.setActive(active);
 	}
 
 	public void setActive(boolean active) {
 		if (active) {
 			staticPlatform.fixture.setSensor(false);
 			this.setColor(255, 255, 255, 1);
 			this.active = true;
 		} else {
 			staticPlatform.fixture.setSensor(true);
			this.setColor(255, 255, 255, 0.2f);
 			this.active = false;
 		}
 	}
 
 }
