 package fr.umlv.escape.world;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.jbox2d.callbacks.ContactImpulse;
 import org.jbox2d.callbacks.ContactListener;
 import org.jbox2d.collision.Manifold;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.contacts.Contact;
 
 import fr.umlv.escape.bonus.Bonus;
 import fr.umlv.escape.bonus.BonusFactory;
 import fr.umlv.escape.front.BattleField;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.game.Player;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.weapon.Bullet;
 import fr.umlv.escape.weapon.ListWeapon;
 
 /**
  * This class manage what should happen when body collide. It is a contact listener of the {@link EscapeWorld}
  * @implements Conctlistener
  */
 public class CollisionMonitor implements ContactListener{
 	private final ArrayList<Body> elemToDelete;
 	private boolean createBonus;
 	private final Random random = new Random(0);
 	private final static int PROBABILITY_NEW_BONUS = 30;
 	private final BonusFactory bonusFactory;
 	private final BattleField battleField;
 	
 	/**
 	 * Constructor.
 	 */
 	public CollisionMonitor(BattleField battleField){
 		EscapeWorld.getTheWorld().setContactListener(this);
 		this.elemToDelete=new ArrayList<Body>();
 		this.bonusFactory = new BonusFactory(battleField);
 		this.battleField = battleField;
 	}
 	
 	/**
 	 * As no operations can be done on bodies while there are colliding some operations are postponed
 	 * and are done in this method which have to be called just after a step.
 	 */
 	public void performPostStepCollision(){		
 		for(int i=0; i<elemToDelete.size();++i){
 			Body body = elemToDelete.get(i);
 			EscapeWorld.getTheWorld().setActive(body, false);
 			EscapeWorld.getTheWorld().destroyBody(body);
 			if(this.createBonus){
				int rand = (((int)(Math.random()*279))%8);
 				Bonus bonus=bonusFactory.createBonus("weapon_reloader", (int)((body.getPosition().x*EscapeWorld.SCALE)), (int)((body.getPosition().y*EscapeWorld.SCALE)), rand);
 				bonus.move();
 				battleField.addBonus(bonus);
 				this.createBonus=false;
 			}
 		}
 		elemToDelete.clear();
 	}
 	
 	@Override
 	public void beginContact(Contact arg0) {
 		Player player=Game.getTheGame().getPlayer1();
 		Ship shipPlayer=player.getShip();
 		Body body;
 		Body body2;
 		Ship enemy;
 		Bullet bullet;
 		Bonus bonus;
 		
 		//if one of the two body that collided is the body of the player's ship
 		if((arg0.getFixtureA().getBody()==shipPlayer.getBody())||
 		   (arg0.getFixtureB().getBody()==shipPlayer.getBody())){
 			//get the other body that collided
 			if(arg0.getFixtureA().getBody()==shipPlayer.getBody()){
 				body=arg0.getFixtureB().getBody();
 			} else {
 				body=arg0.getFixtureA().getBody();
 			}
 			//if the second body that collided is an enemy
 			if((enemy=battleField.getShip(body))!=null){
 				shipPlayer.takeDamage(10);
 				enemy.takeDamage(20);
 				if(!enemy.isAlive()){
 					impactEnemyDead(enemy,player);
 					elemToDelete.add(body);
 				}
 			} //else if the second body that collided is a bullet 
 			else if((bullet=battleField.getBullet(body))!=null){
 				shipPlayer.takeDamage(bullet.getDamage());
 				elemToDelete.add(body);
 			} //else if the second body that collided is a bonus 
 			else if((bonus=battleField.getBonus(body))!=null){
 				ListWeapon playerWeapons = shipPlayer.getListWeapon();
 				playerWeapons.addWeapon(bonus.getType(), bonus.getQuantity());
 				elemToDelete.add(body);
 			}
 		} else {
 			body=arg0.getFixtureA().getBody();
 			body2=arg0.getFixtureB().getBody();
 
 			if((enemy=battleField.getShip(body))!=null){
 				if((bullet=battleField.getBullet(body2))==null){
 					throw new AssertionError();
 				}
 				enemy.takeDamage(bullet.getDamage());
 				if(bullet == player.getShip().getCurrentWeapon().getLoadingBullet()){
 					player.getShip().getCurrentWeapon().setLoadingBullet(null);
 				}
 				if(!enemy.isAlive()){
 					elemToDelete.add(body);
 					impactEnemyDead(enemy,player);
 				}
 				if((!bullet.getNameLvl().equals("fireball_3")) && (!bullet.getName().equals("xray"))){
 					elemToDelete.add(body2);
 				}
 			} else {
 				bullet=battleField.getBullet(body);
 				enemy=battleField.getShip(body2);
 				if((bullet==null)||(enemy==null)){
 /*					System.out.println(body.m_fixtureList.m_filter.categoryBits);
 					System.out.println(body.m_fixtureList.m_filter.maskBits);
 					System.out.println(body.getPosition().x*50+" - "+body.getPosition().y);
 					System.out.println(body2.m_fixtureList.m_filter.categoryBits);
 					System.out.println(body2.m_fixtureList.m_filter.maskBits);
 					System.out.println(body2.getPosition().x*50+" - "+body2.getPosition().y);
 				*/	EscapeWorld.getTheWorld().destroyBody(body);
 					EscapeWorld.getTheWorld().destroyBody(body2);
 					return;
 					//throw new AssertionError();
 				}
 				enemy.takeDamage(bullet.getDamage());
 				if(bullet == player.getShip().getCurrentWeapon().getLoadingBullet()){
 					player.getShip().getCurrentWeapon().setLoadingBullet(null);
 				}
 				if(!enemy.isAlive()){
 					elemToDelete.add(body2);
 					impactEnemyDead(enemy, player);
 				}
 				if((!bullet.getNameLvl().equals("fireball_3"))&&(!bullet.getName().equals("xray"))){
 					elemToDelete.add(body);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void endContact(Contact arg0) {
 		//nothing to do
 	}
 
 	@Override
 	public void postSolve(Contact arg0, ContactImpulse arg1) {
 		//Nothing to do	
 	}
 
 	@Override
 	public void preSolve(Contact arg0, Manifold arg1) {
 		//Nothing to do
 	}
 	
 	private void impactEnemyDead(Ship enemy, Player player){
 		int score=0;
 		String name = enemy.getName();
 		if(name.equals("DefaultShip"))	score=25;
 		if(name.equals("KamikazeShip"))	score=25;
 		if(name.equals("BatShip"))		score=50;
 		if( name.equals("FirstBoss")  ||
 			name.equals("SecondBoss") ||
 			name.equals("ThirdBoss")) 	score=1000;
 		player.addScore(score);
 		
 		if(random.nextInt(100) <= PROBABILITY_NEW_BONUS){
 			this.createBonus=true;
 		}
 	}
 }
