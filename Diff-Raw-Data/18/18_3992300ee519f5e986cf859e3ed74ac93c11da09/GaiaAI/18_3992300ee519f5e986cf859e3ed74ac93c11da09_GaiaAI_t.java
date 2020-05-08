 package com.soc.ai.bosses;
 
 import java.util.Random;
 
 import com.artemis.Entity;
 import com.artemis.utils.Bag;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.math.Vector2;
 import com.soc.ai.AI;
 import com.soc.core.Constants.World;
 import com.soc.core.Constants;
 import com.soc.core.EntityFactory;
 import com.soc.core.SoC;
 import com.soc.game.components.Bounds;
 import com.soc.game.components.Damage;
 import com.soc.game.components.Debuff;
 import com.soc.game.components.Position;
 import com.soc.game.components.Stats;
 import com.soc.game.states.alterations.Push;
 import com.soc.game.states.benefits.Inmune;
 
 public class GaiaAI extends AI{
 
 	public float timer;
 	boolean avatarSpawned, fighting;
 	public Random r;
 	int topTile, bottomTile, leftTile, rigthTile;
 	float tornadoSpeed;
 	boolean horizontal;
 	
 	public GaiaAI(){
 		timer = 0;
 		r = new Random();
 		topTile = 38;
 		bottomTile = 14;
 		leftTile = 114;
 		rigthTile = 114+78;
 		horizontal = true;
 		tornadoSpeed = 0.8f;
 		
 	}
 	
 	@Override
 	public void process(Entity e) {
 		timer -= SoC.game.world.delta;
 		
 		if(fighting){
 			Stats stats = SoC.game.statsmapper.get(e);
 
 			if(timer <= 0){
 				float healthProportion = (float)stats.health/(float)stats.maxHealth;
 				if(horizontal){					
 					boolean left = false;
 					for(float i = 0; i < topTile - bottomTile; i += 3.5f){
 						Entity tornado = EntityFactory.createTornado(
 								(((left)?(leftTile):(rigthTile)))*World.TILE_SIZE + World.TILE_SIZE*0.5f, 
 								(bottomTile + i + r.nextFloat()*3.5f) * World.TILE_SIZE + World.TILE_SIZE*0.5f,
 								SoC.game.positionmapper.get(SoC.game.player).z, 
 								((left)?new Vector2(1, 0):new Vector2(-1,0)),
 								tornadoSpeed);
 
 						SoC.game.groupmanager.add(tornado, Constants.Groups.ENEMY_ATTACKS);
 						SoC.game.groupmanager.add(tornado, Constants.Groups.MAP_BOUND);
 						SoC.game.groupmanager.add(tornado, Constants.Groups.PROJECTILES);
 						SoC.game.levelmanager.setLevel(tornado, Constants.Groups.LEVEL+SoC.game.positionmapper.get(SoC.game.player).z);
 						tornado.addToWorld();
 						left = !left;
 					}
 				} else {
 					boolean top = false;
 					for(float i = 0; i < rigthTile - leftTile; i += 3.5f){
 						if(!top){
 						Entity tornado = EntityFactory.createTornado(
 								(leftTile + i +r.nextFloat()*3.5f)*World.TILE_SIZE + World.TILE_SIZE*0.5f,
 								topTile*World.TILE_SIZE + World.TILE_SIZE*0.5f, 
 								SoC.game.positionmapper.get(SoC.game.player).z, 
 								new Vector2(0,-1),
 								tornadoSpeed);
 							SoC.game.groupmanager.add(tornado, Constants.Groups.ENEMY_ATTACKS);
 							SoC.game.groupmanager.add(tornado, Constants.Groups.MAP_BOUND);
 							SoC.game.groupmanager.add(tornado, Constants.Groups.PROJECTILES);
 							SoC.game.levelmanager.setLevel(tornado, Constants.Groups.LEVEL+SoC.game.positionmapper.get(SoC.game.player).z);
 							tornado.addToWorld();
 						}
 						top = !top;
 						
 					}
 				}
 				if(healthProportion <= 0.75f){
 					horizontal = !horizontal;
 				}
 				if(healthProportion <= 0.5f){
 					int x;
 					int y;
 					float meteorNumber = 10 * ((0.5f-healthProportion/0.5f) + 1*(healthProportion/0.5f));
 					do{
 					x = r.nextInt(rigthTile-leftTile) + leftTile;
 					y = r.nextInt(topTile - bottomTile) + bottomTile;
 					Entity spawned = EntityFactory.createMeteor(x*World.TILE_SIZE+World.TILE_SIZE*0.5f, y*World.TILE_SIZE+World.TILE_SIZE*0.5f, SoC.game.positionmapper.get(SoC.game.player).z);	
 					SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 					SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 					SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +(SoC.game.positionmapper.get(SoC.game.player).z+1));
 					spawned.addToWorld();
 					meteorNumber--;
 					} while(meteorNumber > 0);
 				}
 				if(healthProportion <= 0.25f && tornadoSpeed != 1.2f){
 					tornadoSpeed = 1.2f;
 				}
 				timer = 1.1f;
 			}
 			
 			
 			if(SoC.game.damagemapper.has(e) && stats.health > SoC.game.damagemapper.get(e).damage-stats.armor && stats.health > 1){
 				Position pos = SoC.game.positionmapper.get(e);
 				Position playerPos = SoC.game.positionmapper.get(SoC.game.player);
 				Push p = new Push(new Vector2(), 1650, 1500);
 				float dstx = playerPos.x - pos.x;
 				if(Math.abs(dstx) > 16) p.direction.x = Math.signum(dstx);
 				p.direction.y = -1;
 				if(p.direction.x != 0) p.distance = 3000;
 				Debuff.addDebuff(SoC.game.player, p);
 			}
 			return;
 		}
 		
 		if(!(SoC.game.progress.gaiaAirDefeated && SoC.game.progress.gaiaDarkDefeated && SoC.game.progress.gaiaFlameDefeated)){
 			if(SoC.game.damagemapper.has(e)) SoC.game.hudSystem.tooltip.pop("An eerie voice resonates:\n I am Gaia, keeper of this place. If thou whish to pass, first seek and defeat my aspects.", 0f, 8f);
 			return;
 		} else {
 			
 			Position playerPos = SoC.game.positionmapper.get(SoC.game.player);
 			
 			if(!SoC.game.progress.gaiaAvatarDefeated && !avatarSpawned && (int)(playerPos.x*World.TILE_FACTOR) >= 115 && ((int)(playerPos.y*World.TILE_FACTOR) >= 7 && (int)(playerPos.y*World.TILE_FACTOR) <= 9)){
 				
 				EntityFactory.createWall(e, 114, 7, 0).addToWorld();
 				EntityFactory.createWall(e, 114, 8, 0).addToWorld();
 				EntityFactory.createWall(e, 114, 9, 0).addToWorld();
 				EntityFactory.createWall(e, 114, 10, 0).addToWorld();
 				
 				SoC.game.hudSystem.tooltip.pop("You hear an echo:\n I see you have defeated my aspects, warrior. Very well, now prove your worth to me.", 0f, 8f);
 
 				Entity gaia = EntityFactory.createGaiaAvatar(158*World.TILE_SIZE, 40*World.TILE_SIZE, 0);
 				SoC.game.groupmanager.add(gaia, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(gaia, Constants.Groups.LEVEL +0);
 				SoC.game.groupmanager.add(gaia, Constants.Groups.ENEMIES);
 				SoC.game.groupmanager.add(gaia, Constants.Groups.CHARACTERS);
 				gaia.addToWorld();
 				
				SoC.game.musicmanager.play(e, "gaia-battle.ogg");
				SoC.game.musicmanager.play(gaia, "gaia-battle.ogg");
 				
 				avatarSpawned = true;
 			} else if(SoC.game.progress.gaiaAvatarDefeated){
 					SoC.game.buffmapper.get(e).removebuff(Inmune.class,e);
 					fighting = true;
 					return;
 
 			}
 		}
 	}
 
 	@Override
 	public void death(Entity e) {
 		SoC.game.progress.gaiaDefeated=true;
 		Position pos = SoC.game.positionmapper.get(e);
 		EntityFactory.createItem(Constants.Items.SILVER_SWORD, pos.x, pos.y, pos.z).addToWorld();	
 	}
 
 }
