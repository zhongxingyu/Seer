 package com.turbonips.troglodytes;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.turbonips.troglodytes.components.AnimationCreature;
 import com.turbonips.troglodytes.components.Collision;
 import com.turbonips.troglodytes.components.Sliding;
 import com.turbonips.troglodytes.components.SpatialForm;
 import com.turbonips.troglodytes.components.Transform;
 
 public class EntityFactory {
 	
 	public static final int ID_PLAYER = 0;
 	public static final int ID_GROUND_LAYER = 1;
 	public static final int ID_BG_LAYER = 2;
 	public static final int ID_FG_LAYER = 3;
 	public static final int ID_WALL_LAYER = 4;
 
 	public static Entity create(World world, int id) throws SlickException {
 		ResourceManager resourceManager = ResourceManager.getInstance();		
 		Image playerImage = (Image)resourceManager.getResource("testplayerimage").getObject();
 		TiledMap startMap = (TiledMap)resourceManager.getResource("trog0").getObject();
 		SpriteSheet playerSpriteSheet = (SpriteSheet)resourceManager.getResource("testplayersprite").getObject();
 		Vector2f slidingStart = new Vector2f((float)playerImage.getWidth()/2, (float)playerImage.getHeight()/2);
 		int speed = 8;
 		Rectangle box = new Rectangle(speed*-15, speed*-12, speed*15, speed*12);
		Vector2f startPosition = new Vector2f(32*5, 64*5);
 		switch(id) {
 			case ID_PLAYER:
 				Entity player = world.createEntity();
 				player.setGroup("CREATURE");
 				player.addComponent(new Transform(startPosition, speed));
				//player.addComponent(new Sliding(slidingStart, speed, box));
 				player.addComponent(new SpatialForm(playerImage, SpatialForm.TYPE_PLAYER));
 				player.addComponent(new AnimationCreature(playerSpriteSheet));
 				player.addComponent(new Collision());
 				player.refresh();
 				return player;
 			case ID_GROUND_LAYER:
 				Entity ground = world.createEntity();
 				ground.setGroup("LAYER");
 				ground.addComponent(new Transform(startPosition, speed));
				//ground.addComponent(new Sliding(slidingStart, speed, box));
 				ground.addComponent(new SpatialForm(startMap, SpatialForm.TYPE_GROUND_LAYER));
 				ground.addComponent(new Collision());
 				ground.refresh();
 				return ground;
 			case ID_BG_LAYER:
 				Entity background = world.createEntity();
 				background.setGroup("LAYER");
 				background.addComponent(new Transform(startPosition, speed));
				//background.addComponent(new Sliding(slidingStart, speed, box));
 				background.addComponent(new SpatialForm(startMap, SpatialForm.TYPE_BACKGROUND_LAYER));
 				background.addComponent(new Collision());
 				background.refresh();
 				return background;
 			case ID_FG_LAYER:
 				Entity foreground = world.createEntity();
 				foreground.setGroup("LAYER");
 				foreground.addComponent(new Transform(startPosition, speed));
				//foreground.addComponent(new Sliding(slidingStart, speed, box));
 				foreground.addComponent(new SpatialForm(startMap, SpatialForm.TYPE_FOREGROUND_LAYER));
 				foreground.addComponent(new Collision());
 				foreground.refresh();
 				return foreground;
 			case ID_WALL_LAYER:
 				Entity wall = world.createEntity();
 				wall.setGroup("LAYER");
 				wall.addComponent(new Transform(startPosition, speed));
				//wall.addComponent(new Sliding(slidingStart, speed, box));
 				wall.addComponent(new SpatialForm(startMap, SpatialForm.TYPE_WALL_LAYER));
 				wall.addComponent(new Collision());
 				wall.refresh();
 				return wall;
 		}
 		return null;
 		
 	}
 	
 }
