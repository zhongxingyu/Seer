 package entities.enemies;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import map.Cell;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;
 
 import utils.ImageUtils;
 import utils.MapLoader;
 import entities.Entity;
 import entities.NonPlayableEntity;
 import entities.players.Player;
 
 public class Enemy extends NonPlayableEntity{
 	
 	private static final int ENEMY_DEFAULT_LAYER = -20;
 	
 	private final Animation left, right;
 	private Animation sprite;
 	
 	/**
 	 * Map containing default representations of all enemies currently required.<br />
 	 * New enemy instances can be created by cloning instances already existing here.<br />
 	 * This is present in order to increase the ease with which enemies are loaded from xml.
 	 */
 	private static final Map<String,Enemy> enemies = new HashMap<String,Enemy>();
 
 	private Enemy(float width,float height, int maxhealth, Animation left, Animation right){
 		super(0,0,width,height,maxhealth);
 		this.left = left;
 		this.right = right;
 		sprite = right;
 	}
 	
 	private Enemy(float x, float y, float width,float height, int maxhealth, Animation left, Animation right){
 		super(x,y,width,height,maxhealth);
 		this.left = left;
 		this.right = right;
 		sprite = right;
 	}
 
 	@Override
 	public Enemy clone() {
 		return new Enemy(getX(), getY(), getWidth(), getHeight(),getMaxHealth(), left, right);
 	}
 
 	/**
 	 * Gets a new instance of the object.<br />
 	 * Implementations should not be case sensitive and not allow common references between entities.
 	 * @param name The string representation of the entity to be created.
 	 * @return A new INonPlayableEntity object.
 	 */
 	public static Enemy getNewEnemy(String name){
 		if(name == null){
 			return null;
 		}
 		return (Enemy) enemies.get(name.toLowerCase()).clone();
 	}
 	
 	/**
 	 * Gets a new instance of the object.<br />
 	 * Implementations should not be case sensitive and not allow common references between entities.
 	 * @param name The string representation of the entity to be created.
 	 * @param x The x coordinate of the newly created enemy.
 	 * @param y The y coordinate of the newly created enemy.
 	 * @return A new INonPlayableEntity object.
 	 */
 	public static Enemy getNewEnemy(Cell currentCell, String name, float x, float y){
 		if(name == null){
 			return null;
 		}
 		Enemy base = enemies.get(name.toLowerCase());
 		return new Enemy(x,y, base.getWidth(), base.getHeight(),base.getMaxHealth(),base.left,base.right);
 	}
 	
 	/**
 	 * Resets the entity storage such that (A)x.getNewEnemy(x) = null;
 	 */
 	public static void clearLoadedEnemies(){
 		enemies.clear();
 	}
 	
 	/**
 	 * Adds an enemy to the enemy copy storage with the specified name.<br />
 	 * Names are not treated as case-sensitive.
 	 */
 	public static void loadEnemy(String name, Enemy e){
 		if(name != null){
 			enemies.put(name.toLowerCase(), e);
 		}
 	}
 	
 	/**
 	 * Creates a new enemy from an XML node. This should not be used except when loading enemies into the enemy template storage.
 	 * @throws ParseException 
 	 * @throws SlickException 
 	 * @throws DOMException 
 	 */
 	public static void loadEnemy(Node node) throws ParseException, DOMException, SlickException {
 		NamedNodeMap attrs = node.getAttributes();
 		
 		String name = attrs.getNamedItem("name").getNodeValue();
 		int health = Integer.parseInt(attrs.getNamedItem("maxhealth").getNodeValue());
 		
 		//set up hitbox
 		float width = 1, height = 1;
 		try{
 			width = Float.parseFloat(attrs.getNamedItem("width").getNodeValue());
 		}catch(NullPointerException e){ }
 		try{
 			height = Float.parseFloat(attrs.getNamedItem("width").getNodeValue());
 		}catch(NullPointerException e){ }
 		
 		//set up animation
 		Animation leftAni,rightAni;
 		{
 			Image[] leftImages,rightImages;
 			Node leftImagesNode = ((Element) node).getElementsByTagName("leftimages").item(0);
 			Node rightImagesNode = ((Element) node).getElementsByTagName("rightimages").item(0);
 			int duration;
 			if(leftImagesNode == null){
 				if(rightImagesNode == null){
 					throw new ParseException("Must have at least either 'leftimages' or 'rightimages' tag defined.");
 				}else{
 					rightImages = ImageUtils.loadImages(rightImagesNode);
 					leftImages = ImageUtils.flipImages(rightImages, true, false);
 					duration = Integer.parseInt(rightImagesNode.getAttributes().getNamedItem("duration").getTextContent());
 				}
 			}else {
 				if(rightImagesNode == null){
 					leftImages = ImageUtils.loadImages(leftImagesNode);
 					rightImages = ImageUtils.flipImages(leftImages, true, false);
 					duration = Integer.parseInt(leftImagesNode.getAttributes().getNamedItem("duration").getTextContent());
 				}else{
 					leftImages = ImageUtils.loadImages(leftImagesNode);
 					rightImages = ImageUtils.loadImages(rightImagesNode);
 					duration = Integer.parseInt(rightImagesNode.getAttributes().getNamedItem("duration").getTextContent());
 					duration += Integer.parseInt(leftImagesNode.getAttributes().getNamedItem("duration").getTextContent());
 					duration /= 2;
 				}
 			}
 			leftAni = new Animation(leftImages, duration);
 			rightAni = new Animation(rightImages, duration);
 		}
 		Enemy e = new Enemy(width,height,health, leftAni, rightAni);
 		loadEnemy(name, e);
 	}
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) {
 		
 		checkMapChanged();
 		
 	    if (isDead()) {
 	        MapLoader.getCurrentCell().removeEntity(this);
 	        return;
 	    }
 		super.updateEntity();
 		if(getdX() < 0){
 			sprite = left;
 		}else if(getdX() > 0){
 			sprite = right;
 		}
 		Player p = MapLoader.getCurrentCell().getPlayer();
 		if (intersects(p)) {
 		    p.takeDamage(1);
 		}
 		sprite.update(DELTA);
 		frameMove();
 	}
 	
 	public void checkMapChanged() {
 		Cell currentCell = MapLoader.getCurrentCell();
 		//check top
 		if ((getY() < 1 && getdY() < 0) || (getX() >= currentCell.getWidth() - (1 + getWidth()) && getdX() > 0) || 
 		(getY() >= currentCell.getHeight() - (1 + getHeight()) && getdY() > 0) || (getX() < 1 && getdX() < 0)) {
 			currentCell.removeEntity(this);
 		}
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) {
		renderSprite(sprite,0,0);
		renderHealthBar(-4);
 	}
 
 	@Override
 	public void collide(Entity e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getLayer() {
 		return ENEMY_DEFAULT_LAYER;
 	}
 
 }
