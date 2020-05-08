 package ua.org.dector.ludumdare.ld24;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 /**
  * @author dector
  */
 public class TitleScreen implements Screen, InputProcessor {
     public static final String BACKGROUND = "ld24/data/title.png";
 
     TextureRegion back;
     SpriteBatch sb;
 
     private MyGame game;
 
     public TitleScreen(MyGame game) {
         this.game = game;
     }
 
     public void render(float delta) {
         sb.begin();
         sb.draw(back, 0, 0);
         sb.end();
     }
 
     public void resize(int width, int height) {
     }
 
     public void show() {
         back = new TextureRegion(new Texture(Gdx.files.internal(BACKGROUND)),
                 App.SCREEN_WIDTH, App.SCREEN_HEIGHT);
         
         sb = new SpriteBatch();
         Gdx.input.setInputProcessor(this);
     }
 
     public void hide() {
         dispose();
     }
 
     public void pause() {
     }
 
     public void resume() {
     }
 
     public void dispose() {
         back.getTexture().dispose();
         sb.dispose();
     }
 
     public boolean keyDown(int keycode) {
        Sounds.get().play(Sounds.SELECT);
        
        if (keycode == Input.Keys.ESCAPE)
            Gdx.app.exit();
        else
            game.setScreen(new GameScreen());
         
         return true;
     }
 
     public boolean keyUp(int keycode) {
         return false;
     }
 
     public boolean keyTyped(char character) {
         return false;
     }
 
     public boolean touchDown(int x, int y, int pointer, int button) {
         return false;
     }
 
     public boolean touchUp(int x, int y, int pointer, int button) {
         return false;
     }
 
     public boolean touchDragged(int x, int y, int pointer) {
         return false;
     }
 
     public boolean mouseMoved(int x, int y) {
         return false;
     }
 
     public boolean scrolled(int amount) {
         return false;
     }
 }
