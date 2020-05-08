 package com.soc.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.soc.components.AnimatedComponent;
 import com.soc.components.Attacker;
 import com.soc.components.Movement;
 import com.soc.components.Bounds;
 import com.soc.components.Position;
 import com.soc.components.Sprite;
 import com.soc.components.State;
 
 public class AnimationMainSystem extends EntityProcessingSystem{
 	@Mapper
 	ComponentMapper<Position> pm;
 	@Mapper
 	ComponentMapper<Movement> am;
 	@Mapper
 	ComponentMapper<Attacker> atm;
 	@Mapper
 	ComponentMapper<State> sm;
 	@Mapper
 	ComponentMapper<Bounds> bm; 
 	
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	
 	@SuppressWarnings("unchecked")
 	public AnimationMainSystem(OrthographicCamera camera) {
 		super(Aspect.getAspectForAll(Movement.class, Attacker.class, State.class, Position.class, Bounds.class));
 		this.camera = camera;
 		this.batch = new SpriteBatch();
 	}
 	
 	@Override
 	protected void begin() {
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 	}
 
 	@Override
 	protected void process(Entity e) {
 		if (pm.has(e)) {
 			Position position = pm.getSafe(e);
 			State state = sm.get(e);
 			Bounds bounds = bm.get(e);
 			
 			AnimatedComponent animation = null;
 			boolean loop = true;
 			if(state.state == State.WALK){
 				animation = am.get(e);
 				animation.time += world.delta;
 			}
 			if(state.state == State.IDLE){
 				animation = am.get(e);
 				animation.time = 0;
 			}
 			if(state.state == State.ATTACK){
 				animation = atm.get(e);
 				animation.time += world.delta;
 				loop = false;
 				if(animation.animations[state.direction].isAnimationFinished(animation.time)){
 					state.state = State.IDLE;
 				}
 			}
 
 			batch.setColor(animation.r, animation.g, animation.b, animation.a);
			batch.draw(animation.animations[state.direction].getKeyFrame(animation.time, false), position.x + animation.ox, position.y + animation.oy);
 		}
 	}
 	
 	@Override
 	protected void end() {
 		batch.end();
 	}
 	
 }
