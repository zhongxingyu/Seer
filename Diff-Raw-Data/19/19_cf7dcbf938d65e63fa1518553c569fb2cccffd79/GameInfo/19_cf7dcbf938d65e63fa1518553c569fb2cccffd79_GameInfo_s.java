 package arcade.games;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import javax.swing.ImageIcon;
 import com.amazonaws.services.s3.model.AmazonS3Exception;
 import arcade.controller.Controller;
 import arcade.database.Database;
 
 
 public class GameInfo {
     private static final String USER_DIRECTORY = System.getProperty("user.dir");
     public static final String FILEPATH = "games.";
     public static final String RESOURCE_DIR_NAME = ".resources.";
 
     public static final String FILE_NAME = ".example";
 
     public static final String GAME_NAME = "name";
     public static final String THUMBNAIL_NAME = "thumbnail";
     public static final String AD_SCREEN = "adscreen";
     public static final String AGE_LIMIT_NAME = "agepermission";
     public static final String PRICE_KEYWORD = "price";
     public static final String GAME_MAIN_CLASS_KEYWORD = "extendsGame";
     public static final String MULTIPLAYER_GAME_MAIN_CLASS_KEYWORD = "extendsMultiplayergame";
     public static final String DESCRIPTION_KEYWORD = "description";
 
     private ResourceBundle myResourceBundle;
     private Controller myModel;
 
     private Database myDb;
     private String myGameName;
 
     public GameInfo (String gamename, String genre, String language, Controller model)
                                                                                       throws MissingResourceException {
         String filepath = FILEPATH + genre + "." + gamename + RESOURCE_DIR_NAME + language;
         myModel = model;
         myResourceBundle = ResourceBundle.getBundle(filepath);
 
     }
 
     public GameInfo (Database database, String id) {
         myDb = database;
         myGameName = id;
     }
 
     public ImageIcon getThumbnail () {
         return new ImageIcon(USER_DIRECTORY + myDb.getGameThumbnail(myGameName));
     }
 
     public String getName () {
         return myGameName;
     }
 
     public String getDescription () {
         return myDb.getGameDescription(myGameName);
     }
 
     public ImageIcon getAdScreen () {
         return new ImageIcon(USER_DIRECTORY + myDb.getGameAdScreen(myGameName));
     }
 
     public double getRating () {
        return myDb.getAverageRating(myGameName);
     }
 
     public double getPrice () {
         // TODO: return value from db.
 
         return 42;
     }
 
     private String getSingleplayerGameClassKeyword () {
         return myDb.getSingleplayerGameClassKeyword(myGameName);
     }
 
     public List<Comment> getComments () {
         return myDb.retrieveCommentsForGame(myGameName);
     }
 
     public List<Score> getScores () {
         return myDb.getScoresForGame(myGameName);
     }
 
     public List<Score> getSortedScores () {
         List<Score> scores = getScores();
         Collections.sort(scores);
         return scores;
     }
 
     public String getGenre () {
         return myDb.getGenre(myGameName);
     }
 
     // Here, there be shiny reflective dragons . . .
 
     /*
      * but seriously, the reflection here is really fragile, we should add some exception handling
      * when
      * we get a chance. I just want to make sure that it works for now. This whole class makes
      * me nervous cause we use a handwritten properties file and we're really relying on the game
      * name
      * being the same as the class name, being the same as the game name thats stored in the
      * database.
      * 
      * In the future, we should consider adding on to the publish procedure a way of generating the
      * properties
      * file algorithmically, which would tighten this up a little and make it less error prone. Once
      * we do that,
      * then we should be able to preserve the game names across the class name, the database game
      * name, the
      * properties file name , and the directory name. This is a later feature though. for now, the
      * game
      * developers will have to worry about getting things exactly right :(
      */
 
     // I SAY I will add better exception handling here but . . . .
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public Game getGame (Controller model) {
         Class gameClass = getSingleplayerGameClass();
         try {
             Constructor con = gameClass.getConstructor(ArcadeInteraction.class);
             try {
                 return (Game) con.newInstance(model);
             }
             catch (IllegalArgumentException e) {
                 e.printStackTrace();
             }
             catch (InstantiationException e) {
                 e.printStackTrace();}
             catch (IllegalAccessException e) {
                 e.printStackTrace();}
             catch (InvocationTargetException e) {
                 e.printStackTrace();}
         }
         catch (SecurityException e) {
             e.printStackTrace();}
         catch (NoSuchMethodException e) {
             e.printStackTrace();}
         return null;
     }
 
     // TODO make sure this doesnt break if the game isnt single player
     @SuppressWarnings("rawtypes")
     private Class getSingleplayerGameClass () {
         try {
             System.out.println(getSingleplayerGameClassKeyword());
             return Class.forName(getSingleplayerGameClassKeyword());
         }
         catch (ClassNotFoundException e) {
             // add some additional tries for letter case, then throw an exception
 
             return null;
         }
     }
 
     @SuppressWarnings("unchecked")
     public GameData getGameData (Game theGame) {
         try {
             return myDb.getGameData(myGameName);
 
         }
         catch (AmazonS3Exception e) {// this should actually be the amazon error. replace.
             @SuppressWarnings("rawtypes")
             Class game = getClass();
             Method method;
             try {
                 method = game.getMethod("generateNewGameProfile");
                 try {
                     return (GameData) method.invoke(theGame);
                 }
                 catch (IllegalArgumentException e1) {
                     e1.printStackTrace();
                 }
                 catch (IllegalAccessException e1) {
                     e1.printStackTrace();
                 }
                 catch (InvocationTargetException e1) {
                     e1.printStackTrace();
                 }
             }
             catch (SecurityException e1) {
                 e1.printStackTrace();
             }
             catch (NoSuchMethodException e1) {
                 e1.printStackTrace();
             }
             return null;
         }
     }
 
     @SuppressWarnings("unchecked")
     public UserGameData getUserGameData (Game theGame, String user) {
         try {
             return myDb.getUserGameData(myGameName, user);
 
         }
         catch (AmazonS3Exception e) {// this should actually be the amazon error. replace.
             @SuppressWarnings("rawtypes")
             Class game = getSingleplayerGameClass();
             Method method;
             try {
                 method = game.getMethod("generateNewProfile");
                 try {
                     return (UserGameData) method.invoke(theGame);
                 }
                 catch (IllegalArgumentException e1) {
                     e1.printStackTrace();
                 }
                 catch (IllegalAccessException e1) {
                     e1.printStackTrace();
                 }
                 catch (InvocationTargetException e1) {
                     e1.printStackTrace();
                 }
             }
             catch (SecurityException e1) {
                 e1.printStackTrace();
             }
             catch (NoSuchMethodException e1) {
                 e1.printStackTrace();
             }
             return null;
         }
     }
 
 }
