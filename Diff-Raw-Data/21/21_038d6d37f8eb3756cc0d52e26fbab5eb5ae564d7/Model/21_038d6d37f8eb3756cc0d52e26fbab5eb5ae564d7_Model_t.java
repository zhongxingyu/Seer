 package arcade.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import arcade.database.Database;
 import arcade.games.ArcadeInteraction;
 import arcade.games.Game;
 import arcade.games.GameData;
 import arcade.games.GameInfo;
 import arcade.games.HighScores;
 import arcade.games.MultiplayerGame;
 import arcade.games.User;
 import arcade.games.UserGameData;
 import arcade.view.LoginView;
 import arcade.view.MainView;
 
 
 public class Model implements ArcadeInteraction {
     public static final String DEFAULT_LOGIN_MESSAGE = "";
     private static final String LOGIN_FAILURE_MESSAGE =
             "The username or password you entered is incorrect";
     private static final String REGISTER_FAILURE_MESSAGE = "That username is already taken.";
     private ResourceBundle myResources;
     private LoginView myLoginView;
     private String myLanguage;
     private Database myDb = new Database();
     private Map<String, GameInfo> myGameInfos = new HashMap<String, GameInfo>();
     private List<GameInfo> mySnapshots;
     private String myUser;
     
     // These will be null until you try to play a game 
     Game myCurrentGame = null;
     MultiplayerGame myCurrentMultiplayerGame = null;
 
     public Model (ResourceBundle rb, String language) {
         myResources = rb;
         myLanguage = language;
     }
 
     public void setLoginView (LoginView login) {
         myLoginView = login;
     }
     
     /**
      * 
      * @param directoryPath
      */
     public void publishGame(String directoryPath) {
         return;
     }
     
 
     /**
      * This should be called after a developer enters the information about
      * his / her game. The method will add the game entry to the database and
      * create a new GameInfo to display in the gamecenter.
      * 
      * This sanitizes all the input so we guarantee that all names an genres are 
      * lowercase on the backend.
      * 
      * @param gameName
      * @param genre
      */
     public void publish (String gameName, String genre) {
         gameName = gameName.toLowerCase();
         genre = genre.toLowerCase();
         myDb.createGame(gameName, genre);
         addGameInfo(newGameInfo(gameName, genre));
     }
 
     private GameInfo newGameInfo (String name, String genre) {
         return new GameInfo(name, genre, myLanguage, this);
     }
 
     private void addGameInfo (GameInfo game) {
         myGameInfos.put(game.getName(), game);
     }
 
     public void authenticate (String username, String password) {
         if (myDb.authenticateUsernameAndPassword(username, password)) {
             myLoginView.dispose();
             organizeSnapshots();
             new MainView(this, myResources);
         }
         else {
             myLoginView.sendMessage(LOGIN_FAILURE_MESSAGE);
         }
     }
 
     /**
      * Create a new user profile by entering user-specific information.
      * This information is eventually stored in the database.
      */
     public void createNewUserProfile (String username,
                                       String pw,
                                       String firstname,
                                       String lastname,
                                       String dataOfBirth) {
        if(myDb.createUser(username, pw, firstname, lastname, dataOfBirth)){
            new LoginView(this , myResources);
        }

        
        
     }
 
     public void createNewUserProfile (String username,
                                       String pw,
                                       String firstname,
                                       String lastname,
                                       String dataOfBirth,
                                       String filepath) {
       System.out.println(myDb.createUser(username, pw, firstname, lastname, dataOfBirth, filepath));
         authenticate(username, pw);
     }
 
     public void deleteUser (String username) {
         myDb.deleteUser(username);
     }
 
     /**
      * Rate a specific game, store in user-game database
      */
     public void rateGame (double rating, String gameName) {
         myDb.updateRating(myUser, gameName, rating);
     }
 
     public void playGame (GameInfo gameinfo) {
         myCurrentGame = gameinfo.getGame(this);
         myCurrentGame.run();
     }
 
     public void playMultiplayerGame (GameInfo gameinfo) {
         MultiplayerGame game = gameinfo.getMultiplayerGame(this);
         game.run();
     }
 
     /**
      * TODO:
      * Get the list of games from the database.
      * 
      * @return
      */
     public Collection<GameInfo> getGameList () {
         return myGameInfos.values();
     }
 
     private void organizeSnapshots () {
         List<String> gameNames = myDb.retrieveListOfGames();
         for (String name : gameNames) {
             addGameInfo(newGameInfo(name, myDb.getGenre(name)));
         }
     }
 
     /**
      * GameDetailPanel must call this method to get game-specific info.
      * 
      * @param gameName: name of the chosen game (String)
      * @return
      */
     public GameInfo getGameDetail (String gameName) {
         return myGameInfos.get(gameName);
     }
 
     /**
      * TODO: Must add user-game specific detail
      * 
      * @param user ,game (whatever that identifies the user and the game)
      * @return
      */
     public UserGameData getUserGameData (String user, String game) {
         // Query database to get info specific to the user and the game (e.g. scores)
         return null;
     }
 
     @Override
     public User getUser () {
         // TODO get the user's avatar, figure out how we are implementing user infor for games
         return null;
     }
 
     public double getAverageRating (String gameName) {
         return myDb.getAverageRating(gameName);
     }
 
     @Override
     public HighScores getHighScores (int n) {
         // TODO I wish I understood how we are planning on implementing high scores . . .
         // nonetheless: do database stuff here
         return null;
     }
 
     @Override
     public void killGame () {
         // save the usergamedata and game data if applicable, and return to detail screen
 
     }
 
     @Override
     public UserGameData getUserGameData (String gameName) {
         UserGameData ugd = myDb.getUserGameData(gameName, myUser);
         if (ugd == null) {
             // use reflection to find the game class and call the generate user profile method
         }
         return ugd;
     }
 
     @Override
     public GameData getGameData (String gameName) {
         GameData gd = myDb.getGameData(gameName);
         if (gd == null) {
             // use reflection to find the game class and call the generate game method
         }
         return gd;
     }
 
 }
