 package gameCharacter;
 
 import inventory.Inventory;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.Arrays;
 import java.util.List;
 
 import level.Level;
 import utils.Direction;
 import utils.JsonUtil;
 import utils.Location;
 import utils.Velocity;
 import actions.ActionInterface;
 import app.RPGame;
 import attacks.BehaviorModifierContainer;
 
 import com.golden.gamedev.object.AnimatedSprite;
 import com.golden.gamedev.util.ImageUtil;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 import counters.Counter;
 import evented.Evented;
 import evented.EventedWrapper;
 
 /**
  * Basic class for all character game objects: enemies, automated characters,
  * and players.
  * 
  * Allows the developer to load directions from a JSON file (see Direction for
  * more info).
  * 
  * Contains a common current direction to be shared between the actions.
  * 
  * Contains Actions and Counters wrappers to execute the actions.
  * 
  * @author Kirill Klimuk
  */
 
 
 public class GameCharacter extends AnimatedSprite implements
 		CharacterInterface, Evented {
 
 
 	private static final long serialVersionUID = 1L;
 
 	private RPGame game;
 	private CharacterDecorator decorator;
 
 	private int curDirection = 0;
 
 	private Velocity velocity = new Velocity(0.07);
 
 	private List<Direction> directions;
 
 	private Velocity curVelocity = new Velocity(0.0);
 	protected Inventory inventory;
 
 	private String configURL;
 
 	Level level;
 
 
 	private EventedWrapper<Counter> counters = new EventedWrapper<Counter>(this);
 	private EventedWrapper<ActionInterface> actions = new EventedWrapper<ActionInterface>(
 			this);
 	private BehaviorModifierContainer behaviorModifiers = new BehaviorModifierContainer();
 
 	public static final int DIR_DOWN = 0;
 	public static final int DIR_UP = 1;
 	public static final int DIR_LEFT = 2;
 	public static final int DIR_RIGHT = 3;
 
 	public GameCharacter(RPGame game, Location loc, String configURL) {
 		super(loc.getX(), loc.getY());
 		this.game = game;
 		this.configURL = configURL;
 		initResources();
 	}
 
 	public void initResources() {
 		JsonObject directions = JsonUtil.getJSON(configURL);
 		constructDirections(directions);
 		stop();
 		inventory = new Inventory(this);
 
 	}
 
 	public void render(Graphics2D g) {
 		super.render(g);
 		counters.render(g);
 		actions.render(g);
 		inventory.render(g);
 	}
 
 	public Location getLocation() {
 		return new Location((int) this.getX(), (int) this.getY());
 	}
 
 	public void update(long elapsed) {
 		behaviorModifiers.setUpAll(elapsed);
 		
 		if (getDecorator() != null)
 			getDecorator().update(elapsed);
 
 		counters.update(elapsed);
 		actions.update(elapsed);
 		inventory.update(elapsed);
 		double[] velocity = curVelocity.get(getCurrentDirection());
 		setSpeed(velocity[0], velocity[1]);
 		super.update(elapsed);
 		
 		behaviorModifiers.unsetUpAll(elapsed);
 	}
 
 	public double[] getVelocity(int direction) {
 		return velocity.get(direction);
 	}
 
 	public double getSpeed() {
 		return velocity.getSpeed();
 	}
 
 	public void setVelocity(double speed) {
 		this.curVelocity.set(speed);
 	}
 
 	public RPGame getGame() {
 		return game;
 	}
 
 	
 	private void constructDirections(JsonObject dirs) {
 		JsonArray dirsDirections = dirs.getAsJsonArray("directions");
 		
 		Direction[] tempDirections = new Direction[4];
 		
 		for(int i=0; i<dirsDirections.size(); i++){
 			JsonObject direction = dirsDirections.get(i).getAsJsonObject();
			BufferedImage image = game.getImage(direction.get("image").getAsString());
 			BufferedImage[] images = ImageUtil.splitImages(image, dirs.get("frames").getAsInt(),
 					1);
 
 			int intepretedDirection = 0;
 
 			if (direction.get("direction").getAsString().equals("DIR_DOWN"))
 				intepretedDirection = GameCharacter.DIR_DOWN;
 			else if (direction.get("direction").getAsString().equals("DIR_UP"))
 				intepretedDirection = GameCharacter.DIR_UP;
 			else if (direction.get("direction").getAsString().equals("DIR_LEFT"))
 				intepretedDirection = GameCharacter.DIR_LEFT;
 			else if (direction.get("direction").getAsString().equals("DIR_RIGHT"))
 				intepretedDirection = GameCharacter.DIR_RIGHT;
 			else
 				throw new RuntimeException("Invalid direction specified");
 
 			tempDirections[intepretedDirection] = new Direction(this, images,
 					intepretedDirection, dirs.get("delay").getAsInt());
 		}
 		directions = Arrays.asList(tempDirections);
 	}
 
 	public EventedWrapper<ActionInterface> getActions() {
 		return actions;
 	}
 
 	public EventedWrapper<Counter> getCounters() {
 		return counters;
 	}
 
 	public boolean isCurrentDirection(int direction) {
 		return direction == curDirection;
 	}
 
 	public int getCurrentDirection() {
 		return curDirection;
 	}
 
 	public void setCurrentDirection(int direction) {
 		curDirection = direction;
 	}
 
 	public void setActiveDirection(int direction) {
 		curDirection = direction;
 		directions.get(direction).changeCharacter(true);
 	}
 
 	public void stop() {
 		curVelocity.set(0);
 		directions.get(curDirection).changeCharacter(false);
 	}
 
 	public Inventory getInventory() {
 		return inventory;
 	}
 
 	public BehaviorModifierContainer getBehaviorModifiers() {
 
 		return behaviorModifiers;
 	}
 	
 	public void setDecorator(CharacterDecorator decorator){
 		this.decorator = decorator;
 	}
 	
 	public CharacterDecorator getDecorator(){
 		return this.decorator;
 	}
 }
