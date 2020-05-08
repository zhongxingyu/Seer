 package battleship.view;
 
 import battleship.logic.Instance;
 
 import java.io.IOException;
 
 /**
  * Application Battleship
  *
  * @author Tom Ohme
  */
 public class Application {
     private Login login;
     private Register register;
     private Menu menu;
     private Position position;
     private PositionFinished positionFinished;
     private Game game;
     private Highscore highscore;
     private Instance instance;
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         Application app = new Application();
         app.init();
         app.start();
     }
 
     private void init() {
         login = new Login(this);
         register = new Register(this);
         //file = new BinaryFile();
         //file.createFile(); TODO use appropriate way to save data (see UML)
     }
 
     private void start() {
         login();
     }
 
     public void login() {
         if (menu != null) {
             menu.setVisible(false);
         }
         register.setVisible(false);
         login.setVisible(true);
     }
 
     public void loginDone() {
         highscore = new Highscore(this);
         login.setVisible(false);
         menu();
     }
 
     public void register() {
         login.setVisible(false);
         register.setVisible(true);
     }
 
     /*
     public void registerDone() {
 		login();
 	}
 	*/
 
     public void menu() {
         positionFinished = new PositionFinished(this);
         if (highscore != null) {
             highscore.setVisible(false);
         }
         if (position != null) {
             position.setVisible(false);
         }
         if (positionFinished != null) {
             positionFinished.setVisible(false);
         }
         if (game != null) {
             game.setVisible(false);
         }
         menu = new Menu(this);
         register.setVisible(false);
         menu.setVisible(true);
     }
 
     public void position() {
         battleship.logic.User lUser = battleship.logic.User.getInstance();
         instance = Instance.getInstance();
         instance.newGame(lUser.getUser());
         position = new Position(this);
         menu.setVisible(false);
         position.setVisible(true);
     }
 
     public void positionFinished() {
         position.setVisible(false);
         positionFinished.setVisible(true);
     }
 
     public void game() {
         game = new Game(this, false);
         positionFinished.setVisible(false);
         game.setVisible(true);
     }
 
     public void gameExit() {
         try {
             instance.storeGame();
         } catch(IOException ioe) {
             // TODO Tom make popup that informs user about error
         }
         game.setVisible(false);
         menu.setVisible(true);
     }
 
     public void loadGame() {
         game = new Game(this, true);
        menu.setVisible(false);
         game.setVisible(true);
     }
 
     public void highscore() {
         menu.setVisible(false);
         highscore.setVisible(true);
     }
 }
