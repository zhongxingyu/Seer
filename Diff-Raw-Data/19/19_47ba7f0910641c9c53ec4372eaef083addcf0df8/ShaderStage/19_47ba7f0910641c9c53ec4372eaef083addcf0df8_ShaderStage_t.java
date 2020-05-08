 package com.razh.laser;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
 import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Widget;
 import com.badlogic.gdx.utils.SnapshotArray;
 import com.razh.laser.sprites.SpriteActor;
 
 public class ShaderStage extends Stage {
 
 	private final Map<Class<?>, Group> mGroups;
 
 	// Decals.
 	private final DecalBatch mDecalBatch;
 	private final DecalGroup mDecalGroup;
 
 	// Meshes.
 	private final MeshGroup mMeshGroup;
 	private final ShaderProgram mMeshShader;
 
 	// Allows us to set colors with actions.
 	private Actor mColorActor;
 
 	private LaserGame mGame;
 
 	public ShaderStage() {
 		this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
 	}
 
 	public ShaderStage(float width, float height, boolean stretch) {
 		super(width, height, stretch);
 
 		Camera camera = new PerspectiveCamera(90.0f, width, height);
 		setCamera(camera);
 		camera.position.set(0, 0, 400f);
 		camera.far = 1e4f;
 		camera.lookAt(0.0f, 0.0f, 0.0f);
 
 		mGroups = new HashMap<Class<?>, Group>();
 
 		mColorActor = new Actor();
 		super.addActor(mColorActor);
 
 		mDecalBatch = new DecalBatch(new CameraGroupStrategy(camera));
 
 		// Create DecalGroup and add it to Groups container.
 		mDecalGroup = new DecalGroup();
 		mGroups.put(DecalActor.class, mDecalGroup);
 		addActor(mDecalGroup);
 
 		// Create MeshGroup and add it to Groups container.
 		// MeshGroup is necessary for transparency/draw(shaderProgram).
 		mMeshGroup = new MeshGroup();
 		mMeshShader = Shader.createSimpleMeshShader();
 		mGroups.put(MeshActor.class, mMeshGroup);
 		mGroups.put(MeshGroup.class, mMeshGroup);
 		addActor(mMeshGroup);
 	}
 
 	@Override
 	public void draw() {
		// Draw decals with negative z-coordinate.
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
 		mDecalGroup.drawBehind(mDecalBatch);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 
 		// Draw meshes.
 		Gdx.gl.glEnable(GL20.GL_BLEND);
 
 		mMeshShader.begin();
 		mMeshShader.setUniformMatrix("u_projTrans", getCamera().combined);
 		mMeshGroup.draw(mMeshShader);
 		mMeshShader.end();
 
 		Gdx.gl.glDisable(GL20.GL_BLEND);
 
		super.draw();

		// Draw decals with positive z-coordinate.
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
 		mDecalGroup.drawAfter(mDecalBatch);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 	}
 
 	public float time = 0.0f;
 	@Override
 	public void act(float delta) {
 		time += 0.01f;
 		Camera camera = getCamera();
 		camera.position.x = (float) (200.0f * Math.cos(time));
 		camera.position.y = (float) (200.0f * Math.sin(time)) - 200.0f;
 		camera.position.z = (float) (200.0f * Math.abs(Math.cos(time))) + 400.0f;
 		camera.lookAt(0, 0, 0);
 
 		super.act(delta);
 	}
 
 	@Override
 	public void addActor(Actor actor) {
 		// If actor is a group (ShaderGroup or otherwise), we don't need to add
 		// it to a group.
 		if (actor instanceof Group ||
 			actor instanceof Widget) {
 			super.addActor(actor);
 			return;
 		}
 
 		Group group = mGroups.get(actor.getClass());
 		// Add if the group exists.
 		if (group != null) {
 			group.addActor(actor);
 		} else {
 			// Otherwise, create a new group.
 			addShaderGroup(actor.getClass()).addActor(actor);
 		}
 	}
 
 	/**
 	 * Create ShaderGroup for the given type. Returns it for chaining.
 	 * @param type
 	 * @return ShaderGroup
 	 */
 	public ShaderGroup addShaderGroup(Class<?> type) {
 		ShaderGroup group = new ShaderGroup();
 		group.setShaderProgram(Shader.getShaderForType(type));
 
 		mGroups.put(type, group);
 		super.addActor(group);
 
 		return group;
 	}
 
 	public void addActorContainer(ActorContainer container) {
 		SnapshotArray<Actor> components = container.getActors();
 		Actor[] actors = components.begin();
 
 		Actor actor;
 		for (int i = 0, n = components.size; i < n; i++) {
 			actor = actors[i];
 
 			if (actor instanceof SpriteActor ||
 				actor instanceof MeshActor ||
 				actor instanceof DecalActor ) {
 				addActor(actor);
 			} else {
 				super.addActor(actor);
 			}
 		}
 
 		components.end();
 		super.addActor(container);
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
 
 	public LaserGame getGame() {
 		return mGame;
 	}
 
 	public void setGame(LaserGame game) {
 		mGame = game;
 	}
 }
