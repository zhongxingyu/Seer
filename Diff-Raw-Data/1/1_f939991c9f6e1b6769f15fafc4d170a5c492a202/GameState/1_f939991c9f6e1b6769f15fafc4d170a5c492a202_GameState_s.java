 
 
 import java.util.ArrayList;
 
 import java.util.Date;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class GameState extends BasicGameState {
 
     int stateID = -1;
     
     GameState( int stateID ) {
         this.stateID = stateID;
     }
 
     PlayerObj player1 = null;
     PlayerObj player2 = null;
     Image background = null;
     UnitsList gameState = null;
     boolean isGameOver = false;
     public static String winningPlayer = "";
     int timeRemaining;
     
     @Override
     public void init(GameContainer container, StateBasedGame game)
             throws SlickException {
         // TODO Auto-generated method stub
         // TODO Auto-generated method stub
         isGameOver = false;
         winningPlayer = "";
         timeRemaining = 120000;
         //gameOverDelay = null;
         
         background = new Image("Assets/Black.jpg");
         gameState = new UnitsList();
         player1 = new PlayerObj(100, MainGame.GAME_HEIGHT - 300,
                 new Control(Input.KEY_W, Input.KEY_S, Input.KEY_A, Input.KEY_D, Input.KEY_LSHIFT),
                 new Image("Assets/Player1.png"), 1);
         player2 = new PlayerObj(MainGame.GAME_WIDTH - 100,MainGame.GAME_HEIGHT - 300,
                 new Control(Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_SPACE),
                 new Image("Assets/Player2.png"), 2);
         gameState.unitCollision.add(player1);
         gameState.unitCollision.add(player2);
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g)
             throws SlickException {
         // TODO Auto-generated method stub
         background.draw(0,0);
         
         player1.draw(container, g);
         player2.draw(container, g);
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta)
             throws SlickException {
         
         // Allow players to quit to menu with backspace or escape
         if (container.getInput().isKeyPressed(Input.KEY_ESCAPE) || container.getInput().isKeyPressed(Input.KEY_BACK)) {
             game.enterState(0);
         }
             // Update Players
             player1.update(container, delta, gameState);
             player2.update(container, delta, gameState);
         if (isGameOver){
             game.enterState(0);
         }
         timeRemaining -= delta;
         if (timeRemaining <= 0){
         	game.enterState(0);
         }
     }
         
     /**
      * Assigns points to the given player based on what kind of enemy they killed. enemyType not yet implemented.
      * @param playerNum which player made the kill
      * @param enemyType what enemy they killed
      */
     public void assignPoints(int playerNum, int enemyType){
 
     }
     
     
     public PlayerObj whichPlayer(int i){
         switch(i){
             case 1:
                 return player1;
             default:
                 return player2;
         }
     }
     
     public boolean isOffscreen(PlayerObj obj){
         Rectangle r = obj.getBoundingBox();
         if(r.getMinX() < 0 || r.getMaxX() > MainGame.GAME_WIDTH || r.getMinY() < 0 || r.getMaxY() > MainGame.GAME_HEIGHT)
             return true;
         return false;
     }
     
     @Override
     public int getID() {
         return stateID;
     }
     
     /* 
      * Method for other states to launch a new game.
      * Initializes a new game
      */
     @Override
     public void enter(GameContainer container, StateBasedGame game) throws SlickException{
         // Uses code from this.init(container, game);
         isGameOver = false;
         gameState = new UnitsList();
         player1 = new PlayerObj(10, 400,
                 new Control(Input.KEY_W, Input.KEY_S, Input.KEY_A, Input.KEY_D, Input.KEY_LSHIFT),
                 new Image("Assets/Player1.png"), 1);
         player2 = new PlayerObj(600,400,
                 new Control(Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_SPACE),
                 new Image("Assets/Player2.png"), 2);
         gameState.unitCollision.add(player1);
         gameState.unitCollision.add(player2);
     }
  
 }
