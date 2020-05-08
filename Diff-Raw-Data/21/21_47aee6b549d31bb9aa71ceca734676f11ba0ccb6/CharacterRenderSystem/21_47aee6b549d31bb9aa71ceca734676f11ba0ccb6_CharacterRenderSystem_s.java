 package com.soc.game.systems;
 
 import java.util.HashMap;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.physics.box2d.Shape;
 import com.soc.game.components.Attacker;
 import com.soc.game.components.Bounds;
 import com.soc.game.components.Character;
 import com.soc.game.components.Movement;
 import com.soc.game.components.Position;
 import com.soc.game.components.State;
 import com.soc.game.graphics.DirectionalAnimatedRenderer;
 import com.soc.game.graphics.DirectionalRenderer;
 
 public class CharacterRenderSystem extends EntityProcessingSystem{
 	@Mapper ComponentMapper<Character> am;
 	@Mapper ComponentMapper<State> sm;
 	@Mapper ComponentMapper<Position> pm;
 	@Mapper ComponentMapper<Bounds> bm;
 
 	
 	
 	@SuppressWarnings("unchecked")
 	public CharacterRenderSystem(OrthographicCamera camera) {
 		super(Aspect.getAspectForAll(Character.class, State.class, Position.class));
 		this.batch = new SpriteBatch();
 		this.camera = camera;
 		shape = new ShapeRenderer();
 	}
 	
 	@Override
 	protected void begin() {
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		shape.setProjectionMatrix(camera.combined);
 		shape.begin(ShapeType.Filled);
 	}
 
 	@Override
 	protected void process(Entity e) {
 		State state = sm.get(e);
 		Position pos = pm.get(e);
 		Bounds bon = bm.get(e);
 		Character animations = am.get(e);
 		
 		DirectionalRenderer r = null;
 		if(state.state == State.ATTACK){
 			r = animations.attack;
 			if(((DirectionalAnimatedRenderer)r).isFinished()){
 				r.time = 0;
 				state.state = State.IDLE;
 			}
 		}
 		
 		if(state.state == State.WALK){
 			r = animations.movement;
 		}
 		
 		if(state.state == State.IDLE){
 			r = animations.idle;
 			r.time = Float.MAX_VALUE;
 		}
 		
 		float angle = pos.direction.angle();
		System.out.println(pos.direction.angle());
 		if(angle%90 != 0){
			System.out.println("EI");
			if(angle>90f){
				angle=180f;
			} else {
 				angle=0f;
 			}
 		}
		r.direction = ((int) (pos.direction.angle())/90 + 3) % 4;
 
 
 		batch.setColor(r.r, r.g, r.b, r.a);
 		batch.draw(r.frame(world.delta), pos.x+r.ox, pos.y+r.oy);
 	}
 	
 	@Override
 	protected void end() {
 		batch.end();
 		shape.end();
 	}
 
 	private SpriteBatch batch;
 	private ShapeRenderer shape;
 	private OrthographicCamera camera;
 }
