 package GUI;
 
 import Client.UserClient;
 
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 
 import Server.gameModule.IGameTable;
 import Server.userModule.UserObject;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 
 /**
  *
  * This class contains all the methods that are called by the GUI to access the
  * data stored in the server.
  *
  * @author mattvertescher & Peter
  */
 public class GUIClient {
 
     private static String IP = "192.168.1.101";
     private int userId;
     private UserClient currentUserClient = new UserClient();
     private UserObject currentUser;
 
     public GUIClient() {
         
     }
     
     // Login Methods
     /**
      * This allows the user to change his username if he is logged on
      *
      * @param newUsername
      * @return: true if successful, false if not
      * @author Peter
      */
     public boolean setUsername(String newUsername) throws RemoteException, SQLException {
         if (this.currentUser != null) {
             this.currentUser.setName(newUsername);
             currentUserClient.getUserProxy().editProfile(currentUser);
             return true;
         }
 
         return false;
     }
 
     /**
      * This gets the username of the current user
      *
      * @return: either username, or message saying could not acquire name
      * @author Peter
      */
     public String getUsername() {
 
         if (currentUser != null) {
             return this.currentUser.getName();
         }
 
         return "Could not get name.";
     }
 
     /**
      * Method tells server that a user has logged out.
      *
      * @return: boolean, successful or not
      */
     public boolean logout() throws RemoteException, SQLException {
         int i = currentUserClient.getUserProxy().logout(currentUser);
         currentUser = null;
         if (i == 0) {
             return true;
         }
         return false;
     }
 
     // End Connection Methods
     /**
      * Setter for email.
      *
      * @return
      * @author Peter
      */
     public boolean setEmail(String email) throws RemoteException, SQLException {
 
         currentUser.setEmail(email);
         int i = currentUserClient.getUserProxy().editProfile(currentUser);
         if (i == 0) {
             return true;
         }
         return false;
     }
 
     /**
      * Getter for email.
      *
      * @return
      * @author Peter
      */
     public String getEmail() {
         return currentUser.getEmail();
     }
 
     /**
      * Setter for password.
      *
      * @param password
      * @return
      * @author Peter
      */
     public boolean setPassword(String password) throws RemoteException, SQLException {
         currentUser.setPassword(password);
         int i = currentUserClient.getUserProxy().editProfile(currentUser);
         if (i == 0) {
             return true;
         }
         return false;
     }
 
     /**
      * Gets the password.
      *
      * @return
      * @author Peter
      */
     public String getPassword() {
 
         return currentUser.getPassword();
     }
 
     /**
      * Setter for account number.
      *
      * @param accountNumber
      * @return
      */
     public boolean setAccountNumber(String accountNumber) {
 
         // Sets account number
 
         return true;
     }
 
     /**
      * Getter for account number.
      *
      * @return account number string
      */
     public String getAccountNumber() {
 
         return "BF34NFs3Ffvt345";
     }
 
     /**
      * Registers a user on the database with the given information
      *
      * @param accountInfo
      * @return: true if successful, false if not
      * @author Peter
      */
     public boolean registerNewAccount(String[] accountInfo) throws RemoteException, SQLException {
         UserObject newUser = new UserObject(accountInfo[2], accountInfo[4], accountInfo[3], 500);
         //automatically gives the player 500 chips for signing up
         int i = currentUserClient.getUserProxy().signup(newUser);
         if (i == 0) {
             System.out.println("Account information sent to server, you are now registered.");
             return true;
         }
         return false;
     }
 
     /**
      * This logs the user in if the email password pair is correct, sets the
      * current user to the logged in user
      *
      * @param email
      * @param password
      * @return: true if successful, false if not
      * @author Peter
      */
     public boolean testEmailPassword(String email, String password) throws RemoteException, SQLException {
 
         this.currentUser = currentUserClient.getUserProxy().login(email, password);
        if(currentUser!=null)
            this.userId = currentUser.getId(););
         System.out.println("You are now logged in," + this.currentUser + ".");
 
         if (this.currentUser != null) {
             return true;
         }
 
         return false;
     }
 
     /**
      * Tells server to send a password recovery email to user.
      *
      * @param email
      * @author Peter
      */
     public boolean sendPasswordRecoveryEmail(String email) throws RemoteException, SQLException {
 
         currentUserClient.getUserProxy().recoverPassword(email);
         System.out.println("Password recovery email sent to " + email + "'s email");
 
         return true;
     }
 
     /**
      * This gets the list of GameTableId's from the server and returns it as an
      * array of Strings
      *
      * @return
      * @author Peter
      */
     public static String[] getListOfGameTables() {
         String[] gameTables = {"Default Table", "Username's Table"};
 
         // Returns a list of the game tables on the server 
         System.out.println("Get list of tables from server");
 
         return gameTables;
     }
 
     /**
      * This gets the players at a selected table
      *
      * @param table
      * @return: an array of stings of players in the game
      */
     public static String[] getPlayersAtGameTable(String tableId) {
         ArrayList<String> playernames = null;
         String[] listOfPlayers = {"Empty", "Empty", "Empty", "Empty", "Empty"};
         int i = 0;
         //need method here for lookup, should return arraylist of playernames, sets the 
         /*for (String element : playernames) {
             listOfPlayers[i] = element;
             i++;
         }*/
 
         // Returns a list of players at a particular table
         System.out.println("Get players at table, " + tableId + ", from server");
 
         return listOfPlayers;
     }
 
     /**
      * Gets only the opponents other than the current user at a table
      *
      * @param username
      * @param table
      * @return
      */
     public static String[] getOpponentsAtGameTable(String username, String table) {
         String[] listOfOpponents = {"Empty", "Empty", "Empty", "Empty"};
         String[] listOfPlayers = getPlayersAtGameTable(table);
         int i = 0;
         int j = 0;
         while (i < 5) {
             if (listOfPlayers[i].equals(username)) {
                 listOfOpponents[j] = listOfPlayers[i];
                 j++;
             }
             i++;
         }
         System.out.println("Get opponents at game table");
 
         return listOfOpponents;
     }
 
     /**
      * Returns the cards for a particular player at a table.
      */
     public static ImageIcon[] getCardsForPlayer(String username, String table) {
         ImageIcon[] cards = null;
 
         return cards;
     }
 
     /**
      * Create a new game table on the server.
      *
      * @param newTableInfo = {tableNameString, anteString, bringInString}
      * @return completed
      */
     public boolean createNewTable(String[] newTableInfo) {
         // String[] createNewTableFields = {tableNameString, anteString, bringInString}; 
 
 
         return true;
     }
 
     /**
      * This returns the ante for a given table
      *
      * @param table
      * @return
      */
     public int getTableAnte(String table) {
         IGameTable currentTable = null;//need a get table method in the server
 
         System.out.println("Getting ante...");
 
         //return currentTable.getAnteLevel();
         
         return 1;      
     }
 
     /**
      * Gets the bring in for a particular table.
      */
     public double getTableBringIn(String table) {
 
         System.out.println("Gets table bring in from server");
 
         return 234;
     }
 
     /**
      * Returns the host of a particular table.
      *
      * @param table
      * @return the host of the table
      */
     public String getTableHost(String table) {
 
         return "I am the host.";
     }
 
     
     public boolean sendBet(double bet) {
         return true;
     }
 
     public boolean fold() {
         return true;
     }
 
     public boolean allIn() {
         return true;
     }
 
     public boolean call() {
         return true;
     }
    
     /**
      * Join
      *
      * @param username
      * @param table
      * @return
      */
     public boolean joinGameTable(String table) {
         /*
         GameTable table; // need ta get gametable method
         boolean i = table.requestJoin(username);
 
         System.out.println(username + "was successful in joing the game table " + table);
         return i;
         */
         return true;
     }
 
     /**
      * Return the current table a user is apart of.
      *
      * @return table name string
      */
     public String getCurrentTable() {
         return "A Game Table";
     }
 
     /**
      * Tells the server that a user has left the table
      *
      * @param table
      * @return completed
      */
     public boolean leaveGameTable(String table) {
 
         return true;
     }
 
     /**
      * Method called if a host wishes to start the game.
      *
      * @param table
      * @return request answer
      */
     public boolean startGameRequest(String table) {
 
 
         return true;
     }
 
     /**
      * Returns the amount of money a user has
      *
      * @param username
      * @return
      */
     public String getUsersWorth(String email) throws RemoteException, SQLException {
         String worth = "Broke";
         UserObject desiredUser = currentUserClient.getUserProxy().getUserObject(email);
         worth = "" + desiredUser.getChips();
         // Returns the worth of a particular player
 
         return worth;
     }
 
     /**
      * Gets chips for a particular user.
      */
     public String getChips(String username) {
 
 
         return "203";
     }
 
     /**
      * Gets the running pot of a table.
      */
     public String getPot(String table) {
 
         // Gets the amount of money in the pot in a table, as a string
 
         return "8675309";
     }
 
     /**
      * Returns the current mimimum bet at a table.
      */
     public String getMinimumBet(String table) {
 
         return "15";
     }
 
     /**
      * This returns a list of the friends usernames of a certain user
      *
      * @param username
      * @return
      */
     public String[] getFriends(String username) throws RemoteException, SQLException {
        // UserObject gotUser = null; // need method to get user by username       
         userId = currentUser.getId();
         ArrayList<UserObject> friends = currentUserClient.getUserProxy().getFriends(userId);
         String[] friendsNames = new String[friends.size()];
         int i = 0;
         for (UserObject element : friends) {
             friendsNames[i] = element.getName();
             i++;
         }
         System.out.println("Get list of friends for user," + username + ", from server");
 
         return friendsNames;
     	//String[] friends = {"John", "Paul"};
     	//return friends; 
     	
     }
 
     /**
      * Adds the selected user as a friend to the logged on user
      *
      * @param friend
      * @return: true or false if the add was successful
      */
     public boolean addFriend(String friend) throws RemoteException, SQLException {
         UserObject friendObject = currentUserClient.getUserProxy().getUserObject(friend); //need getUserObject from username
         int i = currentUserClient.getUserProxy().addFriend(userId, friendObject.getId());
         System.out.println(currentUser.getName() + " wants to add " + friend + " as a friend");
         if (i == 0) {
             return true;
         }
         return false;
     }
 
     /**
      * Checks if the current user is friends with the given username
      *
      * @param friend
      * @return true if friends
      */
     public boolean hasFriend(String friend) throws RemoteException, SQLException {
         String[] userFriends = getFriends(getUsername());
         for (int i = 0; i < userFriends.length; i++) {
             if (userFriends[i].equals(friend)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Sends a table invite to the server.
      *
      * @param table
      * @param friend
      * @return completed
      */
     public static boolean inviteFriendToTable(String table, String friend) {
 
         return true;
     }
 
     /**
      * Deletes a friend from a players list of friends.
      *
      * @param friend
      * @return
      * @throws SQLException 
      * @throws RemoteException 
      */
     public boolean deleteFriend(String friend) throws RemoteException, SQLException {
     	UserObject friendObject = currentUserClient.getUserProxy().getUserObject(friend); //need getUserObject from username
         int i = currentUserClient.getUserProxy().deleteFriend(userId, friendObject.getId());
         System.out.println(currentUser.getName() + " wants to add " + friend + " as a friend");
         if (i == 0) {
             return true;
         }
         return false;
         
     }
 
     /**
      * Sets the 300 x 300 avatar for a user.
      */
     public static boolean setAvatar(ImageIcon avatar) {
 
         // Sets a users avatar 
 
 
         return true;
     }
 
     /**
      * Gets the 300 x 300 avatar for a particular user.
      */
     public static ImageIcon getAvatar(String username) {
         java.net.URL path = GUIClient.class.getResource("images/question_mark.jpg");
         ImageIcon avatar = new ImageIcon(path);
 
         // Returns the avatar for a particular user
 
         return avatar;
     }
 
     /**
      * Sets the avatar icon for the current user.
      *
      * @param avatar
      * @return completed
      */
     public static boolean setAvatarIcon(ImageIcon avatar) {
 
         return true;
     }
 
     /**
      * Gets the 50 x 50 avatar icon for a particular user.
      */
     public static ImageIcon getAvatarIcon(String username) {
         ImageIcon avatarIcon = new ImageIcon(GUIClient.class.getResource("images/person_icon.png"));
 
         /*
          * BufferedImage image = null; try { image =
          * ImageIO.read(GUIClient.class.getResource("/icons/smiley.jpg").toString());
          * } catch (IOException ex) { System.out.println("Image IO read
          * failed"); }
          */
         // BufferedImage bufferedImage = ImageResizer.resizeTrick(image, width, height); 
 
         //ImageIcon avatar = ImageResizer.resizeImage(GUIClient.class.getResource("/icons/smiley.jpg").toString(), 50, 50);
         //opponent1AvatarLabel.setIcon(avatar);
 
         return avatarIcon;
     }
 
     /**
      * This gets the leader board from the server
      *
      * @return
      */
     public static String[][] retrieveStatistics() throws SQLException {
         return Server.statisticsModule.leaderBoard.leaderBoardDisplay();
     }
 
     // Chat Methods
     /**
      * Sends a chat message to server.
      */
     public static boolean sendChatMessage(String usernameSender, String[] usernameRecipients, String message) {
 
         System.out.println("String message sent to server");
 
         return true;
     }
 
     // Constructor not needed 
     public GUIClient(String ip) {
 
         // Specifies server to connect to
 
         UserClient userClient = new UserClient();
 
         // Finish rmi connection here 
 
     }
 
     // Connection Methods
     /**
      * Setter for IP.
      */
     public static void setIp(String ip) {
         IP = ip;
     }
 
     /**
      * Getter for IP.
      */
     public static String getIp() {
         return IP;
     }
 
     /**
      * Method pings server to check connection.
      */
     public static boolean ping() {
 
         // Pings server to test connection
 
         return false;
     }
     // End Connection Methods
 }
