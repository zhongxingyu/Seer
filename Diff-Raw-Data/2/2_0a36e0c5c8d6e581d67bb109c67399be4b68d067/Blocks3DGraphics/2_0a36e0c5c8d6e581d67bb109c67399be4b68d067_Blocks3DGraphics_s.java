 package de.blocks;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Input.Peripheral;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
 import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 
 public class Blocks3DGraphics implements ApplicationListener {
 	public static StillModel PlaneModel;
 	public static StillModel BlockModel;
 	public static Texture SpadesTexture;
 	public static Texture BlackTexture;
 
 	private BlocksGame blocksGame;
 	private PerspectiveCamera camera;
 	private ShaderProgram shader;
 	private boolean previousSpacePress;
 	private SpriteBatch spriteBatch;
 	private BitmapFont font;
 
 	// variables for rotation handling
 	private float startAzimuth;
 	private boolean showOrientationIndicator;
 	private int orientationIndicator;
 	private int currentOrientationZone;
 	private int lastOrientationZone;
 	private int activeOrientationZone;
 	private float[] borderOrientationZone = new float[4];
 
 	private float accelerationZ;
 
 	@Override
 	public void create() {
 		final ObjLoader loader = new ObjLoader();
 		Blocks3DGraphics.PlaneModel = loader.loadObj(Gdx.files
 				.internal("data/plane.obj"));
 		Blocks3DGraphics.BlockModel = loader.loadObj(Gdx.files
 				.internal("data/block.obj"));
 		Blocks3DGraphics.SpadesTexture = new Texture(
 				Gdx.files.internal("data/Spades.png"), true);
 		Blocks3DGraphics.SpadesTexture.setFilter(TextureFilter.MipMap,
 				TextureFilter.MipMap);
 		Blocks3DGraphics.SpadesTexture.setWrap(TextureWrap.ClampToEdge,
 				TextureWrap.ClampToEdge);
 
 		Blocks3DGraphics.BlackTexture = new Texture(
 				Gdx.files.internal("data/black.png"), true);
 		Blocks3DGraphics.BlackTexture.setFilter(TextureFilter.MipMap,
 				TextureFilter.MipMap);
 		Blocks3DGraphics.BlackTexture.setWrap(TextureWrap.ClampToEdge,
 				TextureWrap.ClampToEdge);
 
 		// System.out.println(BlockModel.subMeshes[0].getMesh().getVertexAttribute(Usage.TextureCoordinates).alias);
 
 		blocksGame = new BlocksGame();
 
 		camera = new PerspectiveCamera(45.0f, Gdx.graphics.getWidth(),
 				Gdx.graphics.getHeight());
 		camera.near = 0.1f;
 		camera.translate(0.0f, 0.0f, 40.0f);
 		camera.lookAt(0.0f, 0.0f, 0.0f);
 		camera.update();
 
 		spriteBatch = new SpriteBatch();
 		font = new BitmapFont();
 
 		String shaderSuffix;
 
 		if (Gdx.app.getType() == ApplicationType.Android) {
 			shaderSuffix = "Android";
 		} else {
 			shaderSuffix = "Desktop";
 		}
 
 		shader = new ShaderProgram(Gdx.files.internal("data/basicVertexShader"
 				+ shaderSuffix), Gdx.files.internal("data/basicFragmentShader"
 				+ shaderSuffix));
 
 		if (!shader.isCompiled()) {
 			System.out.println("Shader compilation failed!");
 			shader.begin();
 			System.out.println(shader.getLog());
 			shader.end();
 		} else {
 			System.out.println("Shader compiled sucessfully!");
 		}
 
 		previousSpacePress = false;
 
 		// setting start orientation
 		if (Gdx.input.isPeripheralAvailable(Peripheral.Compass)) {
 
 			startAzimuth = Gdx.input.getAzimuth() + 180.0f;
 			borderOrientationZone[0] = 0;
 			borderOrientationZone[1] = 90;
 			borderOrientationZone[2] = 180;
 			borderOrientationZone[3] = 270;
 			orientationIndicator = 0;
 			showOrientationIndicator = false;
 			currentOrientationZone = 1;
 			lastOrientationZone = 1;
 			activeOrientationZone = 1;
 		}
 
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void render() {
 		final float delta = Gdx.graphics.getDeltaTime();
 		blocksGame.update(delta);
 
 		// Movement input
 		if (Gdx.input.isKeyPressed(Keys.A)) {
 			blocksGame.getGameField().moveBlocks(-0.2f, 0.0f);
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.D)) {
 			blocksGame.getGameField().moveBlocks(0.2f, 0.0f);
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.W)) {
 			blocksGame.getGameField().moveBlocks(0.0f, 0.2f);
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.S)) {
 			blocksGame.getGameField().moveBlocks(0.0f, -0.2f);
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
 			if (!previousSpacePress) {
 				blocksGame.nextColor();
 			}
 
 			previousSpacePress = true;
 		} else {
 			previousSpacePress = false;
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.X))
 			blocksGame.removeBlocksFromRemovalZones();
 
 		if (Gdx.app.getType() == ApplicationType.Android) {
 			blocksGame.getGameField().moveBlocks(0.01f * Gdx.input.getDeltaX(),
 					-0.01f * Gdx.input.getDeltaY());
 
 			// touch control of color change to be removed in final version
 			if (Gdx.input.justTouched()) {
 				blocksGame.nextColor();
 				Gdx.input.vibrate(50);
 			}
 
 			// color change by rotating device
 			if ((Math.abs(Gdx.input.getPitch()) <= 8.0f)
 					&& (Math.abs(Gdx.input.getRoll()) <= 8.0f)) {
 				if (changeOfOrientationZone(Gdx.input.getAzimuth())) {
 					blocksGame.nextColor();
 					Gdx.input.vibrate(50);
 				}
 			}
 			else {
 				showOrientationIndicator = false;
 			}
 
 			// Accelerator axis are inverted due to landscape mode !!
 			float deltaY = Gdx.input.getAccelerometerX() / 10;
 			float deltaX = Gdx.input.getAccelerometerY() / 10;
 
 			blocksGame.getGameField().moveBlocks(deltaX, 0.0f);
 			blocksGame.getGameField().moveBlocks(0.0f, -deltaY);
 
 			if (Math.abs(accelerationZ - Gdx.input.getAccelerometerZ()) >= 8) {
 				blocksGame.removeBlocksFromRemovalZones();
 				long[] pattern = { 0, 30, 15, 30 };
 				Gdx.input.vibrate(pattern, -1);
 			}
 
 			accelerationZ = Gdx.input.getAccelerometerZ();
 		}
 
 		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(),
 				Gdx.graphics.getHeight());
 		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
 		Gdx.gl.glEnable(GL10.GL_BLEND);
 		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
 		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
 		Gdx.gl.glCullFace(GL10.GL_BACK);
 		shader.begin();
 		shader.setUniformMatrix("u_viewProjectionMatrix", camera.combined);
 		blocksGame.render(shader);
 		shader.end();
 		Gdx.gl.glDisable(GL10.GL_CULL_FACE);
 
 		spriteBatch.begin();
 
 		if (Gdx.input.isPeripheralAvailable(Peripheral.Compass)) {
 			font.draw(spriteBatch, "Azimuth: " + Gdx.input.getAzimuth(), 10,
 					Gdx.graphics.getHeight() - 10);
 			font.draw(spriteBatch, "Pitch: " + Gdx.input.getPitch(), 10,
 					Gdx.graphics.getHeight() - 30);
 			font.draw(spriteBatch, "Roll: " + Gdx.input.getRoll(), 10,
 					Gdx.graphics.getHeight() - 50);
 			font.draw(spriteBatch,
 					"AccelerometerZ: " + Gdx.input.getAccelerometerZ(), 10,
 					Gdx.graphics.getHeight() - 70);
 			if (showOrientationIndicator)  {
 				font.draw(spriteBatch,
 						"OrientationChangeProgress: " + orientationIndicator + " %", 10,
 						Gdx.graphics.getHeight() - 90);
 			}
 		} else {
 			font.draw(spriteBatch, "No Compass available", 10,
 					Gdx.graphics.getHeight() - 10);
 		}
 
 		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10,
 				30);
 		font.draw(spriteBatch, "Score : " + blocksGame.getScore(),
 				Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 10);
 
 		blocksGame.renderMessages(spriteBatch, font);
 		spriteBatch.end();
 	}
 
 	// calculation active zone for rotation
 	private boolean changeOfOrientationZone(float azimuth) {
 
 		boolean changeOfZone;
 		int newOrientationZone;
 
 		changeOfZone = false;
 		azimuth = (azimuth + 180.0f + (45 - startAzimuth)) % 360;
 		if ((azimuth >= borderOrientationZone[0])
 				&& (azimuth < borderOrientationZone[1])) {
 			newOrientationZone = 1;
 		} else {
 			if ((azimuth >= borderOrientationZone[1])
 					&& (azimuth < borderOrientationZone[2])) {
 				newOrientationZone = 2;
 			} else {
 				if ((azimuth >= borderOrientationZone[2])
 						&& (azimuth < borderOrientationZone[3])) {
 					newOrientationZone = 3;
 				} else {
 					newOrientationZone = 4;
 				}
 			}
 		}
 		if (newOrientationZone != currentOrientationZone) {
 			lastOrientationZone = currentOrientationZone;
 			currentOrientationZone = newOrientationZone;
 			showOrientationIndicator = false;
 		}
 		else {
 			 if (azimuth >= (borderOrientationZone[newOrientationZone - 1] - 45)) {
 				 showOrientationIndicator = true;
 				 if (newOrientationZone < 4) {
					 orientationIndicator = (int) ((borderOrientationZone[newOrientationZone] - azimuth) / 0.45f);
 				 }
 				 else {
 					 orientationIndicator = (int) ((360 - azimuth) / 0.45f);
 				 }
 			 }
 			 else {
 				 showOrientationIndicator = false;
 			 }
 		}
 		if ((currentOrientationZone == ((activeOrientationZone % 4) + 1))
 				&& (lastOrientationZone == activeOrientationZone)) {
 			changeOfZone = true;
 			activeOrientationZone = newOrientationZone;
 		}
 
 		return changeOfZone;
 	}
 
 	@Override
 	public void resize(final int width, final int height) {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
