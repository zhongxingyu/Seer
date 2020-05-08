 package spaceshooters.entities;
 
 import java.util.Random;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 
 import spaceshooters.level.Level;
 
 public class BulletSupply extends Entity {
 	
 	private Random random;
 	
 	public BulletSupply(Level level, int startX, int startY) {
 		super(level, startX, startY, EntityType.BULLET_SUPPLY);
 		random = new Random();
 	}
 	
 	@Override
 	public void update(GameContainer container, int delta) throws SlickException {
 		this.collideCheck(EntityType.PLAYER);
 		
 		y += 0.3F * delta;
 	}
 	
 	@Override
 	public void onCollide(Entity entity) throws SlickException {
 		this.setDead();
 		Player player = (Player) entity;
 		int toAdd = random.nextInt(11) + 4;
 		// Remove this if later, after debugging...
 		if (player.getAmmo() < 999) {
 			player.addAmmo(toAdd >= 12 ? 12 : toAdd);
 		}
 	}
 	
 	@Override
 	public void onDeath() {
 	}
 	
 	@Override
 	public String getTextureFile() {
 		return "anim.png";
 	}
 }
