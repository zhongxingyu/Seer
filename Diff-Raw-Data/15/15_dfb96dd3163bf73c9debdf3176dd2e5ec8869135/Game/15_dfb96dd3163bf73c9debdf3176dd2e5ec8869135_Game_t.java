 package com.razh.tiling;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.Interpolation;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.SnapshotArray;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
 
 public class Game implements ApplicationListener {
 	private InputProcessor mInputProcessor;
 	private Player mPlayer;
 	private MeshStage mStage;
 	private FPSLogger mFPSLogger;
 	private boolean mGL20;
 
 	private ShaderProgram mShaderProgram;
 	private Uniforms mUniforms;
 
 	private boolean mShaderProgramNeedsUpdate;
 	private boolean mLightUniformsNeedRefresh;
 
 	private PointLight pLight;
 	private MeshActor meshActor;
 
 	private BitmapFont mFont;
 	private SpriteBatch mSpriteBatch;
 
 	@Override
 	public void create() {
 		Gdx.graphics.setVSync(true);
 
 		mStage = new MeshStage();
 		mFPSLogger = new FPSLogger();
 
 		mGL20 = Gdx.graphics.isGL20Available();
 		if (!mGL20) {
 			Gdx.app.exit();
 		}
 
 		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
 		Gdx.gl20.glCullFace(GL20.GL_BACK);
 
 		mShaderProgramNeedsUpdate = false;
 		mLightUniformsNeedRefresh = false;
 //		mShaderProgram = Shader.createPhongShaderProgram();
 		mShaderProgram = Shader.createLambertShaderProgram();
 		mUniforms = new Uniforms();
 
 		mStage.setShaderProgram(mShaderProgram);
 		mStage.setPointLightShaderProgram(Shader.createPointLightShaderProgram());
 		mStage.getCamera().position.z = 10000.0f;
 		mStage.getCamera().far = 15000.0f;
 
 		mFont = new BitmapFont();
 
 		MeshMaterial material = new MeshMaterial(new Color(0.33f, 0.33f, 0.33f, 1.0f), new Color(Color.WHITE), new Color(Color.BLACK), 50);
 
 		meshActor = new MeshActor();
 		meshActor.setWidth(100.0f);
 		meshActor.setHeight(100.0f);
		meshActor.setDepth(50.0f);
 		meshActor.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2 - 200);
 		meshActor.setColor(new Color(Color.BLUE).add(new Color(0.25f,0.0f,0.0f,0.0f)));
 		meshActor.setMaterial(material);
 		meshActor.setOrientation(180);
 		meshActor.setMesh(Geometry.createTriangularBipyramid());
 		meshActor.addAction(
 			parallel(
 				forever(
 					sequence(
 						moveBy(200, 200, 2.0f, Interpolation.pow2),
 						moveBy(-200, 200, 2.0f, Interpolation.pow2),
 						moveBy(-200, -200, 2.0f, Interpolation.pow2),
 						moveBy(200, -200, 2.0f, Interpolation.pow2)
 					)
 				),
 				forever(
 					rotateBy(360, 4.0f)
 				)
 			)
 		);
 		mStage.addActor(meshActor);
 
 		MeshActor meshActor2 = new MeshActor();
 		meshActor2.setWidth(100.0f);
 		meshActor2.setHeight(100.0f);
 		meshActor2.setDepth(50.0f);
 		meshActor2.setPosition(200, 200);
 		meshActor2.setColor(new Color(Color.RED).add(new Color(0.0f, 0.0f, 0.25f, 0.0f)));
 		meshActor2.setMesh(Geometry.createOctagonalBipyramid());
 		meshActor2.setMaterial(material);
 		meshActor2.addAction(
 			forever(
 				rotateBy(360, 4.0f)
 			)
 		);
 		mStage.addActor(meshActor2);
 
 		MeshActor meshActor3 = new MeshActor();
 		meshActor3.setWidth(100.0f);
 		meshActor3.setHeight(100.0f);
		meshActor3.setDepth(50.0f);
 		meshActor3.setPosition(800, 200);
 		meshActor3.setColor(new Color(Color.WHITE));
 		meshActor3.setMesh(Geometry.createOctagonalBipyramid());
 		meshActor3.setMaterial(material);
 		mStage.addActor(meshActor3);
 
 		MeshActor meshActor4 = new MeshActor();
 		meshActor4.setWidth(100.0f);
 		meshActor4.setHeight(100.0f);
 		meshActor4.setDepth(50.0f);
 		meshActor4.setPosition(800, 600);
 		meshActor4.setColor(new Color(Color.GRAY));
 		meshActor4.setMesh(Geometry.createOctahedron());
 		meshActor4.setMaterial(material);
 		meshActor4.addAction(
 				forever(
 					rotateBy(360, 4.0f)
 				)
 			);
 		mStage.addActor(meshActor4);
 
 		AmbientLight aLight = new AmbientLight();
 		aLight.setColor(0.25f, 0.25f, 0.25f, 1.0f);
 		mStage.addLight(aLight);
 
 		pLight = new PointLight();
 		pLight.setColor(new Color(Color.RED));
 		pLight.setPosition(200, Gdx.graphics.getHeight() / 2 + 50, 100);
 		pLight.setWidth(3);
 		pLight.setHeight(3);
 		pLight.addAction(
 			forever(
 				sequence(
 					moveBy(600, 0, 3.0f, Interpolation.pow2),
 					moveBy(-600, 0, 3.0f, Interpolation.pow2)
 				)
 			)
 		);
 		pLight.setDistance(600);
 		mStage.addLight(pLight);
 
 		PointLight pLight2 = new PointLight();
 		pLight2 = new PointLight();
 		pLight2.setColor(new Color(Color.BLUE));
 		pLight2.setPosition(800, Gdx.graphics.getHeight() / 2, 100);
 		pLight2.setWidth(3);
 		pLight2.setHeight(3);
 		pLight2.setDistance(800);
 		mStage.addLight(pLight2);
 
 		PointLight pLight3 = new PointLight();
 		pLight3 = new PointLight();
 		pLight3.setColor(new Color(Color.GREEN));
 		pLight3.setPosition(500, Gdx.graphics.getHeight() / 2 - 200, 100);
 		pLight3.setWidth(3);
 		pLight3.setHeight(3);
 		pLight3.setDistance(8000);
 		mStage.addLight(pLight3);
 		mShaderProgramNeedsUpdate = true;
 //		setupLights();
 
 		mPlayer = new Player();
 
 		mInputProcessor = new GameInputProcessor();
 		((GameInputProcessor) mInputProcessor).setPlayer(mPlayer);
 		((GameInputProcessor) mInputProcessor).setStage(mStage);
 		Gdx.input.setInputProcessor(mInputProcessor);
 	}
 
 	public void setupLights() {
 		Color ambientLightColor = new Color();
 		ArrayList<Color> pointLightColors = new ArrayList<Color>();
 		ArrayList<Float> pointLightPositions = new ArrayList<Float>();
 		ArrayList<Float> pointLightDistances = new ArrayList<Float>();
 		int pointLightCount = 0;
 
 		SnapshotArray<Light> children = mStage.getLights();
 		Light[] lights = children.begin();
 		Vector3 position;
 		for (int i = 0, n = children.size; i < n; i++) {
 			Light light = lights[i];
 
 			if (!light.isVisible()) {
 				continue;
 			}
 
 			// Ambient light color is sum of all ambient lights.
 			if (light instanceof AmbientLight) {
 				ambientLightColor.add(light.getColor());
 			} else if (light instanceof PointLight) {
 				if (!light.isVisible()) {
 					continue;
 				}
 
 				pointLightCount++;
 
 				pointLightColors.add(light.getColor());
 
 				position = light.getPosition();
 				pointLightPositions.add(position.x);
 				pointLightPositions.add(position.y);
 				pointLightPositions.add(position.z);
 
 				pointLightDistances.add(((PointLight) light).getDistance());
 			}
 		}
 		children.end();
 
 		if (Shader.MAX_POINT_LIGHTS != pointLightCount) {
 			mShaderProgramNeedsUpdate = true;
 			Shader.MAX_POINT_LIGHTS = pointLightCount;
 		}
 
 		mUniforms.setAmbientLightColor(ambientLightColor);
 		if (pointLightCount > 0) {
 			mUniforms.setPointLightColors(pointLightColors);
 			mUniforms.setPointLightPositions(pointLightPositions);
 			mUniforms.setPointLightDistances(pointLightDistances);
 			mLightUniformsNeedRefresh = true;
 		}
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	@Override
 	public void render() {
 		update();
 
 		Color backgroundColor = mStage.getColor();
 
 		Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		mStage.draw();
 
 		mFPSLogger.log();
 	}
 
 	public void update() {
 		float delta = Math.min(Gdx.graphics.getDeltaTime(), 1 / 30.0f);
 		mStage.act(delta);
 //		pLight.setPosition(Gdx.input.getX(), -Gdx.input.getY() + Gdx.graphics.getHeight() + 100, -10);
 //		pLight.setPosition(Gdx.input.getX(), -Gdx.input.getY() + Gdx.graphics.getHeight(), 0);
 
 		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
 
 		}
 
 		setupLights();
 
 		if (mShaderProgramNeedsUpdate) {
 			mShaderProgramNeedsUpdate = false;
 			mShaderProgram.dispose();
 			mShaderProgram = Shader.createPhongShaderProgram();
 //			mShaderProgram = Shader.createLambertShaderProgram();
 			mStage.setShaderProgram(mShaderProgram);
 		}
 
 		if (mLightUniformsNeedRefresh) {
 			mLightUniformsNeedRefresh = false;
 			mUniforms.setUniforms(mShaderProgram);
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
