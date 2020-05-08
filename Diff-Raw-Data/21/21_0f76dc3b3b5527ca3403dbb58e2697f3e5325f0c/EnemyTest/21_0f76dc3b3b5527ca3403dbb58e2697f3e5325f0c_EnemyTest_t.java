 package model.sprites;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import model.geometrical.CollisionBox;
 import model.geometrical.Position;
 import model.items.SupplyFactory;
 import model.items.weapons.Weapon;
 import model.items.weapons.WeaponFactory;
 
 import org.junit.Test;
 
 /**
  * 
  * @author Linus Hagvall
  *
  */
 public class EnemyTest {
 
 	@Test
 	public void moveXAxis(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e.setState(Enemy.State.RUNNING);
 		e.setDirection((float) (Math.PI/2));
 		e.moveXAxis();
 		assertTrue(e.getCenter().getX() == 1);
 		assertTrue(e.getCenter().getY() == 1);
 		
 		e.setDirection((float) (Math.PI));
 		e.moveXAxis();
 		assertTrue(e.getCenter().getX() == 1-e.getSpeed());
 		assertTrue(e.getCenter().getY() == 1);
 		
 		e.setDirection((float) (Math.PI/4));
 		e.moveBack();
 		e.moveXAxis();
 		assertTrue(e.getCenter().getX() == (float)(1 + Math.sqrt(e.getSpeed()*e.getSpeed()/2)));
 		assertTrue(e.getCenter().getY() == 1);
 	}
 	
 	@Test
 	public void moveYAxis(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e.setState(Enemy.State.RUNNING);
 		
 		e.setDirection((float) (Math.PI/2));
 		e.moveYAxis();
 		assertTrue(e.getCenter().getX() == 1);
 		assertTrue(e.getCenter().getY() == 1-e.getSpeed());
 		
 		e.setDirection((float) (Math.PI));
 		e.moveBack();
 		e.moveYAxis();
 		assertTrue(e.getCenter().getX() == 1);
 		assertTrue(e.getCenter().getY() == 1);
 		
 		e.setDirection((float) (Math.PI/4));
 		e.moveBack();
 		e.moveYAxis();
 		assertTrue(e.getCenter().getX() == 1);
 		assertTrue(e.getCenter().getY() == (float)(1 - Math.sqrt(0.02)));
 	}
 
 	@Test
 	public void getActiveWeapon(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, WeaponFactory.createWeapon
 			(WeaponFactory.Type.FISTS, WeaponFactory.Level.RUSTY), 50, 0);
 		Weapon w = e.getActiveWeapon();
 		
 		assertTrue(w.getKeys()[0].equals("Rusty"));
 		assertTrue(w.getKeys()[1].equals("Fists"));
 	}
 	
 	@Test
 	public void getDirection(){
		Enemy e = new Enemy(new Position(1,1), 0.2f, WeaponFactory.createWeapon
				(WeaponFactory.Type.FISTS, WeaponFactory.Level.RUSTY), 50, 0);
		e.setDirection(5f);
		assertTrue(e.getDirection() == 5f);
		e.setDirection((float)Math.PI/2);
		assertTrue(e.getDirection() == (float)Math.PI/2);
 	}
 	
 	@Test
 	public void getPosition(){
 		Enemy e = new Enemy(new Position(1f,1f), 1f, null, 1, 0);
 		e.setPosition(new Position(1.5f, 2f));
 		assertTrue(e.getPosition().getX() == 1.5f && e.getPosition().getY() == 2f);
 	}
 	
 	@Test
 	public void setPosition(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e.setPosition(new Position(2,3));
 		assertTrue(e.getPosition().getX() == 2 && e.getPosition().getY() == 3);
 	}
 	
 	@Test
 	public void getHealth(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		assertTrue(e.getHealth() == 50);
 	}
 	
 	@Test
 	public void setDirection(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e.setDirection(1);
 		assertTrue(e.getDirection() == 1);
 	}
 	
 	@Test 
 	public void ReduceHealth(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e.reduceHealth(10);
 		assertTrue(e.getHealth() == 40);
 	}
 	
 	@Test
 	public void getProjectileSpawn(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		
 		Position projectileSpawn = new Position(e.getPosition().getX() + 
 				e.getHitBox().getWidth()/2 + (float)(Math.cos(e.getDirection())*0.5f), 
 				e.getPosition().getY() + e.getHitBox().getHeight()/2 - 
 				(float)(Math.sin(e.getDirection())*0.5f));
 		
 		assertTrue(e.getProjectileSpawn().getX() == projectileSpawn.getX());
 		assertTrue(e.getProjectileSpawn().getY() == projectileSpawn.getY());
 	}
 	
 	@Test
 	public void getCenter(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		
 		float targetX = e.getPosition().getX() + e.getHitBox().getWidth()/2;
 		float targetY = e.getPosition().getY() + e.getHitBox().getHeight()/2; 
 		
 		assertTrue(e.getCenter().getX() == targetX && e.getCenter().getY() == targetY);
 		
 	}
 	
 	@Test
 	public void restore(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		String[] s = e.getData();
 		Enemy e2 = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e2.restore(s);
 		assertTrue(e.getHealth() == e2.getHealth());
 		assertTrue(e.getSpeed() == e2.getSpeed());
 		assertTrue(e.getPosition().getX() == e2.getPosition().getX());
 		assertTrue(e.getPosition().getY() == e2.getPosition().getY());
 	}
 	
 	@Test
 	public void moveBack(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e.setDirection((float) (Math.PI/1.5));
 		e.moveYAxis();
 		e.moveXAxis();
 		e.moveBack();
 		assertTrue(e.getCenter().getX() == 1);
 		assertTrue(e.getCenter().getY() == 1);
 		
 		e.setDirection((float) (Math.PI/3.5));
 		e.moveYAxis();
 		e.moveXAxis();
 		e.moveBack();
 		assertTrue(e.getCenter().getX() == 1);
 		assertTrue(e.getCenter().getY() == 1);
 	}
 
 	@Test
 	public void getHitBox(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		assertTrue(e.getHitBox() instanceof CollisionBox);
 	}
 	
 	@Test
 	public void getMoveBox(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		assertTrue(e.getMoveBox() instanceof CollisionBox);
 	}
 	
 	@Test
 	public void pickUpItem(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		assertTrue(!e.pickUpItem(SupplyFactory.createRandomSupply(e.getPosition())));
 		assertTrue(!e.pickUpItem(WeaponFactory.createRandomWeapon()));
 	}
 	
 	@Test
 	public void setPathfindingListIndex(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e.setPathfindingListIndex(5);
 		assertTrue(e.getPathfindingListIndex() == 5);
 	}
 	
 	@Test
 	public void getPathfindingListIndex(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		e.setPathfindingListIndex(5);
 		assertTrue(e.getPathfindingListIndex() == 5);
 	}
 	
 	@Test
 	public void setPathfindingList(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		List<Position> list = new ArrayList<Position>();
 		e.setPathfindingList(list);
 		assertTrue(e.getPathfindingList() == list);
 	}
 	
 	@Test
 	public void getPathfindingList(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		List<Position> list = new ArrayList<Position>();
 		e.setPathfindingList(list);
 		assertTrue(e.getPathfindingList() == list);
 	}
 	
 	@Test
 	public void getSpeed(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		assertTrue(e.getSpeed() == 0.2f);
 	}
 		
 	@Test
 	public void setState(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 
 		e.setState(Enemy.State.STANDING);
 		assertTrue(e.getState() == Enemy.State.STANDING);
 		
 		e.setState(Enemy.State.WALKING);
 		assertTrue(e.getState() == Enemy.State.WALKING);
 		
 		e.setState(Enemy.State.RUNNING);
 		assertTrue(e.getState() == Enemy.State.RUNNING);
 	}
 	
 	@Test
 	public void setWay(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		List<Position> list = new ArrayList<Position>();
 		e.setPathfindingList(new ArrayList<Position>());
 		e.setPathfindingListIndex(5);
 		e.setWay(list);
 		assertTrue(e.getPathfindingList() == list);
 		assertTrue(e.getPathfindingListIndex() == 0);
 	}
 	
 	@Test
 	public void getData(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		
 		String[] s = new String[] {
 				e.getHealth() + "",
 				e.getSpeed() + "",
 				e.getCenter().getX() + "",
 				e.getCenter().getY() + ""
 		};
 		for(int i = 0; i<s.length; i++){
 			assertTrue(s[i].equals(e.getData()[i]));
 		}
 	}
 	
 	@Test
 	public void setWeapon(){
 		Enemy e = new Enemy(new Position(1,1), 0.2f, null, 50, 0);
 		Weapon w = WeaponFactory.createRandomWeapon();
 		e.setWeapon(w);
 		assertTrue(e.getActiveWeapon() == w);
 	}
 }
