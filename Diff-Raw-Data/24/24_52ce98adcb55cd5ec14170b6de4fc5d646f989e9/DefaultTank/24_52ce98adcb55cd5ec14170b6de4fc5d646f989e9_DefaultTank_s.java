 package chalmers.TDA367.B17.tanks;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.model.AbstractTank;
 import chalmers.TDA367.B17.model.Player;
 import chalmers.TDA367.B17.weapons.DefaultTurret;
 
 public class DefaultTank extends AbstractTank {
 	//The default maxSpeed of DefaultTank
 	public static final float DEFAULT_MAXSPEED = 0.07f;
 	//The default minSpeed of DefaultTank
 	public static final float DEFAULT_MINSPEED = -0.04f;
 	
 	/**
	 * Create a new DefaultTank.
 	 * @param direction The direction the tank will face.
	 * @param player The owner of this tank.
 	 */
	public DefaultTank(int id, Vector2f direction, Player player) {
		super(id, direction, DEFAULT_MAXSPEED, DEFAULT_MINSPEED, player);
 		setShape(new Rectangle(100, 150, 48, 63));
 		setTurretOffset(6);
		turret = new DefaultTurret(1000+id, new Vector2f(this.getPosition().x, this.getPosition().y+this.getTurretOffset()), direction.getTheta(), this);
		spriteID = "tank_blue";
 		setHealth(100);
 		GameController.getInstance().getWorld().addEntity(this);
 	}
 }
