 package org.linesofcode.antfarm.sceneObjects;
 
 import org.linesofcode.antfarm.AntFarm;
 import org.linesofcode.antfarm.behavior.SeekBehavior;
 import org.linesofcode.antfarm.behavior.SteeringBehavior;
 import org.linesofcode.antfarm.behavior.WanderingBehavior;
 import org.linesofcode.antfarm.exception.OutOfBoundsException;
 import org.linesofcode.antfarm.exception.PathIsBlockedException;
 import org.linesofcode.antfarm.exception.OutOfBoundsException.Direction;
 
 import processing.core.PVector;
 
 import java.awt.Color;
 
 public class Ant implements SceneObject, Obstacle {
 
     public static float SIZE = 4f;
 
     public static float TRAIL_INTERVAL = .75f;
 
     public static float MAX_TIME_TO_LIVE = 240f;
     public static float MIN_TIME_TO_LIVE = 120f;
     public static float MAX_WANDERING_TIME = 90f;
     public static float MIN_WANDERING_TIME = 60f;
     public static float MAX_IDLE_TIME = 5f;
 
     public static float MOVEMENT_RATE = 30f;
     public static float VIEW_DISTANCE = 30f;
 
     public static float FIELD_OF_VIEW = 120f;
 
     private final AntFarm antFarm;
     private final Hive hive;
     private PVector position;
     private PVector viewDirection = new PVector(0f, 0f);
     private boolean visible = false;
     private int color;
     private float rotation = 0f;
     
     private AntState state;
     private boolean carriesFood;
     private float timeToLive;
     private float speedMultiplier;
     private float idleTime;
     private float wanderingTime;
     private float maxWanderingTime;
     private SteeringBehavior behavior;
     private BoundingBox bounds;
 	private boolean overrideBehavior;
 	private Food foodTarget;
 	private float trailTime = 0;
 
     public Ant(AntFarm antFarm, Hive hive) {
         this.antFarm = antFarm;
         this.hive = hive;
         color = hive.getColor();
         timeToLive = antFarm.random(MIN_TIME_TO_LIVE, MAX_TIME_TO_LIVE);
         speedMultiplier = antFarm.random(0.75f, 1.25f);
         enterHive();
     }
 
     public void update(float delta) {
 
     	timeToLive -= delta;
     	if(timeToLive <= 0) {
     		die();
     		return;
     	}
     	
         switch(state) {
         case IDLE: {
         	idleTime += delta;
         	if(idleTime >= MAX_IDLE_TIME) {
         		leaveHive();
         	}
         	return;
         }
         case WANDERING: {
         	// TODO trail found
         	wanderingTime += delta;
         	if(wanderingTime >= maxWanderingTime) {
         		returnHome();
         		break;
         	}
         	Food food = antFarm.getFoodInProximity(this);
         	if(food != null) {
         		approachFood(food);
         	}
         	break;
         }
         case APPROACHING_FOOD: {
         	if(isFoodClose(foodTarget)) {
         		pickupFood(foodTarget);
         		foodTarget = null;
         		returnHome();
         	}
         	break;
         }
         case RETURNING_HOME: {
         	if(isNearHive()) {
         		enterHive();
         		break;
         	}
         	if(carriesFood) {
         		trailTime -= delta;
         		if(trailTime <= 0) {
         			putTrail();
         			trailTime = TRAIL_INTERVAL;
         		}
         	}
         	break;
         }
         case FOLLOWING_TRAIL: {
         	// TODO check for trail
         	// TODO if no trail, wander
         	break;
         }
         default: {
         	throw new RuntimeException("Unrecognized ant state");
         }
         }
         
         behavior.update(delta);
         if(!overrideBehavior) {
         	turn(behavior.getRotationDelta());
         }
         computeViewDirection();
         move(delta);
         bounds = new BoundingBox(position, rotation, new PVector(-SIZE, SIZE), new PVector(0, -SIZE), new PVector(SIZE, SIZE));
     }
 
     private boolean isFoodClose(Food foodTarget) {
     	float dx = Math.abs(position.x - foodTarget.getPosition().x);
     	float dy = Math.abs(position.y - foodTarget.getPosition().y);
 		return dx <= Food.SIZE/2 && dy <= Food.SIZE/2;
 	}
 
 	private void approachFood(Food food) {
 		foodTarget = food;
 		state = AntState.APPROACHING_FOOD;
 		behavior = new SeekBehavior(food.getPosition(), this);
 	}
 
 	private boolean isNearHive() {
     	float dx = Math.abs(position.x - hive.getCenter().x);
     	float dy = Math.abs(position.y - hive.getCenter().y);
 		return dx <= (Hive.SIZE / 2) && dy <= (Hive.SIZE / 2);
 	}
 
 	private void computeViewDirection() {
 		viewDirection.x = AntFarm.sin(rotation);
 		viewDirection.y = -AntFarm.cos(rotation);
 		viewDirection.normalize();
 	}
 
     private void move(float delta) {
     	
     	PVector velocity = PVector.mult(viewDirection, MOVEMENT_RATE * delta);
     	velocity.mult(speedMultiplier);
     	PVector newPosition = PVector.add(position, velocity);
     	
     	if(antFarm.isPathBlocked(this, newPosition)) {
     		// FIXME doesn't work; see #25
     		overrideBehavior = true;
     		turn((float)Math.toRadians(-90.0));
     		return;
     	}
     	
     	try {
 			antFarm.moveAnt(this, newPosition);
 			overrideBehavior = false;
 		} catch (OutOfBoundsException e) {
 			dodgeBounds(e.getDirection());
 		} catch (PathIsBlockedException e) {
 			// movement is prohibited
 		}
     }
     
 	private void dodgeBounds(Direction direction) {
 		overrideBehavior = true;
 		if(direction == OutOfBoundsException.Direction.Y_AXIS) {
 			rotation = (float)Math.toRadians(180.0) - rotation;
 			return;
 		}
 		if(direction == OutOfBoundsException.Direction.X_AXIS) {
 			rotation = -rotation;
 			return;
 		}
 	}
 
 	private void turn(float delta) {
 		rotation += delta;
 	}
     
 	public void draw() {
 
     	if(!visible) {
     		return;
     	}
     	
     	antFarm.translate(position.x, position.y);
         antFarm.rotate(rotation);
         
         if(antFarm.isDrawViewDirectionEnabled()) {
         	antFarm.stroke(Color.RED.getRGB());
         	antFarm.line(0, 0, 0, -4f * SIZE);
     	}
         
         antFarm.stroke(0);
         antFarm.fill(color);
        antFarm.strokeWeight(1f);
 
         antFarm.beginShape();
         antFarm.vertex(-SIZE, SIZE);
         antFarm.vertex(0, -SIZE);
         antFarm.vertex(SIZE, SIZE);
         antFarm.endShape();
         
         if(carriesFood) {
         	antFarm.stroke(Food.OUTLINE_COLOR);
         	antFarm.fill(Food.COLOR);
         	antFarm.ellipse(0, -SIZE, Food.SIZE, Food.SIZE);
         }
 
         antFarm.rotate(-rotation);
         antFarm.translate(-position.x, -position.y);
     }
 
 	private void putTrail() {
 		antFarm.putPheromone(this);
 	}
 
     private void pickupFood(Food food) {
     	food.pickUp();
     	carriesFood = true;
     }
 
     private void enterHive() {
     	hive.pickUpAnt();
     	if(carriesFood) {
 	    	hive.putFood();
 	    	carriesFood = false;
     	}
     	visible = false;
         bounds = null;
     	idle();
     }
     
     private void leaveHive() {
     	position = hive.getSpawnPosition();
 
     	visible = true;
     	
     	PVector distance = PVector.sub(position, hive.getCenter());
     	distance.normalize();
     	PVector up = new PVector(0f, 1f);
     	rotation = PVector.angleBetween(distance, up);
     	
     	wanderingTime = 0f;
     	maxWanderingTime = antFarm.random(MIN_WANDERING_TIME, MAX_WANDERING_TIME);
     	wander();
     }
 
     private void wander() {
     	state = AntState.WANDERING;
     	behavior = new WanderingBehavior();
     }
 
     private void returnHome() {
     	state = AntState.RETURNING_HOME;
     	behavior = new SeekBehavior(hive.getCenter(), this);
     }
 
     private void idle() {
     	idleTime = 0;
     	state = AntState.IDLE;
     }
 
     public void die() {
     	visible = false;
     	antFarm.removeAnt(this);
     	hive.decreaseAnts();
     }
 
     public AntFarm getAntFarm() {
         return antFarm;
     }
 
     public PVector getPosition() {
         return position;
     }
 
 	public PVector getViewDirection() {
 		return viewDirection;
 	}
 
     public Hive getHive() {
         return hive;
     }
 
     @Override
     public BoundingBox getBoundingBox() {
         return bounds;
     }
 
 	public float getHeading() {
 		return rotation;
 	}
 
 	public void setHeading(float angle) {
 		rotation = angle;
 	}
 
 	public void setPosition(PVector newPosition) {
 		position = newPosition;
 	}
 
 	public float getRotation() {
         return rotation;
     }
 	
 	public float getRelativeStamina() {
 		return wanderingTime / maxWanderingTime;
 	}
 }
