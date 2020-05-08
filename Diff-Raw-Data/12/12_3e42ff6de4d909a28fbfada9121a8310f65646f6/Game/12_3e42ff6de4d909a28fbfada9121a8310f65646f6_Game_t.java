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
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.SnapshotArray;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.razh.tiling.json.LevelDeserializer;
 import com.razh.tiling.json.MeshActorDeserializer;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
 
 public class Game implements ApplicationListener {
 	private InputProcessor mInputProcessor;
 	private Player mPlayer;
 	private LevelDeserializer mLevelLoader;
 	private MeshStage mStage;
 	private FPSLogger mFPSLogger;
 	private boolean mGL20;
 
 	private ShaderProgram mShaderProgram;
 	private ShaderProgram mColorShaderProgram;
 	private Uniforms mUniforms;
 
 	private boolean mShaderProgramNeedsUpdate;
 	private boolean mLightUniformsNeedRefresh;
 
 	private PointLight pLight;
 	private MeshActor meshActor;
 
 	private BitmapFont mFont;
 	private SpriteBatch mSpriteBatch;
 
 	private enum LightingModel {
 		LAMBERT,
 		PHONG
 	};
 	private LightingModel mLightingModel = LightingModel.LAMBERT;
 
 	@Override
 	public void create() {
 		Gdx.graphics.setVSync(true);
 
 		mStage = new MeshStage();
 		mFPSLogger = new FPSLogger();
 
 		mGL20 = Gdx.graphics.isGL20Available();
 		if (!mGL20) {
 			Gdx.app.exit();
 		}
 
 		Gdx.gl20.glEnable(GL20.GL_BLEND);
 		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
 
 		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
 		Gdx.gl20.glCullFace(GL20.GL_BACK);
 
 		mShaderProgramNeedsUpdate = false;
 		mLightUniformsNeedRefresh = false;
 		if (mLightingModel == LightingModel.PHONG) {
 			mShaderProgram = Shader.createPhongShaderProgram();
 		} else if (mLightingModel == LightingModel.LAMBERT) {
 			mShaderProgram = Shader.createLambertShaderProgram();
 		}
 		mUniforms = new Uniforms();
 
 		mStage.setShaderProgram(mShaderProgram);
 		mStage.setPointLightShaderProgram(Shader.createPointLightShaderProgram());
 		mStage.getCamera().position.z = 10000.0f;
 		mStage.getCamera().far = 15000.0f;
 
 		mSpriteBatch = new SpriteBatch();
 		mFont = new BitmapFont();
 		mFont.setColor(Color.WHITE);
 
 		MeshMaterial material = new MeshMaterial(new Color(0.33f, 0.33f, 0.33f, 1.0f), new Color(Color.WHITE), new Color(Color.BLACK), 50);
 		if (mLightingModel == LightingModel.PHONG) {
 			material.setShiny(true);
 		}
 
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
 				parallel(
 					rotateBy(360, 4.0f),
 					sequence(
 						alpha(0.0f, 1.0f),
 						alpha(1.0f, 1.0f)
 					)
 				)
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
 				rotateBy(360, 2.0f)
 			)
 		);
 		meshActor4.setRotationAxis(new Vector3(Vector3.X).add(Vector3.Y).nor());
 		mStage.addActor(meshActor4);
 
 //		for (int i = 0; i < 50;i++) {
 		MeshActor mA5 = new MeshActor();
 		mA5.setWidth(50.0f);
 		mA5.setHeight(50.0f);
 		mA5.setDepth(50.0f);
 //		mA5.setPosition(100 + 20 * i, 500);
 		mA5.setPosition(100, 500);
 		mA5.setColor(new Color(Color.GREEN));
 		mA5.setMesh(Geometry.createBicolorBipyramid(4, new Color(Color.ORANGE), new Color(Color.MAGENTA)));
 //		mA5.setMesh(Geometry.createOctahedron());
 		mA5.setMaterial(material);
 		mA5.addAction(
 			forever(
 				rotateBy(360, 2.0f)
 			)
 		);
 		mStage.addColorActor(mA5);
 //		}
 		if (mLightingModel == LightingModel.PHONG) {
 			mColorShaderProgram = Shader.createColorPhongShaderProgram();
 		} else if (mLightingModel == LightingModel.LAMBERT) {
 			mColorShaderProgram = Shader.createColorLambertShaderProgram();
 		}
 		mStage.setColorShaderProgram(mColorShaderProgram);
 
 
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
 
 		PointLight pLight4 = new PointLight();
 		pLight4 = new PointLight();
 		pLight4.setColor(new Color(Color.WHITE));
 		pLight4.setPosition(500, Gdx.graphics.getHeight() / 2 + 200, 100);
 		pLight4.setWidth(3);
 		pLight4.setHeight(3);
 		pLight4.setDistance(2000);
 		mStage.addLight(pLight4);
 
 		mShaderProgramNeedsUpdate = true;
 
 		mPlayer = new Player();
 		mLevelLoader = new LevelDeserializer();
 
 		Gson gson = new GsonBuilder()
 			.registerTypeAdapter(Level.class, new LevelDeserializer())
 			.registerTypeAdapter(MeshActor.class, new MeshActorDeserializer())
 			.create();
 
 		String json;
 		System.out.println("COLOR-BLACK-----");
 		json = gson.toJson(new Color(Color.BLACK));
 		System.out.println(json);
		String testJson = "{\"x\":100,\"y\":100,\"width\":100,\"height\":100,\"rotation\":180,\"sides\":3,\"color\":{\"r\":100,\"g\":0,\"b\":0,\"a\":1},\"altColor\":{\"r\":255,\"g\":255,\"b\":255,\"a\":1}}";
 		MeshActor jsonActor = gson.fromJson(testJson, MeshActor.class);
 		mStage.addColorActor(jsonActor);
 		jsonActor.addAction(
 			forever(
 				rotateBy(360, 2.0f)
 			)
 		);
 
 
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
 		mSpriteBatch.dispose();
 		mFont.dispose();
 		mStage.dispose();
 	}
 
 	@Override
 	public void render() {
 		update();
 
 		Color backgroundColor = mStage.getColor();
 
 		Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		Gdx.gl20.glEnable(GL20.GL_BLEND);
 		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
 		mStage.draw();
 		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
 
 		mSpriteBatch.begin();
 		mFont.draw(mSpriteBatch, Integer.toString(Gdx.graphics.getFramesPerSecond()), Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.9f);
 		mSpriteBatch.end();
 
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
 			if (mLightingModel == LightingModel.PHONG) {
 				mShaderProgram = Shader.createPhongShaderProgram();
 			} else if (mLightingModel == LightingModel.LAMBERT) {
 				mShaderProgram = Shader.createLambertShaderProgram();
 			}
 			if (mLightingModel == LightingModel.PHONG) {
 				mColorShaderProgram = Shader.createColorPhongShaderProgram();
 			} else if (mLightingModel == LightingModel.LAMBERT) {
 				mColorShaderProgram = Shader.createColorLambertShaderProgram();
 			}
 			mStage.setShaderProgram(mShaderProgram);
 			mStage.setColorShaderProgram(mColorShaderProgram);
 		}
 
 		if (mLightUniformsNeedRefresh) {
 			mLightUniformsNeedRefresh = false;
 			mUniforms.setUniforms(mShaderProgram);
 			mUniforms.setUniforms(mColorShaderProgram);
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
