 package com.soc.core;
 
 import com.artemis.Entity;
 import com.badlogic.gdx.math.Vector2;
 import com.osc.game.benefits.Unmovable;
 import com.soc.ai.BallistaAI;
 import com.soc.ai.EyeballAI;
 import com.soc.ai.GaiaAirAI;
 import com.soc.ai.MaggotAI;
 import com.soc.ai.RedMonsterAI;
 import com.soc.ai.SatanAI;
 import com.soc.ai.SkeletonAI;
 import com.soc.ai.SlimeAI;
 import com.soc.ai.ZombiAI;
 import com.soc.core.Constants.Spells;
 import com.soc.core.Constants.World;
 import com.soc.game.attacks.ArrowProcessor;
 import com.soc.game.attacks.BiteProcessor;
 import com.soc.game.attacks.ChargeProcessor;
 import com.soc.game.attacks.DaggerThrowProcessor;
 import com.soc.game.attacks.FlameProcessor;
 import com.soc.game.attacks.HarmfulEnemyProcessor;
 import com.soc.game.attacks.IcicleProcessor;
 import com.soc.game.attacks.PoisonCloudProcessor;
 import com.soc.game.attacks.PunchProcessor;
 import com.soc.game.attacks.QuakeBladeProcessor;
 import com.soc.game.attacks.SlashProcessor;
 import com.soc.game.attacks.TornadoProcessor;
 import com.soc.game.attacks.VenomSwordProcessor;
 import com.soc.game.attacks.WhirlbladeProcessor;
 import com.soc.game.components.Attack;
 import com.soc.game.components.Bounds;
 import com.soc.game.components.Buff;
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
 	
 	public static Entity loadCharacter(Position pos, Stats st, String clazz, Player player){
 		Entity e = SoC.game.world.createEntity();
 		
 		Character animations = new Character();
 		e.addComponent(pos);
 		e.addComponent(st);
 	    e.addComponent(animations);
 		e.addComponent(new Velocity(0,0,Constants.Characters.VELOCITY));
 		e.addComponent(player);
 		e.addComponent(new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT));
 	    e.addComponent(new Feet(32, 10));
 	    e.addComponent(new State(0));
 
 	    if(clazz.equals(Constants.Characters.WARRIOR)){
 	    	GraphicsLoader.loadWarrior(animations);
 	    }
 	    
 		return e;
 	}
 
 	public static Entity createCharacter(float px, float py, int pz,float range, int damage, int type){
 		Entity e = SoC.game.world.createEntity();
 		Character animations = new Character();
 		
 	    e.addComponent(new Position(px,py, pz));
 	    e.addComponent(new Player());
 	    e.addComponent(new Velocity(0,0,Constants.Characters.VELOCITY));
 	    e.addComponent(new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT));
 	    e.addComponent(new Stats(100, 50, 0, 100, 100, 0, 1, 1, 1, 1, 1, Constants.Spells.SLASH, new int[]{Constants.Spells.DAGGER_THROW, Constants.Spells.CHARGE, Constants.Spells.WHIRLBLADE, Constants.Spells.QUAKEBLADE}));
 	    e.addComponent(new State(0));
 	    e.addComponent(new Feet(32, 10));
 	    e.addComponent(animations);
 	    
 	    if(type == Constants.Classes.HUNTER){
 	    } else if(type == Constants.Classes.WARRIOR){
 	    	GraphicsLoader.loadWarrior(animations);
 	    } else if(type == Constants.Classes.MAGE){
 	    	
 	    } else {
 	    	
 	    }
 	    
 	    return e;
 	}
 	
 	/*
 	 * ENEMIES
 	 */
 	
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
 	    e.addComponent(new Stats(10, 0, 0, 10, 0, 0, 1, 1, 1, 100, 1, Constants.Spells.PUNCH, new int[]{}));
 	    e.addComponent(new Enemy(600,10, new BallistaAI()));
 	    e.addComponent(new Feet(25, 25));
 		Buff.addbuff(e, new Unmovable());
 
 	    
 	    Character animations = new Character();
 	    GraphicsLoader.loadBallista(animations);
 	    e.addComponent(animations);
 	    
 	    return e;
 	}
 	
 	public static Entity createMaggot(float px, float py, int pz){
 		Entity e = SoC.game.world.createEntity();
 		
 		e.addComponent(new Position(px, py, pz));
 		e.addComponent(new Velocity(0,0,200));
 		e.addComponent(new Bounds(25, 25));
 		e.addComponent(new State(1));
 		e.addComponent(new Attack(new HarmfulEnemyProcessor(), 10));
 		e.addComponent(new Feet(25, 25));
 		e.addComponent(new Enemy(1000, 5, new MaggotAI()));
 		e.addComponent(new Stats(
 				1, 
 				0, 
 				0, 
 				0, 
 				0, 
 				0, 
 				1, 
 				0, 
 				0, 
 				0, 
 				0, 
 				0, 
 				null));
 		Character animations = new Character();
 		GraphicsLoader.loadMaggot(animations);
 		e.addComponent(animations);
 		
 		return e;
 	}
 	
 	public static Entity createSlime(float px, float py, int pz){
 		Entity e = SoC.game.world.createEntity();
 		
 		e.addComponent(new Position(px, py, pz));
 		e.addComponent(new Velocity(0,0,100));
 		e.addComponent(new Bounds(64, 64));
 		e.addComponent(new State(1));
 		e.addComponent(new Attack(new HarmfulEnemyProcessor(), 10));
 		e.addComponent(new Feet(30, 15));
 		e.addComponent(new Enemy(1000, 5, new SlimeAI()));
 		e.addComponent(new Stats(
 				1, 
 				0, 
 				0, 
 				0, 
 				0, 
 				0, 
 				1, 
 				0, 
 				0, 
 				0, 
 				0, 
 				0, 
 				null));
 		Character animations = new Character();
 		GraphicsLoader.loadSlime(animations);
 		e.addComponent(animations);
 		
 		return e;
 	}
 	
 	public static Entity createZombie(float px, float py, int pz){
 		Entity e = SoC.game.world.createEntity();
 		
 	    e.addComponent(new Position(px,py, pz));
 	    e.addComponent(new Velocity(0,0,100));
 	    e.addComponent(new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT));
 	    e.addComponent(new State(1));
 	    e.addComponent(new Stats(10, 0, 0, 10, 0, 0, 1, 1, 1, 1, 1, Constants.Spells.BITE, new int[]{}));
 	    e.addComponent(new Feet(32, 10));
 	    e.addComponent(new Enemy(600,10, new ZombiAI()));
 	    
 		Character animations = new Character();
 		GraphicsLoader.loadZombie(animations);
 		e.addComponent(animations);
 		
 		return e;
 	}
 	
 	public static Entity createSatan(float px, float py, int pz){
 		Entity e = SoC.game.world.createEntity();
 		e.addComponent(new Position(px, py, pz));
 		e.addComponent(new Velocity(0,0,100));
 		e.addComponent(new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT));
 		e.addComponent(new State(1));
 	    e.addComponent(new Stats(50, 0, 0, 50, 0, 0, 1, 1, 1, 1, 1, Constants.Spells.VENOMSWORD, new int[]{}));
 		e.addComponent(new Feet(32, 15));
 		e.addComponent(new Enemy(1000, 5, new SatanAI()));
 		Character animations = new Character();
 		GraphicsLoader.loadSatan(animations);
 		e.addComponent(animations);
 		
 		return e;
 	}
 	
 	public static Entity createGaiaAir(float px, float py, int pz){
 		Entity e = SoC.game.world.createEntity();
 		
 		e.addComponent(new Position(px, py, pz));
 		System.out.println(py);
 		System.out.println(px);
 		e.addComponent(new Velocity(0,0,0));
 		e.addComponent(new Bounds(32, 64));
 		e.addComponent(new Feet(32, 64));
 		e.addComponent(new State(0));
 		e.addComponent(new Enemy(0, 5, new GaiaAirAI()));
 		e.addComponent(new Stats(
 				100, 
 				0, 
 				0, 
 				100, 
 				0, 
 				0, 
 				1, 
 				0, 
 				0, 
 				0, 
 				0, 
 				0, 
 				null));
 		Buff.addbuff(e, new Unmovable());
 		Character animations = new Character();
 		GraphicsLoader.loadGaiaAir(animations);
 		e.addComponent(animations);
 		
 		return e;
 	}
 	
 	public static Entity createEyeball(float x, float y, int z) {
 		Entity e = SoC.game.world.createEntity();
 		
 		e.addComponent(new Position(x, y, z));
 		e.addComponent(new Velocity(0,0,0));
 		e.addComponent(new Bounds(32, 38));
 		e.addComponent(new Feet(32, 38));
 		e.addComponent(new Flying());
 		e.addComponent(new State(1));
 		e.addComponent(new Enemy(0, 5, new EyeballAI()));
 		e.addComponent(new Stats(
 				100, 
 				0, 
 				0, 
 				100, 
 				0, 
 				0, 
 				1, 
 				0, 
 				0, 
 				0, 
 				0, 
 				0, 
 				null));
 		Character animations = new Character();
 		GraphicsLoader.loadEyeball(animations);
 		e.addComponent(animations);
 	   	
 	   	return e;		
 	}
 	
 	public static Entity createRedMonster(float px, float py, int pz){
 		Entity e = SoC.game.world.createEntity();
 		
 		e.addComponent(new Position(px, py, pz));
 		System.out.println(py);
 		System.out.println(px);
 		e.addComponent(new Velocity(0,0,0));
 		e.addComponent(new Bounds(32, 64));
 		e.addComponent(new Feet(32, 64));
 		e.addComponent(new State(0));
 		e.addComponent(new Enemy(0, 5, new RedMonsterAI()));
 		e.addComponent(new Stats(
 				100, 
 				0, 
 				0, 
 				100, 
 				0, 
 				0, 
 				1, 
 				0, 
 				0, 
 				0, 
 				0, 
 				0, 
 				null));
 		Character animations = new Character();
 		GraphicsLoader.loadRedMonster(animations);
 		e.addComponent(animations);
 		
 		return e;
 	}
 	
 	
 	
 	/*
 	 * ATTACKS
 	 */
 		
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
 		int addX=0;
 		int addY=0;
 		if(Math.abs(pos.direction.x)>0){
 			addY=26;
 		}if(Math.abs(pos.direction.y)>0){
 			addX=26;	
 		}
 
 		e.addComponent( new Position(pos.x+addX,pos.y+addY, pos.z, pos.direction) );
 		e.addComponent( new Bounds(1,1));
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
 				
 		e.addComponent( new Position(pos.x + Constants.Characters.WIDTH * pos.direction.x, pos.y + Constants.Characters.HEIGHT * pos.direction.y, pos.z));
 		e.addComponent( new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT) );
 	   	e.addComponent( new Attack(new PunchProcessor(pos.direction, range), damage) );
 	   	
 	   	return e;
 	}
 	
 	public static Entity createBite(String group, Position pos, int damage, int range){
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Position(pos.x + Constants.Characters.WIDTH * pos.direction.x, pos.y + Constants.Characters.HEIGHT * pos.direction.y, pos.z));
 		e.addComponent( new Bounds(Constants.Characters.WIDTH, Constants.Characters.HEIGHT) );
 	   	e.addComponent( new Attack(new BiteProcessor(pos.direction, range), damage) );
 	   	
 	   	return e;
 	}
 	
 	public static Entity createVenomSword(String group, Position pos, int damage, int range){
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Position(pos.x + Constants.Characters.WIDTH * pos.direction.x, pos.y + Constants.Characters.HEIGHT * pos.direction.y, pos.z));
		e.addComponent( new Bounds(Constants.Characters.WIDTH*3, Constants.Characters.HEIGHT*3) );
 	   	e.addComponent( new Attack(new VenomSwordProcessor(pos.direction, range), damage) );
 	   	
 	   	return e;
 	}
 	public static Entity createCharge(Entity source, String group, Position pos, int damage){
 		Entity e=SoC.game.world.createEntity();
 				
 		e.addComponent( new Position(pos.x-Spells.CHARGE_BOX*0.5f, pos.y-Spells.CHARGE_BOX*0.5f, pos.z, pos.direction.cpy()) );
 		e.addComponent( new Bounds(Spells.CHARGE_BOX, Spells.CHARGE_BOX) );
 		e.addComponent( new Velocity(300*pos.direction.x, 300*pos.direction.y, Constants.Spells.CHARGE_SPEED) );
 	   	e.addComponent( new Attack(new ChargeProcessor(source, Constants.Spells.CHARGE_DURATION), damage) );
 	   	
 	   	return e;
 	}
 	
 	public static Entity createSpawner(float x, float y, int z, int width, int height, String type, int max, int range, float interval, boolean respawn){
 		Entity e = SoC.game.world.createEntity();
 		
 		e.addComponent( new Position(x, y, z) );
 		e.addComponent( new Bounds(width, height));
 		e.addComponent( new Spawner(type, max, range, interval,respawn));
 		
 		return e;
 	}
 
 
 	public static Entity createSlash(Position pos, int damage) {
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Velocity(0, 0, Constants.Spells.DAGGER_SPEED) );
 		float centerx = pos.x + Constants.Characters.WIDTH*0.5f;
 		float centery = pos.y + Constants.Characters.HEIGHT*0.5f;
 		if(pos.direction.x != 0){
 			e.addComponent( new Position(centerx, pos.y, pos.z, pos.direction.cpy()) );
 			e.addComponent( new Bounds((int) ((int) World.TILE_SIZE*Math.signum(pos.direction.x)*2.2f), (int) World.TILE_SIZE*2) );
 
 		} else {
 			e.addComponent( new Position(centerx-World.TILE_SIZE, centery, pos.z, pos.direction) );
 			e.addComponent( new Bounds((int)World.TILE_SIZE*3, (int) (World.TILE_SIZE*((pos.direction.y<0)?1.2f:1.6f)*Math.signum(pos.direction.y))));
 
 		}
 	   	e.addComponent( new Attack(new SlashProcessor(), damage) );
 	   	
 	   	return e;		
 	}
 	
 	public static Entity createWhirlblade(Position pos, Bounds bon,int damage) {
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Velocity(0, 0, Constants.Spells.DAGGER_SPEED) );
 		e.addComponent(new Position(pos.x, pos.y, pos.z));
 	   	e.addComponent( new Attack(new WhirlbladeProcessor(pos, bon), damage) );
 	   	
 	   	return e;		
 	}
 	
 	public static Entity createPoisonCloud(Position pos, Bounds bon) {
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Velocity(0, 0, Constants.Spells.DAGGER_SPEED) );
 		e.addComponent(new Position(pos.x+bon.width*0.5f, pos.y+bon.height+0.5f, pos.z));
 	   	e.addComponent( new Attack(new PoisonCloudProcessor(), 0) );
 	   	
 	   	return e;		
 	}
 	
 	public static Entity createQuake(Position pos, Feet feet, int damage) {
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Velocity(0, 0, 0) );
 		e.addComponent(new Position(pos.x+feet.width*0.5f, pos.y+feet.heigth+0.5f, pos.z, pos.direction.cpy()));
 	   	e.addComponent( new Attack(new QuakeBladeProcessor(), damage) );
 	   	
 	   	return e;		
 	}
 	
 	public static Entity createTornado(float x, float y, int z, Vector2 direction) {
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Velocity(Constants.Spells.TORNADO_SPEED*direction.x, Constants.Spells.TORNADO_SPEED*direction.y, 0) );
 		e.addComponent( new Position(x, y, z, direction));
 		e.addComponent( new Bounds(44, 32) );
 		e.addComponent( new Flying() );
 	   	e.addComponent( new Attack(new TornadoProcessor(), 0) );
 	   	
 	   	return e;		
 	}
 	
 	public static Entity createFlame(float x, float y, int z, Vector2 direction) {
 		Entity e=SoC.game.world.createEntity();
 		
 		e.addComponent( new Position(x, y, z, direction));
 		e.addComponent( new Bounds(32, 70) );
 	   	e.addComponent( new Attack(new FlameProcessor(), 0) );
 	   	
 	   	return e;		
 	}
 	
 }
