 package game.model.entity;
 
 import game.util.Polygon;
 import game.util.Vector2D;
 
 public class Bullet extends Entity {
 	
 	public Spaceship thrower;
 	public static float BULLET_SPEED = .3f;
	public static int[] BULLET_X={0,0,15,15,0};//{0,10, 0,0};
	public static int[] BULLET_Y={0,3,3,0,0};//{-3,0,3,-3};
 	
 	public Bullet(Spaceship thrower) {
 		super(thrower.getX(), thrower.getY(), new Vector2D(thrower.getOrientation(),BULLET_SPEED)); //TODO faire apparaitre le bullet devant le lanceur et pas SUR le lanceur
		this.setColor(this.getColor()[0], this.getColor()[1], this.getColor()[2]);
 		//this.setOrientation(thrower.getOrientation());
 		//super.MOMENTUM_INCREASE_RATE=0;
 		super.SPEED_WEAROFF_RATE=0;
 		this.thrower = thrower;
 		this.setCollisionBox(new Polygon(BULLET_X, BULLET_Y));
 		this.getMomentum().setT(thrower.getOrientation());
 		this.getCollisionBox().rotate(this.getOrientation());
 	}
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onCollision(Entity otherEntity) {
 		this.destroy();
 	}
 
 }
