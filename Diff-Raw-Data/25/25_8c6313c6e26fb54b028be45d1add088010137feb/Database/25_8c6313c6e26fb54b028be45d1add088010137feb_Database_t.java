 package arcade.database;
 import arcade.controller.GameSpecificData;
 import arcade.controller.UserSpecificData;
 import arcade.games.Comment;
 import arcade.games.GameData;
 import arcade.games.Score;
 import arcade.games.UserGameData;
 import java.util.ArrayList;
 import java.util.List;
 import util.Pixmap;
 
 
 /**
  * Creates overall database
  * @author Natalia Carvalho
  */
 public class Database {
 
     private GameTable myGameTable;
     private UserTable myUserTable;
     private ScoreTable myScoreTable;
     private CommentTable myCommentTable;
     private S3Connections myS3Instance;
     
     /**
      * Database constructor
      */
     public Database () {
         myGameTable = new GameTable();
         myUserTable = new UserTable();
         myScoreTable = new ScoreTable();
         myCommentTable = new CommentTable();
         myS3Instance = new S3Connections();
     }
 
     /**
      * Closes the database connection
      */
     public void closeDatabaseConnection () {
         myGameTable.closeConnection();
         myUserTable.closeConnection();
         myScoreTable.closeConnection();
         myCommentTable.closeConnection();
     }
 
     /**
      * Creates a user when given username, pw, firstname, lastname, and dataofbirth
      * @param username is user
      * @param pw is password
      * @param firstname is first name
      * @param lastname is last name
      * @param dataOfBirth is DOB
      */
     public boolean createUser (String username,
                                String pw,
                                String firstname,
                                String lastname,
                                String dataOfBirth) {
         return myUserTable.createUser(username, pw, firstname, lastname, dataOfBirth);
     }
 
     /**
      * Creates a user when given username, pw, firstname, lastname, and dataofbirth and avatar
      * 
      * @param username is user
      * @param pw is password
      * @param firstname is first name
      * @param lastname is last name
      * @param dataOfBirth is DOB
      * @param filepath is the filepath for the avatar
      */
     public boolean createUser (String username, String pw, String firstname, String lastname,
                                String dataOfBirth, String filepath) {
         insertAvatar(username, filepath);
         return myUserTable.createUser(username, pw, firstname, lastname, dataOfBirth);
     }
     
     public boolean createUser(UserSpecificData data) {
 		return createUser(data.getUsername(), data.getPassword(), data.getFirstName(), data.getLastName(), data.getDOB(), data.getImageFilePath());
 	}
     
 
     /**
      * Creates a new game
      * @param gameName is name of game
      * @param author is author
      * @param genre is genre
      * @param price is price of game
      * @param extendsGame string
      * @param extendsMultiplayerGame string
      * @param ageRating permissions
      * @param singlePlayer game
      * @param multiplayer game
      * @param thumbnailPath location of thumbnail
      * @param adscreenPath location of adscreen 
      * @param description of game
      */
     public boolean createGame (String gameName,
                                String author,
                                String genre,
                                double price,
                                String extendsGame,
                                String extendsMultiplayerGame,
                                int ageRating,
                                boolean singlePlayer,
                                boolean multiplayer,
                                String thumbnailPath,
                                String adscreenPath,
                                String description) {
         insertGameThumbnail(gameName, thumbnailPath);
         insertAdScreenPath(gameName, adscreenPath);
         return myGameTable.createGame(gameName, author, genre, price, extendsGame,
                                       extendsMultiplayerGame, ageRating, singlePlayer, multiplayer,
                                       thumbnailPath, adscreenPath, description);
     }
 
     /**
      * Inserts avatar into S3 Instance
      * @param username user
      * @param filepath of new avatar image
      */
     public void insertAvatar (String username, String filepath) {
         myS3Instance.putAvatarIntoBucket(username, filepath);
     }
     
     /**
      * Returns pixmap of avatar
      * @param username user
      */
     public Pixmap getAvatar (String username) {
         return new Pixmap(myS3Instance.getAvatar(username));
     }
 
     /**
      * Returns date of birth of a user
      * @param username user
      */
     public String retrieveDOB (String username) {
         return myUserTable.retrieveDOB(username);
     }
 
     /**
      * Returns list of games
      */
     public List<String> retrieveListOfGames () {
         return myGameTable.retrieveGameList();
     }
     
     /**
      * Returns list of users
      */
     public List<String> retrieveListOfUsers() {
         return myUserTable.retrieveUsernames();
     }
 
     /**
      * Returns true if username and password match up, false otherwise
      * @param username is user
      * @param password is password
      */
     public boolean authenticateUsernameAndPassword (String username, String password) {
         return myUserTable.authenticateUsernameAndPassword(username, password);
     }
 
 
     /**
      * Deletes user
      * @param username is user
      */
     public void deleteUser (String username) {
         //myUserGameDataTable.deleteUser(retrieveUserId(username));
         myUserTable.deleteUser(username);
     }
 
     /**
      * Deletes game
      * @param gameName is game to be deleted
      */
     public void deleteGame (String gameName) {
         myGameTable.deleteGame(gameName);
     }
 
     /**
      * Returns true if usernameExists, false otherwise
      * @param username is user
      */
     public boolean usernameExists (String username) {
         return myUserTable.usernameExists(username);
     }
 
     /**
      * Returns true if usernameExists, false otherwise
      * @param username is user
      * @param gameName is game name
      * @param newHighScore is high score to be inserted
      */
     public void addNewHighScore (String username, String gameName, int newHighScore) {
         myScoreTable.addNewHighScore(retrieveUserId(username), retrieveGameId(gameName),
                                      newHighScore);
     }
     
     /**
      * Returns list of scores for a game
      * @param gameName is game name
      */
     public List<Score> getScoresForGame(String gameName) {
         List<String> usernames = retrieveListOfUsers();
         List<Score> myScores = new ArrayList<Score>();
         for (String user : usernames) {
             myScores.addAll(myScoreTable.getScoresForGame(retrieveGameId(gameName), 
                                                           retrieveUserId(user), gameName, user));
         }
         return myScores;
     }
     
     /**
      * Returns list of scores for a user
      * @param username is user
      */
     public List<Score> getScoresForUser(String username) {
         List<String> games = retrieveListOfGames();
         List<Score> myScores = new ArrayList<Score>();
         for (String game : games) {
             myScores.addAll(myScoreTable.getScoresForGame(retrieveGameId(game), 
                                                           retrieveUserId(username), game, username));
         }
         return myScores;
     }
     
     /**
      * Returns list of scores for a game and user
      * @param gameName is game name
      * @param username is user
      */
     public List<Score> getScoresForGameAndUser(String username, String gameName) {
         return myScoreTable.getScoresForGame(retrieveGameId(gameName), 
                                              retrieveUserId(username), gameName, username);
     }
 
     /** 
      * Stores user game data on S3 instance
      * @param gameName is game name
      * @param username is user
      * @param usd is UserGameData
      */
     public void storeUserGameData (String gameName,
                                    String username,
                                    UserGameData usd) {
         myS3Instance.putUserGameDataIntoBucket(username, gameName, usd);
     }
 
     /** 
      * Gets UserGameData from S3 Instance
      * @param gameName is game name
      * @param username is user
      */
     public UserGameData getUserGameData (String gameName, String username) {
         return myS3Instance.getUserGameDataFromBucket(username, gameName);
     }
 
     /** 
      * Stores GameData from S3 Instance
      * @param gameName is game name
      * @param gd is gameData
      */
     public void storeGameData (String gameName, GameData gd) {
         myS3Instance.putGameDataIntoBucket(gameName, gd);
     }
 
     /** 
      * Gets GameData from S3 Instance
      * @param gameName is game name
      */
     public GameData getGameData (String gameName) {
         return myS3Instance.getGameDataFromBucket(gameName);
     }
 
 
     /** 
      * Inserts comment for a user and game
      * @param username is user
      * @param gameName is game name
      * @param comment is comment to be inserted
     * @param rating is rating
      */
    public void insertCommentAndRating (String username, String gameName, 
                                        String comment, double rating) {
        myCommentTable.addNewCommentAndRating(retrieveGameId(gameName), 
                                              retrieveUserId(username), comment, rating);
     }
 
     /**
      * Retrieves all comments for game
      * @param gameName is game name
      */
     public List<Comment> retrieveCommentsForGame (String gameName) {
        List<Comment> myComments = new ArrayList<Comment>();
        List<String> myUsers = myUserTable.retrieveUsernames();
        for (String s : myUsers) {
            myComments.addAll(myCommentTable.getAllCommentsAndRatingsForGame(
                                     retrieveGameId(gameName), s, retrieveUserId(s)));
        }
        return myComments;
     }
 
     /**
      * Prints gameTable
      */
     public void printGameTable () {
         myGameTable.printEntireTable();
     }
 
     /**
      * Retrieves user table
      */
     public void printUserTable () {
         myUserTable.printEntireTable();
     }
     
     /**
      * Retrieves score table
      */
     public void printScoreTable () {
         myScoreTable.printEntireTable();
     }
     
     /**
      * Retrieves comment table
      */
     public void printCommentTable () {
         myCommentTable.printEntireTable();
     }
 
     /**
      * Retrieves a gameID from game
      * @param gameName for gameID
      */
     private String retrieveGameId (String gameName) {
         return myGameTable.retrieveGameId(gameName);
     }
 
     /**
      * Retrieves a userID for user
      * @param username is user
      */
     private String retrieveUserId (String username) {
         return myUserTable.retrieveUserId(username);
     }
 
     /**
      * Retrieves genre for game
      * @param gameName is game
      */
     public String getGenre (String gameName) {
         return myGameTable.getGenre(gameName);
     }
 
     /**
      * Retrieves author for game
      * @param gameName is game
      */
     public String getAuthor (String gameName) {
         return myGameTable.getAuthor(gameName);
     }
 
     /**
      * Retrieves age permission for game
      * @param gameName is game
      */
     public int getAgePermission (String gameName) {
         return myGameTable.getAgePermission(gameName);
     }
 
     /**
      * Retrieves price of game
      * @param gameName is game
      */
     public double getPrice (String gameName) {
         return myGameTable.getPrice(gameName);
     }
 
     /**
      * Retrieves SingleplayerGameClassKeyword of game
      * @param gameName is game
      */
     public String getSingleplayerGameClassKeyword (String gameName) {
         return myGameTable.getExtendsGame(gameName);
     }
 
     /**
      * Retrieves MultiplayerGameClassKeyword of game
      * @param gameName is game
      */
     public String getMultiplayerGameClassKeyword (String gameName) {
         return myGameTable.getExtendsGameMultiplayer(gameName);
     }
 
     /**
      * Returns true if game is single player
      * @param gameName is game
      */
     public boolean getIsSinglePlayer (String gameName) {
         return myGameTable.getIsSinglePlayer(gameName);
     }
 
     /**
      * Returns true if game is multiplayer
      * @param gameName is game
      */
     public boolean getIsMultiplayer (String gameName) {
         return myGameTable.getIsMultiplayer(gameName);
     }
 
     /**
      * Retrieves gameDescription for game
      * @param gameName is game
      */
     public String getGameDescription (String gameName) {
         return myGameTable.getDescription(gameName);
     }
 
     /**
      * Retrieves average game rating for game
      * @param gameName is game
      */
     public double getAverageRating (String gameName) {
         return myCommentTable.getAverageRating(retrieveGameId(gameName));
         
     }
 
     /**
      * Inserts game thumbnail into Amazon S3 instance
      * @param gameName is game
      * @param filepath is where game is located
      */
     public void insertGameThumbnail (String gameName, String filepath) {
         myS3Instance.putGameThumbnailIntoBucket(gameName, filepath);
     }
     
     /**
      * Inserts ad screen into Amazon S3 instance
      * @param gameName is game
      * @param filepath is where the adscreen is located
      */
     public void insertAdScreenPath (String gameName, String filepath) {
         myS3Instance.putAdScreenIntoBucket(gameName, filepath);
     }
 
     /**
      * Returns path of saved location of game thumbnail
      * @param gameName is game
      */
     public String getGameThumbnail (String gameName) {
         return myS3Instance.getThumbnail(gameName);
     }
 
     /**
      * Returns path of saved location of adscreen
      * @param gameName is game
      */
     public String getGameAdScreen (String gameName) {
         return myS3Instance.getThumbnail(gameName);
     }
 
     
     
     // USER-SPECIFIC QUERY METHODS : TO BE FILLED OUT WHEN USER-DATABASE CONTAINS ALL THESE INFO
 	public String getUserRealName(String username) {
 		// TODO Auto-generated method stub
 		return "Bob";
 	}
 
 	public int getUserAge(String username) {
 		// TODO Auto-generated method stub
 		return 18;
 	}
 
 	public String getUserHometown(String username) {
 		// TODO Auto-generated method stub
 		return "Waco";
 	}
 
 	public String getUserSchool(String username) {
 		// TODO Auto-generated method stub
 		return "Duke";
 	}
 
 	public String getUserHobbies(String username) {
 		// TODO Auto-generated method stub
 		return "Coding";
 		
 	}
 
 	public String getUserFavoriteColor(String username) {
 		// TODO Auto-generated method stub
 		return "Blue";
 		
 	}
 
 	public String getUserFavoriteFood(String username) {
 		// TODO Auto-generated method stub
 		return "rice";
 	}
 
 	
 	// Setting & changing user-specific information
 	
 	// Not sure what to pass as input: Pixmap or file-path (String)?
 	public void setUserAvatar(Pixmap newAvatar) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setUserRealName(String newName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setUserHometown(String newHometown) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setUserSchool(String newSchool) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setUserFavoriteColor(String newFavoriteColor) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setUserFavoriteFood(String newFavoriteFood) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	
 
 }
