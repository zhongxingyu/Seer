 package domain;
 
 import java.sql.SQLException;
 import java.util.*;
 import java.io.*;
 import persistance.SQL_Connect;
 import presentation.*;
 import data.*;
 
 public class UserController {
 	final Home home = new Home();
 	CreateDestinationHandler newDest;
 	Boundary bound;
 	UserData user;
 	SQL_Connect connect;
 	StartController start;
 	ArrayList<String> friendArrayList = new ArrayList<String>();
 	BinaryTree friends = new BinaryTree();
 	protected String userAction;
 	int intAction;
 	
 	/*
 	 * initializes 
 	 */
 	public UserController(UserData user, Boundary bound, SQL_Connect connect){
 		this.user = user;
 		this.bound = bound;
 		this.connect = connect;
 		menu();
 	}
 	/*
 	 * Displays menu for user, gets input and depending on the input decides which action to do next
 	 */
 	private void menu(){
 		getFriends();
 		home.setVisible(true);
 		addActionListener(home);
 	}
 	
 	protected void addActionListener(Home home){
 		home.addButtonActionListener1(
 				new java.awt.event.ActionListener(){
 					public void actionPerformed(java.awt.event.ActionEvent evt){
 						String userAction = ((javax.swing.JButton)evt.getSource()).getName();
 						System.out.println(userAction);
 						menuAction(userAction);
 					}
 				}
 		);
 	}
 	
 	protected boolean isNumeric(String str){
 		try{
 			Integer.parseInt(str);
 		}catch(NumberFormatException e){
 			return false;
 		}
 		return true;
 	}
 	
 	private void menuAction(String userAction){
 		int intAction = 0;
 		if ( isNumeric(userAction))
 			intAction = Integer.parseInt(userAction);
 		
 		switch (intAction){
 			case 1:
 				//newDest = new CreateDestinationHandler(user, connect);
 				break;
 			case 2: friendList();
 				break;
 			case 3: recentFriendDestinations();
 				break;
 			case 4: specificDest(user.getUserName());
 				break;
 			case 5: addFriend();
 				break;
 			case 6: 
 				String name = bound.promptForString("Enter the username of whoms you wanna browse destinations: ");
 					if(friends.contains(name))
 						specificDest(name);
 					else{
 						System.out.println(name+" is not a friend");
 						menu();
 					}
 				break;
 			// redirect to option screen
 			case 7: 
 				redirectToOption();
 				break;
 			// logout button
 			case 8: 	user = null;
 						start = new StartController();
 						start.addActionListener();	
 						break;
 			}			
 	}
 	
 	
 	/* 
 	 * Redirects you to either moderator option site, administrator option site or tells you that you dont have the access
 	 */
 	private void redirectToOption() {
 		int type = user.getType(); 
 		if (type == 1){
 			System.out.println("you don't have acces noob!");
 		} else if (type == 2){
 			final ModController mc = new ModController(user, bound, connect);
 			home.setVisible(false);
 		} else if (type == 3){
 			final AdminController ac = new AdminController(user, bound, connect);
 			home.setVisible(false);
 		}	
 	}
 	/*
 	 * Displays all your friends.
 	 */
 	private void friendList(){
 		/* If the list of friends is empty it tells the user that he doesnt have any friends yet, and returns to menu*/
 		if(friendArrayList.isEmpty()){
 			bound.printLine("You don't have any friends yet");
 			menu();
 		}
 		/* Prints out all the friends in the friend array*/
 		for(int i = 0; i < friendArrayList.size(); i++){
 			bound.printLine(friendArrayList.get(i));
 		}
 		menu();	
 	}
 	
 	/*
 	 * gets address from user and creates converts all the strings to lowercase so there wont be dublicates in the database. 
 	 */
 	private void createNewDest(){
 		bound.printLine("Create new destination. \nEnter address: ");
 		String name =bound.promptForString("Name: ");
 		String street =bound.promptForString("Street: ");
 		String city = bound.promptForString("City: ");
 		int zip = bound.promptForInt("Zip-code: ");
 		String country = bound.promptForString("Country: ");
 		checkDestAndUpdate(name.toLowerCase(),street.toLowerCase(),city.toLowerCase(),zip,country.toLowerCase());
 		menu();
 		
 	}
 	
 	/*
 	 * Gets a table from the database with all the destinations your friend have visited.
 	 * Prints the 10 most recent and if you want to see more you can ask to see 10 more.
 	 */
 	private void recentFriendDestinations(){
 		Object[][] visits = null;
 		try {
 			/* Initializes  visits to a 2 dimensional array with all destinations the users friends has visited ordered by date*/
 			visits = connect.executeQuery("CALL create_Friend_Visits('"+user.getUserName()+"')");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		/* If the 2 dimensional array is empty, it will let the user know there is no destinations and return to menu
 		 * If it is not empty it will print the destinations 10 at a time */
 		if(visits == null){
 			System.out.println("There are no destinations");
 			return;
 		}else
 			printDestinations(visits);	
 		menu();
 	}
 	
 	/*
 	 * Check is if destination already exists, opret destination i destinations tabel hvis den ikke eksistere,
 	 * Opret nyt besg.
 	 */
 	private void checkDestAndUpdate(String name, String street, String city, int zip, String country){
 		Integer picID = null;
 		Integer textID = null;
 		
 		// Call the getPicID function that asks the user if he wants to upload a picture to, returns '0' if he doesnt
 		// and if he wants it returns the primary key related to the uploaded picture and initializes tempPicID to this value.
 		int tempPicID = getPicID();
 		if(tempPicID != 0)
 			picID = tempPicID; // if tempPicID does not equal '0' it initializes picID to the value of the primary key for the picture uploaded
 		
 		// Calls the getPost function that gives the user the possibility to attach a post to the his visit
 		// It sets tempPostID to the return value of getPost which is '0' if he didnt want to attach a post.
 		// And the primary key of the post in the text table in the database.
 		int tempPostID = getPost();
 		if(tempPostID != 0)
 			textID = tempPostID;	// if tempPostID does not equal '0' it initializes textID to the value of the primary key for the post uploaded
 		try {
 			// If the destination that the user has visited already exists in the database it will initialize a 2 dimensianal array to contain
 			// only the value of the corrosponding destination ID.
 			Object[][] dest = connect.executeQuery("SELECT destID FROM destinations WHERE name ='"+name+"' AND street ='"+street+"' AND city ='"+city+
 									"' AND zip ="+zip+" AND country ='"+country+"';");
 			
 			try{
 				// initializes destID to the value in the 2 dimensional array if it is not empty if it is empty it will throw an exception
 				int destID =(Integer) dest[0][0];
 				System.out.println();
 				// Inserts username, destID, picID, textID, and the date of the upload will also be saved in the database
 				connect.executeUpdate("insert into visits values('"+user.getUserName()+ "',"+destID+","+picID+","+textID+",CURRENT_TIMESTAMP;");
 		
 			}catch(ArrayIndexOutOfBoundsException e){
 				//If the 2 dimensional array was empty means that the database does not contain that destination
 				//So it will create a the destination in the database, and auto increment the destID
 				connect.executeUpdate("INSERT INTO destinations(name,street,city,zip,country)" +
 									  "VALUES('"+name+"','"+street+"','"+city+"',"+zip+",'"+country+"');");
 				//Creates a 2 dimensional array that will contain the the biggest destID which will be the ID of the destination
 				//which was just created. And it will insert this destID in the visits table with the username, picID, textID and the timestamp
 				Object[][] mDest = connect.executeQuery("SELECT MAX(destID) FROM destinations;");
 				int maxDest = (Integer)mDest[0][0];
 				connect.executeUpdate("insert into visits values('"+user.getUserName()+ "',"+maxDest+","+picID+","+textID+",CURRENT_TIMESTAMP)");
 			}
 		} catch (SQLException e) {
 			
 		}
 	}
 	/*
 	 * gets post from user
 	 */
 	private int getPost(){
 		int textID = 0;
 		boolean repeat = true;
 		do{
 			String post = bound.promptForString("Write your post dont press enter till you're done\nTo cancel enter '0': ");
 			if (post.equals("0"))
 				return 0;
 			try {
 				connect.executeUpdate("INSERT INTO text(source) VALUES('"+post+"')");
 				Object[][] text = connect.executeQuery("SELECT max(text_ID) FROM text;");
 				textID =(Integer)text[0][0];
 				repeat = false;
 				System.out.println(repeat);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}while(repeat);
 			
 		return	textID;
 	}
 	/*
 	 * Pulls list of friends from DB, saves it in the 2 dimensional friend array
 	 */
 	private void getFriends(){
 		
 		Object[][] friendArray = null;
 		String uName = user.getUserName();
 		System.out.println(uName);
 		try {
 			/* Calls a stored procedure that will return a list of friends as a 2 dimensional array*/
 			friendArray = connect.executeQuery("CALL Create_Friendlist('"+uName+"');");
 			System.out.println("hmmm");
 			/* If the user does not have any friends, stored procedure will not have returned anything and the 2 dimensional array will
 			 * still equal null, and we will stop the method */
 			if (friendArray == null){
 				System.out.println("no friensa");
 				return;
 			}
 			/* Passes the 2 dimensional array to intialize the friends arraylist and binarytree*/
 			parseToArraylist(friendArray);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	/*
 	 * Parsing the two dimensional array to a Arraylist of friends.
 	 */
 	private void parseToArraylist(Object[][] friendArray){
 		/*Each friend in the 2 dimensional array will be saved in the friends arraylist and binarytree*/
 		for(int i = 0; i < friendArray.length;i++){
 			friends.add((String)friendArray[i][0]);
 			friendArrayList.add((String)friendArray[i][0]);
 			
 		}
 	}
 	/*
 	 * Prints the next ten destinations in the visits table
 	 */
 	private void printDestinations(Object[][] dest){
 		int index = 0;
 		do{
 			/* If index > dest.length it means that there are no more destinations in dest and it returns to menu */
 			if(index >= dest.length)
 				break;
 			if (index == 0){
 				printTenDest(index, dest);
 				index += 10;
 				continue;
 			}
 			int desition = bound.promptForInt("Type '0' to print 10 more friend destinations \n"+
 					  						  "Type any other number to go back to menu");
 			/* If user entered '0' it call printTenDest that print 10 destinations if possible
 			 * If any other number is entered it will return to menu */
 			if(desition == 0)
 				
 				printTenDest(index, dest);
 			else
 				break;
 			index += 10;
 		}while(true);
 	}
 	
 	/*
 	 * Prints up to 10 destinations from the 2 dimensional array
 	 */
 	private void printTenDest(int index, Object[][] dest){
 		int limit;
 		/* If there is not 10 more destinations in dest it will set the limit to the length of dest
 		 * Else it will set the limit to index + 10 which will result in 10 more printed destinations*/
 		if(index+10 > dest.length)
 			limit= dest.length;
 		else
 			limit = index +10;
 		/* Will print the next destination in dest till it reach the limit*/
 		for(int i = index; i < limit;i++){
 			for(int j = 0; j < dest[0].length; j++){
 				System.out.print(dest[i][j]+", ");
 			}
 			System.out.println();
 		}
 		/* Tells the user when there are no more destinations*/
 		if(index+10 > dest.length)
 			System.out.println("There are no more destinations!");
 	}
 	
 	/*
 	 * Adds a friend  to the database, arraylist and binarytree
 	 */
 	private void addFriend(){
 		bound.printLine("Add Friend");
 		String newFriend = bound.promptForString("Enter the username of the friend you want to add: ");
 		/* Checks if the username entered is already a friend, if it is it will let the user know and return to menu*/
 		if(friends.contains(newFriend)){
 			System.out.println(newFriend + " is already your friend");
 			menu();
 			return;
 		/* If the user is not alredy friends with the username entered it  will check if the user exists,
 		 * if it does it will be saved in the database, arraylist and binary tree and it will return to menu*/
 		}else if(!isNameAvailable(newFriend)){
 			try {
 				connect.executeUpdate("INSERT INTO userRelations VALUES('"+user.getUserName()+"','"+newFriend+"')");
 				friendArrayList.add(newFriend);
 				friends.add(newFriend);
 				System.out.println(newFriend+" has been added as a friend");
 				menu();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		/* If neither of the above is true, the user doesn't exist. It will let the user know and return to menu*/
 		}else{
 			System.out.println("The username entered does not exist");
 			menu();
 		}
 	}
 	
 	/*
 	 * Checks if a username is available, returns true if it is, false if not
 	 */
 	private boolean isNameAvailable(String userName){
 		Object[][] nameAvailability = null;
 		try {
 			/* If the username exists in the database, it will save the username in nameAvailability, if not the value will stay null*/
 			nameAvailability = connect.executeQuery("SELECT userName FROM users WHERE userName = '" + userName+"'");
 			
 			/* If the nameAvailabilty is empty there is no such name in the database and it will return true, else it will return false*/
 			if(nameAvailability == null)
 				return true;
 			else
 				return false;
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	/*
 	 * getPicID Asks user if he wants to upload a picture returns 0, if he doesn't. Asks for the address of the picture
 	 * If the address is correct it will save it in the database and return the related primarykey.
 	 * 
 	 * By: Jacob Espersen
 	 */
 	private int getPicID(){
 		boolean repeat = true;
 		int picID = 0;
 		do{
 			String address = bound.promptForString("Write the address of the picture\nEnter '0' to cancel");
 			if (address.equals("0")) 	//Returns zero if The user will not upload a picture
 				return 0;
 			if(address.charAt(0) != '/'){	//If the address that the user typed It will go to the next iteration of the while loop
 				bound.printLine("The address has to start with '/'");
 				continue;
 			}
 			FileInputStream fis = null;
 			try{
 				File file = new File(address); 	//Creates the file
 				fis = new FileInputStream(file);	// reads the file and creates a stream of bytes
 				connect.insertPic(fis, file); 	// inserts the byte stream in the database
 				Object[][] maxPicID = connect.executeQuery("SELECT MAX(picID) FROM pics"); // creates a 2 dimensional array with only the max value of picID
 				picID = (Integer)maxPicID[0][0]; // Passes the max value of picID to a integer
 				repeat = false;
 			}catch(Exception e){
 				System.out.println(e);
 				System.out.println("File not found try again");
 			}
 		}while(repeat);
 		return picID; // returns the picID
 	}
 	
 	/*
 	 * Gives the user the possibility to print all his destinations ordered by date
 	 */
 	private void specificDest(String name){
 		Object[][] dest = null;
 		try{
 			/* Creates a 2 dimensional array of destinations that the user has visited*/
 			dest = connect.executeQuery("SELECT name, city, country FROM destinations, visits WHERE destinations.destID = visits.destID "+
 										"AND visits.username ='"+name+"' ORDER BY visits.date DESC;" );
 			/* If the 2 dimensional array is empty, the user hasn't visited any destinations, and returns to menu,
 			 * else it prints the destinations the user visited 10 at the time*/
 			if(dest == null){
 				System.out.println("There have'nt been added any destinations yet");
 				return;
 			}else
 				printDestinations(dest);
 		}catch(SQLException e){
 			System.out.println(e);
 		}
 		menu();
 				
 	}
 
 	
 	private void reportPost(){
 		
 	}
 }
