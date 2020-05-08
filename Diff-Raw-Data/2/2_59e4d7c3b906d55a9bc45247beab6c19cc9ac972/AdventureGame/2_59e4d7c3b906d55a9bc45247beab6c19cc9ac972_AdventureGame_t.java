     /*
  * By Marieta, Aron and Kelvin
  * In January 2013
  */
 
 package summativeproject;
 
 import states.*;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.*;
 
 /**
  *
  * @author root
  */
 public class AdventureGame extends StateBasedGame{
 
     public static final int menu = 0;
     public static final int play = 1;
     public static final int gameover = 2;
     public static final int gamewin = 3;
 
     //sets the title to Adventure Game
     public AdventureGame(){
         super("Adventure Game");
         this.addState(new MainMenuState(menu));
         this.addState(new GameplayState(play));
         this.addState(new GameOverState(gameover));
         this.addState(new GameWinState(gamewin));
 
     }
 
     public void initStatesList(GameContainer container) throws SlickException {
         this.getState(menu) .init(container, this);
         this.getState(play) .init(container, this);
         this.getState(gameover) .init(container, this);
         this.getState(gamewin) .init(container, this);
         this.enterState(menu);
     }
 
     public static void main(String[] args) {
         //run the program container, else give back error message
         try{
             AppGameContainer app = new AppGameContainer(new AdventureGame());
             app.setDisplayMode(480, 480, false);
             app.setTargetFrameRate(60);
            app.setShowFPS(false);
             app.start();
         }
         catch(SlickException e){
             e.printStackTrace();
         }
     }
 }
