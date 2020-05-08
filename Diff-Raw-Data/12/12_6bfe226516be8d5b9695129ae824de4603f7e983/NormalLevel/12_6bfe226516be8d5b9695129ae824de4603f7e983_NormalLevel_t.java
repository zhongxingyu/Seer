 package pl.spaceshooters.level;
 
 import java.util.Random;
 
 import pl.spaceshooters.aurora.entity.EntityType;
 import pl.spaceshooters.aurora.level.LevelType;
 import pl.spaceshooters.entity.Asteroid;
 import pl.spaceshooters.entity.Enemy;
 import pl.spaceshooters.main.Spaceshooters;
 import pl.spaceshooters.particle.Star;
 import pl.spaceshooters.util.Font;
 
 /**
  * All the singleplayer magic happens here.
  * 
  * @author Mat
  * 
  */
 public class NormalLevel extends SingleplayerLevel {
 	
 	private Random random = new Random();
 	
 	public void init() {
 		//factory.postEvent(new LevelLoadEvent(this));
 		for (int i = 0; i < 12; i++) {
 			Star s = new Star(random.nextInt(Spaceshooters.WIDTH), random.nextInt(Spaceshooters.HEIGHT));
 			this.spawnParticle(s);
 		}
 	}
 	
 	@Override
 	public void update(int delta) {
 		super.update(delta);
 		
 		// Perform updates.
 		int difficulty = Spaceshooters.getInstance().getConfiguration().getInt("difficulty");
 		
 		// Entity spawning.
 		if ((this.getEntities().size() - this.getEntities(EntityType.BULLET)) < this.getMaxEntities())
			if ((difficulty == 0 || difficulty == 1) ? random.nextInt(100) == 2 : random.nextInt(50) == 2) {
 				Asteroid asteroid = new Asteroid(this, random.nextInt(Spaceshooters.WIDTH), 0);
 				Enemy enemy = new Enemy(this, random.nextInt(Spaceshooters.WIDTH), 0);
 				if (random.nextInt(2) == 0) {
 					if (difficulty != 0) {
 						this.spawnEntity(enemy);
 					} else {
 						this.spawnEntity(asteroid);
 					}
 				} else {
 					this.spawnEntity(asteroid);
 				}
 			}
 		
 		// Star spawning.
		if (random.nextInt(100) == 2) {
 			Star star = new Star(random.nextInt(pl.spaceshooters.main.Spaceshooters.WIDTH), 0);
 			this.spawnParticle(star);
 		}
 	}
 	
 	@Override
 	public void render() {
 		super.render();
 		Font.getFont().renderString(Spaceshooters.getInstance().getTranslator().getTranslated("player.score") + ": " + player.getScore(), 0, 0);
 		Font.getFont().renderString(Spaceshooters.getInstance().getTranslator().getTranslated("player.ammo") + ": " + player.getAmmo(), 680, 0);
 	}
 	
 	@Override
 	public LevelType getLevelType() {
 		return LevelType.NORMAL;
 	}
 }
