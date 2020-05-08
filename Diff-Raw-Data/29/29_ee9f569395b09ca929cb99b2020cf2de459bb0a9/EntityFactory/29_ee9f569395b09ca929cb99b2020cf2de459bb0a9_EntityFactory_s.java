 package com.opg.javagamexyz.artemis;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.artemis.managers.GroupManager;
 import com.badlogic.gdx.math.MathUtils;
 import com.opg.javagamexyz.JavagameXYZ.Constants;
 import com.opg.javagamexyz.artemis.components.Bounds;
 import com.opg.javagamexyz.artemis.components.ColorAnimation;
 import com.opg.javagamexyz.artemis.components.Expires;
 import com.opg.javagamexyz.artemis.components.Health;
 import com.opg.javagamexyz.artemis.components.ParallaxStar;
 import com.opg.javagamexyz.artemis.components.Player;
 import com.opg.javagamexyz.artemis.components.Position;
 import com.opg.javagamexyz.artemis.components.ScaleAnimation;
 import com.opg.javagamexyz.artemis.components.Sprite;
 import com.opg.javagamexyz.artemis.components.Sprite.Layer;
 import com.opg.javagamexyz.artemis.components.Velocity;
 
// TODO remove lots of "new" usage
 public class EntityFactory {
 	public static Entity createPlayer(World world, float x, float y) {
 		Entity e = world.createEntity();
 		
 		Position position = new Position(x, y);
 		e.addComponent(position);
 		
 		Sprite sprite = new Sprite("fighter", Layer.ACTORS_3);
 		sprite.r = 93/255f;
 		sprite.g = 255/255f;
 		sprite.b = 129/255f;
 		sprite.a = 1.0f;
 		e.addComponent(sprite);
 		
 		Velocity velocity = new Velocity();
 		e.addComponent(velocity);
 		
 		Player player = new Player();
 		e.addComponent(player);
 		
 		Bounds bounds = new Bounds(43);
 		e.addComponent(bounds);
 		
 		return e;
 	}
 	
 	public static Entity createBullet(World world, float x, float y) {
 		Entity e = world.createEntity();
 		
 		Position position = new Position(x, y);
 		e.addComponent(position);
 		
 		Sprite sprite = new Sprite("bullet", Layer.PARTICLES);
 		e.addComponent(sprite);
 		
 		Velocity velocity = new Velocity(0, 800);
 		e.addComponent(velocity);
 		
 		Expires expires = new Expires(2.0f);
 		e.addComponent(expires);
 		
 		Bounds bounds = new Bounds(5);
 		e.addComponent(bounds);
 		
 		world.getManager(GroupManager.class).add(e, Constants.Groups.PLAYER_BULLETS);
 		
 		return e;
 	}
 	
 	public static Entity createEnemyShip(World world, String name, Layer layer, float health, float x, float y, float vx, float vy, float boundsRadius) {
 		Entity e = world.createEntity();
 		
 		Position position = new Position(x, y);
 		e.addComponent(position);
 		
 		Sprite sprite = new Sprite(name, layer);
 		sprite.r = 255/255f;
 		sprite.g = 0/255f;
 		sprite.b = 142/255f;
 		sprite.a = 1.0f;
 		e.addComponent(sprite);
 		
 		Velocity velocity = new Velocity(vx, vy);
 		e.addComponent(velocity);
 		
 		Health h = new Health(health);
 		e.addComponent(h);
 		
 		Bounds bounds = new Bounds(boundsRadius);
 		e.addComponent(bounds);
 		
 		world.getManager(GroupManager.class).add(e, Constants.Groups.ENEMY_SHIPS);
 		
 		return e;
 	}
 	
 	public static Entity createParticle(World world, float x, float y) {
 		Entity e = world.createEntity();
 		
 		Position position = new Position(x, y);
 		e.addComponent(position);
 		
 		Sprite sprite = new Sprite("particle", Layer.PARTICLES);
 		sprite.scaleX = sprite.scaleY = MathUtils.random(0.3f,  0.6f);
 		sprite.r = 255/255f;
 		sprite.g = 216/255f;
 		sprite.b = 0/255f;
 		sprite.a = 0.5f;
 		e.addComponent(sprite);
 		
 		// TODO this is a bit expensive, but not too bad
 		float radians = MathUtils.random(2 * MathUtils.PI);
 		float magnitude = MathUtils.random(400);
 		
 		Velocity velocity = new Velocity(magnitude * MathUtils.cos(radians), magnitude * MathUtils.sin(radians));
 		e.addComponent(velocity);
 		
 //		Velocity velocity = new Velocity(MathUtils.random(-400, 400), MathUtils.random(-400, 400));
 //		e.addComponent(velocity);
 		
 		Expires expires = new Expires(1.0f);
 		e.addComponent(expires);
 		
 		ColorAnimation colorAnimation = new ColorAnimation();
 		colorAnimation.alphaAnimate = true;
 		colorAnimation.alphaSpeed = -1.0f;
 		colorAnimation.alphaMin = 0.0f;
 		colorAnimation.alphaMax = 1.0f;
 		colorAnimation.repeat = false;
 		e.addComponent(colorAnimation);
 		
 		return e;
 	}
 	
 	public static Entity createExplosion(World world, float x, float y, float scale) {
 		Entity e = world.createEntity();
 		
 		Position position = new Position(x, y);
 		e.addComponent(position);
 		
 		Sprite sprite = new Sprite("explosion", Layer.PARTICLES);
 		sprite.scaleX = sprite.scaleY = scale;
 		sprite.r = 255/255f;
 		sprite.g = 216/255f;
 		sprite.b = 0/255f;
 		sprite.a = 0.5f;
 		e.addComponent(sprite);
 		
 		Expires expires = new Expires(0.5f);
 		e.addComponent(expires);
 		
 		ScaleAnimation scaleAnimation = new ScaleAnimation();
 		scaleAnimation.active = true;
 		scaleAnimation.max = scale;
 		scaleAnimation.min = scale/100f;
 		scaleAnimation.speed = -3.0f;
 		scaleAnimation.repeat = false;
 		e.addComponent(scaleAnimation);
 		
 		return e;
 	}
 	
 	public static Entity createStar(World world) {
 		Entity e = world.createEntity();
 		
 		Position position = new Position();
 		position.x = MathUtils.random(0, Constants.Game.FRAME_WIDTH);
 		position.y = MathUtils.random(0, Constants.Game.FRAME_HEIGHT);
 		e.addComponent(position);
 		
 		Sprite sprite = new Sprite("particle", Layer.BACKGROUND);
 		sprite.scaleX = sprite.scaleY = MathUtils.random(0.5f, 1.0f);
 		sprite.a = MathUtils.random(0.1f, 0.5f);
 		e.addComponent(sprite);
 		
 		Velocity velocity = new Velocity();
 		velocity.vy = MathUtils.random(-10f, -60f);
 		e.addComponent(velocity);
 		
 		ParallaxStar parallaxStar = new ParallaxStar();
 		e.addComponent(parallaxStar);
 		
 		ColorAnimation colorAnimation = new ColorAnimation();
 		colorAnimation.alphaAnimate = true;
 		colorAnimation.repeat = true;
 		colorAnimation.alphaSpeed = MathUtils.random(0.2f, 0.7f);
 		colorAnimation.alphaMin = 0.1f;
 		colorAnimation.alphaMax = 0.5f;
 		e.addComponent(colorAnimation);
 		
 		return e;
 	}
 }
