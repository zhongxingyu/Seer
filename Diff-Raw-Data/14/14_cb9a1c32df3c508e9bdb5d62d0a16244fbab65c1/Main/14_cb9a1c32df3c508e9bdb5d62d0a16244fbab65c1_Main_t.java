 package com.db.ncsu;
 
 import java.util.Scanner;
 
 import com.db.database.DatabaseManager;
 import com.db.ncsu.command.Command;
 import com.db.ncsu.user.User;
 import com.db.ncsu.view.CommandView;
 
 public class Main {
 
 
 	public static int userId = 1;
 	public static int userStoreId = 1;
 	public static String department = "";
 
 	public static void main(String args[])
 	{
 		System.out.println("Enter your Id");
 
 
 
 		Scanner scanner = new Scanner(System.in);
 		String id = scanner.nextLine();
 
 
 		// Functions Lookup the type of user - Present a sales user		
 		User user = new User();
 
 		//Look up id, set userId, setstoreId, bring back department
 		department = DatabaseManager.lookUpUser(id);
 
 		Command[] userCommands = null;
 
 		if (department.equals("Franchise Manager"))
 		{
 			userCommands = user.getFranchiseCommands();  
 		}
 		else if (department.equals("Manager"))
 		{
 			userCommands = user.managerCommands();
 		}
 		else if (department.equals("Billing"))
 		{
 			userCommands = user.billingCommands();
 		}
 		else if (department.equals("Sales"))
 		{
 			userCommands = user.getSalesCommands();
 		}
 		else if (department.equals("Stocking"))
 		{
 			userCommands = user.stockingCommands();
 		}
 		else{
 			System.out.println("User does not have a valid account type. Please contact database administrator.");
 			return;
 		}
 
 
 		while (true)
 		{
			Scanner scanner2 = new Scanner(System.in);			
 			System.out.println("**** "+department + " Commands ****");
 			System.out.println("Enter a command");
 
 			int i = 1;
 			for (Command command : userCommands)
 			{
 				System.out.println(i + " "+ command.getCommandName());
 				i++;
 			}
			String chosenCommand = scanner2.nextLine();
 			int choosenCommand = -1;
 			try{
 				choosenCommand = Integer.parseInt(chosenCommand);
 			} catch (NumberFormatException e) {
 				System.out.println("Incorrect input. Please enter the number of an option.");
 				continue;
 			}
 			if(choosenCommand <= userCommands.length && choosenCommand > 0){
 				Command commandToExecute = userCommands[choosenCommand-1];
 				CommandView commandView = new CommandView();
 				commandView.enterParametersAndExeute(commandToExecute);
 			}
 			else{
 				System.out.println("Option number does not exist. Please try again.");
 			}
 
 		}
 	}
 
 
 }
