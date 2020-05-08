 package game;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import components.Component;
 import components.RenderComponent;
 
 public class Entity {
 
 	private String id;
 
 	private Vector2f position;
 	private float scale;
 	private float rotation;
 	private int health;
 	private float radius;
 	//
 	private RenderComponent renderComponent = null;
 	private ArrayList<Component> components = null;
 
 	public Entity(String id) {
 		this.id = id;
 
 		components = new ArrayList<Component>();
 
 		position = new Vector2f(0, 0);
 		scale = 1;
 		rotation = 0;
 	}
 
 	public void AddComponent(Component component) {
 		if (RenderComponent.class.isInstance(component))
 			renderComponent = (RenderComponent) component;
 
 		component.setOwnerEntity(this);
 		components.add(component);
 	}
 
 	public Component getComponent(String id) {
 		for (Component comp : components) {
 			if (comp.getID().equalsIgnoreCase(id))
 				return comp;
 		}
 
 		return null;
 	}
 
 	public Vector2f getPosition() {
 		return position;
 	}
 
 	public float getScale() {
 		return scale;
 	}
 
 	public float getRotation() {
 		return rotation;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setPosition(Vector2f position) {
 		this.position = position;
 	}
 
 	public void setRotation(float rotate) {
 		rotation = rotate;
 	}
 
 	public void setScale(float scale) {
 		this.scale = scale;
 	}
 
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		for (Component component : components) {
 			component.update(gc, sb, delta);
 		}
 	}
 
 	public void render(GameContainer gc, StateBasedGame sb, Graphics gr) {
 		if (renderComponent != null)
 			renderComponent.render(gc, sb, gr);
 	}
 	
 	public void setHealth(int health){
 		this.health = health;
 	}
 	
 	public int getHealth(){
 		return health;
 	}
 	
 	public float getRadius(){
 		return radius;
 	}
 	
	public void setRedius(float radius){
 		this.radius = radius;
 	}
 	
 	public void damage(int damage){
 		this.health -= damage;
 	}
 	
 	public void heal(int heal){
 		this.health += heal;
 	}
 }
