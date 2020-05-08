 package com.tacoid.gravicious.actors;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.tacoid.gravicious.Gravicious;
 import com.tacoid.gravicious.UnitConverter;
 import com.tacoid.gravicious.elements.Planet;
 import com.tacoid.gravicious.screens.AbstractGameScreen;
 
 public class PlanetActor extends ElementActor {
 	public Rectangle area;
 	public ShapeRenderer shapeRenderer;
 	public Planet planet;
 	private BodyDef bodyDef;
 
 	public PlanetActor(Planet p) {
 		super(p);
 		shapeRenderer = new ShapeRenderer();
 		planet = p;
 		setX(100);
 		setY(100);
 		bodyDef = new BodyDef();
 		bodyDef.type = BodyType.StaticBody;
 		bodyDef.position.set(UnitConverter.PixelToMeters(getX()), UnitConverter.PixelToMeters(getY()));
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
 		batch.end();
 			shapeRenderer.begin(ShapeType.FilledCircle);
 				shapeRenderer.setColor(new Color(0.25f, 0.61f, 0f, 1.0f));
 				shapeRenderer.filledCircle(this.getX(), this.getY(), planet.getRadius());
 			shapeRenderer.end();
 			shapeRenderer.begin(ShapeType.Circle);
 				shapeRenderer.setColor(Color.RED);
 				shapeRenderer.circle(this.getX(), this.getY(), planet.getInfluenceRadius());
 			shapeRenderer.end();
 			shapeRenderer.begin(ShapeType.Circle);
 				shapeRenderer.setColor(Color.GREEN);
 			shapeRenderer.circle(this.getX(), this.getY(), planet.getRadius() + 0.75f*(planet.getInfluenceRadius()-planet.getRadius()));
 			shapeRenderer.end();
 		batch.begin();
 	}
 
 	@Override
 	public Actor hit(float x, float y, boolean touchable) {
 		AbstractGameScreen screen = (AbstractGameScreen)Gravicious.getInstance().getScreen();
 
 		if(!screen.isEditor()) return null;
 		if (touchable && this.getTouchable() != Touchable.enabled) return null;
 
 		double distance = x*x + y*y;
		if (distance <= planet.getRadius()*planet.getRadius()){
 			return this;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void createBody(World world) {
 		body = this.planet.getLevel().getWorld().createBody(bodyDef);
 		
 		updateBody();
 		/*CircleShape dynamicCircle = new CircleShape();  
 		dynamicCircle.setRadius(this.planet.getRadius());  
 		FixtureDef fixtureDef = new FixtureDef();  
 		fixtureDef.shape = dynamicCircle;
 		fixtureDef.density = 1.0f;
 		fixtureDef.friction = 0.0f;  
 		fixtureDef.restitution = 1;
 		body.createFixture(fixtureDef);*/
 		body.setUserData(this.planet);
 	}
 
 	@Override
 	public void setX(float x) {
 		super.setX(x);
 		updateBodyTransform();
 	}
 
 	@Override
 	public void setY(float y) {
 		super.setY(y);
 		updateBodyTransform();
 	}
 
 	void updateBodyTransform() {
 		if(body != null) {
 			body.setTransform(new Vector2(UnitConverter.PixelToMeters(getX()), 
 										  UnitConverter.PixelToMeters(getY())), 
 							   0.0f);
 		}
 	}
 
 	@Override
 	public void updateBody() {
 		CircleShape dynamicCircle = new CircleShape();  
 		dynamicCircle.setRadius(UnitConverter.PixelToMeters(this.planet.getRadius()));  
 		FixtureDef fixtureDef = new FixtureDef();  
 		fixtureDef.shape = dynamicCircle;  
 		fixtureDef.density = 1.0f;  
 		fixtureDef.friction = 0.0f;  
 		fixtureDef.restitution = 1;  
 		body.createFixture(fixtureDef);
 	}
 }
