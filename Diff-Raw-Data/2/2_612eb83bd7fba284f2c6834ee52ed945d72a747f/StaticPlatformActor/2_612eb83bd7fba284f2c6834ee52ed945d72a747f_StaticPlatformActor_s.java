 package com.pix.mind.actors;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.NinePatch;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.box2d.bodies.StaticPlatform;
 
 public class StaticPlatformActor extends Group {
 	StaticPlatform staticPlatform;
 	public Color color;
 	boolean active;
 	Image platformImage;
 	Image frame;
 	public StaticPlatformActor(StaticPlatform platform, Color color,
 			boolean active) {
 		//super(PixMindGame.getSkin().getDrawable(
 		//		PixMindGame.platformColorToTexture.get(color)));
 		platformImage = new Image(PixMindGame.getSkin().getDrawable(PixMindGame.platformColorToTexture.get(color)));
 		this.addActor(platformImage);
 		NinePatch patch = PixMindGame.getSkin().getPatch("platformframe");
 
 		frame = new Image(patch);
 		
 		this.addActor(frame);
 		
 		staticPlatform = platform;
 		initialize(color, active);
 	}
 
 	public StaticPlatformActor(World world, float posX, float posY,
 			float width, float height, Color color, boolean active) {
 		//super(PixMindGame.getSkin().getDrawable(
 		//		PixMindGame.platformColorToTexture.get(color)));
 		platformImage = new Image(PixMindGame.getSkin().getDrawable(PixMindGame.platformColorToTexture.get(color)));
 		this.addActor(platformImage);
		frame = new Image(PixMindGame.getSkin().getPatch("frame"));
 		this.addActor(frame);
 		
 		staticPlatform = new StaticPlatform(world, posX, posY, width, height);
 		
 		initialize(color, active);
 	}
 
 	private void initialize(Color color, boolean active) {
 		staticPlatform.fixture.setUserData(this);
 		platformImage.setSize(staticPlatform.PlatformWidth * PixMindGame.BOX_TO_WORLD
 				* 2, staticPlatform.PlatformHeight * PixMindGame.BOX_TO_WORLD
 				* 2);
 		platformImage.setPosition(staticPlatform.getPosX(), staticPlatform.getPosY());
 		frame.setSize(staticPlatform.PlatformWidth * PixMindGame.BOX_TO_WORLD
 				* 2, staticPlatform.PlatformHeight * PixMindGame.BOX_TO_WORLD
 				* 2);
 		frame.setPosition(staticPlatform.getPosX(), staticPlatform.getPosY());
 		frame.setColor(color);
 		this.color = color;
 		this.active = active;
 		this.setActive(active);
 	}
 
 	public void setActive(boolean active) {
 		if (active) {
 			staticPlatform.fixture.setSensor(false);
 			platformImage.setColor(255, 255, 255, 1);
 			frame.setColor(Color.CLEAR);
 			this.active = true;
 		} else {
 			staticPlatform.fixture.setSensor(true);
 
 			platformImage.setColor(255, 255, 255, 0.2f);
 			frame.setColor(color.r, color.g, color.b,1f);
 			
 
 			this.active = false;
 		}
 	}
 
 }
