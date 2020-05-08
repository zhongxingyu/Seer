 package jam.ld24.game;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Zombie0World extends StateBasedGame {
     
     public Zombie0World() {
         super("Zombie0's World - v1.0");
         this.addState(new PublisherState(C.States.PUBLISHER_STATE.value));
         this.addState(new StartState(C.States.START_STATE.value));
         this.addState(new MainState(C.States.MAIN_STATE.value));
         this.addState(new CreditsState(C.States.CREDITS_STATE.value));
         this.addState(new InstructionsState(C.States.INSTRUCTIONS_STATE.value));
         
        //this.enterState(C.States.PUBLISHER_STATE.value);
        this.enterState(C.States.MAIN_STATE.value);
     }
     
     public static void main(String[] args) throws SlickException 
     {
          AppGameContainer app = new AppGameContainer(new Zombie0World());
          app.setDisplayMode(C.SCREEN_WIDTH, C.SCREEN_HEIGHT, false);
          app.setShowFPS(C.DEBUG_MODE);
          app.setMouseGrabbed(true);
          app.start();
     }
 
     @Override
     public void initStatesList(GameContainer gc) throws SlickException {
         this.getState(C.States.PUBLISHER_STATE.value).init(gc, this);
         this.getState(C.States.START_STATE.value).init(gc, this);
         //this.getState(C.States.MAIN_STATE.value).init(gc, this);
         this.getState(C.States.CREDITS_STATE.value).init(gc, this);
         this.getState(C.States.INSTRUCTIONS_STATE.value).init(gc, this);
     }
 }
 
