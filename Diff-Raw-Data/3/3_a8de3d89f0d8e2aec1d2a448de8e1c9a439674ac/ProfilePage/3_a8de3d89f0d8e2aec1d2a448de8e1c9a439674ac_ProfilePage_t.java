 package src;
 
 import java.util.*;
 import java.io.*;
 
 import networking.OnlineProfile;
 import networking.PlayerLogin;
 
 public class ProfilePage {
 
 	// Fields
 	public static Scanner sc;
 	
 	private static String Username;
 	private String Email;
 	
 	private boolean infoSet; //if the players username and email
 	
 	// Constructor: Scanner to read user input
 	public ProfilePage() {
 		sc = new Scanner(System.in);
 	}
 
 	// prompts user if the have an account or not
 	// if they are, the are directed to createProfile();
 	// else they are directed to enterProfile();
 	public void promptPage() throws FileNotFoundException {
 		PlayerLogin pl = new PlayerLogin(this, sc);
 		infoSet = false;
 		pl.login();
 		infoSet = pl.setInfo();
 		if(infoSet){
 			inProfile(Username);
 		}
 	}
 
 	// accessed once user is logged in, also after each round of gameplay
 	// accesses User class to access information about the user
 	// prompts user if they want to keep playing the game or if they want out
 	//
 	public void inProfile(String u) throws FileNotFoundException {
 		User user = new User(u); // USER CLASS FOR TARGET USER
 		System.out.println("\n<--------------------------------------------->");
 		System.out.println(user.Username + "'s Profile");
 		System.out.println("Level: " + user.getLevel());
 		System.out.println("Points: " + user.getPoints());
 		System.out.println("Questions answered Correct: " + user.getCorrect());
 		System.out.println("Questions answered Incorrect: "
 				+ user.getIncorrect());
 		System.out.println("Total Questions Attempted: "
 				+ user.getTotalQuestions());
 		System.out.println("<--------------------------------------------->");
 		System.out.print("\nWould you like to play or exit? (play/exit) ");
 		String yn = sc.nextLine().toLowerCase();
 		while (!yn.equals("play") && !yn.equals("p") && !yn.equals("exit")
 				&& !yn.equals("e")) {
 			System.out.println(yn + " is incorrect input, try again....");
 			System.out.print("Play or Exit? (play/exit) ");
 			yn = sc.nextLine().toLowerCase();
 		}
 		
 		// let the games begin!
 		if (yn.equals("play") || yn.equals("p")) {
 			System.out.print("Online? (y/n): ");
 			// force valid input
			yn = sc.next();
			while (!yn.equals("y") && !yn.equals("n")) {
 				System.out.print("(y/n): ");
 				yn = sc.next();
 			}
 			// if user wants to play online
 			if (yn.equals("y"))
 				new OnlineProfile(user);
 			else
 				Chatter.play(user.getLevel(), user);
 		} else if (yn.equals("exit") || yn.equals("e")) {
 			user.userOnline(false);
 			System.exit(0);
 		}
 	}
 
 	//username setter
 	public static void setUsername(String username) {
 		Username = username;
 	}
 
 	//username getter
 	public static String getUsername() {
 		return Username;
 	}
 
 	//email setter
 	public void setEmail(String email) {
 		Email = email;
 	}
 
 	//email getter
 	public String getEmail() {
 		return Email;
 	}
 }
