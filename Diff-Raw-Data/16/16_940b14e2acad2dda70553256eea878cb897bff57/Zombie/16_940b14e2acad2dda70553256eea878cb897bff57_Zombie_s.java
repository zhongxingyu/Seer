 package se.doverfelt.dungeonCrawl;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
import it.randomtower.engine.ResourceManager;
 import it.randomtower.engine.tween.Tweener;
 
 public class Zombie extends Creature{
 	
 	public Tweener tweener;
 
 	public Zombie(float x, float y, WorldOverworld world) {
 		super(x, y, 15, 1, 1);
 		setAI(new ZombieAI(this, world));
 		
 		setGraphic(ResourceManager.getSpriteSheet("char").getSprite(1, 1).getScaledCopy(scaleFactor));
 		
 		//setHitBox(0, 0, ResourceManager.getSpriteSheet("char").getSprite(1, 1).getScaledCopy(scaleFactor).getWidth(), ResourceManager.getSpriteSheet("char").getSprite(1, 1).getScaledCopy(scaleFactor).getHeight());
 
         setHitBox(0, 0, ResourceManager.getSpriteSheet("char").getSprite(1, 1).getScaledCopy(scaleFactor).getWidth(), ResourceManager.getSpriteSheet("char").getSprite(1, 1).getScaledCopy(scaleFactor).getHeight(), true);
 
 		addType(Creature.ZOMBIE);
 
 	}
 	
 	@Override
 	public void render(GameContainer gc, Graphics g) {
 		
 		g.setColor(Color.black);
 		g.fillRect(x, y - 10, width, 6);
 		g.setColor(Color.red);
 		g.fillRect(x + 1, y - 9, (width - 2) * hp() / maxHp(), 4);
 		
 		g.drawImage(currentImage, x, y);
 	}
 	
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {
 		super.update(gc, delta);
 		
 		if (((ZombieAI)getAI()).hasTarget && this.collide(Tile.WALL, x + 1, y + 1) == null && this.collide(Tile.WALL, x - 1, y - 1) == null) {
 			setPosition(tweener.apply(this));
 		} else if (((ZombieAI)getAI()).hasTarget && this.collide(Tile.WALL, x + 1, y) != null) {
             tweener.pause();
 			setPosition(new Vector2f(x - 1, y));
 		} else if (((ZombieAI)getAI()).hasTarget && this.collide(Tile.WALL, x - 1, y) != null) {
             tweener.pause();
             setPosition(new Vector2f(x + 1, y));
         } else if (((ZombieAI)getAI()).hasTarget && this.collide(Tile.WALL, x, y + 1) != null) {
             tweener.pause();
             setPosition(new Vector2f(x, y - 1));
         } else if (((ZombieAI)getAI()).hasTarget && this.collide(Tile.WALL, x, y - 1) != null) {
             tweener.pause();
             setPosition(new Vector2f(x, y + 1));
         }
 		
 	}
 	
 	
 
 }
