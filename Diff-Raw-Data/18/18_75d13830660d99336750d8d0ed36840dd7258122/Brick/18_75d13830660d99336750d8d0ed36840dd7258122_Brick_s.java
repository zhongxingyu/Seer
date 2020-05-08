 package brickManager;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
 
 import sceneManager.EntityView;
 import sceneManager.SceneNode;
 
 public abstract class Brick implements EntityView {	
 	public enum BrickType {
 		SQUARE, TRIANGLE, SYSTEM;
 	}
 
 	protected BrickObject parent;
 	
 	protected BrickType brickType;
 	protected int index;
 	
 	protected Vector2D position;
 	
 	protected int health;
 	
 	protected float edgeLength;
 	protected int numSegments;
 	
 	protected Hashtable<String, SceneNode> sceneNodes = new Hashtable<String, SceneNode>();
 	
 	protected ArrayList<Float> vertices;
 	protected ArrayList<Float> normals;
 	protected ArrayList<Float> textureCoords;
 
 	protected boolean alive = true;
 	protected boolean exploding = false;
 	
 	/**
 	 * Must be greater than 0.
 	 */
	protected int explosionTimer = 10;
 	
 	public Brick(BrickType brickType, int index, int health, Vector2D position, float edgeLength, int numSegments) {
 		this.brickType = brickType;
 		this.index = index;
 		this.health = health;
 		this.position = position;
 		this.edgeLength = edgeLength;
 		this.numSegments = numSegments;
 	}
 
 	@Override
 	public ArrayList<SceneNode> getView() {
 		ArrayList<SceneNode> nodes = new ArrayList<SceneNode>(sceneNodes.values());
 		return nodes;
 	}
 	
 	public int getHealth() {
 		return health;
 	}
 	
 	public void setHealth(int health) {
 		this.health = health;
 	}
 	
 	public Vector2D getPosition() {
 		return position;
 	}
 	
 	public void setPosition(Vector2D position) {
 		this.position = position;
 	}
 	
 	public BrickType getBrickType() {
 		return brickType;
 	}
 	
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index) {
 		this.index = index;
 	}
 
 	public float getEdgeLength() {
 		return edgeLength;
 	}
 
 	public void setEdgeLength(float edgeLength) {
 		this.edgeLength = edgeLength;
 	}
 
 	public float getNumSegments() {
 		return numSegments;
 	}
 	
 	public boolean isAlive() {
 		return alive;
 	}
 
 	public void setAlive(boolean alive) {
 		this.alive = alive;
 	}
 
 	public boolean isExploding() {
 		return exploding;
 	}
 
 	public void setExploding(boolean exploding) {
 		this.exploding = exploding;
 	}
 
 	public int getExplosionTimer() {
 		return explosionTimer;
 	}
 
 	public void setExplosionTimer(int explosionTimer) {
 		this.explosionTimer = explosionTimer;
 	}
 	
 	public abstract ArrayList<Float> getVertices();
 	
 	public abstract ArrayList<Float> getNormals(float radius);
 	
 	public abstract ArrayList<Float> getTextureCoords();
 	
 	public abstract ArrayList<double[]> getLines();
 
 	public void setParent(BrickObject parent) {
 		this.parent = parent;
 	}
 }
