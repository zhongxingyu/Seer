 package fr.umlv.escape.world;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 
 /** Class that supplies some method to manipulate {@link Body}
  */
 public class Bodys {
 	private Bodys(){
 	}
 
 	/**
 	 * Create a basic rectangle body in the {@link EscapeWorld}.
 	 * 
 	 * @param posX position x of the body to create.
 	 * @param posY position y of the body to create.
 	 * @param width width of the body to create.
 	 * @param height height of the body to create.
 	 * @param type type of the body to create. (0 - DYNAMIC, 1 - KINEMATIC, 2 - STATIC).
 	 * @return The body created.
 	 */
 	public static Body createBasicRectangle(float posX, float posY, float width, float height,int type){
		System.out.println("Body Rect "+posX+"-"+posY);
 		if(type>2 || type<0){
 			throw new IllegalArgumentException("must be between 0 and 2");
 		}
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.position.set(posX/EscapeWorld.SCALE,posY/EscapeWorld.SCALE);
 		PolygonShape dynamicBox = new PolygonShape();
 		FixtureDef fixtureDef = new FixtureDef();
 		
 		switch(type){
 		case 0:
 			bodyDef.type = BodyType.DYNAMIC;
 			dynamicBox.setAsBox((width/EscapeWorld.SCALE)/2, (height/EscapeWorld.SCALE)/2);
 			fixtureDef.isSensor=true;
 			break;
 		case 1:
 			bodyDef.type = BodyType.KINEMATIC;
 			break;
 		default: 
 			bodyDef.type = BodyType.STATIC;
 			dynamicBox.setAsBox((width/EscapeWorld.SCALE), (height/EscapeWorld.SCALE));
 			break;
 		}
 		fixtureDef.shape = dynamicBox;
 		fixtureDef.density=1f;
 		fixtureDef.friction=0f;
 		fixtureDef.restitution=0f;
 		Body body = EscapeWorld.getTheWorld().createBody(bodyDef);
 		EscapeWorld.getTheWorld().createFixture(body, fixtureDef);
 		body.setActive(true);
 		return body;
 	}
 	
 	/**
 	 * Create a basic circle body in the {@link EscapeWorld}.
 	 * 
 	 * @param posX position x of the body to create.
 	 * @param posY position y of the body to create.
 	 * @param radius of the body to create.
 	 * @param type type of the body to create. (0 - DYNAMIC, 1 - KINEMATIC, 2 - STATIC).
 	 * @return The body created.
 	 */
 	public static Body createBasicCircle(float posX, float posY, float radius,int type){
		System.out.println("Body Circle "+posX+"-"+posY);
 		if(type>2 || type<0){
 			throw new IllegalArgumentException("must be between 0 and 2");
 		}
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.position.set(posX/EscapeWorld.SCALE,posY/EscapeWorld.SCALE);  
 		CircleShape cs = new CircleShape();
 		FixtureDef fixtureDef = new FixtureDef();
 		
 		switch(type){
 		case 0:
 			bodyDef.type = BodyType.DYNAMIC;
 			cs.m_radius = (radius/EscapeWorld.SCALE);
 			fixtureDef.isSensor=true;
 			break;
 		case 1:
 			bodyDef.type = BodyType.KINEMATIC;
 			break;
 		default:
 			bodyDef.type = 	BodyType.STATIC;
 			cs.m_radius = (radius/EscapeWorld.SCALE)/2;
 			break;
 		}
 		fixtureDef.shape = cs;
 		fixtureDef.density = 1f;
 		fixtureDef.friction = 0f;
 		fixtureDef.restitution = 0f;
 		Body body = EscapeWorld.getTheWorld().createBody(bodyDef);
 		EscapeWorld.getTheWorld().createFixture(body, fixtureDef);
 		return body;
 	}
 }
