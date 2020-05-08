 package com.razh.laser.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.QueryCallback;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
 import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
 import com.razh.laser.LaserGame;
 import com.razh.laser.MeshStage;
 import com.razh.laser.Shader;
 import com.razh.laser.entities.Entity;
 import com.razh.laser.entities.EntityFactory;
 
 public class GameScreen extends BasicScreen {
 
 	private Body mGroundBody;
 
 	public GameScreen(LaserGame game) {
 		super(game);
 
 		setStage(new MeshStage());
 
 		Entity entity = EntityFactory.createLaserSource(getMeshStage().getWorld());
 		getStage().addActor(entity.getActor());
 
 		getMeshStage().setShaderProgram(Shader.createSimpleShader());
 
 		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(Gdx.graphics.getWidth(), 10);
 
 		BodyDef groundBodyDef = new BodyDef();
 		groundBodyDef.type = BodyType.StaticBody;
 		mGroundBody = getMeshStage().getWorld().createBody(groundBodyDef);
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = groundPoly;
 		// Value used to check whether or not to collide.
 //		fixtureDef.filter.groupIndex = 0;
 		mGroundBody.createFixture(fixtureDef);
 		groundPoly.dispose();
 
 		addInputProcessor(getStage());
 		addInputProcessor(new InputProcessor() {
 			private MouseJoint mMouseJoint = null;
 			private Body mHitBody = null;
 			private final Vector2 position = new Vector2();
 
 			@Override
 			public boolean keyDown(int keycode) {
 				return false;
 			}
 
 			@Override
 			public boolean keyUp(int keycode) {
 				return false;
 			}
 
 			@Override
 			public boolean keyTyped(char character) {
 				return false;
 			}
 
 			QueryCallback callback = new QueryCallback() {
 
 				@Override
 				public boolean reportFixture(Fixture fixture) {
 					if (fixture.testPoint(position.x, position.y)) {
 						mHitBody = fixture.getBody();
 						return false;
 					} else {
 						return true;
 					}
 				}
 
 			};
 
 			@Override
 			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 				position.set(screenX, screenY);
 				position.set(getStage().screenToStageCoordinates(position));
 
 				mHitBody = null;
 				getMeshStage().getWorld().QueryAABB(callback,
 				                                    position.x - 0.1f, position.y - 0.1f,
 				                                    position.x + 0.1f, position.y + 0.1f);
 
 				if (mHitBody != null) {
 					MouseJointDef mouseJointDef = new MouseJointDef();
 					mouseJointDef.bodyA = mGroundBody;
 					mouseJointDef.bodyB = mHitBody;
 					mouseJointDef.target.set(position);
 					mouseJointDef.maxForce = 1000.0f * mHitBody.getMass();
 
 					mMouseJoint = (MouseJoint) getMeshStage().getWorld().createJoint(mouseJointDef);
 					mHitBody.setAwake(true);
 				}
 
 				return false;
 			}
 
 			@Override
 			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 				if (mMouseJoint != null) {
 					getMeshStage().getWorld().destroyJoint(mMouseJoint);
 					mMouseJoint = null;
 				}
 
 				return false;
 			}
 
 			@Override
 			public boolean touchDragged(int screenX, int screenY, int pointer) {
 				if (mMouseJoint != null) {
 					position.set(screenX, screenY);
 					position.set(getStage().screenToStageCoordinates(position));
 					mMouseJoint.setTarget(position);
 				}
 
 				return false;
 			}
 
 			@Override
 			public boolean mouseMoved(int screenX, int screenY) {
 				return false;
 			}
 
 			@Override
 			public boolean scrolled(int amount) {
 				return false;
 			}
 
 		});
 	}
 
 	@Override
 	public void render(float delta) {
 		getStage().act(delta);
 
 		Color backgroundColor = getMeshStage().getColor();
 
 		Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		Gdx.gl20.glEnable(GL20.GL_BLEND);
 		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
 		getStage().draw();
 		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
 	}
 
 	@Override
 	public void resize(int width, int height) {}
 
 	@Override
 	public void pause() {}
 
 	@Override
 	public void resume() {}
 
 	@Override
 	public void dispose() {}
 }
