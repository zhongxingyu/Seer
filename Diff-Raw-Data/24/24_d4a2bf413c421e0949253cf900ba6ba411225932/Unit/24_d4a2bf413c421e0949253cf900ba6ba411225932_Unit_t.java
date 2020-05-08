 package fr.odai.zerozeroduck.model;
 
 
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 public abstract class Unit {
 
 	
 	float invincibilityTime=0;
 	
 	public static float SIZE = 1; // half a unit
 	static float COEF_H = 1;
 	static float COEF_W = 1;
 	
 	static float ANIM_PERIOD;
 	protected static float RUNNING_FRAME_DURATION;
 			
 	float       stateTime = 0;
 	float 		animTime = 0;
 	Vector2 	position;
 	Vector2     velocity;
 	Vector2     bouciness;
 	Rectangle 	bounds = new Rectangle();
 	boolean		facingLeft = true;
 	boolean 	isVisible = true;
 	boolean		toBeRemoved = false;
 	ParticleEffectPool smokeEffectPool;
 	int 		hp;
 	int 		damage;
 	int 		score;
 	TextureRegion textureFrame;
 	TextureRegion textureBase;
 	Animation walkRight;
 	
 	World world;
 
 	public Unit(Vector2 position, World world, TextureAtlas atlas) {
 		this.position = position;
 		this.world = world;
 		this.loadTextures(atlas);
 	}
 	
 	abstract protected void loadTextures(TextureAtlas atlas);
 	
 	public int getScore(){
 		return score;
 	}
 	
 	public Vector2 getPosition() {
 		return position.cpy().add(bouciness.tmp().mul(Math.abs((float) Math.sin((animTime / ANIM_PERIOD * Math.PI)))));
 	}
 	
 	public boolean getIsVisible(){
 		return isVisible;
 	}
 
 	public boolean isToBeRemoved() {
 		return toBeRemoved;
 	}
 
 	public Rectangle getBounds() {
 		return bounds;
 	}
 
 	public float getStateTime() {
 		return stateTime;
 	}
 
 	public boolean isFacingLeft() {
 		return facingLeft;
 	}
 
 	public void setFacingLeft(boolean facingLeft) {
 		this.facingLeft = facingLeft;
 	}
 	
 	public Rectangle getPositionnedBounds(){
 		return new Rectangle(position.x, position.y, this.bounds.width, this.bounds.height);
 	}
 	
 	public int damageWhenFinish(Rectangle rect){
		if(getPosition().x>rect.x){
 			return -damage;
 		}
 		else return 0;
 	}
 	
 	public void kill(){
 		toBeRemoved = true;
 	}
 	
 	abstract public void draw(SpriteBatch spriteBatch, Array<PooledEffect> effects, ShapeRenderer shr, float ppuX, float ppuY);
 	
 	public void update(float delta){
 		stateTime+=delta;
 		animTime+=delta;
 		invincibilityTime-=delta;
 	}
 }
