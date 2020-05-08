 package eu32k.ludumdare.ld26.state;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.badlogic.gdx.math.Vector2;
 import com.sun.corba.se.spi.orbutil.fsm.State;
 
 import eu32k.libgdx.common.TempVector2;
 import eu32k.ludumdare.ld26.animation.TileAnimator;
 import eu32k.ludumdare.ld26.effects.GameObjectMove;
 import eu32k.ludumdare.ld26.effects.IRunningEffect;
 import eu32k.ludumdare.ld26.events.EventQueue;
 import eu32k.ludumdare.ld26.level.Level;
 import eu32k.ludumdare.ld26.level.LevelConfig;
 import eu32k.ludumdare.ld26.level.LevelConfigSequence;
 import eu32k.ludumdare.ld26.level.Tile;
 import eu32k.ludumdare.ld26.level.TileSpawner;
 import eu32k.ludumdare.ld26.objects.GameObject;
 import eu32k.ludumdare.ld26.objects.Goal;
 import eu32k.ludumdare.ld26.objects.Player;
 import eu32k.ludumdare.ld26.rendering.TextConsole;
 
 public class LevelState extends GameState {
 
    private TextConsole console;
    private boolean initializing;
    private boolean running;
    private int deathType;
    private float levelRunning;
    private Level level;
    private List<IRunningEffect> runningEffects;
    private TileAnimator tileAnimator;
 
    public Tile toPop;
    public Tile spawned;
    public Tile playerTile;
    public Tile goalTile;
 
    private Goal goal;
 
    public float deathConditionTimer;
 
    public EventQueue events;
 
    private boolean paused;
 
    private int width;
 
    private int height;
 
    private Random random;
 
    private LevelConfigSequence levels;
 
    private TileSpawner tileSpawner;
    private GlobalState globalState;
 
    public LevelState() {
       runningEffects = new ArrayList<IRunningEffect>();
       tileAnimator = new TileAnimator();
       deathConditionTimer = 0;
       events = new EventQueue();
       setLevels(new LevelConfigSequence());
       this.globalState = StateMachine.instance().getState(GlobalState.class);
    }
 
    @Override
    public void init() {
       transitions.add(LevelWinningState.class);
       transitions.add(LevelPauseState.class);
       transitions.add(LevelLosingState.class);
       transitions.add(LevelInitState.class);
       transitions.add(MenuState.class);
    }
 
    @Override
    public void destroy() {
       // TODO Auto-generated method stub
 
    }
 
    @Override
    public void enter() {
       running = true;
    }
 
    @Override
    public void leave() {
       running = false;
    }
 
    public boolean ready() {
       return level != null;
    }
 
    public Level getLevel() {
       return level;
    }
 
    public void setLevel(Level level) {
       this.level = level;
       if (level != null) {
          width = level.getWidth();
          height = level.getHeight();
       }
    }
 
    public List<IRunningEffect> getRunningEffects() {
       return runningEffects;
    }
 
    public TileAnimator getTileAnimator() {
       return tileAnimator;
    }
 
    public void setTileAnimator(TileAnimator tileAnimator) {
       this.tileAnimator = tileAnimator;
    }
 
    public void setTextConsole(TextConsole console) {
       this.console = console;
    }
 
    // public void log(String text) {
    // if (console != null) {
    // console.addLine(text);
    // }
    // }
    //
    // public void log(String text, Color color) {
    // if (console != null) {
    // console.addLine(text, color);
    // }
    // }
 
    public int getDeathType() {
       return deathType;
    }
 
    public void setDeathType(int deathType) {
       this.deathType = deathType;
    }
 
    public boolean isRunning() {
       return running;
    }
 
    public void setRunning(boolean running) {
       this.running = running;
    }
 
    public EventQueue getEvents() {
       return events;
    }
 
    public void setPaused(boolean paused) {
       this.paused = paused;
    }
 
    public boolean isPaused() {
       return paused;
    }
 
    public Goal getGoal() {
       return goal;
    }
 
    public void setGoal(Goal goal) {
       this.goal = goal;
    }
 
    public void initGame() {
       PlayerState ps = StateMachine.instance().getState(PlayerState.class);
       ps.resetStatistic(PlayerState.STATISTIC_DEATHS);
       ps.resetStatistic(PlayerState.STATISTIC_RETRIES);
       ps.resetStatistic(PlayerState.STATISTIC_SUCCESSFULTIME);
       ps.resetStatistic(PlayerState.STATISTIC_TOTALTIME);
       levels.reset();
    }
 
    public void initLevel() {
       if (levels != null) {
          initLevel(levels.getCurrentConfig());
       }
    }
 
    public void initLevel(LevelConfig cfg) {
       if (cfg == null) {
          return;
       }
       if (tileSpawner == null) {
          tileSpawner = new TileSpawner();
       }
       levelRunning = 0f;
       globalState.pool().tiles().setInUseForAll(false);
       PlayerState ps = StateMachine.instance().getState(PlayerState.class);
       events.clear();
       runningEffects.clear();
       spawned = null;
       toPop = null;
       playerTile = null;
       goalTile = null;
       random = new Random(cfg.seed);
       width = cfg.width;
       height = cfg.height;
       if (width < 3) {
          width = 3;
       }
       if (height < 3) {
          height = 3;
       }
       level = new Level(globalState.pool().tiles(), width, height);
       level.setRandom(random);
       level.setSpawnDistance(cfg.spawnDistance);
       level.generateRandomTiles();
 
       initPlacePlayerAndGoal();
 
       tileSpawner.spawnTile(5f);
 
    }
 
    private void initPlacePlayerAndGoal() {
       Vector2 p = TempVector2.tmp;
       Vector2 g = TempVector2.tmp2;
       Vector2 tmp = TempVector2.tmp3;
       p.set(random.nextInt(width), random.nextInt(height));
 
       findSuitableGoalPosition(p, g, tmp);
 
       Player player = StateMachine.instance().getState(PlayerState.class).getPlayer();
       if (goal == null) {
          goal = new Goal(0, 0);
       }
       goal.setFreeMovement(false);
       positionGameObject(goal, (int) g.x, (int) g.y);
 
       positionGameObject(player, (int) p.x, (int) p.y);
    }
 
    public void update(float delta) {
       events.tick(delta);
       tileSpawner.update(delta);
       levelRunning += delta;
    }
 
    private void findSuitableGoalPosition(Vector2 p, Vector2 g, Vector2 tmp) {
       do {
          g.set(random.nextInt(width), random.nextInt(height));
          tmp.set(g);
       } while (tmp.sub(p).len() < 1.5f);
    }
 
    public void repositionGoal() {
       // log("Repositioning goal");
       Vector2 p = TempVector2.tmp;
       Vector2 g = TempVector2.tmp2;
       Vector2 tmp = TempVector2.tmp3;
       Player player = StateMachine.instance().getState(PlayerState.class).getPlayer();
       p.set(player.getX(), player.getY());
       findSuitableGoalPosition(p, g, tmp);
       GameObjectMove move = new GameObjectMove();
      move.initMove(goal, g.x + 0.5f, g.y + 0.5f, 5f);
       runningEffects.add(move);
    }
 
    public void positionGameObject(GameObject obj, int x, int y) {
       obj.setPosition(x + 0.5f, y + 0.5f);
    }
 
    public LevelConfigSequence getLevels() {
       return levels;
    }
 
    public void setLevels(LevelConfigSequence levels) {
       this.levels = levels;
    }
 
    public boolean isInitializing() {
       return initializing;
    }
 
    public void addDeathStatistics() {
       PlayerState ps = StateMachine.instance().getState(PlayerState.class);
       addTime(ps, PlayerState.STATISTIC_TOTALTIME);
       ps.countStatistic(PlayerState.STATISTIC_DEATHS);
    }
 
    private void addTime(PlayerState ps, String key) {
       int total = ps.genericStatistics.get(key);
       ps.setStatistic(key, total + (int) levelRunning);
    }
 
    public void addRetryStatistics() {
       PlayerState ps = StateMachine.instance().getState(PlayerState.class);
       ps.countStatistic(PlayerState.STATISTIC_RETRIES);
    }
 
    public void addSuccessStatistic() {
       PlayerState ps = StateMachine.instance().getState(PlayerState.class);
       addTime(ps, PlayerState.STATISTIC_TOTALTIME);
       addTime(ps, PlayerState.STATISTIC_SUCCESSFULTIME);
 
    }
 
    public int getCurrentLevelIndex() {
       return levels == null ? 0 : levels.getLevelIndex();
    }
 
 }
