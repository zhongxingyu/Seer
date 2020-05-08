 package com.jumpandrun;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.WorldManifold;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.utils.Array;
 
 public class GameInstance {
 
 	final static float MAX_VELOCITY = 20f;	
 	final static float GRAVITY = -160f;
 	
 	public static GameInstance instance;
 	
 	Map map;
 	
 	public Array<MovingPlatform> platforms = new Array<MovingPlatform>();
 	public Array<Block> blocks = new Array<Block>();
 	public Array<Enemy> enemies = new Array<Enemy>();
 	public Array<Bullet> bullets = new Array<Bullet>();
 	public World world  = new World(new Vector2(0, GRAVITY), true);
 	public Player player;	
 
 	float stillTime = 0;
 	long lastGroundTime = 0;
 
 	MovingPlatform groundedPlatform = null;
 	
 	Body box;
 	Block block;
 
 	Fixture playerPhysicsFixture;
 	Fixture playerSensorFixture;
 	
 	public static GameInstance getInstance() {
 		if (instance == null) {
 			instance = new GameInstance();
 		}
 		return instance;
 	}
 	
 	public void resetGame() {
 		platforms.clear();
 		blocks.clear();
 		enemies.clear();
 		bullets.clear();
 		world.dispose();
 		
 		
 		
 		world  = new World(new Vector2(0, GRAVITY), true);
 		map = new Map();
 	}
 	
 	public Body createBox(BodyType type, float width, float height, float density) {
 		BodyDef def = new BodyDef();
 		def.type = type;
 		Body box = GameInstance.getInstance().world.createBody(def);
  
 		PolygonShape poly = new PolygonShape();
 		poly.setAsBox(width, height);
 		box.createFixture(poly, density);
 		poly.dispose();
  
 		return box;
 	}	
  
 	public Body createEdge(BodyType type, float x1, float y1, float x2, float y2, float density) {
 		BodyDef def = new BodyDef();
 		def.type = type;
 		Body box = world.createBody(def);
 		PolygonShape poly = new PolygonShape();		
 		poly.setAsEdge(new Vector2(0, 0), new Vector2(x2 - x1, y2 - y1));
 		box.createFixture(poly, density);
 		box.setTransform(x1, y1, 0);
 		poly.dispose();
  
 		return box;
 	}
  
 	public Body createCircle(BodyType type, float radius, float density) {
 		BodyDef def = new BodyDef();
 		def.type = type;
 		Body box = world.createBody(def);
  
 		CircleShape poly = new CircleShape();
 		poly.setRadius(radius);
 		box.createFixture(poly, density);
 		poly.dispose();
  
 		return box;
 	}	
 	
 	public void addEnemySpawner(float x, float y) {
 		EnemySpawner enemySpawner = new EnemySpawner(x, y);
 		box = createBox(BodyType.StaticBody, 1, 1, 3);
 		box.setTransform(enemySpawner.position.x , enemySpawner.position.y, 0);
 		
 		enemySpawner.body = box;
 		blocks.add(enemySpawner);
 		enemySpawner.body.setUserData(enemySpawner);		
 	}
 	
 	public void addJumpBlock(float x, float y) {
 		JumpBlock block = new JumpBlock(x,y);
 		box = createBox(BodyType.StaticBody, 1, 1, 3);
 		box.setTransform(block.position.x , block.position.y, 0);
 		
 		block.body = box;
 		blocks.add(block);
 		block.body.setUserData(block);
 	}
 	
 	public void addBlock(float x, float y) {
 		Block block = new Block(x,y);
 		box = createBox(BodyType.StaticBody, 1, 1, 3);
 		box.setTransform(block.position.x , block.position.y, 0);
 		
 		block.body = box;
 		block.body.setUserData(block);
 		blocks.add(block);
 	}
 	
 	public void addEnemy() {
 		for(Block block:blocks) {
 			if(block instanceof EnemySpawner) {
 				Enemy enemy = new Enemy(block.position.x, block.position.y-1.5f);				
 				if(Math.random() >= 0.5) {
 					enemy.direction.x = -enemy.direction.x;
 				}
 				Body box = createCircle(BodyType.DynamicBody, 1,1);		
 				box.setBullet(true);		 
 				box.setTransform(block.position.x, block.position.y-1.5f, 0);
 				
 				enemy.body = box;
 				box.setFixedRotation(true);
 				enemy.body.setUserData(enemy);
 				enemies.add(enemy);
 			}
 		
 		}
 	}
 	
 	public void addBullet() {
 		if(player.lastshot < player.shotlimit)
 			return;
 		player.lastshot = 0;
 		Bullet b = new Bullet(player.position.x, player.position.y-1.5f);
 		
		Body box = createBox(BodyType.DynamicBody, 0.2f, 0.2f, 1);
 		
 		box.setBullet(true);		 
 		box.setTransform(player.position.x+player.xdir, player.position.y, 0);
 		Vector2 dir = new Vector2(player.xdir, 0);
 		dir = dir.nor();
 		dir.mul(80);
 		box.setLinearVelocity(dir);
 		
 //		enemy.enemyPhysicsFixture = enemyPhysicsFixture;
 		b.body = box;
 		b.body.setUserData(b);
 		bullets.add(b);
 	}
  
 	public void createPlayer(float x, float y) {
 		BodyDef def = new BodyDef();
 		def.type = BodyType.DynamicBody;
 		Body box = world.createBody(def);
 		 
 		PolygonShape poly = new PolygonShape();		
 		poly.setAsBox(0.1f, 0.1f);
 		playerPhysicsFixture = box.createFixture(poly, 0);
 		poly.dispose();			
  
 		CircleShape circle = new CircleShape();		
 		circle.setRadius(1f);
 		circle.setPosition(new Vector2(0, -0.6f));
 		playerSensorFixture = box.createFixture(circle, 1);		
 		circle.dispose();		
  
 		box.setBullet(true);
  
 		box.setFixedRotation(true);
 		box.setTransform(x, y, 0);
 		
 		player = new Player(x, y);
 		player.body = box;
 		player.body.setUserData(player);
 	}
 	
 	public boolean isPlayerGrounded() {				
 		groundedPlatform = null;
 		List<Contact> contactList = world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching() && (contact.getFixtureA() == playerSensorFixture ||
 			   contact.getFixtureB() == playerSensorFixture)) {				
  
 				Vector2 pos = player.position.tmp();
 				WorldManifold manifold = contact.getWorldManifold();
 				boolean below = true;
 				for(int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
 					below &= (manifold.getPoints()[j].y < pos.y - 1.5f);
 				}
  
 				if(below) {
 					if(contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals("p")) {
 						groundedPlatform = (MovingPlatform)contact.getFixtureA().getBody().getUserData();							
 					}
  
 					if(contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals("p")) {
 						groundedPlatform = (MovingPlatform)contact.getFixtureB().getBody().getUserData();
 					}											
 					return true;			
 				}
  
 				return false;
 			}
 		}
 		return false;
 	}
 	public void flagBullets() {				
 		//List<Contact> contactList = world.getContactList();
 		/*Iterator<Body> it = world.getBodies();
 		while(it.hasNext()) {
 			Body b = it.next();
 			if(b.getUserData() instanceof Bullet) {
 				if(((Bullet)b.getUserData()).age > 100) {
 					((Bullet)b.getUserData()).kill = true;
 					
 				}
 			}
 		}*/
 		
 		List<Contact> contactList = world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching()) {
 				Object a = contact.getFixtureA().getBody().getUserData();
 				Object b = contact.getFixtureB().getBody().getUserData();
 				if(a instanceof Bullet  && b instanceof Enemy) {
 					((Bullet)a).kill = true;
 					((Enemy)b).health-= ((Bullet)a).damage;
 				} else if(b instanceof Bullet && a instanceof Enemy) {
 					((Bullet)b).kill = true;
 					((Enemy)a).health-= ((Bullet)b).damage;
 				}
 				if(a instanceof Bullet && !(b instanceof Bullet) && !(b instanceof Player)) {
 					((Bullet)a).kill = true;
 				} else if(b instanceof Bullet && !(a instanceof Bullet) && !(a instanceof Player)) {
 					((Bullet)b).kill = true;
 				}
 			}
 		}
 
 	}
 	
 	public void removeKilled() {
 		//Bullets
 		boolean found = false;
 		do {
 			found = false;
 			for(int e = 0; e < bullets.size; e++) {
 				if(bullets.get(e).kill) {
 					found = true;
 					world.destroyBody(bullets.get(e).body);
 					bullets.removeIndex(e);
 					break;
 				}
 			}
 		}while(found);
 		
 		//Enemies
 		found = false;
 		do {
 			found = false;
 			for(int e = 0; e < enemies.size; e++) {
 				if(enemies.get(e).kill || enemies.get(e).health <= 0) {
 					found = true;
 					world.destroyBody(enemies.get(e).body);
 					enemies.removeIndex(e);
 					break;
 				}
 			}
 		}while(found);
 	}
 	
 	public void physicStuff(float delta) {
 		Vector2 vel = player.body.getLinearVelocity();
 		Vector2 pos = player.position.tmp();		
 		boolean grounded = isPlayerGrounded();
 		/*if(grounded) {
 			lastGroundTime = System.nanoTime();
 		} else {
 			if(System.nanoTime() - lastGroundTime < 100000000) {
 				grounded = true;
 			}
 		}*/
  
 		// cap max velocity on x		
 		if(Math.abs(vel.x) > MAX_VELOCITY) {			
 			vel.x = Math.signum(vel.x) * MAX_VELOCITY;
 			player.body.setLinearVelocity(vel.x, vel.y);
 		}
 		
 		if(Gdx.input.isKeyPressed(Keys.SPACE)) {
 			addBullet();
 		}
 		
 		// calculate stilltime & damp
 		if(!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D)) {			
 			stillTime += Gdx.graphics.getDeltaTime();
 			player.body.setLinearVelocity(vel.x * 0.9f, vel.y);
 		}
 		else { 
 			stillTime = 0;
 		}			
  
 		// disable friction while jumping
 		if(!grounded) {			
 			playerPhysicsFixture.setFriction(0f);
 			playerSensorFixture.setFriction(0f);			
 		} else {
 			/*if(!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D) && stillTime > 0.2) {
 				playerPhysicsFixture.setFriction(100f);
 				playerSensorFixture.setFriction(100f);
 			}
 			else {
 				playerPhysicsFixture.setFriction(0.2f);
 				playerSensorFixture.setFriction(0.2f);
 			}*/
  
 			if(groundedPlatform != null && groundedPlatform.dist == 0) {
 				player.body.applyLinearImpulse(0, -20, pos.x, pos.y);				
 			}
 		}		
  
 		// apply left impulse, but only if max velocity is not reached yet
 		if(Gdx.input.isKeyPressed(Keys.A) && vel.x > -MAX_VELOCITY) {
 			player.body.applyLinearImpulse(-3.4f, 0, pos.x, pos.y);
 			player.xdir = -1;
 		}
  
 		// apply right impulse, but only if max velocity is not reached yet
 		if(Gdx.input.isKeyPressed(Keys.D) && vel.x < MAX_VELOCITY) {
 			player.body.applyLinearImpulse(3.4f, 0, pos.x, pos.y);
 			player.xdir = 1;
 		}
  
 		// jump, but only when grounded
 		if(player.jump) {			
 			
 			//player.jump = false;
 			if(grounded) {
 				player.body.setLinearVelocity(vel.x, 0);			
 				player.body.setTransform(pos.x, pos.y + 0.01f, 0);
 				player.body.applyLinearImpulse(0, 80, pos.x, pos.y);	
 				lastGroundTime = System.nanoTime();
 			} else if(System.nanoTime() - lastGroundTime < 150000000) {
 				player.body.applyLinearImpulse(0, 710*delta, pos.x, pos.y);
 				//player.body.setLinearVelocity(vel.x, vel.y+1*(100000000-(System.nanoTime() - lastGroundTime))/100000000f);
 			}
 		}				
  
 		// update platforms
 		for(int i = 0; i < platforms.size; i++) {
 			MovingPlatform platform = platforms.get(i);
 			platform.update(Math.max(1/30.0f, Gdx.graphics.getDeltaTime()));
 		}
 		
 		//update jump plattforms
 		for(int i = 0; i < blocks.size; i++) {
 			Block block = blocks.get(i);
 			if(block instanceof JumpBlock) {
 				if(((JumpBlock) block).jumpAnim > 0) {
 					((JumpBlock) block).jumpAnim = Math.max(0, ((JumpBlock) block).jumpAnim - Gdx.graphics.getDeltaTime());
 				} 
 			}			
 		}
 		
 		//update enemies
 		for(int i = 0; i < enemies.size; i++) {
 			Enemy enemy = enemies.get(i);
 			enemy.move();
 			enemy.update();
 			enemy.body.setAwake(true);
 		}
 		//update bullets
 		for(int i = 0; i < bullets.size; i++) {
 			Bullet bullet = bullets.get(i);
			bullet.update(delta);	
 			bullet.body.setAwake(true);
 		}
 		flagBullets();
 		removeKilled();
 		
 		player.lastshot+= delta;
  
 		player.update();
 		player.body.setAwake(true);
 		
 		//check player/enemy collision
 		checkPlayerEnemyCollision();
 		
 		if(player.alive == false) {
 			resetGame();
 		}
 		
 		// le step...			
 		world.step(delta, 50, 50);		
 	}
 	
 	public void activateJumpBlocks() {
 		List<Contact> contactList = world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching()) {
 				if(contact.getFixtureA().getBody().getUserData() instanceof JumpBlock) {
 					if(contact.getFixtureA().getBody().getPosition().y < contact.getFixtureB().getBody().getPosition().y-1) {
 						contact.getFixtureB().getBody().applyLinearImpulse(0, 85, player.position.x, player.position.y);
 	//					((JumpBlock)contact.getFixtureA().getBody().getUserData()).jump();
 					}
 				}
 				if(contact.getFixtureB().getBody().getUserData() instanceof JumpBlock ) {
 					if(contact.getFixtureA().getBody().getPosition().y-1 > contact.getFixtureB().getBody().getPosition().y) {
 					contact.getFixtureA().getBody().applyLinearImpulse(0, 85, player.position.x, player.position.y);
 //					((JumpBlock)contact.getFixtureB().getBody().getUserData()).jump();
 					}
 				}
 			}
 		}
 		
 		for(int i = 0; i < blocks.size; i++) {
 			Block block = blocks.get(i);
 			if(block instanceof JumpBlock) {
 				((JumpBlock) block).jump();
 			}			
 		}
 	}
 	
 	
 	public void checkPlayerEnemyCollision() {
 		List<Contact> contactList = world.getContactList();
 		for(int i = 0; i < contactList.size(); i++) {
 			Contact contact = contactList.get(i);
 			if(contact.isTouching()) {
 				if(contact.getFixtureA().getBody().getUserData() instanceof Player  && contact.getFixtureB().getBody().getUserData() instanceof Enemy) {
 					player.alive = false;
 				}
 				if(contact.getFixtureB().getBody().getUserData() instanceof Player && contact.getFixtureA().getBody().getUserData() instanceof Enemy) {
 					player.alive = false;
 				}
 			}
 		}
 		
 	}
 }
