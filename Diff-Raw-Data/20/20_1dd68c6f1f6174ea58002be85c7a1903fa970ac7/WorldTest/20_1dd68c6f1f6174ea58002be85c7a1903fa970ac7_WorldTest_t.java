 package model.world;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import model.geometrical.Position;
 import model.sprites.Enemy;
 import model.sprites.EnemyFactory;
 import model.sprites.Player;
 import model.sprites.Sprite;
 import model.sprites.Sprite.State;
 import model.items.weapons.*;
 import model.items.weapons.WeaponFactory.Level;
 import model.items.weapons.WeaponFactory.Type;
 
 import org.junit.Test;
 
 public class WorldTest {
 	
 	@Test
 	public void getTiles(){
 		Tile[][] tiles = new Tile[10][10];
 		for (int x = 0; x < tiles.length; x++){
 			for(int y = 0; y < tiles.length; y++){
 				tiles[x][y] = new Tile(new Position(x, y), 0);
 			}
 		}
 		
 		World w = new World(tiles);
 		
 		Tile[][] testTiles= w.getTiles();
 		
 		assertTrue(testTiles.length == 10 && testTiles[9][9] != null);
 	}
 	
 	@Test
 	public void setTiles(){
 		Tile[][] tiles = new Tile[10][10];
 		for (int x = 0; x < tiles.length; x++){
 			for(int y = 0; y < tiles.length; y++){
 				tiles[x][y] = new Tile(new Position(x, y), 0);
 			}
 		}
 		
 		World w = new World(null);
 		
 		w.setTiles(tiles);
 		
 		assertTrue(w.getTiles().length == 10 && w.getTiles()[9][9] != null);
 	}
 	
 	@Test
 	public void addSprite(){
 		World w = new World(null);
 		Sprite s = new Player(1,1);
 		w.addSprite(s);
 		
 		assertTrue(w.getSprites().size() == 1 && w.getSprites().contains(s));
 	}
 	
 	@Test
 	public void removeSprite(){
 		World w = new World(null);
 		Sprite s = new Player(1,1);
 		w.addSprite(s);
 		w.removeSprite(s);
 		assertTrue(w.getSprites().size() == 0);
 	}
 	
 	@Test
 	public void removeSprites(){
 		World w = new World(null);
 		List<Sprite> sprites = new ArrayList<Sprite>(); 
 		sprites.add(EnemyFactory.createEasyEnemy(new Position(1,1))); 
 		sprites.add(EnemyFactory.createEasyEnemy(new Position(1,2)));
 		
		for(int i = 0; i < sprites.size(); i++){
			w.addSprite(sprites.get(i));
		}
		
		assertTrue(w.getSprites().size() == 2);

 		w.removeSprites(sprites);
 		
 		assertTrue(w.getSprites().size() == 0);
 	}
 	
 	@Test
 	public void getSprites(){
 		World w = new World(null);
 		Sprite e = EnemyFactory.createEasyEnemy(new Position(1,1));
 		Sprite p = new Player(1,1);
 		
 		w.addSprite(p);
 		w.addSprite(e);
 		
 		assertTrue(w.getSprites().size() == 2 && w.getSprites().contains(p) 
 			&& w.getSprites().contains(e));
 	}
 	
 	@Test
 	public void addProjectile(){
 		World w = new World(null);
 		Projectile p = new Projectile(1,1,1,1,new Position(1,1));
 		
 		w.addProjectile(p);
 		
 		assertTrue(w.getProjectiles().size() == 1 && w.getProjectiles().contains(p));
 	}
 	
 	@Test
 	public void removeProjectile(){
 		World w = new World(null);
 		Projectile p = new Projectile(1,1,1,1,new Position(1,1));
 		
 		w.addProjectile(p);
 		w.removeProjectile(p);
 		
 		assertTrue(w.getProjectiles().size() == 0);
 	}
 	
 	@Test
 	public void removeProjectiles(){
 		World w = new World(null);
 		List<Projectile> projectiles = new ArrayList<Projectile>();
 		projectiles.add(new Projectile(1,1,1,1, new Position(1,1)));
 		projectiles.add(new Projectile(1,1,1,1, new Position(1,2)));
 		
		for(int i = 0; i < projectiles.size(); i++){
			w.addProjectile(projectiles.get(i));
		}
		
		assertTrue(w.getProjectiles().size() == 2);
		
 		w.removeProjectiles(projectiles);
 		
 		assertTrue(w.getProjectiles().size() == 0);
 		
 	}
 
 	@Test
 	public void getProjectiles(){
 		World w = new World(null);
 		Projectile p1 = new Projectile(1,1,1,1, new Position(1,1));
 		Projectile p2 = new Projectile(1,1,1,1, new Position(1,1));
 		
 		w.addProjectile(p1);
 		w.addProjectile(p2);
 		
 		assertTrue(w.getProjectiles().size() == 2 && w.getProjectiles().contains(p1)
 			&& w.getProjectiles().contains(p2));
 	}
 	
 	@Test
 	public void Update() {
 		//No need to check sprite.move(), already checked in EnemyTest and PlayerTest
 		World w = new World(null);
 		Player p = new Player(1,1);
 		Enemy e = EnemyFactory.createEasyEnemy(new Position(2,1));
 		Weapon weapon = WeaponFactory.createWeapon(Type.PISTOL, Level.LARGE);
 		
 		w.addSprite(e);
 		w.addSprite(p);
 		
 		p.setMoveDir(0f);
 		w.update();
 		
 		//TODO check if collision noticed by World.update()
 		
 		p.setPosition(new Position(0,1));
 		p.setState(State.STANDING);
 		p.setWeapon(weapon);
 		w.addProjectile(p.getActiveWeapon().createProjectile(p.getDirection(), p.getProjectileSpawn()));
 		
 		for(int i = 0; i < 25; i++){
 			w.update();
 		}
 		
 		assertTrue(e.getHealth() == 25 && w.getProjectiles().size() == 0);
 		
 		p.setWeapon(WeaponFactory.createWeapon(Type.PISTOL, Level.LARGE));//TODO why is this needed?
 		w.addProjectile(p.getActiveWeapon().createProjectile(p.getDirection(), p.getProjectileSpawn()));
 		for(int i = 0; i < 50; i++){
 			w.update();
 		}
 		
 		assertTrue(!(w.getSprites().contains(e)) && w.getProjectiles().size() == 0);
 		
 		w.addProjectile(p.getActiveWeapon().createProjectile(p.getDirection(), p.getProjectileSpawn()));
 		
 		for(int i = 0; i < 200; i++){
 			w.update();
 		}
 		assertTrue(w.getProjectiles().size() == 0);
 	}
 
 }
