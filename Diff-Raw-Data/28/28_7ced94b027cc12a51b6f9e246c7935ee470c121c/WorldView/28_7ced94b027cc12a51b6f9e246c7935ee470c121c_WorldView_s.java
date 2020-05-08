 package view;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Filter;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 
 public class WorldView {
 	private model.World world;
 	private CharacterView characterView;
 	private Vec2 gravity;
 	boolean doSleep;
 	private World jBox2DWorld;
 	private Body characterBody;
 	private Body groundBody;
 	
 	public WorldView(model.World world, CharacterView characterView) {
 		this.world = world;
 		this.characterView = characterView;
 		gravity = new Vec2(0.0f, 9.82f);
 		doSleep = true;
 		jBox2DWorld = new World(gravity, doSleep);
		//Need to convert world coordinates to pixels TODO
 		//ground
		addSolidGround(0, world.getWorldHeight(), world.getWorldWidth(), 1); //(x, y, width, height)
 		//left wall
		addSolidGround(-1, 0, 1, toPixelHeight(world.getWorldHeight()));
 		//right wall
		addSolidGround(world.getWorldWidth(), 0, 1, world.getWorldHeight());
 		//roof
		addSolidGround(-1, -1, world.getWorldHeight(), 1);
 		characterBody = jBox2DWorld.createBody(characterView.getBodyDef());
 		characterBody.createFixture(characterView.getFixtureDef());
 		characterBody.m_mass = 1000f;
 	}
 	
 	public CharacterView getCharacterView() {
 		return characterView;
 	}
 
 	public World getJBox2DWorld() {
 		return jBox2DWorld;
 	}
 	
 	public Body getCharacterBody() {
 		return characterBody;
 	}
 	
 	/*
 	 * Convert methods that allows us to change between world coordinates and pixels
 	 */
 	//World coordinate to pixel, x
 	public float toPixelPosX(float posX) {
 	    float x = world.getWorldWidth() * posX / 100.0f;
 	    return x;
 	}
 	
 	//Pixel to world coordinate, x
 	public float toPosX(float posX) {
 	    float x = (posX * 100.0f * 1.0f) / world.getWorldWidth();
 	    return x;
 	}
 	
 	//World coordinate to pixel, y
 	public float toPixelPosY(float posY) {
 	    float y = world.getWorldHeight() - (1.0f * world.getWorldHeight()) * posY / 100.0f;
 	    return y;
 	}
 	
 	//Pixel to world coordinate, y
 	public float toPosY(float posY) {
 	    return 100.0f - ((posY * 100 * 1.0f) / world.getWorldHeight()) ;
 	}
 	
 	//world width to pixel width
 	public float toPixelWidth(float width) {
 	    return world.getWorldWidth()*width / 100.0f;
 	}
 	
 	//world height to pixel height
 	public float toPixelHeight(float height) {
 	    return world.getWorldHeight() * height / 100.0f;
 	}
 	
 	/**
 	 * Add solid ground to prevent the character from moving outside of the window.
 	 */
 	private void addSolidGround(final float posX, final float posY, final float width, final float height) {
 		PolygonShape polygonShape = new PolygonShape();
 		polygonShape.setAsBox(width, height);
 		
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = polygonShape;
 		fixtureDef.friction = 0.1f;
 		fixtureDef.density = 1f;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(posX, posY);
		bodyDef.type = BodyType.STATIC;
		bodyDef.fixedRotation = true;
		
 		groundBody = jBox2DWorld.createBody(bodyDef);
 		groundBody.createFixture(fixtureDef);
 	}
 	
 }
