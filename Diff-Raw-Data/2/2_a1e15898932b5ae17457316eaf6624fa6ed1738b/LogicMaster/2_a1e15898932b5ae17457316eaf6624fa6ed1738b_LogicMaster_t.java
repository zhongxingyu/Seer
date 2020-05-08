 package com.inda.hacksmack;
 
 import java.util.Iterator;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import com.inda.hacksmack.factory.AnimationFactory;
 import com.inda.hacksmack.model.Enemy;
 import com.inda.hacksmack.model.GameMode;
 import com.inda.hacksmack.model.GameState;
 import com.inda.hacksmack.model.HealthBar;
 import com.inda.hacksmack.model.Item;
 import com.inda.hacksmack.model.Item.ItemType;
 import com.inda.hacksmack.model.Player;
 import com.inda.hacksmack.model.Projectile;
 
 public class LogicMaster {
 
 	@SuppressWarnings("unused")
 	private double timepassed = 0;
 	private static LogicMaster _instance;
 
 	private LogicMaster() {
 	}
 
 	public static LogicMaster getInstance() {
 		if (_instance == null) {
 			_instance = new LogicMaster();
 		}
 		return _instance;
 	}
 
 	public void handleLogics(GameState state, int delta) {
 		switch (state.getGameMode()) {
 		case GAMEPLAY:
 
 			timepassed += delta;
 			Player player = state.getPlayer();
 
 			for (Iterator<Item> iterator = state.getItems().iterator(); iterator.hasNext();) {
 				Item item = iterator.next();
 				if (System.currentTimeMillis() > item.getDestroyTime()) {
 					iterator.remove();
 				}
 			}
 
 			//
 			// Kollar om spelaren krockar
 			//
 
 			// player.getPosition().add(new Vector2f(player.getDirection()).normalise().scale((float) (player.getSpeed() * delta / (float) 1000)));
 
 			Vector2f temp = (new Vector2f(player.getPosition())).add(new Vector2f(player.getDirection()).normalise().scale((float) (player.getSpeed() * delta / (float) 1000)));
 
 			boolean krock = false;
 			// kolla krock med fienden
 			for (Enemy enemy : state.getEnemies()) {
 				if (enemy.getPosition().distance(temp) < player.getRadius() + enemy.getRadius()) {
 					// System.out.println("Krock!");
 					krock = true;
 				}
 
 			}
 
 			// Kollar om man krockar med banan.
 			if (!state.getMap().collidesWithMap(temp, player.getRadius())) {
 				krock = true;
 			}
 
 			// Kollar om man g�tt p� item, om s� plocka upp eller g�r n�got special.
 			for (Iterator<Item> it = state.getItems().iterator(); it.hasNext();) {
 				Item item = it.next();
 				if (item.getPosition().distance(player.getPosition()) < item.getRadius() + player.getRadius()) {
 
 					if (item.getType() == ItemType.END) {
 						boolean gem = false;
 						for (Item i : state.getItems()) {
 							if (i.getType() == ItemType.GEM)
 								gem = true;
 						}
 						if (!gem) {
 							// TODO: inga fler gems p� kartan och man st�r p� m�let, klara banan!
 
 						}
 					}
 
 					if (!item.getPassable()) {
 						krock = true;
 						System.out.println("boink");
 					}
 					if (item.getCanBePicked()) {
 						player.getInventory().add(item);
 						it.remove();
 					}
 
 				}
 
 			}
 
 			// Ingen krock, flytta spelaren
 			if (!krock) {
 				player.getPosition().set(temp);
 			}
 
 			//
 			// slut p� spelarkrockkoll
 			//
 
 			//
 			// Kollar fiendernas krockar
 			//
 
 			for (Enemy enemy : state.getEnemies()) {
 				enemy.updateDirection(state.getPlayer());
 
 				temp = (new Vector2f(enemy.getPosition())).add(new Vector2f(enemy.getDirection()).normalise().scale((float) (enemy.getSpeed() * delta / (float) 1000)));
 
 				krock = false;
 				// kolla krock med fienden
 				for (Enemy e : state.getEnemies()) {
 					if (e == enemy)
 						continue;
 					if (e.getPosition().distance(temp) < e.getRadius() + enemy.getRadius()) {
 
 						krock = true;
 					}
 
 				}
 
 				// Kollar om man krockar med banan.
 				if (!state.getMap().collidesWithMap(temp, enemy.getRadius())) {
 					krock = true;
 
 				}
 
 				if (!krock) {
 
 					enemy.getPosition().set(temp);
 				}
 			}
 
 			//
 			// slut p� fiendekrockkoll
 			//
 
 			//
 			// Kollar projektilkrockar
 			//
 
 			for (Enemy enemy : state.getEnemies()) {
 				enemy.updateDirection(state.getPlayer());
 
 				temp = (new Vector2f(enemy.getPosition())).add(new Vector2f(enemy.getDirection()).normalise().scale((float) (enemy.getSpeed() * delta / (float) 1000)));
 
 				krock = false;
 				// kolla krock med fienden
 				for (Enemy e : state.getEnemies()) {
 					if (e == enemy)
 						continue;
 					if (e.getPosition().distance(temp) < e.getRadius() + enemy.getRadius()) {
 
 						krock = true;
 					}
 
 				}
 
 				// Kollar om man krockar med banan.
 				if (!state.getMap().collidesWithMap(temp, enemy.getRadius())) {
 					krock = true;
 
 				}
 			}
 			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!
 			for (Iterator<Projectile> it = state.getProjectiles().iterator(); it.hasNext();) {
 				Projectile proj = it.next();
 				proj.getPosition().add(new Vector2f(proj.getDirection()).normalise().scale((float) (proj.getSpeed() * delta / (float) 1000)));
 				// Kollar krock med fienderna
 				for (Iterator<Enemy> e = state.getEnemies().iterator(); e.hasNext();) {
 					Enemy enemy = e.next();
 					if (enemy == proj.getSource())
 						continue;
 					if (proj.getPosition().distance(enemy.getPosition()) < proj.getRadius() + enemy.getRadius()) {
 						// En fiende har tr?ffats, g?r dmg.
 						// System.out.println("Did " + proj.getDamage() + " points of dmg! " + (int)(enemy.getHealth()-proj.getDamage()) + " hp left!");
 						enemy.setHealth((int) (enemy.getHealth() - proj.getDamage()));
 						proj.explode();
 						it.remove();
 						if (enemy.getHealth() <= 0) {
 							e.remove();
 							// add death animation, let the remains be on the floor for a while after as well
 							Item item = AnimationFactory.newAnimation(enemy.getDeathAnimationId(), enemy.getPosition());
 							item.getAnimation().setLooping(false);
 							item.setDestroyTime((long) (System.currentTimeMillis() + 4000));
 							item.setPassable(true);
 							state.getItems().add(item);
 						}
 						break;
 					}
 				}
 				// Kollar krock med v�ggen
 				if (!state.getMap().collidesWithMap(proj.getPosition(), proj.getRadius())) {
 					proj.explode();
 					it.remove();
 				}
 				// Kollar krock med spelaren!
 				if (proj.getSource() != player) {
 					player.setHealth((int) (player.getHealth() - proj.getDamage()));
 					if (player.getHealth() <= 0) {
 						// TODO: spelaren d�r, game over.
 					}
 					proj.explode();
 					it.remove();
 				}
 			}
 
 			/**
 			 * Update health bar levels
 			 */
 
 			HealthBar healthBar = state.getHealthBar();
 
 			healthBar.setHealthPercentage(player.getHpPercentage());
			healthBar.setShieldPercentage(player.getShieldPercentage());
 
 			break;
 
 		case CUT_SCENE:
 			if (state.getCutScene().done()) { // if a cutscene is done, go to gameplay
 				state.setGameMode(GameMode.GAMEPLAY);
 			}
 			break;
 
 		default:
 			break;
 		}
 	}
 
 }
