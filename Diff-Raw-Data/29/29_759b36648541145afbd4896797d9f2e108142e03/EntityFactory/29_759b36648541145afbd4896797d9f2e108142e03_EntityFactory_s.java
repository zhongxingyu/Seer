 package com.soc.core;
 
 import com.artemis.Entity;
 import com.badlogic.gdx.math.Vector2;
 import com.soc.ai.BallistaAI;
 import com.soc.ai.SkeletonAI;
 import com.soc.core.Constants.Spells;
 import com.soc.core.Constants.World;
 import com.soc.game.attacks.ArrowProcessor;
 import com.soc.game.attacks.ChargeProcessor;
 import com.soc.game.attacks.DaggerThrowProcessor;
 import com.soc.game.attacks.IcicleProcessor;
 import com.soc.game.attacks.PunchProcessor;
 import com.soc.game.attacks.SlashProcessor;
 import com.soc.game.components.Attack;
 import com.soc.game.components.Bounds;
 import com.soc.game.components.Character;
 import com.soc.game.components.Enemy;
 import com.soc.game.components.Feet;
 import com.soc.game.components.Flying;
 import com.soc.game.components.Player;
 import com.soc.game.components.Position;
 import com.soc.game.components.Spawner;
 import com.soc.game.components.State;
 import com.soc.game.components.Stats;
 import com.soc.game.components.Velocity;
 import com.soc.utils.GraphicsLoader;
 
 
 public class EntityFactory {
 	
 
 	public static Entity createCharacter(float px, float py, int pz,float range, int damage, int type){
 		Entity e = SoC.game.world.createEntity();
 		Character animations = new Character();
 		
 	    e.addComponent(new Position(px,py, pz));
 	    e.addComponent(new Player());
 	    e.addComponent(new Velocity(0,0,Constants.Characters.VELOCITY));
 	    e.addComponent(new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT));
 	    e.addComponent(new Stats(100, 50, 0, 100, 100, 0, 1, 1, 1, 1, 1, Constants.Spells.SLASH, new int[]{Constants.Spells.DAGGER_THROW, Constants.Spells.CHARGE}));
 	    e.addComponent(new State(0));
 	    e.addComponent(new Feet(32, 10));
 	    e.addComponent(animations);
 	    
 	    if(type == Constants.Classes.HUNTER){
 //	    	animations.idle = GraphicsLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/archer-walk.png")), 1.0f, 64, 64, false);
 //		    animations.movement = GraphicsLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/archer-walk.png")), 1.0f, 64, 64, true);
 //		    animations.attack = GraphicsLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/archer-attack.png")), 0.4f, 64, 64, false);
 	    } else if(type == Constants.Classes.WARRIOR){
 	    	GraphicsLoader.loadWarrior(animations);
 	    } else if(type == Constants.Classes.MAGE){
 //	    	animations.idle = GraphicsLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/mage-walk.png")), 1.0f, 64, 64, false);
 //	 	    animations.movement = GraphicsLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/mage-walk.png")), 1.0f, 64, 64, true);
 //	 	    animations.attack = GraphicsLoader.loadCharacterSpriteSheet(new Texture(Gdx.files.internal("resources/mage-attack.png")), 0.4f, 64, 64, false);
 	    } else {
 	    	
 	    }
 	    
 	    return e;
 	}
 	
 	
 	public static Entity createSkeleton(float px, float py, int pz,int damage){
 		Entity e = SoC.game.world.createEntity();
 	    e.addComponent(new Position(px,py, pz));
 	    e.addComponent(new Velocity(0,0,100));
 	    e.addComponent(new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT));
 	    e.addComponent(new State(1));
 	    e.addComponent(new Stats(10, 0, 0, 10, 0, 0, 1, 1, 1, 1, 1, Constants.Spells.PUNCH, new int[]{}));
 	    e.addComponent(new Feet(32, 10));
 	    e.addComponent(new Enemy(600,10, new SkeletonAI()));
 	    
 	    Character animations = new Character();
 	    GraphicsLoader.loadSkeleton(animations);
 	    e.addComponent(animations);
 	    
 	    return e;
 	}
 	
 	public static Entity createBallista(float px, float py, int pz, int damage){
 		Entity e = SoC.game.world.createEntity();
 	    e.addComponent(new Position(px,py, pz));
 	    e.addComponent(new Velocity(0,0,0));
 	    e.addComponent(new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT));
 	    e.addComponent(new State(0));
 	    e.addComponent(new Stats(10, 0, 0, 10, 0, 0, 1, 1, 1, 1, 1, Constants.Spells.PUNCH, new int[]{}));
 	    e.addComponent(new Enemy(600,10, new BallistaAI()));
 	    e.addComponent(new Feet(25, 25));
 	    
 	    Character animations = new Character();
 	    GraphicsLoader.loadBallista(animations);
 	    e.addComponent(animations);
 	    
 	    return e;
 	}
 		
 	public static Entity createDaggerThrow(String group, Position pos, int damage, Vector2 dir){
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Position(pos.x,pos.y, pos.z) );
 		e.addComponent( new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT) );
 		e.addComponent( new Velocity(Constants.Spells.DAGGER_SPEED*dir.x, Constants.Spells.DAGGER_SPEED*dir.y,900) );
 	   	e.addComponent( new Flying() );
 	   	e.addComponent( new Attack(new DaggerThrowProcessor(pos), damage) );
 	   	
 	   	return e;
 	}
 	
 	public static Entity createArrow(String group, Position pos, int damage, Vector2 dir){
 		Entity e=SoC.game.world.createEntity();
 		
		e.addComponent( new Position(pos.x,pos.y, pos.z) );
 		e.addComponent( new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT) );
 		e.addComponent( new Velocity(Constants.Spells.ARROW_SPEED*dir.x, Constants.Spells.ARROW_SPEED*dir.y,Constants.Spells.ARROW_SPEED) );
 	   	e.addComponent( new Flying() );
 	   	e.addComponent( new Attack(new ArrowProcessor(), damage) );
 	   	
 	   	return e;
 	}
 	public static Entity createIcicle(String group ,Position pos, int damage, int range, Vector2 dir){
 		Entity e=SoC.game.world.createEntity();
 				
 		e.addComponent( new Position(pos.x,pos.y, pos.z) );
 		e.addComponent( new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT) );
 		e.addComponent( new Velocity(300*dir.x, 300*dir.y, Constants.Spells.DAGGER_SPEED) );
 	   	e.addComponent( new Flying());
 	   	e.addComponent( new Attack(new IcicleProcessor(dir, range), damage ) );
 	   	
 	   	return e;
 	}
 	
 	public static Entity createFireball(String group, Position pos, int damage, int range, Vector2 dir){
 		Entity e=SoC.game.world.createEntity();
 				
 		e.addComponent( new Position(pos.x, pos.y, pos.z) );
 		e.addComponent( new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT) );
 		e.addComponent( new Velocity(300*dir.x, 300*dir.y, Constants.Spells.DAGGER_SPEED) );
 	   	e.addComponent( new Flying());
 	   	e.addComponent( new Attack(new IcicleProcessor(dir, range), damage) );
 	   	
 	   	return e;
 	}
 	
 	public static Entity createPunch(String group, Position pos, int damage, int range){
 		Entity e=SoC.game.world.createEntity();
 				
 		e.addComponent( new Position(pos.x, pos.y, pos.z) );
 		e.addComponent( new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT) );
 		e.addComponent( new Velocity(0, 0, Constants.Spells.DAGGER_SPEED) );
 	   	e.addComponent( new Attack(new PunchProcessor(pos.direction, range), damage) );
 	   	
 	   	return e;
 	}
 	
 	public static Entity createCharge(Entity source, String group, Position pos, int damage){
 		Entity e=SoC.game.world.createEntity();
 				
 		e.addComponent( new Position(pos.x-Spells.CHARGE_BOX*0.5f, pos.y-Spells.CHARGE_BOX*0.5f, pos.z, pos.direction) );
 		e.addComponent( new Bounds(Spells.CHARGE_BOX, Spells.CHARGE_BOX) );
 		e.addComponent( new Velocity(300*pos.direction.x, 300*pos.direction.y, Constants.Spells.CHARGE_SPEED) );
 	   	e.addComponent( new Attack(new ChargeProcessor(source, Constants.Spells.CHARGE_DURATION), damage) );
 	   	
 	   	return e;
 	}
 	
 	public static Entity createSpawner(float x, float y, int z, int width, int height, String type, int max, int range, float interval,boolean respawn){
 		Entity e = SoC.game.world.createEntity();
 		
 		e.addComponent( new Position(x, y, z) );
 		e.addComponent( new Bounds(width, height));
 		e.addComponent( new Spawner(type, max, range, interval,respawn));
 		
 		return e;
 	}
 
 
 	public static Entity createSlash(String group, Position pos, int damage) {
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Velocity(0, 0, Constants.Spells.DAGGER_SPEED) );
 		float centerx = pos.x + Constants.Characters.WIDTH*0.5f;
 		float centery = pos.y + Constants.Characters.HEIGHT*0.5f;
 		if(pos.direction.x != 0){
 			e.addComponent( new Position(centerx, pos.y, pos.z, pos.direction) );
 			e.addComponent( new Bounds((int) ((int) World.TILE_SIZE*Math.signum(pos.direction.x)*2.2f), (int) World.TILE_SIZE*2) );
 
 		} else {
 			e.addComponent( new Position(centerx-World.TILE_SIZE, centery, pos.z, pos.direction) );
 			e.addComponent( new Bounds((int)World.TILE_SIZE*3, (int) (World.TILE_SIZE*((pos.direction.y<0)?1.2f:1.6f)*Math.signum(pos.direction.y))));
 
 		}
 	   	e.addComponent( new Attack(new SlashProcessor(), damage) );
 	   	
 	   	return e;		
 	}
 }
