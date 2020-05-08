 package cargame.elements;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import utils.Box2DUtils;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Manifold;
 
 import cargame.CarGame;
 import cargame.screens.GameScreen;
 
 public class TrackSensor implements ContactListener {
 
 	private GameScreen game;
 	private Body sensor1;
 	private Body sensor2;
 	
	private Map<Car,Boolean> wrongDirection;
 	
	private Map<Car,Boolean> passedSensor1;
	private Map<Car,Boolean> passedSensor2;
 	
 	public TrackSensor(GameScreen game) {
 		super();
 		
 		this.game = game;
 		
 		// Finish line sensors
		
 		passedSensor1 = new HashMap<Car, Boolean>();
 		passedSensor2 = new HashMap<Car, Boolean>();
 		sensor2 = Box2DUtils.createPolygonBody(game.getWorld(), new Vector2(135, 25), 1f, 15f, 0f, 0f, 0f, false,true);
 		sensor1 = Box2DUtils.createPolygonBody(game.getWorld(), new Vector2(140, 25), 1f, 15f, 0f, 0f, 0f, false,true);
 		
		wrongDirection =  new HashMap<Car, Boolean>();
		
 		// Move cars to start line
 		for(int i=0;i<game.getElements().size();i++){
 			Car car = (Car) game.getElements().get(i);
 			if(car != null){
				wrongDirection.put(car, true);
 				car.getBody().setTransform(sensor1.getPosition().x+10, sensor1.getPosition().y, (float)( sensor1.getAngle()-Math.PI/2.0));
 			}
 			
 		}
 	}
 
 	@Override
	public void endContact(Contact contact) {
 
 		// Finish line sensors
 		Car car = getCarCollision(contact,sensor1);
         if(car != null){
         	passedSensor1.put(car, true);
         	if(passedSensor2.containsKey(car) && passedSensor2.get(car)){
        		if(!wrongDirection.get(car)){
        			wrongDirection.put(car,true); 
         		}
         		
         		passedSensor2.put(car, false);
         		passedSensor1.put(car, false);
         	}
         }
         
         car = getCarCollision(contact,sensor2);
         if(car != null){
         	passedSensor2.put(car, true);
         	if(passedSensor1.containsKey(car) && passedSensor1.get(car)){
        		if(wrongDirection.get(car)){
        			wrongDirection.put(car, false);
         		}else{
         			car.addLap();
         		}
         		passedSensor1.put(car, false);
         		passedSensor2.put(car, false);
         	}
         }
 	}
 
 	@Override
	public void beginContact(Contact contact) {
 	}
 	
 	private Car getCarCollision(Contact contact,Body sensor){
 		List<Element> gameElements = game.getElements();
 		
 		for(Element element: gameElements){
 			if(contact.getFixtureA().getBody().equals(element.getBody()) && contact.getFixtureB().getBody().equals(sensor)){
 				return (Car) element;
 			}
 			if(contact.getFixtureB().getBody().equals(element.getBody()) && contact.getFixtureA().getBody().equals(sensor)){
 				return (Car) element;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void postSolve(Contact arg0, ContactImpulse arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void preSolve(Contact arg0, Manifold arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 }
