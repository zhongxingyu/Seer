 package pl.spaceshooters.level;
 
 import java.util.Random;
 
 import pl.spaceshooters.aurora.level.LevelType;
 import pl.spaceshooters.aurora.particle.IParticle;
 import pl.spaceshooters.main.Spaceshooters;
 import pl.spaceshooters.particle.Star;
 
 public class MenuLevel extends SingleplayerLevel {
 	
 	private Random random = new Random();
 	
 	public void init() {
 		for (int i = 0; i < 12; i++) {
 			Star s = new Star(random.nextInt(Spaceshooters.WIDTH), random.nextInt(Spaceshooters.HEIGHT));
 			particles.add(s);
 		}
 	}
 	
 	@Override
 	public void update(int delta) {
 		for (IParticle s : particles)
 			if (!s.isAlive()) {
 				particles.remove(s);
 			} else {
 				s.update(delta);
 			}
 		
 		if (delta <= 0)
 			return;
 		
		if (random.nextInt(100) == 20) {
 			particles.add(new Star(random.nextInt(800), 0));
 		}
 	}
 	
 	@Override
 	public void render() {
 		for (IParticle s : particles) {
 			s.render();
 		}
 	}
 	
 	@Override
 	public LevelType getLevelType() {
 		return LevelType.MENU;
 	}
 }
