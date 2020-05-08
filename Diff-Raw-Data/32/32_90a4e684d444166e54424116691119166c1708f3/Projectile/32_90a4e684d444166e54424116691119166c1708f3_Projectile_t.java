 package com.gdxtest02;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.esotericsoftware.spine.Skeleton;
 import com.esotericsoftware.spine.SkeletonData;
 import com.esotericsoftware.spine.SkeletonJson;
 import com.gdxtest02.effects.FireEffect;
 import com.gdxtest02.effects.GreenEffect;
 import com.gdxtest02.effects.IceEffect;
 
 import static com.gdxtest02.GdxTest02.log;
 
 public class Projectile {
 	private float x;
 	private float y;
 	private float speedx;
 	private float speedy;
 	
 	private float time;
 	private float duration;
 
 	private Char source;
 	private Char target;
 	private AnimRenderer renderer;
 	private Effect effecttype;
 	protected Effect effect;
 	private boolean flipX;
 	private int flipXFactor;
 	private Skeleton skeleton;
 	
 	public Projectile(AnimRenderer renderer) {
 		this.renderer = renderer;
 		x = 0;
 		y = 0;
 		speedx = 200;
 		speedy = 0;
 		duration = 2;
 		time = 0;
 		setFlipXFactor();
 		
 		loadSkeleton(1f);
 	}
 	
 	private void loadSkeleton(float scale) {
 		String atlasfile = "data/spine/proj/skeleton.atlas";
 		String jsonfile = "data/spine/proj/skeleton.json";
 
 		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(atlasfile));
 		SkeletonJson json = new SkeletonJson(atlas);
 		json.setScale(scale);
 		SkeletonData sd = json.readSkeletonData(Gdx.files.internal(jsonfile));
 		skeleton = new Skeleton(sd);
 //		skeleton.setSlotsToSetupPose();
 		skeleton.updateWorldTransform();
 	}
 
 	public void start() {
 		if (effect == null) effect = new FireEffect();
		time = 0;
 		effect.start();
 		effect.setPosition(x, y);
 	}
 	
 	public void setPos(float x, float y) {
 		this.x = x;
 		this.y = y;
 		skeleton.setX(x);
 		skeleton.setY(y);
 	}
 	
 	public float getX() {
 		return x;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 
 	public Char getSource() {
 		return source;
 	}
 
 	public void setSource(Char source) {
 		this.source = source;
 	}
 
 	public Char getTarget() {
 		return target;
 	}
 
 	public void setTarget(Char target) {
 		this.target = target;
 	}
 
 	public void update(float delta) {
 		time += delta;
 		x += delta * speedx * flipXFactor;
 		y += delta * speedy;
 		skeleton.setX(x);
 		skeleton.setY(y);
 		effect.setPosition(x, y);
 		effect.update(delta);
//		log("projupdate time: " + time + " duration: "+ duration);
 		if (time >= duration) end();
 	}
 	
 	public void end() {
 		renderer.removeProjectile();
 //		log("end proj time: " + time + " duration: "+ duration);
 	}
 
 	public void draw(SpriteBatch batch) {
 		renderer.drawSkeleton(batch, skeleton);
 		effect.draw(batch);
 //		log("proj draw, skel: " + skeleton);
 	}
 
 	public Effect getEffecttype() {
 		return effecttype;
 	}
 
 	public void setEffecttype(Effect effecttype) {
 		this.effecttype = effecttype;
 	}
 
 	public float getSpeedx() {
 		return speedx;
 	}
 
 	public void setSpeedx(float speedx) {
 		this.speedx = speedx;
 	}
 
 	public float getSpeedy() {
 		return speedy;
 	}
 
 	public void setSpeedy(float speedy) {
 		this.speedy = speedy;
 	}
 
 	public float getDuration() {
 		return duration;
 	}
 
 	public void setDuration(float duration) {
 		this.duration = duration;
 	}
 
 	public float getTime() {
 		return time;
 	}
 
 	public void setFlipX(boolean flipX) {
 		this.flipX = flipX;
 		setFlipXFactor();
 	}
 
 	private void setFlipXFactor() {
 		if (flipX) flipXFactor = -1;
 		else flipXFactor = 1;
 	}
 
 }
