 package com.soc.ai.bosses;
 
 import java.util.Random;
 import java.util.Vector;
 
 import com.artemis.Entity;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.math.Vector2;
 import com.soc.ai.AI;
 import com.soc.core.Constants;
 import com.soc.core.Constants.World;
 import com.soc.core.EntityFactory;
 import com.soc.core.SoC;
 import com.soc.game.components.Buff;
 import com.soc.game.components.Position;
 import com.soc.game.components.State;
 import com.soc.game.components.Stats;
 import com.soc.game.states.benefits.Casting;
 import com.soc.game.states.benefits.Inmune;
 import com.soc.game.states.benefits.Teleport;
 import com.soc.screens.CreditsScreen;
 
 public class CydoniaAI extends AI{
 	
 	public int leftmostTile = 27;
 	public int leftTile = 35;
 	public int rightTile = 65;
 	public int rightmostTile = 72;
 	public int topTile = 179;
 	public int bottomTile = 156;
 	public int counter = 0;
 	public float timer;
 	public float x, y;
 	public boolean init, teleported, standard, waving, horizontal, horizontalWave;
 	public float phasesLife;
 	public int state;
 	
 	public static final int STANDARD = 0;
 	public static final int CASTING = 1;
 	public static final int WAVING = 2;
 	
 	public Vector2[] windblades;
 	public int waveCounter;
 	
 	public CydoniaAI(){
 		standard = true;
 		init = false;
 		teleported = false;
 		timer=5f;
 		waving=false;
 		windblades = new Vector2[]{
 				new Vector2(leftmostTile, topTile),
 				new Vector2(leftmostTile, bottomTile),
 				new Vector2(rightmostTile, topTile),
 				new Vector2(rightmostTile, bottomTile),
 		};
 		phasesLife=0.75f;
 
 	}
 
 
 
 
 	public void init(Entity e){
 		Position pos = SoC.game.positionmapper.get(SoC.game.player);
 		if(pos.x * World.TILE_FACTOR > 47 && pos.x * World.TILE_FACTOR < 52 && pos.y *World.TILE_FACTOR > 185){
 			state = STANDARD;
 			init = true;
 			Buff.addbuff(SoC.game.player, new Teleport(50*World.TILE_SIZE, 165*World.TILE_SIZE, 0));
 			EntityFactory.createWall(e, 46, topTile, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile, 0).addToWorld();
 			EntityFactory.createWall(e, 48, topTile, 0).addToWorld();
 			EntityFactory.createWall(e, 49, topTile, 0).addToWorld();
 			EntityFactory.createWall(e, 50, topTile, 0).addToWorld();
 			EntityFactory.createWall(e, 51, topTile, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile, 0).addToWorld();
 			EntityFactory.createWall(e, 53, topTile, 0).addToWorld();
 			
 			EntityFactory.createWall(e, 46, topTile+1, 0).addToWorld();
 			EntityFactory.createWall(e, 46, topTile+2, 0).addToWorld();
 			EntityFactory.createWall(e, 46, topTile+3, 0).addToWorld();
 			EntityFactory.createWall(e, 46, topTile+4, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile+5, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile+6, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile+7, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile+8, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile+9, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile+10, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile+11, 0).addToWorld();
 			EntityFactory.createWall(e, 47, topTile+12, 0).addToWorld();
 			
 			EntityFactory.createWall(e, 53, topTile, 0).addToWorld();
 			EntityFactory.createWall(e, 53, topTile+1, 0).addToWorld();
 			EntityFactory.createWall(e, 53, topTile+2, 0).addToWorld();
 			EntityFactory.createWall(e, 53, topTile+3, 0).addToWorld();
 			EntityFactory.createWall(e, 53, topTile+4, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+5, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+6, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+7, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+8, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+9, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+10, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+11, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+12, 0).addToWorld();
 			
 			EntityFactory.createWall(e, 47, topTile+13, 0).addToWorld();
 			EntityFactory.createWall(e, 48, topTile+13, 0).addToWorld();
 			EntityFactory.createWall(e, 49, topTile+13, 0).addToWorld();
 			EntityFactory.createWall(e, 50, topTile+13, 0).addToWorld();
 			EntityFactory.createWall(e, 51, topTile+13, 0).addToWorld();
 			EntityFactory.createWall(e, 52, topTile+13, 0).addToWorld();
 			
 			EntityFactory.createWall(e, 41, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 42, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 43, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 44, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 45, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 46, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 47, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 48, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 49, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 50, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 51, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 52, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 53, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 54, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 55, bottomTile, 0).addToWorld();			
 			EntityFactory.createWall(e, 50, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 56, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 57, bottomTile, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile, 0).addToWorld();
 			
 			EntityFactory.createWall(e, 41, bottomTile-1, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-2, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-3, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-4, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-5, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-6, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-7, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-8, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-9, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-10, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-11, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-12, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-13, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-14, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-15, 0).addToWorld();
 			EntityFactory.createWall(e, 41, bottomTile-16, 0).addToWorld();
 			
 			EntityFactory.createWall(e, 58, bottomTile-1, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-2, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-3, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-4, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-5, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-6, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-7, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-8, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-9, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-10, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-11, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-12, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-13, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-14, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-15, 0).addToWorld();
 			EntityFactory.createWall(e, 58, bottomTile-16, 0).addToWorld();
 
 			SoC.game.musicmanager.play(e, "steeps-of-destiny.ogg");
 		}
 	}
 	
 	public void wave(Entity e, Vector2 direction){
 		Position p=SoC.game.positionmapper.get(e);
 		Entity fireStone=null;
 		int holeWave=0;
 		if(direction.x==0 && direction.y==-1){
 			holeWave=leftmostTile+6+AI.rng.nextInt((rightmostTile-leftmostTile)-12)+1;
 			for(int i=this.leftmostTile;i<rightmostTile;i=i+2){
 				if(i==holeWave || holeWave-1==i  ||holeWave+1==i) {
 					i+=2;
 					continue;
 				}
 			fireStone = EntityFactory.createFireStone(i*Constants.World.TILE_SIZE, (topTile-2)*Constants.World.TILE_SIZE, p.z,direction,true);
 		    SoC.game.groupmanager.add(fireStone, Constants.Groups.ENEMY_ATTACKS);
 		    SoC.game.groupmanager.add(fireStone, Constants.Groups.MAP_BOUND);
 		    SoC.game.groupmanager.add(fireStone, Constants.Groups.PROJECTILES);
 		    SoC.game.levelmanager.setLevel(fireStone, Constants.Groups.LEVEL+p.z);
 		    fireStone.addToWorld();
 			}
 		}else if(direction.x==-1 && direction.y==0){
 			holeWave=bottomTile+6+AI.rng.nextInt(topTile-bottomTile-12)+1;
 			for(int i=this.bottomTile;i<=(topTile);i++){
 				if(i==holeWave) {
 					i+=3;
 					continue;
 				}
 			fireStone = EntityFactory.createFireStone(rightmostTile*Constants.World.TILE_SIZE, i*Constants.World.TILE_SIZE, p.z,direction,true);
 		    SoC.game.groupmanager.add(fireStone, Constants.Groups.ENEMY_ATTACKS);
 		    SoC.game.groupmanager.add(fireStone, Constants.Groups.MAP_BOUND);
 		    SoC.game.groupmanager.add(fireStone, Constants.Groups.PROJECTILES);
 		    SoC.game.levelmanager.setLevel(fireStone, Constants.Groups.LEVEL+p.z);
 		    fireStone.addToWorld();
 			}
 		}
 	}
 
 	@Override
 	public void process(Entity e) {
 		if(!init){
 			init(e);
 			return;
 		}
 		timer-=SoC.game.world.delta;
 		if(state == STANDARD){
 			standard(e);
 			return;
 		}
 		if(state == CASTING){
 			casting(e);
 			return;
 		}
 		if(state == WAVING){
 			waving(e);
 			return;
 		}
 
 	}
 
 
 	@Override
 	public void death(Entity e) {
 		SoC.game.setScreen(new CreditsScreen(SoC.game));
 	}
 	
 	private void sendWindBlades(){
 		Stats stats = SoC.game.statsmapper.get(SoC.game.player);
 
 		for (int i = 0; i < windblades.length; i++){
 			Vector2 windblade = windblades[i];
 			Position playPos = SoC.game.positionmapper.get(SoC.game.player);
 			Vector2 direction = new Vector2();
 		
 			float dstx = playPos.x - windblade.x*World.TILE_SIZE;
 			float dsty = playPos.y - windblade.y*World.TILE_SIZE;
 		
 			if(Math.abs(dstx) > Math.abs(dsty)){
 				direction.x = Math.signum(dstx);
 				direction.y = Math.signum(dsty)*(Math.abs(dsty)/Math.abs(dstx));
 			} else {
 				direction.y = Math.signum(dsty);
 				direction.x = Math.signum(dstx)*(Math.abs(dstx)/Math.abs(dsty));
 			}
 		
 			Entity blade = EntityFactory.createWindblade(windblade.x*World.TILE_SIZE, windblade.y*World.TILE_SIZE, 0, direction, (int) (stats.maxHealth*0.07f+stats.armor));
 			SoC.game.groupmanager.add(blade, Constants.Groups.PROJECTILES);
 			SoC.game.groupmanager.add(blade, Constants.Groups.ENEMY_ATTACKS);
 			SoC.game.groupmanager.add(blade, Constants.Groups.MAP_BOUND);
 			SoC.game.groupmanager.add(blade, Constants.Groups.DESTROYABLE_PROJECTILES);
 			SoC.game.levelmanager.setLevel(blade, Constants.Groups.LEVEL+0);
 			blade.addToWorld();
 		}
 	}
 	
 	
 	private void sendPoisonCloud(Entity e) {
 		Position pos = SoC.game.positionmapper.get(e);
 		Position playPos = SoC.game.positionmapper.get(SoC.game.player);
 	
 		float dstx = playPos.x - pos.x;
 		float dsty = playPos.y - pos.y;
 	
 		if(Math.abs(dstx) > Math.abs(dsty)){
 			pos.direction.x = Math.signum(dstx);
 			pos.direction.y = Math.signum(dsty)*(Math.abs(dsty)/Math.abs(dstx));
 		} else {
 			pos.direction.y = Math.signum(dsty);
 			pos.direction.x = Math.signum(dstx)*(Math.abs(dstx)/Math.abs(dsty));
 		}
 		Entity cloud = EntityFactory.createMovingCloud(pos, SoC.game.boundsmapper.get(e));
 		SoC.game.groupmanager.add(cloud, Constants.Groups.PROJECTILES);
 		SoC.game.groupmanager.add(cloud, Constants.Groups.ENEMY_ATTACKS);
 		SoC.game.groupmanager.add(cloud, Constants.Groups.MAP_BOUND);
 		SoC.game.levelmanager.setLevel(cloud, Constants.Groups.LEVEL + pos.z);
 		cloud.addToWorld();		
 	}
 	
 	private void callFireRain(boolean horizontal){
 		Stats stats = SoC.game.statsmapper.get(SoC.game.player);
 		for(int i=this.leftmostTile;i < rightmostTile;i=i+((horizontal)?2:8)){
 			for(int j=this.bottomTile;j<(topTile);j=j+((horizontal)?8:2)){
 				Entity spawned = EntityFactory.createMeteor(i*World.TILE_SIZE+World.TILE_SIZE*0.5f, j*World.TILE_SIZE+World.TILE_SIZE*0.5f, 0, (int) (stats.maxHealth*0.2f));	
 				SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +0);
 				spawned.addToWorld();				
 			}
 		}
 		horizontal = !horizontal;
 	}
 	
 	
 	public void standard(Entity e){
 		Buff b = SoC.game.buffmapper.get(e);
 		if(!b.buffClasses.contains(Inmune.class)){
 			Buff.addbuff(e, new Inmune());
 		}
 		if(timer <= 0){
 			if(!teleported){
 				if(counter > 5){
 					timer = Constants.Buff.TELEPORT_CAST_TIME;
 					teleported = true;
 					Position pos = SoC.game.positionmapper.get(e);
 
 					pos.direction.x = 0;
 					pos.direction.y = -1;
 					
 					Buff.addbuff(e, new Teleport((leftTile+(rightTile-leftTile)*0.5f)*World.TILE_SIZE, (bottomTile+(topTile-bottomTile)*0.5f)*World.TILE_SIZE, 0));
 					
 					SoC.game.charactermapper.get(e).renderers[State.ATTACK].time=0;
 					SoC.game.statemapper.get(e).state = State.ATTACK;
 				} else {
 
 					teleported = true;
 
 					Position pos = SoC.game.positionmapper.get(e);
 
 					pos.direction.x = 0;
 					pos.direction.y = -1;
 			
 					x = AI.rng.nextInt(29) + leftTile;
 					y = AI.rng.nextInt(21) + bottomTile;
 			
 					Buff.addbuff(e, new Teleport(x*World.TILE_SIZE, y*World.TILE_SIZE, 0));
 			
 					timer = Constants.Buff.TELEPORT_CAST_TIME;
 					SoC.game.charactermapper.get(e).renderers[State.ATTACK].time=0;
 					SoC.game.statemapper.get(e).state = State.ATTACK;
 				}
 			} else {
 				teleported = false;
 				if(counter > 5){
 					state = CASTING;
 					SoC.game.buffmapper.get(e).removebuff(Inmune.class, e);
 					Buff.addbuff(e, new Casting(5f, Constants.BuffColors.RED));
 					timer = 5f;
 					SoC.game.charactermapper.get(e).renderers[State.ATTACK].time=0;
 					SoC.game.statemapper.get(e).state = State.ATTACK;
 					counter = 0;
 					return;
 				}
 				int attack = AI.rng.nextInt(10);
 				if(attack <= 2){
 					callFireRain(horizontal);
 					horizontal=!horizontal;
 				} else if(attack <= 7){
 					sendWindBlades();
 				} else {
 					sendPoisonCloud(e);
 				}
 				counter++;
 				
 			}
 		}
 	}
 	
 	public void casting(Entity e){
		Buff b = SoC.game.buffmapper.get(e);
		if(!b.buffClasses.contains(Inmune.class)){
			Buff.addbuff(e, new Inmune());
		}
 		if(SoC.game.damagemapper.has(e)){
 			Stats stats = SoC.game.statsmapper.get(e);
 			System.out.println("Que pone: "+stats.health+" "+stats.maxHealth+""+((stats.health - (SoC.game.damagemapper.get(e).damage - stats.armor))/stats.maxHealth) );
 			if(((float)(stats.health - (SoC.game.damagemapper.get(e).damage - stats.armor))/(float)stats.maxHealth) <= phasesLife){
 				phasesLife-=0.25;
 				state = WAVING;
 				counter = 10;
 				Buff.addbuff(e, new Teleport(50*World.TILE_SIZE, 187*World.TILE_SIZE, 0));
 			} else {
 				state = STANDARD;
 			}
 			SoC.game.buffmapper.get(e).removebuff(Casting.class, e);
 		} else {
 			if(timer <= 0){
 				for(int i=this.leftmostTile;i < rightmostTile;i++){
 					for(int j=this.bottomTile;j<topTile;j++){
 						Entity spawned = EntityFactory.createFlame(i*World.TILE_SIZE+World.TILE_SIZE*0.5f, j*World.TILE_SIZE+World.TILE_SIZE*0.5f, 0, 0);	
 						SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 						SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 						SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +0);
 						spawned.addToWorld();				
 					}
 				}
 				timer = 20f;
 				Buff.addbuff(e, new Inmune());
 			}
 		}
 	}
 	
 	public void waving(Entity e){
 		if(counter>0){
 			if(timer<=0){
 				if(horizontalWave)
 					wave(e,new Vector2(0,-1));
 				else
 					wave(e,new Vector2(-1,0));
 				horizontalWave=!horizontalWave;
 				counter--;
 				timer=3.5f;
 			}
 		}else{
 			state = STANDARD;
 			counter=0;
 		}
 	}
 }
