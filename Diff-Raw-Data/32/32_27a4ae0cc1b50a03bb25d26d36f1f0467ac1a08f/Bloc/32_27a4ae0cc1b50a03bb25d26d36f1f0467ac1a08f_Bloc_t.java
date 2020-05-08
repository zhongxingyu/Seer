 /*
  * Project : JAVA: YouRobot
  * ESIPE-MLV
  * 
  * VERNEAU Julien
  * ANTOINE Sébastien
  * 
  * 
  * IR2 - DECEMBER 2011
  * 
  */
 package fr.sebgghb22.droidarena.gdx.game.item;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 
 import fr.sebgghb22.droidarena.gdx.utils.Option;
 
 /**
  * The Class Bloc permit to create an object in the arena
  */
 public class Bloc implements Item  {
 
 
 	/** The body of the bloc */
 	protected Body body;
 
 	/** The definition of the body */
 	private BodyDef bd;
 
 	/** The properties of the bloc */
 	protected Properties p;
 	
 	protected Texture tex;
 	
 	protected String img;
 
 	/**
 	 * Instantiates a new bloc.
 	 *
 	 * @param p the properties of the new blocs
 	 * @param x the abscissa position of the new bloc
 	 * @param y the ordinate position of the new bloc
 	 */
 	public Bloc(Properties p ,float x, float y){
 		bd = new BodyDef();
 		this.p = p;
 		this.img= p.getImg();
 		bd.type = p.getBodyType();
		bd.position.set(new Vec2(x*Option.SCALING, y*Option.SCALING));
 		/*to scale image and body on the frame*/
 	}
 
 	/* (non-Javadoc)
 	 * @see fr.umlv.urobot.items.Item#getCenter()
 	 */
 	@Override
 	public Vec2 getCenter() {
 		Vec2 center = body.getPosition();
		return new Vec2((center.x/Option.SCALING)+tex.getWidth()/2,(center.y/Option.SCALING)+tex.getHeight()/2);
 	}
 
 	/* (non-Javadoc)
 	 * @see fr.umlv.urobot.items.Item#getScreenPosition()
 	 */
 	@Override
 	public Vec2 getScreenPosition() {
		return new Vec2(body.getPosition().x/Option.SCALING,body.getPosition().y/Option.SCALING);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see fr.umlv.urobot.items.Item#getBody()
 	 */
 	@Override
 	public Body getBody() {
 		return body;
 	}
 
 	/* (non-Javadoc)
 	 * @see fr.umlv.urobot.items.Item#getBodyDef()
 	 */
 	@Override
 	public BodyDef getBodyDef() {
 		return bd;
 	}
 
 	/* (non-Javadoc)
 	 * @see fr.umlv.urobot.items.Item#setBody(org.jbox2d.dynamics.Body)
 	 */
 	@Override
 	public void setBody(Body createBody) {
 		this.body = createBody;
 		PolygonShape box = new PolygonShape();
 		box.setAsBox((Option.UNIT/2)*Option.SCALING, (Option.UNIT/2)*Option.SCALING);
 		FixtureDef fd = new FixtureDef();
 		fd.shape = box;
 		fd.density = p.getResistance();
 		fd.friction = p.getResistance();
 		this.body.createFixture(fd); 
 		this.body.setFixedRotation(p.isFixedRotation());
 		this.body.setUserData(this);
 		this.body.getFixtureList().setSensor(p.isSensor());
 		this.body.setUserData(this);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see fr.umlv.urobot.items.Item#getAngle()
 	 */
 	@Override
 	public float getAngle() {
 		if(body == null)return 0;
 		return body.getAngle();
 	}
 
 	/**
 	 * Gets the properties of the bloc.
 	 *
 	 * @return the {@link Properties}
 	 */
 	public Properties getProperties() {
 		return this.p;
 	}
 
 	/* (non-Javadoc)
 	 * @see fr.umlv.urobot.items.Item#update()
 	 */
 	@Override
 	public void update() {
 		if(body.getLinearVelocity().length() < 3 && body.getJointList() == null){
 			body.setType(BodyType.STATIC);
 		}
 
 	}
 
 	@Override
 	public Texture getTexture() {
 		return tex;
 	}
 
 	@Override
 	public String getImg() {
 		// TODO Auto-generated method stub
 		return img;
 	}
 
 	@Override
 	public void setTexture(Texture t) {
 		this.tex=t;
 	}
 	
 }
