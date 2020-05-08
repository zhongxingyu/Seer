 import java.io.*;
 import java.util.*;
 
 public class Backend
 {
 
 	static final String USERS_DB = "users.csv";
 	static final String PROFILES_DB = "info.csv";
 	static public String email = "";
 
 	static void userMenu() {
 
 		System.out.print("Are you a new user (y / n)?: ");
 
 		Scanner scanner = new Scanner(System.in);
 
 		String input = scanner.nextLine();		
 
 		if (input.equals("y")){
 
 			String firstName = askForStringInput("Please enter your first name: ");
 
 			boolean nameIsValid = validateName(firstName);
 
 			while (!nameIsValid){
 
 				firstName = askForStringInput("Oops you made a mistake, please re-enter first your name:");
 				nameIsValid = validateName(firstName);
 			}
 
 			String lastName  = askForStringInput("Please enter your last name: ");
 
 			nameIsValid = validateName(lastName);
 
 			while (!nameIsValid){
 
 				lastName = askForStringInput("Oops you made a mistake, please re-enter last your name:");
 				nameIsValid = validateName(lastName);
 			}
 
 			email = askForStringInput("Please enter your email address: ");
 
 			boolean emailIsValid = validateEmail(email);
 
 			String [] userInfo = readInputFile(USERS_DB);
 
 			while ( !emailIsValid || checkExistingUser(email, userInfo) ) {
 				if (checkExistingUser(email, userInfo)){
 					email = askForStringInput("Oops email already taken, please enter another email address: ");
 				} else {
 					email = askForStringInput("Oops email is not valid, please re-try: ");
 				}	
 				emailIsValid = validateEmail(email);
 			}
 
 
 			String password = askForPassword("Please enter your password: ");	    	
 			String pswrdConfirm = askForPassword("Please confirm your password: ");	    	
 
 			boolean isPassEqual = validatePassword(password, pswrdConfirm);
 
 			while (!isPassEqual) {
 
 				System.out.println("Passwords did not match, please enter them again.");
 
 				password = askForPassword("Please enter your password: ");	    	
 				pswrdConfirm = askForPassword("Please confirm your password: ");
 
 				isPassEqual = validatePassword(password, pswrdConfirm);
 			}
 
 
 			String storeUserResult = storeUser(email, password, firstName, lastName);
 
 			outputInfo(storeUserResult, USERS_DB); //Store User Info
 
 			System.out.println("*-----------------------------*");
 			System.out.println("Account Created Successfully.");
 			System.out.println("*-----------------------------*");	    	
 			System.out.println("Please setup your profile...");
 												
 			outputInfo(storeUserInfo(), PROFILES_DB); //Store User Profile Info.
 
 
 		} else if (input.equals("n")) {
 
 			String email  = askForStringInput("Please enter your email address: ");
 			String password  = askForPassword("Please enter your password: ");	    		    	
 
 			//Check for existing user.
 
 			String [] userInfo = readInputFile(USERS_DB);
 
 			boolean loginStatus = userLogIn(email, password, userInfo);
 
 			if (loginStatus){
 				
 				System.out.println("You are logged in.");
 
 				printUserProfile(PROFILES_DB, email);
 
 			} else {
 				System.out.println("Username or password did not match.");
 			}
 			
 		} else {
 
 			System.out.println("Please enter 'y' or 'n' ");
 		}
 
 
 	}
 
 	static String [] readInputFile (String filename) { 
 
 		String [] records = new String [5]; 
 
 		File userDB = new File(filename);
 
 		String content = "";
 
 		try{
 			Scanner textScanner = new Scanner(userDB);
 
 			while (textScanner.hasNextLine()){
 
 				content+=textScanner.nextLine();
 
 			}
 		} catch (FileNotFoundException e) {
			e.printStackTrace();
 		}
 
 		records = content.split(";");
 
 		return records; 
 
 	}
 
 	static void outputInfo (String record, String filename) {
 
 		PrintWriter out = null;
 
 		try {
 			out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
 			out.write(record);
 		} catch (IOException e) {
 			System.out.println("Oops something went wrong, could not save your data.");
 		} finally {
 			if (out != null){
 				out.close();
 			}
 		}
 
 	}	
 
 	static boolean checkExistingUser (String email, String [] userInfo) { 
 
 		String [] records = new String[100];
 
 		for (int i=0; i < userInfo.length; i++){
 
 			records = userInfo[i].split(",");
 
 			if (email.equals(records[0])){
 				return true;
 			} 
 		}
 
 		return false; 
 	}
 
 	static boolean validatePassword (String password1, String password2) { 
 
 		if (password1.equals(password2)){
 			return true;
 		}
 
 		return false;
 	}
 
 	 static String encryptPassword (String password){
 
 		String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
 
 		return hashed;
 
 	 }
 
 	 static boolean comparePasswords (String candidate, String hashed){
 
 		if (BCrypt.checkpw(candidate, hashed)){
 			return true;
 		} else {
 			return false;
 		}
 
 	}	 	
 
 	//Validation-----------------------
 	static boolean validateEmail (String email) { 
 
 		if (email.matches("^[a-z\\.]+[a-z]+@[a-z]+(\\.[a-z]{2,3})$"))
 		{
 			return true;
 		}
 
 		return false; 
 	}
 
 	static boolean validateName (String name){
 
 		if (name.matches("[a-zA-Z]+")){
 			return true;
 		} 
 
 		return  false;
 	}
 
 	static boolean validateTwitterHandle(String tHandle){
 
 		if (tHandle.matches("^@[a-zA-Z0-9_\\.]+")){
 			return true;
 		} 
 
 		return  false;
 	}
 
 	static boolean validateGithubUserName(String ghUsername){
 
 		if (ghUsername.matches("[a-zA-Z0-9_]+")){
 			return true;
 		} 
 
 		return  false;
 	}
 
 	static boolean validateURL(String url){
 
 		if (url.matches("^(http://){1}([w]{3}\\.)?[a-z]+(\\.[a-z]{2,3})$")){
 			return true;
 		} 
 
 		return  false;
 	}
 
 	static boolean validateNameWithSpaces(String input){
 
 		if (input.matches("^[\\w\\.\\s_]+[\\w\\.]+$")){
 			return true;
 		} 
 
 		return  false;		
 	}
 
 	static boolean validatePhoneNumber(String phoneNumber){
 
 		if (phoneNumber.matches("^[0-9]{3}-[0-9]{3}-[0-9]{4}$")){
 			return true;
 		} 
 
 		return  false;
 	}		
 
 	static String storeUser(String email, String password, String firstName, String lastName) {
 
 		password = encryptPassword(password);
 
 		return email+","+password+","+firstName+","+lastName+";"; 
 
 	}
 
 	//this method asks the user for profile info, validates it, and then stores it in a comma separated String
 	static String storeUserInfo() {
 			
 			 String twitterHandle = askForStringInput("Please enter your twitter handle (don't forget the '@'): ");
 
 			 boolean inputIsValid = validateTwitterHandle(twitterHandle);
 
 			 while (!inputIsValid){
 
 				twitterHandle = askForStringInput("Oops you made a mistake, please re-enter first your handle: ");
 				inputIsValid = validateTwitterHandle(twitterHandle);
 			 }	
 
 			 String ghUsername = askForStringInput("Please enter your GitHub username: ");
 
 			 inputIsValid = validateGithubUserName(ghUsername);
 
 			 while (!inputIsValid){
 
 				ghUsername = askForStringInput("Oops you made a mistake, please re-enter your GitHub username: ");
 				inputIsValid = validateGithubUserName(ghUsername);
 			 }
 
 			String websiteName = askForStringInput("Please enter your website address: ");
 
 			inputIsValid = validateURL(websiteName);
 
 			while (!inputIsValid){
 
 				websiteName = askForStringInput("Oops you made a mistake, please re-enter website address: ");
 				inputIsValid = validateURL(websiteName);
 			}
 
 			String universityName = askForStringInput("Please enter your university name: ");
 
 			inputIsValid = validateNameWithSpaces(universityName);
 
 			while (!inputIsValid){
 
 				universityName = askForStringInput("Oops you made a mistake, please re-enter your university name: ");
 				inputIsValid = validateNameWithSpaces(universityName);
 			}
 
 			String majorName = askForStringInput("Please enter your major name: ");
 
 			inputIsValid = validateNameWithSpaces(majorName);
 
 			while (!inputIsValid){
 
 				majorName = askForStringInput("Oops you made a mistake, please re-enter your major name: ");
 				inputIsValid = validateNameWithSpaces(majorName);
 			}
 
 			String currentCompany = askForStringInput("Please enter your current company: ");
 
 			inputIsValid = validateName(currentCompany);
 
 			while (!inputIsValid){
 
 				currentCompany = askForStringInput("Oops you made a mistake, please re-enter current company: ");
 				inputIsValid = validateNameWithSpaces(currentCompany);
 			}
 
 			String currentTitle = askForStringInput("Please enter your current title: ");
 
 			inputIsValid = validateNameWithSpaces(currentTitle);
 
 			while (!inputIsValid){
 
 				currentTitle = askForStringInput("Oops you made a mistake, please re-enter your current title: ");
 				inputIsValid = validateNameWithSpaces(currentTitle);
 			}
 
 			String phoneNumber = askForStringInput("Please enter your phone number (use format: ###-###-####): ");
 
 			inputIsValid = validatePhoneNumber(phoneNumber);
 
 			while (!inputIsValid){
 
 				phoneNumber = askForStringInput("Oops you made a mistake, please re-enter a valid phone number: ");
 				inputIsValid = validatePhoneNumber(phoneNumber);
 			}	
 
 			String result = email+",twitter: "+twitterHandle+",";
 
 			result += "Github: "+ghUsername+",Website: "+websiteName+",";
 
 			result += "University: "+universityName+",Major: "+majorName+",Company: "+currentCompany+",";
 			
 			result += "Title: "+currentTitle+",Phone: "+phoneNumber+";";
 
 		 return result; 
 	}
 
 	//this method takes user provided email, password, and user info and checks it against the data from the user.csv file
 	static boolean userLogIn (String email, String password, String [] userInfo) { 
 
 		String [] record = new String[100];
 
 		for (int i=0; i < userInfo.length; i++){
 
 			record = userInfo[i].split(",");
 
 			if ( email.equals(record[0]) &&  comparePasswords(password, record[1]) ){
 				return true;
 			} 
 
 		}
 
 		return false; 
 	}
 
 	//this method takes a user provided email and the info.csv file object, pulls the profile info of the user with the provided email
 	static void printUserProfile (String filename, String email) {
 
 		String [] records = readInputFile(filename);
 
 		String [] userInfo = new String[100];
 
 		for (int i=0; i < records.length; i++){
 
 			userInfo = records[i].split(",");
 
 			if (email.equals(userInfo[0])){
 			
 				for (int j=1; j < userInfo.length; j++ ){
 					System.out.println(userInfo[j]);
 				}
 
 			} 
 		}
 	}
 
 	static String askForStringInput(String message){
 
 		Scanner scanner = new Scanner(System.in);
 
 		System.out.print(message);
 
 		String input = scanner.nextLine();
 
 		return input;
 
 	}
 
 	static String capitalizeWord(String word){
 
 		if (word.length() > 1){
 			return word.substring(0, 1).toUpperCase() + word.substring(1);			
 		}
 
 		return word;
 	}
 
 	static public String askForPassword(String message) {  
 
 		Console console = System.console();
 		
 		if (console == null) {
 			System.out.println("Couldn't get Console instance");
 			System.exit(0);
 		}
 
 		char passwordArray[] = console.readPassword(message);
 
 		return new String(passwordArray);
 
 	}
 
 }
 
