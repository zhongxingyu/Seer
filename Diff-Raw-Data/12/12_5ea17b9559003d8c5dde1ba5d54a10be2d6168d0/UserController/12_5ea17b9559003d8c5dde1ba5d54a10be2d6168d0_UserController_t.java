 package domain;
 
 import java.sql.SQLException;
 import java.util.*;
 
 import persistance.SQL_Connect;
 import presentation.Boundary;
 
 import data.BinaryTree;
 import data.UserData;
 
 
 public class UserController {
 	Boundary bound;
 	UserData user;
 	SQL_Connect connect;
	ArrayList<String> friendArrayList = new ArrayList();
	BinaryTree friends = new BinaryTree();
 	
 	/*
 	 * initializes 
 	 */
 	public UserController(UserData user, Boundary bound, SQL_Connect connect){
 		this.user = user;
 		this.bound = bound;
 		this.connect = connect;
 		getFriends();
 		menu();
 	}
 	/*
 	 * Displays menu for user, gets input and depending on the input decides which action to do next
 	 */
 	private void menu(){
 		bound.displayLoggedInMenu();
 		switch(bound.promptForInt("")){
 			case 1: 
 				createNewDest();
 				break;
 			case 2:
 				friendList();
 				break;
 			case 3: 
 				recentFriendDestinations();
 				break;
 //			case 4: 
 //				addFriend();
 				
 			// case 5 tilfoej ven
 			// case 6 egne destinationer
 			// 
 				//break;
 		}
 	}
 	/*
 	 * Displays all your friends.
 	 */
 	private void friendList(){
 		for(int i = 0; i < friendArrayList.size(); i++){
 			bound.printLine(friendArrayList.get(i));
 		}
 		do{
 			bound.seperator();
 			int input = bound.promptForInt("Type '0' to go to menu");
 			if(input == 0)
 				menu();
 			else
 				bound.printLine("Invalid input");
 		}while(true);
 		
 	}
 	/*
 	 * gets address from user and creates destionation. 
 	 * WHAT THE FUCKING FUCK? 
 	 * HJLP MANDAG TAK
 	 */
 	private void createNewDest(){
 		bound.printLine("Create new destination. \nEnter address: ");
 		String name = bound.promptForString("Name: ");
 		String street = bound.promptForString("Street: ");
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
 			visits = connect.executeQuery("CALL create_Friend_Visits('"+user.getUserName()+"')");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		if(visits != null){
 			int index = 0;
 			showTenDest(index, visits);
 			do{
 				index += 10;
 				int desition = bound.promptForInt("Type '0' to print 10 more friend destinations \n"+
 									  "Type any other key to go back to menu");
 				if(desition==0)
 					showTenDest(index,visits);
 				else{
 					break;
 				}
 				
 			}while(true);
 		}
 	}
 	
 	/*
 	 * Check is if destination already exists, opret destination i destinations tabel hvis den ikke eksistere,
 	 * Opret nyt besg.
 	 */
 	private void checkDestAndUpdate(String name, String street, String city, int zip, String country){
 		// get pictures method
 		//String post = getPost();
 		try {
 			Object[][] dest = connect.executeQuery("SELECT destID FROM destinations WHERE name ="+name+"street ="+street+"AND city ="+city+
 									"AND zip ="+zip+"country ="+country);
 			try{
 				int destID =(Integer) dest[0][0];
 				connect.executeUpdate("insert into visits values('"+user.getUserName()+ "',"+destID+", null,null,CURRENT_TIMESTAMP");
 			}catch(ArrayIndexOutOfBoundsException e){
 				connect.executeUpdate("INSERT INTO destinations(name,street,city,zip,country)" +
 									  "VALUES('"+name+"','"+street+"','"+city+"',"+zip+",'"+country+"');");
 				Object[][] mDest = connect.executeQuery("SELECT MAX(destID) FROM destinations;");
 				int maxDest = (Integer)mDest[0][0];
 				connect.executeUpdate("insert into visits values('"+user.getUserName()+ "',"+maxDest+", null,null,CURRENT_TIMESTAMP");
 			}
 		} catch (SQLException e) {
 			//create new destination ID and save destinations, brug stored procedures til at finde det nste destID
 			
 		}
 	}
 	/*
 	 * gets post from user
 	 */
 	private String getPost(){
 		String post = bound.promptForString("Write your post: ");
 		return post;
 	}
 	/*
 	 * Pulls list of friends from DB, saves it in the 2 dimensional friend array
 	 */
 	private void getFriends(){
 		String uName = user.getUserName();
 		try {
 			Object[][] friendArray = connect.executeQuery("CALL Create_Friendlist('"+uName+"');");
 			parseToArraylist(friendArray);
 		} catch (SQLException e) {
 			
 			e.printStackTrace();
 		}
 	}
 	/*
 	 * Parsing the two dimensional array to a Arraylist of friends.
 	 */
 	private void parseToArraylist(Object[][] friendArray){
 		for(int i = 0; i < friendArray.length;i++){
			friends.add((String)friendArray[i][0]);
			friendArrayList.add((String)friendArray[i][0]);
			
 		}
 	}
 	/*
 	 * Prints the next ten destinations in the visits table
 	 */
 	private void showTenDest(int index, Object[][] visits){
 		int limit;
 		if(index+10 > visits.length)
 			limit= visits.length;
 		else
 			limit = index +10;
 		for(int i = index; i < limit;i++){
 			for(int j = 0; j < visits[0].length; j++){
 				System.out.print(visits[i][j]+", ");
 			}
 			System.out.println();
 		}
 		menu();
 	}
 	
 	private void addFriend(){
 		bound.printLine("Add Friend");
 		String newFriend = bound.promptForString("Enter the username of the friend you want to add: ");
 		if(!isNameAvailable(newFriend)){
 			try {
 				connect.executeUpdate("INSERT INTO userRelations VALUES('"+user.getUserName()+"','"+newFriend+"')");
 				friendArrayList.add(newFriend);
 				friends.add(newFriend);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private boolean isNameAvailable(String userName){
 		Object[][] nameAvailability;
 		try {
 			nameAvailability = connect.executeQuery("SELECT userName FROM users WHERE userName = '" + userName+"'");
 			try{
 				if(nameAvailability[0].length>0)
 					return false;
 			}catch(ArrayIndexOutOfBoundsException e){
 				return true;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 
 }
