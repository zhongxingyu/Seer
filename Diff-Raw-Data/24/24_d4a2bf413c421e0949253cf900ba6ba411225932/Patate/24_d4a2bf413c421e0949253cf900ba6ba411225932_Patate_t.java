 package fr.odai.zerozeroduck.model;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 public class Patate extends Unit {
 
 	public enum State {
 		IDLE, WALKING, DYING
 	}
 
 	State state = State.WALKING;
 
 	public Patate(Vector2 position, World world, TextureAtlas atlas) {
 		super(position, world, atlas);
 		Patate.ANIM_PERIOD = 60f / (float) World.BPM / 2f;
 		Patate.SIZE = 0.5f;
 		Patate.COEF_H = 1.5f;
 		this.velocity = new Vector2(1f, 0);
 		this.bouciness = new Vector2(0, 0.2f);
 		this.hp = 100;
 		this.damage = 100;
 		this.score = 100;
 		this.bounds.height = SIZE * COEF_H;
 		this.bounds.width = SIZE * COEF_W;
 	}
 	
 	@Override
 	protected void loadTextures(TextureAtlas atlas) {
 		ParticleEffect smokeEffect = new ParticleEffect();
 		smokeEffect.load(Gdx.files.internal("particle/smoke.p"),
 				Gdx.files.internal("particle"));
 		this.smokeEffectPool = new ParticleEffectPool(smokeEffect, 1, 2);
 		
 		Patate.RUNNING_FRAME_DURATION = 60f / World.BPM / 8;
 		
 		textureBase = atlas.findRegion("Patate1");
 		TextureRegion[] walkRightFrames = new TextureRegion[4];
 		for (int i = 0; i <= 3; i++) {
 			walkRightFrames[i] = atlas.findRegion("Patate" + (i + 1));
 		}
 		walkRight = new Animation(Patate.RUNNING_FRAME_DURATION, walkRightFrames);
 	}
 
 	@Override
 	public void update(float delta) {
 		super.update(delta);
 		if (((animTime < 0.25f * (float) ANIM_PERIOD) || (animTime > 0.5f * (float) ANIM_PERIOD && animTime <= 0.75f * (float) ANIM_PERIOD))
 				&& invincibilityTime > 0) {
 			isVisible = false;
 		} else if (state != State.DYING)
 			isVisible = true;
 
 		if (state == State.DYING) {
 			hp = 0;
 			isVisible = false;
 		}
 
 		if (animTime > ANIM_PERIOD)
 			animTime -= ANIM_PERIOD;
 
 		if (state == State.WALKING) {
 			position.add(velocity.tmp().mul(delta));
 			position.y = world.getFloorHeight(position.x);
 		}
 
 		Rectangle positionnedBounds = this.getPositionnedBounds();
 		for (Trap trap : world.getTraps()) {
 			if (trap.getState() == Trap.State.HURTING && invincibilityTime <= 0) {
 				int hpModifier = trap.damageWhenTrapped(positionnedBounds);
 				if (hpModifier < 0) {
 					invincibilityTime = 1.0f;
 					hp += hpModifier;
 					if (hp <= 0) {
 						setState(State.DYING);
 						world.setScore(world.getScore() + score);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void draw(SpriteBatch spriteBatch, Array<PooledEffect> effects, ShapeRenderer shr, float ppuX, float ppuY) {
 		textureFrame = textureBase;
 		if (state.equals(State.WALKING)) {
 			textureFrame = walkRight.getKeyFrame(this.getStateTime(), true);
 		}
 		if (isVisible) {
			spriteBatch.draw(textureFrame, getPosition().x * ppuX, getPosition().y * ppuY, bounds.width * ppuX,	bounds.height * ppuY);
 		}
 		if (state == State.DYING) {
 			// Create effect:
 			PooledEffect effect = smokeEffectPool.obtain();
 			effect.setDuration(500);
 			effect.setPosition(
					getPosition().x * ppuX + bounds.x / 2.f
 							* ppuX, bounds.y * ppuY + 0.05f
 							* ppuY);
 			effects.add(effect);
 		}
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	public void setState(State state) {
 		this.state = state;
 		stateTime = 0;
 	}
 
 	@Override
 	public void kill() {
 		setState(State.DYING);
 	}
 }
