 package com.soc;
 
 import com.artemis.Entity;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.math.Vector2;
 import com.soc.components.Attacker;
 import com.soc.components.Bounds;
 import com.soc.components.Flying;
 import com.soc.components.Movement;
 import com.soc.components.Player;
 import com.soc.components.Position;
 import com.soc.components.State;
 import com.soc.components.Velocity;
 import com.soc.components.WeaponAttack;
 
 public class EntityFactory {
 	
 	public static EntityFactory instance;
 	private com.artemis.World entityWorld;
 	
 	private EntityFactory(com.artemis.World entityWorld){
 		this.entityWorld = entityWorld;
 	}
 	
 	public static EntityFactory initialize(com.artemis.World entityWorld){
 		instance = new EntityFactory(entityWorld);
 		return instance;
 	}
 	
 	public static EntityFactory getInstance(){
 		return instance;
 	}
 
 	public Entity createArcher(float px, float py, float range, int damage){
 		Entity e = entityWorld.createEntity();
 	    e.addComponent(new Position(px,py));
 	    e.addComponent(new Player());
 	    e.addComponent(new Velocity(0,0));
 	    e.addComponent(new Bounds(32, 32));
 	    e.addComponent(new State(0,0));
 	    
 	    Movement movement = new Movement();
 	    
 	   	AnimationLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/archer-walk.png")), movement, 1.0f, 64, 64);
 	   	e.addComponent(movement);
 	    
 	   	Attacker attack = new Attacker(range, damage);
 	   	AnimationLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/archer-attack.png")), attack, 0.4f, 64, 64);
 	   	e.addComponent(attack);
 	   	
 	    e.addToWorld();
 	    
 	    
 	    return e;
 	}
 	
 	public Entity createWarrior(float px, float py, int damage, float range){
 		Entity e = entityWorld.createEntity();
 	    e.addComponent(new Position(px,py));
 	    e.addComponent(new Player());
 	    e.addComponent(new Velocity(0,0));
 	    e.addComponent(new Bounds(32, 32));
 	    e.addComponent(new State(0,0));
 	    
 	    Movement movement = new Movement();
 	    
 	   	AnimationLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/warrior-walk.png")), movement, 1.0f, 64, 64);
 	   	e.addComponent(movement);
 	    
 	   	Attacker attack = new Attacker(range,damage);
 	   	AnimationLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/warrior-attack.png")), attack, 0.4f, 128, 128);
 	   	e.addComponent(attack);
 	   	
 	    e.addToWorld();
 	    
 	    
 	    return e;
 	}
 	
 	public Entity createMage(float px, float py, int damage, float range){
 		Entity e = entityWorld.createEntity();
 	    e.addComponent(new Position(px,py));
 	    e.addComponent(new Player());
 	    e.addComponent(new Velocity(0,0));
 	    e.addComponent(new Bounds(32, 32));
 	    e.addComponent(new State(0,0));
 	    
 	    Movement movement = new Movement();
 	    
 	   	AnimationLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/mage-walk.png")), movement, 1.0f, 64, 64);
 	   	e.addComponent(movement);
 	    
 	   	Attacker attack = new Attacker(range,damage);
	   	AnimationLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/mage-attack.png")), attack, 0.4f, 128, 128);
 	   	e.addComponent(attack);
 	   	
 	    e.addToWorld();
 	    
 	    
 	    return e;
 	}
 	
 	
 	public Entity createAttack(float x, float y, int attackType, int damage, float range, Vector2 dir){
 		Entity e=entityWorld.createEntity();
 		WeaponAttack weaponAttack=new WeaponAttack(range,damage);
 		e.addComponent(weaponAttack);
 		Position position=new Position(x,y);
 		e.addComponent(position);
 		Velocity v=new Velocity(Constants.Attacks.DAGGER_SPEED*dir.x, Constants.Attacks.DAGGER_SPEED*dir.y);
 		e.addComponent(v);
 		Bounds b=new Bounds(10,10);
 		e.addComponent(b); 
 	   	AnimationLoader.loadProjectileSpriteSheet(new Texture(Gdx.files.internal("resources/dagger-attack.png")), weaponAttack, 0.2f, 64, 64);		
 	   	e.addComponent(new Flying());
 	   	e.addToWorld();
 	   	return e;
 	}
 	
 
 }
