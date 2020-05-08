 package sps.graphics;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import sps.bridge.DrawDepth;
 import sps.core.Point2;
 import sps.core.Settings;
 
 public class Renderer {
 
     // This is the resolution used by the game internally
     public static final int VirtualHeight = Settings.get().spriteHeight * Settings.get().tileMapHeight;
     public static final int VirtualWidth = Settings.get().spriteWidth * Settings.get().tileMapWidth;
 
     private static Renderer instance;
 
     public static Renderer get() {
         if (instance == null) {
             instance = new Renderer();
         }
         return instance;
     }
 
     private final SpriteBatch batch;
     private final OrthographicCamera camera;
 
     private Renderer() {
         camera = new OrthographicCamera();
         camera.setToOrtho(false, VirtualWidth, VirtualHeight);
         batch = new SpriteBatch();
     }
 
     public Point2 center() {
         return new Point2(VirtualWidth / 2, VirtualHeight / 2);
     }
 
     public void begin() {
         Gdx.gl.glClearColor(0, 0, 0, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
         camera.update();
         batch.setProjectionMatrix(camera.combined);
         batch.begin();
     }
 
     public void end() {
         batch.end();
     }
 
     // Sprite rendering
     public void draw(Sprite sprite, Point2 position, DrawDepth depth, Color color) {
         render(sprite, position, depth, color, Settings.get().spriteWidth, Settings.get().spriteHeight);
     }
 
     public void draw(Sprite sprite, Point2 position, DrawDepth depth, Color color, float scaleX, float scaleY) {
         render(sprite, position, depth, color, scaleX, scaleY);
     }
 
     private void render(Sprite sprite, Point2 position, DrawDepth depth, Color color, float scaleX, float scaleY) {
         sprite.setColor(color);
         sprite.setSize(scaleX, scaleY);
         sprite.setPosition(position.X, position.Y);
         sprite.draw(batch);
     }
 
     // String rendering
     public void drawString(String content, Point2 location, Color filter, float scale, DrawDepth depth) {
         renderString(content, location, filter, scale, depth);
     }
 
     private void renderString(String content, Point2 location, Color filter, float scale, DrawDepth depth) {
         Assets.get().font().setScale(scale);
         Assets.get().font().setColor(filter);
        Assets.get().font().draw(batch, content, location.X, location.Y);
     }
 }
