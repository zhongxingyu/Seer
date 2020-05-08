 package niktgar.tod.core;
 
 import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
 import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
 import static org.lwjgl.opengl.GL11.GL_PROJECTION;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.glClear;
 import static org.lwjgl.opengl.GL11.glDisable;
 import static org.lwjgl.opengl.GL11.glEnable;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glMatrixMode;
 import static org.lwjgl.opengl.GL11.glOrtho;
 import static org.lwjgl.opengl.GL11.glViewport;
 import niktgar.tod.entity.PlayerEntity;
 import niktgar.tod.level.Level;
 import niktgar.tod.level.MapLoader;
 import niktgar.tod.level.block.BlockLayer;
 import niktgar.tod.level.block.BlockMap;
 import niktgar.tod.level.block.BlockMapBuilder;
 import niktgar.tod.sprite.Sprite;
 import niktgar.tod.sprite.SpriteLoader;
 import niktgar.tod.sprite.TextureLoader;
 import niktgar.tod.sprite.animation.AnimationLoader;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 public class GameLoop {
 
     private final DisplayProperties displayProperties = new DisplayProperties(800, 600, false, "tod-hack");
 
     private final TextureLoader textureLoader;
     private final SpriteLoader spriteLoader;
     private final AnimationLoader animationLoader;
 
     private final MapLoader mapLoader;
     private final BlockMapBuilder mapBuilder;
 
     private final Timer timer;
 
     private Sprite background;
 
     private BlockLayer blockLayer;
     private BlockMap blockMap;
     private Level level;
 
     private PlayerEntity player;
 
     public GameLoop() {
         textureLoader = new TextureLoader();
         spriteLoader = new SpriteLoader(textureLoader);
         animationLoader = new AnimationLoader();
         mapLoader = new MapLoader();
         mapBuilder = new BlockMapBuilder(spriteLoader);
         timer = new Timer();
     }
 
     public void run() {
         timer.reset();
         while (!Display.isCloseRequested()) {
             glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
             glMatrixMode(GL_MODELVIEW);
             glLoadIdentity();
 
             timer.tick();
 
             Display.sync(60);
             level.update(timer.delta());
             level.handleCollisions();
             background.draw(0, 0);
             level.draw();
             Display.update();
         }
         Display.destroy();
     }
 
     public void initialize() throws TODException {
         try {
             Display.setTitle(displayProperties.title());
             Display.setFullscreen(displayProperties.isFullscreen());
             Display.setDisplayMode(new DisplayMode(displayProperties.width(), displayProperties.height()));
             Display.create();
 
             glEnable(GL_TEXTURE_2D);
             glDisable(GL_DEPTH_TEST);
             glMatrixMode(GL_PROJECTION);
             glLoadIdentity();
             glOrtho(0, displayProperties.width(), displayProperties.height(), 0, -1, 1);
             glMatrixMode(GL_MODELVIEW);
             glLoadIdentity();
             glViewport(0, 0, displayProperties.width(), displayProperties.height());
 
             player = new PlayerEntity(animationLoader.loadMaskedAnimation("entities/angry_tree"),
                     animationLoader.loadMaskedAnimation("entities/angry_tree_left"), animationLoader.loadMaskedAnimation("entities/angry_tree_right"));
 
             background = spriteLoader.loadSprite("forest.jpg");
         } catch (LWJGLException e) {
             throw new RuntimeException("Game initialization failed");
         }
     }
 
     public void loadTestLevel() throws TODException {
         blockLayer = new BlockLayer();
         blockMap = mapBuilder.buildBlockMap(mapLoader.createTestMap(), blockLayer);
         level = new Level(blockMap, blockLayer, player);
     }
 }
