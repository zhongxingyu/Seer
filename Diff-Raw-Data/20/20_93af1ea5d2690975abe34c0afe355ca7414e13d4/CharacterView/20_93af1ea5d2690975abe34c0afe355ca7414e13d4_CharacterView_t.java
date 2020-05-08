 package view;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.joints.WeldJointDef;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 
 
 import utils.WorldUtils;
 
 import model.Character;
 
 public class CharacterView {
 	private Character character;
 	private Shape slickShape;
 	private CircleShape jBox2DCircle;
 	private Color color;
 	private Body characterBody;
 	
 	public CharacterView(Character character, WorldView worldView) {
 		this.character = character;
 		this.slickShape = new Circle((float)(this.character.getX()-(Character.RADIUS/2f)), 
 				(float)(this.character.getY()-(Character.RADIUS/2f)), Character.RADIUS);
 		this.color = Color.blue;
 		
 		jBox2DCircle = new CircleShape();
 		jBox2DCircle.m_radius = WorldUtils.pixel2Meter(Character.RADIUS);
 		
 		//ska inställningarna vara i controllern? vi tror det! :)
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.position.set(WorldUtils.pixel2Meter(character.getX()),WorldUtils.pixel2Meter(character.getY()));
 		bodyDef.type = BodyType.DYNAMIC;
 		bodyDef.fixedRotation = true;
 		
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = this.jBox2DCircle;
 		fixtureDef.density = 1f; //gör till konstanter TODO
 		fixtureDef.friction = 0.8f;
 		fixtureDef.restitution = 0f;
 		
		characterBody = worldView.getjBox2DWorld().createBody(bodyDef);
 		characterBody.createFixture(fixtureDef);
 		characterBody.m_mass = 35f;
 		
 		CircleShape antiFrictionShape = new CircleShape();
 		antiFrictionShape.m_radius = WorldUtils.pixel2Meter(Character.RADIUS);
 		
 		BodyDef leftAntiFrictionBodyDef = new BodyDef();
 		leftAntiFrictionBodyDef.position.set(WorldUtils.pixel2Meter(character.getX()-1), WorldUtils.pixel2Meter(character.getY()-1));
 		leftAntiFrictionBodyDef.type = BodyType.DYNAMIC;
 		
 		BodyDef rightAntiFrictionBodyDef = new BodyDef();
 		rightAntiFrictionBodyDef.position.set(WorldUtils.pixel2Meter(character.getX()+1), WorldUtils.pixel2Meter(character.getY()-1));
 		rightAntiFrictionBodyDef.type = BodyType.DYNAMIC;
 		
 		FixtureDef antiFrictionFixtureDef = new FixtureDef();
 		antiFrictionFixtureDef.shape = antiFrictionShape;
 		antiFrictionFixtureDef.density = 1f;
 		antiFrictionFixtureDef.friction = 0.1f;
 		antiFrictionFixtureDef.restitution = 0f;
 		
 		Body leftAntiFrictionBody = worldView.getjBox2DWorld().createBody(leftAntiFrictionBodyDef);
 		leftAntiFrictionBody.createFixture(antiFrictionFixtureDef);
 		leftAntiFrictionBody.m_mass = 1f;
 		leftAntiFrictionBody.setAwake(false);
 		
 		WeldJointDef leftWeldJointDef = new WeldJointDef();
 		leftWeldJointDef.collideConnected = false;
 		leftWeldJointDef.localAnchorA.set(0,0);
 		leftWeldJointDef.localAnchorB.set(0,0);
 		leftWeldJointDef.initialize(leftAntiFrictionBody, characterBody, characterBody.getWorldCenter());
 		worldView.getjBox2DWorld().createJoint(leftWeldJointDef);
 		
 		Body rightAntiFrictionBody = worldView.getjBox2DWorld().createBody(rightAntiFrictionBodyDef);
 		rightAntiFrictionBody.createFixture(antiFrictionFixtureDef);
 		rightAntiFrictionBody.m_mass = 1f;
 		rightAntiFrictionBody.setAwake(false);
 		
 		WeldJointDef rightWeldJointDef = new WeldJointDef();
 		rightWeldJointDef.collideConnected = false;
 		rightWeldJointDef.localAnchorA.set(0, 0);
 		rightWeldJointDef.localAnchorB.set(0, 0);
 		rightWeldJointDef.initialize(rightAntiFrictionBody, characterBody, characterBody.getWorldCenter());
 		worldView.getjBox2DWorld().createJoint(rightWeldJointDef);
 	}
 	
 	public org.newdawn.slick.geom.Shape getSlickShape() {
 		return this.slickShape;
 	}
 	
 	public Body getCharacterBody() {
 		return characterBody;
 	}
 	
 	public Color getColor() {
 		return this.color;
 	}
 	
 	public void setColor(Color color) {
 		this.color = color;
 		
 	}
 
 	public Character getCharacter() {
 		return this.character;
 	}
}
