 package cz.emo4d.zen.gameplay;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 import cz.emo4d.zen.gameplay.Entity.Direction;
 import cz.emo4d.zen.remote.DeviceEvent;
 import cz.emo4d.zen.remote.RemoteControl;
 import cz.emo4d.zen.screens.Map;
 
 public class BulletManager {
 	
 	public BulletManager(Map map, Texture tex, EffectManager em) {
 		super();
 		this.map = map;
 		this.tex = tex;
 		this.em = em;
 	}
 
 	private Array<Bullet> activeBullets = new Array<Bullet>();
 	private Map map;
 	private Texture tex;
 	private EffectManager em;
 	
 	public void shoot(Vector2 origin, Direction dir, int strength, Player player) {
 		Bullet bullet = new Bullet(tex);
 		bullet.setMap(map);
 		bullet.shoot(origin, dir, strength, player);
 		activeBullets.add(bullet);
 	}
 	
 	public void collision(Array<Player> players, Array<Mob> enemies) {
 		//boolean collision = false;
 		for (int i = 0; i < activeBullets.size; i++) {					
 			Bullet b = activeBullets.get(i);
 			
 			for (int j = 0; j < players.size; j++) {
 				Player p = players.get(j);				
 				
 				if (p.alive && p != b.player && b.collision(p)) {
 					p.takeHit(b.strength);				
 					em.addEffect(EffectManager.AvailableEffects.BULLET_EXPLOSION,
 							b.position.x, b.position.y);
 					em.addEffect(EffectManager.AvailableEffects.HIT_BLOOD, p.position.x, p.position.y);
 					
 					if (p.health <= 0)
 					{
 						em.addEffect(EffectManager.AvailableEffects.DIE_EXPLOSION, p.position.x, p.position.y);
 						p.alive = false;
 					}
 					
 					activeBullets.removeIndex(i);
 					break;
 				}
 			}
 		}
 			
 			
 			/*if (activeBullets.get(i).collision(enemy)) {
 				enemy.takeHit(activeBullets.get(i).strength);				
 				em.addEffect(EffectManager.AvailableEffects.BULLET_EXPLOSION,
 						activeBullets.get(i).position.x, activeBullets.get(i).position.y);
 				activeBullets.removeIndex(i);
 				collision = true;
 			}			
 		}
 		return collision;*/
 	}
 	
 	public void collisionWithMap(RemoteControl rc) {
 		for (int i = 0; i < activeBullets.size; i++) {
 			if (activeBullets.get(i).collisionWithMap()) {
 				em.addEffect(EffectManager.AvailableEffects.BULLET_EXPLOSION,
 						activeBullets.get(i).position.x, activeBullets.get(i).position.y);
 				
 				activeBullets.removeIndex(i);
 				
				rc.emitEvent(0, DeviceEvent.VIBRATE);
 			}			
 		}
 	}
 	
 	public void update(float deltaTime) {
 		for (Bullet p : activeBullets) {
 			p.update(deltaTime);
 		}
 	}
 	
 	
 	public void render(SpriteBatch spriteBatch) {
 		for (Bullet p : activeBullets) {
 			p.render(spriteBatch);
 		}		
 	}
 }
