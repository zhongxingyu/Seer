 package entities.players;
 
 import java.util.Map;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 import utils.Tile;
 import entities.Entity;
 import entities.players.abilities.AbilityFinder;
 import entities.players.abilities.IPlayerAbility;
 
 public class Player extends Entity {
 	
 	private Animation sprite;
	private Map<String, IPlayerAbility> abilities = AbilityFinder.initialiseAbilities();
 
 	public Player(Rectangle hitbox, int maxhealth) {
 		super(hitbox, maxhealth);
 		Image[] movementRight = null;
 		try {
 			movementRight = new Image[]{new Image("data/images/dvl1_rt1.gif"), new Image("data/images/dvl1_rt2.gif")};
 		} catch (SlickException e) {
 			//do shit all
 		}
 		int[] duration = {300,300};
 		sprite = new Animation(movementRight, duration, false);
 	}
 	
 	protected Object clone() {
 		return new Player(new Rectangle(getX(), getY(), getWidth(), getHeight()),getMaxHealth());
 	}
 	
 	/**
 	 * checks if the player has the ability, if the player does the ability is 'used'
 	 * @param key
 	 */
 	public void useAbility(String key)
 	{
 		//lookup the key in the allowed abilities
 		//if the key is in the map then ability.use(this)
 		IPlayerAbility tempability = abilities.get(key.toLowerCase());
 		if(tempability != null)
 		{
 			tempability.use(this);
 		}
 		
 	}
 	
 	private void playerJump() {
 		useAbility("doublejump");
 		if (isOnGround()) {
 			super.jump();
 		}
 	}
 
 	/**
 	 * This is the only method that needs to be called to update the player.
 	 * Input is assumed to already have been checked for being pressed
 	 * the previous frame.
 	 */
 	@Override
 	public void update(Input input, Tile[][] properties, int delta) {
 		this.properties = properties;
 		this.delta = delta; //time difference since last frame
 		
 		if (input.isKeyPressed(Input.KEY_SPACE)) {
 			playerJump();
 		}
 		
 		frameMove();
 	}
 	
 	@Override
 	public void render() {
 		sprite.draw((int)getX(), (int)getY(), new Color(256,256,256));
 	}
 }
