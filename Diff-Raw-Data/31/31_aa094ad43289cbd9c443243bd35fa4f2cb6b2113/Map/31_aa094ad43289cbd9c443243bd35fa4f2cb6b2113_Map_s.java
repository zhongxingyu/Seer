 package Level;
 
 import java.util.Iterator;
 
 import Main.Game;
 import box2dLight.PointLight;
 import box2dLight.RayHandler;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.maps.MapObject;
 import com.badlogic.gdx.maps.objects.CircleMapObject;
 import com.badlogic.gdx.maps.objects.EllipseMapObject;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTile;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
 import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
 import com.badlogic.gdx.utils.Array;
 
 public class Map {
 	
 	protected TiledMap map;
 	protected OrthogonalTiledMapRenderer renderer;
 	protected OrthographicCamera camera;
 	
 	protected float lightOffsetX;
 	protected float lightOffsetY;
 	
 	public Map() {
 		
 	}
 	
 	public void create() {
 		
 		lightOffsetX = 0;
 		lightOffsetY = 0;
 		
 		TmxMapLoader loader = new TmxMapLoader();
 		map = loader.load("res/Maps/test.tmx");
 		
 		renderer = new OrthogonalTiledMapRenderer(map);
 		
 		camera = new OrthographicCamera();
 		
 		//animated tiles
 			//Frames for water tile animation
 				Array<StaticTiledMapTile> waterFrameTiles = new Array<StaticTiledMapTile>(2);
 			//Getting frame tiles
 			Iterator<TiledMapTile> tiles = map.getTileSets().getTileSet("Main").iterator();
 			while(tiles.hasNext()) {
 				TiledMapTile tile = tiles.next();
 				if(tile.getProperties().containsKey("animation") && tile.getProperties().get("animation", String.class).equals("water")) {
 					waterFrameTiles.add((StaticTiledMapTile) tile);
 				}
 			}
 			
 			AnimatedTiledMapTile water = new AnimatedTiledMapTile(1.0f, waterFrameTiles);
 			
 			TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
 			for(int x = 0; x < layer.getWidth(); x++) {
 				for(int y = 0; y < layer.getHeight(); y++) {
 					Cell cell = layer.getCell(x, y);
 					if(cell.getTile().getProperties().containsKey("animation") && cell.getTile().getProperties().get("animation", String.class).equals("water")) {
 						cell.setTile(water);
 					}
 				}
 			}
 	}
 	
 	public void render() {
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		
 		renderer.setView(camera);
 		map.getLayers().get(1).setVisible(false);
 		renderer.render();
 	}
 	
 	public void resize(int width, int height) {
 		camera.viewportWidth = width;
 		camera.viewportHeight = height;
 		camera.update();
 	}
 	
 	public OrthogonalTiledMapRenderer getRenderer() {
 		return renderer;
 	}
 	
 	public TiledMap getMap() {
 		return map;
 	}
 	
 	public OrthographicCamera getCamera() {
 		return camera;
 	}
 	
 	public float getLightOffsetX() {
 		return lightOffsetX;
 	}
 	
 	public float getLightOffsetY() {
 		return lightOffsetY;
 	}
 	
 	public void setLightOffsets(float x, float y) {
		lightOffsetX += x * Gdx.graphics.getDeltaTime();
		lightOffsetY += y * Gdx.graphics.getDeltaTime();
 	}
 }
