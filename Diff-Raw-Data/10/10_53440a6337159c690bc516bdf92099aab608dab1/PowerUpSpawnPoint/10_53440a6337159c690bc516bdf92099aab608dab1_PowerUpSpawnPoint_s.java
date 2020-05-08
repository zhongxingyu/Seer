 package chalmers.TDA367.B17.spawnpoints;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.model.AbstractPowerUp;
 import chalmers.TDA367.B17.model.AbstractSpawnPoint;
 import chalmers.TDA367.B17.model.Entity;
 import chalmers.TDA367.B17.powerups.PowerUpFactory;
 
 public class PowerUpSpawnPoint extends AbstractSpawnPoint{
 	
 	private String type;
 	private int spawnRate;
 	private int spawnTimer;
 	
 	public PowerUpSpawnPoint(Vector2f position, int spawnRate, String type) {
 		super(position);
 		this.type = type;
 		spriteID = "powerup_spawnpoint";
 		
 		this.spawnRate = spawnRate;
 		spawnTimer = spawnRate;
 		
 		Vector2f size = new Vector2f(23f, 23f);
 		setShape(new Rectangle(position.getX()-size.getX()/2, position.getY()-size.getY()/2, size.getX(), size.getY()));
 	}
 	
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type){
 		this.type = type;
 	}
 	
 	@Override
 	public void spawnEntity(){
 		PowerUpFactory.getPowerUp(getType(), getPosition());
 	}
 	
 	@Override
 	public void update(int delta){
 		if(spawnRate != 0){
		spawnTimer -= delta;
		if(spawnTimer <= 0 && isSpawnable()){
			spawnTimer = spawnRate;
			spawnEntity();
		}
 		}else if(isSpawnable()){
 			spawnEntity();
 		}
 		setSpawnable(true);
 	}
 	
 	@Override
 	public void didCollideWith(Entity entity){
 		if(entity instanceof AbstractPowerUp){
 			setSpawnable(false);
 		}
 	}
 }
