 package fr.kohen.alexandre.framework.systems;
 
 import java.util.HashMap;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.managers.GroupManager;
 import com.artemis.systems.EntityProcessingSystem;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.FrameBuffer;
 import com.badlogic.gdx.maps.MapLayer;
 import com.badlogic.gdx.maps.MapObject;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 
 import fr.kohen.alexandre.framework.base.Systems;
 import fr.kohen.alexandre.framework.components.CameraComponent;
 import fr.kohen.alexandre.framework.components.MapComponent;
 import fr.kohen.alexandre.framework.components.Transform;
 import fr.kohen.alexandre.framework.systems.interfaces.CameraSystem;
 import fr.kohen.alexandre.framework.systems.interfaces.MapDrawSystem;
 
 public class DefaultMapSystem extends EntityProcessingSystem implements MapDrawSystem {
 	protected ComponentMapper<MapComponent> 	mapMapper;
 	protected ComponentMapper<CameraComponent> 	cameraMapper;
 	protected ComponentMapper<Transform> 		transformMapper;
 	protected FrameBuffer framebuffer = new FrameBuffer(Format.RGBA4444, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
 	protected HashMap<Entity, Sprite> spriteMap = new HashMap<Entity, Sprite>();
 	protected ImmutableBag<Entity> cameras;
 	protected CameraSystem cameraSystem;
 	protected HashMap<Integer, Entity> maps;
 	
 	
 	@SuppressWarnings("unchecked")
 	public DefaultMapSystem() {
 		super( Aspect.getAspectForAll(MapComponent.class) );
 	}
 	
 	@Override
 	public void initialize() {
 		cameraSystem		= Systems.get(CameraSystem.class, world);
 		
 		mapMapper 		= ComponentMapper.getFor(MapComponent.class, world);
 		cameraMapper 	= ComponentMapper.getFor(CameraComponent.class, world);
 		transformMapper = ComponentMapper.getFor(Transform.class, world);
 		
 		maps			= new HashMap<Integer, Entity>();
 	}
 
 	protected void begin() {
 		cameras = world.getManager(GroupManager.class).getEntities("CAMERA");
 	}
 	
 	@Override
 	protected void process(Entity e) {
 		for (int i = 0, s = cameras.size(); s > i; i++) {
 			Entity cameraEntity = cameras.get(i);
 			if( mapMapper.get(e).mapId == transformMapper.get(cameraEntity).mapId ) {
 				OrthogonalTiledMapRenderer renderer = new OrthogonalTiledMapRenderer(mapMapper.get(e).tmap, 1);
 				OrthographicCamera camera = new OrthographicCamera( Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
 				camera.setToOrtho(true);
 				camera.rotate( - transformMapper.get(cameraEntity).rotation );
 				camera.translate(
 						transformMapper.get(cameraEntity).position.x - transformMapper.get(e).position.x - cameraMapper.get(cameraEntity).size.x/2, 
 						transformMapper.get(cameraEntity).position.y - transformMapper.get(e).position.y - cameraMapper.get(cameraEntity).size.y/2
 					);
 				camera.update();
 				
 				
 				framebuffer.begin();
 				Gdx.gl.glClearColor(1, 1, 1, 0);
 				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 					renderer.setView( camera );
 					renderer.render();
 				framebuffer.end();
 				
 				TextureRegion region = new TextureRegion(
 						framebuffer.getColorBufferTexture(), 
 						(int) ((Gdx.graphics.getWidth() - cameraMapper.get(cameraEntity).size.x) / 2), 
 						(int) ((Gdx.graphics.getHeight() - cameraMapper.get(cameraEntity).size.y) / 2), 
 						(int) cameraMapper.get(cameraEntity).size.x, 
 						(int) cameraMapper.get(cameraEntity).size.y
 					);
 				
 				Sprite sprite = new Sprite(region);
 				sprite.setPosition(
 						transformMapper.get(cameraEntity).position.x - cameraMapper.get(cameraEntity).size.x/2, 
 						transformMapper.get(cameraEntity).position.y - cameraMapper.get(cameraEntity).size.y/2
 					);
 				spriteMap.put(e, sprite);
 				cameraSystem.addToCamera(cameraEntity, e);
 			}
 		}
 	}
 	
 	@Override
 	protected void inserted(Entity e) {
 		maps.put(mapMapper.get(e).mapId, e);
 		
 		for (MapLayer layer : mapMapper.get(e).tmap.getLayers()) {
 			if (layer.isVisible()) {
 				if (layer instanceof TiledMapTileLayer) {
 					for (int x = 0; x < ((TiledMapTileLayer) layer).getWidth(); x++) {
 						for (int y = 0; y < ((TiledMapTileLayer) layer).getHeight(); y++) {
 							checkTile(x, y, (TiledMapTileLayer) layer, mapMapper.get(e).mapId);
 						}
 					}
 				} else {
 					for (MapObject object : layer.getObjects()) {
 						checkObject(object, (TiledMapTileLayer) layer, mapMapper.get(e).mapId);
 					}
 				}
 			}				
 		}
 	}
 	
 
 	protected void checkTile(int x, int y, TiledMapTileLayer layer, int mapId) {
 	}
 	protected void checkObject(MapObject object, TiledMapTileLayer layer, int mapId) {
 	}
 
 	
 	public void draw(Entity e, SpriteBatch batch) {
 		Sprite sprite = spriteMap.get(e);
 		if( sprite != null ) {
 			sprite.draw(batch);
 		}
 		
 		
 	}
 
 	public Entity getMap(int mapId) {
 		return maps.get(mapId);
 	}
 	
 	@Override
 	public boolean canProcess(Entity e) {
 		return mapMapper.has(e);
 	}
 	
 	@Override
 	protected void removed(Entity e) {
 		maps.remove(e);
 	}
 	
 }
