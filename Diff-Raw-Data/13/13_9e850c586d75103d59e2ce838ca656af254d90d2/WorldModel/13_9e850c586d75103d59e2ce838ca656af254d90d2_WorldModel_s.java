 package world;
 
 import java.util.Locale;
 import java.util.Observable;
 
 
 
 public class WorldModel extends Observable {
   public enum WorldEvents {
     CONFIG_CHANGED, STEP_FORWARD;
   }
   public enum GameState {
     STOP, PLAY, PAUSE, GAME_OVER;
   }
   private int    score=0;
   private int	 cntEaten=0;
   private int    speed=4;
   private int    [][] gameGrid;
   private GameState   state;
   private Locale locale;
   
   
   public WorldModel(){
     setLocale(Locale.getDefault());
     state = GameState.STOP;
   }
   /**
    * @param width grid width
    * @param height grid height
    */
   public void setGridDimensions(int width, int height) {
     
     
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
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
    this.locale = locale;
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
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
    this.state = state;
   }
   /**
    * @return the state
    */
   public GameState getState() {
     return state;
   }
   /**
    * @return the gameGrid
    */
   public int    [][] getGameGrid() {
     return gameGrid.clone(); //TODO : Deep clone
   }
  
  
 }
