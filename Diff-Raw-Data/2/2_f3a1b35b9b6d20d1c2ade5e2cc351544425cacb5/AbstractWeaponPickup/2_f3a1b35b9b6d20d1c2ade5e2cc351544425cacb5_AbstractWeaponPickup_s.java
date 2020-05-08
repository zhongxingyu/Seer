 package chalmers.TDA367.B17.weaponPickups;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.model.AbstractTank;
 import chalmers.TDA367.B17.model.Entity;
 
 public abstract class AbstractWeaponPickup extends Entity{
 	private String name;
 	protected AbstractTank absTank;
 	protected String type;
 
 	/**
 	 * Create a new AbstractPowerUp.
 	 */
 	public AbstractWeaponPickup(Vector2f position) {
 		super();
 		absTank = null;
		Vector2f size = new Vector2f(60f, 25f);
 		setShape(new Rectangle(position.getX()-size.getX()/2, position.getY()-size.getY()/2, size.getX(), size.getY()));
 	}
 
 	/**
 	 * Get the name of this power up.
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Set the name of this power up.
 	 * @param name the new name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public void update(int delta) {
 		
 	}
 	public void activate(AbstractTank absTank){
 		this.absTank = absTank;
 		spriteID = "";
 		this.destroy();
 	}
 	public void didCollideWith(Entity entity){
 		if(entity instanceof AbstractTank){
 			activate((AbstractTank)entity);
 		}
 	}
 }
