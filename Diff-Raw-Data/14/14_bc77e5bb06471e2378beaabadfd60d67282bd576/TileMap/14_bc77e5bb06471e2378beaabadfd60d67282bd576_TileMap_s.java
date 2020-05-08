 package com.punchline.javalib.entities.components.tile;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.entities.components.Component;
 import com.punchline.javalib.entities.components.ComponentManager;
 import com.punchline.javalib.entities.components.physical.Transform;
 import com.punchline.javalib.entities.components.render.Renderable;
 import com.punchline.javalib.utils.Convert;
 import com.punchline.javalib.utils.Display;
 
 /**
  * Component wrapper for a map made in Tiled. When added to an Entity, this will automatically add bodies to the
  * Box2D physics world, as defined by the Tiled map's "physics" object layer and the given materials XML file.
  * @author Natman64
  * @created Aug 1, 2013
  */
 public class TileMap implements Component, Renderable, Transform {
 
 	//region Fields/Initialization
 	
 	private static TmxMapLoader loader = new TmxMapLoader();
 	
 	private TiledMap map;
 	private MapBodyManager bodyManager;
 	private OrthogonalTiledMapRenderer renderer;
 	
 	/**
 	 * Constructs a TileMap component.
 	 * @param world The EntityWorld containing this map.
 	 * @param mapFilename The file path of the map's .tmx file.
 	 * @param materialsFilename The file path of the XML file containing the materials info for the map.
 	 */
 	public TileMap(EntityWorld world, String mapFilename, String materialsFilename) {
 		map = loader.load(mapFilename);
 		
 		bodyManager = new MapBodyManager(world.getPhysicsWorld(), 
 				Convert.getMeterPixelRatio(), materialsFilename, 0);
		
		renderer = new OrthogonalTiledMapRenderer(map);
 	}
 	
 	//endregion
 	
 	//region Transform Implementation
 	
 	@Override
 	public Vector2 getPosition() {
 		return Vector2.Zero.cpy();
 	}
 
 	@Override
 	public float getRotation() {
 		return 0f;
 	}
 
 	@Override
 	public Vector2 getOrigin() {
 		return Vector2.Zero.cpy();
 	}
 
 	@Override
 	public float getWidth() {
 		return 0f;
 	}
 
 	@Override
 	public float getHeight() {
 		return 0f;
 	}
 
 	//endregion
 	
 	//region Renderable Implementation
 	
 	@Override
 	public void setPosition(Vector2 position) { }
 
 	@Override
 	public void setRotation(float degrees) { }
 
 	@Override
 	public void setScale(float scaleX, float scaleY) { }
 
 	@Override
 	public void setOrigin(Vector2 origin) { }
 
 	@Override
 	public void draw(SpriteBatch spriteBatch, float deltaSeconds) {
 		renderer.setView(spriteBatch.getProjectionMatrix().cpy(), 0, 0, Display.getRealWidth(), Display.getRealHeight());
 		renderer.render();
 	}
 
 	//endregion
 	
 	//region Events
 	
 	@Override
 	public void onAdd(ComponentManager container) {
 		bodyManager.createPhysics(map);
 	}
 
 	@Override
 	public void onRemove(ComponentManager container) {
 		bodyManager.destroyPhysics();
 		map.dispose();
 		renderer.dispose();
 	}
 
 	//endregion
 	
 }
