 package eu32k.ludumdare.ld26.stages;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 import eu32k.ludumdare.ld26.effects.EffectsManager;
 import eu32k.ludumdare.ld26.effects.IRunningEffect;
 import eu32k.ludumdare.ld26.gameplay.GameEventHandler;
 import eu32k.ludumdare.ld26.gameplay.GameplayEvent;
 import eu32k.ludumdare.ld26.gameplay.GameplayEvent.GameplayEventType;
 import eu32k.ludumdare.ld26.level.Level;
 import eu32k.ludumdare.ld26.level.Tile;
 import eu32k.ludumdare.ld26.level.TileSpawner;
 import eu32k.ludumdare.ld26.objects.Goal;
 import eu32k.ludumdare.ld26.objects.Player;
 import eu32k.ludumdare.ld26.rendering.MainRenderer;
 import eu32k.ludumdare.ld26.state.GameState;
 import eu32k.ludumdare.ld26.state.GlobalState;
 import eu32k.ludumdare.ld26.state.LevelLosingState;
 import eu32k.ludumdare.ld26.state.LevelState;
 import eu32k.ludumdare.ld26.state.PlayerState;
 import eu32k.ludumdare.ld26.state.StateMachine;
 
 public class GameStage extends Stage {
 
    private static final float ZOOM = 5.55555555555f;
    private Camera camera;
 
    private MainRenderer renderer;
    private Player player;
    private Level level;
    private EffectsManager effects;
 
    private TileSpawner tileSpawner;
 
    private GameEventHandler eventHandler;
 
    private LevelState levelState;
 
    private PlayerState playerState;
 
    private GlobalState globalState;
    private float pauseTimer;
 
    public GameStage(EffectsManager effects) {
       this.effects = effects;
 
       float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
       camera = new OrthographicCamera(2.0f * aspectRatio * ZOOM, 2.0f * ZOOM);
 
       tileSpawner = new TileSpawner();
       eventHandler = new GameEventHandler();
       levelState = StateMachine.instance().getState(LevelState.class);
       globalState = StateMachine.instance().getState(GlobalState.class);
 
       renderer = new MainRenderer();
       levelState.setTextConsole(renderer.getConsole());
       StateMachine.instance().createState(new PlayerState());
       playerState = StateMachine.instance().getState(PlayerState.class);
       player = playerState.getPlayer();
 
       levelState.initLevel(5, 5);
       tileSpawner.init();
 
       renderer.getConsole().addLine("Hallo Velo");
       renderer.getConsole().addLine("Hallo Tibau abuas");
    }
 
    @Override
    public void draw() {
       boolean running = levelState.isRunning();
       float delta = Gdx.graphics.getDeltaTime();
 
       if (!levelState.ready()) {
          return;
       }
       this.level = levelState.getLevel();
       // updates --------------------------------------
       setPlayerAndGoalTile();
 
       pauseTimer -= delta;
 
       if (running) {
 
          repositionGoal();
 
          levelState.getEvents().tick(delta);
 
          tileSpawner.update(delta);
 
          updateRunningEffects(delta);
 
          checkGameConditions(delta);
 
          updatePlayerInput(delta);
       } else if (levelState.isPaused()) {
          if (pausedPressed()) {
             globalState.getEvents().enqueue(new GameplayEvent(GameplayEventType.RESUME));
             return;
          }
 
       }
       GameState currentState = StateMachine.instance().getCurrentState();
       if (currentState instanceof LevelLosingState) {
          LevelLosingState lState = (LevelLosingState) currentState;
          if (lState.handleKeys(delta) && (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) || (Gdx.input.isTouched()))) {
             globalState.getEvents().enqueue(new GameplayEvent(GameplayEventType.LOSE, 0, GameplayEvent.PARAM_LOSE_TOLOST));
 
          }
       }
       // tileAnimator.update(delta);
 
       camera.position.x = level.getWidth() / 2.0f;
       camera.position.y = level.getHeight() / 2.0f;
       camera.update();
 
       renderer.setPaused(levelState.isPaused());
       // rendering ------------------------------------
       renderer.render(delta, camera, level.getTiles(), player, levelState.getGoal(), effects.getCurrentColor());
    }
 
    private void repositionGoal() {
       Goal goal = levelState.getGoal();
       if (goal != null && !goal.isFreeMovement() && levelState.goalTile == null) {
          levelState.repositionGoal();
       }
    }
 
    private void checkGameConditions(float delta) {
       List<IRunningEffect> runningEffects = levelState.getRunningEffects();
       if (runningEffects.size() > 0) {
          int count = 0;
          boolean movingTileInvolved = false;
          for (Tile t : levelState.getLevel().getTiles()) {
             Vector2 shiftedPosition = player.getShiftedPosition();
             if (!player.canMoveIntoTile(shiftedPosition, t)) {
                count++;
                if (t.isMoving()) {
                   movingTileInvolved = true;
                }
             }
             if (movingTileInvolved && count > 1) {
                levelState.getEvents().enqueue(new GameplayEvent(GameplayEventType.LOSE, 0, GameplayEvent.PARAM_LOSE_SQUASHED));
             }
          }
       }
 
       if (levelState.playerTile == null) {
          levelState.deathConditionTimer += delta;
          if (levelState.deathConditionTimer > 0.05) {
             levelState.getEvents().enqueue(new GameplayEvent(GameplayEventType.LOSE, 0, GameplayEvent.PARAM_LOSE_FALLOFFBOARD));
          }
       } else {
          levelState.deathConditionTimer = 0;
       }
       
       if(levelState.playerTile != null && levelState.playerTile.equals(levelState.goalTile))
       {
          Goal g = levelState.getGoal();
          if(g.intersects(player))
          {
             levelState.getEvents().enqueue(new GameplayEvent(GameplayEventType.WIN));         
          }
       }
    }
 
    private void updateRunningEffects(float delta) {
       List<IRunningEffect> runningEffects = levelState.getRunningEffects();
       Iterator<IRunningEffect> effectsIterator = runningEffects.iterator();
       while (effectsIterator.hasNext()) {
          IRunningEffect effect = effectsIterator.next();
          if (effect.complete()) {
             effectsIterator.remove();
          } else {
             effect.update(delta);
             if (effect.complete()) {
                effectsIterator.remove();
             }
          }
       }
    }
 
    private void updatePlayerInput(float delta) {
       // player inputs --------------------------------
       boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
       boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
       boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
       boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
 
       if (pausedPressed()) {
          levelState.getEvents().enqueue(new GameplayEvent(GameplayEventType.PAUSE));
          return;
       }
 
       boolean escapePressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
 
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
 
       if (escapePressed) {
          effects.stopSong(null);
       }
    }
 
    private boolean pausedPressed() {
       boolean pause = Gdx.input.isKeyPressed(Input.Keys.P);
       if (pause) {
          if (pauseTimer <= 0) {
             pauseTimer = 0.5f;
             return true;
          }
       }
       return false;
    }
 
    private void setPlayerAndGoalTile() {
       float px = player.position.x;
       float py = player.position.y;
       Goal g = levelState.getGoal();
       boolean setPlayerTile = !(levelState.playerTile != null && levelState.playerTile.contains(px, py));
      boolean setGoalTile = !(levelState.goalTile != null && levelState.playerTile.contains(g.getX(), g.getY()));
       
       if (setPlayerTile) {
          Tile t = findPlayerTile();
          levelState.playerTile = t;
          if (t != null) {
             renderer.getConsole().addLine("Player entered tile on position: " + Float.toString(t.getX()) + "/" + Float.toString(t.getY()));
          }
       }
       if (setGoalTile) {
          Tile t = findGoalTile();
          levelState.goalTile = t;
          if (t != null) {
             renderer.getConsole().addLine("Goal entered tile on position: " + Float.toString(t.getX()) + "/" + Float.toString(t.getY()));
          }
 
       }
    }
 
    private Tile findPlayerTile() {
       List<Tile> tiles = levelState.getLevel().getTiles();
       for (Tile tile : tiles) {
          if (!tile.isDead() && tile.contains(player.position.x, player.position.y)) {
             return tile;
          }
       }
       return null;
    }
 
    private Tile findGoalTile() {
       Goal goal = levelState.getGoal();
       List<Tile> tiles = levelState.getLevel().getTiles();
       for (Tile tile : tiles) {
          if (!tile.isDead() && tile.contains(goal.position.x, goal.position.y)) {
             return tile;
          }
       }
       return null;
    }
 }
