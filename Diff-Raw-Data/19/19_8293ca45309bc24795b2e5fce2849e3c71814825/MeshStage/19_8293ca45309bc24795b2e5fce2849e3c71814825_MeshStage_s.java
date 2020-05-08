 package com.razh.tiling;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.utils.SnapshotArray;
 
 public class MeshStage extends Stage {
 	private float mScale;
 
 	private MeshGroup mRoot;
 	private MeshGroup mColorRoot;
 	private ShaderProgram mShaderProgram;
 	private ShaderProgram mColorShaderProgram;
 	private ShaderProgram mPointLightShaderProgram;
 
 	private SnapshotArray<Light> mLights;
 
 	// Allows us to set colors and stuff with actions.
 	private Actor mColorActor;
 
 	public MeshStage() {
 		this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
 	}
 
 	public MeshStage(float width, float height, boolean stretch) {
 		super(width, height, stretch);
 		if (width != Gdx.graphics.getWidth() && height != Gdx.graphics.getHeight()) {
 			setCamera(new OrthographicCamera());
 			setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), stretch);
 		}
 
 		setScale(1.0f);
 
 		mRoot = new MeshGroup();
 		mRoot.setStage(this);
 
 		mColorRoot = new MeshGroup();
 		mColorRoot.setStage(this);
 
 		mLights = new SnapshotArray<Light>(Light.class);
 
 		mColorActor = new Actor();
 		mColorActor.setColor(Color.BLACK);
 	}
 
 	public void setShaderProgram(ShaderProgram shaderProgram) {
 		mShaderProgram = shaderProgram;
 	}
 
 	public void setColorShaderProgram(ShaderProgram shaderProgram) {
 		mColorShaderProgram = shaderProgram;
 	}
 
 	public void setPointLightShaderProgram(ShaderProgram shaderProgram) {
 		mPointLightShaderProgram = shaderProgram;
 	}
 
 	public void draw(ShaderProgram shaderProgram) {
 		mShaderProgram = shaderProgram;
 
 		draw();
 	}
 
 	@Override
 	public void draw() {
 		getCamera().update();
 
 		// Render normal objects.
 		if (mShaderProgram != null) {
 			mShaderProgram.begin();
 			mShaderProgram.setUniformMatrix("projectionMatrix", getCamera().projection);
 			mShaderProgram.setUniformMatrix("viewMatrix", getCamera().view);
 
 			mRoot.draw(mShaderProgram, 1.0f);
 
 			mShaderProgram.end();
 		}
 
 		// Render multi-color objects.
 		if (mColorShaderProgram != null) {
 			mColorShaderProgram.begin();
 
 			mColorShaderProgram.setUniformMatrix("projectionMatrix", getCamera().projection);
 			mColorShaderProgram.setUniformMatrix("viewMatrix", getCamera().view);
 
 			mColorRoot.draw(mColorShaderProgram, 1.0f);
 
 			mColorShaderProgram.end();
 		}
 
 		// Render light positions.
 		if (mPointLightShaderProgram != null) {
 			mPointLightShaderProgram.begin();
 			mPointLightShaderProgram.setUniformMatrix("modelViewProjectionMatrix", getCamera().combined);
 			Light[] lights = mLights.begin();
 			for (int i = 0, n = mLights.size; i < n; i++) {
 				Light light = lights[i];
 
 				if (!light.isVisible()) {
 					continue;
 				}
 
 				if (light instanceof PointLight) {
 					light.draw(mPointLightShaderProgram, 1.0f);
 				}
 			}
 			mLights.end();
 			mPointLightShaderProgram.end();
 		}
 	}
 
 	public float getScale() {
 		return mScale;
 	}
 
 	public void setScale(float scale) {
		if ( scale != 1.0f ) {
			mScale = scale;
 			setViewport(Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale, false);
 			getCamera().position.z = 10000.0f;
 		}
 	}
 
 	@Override
 	public void addActor(Actor actor) {
 		mRoot.addActor(actor);
 	}
 
 	public void addColorActor(MeshActor actor) {
 		mColorRoot.addActor(actor);
 	}
 
 	public SnapshotArray<Light> getLights() {
 		return mLights;
 	}
 
 	public void addLight(Light light) {
 		mLights.add(light);
 	}
 
 	@Override
 	public void act(float delta) {
 		mRoot.act(delta);
 		mColorRoot.act(delta);
 
 		Light[] lights = mLights.begin();
 		for (int i = 0, n = mLights.size; i < n; i++) {
 			Light light = lights[i];
 
 			light.act(delta);
 		}
 		mLights.end();
 
 		mColorActor.act(delta);
 	}
 
 	@Override
 	public MeshGroup getRoot() {
 		return mRoot;
 	}
 
 	public MeshGroup getColorRoot() {
 		return mColorRoot;
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
 
 	@Override
 	public Actor hit(float stageX, float stageY, boolean touchable) {
		stageX /= getScale();
		stageY /= getScale();
 
 		Vector2 actorCoords = Vector2.tmp;
 		getRoot().parentToLocalCoordinates(actorCoords.set(stageX, stageY));
 		Actor hit = getRoot().hit(actorCoords.x, actorCoords.y, touchable);
 		if (hit == null) {
 			return getColorRoot().hit(actorCoords.x, actorCoords.y, touchable);
 		} else {
 			return hit;
 		}
 	}
 
 	public void clearActors() {
 		mRoot.clear();
 		mColorRoot.clear();
 		mLights = new SnapshotArray<Light>(Light.class);
 	}
 }
