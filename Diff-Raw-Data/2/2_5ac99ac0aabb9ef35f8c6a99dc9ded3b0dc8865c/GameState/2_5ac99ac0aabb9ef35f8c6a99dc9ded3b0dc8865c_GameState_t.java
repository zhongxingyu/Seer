 package duckhunt;
 //Import Statements
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.OpenGLException;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class GameState extends BasicGameState {
 
     //Declaring and Instantiating Variables
     private boolean mousePressed;
     private boolean catHit;
     private boolean pointScored;
     private boolean playMusic = true;
     private Image land;
     private Image scoreImg;
     private Image cursor = null;
     private NyanCat cat = null;
     private Music gameMusic = null;
     private int time;
     private int difficulty = 3;
     private int stateId = -1;
     private int score = 0;
 
     public GameState(int id) {
         this.stateId = id;
     }
 
     @Override
     public int getID() {
         return this.stateId;
     }
 
     @Override
     public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
         //Initialise Music
         gameMusic = new Music("music/patty.wav");
         gameMusic.loop();
 
         //Initialise Resources
 
         land = new Image("images/bkgd.png");
         scoreImg = new Image("images/score.png");
         cat = new NyanCat(gc.getHeight(), gc.getWidth());
         cursor = new Image("images/cursor.png"); /*Uncompressed PNG 2^n by 2^n dimensions Required */
         
         //Set the mouse cursor to the cursor image variable
         gc.setMouseCursor(cursor, 16, 16);
 
     }
 
     @Override
     public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 
         try {
             g.drawImage(land, 0, 0);
             g.drawImage(scoreImg, 50, 600);
             
             //Render the cat
             cat.render(g);
 
             if (mousePressed && catHit) {
 
                 if (pointScored == false) {
                     score += 20;
 
                     cat.reset();
                 }
             }
 
             g.drawString(Integer.toString(score), 150, 615);
            g.drawString(Integer.toString(30 - (time / 1000)), 150, 640);
 
         } catch (OpenGLException ex) {
             // just ignore it - prevents random crashes
             System.err.println("OpenGLException: " + ex.getMessage());
         }
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
         int posX = Mouse.getX();
         int posY = gc.getHeight() - Mouse.getY();
 
         time += delta;
 
         cat.think(delta * difficulty);
 
         Input input = gc.getInput();
         if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
             mousePressed = true;
             catHit = cat.contains(new Vector2f(posX, posY));
         } else {
             mousePressed = false;
         }
         //Pause music, but only if it is currently playing.
         if (input.isKeyPressed(Input.KEY_M) & gameMusic.paused() == false) {
             gameMusic.pause();
         }
         if (input.isKeyPressed(Input.KEY_N) & gameMusic.paused() == true) {
             gameMusic.loop();
         }
 
         //Timer
         if ((time / 1000) > 30) {
             DuckHunt.setScore(score);
             sbg.enterState(2);
         }
         if (input.isKeyPressed(Input.KEY_X)) {
             DuckHunt.setScore(score);
             sbg.enterState(2);
         }
     }
 }
