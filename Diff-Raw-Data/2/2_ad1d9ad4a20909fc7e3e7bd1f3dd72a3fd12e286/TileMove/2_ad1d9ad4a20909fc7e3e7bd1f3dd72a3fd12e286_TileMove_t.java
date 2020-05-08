 package eu32k.ludumdare.ld26.effects;
 
 import com.badlogic.gdx.math.Vector2;
 
 import eu32k.ludumdare.ld26.level.MoveComplete;
 import eu32k.ludumdare.ld26.level.Tile;
 import eu32k.ludumdare.ld26.objects.Goal;
 import eu32k.ludumdare.ld26.objects.Player;
 import eu32k.ludumdare.ld26.state.LevelState;
 import eu32k.ludumdare.ld26.state.PlayerState;
 import eu32k.ludumdare.ld26.state.StateMachine;
 
 public class TileMove implements IRunningEffect {
    private boolean complete;
    private Tile tile;
    private float targetX;
    private float targetY;
    private float speedX;
    private float speedY;
    private LevelState levelState;
    private Player player;
    
    public boolean complete() {
       return complete;
    }
 
    public TileMove()
    {
       this.levelState = StateMachine.instance().getState(LevelState.class);
       PlayerState playerState = StateMachine.instance().getState(PlayerState.class);
       if(playerState != null)
       {
          this.player = playerState.getPlayer();
       }
    }
    
    public void initMove(Tile tile, float targetX, float targetY, float speed)
    {
       this.tile = tile;
       this.targetX = targetX;
       this.targetY = targetY;
       complete = false;
       Vector2 t1 = Vector2.tmp;
       Vector2 t2 = Vector2.tmp2;
       t1.set(tile.getX(), tile.getY());
       t2.set(targetX, targetY);
       t2.sub(t1).nor().mul(speed);
       speedX = t2.x;
       speedY = t2.y;
    }
    
    public void update(float delta) {
       float x = tile.getX();
       float y = tile.getY();
       
       //Player offset to tile... used in case player is moved with a tile to put him in the exact same position
       float poX = 0;
       float poY = 0;
       float goX = 0;
       float goY = 0;
       Goal goal = levelState.getGoal();
       boolean movesPlayer = player != null && levelState.playerTile != null && levelState.playerTile.equals(tile);
       boolean movesGoal = goal != null && levelState.goalTile != null && levelState.goalTile.equals(tile);
      if(goal.isFreeMovement())
         movesGoal = false;
       if(movesPlayer)
       {
          poX = player.getX() - x;
          poY = player.getY() - y;
       }
 
       if(movesGoal)
       {
          goX = goal.getX() - x;
          goY = goal.getY() - y;
       }
 
       x += speedX * delta;
       y += speedY * delta;
       if ((speedX > 0 && x >= targetX) || (speedX < 0 && x <= targetX) || (speedY > 0 && y >= targetY) || (speedY < 0 && y <= targetY)) {
          levelState.getEvents().enqueue(new MoveComplete(this));
          tile.setX(targetX);
          tile.setY(targetY);
          if(movesPlayer)
          {
             player.setMovingWithTile(true);
             player.setPosition(tile.getX() + poX, tile.getY() + poY);
          }         
          if(movesGoal)
          {
             goal.setPosition(tile.getX() + goX, tile.getY() + goY);
          }         
          
          complete = true;
          return;
       }
       tile.setX(x);
       tile.setY(y);
       if(movesPlayer)
       {
          player.setMovingWithTile(false);
          player.setPosition(tile.getX() + poX, tile.getY() + poY);
       }         
       if(movesGoal)
       {
          goal.setPosition(tile.getX() + goX, tile.getY() + goY);
       }         
    }
 
    public Tile getTile() {
       return tile;
    }
 
    public void setTile(Tile tile) {
       this.tile = tile;
    }
    
 }
