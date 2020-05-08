 import java.util.Scanner;
 
 public class ATM {
 
 
 	Scanner a = new Scanner(System.in);
 
 
 	//state
 	private User active;
 	private User account1 = new User("Bob", "1234", 100.00);
 	private User account2 = new User("Alice", "3333", 250.00);
 	private User account3 = new User("JT", "0001", 999999999.00);
	private User[] users = {banker1, banker2, banker3};
 	//constructor
 	public ATM() {
 
 		login();
 		commands();
 	}
 
 
 	//behavior
 	public void login() {
 
 		String name;
 		String p;
 
 		int index = -1;
 		while(index == -1) {
 
 			System.out.print("Name: ");
 
 			name = a.nextLine();
 			System.out.println();
 
 			for (int i = 0; i < users.length; i++) {
 				if (name.equals(users[i].name())) {
 					index = i;
 				}
 			}
 
 			if (index == -1) {
 				System.out.println("Invalid name");
 			} else {
 				System.out.print("PIN: ");
 				p = a.nextLine();
 				if (p.equals(users[index].PIN())) {
 					System.out.println("Login successful");
 				} else {
 					System.out.println("Login failed");
 					index = -1;
 				}
 			}
 		}
 		this.active = users[index];
 
 	}
 
 	public void commands() {
 
 		System.out.println("Welcome " + active.name());
 		System.out.print("Type 1 to check balance, 2 to withdraw funds, 3 to change name or PIN, or 4 to logout: ");
 		String input = a.nextLine();
 		System.out.println();
 		if (input.equals("1")) {
 			checkBalance();
 		} else if (input.equals("2")) {
 			withdraw();
 		} else if (input.equals("3")) {
 			change();
 		} else if (input.equals("4")) {
 			logout();
 		} else {
 			System.out.println("Unknown command");
 			commands();
 		}
 	}
 
 	public void withDraw() {
 		System.out.print("Amount to withDraw: ");
 		int w = a.nextInt();
 		System.out.println();
 		active.setBalance(active.balance()- w);
 		commands();
 	}
 
 
 
 
 	public void checkBalance() {
 		System.out.println("Current balance is: " + active.balance());
 		commands();
 	}
 
 	public void logout() {
 		active = blank;
 		System.out.println("Log out worked");
 
 	}
 
 	
 	public void change() {
 
 		System.out.print("Type 1 to change name, 2 to change PIN: ");
 		String input = a.nextLine();
 
 		if (input.equals("1")) {
 			System.out.println("New name: ");
 			String newname = a.nextLine();
 			System.out.println();
 			active.setName(newname);
 
 		} else if (input.equals("2")) {
 			System.out.println("New PIN: ");
 			String newPIN = a.nextLine();
 			System.out.println();
 			active.setPIN(newPIN);
 
 		} else {
 			System.out.println("Error");
 
 		}
 		commands();
 	}
 
 
 }
