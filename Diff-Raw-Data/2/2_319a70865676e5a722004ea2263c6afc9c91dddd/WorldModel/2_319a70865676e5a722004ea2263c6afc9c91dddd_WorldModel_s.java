 package world;
 
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.Observable;
 import java.util.Queue;
 
 
 
 public class WorldModel extends Observable {
   public enum WorldEvents {
     CONFIG_CHANGED, STEP_FORWARD;
   }
   public enum GameState {
     STOP, PLAY, PAUSE, GAME_OVER;
   }
   public enum Direction {
 	UP, DOWN, LEFT, RIGHT;
   }
   private int    score=0;
   private int	 cntEaten=0;
   private int    speed=4;
   private int gridWidth=100;
   private int gridHeight=100;
   private int    [][] gameGrid;
   private GameState   state;
   private Locale locale;
   private LinkedList<GridPoint> snake=new LinkedList<GridPoint>();
   private Direction nextDirection = Direction.LEFT;
   private GridPoint insect = new GridPoint(0, 0);
   
   public WorldModel(){
     setLocale(Locale.getDefault());
     state = GameState.STOP;
     snake.addFirst(new GridPoint(gridWidth/2, gridHeight/2));
     snake.addFirst(new GridPoint(gridWidth/2-1, gridHeight/2));
     snake.addFirst(new GridPoint(gridWidth/2-2, gridHeight/2));
     snake.addFirst(new GridPoint(gridWidth/2-2, gridHeight/2-1));
     snake.addFirst(new GridPoint(gridWidth/2-2, gridHeight/2-2));
   }
   /**
    * @param width grid width
    * @param height grid height
    */
   public void setGridDimensions(int width, int height) {
     gridWidth=width; gridHeight=height;
   }
   /**
    * @param speed the speed to set
    */
   public void setSpeed(int speed) {
     this.speed = speed;
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
   }
   /**
    * @return the speed
    */
   public int getSpeed() {
     return speed;
   }
   /**
    * @param score the score to set
    */
   public void setScore(int score) {
     this.score = score;
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
   }
   /**
    * @return the score
    */
   public int getScore() {
     return score;
   }
   /**
    * set the number of eaten insects
    * @param cntEaten number of eaten insects
    */
   public void setCntEaten(int cntEaten) {
 	  setChanged();
 	  notifyObservers(WorldEvents.CONFIG_CHANGED);
 	  this.cntEaten = cntEaten;	  
   }
   /**
    * get the number of eaten insects
    */
   public int getCntEaten() {
 	  return cntEaten;	  
   }
   /**
    * @param locale the locale to set
    */
   public void setLocale(Locale locale) {
 	this.locale = locale;
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
   }
   /**
    * @return the locale
    */
   public Locale getLocale() {
     return locale;
   }
   /**
    * @param state the state to set
    */
   public void setState(GameState state) {
 	this.state = state;
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
   }
   /**
    * @return the state
    */
   public GameState getState() {
     return state;
   }
   /**
    * sets the next direction to d
    * @param d next direction
    */
   public void setNextDirection(Direction d) {
 	  nextDirection = d;
   }
   /**
    * @return the insect
    */
   public GridPoint getInsect(){
 	  return insect;
   }
   /**
    * @return the Queue representing the snake
    */
   public Queue<GridPoint> getSnake(){
 	  return snake;
   }
   
   /**
    * Move snake to its next position.
    */
   public void stepForward() {
 	GridPoint crt = snake.getFirst();
 	GridPoint next;
 	
 	switch(nextDirection) {
 		case UP:
 			next = new GridPoint(crt.getX(), crt.getY()-1);
 			break;
 		case DOWN:
 			next = new GridPoint(crt.getX(), crt.getY()+1);
 			break;
 		case LEFT:
 			next = new GridPoint(crt.getX()-1, crt.getY());
 			break;
 		case RIGHT:
 			next = new GridPoint(crt.getX()+1, crt.getY());
 			break;
 		default:
 			next = new GridPoint(crt.getX(), crt.getY());
 	}
 	
 	//TODO: if next position is not valid return error value
 	if(		next.getX()<0 || next.getX()>=gridWidth ||	//next point out of bounds
			next.getY()<0 || next.getX()>=gridHeight) {
 		return; //TODO: return error
 	}
 	
 	for(GridPoint p : snake) {	// snake eat its own tail
 		if(next.getX()==p.getX() && next.getY()==p.getY()) {
 			return; //TODO: return error
 		}
 	}
 	
 	snake.addFirst(next);
 	
 	// Insect has been eaten, snake grows longer
 	if(next.getX()==insect.getX() && next.getY()==insect.getY()) {
 		//TODO: replace insect somewhere else
 	}
 	else {
 		snake.removeLast();
 	}
 	
     setChanged();
     notifyObservers(WorldEvents.STEP_FORWARD);
   }
 }
