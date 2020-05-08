 package com.mangecailloux.rube;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.PolygonRegion;
 import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.Shape;
 import com.badlogic.gdx.utils.Array;
 import com.mangecailloux.rube.loader.RubeSceneLoader;
 import com.mangecailloux.rube.loader.serializers.utils.RubeImage;
 
 
 /**
  * Use the left-click to pan.  Scroll-wheel zooms.
  * 
  * @author cvayer, tescott
  *
  */
 public class RubeLoaderTest implements ApplicationListener, InputProcessor {
 	private OrthographicCamera camera;
 	private RubeSceneLoader	loader;
 	private RubeScene	scene;
 	private Box2DDebugRenderer debugRender;
 	
 	private Array<SimpleSpatial> spatials; // used for rendering rube images
 	private Array<PolySpatial> polySpatials;
 	private Map<String, Texture> textureMap;
 	private Map<Texture, TextureRegion> textureRegionMap;
 	
 	private static final Vector2 mTmp = new Vector2(); // shared by all objects
 	private SpriteBatch       batch;
 	private PolygonSpriteBatch polygonBatch;
 	
 	// used for pan and scanning with the mouse.
 	private Vector3 mCamPos;
 	private Vector3 mCurrentPos;
 	
 	@Override
 	public void create() {		
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		
 		Gdx.input.setInputProcessor(this);
 		
 		mCamPos = new Vector3();
 		mCurrentPos = new Vector3();
 		
 		camera = new OrthographicCamera(50, 50*h/w);
 		
 		loader = new RubeSceneLoader();
 		
 		scene = loader.loadScene(Gdx.files.internal("data/palm.json"));
 		
 		debugRender = new Box2DDebugRenderer();
 		
 		batch = new SpriteBatch();
 		polygonBatch = new PolygonSpriteBatch();
 		
 		textureMap = new HashMap<String, Texture>();
 		textureRegionMap = new HashMap<Texture, TextureRegion>();
 		
 		createSpatialsFromRubeImages(scene);
 		createPolySpatialsFromRubeFixtures(scene);
 		
 		//
 		// example of custom property handling
 		//
 		Array<Body> bodies = scene.getBodies();
 		if ((bodies != null) && (bodies.size > 0))
 		{
 			for (int i=0; i < bodies.size; i++)
 			{
 				Body body = bodies.get(i);
 				String gameInfo = scene.getCustom(body, "GameInfo", "");
 				System.out.println("GameInfo custom property: " + gameInfo);
 			}
 		}
 		
 		scene.printStats();
 		
 		scene.clear(); // no longer need any scene references
 	}
 
 	@Override
 	public void dispose() 
 	{
 	}
 
 	@Override
 	public void render() {		
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		scene.step();
 
 		if ((spatials != null) && (spatials.size > 0))
 		{
 			batch.setProjectionMatrix(camera.combined);
 			batch.begin();
 			for (int i = 0; i < spatials.size; i++)
 			{
 				spatials.get(i).render(batch, 0);
 			}
 			batch.end();
 		}
 		
 		if ((polySpatials != null) && (polySpatials.size > 0))
 		{
 			polygonBatch.setProjectionMatrix(camera.combined);
 			polygonBatch.begin();
 			for (int i = 0; i < polySpatials.size; i++)
 			{
 				polySpatials.get(i).render(polygonBatch, 0);
 			}
 			polygonBatch.end();
 		}
 		
 		debugRender.render(scene.world, camera.combined);
 	}
 	
 	
 	/**
 	 * Creates an array of SimpleSpatial objects from RubeImages.
 	 * 
 	 * @param scene2
 	 */
 	private void createSpatialsFromRubeImages(RubeScene scene) {
 
 		Array<RubeImage> images = scene.getImages();
 		if ((images != null) && (images.size > 0))
 		{
 			spatials = new Array<SimpleSpatial>();
 			for (int i = 0; i < images.size; i++)
 			{
 				RubeImage image = images.get(i);
 				mTmp.set(image.width,image.height);
 				String textureFileName = "data/" + image.file;
 				Texture texture = textureMap.get(textureFileName);
 				if (texture == null)
 				{
 					texture = new Texture(textureFileName);
 					textureMap.put(textureFileName, texture);
 				}
 				SimpleSpatial spatial = new SimpleSpatial(texture, image.flip, image.body, image.color, mTmp, image.center, image.angleInRads * MathUtils.radiansToDegrees);
 				spatials.add(spatial);
 			}
 		}
 	}
 	
 	
 	/**
 	 * Creates an array of PolySpatials based on fixture information from the scene.  Note that
 	 * fixtures create aligned textures.
 	 * 
 	 * @param scene
 	 */
 	private void createPolySpatialsFromRubeFixtures(RubeScene scene) {
 		Array<Body> bodies = scene.getBodies();
 		
 		if ((bodies != null) && (bodies.size > 0))
 		{	
 			polySpatials = new Array<PolySpatial>();
 			Vector2 bodyPos = new Vector2();
 			// for each body in the scene...
 			for (int i = 0 ; i < bodies.size; i++)
 			{
 				Body body = bodies.get(i);
 				bodyPos.set(body.getPosition());
 				
 				ArrayList<Fixture> fixtures = body.getFixtureList();
 				
 				if ((fixtures != null) && (fixtures.size() > 0))
 				{
 					// for each fixture on the body...
 					for (int j = 0; j < fixtures.size(); j++)
 					{
 						Fixture fixture = fixtures.get(j);
 						
 						String textureName = scene.getCustom(fixture, "TextureMask", (String)null);
 						if (textureName != null)
 						{
 							String textureFileName = "data/" + textureName;
 							Texture texture = textureMap.get(textureFileName);
 							TextureRegion textureRegion = null;
 							if (texture == null)
 							{
 								texture = new Texture(textureFileName);
 								texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
 								textureMap.put(textureFileName, texture);
 								textureRegion = new TextureRegion(texture);
 								textureRegionMap.put(texture,textureRegion);
 							}
 							else
 							{
 								textureRegion = textureRegionMap.get(texture);
 							}
 							
 							// only handle polygons at this point -- no chain, edge, or circle fixtures.
 							if (fixture.getType() == Shape.Type.Polygon)
 							{
 								PolygonShape shape = (PolygonShape)fixture.getShape();
 								int vertexCount = shape.getVertexCount();
 								float [] vertices = new float[vertexCount*2];
 								
 								// static bodies are texture aligned and do not get drawn based off of the related body.
 								if (body.getType() == BodyType.StaticBody)
 								{
 									for (int k = 0; k < vertexCount; k++)
 									{
 
 										shape.getVertex(k, mTmp);
										mTmp.rotate(body.getAngle()*MathUtils.radiansToDegrees);
 										mTmp.add(bodyPos); // convert local coordinates to world coordinates to that textures are aligned
 										vertices[k*2] = mTmp.x*PolySpatial.PIXELS_PER_METER;
 										vertices[k*2+1] = mTmp.y*PolySpatial.PIXELS_PER_METER;
 									}
 									PolygonRegion region = new PolygonRegion(textureRegion, vertices);
 									PolySpatial spatial = new PolySpatial(region, Color.WHITE);
 									polySpatials.add(spatial);
 								}
 								else
 								{
 									// all other fixtures are aligned based on their associated body. 
 									for (int k = 0; k < vertexCount; k++)
 									{
 										shape.getVertex(k, mTmp);
 										vertices[k*2] = mTmp.x*PolySpatial.PIXELS_PER_METER;
 										vertices[k*2+1] = mTmp.y*PolySpatial.PIXELS_PER_METER;
 									}
 									PolygonRegion region = new PolygonRegion(textureRegion, vertices);
 									PolySpatial spatial = new PolySpatial(region, body, Color.WHITE);
 									polySpatials.add(spatial);
 								}
 							}
 						}
 					}
 				}
 			}
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
 
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		mCamPos.set(screenX,screenY,0);
 		camera.unproject(mCamPos);
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		mCurrentPos.set(screenX,screenY,0);
 		camera.unproject(mCurrentPos);
 		camera.position.sub(mCurrentPos.sub(mCamPos));
 		camera.update();
 		return true;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		camera.zoom += (amount * 0.1f);
 		if (camera.zoom < 0.1f)
 		{
 			camera.zoom = 0.1f;
 		}
 		camera.update();
 		return true;
 	}
 }
