 package game;
 
 import gameManager.GameManager;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import javax.media.opengl.GL3bc;
 
 import modelManager.TextureLoader;
 import objectManager.ObjectChangeEvent;
 import objectManager.ObjectChangeEvent.ObjectChangeType;
 import objectManager.ObjectType;
 
 import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
 
 import physicsManager.PhysicalObject;
 import sceneManager.SceneNode;
 import utilityManager.MathBox;
 import aiManager.Agent;
 import aiManager.AgentInput.AgentInputType;
 import aiManager.AgentInputAttack;
 import aiManager.AgentInputMove;
 import brickManager.Brick;
 import brickManager.Brick.BrickType;
 import collisionManager.Collidable;
 
 import com.jogamp.opengl.util.texture.Texture;
 
 public class TestShip extends Agent {
 	private ArrayList<TestWeapon> weapons = new ArrayList<TestWeapon>();
 	
 	public TestShip(Hashtable<String, Double> values, ArrayList<Brick> bricks, ArrayList<ArrayList<Integer>> adjacencyList) {
 		super(ObjectType.AGENT, null, values, bricks, adjacencyList);
 	}
 
 	@Override
 	public void updateView() {
 		if (sceneNodes.get("root") == null) {
 			SceneNode root = new SceneNode(null) {
 				private Texture metalTexture;
 				
 				@Override
 				public void update(GL3bc gl) {
 					if (metalTexture == null) {
 						TextureLoader.loadTexture(gl, "metal1", "data/border.png");
 						metalTexture = TextureLoader.getTexture("metal1");
 					}
 					metalTexture.enable(gl);
 					metalTexture.bind(gl);
 					
 					gl.glPushMatrix();
 
 					// translate and rotate
 					gl.glTranslatef((float) position.getX(), (float) position.getY(), 0);
 					gl.glRotatef((float) (orientation * 180 / Math.PI), 0, 0, 1);
 					
 					for (int i = 0; i < bricks.size(); i++) {
 						Brick brick = bricks.get(i);
 						brick.updateView();
 						
 						for (SceneNode brickView : brick.getView()) {
 							gl.glPushMatrix();
 							if (brick.isExploding()) {
 								gl.glRotatef((float) -(orientation * 180 / Math.PI), 0, 0, 1);
 							}
 							brickView.update(gl);
 							gl.glPopMatrix();
 						}
 					}
 					
 					gl.glPopMatrix();
 					
 					if (selected && currentOrder != null) {
 						Vector2D target = null;
 						if (currentOrder.getAgentInputType() == AgentInputType.MOVE) { 
 							gl.glColor4f(0.0f, 1.0f, 0.0f, 0.4f);
 							target = ((AgentInputMove) currentOrder).getDestination();
 						}
 						else if (currentOrder.getAgentInputType() == AgentInputType.ATTACK) {
 							gl.glColor4f(1.0f, 0.0f, 0.0f, 0.4f);
 							target = ((AgentInputAttack) currentOrder).getTarget().getPosition();
 						}
 						
 						gl.glEnable(GL3bc.GL_BLEND);
 						gl.glLineWidth(1.0f);
 						
 						gl.glPushMatrix();
 							gl.glTranslatef(0.0f, 0.0f, 0.0f);	
 							gl.glBegin(GL3bc.GL_LINES);
 								gl.glVertex3d(position.getX(), position.getY(), 0.0d);
 								gl.glVertex3d(target.getX(), target.getY(), 0.0d);
 							gl.glEnd();
 						gl.glPopMatrix();
 						
 						gl.glPushMatrix();
 							gl.glTranslated(target.getX(), target.getY(), 0.0d);
 							double r = 0;
 							gl.glBegin(GL3bc.GL_POLYGON);
 								for (int i = 0; i < 360; i++) {
 									r = i * Math.PI/180;
 									gl.glVertex3d(Math.cos(r) * 5, Math.sin(r) * 5, 1.0);
 								}
 							gl.glEnd();
 						gl.glPopMatrix();
 						
 						gl.glDisable(GL3bc.GL_BLEND);
 					}
 				}
 			};
 			sceneNodes.put("root", root);
 		}
 	}
 
 	//TODO this is pretty general stuff, things like processing a move order etc. should be moved to the engine
 	@Override
 	public void update() {
		if (getBrickFromIndex(0).getHealth() <= 0) {
 			setAlive(false);
 		}
 		
 		updateBricks();
 		
 		ArrayList<PhysicalObject> newGameObjects = new ArrayList<PhysicalObject>();
 		
 		if (!inputQueue.isEmpty()) {
 			currentOrder = inputQueue.remove(0);
 		}
 		
 		if (currentOrder != null) {
 			if (currentOrder.getAgentInputType() == AgentInputType.MOVE) {
 				moveTo(((AgentInputMove) currentOrder).getDestination());
 			}
 			else if (currentOrder.getAgentInputType() == AgentInputType.ATTACK) {
 				AgentInputAttack currentAttackOrder = (AgentInputAttack) currentOrder;
 				
 				PhysicalObject target = currentAttackOrder.getTarget();
 				
 				if (target.isAlive()) {
 					if (target.getPosition().subtract(position).getNorm() < 300.0) {
 						attack(((AgentInputAttack) currentOrder).getTarget());
 						for (TestWeapon weapon : weapons) {
 							weapon.setPosition(position);
 							weapon.setOrientation(orientation);
 							weapon.setVelocity(velocityVec);
 							
 							weapon.update();
 							newGameObjects.addAll(weapon.getProjectiles());
 							weapon.clearProjectiles();
 						}
 					}
 					else {
 						follow(target);
 					}
 				}
 				else {
 					currentOrder = null;
 				}
 			}
 		}
 		
		if (bricks.isEmpty()) {
			setAlive(false);
		}
		
 		if (!newGameObjects.isEmpty()) {
 			updateObservers(new ObjectChangeEvent(this, ObjectChangeType.CREATION, newGameObjects));
 		}
 		newGameObjects.clear();
 	}
 	
 	public ArrayList<TestWeapon> getWeapons() {
 		return weapons;
 	}
 	
 	public void addWeapon(TestWeapon weapon) {
 		weapons.add(weapon);
 	}
 
 	@Override
 	public void collide(Collidable collider) {
 		Brick closest = getClosestBrick(collider.getPosition());
 
 		closest.setHealth(closest.getHealth() - 10);
 	}
 
 	@Override
 	public double getRadius() {
 		if (bricks.isEmpty()) {
 			return 0.0;
 		}
 		
 		Brick max = bricks.get(0);
 		for (Brick brick : bricks) {
 			if (!brick.isExploding() && brick.isAlive() && brick.getPosition().getNorm() > max.getPosition().getNorm()) {
 				max = brick;
 			}
 		}
 		
 		if (max.getBrickType() == BrickType.SQUARE) {
 			return max.getPosition().getNorm() + Math.sqrt(max.getEdgeLength()*2 + max.getEdgeLength()*2);
 		}
 		else if (max.getBrickType() == BrickType.TRIANGLE) {
 			return max.getPosition().getNorm();
 		}
 		else {
 			return -1;
 		}
 	}
 
 	@Override
 	protected PhysicalObject createFragment(ArrayList<Brick> fragmentBricks, ArrayList<ArrayList<Integer>> adjacencyList) {
 		//get average position of fragment bricks
 		Vector2D averagePosition = new Vector2D(0.0, 0.0);
 		for (Brick brick : fragmentBricks) {
 			averagePosition = averagePosition.add(brick.getPosition());
 		}
 		averagePosition = averagePosition.scalarMultiply(1.0/fragmentBricks.size());
 		
 		//adjust position of bricks to be relative to new average position
 		for (Brick brick : fragmentBricks) {
 			brick.setPosition(brick.getPosition().subtract(averagePosition));
 		}
 		
 		//update brick indices
 		for (int i = 0; i < fragmentBricks.size(); i++) {
 			//change adjacency list
 			for (int j = 0; j < adjacencyList.size(); j++) {
 				if (adjacencyList.get(j).contains(new Integer(fragmentBricks.get(i).getIndex()))) {
 					adjacencyList.get(j).remove(new Integer(fragmentBricks.get(i).getIndex()));
 					adjacencyList.get(j).add(new Integer(i));
 				}
 			}
 			//change brick index
 			fragmentBricks.get(i).setIndex(i);
 		}
 		
 		//set averagePosition of bricks to an absolute position
 		averagePosition = MathBox.rotatePoint(averagePosition, orientation).add(position);
 		
 		//generate new values
 		Hashtable<String, Double> values = new Hashtable<String, Double>();
 		values.put("mass", 5000.0);
 		values.put("positionX", averagePosition.getX());
 		values.put("positionY", averagePosition.getY());
 		values.put("orientation", orientation);
 		values.put("forceX", Math.random() * MathBox.nextSign());
 		values.put("forceY", Math.random() * MathBox.nextSign());
 		values.put("forceMagnitude", Math.random() * 100.0);
 		values.put("turningForce", Math.random() * 0.1 * MathBox.nextSign());
 		values.put("velocityX", 0.0);
 		values.put("velocityY", 0.0);
 		values.put("turningVelocity", 0.0);
 		values.put("maxVelocity", 0.5);
 		values.put("maxTurningVelocity", 0.1);
 		values.put("maxForce", 1.0);
 		values.put("maxTurningForce", 1.0);
 		
 		TestShipFragment fragment = new TestShipFragment(this, values, fragmentBricks, adjacencyList);
 		fragment.addObserver(GameManager.getObjectManager());
 		return fragment;
 	}
 
 	@Override
 	public boolean canCollide() {
 		if (alive) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 }
