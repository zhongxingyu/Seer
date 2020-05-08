 package ch.zhaw.arsphema;
 
 import ch.zhaw.arsphema.screen.*;
 import ch.zhaw.arsphema.services.Services;
 import com.badlogic.gdx.Game;
 
 public class MyGdxGame extends Game {
 
     private MainMenuScreen mainMenuScreen;
     private GameScreen gameScreen;
     private OptionScreen optionScreen;
     private HighscoreScreen highscoreScreen;
     private HighscoreInsertScreen highscoreInsertScreen;
     private CreditsScreen creditsScreen;
 	private LoadingScreen loadingScreen;
 
 
     @Override
     public void create() {
         //loader screens
     	loadingScreen = new LoadingScreen(this);

         setScreen(loadingScreen);


     }
     
     public void initScreens() {
     	mainMenuScreen = new MainMenuScreen(this);
         createNewGame();
         optionScreen = new OptionScreen(this);
         highscoreScreen = new HighscoreScreen(this);
         highscoreInsertScreen = new HighscoreInsertScreen(this);
         creditsScreen = new CreditsScreen(this);
 
         //load profile
         Services.initProfileManager();
 //        Services.getProfileManager().createTestProfile(); //testing
         Services.getProfileManager().loadPlayerProfile();
     }
 
     public void gameOver(int score) {
         if(score>Services.getProfileManager().loadPlayerProfile().getMinimalScore()){
             highscoreInsertScreen.setScore(score);
             setScreen(highscoreInsertScreen);
         }else{
             setScreen(mainMenuScreen);
         }
     }
 
     public MainMenuScreen getMainMenuScreen() {
         return mainMenuScreen;
     }
 
     public void createNewGame() {
         gameScreen = new GameScreen(this);
     }
 
     public GameScreen getGameScreen() {
         return gameScreen;
     }
 
     public OptionScreen getOptionScreen() {
         return optionScreen;
     }
 
     public HighscoreScreen getHighscoreScreen() {
         return highscoreScreen;
     }
 
     public HighscoreInsertScreen getHighscoreInsertScreen() {
         return highscoreInsertScreen;
     }
 
     public CreditsScreen getCreditsScreen() {
         return creditsScreen;
     }
 
     @Override
     public void dispose() {
         super.dispose();
         loadingScreen.dispose();
         highscoreInsertScreen.dispose();
         highscoreScreen.dispose();
         optionScreen.dispose();
         creditsScreen.dispose();
         gameScreen.dispose();
         mainMenuScreen.dispose();
     }
 }
