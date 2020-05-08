 package com.gdxtest02;
 
 import static com.gdxtest02.GdxTest02.log;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.esotericsoftware.spine.Animation;
 import com.esotericsoftware.spine.Skeleton;
 import com.esotericsoftware.spine.SkeletonData;
 import com.gdxtest02.effects.FireEffect;
 import com.gdxtest02.projectiles.Projectile01;
 import com.gdxtest02.projectiles.Projectile02;
 
 /**This is the base class every animation will extend, it should have commonly used
  * methods that most animations use
  *  
  * @author Vandr
  *
  */
 public class CharAnim {
 
 	protected AnimRenderer renderer;
 	protected Skeleton skeleton;
 	protected SkeletonData sd;
 //	protected Animation standAnimation;
 //	private Animation punchAnimation;
 //	private Animation winkAnimation;
 	protected float animationTime;
 	protected float delta;
 	protected float lastTime;
 	protected Effect effecttype;
 	protected String name = "";
 
 	public CharAnim(AnimRenderer animRenderer) {
 		renderer = animRenderer;
 		
 //		ini();
 	}
 
 	/**Called when the animation needs to start, must call this before using the animation
 	 * 
 	 * @return
 	 */
 	public void start() {
 		
 		skeleton = renderer.getSkeleton();
 		sd = renderer.getSkeletonData();
 //		Char owner = renderer.getOwner();
 //		if (owner != null) {
 //			log("charanim ini, skeleton: " + skeleton.hashCode() + " owner: " + renderer.getOwner()
 //					+ " hash: " + renderer.getOwner().hashCode());
 //		}
 		animationTime = 0;
 		ini();
 	}
 
 	public void draw() {
 //		log("charanim draw this: " + this + " name: " + name + " etype: " + effecttype + 
 //				" time:" + animationTime);
 		delta = Gdx.graphics.getDeltaTime();
 		lastTime = animationTime;
 		animationTime += delta;
 	}
 	
 	/**ends animation and go back to default anim
 	 * 
 	 */
 	public void end() {
 		renderer.setAnimToDefault();
 	}
 	
 	public void setEffect(Effect type) {
 		renderer.resetParticles();
 		this.effecttype = type;
 //		ini();
 	}
 	
 	protected void ini() {
 		
 	}
 	
 	public String getEffect(String effecttype) {
 		return effecttype;
 	}
 	
 	/**Adds an effect tied to the animation, this effect will disappear
 	 * if the animation ends
 	 * 
 	 * @return
 	 */
 	protected Effect addAnimEffect() {
 		Effect effect = getNewEffect();
 //		effect.load(getParticleFile(), Gdx.files.internal("effects"));
 		effect.start();
 		renderer.addAnimEffect(effect);
 		return effect;
 	}
 	
 	/**Adds an effect tied to the char, this effect will disappear
 	 * if the char dies
 	 * 
 	 * @return
 	 */
 	protected Effect addCharEffect() {
 		Effect effect = getNewEffect();
 //		effect.load(getParticleFile(), Gdx.files.internal("effects"));
 		effect.start();
 		renderer.addCharEffect(effect);
 		return effect;
 	}
 	
 	protected Effect getNewEffect() {
 		Effect effect;
 		if (effecttype != null) effect = effecttype.getClone(); 
 		else effect = new FireEffect();
 		return effect;
 	}
 
 //	private FileHandle getParticleFile() {
 //		return AnimRenderer.getParticleFile(effecttype);
 //	}
 	
 	protected Projectile addProjectile(float x, float y, Projectile p) {
		if (renderer.getNumProj() > 0) return null;
 		if (p == null) p = new Projectile02(renderer);
 		p.setPos(x, y);
 		p.setEffecttype(getNewEffect());
 		renderer.createProjectile(p);
 		return p;
 	}
 	
 	/**Create a hit effect on the target
 	 * 
 	 */
 	protected void createHitEffect() {
 		Char c = renderer.getOwner();
 		Char t = c.getTarget();
 		
 		renderer.addEffect("hit", t.getPosX(), t.getPosY() + 70f);
 		
 //		log("hit created");
 	}
 
 	public String getName() {
 		return name;
 	}
 }
