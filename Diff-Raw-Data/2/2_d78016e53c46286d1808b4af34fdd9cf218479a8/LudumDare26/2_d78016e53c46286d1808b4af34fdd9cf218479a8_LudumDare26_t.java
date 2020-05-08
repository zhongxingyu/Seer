 package eu32k.ludumdare.ld26;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 import eu32k.libgdx.SimpleGame;
 import eu32k.ludumdare.ld26.effects.EffectsManager;
 import eu32k.ludumdare.ld26.level.Level;
 import eu32k.ludumdare.ld26.level.TileSpawner;
 import eu32k.ludumdare.ld26.rendering.MainRenderer;
 import eu32k.ludumdare.ld26.state.GlobalState;
 import eu32k.ludumdare.ld26.state.LevelFinishedState;
 import eu32k.ludumdare.ld26.state.LevelState;
 import eu32k.ludumdare.ld26.state.MenuState;
 import eu32k.ludumdare.ld26.state.PauseState;
 import eu32k.ludumdare.ld26.state.PlayerState;
 import eu32k.ludumdare.ld26.state.StateMachine;
 
 public class LudumDare26 extends SimpleGame {
 
    private MainRenderer renderer;
    private Player player;
    private Level level;
    private EffectsManager effects;
    
    private TileSpawner tileSpawner;
    
    private LevelState levelState;
 
    public LudumDare26() {
       super(false);
       StateMachine.instance().createState(new GlobalState());
       StateMachine.instance().createState(new PlayerState());
       StateMachine.instance().createState(new MenuState());
       StateMachine.instance().createState(new LevelState());
       StateMachine.instance().createState(new LevelFinishedState());
       StateMachine.instance().createState(new PauseState());
       StateMachine.instance().enterState(LevelState.class);
       
       effects = new EffectsManager();
       tileSpawner = new TileSpawner();
       levelState = StateMachine.instance().getState(LevelState.class);
    }
 
    @Override
    public void init() {
       renderer = new MainRenderer();
 
       player = new Player(13.5f, 13.5f);
       level = new Level(5, 5);
       level.generateRandomTiles();
       
       levelState.setLevel(level);
 
       tileSpawner.init();
       effects.initBitbreak(0);
    }
 
    private float zoom = 100.0f;
 
    @Override
    public void draw(float delta) {
 
       // player inputs --------------------------------
       boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
       boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
       boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
       boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
 
       boolean escapePressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
 
       // updates --------------------------------------
       Vector2 velocity = new Vector2(0.0f, 0.0f);
       if (up) {
          velocity.add(new Vector2(0.0f, 1.0f));
       }
       if (down) {
          velocity.add(new Vector2(0.0f, -1.0f));
       }
       if (left) {
          velocity.add(new Vector2(-1.0f, 0.0f));
       }
       if (right) {
          velocity.add(new Vector2(1.0f, 0.0f));
       }
       if (Gdx.input.isTouched()) {
          Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
          camera.unproject(touch);
         velocity = new Vector2(touch.x - Player.WIDTH / 2, touch.y - Player.HEIGHT / 2).sub(player.position);
       }
       velocity.nor();
       velocity.mul(delta);
       player.move(velocity, level.getTiles());
 
       setZoom(zoom);
 
       if (escapePressed) {
          effects.stopSong(null);
       }
       StateMachine.instance().getState(GlobalState.class).getEvents().tick(delta);
       tileSpawner.update(delta);
       effects.update(delta);
 
       camera.position.x = 100;
       camera.position.y = 60;
       camera.update();
 
       // rendering ------------------------------------
       renderer.render(delta, camera, level, level.getTiles(), player, effects.getCurrentColor());
    }
 
    @Override
    public void dispose() {
       super.dispose();
       renderer.dispose();
    }
 }
