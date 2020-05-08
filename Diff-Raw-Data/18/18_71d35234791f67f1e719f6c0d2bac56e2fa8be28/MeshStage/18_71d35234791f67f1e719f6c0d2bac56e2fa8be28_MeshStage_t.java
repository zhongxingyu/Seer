 package com.razh.laser;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 public class MeshStage extends Stage {
 	private MeshGroup mRoot;
 	private ShaderProgram mShaderProgram;
 
 	// Allows us to set colors with actions.
 	private Actor mColorActor;
 
 	private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
 	private World mWorld;
 
 	public MeshStage() {
 		this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
 	}
 
 	public MeshStage(float width, float height, boolean stretch) {
 		super(width, height, stretch);
 		if (width != LaserGame.WIDTH || height != LaserGame.HEIGHT) {
 			setViewport(LaserGame.WIDTH, LaserGame.HEIGHT, true);
 			getCamera().position.set(0.5f * LaserGame.WIDTH,
 			                         0.5f * LaserGame.HEIGHT,
 			                         0.0f);
 		}
 
 		mRoot = new MeshGroup();
 		mRoot.setStage(this);
 
 		mColorActor = new Actor();
 		mColorActor.setColor(Color.BLACK);
 
 		setWorld(new World(Vector2.Zero, true));
 	}
 
 	@Override
 	public void draw() {
 		Camera camera = getCamera();
 		camera.update();
 
 		if (mShaderProgram != null) {
 			mShaderProgram.begin();
 
 			mShaderProgram.setUniformMatrix("projectionMatrix", camera.projection);
 			mShaderProgram.setUniformMatrix("viewMatrix", camera.view);
 
 			mRoot.draw(mShaderProgram);
 
 			mShaderProgram.end();
 		}
 
 		debugRenderer.render(mWorld, camera.combined);
		mWorld.step(1.0f / 60.0f, 6, 2);
 	}
 
 	@Override
 	public void addActor(Actor actor) {
 		mRoot.addActor(actor);
 	}
 
 	@Override
 	public void act(float delta) {
 		mRoot.act(delta);
 		mColorActor.act(delta);
 	}
 
 	public ShaderProgram getShaderProgram() {
 		return mShaderProgram;
 	}
 
 	public void setShaderProgram(ShaderProgram shaderProgram) {
 		mShaderProgram = shaderProgram;
 	}
 
 	@Override
 	public MeshGroup getRoot() {
 		return mRoot;
 	}
 
 	public Color getColor() {
 		return mColorActor.getColor();
 	}
 
 	public void setColor(Color color) {
 		mColorActor.setColor(color);
 	}
 
 	@Override
 	public void addAction(Action action) {
 		mColorActor.addAction(action);
 	}
 
 	public World getWorld() {
 		return mWorld;
 	}
 
 	public void setWorld(World world) {
 		mWorld = world;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		mShaderProgram.dispose();
 	}
 }
